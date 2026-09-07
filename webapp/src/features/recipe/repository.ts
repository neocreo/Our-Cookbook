// Recipe repository — the single access point for recipe data.
// Backed by RxDB when available; falls back to an in-memory session store if
// IndexedDB initialization fails. Storage state lives in lib/store.ts so the
// cookbook repository (a separate bounded context) shares the same database
// without depending on this module.
//
// Auto-cookbook: when the user saves their first recipe and no cookbook
// exists, "Mina recept" is created automatically and the recipe is added to it.

import type { Recipe, RecipeInput } from '../../types/recipe'
import type { Cookbook } from '../../types/cookbook'
import { createRecipe, withRecipeUpdate } from '../../types/recipe'
import { withRemovedRecipe, createCookbook } from '../../types/cookbook'
import { initStore, getMode, getDb, getMemRecipes, getMemCookbooks } from '../../lib/store'

type RecipeListener = (recipes: Recipe[]) => void

const recipeListeners = new Set<RecipeListener>()

function sortedRecipes(): Recipe[] {
  return [...getMemRecipes().values()].sort((a, b) => b.updatedAt.localeCompare(a.updatedAt))
}

function notifyRecipes(): void {
  const list = sortedRecipes()
  for (const l of recipeListeners) l(list)
}

export async function initRepository(deviceId: string): Promise<void> {
  await initStore(deviceId)
}

export function subscribeRecipes(cb: RecipeListener): () => void {
  if (getMode() === 'rxdb' && getDb()) {
    const sub = getDb()!.recipes.find().$.subscribe((docs) => {
      const list = docs
        .map((d) => d.toMutableJSON())
        .sort((a, b) => b.updatedAt.localeCompare(a.updatedAt))
      cb(list)
    })
    return () => sub.unsubscribe()
  }
  recipeListeners.add(cb)
  cb(sortedRecipes())
  return () => recipeListeners.delete(cb)
}

export async function getRecipe(id: string): Promise<Recipe | null> {
  if (getMode() === 'rxdb' && getDb()) {
    const doc = await getDb()!.recipes.findOne(id).exec()
    return doc ? doc.toMutableJSON() : null
  }
  return getMemRecipes().get(id) ?? null
}

export async function saveRecipe(
  recipe: Recipe | RecipeInput,
  deviceId: string,
): Promise<Recipe> {
  const isExisting = (r: Recipe | RecipeInput): r is Recipe =>
    typeof (r as Recipe).id === 'string' && (r as Recipe).createdAt !== undefined

  if (isExisting(recipe)) {
    const updated = withRecipeUpdate(recipe, deviceId)
    if (getMode() === 'rxdb' && getDb()) {
      const doc = await getDb()!.recipes.findOne(recipe.id).exec()
      if (doc) {
        await doc.incrementalModify((d) => ({ ...d, ...updated }))
      } else {
        await getDb()!.recipes.insert(updated)
      }
    } else {
      getMemRecipes().set(updated.id, updated)
      notifyRecipes()
    }
    return updated
  }

  const created = createRecipe(recipe, deviceId)
  if (getMode() === 'rxdb' && getDb()) {
    await getDb()!.recipes.insert(created)
    await ensureDefaultCookbook(deviceId, created.id)
  } else {
    getMemRecipes().set(created.id, created)
    await ensureDefaultCookbook(deviceId, created.id)
    notifyRecipes()
  }
  return created
}

export async function deleteRecipe(id: string): Promise<void> {
  if (getMode() === 'rxdb' && getDb()) {
    const doc = await getDb()!.recipes.findOne(id).exec()
    if (doc) await doc.remove()
    await removeFromCookbooks(id)
  } else {
    getMemRecipes().delete(id)
    removeFromCookbooks(id)
    notifyRecipes()
  }
}

export async function toggleFavorite(id: string, deviceId: string): Promise<void> {
  const recipe = await getRecipe(id)
  if (!recipe) return
  const updated: Recipe = { ...recipe, isFavorite: !recipe.isFavorite }
  await saveRecipe(updated, deviceId)
}

export function getRepositoryMode() {
  return getMode()
}

// --- search + filter helpers (LIKE-based, same as the Kotlin app) ---

export function searchRecipes(recipes: Recipe[], query: string): Recipe[] {
  const q = query.trim().toLowerCase()
  if (!q) return recipes
  return recipes.filter((r) => {
    if (r.title.toLowerCase().includes(q)) return true
    if (r.category.toLowerCase().includes(q)) return true
    if (r.description?.toLowerCase().includes(q)) return true
    if (r.tags.some((t) => t.toLowerCase().includes(q))) return true
    if (r.ingredients.some((i) => i.name.toLowerCase().includes(q))) return true
    return false
  })
}

export function allCategories(recipes: Recipe[]): string[] {
  const set = new Set<string>()
  for (const r of recipes) if (r.category) set.add(r.category)
  return [...set].sort()
}

export function allTags(recipes: Recipe[]): string[] {
  const set = new Set<string>()
  for (const r of recipes) for (const t of r.tags) set.add(t)
  return [...set].sort()
}

// --- cookbook coupling (auto-cookbook + cleanup on delete) ---

async function ensureDefaultCookbook(deviceId: string, recipeId: string): Promise<void> {
  const empty = getMode() !== 'rxdb' ? getMemCookbooks().size === 0 : await cookbooksCount() === 0
  if (!empty) return
  const cookbook = createCookbook('Mina recept', deviceId, null, [recipeId])
  if (getMode() === 'rxdb' && getDb()) {
    await getDb()!.cookbooks.insert(cookbook)
  } else {
    getMemCookbooks().set(cookbook.id, cookbook)
  }
}

async function cookbooksCount(): Promise<number> {
  if (getMode() === 'rxdb' && getDb()) return await getDb()!.cookbooks.count().exec()
  return getMemCookbooks().size
}

async function removeFromCookbooks(recipeId: string): Promise<void> {
  if (getMode() === 'rxdb' && getDb()) {
    const docs = await getDb()!.cookbooks.find().exec()
    const affected = docs.filter((d) => (d.toMutableJSON().recipeIds ?? []).includes(recipeId))
    await Promise.all(
      affected.map((doc) =>
        doc.incrementalModify((d) => withRemovedRecipe(d as Cookbook, recipeId)),
      ),
    )
  } else {
    for (const [id, cb] of getMemCookbooks()) {
      getMemCookbooks().set(id, withRemovedRecipe(cb, recipeId))
    }
  }
}
