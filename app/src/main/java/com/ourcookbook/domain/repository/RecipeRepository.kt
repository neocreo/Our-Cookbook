package com.ourcookbook.domain.repository

import com.ourcookbook.domain.model.Recipe
import com.ourcookbook.domain.model.ConflictResolution
import com.ourcookbook.domain.model.SyncConflict
import kotlinx.coroutines.flow.Flow
import java.time.Instant

/**
 * Repository interface for Recipe operations
 * Defines the contract for recipe data access in the domain layer
 */
interface RecipeRepository {
    
    // CRUD Operations
    suspend fun createRecipe(recipe: Recipe): String
    suspend fun updateRecipe(recipe: Recipe)
    suspend fun deleteRecipe(id: String)
    suspend fun getRecipeById(id: String): Recipe?
    suspend fun getRecipesByIds(ids: List<String>): List<Recipe>
    
    // Query Operations
    fun getAllRecipes(): Flow<List<Recipe>>
    suspend fun getAllRecipesOnce(): List<Recipe>
    fun getFavorites(): Flow<List<Recipe>>
    fun getRecipesByCategory(category: String): Flow<List<Recipe>>
    fun getRecipesByDevice(deviceId: String): Flow<List<Recipe>>
    fun searchRecipes(query: String): Flow<List<Recipe>>
    
    // Sync Operations
    suspend fun getUpdatedSince(since: Instant): List<Recipe>
    suspend fun getRecipesNeedingSync(): List<Recipe>
    suspend fun markRecipeSynced(recipeId: String)
    
    // Conflict Resolution
    suspend fun detectConflicts(localRecipes: List<Recipe>, remoteRecipes: List<Recipe>): List<SyncConflict>
    suspend fun resolveConflict(conflict: SyncConflict, resolution: ConflictResolution): Boolean
    
    // Checksum Operations
    suspend fun validateChecksum(recipeId: String): Boolean
    suspend fun updateChecksum(recipeId: String): Boolean
    
    // Utility Operations
    suspend fun getRecipeCount(): Int
    suspend fun getRecentRecipes(limit: Int): List<Recipe>
    suspend fun getTopRatedRecipes(limit: Int): List<Recipe>
    suspend fun getRecipeByChecksum(checksum: String): Recipe?

    suspend fun getRecipesByCookbookId(cookbookId: String): List<Recipe>
    suspend fun deleteRecipesByCookbookId(cookbookId: String): Boolean
}