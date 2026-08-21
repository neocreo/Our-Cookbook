package com.ourcookbook.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ourcookbook.domain.model.Recipe
import com.ourcookbook.domain.usecase.recipe.GetAllRecipes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for TagsScreen
 * Task 2.2.02: Category and Tag Filtering
 */
@HiltViewModel
class TagsViewModel @Inject constructor(
    private val getAllRecipes: GetAllRecipes
) : ViewModel() {
    
    data class TagInfo(
        val name: String,
        val recipeCount: Int
    )
    
    data class TagsState(
        val tags: List<TagInfo> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null
    )
    
    private val _state = MutableStateFlow(TagsState())
    val state: StateFlow<TagsState> = _state.asStateFlow()
    
    // Predefined tags
    private val allTags = listOf(
        "Vegetarian", "Vegan", "Gluten-Free", "Dairy-Free", "Keto",
        "Quick", "Easy", "Family-Friendly", "Meal Prep", "Comfort Food"
    )
    
    init {
        loadTags()
    }
    
    private fun loadTags() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val recipes = getAllRecipes().first()
                val tagCounts = allTags.map { tag ->
                    val count = recipes.count { it.tags.contains(tag) }
                    TagInfo(tag, count)
                }
                _state.value = _state.value.copy(
                    tags = tagCounts,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Failed to load tags: ${e.message}",
                    isLoading = false
                )
            }
        }
    }
}
