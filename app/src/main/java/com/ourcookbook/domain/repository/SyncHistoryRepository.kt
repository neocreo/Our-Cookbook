package com.ourcookbook.domain.repository

import com.ourcookbook.domain.model.SyncHistoryEntry

/**
 * Repository for persisting and querying sync history entries.
 *
 * Consumed by [com.ourcookbook.domain.usecase.sync.SyncHistoryManager].
 * Implementation is provided by the data layer.
 */
interface SyncHistoryRepository {
    suspend fun createEntry(entry: SyncHistoryEntry): SyncHistoryEntry
    suspend fun updateEntry(entry: SyncHistoryEntry): SyncHistoryEntry
    suspend fun deleteEntry(entryId: String): Boolean
    suspend fun deleteEntriesOlderThan(thresholdMillis: Long): Int
    suspend fun deleteAllEntries(): Int

    suspend fun getEntryByOperationId(operationId: String): SyncHistoryEntry?
    suspend fun getEntriesForRecipe(recipeId: String): List<SyncHistoryEntry>
    suspend fun getEntriesByUser(userId: String, limit: Int): List<SyncHistoryEntry>

    @Suppress("LongParameterList")
    suspend fun getEntries(
        limit: Int,
        offset: Int,
        operationType: String?,
        status: String?,
        deviceId: String?,
        startDate: Long?,
        endDate: Long?
    ): List<SyncHistoryEntry>

    suspend fun getRecentEntries(limit: Int): List<SyncHistoryEntry>
    suspend fun getEntriesSince(timestampMillis: Long): List<SyncHistoryEntry>
    suspend fun getAllEntries(): List<SyncHistoryEntry>

    suspend fun getEntryCount(): Int
    suspend fun getMostRecentEntry(): SyncHistoryEntry?
    suspend fun getLastSuccessfulEntry(): SyncHistoryEntry?
    suspend fun getLastFailedEntry(): SyncHistoryEntry?
}
