package com.ourcookbook.data.datasource.local

import com.ourcookbook.data.db.dao.TombstoneDao
import com.ourcookbook.data.db.entity.TombstoneEntity
import com.ourcookbook.domain.model.Tombstone
import com.ourcookbook.domain.model.EntityType
import com.ourcookbook.domain.service.ChecksumService
import com.ourcookbook.data.db.DatabaseConverters
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import javax.inject.Inject

/**
 * Local data source implementation for Tombstone operations
 */
class TombstoneLocalDataSource @Inject constructor(
    private val tombstoneDao: TombstoneDao,
    private val checksumService: ChecksumService,
    private val converters: DatabaseConverters
) : ITombstoneLocalDataSource {
    
    override suspend fun insert(tombstone: TombstoneEntity): Long {
        return tombstoneDao.insert(tombstone)
    }
    
    override suspend fun delete(id: String): Int {
        return tombstoneDao.delete(id)
    }
    
    override suspend fun deleteByEntity(entityType: EntityType, entityId: String): Int {
        return tombstoneDao.deleteByEntity(entityType, entityId)
    }
    
    override suspend fun deleteBefore(before: Instant): Int {
        return tombstoneDao.deleteBefore(before)
    }
    
    override suspend fun deleteAll(): Int {
        return tombstoneDao.deleteAll()
    }
    
    override suspend fun getById(id: String): TombstoneEntity? {
        return tombstoneDao.getById(id)
    }
    
    override suspend fun getByEntity(entityType: EntityType, entityId: String): TombstoneEntity? {
        return tombstoneDao.getByEntity(entityType, entityId)
    }
    
    override suspend fun getByType(entityType: EntityType): List<TombstoneEntity> {
        return tombstoneDao.getByType(entityType)
    }
    
    override suspend fun getByDevice(deviceId: String): List<TombstoneEntity> {
        return tombstoneDao.getByDevice(deviceId)
    }
    
    override suspend fun getSince(since: Instant): List<TombstoneEntity> {
        return tombstoneDao.getSince(since)
    }

    override suspend fun getAll(): List<TombstoneEntity> {
        return tombstoneDao.getAll()
    }

    override suspend fun toDomainModel(entity: TombstoneEntity): Tombstone {
        return Tombstone(
            id = entity.id,
            entityType = entity.entityType,
            entityId = entity.entityId,
            deletedAt = entity.deletedAt,
            deletedByDeviceId = entity.deletedByDeviceId,
            checksum = entity.checksum,
            versionVector = converters.fromJsonToVersionVector(entity.versionVectorJson)
        )
    }
    
    override suspend fun toEntity(domainModel: Tombstone): TombstoneEntity {
        return TombstoneEntity(
            id = domainModel.id,
            entityType = domainModel.entityType,
            entityId = domainModel.entityId,
            deletedAt = domainModel.deletedAt,
            deletedByDeviceId = domainModel.deletedByDeviceId,
            checksum = domainModel.checksum,
            versionVectorJson = converters.fromVersionVectorToJson(domainModel.versionVector)
        )
    }
    
    override suspend fun validateChecksum(entity: TombstoneEntity): Boolean {
        val data = "${entity.id}|${entity.entityType}|${entity.entityId}|${entity.deletedAt}|${entity.deletedByDeviceId}|${entity.checksum}|${entity.versionVectorJson}"
        val expectedChecksum = checksumService.calculateChecksum(data)
        return entity.checksum == expectedChecksum
    }
    
    override suspend fun updateChecksum(entity: TombstoneEntity): TombstoneEntity {
        val data = "${entity.id}|${entity.entityType}|${entity.entityId}|${entity.deletedAt}|${entity.deletedByDeviceId}|${entity.versionVectorJson}"
        val newChecksum = checksumService.calculateChecksum(data)
        return entity.copy(checksum = newChecksum)
    }
}

/**
 * Interface for Tombstone local data source operations
 */
interface ITombstoneLocalDataSource {
    suspend fun insert(tombstone: TombstoneEntity): Long
    suspend fun delete(id: String): Int
    suspend fun deleteByEntity(entityType: EntityType, entityId: String): Int
    suspend fun deleteBefore(before: Instant): Int
    suspend fun deleteAll(): Int
    suspend fun getById(id: String): TombstoneEntity?
    suspend fun getByEntity(entityType: EntityType, entityId: String): TombstoneEntity?
    suspend fun getByType(entityType: EntityType): List<TombstoneEntity>
    suspend fun getByDevice(deviceId: String): List<TombstoneEntity>
    suspend fun getSince(since: Instant): List<TombstoneEntity>
    suspend fun getAll(): List<TombstoneEntity>
    
    // Domain model conversion
    suspend fun toDomainModel(entity: TombstoneEntity): Tombstone
    suspend fun toEntity(domainModel: Tombstone): TombstoneEntity
    
    // Checksum validation
    suspend fun validateChecksum(entity: TombstoneEntity): Boolean
    suspend fun updateChecksum(entity: TombstoneEntity): TombstoneEntity
}