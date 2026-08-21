package com.ourcookbook.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ourcookbook.domain.model.Recipe
import com.ourcookbook.domain.usecase.recipe.GetRecipesByIngredients
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Ingredient Search Screen
 * Task 2.2.05: Ingredient-based Search Implementation
 *
 * Handles ingredient selection, recipe search by ingredients, and match scoring
 */
@HiltViewModel
class IngredientSearchViewModel @Inject constructor(
    private val getRecipesByIngredients: GetRecipesByIngredients
) : ViewModel() {

    // State for the ingredient search screen
    private val _state = MutableStateFlow<IngredientSearchState>(IngredientSearchState.Idle)
    val state: StateFlow<IngredientSearchState> = _state.asStateFlow()

    // Available ingredients from the database (could be loaded from assets)
    private val availableIngredients = listOf(
        // Vegetables
        "tomatoes", "onions", "garlic", "peppers", "carrots", "potatoes", "broccoli", "spinach",
        "lettuce", "cucumbers", "zucchini", "eggplant", "mushrooms", "corn", "peas", "green beans",
        
        // Proteins
        "chicken", "beef", "pork", "fish", "shrimp", "turkey", "tofu", "eggs", "ground beef",
        "chicken breast", "salmon", "tuna", "bacon", "ham", "sausage",
        
        // Dairy
        "cheese", "milk", "butter", "cream", "yogurt", "sour cream", "parmesan", "cheddar",
        
        // Grains & Pasta
        "flour", "rice", "pasta", "bread", "oats", "quinoa", "couscous", "noodles", "tortillas",
        
        // Baking
        "sugar", "brown sugar", "honey", "vanilla extract", "baking powder", "baking soda",
        "yeast", "cornstarch",
        
        // Canned Goods
        "tomato sauce", "tomato paste", "chicken broth", "beef broth", "vegetable broth",
        "coconut milk", "black beans", "kidney beans", "chickpeas",
        
        // Spices & Herbs
        "salt", "pepper", "paprika", "cumin", "chili powder", "cinnamon", "nutmeg", "oregano",
        "basil", "thyme", "rosemary", "parsley", "cilantro", "dill", "ginger", "curry powder",
        
        // Oils & Vinegars
        "olive oil", "vegetable oil", "canola oil", "butter", "apple cider vinegar", "balsamic vinegar",
        "white vinegar", "red wine vinegar",
        
        // Nuts & Seeds
        "almonds", "walnuts", "peanuts", "pecans", "cashews", "sunflower seeds", "chia seeds",
        
        // Fruits
        "apples", "bananas", "oranges", "lemons", "limes", "berries", "strawberries", "blueberries",
        "raspberries", "peaches", "pears", "pineapple", "mango", "avocado",
        
        // Dairy & Eggs
        "heavy cream", "whipping cream", "half and half", "evaporated milk", "condensed milk",
        
        // Sweeteners
        "maple syrup", "agave nectar", "powdered sugar", "confectioners sugar",
        
        // Chocolate
        "chocolate chips", "dark chocolate", "milk chocolate", "white chocolate", "cocoa powder",
        
        // Other
        "wine", "beer", "soy sauce", "worcestershire sauce", "hot sauce", "mustard", "mayonnaise",
        "ketchup", "peanut butter", "jam", "jelly", "marmalade"
    )

    // Selected ingredients
    private var currentSelectedIngredients: List<String> = emptyList()

    // Current search query
    private var currentQuery: String = ""

    /**
     * Handle events from the UI
     */
    fun handleEvent(event: IngredientSearchEvent) {
        when (event) {
            is IngredientSearchEvent.QueryChanged -> onQueryChanged(event.query)
            is IngredientSearchEvent.SelectIngredient -> onSelectIngredient(event.ingredient)
            is IngredientSearchEvent.RemoveIngredient -> onRemoveIngredient(event.ingredient)
            is IngredientSearchEvent.StartSearch -> startSearch()
            is IngredientSearchEvent.ClearSelection -> clearSelection()
            is IngredientSearchEvent.Retry -> retrySearch()
        }
    }

    /**
     * Handle query changes for ingredient search
     */
    private fun onQueryChanged(query: String) {
        currentQuery = query
        
        // Filter suggestions based on query
        val suggestions = if (query.isNotBlank()) {
            availableIngredients
                .filter { it.contains(query, ignoreCase = true) }
                .filter { it !in currentSelectedIngredients }
                .sorted()
                .take(10)
        } else {
            emptyList()
        }

        _state.value = IngredientSearchState.SelectingIngredients(
            query = query,
            suggestions = suggestions,
            selectedIngredients = currentSelectedIngredients
        )
    }

    /**
     * Select an ingredient
     */
    private fun onSelectIngredient(ingredient: String) {
        if (ingredient !in currentSelectedIngredients) {
            currentSelectedIngredients = currentSelectedIngredients + ingredient
            currentQuery = ""
            
            updateSelectionState()
        }
    }

    /**
     * Remove an ingredient from selection
     */
    private fun onRemoveIngredient(ingredient: String) {
        currentSelectedIngredients = currentSelectedIngredients - ingredient
        updateSelectionState()
    }

    /**
     * Update the selection state
     */
    private fun updateSelectionState() {
        val suggestions = if (currentQuery.isNotBlank()) {
            availableIngredients
                .filter { it.contains(currentQuery, ignoreCase = true) }
                .filter { it !in currentSelectedIngredients }
                .sorted()
                .take(10)
        } else {
            emptyList()
        }

        _state.value = IngredientSearchState.SelectingIngredients(
            query = currentQuery,
            suggestions = suggestions,
            selectedIngredients = currentSelectedIngredients
        )
    }

    /**
     * Start searching for recipes
     */
    private fun startSearch() {
        if (currentSelectedIngredients.isEmpty()) {
            _state.value = IngredientSearchState.Idle
            return
        }

        viewModelScope.launch {
            _state.value = IngredientSearchState.Searching
            
            try {
                val result = getRecipesByIngredients(
                    ingredients = currentSelectedIngredients,
                    minMatchPercentage = 0.3f  // At least 30% of ingredients must match
                )
                
                result.onSuccess { recipesWithScores ->
                    if (recipesWithScores.isEmpty()) {
                        _state.value = IngredientSearchState.Empty
                    } else {
                        val recipes = recipesWithScores.map { it.recipe }
                        val matchScores = recipesWithScores.associate { it.recipe.id to it.matchScore }
                        
                        _state.value = IngredientSearchState.Results(
                            recipes = recipes,
                            selectedIngredients = currentSelectedIngredients,
                            matchScores = matchScores
                        )
                    }
                }.onFailure { e ->
                    _state.value = IngredientSearchState.Error(
                        message = "Failed to search recipes: ${e.message}"
                    )
                }
            } catch (e: Exception) {
                _state.value = IngredientSearchState.Error(
                    message = "Error: ${e.message}"
                )
            }
        }
    }

    /**
     * Clear ingredient selection
     */
    private fun clearSelection() {
        currentSelectedIngredients = emptyList()
        currentQuery = ""
        _state.value = IngredientSearchState.Idle
    }

    /**
     * Retry the search
     */
    private fun retrySearch() {
        if (currentSelectedIngredients.isNotEmpty()) {
            startSearch()
        } else {
            _state.value = IngredientSearchState.Idle
        }
    }

    /**
     * Get current selected ingredients
     */
    fun getSelectedIngredients(): List<String> = currentSelectedIngredients

    /**
     * Get current query
     */
    fun getCurrentQuery(): String = currentQuery
}

/**
 * State for Ingredient Search Screen
 */
sealed class IngredientSearchState {
    object Idle : IngredientSearchState()
    data class SelectingIngredients(
        val query: String = "",
        val suggestions: List<String> = emptyList(),
        val selectedIngredients: List<String> = emptyList()
    ) : IngredientSearchState()
    object Searching : IngredientSearchState()
    data class Results(
        val recipes: List<Recipe>,
        val selectedIngredients: List<String>,
        val matchScores: Map<String, Float>
    ) : IngredientSearchState()
    data class Error(val message: String) : IngredientSearchState()
    object Empty : IngredientSearchState()
}

/**
 * Event for Ingredient Search Screen
 */
sealed class IngredientSearchEvent {
    data class QueryChanged(val query: String) : IngredientSearchEvent()
    data class SelectIngredient(val ingredient: String) : IngredientSearchEvent()
    data class RemoveIngredient(val ingredient: String) : IngredientSearchEvent()
    object StartSearch : IngredientSearchEvent()
    object ClearSelection : IngredientSearchEvent()
    object Retry : IngredientSearchEvent()
}
