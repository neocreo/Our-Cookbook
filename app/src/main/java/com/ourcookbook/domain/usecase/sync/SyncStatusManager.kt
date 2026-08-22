package com.ourcookbook.domain.usecase.sync

import com.ourcookbook.data.repository.DriveRepository
import com.ourcookbook.domain.model.Recipe
import com.ourcookbook.domain.model.SyncStatus
import com.ourcookbook.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/**
 * Use case for managing sync status indicators and notifications
 * Task 2.2.16: Sync status indicators and notifications
 *
 * Provides real-time sync status information and notifications
 */
class SyncStatusManager @Inject constructor(
    private val driveRepository: DriveRepository,
    private val recipeRepository: RecipeRepository,
    private val pushToDriveWithChecksum: PushToDriveWithChecksum
) {

    /**
     * Overall sync status
     */
    sealed class OverallSyncStatus {
        object Synced : OverallSyncStatus()
        object Syncing : OverallSyncStatus()
        object NotSynced : OverallSyncStatus()
        data class Error(val message: String) : OverallSyncStatus()
    }

    /**
     * Sync status for individual items
     */
    data class ItemSyncStatus(
        val recipeId: String,
        val status: SyncStatus,
        val lastSynced: Long?,
        val checksum: String?,
        val error: String?
    )

    /**
     * Sync notification
     */
    data class SyncNotification(
        val type: NotificationType,
        val message: String,
        val recipeId: String? = null,
        val timestamp: Long = System.currentTimeMillis()
    )

    /**
     * Notification types
     */
    enum class NotificationType {
        SUCCESS,
        WARNING,
        ERROR,
        INFO
    }

    /**
     * Get overall sync status as a Flow
     */
    fun getOverallSyncStatus(): Flow<OverallSyncStatus> {
        return combine(
            driveRepository.getAuthenticationStatus(),
            recipeRepository.getSyncStatusCount()
        ) { authStatus, syncCounts ->
            when {
                !authStatus.isAuthenticated -> OverallSyncStatus.NotSynced
                syncCounts.needingSync > 0 -> OverallSyncStatus.NotSynced
                syncCounts.syncing > 0 -> OverallSyncStatus.Syncing
                else -> OverallSyncStatus.Synced
            }
        }
    }

    /**
     * Get detailed sync status for all items
     */
    suspend fun getDetailedSyncStatus(): List<ItemSyncStatus> {
        val allRecipes = recipeRepository.getAllRecipesOnce()
        
        return allRecipes.map { recipe ->
            ItemSyncStatus(
                recipeId = recipe.id,
                status = recipe.syncStatus,
                lastSynced = recipe.lastSyncedAt,
                checksum = recipe.checksum,
                error = null
            )
        }
    }

    /**
     * Get sync status for specific recipes
     */
    suspend fun getSyncStatusForRecipes(
        recipeIds: List<String>
    ): List<ItemSyncStatus> {
        val recipes = recipeRepository.getRecipesByIds(recipeIds)
        
        return recipes.map { recipe ->
            ItemSyncStatus(
                recipeId = recipe.id,
                status = recipe.syncStatus,
                lastSynced = recipe.lastSyncedAt,
                checksum = recipe.checksum,
                error = null
            )
        }
    }

    /**
     * Get count of items needing sync
     */
    suspend fun getNeedingSyncCount(): Int {
        return recipeRepository.getRecipesNeedingSyncCount()
    }

    /**
     * Get count of items currently syncing
     */
    suspend fun getSyncingCount(): Int {
        return recipeRepository.getSyncingRecipesCount()
    }

    /**
     * Get count of items with errors
     */
    suspend fun getErrorCount(): Int {
        return recipeRepository.getSyncErrorRecipesCount()
    }

    /**
     * Get sync status summary
     */
    suspend fun getSyncStatusSummary(): SyncStatusSummary {
        val total = recipeRepository.getRecipeCount()
        val synced = recipeRepository.getSyncedRecipesCount()
        val needingSync = recipeRepository.getRecipesNeedingSyncCount()
        val syncing = recipeRepository.getSyncingRecipesCount()
        val errors = recipeRepository.getSyncErrorRecipesCount()
        
        val lastSyncTime = recipeRepository.getLastSyncTime()
        
        return SyncStatusSummary(
            total = total,
            synced = synced,
            needingSync = needingSync,
            syncing = syncing,
            errors = errors,
            lastSyncTime = lastSyncTime,
            isAuthenticated = driveRepository.isAuthenticated()
        )
    }

    /**
     * Sync status summary
     */
    data class SyncStatusSummary(
        val total: Int,
        val synced: Int,
        val needingSync: Int,
        val syncing: Int,
        val errors: Int,
        val lastSyncTime: Long?,
        val isAuthenticated: Boolean
    ) {
        val progress: Float get() = if (total > 0) synced.toFloat() / total else 0f
        val isFullySynced: Boolean get() = needingSync == 0 && syncing == 0 && errors == 0
    }

    /**
     * Start sync and return notifications as a Flow
     */
    fun startSyncAndNotify(): Flow<SyncNotification> {
        // This would be implemented with actual Flow in a real app
        // For now, return a simple flow
        return kotlinx.coroutines.flow.flow {
            emit(SyncNotification(
                type = NotificationType.INFO,
                message = "Starting sync..."
            ))
            
            // Simulate sync process
            val pending = recipeRepository.getRecipesNeedingSyncCount()
            
            if (pending > 0) {
                emit(SyncNotification(
                    type = NotificationType.INFO,
                    message = "Syncing $pending recipes..."
                ))
            }
            
            // Check for errors
            val errors = recipeRepository.getSyncErrorRecipesCount()
            if (errors > 0) {
                emit(SyncNotification(
                    type = NotificationType.WARNING,
                    message = "$errors recipes failed to sync"
                ))
            }
            
            emit(SyncNotification(
                type = NotificationType.SUCCESS,
                message = "Sync completed"
            ))
        }
    }

    /**
     * Create a notification for sync success
     */
    fun createSuccessNotification(
        recipeId: String,
        message: String
    ): SyncNotification {
        return SyncNotification(
            type = NotificationType.SUCCESS,
            message = message,
            recipeId = recipeId
        )
    }

    /**
     * Create a notification for sync warning
     */
    fun createWarningNotification(
        recipeId: String? = null,
        message: String
    ): SyncNotification {
        return SyncNotification(
            type = NotificationType.WARNING,
            message = message,
            recipeId = recipeId
        )
    }

    /**
     * Create a notification for sync error
     */
    fun createErrorNotification(
        recipeId: String? = null,
        message: String
    ): SyncNotification {
        return SyncNotification(
            type = NotificationType.ERROR,
            message = message,
            recipeId = recipeId
        )
    }

    /**
     * Create a notification for sync info
     */
    fun createInfoNotification(
        recipeId: String? = null,
        message: String
    ): SyncNotification {
        return SyncNotification(
            type = NotificationType.INFO,
            message = message,
            recipeId = recipeId
        )
    }

    /**
     * Check if sync is currently in progress
     */
    suspend fun isSyncInProgress(): Boolean {
        return recipeRepository.getSyncingRecipesCount() > 0
    }

    /**
     * Get the last sync time
     */
    suspend fun getLastSyncTime(): Long? {
        return recipeRepository.getLastSyncTime()
    }

    /**
     * Get time since last sync
     */
    suspend fun getTimeSinceLastSync(): Long? {
        val lastSyncTime = getLastSyncTime()
        return lastSyncTime?.let { System.currentTimeMillis() - it }
    }

    /**
     * Format time since last sync as human-readable string
     */
    suspend fun getTimeSinceLastSyncFormatted(): String {
        val milliseconds = getTimeSinceLastSync() ?: return "Never"
        
        return when {
            milliseconds < 60000 -> "Less than a minute ago"
            milliseconds < 3600000 -> "${milliseconds / 60000} minutes ago"
            milliseconds < 86400000 -> "${milliseconds / 3600000} hours ago"
            else -> "${milliseconds / 86400000} days ago"
        }
    }

    /**
     * Get sync status color for UI
     */
    fun getStatusColor(status: SyncStatus): String {
        return when (status) {
            SyncStatus.SYNCED -> "#4CAF50" // Green
            SyncStatus.NEEDS_SYNC -> "#FFC107" // Amber
            SyncStatus.SYNCING -> "#2196F3" // Blue
            SyncStatus.ERROR -> "#F44336" // Red
            SyncStatus.UNKNOWN -> "#9E9E9E" // Gray
        }
    }

    /**
     * Get sync status icon for UI
     */
    fun getStatusIcon(status: SyncStatus): String {
        return when (status) {
            SyncStatus.SYNCED -> "check_circle"
            SyncStatus.NEEDS_SYNC -> "sync"
            SyncStatus.SYNCING -> "sync"
            SyncStatus.ERROR -> "error"
            SyncStatus.UNKNOWN -> "help"
        }
    }

    /**
     * Get sync status message for UI
     */
    fun getStatusMessage(status: SyncStatus): String {
        return when (status) {
            SyncStatus.SYNCED -> "Synced"
            SyncStatus.NEEDS_SYNC -> "Needs sync"
            SyncStatus.SYNCING -> "Syncing..."
            SyncStatus.ERROR -> "Sync error"
            SyncStatus.UNKNOWN -> "Unknown"
        }
    }

    /**
     * Get overall sync status message
     */
    suspend fun getOverallStatusMessage(): String {
        val summary = getSyncStatusSummary()
        
        return when {
            summary.needingSync > 0 -> "${summary.needingSync} recipes need sync"
            summary.syncing > 0 -> "Syncing ${summary.syncing} recipes..."
            summary.errors > 0 -> "${summary.errors} sync errors"
            else -> "All recipes synced"
        }
    }

    /**
     * Check if authentication is required for sync
     */
    suspend fun isAuthenticationRequired(): Boolean {
        return !driveRepository.isAuthenticated()
    }

    /**
     * Get authentication status message
     */
    suspend fun getAuthenticationStatusMessage(): String {
        return if (driveRepository.isAuthenticated()) {
            "Connected to Google Drive"
        } else {
            "Not connected to Google Drive"
        }
    }

    /**
     * Create a sync progress object for UI
     */
    data class SyncProgress(
        val current: Int,
        val total: Int,
        val percentage: Float,
        val message: String
    )

    /**
     * Get sync progress for UI
     */
    suspend fun getSyncProgress(): SyncProgress {
        val summary = getSyncStatusSummary()
        val total = summary.total
        val synced = summary.synced
        
        val percentage = if (total > 0) synced.toFloat() / total else 0f
        
        val message = when {
            summary.needingSync > 0 -> "${summary.needingSync} recipes need sync"
            summary.syncing > 0 -> "Syncing..."
            else -> "All synced"
        }
        
        return SyncProgress(
            current = synced,
            total = total,
            percentage = percentage,
            message = message
        )
    }
}
