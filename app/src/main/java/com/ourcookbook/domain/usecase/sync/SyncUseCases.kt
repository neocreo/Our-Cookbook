package com.ourcookbook.domain.usecase.sync

import com.ourcookbook.domain.model.ConflictResolution
import com.ourcookbook.domain.model.ConflictStatus
import com.ourcookbook.domain.model.SyncConflict
import com.ourcookbook.domain.model.SyncMetadata
import com.ourcookbook.domain.repository.SyncConflictRepository
import com.ourcookbook.domain.repository.SyncMetadataRepository
import kotlinx.coroutines.flow.Flow
import java.time.Instant

/**
 * Use cases for Sync Conflict operations
 * These use cases encapsulate the business logic for conflict management
 */

// Create Conflict Use Case
class CreateConflict(
    private val repository: SyncConflictRepository
) {
    suspend operator fun invoke(conflict: SyncConflict): Result<String> {
        return try {
            if (!conflict.isValid()) {
                return Result.failure(IllegalArgumentException("Conflict validation failed"))
            }
            val conflictId = repository.createConflict(conflict)
            Result.success(conflictId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Update Conflict Use Case
class UpdateConflict(
    private val repository: SyncConflictRepository
) {
    suspend operator fun invoke(conflict: SyncConflict): Result<Unit> {
        return try {
            repository.updateConflict(conflict)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Delete Conflict Use Case
class DeleteConflict(
    private val repository: SyncConflictRepository
) {
    suspend operator fun invoke(conflictId: String): Result<Unit> {
        return try {
            repository.deleteConflict(conflictId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Delete Conflicts By Status Use Case
class DeleteConflictsByStatus(
    private val repository: SyncConflictRepository
) {
    suspend operator fun invoke(status: ConflictStatus): Result<Unit> {
        return try {
            repository.deleteConflictsByStatus(status)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Get Conflict By ID Use Case
class GetConflictById(
    private val repository: SyncConflictRepository
) {
    suspend operator fun invoke(conflictId: String): Result<SyncConflict?> {
        return try {
            val conflict = repository.getConflictById(conflictId)
            Result.success(conflict)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Get Conflicts By Status Use Case
class GetConflictsByStatus(
    private val repository: SyncConflictRepository
) {
    operator fun invoke(status: ConflictStatus): Flow<List<SyncConflict>> {
        return repository.getConflictsByStatus(status)
    }
}

// Get Conflicts By Recipe Use Case
class GetConflictsByRecipe(
    private val repository: SyncConflictRepository
) {
    suspend operator fun invoke(recipeId: String): Result<List<SyncConflict>> {
        return try {
            val conflicts = repository.getConflictsByRecipe(recipeId)
            Result.success(conflicts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Get Conflicts Since Use Case
class GetConflictsSince(
    private val repository: SyncConflictRepository
) {
    suspend operator fun invoke(since: Instant): Result<List<SyncConflict>> {
        return try {
            val conflicts = repository.getConflictsSince(since)
            Result.success(conflicts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Get Pending Conflict Count Use Case
class GetPendingConflictCount(
    private val repository: SyncConflictRepository
) {
    suspend operator fun invoke(): Result<Int> {
        return try {
            val count = repository.getPendingConflictCount()
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Get All Conflicts Use Case
class GetAllConflicts(
    private val repository: SyncConflictRepository
) {
    suspend operator fun invoke(): Result<List<SyncConflict>> {
        return try {
            val conflicts = repository.getAllConflicts()
            Result.success(conflicts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Resolve Conflict Use Case (business logic level)
class ResolveSyncConflict(
    private val repository: SyncConflictRepository,
    private val getConflictById: GetConflictById,
    private val updateConflict: UpdateConflict
) {
    suspend operator fun invoke(conflictId: String, resolution: ConflictResolution): Result<Unit> {
        return try {
            val conflict = getConflictById(conflictId).getOrThrow()
                ?: return Result.failure(NoSuchElementException("Conflict not found"))
            
            val resolvedConflict = conflict.withResolution(resolution)
            updateConflict(resolvedConflict)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Validate Conflict Checksum Use Case
class ValidateConflictChecksum(
    private val repository: SyncConflictRepository
) {
    suspend operator fun invoke(conflictId: String): Result<Boolean> {
        return try {
            val isValid = repository.validateConflictChecksum(conflictId)
            Result.success(isValid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Update Conflict Checksum Use Case
class UpdateConflictChecksum(
    private val repository: SyncConflictRepository
) {
    suspend operator fun invoke(conflictId: String): Result<Boolean> {
        return try {
            val updated = repository.updateConflictChecksum(conflictId)
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
