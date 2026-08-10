package com.ourcookbook.domain.repository

import com.ourcookbook.domain.model.DevicePreferences

/**
 * Repository interface for DevicePreferences operations
 * Defines the contract for device preferences data access in the domain layer
 */
interface DevicePreferencesRepository {
    
    // CRUD Operations
    suspend fun createPreferences(preferences: DevicePreferences): String
    suspend fun updatePreferences(preferences: DevicePreferences)
    suspend fun deletePreferences(id: String)
    suspend fun deletePreferencesByDevice(deviceId: String)
    suspend fun getPreferencesById(id: String): DevicePreferences?
    suspend fun getPreferencesByDevice(deviceId: String): DevicePreferences?
    
    // Query Operations
    suspend fun getAllPreferences(): List<DevicePreferences>
    
    // Utility Operations
    suspend fun getPreferencesCount(): Int
    
    // Checksum Operations
    suspend fun validatePreferencesChecksum(preferencesId: String): Boolean
    suspend fun updatePreferencesChecksum(preferencesId: String): Boolean
}