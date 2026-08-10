package com.ourcookbook.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ourcookbook.domain.model.Recipe
import com.ourcookbook.domain.usecase.recipe.DeleteRecipe
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
 * Enhanced for Task 2.1.01 with sorting and pagination support
 */
sealed class RecipeListState {
    object Loading : RecipeListState()
    data class Success(
        val recipes: List<Recipe> = emptyList(),
        val favorites: List<Recipe> = emptyList(),
        val isLoadingMore: Boolean = false,
        val hasMore: Boolean = true,
        val currentPage: Int = 1,
        val totalPages: Int = 1,
        val sortOption: SortOption = SortOption.TITLE_ASC,
        val filterCategory: String? = null,
        val showFavoritesOnly: Boolean = false,
        val searchQuery: String = ""
    ) : RecipeListState()
    data class Error(val message: String) : RecipeListState()
    object Empty : RecipeListState()
}

/**
 * Sort options for recipe list
 */
enum class SortOption {
    TITLE_ASC,      // Title A-Z
    TITLE_DESC,     // Title Z-A
    RATING_DESC,    // Rating High-Low
    RATING_ASC,     // Rating Low-High
    DATE_DESC,      // Date Newest
    DATE_ASC,       // Date Oldest
    TIME_ASC,       // Time Quickest
    TIME_DESC       // Time Longest
}

/**
 * Event for RecipeListScreen
 * Enhanced for Task 2.1.01 with sorting and pagination
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
    data class SortBy(val sortOption: SortOption) : RecipeListEvent()
    data class SetPage(val page: Int) : RecipeListEvent()
    data class SetPageSize(val pageSize: Int) : RecipeListEvent()
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
 * Enhanced implementation for Task 2.1.01 with:
 * - Search functionality
 * - Category filtering
 * - Favorites filtering
 * - Sorting options
 * - Pagination support
 * - Pull-to-refresh
 * 
 * Integrates with:
 * - RecipeListViewModel from Task 1.7
 * - UI components from Task 1.8
 * - Navigation from Task 1.9
 * - Theme from Task 1.10
 */
@HiltViewModel
class RecipeListViewModel @Inject constructor(
    private val getAllRecipes: GetAllRecipes,
    private val getFavorites: GetFavorites,
    private val getRecipesByCategory: GetRecipesByCategory,
    private val searchRecipes: SearchRecipes,
    private val toggleFavorite: ToggleFavorite,
    private val deleteRecipe: DeleteRecipe
) : ViewModel() {

    private val _state = MutableStateFlow<RecipeListState>(RecipeListState.Loading)
    val state: StateFlow<RecipeListState> = _state.asStateFlow()

    private val _actions = MutableStateFlow<RecipeListAction?>(null)
    val actions: StateFlow<RecipeListAction?> = _actions.asStateFlow()

    // Pagination settings
    private var currentPage: Int = 1
    private val pageSize: Int = 20 // Items per page
    private var allRecipes: List<Recipe> = emptyList()
    private var filteredRecipes: List<Recipe> = emptyList()
    
    // Current filters and sort
    private var currentQuery: String = ""
    private var currentCategory: String? = null
    private var currentShowFavorites: Boolean = false
    private var currentSortOption: SortOption = SortOption.TITLE_ASC

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
            is RecipeListEvent.SortBy -> sortBy(event.sortOption)
            is RecipeListEvent.SetPage -> setPage(event.page)
            is RecipeListEvent.SetPageSize -> setPageSize(event.pageSize)
        }
    }

    private fun loadRecipes() {
        viewModelScope.launch {
            _state.value = RecipeListState.Loading
            
            try {
                // Load all recipes first
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
                        allRecipes = recipes
                        filteredRecipes = applyFiltersAndSort(recipes)
                        updateStateWithPagination()
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
                        allRecipes = recipes
                        filteredRecipes = applyFiltersAndSort(recipes)
                        updateStateWithPagination()
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
            currentPage = 1
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
                        allRecipes = recipes
                        filteredRecipes = applyFiltersAndSort(recipes)
                        updateStateWithPagination()
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
            currentPage = 1
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
                        allRecipes = recipes
                        filteredRecipes = applyFiltersAndSort(recipes)
                        updateStateWithPagination()
                    }
            } catch (e: Exception) {
                _state.value = RecipeListState.Error("Failed to load favorites: ${e.message}")
            }
        }
    }

    private fun sortBy(sortOption: SortOption) {
        viewModelScope.launch {
            currentSortOption = sortOption
            filteredRecipes = applyFiltersAndSort(allRecipes)
            currentPage = 1
            updateStateWithPagination()
        }
    }

    private fun setPage(page: Int) {
        viewModelScope.launch {
            currentPage = page.coerceAtLeast(1)
            updateStateWithPagination()
        }
    }

    private fun setPageSize(pageSize: Int) {
        // Page size change would require reloading data
        // For now, just update the state
        viewModelScope.launch {
            updateStateWithPagination()
        }
    }

    private fun applyFiltersAndSort(recipes: List<Recipe>): List<Recipe> {
        var result = recipes
        
        // Apply search filter if query exists
        if (currentQuery.isNotBlank()) {
            result = result.filter {
                it.title.contains(currentQuery, ignoreCase = true) ||
                it.description?.contains(currentQuery, ignoreCase = true) == true ||
                it.ingredients.any { ingredient -> 
                    ingredient.name.contains(currentQuery, ignoreCase = true)
                } ||
                it.tags.any { tag -> 
                    tag.contains(currentQuery, ignoreCase = true)
                }
            }
        }
        
        // Apply category filter
        if (currentCategory != null) {
            result = result.filter { it.category == currentCategory }
        }
        
        // Apply favorites filter
        if (currentShowFavorites) {
            result = result.filter { it.isFavorite }
        }
        
        // Apply sorting
        result = when (currentSortOption) {
            SortOption.TITLE_ASC -> result.sortedBy { it.title }
            SortOption.TITLE_DESC -> result.sortedByDescending { it.title }
            SortOption.RATING_DESC -> result.sortedByDescending { it.rating ?: 0f }
            SortOption.RATING_ASC -> result.sortedBy { it.rating ?: 0f }
            SortOption.DATE_DESC -> result.sortedByDescending { it.updatedAt }
            SortOption.DATE_ASC -> result.sortedBy { it.updatedAt }
            SortOption.TIME_ASC -> result.sortedBy { it.totalTime ?: Int.MAX_VALUE }
            SortOption.TIME_DESC -> result.sortedByDescending { it.totalTime ?: 0 }
        }
        
        return result
    }

    private fun updateStateWithPagination() {
        val startIndex = (currentPage - 1) * pageSize
        val paginatedRecipes = filteredRecipes.drop(startIndex).take(pageSize)
        
        val totalPages = if (filteredRecipes.isEmpty()) 1 else 
            ((filteredRecipes.size - 1) / pageSize) + 1
        
        if (paginatedRecipes.isEmpty() && filteredRecipes.isNotEmpty()) {
            // If current page has no items but there are items, go to last page
            currentPage = totalPages
            updateStateWithPagination()
            return
        }
        
        if (filteredRecipes.isEmpty()) {
            _state.value = RecipeListState.Empty
        } else {
            _state.value = RecipeListState.Success(
                recipes = paginatedRecipes,
                favorites = filteredRecipes.filter { it.isFavorite },
                isLoadingMore = false,
                hasMore = currentPage < totalPages,
                currentPage = currentPage,
                totalPages = totalPages,
                sortOption = currentSortOption,
                filterCategory = currentCategory,
                showFavoritesOnly = currentShowFavorites,
                searchQuery = currentQuery
            )
        }
    }

    private fun loadMore() {
        viewModelScope.launch {
            val currentState = _state.value
            if (currentState is RecipeListState.Success && currentState.hasMore && !currentState.isLoadingMore) {
                _state.value = currentState.copy(isLoadingMore = true)
                
                // Simulate loading more (in real implementation, this would fetch more data)
                currentPage++
                updateStateWithPagination()
                
                _state.value = currentState.copy(isLoadingMore = false)
            }
        }
    }

    private fun refresh() {
        currentPage = 1
        loadRecipes()
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
            try {
                deleteRecipe(recipeId).onSuccess {
                    // Remove from local list and refresh
                    allRecipes = allRecipes.filter { it.id != recipeId }
                    filteredRecipes = filteredRecipes.filter { it.id != recipeId }
                    updateStateWithPagination()
                }.onFailure { e ->
                    _actions.value = RecipeListAction.ShowError("Failed to delete recipe: ${e.message}")
                }
            } catch (e: Exception) {
                _actions.value = RecipeListAction.ShowError("Failed to delete recipe: ${e.message}")
            }
        }
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
    
    /**
     * Get current sort option as display string
     */
    fun getCurrentSortDisplay(): String {
        return when (currentSortOption) {
            SortOption.TITLE_ASC -> "Title (A-Z)"
            SortOption.TITLE_DESC -> "Title (Z-A)"
            SortOption.RATING_DESC -> "Rating (High-Low)"
            SortOption.RATING_ASC -> "Rating (Low-High)"
            SortOption.DATE_DESC -> "Date (Newest)"
            SortOption.DATE_ASC -> "Date (Oldest)"
            SortOption.TIME_ASC -> "Time (Quickest)"
            SortOption.TIME_DESC -> "Time (Longest)"
        }
    }
    
    /**
     * Get available categories from loaded recipes
     */
    fun getAvailableCategories(): List<String> {
        return allRecipes.map { it.category }.distinct().sorted()
    }
    
    /**
     * Get favorite recipes count
     */
    fun getFavoritesCount(): Int {
        return allRecipes.count { it.isFavorite }
    }
    
    /**
     * Get total recipes count
     */
    fun getTotalRecipesCount(): Int {
        return allRecipes.size
    }
}