// Cookbook repository — the single access point for cookbook data. A separate
// bounded context from recipes; shares the storage layer via lib/store.ts.
//
// Cookbooks are collections of recipe IDs. Creating, renaming, adding/removing
// recipes, and deleting cookbooks all go through here.

import type { Cookbook } from '../../types/cookbook'
import { createCookbook, withAddedRecipe, withRemovedRecipe } from '../../types/cookbook'
import { getMode, getDb, getMemCookbooks } from '../../lib/store'

type CookbookListener = (cookbooks: Cookbook[]) => void

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
): Promise<Cookbook> {
  const cookbook = createCookbook(name, deviceId, description, [])
  if (getMode() === 'rxdb' && getDb()) {
    await getDb()!.cookbooks.insert(cookbook)
  } else {
    getMemCookbooks().set(cookbook.id, cookbook)
    notifyCookbooks()
  }
  return cookbook
}

export async function renameCookbook(id: string, name: string): Promise<void> {
  if (getMode() === 'rxdb' && getDb()) {
    const doc = await getDb()!.cookbooks.findOne(id).exec()
    if (doc) await doc.incrementalModify((d) => ({ ...d, name, updatedAt: new Date().toISOString() }))
  } else {
    const cb = getMemCookbooks().get(id)
    if (cb) {
      getMemCookbooks().set(id, { ...cb, name, updatedAt: new Date().toISOString() })
      notifyCookbooks()
    }
  }
}

export async function addRecipeToCookbook(cookbookId: string, recipeId: string): Promise<void> {
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
}

export async function removeRecipeFromCookbook(
  cookbookId: string,
  recipeId: string,
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
}

export async function deleteCookbook(id: string): Promise<void> {
  if (getMode() === 'rxdb' && getDb()) {
    const doc = await getDb()!.cookbooks.findOne(id).exec()
    if (doc) await doc.remove()
  } else {
    getMemCookbooks().delete(id)
    notifyCookbooks()
  }
}
