package com.ourcookbook.ui.screens.profile

import com.ourcookbook.domain.model.Device
import com.ourcookbook.domain.model.DevicePreferences

/**
 * State for User Profile Screen
 * Task 2.1.10: User Profile Screen Implementation
 */
data class UserProfileState(
    val device: Device? = null,
    val preferences: DevicePreferences? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isEditing: Boolean = false,
    val editedDeviceName: String = "",
    val successMessage: String? = null
)

sealed class UserProfileEvent {
    object LoadProfile : UserProfileEvent()
    object StartEditing : UserProfileEvent()
    object CancelEditing : UserProfileEvent()
    data class UpdateDeviceName(val name: String) : UserProfileEvent()
    object SaveProfile : UserProfileEvent()
    object ClearError : UserProfileEvent()
    object ClearSuccess : UserProfileEvent()
}
