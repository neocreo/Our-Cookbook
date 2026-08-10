package com.ourcookbook.data.datasource.local

import com.ourcookbook.data.db.dao.DeviceDao
import com.ourcookbook.data.db.entity.DeviceEntity
import com.ourcookbook.domain.model.Device
import com.ourcookbook.domain.service.ChecksumService
import com.ourcookbook.data.db.DatabaseConverters
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import javax.inject.Inject

/**
 * Local data source implementation for Device operations
 */
class DeviceLocalDataSource @Inject constructor(
    private val deviceDao: DeviceDao,
    private val checksumService: ChecksumService,
    private val converters: DatabaseConverters
) : IDeviceLocalDataSource {
    
    override suspend fun insert(device: DeviceEntity): Long {
        return deviceDao.insert(device)
    }
    
    override suspend fun update(device: DeviceEntity): Int {
        return deviceDao.update(device)
    }
    
    override suspend fun delete(id: String): Int {
        return deviceDao.delete(id)
    }
    
    override suspend fun deleteAll(): Int {
        return deviceDao.deleteAll()
    }
    
    override suspend fun getById(id: String): DeviceEntity? {
        return deviceDao.getById(id)
    }
    
    override suspend fun getByDeviceId(deviceId: String): DeviceEntity? {
        return deviceDao.getByDeviceId(deviceId)
    }
    
    override fun getAll(): Flow<List<DeviceEntity>> {
        return deviceDao.getAll()
    }
    
    override suspend fun getAllOnce(): List<DeviceEntity> {
        return deviceDao.getAllOnce()
    }
    
    override suspend fun getActiveSince(since: Instant): List<DeviceEntity> {
        return deviceDao.getActiveSince(since)
    }
    
    override suspend fun updateLastSeen(deviceId: String, timestamp: Instant): Int {
        return deviceDao.updateLastSeen(deviceId, timestamp)
    }
    
    override suspend fun toDomainModel(entity: DeviceEntity): Device {
        return Device(
            id = entity.id,
            name = entity.name,
            deviceId = entity.deviceId,
            capabilities = entity.capabilities,
            createdAt = entity.createdAt,
            lastSeenAt = entity.lastSeenAt
        )
    }
    
    override suspend fun toEntity(domainModel: Device): DeviceEntity {
        return DeviceEntity(
            id = domainModel.id,
            name = domainModel.name,
            deviceId = domainModel.deviceId,
            capabilities = domainModel.capabilities,
            createdAt = domainModel.createdAt,
            lastSeenAt = domainModel.lastSeenAt
        )
    }
    
    override suspend fun validateChecksum(entity: DeviceEntity): Boolean {
        val data = "${entity.id}|${entity.name}|${entity.deviceId}|${entity.capabilities}|${entity.createdAt}|${entity.lastSeenAt}"
        val expectedChecksum = checksumService.calculateChecksum(data)
        // Devices don't have stored checksums, validation is for sync purposes
        return true
    }
    
    override suspend fun updateChecksum(entity: DeviceEntity): DeviceEntity {
        // Devices don't have individual checksums
        return entity
    }
}

/**
 * Interface for Device local data source operations
 */
interface IDeviceLocalDataSource {
    suspend fun insert(device: DeviceEntity): Long
    suspend fun update(device: DeviceEntity): Int
    suspend fun delete(id: String): Int
    suspend fun deleteAll(): Int
    suspend fun getById(id: String): DeviceEntity?
    suspend fun getByDeviceId(deviceId: String): DeviceEntity?
    fun getAll(): Flow<List<DeviceEntity>>
    suspend fun getAllOnce(): List<DeviceEntity>
    suspend fun getActiveSince(since: Instant): List<DeviceEntity>
    suspend fun updateLastSeen(deviceId: String, timestamp: Instant): Int
    
    // Domain model conversion
    suspend fun toDomainModel(entity: DeviceEntity): Device
    suspend fun toEntity(domainModel: Device): DeviceEntity
    
    // Checksum validation
    suspend fun validateChecksum(entity: DeviceEntity): Boolean
    suspend fun updateChecksum(entity: DeviceEntity): DeviceEntity
}