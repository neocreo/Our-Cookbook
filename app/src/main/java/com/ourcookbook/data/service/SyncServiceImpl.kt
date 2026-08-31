package com.ourcookbook.data.service

import com.ourcookbook.data.datasource.remote.IRecipeRemoteDataSource
import com.ourcookbook.domain.model.Recipe
import com.ourcookbook.domain.model.SyncConflict
import com.ourcookbook.domain.model.SyncLog
import com.ourcookbook.domain.model.SyncMetadata
import com.ourcookbook.domain.repository.RecipeRepository
import com.ourcookbook.domain.service.ChecksumService
import com.ourcookbook.domain.service.ConflictResolver
import com.ourcookbook.domain.service.SyncService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Duration
import java.time.Instant
import javax.inject.Inject

/**
 * Implementation of SyncService for managing synchronization between local and remote data
 * Uses RecipeRepository for local operations and IRecipeRemoteDataSource for remote operations
 */
class SyncServiceImpl @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val remoteDataSource: IRecipeRemoteDataSource,
    private val checksumService: ChecksumService
) : SyncService {
    
    private val conflictResolver: ConflictResolver = ConflictResolverImpl(checksumService)
    
    private val _syncStatus = MutableStateFlow<SyncService.SyncStatus>(SyncService.SyncStatus.Idle)
    override fun getSyncStatusFlow(): Flow<SyncService.SyncStatus> = _syncStatus.asStateFlow()
    
    override suspend fun syncAll(): SyncService.SyncResult {
        val startTime = Instant.now()
        var syncedItems = 0
        var conflicts = 0
        val errors = mutableListOf<String>()
        
        try {
            _syncStatus.value = SyncService.SyncStatus.CheckingForChanges
            
            // Get local and remote changes
            val localChanges = getLocalChanges()
            val remoteChanges = getRemoteChanges()
            
            _syncStatus.value = SyncService.SyncStatus.Syncing
            
            // Detect conflicts
            val detectedConflicts = detectConflicts()
            conflicts = detectedConflicts.size
            
            // For now, just push local changes and pull remote changes
            // In a real implementation, this would be more sophisticated
            
            if (localChanges.isNotEmpty()) {
                val pushResult = pushChanges(localChanges)
                syncedItems += pushResult.syncedItems
                errors.addAll(pushResult.errors)
            }
            
            if (remoteChanges.isNotEmpty()) {
                val pullResult = pullChanges()
                syncedItems += pullResult.syncedItems
                errors.addAll(pullResult.errors)
            }
            
            val endTime = Instant.now()
            val duration = Duration.between(startTime, endTime)
            
            _syncStatus.value = SyncService.SyncStatus.Success(syncedItems, conflicts)
            
            return SyncService.SyncResult(
                success = errors.isEmpty(),
                syncedItems = syncedItems,
                conflicts = conflicts,
                errors = errors,
                duration = duration,
                timestamp = endTime
            )
            
        } catch (e: Exception) {
            _syncStatus.value = SyncService.SyncStatus.Error(e.message ?: "Unknown error")
            return SyncService.SyncResult(
                success = false,
                syncedItems = syncedItems,
                conflicts = conflicts,
                errors = listOf(e.message ?: "Unknown error"),
                duration = Duration.between(startTime, Instant.now()),
                timestamp = Instant.now()
            )
        }
    }
    
    override suspend fun pushChanges(recipes: List<Recipe>): SyncService.SyncResult {
        val startTime = Instant.now()
        var syncedItems = 0
        val errors = mutableListOf<String>()
        
        try {
            // Update checksums for all recipes before pushing
            val recipesWithChecksums = recipes.map { recipe ->
                checksumService.updateRecipeChecksum(recipe)
            }
            
            // In a real implementation, this would call the remote data source
            // For now, we'll simulate the operation
            syncedItems = recipesWithChecksums.size
            
            return SyncService.SyncResult(
                success = true,
                syncedItems = syncedItems,
                conflicts = 0,
                errors = errors,
                duration = Duration.between(startTime, Instant.now()),
                timestamp = Instant.now()
            )
            
        } catch (e: Exception) {
            errors.add(e.message ?: "Unknown error during push")
            return SyncService.SyncResult(
                success = false,
                syncedItems = syncedItems,
                conflicts = 0,
                errors = errors,
                duration = Duration.between(startTime, Instant.now()),
                timestamp = Instant.now()
            )
        }
    }
    
    override suspend fun pullChanges(): SyncService.SyncResult {
        val startTime = Instant.now()
        var syncedItems = 0
        val errors = mutableListOf<String>()
        
        try {
            // In a real implementation, this would call the remote data source
            // to get changes and apply them locally
            // For now, we'll simulate the operation
            
            // Get remote recipes (simulated)
            val remoteRecipes = try {
                remoteDataSource.getAllRecipes()
            } catch (e: Exception) {
                errors.add("Failed to get remote recipes: ${e.message}")
                return SyncService.SyncResult(
                    success = false,
                    syncedItems = 0,
                    conflicts = 0,
                    errors = errors,
                    duration = Duration.between(startTime, Instant.now()),
                    timestamp = Instant.now()
                )
            }
            
             // Check for conflicts and apply changes
             val localRecipes = try {
                 recipeRepository.getAllRecipesOnce()
             } catch (e: Exception) {
                 errors.add("Failed to get local recipes: ${e.message}")
                 return SyncService.SyncResult(
                     success = false,
                     syncedItems = 0,
                     conflicts = 0,
                     errors = errors,
                     duration = Duration.between(startTime, Instant.now()),
                     timestamp = Instant.now()
                 )
             }
            
            // Simple sync: just count the remote recipes as synced
            // In a real implementation, this would be more sophisticated
            syncedItems = remoteRecipes.size
            
            return SyncService.SyncResult(
                success = true,
                syncedItems = syncedItems,
                conflicts = 0,
                errors = errors,
                duration = Duration.between(startTime, Instant.now()),
                timestamp = Instant.now()
            )
            
        } catch (e: Exception) {
            errors.add(e.message ?: "Unknown error during pull")
            return SyncService.SyncResult(
                success = false,
                syncedItems = syncedItems,
                conflicts = 0,
                errors = errors,
                duration = Duration.between(startTime, Instant.now()),
                timestamp = Instant.now()
            )
        }
    }
    
    override suspend fun getLocalChanges(): List<Recipe> {
        // Get recipes that have been updated since last sync
        val lastSync = getSyncMetadata().lastSyncTimestamp
        return if (lastSync != null) {
            recipeRepository.getUpdatedSince(lastSync)
        } else {
            recipeRepository.getAllRecipesOnce()
        }
    }
    
    override suspend fun getRemoteChanges(): List<Recipe> {
        // Get all recipes from remote and filter based on last sync
        return remoteDataSource.getAllRecipes()
    }
    
    override suspend fun detectConflicts(): List<SyncConflict> {
        val conflicts = mutableListOf<SyncConflict>()
        
        try {
            val localRecipes = getLocalChanges()
            val remoteRecipes = getRemoteChanges()
            
            // Compare each local recipe with each remote recipe
            for (localRecipe in localRecipes) {
                for (remoteRecipe in remoteRecipes) {
                    // Skip if same ID (already synced)
                    if (localRecipe.id == remoteRecipe.id) {
                        continue
                    }
                    
                    // Check if they have the same content (duplicate detection)
                    val localChecksum = checksumService.calculateChecksum(localRecipe)
                    val remoteChecksum = checksumService.calculateChecksum(remoteRecipe)
                    
                    if (localChecksum == remoteChecksum) {
                        // Same content, different IDs - this is a duplicate
                        // For now, we'll treat this as a conflict
                        val conflict = SyncConflict(
                            id = java.util.UUID.randomUUID().toString(),
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
                        conflicts.add(conflict)
                    }
                }
            }
            
        } catch (e: Exception) {
            // Log error but don't fail
            println("Error detecting conflicts: ${e.message}")
        }
        
        return conflicts
    }
    
    override suspend fun resolveConflict(
        conflict: SyncConflict, 
        resolution: com.ourcookbook.domain.model.ConflictResolution
    ): Boolean {
        return try {
            val result = conflictResolver.resolveConflict(conflict, resolution)
            // Apply the resolution
            when (result.action) {
                com.ourcookbook.domain.model.ResolutionAction.OVERWRITE_REMOTE -> {
                    // Push the local version to remote
                    remoteDataSource.pushRecipes(listOf(result.resolvedRecipe))
                }
                com.ourcookbook.domain.model.ResolutionAction.UPDATE_LOCAL -> {
                    // Update local with remote version
                    recipeRepository.updateRecipe(result.resolvedRecipe)
                }
                com.ourcookbook.domain.model.ResolutionAction.MERGE -> {
                    // Update both local and remote with merged version
                    recipeRepository.updateRecipe(result.resolvedRecipe)
                    remoteDataSource.pushRecipes(listOf(result.resolvedRecipe))
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }
    
    override fun getSyncStatus(): SyncService.SyncStatus {
        return _syncStatus.value
    }
    
    override suspend fun getSyncLogs(): List<SyncLog> {
        // In a real implementation, this would fetch from the sync log repository
        return emptyList()
    }
    
    override suspend fun getSyncMetadata(): SyncMetadata {
        // In a real implementation, this would fetch from the sync metadata repository
        // For now, return default metadata
        return SyncMetadata(
            id = "default",
            deviceId = "default-device",
            lastSyncTimestamp = null,
            lastSuccessfulSync = null,
            syncInProgress = false,
            pendingChanges = 0,
            conflictCount = 0
        )
    }
    
    override suspend fun updateSyncMetadata(metadata: SyncMetadata) {
        // In a real implementation, this would update the sync metadata repository
        // For now, do nothing as we don't have the repository injected
    }
}
