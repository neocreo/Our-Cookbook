package com.ourcookbook.domain.usecase.recipe

import com.ourcookbook.domain.model.ConflictResolution
import com.ourcookbook.domain.model.Recipe
import com.ourcookbook.domain.model.SyncConflict
import com.ourcookbook.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import java.time.Instant

/**
 * Use cases for Recipe synchronization operations
 * These use cases encapsulate the business logic for sync-related operations
 */

// Get Updated Since Use Case
class GetUpdatedSince(
    private val repository: RecipeRepository
) {
    suspend operator fun invoke(since: Instant): Result<List<Recipe>> {
        return try {
            val recipes = repository.getUpdatedSince(since)
            Result.success(recipes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Get Recipes Needing Sync Use Case
class GetRecipesNeedingSync(
    private val repository: RecipeRepository
) {
    suspend operator fun invoke(): Result<List<Recipe>> {
        return try {
            val recipes = repository.getRecipesNeedingSync()
            Result.success(recipes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Mark Recipe Synced Use Case
class MarkRecipeSynced(
    private val repository: RecipeRepository
) {
    suspend operator fun invoke(recipeId: String): Result<Unit> {
        return try {
            repository.markRecipeSynced(recipeId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Validate Checksum Use Case
class ValidateChecksum(
    private val repository: RecipeRepository
) {
    suspend operator fun invoke(recipeId: String): Result<Boolean> {
        return try {
            val isValid = repository.validateChecksum(recipeId)
            Result.success(isValid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Update Checksum Use Case
class UpdateChecksum(
    private val repository: RecipeRepository
) {
    suspend operator fun invoke(recipeId: String): Result<Boolean> {
        return try {
            val updated = repository.updateChecksum(recipeId)
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Get Recipe By Checksum Use Case
class GetRecipeByChecksum(
    private val repository: RecipeRepository
) {
    suspend operator fun invoke(checksum: String): Result<Recipe?> {
        return try {
            val recipe = repository.getRecipeByChecksum(checksum)
            Result.success(recipe)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Detect Conflicts Use Case
class DetectConflicts(
    private val repository: RecipeRepository
) {
    suspend operator fun invoke(localRecipes: List<Recipe>, remoteRecipes: List<Recipe>): Result<List<SyncConflict>> {
        return try {
            val conflicts = repository.detectConflicts(localRecipes, remoteRecipes)
            Result.success(conflicts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Resolve Conflict Use Case
class ResolveConflict(
    private val repository: RecipeRepository
) {
    suspend operator fun invoke(conflict: SyncConflict, resolution: ConflictResolution): Result<Boolean> {
        return try {
            val resolved = repository.resolveConflict(conflict, resolution)
            Result.success(resolved)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Sync Recipe Use Case (comprehensive sync operation)
class SyncRecipe(
    private val repository: RecipeRepository,
    private val getRecipesNeedingSync: GetRecipesNeedingSync,
    private val markRecipeSynced: MarkRecipeSynced,
    private val detectConflicts: DetectConflicts,
    private val resolveConflict: ResolveConflict
) {
    suspend operator fun invoke(remoteRecipes: List<Recipe>): Result<SyncResult> {
        return try {
            // Get local recipes that need sync
            val localRecipes = getRecipesNeedingSync().getOrThrow()
            
            // Detect conflicts
            val conflicts = detectConflicts(localRecipes, remoteRecipes).getOrThrow()
            
            // For now, auto-resolve by keeping local (this would be user choice in UI)
            conflicts.forEach { conflict ->
                resolveConflict(conflict, ConflictResolution.KeepLocal).getOrThrow()
            }
            
            // Mark all synced
            localRecipes.forEach { recipe ->
                markRecipeSynced(recipe.id).getOrThrow()
            }
            
            Result.success(SyncResult(
                syncedRecipes = localRecipes.size + remoteRecipes.size,
                conflicts = conflicts.size,
                timestamp = Instant.now()
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Result of a sync operation
 */
data class SyncResult(
    val syncedRecipes: Int,
    val conflicts: Int,
    val timestamp: Instant
)
