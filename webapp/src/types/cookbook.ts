// Cookbook — a collection of recipes. Ported from Cookbook.kt; field names
// match the Kotlin app for Drive-format compatibility.

export interface Cookbook {
  id: string
  name: string
  description: string | null
  ownerDeviceId: string
  ownerName: string | null
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
  ownerName: string | null = null,
): Cookbook {
  const now = new Date().toISOString()
  return {
    id: crypto.randomUUID(),
    name,
    description,
    ownerDeviceId,
    ownerName,
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

// Owner label shown in the UI. Falls back to the current user's name for
// cookbooks created on this device, so the device id is never shown.
export function ownerDisplayName(
  cb: Cookbook,
  currentDeviceId: string,
  currentUserName: string | null,
): string {
  if (cb.ownerName) return cb.ownerName
  if (cb.ownerDeviceId === currentDeviceId) return currentUserName?.trim() || 'You'
  return 'Unknown'
}
