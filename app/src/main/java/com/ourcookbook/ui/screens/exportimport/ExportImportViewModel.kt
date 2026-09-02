package com.ourcookbook.ui.screens.exportimport

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ourcookbook.domain.model.*
import com.ourcookbook.domain.usecase.exportimport.ExportUseCases
import com.ourcookbook.domain.usecase.exportimport.ImportUseCases
import com.ourcookbook.ui.screens.exportimport.ExportImportAction
import com.ourcookbook.ui.screens.exportimport.ExportImportEvent
import com.ourcookbook.ui.screens.exportimport.ExportImportState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * ViewModel for Export/Import Screen
 * Task 2.1.09: Export/Import Screen Implementation
 */

@HiltViewModel
class ExportImportViewModel @Inject constructor(
    private val exportUseCases: ExportUseCases,
    private val importUseCases: ImportUseCases
) : ViewModel() {
    
    // State management
    private val _state = MutableStateFlow(ExportImportState())
    val state: StateFlow<ExportImportState> = _state.asStateFlow()
    
    // Actions flow
    private val _actions = MutableSharedFlow<ExportImportAction>(extraBufferCapacity = 64)
    val actions: SharedFlow<ExportImportAction> = _actions.asSharedFlow()
    
    // Current operation tracking
    private var currentOperation: ExportImportOperation? = null
    
    init {
        // Initialize with default values
        viewModelScope.launch {
            loadOperationHistory()
        }
    }
    
    /**
     * Handle events from the UI
     */
    fun handleEvent(event: ExportImportEvent) {
        viewModelScope.launch {
            when (event) {
                // Mode switching
                is ExportImportEvent.SwitchMode -> switchMode(event.mode)
                
                // Export events
                is ExportImportEvent.SelectExportTarget -> selectExportTarget(event.target)
                is ExportImportEvent.SelectExportFormat -> selectExportFormat(event.format)
                is ExportImportEvent.ToggleRecipeSelection -> toggleRecipeSelection(event.recipeId)
                is ExportImportEvent.ToggleCookbookSelection -> toggleCookbookSelection(event.cookbookId)
                is ExportImportEvent.SelectAllRecipes -> selectAllRecipes(event.select)
                is ExportImportEvent.SelectAllCookbooks -> selectAllCookbooks(event.select)
                is ExportImportEvent.UpdateExportSettings -> updateExportSettings(event.settings)
                is ExportImportEvent.StartExport -> startExport(event.location)
                is ExportImportEvent.CancelExport -> cancelExport(event.operationId)
                
                // Import events
                is ExportImportEvent.SelectImportFormat -> selectImportFormat(event.format)
                is ExportImportEvent.SelectImportTarget -> selectImportTarget(event.target)
                is ExportImportEvent.AddFilesForImport -> addFilesForImport(event.filePaths)
                is ExportImportEvent.RemoveFileFromImport -> removeFileFromImport(event.filePath)
                is ExportImportEvent.UpdateImportSettings -> updateImportSettings(event.settings)
                is ExportImportEvent.StartImport -> startImport(event.location)
                is ExportImportEvent.PreviewImport -> previewImport(event.filePath)
                is ExportImportEvent.CancelImport -> cancelImport(event.operationId)
                
                // Conflict resolution
                is ExportImportEvent.ResolveConflict -> resolveConflict(event.resolution)
                is ExportImportEvent.SkipConflict -> skipConflict()
                is ExportImportEvent.OverwriteConflict -> overwriteConflict()
                
                // UI events
                ExportImportEvent.ShowFormatSelection -> showFormatSelection()
                ExportImportEvent.HideFormatSelection -> hideFormatSelection()
                ExportImportEvent.ShowPreviewDialog -> showPreviewDialog()
                ExportImportEvent.HidePreviewDialog -> hidePreviewDialog()
                ExportImportEvent.ShowProgressDialog -> showProgressDialog()
                ExportImportEvent.HideProgressDialog -> hideProgressDialog()
                ExportImportEvent.ShowConflictDialog -> showConflictDialog()
                ExportImportEvent.HideConflictDialog -> hideConflictDialog()
                ExportImportEvent.ShowHistoryDialog -> showHistoryDialog()
                ExportImportEvent.HideHistoryDialog -> hideHistoryDialog()
                ExportImportEvent.ShowLocationSelection -> showLocationSelection()
                ExportImportEvent.HideLocationSelection -> hideLocationSelection()
                ExportImportEvent.ShowFilePicker -> showFilePicker()
                ExportImportEvent.HideFilePicker -> hideFilePicker()
                
                // History events
                ExportImportEvent.LoadOperationHistory -> loadOperationHistory()
                is ExportImportEvent.RetryOperation -> retryOperation(event.operationId)
                ExportImportEvent.ClearOperationHistory -> clearOperationHistory()
                
                // Cloud integration
                ExportImportEvent.ConnectToDrive -> connectToDrive()
                ExportImportEvent.DisconnectFromDrive -> disconnectFromDrive()
                ExportImportEvent.LoadDriveFiles -> loadDriveFiles()
                is ExportImportEvent.ExportToDrive -> exportToDrive(event.folderId)
                is ExportImportEvent.ImportFromDrive -> importFromDrive(event.fileId)
                
                // Batch operations
                is ExportImportEvent.StartBatchExport -> startBatchExport(event.items)
                is ExportImportEvent.StartBatchImport -> startBatchImport(event.filePaths)
                ExportImportEvent.ClearBatchResults -> clearBatchResults()
                
                // Navigation
                ExportImportEvent.NavigateBack -> navigateBack()
                ExportImportEvent.NavigateToExportSettings -> navigateToExportSettings()
                ExportImportEvent.NavigateToImportSettings -> navigateToImportSettings()
                ExportImportEvent.NavigateToOperationHistory -> navigateToOperationHistory()
                
                // Error handling
                is ExportImportEvent.ShowError -> showError(event.message)
                ExportImportEvent.ClearError -> clearError()
                is ExportImportEvent.ShowSuccess -> showSuccess(event.message)
                ExportImportEvent.ClearSuccess -> clearSuccess()
            }
        }
    }
    
    // ==================== MODE SWITCHING ====================
    
    private suspend fun switchMode(mode: ExportImportMode) {
        _state.update { it.copy(currentMode = mode) }
    }
    
    // ==================== EXPORT FUNCTIONS ====================
    
    private suspend fun selectExportTarget(target: ExportTarget) {
        _state.update { it.copy(exportTarget = target) }
    }
    
    private suspend fun selectExportFormat(format: ExportFormat) {
        _state.update { 
            it.copy(
                exportSettings = it.exportSettings.copy(format = format),
                showFormatSelection = false
            )
        }
    }
    
    private suspend fun toggleRecipeSelection(recipeId: String) {
        _state.update { state ->
            val selectedRecipeIds = if (state.selectedRecipeIds.contains(recipeId)) {
                state.selectedRecipeIds - recipeId
            } else {
                state.selectedRecipeIds + recipeId
            }
            state.copy(selectedRecipeIds = selectedRecipeIds)
        }
    }
    
    private suspend fun toggleCookbookSelection(cookbookId: String) {
        _state.update { state ->
            val selectedCookbookIds = if (state.selectedCookbookIds.contains(cookbookId)) {
                state.selectedCookbookIds - cookbookId
            } else {
                state.selectedCookbookIds + cookbookId
            }
            state.copy(selectedCookbookIds = selectedCookbookIds)
        }
    }
    
    private suspend fun selectAllRecipes(select: Boolean) {
        _state.update { state ->
            if (select) {
                // In a real implementation, we would have access to all recipe IDs
                // For now, we'll just set a flag or use a placeholder
                state.copy(selectedRecipeIds = listOf("all"))
            } else {
                state.copy(selectedRecipeIds = emptyList())
            }
        }
    }
    
    private suspend fun selectAllCookbooks(select: Boolean) {
        _state.update { state ->
            if (select) {
                // In a real implementation, we would have access to all cookbook IDs
                state.copy(selectedCookbookIds = listOf("all"))
            } else {
                state.copy(selectedCookbookIds = emptyList())
            }
        }
    }
    
    private suspend fun updateExportSettings(settings: ExportSettings) {
        _state.update { it.copy(exportSettings = settings) }
    }
    
    private suspend fun startExport(location: String?) {
        val currentState = _state.value
        
        // Validate export settings
        val errors = exportUseCases.validateExportSettings(currentState.exportSettings)
        if (errors.isNotEmpty()) {
            _actions.emit(ExportImportAction.ShowErrorMessage(errors.joinToString(", ")))
            return
        }
        
        // Check what we're exporting
        val itemsToExport = when (currentState.exportTarget) {
            ExportTarget.INDIVIDUAL_RECIPE -> currentState.selectedRecipeIds
            ExportTarget.ENTIRE_COOKBOOK -> currentState.selectedCookbookIds
            ExportTarget.ALL_RECIPES -> listOf("all")
        }
        
        if (itemsToExport.isEmpty()) {
            _actions.emit(ExportImportAction.ShowErrorMessage("Please select items to export"))
            return
        }
        
        // Create operation
        val operation = exportUseCases.createExportOperation(
            currentState.exportTarget,
            currentState.exportSettings.format,
            itemsToExport.size
        )
        
        currentOperation = operation
        
        // Update state
        _state.update { 
            it.copy(
                isOperationInProgress = true,
                currentOperations = it.currentOperations + operation,
                showProgressDialog = true
            )
        }
        
        // Perform export based on target
        try {
            when (currentState.exportTarget) {
                ExportTarget.INDIVIDUAL_RECIPE -> {
                    if (itemsToExport.size == 1) {
                        exportSingleRecipe(itemsToExport[0], location)
                    } else {
                        exportMultipleRecipes(itemsToExport, location)
                    }
                }
                ExportTarget.ENTIRE_COOKBOOK -> {
                    if (itemsToExport.size == 1) {
                        exportSingleCookbook(itemsToExport[0], location)
                    } else {
                        exportMultipleCookbooks(itemsToExport, location)
                    }
                }
                ExportTarget.ALL_RECIPES -> {
                    exportAllRecipes(location)
                }
            }
        } catch (e: Exception) {
            updateOperationStatus(operation.id, OperationStatus.FAILED, e.message ?: "Unknown error")
            _actions.emit(ExportImportAction.ShowErrorMessage("Export failed: ${e.message}"))
        }
    }
    
    private suspend fun exportSingleRecipe(recipeId: String, location: String?) {
        val currentState = _state.value
        val operation = currentOperation ?: return
        
        try {
            val result = exportUseCases(
                recipeId,
                currentState.exportSettings.format,
                currentState.exportSettings
            )
            
            result.onSuccess { fileInfo ->
                updateOperationStatus(operation.id, OperationStatus.COMPLETED)
                _actions.emit(ExportImportAction.OperationCompleted(operation))
                _actions.emit(ExportImportAction.ShowMessage("Recipe exported successfully to ${fileInfo.filePath}"))
                
                // Save to history
                saveOperationToHistory(operation.copy(
                    status = OperationStatus.COMPLETED,
                    filePaths = listOf(fileInfo.filePath),
                    successCount = 1
                ))
            }.onFailure { e ->
                updateOperationStatus(operation.id, OperationStatus.FAILED, e.message ?: "Unknown error")
                _actions.emit(ExportImportAction.OperationFailed(operation, e.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            updateOperationStatus(operation.id, OperationStatus.FAILED, e.message ?: "Unknown error")
            _actions.emit(ExportImportAction.OperationFailed(operation, e.message ?: "Unknown error"))
        } finally {
            _state.update { it.copy(isOperationInProgress = false) }
        }
    }
    
    private suspend fun exportMultipleRecipes(recipeIds: List<String>, location: String?) {
        val currentState = _state.value
        val operation = currentOperation ?: return
        
        try {
            val result = exportUseCases.exportRecipes(
                recipeIds,
                currentState.exportSettings.format,
                currentState.exportSettings
            )
            
            result.onSuccess { fileInfo ->
                updateOperationStatus(operation.id, OperationStatus.COMPLETED)
                _actions.emit(ExportImportAction.OperationCompleted(operation))
                _actions.emit(ExportImportAction.ShowMessage("${recipeIds.size} recipes exported successfully"))
                
                saveOperationToHistory(operation.copy(
                    status = OperationStatus.COMPLETED,
                    filePaths = listOf(fileInfo.filePath),
                    successCount = recipeIds.size
                ))
            }.onFailure { e ->
                updateOperationStatus(operation.id, OperationStatus.FAILED, e.message ?: "Unknown error")
                _actions.emit(ExportImportAction.OperationFailed(operation, e.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            updateOperationStatus(operation.id, OperationStatus.FAILED, e.message ?: "Unknown error")
            _actions.emit(ExportImportAction.OperationFailed(operation, e.message ?: "Unknown error"))
        } finally {
            _state.update { it.copy(isOperationInProgress = false) }
        }
    }
    
    private suspend fun exportSingleCookbook(cookbookId: String, location: String?) {
        val currentState = _state.value
        val operation = currentOperation ?: return
        
        try {
            val result = exportUseCases.exportCookbook(
                cookbookId,
                currentState.exportSettings.format,
                currentState.exportSettings
            )
            
            result.onSuccess { fileInfo ->
                updateOperationStatus(operation.id, OperationStatus.COMPLETED)
                _actions.emit(ExportImportAction.OperationCompleted(operation))
                _actions.emit(ExportImportAction.ShowMessage("Cookbook exported successfully"))
                
                saveOperationToHistory(operation.copy(
                    status = OperationStatus.COMPLETED,
                    filePaths = listOf(fileInfo.filePath),
                    successCount = 1
                ))
            }.onFailure { e ->
                updateOperationStatus(operation.id, OperationStatus.FAILED, e.message ?: "Unknown error")
                _actions.emit(ExportImportAction.OperationFailed(operation, e.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            updateOperationStatus(operation.id, OperationStatus.FAILED, e.message ?: "Unknown error")
            _actions.emit(ExportImportAction.OperationFailed(operation, e.message ?: "Unknown error"))
        } finally {
            _state.update { it.copy(isOperationInProgress = false) }
        }
    }
    
    private suspend fun exportMultipleCookbooks(cookbookIds: List<String>, location: String?) {
        val currentState = _state.value
        val operation = currentOperation ?: return
        
        try {
            val result = exportUseCases.batchExport(
                cookbookIds.map { "cookbook_$it" },
                currentState.exportSettings.format,
                currentState.exportSettings
            ) { processed, total ->
                updateOperationProgress(operation.id, processed, total)
            }
            
            result.onSuccess { batchResult ->
                updateOperationStatus(operation.id, OperationStatus.COMPLETED)
                _actions.emit(ExportImportAction.OperationCompleted(operation))
                _actions.emit(ExportImportAction.ShowMessage("${batchResult.successfulItems} cookbooks exported successfully"))
                
                saveOperationToHistory(operation.copy(
                    status = OperationStatus.COMPLETED,
                    successCount = batchResult.successfulItems,
                    failureCount = batchResult.failedItems,
                    errorMessages = batchResult.errorMessages.values.toList()
                ))
            }.onFailure { e ->
                updateOperationStatus(operation.id, OperationStatus.FAILED, e.message ?: "Unknown error")
                _actions.emit(ExportImportAction.OperationFailed(operation, e.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            updateOperationStatus(operation.id, OperationStatus.FAILED, e.message ?: "Unknown error")
            _actions.emit(ExportImportAction.OperationFailed(operation, e.message ?: "Unknown error"))
        } finally {
            _state.update { it.copy(isOperationInProgress = false) }
        }
    }
    
    private suspend fun exportAllRecipes(location: String?) {
        val currentState = _state.value
        val operation = currentOperation ?: return
        
        try {
            val result = exportUseCases.exportAllRecipes(
                currentState.exportSettings.format,
                currentState.exportSettings
            )
            
            result.onSuccess { fileInfo ->
                updateOperationStatus(operation.id, OperationStatus.COMPLETED)
                _actions.emit(ExportImportAction.OperationCompleted(operation))
                _actions.emit(ExportImportAction.ShowMessage("All recipes exported successfully"))
                
                saveOperationToHistory(operation.copy(
                    status = OperationStatus.COMPLETED,
                    filePaths = listOf(fileInfo.filePath),
                    successCount = fileInfo.recipeCount
                ))
            }.onFailure { e ->
                updateOperationStatus(operation.id, OperationStatus.FAILED, e.message ?: "Unknown error")
                _actions.emit(ExportImportAction.OperationFailed(operation, e.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            updateOperationStatus(operation.id, OperationStatus.FAILED, e.message ?: "Unknown error")
            _actions.emit(ExportImportAction.OperationFailed(operation, e.message ?: "Unknown error"))
        } finally {
            _state.update { it.copy(isOperationInProgress = false) }
        }
    }
    
    private suspend fun cancelExport(operationId: String) {
        updateOperationStatus(operationId, OperationStatus.CANCELLED)
        _state.update { 
            it.copy(
                isOperationInProgress = false,
                showProgressDialog = false
            )
        }
        currentOperation = null
    }
    
    // ==================== IMPORT FUNCTIONS ====================
    
    private suspend fun selectImportFormat(format: ImportFormat) {
        _state.update { 
            it.copy(
                importSettings = it.importSettings.copy(format = format),
                showFormatSelection = false
            )
        }
    }
    
    private suspend fun selectImportTarget(target: ImportTarget) {
        _state.update { it.copy(importSettings = it.importSettings.copy(target = target)) }
    }
    
    private suspend fun addFilesForImport(filePaths: List<String>) {
        _state.update { state ->
            val updatedPaths = (state.selectedFilePaths + filePaths).distinct()
            state.copy(selectedFilePaths = updatedPaths)
        }
    }
    
    private suspend fun removeFileFromImport(filePath: String) {
        _state.update { state ->
            state.copy(selectedFilePaths = state.selectedFilePaths - filePath)
        }
    }
    
    private suspend fun updateImportSettings(settings: ImportSettings) {
        _state.update { it.copy(importSettings = settings) }
    }
    
    private suspend fun startImport(location: String?) {
        val currentState = _state.value
        
        if (currentState.selectedFilePaths.isEmpty()) {
            _actions.emit(ExportImportAction.ShowErrorMessage("Please select files to import"))
            return
        }
        
        // Create operation
        val operation = importUseCases.createImportOperation(
            currentState.selectedFilePaths.size,
            currentState.importSettings.format
        )
        
        currentOperation = operation
        
        // Update state
        _state.update { 
            it.copy(
                isOperationInProgress = true,
                currentOperations = it.currentOperations + operation,
                showProgressDialog = true
            )
        }
        
        try {
            // For now, we'll import all selected files
            // In a real implementation, we might want to handle different import targets
            importMultipleFiles(currentState.selectedFilePaths, location)
        } catch (e: Exception) {
            updateOperationStatus(operation.id, OperationStatus.FAILED, e.message ?: "Unknown error")
            _actions.emit(ExportImportAction.ShowErrorMessage("Import failed: ${e.message}"))
        }
    }
    
    private suspend fun importMultipleFiles(filePaths: List<String>, location: String?) {
        val currentState = _state.value
        val operation = currentOperation ?: return
        
        try {
            val result = importUseCases.batchImport(
                filePaths,
                currentState.importSettings.format,
                currentState.importSettings,
                onProgress = { processed, total ->
                    updateOperationProgress(operation.id, processed, total)
                },
                onConflict = { com.ourcookbook.domain.model.ConflictResolution.KeepLocal }
            )

            result.onSuccess { batchResult ->
                updateOperationStatus(operation.id, OperationStatus.COMPLETED)
                _actions.emit(ExportImportAction.OperationCompleted(operation))

                if (batchResult.hasFailures) {
                    _actions.emit(ExportImportAction.ShowMessage(
                        "Import completed with ${batchResult.failedItems} failures"
                    ))
                } else {
                    _actions.emit(ExportImportAction.ShowMessage(
                        "${batchResult.successfulItems} items imported successfully"
                    ))
                }
                
                saveOperationToHistory(operation.copy(
                    status = OperationStatus.COMPLETED,
                    successCount = batchResult.successfulItems,
                    failureCount = batchResult.failedItems,
                    errorMessages = batchResult.errorMessages.values.toList()
                ))
                
                // Clear selected files after import
                _state.update { it.copy(selectedFilePaths = emptyList()) }
                
            }.onFailure { e ->
                updateOperationStatus(operation.id, OperationStatus.FAILED, e.message ?: "Unknown error")
                _actions.emit(ExportImportAction.OperationFailed(operation, e.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            updateOperationStatus(operation.id, OperationStatus.FAILED, e.message ?: "Unknown error")
            _actions.emit(ExportImportAction.OperationFailed(operation, e.message ?: "Unknown error"))
        } finally {
            _state.update { it.copy(isOperationInProgress = false) }
        }
    }
    
    private suspend fun previewImport(filePath: String) {
        val currentState = _state.value
        
        try {
            val result = importUseCases.previewImport(
                filePath,
                currentState.importSettings.format
            )
            
            result.onSuccess { preview ->
                _state.update { 
                    it.copy(
                        importPreview = preview,
                        showPreviewDialog = true
                    )
                }
            }.onFailure { e ->
                _actions.emit(ExportImportAction.ShowErrorMessage("Preview failed: ${e.message}"))
            }
        } catch (e: Exception) {
            _actions.emit(ExportImportAction.ShowErrorMessage("Preview failed: ${e.message}"))
        }
    }
    
    private suspend fun cancelImport(operationId: String) {
        updateOperationStatus(operationId, OperationStatus.CANCELLED)
        _state.update { 
            it.copy(
                isOperationInProgress = false,
                showProgressDialog = false
            )
        }
        currentOperation = null
    }
    
    // ==================== CONFLICT RESOLUTION ====================
    
    private suspend fun resolveConflict(resolution: ConflictResolution) {
        val currentState = _state.value
        val conflict = currentState.currentConflict ?: return

        try {
            val result = importUseCases.resolveConflict(conflict, mapToDomainResolution(resolution))
            
            result.onSuccess { recipe ->
                if (recipe != null) {
                    // Recipe was imported successfully
                    _actions.emit(ExportImportAction.ShowMessage("Conflict resolved: Recipe imported"))
                } else {
                    // Conflict was skipped or existing kept
                    _actions.emit(ExportImportAction.ShowMessage("Conflict resolved: Existing recipe kept"))
                }
                
                // Clear the current conflict
                _state.update { 
                    it.copy(
                        currentConflict = null,
                        showConflictDialog = false
                    )
                }
                
                // Continue with the import process
                // In a real implementation, we would resume the import
                
            }.onFailure { e ->
                _actions.emit(ExportImportAction.ShowErrorMessage("Failed to resolve conflict: ${e.message}"))
            }
        } catch (e: Exception) {
            _actions.emit(ExportImportAction.ShowErrorMessage("Failed to resolve conflict: ${e.message}"))
        }
    }
    
    private suspend fun skipConflict() {
        val currentState = _state.value
        
        // Skip the current conflict
        _state.update { 
            it.copy(
                currentConflict = null,
                showConflictDialog = false
            )
        }
        
        _actions.emit(ExportImportAction.ShowMessage("Conflict skipped"))
    }
    
    private suspend fun overwriteConflict() {
        resolveConflict(ConflictResolution.REPLACE_WITH_NEW)
    }

    private fun mapToDomainResolution(resolution: ConflictResolution): com.ourcookbook.domain.model.ConflictResolution {
        return when (resolution) {
            ConflictResolution.KEEP_EXISTING -> com.ourcookbook.domain.model.ConflictResolution.KeepLocal
            ConflictResolution.REPLACE_WITH_NEW -> com.ourcookbook.domain.model.ConflictResolution.KeepRemote
            ConflictResolution.MERGE -> com.ourcookbook.domain.model.ConflictResolution.KeepLocal
            ConflictResolution.SKIP -> com.ourcookbook.domain.model.ConflictResolution.KeepLocal
        }
    }
    
    // ==================== OPERATION HISTORY ====================
    
    private suspend fun loadOperationHistory() {
        try {
            val history = withContext(Dispatchers.IO) {
                // In a real implementation, we would load from the repository
                // For now, return empty list
                emptyList<ExportImportOperation>()
            }
            _state.update { it.copy(operationHistory = history) }
        } catch (e: Exception) {
            _actions.emit(ExportImportAction.ShowErrorMessage("Failed to load history: ${e.message}"))
        }
    }
    
    private suspend fun retryOperation(operationId: String) {
        // Find the operation in history
        val operation = _state.value.operationHistory.find { it.id == operationId }
        if (operation == null) {
            _actions.emit(ExportImportAction.ShowErrorMessage("Operation not found"))
            return
        }
        
        when (operation.type) {
            OperationType.EXPORT -> {
                // Recreate the export operation
                val exportTarget = try {
                    ExportTarget.valueOf(operation.target)
                } catch (e: Exception) {
                    ExportTarget.INDIVIDUAL_RECIPE
                }
                
                val exportFormat = try {
                    ExportFormat.valueOf(operation.format)
                } catch (e: Exception) {
                    ExportFormat.JSON
                }
                
                val newOperation = exportUseCases.createExportOperation(
                    exportTarget,
                    exportFormat,
                    operation.totalItems
                )
                
                currentOperation = newOperation
                
                _state.update { 
                    it.copy(
                        currentOperations = it.currentOperations + newOperation,
                        isOperationInProgress = true
                    )
                }
                
                // For now, just show a message
                _actions.emit(ExportImportAction.ShowMessage("Retrying export operation..."))
                
                // In a real implementation, we would retry the actual export
            }
            OperationType.IMPORT -> {
                // Recreate the import operation
                val importFormat = try {
                    ImportFormat.valueOf(operation.format)
                } catch (e: Exception) {
                    ImportFormat.JSON
                }
                
                val newOperation = importUseCases.createImportOperation(
                    operation.totalItems,
                    importFormat
                )
                
                currentOperation = newOperation
                
                _state.update { 
                    it.copy(
                        currentOperations = it.currentOperations + newOperation,
                        isOperationInProgress = true
                    )
                }
                
                // For now, just show a message
                _actions.emit(ExportImportAction.ShowMessage("Retrying import operation..."))
                
                // In a real implementation, we would retry the actual import
            }
        }
    }
    
    private suspend fun clearOperationHistory() {
        try {
            // In a real implementation, we would clear the history from the repository
            _state.update { it.copy(operationHistory = emptyList()) }
            _actions.emit(ExportImportAction.ShowMessage("Operation history cleared"))
        } catch (e: Exception) {
            _actions.emit(ExportImportAction.ShowErrorMessage("Failed to clear history: ${e.message}"))
        }
    }
    
    private suspend fun saveOperationToHistory(operation: ExportImportOperation) {
        // In a real implementation, we would save to the repository
        _state.update { state ->
            val updatedHistory = (state.operationHistory + operation).sortedByDescending { it.timestamp }
            state.copy(operationHistory = updatedHistory)
        }
    }
    
    // ==================== CLOUD INTEGRATION ====================
    
    private suspend fun connectToDrive() {
        // In a real implementation, we would connect to Google Drive
        _state.update { it.copy(isDriveConnected = true) }
        _actions.emit(ExportImportAction.ShowMessage("Connected to Google Drive"))
    }
    
    private suspend fun disconnectFromDrive() {
        // In a real implementation, we would disconnect from Google Drive
        _state.update { it.copy(isDriveConnected = false) }
        _actions.emit(ExportImportAction.ShowMessage("Disconnected from Google Drive"))
    }
    
    private suspend fun loadDriveFiles() {
        try {
            val result = importUseCases.listDriveFiles()
            
            result.onSuccess { files ->
                _state.update { it.copy(driveFiles = files) }
            }.onFailure { e ->
                _actions.emit(ExportImportAction.ShowErrorMessage("Failed to load Drive files: ${e.message}"))
            }
        } catch (e: Exception) {
            _actions.emit(ExportImportAction.ShowErrorMessage("Failed to load Drive files: ${e.message}"))
        }
    }
    
    private suspend fun exportToDrive(folderId: String?) {
        val currentState = _state.value
        
        if (currentState.selectedRecipeIds.isEmpty() && currentState.selectedCookbookIds.isEmpty()) {
            _actions.emit(ExportImportAction.ShowErrorMessage("Please select items to export"))
            return
        }
        
        // In a real implementation, we would export to Google Drive
        _actions.emit(ExportImportAction.ShowMessage("Exporting to Google Drive..."))
        
        // For now, just show a success message
        viewModelScope.launch {
            kotlinx.coroutines.delay(2000) // Simulate delay
            _actions.emit(ExportImportAction.ShowMessage("Export to Google Drive completed"))
        }
    }
    
    private suspend fun importFromDrive(fileId: String) {
        try {
            val result = importUseCases.importFromDrive(fileId)
            
            result.onSuccess { (content, fileName) ->
                _actions.emit(ExportImportAction.ShowMessage("Imported from Google Drive: $fileName"))
                // In a real implementation, we would process the imported content
            }.onFailure { e ->
                _actions.emit(ExportImportAction.ShowErrorMessage("Failed to import from Drive: ${e.message}"))
            }
        } catch (e: Exception) {
            _actions.emit(ExportImportAction.ShowErrorMessage("Failed to import from Drive: ${e.message}"))
        }
    }
    
    // ==================== BATCH OPERATIONS ====================
    
    private suspend fun startBatchExport(items: List<String>) {
        val currentState = _state.value
        
        if (items.isEmpty()) {
            _actions.emit(ExportImportAction.ShowErrorMessage("Please select items to export"))
            return
        }
        
        // Create operation
        val operation = exportUseCases.createExportOperation(
            ExportTarget.INDIVIDUAL_RECIPE, // or ENTIRE_COOKBOOK based on items
            currentState.exportSettings.format,
            items.size
        )
        
        currentOperation = operation
        
        _state.update { 
            it.copy(
                isOperationInProgress = true,
                currentOperations = it.currentOperations + operation,
                showProgressDialog = true
            )
        }
        
        try {
            val result = exportUseCases.batchExport(
                items,
                currentState.exportSettings.format,
                currentState.exportSettings
            ) { processed, total ->
                updateOperationProgress(operation.id, processed, total)
            }
            
            result.onSuccess { batchResult ->
                updateOperationStatus(operation.id, OperationStatus.COMPLETED)
                _state.update { 
                    it.copy(
                        batchOperationResult = batchResult,
                        showBatchResults = true
                    )
                }
                _actions.emit(ExportImportAction.OperationCompleted(operation))
                
                saveOperationToHistory(operation.copy(
                    status = OperationStatus.COMPLETED,
                    successCount = batchResult.successfulItems,
                    failureCount = batchResult.failedItems,
                    errorMessages = batchResult.errorMessages.values.toList()
                ))
                
            }.onFailure { e ->
                updateOperationStatus(operation.id, OperationStatus.FAILED, e.message ?: "Unknown error")
                _actions.emit(ExportImportAction.OperationFailed(operation, e.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            updateOperationStatus(operation.id, OperationStatus.FAILED, e.message ?: "Unknown error")
            _actions.emit(ExportImportAction.OperationFailed(operation, e.message ?: "Unknown error"))
        } finally {
            _state.update { it.copy(isOperationInProgress = false) }
        }
    }
    
    private suspend fun startBatchImport(filePaths: List<String>) {
        val currentState = _state.value
        
        if (filePaths.isEmpty()) {
            _actions.emit(ExportImportAction.ShowErrorMessage("Please select files to import"))
            return
        }
        
        // Create operation
        val operation = importUseCases.createImportOperation(
            filePaths.size,
            currentState.importSettings.format
        )
        
        currentOperation = operation
        
        _state.update { 
            it.copy(
                isOperationInProgress = true,
                currentOperations = it.currentOperations + operation,
                showProgressDialog = true
            )
        }
        
        try {
            val result = importUseCases.batchImport(
                filePaths,
                currentState.importSettings.format,
                currentState.importSettings,
                onProgress = { processed, total ->
                    updateOperationProgress(operation.id, processed, total)
                },
                onConflict = { com.ourcookbook.domain.model.ConflictResolution.KeepLocal }
            )

            result.onSuccess { batchResult ->
                updateOperationStatus(operation.id, OperationStatus.COMPLETED)
                _state.update {
                    it.copy(
                        batchOperationResult = batchResult,
                        showBatchResults = true
                    )
                }
                _actions.emit(ExportImportAction.OperationCompleted(operation))
                
                saveOperationToHistory(operation.copy(
                    status = OperationStatus.COMPLETED,
                    successCount = batchResult.successfulItems,
                    failureCount = batchResult.failedItems,
                    errorMessages = batchResult.errorMessages.values.toList()
                ))
                
            }.onFailure { e ->
                updateOperationStatus(operation.id, OperationStatus.FAILED, e.message ?: "Unknown error")
                _actions.emit(ExportImportAction.OperationFailed(operation, e.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            updateOperationStatus(operation.id, OperationStatus.FAILED, e.message ?: "Unknown error")
            _actions.emit(ExportImportAction.OperationFailed(operation, e.message ?: "Unknown error"))
        } finally {
            _state.update { it.copy(isOperationInProgress = false) }
        }
    }
    
    private suspend fun clearBatchResults() {
        _state.update { 
            it.copy(
                batchOperationResult = null,
                showBatchResults = false
            )
        }
    }
    
    // ==================== UI STATE FUNCTIONS ====================
    
    private suspend fun showFormatSelection() {
        _state.update { it.copy(showFormatSelection = true) }
    }
    
    private suspend fun hideFormatSelection() {
        _state.update { it.copy(showFormatSelection = false) }
    }
    
    private suspend fun showPreviewDialog() {
        _state.update { it.copy(showPreviewDialog = true) }
    }
    
    private suspend fun hidePreviewDialog() {
        _state.update { it.copy(showPreviewDialog = false) }
    }
    
    private suspend fun showProgressDialog() {
        _state.update { it.copy(showProgressDialog = true) }
    }
    
    private suspend fun hideProgressDialog() {
        _state.update { it.copy(showProgressDialog = false) }
    }
    
    private suspend fun showConflictDialog() {
        _state.update { it.copy(showConflictDialog = true) }
    }
    
    private suspend fun hideConflictDialog() {
        _state.update { it.copy(showConflictDialog = false) }
    }
    
    private suspend fun showHistoryDialog() {
        _state.update { it.copy(showHistoryDialog = true) }
    }
    
    private suspend fun hideHistoryDialog() {
        _state.update { it.copy(showHistoryDialog = false) }
    }
    
    private suspend fun showLocationSelection() {
        _state.update { it.copy(showLocationSelection = true) }
    }
    
    private suspend fun hideLocationSelection() {
        _state.update { it.copy(showLocationSelection = false) }
    }
    
    private suspend fun showFilePicker() {
        _state.update { it.copy(showFilePicker = true) }
    }
    
    private suspend fun hideFilePicker() {
        _state.update { it.copy(showFilePicker = false) }
    }
    
    // ==================== NAVIGATION ====================
    
    private suspend fun navigateBack() {
        _actions.emit(ExportImportAction.NavigationAction("back"))
    }
    
    private suspend fun navigateToExportSettings() {
        _actions.emit(ExportImportAction.NavigationAction("export_settings"))
    }
    
    private suspend fun navigateToImportSettings() {
        _actions.emit(ExportImportAction.NavigationAction("import_settings"))
    }
    
    private suspend fun navigateToOperationHistory() {
        _actions.emit(ExportImportAction.NavigationAction("operation_history"))
    }
    
    // ==================== ERROR HANDLING ====================
    
    private suspend fun showError(message: String) {
        _state.update { it.copy(errorMessage = message) }
    }
    
    private suspend fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }
    
    private suspend fun showSuccess(message: String) {
        _state.update { it.copy(successMessage = message) }
    }
    
    private suspend fun clearSuccess() {
        _state.update { it.copy(successMessage = null) }
    }
    
    // ==================== OPERATION HELPERS ====================
    
    private suspend fun updateOperationStatus(
        operationId: String,
        status: OperationStatus,
        errorMessage: String? = null
    ) {
        _state.update { state ->
            val updatedOperations = state.currentOperations.map { op ->
                if (op.id == operationId) {
                    op.copy(
                        status = status,
                        errorMessages = if (errorMessage != null) op.errorMessages + errorMessage else op.errorMessages
                    )
                } else {
                    op
                }
            }
            
            val updatedHistory = state.operationHistory.map { op ->
                if (op.id == operationId) {
                    op.copy(status = status)
                } else {
                    op
                }
            }
            
            state.copy(
                currentOperations = updatedOperations,
                operationHistory = updatedHistory
            )
        }
        
        if (status == OperationStatus.COMPLETED || status == OperationStatus.FAILED || status == OperationStatus.CANCELLED) {
            currentOperation = null
        }
    }
    
    private fun updateOperationProgress(
        operationId: String,
        processedItems: Int,
        totalItems: Int
    ) {
        val progress = if (totalItems > 0) processedItems.toFloat() / totalItems else 0f
        
        _state.update { state ->
            val updatedOperations = state.currentOperations.map { op ->
                if (op.id == operationId) {
                    op.copy(
                        processedItems = processedItems,
                        progress = progress
                    )
                } else {
                    op
                }
            }
            
            state.copy(
                currentOperations = updatedOperations,
                currentProgress = progress
            )
        }
        
        _actions.tryEmit(ExportImportAction.UpdateProgress(progress))
    }
    
    /**
     * Clear the current action
     */
    fun clearAction() {
        viewModelScope.launch {
            // Clear any temporary state
            _state.update { 
                it.copy(
                    errorMessage = null,
                    successMessage = null
                )
            }
        }
    }
}
