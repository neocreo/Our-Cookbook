package com.ourcookbook.domain.usecase.devicepreferences

import com.ourcookbook.domain.model.DevicePreferences
import com.ourcookbook.domain.repository.DevicePreferencesRepository
import javax.inject.Inject

/**
 * Use case to get device preferences by device ID
 */
class GetDevicePreferencesByDevice @Inject constructor(
    private val repository: DevicePreferencesRepository
) {
    
    suspend operator fun invoke(deviceId: String): Result<DevicePreferences> {
        return repository.getDevicePreferencesByDevice(deviceId)
    }
}