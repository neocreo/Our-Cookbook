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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
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
        val hasMore: Boolean = true
    ) : CookbookManagementState()
    data class Error(val message: String) : CookbookManagementState()
    object Empty : CookbookManagementState()
}

/**
 * Event for CookbookManagementScreen
 */
sealed class CookbookManagementEvent {
    object LoadCookbooks : CookbookManagementEvent()
    data class SelectCookbook(val cookbookId: String) : CookbookManagementEvent()
    data class CreateCookbook(val name: String, val description: String?) : CookbookManagementEvent()
    data class UpdateCookbook(val cookbook: Cookbook) : CookbookManagementEvent()
    data class DeleteCookbook(val cookbookId: String) : CookbookManagementEvent()
    data class AddRecipeToCookbook(val recipeId: String) : CookbookManagementEvent()
    data class RemoveRecipeFromCookbook(val recipeId: String) : CookbookManagementEvent()
    data class SearchCookbooks(val query: String) : CookbookManagementEvent()
    object LoadMore : CookbookManagementEvent()
    object Refresh : CookbookManagementEvent()
}

/**
 * Action for CookbookManagementScreen
 */
sealed class CookbookManagementAction {
    data class ShowCookbookDetail(val cookbookId: String) : CookbookManagementAction()
    data class ShowCreateCookbookDialog(val defaultName: String = "") : CookbookManagementAction()
    data class ShowEditCookbookDialog(val cookbook: Cookbook) : CookbookManagementAction()
    data class ShowDeleteConfirmation(val cookbookId: String) : CookbookManagementAction()
    data class ShowRecipeSelection(val cookbookId: String) : CookbookManagementAction()
    data class ShowError(val message: String) : CookbookManagementAction()
    object ShowEmptyState : CookbookManagementAction()
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
    private val removeRecipeFromCookbook: RemoveRecipeFromCookbook
) : ViewModel() {

    private val _state = MutableStateFlow<CookbookManagementState>(CookbookManagementState.Loading)
    val state: StateFlow<CookbookManagementState> = _state.asStateFlow()

    private val _actions = MutableStateFlow<CookbookManagementAction?>(null)
    val actions: StateFlow<CookbookManagementAction?> = _actions.asStateFlow()

    private var currentDeviceId: String = "current_device_id" // Will be set properly in production
    private var currentQuery: String = ""

    init {
        loadCookbooks()
    }

    fun handleEvent(event: CookbookManagementEvent) {
        when (event) {
            is CookbookManagementEvent.LoadCookbooks -> loadCookbooks()
            is CookbookManagementEvent.SelectCookbook -> selectCookbook(event.cookbookId)
            is CookbookManagementEvent.CreateCookbook -> createCookbook(event.name, event.description)
            is CookbookManagementEvent.UpdateCookbook -> updateCookbook(event.cookbook)
            is CookbookManagementEvent.DeleteCookbook -> deleteCookbook(event.cookbookId)
            is CookbookManagementEvent.AddRecipeToCookbook -> addRecipeToCookbook(event.recipeId)
            is CookbookManagementEvent.RemoveRecipeFromCookbook -> removeRecipeFromCookbook(event.recipeId)
            is CookbookManagementEvent.SearchCookbooks -> searchCookbooks(event.query)
            is CookbookManagementEvent.LoadMore -> loadMore()
            is CookbookManagementEvent.Refresh -> refresh()
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
                                if (userCookbooks.isEmpty() && sharedCookbooks.isEmpty()) {
                                    _state.value = CookbookManagementState.Empty
                                } else {
                                    _state.value = CookbookManagementState.Success(
                                        cookbooks = userCookbooks,
                                        sharedCookbooks = sharedCookbooks,
                                        isLoadingMore = false,
                                        hasMore = true
                                    )
                                }
                            }
                    }
                
            } catch (e: Exception) {
                _state.value = CookbookManagementState.Error("Failed to load cookbooks: ${e.message}")
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

    private fun createCookbook(name: String, description: String?) {
        viewModelScope.launch {
            try {
                val cookbook = Cookbook.create(
                    name = name,
                    ownerDeviceId = currentDeviceId,
                    description = description
                )
                
                val result = createCookbook(cookbook)
                result.onSuccess { cookbookId ->
                    // Refresh the list to include the new cookbook
                    loadCookbooks()
                }.onFailure { e ->
                    _actions.value = CookbookManagementAction.ShowError("Failed to create cookbook: ${e.message}")
                }
                
            } catch (e: Exception) {
                _actions.value = CookbookManagementAction.ShowError("Failed to create cookbook: ${e.message}")
            }
        }
    }

    private fun updateCookbook(cookbook: Cookbook) {
        viewModelScope.launch {
            try {
                val result = updateCookbook(cookbook)
                result.onSuccess {
                    // Refresh the list to reflect the changes
                    loadCookbooks()
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
            _actions.value = CookbookManagementAction.ShowDeleteConfirmation(cookbookId)
        }
    }

    fun confirmDeleteCookbook(cookbookId: String) {
        viewModelScope.launch {
            try {
                val result = deleteCookbook(cookbookId)
                result.onSuccess {
                    // Refresh the list to remove the deleted cookbook
                    loadCookbooks()
                }.onFailure { e ->
                    _actions.value = CookbookManagementAction.ShowError("Failed to delete cookbook: ${e.message}")
                }
                
            } catch (e: Exception) {
                _actions.value = CookbookManagementAction.ShowError("Failed to delete cookbook: ${e.message}")
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
                            _state.value = CookbookManagementState.Success(
                                cookbooks = cookbooks,
                                isLoadingMore = false,
                                hasMore = false
                            )
                        }
                    }
                
            } catch (e: Exception) {
                _state.value = CookbookManagementState.Error("Search failed: ${e.message}")
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
}