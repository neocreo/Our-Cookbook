package com.ourcookbook.domain.usecase.sync

import com.ourcookbook.data.repository.DriveRepository
import com.ourcookbook.domain.model.Recipe
import com.ourcookbook.domain.model.SyncOperation
import com.ourcookbook.domain.repository.RecipeRepository
import java.security.MessageDigest
import javax.inject.Inject

/**
 * Use case for pushing changes to Google Drive with pre-push checksum verification
 * Task 2.2.13: Push changes to Google Drive with pre-push checksum verification
 *
 * Ensures data integrity by verifying checksums before pushing changes to Drive
 */
class PushToDriveWithChecksum @Inject constructor(
    private val driveRepository: DriveRepository,
    private val recipeRepository: RecipeRepository
) {

    /**
     * Result of push operation with checksum verification
     */
    sealed class PushResult {
        data class Success(
            val syncedRecipes: List<Recipe>,
            val checksums: Map<String, String>,
            val operationId: String,
            val timestamp: Long
        ) : PushResult()
        
        data class ChecksumMismatch(
            val recipeId: String,
            val localChecksum: String,
            val remoteChecksum: String?,
            val message: String
        ) : PushResult()
        
        data class Conflict(
            val recipeId: String,
            val localVersion: Long,
            val remoteVersion: Long,
            val message: String
        ) : PushResult()
        
        data class Failure(
            val errorMessage: String,
            val exception: Exception? = null
        ) : PushResult()
    }

    /**
     * Push settings
     */
    data class PushSettings(
        val forcePush: Boolean = false,
        val verifyChecksums: Boolean = true,
        val includeDeleted: Boolean = true,
        val batchSize: Int = 50
    )

    /**
     * Push changes to Google Drive with checksum verification
     *
     * @param recipeIds List of recipe IDs to push (empty for all)
     * @param settings Push settings
     * @return PushResult with operation information
     */
    suspend operator fun invoke(
        recipeIds: List<String> = emptyList(),
        settings: PushSettings = PushSettings()
    ): List<PushResult> {
        return try {
            // Get recipes to sync
            val recipesToSync = if (recipeIds.isEmpty()) {
                recipeRepository.getAllRecipesOnce()
            } else {
                recipeRepository.getRecipesByIds(recipeIds)
            }
            
            if (recipesToSync.isEmpty()) {
                return listOf(PushResult.Failure("No recipes to sync"))
            }
            
            // Calculate local checksums
            val localChecksums = calculateChecksums(recipesToSync)
            
            // Check connection and authentication
            if (!driveRepository.isAuthenticated()) {
                return listOf(PushResult.Failure("Not authenticated with Google Drive"))
            }
            
            // Get remote checksums for comparison
            val remoteChecksums = if (settings.verifyChecksums) {
                driveRepository.getRemoteChecksums(recipesToSync.map { it.id })
            } else {
                emptyMap()
            }
            
            // Verify checksums before pushing
            val results = mutableListOf<PushResult>()
            
            recipesToSync.forEach { recipe ->
                val localChecksum = localChecksums[recipe.id]
                val remoteChecksum = remoteChecksums[recipe.id]
                
                if (settings.verifyChecksums && remoteChecksum != null) {
                    if (localChecksum != remoteChecksum) {
                        results.add(
                            PushResult.ChecksumMismatch(
                                recipeId = recipe.id,
                                localChecksum = localChecksum ?: "",
                                remoteChecksum = remoteChecksum,
                                message = "Checksum mismatch for recipe ${recipe.title}"
                            )
                        )
                        return@forEach
                    }
                }
                
                // Check for version conflicts
                val remoteVersion = driveRepository.getRemoteVersion(recipe.id)
                if (remoteVersion != null && remoteVersion > recipe.version) {
                    results.add(
                        PushResult.Conflict(
                            recipeId = recipe.id,
                            localVersion = recipe.version,
                            remoteVersion = remoteVersion,
                            message = "Version conflict for recipe ${recipe.title}"
                        )
                    )
                    return@forEach
                }
            }
            
            // If we have only checksum mismatches or conflicts, return them
            if (results.any { it is PushResult.ChecksumMismatch || it is PushResult.Conflict }) {
                return results
            }
            
            // All checks passed, proceed with push
            if (settings.forcePush || results.none { it is PushResult.ChecksumMismatch || it is PushResult.Conflict }) {
                val pushResult = performPush(recipesToSync, localChecksums, settings)
                
                // Convert to list for consistency
                return when (pushResult) {
                    is PushResult.Success -> listOf(pushResult)
                    is PushResult.Failure -> listOf(pushResult)
                    else -> listOf(pushResult)
                }
            }
            
            return results
            
        } catch (e: Exception) {
            listOf(PushResult.Failure(
                errorMessage = "Failed to push to Drive: ${e.message}",
                exception = e
            ))
        }
    }

    /**
     * Perform the actual push operation
     */
    private suspend fun performPush(
        recipes: List<Recipe>,
        checksums: Map<String, String>,
        settings: PushSettings
    ): PushResult {
        return try {
            val operationId = java.util.UUID.randomUUID().toString()
            val timestamp = System.currentTimeMillis()
            
            // Push recipes in batches
            recipes.chunked(settings.batchSize).forEach { batch ->
                driveRepository.pushRecipes(batch, checksums, operationId)
            }
            
            // Update local sync status
            recipes.forEach { recipe ->
                recipeRepository.updateRecipeSyncStatus(
                    recipe.id,
                    true,
                    timestamp,
                    checksums[recipe.id] ?: ""
                )
            }
            
            // Record the operation
            val operation = SyncOperation.create(
                id = operationId,
                type = "PUSH",
                status = "COMPLETED",
                recipeIds = recipes.map { it.id },
                timestamp = timestamp,
                checksums = checksums
            )
            
            // Save operation to history
            // This would be implemented with a SyncHistoryRepository
            
            PushResult.Success(
                syncedRecipes = recipes,
                checksums = checksums,
                operationId = operationId,
                timestamp = timestamp
            )
            
        } catch (e: Exception) {
            PushResult.Failure(
                errorMessage = "Failed to perform push: ${e.message}",
                exception = e
            )
        }
    }

    /**
     * Calculate MD5 checksum for a recipe
     */
    fun calculateChecksum(recipe: Recipe): String {
        val content = buildChecksumContent(recipe)
        val bytes = content.toByteArray(Charsets.UTF_8)
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }
    }

    /**
     * Calculate checksums for multiple recipes
     */
    fun calculateChecksums(recipes: List<Recipe>): Map<String, String> {
        return recipes.associate { recipe ->
            recipe.id to calculateChecksum(recipe)
        }
    }

    /**
     * Build content for checksum calculation
     * Includes all relevant fields that affect the recipe
     */
    private fun buildChecksumContent(recipe: Recipe): String {
        return """|
            |${recipe.id}|
            |${recipe.title}|
            |${recipe.description}|
            |${recipe.category}|
            |${recipe.servingSize}|
            |${recipe.prepTime}|
            |${recipe.cookTime}|
            |${recipe.totalTime}|
            |${recipe.rating}|
            |${recipe.favorite}|
            |${recipe.source}|
            |${recipe.tags.joinToString(",")}|
            |${recipe.ingredients.joinToString("|") { "${it.name}|${it.amount}|${it.unit}|${it.notes}" }}|
            |${recipe.instructions.joinToString("|")}|
            |${recipe.version}|
            |${recipe.createdAt}|
            |${recipe.updatedAt}|
        """.trimMargin()
    }

    /**
     * Verify checksum before push
     *
     * @param recipe The recipe to verify
     * @param remoteChecksum The checksum from the remote
     * @return true if checksums match or if remote doesn't exist
     */
    suspend fun verifyChecksum(
        recipe: Recipe,
        remoteChecksum: String?
    ): Boolean {
        if (remoteChecksum == null) {
            // No remote version, safe to push
            return true
        }
        
        val localChecksum = calculateChecksum(recipe)
        return localChecksum == remoteChecksum
    }

    /**
     * Get pre-push verification report
     *
     * @param recipeIds List of recipe IDs to verify
     * @return Map of recipe ID to verification status
     */
    suspend fun getVerificationReport(
        recipeIds: List<String>
    ): Map<String, VerificationStatus> {
        val recipes = recipeRepository.getRecipesByIds(recipeIds)
        val localChecksums = calculateChecksums(recipes)
        
        val remoteChecksums = driveRepository.getRemoteChecksums(recipeIds)
        val remoteVersions = driveRepository.getRemoteVersions(recipeIds)
        
        return recipes.associate { recipe ->
            val localChecksum = localChecksums[recipe.id] ?: ""
            val remoteChecksum = remoteChecksums[recipe.id]
            val remoteVersion = remoteVersions[recipe.id]
            
            val status = when {
                remoteChecksum == null -> VerificationStatus.NEW
                localChecksum == remoteChecksum -> VerificationStatus.MATCH
                remoteVersion != null && remoteVersion > recipe.version -> VerificationStatus.CONFLICT
                else -> VerificationStatus.MISMATCH
            }
            
            recipe.id to status
        }
    }

    /**
     * Verification status for pre-push check
     */
    enum class VerificationStatus {
        MATCH,       // Local and remote checksums match
        MISMATCH,    // Checksums don't match
        NEW,         // No remote version exists
        CONFLICT     // Remote version is newer
    }

    /**
     * Resolve checksum mismatch
     *
     * @param recipe The recipe with mismatch
     * @param strategy Resolution strategy
     * @return PushResult with resolution information
     */
    suspend fun resolveChecksumMismatch(
        recipe: Recipe,
        strategy: ChecksumMismatchStrategy
    ): PushResult {
        return when (strategy) {
            ChecksumMismatchStrategy.FORCE_PUSH -> {
                // Force push even with checksum mismatch
                val checksum = calculateChecksum(recipe)
                driveRepository.pushRecipe(recipe, checksum, java.util.UUID.randomUUID().toString())
                PushResult.Success(
                    syncedRecipes = listOf(recipe),
                    checksums = mapOf(recipe.id to checksum),
                    operationId = java.util.UUID.randomUUID().toString(),
                    timestamp = System.currentTimeMillis()
                )
            }
            ChecksumMismatchStrategy.PULL_FIRST -> {
                // Pull the remote version first
                val remoteRecipe = driveRepository.pullRecipe(recipe.id)
                if (remoteRecipe != null) {
                    // Merge or replace local recipe
                    recipeRepository.updateRecipe(remoteRecipe)
                }
                PushResult.Failure("Pulled remote version, please retry push")
            }
            ChecksumMismatchStrategy.SKIP -> {
                PushResult.ChecksumMismatch(
                    recipeId = recipe.id,
                    localChecksum = calculateChecksum(recipe),
                    remoteChecksum = driveRepository.getRemoteChecksum(recipe.id),
                    message = "Skipped due to checksum mismatch"
                )
            }
            ChecksumMismatchStrategy.MERGE -> {
                // Merge changes (this would be implemented based on specific merge logic)
                PushResult.Failure("Merge strategy not implemented")
            }
        }
    }

    /**
     * Strategy for resolving checksum mismatches
     */
    enum class ChecksumMismatchStrategy {
        FORCE_PUSH,   // Force push local version
        PULL_FIRST,   // Pull remote version first
        SKIP,         // Skip this recipe
        MERGE         // Merge changes
    }

    /**
     * Check if push is possible (authenticated and connected)
     */
    suspend fun isPushPossible(): Boolean {
        return driveRepository.isAuthenticated() && driveRepository.isConnected()
    }

    /**
     * Get pending changes that need to be pushed
     */
    suspend fun getPendingChanges(): List<Recipe> {
        return recipeRepository.getRecipesNeedingSync()
    }

    /**
     * Get count of pending changes
     */
    suspend fun getPendingChangesCount(): Int {
        return recipeRepository.getRecipesNeedingSyncCount()
    }
}
