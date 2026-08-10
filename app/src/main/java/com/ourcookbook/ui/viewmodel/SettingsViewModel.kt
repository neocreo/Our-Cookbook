package com.ourcookbook.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ourcookbook.domain.model.DevicePreferences
import com.ourcookbook.domain.usecase.devicepreferences.GetDevicePreferencesByDevice
import com.ourcookbook.domain.usecase.devicepreferences.UpdateDevicePreferences
import com.ourcookbook.domain.usecase.devicepreferences.CreateDevicePreferences
import com.ourcookbook.domain.usecase.sync.GetSyncStatus
import com.ourcookbook.domain.usecase.sync.UpdateSyncInProgress
import com.ourcookbook.domain.usecase.sync.UpdateLastSyncTimestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * State for SettingsScreen
 */
data class SettingsState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val preferences: DevicePreferences? = null,
    val syncStatus: String = "IDLE",
    val appVersion: String = "1.0.0",
    val deviceName: String = "",
    val deviceId: String = "",
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false
) {
    val theme: String get() = preferences?.theme ?: "SYSTEM"
    val fontSize: String get() = preferences?.fontSize ?: "MEDIUM"
    val syncFrequency: String get() = preferences?.syncFrequency ?: "AUTO"
    val offlineMode: Boolean get() = preferences?.offlineMode ?: false
    val notificationsEnabled: Boolean get() = preferences?.notificationsEnabled ?: true
}

/**
 * Event for SettingsScreen
 */
sealed class SettingsEvent {
    object LoadSettings : SettingsEvent()
    data class UpdateTheme(val theme: String) : SettingsEvent()
    data class UpdateFontSize(val fontSize: String) : SettingsEvent()
    data class UpdateSyncFrequency(val frequency: String) : SettingsEvent()
    data class UpdateOfflineMode(val enabled: Boolean) : SettingsEvent()
    data class UpdateNotificationsEnabled(val enabled: Boolean) : SettingsEvent()
    object SaveSettings : SettingsEvent()
    object TriggerSync : SettingsEvent()
    object ClearCache : SettingsEvent()
    object ExportData : SettingsEvent()
    object ImportData : SettingsEvent()
    object ClearError : SettingsEvent()
}

/**
 * Action for SettingsScreen
 */
sealed class SettingsAction {
    data class ShowError(val message: String) : SettingsAction()
    data class ShowSuccess(val message: String) : SettingsAction()
    data class NavigateToSyncStatus : SettingsAction()
    data class NavigateToExport : SettingsAction()
    data class NavigateToImport : SettingsAction()
    object ShowThemeDialog : SettingsAction()
    object ShowFontSizeDialog : SettingsAction()
    object ShowSyncFrequencyDialog : SettingsAction()
}

/**
 * ViewModel for SettingsScreen
 * Handles app settings and preferences management
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getDevicePreferencesByDevice: GetDevicePreferencesByDevice,
    private val updateDevicePreferences: UpdateDevicePreferences,
    private val createDevicePreferences: CreateDevicePreferences,
    private val getSyncStatus: GetSyncStatus,
    private val updateSyncInProgress: UpdateSyncInProgress,
    private val updateLastSyncTimestamp: UpdateLastSyncTimestamp
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    private val _actions = MutableStateFlow<SettingsAction?>(null)
    val actions: StateFlow<SettingsAction?> = _actions.asStateFlow()

    private var currentDeviceId: String = "current_device_id" // Will be set properly in production

    init {
        loadSettings()
    }

    fun handleEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.LoadSettings -> loadSettings()
            is SettingsEvent.UpdateTheme -> updateTheme(event.theme)
            is SettingsEvent.UpdateFontSize -> updateFontSize(event.fontSize)
            is SettingsEvent.UpdateSyncFrequency -> updateSyncFrequency(event.frequency)
            is SettingsEvent.UpdateOfflineMode -> updateOfflineMode(event.enabled)
            is SettingsEvent.UpdateNotificationsEnabled -> updateNotificationsEnabled(event.enabled)
            is SettingsEvent.SaveSettings -> saveSettings()
            is SettingsEvent.TriggerSync -> triggerSync()
            is SettingsEvent.ClearCache -> clearCache()
            is SettingsEvent.ExportData -> exportData()
            is SettingsEvent.ImportData -> importData()
            is SettingsEvent.ClearError -> clearError()
        }
    }

    private fun loadSettings() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            try {
                // Load device preferences
                val preferencesResult = getDevicePreferencesByDevice(currentDeviceId)
                val preferences = preferencesResult.getOrNull()
                
                // Load sync status
                val syncStatusResult = getSyncStatus(currentDeviceId)
                val syncStatus = syncStatusResult.getOrDefault("IDLE").toString()
                
                _state.value = _state.value.copy(
                    isLoading = false,
                    preferences = preferences,
                    syncStatus = syncStatus,
                    deviceId = currentDeviceId,
                    deviceName = "My Device" // Will be loaded from device info in production
                )
                
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Failed to load settings: ${e.message}"
                )
            }
        }
    }

    private fun updateTheme(theme: String) {
        val currentState = _state.value
        val updatedPreferences = currentState.preferences?.copy(theme = theme)
            ?: DevicePreferences(
                id = UUID.randomUUID().toString(),
                deviceId = currentDeviceId,
                theme = theme,
                fontSize = "MEDIUM",
                syncFrequency = "AUTO",
                offlineMode = false,
                notificationsEnabled = true
            )
        
        _state.value = currentState.copy(preferences = updatedPreferences)
    }

    private fun updateFontSize(fontSize: String) {
        val currentState = _state.value
        val updatedPreferences = currentState.preferences?.copy(fontSize = fontSize)
            ?: DevicePreferences(
                id = UUID.randomUUID().toString(),
                deviceId = currentDeviceId,
                theme = "SYSTEM",
                fontSize = fontSize,
                syncFrequency = "AUTO",
                offlineMode = false,
                notificationsEnabled = true
            )
        
        _state.value = currentState.copy(preferences = updatedPreferences)
    }

    private fun updateSyncFrequency(frequency: String) {
        val currentState = _state.value
        val updatedPreferences = currentState.preferences?.copy(syncFrequency = frequency)
            ?: DevicePreferences(
                id = UUID.randomUUID().toString(),
                deviceId = currentDeviceId,
                theme = "SYSTEM",
                fontSize = "MEDIUM",
                syncFrequency = frequency,
                offlineMode = false,
                notificationsEnabled = true
            )
        
        _state.value = currentState.copy(preferences = updatedPreferences)
    }

    private fun updateOfflineMode(enabled: Boolean) {
        val currentState = _state.value
        val updatedPreferences = currentState.preferences?.copy(offlineMode = enabled)
            ?: DevicePreferences(
                id = UUID.randomUUID().toString(),
                deviceId = currentDeviceId,
                theme = "SYSTEM",
                fontSize = "MEDIUM",
                syncFrequency = "AUTO",
                offlineMode = enabled,
                notificationsEnabled = true
            )
        
        _state.value = currentState.copy(preferences = updatedPreferences)
    }

    private fun updateNotificationsEnabled(enabled: Boolean) {
        val currentState = _state.value
        val updatedPreferences = currentState.preferences?.copy(notificationsEnabled = enabled)
            ?: DevicePreferences(
                id = UUID.randomUUID().toString(),
                deviceId = currentDeviceId,
                theme = "SYSTEM",
                fontSize = "MEDIUM",
                syncFrequency = "AUTO",
                offlineMode = false,
                notificationsEnabled = enabled
            )
        
        _state.value = currentState.copy(preferences = updatedPreferences)
    }

    private fun saveSettings() {
        viewModelScope.launch {
            val currentState = _state.value
            val preferences = currentState.preferences ?: return@launch
            
            _state.value = currentState.copy(isSaving = true, error = null)
            
            try {
                val result = if (preferences.id.isBlank()) {
                    // New preferences
                    createDevicePreferences(preferences)
                } else {
                    // Update existing preferences
                    updateDevicePreferences(preferences)
                }
                
                result.onSuccess {
                    _state.value = currentState.copy(
                        isSaving = false,
                        saveSuccess = true
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

    private fun triggerSync() {
        viewModelScope.launch {
            try {
                updateSyncInProgress(currentDeviceId, true)
                _actions.value = SettingsAction.NavigateToSyncStatus
                
            } catch (e: Exception) {
                _actions.value = SettingsAction.ShowError("Failed to start sync: ${e.message}")
            }
        }
    }

    private fun clearCache() {
        viewModelScope.launch {
            // In production, this would clear various caches
            _actions.value = SettingsAction.ShowSuccess("Cache cleared successfully")
        }
    }

    private fun exportData() {
        viewModelScope.launch {
            _actions.value = SettingsAction.NavigateToExport
        }
    }

    private fun importData() {
        viewModelScope.launch {
            _actions.value = SettingsAction.NavigateToImport
        }
    }

    private fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    fun showThemeDialog() {
        viewModelScope.launch {
            _actions.value = SettingsAction.ShowThemeDialog
        }
    }

    fun showFontSizeDialog() {
        viewModelScope.launch {
            _actions.value = SettingsAction.ShowFontSizeDialog
        }
    }

    fun showSyncFrequencyDialog() {
        viewModelScope.launch {
            _actions.value = SettingsAction.ShowSyncFrequencyDialog
        }
    }

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
}