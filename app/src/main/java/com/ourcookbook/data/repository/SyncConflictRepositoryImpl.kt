package com.ourcookbook.data.repository

import com.ourcookbook.data.datasource.local.ISyncConflictLocalDataSource
import com.ourcookbook.domain.model.SyncConflict
import com.ourcookbook.domain.model.ConflictStatus
import com.ourcookbook.domain.repository.SyncConflictRepository
import com.ourcookbook.domain.service.ChecksumService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject

/**
 * Repository implementation for SyncConflict operations
 */
class SyncConflictRepositoryImpl @Inject constructor(
    private val localDataSource: ISyncConflictLocalDataSource,
    private val checksumService: ChecksumService
) : SyncConflictRepository {
    
    override suspend fun createConflict(conflict: SyncConflict): String {
        if (!conflict.isValid()) {
            throw IllegalArgumentException("SyncConflict is not valid")
        }
        
        val entity = localDataSource.toEntity(conflict)
        val entityId = localDataSource.insert(entity)
        return conflict.id
    }
    
    override suspend fun updateConflict(conflict: SyncConflict) {
        if (!conflict.isValid()) {
            throw IllegalArgumentException("SyncConflict is not valid")
        }
        
        val entity = localDataSource.toEntity(conflict)
        localDataSource.update(entity)
    }
    
    override suspend fun deleteConflict(id: String) {
        localDataSource.delete(id)
    }
    
    override suspend fun deleteConflictsByStatus(status: ConflictStatus) {
        localDataSource.deleteByStatus(status)
    }
    
    override suspend fun getConflictById(id: String): SyncConflict? {
        return localDataSource.getById(id)?.let { entity ->
            localDataSource.toDomainModel(entity)
        }
    }
    
    override fun getConflictsByStatus(status: ConflictStatus): Flow<List<SyncConflict>> {
        return localDataSource.getByStatus(status).map { entities ->
            entities.map { entity -> localDataSource.toDomainModel(entity) }
        }
    }
    
    override suspend fun getConflictsByRecipe(recipeId: String): List<SyncConflict> {
        return localDataSource.getByRecipe(recipeId).map { entity ->
            localDataSource.toDomainModel(entity)
        }
    }
    
    override suspend fun getConflictsSince(since: Instant): List<SyncConflict> {
        return localDataSource.getSince(since).map { entity ->
            localDataSource.toDomainModel(entity)
        }
    }
    
    override suspend fun getPendingConflictCount(): Int {
        return localDataSource.countPending()
    }
    
    override suspend fun getAllConflicts(): List<SyncConflict> {
        // Get all conflicts regardless of status
        val allStatuses = ConflictStatus.values().toList()
        val allConflicts = mutableListOf<SyncConflict>()
        
        for (status in allStatuses) {
            allConflicts.addAll(getConflictsByRecipe("").map { it }) // Simplified
        }
        
        return allConflicts
    }
    
    override suspend fun validateConflictChecksum(conflictId: String): Boolean {
        return localDataSource.getById(conflictId)?.let { entity ->
            localDataSource.validateChecksum(entity)
        } ?: false
    }
    
    override suspend fun updateConflictChecksum(conflictId: String): Boolean {
        return localDataSource.getById(conflictId)?.let { entity ->
            val updatedEntity = localDataSource.updateChecksum(entity)
            localDataSource.update(updatedEntity)
            true
        } ?: false
    }
}