package com.ourcookbook.domain.usecase.devicepreferences

import com.ourcookbook.domain.model.DevicePreferences
import com.ourcookbook.domain.repository.DevicePreferencesRepository
import javax.inject.Inject

/**
 * Use case to create new device preferences
 */
class CreateDevicePreferences @Inject constructor(
    private val repository: DevicePreferencesRepository
) {
    
    suspend operator fun invoke(preferences: DevicePreferences): Result<DevicePreferences> {
        return repository.createDevicePreferences(preferences)
    }
}