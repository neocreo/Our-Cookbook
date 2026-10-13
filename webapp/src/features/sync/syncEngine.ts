// Sync engine — orchestrates the sync cycle over the Drive folder tree:
//   1. Read the Drive index (rebuilt from the tree when missing)
//   2. Pull remote cookbooks and merge (last write wins, local delete wins)
//   3. Project the merged cookbooks onto folders — one folder per cookbook
//   4. Push pending recipe changes into their cookbook folders
//   5. Pull remote recipes, detect and resolve conflicts
//   6. Save remote-only recipes locally, apply remote deletions
//   7. Auto-file recipes that ended up in no cookbook
//   8. Persist the index and sync metadata
//
// Deletion semantics: a tombstone wins. A recipe deleted on any device is
// deleted everywhere on the next sync; a locally deleted recipe is never
// resurrected by a remote copy. Recipes are never deleted with a cookbook —
// their files move to the Unsorted folder.

import type { Recipe } from '../../types/recipe'
import type { Cookbook } from '../../types/cookbook'
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
import { mergeCookbooks } from './cookbookMerge'
import { primaryCookbookId, recipeFileName } from './filing'

export interface SyncResult {
  pushed: number
  pulled: number
  conflicts: ConflictInfo[]
  errors: string[]
}

/** How the engine applies remote data and filing on this device. */
export interface SyncCallbacks {
  saveRemoteRecipe: (recipe: Recipe) => Promise<void>
  saveRemoteCookbook: (cookbook: Cookbook) => Promise<void>
  deleteRemoteRecipe: (recipeId: string) => Promise<void>
  /** File recipes that belong to no cookbook into the default cookbook. */
  fileUnfiledRecipes: (recipeIds: string[]) => Promise<void>
}

const DEFAULT_STRATEGY: ResolutionStrategy = 'MERGE'

export async function runSync(
  accessToken: string,
  localRecipes: Recipe[],
  localCookbooks: Cookbook[],
  deviceId: string,
  callbacks: SyncCallbacks,
  strategy: ResolutionStrategy = DEFAULT_STRATEGY,
): Promise<SyncResult> {
  const drive = new DriveApiClient(accessToken)
  markSyncStart(deviceId)

  const result: SyncResult = { pushed: 0, pulled: 0, conflicts: [], errors: [] }

  try {
    // 1. Drive index — the id -> file/folder map.
    const index = await drive.getIndex()

    // Split the queue: recipes are pushed per file, cookbooks via the merge.
    const queue = getPendingSyncQueue()
    const cookbookEntryIds: string[] = []
    const locallyDeletedCookbookIds: string[] = []
    for (const entry of queue) {
      if (entry.entityType === 'COOKBOOK') {
        cookbookEntryIds.push(entry.id)
        if (entry.operation === 'DELETE') locallyDeletedCookbookIds.push(entry.entityId)
      }
    }

    // 2. Pull and merge cookbooks.
    const remoteEntries = await drive.pullCookbooks(index)
    for (const { cookbook, folderId } of remoteEntries) {
      if (!index.cookbooks[cookbook.id]) index.cookbooks[cookbook.id] = folderId
    }
    const merge = mergeCookbooks(
      localCookbooks,
      remoteEntries.map((e) => e.cookbook),
      locallyDeletedCookbookIds,
    )

    // 3. Project onto folders — every merged cookbook gets one, deleted
    //    ones lose theirs (their recipe files move to Unsorted).
    await drive.syncCookbookFolders(merge.merged, locallyDeletedCookbookIds, index)
    for (const cb of merge.toSaveLocally) {
      await callbacks.saveRemoteCookbook(cb)
      result.pulled++
    }

    // The effective grouping every filing decision uses.
    const mergedCookbooks = merge.merged
    const folderForRecipe = (recipeId: string): string => {
      const primary = primaryCookbookId(recipeId, mergedCookbooks)
      return (primary && index.cookbooks[primary]) || index.unsortedFolderId || index.rootFolderId
    }

    // 4. Push pending recipe changes.
    for (const entry of queue) {
      if (entry.entityType === 'COOKBOOK') continue
      try {
        if (entry.operation === 'DELETE') {
          await drive.deleteRecipe(entry.entityId, index)
          await drive.addTombstone(entry.entityId, index)
        } else {
          const recipe = JSON.parse(entry.data) as Recipe
          await drive.pushRecipe(recipe, folderForRecipe(recipe.id), recipeFileName(recipe.title), index)
        }
        markSynced(entry.id)
        result.pushed++
      } catch (err) {
        const msg = err instanceof Error ? err.message : 'Unknown error'
        markSyncFailed(entry.id, msg)
        result.errors.push(`Push ${entry.entityId}: ${msg}`)
      }
    }

    // 5. Pull remote recipes and tombstones.
    const remoteRecipesArr = await drive.pullRecipes(index)
    const remoteRecipes = new Map<string, Recipe>()
    for (const r of remoteRecipesArr) remoteRecipes.set(r.id, r)
    const tombstones = new Set(await drive.getTombstones(index))

    // 6. Detect and resolve recipe conflicts. A locally deleted recipe
    //    stays deleted — the pending DELETE retries; never resurrect.
    const conflicts = detectConflicts(localRecipes, remoteRecipes)
    result.conflicts = conflicts
    for (const conflict of conflicts) {
      try {
        if (conflict.conflictType === 'DELETED_LOCAL') continue
        if (conflict.conflictType === 'DELETED_REMOTE') {
          // New local recipe (already pushed above) or deleted on another
          // device — the tombstone handles that below. Never push over one.
          if (!tombstones.has(conflict.recipeId) && conflict.localRecipe) {
            await drive.pushRecipe(
              conflict.localRecipe,
              folderForRecipe(conflict.localRecipe.id),
              recipeFileName(conflict.localRecipe.title),
              index,
            )
          }
          continue
        }
        const resolution = resolveConflict(conflict, strategy)
        await callbacks.saveRemoteRecipe(resolution.recipe)
        await drive.pushRecipe(
          resolution.recipe,
          folderForRecipe(resolution.recipe.id),
          recipeFileName(resolution.recipe.title),
          index,
        )
      } catch (err) {
        const msg = err instanceof Error ? err.message : 'Unknown error'
        result.errors.push(`Conflict ${conflict.recipeId}: ${msg}`)
      }
    }

    // 7. Save remote-only recipes (new from another device).
    const localIds = new Set(localRecipes.map((r) => r.id))
    for (const [id, remote] of remoteRecipes) {
      if (!localIds.has(id) && !tombstones.has(id) && !conflicts.some((c) => c.recipeId === id)) {
        await callbacks.saveRemoteRecipe(remote)
        result.pulled++
      }
    }

    // Apply remote tombstones — delete locally what was deleted elsewhere.
    for (const tombstoneId of tombstones) {
      if (localIds.has(tombstoneId)) {
        await callbacks.deleteRemoteRecipe(tombstoneId)
        result.pulled++
      }
    }

    // 8. Every recipe belongs to a cookbook: file stragglers into the
    //    default cookbook on this device.
    const filed = new Set(mergedCookbooks.flatMap((cb) => cb.recipeIds))
    const unfiled = localRecipes.filter((r) => !filed.has(r.id)).map((r) => r.id)
    if (unfiled.length > 0) {
      await callbacks.fileUnfiledRecipes(unfiled)
    }

    // 9. Persist the index, drain the cookbook queue, done.
    await drive.writeIndex(index)
    for (const entryId of cookbookEntryIds) markSynced(entryId)

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
