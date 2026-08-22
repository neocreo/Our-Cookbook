package com.ourcookbook.domain.usecase.sync

import com.ourcookbook.data.repository.DriveRepository
import com.ourcookbook.domain.model.Recipe
import com.ourcookbook.domain.repository.RecipeRepository
import javax.inject.Inject

/**
 * Use case for processing tombstones during pull
 * Task 2.2.19: Tombstone processing during pull
 *
 * Handles deleted recipes (tombstones) during sync operations
 */
class TombstoneProcessor @Inject constructor(
    private val driveRepository: DriveRepository,
    private val recipeRepository: RecipeRepository
) {

    /**
     * Tombstone information
     */
    data class TombstoneInfo(
        val recipeId: String,
        val deletedAt: Long,
        val deletedBy: String?,
        val version: Long,
        val checksum: String?
    )

    /**
     * Result of tombstone processing
     */
    sealed class TombstoneResult {
        data class Processed(
            val recipeId: String,
            val action: TombstoneAction,
            val message: String
        ) : TombstoneResult()
        
        data class Skipped(
            val recipeId: String,
            val reason: String
        ) : TombstoneResult()
        
        data class Failure(
            val errorMessage: String,
            val exception: Exception? = null
        ) : TombstoneResult()
    }

    /**
     * Action to take when processing a tombstone
     */
    enum class TombstoneAction {
        DELETE_LOCAL,      // Delete the local recipe
        RESTORE_REMOTE,   // Restore from remote (if available)
        KEEP_LOCAL,       // Keep the local recipe
        ASK_USER,         // Ask user to decide
        IGNORE            // Ignore the tombstone
    }

    /**
     * Process tombstones during pull operation
     *
     * @param tombstones List of tombstone information
     * @param strategy Default strategy for processing
     * @return List of TombstoneResult objects
     */
    suspend fun processTombstones(
        tombstones: List<TombstoneInfo>,
        strategy: TombstoneAction = TombstoneAction.DELETE_LOCAL
    ): List<TombstoneResult> {
        return tombstones.map { tombstone ->
            processTombstone(tombstone, strategy)
        }
    }

    /**
     * Process a single tombstone
     *
     * @param tombstone Tombstone information
     * @param strategy Default strategy for processing
     * @return TombstoneResult with operation information
     */
    suspend fun processTombstone(
        tombstone: TombstoneInfo,
        strategy: TombstoneAction = TombstoneAction.DELETE_LOCAL
    ): TombstoneResult {
        return when (strategy) {
            TombstoneAction.DELETE_LOCAL -> {
                deleteLocalRecipe(tombstone)
            }
            TombstoneAction.RESTORE_REMOTE -> {
                restoreFromRemote(tombstone)
            }
            TombstoneAction.KEEP_LOCAL -> {
                keepLocalRecipe(tombstone)
            }
            TombstoneAction.ASK_USER -> {
                TombstoneResult.Skipped(
                    recipeId = tombstone.recipeId,
                    reason = "User decision required"
                )
            }
            TombstoneAction.IGNORE -> {
                TombstoneResult.Skipped(
                    recipeId = tombstone.recipeId,
                    reason = "Ignored"
                )
            }
        }
    }

    /**
     * Delete the local recipe when tombstone is found
     */
    private suspend fun deleteLocalRecipe(tombstone: TombstoneInfo): TombstoneResult {
        return try {
            val localRecipe = recipeRepository.getRecipeById(tombstone.recipeId)
            
            if (localRecipe != null) {
                // Check if the local recipe has been modified since the tombstone was created
                if (localRecipe.updatedAt > tombstone.deletedAt) {
                    return TombstoneResult.Skipped(
                        recipeId = tombstone.recipeId,
                        reason = "Local recipe was modified after remote deletion"
                    )
                }
                
                // Delete the local recipe
                recipeRepository.deleteRecipe(tombstone.recipeId)
                
                TombstoneResult.Processed(
                    recipeId = tombstone.recipeId,
                    action = TombstoneAction.DELETE_LOCAL,
                    message = "Deleted local recipe"
                )
            } else {
                TombstoneResult.Skipped(
                    recipeId = tombstone.recipeId,
                    reason = "Local recipe not found"
                )
            }
        } catch (e: Exception) {
            TombstoneResult.Failure(
                errorMessage = "Failed to delete local recipe: ${e.message}",
                exception = e
            )
        }
    }

    /**
     * Restore recipe from remote when tombstone is found
     */
    private suspend fun restoreFromRemote(tombstone: TombstoneInfo): TombstoneResult {
        return try {
            // Try to get the recipe from remote (might be in trash or version history)
            val remoteRecipe = driveRepository.getRecipeFromHistory(tombstone.recipeId, tombstone.version)
            
            if (remoteRecipe != null) {
                // Restore the recipe locally
                recipeRepository.createRecipe(remoteRecipe)
                
                TombstoneResult.Processed(
                    recipeId = tombstone.recipeId,
                    action = TombstoneAction.RESTORE_REMOTE,
                    message = "Restored recipe from remote history"
                )
            } else {
                TombstoneResult.Skipped(
                    recipeId = tombstone.recipeId,
                    reason = "Remote recipe not found in history"
                )
            }
        } catch (e: Exception) {
            TombstoneResult.Failure(
                errorMessage = "Failed to restore from remote: ${e.message}",
                exception = e
            )
        }
    }

    /**
     * Keep the local recipe when tombstone is found
     */
    private suspend fun keepLocalRecipe(tombstone: TombstoneInfo): TombstoneResult {
        return try {
            val localRecipe = recipeRepository.getRecipeById(tombstone.recipeId)
            
            if (localRecipe != null) {
                // Mark the recipe as needing sync (it will be pushed back to Drive)
                recipeRepository.updateRecipeSyncStatus(
                    localRecipe.id,
                    com.ourcookbook.domain.model.SyncStatus.NEEDS_SYNC,
                    System.currentTimeMillis(),
                    localRecipe.checksum ?: ""
                )
                
                TombstoneResult.Processed(
                    recipeId = tombstone.recipeId,
                    action = TombstoneAction.KEEP_LOCAL,
                    message = "Kept local recipe, marked for sync"
                )
            } else {
                TombstoneResult.Skipped(
                    recipeId = tombstone.recipeId,
                    reason = "Local recipe not found"
                )
            }
        } catch (e: Exception) {
            TombstoneResult.Failure(
                errorMessage = "Failed to keep local recipe: ${e.message}",
                exception = e
            )
        }
    }

    /**
     * Get tombstones from Drive
     *
     * @return List of TombstoneInfo objects
     */
    suspend fun getTombstones(): List<TombstoneInfo> {
        return try {
            val remoteTombstones = driveRepository.getTombstones()
            
            remoteTombstones.map { tombstone ->
                TombstoneInfo(
                    recipeId = tombstone.recipeId,
                    deletedAt = tombstone.deletedAt,
                    deletedBy = tombstone.deletedBy,
                    version = tombstone.version,
                    checksum = tombstone.checksum
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Get tombstones for specific recipe IDs
     *
     * @param recipeIds List of recipe IDs to check
     * @return List of TombstoneInfo objects for the specified IDs
     */
    suspend fun getTombstonesForRecipes(recipeIds: List<String>): List<TombstoneInfo> {
        return try {
            val remoteTombstones = driveRepository.getTombstonesForRecipes(recipeIds)
            
            remoteTombstones.map { tombstone ->
                TombstoneInfo(
                    recipeId = tombstone.recipeId,
                    deletedAt = tombstone.deletedAt,
                    deletedBy = tombstone.deletedBy,
                    version = tombstone.version,
                    checksum = tombstone.checksum
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Check if a recipe has a tombstone
     *
     * @param recipeId ID of the recipe to check
     * @return TombstoneInfo if exists, null otherwise
     */
    suspend fun hasTombstone(recipeId: String): TombstoneInfo? {
        return try {
            val tombstone = driveRepository.getTombstone(recipeId)
            
            tombstone?.let {
                TombstoneInfo(
                    recipeId = it.recipeId,
                    deletedAt = it.deletedAt,
                    deletedBy = it.deletedBy,
                    version = it.version,
                    checksum = it.checksum
                )
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Process tombstones during pull and return results
     *
     * @param recipeIds List of recipe IDs being pulled
     * @param strategy Default strategy for processing
     * @return List of TombstoneResult objects
     */
    suspend fun processTombstonesDuringPull(
        recipeIds: List<String>,
        strategy: TombstoneAction = TombstoneAction.DELETE_LOCAL
    ): List<TombstoneResult> {
        // Get tombstones for the recipes being pulled
        val tombstones = getTombstonesForRecipes(recipeIds)
        
        // Process each tombstone
        return processTombstones(tombstones, strategy)
    }

    /**
     * Get tombstone statistics
     *
     * @return Map of statistics
     */
    suspend fun getTombstoneStatistics(): Map<String, Any> {
        val tombstones = getTombstones()
        
        val byDeleter = tombstones.groupBy { it.deletedBy ?: "Unknown" }
        val byDate = tombstones.groupBy { 
            java.time.LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(it.deletedAt),
                java.time.ZoneId.systemDefault()
            ).toLocalDate().toString()
        }
        
        return mapOf(
            "total_tombstones" to tombstones.size,
            "by_deleter" to byDeleter.mapValues { it.value.size },
            "by_date" to byDate.mapValues { it.value.size },
            "recent_tombstones" to tombstones.filter { 
                it.deletedAt > System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000) 
            }.size
        )
    }

    /**
     * Clean up tombstones (remove from Drive)
     *
     * @param olderThan Delete tombstones older than this timestamp
     * @return Number of tombstones cleaned up
     */
    suspend fun cleanupTombstones(olderThan: Long): Int {
        return try {
            driveRepository.deleteTombstonesOlderThan(olderThan)
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Restore all tombstones
     *
     * @return Number of recipes restored
     */
    suspend fun restoreAllTombstones(): Int {
        val tombstones = getTombstones()
        var count = 0
        
        tombstones.forEach { tombstone ->
            val result = restoreFromRemote(tombstone)
            if (result is TombstoneResult.Processed) {
                count++
            }
        }
        
        return count
    }

    /**
     * Delete all local recipes that have tombstones
     *
     * @return Number of recipes deleted
     */
    suspend fun deleteAllTombstonedRecipes(): Int {
        val tombstones = getTombstones()
        var count = 0
        
        tombstones.forEach { tombstone ->
            val result = deleteLocalRecipe(tombstone)
            if (result is TombstoneResult.Processed) {
                count++
            }
        }
        
        return count
    }

    /**
     * Get tombstones that have local recipes
     *
     * @return List of TombstoneInfo objects that have local recipes
     */
    suspend fun getTombstonesWithLocalRecipes(): List<TombstoneInfo> {
        val tombstones = getTombstones()
        val localRecipeIds = recipeRepository.getAllRecipeIds().toSet()
        
        return tombstones.filter { 
            it.recipeId in localRecipeIds 
        }
    }

    /**
     * Get tombstones without local recipes
     *
     * @return List of TombstoneInfo objects that don't have local recipes
     */
    suspend fun getTombstonesWithoutLocalRecipes(): List<TombstoneInfo> {
        val tombstones = getTombstones()
        val localRecipeIds = recipeRepository.getAllRecipeIds().toSet()
        
        return tombstones.filter { 
            it.recipeId !in localRecipeIds 
        }
    }

    /**
     * Get tombstones that were deleted recently
     *
     * @param hours Number of hours to look back
     * @return List of recent TombstoneInfo objects
     */
    suspend fun getRecentTombstones(hours: Int = 24): List<TombstoneInfo> {
        val tombstones = getTombstones()
        val cutoff = System.currentTimeMillis() - (hours * 60 * 60 * 1000)
        
        return tombstones.filter { 
            it.deletedAt > cutoff 
        }
    }

    /**
     * Check if a recipe ID exists as a tombstone
     *
     * @param recipeId ID of the recipe to check
     * @return true if tombstone exists
     */
    suspend fun existsAsTombstone(recipeId: String): Boolean {
        return hasTombstone(recipeId) != null
    }

    /**
     * Get tombstone processing report
     *
     * @param tombstones List of tombstones to process
     * @param strategy Strategy to use for processing
     * @return Map with processing report
     */
    suspend fun getProcessingReport(
        tombstones: List<TombstoneInfo>,
        strategy: TombstoneAction
    ): Map<String, Any> {
        val results = processTombstones(tombstones, strategy)
        
        val processed = results.filterIsInstance<TombstoneResult.Processed>().size
        val skipped = results.filterIsInstance<TombstoneResult.Skipped>().size
        val failed = results.filterIsInstance<TombstoneResult.Failure>().size
        
        val byAction = results.filterIsInstance<TombstoneResult.Processed>().groupBy { it.action }
        
        return mapOf(
            "total" to tombstones.size,
            "processed" to processed,
            "skipped" to skipped,
            "failed" to failed,
            "by_action" to byAction.mapValues { it.value.size },
            "success_rate" to if (tombstones.isNotEmpty()) processed.toFloat() / tombstones.size else 0f
        )
    }
}
