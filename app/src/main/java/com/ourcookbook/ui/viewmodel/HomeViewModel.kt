package com.ourcookbook.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ourcookbook.domain.model.Recipe
import com.ourcookbook.domain.usecase.cookbook.GetCookbooks
import com.ourcookbook.domain.usecase.recipe.GetRecipes
import com.ourcookbook.domain.usecase.recipe.SearchRecipes
import com.ourcookbook.ui.service.SyncStatusService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * State for HomeScreen
 */
data class HomeState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val recentRecipes: List<Recipe> = emptyList(),
    val categories: List<String> = emptyList(),
    val favorites: List<Recipe> = emptyList(),
    val cookbooks: List<com.ourcookbook.domain.model.Cookbook> = emptyList(),
    val syncStatus: String = "IDLE"
)

/**
 * ViewModel for HomeScreen
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getRecipes: GetRecipes,
    private val getCookbooks: GetCookbooks,
    private val searchRecipes: SearchRecipes,
    private val syncStatusService: SyncStatusService
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        observeSyncStatus()
    }

    fun loadData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            try {
                // Load recent recipes
                val recipes = getRecipes().catch { e ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "Failed to load recipes: ${e.message}"
                    )
                }.collect { result ->
                    result.fold(
                        onSuccess = { recipes ->
                            val recentRecipes = recipes.sortedByDescending { it.updatedAt }.take(5)
                            val favorites = recipes.filter { it.isFavorite }
                            _state.value = _state.value.copy(
                                recentRecipes = recentRecipes,
                                favorites = favorites
                            )
                        },
                        onFailure = { e ->
                            _state.value = _state.value.copy(
                                isLoading = false,
                                error = e.message ?: "Failed to load recipes"
                            )
                        }
                    )
                }

                // Load cookbooks
                getCookbooks().catch { e ->
                    // Handle error but don't fail the whole screen
                }.collect { result ->
                    result.fold(
                        onSuccess = { cookbooks ->
                            _state.value = _state.value.copy(
                                cookbooks = cookbooks
                            )
                        },
                        onFailure = { e ->
                            // Handle error
                        }
                    )
                }

                // Set categories
                val categories = listOf("Breakfasts", "Mains", "Desserts & Snacks", "Sides", "Sauces and Spices")
                _state.value = _state.value.copy(
                    categories = categories,
                    isLoading = false
                )

            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Failed to load data: ${e.message}"
                )
            }
        }
    }

    private fun observeSyncStatus() {
        viewModelScope.launch {
            syncStatusService.syncStatus.collect { status ->
                _state.value = _state.value.copy(syncStatus = status.toString())
            }
        }
    }

    fun refresh() {
        loadData()
    }
}
