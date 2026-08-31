package com.ourcookbook.domain.usecase.sync

import com.ourcookbook.domain.model.Recipe
import com.ourcookbook.domain.repository.RecipeRepository
import java.time.Instant
import javax.inject.Inject

/**
 * Use case for conflict detection and resolution
 * Task 2.2.17: Conflict detection and resolution UI
 *
 * Provides functionality to detect and resolve conflicts between local and remote recipes
 */
class ConflictResolver @Inject constructor(
    private val recipeRepository: RecipeRepository
) {

    /**
     * Conflict type
     */
    enum class ConflictType {
        VERSION_MISMATCH,      // Local and remote versions differ
        CHECKSUM_MISMATCH,     // Content checksums differ
        DELETED_LOCAL,         // Recipe deleted locally but exists remotely
        DELETED_REMOTE,        // Recipe deleted remotely but exists locally
        BOTH_MODIFIED          // Both local and remote versions modified
    }

    /**
     * Conflict information
     */
    data class ConflictInfo(
        val recipeId: String,
        val localRecipe: Recipe?,
        val remoteRecipe: Recipe?,
        val conflictType: ConflictType,
        val localVersion: Long,
        val remoteVersion: Long,
        val localChecksum: String?,
        val remoteChecksum: String?,
        val detectedAt: Instant = Instant.now()
    )

    /**
     * Conflict resolution strategy
     */
    enum class ResolutionStrategy {
        KEEP_LOCAL,          // Keep local version
        KEEP_REMOTE,         // Keep remote version
        MERGE,               // Merge changes
        KEEP_BOTH,           // Keep both (create copy)
        ASK_USER            // Ask user to decide
    }

    /**
     * Resolution result
     */
    sealed class ResolutionResult {
        data class Resolved(
            val recipe: Recipe,
            val strategy: ResolutionStrategy,
            val message: String
        ) : ResolutionResult()
        
        data class Skipped(
            val conflict: ConflictInfo,
            val message: String
        ) : ResolutionResult()
        
        data class Failure(
            val errorMessage: String,
            val exception: Exception? = null
        ) : ResolutionResult()
    }

    /**
     * Detect conflicts between local and remote recipes
     *
     * @param localRecipes List of local recipes
     * @param remoteRecipes Map of recipe ID to remote recipe
     * @param remoteChecksums Map of recipe ID to remote checksum
     * @return List of ConflictInfo objects
     */
    fun detectConflicts(
        localRecipes: List<Recipe>,
        remoteRecipes: Map<String, Recipe>,
        remoteChecksums: Map<String, String>? = null
    ): List<ConflictInfo> {
        val conflicts = mutableListOf<ConflictInfo>()
        
        localRecipes.forEach { localRecipe ->
            val remoteRecipe = remoteRecipes[localRecipe.id]
            val remoteChecksum = remoteChecksums?.get(localRecipe.id)
            
            when {
                remoteRecipe == null -> {
                    // Recipe exists locally but not remotely
                    // This could be a new recipe or a deleted remote
                    conflicts.add(
                        ConflictInfo(
                            recipeId = localRecipe.id,
                            localRecipe = localRecipe,
                            remoteRecipe = null,
                            conflictType = ConflictType.DELETED_REMOTE,
                            localVersion = localRecipe.versionVector.counter.toLong(),
                            remoteVersion = 0,
                            localChecksum = localRecipe.checksum,
                            remoteChecksum = null
                        )
                    )
                }
                localRecipe.checksum != remoteChecksum -> {
                    // Content differs
                    conflicts.add(
                        ConflictInfo(
                            recipeId = localRecipe.id,
                            localRecipe = localRecipe,
                            remoteRecipe = remoteRecipe,
                            conflictType = ConflictType.CHECKSUM_MISMATCH,
                            localVersion = localRecipe.versionVector.counter.toLong(),
                            remoteVersion = remoteRecipe.versionVector.counter.toLong(),
                            localChecksum = localRecipe.checksum,
                            remoteChecksum = remoteChecksum
                        )
                    )
                }
                localRecipe.versionVector.counter.toLong() != remoteRecipe.versionVector.counter.toLong() -> {
                    // Versions differ but checksums match (unlikely but possible)
                    conflicts.add(
                        ConflictInfo(
                            recipeId = localRecipe.id,
                            localRecipe = localRecipe,
                            remoteRecipe = remoteRecipe,
                            conflictType = ConflictType.VERSION_MISMATCH,
                            localVersion = localRecipe.versionVector.counter.toLong(),
                            remoteVersion = remoteRecipe.versionVector.counter.toLong(),
                            localChecksum = localRecipe.checksum,
                            remoteChecksum = remoteChecksum
                        )
                    )
                }
                localRecipe.updatedAt != remoteRecipe.updatedAt -> {
                    // Both modified (checksums match but timestamps differ)
                    conflicts.add(
                        ConflictInfo(
                            recipeId = localRecipe.id,
                            localRecipe = localRecipe,
                            remoteRecipe = remoteRecipe,
                            conflictType = ConflictType.BOTH_MODIFIED,
                            localVersion = localRecipe.versionVector.counter.toLong(),
                            remoteVersion = remoteRecipe.versionVector.counter.toLong(),
                            localChecksum = localRecipe.checksum,
                            remoteChecksum = remoteChecksum
                        )
                    )
                }
            }
        }
        
        // Check for recipes that exist remotely but not locally
        remoteRecipes.forEach { (recipeId, remoteRecipe) ->
            if (localRecipes.none { it.id == recipeId }) {
                conflicts.add(
                    ConflictInfo(
                        recipeId = recipeId,
                        localRecipe = null,
                        remoteRecipe = remoteRecipe,
                        conflictType = ConflictType.DELETED_LOCAL,
                        localVersion = 0,
                        remoteVersion = remoteRecipe.versionVector.counter.toLong(),
                        localChecksum = null,
                        remoteChecksum = remoteRecipe.checksum
                    )
                )
            }
        }
        
        return conflicts
    }

    /**
     * Resolve a single conflict
     *
     * @param conflict The conflict to resolve
     * @param strategy The resolution strategy
     * @return ResolutionResult with operation information
     */
    suspend fun resolveConflict(
        conflict: ConflictInfo,
        strategy: ResolutionStrategy
    ): ResolutionResult {
        return when (strategy) {
            ResolutionStrategy.KEEP_LOCAL -> {
                resolveKeepLocal(conflict)
            }
            ResolutionStrategy.KEEP_REMOTE -> {
                resolveKeepRemote(conflict)
            }
            ResolutionStrategy.MERGE -> {
                resolveMerge(conflict)
            }
            ResolutionStrategy.KEEP_BOTH -> {
                resolveKeepBoth(conflict)
            }
            ResolutionStrategy.ASK_USER -> {
                ResolutionResult.Skipped(
                    conflict = conflict,
                    message = "User decision required"
                )
            }
        }
    }

    private suspend fun createOrUpdateRecipe(recipe: Recipe) {
        if (recipeRepository.getRecipeById(recipe.id) == null) {
            recipeRepository.createRecipe(recipe)
        } else {
            recipeRepository.updateRecipe(recipe)
        }
    }

    /**
     * Resolve by keeping local version
     */
    private suspend fun resolveKeepLocal(conflict: ConflictInfo): ResolutionResult {
        return try {
            conflict.localRecipe?.let { localRecipe ->
                // Update the recipe to mark it as synced
                recipeRepository.markRecipeSynced(localRecipe.id)

                ResolutionResult.Resolved(
                    recipe = localRecipe,
                    strategy = ResolutionStrategy.KEEP_LOCAL,
                    message = "Kept local version"
                )
            } ?: run {
                ResolutionResult.Failure("No local recipe to keep")
            }
        } catch (e: Exception) {
            ResolutionResult.Failure(
                errorMessage = "Failed to resolve conflict: ${e.message}",
                exception = e
            )
        }
    }

    /**
     * Resolve by keeping remote version
     */
    private suspend fun resolveKeepRemote(conflict: ConflictInfo): ResolutionResult {
        return try {
            conflict.remoteRecipe?.let { remoteRecipe ->
                // Save remote recipe as local
                createOrUpdateRecipe(remoteRecipe)
                
                ResolutionResult.Resolved(
                    recipe = remoteRecipe,
                    strategy = ResolutionStrategy.KEEP_REMOTE,
                    message = "Kept remote version"
                )
            } ?: run {
                ResolutionResult.Failure("No remote recipe to keep")
            }
        } catch (e: Exception) {
            ResolutionResult.Failure(
                errorMessage = "Failed to resolve conflict: ${e.message}",
                exception = e
            )
        }
    }

    /**
     * Resolve by merging changes
     */
    private suspend fun resolveMerge(conflict: ConflictInfo): ResolutionResult {
        return try {
            val localRecipe = conflict.localRecipe ?: return ResolutionResult.Failure("No local recipe")
            val remoteRecipe = conflict.remoteRecipe ?: return ResolutionResult.Failure("No remote recipe")
            
            // Merge the recipes (simple merge - in production, use more sophisticated merging)
            val mergedRecipe = mergeRecipes(localRecipe, remoteRecipe)
            
            // Save the merged recipe
            createOrUpdateRecipe(mergedRecipe)
            
            ResolutionResult.Resolved(
                recipe = mergedRecipe,
                strategy = ResolutionStrategy.MERGE,
                message = "Merged local and remote versions"
            )
        } catch (e: Exception) {
            ResolutionResult.Failure(
                errorMessage = "Failed to merge recipes: ${e.message}",
                exception = e
            )
        }
    }

    /**
     * Merge two recipes (simple implementation)
     */
    private fun mergeRecipes(local: Recipe, remote: Recipe): Recipe {
        // Prefer local changes for most fields
        // In production, use proper merge logic with conflict resolution
        
        return local.copy(
            // Keep the latest update time
            updatedAt = maxOf(local.updatedAt, remote.updatedAt),
            // Merge tags
            tags = (local.tags + remote.tags).distinct(),
            // For other fields, prefer local if modified more recently
            title = if (local.updatedAt >= remote.updatedAt) local.title else remote.title,
            description = if (local.updatedAt >= remote.updatedAt) local.description else remote.description,
            category = if (local.updatedAt >= remote.updatedAt) local.category else remote.category,
            servingSize = if (local.updatedAt >= remote.updatedAt) local.servingSize else remote.servingSize,
            prepTime = if (local.updatedAt >= remote.updatedAt) local.prepTime else remote.prepTime,
            cookTime = if (local.updatedAt >= remote.updatedAt) local.cookTime else remote.cookTime,
            rating = if (local.updatedAt >= remote.updatedAt) local.rating else remote.rating,
            isFavorite = if (local.updatedAt >= remote.updatedAt) local.isFavorite else remote.isFavorite,
            source = if (local.updatedAt >= remote.updatedAt) local.source else remote.source,
            // Merge ingredients
            ingredients = mergeIngredients(local.ingredients, remote.ingredients),
            // Merge instructions
            instructions = mergeInstructions(local.instructions, remote.instructions),
            // Merge version vectors
            versionVector = local.versionVector
        )
    }

    /**
     * Merge ingredients from two recipes
     */
    private fun mergeIngredients(
        localIngredients: List<com.ourcookbook.domain.model.Ingredient>,
        remoteIngredients: List<com.ourcookbook.domain.model.Ingredient>
    ): List<com.ourcookbook.domain.model.Ingredient> {
        // Simple merge: combine all ingredients and remove duplicates by name
        val allIngredients = localIngredients + remoteIngredients
        val seenNames = mutableSetOf<String>()
        
        return allIngredients.filter { ingredient ->
            seenNames.add(ingredient.name)
        }
    }

    /**
     * Merge instructions from two recipes
     */
    private fun mergeInstructions(
        localInstructions: List<String>,
        remoteInstructions: List<String>
    ): List<String> {
        // Simple merge: combine all instructions
        return localInstructions + remoteInstructions
    }

    /**
     * Resolve by keeping both versions
     */
    private suspend fun resolveKeepBoth(conflict: ConflictInfo): ResolutionResult {
        return try {
            val localRecipe = conflict.localRecipe ?: return ResolutionResult.Failure("No local recipe")
            val remoteRecipe = conflict.remoteRecipe ?: return ResolutionResult.Failure("No remote recipe")
            
            // Create a copy of the remote recipe with a new ID
            val copiedRecipe = remoteRecipe.copy(
                id = java.util.UUID.randomUUID().toString(),
                title = "${remoteRecipe.title} (Copy)"
            )
            
            // Save the copied recipe
            recipeRepository.createRecipe(copiedRecipe)
            
            // Mark local recipe as synced
            recipeRepository.markRecipeSynced(localRecipe.id)

            ResolutionResult.Resolved(
                recipe = localRecipe,
                strategy = ResolutionStrategy.KEEP_BOTH,
                message = "Kept both versions, created copy of remote"
            )
        } catch (e: Exception) {
            ResolutionResult.Failure(
                errorMessage = "Failed to keep both versions: ${e.message}",
                exception = e
            )
        }
    }

    /**
     * Resolve multiple conflicts with the same strategy
     *
     * @param conflicts List of conflicts to resolve
     * @param strategy The resolution strategy
     * @return List of ResolutionResult objects
     */
    suspend fun resolveConflicts(
        conflicts: List<ConflictInfo>,
        strategy: ResolutionStrategy
    ): List<ResolutionResult> {
        return conflicts.map { conflict ->
            resolveConflict(conflict, strategy)
        }
    }

    /**
     * Auto-resolve conflicts with smart strategy selection
     *
     * @param conflicts List of conflicts to resolve
     * @return List of ResolutionResult objects
     */
    suspend fun autoResolveConflicts(
        conflicts: List<ConflictInfo>
    ): List<ResolutionResult> {
        return conflicts.map { conflict ->
            val strategy = selectSmartStrategy(conflict)
            resolveConflict(conflict, strategy)
        }
    }

    /**
     * Select a smart strategy based on conflict type
     */
    private fun selectSmartStrategy(conflict: ConflictInfo): ResolutionStrategy {
        return when (conflict.conflictType) {
            ConflictType.DELETED_LOCAL -> ResolutionStrategy.KEEP_REMOTE
            ConflictType.DELETED_REMOTE -> ResolutionStrategy.KEEP_LOCAL
            ConflictType.VERSION_MISMATCH -> ResolutionStrategy.KEEP_REMOTE
            ConflictType.CHECKSUM_MISMATCH -> ResolutionStrategy.MERGE
            ConflictType.BOTH_MODIFIED -> ResolutionStrategy.MERGE
        }
    }

    /**
     * Get conflict statistics
     */
    fun getConflictStatistics(conflicts: List<ConflictInfo>): Map<String, Any> {
        val byType = conflicts.groupBy { it.conflictType }
        
        return mapOf<String, Any>(
            "total" to conflicts.size,
            "by_type" to byType.mapValues { it.value.size },
            "deleted_local" to (byType[ConflictType.DELETED_LOCAL]?.size ?: 0),
            "deleted_remote" to (byType[ConflictType.DELETED_REMOTE]?.size ?: 0),
            "version_mismatch" to (byType[ConflictType.VERSION_MISMATCH]?.size ?: 0),
            "checksum_mismatch" to (byType[ConflictType.CHECKSUM_MISMATCH]?.size ?: 0),
            "both_modified" to (byType[ConflictType.BOTH_MODIFIED]?.size ?: 0)
        )
    }

    /**
     * Get conflict severity
     */
    fun getConflictSeverity(conflict: ConflictInfo): ConflictSeverity {
        return when (conflict.conflictType) {
            ConflictType.DELETED_LOCAL -> ConflictSeverity.HIGH
            ConflictType.DELETED_REMOTE -> ConflictSeverity.HIGH
            ConflictType.BOTH_MODIFIED -> ConflictSeverity.HIGH
            ConflictType.CHECKSUM_MISMATCH -> ConflictSeverity.MEDIUM
            ConflictType.VERSION_MISMATCH -> ConflictSeverity.LOW
        }
    }

    /**
     * Conflict severity levels
     */
    enum class ConflictSeverity {
        LOW,
        MEDIUM,
        HIGH
    }

    /**
     * Get conflict resolution suggestions
     */
    fun getResolutionSuggestions(conflict: ConflictInfo): List<ResolutionStrategy> {
        return when (conflict.conflictType) {
            ConflictType.DELETED_LOCAL -> {
                listOf(
                    ResolutionStrategy.KEEP_REMOTE,
                    ResolutionStrategy.KEEP_LOCAL,
                    ResolutionStrategy.ASK_USER
                )
            }
            ConflictType.DELETED_REMOTE -> {
                listOf(
                    ResolutionStrategy.KEEP_LOCAL,
                    ResolutionStrategy.KEEP_REMOTE,
                    ResolutionStrategy.ASK_USER
                )
            }
            ConflictType.VERSION_MISMATCH -> {
                listOf(
                    ResolutionStrategy.KEEP_REMOTE,
                    ResolutionStrategy.KEEP_LOCAL,
                    ResolutionStrategy.ASK_USER
                )
            }
            ConflictType.CHECKSUM_MISMATCH -> {
                listOf(
                    ResolutionStrategy.MERGE,
                    ResolutionStrategy.KEEP_LOCAL,
                    ResolutionStrategy.KEEP_REMOTE,
                    ResolutionStrategy.ASK_USER
                )
            }
            ConflictType.BOTH_MODIFIED -> {
                listOf(
                    ResolutionStrategy.MERGE,
                    ResolutionStrategy.KEEP_LOCAL,
                    ResolutionStrategy.KEEP_REMOTE,
                    ResolutionStrategy.KEEP_BOTH,
                    ResolutionStrategy.ASK_USER
                )
            }
        }
    }

    /**
     * Get conflict description for UI
     */
    fun getConflictDescription(conflict: ConflictInfo): String {
        return when (conflict.conflictType) {
            ConflictType.DELETED_LOCAL -> {
                "Recipe was deleted locally but exists on Drive"
            }
            ConflictType.DELETED_REMOTE -> {
                "Recipe exists locally but was deleted on Drive"
            }
            ConflictType.VERSION_MISMATCH -> {
                "Local and remote versions differ"
            }
            ConflictType.CHECKSUM_MISMATCH -> {
                "Local and remote content differ"
            }
            ConflictType.BOTH_MODIFIED -> {
                "Both local and remote versions were modified"
            }
        }
    }

    /**
     * Get conflict title for UI
     */
    fun getConflictTitle(conflict: ConflictInfo): String {
        return when (conflict.conflictType) {
            ConflictType.DELETED_LOCAL -> "Deleted Locally"
            ConflictType.DELETED_REMOTE -> "Deleted on Drive"
            ConflictType.VERSION_MISMATCH -> "Version Conflict"
            ConflictType.CHECKSUM_MISMATCH -> "Content Conflict"
            ConflictType.BOTH_MODIFIED -> "Both Modified"
        }
    }
}
