package com.ourcookbook.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ourcookbook.domain.model.Cookbook
import com.ourcookbook.domain.model.Ingredient
import com.ourcookbook.domain.model.Recipe
import com.ourcookbook.domain.usecase.cookbook.AddRecipeToCookbook
import com.ourcookbook.domain.usecase.cookbook.CreateCookbook
import com.ourcookbook.domain.usecase.cookbook.GetCookbooks
import com.ourcookbook.domain.usecase.recipe.CreateRecipe
import com.ourcookbook.domain.usecase.recipe.GetRecipes
import com.ourcookbook.domain.usecase.recipe.SearchRecipes
import com.ourcookbook.ui.service.SyncStatusService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Instant
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
    val cookbooks: List<Cookbook> = emptyList(),
    val syncStatus: String = "IDLE"
)

/**
 * ViewModel for HomeScreen
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getRecipes: GetRecipes,
    private val getCookbooks: GetCookbooks,
    private val searchRecipes: SearchRecipes,
    private val syncStatusService: SyncStatusService,
    private val createCookbook: CreateCookbook,
    private val createRecipe: CreateRecipe,
    private val addRecipeToCookbook: AddRecipeToCookbook
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private var seeded = false

    init {
        observeData()
        observeSyncStatus()
    }

    private fun currentDeviceId(): String {
        return context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
            .getString("device_id", null) ?: "local-device"
    }

    /**
     * Observe recipes and cookbooks reactively. Clears isLoading once the first
     * emission arrives so the screen never sticks on a spinner. Seeds a sample
     * cookbook + recipes on first run when none exist.
     */
    private fun observeData() {
        viewModelScope.launch {
            try {
                if (!seeded) {
                    seedSampleCookbookIfNeeded()
                }

                getRecipes()
                    .catch { e -> _state.value = _state.value.copy(isLoading = false, error = "Failed to load recipes: ${e.message}") }
                    .combine(
                        getCookbooks().catch { /* cookbooks are non-critical */ }
                    ) { recipeResult, cookbookResult ->
                        HomeData(recipeResult.getOrDefault(emptyList()), cookbookResult.getOrDefault(emptyList()))
                    }
                    .collect { data ->
                        val recentRecipes = data.recipes.sortedByDescending { it.updatedAt }.take(5)
                        val favorites = data.recipes.filter { it.isFavorite }
                        _state.value = _state.value.copy(
                            recentRecipes = recentRecipes,
                            favorites = favorites,
                            cookbooks = data.cookbooks,
                            categories = DEFAULT_CATEGORIES,
                            isLoading = false,
                            error = null
                        )
                    }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Failed to load data: ${e.message}"
                )
            }
        }
    }

    private data class HomeData(val recipes: List<Recipe>, val cookbooks: List<Cookbook>)

    private suspend fun seedSampleCookbookIfNeeded() {
        try {
            val existing = getCookbooks().first().getOrDefault(emptyList())
            if (existing.isNotEmpty()) {
                seeded = true
                return
            }

            val deviceId = currentDeviceId()
            val cookbook = Cookbook(
                name = "My First Cookbook",
                description = "A sample cookbook to get you started",
                ownerDeviceId = deviceId
            )
            val cookbookId = createCookbook(cookbook).getOrElse {
                seeded = true
                return
            }

            sampleRecipes(deviceId).forEach { recipe ->
                createRecipe(recipe).onSuccess { recipeId ->
                    addRecipeToCookbook(cookbookId, recipeId)
                }
            }
            seeded = true
        } catch (e: Exception) {
            seeded = true
        }
    }

    private fun sampleRecipes(deviceId: String): List<Recipe> {
        val now = Instant.now()
        return listOf(
            Recipe(
                title = "Classic Pancakes",
                description = "Fluffy breakfast pancakes",
                category = "Breakfast & Brunch",
                ingredients = listOf(
                    Ingredient(name = "all-purpose flour", amount = "1 1/2", unit = "cups", order = 0),
                    Ingredient(name = "milk", amount = "1 1/4", unit = "cups", order = 1),
                    Ingredient(name = "egg", amount = "1", unit = "", order = 2),
                    Ingredient(name = "baking powder", amount = "1", unit = "tbsp", order = 3),
                    Ingredient(name = "salt", amount = "1/2", unit = "tsp", order = 4)
                ),
                instructions = listOf(
                    "Whisk flour, baking powder and salt together.",
                    "Beat in the egg and milk until smooth.",
                    "Pour batter onto a hot griddle and cook until bubbles form.",
                    "Flip and cook until golden brown."
                ),
                servingSize = 4,
                prepTime = 10,
                cookTime = 15,
                source = "Sample recipe",
                tags = listOf("breakfast", "easy"),
                deviceId = deviceId,
                createdAt = now,
                updatedAt = now
            ),
            Recipe(
                title = "Hearty Beef Stew",
                description = "A warming one-pot dinner",
                category = "Soups & Stews",
                ingredients = listOf(
                    Ingredient(name = "beef chuck", amount = "2", unit = "lb", order = 0),
                    Ingredient(name = "onion", amount = "1", unit = "", order = 1),
                    Ingredient(name = "carrots", amount = "3", unit = "", order = 2),
                    Ingredient(name = "potatoes", amount = "4", unit = "", order = 3),
                    Ingredient(name = "beef broth", amount = "4", unit = "cups", order = 4)
                ),
                instructions = listOf(
                    "Sear the cubed beef in a hot pot until browned.",
                    "Add onion and cook until softened.",
                    "Pour in broth, add carrots and potatoes, and simmer.",
                    "Cover and cook on low for 2 hours until tender."
                ),
                servingSize = 6,
                prepTime = 15,
                cookTime = 120,
                source = "Sample recipe",
                tags = listOf("dinner", "comfort"),
                deviceId = deviceId,
                createdAt = now,
                updatedAt = now
            ),
            Recipe(
                title = "Chocolate Chip Cookies",
                description = "Soft-baked classic cookies",
                category = "Desserts",
                ingredients = listOf(
                    Ingredient(name = "butter", amount = "1", unit = "cup", order = 0),
                    Ingredient(name = "brown sugar", amount = "3/4", unit = "cup", order = 1),
                    Ingredient(name = "flour", amount = "2 1/4", unit = "cups", order = 2),
                    Ingredient(name = "chocolate chips", amount = "2", unit = "cups", order = 3),
                    Ingredient(name = "egg", amount = "2", unit = "", order = 4)
                ),
                instructions = listOf(
                    "Cream butter and sugar until light.",
                    "Beat in eggs one at a time.",
                    "Stir in flour and fold in chocolate chips.",
                    "Drop onto a tray and bake at 180C for 10-12 minutes."
                ),
                servingSize = 24,
                prepTime = 15,
                cookTime = 12,
                source = "Sample recipe",
                tags = listOf("dessert", "baking"),
                deviceId = deviceId,
                createdAt = now,
                updatedAt = now
            )
        )
    }

    private fun observeSyncStatus() {
        viewModelScope.launch {
            syncStatusService.syncStatus.collect { status ->
                _state.value = _state.value.copy(syncStatus = status.toString())
            }
        }
    }

    fun loadData() {
        // Reactive observation starts in init; kept as a manual refresh hook.
    }

    fun refresh() {
        _state.value = _state.value.copy(isLoading = false)
    }

    companion object {
        val DEFAULT_CATEGORIES = listOf(
            "Breakfast & Brunch",
            "Appetizers",
            "Soups & Stews",
            "Salads",
            "Sides",
            "Main Dishes",
            "Desserts",
            "Snacks",
            "Beverages",
            "Condiments"
        )
    }
}
