package com.ourcookbook.domain.usecase.sync

import com.ourcookbook.data.repository.DriveRepository
import com.ourcookbook.domain.model.Recipe
import com.ourcookbook.domain.repository.RecipeRepository
import javax.inject.Inject

/**
 * Use case for incremental sync with startPageToken
 * Task 2.2.20: Incremental sync with startPageToken
 *
 * Implements efficient incremental sync using Google Drive API's startPageToken
 */
class IncrementalSyncManager @Inject constructor(
    private val driveRepository: DriveRepository,
    private val recipeRepository: RecipeRepository,
    private val pushToDriveWithChecksum: PushToDriveWithChecksum,
    private val batchedDriveOperations: BatchedDriveOperations,
    private val tombstoneProcessor: TombstoneProcessor
) {

    /**
     * Result of incremental sync operation
     */
    sealed class IncrementalSyncResult {
        data class Success(
            val syncedRecipes: List<Recipe>,
            val deletedRecipes: List<String>,
            val nextPageToken: String?,
            val hasMore: Boolean,
            val syncToken: String
        ) : IncrementalSyncResult()
        
        data class NoChanges(
            val nextPageToken: String?
        ) : IncrementalSyncResult()
        
        data class Failure(
            val errorMessage: String,
            val exception: Exception? = null
        ) : IncrementalSyncResult()
    }

    /**
     * Incremental sync settings
     */
    data class IncrementalSyncSettings(
        val pageSize: Int = 100,           // Number of items per page
        val maxPages: Int = 10,            // Maximum number of pages to sync
        val includeDeleted: Boolean = true, // Include tombstone processing
        val verifyChecksums: Boolean = true  // Verify checksums during sync
    )

    /**
     * Perform incremental sync
     *
     * @param startPageToken Token for the starting page (null for first sync)
     * @param settings Sync settings
     * @return IncrementalSyncResult with operation information
     */
    suspend operator fun invoke(
        startPageToken: String? = null,
        settings: IncrementalSyncSettings = IncrementalSyncSettings()
    ): IncrementalSyncResult {
        return try {
            // Check authentication
            if (!driveRepository.isAuthenticated()) {
                return IncrementalSyncResult.Failure("Not authenticated with Google Drive")
            }
            
            // Get the next page of changes from Drive
            val pageResult = driveRepository.getChangesPage(
                startPageToken = startPageToken,
                pageSize = settings.pageSize
            )
            
            if (pageResult.recipes.isEmpty() && pageResult.deletedRecipeIds.isEmpty()) {
                return IncrementalSyncResult.NoChanges(
                    nextPageToken = pageResult.nextPageToken
                )
            }
            
            // Process the page
            val result = processChangesPage(
                pageResult = pageResult,
                settings = settings
            )
            
            // Check if there are more pages and we haven't exceeded max
            if (pageResult.hasMore && settings.maxPages > 1) {
                // Continue with next page
                val nextResult = this(
                    startPageToken = pageResult.nextPageToken,
                    settings = settings.copy(maxPages = settings.maxPages - 1)
                )
                
                if (nextResult is IncrementalSyncResult.Success) {
                    return IncrementalSyncResult.Success(
                        syncedRecipes = result.syncedRecipes + nextResult.syncedRecipes,
                        deletedRecipes = result.deletedRecipes + nextResult.deletedRecipes,
                        nextPageToken = nextResult.nextPageToken,
                        hasMore = nextResult.hasMore,
                        syncToken = nextResult.syncToken
                    )
                }
            }
            
            // Return the result
            IncrementalSyncResult.Success(
                syncedRecipes = result.syncedRecipes,
                deletedRecipes = result.deletedRecipes,
                nextPageToken = pageResult.nextPageToken,
                hasMore = pageResult.hasMore,
                syncToken = generateSyncToken()
            )
            
        } catch (e: Exception) {
            IncrementalSyncResult.Failure(
                errorMessage = "Failed to perform incremental sync: ${e.message}",
                exception = e
            )
        }
    }

    /**
     * Process a single page of changes
     */
    private suspend fun processChangesPage(
        pageResult: DriveRepository.ChangesPageResult,
        settings: IncrementalSyncSettings
    ): IncrementalSyncResult.Success {
        val syncedRecipes = mutableListOf<Recipe>()
        val deletedRecipes = mutableListOf<String>()
        
        // Process new/changed recipes
        pageResult.recipes.forEach { remoteRecipe ->
            try {
                val localRecipe = recipeRepository.getRecipeById(remoteRecipe.id)
                
                if (localRecipe == null) {
                    // New recipe, pull it
                    recipeRepository.createRecipe(remoteRecipe)
                    syncedRecipes.add(remoteRecipe)
                } else {
                    // Existing recipe, check if it needs update
                    if (settings.verifyChecksums) {
                        val localChecksum = pushToDriveWithChecksum.calculateChecksum(localRecipe)
                        val remoteChecksum = remoteRecipe.checksum
                        
                        if (localChecksum != remoteChecksum) {
                            // Remote has changes, update local
                            recipeRepository.updateRecipe(remoteRecipe)
                            syncedRecipes.add(remoteRecipe)
                        }
                    } else {
                        // Skip checksum verification, just update if version is newer
                        if (remoteRecipe.version > localRecipe.version) {
                            recipeRepository.updateRecipe(remoteRecipe)
                            syncedRecipes.add(remoteRecipe)
                        }
                    }
                }
                
                // Update sync status
                recipeRepository.updateRecipeSyncStatus(
                    remoteRecipe.id,
                    com.ourcookbook.domain.model.SyncStatus.SYNCED,
                    System.currentTimeMillis(),
                    remoteRecipe.checksum ?: ""
                )
                
            } catch (e: Exception) {
                // Continue with other recipes
            }
        }
        
        // Process deleted recipes (tombstones)
        if (settings.includeDeleted) {
            val tombstoneResults = tombstoneProcessor.processTombstonesDuringPull(
                pageResult.deletedRecipeIds,
                TombstoneProcessor.TombstoneAction.DELETE_LOCAL
            )
            
            tombstoneResults.filterIsInstance<TombstoneProcessor.TombstoneResult.Processed>().forEach {
                deletedRecipes.add(it.recipeId)
            }
        }
        
        return IncrementalSyncResult.Success(
            syncedRecipes = syncedRecipes,
            deletedRecipes = deletedRecipes,
            nextPageToken = pageResult.nextPageToken,
            hasMore = pageResult.hasMore,
            syncToken = generateSyncToken()
        )
    }

    /**
     * Perform full incremental sync (all pages)
     *
     * @param settings Sync settings
     * @return IncrementalSyncResult with operation information
     */
    suspend fun performFullIncrementalSync(
        settings: IncrementalSyncSettings = IncrementalSyncSettings()
    ): IncrementalSyncResult {
        var allSyncedRecipes = emptyList<Recipe>()
        var allDeletedRecipes = emptyList<String>()
        var currentPageToken: String? = null
        var hasMore = true
        var pagesProcessed = 0
        
        while (hasMore && pagesProcessed < settings.maxPages) {
            val result = this(currentPageToken, settings.copy(maxPages = 1))
            
            when (result) {
                is IncrementalSyncResult.Success -> {
                    allSyncedRecipes += result.syncedRecipes
                    allDeletedRecipes += result.deletedRecipes
                    currentPageToken = result.nextPageToken
                    hasMore = result.hasMore
                    pagesProcessed++
                }
                is IncrementalSyncResult.NoChanges -> {
                    hasMore = false
                }
                is IncrementalSyncResult.Failure -> {
                    return result
                }
            }
        }
        
        return IncrementalSyncResult.Success(
            syncedRecipes = allSyncedRecipes,
            deletedRecipes = allDeletedRecipes,
            nextPageToken = currentPageToken,
            hasMore = hasMore,
            syncToken = generateSyncToken()
        )
    }

    /**
     * Get the current sync token
     *
     * @return Current sync token
     */
    suspend fun getCurrentSyncToken(): String {
        return driveRepository.getSyncToken() ?: generateSyncToken()
    }

    /**
     * Generate a new sync token
     */
    private fun generateSyncToken(): String {
        return "sync_${System.currentTimeMillis()}_${java.util.UUID.randomUUID()}"
    }

    /**
     * Save the sync token
     *
     * @param syncToken Token to save
     */
    suspend fun saveSyncToken(syncToken: String) {
        driveRepository.saveSyncToken(syncToken)
    }

    /**
     * Get the start page token for the next sync
     *
     * @return Start page token
     */
    suspend fun getStartPageToken(): String? {
        return driveRepository.getStartPageToken()
    }

    /**
     * Save the start page token
     *
     * @param startPageToken Token to save
     */
    suspend fun saveStartPageToken(startPageToken: String) {
        driveRepository.saveStartPageToken(startPageToken)
    }

    /**
     * Check if incremental sync is available
     */
    suspend fun isIncrementalSyncAvailable(): Boolean {
        return driveRepository.isAuthenticated() && 
               driveRepository.supportsIncrementalSync()
    }

    /**
     * Get incremental sync status
     */
    suspend fun getIncrementalSyncStatus(): IncrementalSyncStatus {
        val lastSyncToken = getCurrentSyncToken()
        val startPageToken = getStartPageToken()
        val hasPendingChanges = recipeRepository.getRecipesNeedingSyncCount() > 0
        
        return IncrementalSyncStatus(
            lastSyncToken = lastSyncToken,
            startPageToken = startPageToken,
            hasPendingChanges = hasPendingChanges,
            isAvailable = isIncrementalSyncAvailable()
        )
    }

    /**
     * Incremental sync status
     */
    data class IncrementalSyncStatus(
        val lastSyncToken: String,
        val startPageToken: String?,
        val hasPendingChanges: Boolean,
        val isAvailable: Boolean
    )

    /**
     * Get changes since last sync
     *
     * @param lastSyncToken Token from last sync
     * @param settings Sync settings
     * @return IncrementalSyncResult with operation information
     */
    suspend fun getChangesSinceLastSync(
        lastSyncToken: String,
        settings: IncrementalSyncSettings = IncrementalSyncSettings()
    ): IncrementalSyncResult {
        // Get the start page token for the last sync
        val startPageToken = driveRepository.getStartPageTokenForSyncToken(lastSyncToken)
        
        if (startPageToken == null) {
            // Full sync needed
            return performFullIncrementalSync(settings)
        }
        
        // Incremental sync from the start page token
        return this(startPageToken, settings)
    }

    /**
     * Initialize incremental sync
     *
     * @return IncrementalSyncResult with initial sync information
     */
    suspend fun initializeIncrementalSync(): IncrementalSyncResult {
        // Get the initial start page token
        val startPageToken = driveRepository.getInitialStartPageToken()
        
        // Perform the first sync
        return this(startPageToken)
    }

    /**
     * Reset incremental sync
     *
     * @return true if reset successfully
     */
    suspend fun resetIncrementalSync(): Boolean {
        return try {
            driveRepository.resetSyncToken()
            driveRepository.resetStartPageToken()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get incremental sync statistics
     *
     * @return Map of statistics
     */
    suspend fun getIncrementalSyncStatistics(): Map<String, Any> {
        val status = getIncrementalSyncStatus()
        val history = driveRepository.getSyncHistory()
        
        return mapOf(
            "is_available" to status.isAvailable,
            "has_pending_changes" to status.hasPendingChanges,
            "last_sync_token" to status.lastSyncToken,
            "has_start_page_token" to (status.startPageToken != null),
            "sync_history_count" to history.size
        )
    }

    /**
     * Check if there are changes to sync
     *
     * @return true if there are changes
     */
    suspend fun hasChangesToSync(): Boolean {
        return driveRepository.hasChanges() || 
               recipeRepository.getRecipesNeedingSyncCount() > 0
    }

    /**
     * Get the number of changes to sync
     *
     * @return Number of changes
     */
    suspend fun getChangesCount(): Int {
        return driveRepository.getChangesCount()
    }

    /**
     * Perform incremental sync with batched operations
     *
     * @param startPageToken Token for the starting page
     * @param batchSettings Batch settings
     * @param syncSettings Sync settings
     * @return IncrementalSyncResult with operation information
     */
    suspend fun performBatchedIncrementalSync(
        startPageToken: String? = null,
        batchSettings: BatchedDriveOperations.BatchSettings = BatchedDriveOperations.BatchSettings(),
        syncSettings: IncrementalSyncSettings = IncrementalSyncSettings()
    ): IncrementalSyncResult {
        // First, get the changes using incremental sync
        val syncResult = this(startPageToken, syncSettings)
        
        if (syncResult is IncrementalSyncResult.Failure) {
            return syncResult
        }
        
        // Then, push any local changes using batched operations
        val localChanges = recipeRepository.getRecipesNeedingSync()
        
        if (localChanges.isNotEmpty()) {
            val pushResult = batchedDriveOperations(
                recipeIds = localChanges.map { it.id },
                settings = batchSettings
            )
            
            if (pushResult is BatchedDriveOperations.BatchResult.Failure) {
                return IncrementalSyncResult.Failure(
                    errorMessage = "Push failed: ${pushResult.errorMessage}",
                    exception = pushResult.exception
                )
            }
        }
        
        return when (syncResult) {
            is IncrementalSyncResult.Success -> syncResult
            is IncrementalSyncResult.NoChanges -> syncResult
            else -> syncResult
        }
    }
}
