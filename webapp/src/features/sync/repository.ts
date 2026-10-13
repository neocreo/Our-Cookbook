// Sync repository — the PendingSync queue. A separate bounded context that
// queues offline changes for later Drive push. The recipe and cookbook
// repositories enqueue operations here on every write; the Drive sync
// engine drains the queue.
//
// The queue persists to localStorage so offline changes survive a reload —
// losing the queue would silently drop offline edits from ever syncing.

import type { PendingSync, SyncOperation, EntityType, SyncMetadata } from '../../types/sync'
import { createPendingSync, withRetryIncrement, shouldRetryPendingSync, createSyncMetadata } from '../../types/sync'

const STORAGE_KEY = 'oc.pendingSync'
const MAX_QUEUE_BYTES = 4 * 1024 * 1024 // safety valve: images live in recipes, not here

function loadQueue(): PendingSync[] {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (!raw) return []
    const parsed = JSON.parse(raw)
    return Array.isArray(parsed) ? parsed as PendingSync[] : []
  } catch {
    return [] // corrupted or unavailable storage — start empty rather than break the app
  }
}

function saveQueue(queue: PendingSync[]): void {
  try {
    // Drop oldest entries if the payload grows past the safety limit.
    let payload = JSON.stringify(queue)
    while (payload.length > MAX_QUEUE_BYTES && queue.length > 0) {
      queue = queue.slice(1)
      payload = JSON.stringify(queue)
    }
    localStorage.setItem(STORAGE_KEY, payload)
  } catch {
    // Storage full or unavailable — the in-memory queue still works this session.
  }
}

const queue: PendingSync[] = loadQueue()
let metadata: SyncMetadata | null = null

function persist(): void {
  saveQueue(queue)
}

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
  persist()
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
    persist()
  }
}

export function markSynced(syncId: string): void {
  const idx = queue.findIndex((p) => p.id === syncId)
  if (idx >= 0) queue.splice(idx, 1)
  persist()
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
