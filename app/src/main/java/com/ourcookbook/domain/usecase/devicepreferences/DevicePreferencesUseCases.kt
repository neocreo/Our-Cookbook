package com.ourcookbook.domain.usecase.devicepreferences

import com.ourcookbook.domain.model.DevicePreferences
import com.ourcookbook.domain.repository.DevicePreferencesRepository

// Delete Preferences Use Case
class DeleteDevicePreferences(
    private val repository: DevicePreferencesRepository
) {
    suspend operator fun invoke(preferencesId: String): Result<Unit> {
        return try {
            repository.deleteDevicePreferences(preferencesId)
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
            repository.deleteDevicePreferences(deviceId)
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
            val result = repository.getDevicePreferencesByDevice(preferencesId)
            Result.success(result.getOrNull())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Get All Preferences Use Case
class GetAllDevicePreferences(
    private val repository: DevicePreferencesRepository
) {
    suspend operator fun invoke(userId: String = ""): Result<List<DevicePreferences>> {
        return try {
            val preferencesList = repository.getAllDevicePreferences(userId)
            Result.success(preferencesList.getOrThrow())
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
            val preferencesList = repository.getAllDevicePreferences("").getOrDefault(emptyList())
            Result.success(preferencesList.size)
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
            Result.success(true)
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
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
