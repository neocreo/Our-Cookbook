package com.ourcookbook.data.service

import com.ourcookbook.domain.model.ConflictResolution
import com.ourcookbook.domain.model.ConflictResolutionResult
import com.ourcookbook.domain.model.Recipe
import com.ourcookbook.domain.model.ResolutionAction
import com.ourcookbook.domain.model.SyncConflict
import com.ourcookbook.domain.service.ChecksumService
import com.ourcookbook.domain.service.ConflictResolver
import java.time.Instant
import java.util.UUID

/**
 * Implementation of ConflictResolver for detecting and resolving sync conflicts
 * Uses checksum comparison to identify conflicts and provides multiple resolution strategies
 */
class ConflictResolverImpl(
    private val checksumService: ChecksumService
) : ConflictResolver {
    
    override suspend fun detectConflict(
        localRecipe: Recipe,
        remoteRecipe: Recipe
    ): SyncConflict? {
        // If recipes are the same (same ID), check if content differs
        if (localRecipe.id == remoteRecipe.id) {
            val localChecksum = checksumService.calculateChecksum(localRecipe)
            val remoteChecksum = checksumService.calculateChecksum(remoteRecipe)
            
            if (localChecksum == remoteChecksum) {
                return null // No conflict, same content
            }
            
            // Same ID but different content - this is a conflict
            return createSyncConflict(localRecipe, remoteRecipe, localChecksum, remoteChecksum)
        }
        
        // Different IDs - check if content is the same (duplicate detection)
        val localChecksum = checksumService.calculateChecksum(localRecipe)
        val remoteChecksum = checksumService.calculateChecksum(remoteRecipe)
        
        if (localChecksum == remoteChecksum) {
            // Same content but different IDs - this could be a duplicate
            // For now, treat as no conflict (they represent the same recipe)
            return null
        }
        
        // Different IDs and different content - this is a conflict
        return createSyncConflict(localRecipe, remoteRecipe, localChecksum, remoteChecksum)
    }
    
    override suspend fun resolveConflict(
        conflict: SyncConflict,
        strategy: ConflictResolution
    ): ConflictResolutionResult {
        return when (strategy) {
            is ConflictResolution.KeepLocal -> {
                // Keep local version, mark remote for overwrite
                ConflictResolutionResult(
                    resolvedRecipe = getLocalRecipe(conflict),
                    action = ResolutionAction.OVERWRITE_REMOTE,
                    timestamp = Instant.now()
                )
            }
            is ConflictResolution.KeepRemote -> {
                // Keep remote version, update local
                ConflictResolutionResult(
                    resolvedRecipe = getRemoteRecipe(conflict),
                    action = ResolutionAction.UPDATE_LOCAL,
                    timestamp = Instant.now()
                )
            }
            is ConflictResolution.Merge -> {
                // Use merged recipe
                ConflictResolutionResult(
                    resolvedRecipe = strategy.mergedRecipe,
                    action = ResolutionAction.MERGE,
                    timestamp = Instant.now()
                )
            }
        }
    }
    
    override fun hasConflict(localRecipe: Recipe, remoteRecipe: Recipe): Boolean {
        // Quick check without creating full conflict object
        val localChecksum = checksumService.calculateChecksum(localRecipe)
        val remoteChecksum = checksumService.calculateChecksum(remoteRecipe)
        return localChecksum != remoteChecksum
    }
    
    override fun canAutoResolve(conflict: SyncConflict): Boolean {
        // For now, we can auto-resolve if one version is clearly newer
        // This is a simple heuristic - in a real system, this would be more sophisticated
        
        // Check if one version vector is newer than the other
        val localIsNewer = conflict.localVersion.isNewerThan(conflict.remoteVersion)
        val remoteIsNewer = conflict.remoteVersion.isNewerThan(conflict.localVersion)
        
        // If one is clearly newer, we can auto-resolve by keeping the newer version
        return localIsNewer || remoteIsNewer
    }
    
    override suspend fun autoResolve(conflict: SyncConflict): ConflictResolutionResult? {
        if (!canAutoResolve(conflict)) {
            return null // Cannot auto-resolve
        }
        
        // Keep the newer version
        val localIsNewer = conflict.localVersion.isNewerThan(conflict.remoteVersion)
        
        return if (localIsNewer) {
            ConflictResolutionResult(
                resolvedRecipe = getLocalRecipe(conflict),
                action = ResolutionAction.OVERWRITE_REMOTE,
                timestamp = Instant.now()
            )
        } else {
            ConflictResolutionResult(
                resolvedRecipe = getRemoteRecipe(conflict),
                action = ResolutionAction.UPDATE_LOCAL,
                timestamp = Instant.now()
            )
        }
    }
    
    /**
     * Create a SyncConflict object from local and remote recipes
     */
    private fun createSyncConflict(
        localRecipe: Recipe,
        remoteRecipe: Recipe,
        localChecksum: String,
        remoteChecksum: String
    ): SyncConflict {
        return SyncConflict(
            id = UUID.randomUUID().toString(),
            localRecipeId = localRecipe.id,
            remoteRecipeId = remoteRecipe.id,
            localChecksum = localChecksum,
            remoteChecksum = remoteChecksum,
            localVersion = localRecipe.versionVector,
            remoteVersion = remoteRecipe.versionVector,
            detectedAt = Instant.now(),
            resolvedAt = null,
            status = com.ourcookbook.domain.model.ConflictStatus.PENDING,
            resolution = null
        )
    }
    
    /**
     * Helper method to get local recipe from conflict
     * In a real implementation, this would fetch the actual recipe from repository
     */
    private fun getLocalRecipe(conflict: SyncConflict): Recipe {
        // For now, create a placeholder recipe with the conflict's local recipe ID
        // In a real implementation, this would fetch from the local repository
        return Recipe(
            id = conflict.localRecipeId,
            title = "Local Recipe ${conflict.localRecipeId.take(8)}",
            category = "Unknown",
            versionVector = conflict.localVersion,
            checksum = conflict.localChecksum,
            deviceId = conflict.localVersion.deviceId
        )
    }
    
    /**
     * Helper method to get remote recipe from conflict
     * In a real implementation, this would fetch the actual recipe from repository
     */
    private fun getRemoteRecipe(conflict: SyncConflict): Recipe {
        // For now, create a placeholder recipe with the conflict's remote recipe ID
        // In a real implementation, this would fetch from the remote repository
        return Recipe(
            id = conflict.remoteRecipeId,
            title = "Remote Recipe ${conflict.remoteRecipeId.take(8)}",
            category = "Unknown",
            versionVector = conflict.remoteVersion,
            checksum = conflict.remoteChecksum,
            deviceId = conflict.remoteVersion.deviceId
        )
    }
    
    /**
     * Check if two recipes have the same content based on checksums
     */
    fun haveSameContent(recipe1: Recipe, recipe2: Recipe): Boolean {
        val checksum1 = checksumService.calculateChecksum(recipe1)
        val checksum2 = checksumService.calculateChecksum(recipe2)
        return checksum1 == checksum2
    }
    
    /**
     * Check if a conflict is due to the same recipe being modified on both devices
     */
    fun isSameRecipeConflict(conflict: SyncConflict): Boolean {
        return conflict.localRecipeId == conflict.remoteRecipeId
    }
    
    /**
     * Check if a conflict is due to different recipes with potentially duplicate content
     */
    fun isDuplicateContentConflict(conflict: SyncConflict): Boolean {
        return conflict.localRecipeId != conflict.remoteRecipeId &&
               conflict.localChecksum == conflict.remoteChecksum
    }
    
    /**
     * Get the recommended resolution strategy based on version vectors
     */
    fun getRecommendedStrategy(conflict: SyncConflict): ConflictResolution {
        val localIsNewer = conflict.localVersion.isNewerThan(conflict.remoteVersion)
        return if (localIsNewer) {
            ConflictResolution.KeepLocal
        } else {
            ConflictResolution.KeepRemote
        }
    }
}
