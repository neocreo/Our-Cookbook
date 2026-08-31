package com.ourcookbook.data.repository

import com.ourcookbook.data.datasource.local.IRecipeLocalDataSource
import com.ourcookbook.data.datasource.remote.IRecipeRemoteDataSource
import com.ourcookbook.domain.model.Recipe
import com.ourcookbook.domain.model.ConflictResolution
import com.ourcookbook.domain.model.SyncConflict
import com.ourcookbook.domain.repository.RecipeRepository
import com.ourcookbook.domain.service.ChecksumService
import com.ourcookbook.domain.service.ConflictResolver
import com.ourcookbook.domain.service.SyncService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject

/**
 * Repository implementation for Recipe operations
 * Implements the RecipeRepository interface with local-first, sync-aware logic
 */
class RecipeRepositoryImpl @Inject constructor(
    private val localDataSource: IRecipeLocalDataSource,
    private val remoteDataSource: IRecipeRemoteDataSource,
    private val checksumService: ChecksumService,
    private val conflictResolver: ConflictResolver,
    private val syncService: SyncService
) : RecipeRepository {
    
    override suspend fun createRecipe(recipe: Recipe): String {
        // Validate recipe
        if (!recipe.isValid()) {
            throw IllegalArgumentException("Recipe is not valid")
        }
        
        // Ensure recipe has proper checksum
        val recipeWithChecksum = checksumService.updateRecipeChecksum(recipe)
        
        // Convert to entity and insert locally
        val entity = localDataSource.toEntity(recipeWithChecksum)
        val entityId = localDataSource.insert(entity)
        
        // Queue for sync
        syncService.pushChanges(listOf(recipeWithChecksum))
        
        return recipeWithChecksum.id
    }
    
    override suspend fun updateRecipe(recipe: Recipe) {
        // Validate recipe
        if (!recipe.isValid()) {
            throw IllegalArgumentException("Recipe is not valid")
        }
        
        // Ensure recipe has proper checksum
        val recipeWithChecksum = checksumService.updateRecipeChecksum(recipe)
        
        // Convert to entity and update locally
        val entity = localDataSource.toEntity(recipeWithChecksum)
        localDataSource.update(entity)
        
        // Queue for sync
        syncService.pushChanges(listOf(recipeWithChecksum))
    }
    
    override suspend fun deleteRecipe(id: String) {
        // Delete locally
        localDataSource.delete(id)
        
        // Queue deletion for sync
        val recipe = getRecipeById(id)
        recipe?.let {
            // Create tombstone for sync
            syncService.pushChanges(listOf(it))
        }
    }
    
    override suspend fun getRecipeById(id: String): Recipe? {
        return localDataSource.getById(id)?.let { entity ->
            localDataSource.toDomainModel(entity)
        }
    }
    
    override suspend fun getRecipesByIds(ids: List<String>): List<Recipe> {
        return localDataSource.getByIds(ids).map { entity ->
            localDataSource.toDomainModel(entity)
        }
    }
    
    override fun getAllRecipes(): Flow<List<Recipe>> {
        return localDataSource.getAll().map { entities ->
            entities.map { entity -> localDataSource.toDomainModel(entity) }
        }
    }
    
    override suspend fun getAllRecipesOnce(): List<Recipe> {
        return localDataSource.getAllOnce().map { entity ->
            localDataSource.toDomainModel(entity)
        }
    }
    
    override fun getFavorites(): Flow<List<Recipe>> {
        return localDataSource.getFavorites().map { entities ->
            entities.map { entity -> localDataSource.toDomainModel(entity) }
        }
    }
    
    override fun getRecipesByCategory(category: String): Flow<List<Recipe>> {
        return localDataSource.getByCategory(category).map { entities ->
            entities.map { entity -> localDataSource.toDomainModel(entity) }
        }
    }
    
    override fun getRecipesByDevice(deviceId: String): Flow<List<Recipe>> {
        return localDataSource.getByDevice(deviceId).map { entities ->
            entities.map { entity -> localDataSource.toDomainModel(entity) }
        }
    }
    
    override fun searchRecipes(query: String): Flow<List<Recipe>> {
        return localDataSource.search(query).map { entities ->
            entities.map { entity -> localDataSource.toDomainModel(entity) }
        }
    }
    
    override suspend fun getUpdatedSince(since: Instant): List<Recipe> {
        return localDataSource.getUpdatedSince(since).map { entity ->
            localDataSource.toDomainModel(entity)
        }
    }
    
    override suspend fun getRecipesNeedingSync(): List<Recipe> {
        // Get recipes that have been updated since last sync
        val lastSync = syncService.getSyncMetadata().lastSyncTimestamp
        return if (lastSync != null) {
            getUpdatedSince(lastSync)
        } else {
            getAllRecipesOnce()
        }
    }
    
    override suspend fun markRecipeSynced(recipeId: String) {
        // Update sync metadata or pending sync status
        // This would be handled by the sync service
    }
    
    override suspend fun detectConflicts(localRecipes: List<Recipe>, remoteRecipes: List<Recipe>): List<SyncConflict> {
        val conflicts = mutableListOf<SyncConflict>()
        
        for (localRecipe in localRecipes) {
            for (remoteRecipe in remoteRecipes) {
                val conflict = conflictResolver.detectConflict(localRecipe, remoteRecipe)
                conflict?.let { conflicts.add(it) }
            }
        }
        
        return conflicts
    }
    
    override suspend fun resolveConflict(conflict: SyncConflict, resolution: ConflictResolution): Boolean {
        return syncService.resolveConflict(conflict, resolution)
    }
    
    override suspend fun validateChecksum(recipeId: String): Boolean {
        return localDataSource.getById(recipeId)?.let { entity ->
            localDataSource.validateChecksum(entity)
        } ?: false
    }
    
    override suspend fun updateChecksum(recipeId: String): Boolean {
        return localDataSource.getById(recipeId)?.let { entity ->
            val updatedEntity = localDataSource.updateChecksum(entity)
            localDataSource.update(updatedEntity)
            true
        } ?: false
    }
    
    override suspend fun getRecipeCount(): Int {
        return localDataSource.count()
    }
    
    override suspend fun getRecentRecipes(limit: Int): List<Recipe> {
        return localDataSource.getRecent(limit).map { entity ->
            localDataSource.toDomainModel(entity)
        }
    }
    
    override suspend fun getTopRatedRecipes(limit: Int): List<Recipe> {
        return localDataSource.getTopRated(limit).map { entity ->
            localDataSource.toDomainModel(entity)
        }
    }
    
    override suspend fun getRecipeByChecksum(checksum: String): Recipe? {
        return localDataSource.getByChecksum(checksum)?.let { entity ->
            localDataSource.toDomainModel(entity)
        }
    }

    override suspend fun getRecipesByCookbookId(cookbookId: String): List<Recipe> {
        return localDataSource.getAllOnce().map { entity -> localDataSource.toDomainModel(entity) }
            .filter { it.deviceId == cookbookId }
    }

    override suspend fun deleteRecipesByCookbookId(cookbookId: String): Boolean {
        // Delete recipes associated with the cookbook
        return try {
            getRecipesByCookbookId(cookbookId).forEach { recipe ->
                localDataSource.delete(recipe.id)
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}

