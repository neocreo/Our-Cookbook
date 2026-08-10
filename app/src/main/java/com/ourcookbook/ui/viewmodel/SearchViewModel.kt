package com.ourcookbook.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ourcookbook.domain.model.Recipe
import com.ourcookbook.domain.usecase.recipe.SearchRecipes
import com.ourcookbook.domain.usecase.recipe.GetRecipesByCategory
import com.ourcookbook.domain.usecase.recipe.FilterRecipesByTags
import com.ourcookbook.domain.usecase.recipe.FilterRecipesByCookingTime
import com.ourcookbook.domain.usecase.recipe.FilterRecipesByServingSize
import com.ourcookbook.domain.usecase.recipe.GetFavorites
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * State for SearchScreen
 */
data class SearchState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val query: String = "",
    val recipes: List<Recipe> = emptyList(),
    val categories: List<String> = emptyList(),
    val selectedCategories: List<String> = emptyList(),
    val selectedTags: List<String> = emptyList(),
    val maxCookingTime: Int? = null,
    val servingSizeRange: Pair<Int?, Int?> = Pair(null, null),
    val showFavoritesOnly: Boolean = false,
    val isSearching: Boolean = false
) {
    val hasActiveFilters: Boolean get() = 
        selectedCategories.isNotEmpty() || 
        selectedTags.isNotEmpty() || 
        maxCookingTime != null || 
        servingSizeRange.first != null || 
        servingSizeRange.second != null || 
        showFavoritesOnly
}

/**
 * Event for SearchScreen
 */
sealed class SearchEvent {
    data class UpdateQuery(val query: String) : SearchEvent()
    data class SelectCategory(val category: String) : SearchEvent()
    data class DeselectCategory(val category: String) : SearchEvent()
    data class SelectTag(val tag: String) : SearchEvent()
    data class DeselectTag(val tag: String) : SearchEvent()
    data class SetMaxCookingTime(val minutes: Int?) : SearchEvent()
    data class SetServingSizeRange(val min: Int?, val max: Int?) : SearchEvent()
    data class SetFavoritesOnly(val showFavorites: Boolean) : SearchEvent()
    object PerformSearch : SearchEvent()
    object ClearFilters : SearchEvent()
    object ClearSearch : SearchEvent()
}

/**
 * Action for SearchScreen
 */
sealed class SearchAction {
    data class ShowRecipeDetail(val recipeId: String) : SearchAction()
    data class ShowError(val message: String) : SearchAction()
    object ShowFilterOptions : SearchAction()
}

/**
 * ViewModel for SearchScreen
 * Handles recipe search with advanced filtering options
 */
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchRecipes: SearchRecipes,
    private val getRecipesByCategory: GetRecipesByCategory,
    private val filterRecipesByTags: FilterRecipesByTags,
    private val filterRecipesByCookingTime: FilterRecipesByCookingTime,
    private val filterRecipesByServingSize: FilterRecipesByServingSize,
    private val getFavorites: GetFavorites
) : ViewModel() {

    private val _state = MutableStateFlow(SearchState())
    val state: StateFlow<SearchState> = _state.asStateFlow()

    private val _actions = MutableStateFlow<SearchAction?>(null)
    val actions: StateFlow<SearchAction?> = _actions.asStateFlow()

    // Available categories for filtering
    private val allCategories = listOf(
        "Breakfasts", "Mains", "Desserts & Snacks", "Sides", "Sauces and Spices",
        "Appetizers", "Soups", "Salads", "Beverages", "Baking"
    )

    // Available tags for filtering
    private val allTags = listOf(
        "Vegetarian", "Vegan", "Gluten-Free", "Dairy-Free", "Keto", 
        "Quick", "Easy", "Family-Friendly", "Meal Prep", "Comfort Food"
    )

    init {
        _state.value = _state.value.copy(categories = allCategories)
    }

    fun handleEvent(event: SearchEvent) {
        when (event) {
            is SearchEvent.UpdateQuery -> updateQuery(event.query)
            is SearchEvent.SelectCategory -> selectCategory(event.category)
            is SearchEvent.DeselectCategory -> deselectCategory(event.category)
            is SearchEvent.SelectTag -> selectTag(event.tag)
            is SearchEvent.DeselectTag -> deselectTag(event.tag)
            is SearchEvent.SetMaxCookingTime -> setMaxCookingTime(event.minutes)
            is SearchEvent.SetServingSizeRange -> setServingSizeRange(event.min, event.max)
            is SearchEvent.SetFavoritesOnly -> setFavoritesOnly(event.showFavorites)
            is SearchEvent.PerformSearch -> performSearch()
            is SearchEvent.ClearFilters -> clearFilters()
            is SearchEvent.ClearSearch -> clearSearch()
        }
    }

    private fun updateQuery(query: String) {
        _state.value = _state.value.copy(query = query)
        
        // Auto-search when query changes (with debounce in UI)
        if (query.isNotBlank()) {
            performSearch()
        } else {
            clearSearch()
        }
    }

    private fun selectCategory(category: String) {
        val currentState = _state.value
        val selectedCategories = if (currentState.selectedCategories.contains(category)) {
            currentState.selectedCategories
        } else {
            currentState.selectedCategories + category
        }
        _state.value = currentState.copy(selectedCategories = selectedCategories)
    }

    private fun deselectCategory(category: String) {
        val currentState = _state.value
        _state.value = currentState.copy(
            selectedCategories = currentState.selectedCategories.filter { it != category }
        )
    }

    private fun selectTag(tag: String) {
        val currentState = _state.value
        val selectedTags = if (currentState.selectedTags.contains(tag)) {
            currentState.selectedTags
        } else {
            currentState.selectedTags + tag
        }
        _state.value = currentState.copy(selectedTags = selectedTags)
    }

    private fun deselectTag(tag: String) {
        val currentState = _state.value
        _state.value = currentState.copy(
            selectedTags = currentState.selectedTags.filter { it != tag }
        )
    }

    private fun setMaxCookingTime(minutes: Int?) {
        _state.value = _state.value.copy(maxCookingTime = minutes)
    }

    private fun setServingSizeRange(min: Int?, max: Int?) {
        _state.value = _state.value.copy(servingSizeRange = Pair(min, max))
    }

    private fun setFavoritesOnly(showFavorites: Boolean) {
        _state.value = _state.value.copy(showFavoritesOnly = showFavorites)
    }

    private fun performSearch() {
        viewModelScope.launch {
            val currentState = _state.value
            _state.value = currentState.copy(isSearching = true, isLoading = true, error = null)
            
            try {
                // Apply filters based on current state
                val recipes = if (currentState.query.isNotBlank()) {
                    // Text search
                    searchRecipes(currentState.query)
                        .catch { e -> 
                            _state.value = currentState.copy(
                                isSearching = false, 
                                isLoading = false,
                                error = "Search failed: ${e.message}"
                            )
                        }
                        .first()
                } else if (currentState.selectedCategories.isNotEmpty()) {
                    // Category filter - for now just use first category
                    getRecipesByCategory(currentState.selectedCategories.first())
                        .catch { e -> 
                            _state.value = currentState.copy(
                                isSearching = false, 
                                isLoading = false,
                                error = "Filter failed: ${e.message}"
                            )
                        }
                        .first()
                } else if (currentState.showFavoritesOnly) {
                    // Favorites only
                    getFavorites()
                        .catch { e -> 
                            _state.value = currentState.copy(
                                isSearching = false, 
                                isLoading = false,
                                error = "Failed to load favorites: ${e.message}"
                            )
                        }
                        .first()
                } else {
                    // No specific search, return empty for now
                    emptyList()
                }

                // Apply additional filters
                val filteredRecipes = applyAdditionalFilters(recipes, currentState)
                
                _state.value = currentState.copy(
                    recipes = filteredRecipes,
                    isSearching = false,
                    isLoading = false
                )
                
            } catch (e: Exception) {
                _state.value = currentState.copy(
                    isSearching = false,
                    isLoading = false,
                    error = "Search failed: ${e.message}"
                )
            }
        }
    }

    private suspend fun applyAdditionalFilters(
        recipes: List<Recipe>,
        state: SearchState
    ): List<Recipe> {
        var filtered = recipes
        
        // Filter by tags
        if (state.selectedTags.isNotEmpty()) {
            val result = filterRecipesByTags(state.selectedTags)
            result.onSuccess { filteredByTags ->
                filtered = filtered.filter { recipe -> 
                    filteredByTags.any { it.id == recipe.id }
                }
            }
        }
        
        // Filter by cooking time
        state.maxCookingTime?.let { maxTime ->
            val result = filterRecipesByCookingTime(maxTime)
            result.onSuccess { filteredByTime ->
                filtered = filtered.filter { recipe -> 
                    filteredByTime.any { it.id == recipe.id }
                }
            }
        }
        
        // Filter by serving size
        val (minServings, maxServings) = state.servingSizeRange
        if (minServings != null || maxServings != null) {
            val result = filterRecipesByServingSize(
                minServings ?: 0, 
                maxServings ?: Int.MAX_VALUE
            )
            result.onSuccess { filteredByServing ->
                filtered = filtered.filter { recipe -> 
                    filteredByServing.any { it.id == recipe.id }
                }
            }
        }
        
        return filtered
    }

    private fun clearFilters() {
        _state.value = _state.value.copy(
            selectedCategories = emptyList(),
            selectedTags = emptyList(),
            maxCookingTime = null,
            servingSizeRange = Pair(null, null),
            showFavoritesOnly = false
        )
        performSearch()
    }

    private fun clearSearch() {
        _state.value = _state.value.copy(
            query = "",
            recipes = emptyList(),
            selectedCategories = emptyList(),
            selectedTags = emptyList(),
            maxCookingTime = null,
            servingSizeRange = Pair(null, null),
            showFavoritesOnly = false
        )
    }

    fun navigateToRecipeDetail(recipeId: String) {
        viewModelScope.launch {
            _actions.value = SearchAction.ShowRecipeDetail(recipeId)
        }
    }

    fun showFilterOptions() {
        viewModelScope.launch {
            _actions.value = SearchAction.ShowFilterOptions
        }
    }

    fun clearAction() {
        viewModelScope.launch {
            _actions.value = null
        }
    }

    fun refresh() {
        performSearch()
    }
}