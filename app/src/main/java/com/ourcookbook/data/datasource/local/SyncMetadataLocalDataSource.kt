package com.ourcookbook.data.datasource.local

import com.ourcookbook.data.db.dao.SyncMetadataDao
import com.ourcookbook.data.db.entity.SyncMetadataEntity
import com.ourcookbook.domain.model.SyncMetadata
import com.ourcookbook.domain.service.ChecksumService
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import javax.inject.Inject

/**
 * Local data source implementation for SyncMetadata operations
 */
class SyncMetadataLocalDataSource @Inject constructor(
    private val syncMetadataDao: SyncMetadataDao,
    private val checksumService: ChecksumService
) : ISyncMetadataLocalDataSource {
    
    override suspend fun insert(metadata: SyncMetadataEntity): Long {
        return syncMetadataDao.insert(metadata)
    }
    
    override suspend fun update(metadata: SyncMetadataEntity): Int {
        return syncMetadataDao.update(metadata)
    }
    
    override suspend fun delete(id: String): Int {
        return syncMetadataDao.delete(id)
    }
    
    override suspend fun deleteByDevice(deviceId: String): Int {
        return syncMetadataDao.deleteByDevice(deviceId)
    }
    
    override suspend fun deleteAll(): Int {
        return syncMetadataDao.deleteAll()
    }
    
    override suspend fun getById(id: String): SyncMetadataEntity? {
        return syncMetadataDao.getById(id)
    }
    
    override suspend fun getByDevice(deviceId: String): SyncMetadataEntity? {
        return syncMetadataDao.getByDevice(deviceId)
    }
    
    override suspend fun getAll(): List<SyncMetadataEntity> {
        return syncMetadataDao.getAll()
    }
    
    override suspend fun updateLastSync(deviceId: String, timestamp: Instant): Int {
        return syncMetadataDao.updateLastSync(deviceId, timestamp)
    }
    
    override suspend fun updateSyncInProgress(deviceId: String, inProgress: Boolean): Int {
        return syncMetadataDao.updateSyncInProgress(deviceId, inProgress)
    }
    
    override suspend fun toDomainModel(entity: SyncMetadataEntity): SyncMetadata {
        return SyncMetadata(
            id = entity.id,
            deviceId = entity.deviceId,
            lastSyncTimestamp = entity.lastSyncTimestamp,
            lastSuccessfulSync = entity.lastSuccessfulSync,
            syncInProgress = entity.syncInProgress,
            pendingChanges = entity.pendingChanges,
            conflictCount = entity.conflictCount
        )
    }
    
    override suspend fun toEntity(domainModel: SyncMetadata): SyncMetadataEntity {
        return SyncMetadataEntity(
            id = domainModel.id,
            deviceId = domainModel.deviceId,
            lastSyncTimestamp = domainModel.lastSyncTimestamp,
            lastSuccessfulSync = domainModel.lastSuccessfulSync,
            syncInProgress = domainModel.syncInProgress,
            pendingChanges = domainModel.pendingChanges,
            conflictCount = domainModel.conflictCount
        )
    }
    
    override suspend fun validateChecksum(entity: SyncMetadataEntity): Boolean {
        val data = "${entity.id}|${entity.deviceId}|${entity.lastSyncTimestamp}|${entity.lastSuccessfulSync}|${entity.syncInProgress}|${entity.pendingChanges}|${entity.conflictCount}"
        val expectedChecksum = checksumService.calculateChecksum(data)
        return true // Sync metadata doesn't have stored checksums
    }
    
    override suspend fun updateChecksum(entity: SyncMetadataEntity): SyncMetadataEntity {
        return entity
    }
}

/**
 * Interface for SyncMetadata local data source operations
 */
interface ISyncMetadataLocalDataSource {
    suspend fun insert(metadata: SyncMetadataEntity): Long
    suspend fun update(metadata: SyncMetadataEntity): Int
    suspend fun delete(id: String): Int
    suspend fun deleteByDevice(deviceId: String): Int
    suspend fun deleteAll(): Int
    suspend fun getById(id: String): SyncMetadataEntity?
    suspend fun getByDevice(deviceId: String): SyncMetadataEntity?
    suspend fun getAll(): List<SyncMetadataEntity>
    suspend fun updateLastSync(deviceId: String, timestamp: Instant): Int
    suspend fun updateSyncInProgress(deviceId: String, inProgress: Boolean): Int
    
    // Domain model conversion
    suspend fun toDomainModel(entity: SyncMetadataEntity): SyncMetadata
    suspend fun toEntity(domainModel: SyncMetadata): SyncMetadataEntity
    
    // Checksum validation
    suspend fun validateChecksum(entity: SyncMetadataEntity): Boolean
    suspend fun updateChecksum(entity: SyncMetadataEntity): SyncMetadataEntity
}