package com.ourcookbook.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ourcookbook.domain.repository.DevicePreferencesRepository
import com.ourcookbook.domain.repository.DeviceRepository
import com.ourcookbook.domain.model.Device
import com.ourcookbook.domain.model.DevicePreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for User Profile Screen
 * Task 2.1.10: User Profile Screen Implementation
 */
@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val preferencesRepository: DevicePreferencesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(UserProfileState())
    val state: StateFlow<UserProfileState> = _state.asStateFlow()

    init {
        loadProfile()
    }

    fun onEvent(event: UserProfileEvent) {
        when (event) {
            UserProfileEvent.LoadProfile -> loadProfile()
            UserProfileEvent.StartEditing -> startEditing()
            UserProfileEvent.CancelEditing -> cancelEditing()
            is UserProfileEvent.UpdateDeviceName -> updateDeviceName(event.name)
            UserProfileEvent.SaveProfile -> saveProfile()
            UserProfileEvent.ClearError -> clearError()
            UserProfileEvent.ClearSuccess -> clearSuccess()
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val device = deviceRepository.getDeviceById("")
                val preferences = preferencesRepository.getDevicePreferencesByDevice("").getOrNull()
                _state.value = _state.value.copy(
                    device = device,
                    preferences = preferences,
                    editedDeviceName = device?.name ?: "",
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Failed to load profile: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    private fun startEditing() {
        _state.value = _state.value.copy(isEditing = true)
    }

    private fun cancelEditing() {
        _state.value = _state.value.copy(
            isEditing = false,
            editedDeviceName = _state.value.device?.name ?: ""
        )
    }

    private fun updateDeviceName(name: String) {
        _state.value = _state.value.copy(editedDeviceName = name)
    }

    private fun saveProfile() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val currentDevice = _state.value.device
                if (currentDevice != null) {
                    val updatedDevice = currentDevice.copy(name = _state.value.editedDeviceName)
                    deviceRepository.updateDevice(updatedDevice)
                    _state.value = _state.value.copy(
                        device = updatedDevice,
                        isEditing = false,
                        isLoading = false,
                        successMessage = "Profile updated successfully!"
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Failed to save profile: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    private fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    private fun clearSuccess() {
        _state.value = _state.value.copy(successMessage = null)
    }
}
