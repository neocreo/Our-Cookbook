package com.ourcookbook.data.repository

import com.ourcookbook.domain.model.DevicePreferences
import com.ourcookbook.domain.repository.DevicePreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of DevicePreferencesRepository
 * Uses SettingsRepository for DataStore operations
 */
@Singleton
class DevicePreferencesRepositoryImpl @Inject constructor(
    private val settingsRepository: SettingsRepository
) : DevicePreferencesRepository {
    
    // ========================================================================
    // DEVICE PREFERENCES OPERATIONS
    // ========================================================================
    
    override suspend fun getDevicePreferencesByDevice(deviceId: String): Result<DevicePreferences> {
        return try {
            // Get all individual settings and combine them into a DevicePreferences object
            val theme = settingsRepository.getTheme()
            val fontSize = settingsRepository.getFontSize()
            val syncFrequency = settingsRepository.getSyncFrequency()
            val offlineMode = settingsRepository.getOfflineMode()
            val notificationsEnabled = settingsRepository.getNotificationsEnabled()
            val language = settingsRepository.getLanguage()
            val textScaling = settingsRepository.getTextScaling()
            val colorBlindnessMode = settingsRepository.getColorBlindnessMode()
            val appLockEnabled = settingsRepository.getAppLockEnabled()
            val appLockType = settingsRepository.getAppLockType()
            val autoLockTimeout = settingsRepository.getAutoLockTimeout()
            val dataEncryptionEnabled = settingsRepository.getDataEncryptionEnabled()
            val debugModeEnabled = settingsRepository.getDebugModeEnabled()
            val logLevel = settingsRepository.getLogLevel()
            val defaultCookbookId = settingsRepository.getDefaultCookbookId()
            
            val preferences = DevicePreferences(
                id = UUID.randomUUID().toString(),
                deviceId = deviceId,
                theme = theme,
                fontSize = fontSize,
                syncFrequency = syncFrequency,
                offlineMode = offlineMode,
                notificationsEnabled = notificationsEnabled,
                language = language,
                textScaling = textScaling,
                colorBlindnessMode = colorBlindnessMode,
                autoSyncEnabled = true, // Default
                appLockEnabled = appLockEnabled,
                appLockType = appLockType,
                dataEncryptionEnabled = dataEncryptionEnabled,
                biometricAuthEnabled = false, // Default
                screenReaderCompatibility = true, // Default
                highContrastMode = false, // Default
                reduceMotion = false, // Default
                debugModeEnabled = debugModeEnabled,
                logLevel = logLevel,
                defaultCookbookId = defaultCookbookId
            )
            
            Result.success(preferences)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun createDevicePreferences(preferences: DevicePreferences): Result<DevicePreferences> {
        return try {
            // Save all settings individually
            settingsRepository.setTheme(preferences.theme)
            settingsRepository.setFontSize(preferences.fontSize)
            settingsRepository.setSyncFrequency(preferences.syncFrequency)
            settingsRepository.setOfflineMode(preferences.offlineMode)
            settingsRepository.setNotificationsEnabled(preferences.notificationsEnabled)
            settingsRepository.setLanguage(preferences.language)
            settingsRepository.setTextScaling(preferences.textScaling)
            settingsRepository.setColorBlindnessMode(preferences.colorBlindnessMode)
            settingsRepository.setAppLockEnabled(preferences.appLockEnabled)
            settingsRepository.setAppLockType(preferences.appLockType)
            settingsRepository.setAutoLockTimeout(preferences.autoLockTimeout)
            settingsRepository.setDataEncryptionEnabled(preferences.dataEncryptionEnabled)
            settingsRepository.setDebugModeEnabled(preferences.debugModeEnabled)
            settingsRepository.setLogLevel(preferences.logLevel)
            preferences.defaultCookbookId?.let { cookbookId ->
                settingsRepository.setDefaultCookbookId(cookbookId)
            }
            
            Result.success(preferences)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateDevicePreferences(preferences: DevicePreferences): Result<DevicePreferences> {
        return try {
            // Update all settings individually
            settingsRepository.setTheme(preferences.theme)
            settingsRepository.setFontSize(preferences.fontSize)
            settingsRepository.setSyncFrequency(preferences.syncFrequency)
            settingsRepository.setOfflineMode(preferences.offlineMode)
            settingsRepository.setNotificationsEnabled(preferences.notificationsEnabled)
            settingsRepository.setLanguage(preferences.language)
            settingsRepository.setTextScaling(preferences.textScaling)
            settingsRepository.setColorBlindnessMode(preferences.colorBlindnessMode)
            settingsRepository.setAppLockEnabled(preferences.appLockEnabled)
            settingsRepository.setAppLockType(preferences.appLockType)
            settingsRepository.setAutoLockTimeout(preferences.autoLockTimeout)
            settingsRepository.setDataEncryptionEnabled(preferences.dataEncryptionEnabled)
            settingsRepository.setDebugModeEnabled(preferences.debugModeEnabled)
            settingsRepository.setLogLevel(preferences.logLevel)
            preferences.defaultCookbookId?.let { cookbookId ->
                settingsRepository.setDefaultCookbookId(cookbookId)
            }
            
            Result.success(preferences)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getAllDevicePreferences(userId: String): Result<List<DevicePreferences>> {
        // For now, return just the current device preferences
        // In a multi-device setup, this would query a database
        return try {
            val currentPreferences = getDevicePreferencesByDevice("current_device")
            Result.success(listOf(currentPreferences.getOrThrow()))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteDevicePreferences(deviceId: String): Result<Unit> {
        return try {
            // Clear all settings for this device
            settingsRepository.clearAllSettings()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun getDevicePreferencesFlow(deviceId: String): Flow<Result<DevicePreferences>> {
        return flow {
            try {
                val preferences = getDevicePreferencesByDevice(deviceId)
                emit(preferences)
            } catch (e: Exception) {
                emit(Result.failure(e))
            }
        }
    }
    
    // ========================================================================
    // THEME SETTINGS
    // ========================================================================
    
    override suspend fun getTheme(deviceId: String): Result<String> {
        return try {
            Result.success(settingsRepository.getTheme())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun setTheme(deviceId: String, theme: String): Result<Unit> {
        return try {
            settingsRepository.setTheme(theme)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ========================================================================
    // FONT AND DISPLAY SETTINGS
    // ========================================================================
    
    override suspend fun getFontSize(deviceId: String): Result<String> {
        return try {
            Result.success(settingsRepository.getFontSize())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun setFontSize(deviceId: String, fontSize: String): Result<Unit> {
        return try {
            settingsRepository.setFontSize(fontSize)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getTextScaling(deviceId: String): Result<Float> {
        return try {
            Result.success(settingsRepository.getTextScaling())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun setTextScaling(deviceId: String, scale: Float): Result<Unit> {
        return try {
            settingsRepository.setTextScaling(scale)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ========================================================================
    // SYNC SETTINGS
    // ========================================================================
    
    override suspend fun getSyncFrequency(deviceId: String): Result<String> {
        return try {
            Result.success(settingsRepository.getSyncFrequency())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun setSyncFrequency(deviceId: String, frequency: String): Result<Unit> {
        return try {
            settingsRepository.setSyncFrequency(frequency)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getOfflineMode(deviceId: String): Result<Boolean> {
        return try {
            Result.success(settingsRepository.getOfflineMode())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun setOfflineMode(deviceId: String, enabled: Boolean): Result<Unit> {
        return try {
            settingsRepository.setOfflineMode(enabled)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ========================================================================
    // NOTIFICATION SETTINGS
    // ========================================================================
    
    override suspend fun getNotificationsEnabled(deviceId: String): Result<Boolean> {
        return try {
            Result.success(settingsRepository.getNotificationsEnabled())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun setNotificationsEnabled(deviceId: String, enabled: Boolean): Result<Unit> {
        return try {
            settingsRepository.setNotificationsEnabled(enabled)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ========================================================================
    // PRIVACY AND SECURITY SETTINGS
    // ========================================================================
    
    override suspend fun getAppLockEnabled(deviceId: String): Result<Boolean> {
        return try {
            Result.success(settingsRepository.getAppLockEnabled())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun setAppLockEnabled(deviceId: String, enabled: Boolean): Result<Unit> {
        return try {
            settingsRepository.setAppLockEnabled(enabled)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getAppLockType(deviceId: String): Result<String> {
        return try {
            Result.success(settingsRepository.getAppLockType())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun setAppLockType(deviceId: String, type: String): Result<Unit> {
        return try {
            settingsRepository.setAppLockType(type)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getDataEncryptionEnabled(deviceId: String): Result<Boolean> {
        return try {
            Result.success(settingsRepository.getDataEncryptionEnabled())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun setDataEncryptionEnabled(deviceId: String, enabled: Boolean): Result<Unit> {
        return try {
            settingsRepository.setDataEncryptionEnabled(enabled)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ========================================================================
    // ACCESSIBILITY SETTINGS
    // ========================================================================
    
    override suspend fun getScreenReaderCompatibility(deviceId: String): Result<Boolean> {
        return try {
            Result.success(settingsRepository.getScreenReaderCompatibility())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun setScreenReaderCompatibility(deviceId: String, enabled: Boolean): Result<Unit> {
        return try {
            settingsRepository.setScreenReaderCompatibility(enabled)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getHighContrastMode(deviceId: String): Result<Boolean> {
        return try {
            Result.success(settingsRepository.getHighContrastMode())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun setHighContrastMode(deviceId: String, enabled: Boolean): Result<Unit> {
        return try {
            settingsRepository.setHighContrastMode(enabled)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getReduceMotion(deviceId: String): Result<Boolean> {
        return try {
            Result.success(settingsRepository.getReduceMotion())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun setReduceMotion(deviceId: String, enabled: Boolean): Result<Unit> {
        return try {
            settingsRepository.setReduceMotion(enabled)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ========================================================================
    // ADVANCED SETTINGS
    // ========================================================================
    
    override suspend fun getDebugModeEnabled(deviceId: String): Result<Boolean> {
        return try {
            Result.success(settingsRepository.getDebugModeEnabled())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun setDebugModeEnabled(deviceId: String, enabled: Boolean): Result<Unit> {
        return try {
            settingsRepository.setDebugModeEnabled(enabled)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getLogLevel(deviceId: String): Result<String> {
        return try {
            Result.success(settingsRepository.getLogLevel())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun setLogLevel(deviceId: String, level: String): Result<Unit> {
        return try {
            settingsRepository.setLogLevel(level)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ========================================================================
    // DEFAULT COOKBOOK SETTINGS
    // ========================================================================
    
    override suspend fun getDefaultCookbookId(deviceId: String): Result<String?> {
        return try {
            Result.success(settingsRepository.getDefaultCookbookId())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun setDefaultCookbookId(deviceId: String, cookbookId: String?): Result<Unit> {
        return try {
            settingsRepository.setDefaultCookbookId(cookbookId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ========================================================================
    // LANGUAGE SETTINGS
    // ========================================================================
    
    override suspend fun getLanguage(deviceId: String): Result<String> {
        return try {
            Result.success(settingsRepository.getLanguage())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun setLanguage(deviceId: String, language: String): Result<Unit> {
        return try {
            settingsRepository.setLanguage(language)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ========================================================================
    // UTILITY FUNCTIONS
    // ========================================================================
    
    override suspend fun exists(deviceId: String): Result<Boolean> {
        return try {
            // Check if we have any settings stored
            val theme = settingsRepository.getTheme()
            Result.success(theme != SettingsRepository.DEFAULT_THEME)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun createDefaultPreferences(deviceId: String): Result<DevicePreferences> {
        return try {
            val preferences = DevicePreferences.createDefault(deviceId)
            createDevicePreferences(preferences)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun syncPreferences(userId: String, preferences: DevicePreferences): Result<List<DevicePreferences>> {
        // For now, just update the current device preferences
        // In a multi-device setup, this would sync across all devices
        return try {
            updateDevicePreferences(preferences)
            Result.success(listOf(preferences))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun exportPreferences(deviceId: String): Result<Map<String, Any>> {
        return try {
            val preferences = getDevicePreferencesByDevice(deviceId)
            Result.success(preferences.getOrThrow().toMap().filterValues { it != null } as Map<String, Any>)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun importPreferences(deviceId: String, data: Map<String, Any>): Result<DevicePreferences> {
        return try {
            val preferences = DevicePreferences.fromMap(data)
            createDevicePreferences(preferences)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ========================================================================
    // HELPER FUNCTIONS
    // ========================================================================
    
    /**
     * Get color blindness mode
     */
    private suspend fun getColorBlindnessMode(): String {
        return try {
            settingsRepository.getColorBlindnessMode()
        } catch (e: Exception) {
            SettingsRepository.DEFAULT_COLOR_BLINDNESS_MODE
        }
    }
    
    /**
     * Get auto lock timeout
     */
    private suspend fun getAutoLockTimeout(): Int {
        return try {
            settingsRepository.getAutoLockTimeout()
        } catch (e: Exception) {
            SettingsRepository.DEFAULT_AUTO_LOCK_TIMEOUT
        }
    }
    
    /**
     * Get default cookbook ID
     */
    private suspend fun getDefaultCookbookId(): String? {
        return try {
            settingsRepository.getDefaultCookbookId()
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Get font size
     */
    private suspend fun getFontSize(): String {
        return try {
            settingsRepository.getFontSize()
        } catch (e: Exception) {
            SettingsRepository.DEFAULT_FONT_SIZE
        }
    }
    
    /**
     * Get text scaling
     */
    private suspend fun getTextScaling(): Float {
        return try {
            settingsRepository.getTextScaling()
        } catch (e: Exception) {
            SettingsRepository.DEFAULT_TEXT_SCALING
        }
    }
    
    /**
     * Get sync frequency
     */
    private suspend fun getSyncFrequency(): String {
        return try {
            settingsRepository.getSyncFrequency()
        } catch (e: Exception) {
            SettingsRepository.DEFAULT_SYNC_FREQUENCY
        }
    }
    
    /**
     * Get offline mode
     */
    private suspend fun getOfflineMode(): Boolean {
        return try {
            settingsRepository.getOfflineMode()
        } catch (e: Exception) {
            SettingsRepository.DEFAULT_OFFLINE_MODE
        }
    }
    
    /**
     * Get notifications enabled
     */
    private suspend fun getNotificationsEnabled(): Boolean {
        return try {
            settingsRepository.getNotificationsEnabled()
        } catch (e: Exception) {
            SettingsRepository.DEFAULT_NOTIFICATIONS_ENABLED
        }
    }
    
    /**
     * Get theme
     */
    private suspend fun getTheme(): String {
        return try {
            settingsRepository.getTheme()
        } catch (e: Exception) {
            SettingsRepository.DEFAULT_THEME
        }
    }
    
    /**
     * Get language
     */
    private suspend fun getLanguage(): String {
        return try {
            settingsRepository.getLanguage()
        } catch (e: Exception) {
            SettingsRepository.DEFAULT_LANGUAGE
        }
    }
    
    /**
     * Get app lock enabled
     */
    private suspend fun getAppLockEnabled(): Boolean {
        return try {
            settingsRepository.getAppLockEnabled()
        } catch (e: Exception) {
            SettingsRepository.DEFAULT_APP_LOCK_ENABLED
        }
    }
    
    /**
     * Get app lock type
     */
    private suspend fun getAppLockType(): String {
        return try {
            settingsRepository.getAppLockType()
        } catch (e: Exception) {
            SettingsRepository.DEFAULT_APP_LOCK_TYPE
        }
    }
    
    /**
     * Get data encryption enabled
     */
    private suspend fun getDataEncryptionEnabled(): Boolean {
        return try {
            settingsRepository.getDataEncryptionEnabled()
        } catch (e: Exception) {
            SettingsRepository.DEFAULT_DATA_ENCRYPTION_ENABLED
        }
    }
    
    /**
     * Get debug mode enabled
     */
    private suspend fun getDebugModeEnabled(): Boolean {
        return try {
            settingsRepository.getDebugModeEnabled()
        } catch (e: Exception) {
            SettingsRepository.DEFAULT_DEBUG_MODE_ENABLED
        }
    }
    
    /**
     * Get log level
     */
    private suspend fun getLogLevel(): String {
        return try {
            settingsRepository.getLogLevel()
        } catch (e: Exception) {
            SettingsRepository.DEFAULT_LOG_LEVEL
        }
    }
}