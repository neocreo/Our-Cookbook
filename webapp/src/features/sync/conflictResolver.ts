// Conflict resolver — ported from Kotlin ConflictResolver.kt. Detects and
// resolves conflicts between local and remote recipes using version vectors
// and checksums.
//
// Resolution strategies:
//   KEEP_LOCAL  — local version wins (push local to Drive)
//   KEEP_REMOTE — remote version wins (save remote locally)
//   MERGE       — field-level merge (latest updatedAt wins, tags merged)
//   KEEP_BOTH   — keep remote as a copy with a new ID

import type { Recipe } from '../../types/recipe'
import { createRecipe } from '../../types/recipe'
import { isVersionNewer } from '../../types/versionVector'

export type ConflictType =
  | 'VERSION_MISMATCH'
  | 'CHECKSUM_MISMATCH'
  | 'DELETED_LOCAL'
  | 'DELETED_REMOTE'
  | 'BOTH_MODIFIED'

export type ResolutionStrategy = 'KEEP_LOCAL' | 'KEEP_REMOTE' | 'MERGE' | 'KEEP_BOTH'

export interface ConflictInfo {
  recipeId: string
  localRecipe: Recipe | null
  remoteRecipe: Recipe | null
  conflictType: ConflictType
  localVersion: number
  remoteVersion: number
  localChecksum: string | null
  remoteChecksum: string | null
}

export interface ConflictResolution {
  recipe: Recipe
  strategy: ResolutionStrategy
  message: string
}

/** Detect conflicts between local and remote recipes. */
export function detectConflicts(
  localRecipes: Recipe[],
  remoteRecipes: Map<string, Recipe>,
): ConflictInfo[] {
  const conflicts: ConflictInfo[] = []

  for (const local of localRecipes) {
    const remote = remoteRecipes.get(local.id)
    if (!remote) {
      // Local exists, remote doesn't — either new local or deleted remote
      conflicts.push({
        recipeId: local.id,
        localRecipe: local,
        remoteRecipe: null,
        conflictType: 'DELETED_REMOTE',
        localVersion: local.versionVector.counter,
        remoteVersion: 0,
        localChecksum: local.checksum || null,
        remoteChecksum: null,
      })
      continue
    }
    if (local.checksum && remote.checksum && local.checksum !== remote.checksum) {
      conflicts.push({
        recipeId: local.id,
        localRecipe: local,
        remoteRecipe: remote,
        conflictType: 'CHECKSUM_MISMATCH',
        localVersion: local.versionVector.counter,
        remoteVersion: remote.versionVector.counter,
        localChecksum: local.checksum || null,
        remoteChecksum: remote.checksum || null,
      })
    } else if (local.versionVector.counter !== remote.versionVector.counter) {
      conflicts.push({
        recipeId: local.id,
        localRecipe: local,
        remoteRecipe: remote,
        conflictType: 'VERSION_MISMATCH',
        localVersion: local.versionVector.counter,
        remoteVersion: remote.versionVector.counter,
        localChecksum: local.checksum || null,
        remoteChecksum: remote.checksum || null,
      })
    } else if (local.updatedAt !== remote.updatedAt) {
      conflicts.push({
        recipeId: local.id,
        localRecipe: local,
        remoteRecipe: remote,
        conflictType: 'BOTH_MODIFIED',
        localVersion: local.versionVector.counter,
        remoteVersion: remote.versionVector.counter,
        localChecksum: local.checksum || null,
        remoteChecksum: remote.checksum || null,
      })
    }
  }

  // Remote exists but not local — deleted locally
  for (const [recipeId, remote] of remoteRecipes) {
    if (!localRecipes.some((r) => r.id === recipeId)) {
      conflicts.push({
        recipeId,
        localRecipe: null,
        remoteRecipe: remote,
        conflictType: 'DELETED_LOCAL',
        localVersion: 0,
        remoteVersion: remote.versionVector.counter,
        localChecksum: null,
        remoteChecksum: remote.checksum || null,
      })
    }
  }

  return conflicts
}

/** Resolve a conflict with the given strategy. Returns the winning recipe. */
export function resolveConflict(
  conflict: ConflictInfo,
  strategy: ResolutionStrategy,
): ConflictResolution {
  switch (strategy) {
    case 'KEEP_LOCAL': {
      if (!conflict.localRecipe) throw new Error('No local recipe to keep')
      return { recipe: conflict.localRecipe, strategy, message: 'Kept local version' }
    }
    case 'KEEP_REMOTE': {
      if (!conflict.remoteRecipe) throw new Error('No remote recipe to keep')
      return { recipe: conflict.remoteRecipe, strategy, message: 'Kept remote version' }
    }
    case 'MERGE': {
      if (!conflict.localRecipe || !conflict.remoteRecipe)
        throw new Error('Merge requires both local and remote')
      const merged = mergeRecipes(conflict.localRecipe, conflict.remoteRecipe)
      return { recipe: merged, strategy, message: 'Merged local and remote' }
    }
    case 'KEEP_BOTH': {
      if (!conflict.remoteRecipe) throw new Error('No remote recipe to keep as copy')
      // Keep remote as a new recipe with a fresh ID
      const copy = createRecipe(
        {
          title: `${conflict.remoteRecipe.title} (copy)`,
          category: conflict.remoteRecipe.category,
          description: conflict.remoteRecipe.description,
          ingredients: conflict.remoteRecipe.ingredients,
          instructions: conflict.remoteRecipe.instructions,
          servingSize: conflict.remoteRecipe.servingSize,
          prepTime: conflict.remoteRecipe.prepTime,
          cookTime: conflict.remoteRecipe.cookTime,
          tags: conflict.remoteRecipe.tags,
          source: conflict.remoteRecipe.source,
          notes: conflict.remoteRecipe.notes,
        },
        conflict.remoteRecipe.deviceId,
      )
      return { recipe: copy, strategy, message: 'Kept both — remote saved as copy' }
    }
  }
}

/** Field-level merge: latest updatedAt wins for scalar fields, tags union. */
function mergeRecipes(local: Recipe, remote: Recipe): Recipe {
  const localIsNewer = isVersionNewer(local.versionVector, remote.versionVector)
  return {
    ...local,
    title: localIsNewer ? local.title : remote.title,
    description: localIsNewer ? local.description : remote.description,
    category: localIsNewer ? local.category : remote.category,
    servingSize: localIsNewer ? local.servingSize : remote.servingSize,
    prepTime: localIsNewer ? local.prepTime : remote.prepTime,
    cookTime: localIsNewer ? local.cookTime : remote.cookTime,
    rating: localIsNewer ? local.rating : remote.rating,
    isFavorite: localIsNewer ? local.isFavorite : remote.isFavorite,
    source: localIsNewer ? local.source : remote.source,
    notes: localIsNewer ? local.notes : remote.notes,
    ingredients: localIsNewer ? local.ingredients : remote.ingredients,
    instructions: localIsNewer ? local.instructions : remote.instructions,
    tags: [...new Set([...local.tags, ...remote.tags])],
    updatedAt: local.updatedAt > remote.updatedAt ? local.updatedAt : remote.updatedAt,
  }
}
