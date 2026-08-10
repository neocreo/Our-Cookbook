package com.ourcookbook.ui.screens.sync

import com.ourcookbook.domain.model.SyncLog
import com.ourcookbook.domain.model.SyncStatus
import java.time.Instant
import java.util.UUID

/**
 * Data model for Sync History items displayed in the Sync Status Screen
 * Task 2.1.06: Sync Status Screen Implementation
 * 
 * Represents a single sync operation with all relevant details for display
 */
data class SyncHistoryItem(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Instant = Instant.now(),
    val status: SyncStatusDisplay,
    val direction: SyncDirection,
    val changesSynchronized: Int = 0,
    val conflicts: Int = 0,
    val durationMs: Long = 0,
    val deviceId: String = "",
    val deviceName: String = "",
    val errorMessage: String? = null,
    val errorCategory: SyncErrorCategory? = null
) {
    val formattedTimestamp: String get() = timestamp.toString().substring(0, 19).replace("T", " ")
    val formattedDuration: String get() {
        return when {
            durationMs < 1000 -> "${durationMs}ms"
            durationMs < 60000 -> "${durationMs / 1000}s"
            else -> "${durationMs / 60000}m ${(durationMs % 60000) / 1000}s"
        }
    }
    
    val isSuccessful: Boolean get() = status == SyncStatusDisplay.SUCCESS
    val hasErrors: Boolean get() = status == SyncStatusDisplay.FAILURE || errorMessage != null
    
    companion object {
        fun fromSyncLog(log: SyncLog, deviceName: String = ""): SyncHistoryItem {
            return SyncHistoryItem(
                id = log.id,
                timestamp = log.timestamp,
                status = when (log.status) {
                    SyncStatus.SUCCESS -> SyncStatusDisplay.SUCCESS
                    SyncStatus.FAILURE -> SyncStatusDisplay.FAILURE
                    SyncStatus.PARTIAL -> SyncStatusDisplay.PARTIAL
                    SyncStatus.CANCELLED -> SyncStatusDisplay.CANCELLED
                },
                direction = SyncDirection.BOTH, // Default, would be determined by actual sync operation
                changesSynchronized = log.syncedItems,
                conflicts = log.conflicts,
                durationMs = log.durationMs,
                deviceId = log.deviceId,
                deviceName = deviceName,
                errorMessage = log.errorMessage,
                errorCategory = log.errorMessage?.let { categorizeError(it) }
            )
        }
        
        private fun categorizeError(errorMessage: String): SyncErrorCategory {
            return when {
                errorMessage.contains("network", ignoreCase = true) || 
                     errorMessage.contains("connection", ignoreCase = true) ||
                     errorMessage.contains("timeout", ignoreCase = true) -> SyncErrorCategory.NETWORK
                errorMessage.contains("permission", ignoreCase = true) || 
                     errorMessage.contains("access", ignoreCase = true) ||
                     errorMessage.contains("auth", ignoreCase = true) -> SyncErrorCategory.PERMISSION
                errorMessage.contains("conflict", ignoreCase = true) -> SyncErrorCategory.CONFLICT
                errorMessage.contains("storage", ignoreCase = true) || 
                     errorMessage.contains("space", ignoreCase = true) -> SyncErrorCategory.STORAGE
                else -> SyncErrorCategory.UNKNOWN
            }
        }
    }
}

/**
 * Display status for sync operations
 */
enum class SyncStatusDisplay {
    SUCCESS, FAILURE, PARTIAL, CANCELLED, SYNCING
}

/**
 * Direction of sync operation
 */
enum class SyncDirection {
    PULL, PUSH, BOTH
}

/**
 * Error categories for sync operations
 */
enum class SyncErrorCategory {
    NETWORK, PERMISSION, CONFLICT, STORAGE, UNKNOWN
}

/**
 * Data model for device management in sync status
 */
data class DeviceSyncInfo(
    val deviceId: String,
    val deviceName: String,
    val lastSeen: Instant? = null,
    val syncStatus: SyncStatusDisplay = SyncStatusDisplay.SUCCESS,
    val lastSyncTimestamp: Instant? = null,
    val pendingChanges: Int = 0,
    val conflictCount: Int = 0,
    val syncCapabilities: Set<String> = emptySet(),
    val isOnline: Boolean = true
) {
    val formattedLastSeen: String get() = lastSeen?.toString()?.substring(0, 19)?.replace("T", " ") ?: "Never"
    val formattedLastSync: String get() = lastSyncTimestamp?.toString()?.substring(0, 19)?.replace("T", " ") ?: "Never"
    val hasPendingChanges: Boolean get() = pendingChanges > 0
    val hasConflicts: Boolean get() = conflictCount > 0
}

/**
 * Data model for conflict summary in sync status
 */
data class ConflictSummary(
    val conflictId: String,
    val recipeName: String,
    val conflictType: String,
    val detectedAt: Instant,
    val status: String,
    val localVersion: String,
    val remoteVersion: String
) {
    val formattedDetectedAt: String get() = detectedAt.toString().substring(0, 19).replace("T", " ")
}

/**
 * Sync statistics for the overview section
 */
data class SyncStatistics(
    val totalSyncs: Int = 0,
    val successfulSyncs: Int = 0,
    val failedSyncs: Int = 0,
    val totalChangesSynced: Int = 0,
    val totalConflicts: Int = 0,
    val averageSyncDuration: Long = 0,
    val lastSyncTimestamp: Instant? = null
) {
    val successRate: Float get() = if (totalSyncs > 0) (successfulSyncs.toFloat() / totalSyncs) * 100 else 0f
    val formattedAverageDuration: String get() {
        return when {
            averageSyncDuration < 1000 -> "${averageSyncDuration}ms"
            averageSyncDuration < 60000 -> "${averageSyncDuration / 1000}s"
            else -> "${averageSyncDuration / 60000}m"
        }
    }
}