package com.ourcookbook.domain.model

import java.time.Instant
import java.util.UUID

/**
 * Domain model for SyncConflict
 * Represents a conflict between local and remote data
 * 
 * Contains information about detected conflicts between local and remote
 * recipe versions, including checksums, version vectors, and resolution status.
 */
data class SyncConflict(
    val id: String = UUID.randomUUID().toString(),
    val localRecipeId: String,
    val remoteRecipeId: String,
    val localChecksum: String,
    val remoteChecksum: String,
    val localVersion: VersionVector,
    val remoteVersion: VersionVector,
    val detectedAt: Instant = Instant.now(),
    val resolvedAt: Instant? = null,
    val status: ConflictStatus = ConflictStatus.PENDING,
    val resolution: ConflictResolution? = null
) {
    fun isValid(): Boolean {
        return localRecipeId.isNotBlank() && 
               remoteRecipeId.isNotBlank() &&
               localChecksum.isNotBlank() &&
               remoteChecksum.isNotBlank()
    }
    
    // Check if conflict is resolved
    val isResolved: Boolean get() = status == ConflictStatus.RESOLVED
    
    // Check if conflict is pending
    val isPending: Boolean get() = status == ConflictStatus.PENDING
    
    // Get the resolution strategy if available
    val resolutionStrategy: ConflictResolution? get() = resolution
    
    // Mark conflict as resolved with a resolution
    fun withResolution(resolution: ConflictResolution): SyncConflict {
        return this.copy(
            resolution = resolution,
            status = ConflictStatus.RESOLVED,
            resolvedAt = Instant.now()
        )
    }
    
    companion object {
        fun create(
            localRecipeId: String,
            remoteRecipeId: String,
            localChecksum: String,
            remoteChecksum: String,
            localVersion: VersionVector,
            remoteVersion: VersionVector
        ): SyncConflict {
            return SyncConflict(
                localRecipeId = localRecipeId,
                remoteRecipeId = remoteRecipeId,
                localChecksum = localChecksum,
                remoteChecksum = remoteChecksum,
                localVersion = localVersion,
                remoteVersion = remoteVersion
            )
        }
    }
}

/**
 * Conflict status
 */
enum class ConflictStatus {
    PENDING,     // Conflict detected, not yet resolved
    RESOLVED,    // Conflict has been resolved
    IGNORED      // Conflict was ignored (keep both versions)
}

/**
 * Conflict resolution strategy
 */
sealed class ConflictResolution {
    object KeepLocal : ConflictResolution()
    object KeepRemote : ConflictResolution()
    data class Merge(val mergedRecipe: Recipe) : ConflictResolution()
}

/**
 * Resolution action types
 */
enum class ResolutionAction {
    OVERWRITE_REMOTE,  // Overwrite remote with local version
    UPDATE_LOCAL,     // Update local with remote version  
    MERGE             // Merge both versions
}

/**
 * Result of conflict resolution
 */
data class ConflictResolutionResult(
    val resolvedRecipe: Recipe,
    val action: ResolutionAction,
    val timestamp: Instant = Instant.now()
)