package com.ourcookbook.domain.repository

import com.ourcookbook.domain.model.PendingSync
import com.ourcookbook.domain.model.EntityType
import com.ourcookbook.domain.model.SyncOperation
import kotlinx.coroutines.flow.Flow
import java.time.Instant

/**
 * Repository interface for PendingSync operations
 * Defines the contract for pending sync data access in the domain layer
 */
interface PendingSyncRepository {
    
    // CRUD Operations
    suspend fun createPendingSync(pending: PendingSync): String
    suspend fun updatePendingSync(pending: PendingSync)
    suspend fun deletePendingSync(id: String)
    suspend fun deletePendingSyncByEntity(entityId: String, entityType: EntityType)
    suspend fun getPendingSyncById(id: String): PendingSync?
    
    // Query Operations
    suspend fun getPendingSyncsByType(entityType: EntityType): List<PendingSync>
    suspend fun getPendingSyncsByEntity(entityId: String, entityType: EntityType): List<PendingSync>
    suspend fun getAllPendingSyncs(): List<PendingSync>
    suspend fun getRetryablePendingSyncs(): List<PendingSync>
    
    // Utility Operations
    suspend fun getPendingSyncCount(): Int
    suspend fun deletePendingSyncsBefore(before: Instant)
    
    // Checksum Operations
    suspend fun validatePendingSyncChecksum(pendingId: String): Boolean
    suspend fun updatePendingSyncChecksum(pendingId: String): Boolean
}