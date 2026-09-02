package com.ourcookbook.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ourcookbook.domain.model.SyncConflict
import com.ourcookbook.domain.model.SyncMetadata
import com.ourcookbook.domain.usecase.sync.GetSyncStatus
import com.ourcookbook.domain.usecase.sync.GetPendingConflictCount
import com.ourcookbook.domain.usecase.sync.GetAllConflicts
import com.ourcookbook.domain.usecase.sync.GetAllSyncMetadata
import com.ourcookbook.domain.usecase.sync.UpdateSyncInProgress
import com.ourcookbook.domain.usecase.sync.UpdateLastSyncTimestamp
import com.ourcookbook.domain.usecase.recipe.SyncRecipe
import com.ourcookbook.domain.usecase.recipe.GetRecipesNeedingSync
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

/**
 * State for SyncStatusScreen
 */
data class SyncState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val syncStatus: String = "IDLE",
    val lastSyncTimestamp: Instant? = null,
    val pendingConflicts: Int = 0,
    val syncMetadata: List<SyncMetadata> = emptyList(),
    val conflicts: List<SyncConflict> = emptyList(),
    val isSyncing: Boolean = false,
    val syncProgress: Int = 0,
    val syncMessage: String? = null
) {
    val hasConflicts: Boolean get() = pendingConflicts > 0
    val lastSyncFormatted: String get() = lastSyncTimestamp?.toString() ?: "Never"
}

/**
 * Event for SyncStatusScreen
 */
sealed class SyncEvent {
    object LoadSyncStatus : SyncEvent()
    object StartSync : SyncEvent()
    object StopSync : SyncEvent()
    object ResolveAllConflicts : SyncEvent()
    object Refresh : SyncEvent()
    data class ResolveConflict(val conflictId: String, val resolution: String) : SyncEvent()
}

/**
 * Action for SyncStatusScreen
 */
sealed class SyncAction {
    data class ShowConflictResolution(val conflictId: String) : SyncAction()
    data class ShowError(val message: String) : SyncAction()
    data class ShowSyncComplete(val syncedItems: Int, val conflicts: Int) : SyncAction()
    object ShowSyncInProgress : SyncAction()
}

/**
 * ViewModel for SyncStatusScreen
 * Handles sync status monitoring and sync operations
 */
@HiltViewModel
class SyncViewModel @Inject constructor(
    private val getSyncStatus: GetSyncStatus,
    private val getPendingConflictCount: GetPendingConflictCount,
    private val getAllConflicts: GetAllConflicts,
    private val getAllSyncMetadata: GetAllSyncMetadata,
    private val updateSyncInProgress: UpdateSyncInProgress,
    private val updateLastSyncTimestamp: UpdateLastSyncTimestamp,
    private val syncRecipe: SyncRecipe,
    private val getRecipesNeedingSync: GetRecipesNeedingSync
) : ViewModel() {

    private val _state = MutableStateFlow(SyncState())
    val state: StateFlow<SyncState> = _state.asStateFlow()

    private val _actions = MutableStateFlow<SyncAction?>(null)
    val actions: StateFlow<SyncAction?> = _actions.asStateFlow()

    private var currentDeviceId: String = "current_device_id" // Will be set properly in production

    init {
        loadSyncStatus()
    }

    fun handleEvent(event: SyncEvent) {
        when (event) {
            is SyncEvent.LoadSyncStatus -> loadSyncStatus()
            is SyncEvent.StartSync -> startSync()
            is SyncEvent.StopSync -> stopSync()
            is SyncEvent.ResolveAllConflicts -> resolveAllConflicts()
            is SyncEvent.Refresh -> refresh()
            is SyncEvent.ResolveConflict -> resolveConflict(event.conflictId, event.resolution)
        }
    }

    private fun loadSyncStatus() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            try {
                // Load sync status
                val statusResult = getSyncStatus(currentDeviceId)
                val syncStatus = statusResult.getOrDefault("IDLE")
                
                // Load pending conflicts
                val conflictsResult = getPendingConflictCount()
                val pendingConflicts = conflictsResult.getOrDefault(0)
                
                // Load sync metadata
                val metadataResult = getAllSyncMetadata()
                val syncMetadata = metadataResult.getOrDefault(emptyList())
                
                // Load all conflicts
                val allConflictsResult = getAllConflicts()
                val conflicts = allConflictsResult.getOrDefault(emptyList())
                
                // Get last sync timestamp
                val lastSyncTimestamp = syncMetadata
                    .filter { it.deviceId == currentDeviceId }
                    .maxByOrNull { it.lastSyncTimestamp ?: java.time.Instant.MIN }
                    ?.lastSyncTimestamp
                
                _state.value = _state.value.copy(
                    isLoading = false,
                    syncStatus = syncStatus.toString(),
                    lastSyncTimestamp = lastSyncTimestamp,
                    pendingConflicts = pendingConflicts,
                    syncMetadata = syncMetadata,
                    conflicts = conflicts,
                    isSyncing = syncStatus.toString() == "SYNCING"
                )
                
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Failed to load sync status: ${e.message}"
                )
            }
        }
    }

    private fun startSync() {
        viewModelScope.launch {
            try {
                // Mark sync as in progress
                updateSyncInProgress(currentDeviceId, true)
                _state.value = _state.value.copy(
                    isSyncing = true,
                    syncProgress = 0,
                    syncMessage = "Checking for changes..."
                )
                _actions.value = SyncAction.ShowSyncInProgress
                
                // Get recipes needing sync
                val recipesResult = getRecipesNeedingSync()
                val localRecipes = recipesResult.getOrDefault(emptyList())
                
                _state.value = _state.value.copy(
                    syncProgress = 25,
                    syncMessage = "Found ${localRecipes.size} local changes"
                )
                
                // Simulate remote recipes (in production, this would come from remote data source)
                val remoteRecipes = emptyList<com.ourcookbook.domain.model.Recipe>()
                
                // Perform sync
                val syncResult = syncRecipe(remoteRecipes)
                
                syncResult.onSuccess { result ->
                    // Update last sync timestamp
                    updateLastSyncTimestamp(currentDeviceId, Instant.now())
                    
                    // Mark sync as complete
                    updateSyncInProgress(currentDeviceId, false)
                    
                    _state.value = _state.value.copy(
                        isSyncing = false,
                        syncProgress = 100,
                        syncMessage = "Sync complete",
                        lastSyncTimestamp = Instant.now()
                    )
                    
                    _actions.value = SyncAction.ShowSyncComplete(
                        syncedItems = result.syncedRecipes,
                        conflicts = result.conflicts
                    )
                    
                    // Refresh status
                    loadSyncStatus()
                    
                }.onFailure { e ->
                    updateSyncInProgress(currentDeviceId, false)
                    _state.value = _state.value.copy(
                        isSyncing = false,
                        error = "Sync failed: ${e.message}"
                    )
                    _actions.value = SyncAction.ShowError("Sync failed: ${e.message}")
                }
                
            } catch (e: Exception) {
                updateSyncInProgress(currentDeviceId, false)
                _state.value = _state.value.copy(
                    isSyncing = false,
                    error = "Sync failed: ${e.message}"
                )
                _actions.value = SyncAction.ShowError("Sync failed: ${e.message}")
            }
        }
    }

    private fun stopSync() {
        viewModelScope.launch {
            try {
                updateSyncInProgress(currentDeviceId, false)
                _state.value = _state.value.copy(
                    isSyncing = false,
                    syncProgress = 0,
                    syncMessage = null
                )
            } catch (e: Exception) {
                _actions.value = SyncAction.ShowError("Failed to stop sync: ${e.message}")
            }
        }
    }

    private fun resolveAllConflicts() {
        viewModelScope.launch {
            val currentState = _state.value
            if (currentState.conflicts.isNotEmpty()) {
                // For now, just navigate to the first conflict
                _actions.value = SyncAction.ShowConflictResolution(currentState.conflicts.first().id)
            }
        }
    }

    private fun resolveConflict(conflictId: String, resolution: String) {
        viewModelScope.launch {
            // TODO: Implement conflict resolution
            // For now, just refresh the status
            loadSyncStatus()
        }
    }

    fun navigateToConflictResolution(conflictId: String) {
        viewModelScope.launch {
            _actions.value = SyncAction.ShowConflictResolution(conflictId)
        }
    }

    private fun refresh() {
        loadSyncStatus()
    }

    fun clearAction() {
        viewModelScope.launch {
            _actions.value = null
        }
    }

    fun setDeviceId(deviceId: String) {
        currentDeviceId = deviceId
        loadSyncStatus()
    }
}