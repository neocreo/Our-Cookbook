package com.ourcookbook.data.repository

import com.ourcookbook.data.datasource.local.IPendingSyncLocalDataSource
import com.ourcookbook.domain.model.PendingSync
import com.ourcookbook.domain.model.EntityType
import com.ourcookbook.domain.model.SyncOperation
import com.ourcookbook.domain.repository.PendingSyncRepository
import com.ourcookbook.domain.service.ChecksumService
import java.time.Instant
import javax.inject.Inject

/**
 * Repository implementation for PendingSync operations
 */
class PendingSyncRepositoryImpl @Inject constructor(
    private val localDataSource: IPendingSyncLocalDataSource,
    private val checksumService: ChecksumService
) : PendingSyncRepository {
    
    override suspend fun createPendingSync(pending: PendingSync): String {
        if (!pending.isValid()) {
            throw IllegalArgumentException("PendingSync is not valid")
        }
        
        val entity = localDataSource.toEntity(pending)
        val entityId = localDataSource.insert(entity)
        return pending.id
    }
    
    override suspend fun updatePendingSync(pending: PendingSync) {
        if (!pending.isValid()) {
            throw IllegalArgumentException("PendingSync is not valid")
        }
        
        val entity = localDataSource.toEntity(pending)
        localDataSource.update(entity)
    }
    
    override suspend fun deletePendingSync(id: String) {
        localDataSource.delete(id)
    }
    
    override suspend fun deletePendingSyncByEntity(entityId: String, entityType: EntityType) {
        localDataSource.deleteByEntity(entityId, entityType)
    }
    
    override suspend fun getPendingSyncById(id: String): PendingSync? {
        return localDataSource.getById(id)?.let { entity ->
            localDataSource.toDomainModel(entity)
        }
    }
    
    override suspend fun getPendingSyncsByType(entityType: EntityType): List<PendingSync> {
        return localDataSource.getByType(entityType).map { entity ->
            localDataSource.toDomainModel(entity)
        }
    }
    
    override suspend fun getPendingSyncsByEntity(entityId: String, entityType: EntityType): List<PendingSync> {
        return localDataSource.getByEntity(entityId, entityType).map { entity ->
            localDataSource.toDomainModel(entity)
        }
    }
    
    override suspend fun getAllPendingSyncs(): List<PendingSync> {
        return localDataSource.getAll().map { entity ->
            localDataSource.toDomainModel(entity)
        }
    }
    
    override suspend fun getRetryablePendingSyncs(): List<PendingSync> {
        return localDataSource.getRetryable().map { entity ->
            localDataSource.toDomainModel(entity)
        }
    }
    
    override suspend fun getPendingSyncCount(): Int {
        return localDataSource.count()
    }
    
    override suspend fun deletePendingSyncsBefore(before: Instant) {
        localDataSource.deleteBefore(before)
    }
    
    override suspend fun validatePendingSyncChecksum(pendingId: String): Boolean {
        return localDataSource.getById(pendingId)?.let { entity ->
            localDataSource.validateChecksum(entity)
        } ?: false
    }
    
    override suspend fun updatePendingSyncChecksum(pendingId: String): Boolean {
        return localDataSource.getById(pendingId)?.let { entity ->
            val updatedEntity = localDataSource.updateChecksum(entity)
            localDataSource.update(updatedEntity)
            true
        } ?: false
    }
}