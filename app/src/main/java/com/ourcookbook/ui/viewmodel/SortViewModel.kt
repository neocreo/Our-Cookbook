package com.ourcookbook.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ourcookbook.domain.usecase.devicepreferences.GetDevicePreferencesByDevice
import com.ourcookbook.domain.usecase.devicepreferences.UpdateDevicePreferences
import com.ourcookbook.domain.usecase.recipe.GetAllRecipes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for SortScreen
 * Task 2.2.03: Advanced Sorting Options
 * Manages sort option selection and persistence
 */
@HiltViewModel
class SortViewModel @Inject constructor(
    private val getDevicePreferencesByDevice: GetDevicePreferencesByDevice,
    private val updateDevicePreferences: UpdateDevicePreferences,
    private val getAllRecipes: GetAllRecipes
) : ViewModel() {
    
    data class SortState(
        val selectedSortOption: SearchSortOption = SearchSortOption.RELEVANCE,
        val availableSortOptions: List<SearchSortOption> = SearchSortOption.values().toList(),
        val isLoading: Boolean = false,
        val error: String? = null,
        val deviceId: String = ""
    )
    
    private val _state = MutableStateFlow(SortState())
    val state: StateFlow<SortState> = _state.asStateFlow()
    
    fun loadCurrentSortOption() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                // Load device preferences to get current sort option
                // For now, default to RELEVANCE
                // In future, this will be loaded from DevicePreferences
                _state.value = _state.value.copy(
                    selectedSortOption = SearchSortOption.RELEVANCE,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Failed to load sort preferences: ${e.message}",
                    isLoading = false
                )
            }
        }
    }
    
    fun selectSortOption(sortOption: SearchSortOption) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                selectedSortOption = sortOption,
                isLoading = true,
                error = null
            )
            try {
                // Save sort preference to device preferences
                // This will be implemented when DevicePreferences is enhanced
                _state.value = _state.value.copy(isLoading = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Failed to save sort preference: ${e.message}",
                    isLoading = false
                )
            }
        }
    }
    
    /**
     * Apply sorting to a list of recipes
     */
    fun applySorting(recipes: List<com.ourcookbook.domain.model.Recipe>): List<com.ourcookbook.domain.model.Recipe> {
        return when (_state.value.selectedSortOption) {
            SearchSortOption.RELEVANCE -> recipes // No sorting, use original order
            SearchSortOption.TITLE_ASC -> recipes.sortedBy { it.title }
            SearchSortOption.TITLE_DESC -> recipes.sortedByDescending { it.title }
            SearchSortOption.DATE_NEWEST -> recipes.sortedByDescending { it.createdAt }
            SearchSortOption.DATE_OLDEST -> recipes.sortedBy { it.createdAt }
            SearchSortOption.RATING_HIGH -> recipes.sortedByDescending { it.rating ?: 0f }
            SearchSortOption.RATING_LOW -> recipes.sortedBy { it.rating ?: 0f }
            SearchSortOption.TIME_SHORTEST -> recipes.sortedBy { 
                it.cookTime ?: Int.MAX_VALUE + (it.prepTime ?: 0) 
            }
            SearchSortOption.TIME_LONGEST -> recipes.sortedByDescending { 
                it.cookTime ?: 0 + (it.prepTime ?: 0) 
            }
        }
    }
}
