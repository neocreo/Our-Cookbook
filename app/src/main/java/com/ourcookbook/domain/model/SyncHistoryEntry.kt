package com.ourcookbook.domain.model

import java.util.UUID

/**
 * History entry for a single sync operation.
 *
 * Stored by [com.ourcookbook.domain.repository.SyncHistoryRepository] and
 * managed by [com.ourcookbook.domain.usecase.sync.SyncHistoryManager].
 */
data class SyncHistoryEntry(
    val id: String,
    val operationId: String,
    val operationType: String,
    val status: String,
    val recipeIds: List<String> = emptyList(),
    val timestamp: Long,
    val duration: Long? = null,
    val itemCount: Int = 0,
    val successCount: Int = 0,
    val failureCount: Int = 0,
    val errorMessages: List<String> = emptyList(),
    val checksums: Map<String, String> = emptyMap(),
    val deviceId: String = "",
    val userId: String = "",
    val completedAt: Long? = null
) {
    companion object {
        @Suppress("LongParameterList")
        fun create(
            id: String = UUID.randomUUID().toString(),
            operationId: String,
            operationType: String,
            status: String,
            recipeIds: List<String>,
            timestamp: Long,
            duration: Long?,
            itemCount: Int,
            successCount: Int,
            failureCount: Int,
            errorMessages: List<String>,
            checksums: Map<String, String>,
            deviceId: String,
            userId: String
        ): SyncHistoryEntry = SyncHistoryEntry(
            id = id,
            operationId = operationId,
            operationType = operationType,
            status = status,
            recipeIds = recipeIds,
            timestamp = timestamp,
            duration = duration,
            itemCount = itemCount,
            successCount = successCount,
            failureCount = failureCount,
            errorMessages = errorMessages,
            checksums = checksums,
            deviceId = deviceId,
            userId = userId
        )
    }
}
