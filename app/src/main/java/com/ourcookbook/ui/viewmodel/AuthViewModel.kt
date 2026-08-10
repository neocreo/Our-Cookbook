package com.ourcookbook.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ourcookbook.domain.model.Device
import com.ourcookbook.domain.usecase.device.CreateDevice
import com.ourcookbook.domain.usecase.device.GetDeviceByDeviceId
import com.ourcookbook.domain.usecase.device.UpdateDevice
import com.ourcookbook.domain.usecase.devicepreferences.CreateDevicePreferences
import com.ourcookbook.domain.usecase.devicepreferences.GetDevicePreferencesByDevice
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * State for AuthScreen
 */
sealed class AuthState {
    object Loading : AuthState()
    object Idle : AuthState()
    object Authenticated : AuthState()
    data class Error(val message: String) : AuthState()
    object DeviceRegistrationRequired : AuthState()
}

/**
 * Event for AuthScreen
 */
sealed class AuthEvent {
    object CheckAuthentication : AuthEvent()
    object StartDeviceRegistration : AuthEvent()
    data class RegisterDevice(val deviceName: String) : AuthEvent()
    object SkipRegistration : AuthEvent()
    object RetryAuthentication : AuthEvent()
}

/**
 * Action for AuthScreen
 */
sealed class AuthAction {
    data class NavigateToDeviceRegistration(val deviceId: String? = null) : AuthAction()
    data class NavigateToHome(val deviceId: String) : AuthAction()
    data class ShowError(val message: String) : AuthAction()
    object ShowLoading : AuthAction()
}

/**
 * ViewModel for AuthScreen
 * Handles authentication and device registration
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val createDevice: CreateDevice,
    private val getDeviceByDeviceId: GetDeviceByDeviceId,
    private val updateDevice: UpdateDevice,
    private val createDevicePreferences: CreateDevicePreferences,
    private val getDevicePreferencesByDevice: GetDevicePreferencesByDevice
) : ViewModel() {

    private val _state = MutableStateFlow<AuthState>(AuthState.Loading)
    val state: StateFlow<AuthState> = _state.asStateFlow()

    private val _actions = MutableStateFlow<AuthAction?>(null)
    val actions: StateFlow<AuthAction?> = _actions.asStateFlow()

    private var currentDeviceId: String? = null

    init {
        checkAuthentication()
    }

    fun handleEvent(event: AuthEvent) {
        when (event) {
            is AuthEvent.CheckAuthentication -> checkAuthentication()
            is AuthEvent.StartDeviceRegistration -> startDeviceRegistration()
            is AuthEvent.RegisterDevice -> registerDevice(event.deviceName)
            is AuthEvent.SkipRegistration -> skipRegistration()
            is AuthEvent.RetryAuthentication -> retryAuthentication()
        }
    }

    private fun checkAuthentication() {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            
            try {
                // Check if we have a device ID stored (in production, this would come from secure storage)
                val deviceId = getStoredDeviceId()
                
                if (deviceId != null) {
                    // Check if device exists
                    val result = getDeviceByDeviceId(deviceId)
                    result.onSuccess { device ->
                        if (device != null) {
                            // Device exists, check if preferences exist
                            checkDevicePreferences(deviceId)
                        } else {
                            // Device doesn't exist, need registration
                            _state.value = AuthState.DeviceRegistrationRequired
                            _actions.value = AuthAction.NavigateToDeviceRegistration(deviceId)
                        }
                    }.onFailure { e ->
                        _state.value = AuthState.Error("Authentication failed: ${e.message}")
                        _actions.value = AuthAction.ShowError("Authentication failed: ${e.message}")
                    }
                } else {
                    // No device ID, need registration
                    _state.value = AuthState.DeviceRegistrationRequired
                    _actions.value = AuthAction.NavigateToDeviceRegistration()
                }
                
            } catch (e: Exception) {
                _state.value = AuthState.Error("Authentication failed: ${e.message}")
                _actions.value = AuthAction.ShowError("Authentication failed: ${e.message}")
            }
        }
    }

    private suspend fun getStoredDeviceId(): String? {
        // In production, this would come from Android Credential Manager or secure storage
        // For now, return null to simulate first-time user
        return null
    }

    private suspend fun checkDevicePreferences(deviceId: String) {
        try {
            val result = getDevicePreferencesByDevice(deviceId)
            result.onSuccess { preferences ->
                if (preferences != null) {
                    // Device and preferences exist, authenticated
                    currentDeviceId = deviceId
                    _state.value = AuthState.Authenticated
                    _actions.value = AuthAction.NavigateToHome(deviceId)
                } else {
                    // Device exists but no preferences, create default preferences
                    createDefaultPreferences(deviceId)
                }
            }.onFailure { e ->
                // If preferences check fails, still consider authenticated
                currentDeviceId = deviceId
                _state.value = AuthState.Authenticated
                _actions.value = AuthAction.NavigateToHome(deviceId)
            }
        } catch (e: Exception) {
            currentDeviceId = deviceId
            _state.value = AuthState.Authenticated
            _actions.value = AuthAction.NavigateToHome(deviceId)
        }
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
            
            currentDeviceId = deviceId
            _state.value = AuthState.Authenticated
            _actions.value = AuthAction.NavigateToHome(deviceId)
            
        } catch (e: Exception) {
            // Even if preferences creation fails, still navigate to home
            currentDeviceId = deviceId
            _state.value = AuthState.Authenticated
            _actions.value = AuthAction.NavigateToHome(deviceId)
        }
    }

    private fun startDeviceRegistration() {
        viewModelScope.launch {
            _actions.value = AuthAction.NavigateToDeviceRegistration()
        }
    }

    private fun registerDevice(deviceName: String) {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            _actions.value = AuthAction.ShowLoading
            
            try {
                val deviceId = UUID.randomUUID().toString()
                
                val device = Device(
                    id = UUID.randomUUID().toString(),
                    deviceId = deviceId,
                    name = deviceName,
                    deviceType = "ANDROID",
                    osVersion = "13", // Will be set properly in production
                    appVersion = "1.0.0",
                    lastSeen = java.time.Instant.now(),
                    createdAt = java.time.Instant.now(),
                    isActive = true,
                    checksum = ""
                )
                
                val result = createDevice(device)
                result.onSuccess { createdDeviceId ->
                    // Store device ID for future authentication
                    storeDeviceId(deviceId)
                    
                    // Create default preferences
                    createDefaultPreferences(deviceId)
                    
                }.onFailure { e ->
                    _state.value = AuthState.Error("Registration failed: ${e.message}")
                    _actions.value = AuthAction.ShowError("Registration failed: ${e.message}")
                }
                
            } catch (e: Exception) {
                _state.value = AuthState.Error("Registration failed: ${e.message}")
                _actions.value = AuthAction.ShowError("Registration failed: ${e.message}")
            }
        }
    }

    private suspend fun storeDeviceId(deviceId: String) {
        // In production, this would store in Android Credential Manager or secure storage
        currentDeviceId = deviceId
    }

    private fun skipRegistration() {
        viewModelScope.launch {
            // Allow temporary access without registration
            val tempDeviceId = "temp_${UUID.randomUUID()}"
            currentDeviceId = tempDeviceId
            _state.value = AuthState.Authenticated
            _actions.value = AuthAction.NavigateToHome(tempDeviceId)
        }
    }

    private fun retryAuthentication() {
        checkAuthentication()
    }

    fun clearAction() {
        viewModelScope.launch {
            _actions.value = null
        }
    }

    fun getCurrentDeviceId(): String? = currentDeviceId
}