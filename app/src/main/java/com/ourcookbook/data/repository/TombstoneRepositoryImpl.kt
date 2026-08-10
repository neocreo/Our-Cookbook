package com.ourcookbook.data.repository

import com.ourcookbook.data.datasource.local.ITombstoneLocalDataSource
import com.ourcookbook.domain.model.Tombstone
import com.ourcookbook.domain.model.EntityType
import com.ourcookbook.domain.repository.TombstoneRepository
import com.ourcookbook.domain.service.ChecksumService
import java.time.Instant
import javax.inject.Inject

/**
 * Repository implementation for Tombstone operations
 */
class TombstoneRepositoryImpl @Inject constructor(
    private val localDataSource: ITombstoneLocalDataSource,
    private val checksumService: ChecksumService
) : TombstoneRepository {
    
    override suspend fun createTombstone(tombstone: Tombstone): String {
        if (!tombstone.isValid()) {
            throw IllegalArgumentException("Tombstone is not valid")
        }
        
        val entity = localDataSource.toEntity(tombstone)
        val entityId = localDataSource.insert(entity)
        return tombstone.id
    }
    
    override suspend fun deleteTombstone(id: String) {
        localDataSource.delete(id)
    }
    
    override suspend fun deleteTombstoneByEntity(entityType: EntityType, entityId: String) {
        localDataSource.deleteByEntity(entityType, entityId)
    }
    
    override suspend fun getTombstoneById(id: String): Tombstone? {
        return localDataSource.getById(id)?.let { entity ->
            localDataSource.toDomainModel(entity)
        }
    }
    
    override suspend fun getTombstoneByEntity(entityType: EntityType, entityId: String): Tombstone? {
        return localDataSource.getByEntity(entityType, entityId)?.let { entity ->
            localDataSource.toDomainModel(entity)
        }
    }
    
    override suspend fun getTombstonesByType(entityType: EntityType): List<Tombstone> {
        return localDataSource.getByType(entityType).map { entity ->
            localDataSource.toDomainModel(entity)
        }
    }
    
    override suspend fun getTombstonesByDevice(deviceId: String): List<Tombstone> {
        return localDataSource.getByDevice(deviceId).map { entity ->
            localDataSource.toDomainModel(entity)
        }
    }
    
    override suspend fun getTombstonesSince(since: Instant): List<Tombstone> {
        return localDataSource.getSince(since).map { entity ->
            localDataSource.toDomainModel(entity)
        }
    }
    
    override suspend fun getTombstoneCount(): Int {
        return localDataSource.getAllTombstones().size
    }
    
    override suspend fun deleteTombstonesBefore(before: Instant) {
        localDataSource.deleteBefore(before)
    }
    
    override suspend fun getAllTombstones(): List<Tombstone> {
        return localDataSource.getAllTombstones().map { entity ->
            localDataSource.toDomainModel(entity)
        }
    }
    
    override suspend fun validateTombstoneChecksum(tombstoneId: String): Boolean {
        return localDataSource.getById(tombstoneId)?.let { entity ->
            localDataSource.validateChecksum(entity)
        } ?: false
    }
    
    override suspend fun updateTombstoneChecksum(tombstoneId: String): Boolean {
        return localDataSource.getById(tombstoneId)?.let { entity ->
            val updatedEntity = localDataSource.updateChecksum(entity)
            localDataSource.insert(updatedEntity) // Using insert as it replaces on conflict
            true
        } ?: false
    }
}