package com.ourcookbook.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ourcookbook.domain.model.DevicePreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Settings Repository
 * Handles all settings-related data operations
 * 
 * This repository provides a unified interface for accessing and modifying
 * application settings using both DataStore (for simple preferences) and
 * Room database (for complex settings that need to be synced across devices).
 */

// DataStore preferences keys
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    // ========================================================================
    // DATASTORE KEYS
    // ========================================================================
    
    companion object {
        // Theme and appearance settings
        val THEME_KEY = stringPreferencesKey("theme")
        val FONT_SIZE_KEY = stringPreferencesKey("font_size")
        val TEXT_SCALING_KEY = floatPreferencesKey("text_scaling")
        val COLOR_BLINDNESS_MODE_KEY = stringPreferencesKey("color_blindness_mode")
        
        // Sync settings
        val SYNC_FREQUENCY_KEY = stringPreferencesKey("sync_frequency")
        val OFFLINE_MODE_KEY = booleanPreferencesKey("offline_mode")
        
        // Notification settings
        val NOTIFICATIONS_ENABLED_KEY = booleanPreferencesKey("notifications_enabled")
        val RECIPE_REMINDERS_ENABLED_KEY = booleanPreferencesKey("recipe_reminders_enabled")
        val SYNC_NOTIFICATIONS_ENABLED_KEY = booleanPreferencesKey("sync_notifications_enabled")
        val UPDATE_NOTIFICATIONS_ENABLED_KEY = booleanPreferencesKey("update_notifications_enabled")
        val NOTIFICATION_SOUND_ENABLED_KEY = booleanPreferencesKey("notification_sound_enabled")
        val NOTIFICATION_VIBRATION_ENABLED_KEY = booleanPreferencesKey("notification_vibration_enabled")
        
        // Privacy and security settings
        val APP_LOCK_ENABLED_KEY = booleanPreferencesKey("app_lock_enabled")
        val APP_LOCK_TYPE_KEY = stringPreferencesKey("app_lock_type")
        val AUTO_LOCK_TIMEOUT_KEY = intPreferencesKey("auto_lock_timeout")
        val DATA_ENCRYPTION_ENABLED_KEY = booleanPreferencesKey("data_encryption_enabled")
        
        // Accessibility settings
        val SCREEN_READER_COMPATIBILITY_KEY = booleanPreferencesKey("screen_reader_compatibility")
        val HIGH_CONTRAST_MODE_KEY = booleanPreferencesKey("high_contrast_mode")
        val REDUCE_MOTION_KEY = booleanPreferencesKey("reduce_motion")
        
        // Advanced settings
        val DEBUG_MODE_ENABLED_KEY = booleanPreferencesKey("debug_mode_enabled")
        val LOG_LEVEL_KEY = stringPreferencesKey("log_level")
        val DEVELOPER_OPTIONS_ENABLED_KEY = booleanPreferencesKey("developer_options_enabled")
        
        // Default cookbook settings
        val DEFAULT_COOKBOOK_ID_KEY = stringPreferencesKey("default_cookbook_id")
        
        // Language settings
        val LANGUAGE_KEY = stringPreferencesKey("language")
        
        // Device info
        val DEVICE_ID_KEY = stringPreferencesKey("device_id")
        val DEVICE_NAME_KEY = stringPreferencesKey("device_name")
        
        // Storage info
        val STORAGE_USAGE_KEY = stringPreferencesKey("storage_usage")
        val LAST_SYNC_TIME_KEY = stringPreferencesKey("last_sync_time")
        
        // Default values
        const val DEFAULT_THEME = "SYSTEM"
        const val DEFAULT_FONT_SIZE = "MEDIUM"
        const val DEFAULT_TEXT_SCALING = 1.0f
        const val DEFAULT_COLOR_BLINDNESS_MODE = "NONE"
        const val DEFAULT_SYNC_FREQUENCY = "AUTO"
        const val DEFAULT_OFFLINE_MODE = false
        const val DEFAULT_NOTIFICATIONS_ENABLED = true
        const val DEFAULT_APP_LOCK_ENABLED = false
        const val DEFAULT_APP_LOCK_TYPE = "PIN"
        const val DEFAULT_AUTO_LOCK_TIMEOUT = 300 // 5 minutes
        const val DEFAULT_DATA_ENCRYPTION_ENABLED = true
        const val DEFAULT_DEBUG_MODE_ENABLED = false
        const val DEFAULT_LOG_LEVEL = "INFO"
        const val DEFAULT_LANGUAGE = "en"
    }
    
    // ========================================================================
    // DEVICE PREFERENCES OPERATIONS
    // ========================================================================
    
    /**
     * Get device preferences by device ID
     */
    suspend fun getDevicePreferencesByDevice(deviceId: String): Result<DevicePreferences> {
        return try {
            val preferences = context.dataStore.data.first()
            
            val theme = preferences[THEME_KEY] ?: DEFAULT_THEME
            val fontSize = preferences[FONT_SIZE_KEY] ?: DEFAULT_FONT_SIZE
            val syncFrequency = preferences[SYNC_FREQUENCY_KEY] ?: DEFAULT_SYNC_FREQUENCY
            val offlineMode = preferences[OFFLINE_MODE_KEY] ?: DEFAULT_OFFLINE_MODE
            val notificationsEnabled = preferences[NOTIFICATIONS_ENABLED_KEY] ?: DEFAULT_NOTIFICATIONS_ENABLED
            
            val devicePreferences = DevicePreferences(
                id = UUID.randomUUID().toString(),
                deviceId = deviceId,
                theme = theme,
                fontSize = fontSize,
                syncFrequency = syncFrequency,
                offlineMode = offlineMode,
                notificationsEnabled = notificationsEnabled
            )
            
            Result.success(devicePreferences)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Create new device preferences
     */
    suspend fun createDevicePreferences(preferences: DevicePreferences): Result<DevicePreferences> {
        return try {
            // Save all preferences to DataStore
            context.dataStore.edit { settings ->
                settings[THEME_KEY] = preferences.theme
                settings[FONT_SIZE_KEY] = preferences.fontSize
                settings[SYNC_FREQUENCY_KEY] = preferences.syncFrequency
                settings[OFFLINE_MODE_KEY] = preferences.offlineMode
                settings[NOTIFICATIONS_ENABLED_KEY] = preferences.notificationsEnabled
                settings[DEVICE_ID_KEY] = preferences.deviceId
            }
            
            Result.success(preferences)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update existing device preferences
     */
    suspend fun updateDevicePreferences(preferences: DevicePreferences): Result<DevicePreferences> {
        return try {
            // Save all preferences to DataStore
            context.dataStore.edit { settings ->
                settings[THEME_KEY] = preferences.theme
                settings[FONT_SIZE_KEY] = preferences.fontSize
                settings[SYNC_FREQUENCY_KEY] = preferences.syncFrequency
                settings[OFFLINE_MODE_KEY] = preferences.offlineMode
                settings[NOTIFICATIONS_ENABLED_KEY] = preferences.notificationsEnabled
            }
            
            Result.success(preferences)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ========================================================================
    // THEME SETTINGS
    // ========================================================================
    
    /**
     * Get current theme setting
     */
    val themeFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[THEME_KEY] ?: DEFAULT_THEME
        }
    
    /**
     * Set theme
     */
    suspend fun setTheme(theme: String) {
        context.dataStore.edit { settings ->
            settings[THEME_KEY] = theme
        }
    }
    
    /**
     * Get current theme (synchronous)
     */
    suspend fun getTheme(): String {
        val preferences = context.dataStore.data.first()
        return preferences[THEME_KEY] ?: DEFAULT_THEME
    }
    
    // ========================================================================
    // FONT AND DISPLAY SETTINGS
    // ========================================================================
    
    /**
     * Get current font size setting
     */
    val fontSizeFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[FONT_SIZE_KEY] ?: DEFAULT_FONT_SIZE
        }
    
    /**
     * Set font size
     */
    suspend fun setFontSize(fontSize: String) {
        context.dataStore.edit { settings ->
            settings[FONT_SIZE_KEY] = fontSize
        }
    }
    
    /**
     * Get current text scaling
     */
    val textScalingFlow: Flow<Float> = context.dataStore.data
        .map { preferences ->
            preferences[TEXT_SCALING_KEY] ?: DEFAULT_TEXT_SCALING
        }
    
    /**
     * Set text scaling
     */
    suspend fun setTextScaling(scale: Float) {
        context.dataStore.edit { settings ->
            settings[TEXT_SCALING_KEY] = scale
        }
    }
    
    /**
     * Get current color blindness mode
     */
    val colorBlindnessModeFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[COLOR_BLINDNESS_MODE_KEY] ?: DEFAULT_COLOR_BLINDNESS_MODE
        }
    
    /**
     * Set color blindness mode
     */
    suspend fun setColorBlindnessMode(mode: String) {
        context.dataStore.edit { settings ->
            settings[COLOR_BLINDNESS_MODE_KEY] = mode
        }
    }
    
    // ========================================================================
    // SYNC SETTINGS
    // ========================================================================
    
    /**
     * Get current sync frequency
     */
    val syncFrequencyFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[SYNC_FREQUENCY_KEY] ?: DEFAULT_SYNC_FREQUENCY
        }
    
    /**
     * Set sync frequency
     */
    suspend fun setSyncFrequency(frequency: String) {
        context.dataStore.edit { settings ->
            settings[SYNC_FREQUENCY_KEY] = frequency
        }
    }
    
    /**
     * Get offline mode setting
     */
    val offlineModeFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[OFFLINE_MODE_KEY] ?: DEFAULT_OFFLINE_MODE
        }
    
    /**
     * Set offline mode
     */
    suspend fun setOfflineMode(enabled: Boolean) {
        context.dataStore.edit { settings ->
            settings[OFFLINE_MODE_KEY] = enabled
        }
    }
    
    /**
     * Get last sync time
     */
    val lastSyncTimeFlow: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[LAST_SYNC_TIME_KEY]
        }
    
    /**
     * Set last sync time
     */
    suspend fun setLastSyncTime(time: String) {
        context.dataStore.edit { settings ->
            settings[LAST_SYNC_TIME_KEY] = time
        }
    }
    
    // ========================================================================
    // NOTIFICATION SETTINGS
    // ========================================================================
    
    /**
     * Get notifications enabled setting
     */
    val notificationsEnabledFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[NOTIFICATIONS_ENABLED_KEY] ?: DEFAULT_NOTIFICATIONS_ENABLED
        }
    
    /**
     * Set notifications enabled
     */
    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { settings ->
            settings[NOTIFICATIONS_ENABLED_KEY] = enabled
        }
    }
    
    /**
     * Get recipe reminders enabled setting
     */
    val recipeRemindersEnabledFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[RECIPE_REMINDERS_ENABLED_KEY] ?: DEFAULT_NOTIFICATIONS_ENABLED
        }
    
    /**
     * Set recipe reminders enabled
     */
    suspend fun setRecipeRemindersEnabled(enabled: Boolean) {
        context.dataStore.edit { settings ->
            settings[RECIPE_REMINDERS_ENABLED_KEY] = enabled
        }
    }
    
    /**
     * Get sync notifications enabled setting
     */
    val syncNotificationsEnabledFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[SYNC_NOTIFICATIONS_ENABLED_KEY] ?: DEFAULT_NOTIFICATIONS_ENABLED
        }
    
    /**
     * Set sync notifications enabled
     */
    suspend fun setSyncNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { settings ->
            settings[SYNC_NOTIFICATIONS_ENABLED_KEY] = enabled
        }
    }
    
    /**
     * Get update notifications enabled setting
     */
    val updateNotificationsEnabledFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[UPDATE_NOTIFICATIONS_ENABLED_KEY] ?: DEFAULT_NOTIFICATIONS_ENABLED
        }
    
    /**
     * Set update notifications enabled
     */
    suspend fun setUpdateNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { settings ->
            settings[UPDATE_NOTIFICATIONS_ENABLED_KEY] = enabled
        }
    }
    
    /**
     * Get notification sound enabled setting
     */
    val notificationSoundEnabledFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[NOTIFICATION_SOUND_ENABLED_KEY] ?: true
        }
    
    /**
     * Set notification sound enabled
     */
    suspend fun setNotificationSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { settings ->
            settings[NOTIFICATION_SOUND_ENABLED_KEY] = enabled
        }
    }
    
    /**
     * Get notification vibration enabled setting
     */
    val notificationVibrationEnabledFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[NOTIFICATION_VIBRATION_ENABLED_KEY] ?: true
        }
    
    /**
     * Set notification vibration enabled
     */
    suspend fun setNotificationVibrationEnabled(enabled: Boolean) {
        context.dataStore.edit { settings ->
            settings[NOTIFICATION_VIBRATION_ENABLED_KEY] = enabled
        }
    }
    
    // ========================================================================
    // PRIVACY AND SECURITY SETTINGS
    // ========================================================================
    
    /**
     * Get app lock enabled setting
     */
    val appLockEnabledFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[APP_LOCK_ENABLED_KEY] ?: DEFAULT_APP_LOCK_ENABLED
        }
    
    /**
     * Set app lock enabled
     */
    suspend fun setAppLockEnabled(enabled: Boolean) {
        context.dataStore.edit { settings ->
            settings[APP_LOCK_ENABLED_KEY] = enabled
        }
    }
    
    /**
     * Get app lock type
     */
    val appLockTypeFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[APP_LOCK_TYPE_KEY] ?: DEFAULT_APP_LOCK_TYPE
        }
    
    /**
     * Set app lock type
     */
    suspend fun setAppLockType(type: String) {
        context.dataStore.edit { settings ->
            settings[APP_LOCK_TYPE_KEY] = type
        }
    }
    
    /**
     * Get auto lock timeout
     */
    val autoLockTimeoutFlow: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[AUTO_LOCK_TIMEOUT_KEY] ?: DEFAULT_AUTO_LOCK_TIMEOUT
        }
    
    /**
     * Set auto lock timeout
     */
    suspend fun setAutoLockTimeout(timeout: Int) {
        context.dataStore.edit { settings ->
            settings[AUTO_LOCK_TIMEOUT_KEY] = timeout
        }
    }
    
    /**
     * Get data encryption enabled setting
     */
    val dataEncryptionEnabledFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[DATA_ENCRYPTION_ENABLED_KEY] ?: DEFAULT_DATA_ENCRYPTION_ENABLED
        }
    
    /**
     * Set data encryption enabled
     */
    suspend fun setDataEncryptionEnabled(enabled: Boolean) {
        context.dataStore.edit { settings ->
            settings[DATA_ENCRYPTION_ENABLED_KEY] = enabled
        }
    }
    
    // ========================================================================
    // ACCESSIBILITY SETTINGS
    // ========================================================================
    
    /**
     * Get screen reader compatibility setting
     */
    val screenReaderCompatibilityFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[SCREEN_READER_COMPATIBILITY_KEY] ?: true
        }
    
    /**
     * Set screen reader compatibility
     */
    suspend fun setScreenReaderCompatibility(enabled: Boolean) {
        context.dataStore.edit { settings ->
            settings[SCREEN_READER_COMPATIBILITY_KEY] = enabled
        }
    }
    
    /**
     * Get high contrast mode setting
     */
    val highContrastModeFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[HIGH_CONTRAST_MODE_KEY] ?: false
        }
    
    /**
     * Set high contrast mode
     */
    suspend fun setHighContrastMode(enabled: Boolean) {
        context.dataStore.edit { settings ->
            settings[HIGH_CONTRAST_MODE_KEY] = enabled
        }
    }
    
    /**
     * Get reduce motion setting
     */
    val reduceMotionFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[REDUCE_MOTION_KEY] ?: false
        }
    
    /**
     * Set reduce motion
     */
    suspend fun setReduceMotion(enabled: Boolean) {
        context.dataStore.edit { settings ->
            settings[REDUCE_MOTION_KEY] = enabled
        }
    }
    
    // ========================================================================
    // ADVANCED SETTINGS
    // ========================================================================
    
    /**
     * Get debug mode enabled setting
     */
    val debugModeEnabledFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[DEBUG_MODE_ENABLED_KEY] ?: DEFAULT_DEBUG_MODE_ENABLED
        }
    
    /**
     * Set debug mode enabled
     */
    suspend fun setDebugModeEnabled(enabled: Boolean) {
        context.dataStore.edit { settings ->
            settings[DEBUG_MODE_ENABLED_KEY] = enabled
        }
    }
    
    /**
     * Get log level
     */
    val logLevelFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[LOG_LEVEL_KEY] ?: DEFAULT_LOG_LEVEL
        }
    
    /**
     * Set log level
     */
    suspend fun setLogLevel(level: String) {
        context.dataStore.edit { settings ->
            settings[LOG_LEVEL_KEY] = level
        }
    }
    
    /**
     * Get developer options enabled setting
     */
    val developerOptionsEnabledFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[DEVELOPER_OPTIONS_ENABLED_KEY] ?: false
        }
    
    /**
     * Set developer options enabled
     */
    suspend fun setDeveloperOptionsEnabled(enabled: Boolean) {
        context.dataStore.edit { settings ->
            settings[DEVELOPER_OPTIONS_ENABLED_KEY] = enabled
        }
    }
    
    // ========================================================================
    // DEFAULT COOKBOOK SETTINGS
    // ========================================================================
    
    /**
     * Get default cookbook ID
     */
    val defaultCookbookIdFlow: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[DEFAULT_COOKBOOK_ID_KEY]
        }
    
    /**
     * Set default cookbook ID
     */
    suspend fun setDefaultCookbookId(cookbookId: String?) {
        context.dataStore.edit { settings ->
            if (cookbookId != null) {
                settings[DEFAULT_COOKBOOK_ID_KEY] = cookbookId
            } else {
                settings.remove(DEFAULT_COOKBOOK_ID_KEY)
            }
        }
    }
    
    // ========================================================================
    // LANGUAGE SETTINGS
    // ========================================================================
    
    /**
     * Get current language setting
     */
    val languageFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[LANGUAGE_KEY] ?: DEFAULT_LANGUAGE
        }
    
    /**
     * Set language
     */
    suspend fun setLanguage(language: String) {
        context.dataStore.edit { settings ->
            settings[LANGUAGE_KEY] = language
        }
    }
    
    // ========================================================================
    // DEVICE INFO
    // ========================================================================
    
    /**
     * Get device ID
     */
    val deviceIdFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[DEVICE_ID_KEY] ?: UUID.randomUUID().toString()
        }
    
    /**
     * Set device ID
     */
    suspend fun setDeviceId(deviceId: String) {
        context.dataStore.edit { settings ->
            settings[DEVICE_ID_KEY] = deviceId
        }
    }
    
    /**
     * Get device name
     */
    val deviceNameFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[DEVICE_NAME_KEY] ?: "My Device"
        }
    
    /**
     * Set device name
     */
    suspend fun setDeviceName(name: String) {
        context.dataStore.edit { settings ->
            settings[DEVICE_NAME_KEY] = name
        }
    }
    
    // ========================================================================
    // STORAGE INFO
    // ========================================================================
    
    /**
     * Get storage usage
     */
    val storageUsageFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[STORAGE_USAGE_KEY] ?: "0"
        }
    
    /**
     * Set storage usage
     */
    suspend fun setStorageUsage(usage: String) {
        context.dataStore.edit { settings ->
            settings[STORAGE_USAGE_KEY] = usage
        }
    }
    
    // ========================================================================
    // CLEAR AND RESET OPERATIONS
    // ========================================================================
    
    /**
     * Clear all settings and reset to defaults
     */
    suspend fun clearAllSettings() {
        context.dataStore.edit { settings ->
            settings.clear()
        }
    }
    
    /**
     * Clear cache
     */
    suspend fun clearCache() {
        // Implementation would clear various caches
        // This is a placeholder for actual cache clearing logic
    }
    
    /**
     * Reset app data
     */
    suspend fun resetAppData() {
        clearAllSettings()
        // Additional reset logic would go here
    }
    
    // ========================================================================
    // MIGRATION AND UTILITY FUNCTIONS
    // ========================================================================
    
    /**
     * Migrate settings from SharedPreferences to DataStore
     */
    suspend fun migrateFromSharedPreferences(sharedPrefs: SharedPreferences) {
        try {
            val editor = context.dataStore.edit { settings ->
                // Migrate theme
                sharedPrefs.getString("theme", DEFAULT_THEME)?.let { theme ->
                    settings[THEME_KEY] = theme
                }
                
                // Migrate font size
                sharedPrefs.getString("font_size", DEFAULT_FONT_SIZE)?.let { fontSize ->
                    settings[FONT_SIZE_KEY] = fontSize
                }
                
                // Migrate sync frequency
                sharedPrefs.getString("sync_frequency", DEFAULT_SYNC_FREQUENCY)?.let { frequency ->
                    settings[SYNC_FREQUENCY_KEY] = frequency
                }
                
                // Migrate offline mode
                if (sharedPrefs.contains("offline_mode")) {
                    settings[OFFLINE_MODE_KEY] = sharedPrefs.getBoolean("offline_mode", DEFAULT_OFFLINE_MODE)
                }
                
                // Migrate notifications enabled
                if (sharedPrefs.contains("notifications_enabled")) {
                    settings[NOTIFICATIONS_ENABLED_KEY] = sharedPrefs.getBoolean("notifications_enabled", DEFAULT_NOTIFICATIONS_ENABLED)
                }
            }
            
            // Clear old SharedPreferences
            sharedPrefs.edit().clear().apply()
            
        } catch (e: Exception) {
            // Migration failed, but we don't want to crash the app
            e.printStackTrace()
        }
    }
    
    /**
     * Export all settings to a map
     */
    suspend fun exportSettings(): Map<String, Any> {
        val preferences = context.dataStore.data.first()
        val settingsMap = mutableMapOf<String, Any>()
        
        preferences.asMap().forEach { (key, value) ->
            when (key) {
                is Preferences.Key<String> -> settingsMap[key.name] = preferences[key] ?: ""
                is Preferences.Key<Boolean> -> settingsMap[key.name] = preferences[key] ?: false
                is Preferences.Key<Int> -> settingsMap[key.name] = preferences[key] ?: 0
                is Preferences.Key<Float> -> settingsMap[key.name] = preferences[key] ?: 0.0f
                is Preferences.Key<Long> -> settingsMap[key.name] = preferences[key] ?: 0L
            }
        }
        
        return settingsMap
    }
    
    /**
     * Import settings from a map
     */
    suspend fun importSettings(settings: Map<String, Any>) {
        context.dataStore.edit { preferences ->
            settings.forEach { (key, value) ->
                when (value) {
                    is String -> preferences[stringPreferencesKey(key)] = value
                    is Boolean -> preferences[booleanPreferencesKey(key)] = value
                    is Int -> preferences[intPreferencesKey(key)] = value
                    is Float -> preferences[floatPreferencesKey(key)] = value
                    is Long -> preferences[longPreferencesKey(key)] = value
                }
            }
        }
    }
    
    /**
     * Get all settings as a single flow
     */
    val allSettingsFlow: Flow<Map<String, Any>> = context.dataStore.data
        .map { preferences ->
            val settingsMap = mutableMapOf<String, Any>()
            preferences.asMap().forEach { (key, value) ->
                when (key) {
                    is Preferences.Key<String> -> settingsMap[key.name] = value ?: ""
                    is Preferences.Key<Boolean> -> settingsMap[key.name] = value ?: false
                    is Preferences.Key<Int> -> settingsMap[key.name] = value ?: 0
                    is Preferences.Key<Float> -> settingsMap[key.name] = value ?: 0.0f
                    is Preferences.Key<Long> -> settingsMap[key.name] = value ?: 0L
                }
            }
            settingsMap
        }
}