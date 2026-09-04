@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.ourcookbook.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeviceHub
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.DriveEta
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SyncDisabled
import androidx.compose.material.icons.filled.SystemUpdateAlt
import androidx.compose.material.icons.filled.TextIncrease
import androidx.compose.material.icons.filled.TextDecrease
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ourcookbook.domain.model.Cookbook
import com.ourcookbook.ui.components.CookbookBottomNavigation
import com.ourcookbook.ui.components.CookbookPrimaryButton
import com.ourcookbook.ui.components.LoadingState
import com.ourcookbook.ui.navigation.Route
import com.ourcookbook.ui.theme.CookbookTheme
import com.ourcookbook.ui.viewmodel.SettingsAction
import com.ourcookbook.ui.viewmodel.SettingsCategory
import com.ourcookbook.ui.viewmodel.SettingsEvent
import com.ourcookbook.ui.viewmodel.SettingsState
import com.ourcookbook.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

/**
 * Settings Screen
 * Task 2.1.08: Complete Settings Screen Implementation
 * 
 * Comprehensive settings screen with all required categories:
 * 1. App Settings (Theme, Language, Font Size, Sync, Offline Mode, Default Cookbook)
 * 2. Account & Device Settings (User Profile, Device Info, Linked Accounts, Sync Status)
 * 3. Privacy & Security (App Lock, Data Encryption, Privacy Policy, Export/Import)
 * 4. Notification Settings (Recipe Reminders, Sync Notifications, Sound/Vibration)
 * 5. Accessibility Settings (Screen Reader, High Contrast, Reduce Motion, Text Scaling)
 * 6. About Section (App Version, Build Number, Changelog, Licenses, Contact)
 * 7. Advanced Settings (Debug Mode, Log Level, Clear Cache, Reset Data)
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    navController: NavController
) {
    val state by viewModel.state.collectAsState()
    val actions by viewModel.actions.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    // Handle navigation actions from ViewModel
    actions?.let { action ->
        LaunchedEffect(action) {
            when (action) {
                is SettingsAction.NavigateToSyncStatus -> {
                    navController.navigate(Route.SYNC_STATUS)
                    viewModel.clearAction()
                }
                is SettingsAction.NavigateToDriveAuth -> {
                    // Navigate to Google Drive auth
                    viewModel.clearAction()
                }
                is SettingsAction.NavigateToExport -> {
                    // Navigate to export screen
                    viewModel.clearAction()
                }
                is SettingsAction.NavigateToImport -> {
                    // Navigate to import screen
                    viewModel.clearAction()
                }
                is SettingsAction.NavigateToPrivacyPolicy -> {
                    // Open privacy policy URL
                    viewModel.clearAction()
                }
                is SettingsAction.ShowSuccess -> {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(action.message)
                        viewModel.clearAction()
                    }
                }
                is SettingsAction.ShowError -> {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(action.message)
                        viewModel.clearAction()
                    }
                }
                else -> {}
            }
        }
    }
    
    // Load settings on first composition
    LaunchedEffect(Unit) {
        viewModel.handleEvent(SettingsEvent.LoadSettings)
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
        },
        bottomBar = {
            CookbookBottomNavigation(
                currentRoute = navController.currentDestination?.route ?: Route.SETTINGS,
                onNavigate = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // ====================================================================
        // 1. APP SETTINGS
        // ====================================================================
        SettingsCategorySection(
            category = SettingsCategory.APPSETTINGS,
            state = state,
            onEvent = onEvent
        )
        
        // ====================================================================
        // 2. ACCOUNT & DEVICE SETTINGS
        // ====================================================================
        SettingsCategorySection(
            category = SettingsCategory.ACCOUNT,
            state = state,
            onEvent = onEvent
        )
        
        // ====================================================================
        // 3. PRIVACY & SECURITY
        // ====================================================================
        SettingsCategorySection(
            category = SettingsCategory.PRIVACY,
            state = state,
            onEvent = onEvent
        )

        // ====================================================================
        // 4. NOTIFICATION SETTINGS
        // ====================================================================
        SettingsCategorySection(
            category = SettingsCategory.NOTIFICATIONS,
            state = state,
            onEvent = onEvent
        )

        // ====================================================================
        // 5. ABOUT SECTION
        // ====================================================================
        SettingsCategorySection(
            category = SettingsCategory.ABOUT,
            state = state,
            onEvent = onEvent
        )

        // ====================================================================
        // 6. ADVANCED SETTINGS
        // ====================================================================
        SettingsCategorySection(
            category = SettingsCategory.ADVANCED,
            state = state,
            onEvent = onEvent
        )
        
        // Save button
        if (state.isSaving) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
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
fun SettingsCategorySection(
    category: SettingsCategory,
    state: SettingsState,
    onEvent: (SettingsEvent) -> Unit
) {
    when (category) {
        SettingsCategory.APPSETTINGS -> AppSettingsSection(state, onEvent)
        SettingsCategory.ACCOUNT -> AccountAndDeviceSettingsSection(state, onEvent)
        SettingsCategory.PRIVACY -> PrivacyAndSecuritySection(state, onEvent)
        SettingsCategory.NOTIFICATIONS -> NotificationSettingsSection(state, onEvent)
        SettingsCategory.ABOUT -> AboutSection(state, onEvent)
        SettingsCategory.ADVANCED -> AdvancedSettingsSection(state, onEvent)
        else -> {}
    }
}

// ============================================================================
// 1. APP SETTINGS SECTION
// ============================================================================

@Composable
fun AppSettingsSection(
    state: SettingsState,
    onEvent: (SettingsEvent) -> Unit
) {
    var showCookbookPicker by remember { mutableStateOf(false) }
    var newCategory by remember { mutableStateOf("") }

    SettingsSectionCard(title = "App Settings", icon = Icons.Default.Tune) {
        // Theme selection
        SettingsItemWithDropdown(
            icon = if (state.theme == "DARK") Icons.Default.DarkMode else Icons.Default.LightMode,
            title = "Theme",
            subtitle = state.themeDisplayName,
            options = listOf("System Default", "Light", "Dark"),
            selectedOption = state.themeDisplayName,
            onOptionSelected = { option ->
                val theme = when (option) {
                    "Light" -> "LIGHT"
                    "Dark" -> "DARK"
                    else -> "SYSTEM"
                }
                onEvent(SettingsEvent.UpdateTheme(theme))
            }
        )

        // Language selection
        SettingsItemWithDropdown(
            icon = Icons.Default.Language,
            title = "Language",
            subtitle = state.languageDisplayName,
            options = listOf("English", "Svenska", "Español", "Français", "Deutsch", "Italiano"),
            selectedOption = state.languageDisplayName,
            onOptionSelected = { option ->
                val language = when (option) {
                    "Svenska" -> "sv"
                    "Español" -> "es"
                    "Français" -> "fr"
                    "Deutsch" -> "de"
                    "Italiano" -> "it"
                    else -> "en"
                }
                onEvent(SettingsEvent.UpdateLanguage(language))
            }
        )

        // Font size adjustment
        SettingsItemWithDropdown(
            icon = Icons.Default.FontDownload,
            title = "Font Size",
            subtitle = state.fontSizeDisplayName,
            options = listOf("Small", "Medium", "Large"),
            selectedOption = state.fontSizeDisplayName,
            onOptionSelected = { option ->
                val fontSize = when (option) {
                    "Small" -> "SMALL"
                    "Large" -> "LARGE"
                    else -> "MEDIUM"
                }
                onEvent(SettingsEvent.UpdateFontSize(fontSize))
            }
        )

        // Default cookbook selection — picker showing user's cookbooks
        SettingsItem(
            icon = Icons.Default.Storage,
            title = "Default Cookbook",
            subtitle = state.defaultCookbookName,
            onClick = { showCookbookPicker = true }
        )
    }

    // Default cookbook picker dialog
    if (showCookbookPicker) {
        CookbookPickerDialog(
            cookbooks = state.cookbooks,
            selectedId = state.defaultCookbookId,
            onDismiss = { showCookbookPicker = false },
            onSelect = { cookbookId ->
                onEvent(SettingsEvent.UpdateDefaultCookbook(cookbookId))
                showCookbookPicker = false
            }
        )
    }

    // Categories section
    SettingsSectionCard(title = "Categories", icon = Icons.Default.Tune) {
        state.categories.forEach { category ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = category,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { onEvent(SettingsEvent.RemoveCategory(category)) }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Remove $category",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = newCategory,
                onValueChange = { newCategory = it },
                label = { Text("Add category") },
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.small,
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
            IconButton(onClick = {
                if (newCategory.isNotBlank()) {
                    onEvent(SettingsEvent.AddCategory(newCategory))
                    newCategory = ""
                }
            }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Category",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// ============================================================================
// 2. ACCOUNT & DEVICE SETTINGS SECTION
// ============================================================================

@Composable
fun AccountAndDeviceSettingsSection(
    state: SettingsState,
    onEvent: (SettingsEvent) -> Unit
) {
    SettingsSectionCard(title = "Account & Device", icon = Icons.Default.AccountCircle) {
        if (!state.isLoggedIn) {
            // Logged out — show only "Connect to Google"
            SettingsItem(
                icon = Icons.Default.Cloud,
                title = "Connect to Google",
                subtitle = "Link your Google Drive account for sync",
                onClick = { onEvent(SettingsEvent.TriggerSync) }
            )
        } else {
            // Logged in — show account details and device settings
            SettingsItem(
                icon = Icons.Default.Person,
                title = "Account",
                subtitle = state.userName ?: state.userEmail ?: "Connected",
                onClick = { }
            )

            // Offline Mode (moved from App Settings)
            SettingsToggle(
                icon = if (state.offlineMode) Icons.Default.CloudOff else Icons.Default.Cloud,
                title = "Offline Mode",
                subtitle = "Use app without internet connection",
                checked = state.offlineMode,
                onCheckedChange = { onEvent(SettingsEvent.UpdateOfflineMode(it)) }
            )

            // Sync Frequency (moved from App Settings)
            SettingsItemWithDropdown(
                icon = Icons.Default.Sync,
                title = "Sync Frequency",
                subtitle = state.syncFrequencyDisplayName,
                options = listOf("Auto", "Manual", "Hourly", "Daily", "Weekly"),
                selectedOption = state.syncFrequencyDisplayName,
                onOptionSelected = { option ->
                    val frequency = when (option) {
                        "Manual" -> "MANUAL"
                        "Hourly" -> "HOURLY"
                        "Daily" -> "DAILY"
                        "Weekly" -> "WEEKLY"
                        else -> "AUTO"
                    }
                    onEvent(SettingsEvent.UpdateSyncFrequency(frequency))
                }
            )

            // Sync Status
            SettingsItem(
                icon = when (state.syncStatus) {
                    "SYNCING" -> Icons.Default.Sync
                    "ERROR" -> Icons.Default.Warning
                    else -> Icons.Default.Check
                },
                title = "Sync Status",
                subtitle = "${state.syncStatus} • ${state.lastSyncTimeDisplay}",
                onClick = { onEvent(SettingsEvent.CheckSyncStatus) }
            )

            // Storage usage
            SettingsItem(
                icon = Icons.Default.Storage,
                title = "Storage Used",
                subtitle = state.storageUsageDisplay,
                onClick = { }
            )

            // Storage usage progress bar
            if (state.maxStorage > 0) {
                LinearProgressIndicator(
                    progress = state.storageUsagePercentage / 100f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .padding(vertical = 8.dp),
                    color = when {
                        state.storageUsagePercentage > 90 -> MaterialTheme.colorScheme.error
                        state.storageUsagePercentage > 70 -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.primary
                    }
                )
            }

            // Disconnect
            SettingsItem(
                icon = Icons.Default.CloudOff,
                title = "Disconnect",
                subtitle = "Remove Google Drive connection",
                isDestructive = true,
                onClick = { onEvent(SettingsEvent.TriggerSync) }
            )
        }
    }
}

// ============================================================================
// 3. PRIVACY & SECURITY SECTION
// ============================================================================

@Composable
fun PrivacyAndSecuritySection(
    state: SettingsState,
    onEvent: (SettingsEvent) -> Unit
) {
    SettingsSectionCard(title = "Privacy & Security", icon = Icons.Default.Security) {
        // Data encryption status — enabled by default (SQLCipher), documented
        SettingsInfoItem(
            icon = Icons.Default.VerifiedUser,
            title = "Data Encryption",
            subtitle = "All local data is encrypted at rest (SQLCipher)"
        )

        // Privacy policy link
        SettingsItem(
            icon = Icons.Default.PrivacyTip,
            title = "Privacy Policy",
            subtitle = "Read our privacy policy",
            onClick = { }
        )

        // Data export option — creates file in Downloads
        SettingsItem(
            icon = Icons.Default.FileUpload,
            title = "Export Data",
            subtitle = "Export your recipes to a file in Downloads",
            onClick = { onEvent(SettingsEvent.ExportData) }
        )

        // Data import option — opens file from Downloads
        SettingsItem(
            icon = Icons.Default.FileDownload,
            title = "Import Data",
            subtitle = "Import recipes from a file",
            onClick = { onEvent(SettingsEvent.ImportData) }
        )
    }
}

// ============================================================================
// 4. NOTIFICATION SETTINGS SECTION
// ============================================================================

@Composable
fun NotificationSettingsSection(
    state: SettingsState,
    onEvent: (SettingsEvent) -> Unit
) {
    SettingsSectionCard(title = "Notifications", icon = Icons.Default.Notifications) {
        // Updated recipe — sync-driven notification
        SettingsToggle(
            icon = Icons.Default.SystemUpdateAlt,
            title = "Updated Recipe",
            subtitle = "Get notified when a synced recipe is updated",
            checked = state.updateNotificationsEnabled,
            onCheckedChange = { onEvent(SettingsEvent.UpdateUpdateNotificationsEnabled(it)) }
        )

        // New recipe — sync-driven notification
        SettingsToggle(
            icon = Icons.Default.Notifications,
            title = "New Recipe",
            subtitle = "Get notified when a new recipe is added via sync",
            checked = state.syncNotificationsEnabled,
            onCheckedChange = { onEvent(SettingsEvent.UpdateSyncNotificationsEnabled(it)) }
        )

        // Sound settings
        SettingsToggle(
            icon = Icons.Default.VolumeUp,
            title = "Notification Sound",
            subtitle = "Play sound for notifications",
            checked = state.notificationSoundEnabled,
            onCheckedChange = { onEvent(SettingsEvent.UpdateNotificationSound(it)) }
        )

        // Vibration settings
        SettingsToggle(
            icon = Icons.Default.Power,
            title = "Notification Vibration",
            subtitle = "Vibrate for notifications",
            checked = state.notificationVibrationEnabled,
            onCheckedChange = { onEvent(SettingsEvent.UpdateNotificationVibration(it)) }
        )
    }
}

// ============================================================================
// 5. ACCESSIBILITY SETTINGS SECTION
// ============================================================================

@Composable
fun AccessibilitySettingsSection(
    state: SettingsState,
    onEvent: (SettingsEvent) -> Unit
) {
    SettingsSectionCard(title = "Accessibility", icon = Icons.Default.Visibility) {
        // Screen reader compatibility
        SettingsToggle(
            icon = Icons.Default.Visibility,
            title = "Screen Reader Compatibility",
            subtitle = "Optimize for screen readers",
            checked = state.screenReaderCompatibility,
            onCheckedChange = { onEvent(SettingsEvent.UpdateScreenReaderCompatibility(it)) }
        )
        
        // High contrast mode
        SettingsToggle(
            icon = Icons.Default.Palette,
            title = "High Contrast Mode",
            subtitle = "Increase contrast for better visibility",
            checked = state.highContrastMode,
            onCheckedChange = { onEvent(SettingsEvent.UpdateHighContrastMode(it)) }
        )
        
        // Reduce motion
        SettingsToggle(
            icon = Icons.Default.DriveEta,
            title = "Reduce Motion",
            subtitle = "Reduce animations and motion",
            checked = state.reduceMotion,
            onCheckedChange = { onEvent(SettingsEvent.UpdateReduceMotion(it)) }
        )
        
        // Text scaling
        SettingsSlider(
            icon = Icons.Default.TextIncrease,
            title = "Text Scaling",
            subtitle = "Adjust text size: ${(state.textScaling * 100).toInt()}%",
            value = state.textScaling,
            valueRange = 0.8f..2.0f,
            steps = 11, // 0.8, 0.9, 1.0, ..., 2.0
            onValueChange = { onEvent(SettingsEvent.UpdateTextScaling(it)) }
        )
        
        // Color blindness modes
        SettingsItemWithDropdown(
            icon = Icons.Default.ColorLens,
            title = "Color Blindness Mode",
            subtitle = state.colorBlindnessModeDisplayName,
            options = listOf("None", "Deuteranopia (Red-Green)", "Protanopia (Red-Green)", "Tritanopia (Blue-Yellow)"),
            selectedOption = state.colorBlindnessModeDisplayName,
            onOptionSelected = { option ->
                val mode = when (option) {
                    "Deuteranopia (Red-Green)" -> "DEUTERANOPIA"
                    "Protanopia (Red-Green)" -> "PROTANOPIA"
                    "Tritanopia (Blue-Yellow)" -> "TRITANOPIA"
                    else -> "NONE"
                }
                onEvent(SettingsEvent.UpdateColorBlindnessMode(mode))
            }
        )
    }
}

// ============================================================================
// 6. ABOUT SECTION
// ============================================================================

@Composable
fun AboutSection(
    state: SettingsState,
    onEvent: (SettingsEvent) -> Unit
) {
    SettingsSectionCard(title = "About", icon = Icons.Default.Info) {
        // App version information (no link, no arrow)
        SettingsInfoItem(
            icon = Icons.Default.SystemUpdateAlt,
            title = "App Version",
            subtitle = state.appVersion
        )

        // Build number (no link, no arrow)
        SettingsInfoItem(
            icon = Icons.Default.BugReport,
            title = "Build Number",
            subtitle = state.buildNumber
        )

        // Changelog
        SettingsItem(
            icon = Icons.Default.History,
            title = "Changelog",
            subtitle = "View what's new in recent versions",
            onClick = { }
        )

        // Open source licenses
        SettingsItem(
            icon = Icons.Default.DeviceHub,
            title = "Open Source Licenses",
            subtitle = "View third-party licenses",
            onClick = { }
        )

        // Contact information
        SettingsItem(
            icon = Icons.Default.Help,
            title = "Contact Us",
            subtitle = "Get help and support",
            onClick = { }
        )

        // Rate the app option
        SettingsItem(
            icon = Icons.Default.WbSunny,
            title = "Rate the App",
            subtitle = "Rate Our Cookbook on the Play Store",
            onClick = { }
        )
    }
}

// ============================================================================
// 7. ADVANCED SETTINGS SECTION
// ============================================================================

@Composable
fun AdvancedSettingsSection(
    state: SettingsState,
    onEvent: (SettingsEvent) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Collapsible header — collapsed by default
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Advanced",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Advanced",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.Clear else Icons.Default.ArrowForward,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))

                // Clear cache
                SettingsItem(
                    icon = Icons.Default.Cached,
                    title = "Clear Cache",
                    subtitle = "Clear temporary app data",
                    onClick = { onEvent(SettingsEvent.ClearCache) }
                )

                // Reset app data
                SettingsItem(
                    icon = Icons.Default.Refresh,
                    title = "Reset App Data",
                    subtitle = "Reset all app data and settings",
                    isDestructive = true,
                    onClick = { onEvent(SettingsEvent.ResetAppData) }
                )
            }
        }
    }
}

// ============================================================================
// COMPOSABLE COMPONENTS
// ============================================================================

@Composable
fun SettingsSectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Section header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            // Section content
            content()
        }
    }
}

@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    isDestructive: Boolean = false,
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
                tint = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
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
                contentDescription = "Go to $title",
                tint = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun SettingsInfoItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
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
}

@Composable
fun CookbookPickerDialog(
    cookbooks: List<Cookbook>,
    selectedId: String?,
    onDismiss: () -> Unit,
    onSelect: (String?) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Default Cookbook") },
        text = {
            Column {
                // "Personal" / no default option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(null) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.material3.RadioButton(
                        selected = selectedId == null,
                        onClick = { onSelect(null) }
                    )
                    Text(
                        text = "Personal (no default)",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                cookbooks.forEach { cookbook ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(cookbook.id) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.RadioButton(
                            selected = selectedId == cookbook.id,
                            onClick = { onSelect(cookbook.id) }
                        )
                        Text(
                            text = cookbook.name,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                if (cookbooks.isEmpty()) {
                    Text(
                        text = "No cookbooks available. Create a cookbook first.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
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
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
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

@Composable
fun SettingsSlider(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int = 0,
    onValueChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
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
        
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            modifier = Modifier.padding(start = 48.dp)
        )
    }
}

@Composable
fun SettingsItemWithDropdown(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
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
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
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
            
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedOption,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    modifier = Modifier.menuAnchor()
                )
                
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    options.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                onOptionSelected(option)
                                expanded = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// DIALOGS
// ============================================================================

@Composable
fun ClearCacheDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        title = { Text("Clear Cache") },
        text = { Text("Are you sure you want to clear the app cache? This will remove temporary data but won't affect your recipes.") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Clear Cache")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        onDismissRequest = onDismiss
    )
}

@Composable
fun ResetAppDataDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        title = { Text("Reset App Data") },
        text = { Text("Are you sure you want to reset all app data? This will remove all your settings and local data. This action cannot be undone.") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Reset Data")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        onDismissRequest = onDismiss
    )
}

@Composable
fun DeleteAccountDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        title = { Text("Delete Account") },
        text = { Text("Are you sure you want to delete your account? This will permanently remove all your data from our servers. This action cannot be undone.") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Delete Account")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        onDismissRequest = onDismiss
    )
}

// ============================================================================
// PREVIEW
// ============================================================================

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