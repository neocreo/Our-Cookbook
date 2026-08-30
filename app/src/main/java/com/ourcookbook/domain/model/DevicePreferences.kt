package com.ourcookbook.domain.model

import java.time.LocalDateTime
import java.util.UUID

/**
 * Device Preferences Model
 * Represents user preferences and settings for a specific device
 */
data class DevicePreferences(
    val id: String = UUID.randomUUID().toString(),
    val deviceId: String,
    val theme: String = "SYSTEM",
    val fontSize: String = "MEDIUM",
    val syncFrequency: String = "AUTO",
    val offlineMode: Boolean = false,
    val notificationsEnabled: Boolean = true,
    
    // Additional settings that can be added
    val language: String = "en",
    val textScaling: Float = 1.0f,
    val colorBlindnessMode: String = "NONE",
    
    // Sync-related settings
    val autoSyncEnabled: Boolean = true,
    val lastSyncTimestamp: LocalDateTime? = null,
    val syncErrorCount: Int = 0,
    
    // Privacy and security settings
    val appLockEnabled: Boolean = false,
    val appLockType: String = "PIN",
    val dataEncryptionEnabled: Boolean = true,
    val biometricAuthEnabled: Boolean = false,
    
    // Accessibility settings
    val screenReaderCompatibility: Boolean = true,
    val highContrastMode: Boolean = false,
    val reduceMotion: Boolean = false,
    
    // Advanced settings
    val debugModeEnabled: Boolean = false,
    val logLevel: String = "INFO",
    
    // Default cookbook settings
    val defaultCookbookId: String? = null,
    
    // Timestamps
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    // Computed properties for convenience
    val themeDisplayName: String
        get() = when (theme) {
            "LIGHT" -> "Light"
            "DARK" -> "Dark"
            "SYSTEM" -> "System Default"
            else -> theme
        }

    val fontSizeDisplayName: String
        get() = when (fontSize) {
            "SMALL" -> "Small"
            "MEDIUM" -> "Medium"
            "LARGE" -> "Large"
            else -> fontSize
        }

    val syncFrequencyDisplayName: String
        get() = when (syncFrequency) {
            "AUTO" -> "Auto"
            "MANUAL" -> "Manual"
            "HOURLY" -> "Hourly"
            "DAILY" -> "Daily"
            "WEEKLY" -> "Weekly"
            else -> syncFrequency
        }

    val appLockTypeDisplayName: String
        get() = when (appLockType) {
            "PIN" -> "PIN"
            "BIOMETRIC" -> "Biometric"
            "PATTERN" -> "Pattern"
            else -> appLockType
        }

    val colorBlindnessModeDisplayName: String
        get() = when (colorBlindnessMode) {
            "NONE" -> "None"
            "DEUTERANOPIA" -> "Deuteranopia (Red-Green)"
            "PROTANOPIA" -> "Protanopia (Red-Green)"
            "TRITANOPIA" -> "Tritanopia (Blue-Yellow)"
            else -> colorBlindnessMode
        }

    val languageDisplayName: String
        get() = when (language) {
            "en" -> "English"
            "es" -> "Español"
            "fr" -> "Français"
            "de" -> "Deutsch"
            "it" -> "Italiano"
            "pt" -> "Português"
            "ru" -> "Русский"
            "zh" -> "中文"
            "ja" -> "日本語"
            else -> language
        }

    /**
     * Check if preferences are valid
     */
    fun isValid(): Boolean {
        return deviceId.isNotBlank() && 
               listOf("LIGHT", "DARK", "SYSTEM").contains(theme) &&
               listOf("SMALL", "MEDIUM", "LARGE").contains(fontSize) &&
               listOf("AUTO", "MANUAL", "HOURLY", "DAILY", "WEEKLY").contains(syncFrequency)
    }

    /**
     * Create a copy with updated timestamp
     */
    fun withUpdatedTimestamp(): DevicePreferences {
        return this.copy(updatedAt = LocalDateTime.now())
    }

    /**
     * Check if sync is enabled
     */
    fun isSyncEnabled(): Boolean {
        return autoSyncEnabled && syncFrequency != "MANUAL"
    }

    /**
     * Check if app requires authentication
     */
    fun requiresAuthentication(): Boolean {
        return appLockEnabled && (appLockType == "PIN" || appLockType == "BIOMETRIC" || appLockType == "PATTERN")
    }

    /**
     * Get all settings as a map for serialization
     */
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "deviceId" to deviceId,
            "theme" to theme,
            "fontSize" to fontSize,
            "syncFrequency" to syncFrequency,
            "offlineMode" to offlineMode,
            "notificationsEnabled" to notificationsEnabled,
            "language" to language,
            "textScaling" to textScaling,
            "colorBlindnessMode" to colorBlindnessMode,
            "autoSyncEnabled" to autoSyncEnabled,
            "lastSyncTimestamp" to lastSyncTimestamp?.toString(),
            "syncErrorCount" to syncErrorCount,
            "appLockEnabled" to appLockEnabled,
            "appLockType" to appLockType,
            "dataEncryptionEnabled" to dataEncryptionEnabled,
            "biometricAuthEnabled" to biometricAuthEnabled,
            "screenReaderCompatibility" to screenReaderCompatibility,
            "highContrastMode" to highContrastMode,
            "reduceMotion" to reduceMotion,
            "debugModeEnabled" to debugModeEnabled,
            "logLevel" to logLevel,
            "defaultCookbookId" to defaultCookbookId,
            "createdAt" to createdAt.toString(),
            "updatedAt" to updatedAt.toString()
        )
    }

    companion object {
        /**
         * Create default preferences for a new device
         */
        fun createDefault(deviceId: String): DevicePreferences {
            return DevicePreferences(
                deviceId = deviceId,
                theme = "SYSTEM",
                fontSize = "MEDIUM",
                syncFrequency = "AUTO",
                offlineMode = false,
                notificationsEnabled = true,
                language = "en",
                textScaling = 1.0f,
                colorBlindnessMode = "NONE",
                autoSyncEnabled = true,
                appLockEnabled = false,
                appLockType = "PIN",
                dataEncryptionEnabled = true,
                biometricAuthEnabled = false,
                screenReaderCompatibility = true,
                highContrastMode = false,
                reduceMotion = false,
                debugModeEnabled = false,
                logLevel = "INFO",
                defaultCookbookId = null
            )
        }

        /**
         * Create preferences from a map
         */
        fun fromMap(map: Map<String, Any?>): DevicePreferences {
            return DevicePreferences(
                id = map["id"] as? String ?: UUID.randomUUID().toString(),
                deviceId = map["deviceId"] as? String ?: "",
                theme = map["theme"] as? String ?: "SYSTEM",
                fontSize = map["fontSize"] as? String ?: "MEDIUM",
                syncFrequency = map["syncFrequency"] as? String ?: "AUTO",
                offlineMode = map["offlineMode"] as? Boolean ?: false,
                notificationsEnabled = map["notificationsEnabled"] as? Boolean ?: true,
                language = map["language"] as? String ?: "en",
                textScaling = map["textScaling"] as? Float ?: 1.0f,
                colorBlindnessMode = map["colorBlindnessMode"] as? String ?: "NONE",
                autoSyncEnabled = map["autoSyncEnabled"] as? Boolean ?: true,
                lastSyncTimestamp = (map["lastSyncTimestamp"] as? String)?.let { LocalDateTime.parse(it) },
                syncErrorCount = map["syncErrorCount"] as? Int ?: 0,
                appLockEnabled = map["appLockEnabled"] as? Boolean ?: false,
                appLockType = map["appLockType"] as? String ?: "PIN",
                dataEncryptionEnabled = map["dataEncryptionEnabled"] as? Boolean ?: true,
                biometricAuthEnabled = map["biometricAuthEnabled"] as? Boolean ?: false,
                screenReaderCompatibility = map["screenReaderCompatibility"] as? Boolean ?: true,
                highContrastMode = map["highContrastMode"] as? Boolean ?: false,
                reduceMotion = map["reduceMotion"] as? Boolean ?: false,
                debugModeEnabled = map["debugModeEnabled"] as? Boolean ?: false,
                logLevel = map["logLevel"] as? String ?: "INFO",
                defaultCookbookId = map["defaultCookbookId"] as? String,
                createdAt = (map["createdAt"] as? String)?.let { LocalDateTime.parse(it) } ?: LocalDateTime.now(),
                updatedAt = (map["updatedAt"] as? String)?.let { LocalDateTime.parse(it) } ?: LocalDateTime.now()
            )
        }

        /**
         * Get valid theme options
         */
        val validThemes: List<String> = listOf("LIGHT", "DARK", "SYSTEM")

        /**
         * Get valid font size options
         */
        val validFontSizes: List<String> = listOf("SMALL", "MEDIUM", "LARGE")

        /**
         * Get valid sync frequency options
         */
        val validSyncFrequencies: List<String> = listOf("AUTO", "MANUAL", "HOURLY", "DAILY", "WEEKLY")

        /**
         * Get valid app lock types
         */
        val validAppLockTypes: List<String> = listOf("PIN", "BIOMETRIC", "PATTERN")

        /**
         * Get valid color blindness modes
         */
        val validColorBlindnessModes: List<String> = listOf("NONE", "DEUTERANOPIA", "PROTANOPIA", "TRITANOPIA")

        /**
         * Get valid log levels
         */
        val validLogLevels: List<String> = listOf("VERBOSE", "DEBUG", "INFO", "WARN", "ERROR")

        /**
         * Get valid languages
         */
        val validLanguages: List<String> = listOf("en", "es", "fr", "de", "it", "pt", "ru", "zh", "ja")
    }
}

/**
 * Theme Preference Enum
 */
enum class ThemePreference {
    LIGHT, DARK, SYSTEM
}

/**
 * Measurement System Enum
 */
enum class MeasurementSystem {
    IMPERIAL, METRIC, BOTH
}

/**
 * Sync Frequency Enum
 */
enum class SyncFrequency {
    AUTOMATIC, MANUAL, HOURLY, DAILY, WEEKLY
}

/**
 * Font Size Enum
 */
enum class FontSize {
    SMALL, NORMAL, MEDIUM, LARGE, EXTRA_LARGE
}