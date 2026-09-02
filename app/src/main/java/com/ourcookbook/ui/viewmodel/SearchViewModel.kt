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
import com.ourcookbook.domain.usecase.recipe.GetAllRecipes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

/**
 * Sort options for search results
 * Task 2.1.04: Search Screen Implementation
 */
enum class SearchSortOption {
    RELEVANCE,       // Relevance (default)
    TITLE_ASC,       // Title A-Z
    TITLE_DESC,      // Title Z-A
    DATE_NEWEST,     // Date created (newest first)
    DATE_OLDEST,     // Date created (oldest first)
    RATING_HIGH,     // Rating (highest first)
    RATING_LOW,      // Rating (lowest first)
    TIME_SHORTEST,   // Cook time (shortest first)
    TIME_LONGEST    // Cook time (longest first)
}

/**
 * State for SearchScreen
 * Enhanced for Task 2.1.04 with sorting and advanced filtering
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
    val isSearching: Boolean = false,
    val sortOption: SearchSortOption = SearchSortOption.RELEVANCE,
    val availableTags: List<String> = emptyList(),
    val totalResults: Int = 0
) {
    val hasActiveFilters: Boolean get() = 
        selectedCategories.isNotEmpty() || 
        selectedTags.isNotEmpty() || 
        maxCookingTime != null || 
        servingSizeRange.first != null || 
        servingSizeRange.second != null || 
        showFavoritesOnly

    val hasResults: Boolean get() = recipes.isNotEmpty()

    val isEmpty: Boolean get() = !isLoading && !isSearching && recipes.isEmpty() && query.isBlank()

    val isNoResults: Boolean get() = !isLoading && !isSearching && recipes.isEmpty() && query.isNotBlank()
}

/**
 * Event for SearchScreen
 * Enhanced for Task 2.1.04 with sorting support
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
    data class SetSortOption(val sortOption: SearchSortOption) : SearchEvent()
    object PerformSearch : SearchEvent()
    object ClearFilters : SearchEvent()
    object ClearSearch : SearchEvent()
    object ToggleFilterDrawer : SearchEvent()
}

/**
 * Action for SearchScreen
 * Enhanced for Task 2.1.04 with navigation and filter actions
 */
sealed class SearchAction {
    data class ShowRecipeDetail(val recipeId: String) : SearchAction()
    data class ShowError(val message: String) : SearchAction()
    object ShowFilterOptions : SearchAction()
    object HideFilterOptions : SearchAction()
    object NavigateToCreateRecipe : SearchAction()
}

/**
 * ViewModel for SearchScreen
 * Enhanced implementation for Task 2.1.04 with:
 * - Full-text search across recipe titles, ingredients, and descriptions
 * - Real-time search as user types
 * - Category filtering (Breakfasts, Mains, Desserts & Snacks, Sides, Sauces and Spices)
 * - Tag filtering
 * - Favorites-only toggle
 * - Advanced filter options (cooking time, serving size)
 * - Sorting options (relevance, title, date, rating, cook time)
 * 
 * Integrates with:
 * - RecipeListViewModel from Task 1.7
 * - UI components from Task 1.8
 * - Navigation from Task 1.9
 * - Theme from Task 1.10
 */
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchRecipes: SearchRecipes,
    private val getRecipesByCategory: GetRecipesByCategory,
    private val filterRecipesByTags: FilterRecipesByTags,
    private val filterRecipesByCookingTime: FilterRecipesByCookingTime,
    private val filterRecipesByServingSize: FilterRecipesByServingSize,
    private val getFavorites: GetFavorites,
    private val getAllRecipes: GetAllRecipes
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

    // Filter drawer state
    private var showFilterDrawer: Boolean = false

    // Debounce timer for search
    private var searchDebounceJob: kotlinx.coroutines.Job? = null
    private val searchDebounceDelay = 300L // milliseconds

    init {
        _state.value = _state.value.copy(
            categories = allCategories,
            availableTags = allTags
        )
        // Load all recipes initially for filtering
        loadAllRecipesForFiltering()
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
            is SearchEvent.SetSortOption -> setSortOption(event.sortOption)
            is SearchEvent.PerformSearch -> performSearch()
            is SearchEvent.ClearFilters -> clearFilters()
            is SearchEvent.ClearSearch -> clearSearch()
            is SearchEvent.ToggleFilterDrawer -> toggleFilterDrawer()
        }
    }

    private fun updateQuery(query: String) {
        _state.value = _state.value.copy(query = query)
        
        // Cancel previous debounce job
        searchDebounceJob?.cancel()
        
        // Auto-search when query changes (with debounce)
        if (query.isNotBlank()) {
            searchDebounceJob = viewModelScope.launch {
                kotlinx.coroutines.delay(searchDebounceDelay)
                performSearch()
            }
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
        performSearch()
    }

    private fun setSortOption(sortOption: SearchSortOption) {
        _state.value = _state.value.copy(sortOption = sortOption)
        performSearch()
    }

    fun performSearch() {
        viewModelScope.launch {
            val currentState = _state.value
            _state.value = currentState.copy(isSearching = true, isLoading = true, error = null)
            
            try {
                // Get base recipes based on search criteria
                val baseRecipes = getBaseRecipes(currentState)
                
                // Apply text search if query exists
                val textFilteredRecipes = if (currentState.query.isNotBlank()) {
                    applyTextSearch(baseRecipes, currentState.query)
                } else {
                    baseRecipes
                }

                // Apply additional filters
                val filteredRecipes = applyAdditionalFilters(textFilteredRecipes, currentState)
                
                // Apply sorting
                val sortedRecipes = applySorting(filteredRecipes, currentState.sortOption)
                
                _state.value = currentState.copy(
                    recipes = sortedRecipes,
                    totalResults = sortedRecipes.size,
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

    private suspend fun getBaseRecipes(state: SearchState): List<Recipe> {
        return when {
            state.selectedCategories.isNotEmpty() -> {
                // For multiple categories, get all recipes and filter by category
                val allRecipes = getAllRecipes().first()
                allRecipes.filter { recipe -> 
                    state.selectedCategories.contains(recipe.category)
                }
            }
            state.showFavoritesOnly -> {
                getFavorites().catch { e -> 
                    throw Exception("Failed to load favorites: ${e.message}")
                }.first()
            }
            else -> {
                // Get all recipes as base
                getAllRecipes().catch { e -> 
                    throw Exception("Failed to load recipes: ${e.message}")
                }.first()
            }
        }
    }

    private fun applyTextSearch(recipes: List<Recipe>, query: String): List<Recipe> {
        val lowerQuery = query.lowercase()
        return recipes.filter { recipe ->
            recipe.title.lowercase().contains(lowerQuery) ||
            recipe.description?.lowercase()?.contains(lowerQuery) == true ||
            recipe.ingredients.any { ingredient -> 
                ingredient.name.lowercase().contains(lowerQuery)
            } ||
            recipe.tags.any { tag -> 
                tag.lowercase().contains(lowerQuery)
            }
        }
    }

    private suspend fun applyAdditionalFilters(
        recipes: List<Recipe>,
        state: SearchState
    ): List<Recipe> {
        var filtered = recipes
        
        // Filter by category (if not already filtered in getBaseRecipes)
        if (state.selectedCategories.isNotEmpty()) {
            filtered = filtered.filter { recipe ->
                state.selectedCategories.contains(recipe.category)
            }
        }
        
        // Filter by tags
        if (state.selectedTags.isNotEmpty()) {
            filtered = filtered.filter { recipe ->
                state.selectedTags.any { tag -> 
                    recipe.tags.contains(tag)
                }
            }
        }
        
        // Filter by cooking time
        state.maxCookingTime?.let { maxTime ->
            filtered = filtered.filter { recipe ->
                recipe.totalTime?.let { it <= maxTime } ?: false
            }
        }
        
        // Filter by serving size
        val (minServings, maxServings) = state.servingSizeRange
        if (minServings != null || maxServings != null) {
            filtered = filtered.filter { recipe ->
                recipe.servingSize?.let { servings ->
                    (minServings == null || servings >= minServings) && 
                    (maxServings == null || servings <= maxServings)
                } ?: false
            }
        }
        
        // Filter by favorites only
        if (state.showFavoritesOnly) {
            filtered = filtered.filter { it.isFavorite }
        }
        
        return filtered
    }

    private fun applySorting(recipes: List<Recipe>, sortOption: SearchSortOption): List<Recipe> {
        return when (sortOption) {
            SearchSortOption.RELEVANCE -> recipes // Keep original order (relevance from search)
            SearchSortOption.TITLE_ASC -> recipes.sortedBy { it.title }
            SearchSortOption.TITLE_DESC -> recipes.sortedByDescending { it.title }
            SearchSortOption.DATE_NEWEST -> recipes.sortedByDescending { it.createdAt }
            SearchSortOption.DATE_OLDEST -> recipes.sortedBy { it.createdAt }
            SearchSortOption.RATING_HIGH -> recipes.sortedByDescending { it.rating ?: 0f }
            SearchSortOption.RATING_LOW -> recipes.sortedBy { it.rating ?: 0f }
            SearchSortOption.TIME_SHORTEST -> recipes.sortedBy { it.totalTime ?: Int.MAX_VALUE }
            SearchSortOption.TIME_LONGEST -> recipes.sortedByDescending { it.totalTime ?: 0 }
        }
    }

    private fun clearFilters() {
        _state.value = _state.value.copy(
            selectedCategories = emptyList(),
            selectedTags = emptyList(),
            maxCookingTime = null,
            servingSizeRange = Pair(null, null),
            showFavoritesOnly = false,
            sortOption = SearchSortOption.RELEVANCE
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
            showFavoritesOnly = false,
            sortOption = SearchSortOption.RELEVANCE,
            totalResults = 0
        )
    }

    private fun toggleFilterDrawer() {
        showFilterDrawer = !showFilterDrawer
        _actions.value = if (showFilterDrawer) {
            SearchAction.ShowFilterOptions
        } else {
            SearchAction.HideFilterOptions
        }
    }

    fun navigateToRecipeDetail(recipeId: String) {
        viewModelScope.launch {
            _actions.value = SearchAction.ShowRecipeDetail(recipeId)
        }
    }

    fun showFilterOptions() {
        viewModelScope.launch {
            showFilterDrawer = true
            _actions.value = SearchAction.ShowFilterOptions
        }
    }

    fun hideFilterOptions() {
        viewModelScope.launch {
            showFilterDrawer = false
            _actions.value = SearchAction.HideFilterOptions
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

    fun navigateToCreateRecipe() {
        viewModelScope.launch {
            _actions.value = SearchAction.NavigateToCreateRecipe
        }
    }

    /**
     * Load all recipes for filtering purposes
     */
    private fun loadAllRecipesForFiltering() {
        viewModelScope.launch {
            try {
                val allRecipes = getAllRecipes().first()
                // Extract unique tags from all recipes
                val allRecipeTags = allRecipes.flatMap { it.tags }.distinct().sorted()
                _state.value = _state.value.copy(
                    availableTags = (allTags + allRecipeTags).distinct().sorted()
                )
            } catch (e: Exception) {
                // Silently fail - tags will use defaults
            }
        }
    }

    /**
     * Get display name for sort option
     */
    fun getSortOptionDisplayName(sortOption: SearchSortOption): String {
        return when (sortOption) {
            SearchSortOption.RELEVANCE -> "Relevance"
            SearchSortOption.TITLE_ASC -> "Title (A-Z)"
            SearchSortOption.TITLE_DESC -> "Title (Z-A)"
            SearchSortOption.DATE_NEWEST -> "Date (Newest)"
            SearchSortOption.DATE_OLDEST -> "Date (Oldest)"
            SearchSortOption.RATING_HIGH -> "Rating (High-Low)"
            SearchSortOption.RATING_LOW -> "Rating (Low-High)"
            SearchSortOption.TIME_SHORTEST -> "Cook Time (Shortest)"
            SearchSortOption.TIME_LONGEST -> "Cook Time (Longest)"
        }
    }

    /**
     * Get all available sort options
     */
    fun getAllSortOptions(): List<SearchSortOption> {
        return SearchSortOption.values().toList()
    }
}