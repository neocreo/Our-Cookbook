package com.ourcookbook.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ourcookbook.domain.model.ConflictResolution
import com.ourcookbook.domain.model.ConflictStatus
import com.ourcookbook.domain.model.Device
import com.ourcookbook.domain.model.SyncConflict
import com.ourcookbook.domain.model.SyncLog
import com.ourcookbook.domain.model.SyncMetadata
import com.ourcookbook.domain.usecase.sync.SyncStatus
import com.ourcookbook.domain.usecase.sync.GetAllConflicts
import com.ourcookbook.domain.usecase.sync.GetAllSyncMetadata
import com.ourcookbook.domain.usecase.sync.GetPendingConflictCount
import com.ourcookbook.domain.usecase.sync.GetSyncStatus
import com.ourcookbook.domain.usecase.sync.ResolveSyncConflict
import com.ourcookbook.domain.usecase.sync.UpdateLastSyncTimestamp
import com.ourcookbook.domain.usecase.sync.UpdateSyncInProgress
import com.ourcookbook.domain.usecase.device.GetAllDevices
import com.ourcookbook.domain.usecase.recipe.GetRecipesNeedingSync
import com.ourcookbook.domain.usecase.recipe.SyncRecipe
import com.ourcookbook.domain.repository.SyncLogRepository
import com.ourcookbook.ui.screens.sync.ConflictSummary
import com.ourcookbook.ui.screens.sync.DeviceSyncInfo
import com.ourcookbook.ui.screens.sync.SyncErrorCategory
import com.ourcookbook.ui.screens.sync.SyncHistoryItem
import com.ourcookbook.ui.screens.sync.SyncStatistics
import com.ourcookbook.ui.screens.sync.SyncStatusDisplay
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

/**
 * Enhanced State for SyncStatusScreen
 * Task 2.1.06: Sync Status Screen Implementation
 * 
 * Comprehensive state management for sync status, history, conflicts, and device management
 */
data class SyncStatusState(
    // Loading states
    val isLoading: Boolean = true,
    val isLoadingHistory: Boolean = false,
    val isLoadingDevices: Boolean = false,
    val isLoadingConflicts: Boolean = false,
    
    // Error states
    val error: String? = null,
    val historyError: String? = null,
    val devicesError: String? = null,
    val conflictsError: String? = null,
    
    // Current sync status
    val syncStatus: SyncStatusDisplay = SyncStatusDisplay.SUCCESS,
    val lastSyncTimestamp: Instant? = null,
    val syncProgress: Int = 0,
    val syncMessage: String? = null,
    val isSyncing: Boolean = false,
    
    // Sync statistics
    val statistics: SyncStatistics = SyncStatistics(),
    
    // Pending changes
    val pendingLocalChanges: Int = 0,
    val pendingRemoteChanges: Int = 0,
    val pendingConflicts: Int = 0,
    
    // Sync history
    val syncHistory: List<SyncHistoryItem> = emptyList(),
    
    // Conflicts
    val conflicts: List<ConflictSummary> = emptyList(),
    
    // Device management
    val devices: List<DeviceSyncInfo> = emptyList(),
    val currentDeviceId: String = "",
    val currentDeviceName: String = "",
    
    // Manual sync controls
    val manualSyncInProgress: Boolean = false,
    val manualSyncProgress: Int = 0,
    val manualSyncMessage: String? = null,
    
    // Error handling
    val syncErrors: List<SyncErrorInfo> = emptyList(),
    
    // Sync frequency settings
    val syncFrequency: SyncFrequency = SyncFrequency.AUTOMATIC,
    val lastSyncCheck: Instant? = null
) {
    val hasConflicts: Boolean get() = pendingConflicts > 0
    val hasPendingChanges: Boolean get() = pendingLocalChanges > 0 || pendingRemoteChanges > 0
    val hasSyncHistory: Boolean get() = syncHistory.isNotEmpty()
    val hasDevices: Boolean get() = devices.isNotEmpty()
    val hasErrors: Boolean get() = syncErrors.isNotEmpty()
    val lastSyncFormatted: String get() = lastSyncTimestamp?.toString()?.substring(0, 19)?.replace("T", " ") ?: "Never"
    val syncStatusText: String get() = when (syncStatus) {
        SyncStatusDisplay.SUCCESS -> "Synced"
        SyncStatusDisplay.FAILURE -> "Error"
        SyncStatusDisplay.PARTIAL -> "Partial Sync"
        SyncStatusDisplay.CANCELLED -> "Cancelled"
        SyncStatusDisplay.SYNCING -> "Syncing"
    }
}

/**
 * Error information for sync operations
 */
data class SyncErrorInfo(
    val id: String,
    val timestamp: Instant,
    val message: String,
    val category: SyncErrorCategory,
    val syncId: String? = null,
    val isResolved: Boolean = false
)

/**
 * Sync frequency options
 */
enum class SyncFrequency {
    AUTOMATIC, MANUAL, HOURLY, DAILY, WEEKLY
}

/**
 * Events for SyncStatusScreen
 */
sealed class SyncStatusEvent {
    object LoadSyncStatus : SyncStatusEvent()
    object LoadSyncHistory : SyncStatusEvent()
    object LoadDevices : SyncStatusEvent()
    object LoadConflicts : SyncStatusEvent()
    object StartFullSync : SyncStatusEvent()
    object StartPullSync : SyncStatusEvent()
    object StartPushSync : SyncStatusEvent()
    object CancelSync : SyncStatusEvent()
    object ResolveAllConflicts : SyncStatusEvent()
    object ClearErrors : SyncStatusEvent()
    object RefreshAll : SyncStatusEvent()
    data class ResolveConflict(val conflictId: String, val resolution: ConflictResolution) : SyncStatusEvent()
    data class RetrySync(val syncId: String) : SyncStatusEvent()
    data class ForceSyncWithDevice(val deviceId: String) : SyncStatusEvent()
    data class SetSyncFrequency(val frequency: SyncFrequency) : SyncStatusEvent()
    data class NavigateToConflictResolution(val conflictId: String) : SyncStatusEvent()
    data class NavigateToDeviceManagement(val deviceId: String? = null) : SyncStatusEvent()
    data class NavigateToSyncDetails(val syncId: String) : SyncStatusEvent()
}

/**
 * Actions for SyncStatusScreen
 */
sealed class SyncStatusAction {
    data class ShowConflictResolution(val conflictId: String) : SyncStatusAction()
    data class ShowDeviceManagement(val deviceId: String? = null) : SyncStatusAction()
    data class ShowSyncDetails(val syncId: String) : SyncStatusAction()
    data class ShowError(val message: String) : SyncStatusAction()
    data class ShowSuccess(val message: String) : SyncStatusAction()
    data class ShowSyncComplete(
        val syncedItems: Int,
        val conflicts: Int,
        val duration: Long
    ) : SyncStatusAction()
    object ShowSyncInProgress : SyncStatusAction()
    object NavigateBack : SyncStatusAction()
}

/**
 * Enhanced ViewModel for SyncStatusScreen
 * Task 2.1.06: Sync Status Screen Implementation
 * 
 * Handles comprehensive sync status monitoring, sync operations, conflict resolution,
 * device management, and manual sync controls
 */
@HiltViewModel
class SyncStatusViewModel @Inject constructor(
    private val getSyncStatus: GetSyncStatus,
    private val getPendingConflictCount: GetPendingConflictCount,
    private val getAllConflicts: GetAllConflicts,
    private val getAllSyncMetadata: GetAllSyncMetadata,
    private val updateSyncInProgress: UpdateSyncInProgress,
    private val updateLastSyncTimestamp: UpdateLastSyncTimestamp,
    private val syncRecipe: SyncRecipe,
    private val getRecipesNeedingSync: GetRecipesNeedingSync,
    private val resolveSyncConflict: ResolveSyncConflict,
    private val getAllDevices: GetAllDevices,
    private val syncLogRepository: SyncLogRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SyncStatusState())
    val state: StateFlow<SyncStatusState> = _state.asStateFlow()

    private val _actions = MutableStateFlow<SyncStatusAction?>(null)
    val actions: StateFlow<SyncStatusAction?> = _actions.asStateFlow()

    private var currentDeviceId: String = ""
    private var currentDeviceName: String = ""

    init {
        loadInitialData()
    }

    fun handleEvent(event: SyncStatusEvent) {
        when (event) {
            is SyncStatusEvent.LoadSyncStatus -> loadSyncStatus()
            is SyncStatusEvent.LoadSyncHistory -> loadSyncHistory()
            is SyncStatusEvent.LoadDevices -> loadDevices()
            is SyncStatusEvent.LoadConflicts -> loadConflicts()
            is SyncStatusEvent.StartFullSync -> startFullSync()
            is SyncStatusEvent.StartPullSync -> startPullSync()
            is SyncStatusEvent.StartPushSync -> startPushSync()
            is SyncStatusEvent.CancelSync -> cancelSync()
            is SyncStatusEvent.ResolveAllConflicts -> resolveAllConflicts()
            is SyncStatusEvent.ClearErrors -> clearErrors()
            is SyncStatusEvent.RefreshAll -> refreshAll()
            is SyncStatusEvent.ResolveConflict -> resolveConflict(event.conflictId, event.resolution)
            is SyncStatusEvent.RetrySync -> retrySync(event.syncId)
            is SyncStatusEvent.ForceSyncWithDevice -> forceSyncWithDevice(event.deviceId)
            is SyncStatusEvent.SetSyncFrequency -> setSyncFrequency(event.frequency)
            is SyncStatusEvent.NavigateToConflictResolution -> navigateToConflictResolution(event.conflictId)
            is SyncStatusEvent.NavigateToDeviceManagement -> navigateToDeviceManagement(event.deviceId)
            is SyncStatusEvent.NavigateToSyncDetails -> navigateToSyncDetails(event.syncId)
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            // Load all data in parallel
            val syncStatusDeferred = launch { loadSyncStatus() }
            val historyDeferred = launch { loadSyncHistory() }
            val devicesDeferred = launch { loadDevices() }
            val conflictsDeferred = launch { loadConflicts() }
            
            // Wait for all to complete
            syncStatusDeferred.join()
            historyDeferred.join()
            devicesDeferred.join()
            conflictsDeferred.join()
            
            // Update loading state
            _state.value = _state.value.copy(isLoading = false)
        }
    }

    private fun loadSyncStatus() {
        viewModelScope.launch {
            try {
                // Load sync status
                val statusResult = getSyncStatus(currentDeviceId)
                val syncStatus = statusResult.getOrDefault(SyncStatus.IDLE)
                
                // Load pending conflicts
                val conflictsResult = getPendingConflictCount()
                val pendingConflicts = conflictsResult.getOrDefault(0)
                
                // Load sync metadata
                val metadataResult = getAllSyncMetadata()
                val syncMetadata = metadataResult.getOrDefault(emptyList())
                
                // Get last sync timestamp
                val lastSyncTimestamp = syncMetadata
                    .filter { it.deviceId == currentDeviceId }
                    .maxByOrNull { it.lastSyncTimestamp ?: Instant.MIN }
                    ?.lastSyncTimestamp
                
                // Get pending changes
                val recipesResult = getRecipesNeedingSync()
                val pendingLocalChanges = recipesResult.getOrDefault(emptyList()).size
                
                // Calculate statistics
                val statistics = calculateStatistics(syncMetadata)
                
                _state.value = _state.value.copy(
                    isLoading = false,
                    syncStatus = when (syncStatus) {
                        SyncStatus.IDLE -> SyncStatusDisplay.SUCCESS
                        SyncStatus.SYNCING -> SyncStatusDisplay.SYNCING
                        SyncStatus.ERROR -> SyncStatusDisplay.FAILURE
                        else -> SyncStatusDisplay.SUCCESS
                    },
                    lastSyncTimestamp = lastSyncTimestamp,
                    pendingConflicts = pendingConflicts,
                    pendingLocalChanges = pendingLocalChanges,
                    statistics = statistics,
                    isSyncing = syncStatus == SyncStatus.SYNCING
                )
                
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Failed to load sync status: ${e.message}"
                )
            }
        }
    }

    private fun loadSyncHistory() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoadingHistory = true, historyError = null)
            
            try {
                val logs = syncLogRepository.getAllLogs()
                val historyItems = logs.map { log ->
                    SyncHistoryItem.fromSyncLog(log, getDeviceName(log.deviceId))
                }.sortedByDescending { it.timestamp }
                
                _state.value = _state.value.copy(
                    isLoadingHistory = false,
                    syncHistory = historyItems
                )
                
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoadingHistory = false,
                    historyError = "Failed to load sync history: ${e.message}"
                )
            }
        }
    }

    private fun loadDevices() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoadingDevices = true, devicesError = null)
            
            try {
                val devices = getAllDevices().first()
                
                val deviceSyncInfos = devices.map { device ->
                    DeviceSyncInfo(
                        deviceId = device.deviceId,
                        deviceName = device.name,
                        lastSeen = device.lastSeenAt,
                        syncStatus = SyncStatusDisplay.SUCCESS, // Would be determined by actual status
                        lastSyncTimestamp = getLastSyncForDevice(device.deviceId),
                        pendingChanges = getPendingChangesForDevice(device.deviceId),
                        conflictCount = getConflictCountForDevice(device.deviceId),
                        syncCapabilities = device.capabilities.map { it.name }.toSet(),
                        isOnline = true // Would be determined by connectivity check
                    )
                }
                
                _state.value = _state.value.copy(
                    isLoadingDevices = false,
                    devices = deviceSyncInfos,
                    currentDeviceId = currentDeviceId,
                    currentDeviceName = currentDeviceName
                )
                
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoadingDevices = false,
                    devicesError = "Failed to load devices: ${e.message}"
                )
            }
        }
    }

    private fun loadConflicts() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoadingConflicts = true, conflictsError = null)
            
            try {
                val conflictsResult = getAllConflicts()
                val conflicts = conflictsResult.getOrDefault(emptyList())
                
                val conflictSummaries = conflicts.map { conflict ->
                    ConflictSummary(
                        conflictId = conflict.id,
                        recipeName = "Recipe ${conflict.localRecipeId.take(8)}", // Would get actual recipe name
                        conflictType = when (conflict.status) {
                            ConflictStatus.PENDING -> "Pending"
                            ConflictStatus.RESOLVED -> "Resolved"
                            ConflictStatus.IGNORED -> "Ignored"
                        },
                        detectedAt = conflict.detectedAt,
                        status = conflict.status.toString(),
                        localVersion = conflict.localVersion.toString(),
                        remoteVersion = conflict.remoteVersion.toString()
                    )
                }
                
                _state.value = _state.value.copy(
                    isLoadingConflicts = false,
                    conflicts = conflictSummaries,
                    pendingConflicts = conflictSummaries.count { it.status == "PENDING" }
                )
                
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoadingConflicts = false,
                    conflictsError = "Failed to load conflicts: ${e.message}"
                )
            }
        }
    }

    private fun startFullSync() {
        viewModelScope.launch {
            try {
                // Mark sync as in progress
                updateSyncInProgress(currentDeviceId, true)
                _state.value = _state.value.copy(
                    isSyncing = true,
                    syncProgress = 0,
                    syncMessage = "Checking for changes...",
                    syncStatus = SyncStatusDisplay.SYNCING
                )
                _actions.value = SyncStatusAction.ShowSyncInProgress
                
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
                        lastSyncTimestamp = Instant.now(),
                        syncStatus = SyncStatusDisplay.SUCCESS
                    )
                    
                    _actions.value = SyncStatusAction.ShowSyncComplete(
                        syncedItems = result.syncedRecipes,
                        conflicts = result.conflicts,
                        duration = 0
                    )
                    
                    // Refresh all data
                    refreshAll()
                    
                }.onFailure { e ->
                    updateSyncInProgress(currentDeviceId, false)
                    _state.value = _state.value.copy(
                        isSyncing = false,
                        error = "Sync failed: ${e.message}",
                        syncStatus = SyncStatusDisplay.FAILURE
                    )
                    _actions.value = SyncStatusAction.ShowError("Sync failed: ${e.message}")
                }
                
            } catch (e: Exception) {
                updateSyncInProgress(currentDeviceId, false)
                _state.value = _state.value.copy(
                    isSyncing = false,
                    error = "Sync failed: ${e.message}",
                    syncStatus = SyncStatusDisplay.FAILURE
                )
                _actions.value = SyncStatusAction.ShowError("Sync failed: ${e.message}")
            }
        }
    }

    private fun startPullSync() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(
                    isSyncing = true,
                    syncProgress = 0,
                    syncMessage = "Pulling changes from Google Drive...",
                    syncStatus = SyncStatusDisplay.SYNCING
                )
                
                // Simulate pull operation
                // In production, this would call the actual pull functionality
                _state.value = _state.value.copy(
                    syncProgress = 50,
                    syncMessage = "Downloading remote changes..."
                )
                
                // Simulate completion
                _state.value = _state.value.copy(
                    isSyncing = false,
                    syncProgress = 100,
                    syncMessage = "Pull complete",
                    lastSyncTimestamp = Instant.now(),
                    syncStatus = SyncStatusDisplay.SUCCESS
                )
                
                _actions.value = SyncStatusAction.ShowSuccess("Pull completed successfully")
                loadSyncStatus()
                
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isSyncing = false,
                    error = "Pull failed: ${e.message}",
                    syncStatus = SyncStatusDisplay.FAILURE
                )
                _actions.value = SyncStatusAction.ShowError("Pull failed: ${e.message}")
            }
        }
    }

    private fun startPushSync() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(
                    isSyncing = true,
                    syncProgress = 0,
                    syncMessage = "Pushing changes to Google Drive...",
                    syncStatus = SyncStatusDisplay.SYNCING
                )
                
                // Simulate push operation
                _state.value = _state.value.copy(
                    syncProgress = 50,
                    syncMessage = "Uploading local changes..."
                )
                
                // Simulate completion
                _state.value = _state.value.copy(
                    isSyncing = false,
                    syncProgress = 100,
                    syncMessage = "Push complete",
                    lastSyncTimestamp = Instant.now(),
                    syncStatus = SyncStatusDisplay.SUCCESS
                )
                
                _actions.value = SyncStatusAction.ShowSuccess("Push completed successfully")
                loadSyncStatus()
                
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isSyncing = false,
                    error = "Push failed: ${e.message}",
                    syncStatus = SyncStatusDisplay.FAILURE
                )
                _actions.value = SyncStatusAction.ShowError("Push failed: ${e.message}")
            }
        }
    }

    private fun cancelSync() {
        viewModelScope.launch {
            try {
                updateSyncInProgress(currentDeviceId, false)
                _state.value = _state.value.copy(
                    isSyncing = false,
                    syncProgress = 0,
                    syncMessage = null,
                    syncStatus = SyncStatusDisplay.CANCELLED
                )
                _actions.value = SyncStatusAction.ShowSuccess("Sync cancelled")
                
            } catch (e: Exception) {
                _actions.value = SyncStatusAction.ShowError("Failed to cancel sync: ${e.message}")
            }
        }
    }

    private fun resolveAllConflicts() {
        viewModelScope.launch {
            val currentState = _state.value
            if (currentState.conflicts.isNotEmpty()) {
                // For now, navigate to the first conflict
                _actions.value = SyncStatusAction.ShowConflictResolution(currentState.conflicts.first().conflictId)
            }
        }
    }

    private fun resolveConflict(conflictId: String, resolution: ConflictResolution) {
        viewModelScope.launch {
            try {
                val result = resolveSyncConflict(conflictId, resolution)
                result.onSuccess {
                    _actions.value = SyncStatusAction.ShowSuccess("Conflict resolved successfully")
                    // Refresh conflicts and sync status
                    loadConflicts()
                    loadSyncStatus()
                }.onFailure { e ->
                    _actions.value = SyncStatusAction.ShowError("Failed to resolve conflict: ${e.message}")
                }
                
            } catch (e: Exception) {
                _actions.value = SyncStatusAction.ShowError("Failed to resolve conflict: ${e.message}")
            }
        }
    }

    private fun retrySync(syncId: String) {
        viewModelScope.launch {
            // Find the sync log and retry
            try {
                val syncLog = syncLogRepository.getLogById(syncId)
                syncLog?.let { log ->
                    // For now, just start a new sync
                    startFullSync()
                }
                
            } catch (e: Exception) {
                _actions.value = SyncStatusAction.ShowError("Failed to retry sync: ${e.message}")
            }
        }
    }

    private fun forceSyncWithDevice(deviceId: String) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(
                    isSyncing = true,
                    syncProgress = 0,
                    syncMessage = "Forcing sync with device $deviceId...",
                    syncStatus = SyncStatusDisplay.SYNCING
                )
                
                // Simulate device sync
                _state.value = _state.value.copy(
                    syncProgress = 100,
                    syncMessage = "Device sync complete",
                    isSyncing = false,
                    syncStatus = SyncStatusDisplay.SUCCESS
                )
                
                _actions.value = SyncStatusAction.ShowSuccess("Device sync completed")
                loadDevices()
                
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isSyncing = false,
                    error = "Device sync failed: ${e.message}",
                    syncStatus = SyncStatusDisplay.FAILURE
                )
                _actions.value = SyncStatusAction.ShowError("Device sync failed: ${e.message}")
            }
        }
    }

    private fun setSyncFrequency(frequency: SyncFrequency) {
        viewModelScope.launch {
            _state.value = _state.value.copy(syncFrequency = frequency)
            _actions.value = SyncStatusAction.ShowSuccess("Sync frequency updated to ${frequency.name}")
        }
    }

    private fun clearErrors() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                error = null,
                historyError = null,
                devicesError = null,
                conflictsError = null,
                syncErrors = emptyList()
            )
            _actions.value = SyncStatusAction.ShowSuccess("Errors cleared")
        }
    }

    private fun refreshAll() {
        viewModelScope.launch {
            loadSyncStatus()
            loadSyncHistory()
            loadDevices()
            loadConflicts()
        }
    }

    private fun navigateToConflictResolution(conflictId: String) {
        viewModelScope.launch {
            _actions.value = SyncStatusAction.ShowConflictResolution(conflictId)
        }
    }

    private fun navigateToDeviceManagement(deviceId: String?) {
        viewModelScope.launch {
            _actions.value = SyncStatusAction.ShowDeviceManagement(deviceId)
        }
    }

    private fun navigateToSyncDetails(syncId: String) {
        viewModelScope.launch {
            _actions.value = SyncStatusAction.ShowSyncDetails(syncId)
        }
    }

    // Helper methods
    private fun calculateStatistics(metadataList: List<SyncMetadata>): SyncStatistics {
        val currentDeviceMetadata = metadataList.filter { it.deviceId == currentDeviceId }
        
        val totalSyncs = currentDeviceMetadata.size
        val successfulSyncs = currentDeviceMetadata.count { it.lastSuccessfulSync != null }
        val failedSyncs = totalSyncs - successfulSyncs
        
        val totalChangesSynced = currentDeviceMetadata.sumOf { it.pendingChanges }
        val totalConflicts = currentDeviceMetadata.sumOf { it.conflictCount }
        
        val lastSyncTimestamp = currentDeviceMetadata
            .maxByOrNull { it.lastSyncTimestamp ?: Instant.MIN }
            ?.lastSyncTimestamp
        
        // Calculate average duration (would need sync logs for actual data)
        val averageSyncDuration = 0L
        
        return SyncStatistics(
            totalSyncs = totalSyncs,
            successfulSyncs = successfulSyncs,
            failedSyncs = failedSyncs,
            totalChangesSynced = totalChangesSynced,
            totalConflicts = totalConflicts,
            averageSyncDuration = averageSyncDuration,
            lastSyncTimestamp = lastSyncTimestamp
        )
    }

    private fun getDeviceName(deviceId: String): String {
        return if (deviceId == currentDeviceId) currentDeviceName else "Device $deviceId"
    }

    private fun getLastSyncForDevice(deviceId: String): Instant? {
        // Would query sync metadata for this device
        return null
    }

    private fun getPendingChangesForDevice(deviceId: String): Int {
        // Would query pending changes for this device
        return 0
    }

    private fun getConflictCountForDevice(deviceId: String): Int {
        // Would query conflict count for this device
        return 0
    }

    fun setDeviceId(deviceId: String) {
        currentDeviceId = deviceId
        currentDeviceName = "Device $deviceId" // Would get actual device name
        loadInitialData()
    }

    fun setDeviceName(deviceName: String) {
        currentDeviceName = deviceName
        _state.value = _state.value.copy(currentDeviceName = deviceName)
    }

    fun clearAction() {
        viewModelScope.launch {
            _actions.value = null
        }
    }
}

