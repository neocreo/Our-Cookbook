package com.ourcookbook.ui.screens.sync

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ourcookbook.ui.components.CookbookPrimaryButton
import com.ourcookbook.ui.components.EmptyState
import com.ourcookbook.ui.components.LoadingState
import com.ourcookbook.ui.navigation.Route
import com.ourcookbook.ui.theme.CookbookTheme
import com.ourcookbook.ui.viewmodel.SyncEvent
import com.ourcookbook.ui.viewmodel.SyncState
import com.ourcookbook.ui.viewmodel.SyncViewModel

/**
 * Sync Status Screen
 * Task 1.9: Complete Navigation Setup
 * 
 * Displays sync status and handles sync operations
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncStatusScreen(
    viewModel: SyncViewModel,
    navController: NavController
) {
    val state by viewModel.state.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.handleEvent(SyncEvent.LoadSyncStatus)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sync Status") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.handleEvent(SyncEvent.Refresh) }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                }
            )
        }
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
                        onRetry = { viewModel.handleEvent(SyncEvent.LoadSyncStatus) }
                    )
                }
                else -> {
                    SyncStatusContent(
                        state = state,
                        onStartSync = { viewModel.handleEvent(SyncEvent.StartSync) },
                        onStopSync = { viewModel.handleEvent(SyncEvent.StopSync) },
                        onConflictClick = { conflictId ->
                            navController.navigate(Route.conflictResolution(conflictId))
                        },
                        onResolveAllConflicts = { 
                            viewModel.handleEvent(SyncEvent.ResolveAllConflicts) 
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun SyncStatusContent(
    state: SyncState,
    onStartSync: () -> Unit,
    onStopSync: () -> Unit,
    onConflictClick: (String) -> Unit,
    onResolveAllConflicts: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Sync status overview
        item {
            SyncStatusOverview(
                state = state,
                onStartSync = onStartSync,
                onStopSync = onStopSync
            )
        }
        
        // Sync progress (if syncing)
        if (state.isSyncing) {
            item {
                SyncProgress(
                    progress = state.syncProgress,
                    message = state.syncMessage ?: "Syncing..."
                )
            }
        }
        
        // Last sync information
        item {
            LastSyncInfo(state = state)
        }
        
        // Conflicts section
        if (state.hasConflicts) {
            item {
                ConflictsSection(
                    conflicts = state.conflicts,
                    onConflictClick = onConflictClick,
                    onResolveAllConflicts = onResolveAllConflicts
                )
            }
        }
        
        // Sync metadata
        item {
            SyncMetadata(state = state)
        }
    }
}

@Composable
fun SyncStatusOverview(
    state: SyncState,
    onStartSync: () -> Unit,
    onStopSync: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when {
            state.isSyncing -> {
                Icon(
                    imageVector = Icons.Default.Sync,
                    contentDescription = "Syncing",
                    modifier = Modifier.height(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Sync in Progress",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = "Syncing your recipes with Google Drive",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                
                CookbookPrimaryButton(
                    text = "Cancel Sync",
                    onClick = onStopSync
                )
            }
            state.pendingConflicts > 0 -> {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Conflicts",
                    modifier = Modifier.height(64.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Text(
                    text = "Conflicts Detected",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    text = "${state.pendingConflicts} conflicts need resolution",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                CookbookPrimaryButton(
                    text = "Start Sync",
                    onClick = onStartSync
                )
            }
            else -> {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Synced",
                    modifier = Modifier.height(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Up to Date",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = "All recipes are synced",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                
                CookbookPrimaryButton(
                    text = "Sync Now",
                    onClick = onStartSync
                )
            }
        }
    }
}

@Composable
fun SyncProgress(
    progress: Int,
    message: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium
        )
        
        LinearProgressIndicator(
            progress = { progress / 100f },
            modifier = Modifier.fillMaxWidth()
        )
        
        Text(
            text = "$progress%",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.align(Alignment.End)
        )
    }
}

@Composable
fun LastSyncInfo(state: SyncState) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Last Sync",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = state.lastSyncFormatted,
            style = MaterialTheme.typography.bodyMedium
        )
        
        Text(
            text = "Status: ${state.syncStatus}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun ConflictsSection(
    conflicts: List<com.ourcookbook.domain.model.SyncConflict>,
    onConflictClick: (String) -> Unit,
    onResolveAllConflicts: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Sync Conflicts (${conflicts.size})",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error
            )
            
            CookbookPrimaryButton(
                text = "Resolve All",
                onClick = onResolveAllConflicts,
                modifier = Modifier.height(32.dp)
            )
        }
        
        if (conflicts.isEmpty()) {
            EmptyState(
                icon = Icons.Default.Warning,
                title = "No conflicts",
                description = "All sync conflicts have been resolved"
            )
        } else {
            items(conflicts, key = { it.id }) { conflict ->
                ConflictItem(
                    conflict = conflict,
                    onClick = { onConflictClick(conflict.id) }
                )
            }
        }
    }
}

@Composable
fun ConflictItem(
    conflict: com.ourcookbook.domain.model.SyncConflict,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Conflict #${conflict.id.take(8)}",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = "Detected: ${conflict.detectedAt}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            
            Text(
                text = "Status: ${conflict.status}",
                style = MaterialTheme.typography.bodySmall,
                color = when (conflict.status) {
                    com.ourcookbook.domain.model.ConflictStatus.PENDING -> MaterialTheme.colorScheme.error
                    com.ourcookbook.domain.model.ConflictStatus.RESOLVED -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
        }
        
        IconButton(onClick = onClick) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "View Conflict"
            )
        }
    }
}

@Composable
fun SyncMetadata(state: SyncState) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Sync Metadata",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        
        if (state.syncMetadata.isEmpty()) {
            Text(
                text = "No sync metadata available",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        } else {
            state.syncMetadata.forEach { metadata ->
                MetadataItem(metadata = metadata)
            }
        }
    }
}

@Composable
fun MetadataItem(metadata: com.ourcookbook.domain.model.SyncMetadata) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "Device: ${metadata.deviceId}",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "Last Sync: ${metadata.lastSyncTimestamp}",
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = "Sync Count: ${metadata.syncCount}",
            style = MaterialTheme.typography.bodySmall
        )
    }
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
            imageVector = Icons.Default.Close,
            contentDescription = "Error",
            modifier = Modifier.height(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        
        Text(
            text = "Sync Error",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.error
        )
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium
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
