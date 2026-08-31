package com.ourcookbook.ui.screens.chromebook

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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mouse
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tablet
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.ourcookbook.domain.usecase.chromebook.ChromebookConfig
import com.ourcookbook.domain.usecase.chromebook.ChromebookOptimizations
import com.ourcookbook.domain.usecase.chromebook.KeyboardShortcut
import com.ourcookbook.ui.components.AppTopAppBar
import com.ourcookbook.ui.theme.CookbookTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Chromebook Settings Screen
 * 
 * Allows users to configure Chromebook-specific settings:
 * - Keyboard shortcuts
 * - Multi-window behavior
 * - Input preferences
 * - Display options
 */
@Composable
fun ChromebookSettingsScreen(
    onBackClick: () -> Unit,
    viewModel: ChromebookSettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val config by viewModel.chromebookConfig.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    var showShortcutsDialog by remember { mutableStateOf(false) }
    var showWindowSettings by remember { mutableStateOf(false) }
    var showInputSettings by remember { mutableStateOf(false) }
    var selectedShortcut by remember { mutableStateOf<KeyboardShortcut?>(null) }
    
    Scaffold(
        topBar = {
            AppTopAppBar(
                title = "Chromebook Settings",
                navigationIcon = Icons.Default.ArrowBack,
                onNavigationClick = onBackClick
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Device Information Section
            DeviceInfoSection(config = config)
            
            Divider()
            
            // Layout Settings Section
            LayoutSettingsSection(
                config = config,
                onMultiPaneChange = { viewModel.setMultiPaneEnabled(it) },
                onExpandedLayoutChange = { viewModel.setExpandedLayoutEnabled(it) }
            )
            
            Divider()
            
            // Input Settings Section
            InputSettingsSection(
                config = config,
                onShowInputSettings = { showInputSettings = true }
            )
            
            Divider()
            
            // Keyboard Shortcuts Section
            KeyboardShortcutsSection(
                onShowShortcuts = { showShortcutsDialog = true }
            )
            
            Divider()
            
            // Window Management Section
            WindowManagementSection(
                config = config,
                onShowWindowSettings = { showWindowSettings = true }
            )
        }
        
        // Keyboard Shortcuts Dialog
        if (showShortcutsDialog) {
            KeyboardShortcutsDialog(
                shortcuts = viewModel.getAllShortcuts(),
                onDismiss = { showShortcutsDialog = false },
                onShortcutSelected = { shortcut ->
                    selectedShortcut = shortcut
                }
            )
        }
        
        // Window Settings Dialog
        if (showWindowSettings) {
            WindowSettingsDialog(
                config = config,
                onDismiss = { showWindowSettings = false },
                onSave = { newSettings ->
                    viewModel.saveWindowSettings(newSettings)
                    scope.launch {
                        snackbarHostState.showSnackbar("Window settings saved")
                    }
                }
            )
        }
        
        // Input Settings Dialog
        if (showInputSettings) {
            InputSettingsDialog(
                config = config,
                onDismiss = { showInputSettings = false },
                onSave = { newSettings ->
                    viewModel.saveInputSettings(newSettings)
                    scope.launch {
                        snackbarHostState.showSnackbar("Input settings saved")
                    }
                }
            )
        }
        
        // Shortcut Detail Dialog
        selectedShortcut?.let { shortcut ->
            ShortcutDetailDialog(
                shortcut = shortcut,
                onDismiss = { selectedShortcut = null }
            )
        }
    }
}

@Composable
private fun DeviceInfoSection(config: ChromebookConfig) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Device Information",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        InfoCard(
            icon = Icons.Default.Tv,
            title = "Device Type",
            value = config.deviceType
        )
        
        InfoCard(
            icon = Icons.Default.Tablet,
            title = "Screen Resolution",
            value = "${config.screenWidthDp} x ${config.screenHeightDp} dp"
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            InfoCard(
                icon = Icons.Default.Keyboard,
                title = "Keyboard",
                value = if (config.hasKeyboard) "Yes" else "No",
                modifier = Modifier.weight(1f)
            )
            
            InfoCard(
                icon = Icons.Default.Mouse,
                title = "Mouse",
                value = if (config.hasMouse) "Yes" else "No",
                modifier = Modifier.weight(1f)
            )
            
            InfoCard(
                icon = Icons.Default.Edit,
                title = "Stylus",
                value = if (config.hasStylus) "Yes" else "No",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun LayoutSettingsSection(
    config: ChromebookConfig,
    onMultiPaneChange: (Boolean) -> Unit,
    onExpandedLayoutChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Layout Settings",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        SettingSwitch(
            title = "Multi-Pane Layout",
            description = "Enable multi-pane layout for large screens",
            checked = config.useMultiPane,
            onCheckedChange = onMultiPaneChange,
            enabled = config.screenWidthDp >= ChromebookOptimizations.MULTI_PANE_THRESHOLD
        )
        
        SettingSwitch(
            title = "Expanded Layout",
            description = "Use expanded layout for very large screens",
            checked = config.useExpandedLayout,
            onCheckedChange = onExpandedLayoutChange,
            enabled = config.screenWidthDp >= ChromebookOptimizations.EXPANDED_SCREEN_WIDTH
        )
    }
}

@Composable
private fun InputSettingsSection(
    config: ChromebookConfig,
    onShowInputSettings: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Input Settings",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        SettingItem(
            title = "Keyboard & Mouse Preferences",
            description = "Configure keyboard shortcuts and mouse behavior",
            icon = Icons.Default.Keyboard,
            onClick = onShowInputSettings
        )
    }
}

@Composable
private fun KeyboardShortcutsSection(
    onShowShortcuts: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Keyboard Shortcuts",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        SettingItem(
            title = "View All Shortcuts",
            description = "See all available keyboard shortcuts",
            icon = Icons.Default.Keyboard,
            onClick = onShowShortcuts
        )
    }
}

@Composable
private fun WindowManagementSection(
    config: ChromebookConfig,
    onShowWindowSettings: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Window Management",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        SettingItem(
            title = "Window Size & Behavior",
            description = "Configure window size and multi-window behavior",
            icon = Icons.Default.Settings,
            onClick = onShowWindowSettings,
            enabled = config.supportsFreeformWindow
        )
        
        SettingSwitch(
            title = "Picture-in-Picture",
            description = "Enable Picture-in-Picture mode for recipes",
            checked = config.supportsPictureInPicture,
            onCheckedChange = { /* TODO: Implement PiP toggle */ },
            enabled = config.supportsPictureInPicture
        )
    }
}

@Composable
private fun InfoCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary
            )
            
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun SettingSwitch(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(false)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
    }
}

@Composable
private fun SettingItem(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(false)
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
            
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
        
        IconButton(
            onClick = onClick,
            enabled = enabled
        ) {
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Open"
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun KeyboardShortcutsDialog(
    shortcuts: List<KeyboardShortcut>,
    onDismiss: () -> Unit,
    onShortcutSelected: (KeyboardShortcut) -> Unit
) {
    val groupedShortcuts = remember(shortcuts) {
        shortcuts.groupBy { 
            when {
                it.action.contains("Recipe") || it.action.contains("Cookbook") -> "Navigation"
                it.action.contains("Save") || it.action.contains("Delete") || 
                it.action.contains("Undo") || it.action.contains("Redo") -> "Editing"
                it.action.contains("Zoom") || it.action.contains("Print") -> "View"
                it.action.contains("Sync") || it.action.contains("Settings") -> "Settings"
                else -> "Other"
            }
        }
    }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Keyboard Shortcuts",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            groupedShortcuts.forEach { (category, shortcutsInCategory) ->
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    shortcutsInCategory.sortedBy { it.action }.forEach { shortcut ->
                        ShortcutItem(
                            shortcut = shortcut,
                            onClick = {
                                onShortcutSelected(shortcut)
                                onDismiss()
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Close")
            }
        }
    }
}

@Composable
private fun ShortcutItem(
    shortcut: KeyboardShortcut,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = shortcut.action,
                style = MaterialTheme.typography.bodyLarge
            )
            
            Text(
                text = shortcut.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        
        Text(
            text = formatShortcutKey(shortcut.keyCombination),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun ShortcutDetailDialog(
    shortcut: KeyboardShortcut,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(shortcut.action) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(shortcut.description)
                
                Text(
                    text = "Shortcut: ${formatShortcutKey(shortcut.keyCombination)}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

@Composable
private fun WindowSettingsDialog(
    config: ChromebookConfig,
    onDismiss: () -> Unit,
    onSave: (Map<String, Any>) -> Unit
) {
    var windowWidth by remember { mutableStateOf(config.getRecommendedWindowSize().first.toString()) }
    var windowHeight by remember { mutableStateOf(config.getRecommendedWindowSize().second.toString()) }
    var rememberWindowSize by remember { mutableStateOf(false) }
    var alwaysOnTop by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Window Settings") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = windowWidth,
                    onValueChange = { windowWidth = it },
                    label = { Text("Window Width (dp)") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = windowHeight,
                    onValueChange = { windowHeight = it },
                    label = { Text("Window Height (dp)") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                
                SettingSwitch(
                    title = "Remember Window Size",
                    description = "Remember window size between sessions",
                    checked = rememberWindowSize,
                    onCheckedChange = { rememberWindowSize = it }
                )
                
                SettingSwitch(
                    title = "Always on Top",
                    description = "Keep window on top of other windows",
                    checked = alwaysOnTop,
                    onCheckedChange = { alwaysOnTop = it },
                    enabled = false // Not supported on all platforms
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(mapOf(
                        "windowWidth" to windowWidth.toIntOrNull(),
                        "windowHeight" to windowHeight.toIntOrNull(),
                        "rememberWindowSize" to rememberWindowSize,
                        "alwaysOnTop" to alwaysOnTop
                    ))
                    onDismiss()
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun InputSettingsDialog(
    config: ChromebookConfig,
    onDismiss: () -> Unit,
    onSave: (Map<String, Any>) -> Unit
) {
    var enableStylus by remember { mutableStateOf(config.hasStylus) }
    var stylusSensitivity by remember { mutableStateOf(0.5f) }
    var enableMouseGestures by remember { mutableStateOf(true) }
    var keyboardRepeatRate by remember { mutableStateOf(500) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Input Settings") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SettingSwitch(
                    title = "Enable Stylus Support",
                    description = "Enable stylus input for annotations",
                    checked = enableStylus,
                    onCheckedChange = { enableStylus = it },
                    enabled = config.hasStylus
                )
                
                if (enableStylus) {
                    Text("Stylus Sensitivity: ${(stylusSensitivity * 100).toInt()}%")
                    Slider(
                        value = stylusSensitivity,
                        onValueChange = { stylusSensitivity = it },
                        valueRange = 0f..1f,
                        steps = 10
                    )
                }
                
                SettingSwitch(
                    title = "Mouse Gestures",
                    description = "Enable mouse gestures for navigation",
                    checked = enableMouseGestures,
                    onCheckedChange = { enableMouseGestures = it }
                )
                
                OutlinedTextField(
                    value = keyboardRepeatRate.toString(),
                    onValueChange = { keyboardRepeatRate = it.toIntOrNull() ?: 0 },
                    label = { Text("Keyboard Repeat Rate (ms)") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(mapOf(
                        "enableStylus" to enableStylus,
                        "stylusSensitivity" to stylusSensitivity,
                        "enableMouseGestures" to enableMouseGestures,
                        "keyboardRepeatRate" to keyboardRepeatRate
                    ))
                    onDismiss()
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Format a key combination for display
 */
private fun formatShortcutKey(combination: com.ourcookbook.domain.usecase.chromebook.KeyCombination): String {
    val parts = mutableListOf<String>()
    
    combination.modifiers.forEach { modifier ->
        parts.add(when (modifier) {
            com.ourcookbook.domain.usecase.chromebook.Modifier.Ctrl -> "Ctrl"
            com.ourcookbook.domain.usecase.chromebook.Modifier.Shift -> "Shift"
            com.ourcookbook.domain.usecase.chromebook.Modifier.Meta -> "Meta"
            com.ourcookbook.domain.usecase.chromebook.Modifier.Alt -> "Alt"
        })
    }
    
    parts.add(combination.key.name)
    
    return parts.joinToString(" + ")
}

@Composable
@Preview(showBackground = true)
fun ChromebookSettingsScreenPreview() {
    CookbookTheme {
        ChromebookSettingsScreen(onBackClick = {})
    }
}

@HiltViewModel
class ChromebookSettingsViewModel @Inject constructor(
    private val chromebookOptimizations: ChromebookOptimizations
) : ViewModel() {
    
    private val _chromebookConfig = MutableStateFlow<ChromebookConfig?>(null)
    val chromebookConfig: Flow<ChromebookConfig> = _chromebookConfig
        .mapNotNull { it }

    init {
        loadConfig()
    }

    private fun loadConfig() {
        // Build the config directly from the injected ChromebookOptimizations (context-only reads)
        _chromebookConfig.value = ChromebookConfig(
            deviceType = chromebookOptimizations.getDeviceType(),
            screenWidthDp = chromebookOptimizations.getScreenWidthDp(),
            screenHeightDp = chromebookOptimizations.getScreenHeightDp(),
            hasKeyboard = chromebookOptimizations.hasHardwareKeyboard(),
            hasMouse = chromebookOptimizations.hasMouseSupport(),
            hasStylus = chromebookOptimizations.hasStylusSupport(),
            isChromebook = chromebookOptimizations.isChromebook(),
            useMultiPane = chromebookOptimizations.shouldUseMultiPaneLayout(),
            useExpandedLayout = chromebookOptimizations.shouldUseExpandedLayout(),
            supportsFreeformWindow = chromebookOptimizations.supportsFreeformWindow(),
            supportsPictureInPicture = chromebookOptimizations.supportsPictureInPicture(),
            keyboardShortcuts = chromebookOptimizations.getKeyboardShortcuts()
        )
    }
    
    fun getAllShortcuts(): List<KeyboardShortcut> {
        return _chromebookConfig.value?.keyboardShortcuts ?: emptyList()
    }
    
    fun setMultiPaneEnabled(enabled: Boolean) {
        _chromebookConfig.value = _chromebookConfig.value?.copy(
            useMultiPane = enabled
        )
    }
    
    fun setExpandedLayoutEnabled(enabled: Boolean) {
        _chromebookConfig.value = _chromebookConfig.value?.copy(
            useExpandedLayout = enabled
        )
    }
    
    fun saveWindowSettings(settings: Map<String, Any>) {
        // Save window settings to preferences
        // Implementation would use SharedPreferences or similar
    }
    
    fun saveInputSettings(settings: Map<String, Any>) {
        // Save input settings to preferences
        // Implementation would use SharedPreferences or similar
    }
}

// Extension function for ChromebookConfig to get recommended window size
fun ChromebookConfig.getRecommendedWindowSize(): Pair<Int, Int> {
    return when {
        screenWidthDp >= 1200 -> Pair(1200, 800)
        screenWidthDp >= 900 -> Pair(1000, 700)
        else -> Pair(800, 600)
    }
}
