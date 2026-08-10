package com.ourcookbook.domain.service

import com.ourcookbook.domain.model.Recipe
import com.ourcookbook.domain.model.SyncConflict
import com.ourcookbook.domain.model.SyncLog
import com.ourcookbook.domain.model.SyncMetadata
import kotlinx.coroutines.flow.Flow

/**
 * Service interface for synchronization operations
 * Provides functionality for syncing data between local storage and remote services
 */
interface SyncService {
    
    /**
     * Perform a full synchronization between local and remote data
     * 
     * @return SyncResult containing information about the sync operation
     */
    suspend fun syncAll(): SyncResult
    
    /**
     * Push local changes to remote storage
     * 
     * @param recipes The recipes to push
     * @return SyncResult with details about the push operation
     */
    suspend fun pushChanges(recipes: List<Recipe>): SyncResult
    
    /**
     * Pull changes from remote storage
     * 
     * @return SyncResult with details about the pull operation
     */
    suspend fun pullChanges(): SyncResult
    
    /**
     * Check for local changes that need to be synced
     * 
     * @return List of recipes that have local changes
     */
    suspend fun getLocalChanges(): List<Recipe>
    
    /**
     * Check for remote changes that need to be synced
     * 
     * @return List of recipes that have remote changes
     */
    suspend fun getRemoteChanges(): List<Recipe>
    
    /**
     * Detect conflicts between local and remote data
     * 
     * @return List of detected sync conflicts
     */
    suspend fun detectConflicts(): List<SyncConflict>
    
    /**
     * Resolve a specific conflict
     * 
     * @param conflict The conflict to resolve
     * @param resolution The resolution strategy to use
     * @return true if conflict was resolved successfully
     */
    suspend fun resolveConflict(conflict: SyncConflict, resolution: com.ourcookbook.domain.model.ConflictResolution): Boolean
    
    /**
     * Get current sync status
     * 
     * @return Current sync status information
     */
    fun getSyncStatus(): SyncStatus
    
    /**
     * Get sync status as a flow for real-time updates
     */
    fun getSyncStatusFlow(): Flow<SyncStatus>
    
    /**
     * Get sync logs
     * 
     * @return List of sync logs
     */
    suspend fun getSyncLogs(): List<SyncLog>
    
    /**
     * Get sync metadata
     * 
     * @return Current sync metadata
     */
    suspend fun getSyncMetadata(): SyncMetadata
    
    /**
     * Update sync metadata
     * 
     * @param metadata The metadata to update
     */
    suspend fun updateSyncMetadata(metadata: SyncMetadata)
    
    /**
     * Result of a sync operation
     */
    data class SyncResult(
        val success: Boolean,
        val syncedItems: Int = 0,
        val conflicts: Int = 0,
        val errors: List<String> = emptyList(),
        val duration: java.time.Duration = java.time.Duration.ZERO,
        val timestamp: java.time.Instant = java.time.Instant.now()
    )
    
    /**
     * Current sync status
     */
    sealed class SyncStatus {
        object Idle : SyncStatus()
        object CheckingForChanges : SyncStatus()
        object Syncing : SyncStatus()
        data class Error(val message: String) : SyncStatus()
        data class Success(val syncedItems: Int, val conflicts: Int) : SyncStatus()
    }
}
