// Sync repository — the PendingSync queue. A separate bounded context that
// queues offline changes for later Drive push. The recipe and cookbook
// repositories enqueue operations here on every write; the Drive sync
// service (Phase 2) drains the queue.
//
// This is the YAGNI exception: the sync schema and queue contract are
// costly to change later, so they're defined now even though Drive push
// isn't wired yet.

import type { PendingSync, SyncOperation, EntityType, SyncMetadata } from '../../types/sync'
import { createPendingSync, withRetryIncrement, shouldRetryPendingSync, createSyncMetadata } from '../../types/sync'

// In-memory queue. In Phase 2 this becomes an RxDB collection; for now the
// queue is session-scoped so the app works without a schema migration.
const queue: PendingSync[] = []
let metadata: SyncMetadata | null = null

function ensureMetadata(deviceId: string): SyncMetadata {
  if (!metadata) metadata = createSyncMetadata(deviceId)
  return metadata
}

export function enqueueSync(
  operation: SyncOperation,
  entityType: EntityType,
  entityId: string,
  data: string,
  deviceId: string,
): PendingSync {
  const entry = createPendingSync(operation, entityType, entityId, data)
  queue.push(entry)
  const m = ensureMetadata(deviceId)
  metadata = { ...m, pendingChanges: m.pendingChanges + 1 }
  return entry
}

export function getPendingSyncQueue(): readonly PendingSync[] {
  return queue
}

export function getPendingCount(): number {
  return queue.length
}

export function markSyncFailed(syncId: string, error: string): void {
  const idx = queue.findIndex((p) => p.id === syncId)
  if (idx >= 0) {
    queue[idx] = withRetryIncrement(queue[idx], error)
  }
}

export function markSynced(syncId: string): void {
  const idx = queue.findIndex((p) => p.id === syncId)
  if (idx >= 0) queue.splice(idx, 1)
  if (metadata) metadata = { ...metadata, pendingChanges: Math.max(0, metadata.pendingChanges - 1) }
}

export function getRetryable(maxRetries = 3): PendingSync[] {
  return queue.filter((p) => shouldRetryPendingSync(p, maxRetries))
}

export function getSyncMetadata(): SyncMetadata | null {
  return metadata
}

export function markSyncStart(deviceId: string): void {
  const m = ensureMetadata(deviceId)
  metadata = { ...m, syncInProgress: true }
}

export function markSyncComplete(deviceId: string, success: boolean): void {
  const now = new Date().toISOString()
  const m = ensureMetadata(deviceId)
  metadata = {
    ...m,
    syncInProgress: false,
    lastSyncTimestamp: now,
    lastSuccessfulSync: success ? now : m.lastSuccessfulSync,
  }
}
