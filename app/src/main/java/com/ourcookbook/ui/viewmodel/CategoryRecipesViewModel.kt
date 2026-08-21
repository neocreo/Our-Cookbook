package com.ourcookbook.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ourcookbook.domain.model.Recipe
import com.ourcookbook.domain.usecase.recipe.GetRecipesByCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for CategoryRecipesScreen
 * Task 2.2.02: Category and Tag Filtering
 */
@HiltViewModel
class CategoryRecipesViewModel @Inject constructor(
    private val getRecipesByCategory: GetRecipesByCategory
) : ViewModel() {
    
    data class CategoryRecipesState(
        val recipes: List<Recipe> = emptyList(),
        val category: String = "",
        val isLoading: Boolean = false,
        val error: String? = null
    )
    
    private val _state = MutableStateFlow(CategoryRecipesState())
    val state: StateFlow<CategoryRecipesState> = _state.asStateFlow()
    
    fun loadRecipes(category: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                category = category,
                isLoading = true,
                error = null
            )
            try {
                getRecipesByCategory(category).collect { recipes ->
                    _state.value = _state.value.copy(
                        recipes = recipes,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Failed to load recipes: ${e.message}",
                    isLoading = false
                )
            }
        }
    }
}
