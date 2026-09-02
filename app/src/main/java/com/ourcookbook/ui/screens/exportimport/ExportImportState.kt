package com.ourcookbook.ui.screens.exportimport

import com.ourcookbook.domain.model.*

/**
 * State classes for Export/Import Screen
 * Task 2.1.09: Export/Import Screen Implementation
 */

/**
 * Main state for Export/Import Screen
 */
data class ExportImportState(
    // Current screen mode
    val currentMode: ExportImportMode = ExportImportMode.EXPORT,
    
    // Export state
    val exportSettings: ExportSettings = ExportSettings(),
    val selectedRecipeIds: List<String> = emptyList(),
    val selectedCookbookIds: List<String> = emptyList(),
    val exportTarget: ExportTarget = ExportTarget.INDIVIDUAL_RECIPE,
    
    // Import state
    val importSettings: ImportSettings = ImportSettings(),
    val selectedFilePaths: List<String> = emptyList(),
    val importPreview: ExportImportPreview? = null,
    
    // Operation state
    val currentOperations: List<ExportImportOperation> = emptyList(),
    val operationHistory: List<ExportImportOperation> = emptyList(),
    val isOperationInProgress: Boolean = false,
    val currentProgress: Float = 0f,
    
    // UI state
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val showFormatSelection: Boolean = false,
    val showPreviewDialog: Boolean = false,
    val showProgressDialog: Boolean = false,
    val showConflictDialog: Boolean = false,
    val showHistoryDialog: Boolean = false,
    val showLocationSelection: Boolean = false,
    
    // Conflict resolution
    val currentConflict: ImportConflict? = null,
    val conflictResolution: ConflictResolutionStrategy = ConflictResolutionStrategy.ASK,
    
    // File picker state
    val showFilePicker: Boolean = false,
    val filePickerMode: FilePickerMode = FilePickerMode.SINGLE,
    
    // Cloud integration
    val isDriveConnected: Boolean = false,
    val driveFiles: List<ExportFileInfo> = emptyList(),
    val isLoadingDriveFiles: Boolean = false,
    
    // Batch operations
    val batchOperationResult: BatchOperationResult? = null,
    val showBatchResults: Boolean = false
) {
    val canExport: Boolean get() = when (exportTarget) {
        ExportTarget.INDIVIDUAL_RECIPE -> selectedRecipeIds.isNotEmpty()
        ExportTarget.ENTIRE_COOKBOOK -> selectedCookbookIds.isNotEmpty()
        ExportTarget.ALL_RECIPES -> true
    }
    
    val canImport: Boolean get() = selectedFilePaths.isNotEmpty()
    
    val hasOperations: Boolean get() = currentOperations.isNotEmpty() || operationHistory.isNotEmpty()
    
    val hasCurrentConflict: Boolean get() = currentConflict != null
}

/**
 * Export/Import screen modes
 */
enum class ExportImportMode {
    EXPORT, IMPORT
}

/**
 * File picker modes
 */
enum class FilePickerMode {
    SINGLE, MULTIPLE
}

/**
 * Events for Export/Import Screen
 */
sealed class ExportImportEvent {
    // Mode switching
    data class SwitchMode(val mode: ExportImportMode) : ExportImportEvent()
    
    // Export events
    data class SelectExportTarget(val target: ExportTarget) : ExportImportEvent()
    data class SelectExportFormat(val format: ExportFormat) : ExportImportEvent()
    data class ToggleRecipeSelection(val recipeId: String) : ExportImportEvent()
    data class ToggleCookbookSelection(val cookbookId: String) : ExportImportEvent()
    data class SelectAllRecipes(val select: Boolean) : ExportImportEvent()
    data class SelectAllCookbooks(val select: Boolean) : ExportImportEvent()
    data class UpdateExportSettings(val settings: ExportSettings) : ExportImportEvent()
    data class StartExport(val location: String? = null) : ExportImportEvent()
    data class CancelExport(val operationId: String) : ExportImportEvent()
    
    // Import events
    data class SelectImportFormat(val format: ImportFormat) : ExportImportEvent()
    data class SelectImportTarget(val target: ImportTarget) : ExportImportEvent()
    data class AddFilesForImport(val filePaths: List<String>) : ExportImportEvent()
    data class RemoveFileFromImport(val filePath: String) : ExportImportEvent()
    data class UpdateImportSettings(val settings: ImportSettings) : ExportImportEvent()
    data class StartImport(val location: String? = null) : ExportImportEvent()
    data class PreviewImport(val filePath: String) : ExportImportEvent()
    data class CancelImport(val operationId: String) : ExportImportEvent()
    
    // Conflict resolution
    data class ResolveConflict(val resolution: ConflictResolution) : ExportImportEvent()
    object SkipConflict : ExportImportEvent()
    object OverwriteConflict : ExportImportEvent()
    
    // UI events
    object ShowFormatSelection : ExportImportEvent()
    object HideFormatSelection : ExportImportEvent()
    object ShowPreviewDialog : ExportImportEvent()
    object HidePreviewDialog : ExportImportEvent()
    object ShowProgressDialog : ExportImportEvent()
    object HideProgressDialog : ExportImportEvent()
    object ShowConflictDialog : ExportImportEvent()
    object HideConflictDialog : ExportImportEvent()
    object ShowHistoryDialog : ExportImportEvent()
    object HideHistoryDialog : ExportImportEvent()
    object ShowLocationSelection : ExportImportEvent()
    object HideLocationSelection : ExportImportEvent()
    object ShowFilePicker : ExportImportEvent()
    object HideFilePicker : ExportImportEvent()
    
    // History events
    object LoadOperationHistory : ExportImportEvent()
    data class RetryOperation(val operationId: String) : ExportImportEvent()
    object ClearOperationHistory : ExportImportEvent()
    
    // Cloud integration
    object ConnectToDrive : ExportImportEvent()
    object DisconnectFromDrive : ExportImportEvent()
    object LoadDriveFiles : ExportImportEvent()
    data class ExportToDrive(val folderId: String? = null) : ExportImportEvent()
    data class ImportFromDrive(val fileId: String) : ExportImportEvent()
    
    // Batch operations
    data class StartBatchExport(val items: List<String>) : ExportImportEvent()
    data class StartBatchImport(val filePaths: List<String>) : ExportImportEvent()
    object ClearBatchResults : ExportImportEvent()
    
    // Navigation
    object NavigateBack : ExportImportEvent()
    object NavigateToExportSettings : ExportImportEvent()
    object NavigateToImportSettings : ExportImportEvent()
    object NavigateToOperationHistory : ExportImportEvent()
    
    // Error handling
    data class ShowError(val message: String) : ExportImportEvent()
    object ClearError : ExportImportEvent()
    data class ShowSuccess(val message: String) : ExportImportEvent()
    object ClearSuccess : ExportImportEvent()
}

/**
 * Actions from ViewModel to UI
 */
sealed class ExportImportAction {
    data class ShowMessage(val message: String) : ExportImportAction()
    data class ShowErrorMessage(val message: String) : ExportImportAction()
    data class UpdateProgress(val progress: Float) : ExportImportAction()
    data class OperationCompleted(val operation: ExportImportOperation) : ExportImportAction()
    data class OperationFailed(val operation: ExportImportOperation, val error: String) : ExportImportAction()
    data class ShowConflict(val conflict: ImportConflict) : ExportImportAction()
    data class NavigationAction(val route: String) : ExportImportAction()
}

/**
 * Conflict resolution options
 */
enum class ConflictResolution {
    KEEP_EXISTING, // Keep the existing recipe
    REPLACE_WITH_NEW, // Replace with the new recipe
    MERGE, // Try to merge the recipes
    SKIP // Skip this conflict
}