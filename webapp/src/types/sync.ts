// Sync types — ported from PendingSync.kt + SyncOperation.kt. Field names
// match the Kotlin models so Drive-synced data stays backwards-compatible.

export type SyncOperation = 'CREATE' | 'UPDATE' | 'DELETE'

export type EntityType =
  | 'RECIPE'
  | 'INGREDIENT'
  | 'RECIPE_IMAGE'
  | 'DEVICE'
  | 'COOKBOOK'
  | 'SHARING_LINK'

export interface PendingSync {
  id: string
  operation: SyncOperation
  entityType: EntityType
  entityId: string
  data: string // JSON representation of the entity
  timestamp: string // ISO
  retryCount: number
  lastError: string | null
}

export function createPendingSync(
  operation: SyncOperation,
  entityType: EntityType,
  entityId: string,
  data: string,
): PendingSync {
  return {
    id: crypto.randomUUID(),
    operation,
    entityType,
    entityId,
    data,
    timestamp: new Date().toISOString(),
    retryCount: 0,
    lastError: null,
  }
}

export function shouldRetryPendingSync(p: PendingSync, maxRetries = 3): boolean {
  return p.retryCount < maxRetries
}

export function withRetryIncrement(p: PendingSync, error: string): PendingSync {
  return { ...p, retryCount: p.retryCount + 1, lastError: error }
}

// Per-device sync state — ported from SyncMetadata.kt
export interface SyncMetadata {
  id: string
  deviceId: string
  lastSyncTimestamp: string | null // ISO
  lastSuccessfulSync: string | null // ISO
  syncInProgress: boolean
  pendingChanges: number
  conflictCount: number
}

export function createSyncMetadata(deviceId: string): SyncMetadata {
  return {
    id: crypto.randomUUID(),
    deviceId,
    lastSyncTimestamp: null,
    lastSuccessfulSync: null,
    syncInProgress: false,
    pendingChanges: 0,
    conflictCount: 0,
  }
}
