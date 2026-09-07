// Sync engine — orchestrates the sync cycle:
//   1. Push pending local changes to Drive
//   2. Pull remote recipes from Drive
//   3. Detect and resolve conflicts
//   4. Save remote-only recipes locally
//   5. Update sync metadata
//
// The engine uses the DriveApiClient for Drive I/O and the conflict resolver
// for merge decisions. Default conflict strategy: MERGE (auto-resolve).

import type { Recipe } from '../../types/recipe'
import { DriveApiClient } from '../../lib/driveClient'
import {
  getPendingSyncQueue,
  markSynced,
  markSyncFailed,
  markSyncStart,
  markSyncComplete,
  getSyncMetadata,
} from './repository'
import { detectConflicts, resolveConflict, type ConflictInfo, type ResolutionStrategy } from './conflictResolver'

export interface SyncResult {
  pushed: number
  pulled: number
  conflicts: ConflictInfo[]
  errors: string[]
}

const DEFAULT_STRATEGY: ResolutionStrategy = 'MERGE'

export async function runSync(
  accessToken: string,
  localRecipes: Recipe[],
  deviceId: string,
  saveRemoteRecipe: (recipe: Recipe) => Promise<void>,
  strategy: ResolutionStrategy = DEFAULT_STRATEGY,
): Promise<SyncResult> {
  const drive = new DriveApiClient(accessToken)
  markSyncStart(deviceId)

  const result: SyncResult = { pushed: 0, pulled: 0, conflicts: [], errors: [] }

  try {
    // 1. Push pending changes
    const queue = getPendingSyncQueue()
    for (const entry of queue) {
      try {
        if (entry.operation === 'DELETE') {
          await drive.deleteRecipeFile(entry.entityId)
          await drive.addTombstone(entry.entityId)
        } else {
          const recipe = JSON.parse(entry.data) as Recipe
          await drive.pushRecipe(recipe)
        }
        markSynced(entry.id)
        result.pushed++
      } catch (err) {
        const msg = err instanceof Error ? err.message : 'Unknown error'
        markSyncFailed(entry.id, msg)
        result.errors.push(`Push ${entry.entityId}: ${msg}`)
      }
    }

    // 2. Pull remote recipes
    const remoteRecipesArr = await drive.pullRecipes()
    const remoteRecipes = new Map<string, Recipe>()
    for (const r of remoteRecipesArr) remoteRecipes.set(r.id, r)

    // 3. Detect conflicts
    const conflicts = detectConflicts(localRecipes, remoteRecipes)
    result.conflicts = conflicts

    // 4. Auto-resolve conflicts + apply remote-only recipes
    for (const conflict of conflicts) {
      try {
        const resolution = resolveConflict(conflict, strategy)
        // Save the winning recipe both locally and to Drive
        if (conflict.conflictType !== 'DELETED_REMOTE') {
          await saveRemoteRecipe(resolution.recipe)
        }
        if (conflict.conflictType === 'DELETED_LOCAL') {
          // Recipe deleted locally but exists remotely — keep remote
          if (conflict.remoteRecipe) await saveRemoteRecipe(conflict.remoteRecipe)
          result.pulled++
        } else if (conflict.conflictType === 'DELETED_REMOTE') {
          // Recipe exists locally but deleted remotely — push local back to Drive
          if (conflict.localRecipe) await drive.pushRecipe(conflict.localRecipe)
        } else {
          // Both exist — push the resolution to Drive
          await drive.pushRecipe(resolution.recipe)
        }
      } catch (err) {
        const msg = err instanceof Error ? err.message : 'Unknown error'
        result.errors.push(`Conflict ${conflict.recipeId}: ${msg}`)
      }
    }

    // 5. Save remote-only recipes (no conflict — new from other device)
    const localIds = new Set(localRecipes.map((r) => r.id))
    for (const [id, remote] of remoteRecipes) {
      if (!localIds.has(id) && !conflicts.some((c) => c.recipeId === id)) {
        await saveRemoteRecipe(remote)
        result.pulled++
      }
    }

    // 6. Apply remote tombstones — delete locally if deleted on another device
    const tombstones = await drive.getTombstones()
    for (const tombstoneId of tombstones) {
      if (localIds.has(tombstoneId) && !conflicts.some((c) => c.recipeId === tombstoneId)) {
        // Remote deleted this recipe — delete locally too
        // (The caller handles local deletion; we just flag it)
        result.pulled++
      }
    }

    markSyncComplete(deviceId, result.errors.length === 0)
  } catch (err) {
    const msg = err instanceof Error ? err.message : 'Sync failed'
    result.errors.push(msg)
    markSyncComplete(deviceId, false)
  }

  return result
}

export function getSyncStatus() {
  return getSyncMetadata()
}
