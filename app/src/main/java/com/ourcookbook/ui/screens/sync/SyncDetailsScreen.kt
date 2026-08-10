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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
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
import com.ourcookbook.ui.components.LoadingState
import com.ourcookbook.ui.theme.CookbookTheme
import com.ourcookbook.ui.theme.CookbookTypography

/**
 * Sync Details Screen
 * Task 2.1.06: Sync Status Screen Implementation
 * 
 * Displays detailed information about a specific sync operation
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncDetailsScreen(
    syncItem: SyncHistoryItem,
    navController: NavController,
    onRetry: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sync Details", style = CookbookTypography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
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
            // Sync Overview
            SyncDetailsOverview(syncItem = syncItem)
            
            // Sync Metadata
            SyncDetailsMetadata(syncItem = syncItem)
            
            // Sync Results
            SyncDetailsResults(syncItem = syncItem)
            
            // Error Information (if applicable)
            if (syncItem.hasErrors) {
                SyncDetailsErrorInfo(syncItem = syncItem, onRetry = onRetry)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun SyncDetailsOverview(syncItem: SyncHistoryItem) {
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
            // Status Icon
            when (syncItem.status) {
                SyncStatusDisplay.SUCCESS -> {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Success",
                        modifier = Modifier.height(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                SyncStatusDisplay.FAILURE -> {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Failed",
                        modifier = Modifier.height(64.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
                SyncStatusDisplay.PARTIAL -> {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Partial",
                        modifier = Modifier.height(64.dp),
                        tint = MaterialTheme.colorScheme.warning
                    )
                }
                SyncStatusDisplay.CANCELLED -> {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancelled",
                        modifier = Modifier.height(64.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                SyncStatusDisplay.SYNCING -> {
                    // This shouldn't happen for completed syncs
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Syncing",
                        modifier = Modifier.height(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Status Text
            Text(
                text = syncItem.status.name,
                style = CookbookTypography.headlineSmall,
                color = when (syncItem.status) {
                    SyncStatusDisplay.SUCCESS -> MaterialTheme.colorScheme.primary
                    SyncStatusDisplay.FAILURE -> MaterialTheme.colorScheme.error
                    SyncStatusDisplay.PARTIAL -> MaterialTheme.colorScheme.warning
                    SyncStatusDisplay.CANCELLED -> MaterialTheme.colorScheme.onSurface
                    SyncStatusDisplay.SYNCING -> MaterialTheme.colorScheme.primary
                }
            )

            // Timestamp
            Text(
                text = "Completed: ${syncItem.formattedTimestamp}",
                style = CookbookTypography.bodyMedium
            )

            // Direction
            SyncDirectionBadge(direction = syncItem.direction)
        }
    }
}

@Composable
fun SyncDetailsMetadata(syncItem: SyncHistoryItem) {
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
                text = "Sync Metadata",
                style = CookbookTypography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            DetailRow(
                label = "Sync ID",
                value = syncItem.id.take(8) + "..."
            )

            DetailRow(
                label = "Device",
                value = syncItem.deviceName.ifEmpty { "Unknown Device" }
            )

            DetailRow(
                label = "Device ID",
                value = syncItem.deviceId.take(8) + "..."
            )

            DetailRow(
                label = "Duration",
                value = syncItem.formattedDuration
            )
        }
    }
}

@Composable
fun SyncDetailsResults(syncItem: SyncHistoryItem) {
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
                text = "Sync Results",
                style = CookbookTypography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            DetailRow(
                label = "Items Synchronized",
                value = syncItem.changesSynchronized.toString()
            )

            DetailRow(
                label = "Conflicts Detected",
                value = syncItem.conflicts.toString()
            )

            DetailRow(
                label = "Sync Direction",
                value = when (syncItem.direction) {
                    SyncDirection.PULL -> "Pull (Download from Google Drive)"
                    SyncDirection.PUSH -> "Push (Upload to Google Drive)"
                    SyncDirection.BOTH -> "Full Sync (Pull + Push)"
                }
            )

            DetailRow(
                label = "Status",
                value = when (syncItem.status) {
                    SyncStatusDisplay.SUCCESS -> "Completed Successfully"
                    SyncStatusDisplay.FAILURE -> "Failed"
                    SyncStatusDisplay.PARTIAL -> "Partial Success"
                    SyncStatusDisplay.CANCELLED -> "Cancelled"
                    SyncStatusDisplay.SYNCING -> "In Progress"
                }
            )
        }
    }
}

@Composable
fun SyncDetailsErrorInfo(syncItem: SyncHistoryItem, onRetry: () -> Unit) {
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
                text = "Error Information",
                style = CookbookTypography.titleMedium,
                color = MaterialTheme.colorScheme.error
            )

            DetailRow(
                label = "Error Category",
                value = syncItem.errorCategory?.name ?: "Unknown"
            )

            DetailRow(
                label = "Error Message",
                value = syncItem.errorMessage ?: "No error message available"
            )

            if (syncItem.errorCategory != null) {
                ErrorCategorySuggestion(category = syncItem.errorCategory)
            }

            Spacer(modifier = Modifier.height(8.dp))

            CookbookPrimaryButton(
                text = "Retry Sync",
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = CookbookTypography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Text(
            text = value,
            style = CookbookTypography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ErrorCategorySuggestion(category: SyncErrorCategory) {
    val (title, description, solution) = when (category) {
        SyncErrorCategory.NETWORK -> Triple(
            "Network Error",
            "Unable to connect to Google Drive",
            "Check your internet connection and try again"
        )
        SyncErrorCategory.PERMISSION -> Triple(
            "Permission Error", 
            "Access to Google Drive denied",
            "Grant permission to access Google Drive in settings"
        )
        SyncErrorCategory.CONFLICT -> Triple(
            "Conflict Error",
            "Data conflicts detected",
            "Resolve conflicts in the Conflict Resolution screen"
        )
        SyncErrorCategory.STORAGE -> Triple(
            "Storage Error",
            "Insufficient storage space",
            "Free up space on your device or Google Drive"
        )
        SyncErrorCategory.UNKNOWN -> Triple(
            "Unknown Error",
            "An unexpected error occurred",
            "Try again or contact support"
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = CookbookTypography.titleSmall,
            color = MaterialTheme.colorScheme.error
        )
        Text(
            text = description,
            style = CookbookTypography.bodySmall
        )
        Text(
            text = "Suggested Solution: $solution",
            style = CookbookTypography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SyncDetailsScreenPreview() {
    CookbookTheme {
        SyncDetailsScreen(
            syncItem = SyncHistoryItem(
                timestamp = java.time.Instant.now(),
                status = SyncStatusDisplay.SUCCESS,
                direction = SyncDirection.BOTH,
                changesSynchronized = 42,
                conflicts = 2,
                durationMs = 1500,
                deviceId = "device-123",
                deviceName = "My Phone",
                errorMessage = null,
                errorCategory = null
            ),
            navController = rememberNavController(),
            onRetry = {}
        )
    }
}