package com.ourcookbook.data.datasource.local

import com.ourcookbook.data.db.dao.PendingSyncDao
import com.ourcookbook.data.db.entity.PendingSyncEntity
import com.ourcookbook.domain.model.PendingSync
import com.ourcookbook.domain.model.EntityType
import com.ourcookbook.domain.model.SyncOperation
import com.ourcookbook.domain.service.ChecksumService
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import javax.inject.Inject

/**
 * Local data source implementation for PendingSync operations
 */
class PendingSyncLocalDataSource @Inject constructor(
    private val pendingSyncDao: PendingSyncDao,
    private val checksumService: ChecksumService
) : IPendingSyncLocalDataSource {
    
    override suspend fun insert(pending: PendingSyncEntity): Long {
        return pendingSyncDao.insert(pending)
    }
    
    override suspend fun update(pending: PendingSyncEntity): Int {
        return pendingSyncDao.update(pending)
    }
    
    override suspend fun delete(id: String): Int {
        return pendingSyncDao.delete(id)
    }
    
    override suspend fun deleteByEntity(entityId: String, entityType: EntityType): Int {
        return pendingSyncDao.deleteByEntity(entityId, entityType.name)
    }
    
    override suspend fun deleteBefore(before: Instant): Int {
        return pendingSyncDao.deleteBefore(before)
    }
    
    override suspend fun deleteAll(): Int {
        return pendingSyncDao.deleteAll()
    }
    
    override suspend fun getById(id: String): PendingSyncEntity? {
        return pendingSyncDao.getById(id)
    }
    
    override suspend fun getByType(entityType: EntityType): List<PendingSyncEntity> {
        return pendingSyncDao.getByType(entityType)
    }
    
    override suspend fun getByEntity(entityId: String, entityType: EntityType): List<PendingSyncEntity> {
        return pendingSyncDao.getByEntity(entityId, entityType)
    }
    
    override suspend fun getAll(): List<PendingSyncEntity> {
        return pendingSyncDao.getAll()
    }
    
    override suspend fun getRetryable(): List<PendingSyncEntity> {
        return pendingSyncDao.getRetryable()
    }
    
    override suspend fun count(): Int {
        return pendingSyncDao.count()
    }
    
    override suspend fun toDomainModel(entity: PendingSyncEntity): PendingSync {
        return PendingSync(
            id = entity.id,
            operation = entity.operation,
            entityType = entity.entityType,
            entityId = entity.entityId,
            data = entity.data,
            timestamp = entity.timestamp,
            retryCount = entity.retryCount,
            lastError = entity.lastError
        )
    }
    
    override suspend fun toEntity(domainModel: PendingSync): PendingSyncEntity {
        return PendingSyncEntity(
            id = domainModel.id,
            operation = domainModel.operation,
            entityType = domainModel.entityType,
            entityId = domainModel.entityId,
            data = domainModel.data,
            timestamp = domainModel.timestamp,
            retryCount = domainModel.retryCount,
            lastError = domainModel.lastError
        )
    }
    
    override suspend fun validateChecksum(entity: PendingSyncEntity): Boolean {
        val data = "${entity.id}|${entity.operation}|${entity.entityType}|${entity.entityId}|${entity.data}|${entity.timestamp}|${entity.retryCount}|${entity.lastError}"
        val expectedChecksum = checksumService.calculateChecksum(data)
        return true // Pending syncs don't have stored checksums
    }
    
    override suspend fun updateChecksum(entity: PendingSyncEntity): PendingSyncEntity {
        return entity
    }
}

/**
 * Interface for PendingSync local data source operations
 */
interface IPendingSyncLocalDataSource {
    suspend fun insert(pending: PendingSyncEntity): Long
    suspend fun update(pending: PendingSyncEntity): Int
    suspend fun delete(id: String): Int
    suspend fun deleteByEntity(entityId: String, entityType: EntityType): Int
    suspend fun deleteBefore(before: Instant): Int
    suspend fun deleteAll(): Int
    suspend fun getById(id: String): PendingSyncEntity?
    suspend fun getByType(entityType: EntityType): List<PendingSyncEntity>
    suspend fun getByEntity(entityId: String, entityType: EntityType): List<PendingSyncEntity>
    suspend fun getAll(): List<PendingSyncEntity>
    suspend fun getRetryable(): List<PendingSyncEntity>
    suspend fun count(): Int
    
    // Domain model conversion
    suspend fun toDomainModel(entity: PendingSyncEntity): PendingSync
    suspend fun toEntity(domainModel: PendingSync): PendingSyncEntity
    
    // Checksum validation
    suspend fun validateChecksum(entity: PendingSyncEntity): Boolean
    suspend fun updateChecksum(entity: PendingSyncEntity): PendingSyncEntity
}