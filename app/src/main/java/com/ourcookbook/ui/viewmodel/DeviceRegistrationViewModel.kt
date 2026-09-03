package com.ourcookbook.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ourcookbook.domain.model.Device
import com.ourcookbook.domain.usecase.device.CreateDevice
import com.ourcookbook.domain.usecase.device.GetDeviceByDeviceId
import com.ourcookbook.domain.usecase.device.UpdateDevice
import com.ourcookbook.domain.usecase.devicepreferences.CreateDevicePreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * State for DeviceRegistrationScreen
 */
data class DeviceRegistrationState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val deviceName: String = "",
    val deviceType: String = "ANDROID",
    val isRegistering: Boolean = false,
    val registrationSuccess: Boolean = false,
    val deviceId: String? = null
) {
    val isFormValid: Boolean get() = deviceName.isNotBlank()
}

/**
 * Event for DeviceRegistrationScreen
 */
sealed class DeviceRegistrationEvent {
    data class UpdateDeviceName(val name: String) : DeviceRegistrationEvent()
    data class UpdateDeviceType(val type: String) : DeviceRegistrationEvent()
    object RegisterDevice : DeviceRegistrationEvent()
    object RetryRegistration : DeviceRegistrationEvent()
    object ClearError : DeviceRegistrationEvent()
}

/**
 * Action for DeviceRegistrationScreen
 */
sealed class DeviceRegistrationAction {
    data class ShowError(val message: String) : DeviceRegistrationAction()
    data class NavigateToHome(val deviceId: String) : DeviceRegistrationAction()
    object NavigateBack : DeviceRegistrationAction()
    object ShowSuccess : DeviceRegistrationAction()
}

/**
 * ViewModel for DeviceRegistrationScreen
 * Handles device registration process
 */
@HiltViewModel
class DeviceRegistrationViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val createDevice: CreateDevice,
    private val getDeviceByDeviceId: GetDeviceByDeviceId,
    private val updateDevice: UpdateDevice,
    private val createDevicePreferences: CreateDevicePreferences
) : ViewModel() {

    companion object {
        private const val PREFS_NAME = "auth_prefs"
        private const val KEY_DEVICE_ID = "device_id"
    }

    private val _state = MutableStateFlow(DeviceRegistrationState())
    val state: StateFlow<DeviceRegistrationState> = _state.asStateFlow()

    private val _actions = MutableStateFlow<DeviceRegistrationAction?>(null)
    val actions: StateFlow<DeviceRegistrationAction?> = _actions.asStateFlow()

    private var existingDeviceId: String? = null

    fun handleEvent(event: DeviceRegistrationEvent) {
        when (event) {
            is DeviceRegistrationEvent.UpdateDeviceName -> updateDeviceName(event.name)
            is DeviceRegistrationEvent.UpdateDeviceType -> updateDeviceType(event.type)
            is DeviceRegistrationEvent.RegisterDevice -> registerDevice()
            is DeviceRegistrationEvent.RetryRegistration -> retryRegistration()
            is DeviceRegistrationEvent.ClearError -> clearError()
        }
    }

    private fun updateDeviceName(name: String) {
        _state.value = _state.value.copy(deviceName = name, error = null)
    }

    private fun updateDeviceType(type: String) {
        _state.value = _state.value.copy(deviceType = type)
    }

    private fun registerDevice() {
        viewModelScope.launch {
            val currentState = _state.value
            
            if (!currentState.isFormValid) {
                _state.value = currentState.copy(error = "Please enter a device name")
                return@launch
            }
            
            _state.value = currentState.copy(isRegistering = true, error = null)
            
            try {
                val deviceId = existingDeviceId ?: UUID.randomUUID().toString()
                
                val device = Device(
                    id = UUID.randomUUID().toString(),
                    deviceId = deviceId,
                    name = currentState.deviceName,
                    createdAt = java.time.Instant.now(),
                    lastSeenAt = java.time.Instant.now()
                )
                
                val result = createDevice(device)
                result.onSuccess { createdDeviceId ->
                    // Store device ID for future authentication
                    storeDeviceId(deviceId)
                    
                    // Create default preferences
                    createDefaultPreferences(deviceId)
                    
                    _state.value = currentState.copy(
                        isRegistering = false,
                        registrationSuccess = true,
                        deviceId = deviceId
                    )
                    
                    _actions.value = DeviceRegistrationAction.ShowSuccess
                    
                    // Navigate to home after a delay
                    kotlinx.coroutines.delay(1500)
                    _actions.value = DeviceRegistrationAction.NavigateToHome(deviceId)
                    
                }.onFailure { e ->
                    _state.value = currentState.copy(
                        isRegistering = false,
                        error = "Registration failed: ${e.message}"
                    )
                    _actions.value = DeviceRegistrationAction.ShowError("Registration failed: ${e.message}")
                }
                
            } catch (e: Exception) {
                _state.value = currentState.copy(
                    isRegistering = false,
                    error = "Registration failed: ${e.message}"
                )
                _actions.value = DeviceRegistrationAction.ShowError("Registration failed: ${e.message}")
            }
        }
    }

    private fun getOsVersion(): String {
        // In production, this would get the actual OS version
        return "13"
    }

    private suspend fun storeDeviceId(deviceId: String) {
        existingDeviceId = deviceId
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_DEVICE_ID, deviceId)
            .apply()
    }

    private suspend fun createDefaultPreferences(deviceId: String) {
        try {
            val preferences = com.ourcookbook.domain.model.DevicePreferences(
                id = UUID.randomUUID().toString(),
                deviceId = deviceId,
                theme = "SYSTEM",
                fontSize = "MEDIUM",
                syncFrequency = "AUTO",
                offlineMode = false,
                notificationsEnabled = true
            )
            
            createDevicePreferences(preferences)
            
        } catch (e: Exception) {
            // Preferences creation failed, but device registration succeeded
        }
    }

    private fun retryRegistration() {
        _state.value = _state.value.copy(
            isRegistering = false,
            registrationSuccess = false,
            error = null
        )
    }

    private fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    fun clearAction() {
        viewModelScope.launch {
            _actions.value = null
        }
    }

    fun setExistingDeviceId(deviceId: String) {
        existingDeviceId = deviceId
        _state.value = _state.value.copy(deviceId = deviceId)
    }

    fun resetRegistrationSuccess() {
        _state.value = _state.value.copy(registrationSuccess = false)
    }
}