package com.ourcookbook.data.repository

import com.ourcookbook.data.datasource.local.ISyncLogLocalDataSource
import com.ourcookbook.domain.model.SyncLog
import com.ourcookbook.domain.model.SyncStatus
import com.ourcookbook.domain.repository.SyncLogRepository
import com.ourcookbook.domain.service.ChecksumService
import java.time.Instant
import javax.inject.Inject

/**
 * Repository implementation for SyncLog operations
 */
class SyncLogRepositoryImpl @Inject constructor(
    private val localDataSource: ISyncLogLocalDataSource,
    private val checksumService: ChecksumService
) : SyncLogRepository {
    
    override suspend fun createLog(log: SyncLog): String {
        if (!log.isValid()) {
            throw IllegalArgumentException("SyncLog is not valid")
        }
        
        val entity = localDataSource.toEntity(log)
        val entityId = localDataSource.insert(entity)
        return log.id
    }
    
    override suspend fun deleteLog(id: String) {
        localDataSource.delete(id)
    }
    
    override suspend fun deleteLogsBefore(before: Instant) {
        localDataSource.deleteBefore(before)
    }
    
    override suspend fun getLogById(id: String): SyncLog? {
        return localDataSource.getById(id)?.let { entity ->
            localDataSource.toDomainModel(entity)
        }
    }
    
    override suspend fun getLogsByDevice(deviceId: String): List<SyncLog> {
        return localDataSource.getByDevice(deviceId).map { entity ->
            localDataSource.toDomainModel(entity)
        }
    }
    
    override suspend fun getRecentLogs(limit: Int): List<SyncLog> {
        return localDataSource.getRecent(limit).map { entity ->
            localDataSource.toDomainModel(entity)
        }
    }
    
    override suspend fun getLogsByStatus(status: SyncStatus): List<SyncLog> {
        return localDataSource.getByStatus(status).map { entity ->
            localDataSource.toDomainModel(entity)
        }
    }
    
    override suspend fun getLogCount(): Int {
        return localDataSource.getRecent(Int.MAX_VALUE).size
    }
    
    override suspend fun getAllLogs(): List<SyncLog> {
        return localDataSource.getRecent(Int.MAX_VALUE).map { entity ->
            localDataSource.toDomainModel(entity)
        }
    }
    
    override suspend fun validateLogChecksum(logId: String): Boolean {
        return localDataSource.getById(logId)?.let { entity ->
            localDataSource.validateChecksum(entity)
        } ?: false
    }
    
    override suspend fun updateLogChecksum(logId: String): Boolean {
        return localDataSource.getById(logId)?.let { entity ->
            val updatedEntity = localDataSource.updateChecksum(entity)
            localDataSource.insert(updatedEntity) // Using insert as it replaces on conflict
            true
        } ?: false
    }
}