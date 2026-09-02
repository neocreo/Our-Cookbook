package com.ourcookbook.ui.viewmodel

import com.ourcookbook.domain.model.DevicePreferences
import java.time.LocalDateTime

/**
 * Settings State Classes
 * Contains all state management for the Settings Screen
 */

/**
 * Main Settings State
 * Holds the complete state of the settings screen
 */
data class SettingsState(
    // Loading and error states
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    
    // Device and app info
    val appVersion: String = "1.0.0",
    val buildNumber: String = "1",
    val deviceName: String = "",
    val deviceId: String = "",
    val deviceModel: String = "",
    val androidVersion: String = "",
    
    // Preferences from DevicePreferences
    val preferences: DevicePreferences? = null,
    
    // Sync status
    val syncStatus: String = "IDLE",
    val lastSyncTime: LocalDateTime? = null,
    val syncInProgress: Boolean = false,
    val syncError: String? = null,
    
    // Account info
    val userEmail: String? = null,
    val userName: String? = null,
    val isLoggedIn: Boolean = false,
    val linkedGoogleDriveAccount: String? = null,
    
    // Storage info
    val storageUsage: Long = 0L, // in bytes
    val maxStorage: Long = 0L, // in bytes
    val recipeCount: Int = 0,
    val cookbookCount: Int = 0,
    
    // Theme settings (computed from preferences)
    val theme: String = "SYSTEM",
    val fontSize: String = "MEDIUM",
    val syncFrequency: String = "AUTO",
    val offlineMode: Boolean = false,
    val notificationsEnabled: Boolean = true,
    
    // Notification settings
    val recipeRemindersEnabled: Boolean = true,
    val syncNotificationsEnabled: Boolean = true,
    val updateNotificationsEnabled: Boolean = true,
    val notificationSoundEnabled: Boolean = true,
    val notificationVibrationEnabled: Boolean = true,
    
    // Privacy and security settings
    val appLockEnabled: Boolean = false,
    val appLockType: String = "PIN", // PIN, BIOMETRIC, PATTERN
    val dataEncryptionEnabled: Boolean = true,
    val autoLockTimeout: Int = 300, // seconds
    
    // Accessibility settings
    val screenReaderCompatibility: Boolean = true,
    val highContrastMode: Boolean = false,
    val reduceMotion: Boolean = false,
    val textScaling: Float = 1.0f, // 1.0 = normal, >1.0 = larger
    val colorBlindnessMode: String = "NONE", // NONE, DEUTERANOPIA, PROTANOPIA, TRITANOPIA
    
    // Advanced settings
    val debugModeEnabled: Boolean = false,
    val logLevel: String = "INFO", // VERBOSE, DEBUG, INFO, WARN, ERROR
    val developerOptionsEnabled: Boolean = false,
    
    // Default cookbook selection
    val defaultCookbookId: String? = null,
    val defaultCookbookName: String = "Personal",
    
    // Language selection
    val language: String = "en", // ISO 639-1 language code
    
    // Navigation state
    val currentCategory: SettingsCategory? = null,
    val expandedCategories: Set<SettingsCategory> = emptySet(),
    
    // Dialog states
    val showThemeDialog: Boolean = false,
    val showFontSizeDialog: Boolean = false,
    val showSyncFrequencyDialog: Boolean = false,
    val showLanguageDialog: Boolean = false,
    val showAppLockDialog: Boolean = false,
    val showClearCacheDialog: Boolean = false,
    val showResetAppDataDialog: Boolean = false,
    val showDeleteAccountDialog: Boolean = false,
    val showExportDataDialog: Boolean = false,
    val showImportDataDialog: Boolean = false,
    
    // Action states
    val lastAction: SettingsAction? = null,
    val actionMessage: String? = null
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
            "DAILY" -> "Daily"
            "WEEKLY" -> "Weekly"
            "HOURLY" -> "Hourly"
            else -> syncFrequency
        }

    val storageUsagePercentage: Float
        get() = if (maxStorage > 0) (storageUsage.toFloat() / maxStorage.toFloat() * 100) else 0f

    val storageUsageDisplay: String
        get() {
            val usageMB = storageUsage / (1024 * 1024)
            val maxMB = if (maxStorage > 0) maxStorage / (1024 * 1024) else 0
            return if (maxStorage > 0) "$usageMB MB / $maxMB MB" else "$usageMB MB"
        }

    val lastSyncTimeDisplay: String
        get() = lastSyncTime?.let {
            val now = LocalDateTime.now()
            val hours = java.time.Duration.between(it, now).toHours()
            val minutes = java.time.Duration.between(it, now).toMinutes() % 60
            
            when {
                hours > 24 -> "${hours / 24} days ago"
                hours > 0 -> "$hours hours ago"
                minutes > 0 -> "$minutes minutes ago"
                else -> "Just now"
            }
        } ?: "Never"

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

    val logLevelDisplayName: String
        get() = logLevel

    val languageDisplayName: String
        get() = when (language) {
            "en" -> "English"
            "es" -> "Español"
            "fr" -> "Français"
            "de" -> "Deutsch"
            "it" -> "Italiano"
            else -> language
        }
}

/**
 * Settings Action Classes
 * Defines all possible actions that can be triggered from the settings screen
 */
sealed class SettingsAction {
    // Navigation actions
    data class NavigateToSyncStatus(val deviceId: String? = null) : SettingsAction()
    data class NavigateToDriveAuth(val redirectUrl: String? = null) : SettingsAction()
    data class NavigateToExport(val format: String = "JSON") : SettingsAction()
    data class NavigateToImport(val format: String = "JSON") : SettingsAction()
    data class NavigateToPrivacyPolicy(val url: String) : SettingsAction()
    object NavigateToOpenSourceLicenses : SettingsAction()
    object NavigateToContact : SettingsAction()
    object NavigateToRateApp : SettingsAction()
    object NavigateToChangelog : SettingsAction()
    data class NavigateToSubSettings(val category: SettingsCategory) : SettingsAction()
    
    // Dialog actions
    object ShowThemeDialog : SettingsAction()
    object ShowFontSizeDialog : SettingsAction()
    object ShowSyncFrequencyDialog : SettingsAction()
    object ShowLanguageDialog : SettingsAction()
    object ShowAppLockDialog : SettingsAction()
    object ShowClearCacheDialog : SettingsAction()
    object ShowResetAppDataDialog : SettingsAction()
    object ShowDeleteAccountDialog : SettingsAction()
    object ShowExportDataDialog : SettingsAction()
    object ShowImportDataDialog : SettingsAction()
    
    // Success/Error feedback
    data class ShowSuccess(val message: String) : SettingsAction()
    data class ShowError(val message: String) : SettingsAction()
    data class ShowInfo(val message: String) : SettingsAction()
    
    // System actions
    object RequestBiometricAuth : SettingsAction()
    object RequestNotificationPermission : SettingsAction()
    object RequestStoragePermission : SettingsAction()
    object OpenAppSettings : SettingsAction()
    
    // Data actions
    data class ExportDataSuccess(val filePath: String) : SettingsAction()
    data class ImportDataSuccess(val count: Int) : SettingsAction()
    object SyncCompleted : SettingsAction()
    object CacheCleared : SettingsAction()
    object AppDataReset : SettingsAction()
    object AccountDeleted : SettingsAction()
}

/**
 * Settings Event Classes
 * Defines all possible events that can be triggered from the UI
 */
sealed class SettingsEvent {
    // Loading and initialization
    object LoadSettings : SettingsEvent()
    object RefreshSettings : SettingsEvent()
    
    // Theme settings
    data class UpdateTheme(val theme: String) : SettingsEvent()
    
    // Font and display settings
    data class UpdateFontSize(val fontSize: String) : SettingsEvent()
    data class UpdateTextScaling(val scale: Float) : SettingsEvent()
    
    // Sync settings
    data class UpdateSyncFrequency(val frequency: String) : SettingsEvent()
    data class UpdateOfflineMode(val enabled: Boolean) : SettingsEvent()
    object TriggerSync : SettingsEvent()
    object CheckSyncStatus : SettingsEvent()
    
    // Notification settings
    data class UpdateNotificationsEnabled(val enabled: Boolean) : SettingsEvent()
    data class UpdateRecipeRemindersEnabled(val enabled: Boolean) : SettingsEvent()
    data class UpdateSyncNotificationsEnabled(val enabled: Boolean) : SettingsEvent()
    data class UpdateUpdateNotificationsEnabled(val enabled: Boolean) : SettingsEvent()
    data class UpdateNotificationSound(val enabled: Boolean) : SettingsEvent()
    data class UpdateNotificationVibration(val enabled: Boolean) : SettingsEvent()
    
    // Privacy and security settings
    data class UpdateAppLockEnabled(val enabled: Boolean) : SettingsEvent()
    data class UpdateAppLockType(val type: String) : SettingsEvent()
    data class UpdateAutoLockTimeout(val timeout: Int) : SettingsEvent()
    data class UpdateDataEncryptionEnabled(val enabled: Boolean) : SettingsEvent()
    object SetupBiometricAuth : SettingsEvent()
    object VerifyBiometricAuth : SettingsEvent()
    object ClearBiometricAuth : SettingsEvent()
    
    // Accessibility settings
    data class UpdateScreenReaderCompatibility(val enabled: Boolean) : SettingsEvent()
    data class UpdateHighContrastMode(val enabled: Boolean) : SettingsEvent()
    data class UpdateReduceMotion(val enabled: Boolean) : SettingsEvent()
    data class UpdateColorBlindnessMode(val mode: String) : SettingsEvent()
    
    // Advanced settings
    data class UpdateDebugModeEnabled(val enabled: Boolean) : SettingsEvent()
    data class UpdateLogLevel(val level: String) : SettingsEvent()
    data class UpdateDeveloperOptionsEnabled(val enabled: Boolean) : SettingsEvent()
    object ClearCache : SettingsEvent()
    object ResetAppData : SettingsEvent()
    
    // Default cookbook settings
    data class UpdateDefaultCookbook(val cookbookId: String?) : SettingsEvent()
    
    // Language settings
    data class UpdateLanguage(val language: String) : SettingsEvent()
    
    // Data management
    object ExportData : SettingsEvent()
    object ImportData : SettingsEvent()
    object DeleteAccount : SettingsEvent()
    
    // UI actions
    data class ToggleCategoryExpansion(val category: SettingsCategory) : SettingsEvent()
    object ClearError : SettingsEvent()
    object ClearSuccess : SettingsEvent()
    
    // Save actions
    object SaveSettings : SettingsEvent()
    object SaveAndSync : SettingsEvent()
}

/**
 * Settings Validation Result
 */
data class SettingsValidationResult(
    val isValid: Boolean,
    val errors: List<String> = emptyList(),
    val warnings: List<String> = emptyList()
)

/**
 * Settings Change Result
 */
data class SettingsChangeResult(
    val success: Boolean,
    val message: String? = null,
    val requiresRestart: Boolean = false,
    val affectedSettings: List<String> = emptyList()
)