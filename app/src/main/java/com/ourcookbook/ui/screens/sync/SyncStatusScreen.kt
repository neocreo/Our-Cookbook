package com.ourcookbook.ui.screens.sync

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeviceHub
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SyncProblem
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ourcookbook.domain.model.ConflictResolution
import com.ourcookbook.domain.model.SyncConflict
import com.ourcookbook.domain.model.SyncMetadata
import com.ourcookbook.ui.components.CookbookCard
import com.ourcookbook.ui.components.CookbookIconButton
import com.ourcookbook.ui.components.CookbookPrimaryButton
import com.ourcookbook.ui.components.CookbookSecondaryButton
import com.ourcookbook.ui.components.EmptyState
import com.ourcookbook.ui.components.LoadingState
import com.ourcookbook.ui.navigation.Route
import com.ourcookbook.ui.theme.CookbookTheme
import com.ourcookbook.ui.theme.CookbookTypography
import com.ourcookbook.ui.viewmodel.SyncStatusAction
import com.ourcookbook.ui.viewmodel.SyncStatusEvent
import com.ourcookbook.ui.viewmodel.SyncStatusState
import com.ourcookbook.ui.viewmodel.SyncStatusViewModel
import kotlinx.coroutines.launch

/**
 * Enhanced Sync Status Screen
 * Task 2.1.06: Sync Status Screen Implementation
 * 
 * Comprehensive sync status screen with:
 * - Sync status overview
 * - Sync history
 * - Conflict resolution
 * - Device management
 * - Manual sync controls
 * - Error handling & recovery
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncStatusScreen(
    viewModel: SyncStatusViewModel,
    navController: NavController
) {
    val state by viewModel.state.collectAsState()
    val actions by viewModel.actions.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Handle actions
    LaunchedEffect(actions) {
        actions?.let { action ->
            when (action) {
                is SyncStatusAction.ShowConflictResolution -> {
                    navController.navigate(Route.conflictResolution(action.conflictId))
                    viewModel.clearAction()
                }
                is SyncStatusAction.ShowDeviceManagement -> {
                    action.deviceId?.let { deviceId ->
                        navController.navigate("${Route.SYNC_STATUS}/devices/$deviceId")
                    } ?: navController.navigate("${Route.SYNC_STATUS}/devices")
                    viewModel.clearAction()
                }
                is SyncStatusAction.ShowSyncDetails -> {
                    navController.navigate("${Route.SYNC_STATUS}/details/${action.syncId}")
                    viewModel.clearAction()
                }
                is SyncStatusAction.ShowError -> {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(action.message)
                    }
                    viewModel.clearAction()
                }
                is SyncStatusAction.ShowSuccess -> {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(action.message)
                    }
                    viewModel.clearAction()
                }
                is SyncStatusAction.ShowSyncComplete -> {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            "Sync complete: ${action.syncedItems} items, ${action.conflicts} conflicts"
                        )
                    }
                    viewModel.clearAction()
                }
                SyncStatusAction.ShowSyncInProgress -> {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Sync in progress...")
                    }
                    viewModel.clearAction()
                }
                SyncStatusAction.NavigateBack -> {
                    navController.popBackStack()
                    viewModel.clearAction()
                }
            }
        }
    }

    // Load initial data
    LaunchedEffect(Unit) {
        viewModel.handleEvent(SyncStatusEvent.LoadSyncStatus)
        viewModel.handleEvent(SyncStatusEvent.LoadSyncHistory)
        viewModel.handleEvent(SyncStatusEvent.LoadDevices)
        viewModel.handleEvent(SyncStatusEvent.LoadConflicts)
    }

    Scaffold(
        topBar = {
            SyncStatusTopAppBar(
                onBackClick = { navController.popBackStack() },
                onRefreshClick = { viewModel.handleEvent(SyncStatusEvent.RefreshAll) },
                onMenuClick = { /* Show menu */ }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when {
                state.isLoading -> {
                    LoadingState()
                }
                state.error != null -> {
                    ErrorState(
                        message = state.error,
                        onRetry = { viewModel.handleEvent(SyncStatusEvent.RefreshAll) }
                    )
                }
                else -> {
                    SyncStatusContent(
                        state = state,
                        onStartFullSync = { viewModel.handleEvent(SyncStatusEvent.StartFullSync) },
                        onStartPullSync = { viewModel.handleEvent(SyncStatusEvent.StartPullSync) },
                        onStartPushSync = { viewModel.handleEvent(SyncStatusEvent.StartPushSync) },
                        onCancelSync = { viewModel.handleEvent(SyncStatusEvent.CancelSync) },
                        onConflictClick = { conflictId ->
                            viewModel.handleEvent(SyncStatusEvent.NavigateToConflictResolution(conflictId))
                        },
                        onResolveAllConflicts = { 
                            viewModel.handleEvent(SyncStatusEvent.ResolveAllConflicts) 
                        },
                        onDeviceClick = { deviceId ->
                            viewModel.handleEvent(SyncStatusEvent.NavigateToDeviceManagement(deviceId))
                        },
                        onForceSyncWithDevice = { deviceId ->
                            viewModel.handleEvent(SyncStatusEvent.ForceSyncWithDevice(deviceId))
                        },
                        onRetrySync = { syncId ->
                            viewModel.handleEvent(SyncStatusEvent.RetrySync(syncId))
                        },
                        onClearErrors = { viewModel.handleEvent(SyncStatusEvent.ClearErrors) },
                        onSyncDetailsClick = { syncId ->
                            viewModel.handleEvent(SyncStatusEvent.NavigateToSyncDetails(syncId))
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncStatusTopAppBar(
    onBackClick: () -> Unit,
    onRefreshClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    TopAppBar(
        title = { Text("Sync Status", style = CookbookTypography.titleLarge) },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        actions = {
            IconButton(onClick = onRefreshClick) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh"
                )
            }
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Menu"
                )
            }
        }
    )
}

@Composable
fun SyncStatusContent(
    state: SyncStatusState,
    onStartFullSync: () -> Unit,
    onStartPullSync: () -> Unit,
    onStartPushSync: () -> Unit,
    onCancelSync: () -> Unit,
    onConflictClick: (String) -> Unit,
    onResolveAllConflicts: () -> Unit,
    onDeviceClick: (String) -> Unit,
    onForceSyncWithDevice: (String) -> Unit,
    onRetrySync: (String) -> Unit,
    onClearErrors: () -> Unit,
    onSyncDetailsClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Sync Status Overview Card
        SyncStatusOverviewCard(
            state = state,
            onStartFullSync = onStartFullSync,
            onStartPullSync = onStartPullSync,
            onStartPushSync = onStartPushSync,
            onCancelSync = onCancelSync
        )

        // Sync Progress (if syncing)
        if (state.isSyncing) {
            SyncProgressCard(
                progress = state.syncProgress,
                message = state.syncMessage ?: "Syncing..."
            )
        }

        // Sync Statistics
        SyncStatisticsCard(statistics = state.statistics)

        // Sync History Section
        SyncHistorySection(
            history = state.syncHistory,
            isLoading = state.isLoadingHistory,
            error = state.historyError,
            onRetrySync = onRetrySync,
            onSyncDetailsClick = onSyncDetailsClick
        )

        // Conflicts Section
        if (state.hasConflicts) {
            ConflictsSection(
                conflicts = state.conflicts,
                onConflictClick = onConflictClick,
                onResolveAllConflicts = onResolveAllConflicts
            )
        }

        // Device Management Section
        DeviceManagementSection(
            devices = state.devices,
            isLoading = state.isLoadingDevices,
            error = state.devicesError,
            onDeviceClick = onDeviceClick,
            onForceSyncWithDevice = onForceSyncWithDevice
        )

        // Error Handling Section
        if (state.hasErrors) {
            ErrorHandlingSection(
                errors = state.syncErrors,
                onClearErrors = onClearErrors
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun SyncStatusOverviewCard(
    state: SyncStatusState,
    onStartFullSync: () -> Unit,
    onStartPullSync: () -> Unit,
    onStartPushSync: () -> Unit,
    onCancelSync: () -> Unit
) {
    CookbookCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        elevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Status Icon and Text
            when (state.syncStatus) {
                SyncStatusDisplay.SUCCESS -> {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Synced",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Up to Date",
                        style = CookbookTypography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "All recipes are synced",
                        style = CookbookTypography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                SyncStatusDisplay.SYNCING -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(64.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 4.dp
                    )
                    Text(
                        text = "Sync in Progress",
                        style = CookbookTypography.headlineSmall
                    )
                    Text(
                        text = "Syncing your recipes with Google Drive",
                        style = CookbookTypography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                SyncStatusDisplay.FAILURE -> {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Error",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "Sync Error",
                        style = CookbookTypography.headlineSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "Last sync failed. Check errors below.",
                        style = CookbookTypography.bodyMedium
                    )
                }
                SyncStatusDisplay.PARTIAL -> {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Partial Sync",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "Partial Sync",
                        style = CookbookTypography.headlineSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "Some recipes may not be synced",
                        style = CookbookTypography.bodyMedium
                    )
                }
                SyncStatusDisplay.CANCELLED -> {
                    Icon(
                        imageVector = Icons.Default.Cancel,
                        contentDescription = "Cancelled",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "Sync Cancelled",
                        style = CookbookTypography.headlineSmall
                    )
                    Text(
                        text = "Sync was cancelled by user",
                        style = CookbookTypography.bodyMedium
                    )
                }
            }

            // Last Sync Info
            Text(
                text = "Last Sync: ${state.lastSyncFormatted}",
                style = CookbookTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            // Pending Changes Info
            if (state.hasPendingChanges) {
                Text(
                    text = "Pending: ${state.pendingLocalChanges} local, ${state.pendingRemoteChanges} remote",
                    style = CookbookTypography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            // Manual Sync Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (state.isSyncing) {
                    CookbookSecondaryButton(
                        text = "Cancel",
                        onClick = onCancelSync,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    CookbookPrimaryButton(
                        text = "Full Sync",
                        onClick = onStartFullSync,
                        modifier = Modifier.weight(1f)
                    )
                    
                    CookbookSecondaryButton(
                        text = "Pull",
                        onClick = onStartPullSync,
                        modifier = Modifier.weight(1f)
                    )
                    
                    CookbookSecondaryButton(
                        text = "Push",
                        onClick = onStartPushSync,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun SyncProgressCard(progress: Int, message: String) {
    CookbookCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = message,
                style = CookbookTypography.bodyMedium
            )
            
            LinearProgressIndicator(
                progress = { progress / 100f },
                modifier = Modifier.fillMaxWidth()
            )
            
            Text(
                text = "$progress%",
                style = CookbookTypography.bodySmall,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Composable
fun SyncStatisticsCard(statistics: SyncStatistics) {
    CookbookCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Sync Statistics",
                style = CookbookTypography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatItem(
                    value = statistics.totalSyncs.toString(),
                    label = "Total Syncs",
                    icon = Icons.Default.Sync,
                    modifier = Modifier.weight(1f)
                )
                
                StatItem(
                    value = "${statistics.successRate.toInt()}%",
                    label = "Success Rate",
                    icon = Icons.Default.CheckCircle,
                    modifier = Modifier.weight(1f)
                )
                
                StatItem(
                    value = statistics.totalChangesSynced.toString(),
                    label = "Items Synced",
                    icon = Icons.Default.Cloud,
                    modifier = Modifier.weight(1f)
                )
                
                StatItem(
                    value = statistics.totalConflicts.toString(),
                    label = "Total Conflicts",
                    icon = Icons.Default.Warning,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun StatItem(value: String, label: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value,
            style = CookbookTypography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = CookbookTypography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun SyncHistorySection(
    history: List<SyncHistoryItem>,
    isLoading: Boolean,
    error: String?,
    onRetrySync: (String) -> Unit,
    onSyncDetailsClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Sync History",
                style = CookbookTypography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            if (history.isNotEmpty()) {
                Text(
                    text = "${history.size} syncs",
                    style = CookbookTypography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }

        when {
            isLoading -> {
                LoadingState(modifier = Modifier.fillMaxWidth())
            }
            error != null -> {
                ErrorState(
                    message = error,
                    onRetry = { /* Retry loading history */ }
                )
            }
            history.isEmpty() -> {
                EmptyState(
                    icon = Icons.Default.History,
                    title = "No Sync History",
                    description = "Sync history will appear here after your first sync"
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(history) { index, item ->
                        SyncHistoryItemCard(
                            item = item,
                            onRetry = { onRetrySync(item.id) },
                            onDetailsClick = { onSyncDetailsClick(item.id) },
                            showDivider = index < history.size - 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SyncHistoryItemCard(
    item: SyncHistoryItem,
    onRetry: () -> Unit,
    onDetailsClick: () -> Unit,
    showDivider: Boolean = false
) {
    CookbookCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        onClick = onDetailsClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = item.formattedTimestamp,
                        style = CookbookTypography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SyncDirectionBadge(direction = item.direction)
                        SyncStatusBadge(status = item.status)
                    }
                }
                
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "${item.changesSynchronized} items",
                        style = CookbookTypography.bodySmall
                    )
                    Text(
                        text = item.formattedDuration,
                        style = CookbookTypography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            
            if (item.hasErrors) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.errorMessage ?: "Unknown error",
                        style = CookbookTypography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    OutlinedButton(
                        onClick = { onRetry() },
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Retry", style = CookbookTypography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
fun SyncDirectionBadge(direction: com.ourcookbook.ui.screens.sync.SyncDirection) {
    val (text, icon, color) = when (direction) {
        com.ourcookbook.ui.screens.sync.SyncDirection.PULL -> Triple("Pull", Icons.Default.CloudDownload, MaterialTheme.colorScheme.secondary)
        com.ourcookbook.ui.screens.sync.SyncDirection.PUSH -> Triple("Push", Icons.Default.CloudUpload, MaterialTheme.colorScheme.tertiary)
        com.ourcookbook.ui.screens.sync.SyncDirection.BOTH -> Triple("Both", Icons.Default.Sync, MaterialTheme.colorScheme.primary)
    }
    
    Row(
        modifier = Modifier
            .background(
                color = color.copy(alpha = 0.2f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = direction.name,
            modifier = Modifier.size(16.dp),
            tint = color
        )
        Text(
            text = text,
            style = CookbookTypography.labelSmall,
            color = color
        )
    }
}

@Composable
fun SyncStatusBadge(status: SyncStatusDisplay) {
    val (text, color) = when (status) {
        SyncStatusDisplay.SUCCESS -> Pair("Success", MaterialTheme.colorScheme.primary)
        SyncStatusDisplay.FAILURE -> Pair("Failed", MaterialTheme.colorScheme.error)
        SyncStatusDisplay.PARTIAL -> Pair("Partial", MaterialTheme.colorScheme.error)
        SyncStatusDisplay.CANCELLED -> Pair("Cancelled", MaterialTheme.colorScheme.onSurface)
        SyncStatusDisplay.SYNCING -> Pair("Syncing", MaterialTheme.colorScheme.primary)
    }
    
    Box(
        modifier = Modifier
            .background(
                color = color.copy(alpha = 0.2f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = CookbookTypography.labelSmall,
            color = color
        )
    }
}

@Composable
fun ConflictsSection(
    conflicts: List<ConflictSummary>,
    onConflictClick: (String) -> Unit,
    onResolveAllConflicts: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Sync Conflicts (${conflicts.size})",
                style = CookbookTypography.titleMedium,
                color = MaterialTheme.colorScheme.error
            )
            
            if (conflicts.isNotEmpty()) {
                OutlinedButton(
                    onClick = onResolveAllConflicts,
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Resolve All", style = CookbookTypography.labelSmall)
                }
            }
        }

        if (conflicts.isEmpty()) {
            EmptyState(
                icon = Icons.Default.Warning,
                title = "No Conflicts",
                description = "All sync conflicts have been resolved"
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(conflicts, key = { it.conflictId }) { conflict ->
                    ConflictItemCard(
                        conflict = conflict,
                        onClick = { onConflictClick(conflict.conflictId) }
                    )
                }
            }
        }
    }
}

@Composable
fun ConflictItemCard(conflict: ConflictSummary, onClick: () -> Unit) {
    CookbookCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = conflict.recipeName,
                        style = CookbookTypography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Text(
                        text = "Conflict #${conflict.conflictId.take(8)}",
                        style = CookbookTypography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    
                    Text(
                        text = "Type: ${conflict.conflictType}",
                        style = CookbookTypography.bodySmall
                    )
                    
                    Text(
                        text = "Detected: ${conflict.formattedDetectedAt}",
                        style = CookbookTypography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "View Conflict",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            if (conflict.status == "PENDING") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "Needs Resolution",
                        style = CookbookTypography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun DeviceManagementSection(
    devices: List<DeviceSyncInfo>,
    isLoading: Boolean,
    error: String?,
    onDeviceClick: (String) -> Unit,
    onForceSyncWithDevice: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Connected Devices",
                style = CookbookTypography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            if (devices.isNotEmpty()) {
                Text(
                    text = "${devices.size} devices",
                    style = CookbookTypography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }

        when {
            isLoading -> {
                LoadingState(modifier = Modifier.fillMaxWidth())
            }
            error != null -> {
                ErrorState(
                    message = error,
                    onRetry = { /* Retry loading devices */ }
                )
            }
            devices.isEmpty() -> {
                EmptyState(
                    icon = Icons.Default.DeviceHub,
                    title = "No Devices",
                    description = "No other devices are connected to your cookbook"
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(devices, key = { it.deviceId }) { device ->
                        DeviceItemCard(
                            device = device,
                            onClick = { onDeviceClick(device.deviceId) },
                            onForceSync = { onForceSyncWithDevice(device.deviceId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DeviceItemCard(
    device: DeviceSyncInfo,
    onClick: () -> Unit,
    onForceSync: () -> Unit
) {
    CookbookCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Device",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = device.deviceName,
                            style = CookbookTypography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Text(
                        text = "ID: ${device.deviceId.take(8)}...",
                        style = CookbookTypography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    
                    Text(
                        text = "Last seen: ${device.formattedLastSeen}",
                        style = CookbookTypography.labelSmall
                    )
                }
                
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "View Device",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Last sync: ${device.formattedLastSync}",
                        style = CookbookTypography.labelSmall
                    )
                    
                    if (device.hasPendingChanges) {
                        Text(
                            text = "Pending: ${device.pendingChanges} changes",
                            style = CookbookTypography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    
                    if (device.hasConflicts) {
                        Text(
                            text = "Conflicts: ${device.conflictCount}",
                            style = CookbookTypography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                
                DeviceStatusIndicator(device = device)
            }
            
            // Force sync button
            OutlinedButton(
                onClick = { onForceSync() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
            ) {
                Text("Force Sync", style = CookbookTypography.labelSmall)
            }
        }
    }
}

@Composable
fun DeviceStatusIndicator(device: DeviceSyncInfo) {
    val (icon, color, text) = when {
        !device.isOnline -> Triple(Icons.Default.Close, MaterialTheme.colorScheme.error, "Offline")
        device.syncStatus == SyncStatusDisplay.SYNCING -> Triple(Icons.Default.Sync, MaterialTheme.colorScheme.primary, "Syncing")
        device.syncStatus == SyncStatusDisplay.FAILURE -> Triple(Icons.Default.Error, MaterialTheme.colorScheme.error, "Error")
        device.hasConflicts -> Triple(Icons.Default.Warning, MaterialTheme.colorScheme.error, "Conflicts")
        device.hasPendingChanges -> Triple(Icons.Default.Cloud, MaterialTheme.colorScheme.secondary, "Pending")
        else -> Triple(Icons.Default.CheckCircle, MaterialTheme.colorScheme.primary, "Synced")
    }
    
    Row(
        modifier = Modifier
            .background(
                color = color.copy(alpha = 0.2f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            modifier = Modifier.size(16.dp),
            tint = color
        )
        Text(
            text = text,
            style = CookbookTypography.labelSmall,
            color = color
        )
    }
}

@Composable
fun ErrorHandlingSection(
    errors: List<com.ourcookbook.ui.viewmodel.SyncErrorInfo>,
    onClearErrors: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Sync Errors (${errors.size})",
                style = CookbookTypography.titleMedium,
                color = MaterialTheme.colorScheme.error
            )
            
            OutlinedButton(
                onClick = onClearErrors,
                modifier = Modifier.height(32.dp)
            ) {
                Text("Clear All", style = CookbookTypography.labelSmall)
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(errors, key = { it.id }) { error ->
                ErrorItemCard(error = error)
            }
        }
    }
}

@Composable
fun ErrorItemCard(error: com.ourcookbook.ui.viewmodel.SyncErrorInfo) {
    CookbookCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = error.message,
                        style = CookbookTypography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Text(
                        text = "Category: ${error.category}",
                        style = CookbookTypography.bodySmall
                    )
                    
                    Text(
                        text = error.timestamp.toString().substring(0, 19).replace("T", " "),
                        style = CookbookTypography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                
                ErrorCategoryIcon(category = error.category)
            }
            
            if (!error.isResolved) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "Unresolved",
                        style = CookbookTypography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun ErrorCategoryIcon(category: com.ourcookbook.ui.screens.sync.SyncErrorCategory) {
    val (icon, color) = when (category) {
        com.ourcookbook.ui.screens.sync.SyncErrorCategory.NETWORK -> Pair(Icons.Default.NetworkCheck, MaterialTheme.colorScheme.error)
        com.ourcookbook.ui.screens.sync.SyncErrorCategory.PERMISSION -> Pair(Icons.Default.Close, MaterialTheme.colorScheme.error)
        com.ourcookbook.ui.screens.sync.SyncErrorCategory.CONFLICT -> Pair(Icons.Default.SyncProblem, MaterialTheme.colorScheme.error)
        com.ourcookbook.ui.screens.sync.SyncErrorCategory.STORAGE -> Pair(Icons.Default.DeviceHub, MaterialTheme.colorScheme.error)
        com.ourcookbook.ui.screens.sync.SyncErrorCategory.UNKNOWN -> Pair(Icons.Default.Error, MaterialTheme.colorScheme.error)
    }
    
    Icon(
        imageVector = icon,
        contentDescription = category.name,
        modifier = Modifier.size(24.dp),
        tint = color
    )
}

@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = "Error",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        
        Text(
            text = "Sync Error",
            style = CookbookTypography.headlineSmall,
            color = MaterialTheme.colorScheme.error
        )
        
        Text(
            text = message,
            style = CookbookTypography.bodyMedium
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        CookbookPrimaryButton(
            text = "Retry",
            onClick = onRetry
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SyncStatusScreenPreview() {
    CookbookTheme {
        SyncStatusScreen(
            viewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
            navController = rememberNavController()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SyncStatusOverviewCardPreview() {
    CookbookTheme {
        Surface {
            SyncStatusOverviewCard(
                state = SyncStatusState(
                    syncStatus = SyncStatusDisplay.SUCCESS,
                    lastSyncTimestamp = java.time.Instant.now(),
                    pendingLocalChanges = 0,
                    pendingRemoteChanges = 0,
                    isSyncing = false
                ),
                onStartFullSync = {},
                onStartPullSync = {},
                onStartPushSync = {},
                onCancelSync = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SyncHistoryItemCardPreview() {
    CookbookTheme {
        Surface {
            SyncHistoryItemCard(
                item = SyncHistoryItem(
                    timestamp = java.time.Instant.now(),
                    status = SyncStatusDisplay.SUCCESS,
                    direction = com.ourcookbook.ui.screens.sync.SyncDirection.BOTH,
                    changesSynchronized = 42,
                    conflicts = 0,
                    durationMs = 1500,
                    deviceName = "My Device"
                ),
                onRetry = {},
                onDetailsClick = {},
                showDivider = false
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ConflictItemCardPreview() {
    CookbookTheme {
        Surface {
            ConflictItemCard(
                conflict = ConflictSummary(
                    conflictId = "conflict-123",
                    recipeName = "Spaghetti Carbonara",
                    conflictType = "Version Conflict",
                    detectedAt = java.time.Instant.now(),
                    status = "PENDING",
                    localVersion = "v1",
                    remoteVersion = "v2"
                ),
                onClick = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DeviceItemCardPreview() {
    CookbookTheme {
        Surface {
            DeviceItemCard(
                device = DeviceSyncInfo(
                    deviceId = "device-123",
                    deviceName = "My Phone",
                    lastSeen = java.time.Instant.now(),
                    syncStatus = SyncStatusDisplay.SUCCESS,
                    lastSyncTimestamp = java.time.Instant.now(),
                    pendingChanges = 0,
                    conflictCount = 0,
                    isOnline = true
                ),
                onClick = {},
                onForceSync = {}
            )
        }
    }
}