package com.ourcookbook.data.repository

import com.ourcookbook.data.datasource.local.IDevicePreferencesLocalDataSource
import com.ourcookbook.domain.model.DevicePreferences
import com.ourcookbook.domain.repository.DevicePreferencesRepository
import com.ourcookbook.domain.service.ChecksumService
import javax.inject.Inject

/**
 * Repository implementation for DevicePreferences operations
 */
class DevicePreferencesRepositoryImpl @Inject constructor(
    private val localDataSource: IDevicePreferencesLocalDataSource,
    private val checksumService: ChecksumService
) : DevicePreferencesRepository {
    
    override suspend fun createPreferences(preferences: DevicePreferences): String {
        if (!preferences.isValid()) {
            throw IllegalArgumentException("DevicePreferences is not valid")
        }
        
        val entity = localDataSource.toEntity(preferences)
        val entityId = localDataSource.insert(entity)
        return preferences.id
    }
    
    override suspend fun updatePreferences(preferences: DevicePreferences) {
        if (!preferences.isValid()) {
            throw IllegalArgumentException("DevicePreferences is not valid")
        }
        
        val entity = localDataSource.toEntity(preferences)
        localDataSource.update(entity)
    }
    
    override suspend fun deletePreferences(id: String) {
        localDataSource.delete(id)
    }
    
    override suspend fun deletePreferencesByDevice(deviceId: String) {
        localDataSource.deleteByDevice(deviceId)
    }
    
    override suspend fun getPreferencesById(id: String): DevicePreferences? {
        return localDataSource.getById(id)?.let { entity ->
            localDataSource.toDomainModel(entity)
        }
    }
    
    override suspend fun getPreferencesByDevice(deviceId: String): DevicePreferences? {
        return localDataSource.getByDevice(deviceId)?.let { entity ->
            localDataSource.toDomainModel(entity)
        }
    }
    
    override suspend fun getAllPreferences(): List<DevicePreferences> {
        return localDataSource.getAll().map { entity ->
            localDataSource.toDomainModel(entity)
        }
    }
    
    override suspend fun getPreferencesCount(): Int {
        return localDataSource.getAll().size
    }
    
    override suspend fun validatePreferencesChecksum(preferencesId: String): Boolean {
        return localDataSource.getById(preferencesId)?.let { entity ->
            localDataSource.validateChecksum(entity)
        } ?: false
    }
    
    override suspend fun updatePreferencesChecksum(preferencesId: String): Boolean {
        return localDataSource.getById(preferencesId)?.let { entity ->
            val updatedEntity = localDataSource.updateChecksum(entity)
            localDataSource.update(updatedEntity)
            true
        } ?: false
    }
}