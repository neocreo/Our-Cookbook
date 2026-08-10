package com.ourcookbook.ui.screens.recipe

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ourcookbook.domain.model.Ingredient
import com.ourcookbook.domain.model.Recipe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests for RecipeDetailScreen
 * Task 2.1.02: Recipe Detail Screen Implementation
 * 
 * Tests the Recipe Detail Screen UI components and functionality
 */

@RunWith(AndroidJUnit4::class)
class RecipeDetailScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockRecipe = Recipe.create(
        title = "Test Recipe",
        category = "Mains",
        description = "A test recipe for UI testing",
        ingredients = listOf(
            Ingredient.create("Ingredient 1", "1", "cup"),
            Ingredient.create("Ingredient 2", "2", "tbsp"),
            Ingredient.create("Ingredient 3", "3", "tsp")
        ),
        instructions = listOf(
            "Step 1: Do something",
            "Step 2: Do something else",
            "Step 3: Finish"
        ),
        servingSize = 4,
        prepTime = 10,
        cookTime = 20,
        rating = 4.5f,
        isFavorite = true,
        notes = "Test notes",
        source = "Test source",
        tags = listOf("Test", "Recipe"),
        deviceId = "test-device"
    )

    @Test
    fun recipeDetailScreen_displaysRecipeTitle() {
        composeTestRule.setContent {
            RecipeDetailContent(
                recipe = mockRecipe,
                onEditClick = {}
            )
        }

        composeTestRule.onNodeWithText("Test Recipe").assertExists()
    }

    @Test
    fun recipeDetailScreen_displaysIngredients() {
        composeTestRule.setContent {
            RecipeDetailContent(
                recipe = mockRecipe,
                onEditClick = {}
            )
        }

        composeTestRule.onNodeWithText("Ingredients").assertExists()
        composeTestRule.onNodeWithText("Ingredient 1").assertExists()
        composeTestRule.onNodeWithText("Ingredient 2").assertExists()
        composeTestRule.onNodeWithText("Ingredient 3").assertExists()
    }

    @Test
    fun recipeDetailScreen_displaysInstructions() {
        composeTestRule.setContent {
            RecipeDetailContent(
                recipe = mockRecipe,
                onEditClick = {}
            )
        }

        composeTestRule.onNodeWithText("Instructions").assertExists()
        composeTestRule.onNodeWithText("Step 1: Do something").assertExists()
        composeTestRule.onNodeWithText("Step 2: Do something else").assertExists()
        composeTestRule.onNodeWithText("Step 3: Finish").assertExists()
    }

    @Test
    fun recipeDetailScreen_displaysMetadata() {
        composeTestRule.setContent {
            RecipeDetailContent(
                recipe = mockRecipe,
                onEditClick = {}
            )
        }

        composeTestRule.onNodeWithText("Serves: 4").assertExists()
        composeTestRule.onNodeWithText("Prep: 10min").assertExists()
        composeTestRule.onNodeWithText("Cook: 20min").assertExists()
    }

    @Test
    fun recipeDetailScreen_displaysAdditionalInfo() {
        composeTestRule.setContent {
            RecipeDetailContent(
                recipe = mockRecipe,
                onEditClick = {}
            )
        }

        composeTestRule.onNodeWithText("Notes").assertExists()
        composeTestRule.onNodeWithText("Test notes").assertExists()
        composeTestRule.onNodeWithText("Source").assertExists()
        composeTestRule.onNodeWithText("Test source").assertExists()
        composeTestRule.onNodeWithText("Tags").assertExists()
    }

    @Test
    fun recipeDetailScreen_displaysCategory() {
        composeTestRule.setContent {
            RecipeDetailContent(
                recipe = mockRecipe,
                onEditClick = {}
            )
        }

        composeTestRule.onNodeWithText("Mains").assertExists()
    }

    @Test
    fun recipeDetailScreen_displaysRating() {
        composeTestRule.setContent {
            RecipeDetailContent(
                recipe = mockRecipe,
                onEditClick = {}
            )
        }

        composeTestRule.onNodeWithText("4.5").assertExists()
    }

    @Test
    fun recipeDetailScreen_displaysEmptyStateForNoIngredients() {
        val recipeWithNoIngredients = mockRecipe.copy(ingredients = emptyList())
        
        composeTestRule.setContent {
            RecipeDetailContent(
                recipe = recipeWithNoIngredients,
                onEditClick = {}
            )
        }

        composeTestRule.onNodeWithText("No ingredients listed").assertExists()
    }

    @Test
    fun recipeDetailScreen_displaysEmptyStateForNoInstructions() {
        val recipeWithNoInstructions = mockRecipe.copy(instructions = emptyList())
        
        composeTestRule.setContent {
            RecipeDetailContent(
                recipe = recipeWithNoInstructions,
                onEditClick = {}
            )
        }

        composeTestRule.onNodeWithText("No instructions provided").assertExists()
    }

    @Test
    fun recipeDetailScreen_displaysPlaceholderForNoImage() {
        val recipeWithNoImage = mockRecipe.copy(imageUrl = null)
        
        composeTestRule.setContent {
            RecipeDetailContent(
                recipe = recipeWithNoImage,
                onEditClick = {}
            )
        }

        composeTestRule.onNodeWithText("No image available").assertExists()
    }

    @Test
    fun recipeDetailScreen_editButtonClick() {
        var editClicked = false
        
        composeTestRule.setContent {
            RecipeDetailContent(
                recipe = mockRecipe,
                onEditClick = { editClicked = true }
            )
        }

        composeTestRule.onNodeWithText("Edit Recipe").performClick()
        
        assert(editClicked)
    }
}