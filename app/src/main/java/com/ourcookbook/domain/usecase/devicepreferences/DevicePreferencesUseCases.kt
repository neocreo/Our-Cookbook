package com.ourcookbook.domain.usecase.devicepreferences

import com.ourcookbook.domain.model.DevicePreferences
import com.ourcookbook.domain.repository.DevicePreferencesRepository

/**
 * Use cases for Device Preferences operations
 * These use cases encapsulate the business logic for device preferences management
 */

// Create Preferences Use Case
class CreateDevicePreferences(
    private val repository: DevicePreferencesRepository
) {
    suspend operator fun invoke(preferences: DevicePreferences): Result<String> {
        return try {
            val preferencesId = repository.createPreferences(preferences)
            Result.success(preferencesId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Update Preferences Use Case
class UpdateDevicePreferences(
    private val repository: DevicePreferencesRepository
) {
    suspend operator fun invoke(preferences: DevicePreferences): Result<Unit> {
        return try {
            repository.updatePreferences(preferences)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Delete Preferences Use Case
class DeleteDevicePreferences(
    private val repository: DevicePreferencesRepository
) {
    suspend operator fun invoke(preferencesId: String): Result<Unit> {
        return try {
            repository.deletePreferences(preferencesId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Delete Preferences By Device Use Case
class DeleteDevicePreferencesByDevice(
    private val repository: DevicePreferencesRepository
) {
    suspend operator fun invoke(deviceId: String): Result<Unit> {
        return try {
            repository.deletePreferencesByDevice(deviceId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Get Preferences By ID Use Case
class GetDevicePreferencesById(
    private val repository: DevicePreferencesRepository
) {
    suspend operator fun invoke(preferencesId: String): Result<DevicePreferences?> {
        return try {
            val preferences = repository.getPreferencesById(preferencesId)
            Result.success(preferences)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Get Preferences By Device Use Case
class GetDevicePreferencesByDevice(
    private val repository: DevicePreferencesRepository
) {
    suspend operator fun invoke(deviceId: String): Result<DevicePreferences?> {
        return try {
            val preferences = repository.getPreferencesByDevice(deviceId)
            Result.success(preferences)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Get All Preferences Use Case
class GetAllDevicePreferences(
    private val repository: DevicePreferencesRepository
) {
    suspend operator fun invoke(): Result<List<DevicePreferences>> {
        return try {
            val preferencesList = repository.getAllPreferences()
            Result.success(preferencesList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Get Preferences Count Use Case
class GetDevicePreferencesCount(
    private val repository: DevicePreferencesRepository
) {
    suspend operator fun invoke(): Result<Int> {
        return try {
            val count = repository.getPreferencesCount()
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Validate Preferences Checksum Use Case
class ValidateDevicePreferencesChecksum(
    private val repository: DevicePreferencesRepository
) {
    suspend operator fun invoke(preferencesId: String): Result<Boolean> {
        return try {
            val isValid = repository.validatePreferencesChecksum(preferencesId)
            Result.success(isValid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Update Preferences Checksum Use Case
class UpdateDevicePreferencesChecksum(
    private val repository: DevicePreferencesRepository
) {
    suspend operator fun invoke(preferencesId: String): Result<Boolean> {
        return try {
            val updated = repository.updatePreferencesChecksum(preferencesId)
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
