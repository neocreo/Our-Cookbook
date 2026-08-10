package com.ourcookbook.data.repository

import com.ourcookbook.data.datasource.local.IDeviceLocalDataSource
import com.ourcookbook.domain.model.Device
import com.ourcookbook.domain.repository.DeviceRepository
import com.ourcookbook.domain.service.ChecksumService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject

/**
 * Repository implementation for Device operations
 */
class DeviceRepositoryImpl @Inject constructor(
    private val localDataSource: IDeviceLocalDataSource,
    private val checksumService: ChecksumService
) : DeviceRepository {
    
    override suspend fun createDevice(device: Device): String {
        if (!device.isValid()) {
            throw IllegalArgumentException("Device is not valid")
        }
        
        val entity = localDataSource.toEntity(device)
        val entityId = localDataSource.insert(entity)
        return device.id
    }
    
    override suspend fun updateDevice(device: Device) {
        if (!device.isValid()) {
            throw IllegalArgumentException("Device is not valid")
        }
        
        val entity = localDataSource.toEntity(device)
        localDataSource.update(entity)
    }
    
    override suspend fun deleteDevice(id: String) {
        localDataSource.delete(id)
    }
    
    override suspend fun getDeviceById(id: String): Device? {
        return localDataSource.getById(id)?.let { entity ->
            localDataSource.toDomainModel(entity)
        }
    }
    
    override suspend fun getDeviceByDeviceId(deviceId: String): Device? {
        return localDataSource.getByDeviceId(deviceId)?.let { entity ->
            localDataSource.toDomainModel(entity)
        }
    }
    
    override fun getAllDevices(): Flow<List<Device>> {
        return localDataSource.getAll().map { entities ->
            entities.map { entity -> localDataSource.toDomainModel(entity) }
        }
    }
    
    override suspend fun getAllDevicesOnce(): List<Device> {
        return localDataSource.getAllOnce().map { entity ->
            localDataSource.toDomainModel(entity)
        }
    }
    
    override suspend fun getActiveDevicesSince(since: Instant): List<Device> {
        return localDataSource.getActiveSince(since).map { entity ->
            localDataSource.toDomainModel(entity)
        }
    }
    
    override suspend fun updateLastSeen(deviceId: String, timestamp: Instant): Boolean {
        return localDataSource.updateLastSeen(deviceId, timestamp) > 0
    }
    
    override suspend fun getDeviceCount(): Int {
        return localDataSource.getAllOnce().size
    }
    
    override suspend fun validateDeviceChecksum(deviceId: String): Boolean {
        return localDataSource.getById(deviceId)?.let { entity ->
            localDataSource.validateChecksum(entity)
        } ?: false
    }
    
    override suspend fun updateDeviceChecksum(deviceId: String): Boolean {
        return localDataSource.getById(deviceId)?.let { entity ->
            val updatedEntity = localDataSource.updateChecksum(entity)
            localDataSource.update(updatedEntity)
            true
        } ?: false
    }
}