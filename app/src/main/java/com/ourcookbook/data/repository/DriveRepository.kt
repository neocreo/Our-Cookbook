package com.ourcookbook.data.repository

import com.ourcookbook.domain.model.AdvisoryLock
import com.ourcookbook.domain.model.Recipe
import kotlinx.coroutines.flow.Flow

/**
 * Repository for interacting with Google Drive for recipe sync.
 *
 * Consumed by the sync use cases (IncrementalSyncManager, PushToDriveWithChecksum,
 * SyncStatusManager, TombstoneProcessor, AdvisoryLockManager, BatchedDriveOperations).
 *
 * This interface defines the contract; the concrete implementation is provided
 * by the data layer.
 */
interface DriveRepository {

    /** Result of a single page of Drive changes. */
    data class ChangesPageResult(
        val recipes: List<Recipe>,
        val deletedRecipeIds: List<String>,
        val nextPageToken: String?,
        val hasMore: Boolean = false
    )

    /** Authentication/connection status emitted as a Flow. */
    data class AuthStatus(
        val isAuthenticated: Boolean = false,
        val accountName: String? = null
    )

    /** A tombstone entry retrieved from Drive. */
    data class RemoteTombstone(
        val recipeId: String,
        val deletedAt: Long,
        val deletedBy: String,
        val version: Int,
        val checksum: String
    )

    /** A sync history entry retrieved from Drive. */
    data class RemoteSyncHistoryEntry(
        val operationId: String,
        val timestamp: Long,
        val type: String,
        val status: String,
        val itemCount: Int
    )

    // ---- Authentication & connection ----
    fun getAuthenticationStatus(): Flow<AuthStatus>
    suspend fun isAuthenticated(): Boolean
    suspend fun isConnected(): Boolean
    suspend fun supportsIncrementalSync(): Boolean

    // ---- Sync tokens / page tokens ----
    suspend fun getSyncToken(): String?
    suspend fun saveSyncToken(token: String)
    suspend fun resetSyncToken()
    suspend fun getStartPageToken(): String?
    suspend fun getStartPageTokenForSyncToken(syncToken: String): String?
    suspend fun getInitialStartPageToken(): String
    suspend fun saveStartPageToken(token: String)
    suspend fun resetStartPageToken()

    // ---- Change detection ----
    suspend fun hasChanges(): Boolean
    suspend fun getChangesCount(): Int
    suspend fun getChangesPage(startPageToken: String, pageSize: Int): ChangesPageResult

    // ---- Recipe push/pull ----
    suspend fun pushRecipe(recipe: Recipe, checksum: String, operationId: String): Boolean
    suspend fun pushRecipes(recipes: List<Recipe>, checksums: Map<String, String>, operationId: String): Boolean
    suspend fun pullRecipe(recipeId: String): Recipe?

    // ---- Remote checksums & versions ----
    suspend fun getRemoteChecksum(recipeId: String): String
    suspend fun getRemoteChecksums(recipeIds: List<String>): Map<String, String>
    suspend fun getRemoteVersion(recipeId: String): Int
    suspend fun getRemoteVersions(recipeIds: List<String>): Map<String, Int>

    // ---- Sync history ----
    suspend fun getSyncHistory(): List<RemoteSyncHistoryEntry>
    suspend fun getRecipeFromHistory(recipeId: String, version: Int): Recipe?

    // ---- Tombstones ----
    suspend fun getTombstones(): List<RemoteTombstone>
    suspend fun getTombstonesForRecipes(recipeIds: List<String>): List<RemoteTombstone>
    suspend fun getTombstone(recipeId: String): RemoteTombstone?
    suspend fun deleteTombstonesOlderThan(timestampMillis: Long): Int

    // ---- Advisory locks (synced to Drive) ----
    suspend fun syncAdvisoryLock(lock: AdvisoryLock): Boolean
    suspend fun deleteAdvisoryLock(lockId: String): Boolean
}
