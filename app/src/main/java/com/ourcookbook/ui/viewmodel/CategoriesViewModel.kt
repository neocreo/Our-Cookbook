package com.ourcookbook.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ourcookbook.domain.model.Recipe
import com.ourcookbook.domain.usecase.recipe.GetAllRecipes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for CategoriesScreen
 * Task 2.2.02: Category and Tag Filtering
 */
@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val getAllRecipes: GetAllRecipes
) : ViewModel() {
    
    data class CategoryInfo(
        val name: String,
        val recipeCount: Int
    )
    
    data class CategoriesState(
        val categories: List<CategoryInfo> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null
    )
    
    private val _state = MutableStateFlow(CategoriesState())
    val state: StateFlow<CategoriesState> = _state.asStateFlow()
    
    // Predefined categories
    private val allCategories = listOf(
        "Breakfasts", "Mains", "Desserts & Snacks", "Sides", "Sauces and Spices",
        "Appetizers", "Soups", "Salads", "Beverages", "Baking"
    )
    
    init {
        loadCategories()
    }
    
    private fun loadCategories() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val recipes = getAllRecipes().first()
                val categoryCounts = allCategories.map { category ->
                    val count = recipes.count { it.category == category }
                    CategoryInfo(category, count)
                }
                _state.value = _state.value.copy(
                    categories = categoryCounts,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Failed to load categories: ${e.message}",
                    isLoading = false
                )
            }
        }
    }
}
