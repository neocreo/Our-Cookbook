package com.ourcookbook.data.datasource.local

import com.ourcookbook.data.db.dao.SyncConflictDao
import com.ourcookbook.data.db.entity.SyncConflictEntity
import com.ourcookbook.domain.model.SyncConflict
import com.ourcookbook.domain.model.ConflictStatus
import com.ourcookbook.domain.service.ChecksumService
import com.ourcookbook.data.db.DatabaseConverters
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import javax.inject.Inject

/**
 * Local data source implementation for SyncConflict operations
 */
class SyncConflictLocalDataSource @Inject constructor(
    private val syncConflictDao: SyncConflictDao,
    private val checksumService: ChecksumService,
    private val converters: DatabaseConverters
) : ISyncConflictLocalDataSource {
    
    override suspend fun insert(conflict: SyncConflictEntity): Long {
        return syncConflictDao.insert(conflict)
    }
    
    override suspend fun update(conflict: SyncConflictEntity): Int {
        return syncConflictDao.update(conflict)
    }
    
    override suspend fun delete(id: String): Int {
        return syncConflictDao.delete(id)
    }
    
    override suspend fun deleteByStatus(status: ConflictStatus): Int {
        return syncConflictDao.deleteByStatus(status)
    }
    
    override suspend fun deleteAll(): Int {
        return syncConflictDao.deleteAll()
    }
    
    override suspend fun getById(id: String): SyncConflictEntity? {
        return syncConflictDao.getById(id)
    }
    
    override fun getByStatus(status: ConflictStatus): Flow<List<SyncConflictEntity>> {
        return syncConflictDao.getByStatus(status)
    }
    
    override suspend fun getByRecipe(recipeId: String): List<SyncConflictEntity> {
        return syncConflictDao.getByRecipe(recipeId)
    }
    
    override suspend fun getSince(since: Instant): List<SyncConflictEntity> {
        return syncConflictDao.getSince(since)
    }
    
    override suspend fun countPending(): Int {
        return syncConflictDao.countPending()
    }
    
    override suspend fun toDomainModel(entity: SyncConflictEntity): SyncConflict {
        return SyncConflict(
            id = entity.id,
            localRecipeId = entity.localRecipeId,
            remoteRecipeId = entity.remoteRecipeId,
            localChecksum = entity.localChecksum,
            remoteChecksum = entity.remoteChecksum,
            localVersion = converters.fromJsonToVersionVector(entity.localVersionJson),
            remoteVersion = converters.fromJsonToVersionVector(entity.remoteVersionJson),
            detectedAt = entity.detectedAt,
            resolvedAt = entity.resolvedAt,
            status = entity.status,
            resolution = entity.resolutionJson?.let { converters.fromJsonToConflictResolution(it) }
        )
    }
    
    override suspend fun toEntity(domainModel: SyncConflict): SyncConflictEntity {
        return SyncConflictEntity(
            id = domainModel.id,
            localRecipeId = domainModel.localRecipeId,
            remoteRecipeId = domainModel.remoteRecipeId,
            localChecksum = domainModel.localChecksum,
            remoteChecksum = domainModel.remoteChecksum,
            localVersionJson = converters.fromVersionVectorToJson(domainModel.localVersion),
            remoteVersionJson = converters.fromVersionVectorToJson(domainModel.remoteVersion),
            detectedAt = domainModel.detectedAt,
            resolvedAt = domainModel.resolvedAt,
            status = domainModel.status,
            resolutionJson = domainModel.resolution?.let { converters.fromConflictResolutionToJson(it) }
        )
    }
    
    override suspend fun validateChecksum(entity: SyncConflictEntity): Boolean {
        val data = "${entity.id}|${entity.localRecipeId}|${entity.remoteRecipeId}|${entity.localChecksum}|${entity.remoteChecksum}|${entity.localVersionJson}|${entity.remoteVersionJson}|${entity.detectedAt}|${entity.resolvedAt}|${entity.status}|${entity.resolutionJson}"
        val expectedChecksum = checksumService.calculateChecksum(data)
        return true // Sync conflicts don't have stored checksums
    }
    
    override suspend fun updateChecksum(entity: SyncConflictEntity): SyncConflictEntity {
        return entity
    }
}

/**
 * Interface for SyncConflict local data source operations
 */
interface ISyncConflictLocalDataSource {
    suspend fun insert(conflict: SyncConflictEntity): Long
    suspend fun update(conflict: SyncConflictEntity): Int
    suspend fun delete(id: String): Int
    suspend fun deleteByStatus(status: ConflictStatus): Int
    suspend fun deleteAll(): Int
    suspend fun getById(id: String): SyncConflictEntity?
    fun getByStatus(status: ConflictStatus): Flow<List<SyncConflictEntity>>
    suspend fun getByRecipe(recipeId: String): List<SyncConflictEntity>
    suspend fun getSince(since: Instant): List<SyncConflictEntity>
    suspend fun countPending(): Int
    
    // Domain model conversion
    suspend fun toDomainModel(entity: SyncConflictEntity): SyncConflict
    suspend fun toEntity(domainModel: SyncConflict): SyncConflictEntity
    
    // Checksum validation
    suspend fun validateChecksum(entity: SyncConflictEntity): Boolean
    suspend fun updateChecksum(entity: SyncConflictEntity): SyncConflictEntity
}