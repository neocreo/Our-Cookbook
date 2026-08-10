package com.ourcookbook.domain.model

import java.time.Instant
import java.util.UUID

/**
 * Domain model for SyncLog
 * Audit trail for sync operations
 * 
 * Contains detailed information about each sync operation including
 * timestamps, status, device information, and performance metrics.
 */
data class SyncLog(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Instant = Instant.now(),
    val status: SyncStatus,
    val deviceId: String,
    val syncedItems: Int = 0,
    val conflicts: Int = 0,
    val durationMs: Long = 0,
    val errorMessage: String? = null
) {
    fun isValid(): Boolean {
        return deviceId.isNotBlank()
    }
    
    // Check if sync was successful
    val isSuccessful: Boolean get() = status == SyncStatus.SUCCESS
    
    // Check if sync had errors
    val hasErrors: Boolean get() = status == SyncStatus.FAILURE || errorMessage != null
    
    // Check if sync was partial
    val isPartial: Boolean get() = status == SyncStatus.PARTIAL
    
    // Get formatted duration string
    val formattedDuration: String get() {
        return when {
            durationMs < 1000 -> "${durationMs}ms"
            durationMs < 60000 -> "${durationMs / 1000}s"
            else -> "${durationMs / 60000}m ${(durationMs % 60000) / 1000}s"
        }
    }
    
    companion object {
        fun create(
            status: SyncStatus,
            deviceId: String,
            syncedItems: Int = 0,
            conflicts: Int = 0,
            durationMs: Long = 0,
            errorMessage: String? = null
        ): SyncLog {
            return SyncLog(
                status = status,
                deviceId = deviceId,
                syncedItems = syncedItems,
                conflicts = conflicts,
                durationMs = durationMs,
                errorMessage = errorMessage
            )
        }
        
        fun createSuccess(
            deviceId: String,
            syncedItems: Int = 0,
            conflicts: Int = 0,
            durationMs: Long = 0
        ): SyncLog {
            return create(SyncStatus.SUCCESS, deviceId, syncedItems, conflicts, durationMs)
        }
        
        fun createFailure(
            deviceId: String,
            errorMessage: String,
            durationMs: Long = 0
        ): SyncLog {
            return create(SyncStatus.FAILURE, deviceId, 0, 0, durationMs, errorMessage)
        }
    }
}

/**
 * Sync operation status
 */
enum class SyncStatus {
    SUCCESS, FAILURE, PARTIAL, CANCELLED
}