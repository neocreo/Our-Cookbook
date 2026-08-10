package com.ourcookbook.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Export
import androidx.compose.material.icons.filled.Import
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TextIncrease
import androidx.compose.material.icons.filled.TextDecrease
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import com.ourcookbook.ui.components.LoadingState
import com.ourcookbook.ui.navigation.Route
import com.ourcookbook.ui.theme.CookbookTheme
import com.ourcookbook.ui.viewmodel.SettingsEvent
import com.ourcookbook.ui.viewmodel.SettingsState
import com.ourcookbook.ui.viewmodel.SettingsViewModel

/**
 * Settings Screen
 * Task 1.9: Complete Navigation Setup
 * 
 * Handles app settings and preferences management
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    navController: NavController
) {
    val state by viewModel.state.collectAsState()
    val actions by viewModel.actions.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.handleEvent(SettingsEvent.LoadSettings)
    }
    
    // Handle navigation actions from ViewModel
    actions?.let { action ->
        when (action) {
            is com.ourcookbook.ui.viewmodel.SettingsAction.NavigateToSyncStatus -> {
                navController.navigate(Route.SYNC_STATUS)
                viewModel.clearAction()
            }
            is com.ourcookbook.ui.viewmodel.SettingsAction.NavigateToDriveAuth -> {
                navController.navigate(Route.DRIVE_AUTH)
                viewModel.clearAction()
            }
            else -> {}
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            state.isLoading -> {
                LoadingState()
            }
            else -> {
                SettingsContent(
                    state = state,
                    onEvent = { event -> viewModel.handleEvent(event) },
                    onNavigateToSyncStatus = { navController.navigate(Route.SYNC_STATUS) },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
fun SettingsContent(
    state: SettingsState,
    onEvent: (SettingsEvent) -> Unit,
    onNavigateToSyncStatus: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Appearance section
        SettingsSection(title = "Appearance") {
            // Theme setting
            SettingsItem(
                icon = if (state.theme == "DARK") Icons.Default.DarkMode else Icons.Default.LightMode,
                title = "Theme",
                subtitle = state.theme.replaceFirstChar { it.uppercase() },
                onClick = { onEvent(SettingsEvent.UpdateTheme(if (state.theme == "DARK") "LIGHT" else "DARK")) }
            )
            
            // Font size setting
            SettingsItem(
                icon = Icons.Default.TextIncrease,
                title = "Font Size",
                subtitle = state.fontSize.replaceFirstChar { it.uppercase() },
                onClick = { 
                    val sizes = listOf("SMALL", "MEDIUM", "LARGE")
                    val currentIndex = sizes.indexOf(state.fontSize)
                    val nextIndex = (currentIndex + 1) % sizes.size
                    onEvent(SettingsEvent.UpdateFontSize(sizes[nextIndex]))
                }
            )
        }
        
        // Sync section
        SettingsSection(title = "Sync") {
            SettingsItem(
                icon = Icons.Default.Sync,
                title = "Sync Status",
                subtitle = state.syncStatus,
                onClick = onNavigateToSyncStatus
            )
            
            SettingsItem(
                icon = Icons.Default.Cloud,
                title = "Google Drive Sync",
                subtitle = "Connect to Google Drive",
                onClick = { onEvent(SettingsEvent.TriggerSync) }
            )
            
            SettingsItem(
                icon = Icons.Default.Sync,
                title = "Sync Frequency",
                subtitle = state.syncFrequency.replaceFirstChar { it.uppercase() },
                onClick = { 
                    val frequencies = listOf("AUTO", "MANUAL", "DAILY", "WEEKLY")
                    val currentIndex = frequencies.indexOf(state.syncFrequency)
                    val nextIndex = (currentIndex + 1) % frequencies.size
                    onEvent(SettingsEvent.UpdateSyncFrequency(frequencies[nextIndex]))
                }
            )
            
            // Offline mode toggle
            SettingsToggle(
                icon = Icons.Default.Cloud,
                title = "Offline Mode",
                subtitle = "Use app without internet connection",
                checked = state.offlineMode,
                onCheckedChange = { onEvent(SettingsEvent.UpdateOfflineMode(it)) }
            )
        }
        
        // Notifications section
        SettingsSection(title = "Notifications") {
            SettingsToggle(
                icon = Icons.Default.Notifications,
                title = "Notifications",
                subtitle = "Receive recipe notifications",
                checked = state.notificationsEnabled,
                onCheckedChange = { onEvent(SettingsEvent.UpdateNotificationsEnabled(it)) }
            )
        }
        
        // Data section
        SettingsSection(title = "Data") {
            SettingsItem(
                icon = Icons.Default.Export,
                title = "Export Data",
                subtitle = "Export your recipes to a file",
                onClick = { onEvent(SettingsEvent.ExportData) }
            )
            
            SettingsItem(
                icon = Icons.Default.Import,
                title = "Import Data",
                subtitle = "Import recipes from a file",
                onClick = { onEvent(SettingsEvent.ImportData) }
            )
            
            SettingsItem(
                icon = Icons.Default.Clear,
                title = "Clear Cache",
                subtitle = "Clear temporary app data",
                onClick = { onEvent(SettingsEvent.ClearCache) }
            )
        }
        
        // About section
        SettingsSection(title = "About") {
            SettingsItem(
                icon = Icons.Default.Check,
                title = "App Version",
                subtitle = state.appVersion,
                onClick = { /* No action */ }
            )
            
            SettingsItem(
                icon = Icons.Default.Cloud,
                title = "Device ID",
                subtitle = state.deviceId,
                onClick = { /* No action */ }
            )
        }
        
        // Save button
        if (state.isSaving) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                androidx.compose.material3.CircularProgressIndicator()
            }
        } else {
            CookbookPrimaryButton(
                text = "Save Settings",
                onClick = { onEvent(SettingsEvent.SaveSettings) },
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        // Success message
        if (state.saveSuccess) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Saved",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Settings saved successfully!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        
        content()
    }
}

@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary
            )
            
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
        
        IconButton(onClick = onClick) {
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Go to $title"
            )
        }
    }
}

@Composable
fun SettingsToggle(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary
            )
            
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    CookbookTheme {
        SettingsScreen(
            viewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
            navController = rememberNavController()
        )
    }
}
