@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.ourcookbook.ui.screens.exportimport

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ourcookbook.domain.model.*
import com.ourcookbook.ui.components.*
import com.ourcookbook.ui.navigation.Route
import com.ourcookbook.ui.screens.exportimport.ExportImportEvent
import com.ourcookbook.ui.screens.exportimport.ExportImportMode
import com.ourcookbook.ui.screens.exportimport.ExportImportState
import com.ourcookbook.ui.theme.CookbookSpacing
import com.ourcookbook.ui.theme.CookbookTypography
import com.ourcookbook.ui.theme.getStatusColor
import kotlinx.coroutines.launch

/**
 * Main Export/Import Screen
 * Task 2.1.09: Export/Import Screen Implementation
 */

@Composable
fun ExportImportScreen(
    navController: NavController,
    viewModel: ExportImportViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            // Convert URIs to file paths
            val filePaths = uris.mapNotNull { uri ->
                uri.path?.takeIf { it.isNotBlank() }
            }
            
            if (filePaths.isNotEmpty()) {
                viewModel.handleEvent(ExportImportEvent.AddFilesForImport(filePaths))
            }
        }
    }
    
    // Handle actions from ViewModel
    LaunchedEffect(Unit) {
        viewModel.actions.collect { action ->
            when (action) {
                is ExportImportAction.ShowMessage -> {
                    // Show snackbar or toast
                    scope.launch {
                        // In a real implementation, show a snackbar
                    }
                }
                is ExportImportAction.ShowErrorMessage -> {
                    // Show error message
                    scope.launch {
                        // In a real implementation, show an error snackbar
                    }
                }
                is ExportImportAction.NavigationAction -> {
                    when (action.route) {
                        "back" -> navController.popBackStack()
                        "export_settings" -> {
                            // Navigate to export settings
                        }
                        "import_settings" -> {
                            // Navigate to import settings
                        }
                        "operation_history" -> {
                            // Navigate to operation history
                        }
                    }
                }
                else -> {}
            }
        }
    }
    
    // Show file picker when requested
    if (state.showFilePicker) {
        LaunchedEffect(Unit) {
            filePickerLauncher.launch(arrayOf("application/*", "text/*"))
            viewModel.handleEvent(ExportImportEvent.HideFilePicker)
        }
    }
    
    // Main content
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(CookbookSpacing.medium)
    ) {
        // Header
        ExportImportHeader(
            currentMode = state.currentMode,
            onModeChange = { mode ->
                viewModel.handleEvent(ExportImportEvent.SwitchMode(mode))
            },
            onBackClick = { viewModel.handleEvent(ExportImportEvent.NavigateBack) }
        )
        
        Spacer(modifier = Modifier.height(CookbookSpacing.medium))
        
        // Mode-specific content
        when (state.currentMode) {
            ExportImportMode.EXPORT -> ExportContent(state, viewModel)
            ExportImportMode.IMPORT -> ImportContent(state, viewModel)
        }
    }
    
    // Dialogs
    if (state.showFormatSelection) {
        FormatSelectionDialog(
            currentFormat = when (state.currentMode) {
                ExportImportMode.EXPORT -> state.exportSettings.format
                ExportImportMode.IMPORT -> state.importSettings.format
            },
            isExport = state.currentMode == ExportImportMode.EXPORT,
            onFormatSelected = { format ->
                when (state.currentMode) {
                    ExportImportMode.EXPORT -> 
                        viewModel.handleEvent(ExportImportEvent.SelectExportFormat(format))
                    ExportImportMode.IMPORT -> 
                        viewModel.handleEvent(ExportImportEvent.SelectImportFormat(format))
                }
            },
            onDismiss = { viewModel.handleEvent(ExportImportEvent.HideFormatSelection) }
        )
    }
    
    if (state.showPreviewDialog && state.importPreview != null) {
        PreviewDialog(
            preview = state.importPreview!!,
            onConfirm = { viewModel.handleEvent(ExportImportEvent.HidePreviewDialog) },
            onDismiss = { viewModel.handleEvent(ExportImportEvent.HidePreviewDialog) }
        )
    }
    
    if (state.showProgressDialog) {
        ProgressDialog(
            title = when (state.currentMode) {
                ExportImportMode.EXPORT -> "Exporting..."
                ExportImportMode.IMPORT -> "Importing..."
            },
            message = "Processing ${state.currentProgress * 100}%",
            progress = state.currentProgress,
            onCancel = {
                when (state.currentMode) {
                    ExportImportMode.EXPORT -> 
                        state.currentOperations.firstOrNull()?.let { op ->
                            viewModel.handleEvent(ExportImportEvent.CancelExport(op.id))
                        }
                    ExportImportMode.IMPORT -> 
                        state.currentOperations.firstOrNull()?.let { op ->
                            viewModel.handleEvent(ExportImportEvent.CancelImport(op.id))
                        }
                }
            }
        )
    }
    
    if (state.showConflictDialog && state.currentConflict != null) {
        ConflictResolutionDialog(
            conflict = state.currentConflict!!,
            onResolve = { resolution ->
                viewModel.handleEvent(ExportImportEvent.ResolveConflict(resolution))
            },
            onSkip = { viewModel.handleEvent(ExportImportEvent.SkipConflict) },
            onDismiss = { viewModel.handleEvent(ExportImportEvent.HideConflictDialog) }
        )
    }
    
    if (state.showHistoryDialog) {
        OperationHistoryDialog(
            operations = state.operationHistory,
            onRetry = { operationId ->
                viewModel.handleEvent(ExportImportEvent.RetryOperation(operationId))
            },
            onClearHistory = { viewModel.handleEvent(ExportImportEvent.ClearOperationHistory) },
            onDismiss = { viewModel.handleEvent(ExportImportEvent.HideHistoryDialog) }
        )
    }
    
    if (state.showBatchResults && state.batchOperationResult != null) {
        BatchResultsDialog(
            result = state.batchOperationResult!!,
            onDismiss = { viewModel.handleEvent(ExportImportEvent.ClearBatchResults) }
        )
    }
    
    // Error message
    state.errorMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { viewModel.handleEvent(ExportImportEvent.ClearError) },
            title = { Text("Error", style = CookbookTypography.headlineSmall) },
            text = { Text(message, style = CookbookTypography.bodyMedium) },
            confirmButton = {
                TextButton(onClick = { viewModel.handleEvent(ExportImportEvent.ClearError) }) {
                    Text("OK", style = CookbookTypography.labelLarge)
                }
            }
        )
    }
    
    // Success message
    state.successMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { viewModel.handleEvent(ExportImportEvent.ClearSuccess) },
            title = { Text("Success", style = CookbookTypography.headlineSmall) },
            text = { Text(message, style = CookbookTypography.bodyMedium) },
            confirmButton = {
                TextButton(onClick = { viewModel.handleEvent(ExportImportEvent.ClearSuccess) }) {
                    Text("OK", style = CookbookTypography.labelLarge)
                }
            }
        )
    }
}

@Composable
private fun ExportImportHeader(
    currentMode: ExportImportMode,
    onModeChange: (ExportImportMode) -> Unit,
    onBackClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Back button
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        
        Spacer(modifier = Modifier.width(CookbookSpacing.small))
        
        // Mode toggle
        Row(
            modifier = Modifier
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    MaterialTheme.shapes.medium
                )
                .padding(4.dp)
        ) {
            ExportImportMode.entries.forEach { mode ->
                val isSelected = mode == currentMode
                
                Text(
                    text = mode.name,
                    style = if (isSelected) {
                        CookbookTypography.labelLarge.copy(color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        CookbookTypography.labelLarge.copy(color = MaterialTheme.colorScheme.onSurface)
                    },
                    modifier = Modifier
                        .clickable { onModeChange(mode) }
                        .padding(horizontal = CookbookSpacing.medium, vertical = CookbookSpacing.small)
                        .background(
                            if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            MaterialTheme.shapes.small
                        )
                        .padding(horizontal = CookbookSpacing.small, vertical = CookbookSpacing.xSmall)
                )
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // History button
        IconButton(onClick = { /* Show history */ }) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = "History",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ExportContent(
    state: ExportImportState,
    viewModel: ExportImportViewModel
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "Export Your Data",
            style = CookbookTypography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(CookbookSpacing.small))
        
        Text(
            text = "Choose what you want to export and the format",
            style = CookbookTypography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(CookbookSpacing.large))
        
        // Export target selection
        ExportTargetSelection(
            selectedTarget = state.exportTarget,
            onTargetSelected = { target ->
                viewModel.handleEvent(ExportImportEvent.SelectExportTarget(target))
            }
        )
        
        Spacer(modifier = Modifier.height(CookbookSpacing.medium))
        
        // Format selection
        FormatSelectionCard(
            title = "Export Format",
            selectedFormat = state.exportSettings.format,
            onFormatSelected = { viewModel.handleEvent(ExportImportEvent.ShowFormatSelection) }
        )
        
        Spacer(modifier = Modifier.height(CookbookSpacing.medium))
        
        // Export location
        LocationSelectionCard(
            title = "Export Location",
            selectedLocation = state.exportSettings.exportLocation,
            onLocationSelected = { viewModel.handleEvent(ExportImportEvent.ShowLocationSelection) }
        )
        
        Spacer(modifier = Modifier.height(CookbookSpacing.medium))
        
        // Item selection based on target
        when (state.exportTarget) {
            ExportTarget.INDIVIDUAL_RECIPE -> {
                RecipeSelectionSection(
                    selectedRecipeIds = state.selectedRecipeIds,
                    onToggleRecipe = { recipeId ->
                        viewModel.handleEvent(ExportImportEvent.ToggleRecipeSelection(recipeId))
                    },
                    onSelectAll = { select ->
                        viewModel.handleEvent(ExportImportEvent.SelectAllRecipes(select))
                    }
                )
            }
            ExportTarget.ENTIRE_COOKBOOK -> {
                CookbookSelectionSection(
                    selectedCookbookIds = state.selectedCookbookIds,
                    onToggleCookbook = { cookbookId ->
                        viewModel.handleEvent(ExportImportEvent.ToggleCookbookSelection(cookbookId))
                    },
                    onSelectAll = { select ->
                        viewModel.handleEvent(ExportImportEvent.SelectAllCookbooks(select))
                    }
                )
            }
            ExportTarget.ALL_RECIPES -> {
                // No selection needed for all recipes
                Text(
                    text = "All recipes will be exported",
                    style = CookbookTypography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(CookbookSpacing.large))
        
        // Export button
        Button(
            onClick = { viewModel.handleEvent(ExportImportEvent.StartExport()) },
            enabled = state.canExport && !state.isOperationInProgress,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            if (state.isOperationInProgress) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text(
                    text = "Export",
                    style = CookbookTypography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(CookbookSpacing.medium))
        
        // Batch export option
        OutlinedButton(
            onClick = { 
                // For batch export, we need to select multiple items
                when (state.exportTarget) {
                    ExportTarget.INDIVIDUAL_RECIPE -> {
                        if (state.selectedRecipeIds.size > 1) {
                            viewModel.handleEvent(ExportImportEvent.StartBatchExport(state.selectedRecipeIds))
                        }
                    }
                    ExportTarget.ENTIRE_COOKBOOK -> {
                        if (state.selectedCookbookIds.size > 1) {
                            viewModel.handleEvent(ExportImportEvent.StartBatchExport(state.selectedCookbookIds))
                        }
                    }
                    else -> {}
                }
            },
            enabled = when (state.exportTarget) {
                ExportTarget.INDIVIDUAL_RECIPE -> state.selectedRecipeIds.size > 1
                ExportTarget.ENTIRE_COOKBOOK -> state.selectedCookbookIds.size > 1
                ExportTarget.ALL_RECIPES -> false
            } && !state.isOperationInProgress,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Batch Export",
                style = CookbookTypography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        // Cloud export options
        Spacer(modifier = Modifier.height(CookbookSpacing.medium))
        
        Divider(color = MaterialTheme.colorScheme.outlineVariant)
        
        Spacer(modifier = Modifier.height(CookbookSpacing.medium))
        
        Text(
            text = "Cloud Export",
            style = CookbookTypography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(CookbookSpacing.small))
        
        // Google Drive export
        OutlinedButton(
            onClick = { viewModel.handleEvent(ExportImportEvent.ExportToDrive(null)) },
            enabled = state.isDriveConnected && state.canExport && !state.isOperationInProgress,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.CloudUpload,
                contentDescription = "Google Drive",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(CookbookSpacing.small))
            Text(
                text = "Export to Google Drive",
                style = CookbookTypography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        if (!state.isDriveConnected) {
            Spacer(modifier = Modifier.height(CookbookSpacing.small))
            TextButton(
                onClick = { viewModel.handleEvent(ExportImportEvent.ConnectToDrive) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Connect to Google Drive",
                    style = CookbookTypography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun ImportContent(
    state: ExportImportState,
    viewModel: ExportImportViewModel
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "Import Data",
            style = CookbookTypography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(CookbookSpacing.small))
        
        Text(
            text = "Select files to import and choose the format",
            style = CookbookTypography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(CookbookSpacing.large))
        
        // Import format selection
        FormatSelectionCard(
            title = "Import Format",
            selectedFormat = state.importSettings.format,
            onFormatSelected = { viewModel.handleEvent(ExportImportEvent.ShowFormatSelection) }
        )
        
        Spacer(modifier = Modifier.height(CookbookSpacing.medium))
        
        // Import target selection
        ImportTargetSelection(
            selectedTarget = state.importSettings.target,
            onTargetSelected = { target ->
                viewModel.handleEvent(ExportImportEvent.SelectImportTarget(target))
            }
        )
        
        Spacer(modifier = Modifier.height(CookbookSpacing.medium))
        
        // Conflict resolution settings
        ConflictResolutionSettings(
            strategy = state.importSettings.conflictResolution,
            onStrategyChange = { strategy ->
                viewModel.handleEvent(ExportImportEvent.UpdateImportSettings(
                    state.importSettings.copy(conflictResolution = strategy)
                ))
            }
        )
        
        Spacer(modifier = Modifier.height(CookbookSpacing.medium))
        
        // File selection
        FileSelectionSection(
            selectedFilePaths = state.selectedFilePaths,
            onAddFiles = { viewModel.handleEvent(ExportImportEvent.ShowFilePicker) },
            onRemoveFile = { filePath ->
                viewModel.handleEvent(ExportImportEvent.RemoveFileFromImport(filePath))
            },
            onPreviewFile = { filePath ->
                viewModel.handleEvent(ExportImportEvent.PreviewImport(filePath))
            }
        )
        
        Spacer(modifier = Modifier.height(CookbookSpacing.large))
        
        // Import button
        Button(
            onClick = { viewModel.handleEvent(ExportImportEvent.StartImport()) },
            enabled = state.canImport && !state.isOperationInProgress,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            if (state.isOperationInProgress) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text(
                    text = "Import",
                    style = CookbookTypography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(CookbookSpacing.medium))
        
        // Batch import option
        OutlinedButton(
            onClick = { 
                if (state.selectedFilePaths.size > 1) {
                    viewModel.handleEvent(ExportImportEvent.StartBatchImport(state.selectedFilePaths))
                }
            },
            enabled = state.selectedFilePaths.size > 1 && !state.isOperationInProgress,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Batch Import",
                style = CookbookTypography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        // Cloud import options
        Spacer(modifier = Modifier.height(CookbookSpacing.medium))
        
        Divider(color = MaterialTheme.colorScheme.outlineVariant)
        
        Spacer(modifier = Modifier.height(CookbookSpacing.medium))
        
        Text(
            text = "Cloud Import",
            style = CookbookTypography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(CookbookSpacing.small))
        
        // Google Drive import
        OutlinedButton(
            onClick = { viewModel.handleEvent(ExportImportEvent.LoadDriveFiles) },
            enabled = state.isDriveConnected && !state.isOperationInProgress,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.CloudDownload,
                contentDescription = "Google Drive",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(CookbookSpacing.small))
            Text(
                text = "Import from Google Drive",
                style = CookbookTypography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        if (!state.isDriveConnected) {
            Spacer(modifier = Modifier.height(CookbookSpacing.small))
            TextButton(
                onClick = { viewModel.handleEvent(ExportImportEvent.ConnectToDrive) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Connect to Google Drive",
                    style = CookbookTypography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        // Drive files list (if connected and files loaded)
        if (state.isDriveConnected && state.driveFiles.isNotEmpty()) {
            Spacer(modifier = Modifier.height(CookbookSpacing.medium))
            
            Text(
                text = "Available Files",
                style = CookbookTypography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(CookbookSpacing.small))
            
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = CookbookSpacing.small)
            ) {
                items(state.driveFiles) { fileInfo ->
                    DriveFileItem(
                        fileInfo = fileInfo,
                        onImport = { viewModel.handleEvent(ExportImportEvent.ImportFromDrive(fileInfo.filePath)) }
                    )
                    Spacer(modifier = Modifier.height(CookbookSpacing.small))
                }
            }
        }
    }
}

@Composable
private fun ExportTargetSelection(
    selectedTarget: ExportTarget,
    onTargetSelected: (ExportTarget) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Export Target",
            style = CookbookTypography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(CookbookSpacing.small))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(CookbookSpacing.small)
        ) {
            ExportTarget.entries.forEach { target ->
                val isSelected = target == selectedTarget
                
                FilterChip(
                    selected = isSelected,
                    onClick = { onTargetSelected(target) },
                    label = {
                        Text(
                            text = when (target) {
                                ExportTarget.INDIVIDUAL_RECIPE -> "Recipes"
                                ExportTarget.ENTIRE_COOKBOOK -> "Cookbooks"
                                ExportTarget.ALL_RECIPES -> "All Recipes"
                            },
                            style = CookbookTypography.labelMedium
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }
    }
}

@Composable
private fun ImportTargetSelection(
    selectedTarget: ImportTarget,
    onTargetSelected: (ImportTarget) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Import Target",
            style = CookbookTypography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(CookbookSpacing.small))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(CookbookSpacing.small)
        ) {
            ImportTarget.entries.forEach { target ->
                val isSelected = target == selectedTarget
                
                FilterChip(
                    selected = isSelected,
                    onClick = { onTargetSelected(target) },
                    label = {
                        Text(
                            text = when (target) {
                                ImportTarget.INDIVIDUAL_RECIPE -> "Recipes"
                                ImportTarget.COOKBOOK -> "Cookbooks"
                                ImportTarget.MULTIPLE_FILES -> "Multiple"
                            },
                            style = CookbookTypography.labelMedium
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }
    }
}

@Composable
private fun FormatSelectionCard(
    title: String,
    selectedFormat: Any, // ExportFormat or ImportFormat
    onFormatSelected: () -> Unit
) {
    Card(
        onClick = onFormatSelected,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(CookbookSpacing.medium)
        ) {
            Text(
                text = title,
                style = CookbookTypography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(CookbookSpacing.small))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = when (selectedFormat) {
                        is ExportFormat -> selectedFormat.name
                        is ImportFormat -> selectedFormat.name
                        else -> "Select Format"
                    },
                    style = CookbookTypography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Select format",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun LocationSelectionCard(
    title: String,
    selectedLocation: String,
    onLocationSelected: () -> Unit
) {
    Card(
        onClick = onLocationSelected,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(CookbookSpacing.medium)
        ) {
            Text(
                text = title,
                style = CookbookTypography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(CookbookSpacing.small))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = selectedLocation,
                    style = CookbookTypography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Select location",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun RecipeSelectionSection(
    selectedRecipeIds: List<String>,
    onToggleRecipe: (String) -> Unit,
    onSelectAll: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Select Recipes",
            style = CookbookTypography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(CookbookSpacing.small))
        
        // In a real implementation, we would show a list of recipes
        // For now, we'll show a placeholder
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(CookbookSpacing.medium)
            ) {
                Text(
                    text = "${selectedRecipeIds.size} recipes selected",
                    style = CookbookTypography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(CookbookSpacing.small))
                
                Button(
                    onClick = { onSelectAll(selectedRecipeIds.isEmpty()) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (selectedRecipeIds.isEmpty()) "Select All" else "Clear Selection",
                        style = CookbookTypography.labelLarge
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(CookbookSpacing.small))
        
        // Placeholder for recipe list
        Text(
            text = "Recipe list would appear here in a real implementation",
            style = CookbookTypography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun CookbookSelectionSection(
    selectedCookbookIds: List<String>,
    onToggleCookbook: (String) -> Unit,
    onSelectAll: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Select Cookbooks",
            style = CookbookTypography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(CookbookSpacing.small))
        
        // In a real implementation, we would show a list of cookbooks
        // For now, we'll show a placeholder
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(CookbookSpacing.medium)
            ) {
                Text(
                    text = "${selectedCookbookIds.size} cookbooks selected",
                    style = CookbookTypography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(CookbookSpacing.small))
                
                Button(
                    onClick = { onSelectAll(selectedCookbookIds.isEmpty()) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (selectedCookbookIds.isEmpty()) "Select All" else "Clear Selection",
                        style = CookbookTypography.labelLarge
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(CookbookSpacing.small))
        
        // Placeholder for cookbook list
        Text(
            text = "Cookbook list would appear here in a real implementation",
            style = CookbookTypography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun FileSelectionSection(
    selectedFilePaths: List<String>,
    onAddFiles: () -> Unit,
    onRemoveFile: (String) -> Unit,
    onPreviewFile: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Select Files",
            style = CookbookTypography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(CookbookSpacing.small))
        
        // Add files button
        OutlinedButton(
            onClick = onAddFiles,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add files",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(CookbookSpacing.small))
            Text(
                text = "Add Files",
                style = CookbookTypography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        Spacer(modifier = Modifier.height(CookbookSpacing.small))
        
        // Selected files list
        if (selectedFilePaths.isNotEmpty()) {
            Text(
                text = "Selected Files (${selectedFilePaths.size}):",
                style = CookbookTypography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(CookbookSpacing.small))
            
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                selectedFilePaths.forEach { filePath ->
                    SelectedFileItem(
                        filePath = filePath,
                        onRemove = { onRemoveFile(filePath) },
                        onPreview = { onPreviewFile(filePath) }
                    )
                    Spacer(modifier = Modifier.height(CookbookSpacing.xSmall))
                }
            }
        } else {
            Spacer(modifier = Modifier.height(CookbookSpacing.medium))
            Text(
                text = "No files selected",
                style = CookbookTypography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SelectedFileItem(
    filePath: String,
    onRemove: () -> Unit,
    onPreview: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(CookbookSpacing.medium)
        ) {
            Icon(
                imageVector = Icons.Default.InsertDriveFile,
                contentDescription = "File",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(CookbookSpacing.small))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = filePath.substringAfterLast("/"),
                    style = CookbookTypography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = filePath,
                    style = CookbookTypography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            
            IconButton(onClick = onPreview) {
                Icon(
                    imageVector = Icons.Default.RemoveRedEye,
                    contentDescription = "Preview",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun DriveFileItem(
    fileInfo: ExportFileInfo,
    onImport: () -> Unit
) {
    Card(
        onClick = onImport,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(CookbookSpacing.medium)
        ) {
            Icon(
                imageVector = when (fileInfo.format) {
                    ExportFormat.JSON -> Icons.Default.Description
                    ExportFormat.MARKDOWN -> Icons.Default.TextSnippet
                    ExportFormat.PDF -> Icons.Default.PictureAsPdf
                    ExportFormat.DOCX -> Icons.Default.InsertDriveFile
                },
                contentDescription = "File",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(CookbookSpacing.small))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = fileInfo.fileName,
                    style = CookbookTypography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = "${fileInfo.recipeCount} recipes • ${formatFileSize(fileInfo.fileSize)}",
                    style = CookbookTypography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            
            Icon(
                imageVector = Icons.Default.CloudDownload,
                contentDescription = "Import",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun ConflictResolutionSettings(
    strategy: ConflictResolutionStrategy,
    onStrategyChange: (ConflictResolutionStrategy) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Conflict Resolution",
            style = CookbookTypography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(CookbookSpacing.small))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(CookbookSpacing.medium)
            ) {
                Text(
                    text = "When duplicate recipes are found:",
                    style = CookbookTypography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(CookbookSpacing.small))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(CookbookSpacing.small)
                ) {
                    ConflictResolutionStrategy.entries.forEach { strategyOption ->
                        val isSelected = strategyOption == strategy
                        
                        FilterChip(
                            selected = isSelected,
                            onClick = { onStrategyChange(strategyOption) },
                            label = {
                                Text(
                                    text = when (strategyOption) {
                                        ConflictResolutionStrategy.ASK -> "Ask"
                                        ConflictResolutionStrategy.OVERWRITE -> "Overwrite"
                                        ConflictResolutionStrategy.SKIP -> "Skip"
                                        ConflictResolutionStrategy.MERGE -> "Merge"
                                    },
                                    style = CookbookTypography.labelMedium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }
        }
    }
}

// ==================== DIALOGS ====================

@Composable
fun FormatSelectionDialog(
    currentFormat: Any,
    isExport: Boolean,
    onFormatSelected: (Any) -> Unit,
    onDismiss: () -> Unit
) {
    val formats = if (isExport) {
        ExportFormat.entries
    } else {
        ImportFormat.entries
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isExport) "Select Export Format" else "Select Import Format",
                style = CookbookTypography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Choose the format for your ${if (isExport) "export" else "import"}:",
                    style = CookbookTypography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                
                Spacer(modifier = Modifier.height(CookbookSpacing.medium))
                
                formats.forEach { format ->
                    val isSelected = when (currentFormat) {
                        is ExportFormat -> format == currentFormat
                        is ImportFormat -> format == currentFormat
                        else -> false
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onFormatSelected(format) }
                            .padding(CookbookSpacing.small)
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { onFormatSelected(format) }
                        )
                        
                        Spacer(modifier = Modifier.width(CookbookSpacing.small))
                        
                        Text(
                            text = format.name,
                            style = CookbookTypography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        // Show format icon or description
                        Text(
                            text = when (format) {
                                ExportFormat.JSON -> "Structured"
                                ExportFormat.MARKDOWN -> "Readable"
                                ExportFormat.PDF -> "Printable"
                                ExportFormat.DOCX -> "Editable"
                                ImportFormat.JSON -> "Structured"
                                ImportFormat.MARKDOWN -> "Readable"
                            },
                            style = CookbookTypography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(CookbookSpacing.xSmall))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", style = CookbookTypography.labelLarge)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large
    )

}
@Composable
fun PreviewItem(
    item: PreviewItem,
    statusColor: androidx.compose.ui.graphics.Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = when (item.type.lowercase()) {
                "recipe" -> Icons.Default.RestaurantMenu
                "cookbook" -> Icons.Default.Book
                else -> Icons.Default.InsertDriveFile
            },
            contentDescription = item.type,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(CookbookSpacing.small))
        
        Text(
            text = item.name,
            style = CookbookTypography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        
        Spacer(modifier = Modifier.width(CookbookSpacing.small))
        
        Text(
            text = item.type,
            style = CookbookTypography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.width(CookbookSpacing.small))
        
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(statusColor, MaterialTheme.shapes.full)
        )
    }
}
@Composable
fun ConflictResolutionDialog(
    conflict: ImportConflict,
    onResolve: (ConflictResolution) -> Unit,
    onSkip: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Conflict Detected",
                style = CookbookTypography.headlineSmall,
                color = MaterialTheme.colorScheme.error
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "A conflict was detected between recipes:",
                    style = CookbookTypography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(CookbookSpacing.medium))
                
                // Existing recipe info
                Text(
                    text = "Existing Recipe:",
                    style = CookbookTypography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(CookbookSpacing.small)
                    ) {
                        Text(
                            text = conflict.existingRecipe.title,
                            style = CookbookTypography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Text(
                            text = "ID: ${conflict.existingRecipe.id}",
                            style = CookbookTypography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        
                        Text(
                            text = "Category: ${conflict.existingRecipe.category}",
                            style = CookbookTypography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(CookbookSpacing.medium))
                
                // New recipe info
                Text(
                    text = "New Recipe:",
                    style = CookbookTypography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(CookbookSpacing.small)
                    ) {
                        Text(
                            text = conflict.newRecipe.title,
                            style = CookbookTypography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Text(
                            text = "ID: ${conflict.newRecipe.id}",
                            style = CookbookTypography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        
                        Text(
                            text = "Category: ${conflict.newRecipe.category}",
                            style = CookbookTypography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(CookbookSpacing.medium))
                
                Text(
                    text = "Conflict Type: ${conflict.conflictType.name}",
                    style = CookbookTypography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        confirmButton = {
            Column {
                TextButton(
                    onClick = { onResolve(ConflictResolution.REPLACE_WITH_NEW) },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Replace Existing", style = CookbookTypography.labelLarge)
                }
                
                TextButton(
                    onClick = { onResolve(ConflictResolution.KEEP_EXISTING) },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("Keep Existing", style = CookbookTypography.labelLarge)
                }
                
                TextButton(
                    onClick = onSkip,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Text("Skip", style = CookbookTypography.labelLarge)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", style = CookbookTypography.labelLarge)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large
    )
}

@Composable
fun OperationHistoryDialog(
    operations: List<ExportImportOperation>,
    onRetry: (String) -> Unit,
    onClearHistory: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Operation History",
                style = CookbookTypography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                if (operations.isEmpty()) {
                    Text(
                        text = "No operations found",
                        style = CookbookTypography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp),
                        contentPadding = PaddingValues(vertical = CookbookSpacing.small)
                    ) {
                        items(operations) { operation ->
                            OperationHistoryItem(
                                operation = operation,
                                onRetry = { onRetry(operation.id) }
                            )
                            Spacer(modifier = Modifier.height(CookbookSpacing.xSmall))
                        }
                    }
                }
            }
        },
        confirmButton = {
            Column {
                if (operations.isNotEmpty()) {
                    TextButton(
                        onClick = onClearHistory,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Clear History", style = CookbookTypography.labelLarge)
                    }
                }
                
                TextButton(onClick = onDismiss) {
                    Text("Close", style = CookbookTypography.labelLarge)
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large
    )
}

@Composable
fun OperationHistoryItem(
    operation: ExportImportOperation,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(CookbookSpacing.small)
        ) {
            Icon(
                imageVector = when (operation.type) {
                    OperationType.EXPORT -> Icons.Default.Upload
                    OperationType.IMPORT -> Icons.Default.Download
                },
                contentDescription = operation.type.name,
                tint = when (operation.status) {
                    OperationStatus.COMPLETED -> MaterialTheme.colorScheme.success
                    OperationStatus.FAILED -> MaterialTheme.colorScheme.error
                    OperationStatus.CANCELLED -> MaterialTheme.colorScheme.warning
                    else -> MaterialTheme.colorScheme.primary
                },
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(modifier = Modifier.width(CookbookSpacing.small))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = operation.displayName,
                    style = CookbookTypography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = "${operation.statusDisplay} • ${operation.fileCount} files",
                    style = CookbookTypography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            
            if (operation.isFailed) {
                TextButton(
                    onClick = onRetry,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Retry", style = CookbookTypography.labelSmall)
                }
            }
        }
    }
}

@Composable
fun BatchResultsDialog(
    result: BatchOperationResult,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Batch Operation Results",
                style = CookbookTypography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Summary
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    BatchResultStat(
                        label = "Total",
                        value = result.totalItems,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    BatchResultStat(
                        label = "Success",
                        value = result.successfulItems,
                        color = MaterialTheme.colorScheme.success
                    )
                    
                    BatchResultStat(
                        label = "Failed",
                        value = result.failedItems,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                
                Spacer(modifier = Modifier.height(CookbookSpacing.medium))
                
                // Success rate
                LinearProgressIndicator(
                    progress = { result.successRate },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(CookbookSpacing.small))
                
                Text(
                    text = "Success Rate: ${(result.successRate * 100).toInt()}%",
                    style = CookbookTypography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Failed items
                if (result.hasFailures) {
                    Spacer(modifier = Modifier.height(CookbookSpacing.medium))
                    
                    Text(
                        text = "Failed Items:",
                        style = CookbookTypography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(CookbookSpacing.small))
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .heightIn(max = 200.dp)
                    ) {
                        result.failedItemIds.forEach { itemId ->
                            Text(
                                text = "• $itemId",
                                style = CookbookTypography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                            
                            result.errorMessages[itemId]?.let { error ->
                                Text(
                                    text = "  Error: $error",
                                    style = CookbookTypography.bodySmall,
                                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(CookbookSpacing.xSmall))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK", style = CookbookTypography.labelLarge)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large
    )
}

@Composable
private fun BatchResultStat(
    label: String,
    value: Int,
    color: androidx.compose.ui.graphics.Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(CookbookSpacing.small)
    ) {
        Text(
            text = value.toString(),
            style = CookbookTypography.titleLarge.copy(color = color),
            color = color
        )
        
        Text(
            text = label,
            style = CookbookTypography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

// ==================== UTILITY FUNCTIONS ====================

@Composable
private fun formatFileSize(bytes: Long): String {
    return when {
        bytes >= 1024 * 1024 * 1024 -> "%.2f GB".format(bytes.toDouble() / (1024 * 1024 * 1024))
        bytes >= 1024 * 1024 -> "%.2f MB".format(bytes.toDouble() / (1024 * 1024))
        bytes >= 1024 -> "%.2f KB".format(bytes.toDouble() / 1024)
        else -> "$bytes B"
    }
}

// ==================== PREVIEW ====================

@Preview(showBackground = true)
@Composable
fun ExportImportScreenPreview() {
    MaterialTheme {
        ExportImportScreen(
            navController = rememberNavController()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FormatSelectionDialogPreview() {
    MaterialTheme {
        FormatSelectionDialog(
            currentFormat = ExportFormat.JSON,
            isExport = true,
            onFormatSelected = {},
            onDismiss = {}
        )
    }
}
@Preview(showBackground = true)
@Composable
fun ConflictResolutionDialogPreview() {
    val conflict = ImportConflict(
        recipeId = "test-id",
        existingRecipe = Recipe.create(
            title = "Existing Recipe",
            category = "Main"
        ),
        newRecipe = Recipe.create(
            title = "New Recipe",
            category = "Main"
        ),
        conflictType = ConflictType.DUPLICATE_ID
    )
    
    MaterialTheme {
        ConflictResolutionDialog(
            conflict = conflict,
            onResolve = {},
            onSkip = {},
            onDismiss = {}
        )
    }
}
