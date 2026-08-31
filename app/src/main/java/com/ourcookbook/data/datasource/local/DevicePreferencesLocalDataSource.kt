package com.ourcookbook.data.datasource.local

import com.ourcookbook.data.db.dao.DevicePreferencesDao
import com.ourcookbook.data.db.entity.DevicePreferencesEntity
import com.ourcookbook.domain.model.DevicePreferences
import com.ourcookbook.domain.model.FontSize
import com.ourcookbook.domain.model.MeasurementSystem
import com.ourcookbook.domain.model.SyncFrequency
import com.ourcookbook.domain.model.ThemePreference
import com.ourcookbook.domain.service.ChecksumService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Local data source implementation for DevicePreferences operations
 */
class DevicePreferencesLocalDataSource @Inject constructor(
    private val preferencesDao: DevicePreferencesDao,
    private val checksumService: ChecksumService
) : IDevicePreferencesLocalDataSource {
    
    override suspend fun insert(preferences: DevicePreferencesEntity): Long {
        return preferencesDao.insert(preferences)
    }
    
    override suspend fun update(preferences: DevicePreferencesEntity): Int {
        return preferencesDao.update(preferences)
    }
    
    override suspend fun delete(id: String): Int {
        return preferencesDao.delete(id)
    }
    
    override suspend fun deleteByDevice(deviceId: String): Int {
        return preferencesDao.deleteByDevice(deviceId)
    }
    
    override suspend fun deleteAll(): Int {
        return preferencesDao.deleteAll()
    }
    
    override suspend fun getById(id: String): DevicePreferencesEntity? {
        return preferencesDao.getById(id)
    }
    
    override suspend fun getByDevice(deviceId: String): DevicePreferencesEntity? {
        return preferencesDao.getByDevice(deviceId)
    }
    
    override suspend fun getAll(): List<DevicePreferencesEntity> {
        return preferencesDao.getAll()
    }
    
    override suspend fun toDomainModel(entity: DevicePreferencesEntity): DevicePreferences {
        return DevicePreferences(
            id = entity.id,
            deviceId = entity.deviceId,
            theme = entity.theme.name,
            syncFrequency = entity.syncFrequency.name,
            autoSyncEnabled = entity.autoSync,
            language = entity.language,
            fontSize = entity.fontSize.name
        )
    }
    
    override suspend fun toEntity(domainModel: DevicePreferences): DevicePreferencesEntity {
        return DevicePreferencesEntity(
            id = domainModel.id,
            deviceId = domainModel.deviceId,
            theme = runCatching { ThemePreference.valueOf(domainModel.theme) }.getOrDefault(ThemePreference.SYSTEM),
            measurementSystem = MeasurementSystem.IMPERIAL,
            syncEnabled = domainModel.autoSyncEnabled,
            autoSync = domainModel.autoSyncEnabled,
            syncFrequency = runCatching { SyncFrequency.valueOf(domainModel.syncFrequency) }.getOrDefault(SyncFrequency.AUTOMATIC),
            language = domainModel.language,
            fontSize = runCatching { FontSize.valueOf(domainModel.fontSize) }.getOrDefault(FontSize.MEDIUM)
        )
    }
    
    override suspend fun validateChecksum(entity: DevicePreferencesEntity): Boolean {
        val data = "${entity.id}|${entity.deviceId}|${entity.theme}|${entity.measurementSystem}|${entity.syncEnabled}|${entity.autoSync}|${entity.syncFrequency}|${entity.language}|${entity.fontSize}"
        val expectedChecksum = checksumService.calculateChecksum(data)
        return true // Preferences don't have stored checksums
    }
    
    override suspend fun updateChecksum(entity: DevicePreferencesEntity): DevicePreferencesEntity {
        return entity
    }
}

/**
 * Interface for DevicePreferences local data source operations
 */
interface IDevicePreferencesLocalDataSource {
    suspend fun insert(preferences: DevicePreferencesEntity): Long
    suspend fun update(preferences: DevicePreferencesEntity): Int
    suspend fun delete(id: String): Int
    suspend fun deleteByDevice(deviceId: String): Int
    suspend fun deleteAll(): Int
    suspend fun getById(id: String): DevicePreferencesEntity?
    suspend fun getByDevice(deviceId: String): DevicePreferencesEntity?
    suspend fun getAll(): List<DevicePreferencesEntity>
    
    // Domain model conversion
    suspend fun toDomainModel(entity: DevicePreferencesEntity): DevicePreferences
    suspend fun toEntity(domainModel: DevicePreferences): DevicePreferencesEntity
    
    // Checksum validation
    suspend fun validateChecksum(entity: DevicePreferencesEntity): Boolean
    suspend fun updateChecksum(entity: DevicePreferencesEntity): DevicePreferencesEntity
}