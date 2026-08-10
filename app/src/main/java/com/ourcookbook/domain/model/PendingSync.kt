package com.ourcookbook.domain.model

import java.time.Instant
import java.util.UUID

/**
 * Domain model for PendingSync
 * Queue for offline changes to be synced
 * 
 * Represents changes that need to be synchronized with the remote storage.
 * Contains all information needed to retry failed sync operations.
 */
data class PendingSync(
    val id: String = UUID.randomUUID().toString(),
    val operation: SyncOperation,
    val entityType: EntityType,
    val entityId: String,
    val data: String, // JSON representation of the entity
    val timestamp: Instant = Instant.now(),
    val retryCount: Int = 0,
    val lastError: String? = null
) {
    fun isValid(): Boolean {
        return entityId.isNotBlank() && data.isNotBlank()
    }
    
    // Check if this is a create operation
    val isCreate: Boolean get() = operation == SyncOperation.CREATE
    
    // Check if this is an update operation
    val isUpdate: Boolean get() = operation == SyncOperation.UPDATE
    
    // Check if this is a delete operation
    val isDelete: Boolean get() = operation == SyncOperation.DELETE
    
    // Check if retry count exceeds maximum allowed retries
    fun shouldRetry(maxRetries: Int = 3): Boolean {
        return retryCount < maxRetries
    }
    
    // Increment retry count with error message
    fun withRetryIncrement(error: String): PendingSync {
        return this.copy(
            retryCount = retryCount + 1,
            lastError = error
        )
    }
    
    companion object {
        fun create(
            operation: SyncOperation,
            entityType: EntityType,
            entityId: String,
            data: String
        ): PendingSync {
            return PendingSync(
                operation = operation,
                entityType = entityType,
                entityId = entityId,
                data = data
            )
        }
    }
}

/**
 * Sync operations
 */
enum class SyncOperation {
    CREATE, UPDATE, DELETE
}

/**
 * Entity types for sync
 */
enum class EntityType {
    RECIPE, INGREDIENT, RECIPE_IMAGE, DEVICE, COOKBOOK, SHARING_LINK
}