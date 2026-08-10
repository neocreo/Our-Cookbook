package com.ourcookbook.data.repository

import com.ourcookbook.data.datasource.local.ISyncMetadataLocalDataSource
import com.ourcookbook.domain.model.SyncMetadata
import com.ourcookbook.domain.repository.SyncMetadataRepository
import com.ourcookbook.domain.service.ChecksumService
import java.time.Instant
import javax.inject.Inject

/**
 * Repository implementation for SyncMetadata operations
 */
class SyncMetadataRepositoryImpl @Inject constructor(
    private val localDataSource: ISyncMetadataLocalDataSource,
    private val checksumService: ChecksumService
) : SyncMetadataRepository {
    
    override suspend fun createMetadata(metadata: SyncMetadata): String {
        if (!metadata.isValid()) {
            throw IllegalArgumentException("SyncMetadata is not valid")
        }
        
        val entity = localDataSource.toEntity(metadata)
        val entityId = localDataSource.insert(entity)
        return metadata.id
    }
    
    override suspend fun updateMetadata(metadata: SyncMetadata) {
        if (!metadata.isValid()) {
            throw IllegalArgumentException("SyncMetadata is not valid")
        }
        
        val entity = localDataSource.toEntity(metadata)
        localDataSource.update(entity)
    }
    
    override suspend fun deleteMetadata(id: String) {
        localDataSource.delete(id)
    }
    
    override suspend fun deleteMetadataByDevice(deviceId: String) {
        localDataSource.deleteByDevice(deviceId)
    }
    
    override suspend fun getMetadataById(id: String): SyncMetadata? {
        return localDataSource.getById(id)?.let { entity ->
            localDataSource.toDomainModel(entity)
        }
    }
    
    override suspend fun getMetadataByDevice(deviceId: String): SyncMetadata? {
        return localDataSource.getByDevice(deviceId)?.let { entity ->
            localDataSource.toDomainModel(entity)
        }
    }
    
    override suspend fun getAllMetadata(): List<SyncMetadata> {
        return localDataSource.getAll().map { entity ->
            localDataSource.toDomainModel(entity)
        }
    }
    
    override suspend fun updateLastSyncTimestamp(deviceId: String, timestamp: Instant): Boolean {
        return localDataSource.updateLastSync(deviceId, timestamp) > 0
    }
    
    override suspend fun updateSyncInProgress(deviceId: String, inProgress: Boolean): Boolean {
        return localDataSource.updateSyncInProgress(deviceId, inProgress) > 0
    }
    
    override suspend fun validateMetadataChecksum(metadataId: String): Boolean {
        return localDataSource.getById(metadataId)?.let { entity ->
            localDataSource.validateChecksum(entity)
        } ?: false
    }
    
    override suspend fun updateMetadataChecksum(metadataId: String): Boolean {
        return localDataSource.getById(metadataId)?.let { entity ->
            val updatedEntity = localDataSource.updateChecksum(entity)
            localDataSource.update(updatedEntity)
            true
        } ?: false
    }
}