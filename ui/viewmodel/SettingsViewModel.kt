package com.ourcookbook.ui.viewmodel

import android.content.Context
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ourcookbook.domain.model.DevicePreferences
import com.ourcookbook.domain.usecase.devicepreferences.GetDevicePreferencesByDevice
import com.ourcookbook.domain.usecase.devicepreferences.UpdateDevicePreferences
import com.ourcookbook.domain.usecase.devicepreferences.CreateDevicePreferences
import com.ourcookbook.domain.usecase.sync.GetSyncStatus
import com.ourcookbook.domain.usecase.sync.UpdateSyncInProgress
import com.ourcookbook.domain.usecase.sync.UpdateLastSyncTimestamp
import com.ourcookbook.ui.screens.settings.SettingsAction
import com.ourcookbook.ui.screens.settings.SettingsCategory
import com.ourcookbook.ui.screens.settings.SettingsEvent
import com.ourcookbook.ui.screens.settings.SettingsState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

/**
 * ViewModel for SettingsScreen
 * Handles app settings and preferences management
 * 
 * This ViewModel manages the complete settings state including:
 * - App settings (theme, language, font size)
 * - Account and device settings
 * - Privacy and security settings
 * - Notification settings
 * - Accessibility settings
 * - Advanced settings
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getDevicePreferencesByDevice: GetDevicePreferencesByDevice,
    private val updateDevicePreferences: UpdateDevicePreferences,
    private val createDevicePreferences: CreateDevicePreferences,
    private val getSyncStatus: GetSyncStatus,
    private val updateSyncInProgress: UpdateSyncInProgress,
    private val updateLastSyncTimestamp: UpdateLastSyncTimestamp,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    private val _actions = MutableStateFlow<SettingsAction?>(null)
    val actions: StateFlow<SettingsAction?> = _actions.asStateFlow()

    private var currentDeviceId: String = UUID.randomUUID().toString()

    init {
        initializeDeviceInfo()
        loadSettings()
    }

    /**
     * Initialize device information
     */
    private fun initializeDeviceInfo() {
        viewModelScope.launch {
            try {
                // Get device info
                val deviceName = Build.MODEL
                val deviceModel = Build.MODEL
                val androidVersion = Build.VERSION.RELEASE
                
                _state.value = _state.value.copy(
                    deviceName = deviceName,
                    deviceModel = deviceModel,
                    androidVersion = androidVersion,
                    appVersion = getAppVersion(),
                    buildNumber = Build.VERSION_CODES.toString()
                )
            } catch (e: Exception) {
                // Use default values if device info cannot be retrieved
                _state.value = _state.value.copy(
                    deviceName = "Unknown Device",
                    deviceModel = "Unknown",
                    androidVersion = "Unknown"
                )
            }
        }
    }

    /**
     * Get app version from context
     */
    private fun getAppVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "1.0.0"
        } catch (e: Exception) {
            "1.0.0"
        }
    }

    /**
     * Handle settings events
     */
    fun handleEvent(event: SettingsEvent) {
        when (event) {
            // Loading and initialization
            is SettingsEvent.LoadSettings -> loadSettings()
            is SettingsEvent.RefreshSettings -> refreshSettings()
            
            // Theme settings
            is SettingsEvent.UpdateTheme -> updateTheme(event.theme)
            
            // Font and display settings
            is SettingsEvent.UpdateFontSize -> updateFontSize(event.fontSize)
            is SettingsEvent.UpdateTextScaling -> updateTextScaling(event.scale)
            
            // Sync settings
            is SettingsEvent.UpdateSyncFrequency -> updateSyncFrequency(event.frequency)
            is SettingsEvent.UpdateOfflineMode -> updateOfflineMode(event.enabled)
            is SettingsEvent.TriggerSync -> triggerSync()
            is SettingsEvent.CheckSyncStatus -> checkSyncStatus()
            
            // Notification settings
            is SettingsEvent.UpdateNotificationsEnabled -> updateNotificationsEnabled(event.enabled)
            is SettingsEvent.UpdateRecipeRemindersEnabled -> updateRecipeRemindersEnabled(event.enabled)
            is SettingsEvent.UpdateSyncNotificationsEnabled -> updateSyncNotificationsEnabled(event.enabled)
            is SettingsEvent.UpdateUpdateNotificationsEnabled -> updateUpdateNotificationsEnabled(event.enabled)
            is SettingsEvent.UpdateNotificationSound -> updateNotificationSound(event.enabled)
            is SettingsEvent.UpdateNotificationVibration -> updateNotificationVibration(event.enabled)
            
            // Privacy and security settings
            is SettingsEvent.UpdateAppLockEnabled -> updateAppLockEnabled(event.enabled)
            is SettingsEvent.UpdateAppLockType -> updateAppLockType(event.type)
            is SettingsEvent.UpdateAutoLockTimeout -> updateAutoLockTimeout(event.timeout)
            is SettingsEvent.UpdateDataEncryptionEnabled -> updateDataEncryptionEnabled(event.enabled)
            is SettingsEvent.SetupBiometricAuth -> setupBiometricAuth()
            is SettingsEvent.VerifyBiometricAuth -> verifyBiometricAuth()
            is SettingsEvent.ClearBiometricAuth -> clearBiometricAuth()
            
            // Accessibility settings
            is SettingsEvent.UpdateScreenReaderCompatibility -> updateScreenReaderCompatibility(event.enabled)
            is SettingsEvent.UpdateHighContrastMode -> updateHighContrastMode(event.enabled)
            is SettingsEvent.UpdateReduceMotion -> updateReduceMotion(event.enabled)
            is SettingsEvent.UpdateColorBlindnessMode -> updateColorBlindnessMode(event.mode)
            
            // Advanced settings
            is SettingsEvent.UpdateDebugModeEnabled -> updateDebugModeEnabled(event.enabled)
            is SettingsEvent.UpdateLogLevel -> updateLogLevel(event.level)
            is SettingsEvent.UpdateDeveloperOptionsEnabled -> updateDeveloperOptionsEnabled(event.enabled)
            is SettingsEvent.ClearCache -> clearCache()
            is SettingsEvent.ResetAppData -> resetAppData()
            
            // Default cookbook settings
            is SettingsEvent.UpdateDefaultCookbook -> updateDefaultCookbook(event.cookbookId)
            
            // Language settings
            is SettingsEvent.UpdateLanguage -> updateLanguage(event.language)
            
            // Data management
            is SettingsEvent.ExportData -> exportData()
            is SettingsEvent.ImportData -> importData()
            is SettingsEvent.DeleteAccount -> deleteAccount()
            
            // UI actions
            is SettingsEvent.ToggleCategoryExpansion -> toggleCategoryExpansion(event.category)
            is SettingsEvent.ClearError -> clearError()
            is SettingsEvent.ClearSuccess -> clearSuccess()
            
            // Save actions
            is SettingsEvent.SaveSettings -> saveSettings()
            is SettingsEvent.SaveAndSync -> saveAndSync()
        }
    }

    /**
     * Load settings from storage
     */
    private fun loadSettings() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            try {
                // Load device preferences
                val preferencesResult = getDevicePreferencesByDevice(currentDeviceId)
                val preferences = preferencesResult.getOrNull() ?: createDefaultPreferences()
                
                // Load sync status
                val syncStatusResult = getSyncStatus(currentDeviceId)
                val syncStatus = syncStatusResult.getOrDefault("IDLE").toString()
                
                // Load storage info (mock for now)
                val storageUsage = 128L * 1024 * 1024 // 128MB
                val maxStorage = 1L * 1024 * 1024 * 1024 // 1GB
                
                _state.value = _state.value.copy(
                    isLoading = false,
                    preferences = preferences,
                    syncStatus = syncStatus,
                    deviceId = currentDeviceId,
                    deviceName = _state.value.deviceName.ifEmpty { "My Device" },
                    storageUsage = storageUsage,
                    maxStorage = maxStorage,
                    recipeCount = 42, // Mock data
                    cookbookCount = 3, // Mock data
                    
                    // Set computed properties from preferences
                    theme = preferences.theme,
                    fontSize = preferences.fontSize,
                    syncFrequency = preferences.syncFrequency,
                    offlineMode = preferences.offlineMode,
                    notificationsEnabled = preferences.notificationsEnabled
                )
                
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Failed to load settings: ${e.message}"
                )
                _actions.value = SettingsAction.ShowError("Failed to load settings: ${e.message}")
            }
        }
    }

    /**
     * Refresh settings from storage
     */
    private fun refreshSettings() {
        loadSettings()
    }

    /**
     * Create default preferences if none exist
     */
    private fun createDefaultPreferences(): DevicePreferences {
        return DevicePreferences(
            id = UUID.randomUUID().toString(),
            deviceId = currentDeviceId,
            theme = "SYSTEM",
            fontSize = "MEDIUM",
            syncFrequency = "AUTO",
            offlineMode = false,
            notificationsEnabled = true
        )
    }

    // ========================================================================
    // THEME SETTINGS
    // ========================================================================

    private fun updateTheme(theme: String) {
        val currentState = _state.value
        val validThemes = listOf("LIGHT", "DARK", "SYSTEM")
        val newTheme = if (validThemes.contains(theme)) theme else "SYSTEM"
        
        val updatedPreferences = currentState.preferences?.copy(theme = newTheme)
            ?: createDefaultPreferences().copy(theme = newTheme)
        
        _state.value = currentState.copy(
            preferences = updatedPreferences,
            theme = newTheme
        )
    }

    // ========================================================================
    // FONT AND DISPLAY SETTINGS
    // ========================================================================

    private fun updateFontSize(fontSize: String) {
        val currentState = _state.value
        val validSizes = listOf("SMALL", "MEDIUM", "LARGE")
        val newFontSize = if (validSizes.contains(fontSize)) fontSize else "MEDIUM"
        
        val updatedPreferences = currentState.preferences?.copy(fontSize = newFontSize)
            ?: createDefaultPreferences().copy(fontSize = newFontSize)
        
        _state.value = currentState.copy(
            preferences = updatedPreferences,
            fontSize = newFontSize
        )
    }

    private fun updateTextScaling(scale: Float) {
        val currentState = _state.value
        val newScale = scale.coerceIn(0.8f, 2.0f) // Limit between 80% and 200%
        
        _state.value = currentState.copy(textScaling = newScale)
    }

    // ========================================================================
    // SYNC SETTINGS
    // ========================================================================

    private fun updateSyncFrequency(frequency: String) {
        val currentState = _state.value
        val validFrequencies = listOf("AUTO", "MANUAL", "HOURLY", "DAILY", "WEEKLY")
        val newFrequency = if (validFrequencies.contains(frequency)) frequency else "AUTO"
        
        val updatedPreferences = currentState.preferences?.copy(syncFrequency = newFrequency)
            ?: createDefaultPreferences().copy(syncFrequency = newFrequency)
        
        _state.value = currentState.copy(
            preferences = updatedPreferences,
            syncFrequency = newFrequency
        )
    }

    private fun updateOfflineMode(enabled: Boolean) {
        val currentState = _state.value
        val updatedPreferences = currentState.preferences?.copy(offlineMode = enabled)
            ?: createDefaultPreferences().copy(offlineMode = enabled)
        
        _state.value = currentState.copy(
            preferences = updatedPreferences,
            offlineMode = enabled
        )
    }

    private fun triggerSync() {
        viewModelScope.launch {
            try {
                updateSyncInProgress(currentDeviceId, true)
                updateLastSyncTimestamp(currentDeviceId, LocalDateTime.now())
                
                _state.value = _state.value.copy(
                    syncInProgress = true,
                    syncStatus = "SYNCING",
                    lastSyncTime = LocalDateTime.now()
                )
                
                _actions.value = SettingsAction.NavigateToSyncStatus(currentDeviceId)
                
                // Simulate sync completion
                kotlinx.coroutines.delay(2000)
                _state.value = _state.value.copy(
                    syncInProgress = false,
                    syncStatus = "SUCCESS"
                )
                _actions.value = SettingsAction.SyncCompleted
                
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    syncInProgress = false,
                    syncStatus = "ERROR",
                    syncError = e.message
                )
                _actions.value = SettingsAction.ShowError("Failed to start sync: ${e.message}")
            }
        }
    }

    private fun checkSyncStatus() {
        viewModelScope.launch {
            try {
                val syncStatusResult = getSyncStatus(currentDeviceId)
                val syncStatus = syncStatusResult.getOrDefault("IDLE").toString()
                
                _state.value = _state.value.copy(syncStatus = syncStatus)
                
            } catch (e: Exception) {
                _actions.value = SettingsAction.ShowError("Failed to check sync status: ${e.message}")
            }
        }
    }

    // ========================================================================
    // NOTIFICATION SETTINGS
    // ========================================================================

    private fun updateNotificationsEnabled(enabled: Boolean) {
        val currentState = _state.value
        val updatedPreferences = currentState.preferences?.copy(notificationsEnabled = enabled)
            ?: createDefaultPreferences().copy(notificationsEnabled = enabled)
        
        _state.value = currentState.copy(
            preferences = updatedPreferences,
            notificationsEnabled = enabled
        )
    }

    private fun updateRecipeRemindersEnabled(enabled: Boolean) {
        _state.value = _state.value.copy(recipeRemindersEnabled = enabled)
    }

    private fun updateSyncNotificationsEnabled(enabled: Boolean) {
        _state.value = _state.value.copy(syncNotificationsEnabled = enabled)
    }

    private fun updateUpdateNotificationsEnabled(enabled: Boolean) {
        _state.value = _state.value.copy(updateNotificationsEnabled = enabled)
    }

    private fun updateNotificationSound(enabled: Boolean) {
        _state.value = _state.value.copy(notificationSoundEnabled = enabled)
    }

    private fun updateNotificationVibration(enabled: Boolean) {
        _state.value = _state.value.copy(notificationVibrationEnabled = enabled)
    }

    // ========================================================================
    // PRIVACY AND SECURITY SETTINGS
    // ========================================================================

    private fun updateAppLockEnabled(enabled: Boolean) {
        _state.value = _state.value.copy(appLockEnabled = enabled)
        
        if (enabled) {
            _actions.value = SettingsAction.ShowAppLockDialog
        }
    }

    private fun updateAppLockType(type: String) {
        val validTypes = listOf("PIN", "BIOMETRIC", "PATTERN")
        val newType = if (validTypes.contains(type)) type else "PIN"
        
        _state.value = _state.value.copy(appLockType = newType)
    }

    private fun updateAutoLockTimeout(timeout: Int) {
        val newTimeout = timeout.coerceIn(30, 3600) // Between 30 seconds and 1 hour
        _state.value = _state.value.copy(autoLockTimeout = newTimeout)
    }

    private fun updateDataEncryptionEnabled(enabled: Boolean) {
        _state.value = _state.value.copy(dataEncryptionEnabled = enabled)
    }

    private fun setupBiometricAuth() {
        _actions.value = SettingsAction.RequestBiometricAuth
    }

    private fun verifyBiometricAuth() {
        _actions.value = SettingsAction.RequestBiometricAuth
    }

    private fun clearBiometricAuth() {
        _state.value = _state.value.copy(appLockEnabled = false)
        _actions.value = SettingsAction.ShowSuccess("Biometric authentication cleared")
    }

    // ========================================================================
    // ACCESSIBILITY SETTINGS
    // ========================================================================

    private fun updateScreenReaderCompatibility(enabled: Boolean) {
        _state.value = _state.value.copy(screenReaderCompatibility = enabled)
    }

    private fun updateHighContrastMode(enabled: Boolean) {
        _state.value = _state.value.copy(highContrastMode = enabled)
    }

    private fun updateReduceMotion(enabled: Boolean) {
        _state.value = _state.value.copy(reduceMotion = enabled)
    }

    private fun updateColorBlindnessMode(mode: String) {
        val validModes = listOf("NONE", "DEUTERANOPIA", "PROTANOPIA", "TRITANOPIA")
        val newMode = if (validModes.contains(mode)) mode else "NONE"
        
        _state.value = _state.value.copy(colorBlindnessMode = newMode)
    }

    // ========================================================================
    // ADVANCED SETTINGS
    // ========================================================================

    private fun updateDebugModeEnabled(enabled: Boolean) {
        _state.value = _state.value.copy(debugModeEnabled = enabled)
    }

    private fun updateLogLevel(level: String) {
        val validLevels = listOf("VERBOSE", "DEBUG", "INFO", "WARN", "ERROR")
        val newLevel = if (validLevels.contains(level)) level else "INFO"
        
        _state.value = _state.value.copy(logLevel = newLevel)
    }

    private fun updateDeveloperOptionsEnabled(enabled: Boolean) {
        _state.value = _state.value.copy(developerOptionsEnabled = enabled)
    }

    private fun clearCache() {
        viewModelScope.launch {
            try {
                // In production, this would clear various caches
                _actions.value = SettingsAction.ShowSuccess("Cache cleared successfully")
                _actions.value = SettingsAction.CacheCleared
                
            } catch (e: Exception) {
                _actions.value = SettingsAction.ShowError("Failed to clear cache: ${e.message}")
            }
        }
    }

    private fun resetAppData() {
        _actions.value = SettingsAction.ShowResetAppDataDialog
    }

    // ========================================================================
    // DEFAULT COOKBOOK SETTINGS
    // ========================================================================

    private fun updateDefaultCookbook(cookbookId: String?) {
        _state.value = _state.value.copy(
            defaultCookbookId = cookbookId,
            defaultCookbookName = if (cookbookId == null) "Personal" else "Cookbook $cookbookId"
        )
    }

    // ========================================================================
    // LANGUAGE SETTINGS
    // ========================================================================

    private fun updateLanguage(language: String) {
        val validLanguages = listOf("en", "es", "fr", "de", "it", "pt", "ru", "zh", "ja")
        val newLanguage = if (validLanguages.contains(language)) language else "en"
        
        _state.value = _state.value.copy(
            language = newLanguage,
            saveSuccess = true
        )
        
        _actions.value = SettingsAction.ShowSuccess("Language changed. Some changes may require app restart.")
    }

    // ========================================================================
    // DATA MANAGEMENT
    // ========================================================================

    private fun exportData() {
        _actions.value = SettingsAction.ShowExportDataDialog
    }

    private fun importData() {
        _actions.value = SettingsAction.ShowImportDataDialog
    }

    private fun deleteAccount() {
        _actions.value = SettingsAction.ShowDeleteAccountDialog
    }

    // ========================================================================
    // UI ACTIONS
    // ========================================================================

    private fun toggleCategoryExpansion(category: SettingsCategory) {
        val currentState = _state.value
        val expandedCategories = currentState.expandedCategories.toMutableSet()
        
        if (expandedCategories.contains(category)) {
            expandedCategories.remove(category)
        } else {
            expandedCategories.add(category)
        }
        
        _state.value = currentState.copy(expandedCategories = expandedCategories)
    }

    private fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    private fun clearSuccess() {
        _state.value = _state.value.copy(saveSuccess = false)
    }

    // ========================================================================
    // SAVE ACTIONS
    // ========================================================================

    private fun saveSettings() {
        viewModelScope.launch {
            val currentState = _state.value
            val preferences = currentState.preferences ?: return@launch
            
            _state.value = currentState.copy(isSaving = true, error = null)
            
            try {
                // Create updated preferences with all current settings
                val updatedPreferences = preferences.copy(
                    theme = currentState.theme,
                    fontSize = currentState.fontSize,
                    syncFrequency = currentState.syncFrequency,
                    offlineMode = currentState.offlineMode,
                    notificationsEnabled = currentState.notificationsEnabled
                )
                
                val result = if (preferences.id.isBlank()) {
                    // New preferences
                    createDevicePreferences(updatedPreferences)
                } else {
                    // Update existing preferences
                    updateDevicePreferences(updatedPreferences)
                }
                
                result.onSuccess {
                    _state.value = currentState.copy(
                        isSaving = false,
                        saveSuccess = true,
                        preferences = updatedPreferences
                    )
                    _actions.value = SettingsAction.ShowSuccess("Settings saved successfully")
                    
                }.onFailure { e ->
                    _state.value = currentState.copy(
                        isSaving = false,
                        error = "Failed to save settings: ${e.message}"
                    )
                    _actions.value = SettingsAction.ShowError("Failed to save settings: ${e.message}")
                }
                
            } catch (e: Exception) {
                _state.value = currentState.copy(
                    isSaving = false,
                    error = "Failed to save settings: ${e.message}"
                )
                _actions.value = SettingsAction.ShowError("Failed to save settings: ${e.message}")
            }
        }
    }

    private fun saveAndSync() {
        saveSettings()
        triggerSync()
    }

    // ========================================================================
    // DIALOG ACTIONS
    // ========================================================================

    fun showThemeDialog() {
        _state.value = _state.value.copy(showThemeDialog = true)
    }

    fun hideThemeDialog() {
        _state.value = _state.value.copy(showThemeDialog = false)
    }

    fun showFontSizeDialog() {
        _state.value = _state.value.copy(showFontSizeDialog = true)
    }

    fun hideFontSizeDialog() {
        _state.value = _state.value.copy(showFontSizeDialog = false)
    }

    fun showSyncFrequencyDialog() {
        _state.value = _state.value.copy(showSyncFrequencyDialog = true)
    }

    fun hideSyncFrequencyDialog() {
        _state.value = _state.value.copy(showSyncFrequencyDialog = false)
    }

    fun showLanguageDialog() {
        _state.value = _state.value.copy(showLanguageDialog = true)
    }

    fun hideLanguageDialog() {
        _state.value = _state.value.copy(showLanguageDialog = false)
    }

    fun showAppLockDialog() {
        _state.value = _state.value.copy(showAppLockDialog = true)
    }

    fun hideAppLockDialog() {
        _state.value = _state.value.copy(showAppLockDialog = false)
    }

    fun showClearCacheDialog() {
        _state.value = _state.value.copy(showClearCacheDialog = true)
    }

    fun hideClearCacheDialog() {
        _state.value = _state.value.copy(showClearCacheDialog = false)
    }

    fun showResetAppDataDialog() {
        _state.value = _state.value.copy(showResetAppDataDialog = true)
    }

    fun hideResetAppDataDialog() {
        _state.value = _state.value.copy(showResetAppDataDialog = false)
    }

    fun showDeleteAccountDialog() {
        _state.value = _state.value.copy(showDeleteAccountDialog = true)
    }

    fun hideDeleteAccountDialog() {
        _state.value = _state.value.copy(showDeleteAccountDialog = false)
    }

    fun showExportDataDialog() {
        _state.value = _state.value.copy(showExportDataDialog = true)
    }

    fun hideExportDataDialog() {
        _state.value = _state.value.copy(showExportDataDialog = false)
    }

    fun showImportDataDialog() {
        _state.value = _state.value.copy(showImportDataDialog = true)
    }

    fun hideImportDataDialog() {
        _state.value = _state.value.copy(showImportDataDialog = false)
    }

    // ========================================================================
    // UTILITY FUNCTIONS
    // ========================================================================

    fun clearAction() {
        viewModelScope.launch {
            _actions.value = null
        }
    }

    fun resetSaveSuccess() {
        _state.value = _state.value.copy(saveSuccess = false)
    }

    fun setDeviceId(deviceId: String) {
        currentDeviceId = deviceId
        loadSettings()
    }

    /**
     * Get current theme setting
     */
    fun getCurrentTheme(): String {
        return _state.value.theme
    }

    /**
     * Get current font size setting
     */
    fun getCurrentFontSize(): String {
        return _state.value.fontSize
    }

    /**
     * Get current sync frequency setting
     */
    fun getCurrentSyncFrequency(): String {
        return _state.value.syncFrequency
    }

    /**
     * Check if offline mode is enabled
     */
    fun isOfflineModeEnabled(): Boolean {
        return _state.value.offlineMode
    }

    /**
     * Check if notifications are enabled
     */
    fun areNotificationsEnabled(): Boolean {
        return _state.value.notificationsEnabled
    }

    /**
     * Validate current settings
     */
    fun validateSettings(): Boolean {
        // Add validation logic here
        return true
    }

    /**
     * Get settings summary for debugging
     */
    fun getSettingsSummary(): String {
        val state = _state.value
        return """
            Settings Summary:
            - Theme: ${state.theme}
            - Font Size: ${state.fontSize}
            - Sync Frequency: ${state.syncFrequency}
            - Offline Mode: ${state.offlineMode}
            - Notifications: ${state.notificationsEnabled}
            - App Lock: ${state.appLockEnabled} (${state.appLockType})
            - Debug Mode: ${state.debugModeEnabled}
            - Language: ${state.language}
            - Device: ${state.deviceName} (${state.deviceId})
        """.trimIndent()
    }
}