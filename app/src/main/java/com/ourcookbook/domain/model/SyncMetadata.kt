package com.ourcookbook.domain.model

import java.time.Instant
import java.util.UUID

/**
 * Domain model for SyncMetadata
 * Per-device sync state
 * 
 * Contains metadata about the sync state for a specific device including
 * timestamps, progress indicators, and counters for pending changes and conflicts.
 */
data class SyncMetadata(
    val id: String = UUID.randomUUID().toString(),
    val deviceId: String,
    val lastSyncTimestamp: Instant? = null,
    val lastSuccessfulSync: Instant? = null,
    val syncInProgress: Boolean = false,
    val pendingChanges: Int = 0,
    val conflictCount: Int = 0
) {
    fun isValid(): Boolean {
        return deviceId.isNotBlank()
    }
    
    // Check if device has ever synced
    val hasSyncedBefore: Boolean get() = lastSyncTimestamp != null
    
    // Check if last sync was successful
    val lastSyncWasSuccessful: Boolean get() = lastSuccessfulSync != null
    
    // Check if there are pending changes to sync
    val hasPendingChanges: Boolean get() = pendingChanges > 0
    
    // Check if there are conflicts to resolve
    val hasConflicts: Boolean get() = conflictCount > 0
    
    // Check if device is currently syncing
    val isCurrentlySyncing: Boolean get() = syncInProgress
    
    // Increment pending changes counter
    fun withPendingChangeIncrement(): SyncMetadata {
        return this.copy(pendingChanges = pendingChanges + 1)
    }
    
    // Decrement pending changes counter
    fun withPendingChangeDecrement(): SyncMetadata {
        return this.copy(pendingChanges = maxOf(0, pendingChanges - 1))
    }
    
    // Increment conflict counter
    fun withConflictIncrement(): SyncMetadata {
        return this.copy(conflictCount = conflictCount + 1)
    }
    
    // Decrement conflict counter
    fun withConflictDecrement(): SyncMetadata {
        return this.copy(conflictCount = maxOf(0, conflictCount - 1))
    }
    
    // Update sync timestamps
    fun withSyncUpdate(success: Boolean): SyncMetadata {
        val now = Instant.now()
        return this.copy(
            lastSyncTimestamp = now,
            lastSuccessfulSync = if (success) now else lastSuccessfulSync,
            syncInProgress = false
        )
    }
    
    // Start sync
    fun withSyncStart(): SyncMetadata {
        return this.copy(syncInProgress = true)
    }
    
    companion object {
        fun create(deviceId: String): SyncMetadata {
            return SyncMetadata(deviceId = deviceId)
        }
    }
}