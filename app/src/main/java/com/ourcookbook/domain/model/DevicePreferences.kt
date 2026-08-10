package com.ourcookbook.domain.model

import java.util.UUID

/**
 * Domain model for DevicePreferences
 * User preferences for a specific device
 * 
 * Contains all user-configurable preferences for a device including
 * theme settings, measurement system, sync preferences, and accessibility options.
 */
data class DevicePreferences(
    val id: String = UUID.randomUUID().toString(),
    val deviceId: String,
    val theme: ThemePreference = ThemePreference.SYSTEM,
    val measurementSystem: MeasurementSystem = MeasurementSystem.IMPERIAL,
    val syncEnabled: Boolean = true,
    val autoSync: Boolean = true,
    val syncFrequency: SyncFrequency = SyncFrequency.AUTOMATIC,
    val language: String = "en",
    val fontSize: FontSize = FontSize.NORMAL
) {
    fun isValid(): Boolean {
        return deviceId.isNotBlank() && language.isNotBlank()
    }
    
    companion object {
        fun create(
            deviceId: String,
            theme: ThemePreference = ThemePreference.SYSTEM,
            measurementSystem: MeasurementSystem = MeasurementSystem.IMPERIAL,
            syncEnabled: Boolean = true,
            autoSync: Boolean = true,
            syncFrequency: SyncFrequency = SyncFrequency.AUTOMATIC,
            language: String = "en",
            fontSize: FontSize = FontSize.NORMAL
        ): DevicePreferences {
            return DevicePreferences(
                deviceId = deviceId,
                theme = theme,
                measurementSystem = measurementSystem,
                syncEnabled = syncEnabled,
                autoSync = autoSync,
                syncFrequency = syncFrequency,
                language = language,
                fontSize = fontSize
            )
        }
    }
}

/**
 * Theme preferences
 */
enum class ThemePreference {
    LIGHT, DARK, SYSTEM
}

/**
 * Measurement systems
 */
enum class MeasurementSystem {
    IMPERIAL, METRIC, BOTH
}

/**
 * Sync frequency options
 */
enum class SyncFrequency {
    AUTOMATIC, MANUAL, HOURLY, DAILY, WEEKLY
}

/**
 * Font size preferences
 */
enum class FontSize {
    SMALL, NORMAL, LARGE, EXTRA_LARGE
}