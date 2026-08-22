package com.ourcookbook.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.ourcookbook.domain.usecase.recipe.GetAllRecipes
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * ViewModel for Tablet-specific functionality
 * Task 2.2.10: Responsive design for tablets
 *
 * Provides state management for tablet-optimized UI components
 */
@HiltViewModel
class TabletViewModel @Inject constructor(
    private val getAllRecipes: GetAllRecipes
) : ViewModel() {

    // Add tablet-specific state and logic here
    // This ViewModel can be extended as needed for tablet-specific features
    
    /**
     * Tablet layout mode
     */
    enum class TabletLayoutMode {
        SINGLE_PANE,    // Standard single-pane layout
        SPLIT_PANE,     // Split-pane layout (list + detail)
        MULTI_PANE      // Multi-pane layout for very large screens
    }
    
    /**
     * Navigation state for tablet
     */
    data class TabletNavigationState(
        val selectedRecipeId: String? = null,
        val selectedCategory: String? = null,
        val searchQuery: String = "",
        val layoutMode: TabletLayoutMode = TabletLayoutMode.SINGLE_PANE
    )
}
