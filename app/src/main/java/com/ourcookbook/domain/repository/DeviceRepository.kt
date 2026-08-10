package com.ourcookbook.domain.repository

import com.ourcookbook.domain.model.Device
import kotlinx.coroutines.flow.Flow
import java.time.Instant

/**
 * Repository interface for Device operations
 * Defines the contract for device data access in the domain layer
 */
interface DeviceRepository {
    
    // CRUD Operations
    suspend fun createDevice(device: Device): String
    suspend fun updateDevice(device: Device)
    suspend fun deleteDevice(id: String)
    suspend fun getDeviceById(id: String): Device?
    suspend fun getDeviceByDeviceId(deviceId: String): Device?
    
    // Query Operations
    fun getAllDevices(): Flow<List<Device>>
    suspend fun getAllDevicesOnce(): List<Device>
    suspend fun getActiveDevicesSince(since: Instant): List<Device>
    
    // Utility Operations
    suspend fun updateLastSeen(deviceId: String, timestamp: Instant): Boolean
    suspend fun getDeviceCount(): Int
    
    // Checksum Operations
    suspend fun validateDeviceChecksum(deviceId: String): Boolean
    suspend fun updateDeviceChecksum(deviceId: String): Boolean
}