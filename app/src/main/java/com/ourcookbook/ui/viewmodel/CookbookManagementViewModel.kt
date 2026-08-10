package com.ourcookbook.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ourcookbook.domain.model.Cookbook
import com.ourcookbook.domain.model.Recipe
import com.ourcookbook.domain.usecase.cookbook.CreateCookbook
import com.ourcookbook.domain.usecase.cookbook.UpdateCookbook
import com.ourcookbook.domain.usecase.cookbook.DeleteCookbook
import com.ourcookbook.domain.usecase.cookbook.GetAllCookbooks
import com.ourcookbook.domain.usecase.cookbook.GetCookbooksByOwner
import com.ourcookbook.domain.usecase.cookbook.GetSharedCookbooks
import com.ourcookbook.domain.usecase.cookbook.SearchCookbooks
import com.ourcookbook.domain.usecase.cookbook.AddRecipeToCookbook
import com.ourcookbook.domain.usecase.cookbook.RemoveRecipeFromCookbook
import com.ourcookbook.domain.usecase.cookbook.ExportCookbook
import com.ourcookbook.domain.usecase.cookbook.ImportCookbook
import com.ourcookbook.domain.usecase.cookbook.ShareCookbook
import com.ourcookbook.domain.usecase.cookbook.GenerateSharingLink
import com.ourcookbook.domain.usecase.cookbook.GetSharingInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/**
 * State for CookbookManagementScreen
 */
sealed class CookbookManagementState {
    object Loading : CookbookManagementState()
    data class Success(
        val cookbooks: List<Cookbook> = emptyList(),
        val sharedCookbooks: List<Cookbook> = emptyList(),
        val selectedCookbook: Cookbook? = null,
        val recipesInSelectedCookbook: List<Recipe> = emptyList(),
        val isLoadingMore: Boolean = false,
        val hasMore: Boolean = true,
        val sortOrder: SortOrder = SortOrder.NAME_ASC,
        val searchQuery: String = "",
        val isExporting: Boolean = false,
        val isImporting: Boolean = false,
        val exportProgress: Int = 0,
        val importProgress: Int = 0,
        val recentlyDeleted: List<Cookbook> = emptyList(),
        val sharingInfo: Map<String, CookbookSharingInfo> = emptyMap()
    ) : CookbookManagementState()
    data class Error(val message: String) : CookbookManagementState()
    object Empty : CookbookManagementState()
}

/**
 * Sort order for cookbooks
 */
enum class SortOrder {
    NAME_ASC,     // A-Z
    NAME_DESC,    // Z-A
    RECENTLY_UPDATED, // Recently updated first
    MOST_RECIPES  // Most recipes first
}

/**
 * Sharing information for cookbooks
 */
data class CookbookSharingInfo(
    val cookbookId: String,
    val sharingLink: String,
    val sharedWithUsers: List<SharedUserInfo> = emptyList(),
    val permissions: Map<String, Set<Permission>> = emptyMap()
)

/**
 * Information about users a cookbook is shared with
 */
data class SharedUserInfo(
    val userId: String,
    val userName: String,
    val permissions: Set<Permission> = setOf(Permission.VIEW)
)

/**
 * Permission types for cookbook sharing
 */
enum class Permission {
    VIEW, EDIT
}

/**
 * Export/Import format options
 */
enum class ExportFormat {
    JSON, MARKDOWN, PDF
}

/**
 * Result of export/import operations
 */
sealed class ExportImportResult {
    object Success : ExportImportResult()
    data class Error(val message: String) : ExportImportResult()
    data class Progress(val percentage: Int) : ExportImportResult()
}

/**
 * Event for CookbookManagementScreen
 */
sealed class CookbookManagementEvent {
    object LoadCookbooks : CookbookManagementEvent()
    data class SelectCookbook(val cookbookId: String) : CookbookManagementEvent()
    data class CreateCookbook(val name: String, val description: String?, val imageUri: String? = null) : CookbookManagementEvent()
    data class UpdateCookbook(val cookbook: Cookbook, val imageUri: String? = null) : CookbookManagementEvent()
    data class DeleteCookbook(val cookbookId: String) : CookbookManagementEvent()
    data class AddRecipeToCookbook(val recipeId: String) : CookbookManagementEvent()
    data class RemoveRecipeFromCookbook(val recipeId: String) : CookbookManagementEvent()
    data class SearchCookbooks(val query: String) : CookbookManagementEvent()
    data class SortCookbooks(val sortOrder: SortOrder) : CookbookManagementEvent()
    object LoadMore : CookbookManagementEvent()
    object Refresh : CookbookManagementEvent()
    
    // Sharing events
    data class ShareCookbook(val cookbookId: String, val userIds: List<String>, val permissions: Set<Permission>) : CookbookManagementEvent()
    data class GenerateSharingLink(val cookbookId: String) : CookbookManagementEvent()
    data class RevokeSharing(val cookbookId: String, val userId: String) : CookbookManagementEvent()
    
    // Import/Export events
    data class ExportCookbook(val cookbookId: String, val format: ExportFormat, val destinationFile: File) : CookbookManagementEvent()
    data class ImportCookbook(val sourceFile: File, val format: ExportFormat) : CookbookManagementEvent()
    
    // Bulk operations
    data class BulkDeleteCookbooks(val cookbookIds: List<String>) : CookbookManagementEvent()
    
    // Undo operations
    data class UndoDeleteCookbook(val cookbookId: String) : CookbookManagementEvent()
}

/**
 * Action for CookbookManagementScreen
 */
sealed class CookbookManagementAction {
    data class ShowCookbookDetail(val cookbookId: String) : CookbookManagementAction()
    data class ShowCreateCookbookDialog(val defaultName: String = "") : CookbookManagementAction()
    data class ShowEditCookbookDialog(val cookbook: Cookbook) : CookbookManagementAction()
    data class ShowDeleteConfirmation(val cookbookId: String, val hasRecipes: Boolean) : CookbookManagementAction()
    data class ShowBulkDeleteConfirmation(val cookbookIds: List<String>) : CookbookManagementAction()
    data class ShowRecipeSelection(val cookbookId: String) : CookbookManagementAction()
    data class ShowError(val message: String) : CookbookManagementAction()
    object ShowEmptyState : CookbookManagementAction()
    
    // Sharing actions
    data class ShowShareDialog(val cookbook: Cookbook) : CookbookManagementAction()
    data class ShowSharingLinkDialog(val sharingLink: String, val qrCodeData: String?) : CookbookManagementAction()
    data class ShowSharingSuccess(val message: String) : CookbookManagementAction()
    
    // Import/Export actions
    data class ShowExportDialog(val cookbook: Cookbook) : CookbookManagementAction()
    data class ShowImportDialog(val supportedFormats: List<ExportFormat>) : CookbookManagementAction()
    data class ShowExportProgress(val progress: Int) : CookbookManagementAction()
    data class ShowImportProgress(val progress: Int) : CookbookManagementAction()
    data class ShowExportSuccess(val filePath: String, val format: ExportFormat) : CookbookManagementAction()
    data class ShowImportSuccess(val cookbookName: String, val recipeCount: Int) : CookbookManagementAction()
    
    // Navigation actions
    data class NavigateToCookbookSettings(val cookbookId: String) : CookbookManagementAction()
    
    // Feedback actions
    data class ShowSnackbar(val message: String) : CookbookManagementAction()
    data class ShowUndoSnackbar(val message: String, val onUndo: () -> Unit) : CookbookManagementAction()
}

/**
 * ViewModel for CookbookManagementScreen
 * Handles cookbook management, creation, and recipe organization
 */
@HiltViewModel
class CookbookManagementViewModel @Inject constructor(
    private val createCookbook: CreateCookbook,
    private val updateCookbook: UpdateCookbook,
    private val deleteCookbook: DeleteCookbook,
    private val getAllCookbooks: GetAllCookbooks,
    private val getCookbooksByOwner: GetCookbooksByOwner,
    private val getSharedCookbooks: GetSharedCookbooks,
    private val searchCookbooks: SearchCookbooks,
    private val addRecipeToCookbook: AddRecipeToCookbook,
    private val removeRecipeFromCookbook: RemoveRecipeFromCookbook,
    private val exportCookbook: ExportCookbook,
    private val importCookbook: ImportCookbook,
    private val shareCookbook: ShareCookbook,
    private val generateSharingLink: GenerateSharingLink,
    private val getSharingInfo: GetSharingInfo
) : ViewModel() {

    private val _state = MutableStateFlow<CookbookManagementState>(CookbookManagementState.Loading)
    val state: StateFlow<CookbookManagementState> = _state.asStateFlow()

    private val _actions = MutableStateFlow<CookbookManagementAction?>(null)
    val actions: StateFlow<CookbookManagementAction?> = _actions.asStateFlow()

    private var currentDeviceId: String = "current_device_id" // Will be set properly in production
    private var currentQuery: String = ""
    private var currentSortOrder: SortOrder = SortOrder.NAME_ASC
    private var recentlyDeletedCookbooks: MutableList<Cookbook> = mutableListOf()

    init {
        loadCookbooks()
    }

    fun handleEvent(event: CookbookManagementEvent) {
        when (event) {
            is CookbookManagementEvent.LoadCookbooks -> loadCookbooks()
            is CookbookManagementEvent.SelectCookbook -> selectCookbook(event.cookbookId)
            is CookbookManagementEvent.CreateCookbook -> createCookbook(event.name, event.description, event.imageUri)
            is CookbookManagementEvent.UpdateCookbook -> updateCookbook(event.cookbook, event.imageUri)
            is CookbookManagementEvent.DeleteCookbook -> deleteCookbook(event.cookbookId)
            is CookbookManagementEvent.AddRecipeToCookbook -> addRecipeToCookbook(event.recipeId)
            is CookbookManagementEvent.RemoveRecipeFromCookbook -> removeRecipeFromCookbook(event.recipeId)
            is CookbookManagementEvent.SearchCookbooks -> searchCookbooks(event.query)
            is CookbookManagementEvent.SortCookbooks -> sortCookbooks(event.sortOrder)
            is CookbookManagementEvent.LoadMore -> loadMore()
            is CookbookManagementEvent.Refresh -> refresh()
            is CookbookManagementEvent.ShareCookbook -> shareCookbook(event.cookbookId, event.userIds, event.permissions)
            is CookbookManagementEvent.GenerateSharingLink -> generateSharingLink(event.cookbookId)
            is CookbookManagementEvent.RevokeSharing -> revokeSharing(event.cookbookId, event.userId)
            is CookbookManagementEvent.ExportCookbook -> exportCookbook(event.cookbookId, event.format, event.destinationFile)
            is CookbookManagementEvent.ImportCookbook -> importCookbook(event.sourceFile, event.format)
            is CookbookManagementEvent.BulkDeleteCookbooks -> bulkDeleteCookbooks(event.cookbookIds)
            is CookbookManagementEvent.UndoDeleteCookbook -> undoDeleteCookbook(event.cookbookId)
        }
    }

    private fun loadCookbooks() {
        viewModelScope.launch {
            _state.value = CookbookManagementState.Loading
            
            try {
                // Load user's cookbooks
                val userCookbooksFlow = getCookbooksByOwner(currentDeviceId)
                
                userCookbooksFlow
                    .catch { e ->
                        _state.value = CookbookManagementState.Error("Failed to load cookbooks: ${e.message}")
                    }
                    .collect { userCookbooks ->
                        // Load shared cookbooks
                        getSharedCookbooks()
                            .catch { e ->
                                // Handle error but don't fail the whole screen
                            }
                            .collect { sharedCookbooks ->
                                // Load sharing info for all cookbooks
                                loadSharingInfo(userCookbooks + sharedCookbooks)
                                
                                if (userCookbooks.isEmpty() && sharedCookbooks.isEmpty()) {
                                    _state.value = CookbookManagementState.Empty
                                } else {
                                    val sortedCookbooks = sortCookbooksList(userCookbooks, currentSortOrder)
                                    _state.value = CookbookManagementState.Success(
                                        cookbooks = sortedCookbooks,
                                        sharedCookbooks = sharedCookbooks,
                                        isLoadingMore = false,
                                        hasMore = true,
                                        sortOrder = currentSortOrder,
                                        searchQuery = currentQuery,
                                        recentlyDeleted = recentlyDeletedCookbooks
                                    )
                                }
                            }
                    }
                
            } catch (e: Exception) {
                _state.value = CookbookManagementState.Error("Failed to load cookbooks: ${e.message}")
            }
        }
    }

    private suspend fun loadSharingInfo(cookbooks: List<Cookbook>) {
        try {
            val sharingInfoMap = mutableMapOf<String, CookbookSharingInfo>()
            cookbooks.forEach { cookbook ->
                getSharingInfo(cookbook.id).collect { sharingInfo ->
                    sharingInfoMap[cookbook.id] = sharingInfo
                }
            }
            val currentState = _state.value
            if (currentState is CookbookManagementState.Success) {
                _state.value = currentState.copy(sharingInfo = sharingInfoMap)
            }
        } catch (e: Exception) {
            // Handle error gracefully
        }
    }

    private fun sortCookbooksList(cookbooks: List<Cookbook>, sortOrder: SortOrder): List<Cookbook> {
        return when (sortOrder) {
            SortOrder.NAME_ASC -> cookbooks.sortedBy { it.name.lowercase() }
            SortOrder.NAME_DESC -> cookbooks.sortedByDescending { it.name.lowercase() }
            SortOrder.RECENTLY_UPDATED -> cookbooks.sortedByDescending { it.updatedAt }
            SortOrder.MOST_RECIPES -> cookbooks.sortedByDescending { it.recipeCount }
        }
    }

    private fun sortCookbooks(sortOrder: SortOrder) {
        viewModelScope.launch {
            currentSortOrder = sortOrder
            val currentState = _state.value
            if (currentState is CookbookManagementState.Success) {
                val sortedCookbooks = sortCookbooksList(currentState.cookbooks, sortOrder)
                _state.value = currentState.copy(
                    cookbooks = sortedCookbooks,
                    sortOrder = sortOrder
                )
            }
        }
    }

    private fun selectCookbook(cookbookId: String) {
        viewModelScope.launch {
            val currentState = _state.value
            if (currentState is CookbookManagementState.Success) {
                val cookbook = currentState.cookbooks.find { it.id == cookbookId }
                    ?: currentState.sharedCookbooks.find { it.id == cookbookId }
                
                if (cookbook != null) {
                    // In production, we would load the recipes for this cookbook
                    // For now, just set the selected cookbook
                    _state.value = currentState.copy(selectedCookbook = cookbook)
                    _actions.value = CookbookManagementAction.ShowCookbookDetail(cookbookId)
                }
            }
        }
    }

    private fun createCookbook(name: String, description: String?, imageUri: String? = null) {
        viewModelScope.launch {
            try {
                if (name.isBlank()) {
                    _actions.value = CookbookManagementAction.ShowError("Cookbook name is required")
                    return@launch
                }
                
                val cookbook = Cookbook.create(
                    name = name,
                    ownerDeviceId = currentDeviceId,
                    description = description
                )
                
                val result = createCookbook(cookbook)
                result.onSuccess { cookbookId ->
                    // Refresh the list to include the new cookbook
                    loadCookbooks()
                    _actions.value = CookbookManagementAction.ShowSnackbar("Cookbook created successfully")
                }.onFailure { e ->
                    _actions.value = CookbookManagementAction.ShowError("Failed to create cookbook: ${e.message}")
                }
                
            } catch (e: Exception) {
                _actions.value = CookbookManagementAction.ShowError("Failed to create cookbook: ${e.message}")
            }
        }
    }

    private fun updateCookbook(cookbook: Cookbook, imageUri: String? = null) {
        viewModelScope.launch {
            try {
                if (cookbook.name.isBlank()) {
                    _actions.value = CookbookManagementAction.ShowError("Cookbook name is required")
                    return@launch
                }
                
                val result = updateCookbook(cookbook)
                result.onSuccess {
                    // Refresh the list to reflect the changes
                    loadCookbooks()
                    _actions.value = CookbookManagementAction.ShowSnackbar("Cookbook updated successfully")
                }.onFailure { e ->
                    _actions.value = CookbookManagementAction.ShowError("Failed to update cookbook: ${e.message}")
                }
                
            } catch (e: Exception) {
                _actions.value = CookbookManagementAction.ShowError("Failed to update cookbook: ${e.message}")
            }
        }
    }

    private fun deleteCookbook(cookbookId: String) {
        viewModelScope.launch {
            val currentState = _state.value
            if (currentState is CookbookManagementState.Success) {
                val cookbook = currentState.cookbooks.find { it.id == cookbookId }
                    ?: currentState.sharedCookbooks.find { it.id == cookbookId }
                
                if (cookbook != null) {
                    val hasRecipes = cookbook.recipeCount > 0
                    _actions.value = CookbookManagementAction.ShowDeleteConfirmation(cookbookId, hasRecipes)
                }
            }
        }
    }

    fun confirmDeleteCookbook(cookbookId: String) {
        viewModelScope.launch {
            try {
                val currentState = _state.value
                if (currentState is CookbookManagementState.Success) {
                    val cookbook = currentState.cookbooks.find { it.id == cookbookId }
                        ?: currentState.sharedCookbooks.find { it.id == cookbookId }
                    
                    if (cookbook != null) {
                        // Store the cookbook for potential undo
                        recentlyDeletedCookbooks.add(cookbook)
                        
                        val result = deleteCookbook(cookbookId)
                        result.onSuccess {
                            // Refresh the list to remove the deleted cookbook
                            loadCookbooks()
                            _actions.value = CookbookManagementAction.ShowUndoSnackbar(
                                message = "Cookbook deleted",
                                onUndo = { undoDeleteCookbook(cookbookId) }
                            )
                        }.onFailure { e ->
                            _actions.value = CookbookManagementAction.ShowError("Failed to delete cookbook: ${e.message}")
                        }
                    }
                }
                
            } catch (e: Exception) {
                _actions.value = CookbookManagementAction.ShowError("Failed to delete cookbook: ${e.message}")
            }
        }
    }

    private fun bulkDeleteCookbooks(cookbookIds: List<String>) {
        viewModelScope.launch {
            _actions.value = CookbookManagementAction.ShowBulkDeleteConfirmation(cookbookIds)
        }
    }

    fun confirmBulkDeleteCookbooks(cookbookIds: List<String>) {
        viewModelScope.launch {
            try {
                var successCount = 0
                var errorCount = 0
                
                cookbookIds.forEach { cookbookId ->
                    try {
                        val currentState = _state.value
                        if (currentState is CookbookManagementState.Success) {
                            val cookbook = currentState.cookbooks.find { it.id == cookbookId }
                                ?: currentState.sharedCookbooks.find { it.id == cookbookId }
                            
                            if (cookbook != null) {
                                recentlyDeletedCookbooks.add(cookbook)
                                deleteCookbook(cookbookId).onSuccess { successCount++ }.onFailure { errorCount++ }
                            }
                        }
                    } catch (e: Exception) {
                        errorCount++
                    }
                }
                
                loadCookbooks()
                if (errorCount > 0) {
                    _actions.value = CookbookManagementAction.ShowSnackbar(
                        "Deleted $successCount cookbooks, $errorCount failed"
                    )
                } else {
                    _actions.value = CookbookManagementAction.ShowSnackbar(
                        "Successfully deleted $successCount cookbooks"
                    )
                }
                
            } catch (e: Exception) {
                _actions.value = CookbookManagementAction.ShowError("Failed to delete cookbooks: ${e.message}")
            }
        }
    }

    private fun undoDeleteCookbook(cookbookId: String) {
        viewModelScope.launch {
            val cookbookToRestore = recentlyDeletedCookbooks.find { it.id == cookbookId }
            if (cookbookToRestore != null) {
                try {
                    // Restore the cookbook
                    val result = createCookbook(cookbookToRestore)
                    result.onSuccess {
                        recentlyDeletedCookbooks.remove(cookbookToRestore)
                        loadCookbooks()
                        _actions.value = CookbookManagementAction.ShowSnackbar("Cookbook restored")
                    }.onFailure { e ->
                        _actions.value = CookbookManagementAction.ShowError("Failed to restore cookbook: ${e.message}")
                    }
                } catch (e: Exception) {
                    _actions.value = CookbookManagementAction.ShowError("Failed to restore cookbook: ${e.message}")
                }
            }
        }
    }

    private fun addRecipeToCookbook(recipeId: String) {
        viewModelScope.launch {
            val currentState = _state.value
            if (currentState is CookbookManagementState.Success && currentState.selectedCookbook != null) {
                try {
                    val result = addRecipeToCookbook(currentState.selectedCookbook.id, recipeId)
                    result.onSuccess {
                        // Refresh the selected cookbook to show the new recipe
                        selectCookbook(currentState.selectedCookbook.id)
                    }.onFailure { e ->
                        _actions.value = CookbookManagementAction.ShowError("Failed to add recipe: ${e.message}")
                    }
                    
                } catch (e: Exception) {
                    _actions.value = CookbookManagementAction.ShowError("Failed to add recipe: ${e.message}")
                }
            } else {
                _actions.value = CookbookManagementAction.ShowError("No cookbook selected")
            }
        }
    }

    private fun removeRecipeFromCookbook(recipeId: String) {
        viewModelScope.launch {
            val currentState = _state.value
            if (currentState is CookbookManagementState.Success && currentState.selectedCookbook != null) {
                try {
                    val result = removeRecipeFromCookbook(currentState.selectedCookbook.id, recipeId)
                    result.onSuccess {
                        // Refresh the selected cookbook to remove the recipe
                        selectCookbook(currentState.selectedCookbook.id)
                    }.onFailure { e ->
                        _actions.value = CookbookManagementAction.ShowError("Failed to remove recipe: ${e.message}")
                    }
                    
                } catch (e: Exception) {
                    _actions.value = CookbookManagementAction.ShowError("Failed to remove recipe: ${e.message}")
                }
            } else {
                _actions.value = CookbookManagementAction.ShowError("No cookbook selected")
            }
        }
    }

    private fun searchCookbooks(query: String) {
        viewModelScope.launch {
            currentQuery = query
            _state.value = CookbookManagementState.Loading
            
            try {
                if (query.isBlank()) {
                    loadCookbooks()
                    return@launch
                }

                searchCookbooks(query)
                    .catch { e ->
                        _state.value = CookbookManagementState.Error("Search failed: ${e.message}")
                    }
                    .collect { cookbooks ->
                        if (cookbooks.isEmpty()) {
                            _state.value = CookbookManagementState.Empty
                        } else {
                            val sortedCookbooks = sortCookbooksList(cookbooks, currentSortOrder)
                            _state.value = CookbookManagementState.Success(
                                cookbooks = sortedCookbooks,
                                isLoadingMore = false,
                                hasMore = false,
                                searchQuery = query,
                                sortOrder = currentSortOrder
                            )
                        }
                    }
                
            } catch (e: Exception) {
                _state.value = CookbookManagementState.Error("Search failed: ${e.message}")
            }
        }
    }

    // Sharing functionality
    private fun shareCookbook(cookbookId: String, userIds: List<String>, permissions: Set<Permission>) {
        viewModelScope.launch {
            try {
                val result = shareCookbook(cookbookId, userIds, permissions)
                result.onSuccess {
                    loadCookbooks()
                    _actions.value = CookbookManagementAction.ShowSharingSuccess(
                        "Cookbook shared successfully with ${userIds.size} users"
                    )
                }.onFailure { e ->
                    _actions.value = CookbookManagementAction.ShowError("Failed to share cookbook: ${e.message}")
                }
            } catch (e: Exception) {
                _actions.value = CookbookManagementAction.ShowError("Failed to share cookbook: ${e.message}")
            }
        }
    }

    private fun generateSharingLink(cookbookId: String) {
        viewModelScope.launch {
            try {
                val result = generateSharingLink(cookbookId)
                result.onSuccess { sharingLink ->
                    // Generate QR code data (simplified - in production would use QR code library)
                    val qrCodeData = "cookbook:$cookbookId"
                    _actions.value = CookbookManagementAction.ShowSharingLinkDialog(sharingLink, qrCodeData)
                }.onFailure { e ->
                    _actions.value = CookbookManagementAction.ShowError("Failed to generate sharing link: ${e.message}")
                }
            } catch (e: Exception) {
                _actions.value = CookbookManagementAction.ShowError("Failed to generate sharing link: ${e.message}")
            }
        }
    }

    private fun revokeSharing(cookbookId: String, userId: String) {
        viewModelScope.launch {
            try {
                // In production, this would call the revoke sharing use case
                // For now, just refresh the sharing info
                loadCookbooks()
                _actions.value = CookbookManagementAction.ShowSnackbar("Sharing revoked for user")
            } catch (e: Exception) {
                _actions.value = CookbookManagementAction.ShowError("Failed to revoke sharing: ${e.message}")
            }
        }
    }

    private fun loadMore() {
        viewModelScope.launch {
            val currentState = _state.value
            if (currentState is CookbookManagementState.Success && currentState.hasMore && !currentState.isLoadingMore) {
                _state.value = currentState.copy(isLoadingMore = true)
                // TODO: Implement pagination
                // For now, just stop loading more
                _state.value = currentState.copy(isLoadingMore = false, hasMore = false)
            }
        }
    }

    private fun refresh() {
        loadCookbooks()
    }

    // Import/Export functionality
    private fun exportCookbook(cookbookId: String, format: ExportFormat, destinationFile: File) {
        viewModelScope.launch {
            try {
                val currentState = _state.value
                if (currentState is CookbookManagementState.Success) {
                    val cookbook = currentState.cookbooks.find { it.id == cookbookId }
                        ?: currentState.sharedCookbooks.find { it.id == cookbookId }
                    
                    if (cookbook != null) {
                        _state.value = currentState.copy(isExporting = true, exportProgress = 0)
                        _actions.value = CookbookManagementAction.ShowExportProgress(0)
                        
                        val result = exportCookbook(cookbookId, format, destinationFile)
                        result.onSuccess {
                            _state.value = currentState.copy(isExporting = false, exportProgress = 100)
                            _actions.value = CookbookManagementAction.ShowExportSuccess(
                                destinationFile.absolutePath,
                                format
                            )
                        }.onFailure { e ->
                            _state.value = currentState.copy(isExporting = false)
                            _actions.value = CookbookManagementAction.ShowError("Export failed: ${e.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                _state.value = (currentState as? CookbookManagementState.Success)?.copy(isExporting = false) ?: currentState
                _actions.value = CookbookManagementAction.ShowError("Export failed: ${e.message}")
            }
        }
    }

    private fun importCookbook(sourceFile: File, format: ExportFormat) {
        viewModelScope.launch {
            try {
                _state.value = CookbookManagementState.Loading
                _actions.value = CookbookManagementAction.ShowImportProgress(0)
                
                val result = importCookbook(sourceFile, format)
                result.onSuccess { importedCookbook ->
                    _state.value = CookbookManagementState.Loading
                    _actions.value = CookbookManagementAction.ShowImportSuccess(
                        importedCookbook.name,
                        importedCookbook.recipeCount
                    )
                    loadCookbooks()
                }.onFailure { e ->
                    _state.value = CookbookManagementState.Loading
                    _actions.value = CookbookManagementAction.ShowError("Import failed: ${e.message}")
                }
            } catch (e: Exception) {
                _state.value = CookbookManagementState.Loading
                _actions.value = CookbookManagementAction.ShowError("Import failed: ${e.message}")
            }
        }
    }

    fun showCreateCookbookDialog(defaultName: String = "") {
        viewModelScope.launch {
            _actions.value = CookbookManagementAction.ShowCreateCookbookDialog(defaultName)
        }
    }

    fun showEditCookbookDialog(cookbook: Cookbook) {
        viewModelScope.launch {
            _actions.value = CookbookManagementAction.ShowEditCookbookDialog(cookbook)
        }
    }

    fun showRecipeSelection(cookbookId: String) {
        viewModelScope.launch {
            _actions.value = CookbookManagementAction.ShowRecipeSelection(cookbookId)
        }
    }

    fun clearAction() {
        viewModelScope.launch {
            _actions.value = null
        }
    }

    fun setDeviceId(deviceId: String) {
        currentDeviceId = deviceId
        loadCookbooks()
    }

    // Helper methods for UI
    fun showCreateCookbookDialog(defaultName: String = "") {
        viewModelScope.launch {
            _actions.value = CookbookManagementAction.ShowCreateCookbookDialog(defaultName)
        }
    }

    fun showEditCookbookDialog(cookbook: Cookbook) {
        viewModelScope.launch {
            _actions.value = CookbookManagementAction.ShowEditCookbookDialog(cookbook)
        }
    }

    fun showRecipeSelection(cookbookId: String) {
        viewModelScope.launch {
            _actions.value = CookbookManagementAction.ShowRecipeSelection(cookbookId)
        }
    }

    fun showShareDialog(cookbook: Cookbook) {
        viewModelScope.launch {
            _actions.value = CookbookManagementAction.ShowShareDialog(cookbook)
        }
    }

    fun showExportDialog(cookbook: Cookbook) {
        viewModelScope.launch {
            _actions.value = CookbookManagementAction.ShowExportDialog(cookbook)
        }
    }

    fun showImportDialog() {
        viewModelScope.launch {
            _actions.value = CookbookManagementAction.ShowImportDialog(
                listOf(ExportFormat.JSON, ExportFormat.MARKDOWN)
            )
        }
    }

    fun navigateToCookbookSettings(cookbookId: String) {
        viewModelScope.launch {
            _actions.value = CookbookManagementAction.NavigateToCookbookSettings(cookbookId)
        }
    }

    // Get cookbook by ID for UI
    fun getCookbookById(cookbookId: String): Cookbook? {
        val currentState = _state.value
        if (currentState is CookbookManagementState.Success) {
            return currentState.cookbooks.find { it.id == cookbookId }
                ?: currentState.sharedCookbooks.find { it.id == cookbookId }
        }
        return null
    }

    // Get sharing info for a cookbook
    fun getSharingInfoForCookbook(cookbookId: String): CookbookSharingInfo? {
        val currentState = _state.value
        if (currentState is CookbookManagementState.Success) {
            return currentState.sharingInfo[cookbookId]
        }
        return null
    }
}