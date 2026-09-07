// Cookbook — a collection of recipes. Ported from Cookbook.kt; field names
// match the Kotlin app for Drive-format compatibility.

export interface Cookbook {
  id: string
  name: string
  description: string | null
  ownerDeviceId: string
  isShared: boolean
  sharingLink: string | null
  imageUri: string | null
  createdAt: string // ISO
  updatedAt: string // ISO
  recipeIds: string[]
}

export function createCookbook(
  name: string,
  ownerDeviceId: string,
  description: string | null = null,
  recipeIds: string[] = [],
): Cookbook {
  const now = new Date().toISOString()
  return {
    id: crypto.randomUUID(),
    name,
    description,
    ownerDeviceId,
    isShared: false,
    sharingLink: null,
    imageUri: null,
    createdAt: now,
    updatedAt: now,
    recipeIds,
  }
}

export function withAddedRecipe(cb: Cookbook, recipeId: string): Cookbook {
  return {
    ...cb,
    recipeIds: cb.recipeIds.includes(recipeId) ? cb.recipeIds : [...cb.recipeIds, recipeId],
    updatedAt: new Date().toISOString(),
  }
}

export function withRemovedRecipe(cb: Cookbook, recipeId: string): Cookbook {
  return {
    ...cb,
    recipeIds: cb.recipeIds.filter((id) => id !== recipeId),
    updatedAt: new Date().toISOString(),
  }
}
