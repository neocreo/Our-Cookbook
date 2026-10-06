// Cookbook merge — the Drive-side merge rules for cookbooks. Cookbooks sync
// as one snapshot file (`_cookbooks.json`), so conflicts resolve with a
// simple strategy: last write wins by `updatedAt`, and a local deletion
// always wins over a remote version. Pure functions — no I/O — so the rules
// are unit-testable and the sync engine stays an orchestrator.

import type { Cookbook } from '../../types/cookbook'

export interface CookbookMergeResult {
  /** The full merged list to write back to Drive. */
  merged: Cookbook[]
  /** Remote cookbooks that are new or newer — save these on this device. */
  toSaveLocally: Cookbook[]
  /** True when the merged list differs from what Drive holds (rewrite needed). */
  remoteChanged: boolean
}

export function mergeCookbooks(
  local: Cookbook[],
  remote: Cookbook[],
  locallyDeletedIds: string[],
): CookbookMergeResult {
  const deleted = new Set(locallyDeletedIds)
  const byId = new Map<string, Cookbook>(remote.map((cb) => [cb.id, cb]))
  const localIds = new Set(local.map((cb) => cb.id))
  const toSaveLocally: Cookbook[] = []
  let remoteChanged = false

  for (const id of deleted) {
    if (byId.delete(id)) remoteChanged = true
  }

  for (const lcb of local) {
    if (deleted.has(lcb.id)) continue
    const rcb = byId.get(lcb.id)
    if (!rcb) {
      // New or changed only here — push the local version.
      byId.set(lcb.id, lcb)
      remoteChanged = true
      continue
    }
    if (rcb.updatedAt > lcb.updatedAt) {
      // Remote is newer — keep remote, store it locally.
      toSaveLocally.push(rcb)
    } else if (lcb.updatedAt > rcb.updatedAt) {
      // Local is newer — push it.
      byId.set(lcb.id, lcb)
      remoteChanged = true
    }
    // Equal timestamps — nothing to do.
  }

  // Remote cookbooks this device has never seen — store them locally.
  for (const rcb of remote) {
    if (!localIds.has(rcb.id)) toSaveLocally.push(rcb)
  }

  return {
    merged: [...byId.values()],
    toSaveLocally,
    remoteChanged,
  }
}
