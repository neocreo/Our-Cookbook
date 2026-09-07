// Drive sync service — the interface for Google Drive sync. This is a stub
// that defines the contract; the actual OAuth + Drive API integration lands
// in Phase 2. The interface is defined now because it's costly to change
// later (YAGNI exception).
//
// The sync flow:
//   1. Drain the PendingSync queue (features/sync/repository)
//   2. For each entry: push the entity JSON to the Drive sync folder
//   3. Pull remote changes, merge with version vectors, resolve conflicts
//   4. Mark entries as synced or failed

import type { PendingSync } from '../../types/sync'
import type { SyncVersionVector } from '../../types/versionVector'

export interface DriveSyncService {
  /** Sign in to Google Drive and establish the sync folder. */
  signIn(): Promise<void>
  /** Sign out and clear credentials. */
  signOut(): Promise<void>
  /** Whether the user is currently authenticated. */
  isAuthenticated(): Promise<boolean>
  /** Push pending changes to Drive. Returns the IDs that succeeded. */
  pushPending(queue: readonly PendingSync[]): Promise<{ synced: string[]; failed: { id: string; error: string }[] }>
  /** Pull remote changes since last sync. Returns the raw remote documents. */
  pullRemote(): Promise<RemoteDocument[]>
  /** Get the sync folder path/ID on Drive. */
  getSyncFolderId(): string | null
}

export interface RemoteDocument {
  entityId: string
  entityType: string
  data: string // JSON
  versionVector: SyncVersionVector
  deviceId: string
  updatedAt: string
}

// Stub implementation — returns no-ops until OAuth is wired in Phase 2.
// Included so the Settings page and sync status can reference a real service
// without crashing.

export class StubDriveSyncService implements DriveSyncService {
  async signIn(): Promise<void> {
    console.warn('[sync] Drive sign-in not implemented yet (Phase 2)')
  }
  async signOut(): Promise<void> {}
  async isAuthenticated(): Promise<boolean> {
    return false
  }
  async pushPending(_queue: readonly PendingSync[]): Promise<{ synced: string[]; failed: { id: string; error: string }[] }> {
    return { synced: [], failed: [] }
  }
  async pullRemote(): Promise<RemoteDocument[]> {
    return []
  }
  getSyncFolderId(): string | null {
    return null
  }
}

// Singleton stub — replace with the real service when OAuth is wired.
let service: DriveSyncService | null = null

export function getDriveSyncService(): DriveSyncService {
  if (!service) service = new StubDriveSyncService()
  return service
}
