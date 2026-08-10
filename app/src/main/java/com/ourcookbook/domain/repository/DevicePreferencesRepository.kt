package com.ourcookbook.domain.repository

import com.ourcookbook.domain.model.DevicePreferences
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for device preferences
 * Defines the contract for accessing and modifying device preferences
 */
interface DevicePreferencesRepository {
    
    // ========================================================================
    // DEVICE PREFERENCES OPERATIONS
    // ========================================================================
    
    /**
     * Get device preferences by device ID
     */
    suspend fun getDevicePreferencesByDevice(deviceId: String): Result<DevicePreferences>
    
    /**
     * Create new device preferences
     */
    suspend fun createDevicePreferences(preferences: DevicePreferences): Result<DevicePreferences>
    
    /**
     * Update existing device preferences
     */
    suspend fun updateDevicePreferences(preferences: DevicePreferences): Result<DevicePreferences>
    
    /**
     * Get all device preferences for a user
     */
    suspend fun getAllDevicePreferences(userId: String): Result<List<DevicePreferences>>
    
    /**
     * Delete device preferences
     */
    suspend fun deleteDevicePreferences(deviceId: String): Result<Unit>
    
    /**
     * Get device preferences flow for a specific device
     */
    fun getDevicePreferencesFlow(deviceId: String): Flow<Result<DevicePreferences>>
    
    // ========================================================================
    // THEME SETTINGS
    // ========================================================================
    
    /**
     * Get current theme for a device
     */
    suspend fun getTheme(deviceId: String): Result<String>
    
    /**
     * Set theme for a device
     */
    suspend fun setTheme(deviceId: String, theme: String): Result<Unit>
    
    // ========================================================================
    // FONT AND DISPLAY SETTINGS
    // ========================================================================
    
    /**
     * Get font size for a device
     */
    suspend fun getFontSize(deviceId: String): Result<String>
    
    /**
     * Set font size for a device
     */
    suspend fun setFontSize(deviceId: String, fontSize: String): Result<Unit>
    
    /**
     * Get text scaling for a device
     */
    suspend fun getTextScaling(deviceId: String): Result<Float>
    
    /**
     * Set text scaling for a device
     */
    suspend fun setTextScaling(deviceId: String, scale: Float): Result<Unit>
    
    // ========================================================================
    // SYNC SETTINGS
    // ========================================================================
    
    /**
     * Get sync frequency for a device
     */
    suspend fun getSyncFrequency(deviceId: String): Result<String>
    
    /**
     * Set sync frequency for a device
     */
    suspend fun setSyncFrequency(deviceId: String, frequency: String): Result<Unit>
    
    /**
     * Get offline mode setting for a device
     */
    suspend fun getOfflineMode(deviceId: String): Result<Boolean>
    
    /**
     * Set offline mode for a device
     */
    suspend fun setOfflineMode(deviceId: String, enabled: Boolean): Result<Unit>
    
    // ========================================================================
    // NOTIFICATION SETTINGS
    // ========================================================================
    
    /**
     * Get notifications enabled setting for a device
     */
    suspend fun getNotificationsEnabled(deviceId: String): Result<Boolean>
    
    /**
     * Set notifications enabled for a device
     */
    suspend fun setNotificationsEnabled(deviceId: String, enabled: Boolean): Result<Unit>
    
    // ========================================================================
    // PRIVACY AND SECURITY SETTINGS
    // ========================================================================
    
    /**
     * Get app lock enabled setting for a device
     */
    suspend fun getAppLockEnabled(deviceId: String): Result<Boolean>
    
    /**
     * Set app lock enabled for a device
     */
    suspend fun setAppLockEnabled(deviceId: String, enabled: Boolean): Result<Unit>
    
    /**
     * Get app lock type for a device
     */
    suspend fun getAppLockType(deviceId: String): Result<String>
    
    /**
     * Set app lock type for a device
     */
    suspend fun setAppLockType(deviceId: String, type: String): Result<Unit>
    
    /**
     * Get data encryption enabled setting for a device
     */
    suspend fun getDataEncryptionEnabled(deviceId: String): Result<Boolean>
    
    /**
     * Set data encryption enabled for a device
     */
    suspend fun setDataEncryptionEnabled(deviceId: String, enabled: Boolean): Result<Unit>
    
    // ========================================================================
    // ACCESSIBILITY SETTINGS
    // ========================================================================
    
    /**
     * Get screen reader compatibility setting for a device
     */
    suspend fun getScreenReaderCompatibility(deviceId: String): Result<Boolean>
    
    /**
     * Set screen reader compatibility for a device
     */
    suspend fun setScreenReaderCompatibility(deviceId: String, enabled: Boolean): Result<Unit>
    
    /**
     * Get high contrast mode setting for a device
     */
    suspend fun getHighContrastMode(deviceId: String): Result<Boolean>
    
    /**
     * Set high contrast mode for a device
     */
    suspend fun setHighContrastMode(deviceId: String, enabled: Boolean): Result<Unit>
    
    /**
     * Get reduce motion setting for a device
     */
    suspend fun getReduceMotion(deviceId: String): Result<Boolean>
    
    /**
     * Set reduce motion for a device
     */
    suspend fun setReduceMotion(deviceId: String, enabled: Boolean): Result<Unit>
    
    // ========================================================================
    // ADVANCED SETTINGS
    // ========================================================================
    
    /**
     * Get debug mode enabled setting for a device
     */
    suspend fun getDebugModeEnabled(deviceId: String): Result<Boolean>
    
    /**
     * Set debug mode enabled for a device
     */
    suspend fun setDebugModeEnabled(deviceId: String, enabled: Boolean): Result<Unit>
    
    /**
     * Get log level for a device
     */
    suspend fun getLogLevel(deviceId: String): Result<String>
    
    /**
     * Set log level for a device
     */
    suspend fun setLogLevel(deviceId: String, level: String): Result<Unit>
    
    // ========================================================================
    // DEFAULT COOKBOOK SETTINGS
    // ========================================================================
    
    /**
     * Get default cookbook ID for a device
     */
    suspend fun getDefaultCookbookId(deviceId: String): Result<String?>
    
    /**
     * Set default cookbook ID for a device
     */
    suspend fun setDefaultCookbookId(deviceId: String, cookbookId: String?): Result<Unit>
    
    // ========================================================================
    // LANGUAGE SETTINGS
    // ========================================================================
    
    /**
     * Get language setting for a device
     */
    suspend fun getLanguage(deviceId: String): Result<String>
    
    /**
     * Set language for a device
     */
    suspend fun setLanguage(deviceId: String, language: String): Result<Unit>
    
    // ========================================================================
    // UTILITY FUNCTIONS
    // ========================================================================
    
    /**
     * Check if device preferences exist for a device
     */
    suspend fun exists(deviceId: String): Result<Boolean>
    
    /**
     * Create default preferences for a new device
     */
    suspend fun createDefaultPreferences(deviceId: String): Result<DevicePreferences>
    
    /**
     * Sync preferences across devices
     */
    suspend fun syncPreferences(userId: String, preferences: DevicePreferences): Result<List<DevicePreferences>>
    
    /**
     * Export preferences for backup
     */
    suspend fun exportPreferences(deviceId: String): Result<Map<String, Any>>
    
    /**
     * Import preferences from backup
     */
    suspend fun importPreferences(deviceId: String, data: Map<String, Any>): Result<DevicePreferences>
}