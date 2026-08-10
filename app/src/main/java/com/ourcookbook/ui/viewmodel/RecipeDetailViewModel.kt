package com.ourcookbook.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ourcookbook.domain.model.Recipe
import com.ourcookbook.domain.usecase.recipe.GetRecipeById
import com.ourcookbook.domain.usecase.recipe.ToggleFavorite
import com.ourcookbook.domain.usecase.recipe.UpdateRecipe
import com.ourcookbook.domain.usecase.recipe.DeleteRecipe
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * State for RecipeDetailScreen
 */
sealed class RecipeDetailState {
    object Loading : RecipeDetailState()
    data class Success(val recipe: Recipe) : RecipeDetailState()
    data class Error(val message: String) : RecipeDetailState()
    object NotFound : RecipeDetailState()
}

/**
 * Event for RecipeDetailScreen
 */
sealed class RecipeDetailEvent {
    data class LoadRecipe(val recipeId: String) : RecipeDetailEvent()
    object ToggleFavorite : RecipeDetailEvent()
    object DeleteRecipe : RecipeDetailEvent()
    object EditRecipe : RecipeDetailEvent()
    object ShareRecipe : RecipeDetailEvent()
    object PrintRecipe : RecipeDetailEvent()
}

/**
 * Action for RecipeDetailScreen
 */
sealed class RecipeDetailAction {
    data class ShowEditScreen(val recipeId: String) : RecipeDetailAction()
    data class ShowDeleteConfirmation(val recipeId: String) : RecipeDetailAction()
    data class ShowShareDialog(val recipe: Recipe) : RecipeDetailAction()
    data class ShowPrintDialog(val recipe: Recipe) : RecipeDetailAction()
    data class ShowError(val message: String) : RecipeDetailAction()
    object NavigateBack : RecipeDetailAction()
}

/**
 * ViewModel for RecipeDetailScreen
 * Handles recipe details, favorite toggling, and recipe operations
 */
@HiltViewModel
class RecipeDetailViewModel @Inject constructor(
    private val getRecipeById: GetRecipeById,
    private val toggleFavorite: ToggleFavorite,
    private val updateRecipe: UpdateRecipe,
    private val deleteRecipe: DeleteRecipe
) : ViewModel() {

    private val _state = MutableStateFlow<RecipeDetailState>(RecipeDetailState.Loading)
    val state: StateFlow<RecipeDetailState> = _state.asStateFlow()

    private val _actions = MutableStateFlow<RecipeDetailAction?>(null)
    val actions: StateFlow<RecipeDetailAction?> = _actions.asStateFlow()

    private var currentRecipeId: String = ""

    fun handleEvent(event: RecipeDetailEvent) {
        when (event) {
            is RecipeDetailEvent.LoadRecipe -> loadRecipe(event.recipeId)
            is RecipeDetailEvent.ToggleFavorite -> toggleFavorite()
            is RecipeDetailEvent.DeleteRecipe -> deleteRecipe()
            is RecipeDetailEvent.EditRecipe -> editRecipe()
            is RecipeDetailEvent.ShareRecipe -> shareRecipe()
            is RecipeDetailEvent.PrintRecipe -> printRecipe()
        }
    }

    private fun loadRecipe(recipeId: String) {
        viewModelScope.launch {
            currentRecipeId = recipeId
            _state.value = RecipeDetailState.Loading
            
            try {
                val result = getRecipeById(recipeId)
                result.onSuccess { recipe ->
                    if (recipe != null) {
                        _state.value = RecipeDetailState.Success(recipe)
                    } else {
                        _state.value = RecipeDetailState.NotFound
                    }
                }.onFailure { e ->
                    _state.value = RecipeDetailState.Error("Failed to load recipe: ${e.message}")
                }
            } catch (e: Exception) {
                _state.value = RecipeDetailState.Error("Failed to load recipe: ${e.message}")
            }
        }
    }

    private fun toggleFavorite() {
        viewModelScope.launch {
            val currentState = _state.value
            if (currentState is RecipeDetailState.Success) {
                try {
                    val result = toggleFavorite(currentState.recipe.id)
                    result.onSuccess {
                        // Refresh the recipe to get updated favorite status
                        loadRecipe(currentState.recipe.id)
                    }.onFailure { e ->
                        _actions.value = RecipeDetailAction.ShowError("Failed to toggle favorite: ${e.message}")
                    }
                } catch (e: Exception) {
                    _actions.value = RecipeDetailAction.ShowError("Failed to toggle favorite: ${e.message}")
                }
            }
        }
    }

    private fun deleteRecipe() {
        viewModelScope.launch {
            val currentState = _state.value
            if (currentState is RecipeDetailState.Success) {
                _actions.value = RecipeDetailAction.ShowDeleteConfirmation(currentState.recipe.id)
            }
        }
    }

    fun confirmDeleteRecipe() {
        viewModelScope.launch {
            val currentState = _state.value
            if (currentState is RecipeDetailState.Success) {
                try {
                    val result = deleteRecipe(currentState.recipe.id)
                    result.onSuccess {
                        _actions.value = RecipeDetailAction.NavigateBack
                    }.onFailure { e ->
                        _actions.value = RecipeDetailAction.ShowError("Failed to delete recipe: ${e.message}")
                    }
                } catch (e: Exception) {
                    _actions.value = RecipeDetailAction.ShowError("Failed to delete recipe: ${e.message}")
                }
            }
        }
    }

    private fun editRecipe() {
        viewModelScope.launch {
            val currentState = _state.value
            if (currentState is RecipeDetailState.Success) {
                _actions.value = RecipeDetailAction.ShowEditScreen(currentState.recipe.id)
            }
        }
    }

    private fun shareRecipe() {
        viewModelScope.launch {
            val currentState = _state.value
            if (currentState is RecipeDetailState.Success) {
                _actions.value = RecipeDetailAction.ShowShareDialog(currentState.recipe)
            }
        }
    }

    private fun printRecipe() {
        viewModelScope.launch {
            val currentState = _state.value
            if (currentState is RecipeDetailState.Success) {
                _actions.value = RecipeDetailAction.ShowPrintDialog(currentState.recipe)
            }
        }
    }

    fun clearAction() {
        viewModelScope.launch {
            _actions.value = null
        }
    }

    fun refresh() {
        if (currentRecipeId.isNotBlank()) {
            loadRecipe(currentRecipeId)
        }
    }
}