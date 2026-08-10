package com.ourcookbook.domain.usecase.device

import com.ourcookbook.domain.model.Device
import com.ourcookbook.domain.repository.DeviceRepository
import kotlinx.coroutines.flow.Flow
import java.time.Instant

/**
 * Use cases for Device operations
 * These use cases encapsulate the business logic for device management
 */

// Create Device Use Case
class CreateDevice(
    private val repository: DeviceRepository
) {
    suspend operator fun invoke(device: Device): Result<String> {
        return try {
            val deviceId = repository.createDevice(device)
            Result.success(deviceId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Update Device Use Case
class UpdateDevice(
    private val repository: DeviceRepository
) {
    suspend operator fun invoke(device: Device): Result<Unit> {
        return try {
            repository.updateDevice(device)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Delete Device Use Case
class DeleteDevice(
    private val repository: DeviceRepository
) {
    suspend operator fun invoke(deviceId: String): Result<Unit> {
        return try {
            repository.deleteDevice(deviceId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Get Device By ID Use Case
class GetDeviceById(
    private val repository: DeviceRepository
) {
    suspend operator fun invoke(id: String): Result<Device?> {
        return try {
            val device = repository.getDeviceById(id)
            Result.success(device)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Get Device By Device ID Use Case
class GetDeviceByDeviceId(
    private val repository: DeviceRepository
) {
    suspend operator fun invoke(deviceId: String): Result<Device?> {
        return try {
            val device = repository.getDeviceByDeviceId(deviceId)
            Result.success(device)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Get All Devices Use Case
class GetAllDevices(
    private val repository: DeviceRepository
) {
    operator fun invoke(): Flow<List<Device>> {
        return repository.getAllDevices()
    }
}

// Get All Devices Once Use Case
class GetAllDevicesOnce(
    private val repository: DeviceRepository
) {
    suspend operator fun invoke(): Result<List<Device>> {
        return try {
            val devices = repository.getAllDevicesOnce()
            Result.success(devices)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Get Active Devices Since Use Case
class GetActiveDevicesSince(
    private val repository: DeviceRepository
) {
    suspend operator fun invoke(since: Instant): Result<List<Device>> {
        return try {
            val devices = repository.getActiveDevicesSince(since)
            Result.success(devices)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Update Last Seen Use Case
class UpdateLastSeen(
    private val repository: DeviceRepository
) {
    suspend operator fun invoke(deviceId: String, timestamp: Instant): Result<Boolean> {
        return try {
            val updated = repository.updateLastSeen(deviceId, timestamp)
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Get Device Count Use Case
class GetDeviceCount(
    private val repository: DeviceRepository
) {
    suspend operator fun invoke(): Result<Int> {
        return try {
            val count = repository.getDeviceCount()
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Validate Device Checksum Use Case
class ValidateDeviceChecksum(
    private val repository: DeviceRepository
) {
    suspend operator fun invoke(deviceId: String): Result<Boolean> {
        return try {
            val isValid = repository.validateDeviceChecksum(deviceId)
            Result.success(isValid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Update Device Checksum Use Case
class UpdateDeviceChecksum(
    private val repository: DeviceRepository
) {
    suspend operator fun invoke(deviceId: String): Result<Boolean> {
        return try {
            val updated = repository.updateDeviceChecksum(deviceId)
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
