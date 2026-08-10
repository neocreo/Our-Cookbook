package com.ourcookbook.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ourcookbook.domain.model.Recipe
import com.ourcookbook.domain.usecase.recipe.GetAllRecipes
import com.ourcookbook.domain.usecase.recipe.GetFavorites
import com.ourcookbook.domain.usecase.recipe.GetRecipesByCategory
import com.ourcookbook.domain.usecase.recipe.SearchRecipes
import com.ourcookbook.domain.usecase.recipe.ToggleFavorite
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * State for RecipeListScreen
 */
sealed class RecipeListState {
    object Loading : RecipeListState()
    data class Success(
        val recipes: List<Recipe> = emptyList(),
        val favorites: List<Recipe> = emptyList(),
        val isLoadingMore: Boolean = false,
        val hasMore: Boolean = true
    ) : RecipeListState()
    data class Error(val message: String) : RecipeListState()
    object Empty : RecipeListState()
}

/**
 * Event for RecipeListScreen
 */
sealed class RecipeListEvent {
    object LoadRecipes : RecipeListEvent()
    data class Search(val query: String) : RecipeListEvent()
    data class FilterByCategory(val category: String?) : RecipeListEvent()
    data class FilterByFavorites(val showFavorites: Boolean) : RecipeListEvent()
    data class ToggleFavorite(val recipeId: String) : RecipeListEvent()
    data class DeleteRecipe(val recipeId: String) : RecipeListEvent()
    object LoadMore : RecipeListEvent()
    object Refresh : RecipeListEvent()
}

/**
 * Action for RecipeListScreen
 */
sealed class RecipeListAction {
    data class ShowRecipeDetail(val recipeId: String) : RecipeListAction()
    data class ShowDeleteConfirmation(val recipeId: String) : RecipeListAction()
    data class ShowError(val message: String) : RecipeListAction()
    object ShowEmptyState : RecipeListAction()
}

/**
 * ViewModel for RecipeListScreen
 * Handles recipe listing, filtering, and basic operations
 */
@HiltViewModel
class RecipeListViewModel @Inject constructor(
    private val getAllRecipes: GetAllRecipes,
    private val getFavorites: GetFavorites,
    private val getRecipesByCategory: GetRecipesByCategory,
    private val searchRecipes: SearchRecipes,
    private val toggleFavorite: ToggleFavorite
) : ViewModel() {

    private val _state = MutableStateFlow<RecipeListState>(RecipeListState.Loading)
    val state: StateFlow<RecipeListState> = _state.asStateFlow()

    private val _actions = MutableStateFlow<RecipeListAction?>(null)
    val actions: StateFlow<RecipeListAction?> = _actions.asStateFlow()

    private var currentQuery: String = ""
    private var currentCategory: String? = null
    private var currentShowFavorites: Boolean = false

    init {
        loadRecipes()
    }

    fun handleEvent(event: RecipeListEvent) {
        when (event) {
            is RecipeListEvent.LoadRecipes -> loadRecipes()
            is RecipeListEvent.Search -> search(event.query)
            is RecipeListEvent.FilterByCategory -> filterByCategory(event.category)
            is RecipeListEvent.FilterByFavorites -> filterByFavorites(event.showFavorites)
            is RecipeListEvent.ToggleFavorite -> toggleRecipeFavorite(event.recipeId)
            is RecipeListEvent.DeleteRecipe -> deleteRecipe(event.recipeId)
            is RecipeListEvent.LoadMore -> loadMore()
            is RecipeListEvent.Refresh -> refresh()
        }
    }

    private fun loadRecipes() {
        viewModelScope.launch {
            _state.value = RecipeListState.Loading
            
            try {
                // Load all recipes
                val allRecipesFlow = if (currentCategory != null) {
                    getRecipesByCategory(currentCategory!!)
                } else if (currentShowFavorites) {
                    getFavorites()
                } else {
                    getAllRecipes()
                }

                allRecipesFlow
                    .catch { e ->
                        _state.value = RecipeListState.Error("Failed to load recipes: ${e.message}")
                    }
                    .collect { recipes ->
                        if (recipes.isEmpty()) {
                            _state.value = RecipeListState.Empty
                        } else {
                            _state.value = RecipeListState.Success(
                                recipes = recipes,
                                isLoadingMore = false,
                                hasMore = true
                            )
                        }
                    }
            } catch (e: Exception) {
                _state.value = RecipeListState.Error("Failed to load recipes: ${e.message}")
            }
        }
    }

    private fun search(query: String) {
        viewModelScope.launch {
            currentQuery = query
            _state.value = RecipeListState.Loading
            
            try {
                if (query.isBlank()) {
                    loadRecipes()
                    return@launch
                }

                searchRecipes(query)
                    .catch { e ->
                        _state.value = RecipeListState.Error("Search failed: ${e.message}")
                    }
                    .collect { recipes ->
                        if (recipes.isEmpty()) {
                            _state.value = RecipeListState.Empty
                        } else {
                            _state.value = RecipeListState.Success(
                                recipes = recipes,
                                isLoadingMore = false,
                                hasMore = false
                            )
                        }
                    }
            } catch (e: Exception) {
                _state.value = RecipeListState.Error("Search failed: ${e.message}")
            }
        }
    }

    private fun filterByCategory(category: String?) {
        viewModelScope.launch {
            currentCategory = category
            currentShowFavorites = false
            _state.value = RecipeListState.Loading
            
            try {
                if (category == null) {
                    loadRecipes()
                    return@launch
                }

                getRecipesByCategory(category)
                    .catch { e ->
                        _state.value = RecipeListState.Error("Filter failed: ${e.message}")
                    }
                    .collect { recipes ->
                        if (recipes.isEmpty()) {
                            _state.value = RecipeListState.Empty
                        } else {
                            _state.value = RecipeListState.Success(
                                recipes = recipes,
                                isLoadingMore = false,
                                hasMore = true
                            )
                        }
                    }
            } catch (e: Exception) {
                _state.value = RecipeListState.Error("Filter failed: ${e.message}")
            }
        }
    }

    private fun filterByFavorites(showFavorites: Boolean) {
        viewModelScope.launch {
            currentShowFavorites = showFavorites
            currentCategory = null
            _state.value = RecipeListState.Loading
            
            try {
                if (!showFavorites) {
                    loadRecipes()
                    return@launch
                }

                getFavorites()
                    .catch { e ->
                        _state.value = RecipeListState.Error("Failed to load favorites: ${e.message}")
                    }
                    .collect { recipes ->
                        if (recipes.isEmpty()) {
                            _state.value = RecipeListState.Empty
                        } else {
                            _state.value = RecipeListState.Success(
                                recipes = recipes,
                                favorites = recipes, // All are favorites in this context
                                isLoadingMore = false,
                                hasMore = false
                            )
                        }
                    }
            } catch (e: Exception) {
                _state.value = RecipeListState.Error("Failed to load favorites: ${e.message}")
            }
        }
    }

    private fun toggleRecipeFavorite(recipeId: String) {
        viewModelScope.launch {
            try {
                toggleFavorite(recipeId).onSuccess {
                    // Refresh the current list to reflect the change
                    loadRecipes()
                }.onFailure { e ->
                    _actions.value = RecipeListAction.ShowError("Failed to toggle favorite: ${e.message}")
                }
            } catch (e: Exception) {
                _actions.value = RecipeListAction.ShowError("Failed to toggle favorite: ${e.message}")
            }
        }
    }

    private fun deleteRecipe(recipeId: String) {
        viewModelScope.launch {
            _actions.value = RecipeListAction.ShowDeleteConfirmation(recipeId)
        }
    }

    fun confirmDeleteRecipe(recipeId: String) {
        viewModelScope.launch {
            // TODO: Implement delete recipe use case
            // For now, just refresh the list
            loadRecipes()
        }
    }

    private fun loadMore() {
        viewModelScope.launch {
            val currentState = _state.value
            if (currentState is RecipeListState.Success && currentState.hasMore && !currentState.isLoadingMore) {
                _state.value = currentState.copy(isLoadingMore = true)
                // TODO: Implement pagination
                // For now, just stop loading more
                _state.value = currentState.copy(isLoadingMore = false, hasMore = false)
            }
        }
    }

    private fun refresh() {
        loadRecipes()
    }

    fun navigateToRecipeDetail(recipeId: String) {
        viewModelScope.launch {
            _actions.value = RecipeListAction.ShowRecipeDetail(recipeId)
        }
    }

    fun clearAction() {
        viewModelScope.launch {
            _actions.value = null
        }
    }
}