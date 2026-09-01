package com.ourcookbook.ui.screens.sync

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DeviceHub
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ourcookbook.ui.components.CookbookCard
import com.ourcookbook.ui.components.CookbookPrimaryButton
import com.ourcookbook.ui.components.CookbookSecondaryButton
import com.ourcookbook.ui.theme.CookbookTheme
import com.ourcookbook.ui.theme.CookbookTypography

/**
 * Device Detail Screen
 * Task 2.1.06: Sync Status Screen Implementation
 * 
 * Displays detailed information about a specific device and its sync status
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceDetailScreen(
    device: DeviceSyncInfo,
    navController: NavController,
    onForceSync: () -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(device.deviceName, style = CookbookTypography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
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
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Device Overview
            DeviceDetailOverview(device = device)
            
            // Device Information
            DeviceDetailInformation(device = device)
            
            // Sync Status
            DeviceDetailSyncStatus(device = device)
            
            // Sync History for this device
            DeviceDetailSyncHistory(device = device)
            
            // Actions
            DeviceDetailActions(
                onForceSync = onForceSync,
                onBackClick = onBackClick
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun DeviceDetailOverview(device: DeviceSyncInfo) {
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
            // Device Icon
            Icon(
                imageVector = Icons.Default.Phone,
                contentDescription = "Device",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            // Device Name
            Text(
                text = device.deviceName,
                style = CookbookTypography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            // Device ID
            Text(
                text = "ID: ${device.deviceId.take(8)}...",
                style = CookbookTypography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            // Overall Status
            DeviceStatusIndicator(device = device)
        }
    }
}

@Composable
fun DeviceDetailInformation(device: DeviceSyncInfo) {
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
                text = "Device Information",
                style = CookbookTypography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            DetailRow(
                label = "Device Name",
                value = device.deviceName
            )

            DetailRow(
                label = "Device ID",
                value = device.deviceId
            )

            DetailRow(
                label = "Last Seen",
                value = device.formattedLastSeen
            )

            DetailRow(
                label = "Online Status",
                value = if (device.isOnline) "Online" else "Offline"
            )

            if (device.syncCapabilities.isNotEmpty()) {
                DetailRow(
                    label = "Capabilities",
                    value = device.syncCapabilities.joinToString(", ")
                )
            }
        }
    }
}

@Composable
fun DeviceDetailSyncStatus(device: DeviceSyncInfo) {
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
                text = "Sync Status",
                style = CookbookTypography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            DetailRow(
                label = "Last Sync",
                value = device.formattedLastSync
            )

            DetailRow(
                label = "Sync Status",
                value = when (device.syncStatus) {
                    SyncStatusDisplay.SUCCESS -> "Synced"
                    SyncStatusDisplay.FAILURE -> "Error"
                    SyncStatusDisplay.PARTIAL -> "Partial Sync"
                    SyncStatusDisplay.CANCELLED -> "Cancelled"
                    SyncStatusDisplay.SYNCING -> "Syncing"
                }
            )

            DetailRow(
                label = "Pending Changes",
                value = device.pendingChanges.toString()
            )

            DetailRow(
                label = "Conflicts",
                value = device.conflictCount.toString()
            )

            // Status indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (device.hasPendingChanges) {
                    StatusBadge(
                        text = "${device.pendingChanges} Pending",
                        color = MaterialTheme.colorScheme.error
                    )
                }
                
                if (device.hasConflicts) {
                    StatusBadge(
                        text = "${device.conflictCount} Conflicts",
                        color = MaterialTheme.colorScheme.error
                    )
                }
                
                if (!device.isOnline) {
                    StatusBadge(
                        text = "Offline",
                        color = MaterialTheme.colorScheme.error
                    )
                }
                
                if (device.syncStatus == SyncStatusDisplay.SUCCESS && !device.hasPendingChanges && !device.hasConflicts) {
                    StatusBadge(
                        text = "Up to Date",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun DeviceDetailSyncHistory(device: DeviceSyncInfo) {
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
                text = "Sync History",
                style = CookbookTypography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            // This would show the sync history for this specific device
            // For now, show placeholder
            Text(
                text = "Sync history for this device will be displayed here",
                style = CookbookTypography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun DeviceDetailActions(
    onForceSync: () -> Unit,
    onBackClick: () -> Unit
) {
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Actions",
                style = CookbookTypography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            CookbookPrimaryButton(
                text = "Force Sync with Device",
                onClick = onForceSync,
                modifier = Modifier.fillMaxWidth()
            )

            CookbookSecondaryButton(
                text = "View All Devices",
                onClick = onBackClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun StatusBadge(text: String, color: Color) {
    androidx.compose.material3.Surface(
        modifier = Modifier.padding(4.dp),
        shape = MaterialTheme.shapes.small,
        color = color.copy(alpha = 0.2f)
    ) {
        Text(
            text = text,
            style = CookbookTypography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DeviceDetailScreenPreview() {
    CookbookTheme {
        DeviceDetailScreen(
            device = DeviceSyncInfo(
                deviceId = "device-123",
                deviceName = "My Phone",
                lastSeen = java.time.Instant.now(),
                syncStatus = SyncStatusDisplay.SUCCESS,
                lastSyncTimestamp = java.time.Instant.now(),
                pendingChanges = 0,
                conflictCount = 0,
                syncCapabilities = setOf("INTERNET", "TOUCHSCREEN"),
                isOnline = true
            ),
            navController = rememberNavController(),
            onForceSync = {},
            onBackClick = {}
        )
    }
}