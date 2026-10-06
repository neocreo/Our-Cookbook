// Cookbook repository — the single access point for cookbook data. A separate
// bounded context from recipes; shares the storage layer via lib/store.ts.
//
// Cookbooks are collections of recipe IDs. Creating, renaming, adding/removing
// recipes, and deleting cookbooks all go through here.

import type { Cookbook } from '../../types/cookbook'
import { createCookbook, withAddedRecipe, withRemovedRecipe } from '../../types/cookbook'
import { getMode, getDb, getMemCookbooks } from '../../lib/store'
import { enqueueSync } from '../sync/repository'

type CookbookListener = (cookbooks: Cookbook[]) => void

/** The default cookbook every unfiled recipe lands in. */
const DEFAULT_COOKBOOK_NAME = 'Mina recept'

const cookbookListeners = new Set<CookbookListener>()

function sortedCookbooks(): Cookbook[] {
  return [...getMemCookbooks().values()].sort((a, b) => a.name.localeCompare(b.name))
}

function notifyCookbooks(): void {
  const list = sortedCookbooks()
  for (const l of cookbookListeners) l(list)
}

export function subscribeCookbooks(cb: CookbookListener): () => void {
  if (getMode() === 'rxdb' && getDb()) {
    const sub = getDb()!.cookbooks.find().$.subscribe((docs) => {
      const list = docs
        .map((d) => d.toMutableJSON())
        .sort((a, b) => a.name.localeCompare(b.name))
      cb(list)
    })
    return () => sub.unsubscribe()
  }
  cookbookListeners.add(cb)
  cb(sortedCookbooks())
  return () => cookbookListeners.delete(cb)
}

export async function getCookbook(id: string): Promise<Cookbook | null> {
  if (getMode() === 'rxdb' && getDb()) {
    const doc = await getDb()!.cookbooks.findOne(id).exec()
    return doc ? doc.toMutableJSON() : null
  }
  return getMemCookbooks().get(id) ?? null
}

export async function createCookbookNamed(
  name: string,
  deviceId: string,
  description: string | null = null,
  ownerName: string | null = null,
): Promise<Cookbook> {
  const cookbook = createCookbook(name, deviceId, description, [], ownerName)
  if (getMode() === 'rxdb' && getDb()) {
    await getDb()!.cookbooks.insert(cookbook)
  } else {
    getMemCookbooks().set(cookbook.id, cookbook)
    notifyCookbooks()
  }
  enqueueSync('CREATE', 'COOKBOOK', cookbook.id, JSON.stringify(cookbook), deviceId)
  return cookbook
}

/** Store a cookbook pulled from Drive as-is — never re-enqueued. */
export async function saveCookbookFromRemote(cookbook: Cookbook): Promise<void> {
  if (getMode() === 'rxdb' && getDb()) {
    const doc = await getDb()!.cookbooks.findOne(cookbook.id).exec()
    if (doc) {
      await doc.incrementalModify(() => cookbook)
    } else {
      await getDb()!.cookbooks.insert(cookbook)
    }
  } else {
    getMemCookbooks().set(cookbook.id, cookbook)
    notifyCookbooks()
  }
}

async function enqueueCookbookUpdate(cookbookId: string, deviceId: string): Promise<void> {
  const cb = await getCookbook(cookbookId)
  if (cb) enqueueSync('UPDATE', 'COOKBOOK', cookbookId, JSON.stringify(cb), deviceId)
}

export async function renameCookbook(
  id: string,
  name: string,
  description: string | null = null,
  deviceId?: string,
): Promise<void> {
  if (getMode() === 'rxdb' && getDb()) {
    const doc = await getDb()!.cookbooks.findOne(id).exec()
    if (doc) await doc.incrementalModify((d) => ({ ...d, name, description, updatedAt: new Date().toISOString() }))
  } else {
    const cb = getMemCookbooks().get(id)
    if (cb) {
      getMemCookbooks().set(id, { ...cb, name, description, updatedAt: new Date().toISOString() })
      notifyCookbooks()
    }
  }
  if (deviceId) await enqueueCookbookUpdate(id, deviceId)
}

export async function addRecipeToCookbook(
  cookbookId: string,
  recipeId: string,
  deviceId?: string,
): Promise<void> {
  if (getMode() === 'rxdb' && getDb()) {
    const doc = await getDb()!.cookbooks.findOne(cookbookId).exec()
    if (doc) await doc.incrementalModify((d) => withAddedRecipe(d as Cookbook, recipeId))
  } else {
    const cb = getMemCookbooks().get(cookbookId)
    if (cb) {
      getMemCookbooks().set(cookbookId, withAddedRecipe(cb, recipeId))
      notifyCookbooks()
    }
  }
  if (deviceId) await enqueueCookbookUpdate(cookbookId, deviceId)
}

export async function removeRecipeFromCookbook(
  cookbookId: string,
  recipeId: string,
  deviceId?: string,
): Promise<void> {
  if (getMode() === 'rxdb' && getDb()) {
    const doc = await getDb()!.cookbooks.findOne(cookbookId).exec()
    if (doc) await doc.incrementalModify((d) => withRemovedRecipe(d as Cookbook, recipeId))
  } else {
    const cb = getMemCookbooks().get(cookbookId)
    if (cb) {
      getMemCookbooks().set(cookbookId, withRemovedRecipe(cb, recipeId))
      notifyCookbooks()
    }
  }
  if (deviceId) await enqueueCookbookUpdate(cookbookId, deviceId)
}

export async function deleteCookbook(id: string, deviceId?: string): Promise<void> {
  if (getMode() === 'rxdb' && getDb()) {
    const doc = await getDb()!.cookbooks.findOne(id).exec()
    if (doc) await doc.remove()
  } else {
    getMemCookbooks().delete(id)
    notifyCookbooks()
  }
  if (deviceId) enqueueSync('DELETE', 'COOKBOOK', id, '', deviceId)
}

/** All cookbooks currently stored. */
export async function listCookbooks(): Promise<Cookbook[]> {
  if (getMode() === 'rxdb' && getDb()) {
    const docs = await getDb()!.cookbooks.find().exec()
    return docs.map((d) => d.toMutableJSON())
  }
  return sortedCookbooks()
}

/**
 * File recipes that belong to no cookbook into the default cookbook
 * ("Mina recept"), creating it if needed — so every recipe is always in a
 * cookbook, on this device and after sync. Returns the number filed.
 */
export async function fileUnfiledRecipes(deviceId: string, recipeIds: string[]): Promise<number> {
  if (recipeIds.length === 0) return 0
  const all = await listCookbooks()
  const target = all.find((cb) => cb.name === DEFAULT_COOKBOOK_NAME)
  let cookbook = target ?? null

  if (!cookbook) {
    cookbook = createCookbook(DEFAULT_COOKBOOK_NAME, deviceId, null, [])
    if (getMode() === 'rxdb' && getDb()) {
      await getDb()!.cookbooks.insert(cookbook)
    } else {
      getMemCookbooks().set(cookbook.id, cookbook)
      notifyCookbooks()
    }
    enqueueSync('CREATE', 'COOKBOOK', cookbook.id, JSON.stringify(cookbook), deviceId)
  }

  const missing = recipeIds.filter((id) => !cookbook!.recipeIds.includes(id))
  if (missing.length === 0) return 0

  const updated = { ...cookbook, recipeIds: [...cookbook.recipeIds, ...missing], updatedAt: new Date().toISOString() }
  if (getMode() === 'rxdb' && getDb()) {
    const doc = await getDb()!.cookbooks.findOne(updated.id).exec()
    if (doc) await doc.incrementalModify(() => updated)
  } else {
    getMemCookbooks().set(updated.id, updated)
    notifyCookbooks()
  }
  enqueueSync('UPDATE', 'COOKBOOK', updated.id, JSON.stringify(updated), deviceId)
  return missing.length
}
