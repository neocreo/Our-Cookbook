package com.ourcookbook.domain.usecase.sync

import com.ourcookbook.domain.model.SyncHistoryEntry
import com.ourcookbook.domain.model.SyncOperationRecord
import com.ourcookbook.domain.repository.SyncHistoryRepository
import java.time.Instant
import javax.inject.Inject

/**
 * Use case for managing sync history and logs
 * Task 2.2.18: Sync history and logs
 *
 * Provides functionality to track, query, and manage sync history
 */
class SyncHistoryManager @Inject constructor(
    private val syncHistoryRepository: SyncHistoryRepository
) {

    /**
     * Result of history operation
     */
    sealed class HistoryResult {
        data class Success(
            val entries: List<SyncHistoryEntry>,
            val count: Int
        ) : HistoryResult()
        
        data class Failure(
            val errorMessage: String,
            val exception: Exception? = null
        ) : HistoryResult()
    }

    /**
     * Sync operation type
     */
    enum class SyncOperationType {
        PUSH,
        PULL,
        FULL_SYNC,
        EXPORT,
        IMPORT
    }

    /**
     * Sync status type
     */
    enum class SyncStatusType {
        STARTED,
        IN_PROGRESS,
        COMPLETED,
        FAILED,
        CANCELLED
    }

    /**
     * Record a sync operation in history
     *
     * @param operation The sync operation to record
     * @return The created history entry
     */
    suspend fun recordSyncOperation(
        operation: SyncOperationRecord
    ): SyncHistoryEntry {
        val entry = SyncHistoryEntry.create(
            id = java.util.UUID.randomUUID().toString(),
            operationId = operation.id,
            operationType = operation.type,
            status = operation.status,
            recipeIds = operation.recipeIds,
            timestamp = operation.timestamp,
            duration = null, // Will be updated when completed
            itemCount = operation.recipeIds.size,
            successCount = 0,
            failureCount = 0,
            errorMessages = operation.errorMessages,
            checksums = operation.checksums,
            deviceId = operation.deviceId,
            userId = operation.userId
        )
        
        return syncHistoryRepository.createEntry(entry)
    }

    /**
     * Start a sync operation
     *
     * @param operationType Type of sync operation
     * @param recipeIds List of recipe IDs involved
     * @param deviceId ID of the device
     * @param userId ID of the user
     * @return SyncHistoryEntry for the started operation
     */
    suspend fun startSyncOperation(
        operationType: SyncOperationType,
        recipeIds: List<String>,
        deviceId: String,
        userId: String
    ): SyncHistoryEntry {
        val operationId = java.util.UUID.randomUUID().toString()
        val timestamp = Instant.now()
        
        val entry = SyncHistoryEntry.create(
            id = java.util.UUID.randomUUID().toString(),
            operationId = operationId,
            operationType = operationType.name,
            status = SyncStatusType.STARTED.name,
            recipeIds = recipeIds,
            timestamp = timestamp.toEpochMilli(),
            duration = null,
            itemCount = recipeIds.size,
            successCount = 0,
            failureCount = 0,
            errorMessages = emptyList(),
            checksums = emptyMap(),
            deviceId = deviceId,
            userId = userId
        )
        
        return syncHistoryRepository.createEntry(entry)
    }

    /**
     * Update a sync operation status
     *
     * @param operationId ID of the operation to update
     * @param status New status
     * @param successCount Number of successful items
     * @param failureCount Number of failed items
     * @param errorMessages List of error messages
     * @return Updated SyncHistoryEntry
     */
    suspend fun updateSyncOperation(
        operationId: String,
        status: SyncStatusType,
        successCount: Int = 0,
        failureCount: Int = 0,
        errorMessages: List<String> = emptyList()
    ): SyncHistoryEntry? {
        val entry = syncHistoryRepository.getEntryByOperationId(operationId)
        
        return entry?.let {
            val now = Instant.now()
            val startTime = Instant.ofEpochMilli(it.timestamp)
            val duration = now.toEpochMilli() - startTime.toEpochMilli()
            
            val updatedEntry = it.copy(
                status = status.name,
                duration = duration,
                successCount = successCount,
                failureCount = failureCount,
                errorMessages = errorMessages,
                completedAt = if (status == SyncStatusType.COMPLETED || status == SyncStatusType.FAILED) {
                    now.toEpochMilli()
                } else {
                    null
                }
            )
            
            syncHistoryRepository.updateEntry(updatedEntry)
        }
    }

    /**
     * Complete a sync operation
     *
     * @param operationId ID of the operation to complete
     * @param successCount Number of successful items
     * @param failureCount Number of failed items
     * @param errorMessages List of error messages
     * @return Updated SyncHistoryEntry
     */
    suspend fun completeSyncOperation(
        operationId: String,
        successCount: Int,
        failureCount: Int,
        errorMessages: List<String> = emptyList()
    ): SyncHistoryEntry? {
        return updateSyncOperation(
            operationId = operationId,
            status = SyncStatusType.COMPLETED,
            successCount = successCount,
            failureCount = failureCount,
            errorMessages = errorMessages
        )
    }

    /**
     * Fail a sync operation
     *
     * @param operationId ID of the operation to fail
     * @param errorMessages List of error messages
     * @return Updated SyncHistoryEntry
     */
    suspend fun failSyncOperation(
        operationId: String,
        errorMessages: List<String>
    ): SyncHistoryEntry? {
        val entry = syncHistoryRepository.getEntryByOperationId(operationId)
        
        return entry?.let {
            val updatedEntry = it.copy(
                status = SyncStatusType.FAILED.name,
                errorMessages = errorMessages,
                completedAt = Instant.now().toEpochMilli()
            )
            
            syncHistoryRepository.updateEntry(updatedEntry)
        }
    }

    /**
     * Get sync history
     *
     * @param limit Maximum number of entries to return
     * @param offset Offset for pagination
     * @param operationType Filter by operation type
     * @param status Filter by status
     * @param deviceId Filter by device ID
     * @param startDate Filter by start date
     * @param endDate Filter by end date
     * @return HistoryResult with matching entries
     */
    suspend fun getSyncHistory(
        limit: Int = 50,
        offset: Int = 0,
        operationType: SyncOperationType? = null,
        status: SyncStatusType? = null,
        deviceId: String? = null,
        startDate: Instant? = null,
        endDate: Instant? = null
    ): HistoryResult {
        return try {
            val entries = syncHistoryRepository.getEntries(
                limit = limit,
                offset = offset,
                operationType = operationType?.name,
                status = status?.name,
                deviceId = deviceId,
                startDate = startDate?.toEpochMilli(),
                endDate = endDate?.toEpochMilli()
            )
            
            HistoryResult.Success(
                entries = entries,
                count = entries.size
            )
        } catch (e: Exception) {
            HistoryResult.Failure(
                errorMessage = "Failed to get sync history: ${e.message}",
                exception = e
            )
        }
    }

    /**
     * Get sync history for a specific device
     *
     * @param deviceId ID of the device
     * @param limit Maximum number of entries to return
     * @return HistoryResult with matching entries
     */
    suspend fun getSyncHistoryForDevice(
        deviceId: String,
        limit: Int = 50
    ): HistoryResult {
        return getSyncHistory(
            limit = limit,
            deviceId = deviceId
        )
    }

    /**
     * Get sync history for a specific user
     *
     * @param userId ID of the user
     * @param limit Maximum number of entries to return
     * @return HistoryResult with matching entries
     */
    suspend fun getSyncHistoryForUser(
        userId: String,
        limit: Int = 50
    ): HistoryResult {
        return try {
            val entries = syncHistoryRepository.getEntriesByUser(userId, limit)
            
            HistoryResult.Success(
                entries = entries,
                count = entries.size
            )
        } catch (e: Exception) {
            HistoryResult.Failure(
                errorMessage = "Failed to get sync history for user: ${e.message}",
                exception = e
            )
        }
    }

    /**
     * Get recent sync operations
     *
     * @param limit Maximum number of entries to return
     * @return List of recent SyncHistoryEntry objects
     */
    suspend fun getRecentSyncOperations(limit: Int = 10): List<SyncHistoryEntry> {
        return syncHistoryRepository.getRecentEntries(limit)
    }

    /**
     * Get sync statistics
     *
     * @param days Number of days to look back
     * @return Map of statistics
     */
    suspend fun getSyncStatistics(days: Int = 30): Map<String, Any> {
        val startDate = Instant.now().minusSeconds((days * 24 * 60 * 60).toLong())
        val entries = syncHistoryRepository.getEntriesSince(startDate.toEpochMilli())
        
        val byType = entries.groupBy { it.operationType }
        val byStatus = entries.groupBy { it.status }
        
        val totalDuration = entries.sumOf { it.duration ?: 0 }
        val totalItems = entries.sumOf { it.itemCount }
        val totalSuccess = entries.sumOf { it.successCount }
        val totalFailure = entries.sumOf { it.failureCount }
        
        return mapOf(
            "total_operations" to entries.size,
            "by_type" to byType.mapValues { it.value.size },
            "by_status" to byStatus.mapValues { it.value.size },
            "total_duration_ms" to totalDuration,
            "total_items" to totalItems,
            "total_success" to totalSuccess,
            "total_failure" to totalFailure,
            "success_rate" to if (totalItems > 0) totalSuccess.toFloat() / totalItems else 0f
        )
    }

    /**
     * Get sync history summary
     *
     * @return SyncHistorySummary object
     */
    suspend fun getSyncHistorySummary(): SyncHistorySummary {
        val entries = syncHistoryRepository.getAllEntries()
        
        val byType = entries.groupBy { it.operationType }
        val byStatus = entries.groupBy { it.status }
        
        val totalDuration = entries.sumOf { it.duration ?: 0 }
        val totalItems = entries.sumOf { it.itemCount }
        val totalSuccess = entries.sumOf { it.successCount }
        val totalFailure = entries.sumOf { it.failureCount }
        
        val lastOperation = entries.maxByOrNull { it.timestamp }
        val firstOperation = entries.minByOrNull { it.timestamp }
        
        return SyncHistorySummary(
            totalOperations = entries.size,
            operationsByType = byType.mapValues { it.value.size },
            operationsByStatus = byStatus.mapValues { it.value.size },
            totalDurationMs = totalDuration,
            totalItems = totalItems,
            totalSuccess = totalSuccess,
            totalFailure = totalFailure,
            successRate = if (totalItems > 0) totalSuccess.toFloat() / totalItems else 0f,
            lastOperationTimestamp = lastOperation?.timestamp,
            firstOperationTimestamp = firstOperation?.timestamp
        )
    }

    /**
     * Sync history summary
     */
    data class SyncHistorySummary(
        val totalOperations: Int,
        val operationsByType: Map<String, Int>,
        val operationsByStatus: Map<String, Int>,
        val totalDurationMs: Long,
        val totalItems: Int,
        val totalSuccess: Int,
        val totalFailure: Int,
        val successRate: Float,
        val lastOperationTimestamp: Long?,
        val firstOperationTimestamp: Long?
    )

    /**
     * Delete a sync history entry
     *
     * @param entryId ID of the entry to delete
     * @return true if deleted, false otherwise
     */
    suspend fun deleteSyncHistoryEntry(entryId: String): Boolean {
        return try {
            syncHistoryRepository.deleteEntry(entryId)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Delete sync history entries older than the specified date
     *
     * @param olderThan Date threshold
     * @return Number of entries deleted
     */
    suspend fun deleteOldSyncHistory(olderThan: Instant): Int {
        return try {
            val threshold = olderThan.toEpochMilli()
            syncHistoryRepository.deleteEntriesOlderThan(threshold)
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Clear all sync history
     *
     * @return Number of entries deleted
     */
    suspend fun clearAllSyncHistory(): Int {
        return try {
            syncHistoryRepository.deleteAllEntries()
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Export sync history as CSV
     *
     * @return CSV string with sync history
     */
    suspend fun exportSyncHistoryAsCsv(): String {
        val entries = syncHistoryRepository.getAllEntries()
        
        val csv = StringBuilder()
        
        // Header
        csv.appendLine("ID,Operation ID,Type,Status,Timestamp,Duration,Item Count,Success,Failure,Device ID,User ID")
        
        // Data
        entries.forEach { entry ->
            csv.appendLine("\"${entry.id}\",\"${entry.operationId}\",\"${entry.operationType}\",\"${entry.status}\",\"${entry.timestamp}\",\"${entry.duration}\",\"${entry.itemCount}\",\"${entry.successCount}\",\"${entry.failureCount}\",\"${entry.deviceId}\",\"${entry.userId}\"")
        }
        
        return csv.toString()
    }

    /**
     * Get sync history for a specific recipe
     *
     * @param recipeId ID of the recipe
     * @return List of SyncHistoryEntry objects for the recipe
     */
    suspend fun getSyncHistoryForRecipe(recipeId: String): List<SyncHistoryEntry> {
        return syncHistoryRepository.getEntriesForRecipe(recipeId)
    }

    /**
     * Get sync history for a specific operation
     *
     * @param operationId ID of the operation
     * @return SyncHistoryEntry for the operation
     */
    suspend fun getSyncHistoryForOperation(operationId: String): SyncHistoryEntry? {
        return syncHistoryRepository.getEntryByOperationId(operationId)
    }

    /**
     * Check if sync history is empty
     */
    suspend fun isSyncHistoryEmpty(): Boolean {
        return syncHistoryRepository.getEntryCount() == 0
    }

    /**
     * Get the most recent sync operation
     */
    suspend fun getMostRecentSyncOperation(): SyncHistoryEntry? {
        return syncHistoryRepository.getMostRecentEntry()
    }

    /**
     * Get the last successful sync operation
     */
    suspend fun getLastSuccessfulSyncOperation(): SyncHistoryEntry? {
        return syncHistoryRepository.getLastSuccessfulEntry()
    }

    /**
     * Get the last failed sync operation
     */
    suspend fun getLastFailedSyncOperation(): SyncHistoryEntry? {
        return syncHistoryRepository.getLastFailedEntry()
    }

    /**
     * Get sync history grouped by date
     *
     * @param days Number of days to group by
     * @return Map of date to list of entries
     */
    suspend fun getSyncHistoryGroupedByDate(days: Int = 7): Map<String, List<SyncHistoryEntry>> {
        val entries = syncHistoryRepository.getRecentEntries(days * 24 * 60 * 60 * 1000)
        
        return entries.groupBy { entry ->
            val instant = Instant.ofEpochMilli(entry.timestamp)
            java.time.LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault())
                .toLocalDate()
                .toString()
        }
    }
}
