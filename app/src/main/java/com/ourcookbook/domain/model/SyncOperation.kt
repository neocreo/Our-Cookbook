package com.ourcookbook.domain.model

import java.util.UUID

/**
 * Represents a sync operation to be recorded in sync history.
 */
data class SyncOperationRecord(
    val id: String = UUID.randomUUID().toString(),
    val type: String,
    val status: String,
    val recipeIds: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis(),
    val errorMessages: List<String> = emptyList(),
    val checksums: Map<String, String> = emptyMap(),
    val deviceId: String = "",
    val userId: String = ""
)
