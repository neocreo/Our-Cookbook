package com.ourcookbook.data.datasource.local

import com.ourcookbook.data.db.dao.SyncLogDao
import com.ourcookbook.data.db.entity.SyncLogEntity
import com.ourcookbook.domain.model.SyncLog
import com.ourcookbook.domain.model.SyncStatus
import com.ourcookbook.domain.service.ChecksumService
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import javax.inject.Inject

/**
 * Local data source implementation for SyncLog operations
 */
class SyncLogLocalDataSource @Inject constructor(
    private val syncLogDao: SyncLogDao,
    private val checksumService: ChecksumService
) : ISyncLogLocalDataSource {
    
    override suspend fun insert(log: SyncLogEntity): Long {
        return syncLogDao.insert(log)
    }
    
    override suspend fun delete(id: String): Int {
        return syncLogDao.delete(id)
    }
    
    override suspend fun deleteBefore(before: Instant): Int {
        return syncLogDao.deleteBefore(before)
    }
    
    override suspend fun deleteAll(): Int {
        return syncLogDao.deleteAll()
    }
    
    override suspend fun getById(id: String): SyncLogEntity? {
        return syncLogDao.getById(id)
    }
    
    override suspend fun getByDevice(deviceId: String): List<SyncLogEntity> {
        return syncLogDao.getByDevice(deviceId)
    }
    
    override suspend fun getRecent(limit: Int): List<SyncLogEntity> {
        return syncLogDao.getRecent(limit)
    }
    
    override suspend fun getByStatus(status: SyncStatus): List<SyncLogEntity> {
        return syncLogDao.getByStatus(status)
    }
    
    override suspend fun toDomainModel(entity: SyncLogEntity): SyncLog {
        return SyncLog(
            id = entity.id,
            timestamp = entity.timestamp,
            status = entity.status,
            deviceId = entity.deviceId,
            syncedItems = entity.syncedItems,
            conflicts = entity.conflicts,
            durationMs = entity.durationMs,
            errorMessage = entity.errorMessage
        )
    }
    
    override suspend fun toEntity(domainModel: SyncLog): SyncLogEntity {
        return SyncLogEntity(
            id = domainModel.id,
            timestamp = domainModel.timestamp,
            status = domainModel.status,
            deviceId = domainModel.deviceId,
            syncedItems = domainModel.syncedItems,
            conflicts = domainModel.conflicts,
            durationMs = domainModel.durationMs,
            errorMessage = domainModel.errorMessage
        )
    }
    
    override suspend fun validateChecksum(entity: SyncLogEntity): Boolean {
        val data = "${entity.id}|${entity.timestamp}|${entity.status}|${entity.deviceId}|${entity.syncedItems}|${entity.conflicts}|${entity.durationMs}|${entity.errorMessage}"
        val expectedChecksum = checksumService.calculateChecksum(data)
        return true // Sync logs don't have stored checksums
    }
    
    override suspend fun updateChecksum(entity: SyncLogEntity): SyncLogEntity {
        return entity
    }
}

/**
 * Interface for SyncLog local data source operations
 */
interface ISyncLogLocalDataSource {
    suspend fun insert(log: SyncLogEntity): Long
    suspend fun delete(id: String): Int
    suspend fun deleteBefore(before: Instant): Int
    suspend fun deleteAll(): Int
    suspend fun getById(id: String): SyncLogEntity?
    suspend fun getByDevice(deviceId: String): List<SyncLogEntity>
    suspend fun getRecent(limit: Int): List<SyncLogEntity>
    suspend fun getByStatus(status: SyncStatus): List<SyncLogEntity>
    
    // Domain model conversion
    suspend fun toDomainModel(entity: SyncLogEntity): SyncLog
    suspend fun toEntity(domainModel: SyncLog): SyncLogEntity
    
    // Checksum validation
    suspend fun validateChecksum(entity: SyncLogEntity): Boolean
    suspend fun updateChecksum(entity: SyncLogEntity): SyncLogEntity
}