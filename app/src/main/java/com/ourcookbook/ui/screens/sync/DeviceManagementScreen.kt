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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.DeviceHub
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ourcookbook.ui.components.CookbookCard
import com.ourcookbook.ui.components.EmptyState
import com.ourcookbook.ui.components.LoadingState
import com.ourcookbook.ui.theme.CookbookTheme
import com.ourcookbook.ui.theme.CookbookTypography

/**
 * Device Management Screen
 * Task 2.1.06: Sync Status Screen Implementation
 * 
 * Displays list of connected devices and their sync status
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceManagementScreen(
    devices: List<DeviceSyncInfo>,
    isLoading: Boolean,
    error: String?,
    navController: NavController,
    onDeviceClick: (String) -> Unit,
    onForceSync: (String) -> Unit,
    onRefresh: () -> Unit
) {
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Device Management", style = CookbookTypography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                    IconButton(onClick = { /* Show menu */ }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Menu"
                        )
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                label = { Text("Search devices") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                },
                singleLine = true
            )

            when {
                isLoading -> {
                    LoadingState(modifier = Modifier.weight(1f))
                }
                error != null -> {
                    ErrorState(
                        message = error,
                        onRetry = onRefresh
                    )
                }
                else -> {
                    DeviceListContent(
                        devices = devices,
                        searchQuery = searchQuery.text,
                        onDeviceClick = onDeviceClick,
                        onForceSync = onForceSync,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun DeviceListContent(
    devices: List<DeviceSyncInfo>,
    searchQuery: String,
    onDeviceClick: (String) -> Unit,
    onForceSync: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredDevices = if (searchQuery.isBlank()) {
        devices
    } else {
        devices.filter {
            it.deviceName.contains(searchQuery, ignoreCase = true) ||
            it.deviceId.contains(searchQuery, ignoreCase = true)
        }
    }

    if (filteredDevices.isEmpty()) {
        EmptyState(
            icon = Icons.Default.DeviceHub,
            title = if (searchQuery.isBlank()) "No Devices" else "No Devices Found",
            description = if (searchQuery.isBlank()) 
                "No devices are connected to your cookbook" 
            else 
                "No devices match your search query"
        )
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredDevices, key = { it.deviceId }) { device ->
                DeviceManagementItem(
                    device = device,
                    onClick = { onDeviceClick(device.deviceId) },
                    onForceSync = { onForceSync(device.deviceId) }
                )
            }
        }
    }
}

@Composable
fun DeviceManagementItem(
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
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                        )
                    }
                    
                    Text(
                        text = "ID: ${device.deviceId.take(8)}...",
                        style = CookbookTypography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
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
                        text = "Last seen: ${device.formattedLastSeen}",
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


@Preview(showBackground = true)
@Composable
fun DeviceManagementScreenPreview() {
    CookbookTheme {
        DeviceManagementScreen(
            devices = listOf(
                DeviceSyncInfo(
                    deviceId = "device-123",
                    deviceName = "My Phone",
                    lastSeen = java.time.Instant.now(),
                    syncStatus = SyncStatusDisplay.SUCCESS,
                    lastSyncTimestamp = java.time.Instant.now(),
                    pendingChanges = 0,
                    conflictCount = 0,
                    isOnline = true
                ),
                DeviceSyncInfo(
                    deviceId = "device-456",
                    deviceName = "My Tablet",
                    lastSeen = java.time.Instant.now().minusSeconds(3600),
                    syncStatus = SyncStatusDisplay.PARTIAL,
                    lastSyncTimestamp = java.time.Instant.now().minusSeconds(3600),
                    pendingChanges = 5,
                    conflictCount = 1,
                    isOnline = true
                )
            ),
            isLoading = false,
            error = null,
            navController = rememberNavController(),
            onDeviceClick = {},
            onForceSync = {},
            onRefresh = {}
        )
    }
}