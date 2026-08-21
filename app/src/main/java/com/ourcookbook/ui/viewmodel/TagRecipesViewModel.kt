package com.ourcookbook.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ourcookbook.domain.model.Recipe
import com.ourcookbook.domain.usecase.recipe.FilterRecipesByTags
import com.ourcookbook.domain.usecase.recipe.GetAllRecipes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for TagRecipesScreen
 * Task 2.2.02: Category and Tag Filtering
 */
@HiltViewModel
class TagRecipesViewModel @Inject constructor(
    private val getAllRecipes: GetAllRecipes,
    private val filterRecipesByTags: FilterRecipesByTags
) : ViewModel() {
    
    data class TagRecipesState(
        val recipes: List<Recipe> = emptyList(),
        val tag: String = "",
        val isLoading: Boolean = false,
        val error: String? = null
    )
    
    private val _state = MutableStateFlow(TagRecipesState())
    val state: StateFlow<TagRecipesState> = _state.asStateFlow()
    
    fun loadRecipes(tag: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                tag = tag,
                isLoading = true,
                error = null
            )
            try {
                val allRecipes = getAllRecipes().first()
                val filteredRecipes = filterRecipesByTags(allRecipes, listOf(tag))
                _state.value = _state.value.copy(
                    recipes = filteredRecipes,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Failed to load recipes: ${e.message}",
                    isLoading = false
                )
            }
        }
    }
}
