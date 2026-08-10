package com.ourcookbook.domain.usecase.sync

import com.ourcookbook.domain.model.SyncMetadata
import com.ourcookbook.domain.repository.SyncMetadataRepository
import java.time.Instant

/**
 * Use cases for Sync Metadata operations
 * These use cases encapsulate the business logic for sync metadata management
 */

// Create Metadata Use Case
class CreateSyncMetadata(
    private val repository: SyncMetadataRepository
) {
    suspend operator fun invoke(metadata: SyncMetadata): Result<String> {
        return try {
            val metadataId = repository.createMetadata(metadata)
            Result.success(metadataId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Update Metadata Use Case
class UpdateSyncMetadata(
    private val repository: SyncMetadataRepository
) {
    suspend operator fun invoke(metadata: SyncMetadata): Result<Unit> {
        return try {
            repository.updateMetadata(metadata)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Delete Metadata Use Case
class DeleteSyncMetadata(
    private val repository: SyncMetadataRepository
) {
    suspend operator fun invoke(metadataId: String): Result<Unit> {
        return try {
            repository.deleteMetadata(metadataId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Delete Metadata By Device Use Case
class DeleteSyncMetadataByDevice(
    private val repository: SyncMetadataRepository
) {
    suspend operator fun invoke(deviceId: String): Result<Unit> {
        return try {
            repository.deleteMetadataByDevice(deviceId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Get Metadata By ID Use Case
class GetSyncMetadataById(
    private val repository: SyncMetadataRepository
) {
    suspend operator fun invoke(metadataId: String): Result<SyncMetadata?> {
        return try {
            val metadata = repository.getMetadataById(metadataId)
            Result.success(metadata)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Get Metadata By Device Use Case
class GetSyncMetadataByDevice(
    private val repository: SyncMetadataRepository
) {
    suspend operator fun invoke(deviceId: String): Result<SyncMetadata?> {
        return try {
            val metadata = repository.getMetadataByDevice(deviceId)
            Result.success(metadata)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Get All Metadata Use Case
class GetAllSyncMetadata(
    private val repository: SyncMetadataRepository
) {
    suspend operator fun invoke(): Result<List<SyncMetadata>> {
        return try {
            val metadataList = repository.getAllMetadata()
            Result.success(metadataList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Update Last Sync Timestamp Use Case
class UpdateLastSyncTimestamp(
    private val repository: SyncMetadataRepository
) {
    suspend operator fun invoke(deviceId: String, timestamp: Instant): Result<Boolean> {
        return try {
            val updated = repository.updateLastSyncTimestamp(deviceId, timestamp)
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Update Sync In Progress Use Case
class UpdateSyncInProgress(
    private val repository: SyncMetadataRepository
) {
    suspend operator fun invoke(deviceId: String, inProgress: Boolean): Result<Boolean> {
        return try {
            val updated = repository.updateSyncInProgress(deviceId, inProgress)
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Validate Metadata Checksum Use Case
class ValidateSyncMetadataChecksum(
    private val repository: SyncMetadataRepository
) {
    suspend operator fun invoke(metadataId: String): Result<Boolean> {
        return try {
            val isValid = repository.validateMetadataChecksum(metadataId)
            Result.success(isValid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Update Metadata Checksum Use Case
class UpdateSyncMetadataChecksum(
    private val repository: SyncMetadataRepository
) {
    suspend operator fun invoke(metadataId: String): Result<Boolean> {
        return try {
            val updated = repository.updateMetadataChecksum(metadataId)
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Get Sync Status Use Case
class GetSyncStatus(
    private val repository: SyncMetadataRepository,
    private val getSyncMetadataByDevice: GetSyncMetadataByDevice
) {
    suspend operator fun invoke(deviceId: String): Result<SyncStatus> {
        return try {
            val metadata = getSyncMetadataByDevice(deviceId).getOrThrow()
            
            val status = if (metadata?.syncInProgress == true) {
                SyncStatus.SYNCING
            } else {
                SyncStatus.IDLE
            }
            
            Result.success(status)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Sync status enum
 */
enum class SyncStatus {
    IDLE,       // No sync in progress
    SYNCING,    // Sync currently in progress
    ERROR       // Sync encountered an error
}
