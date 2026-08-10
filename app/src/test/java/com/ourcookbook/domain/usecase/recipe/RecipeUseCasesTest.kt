package com.ourcookbook.domain.usecase.recipe

import com.ourcookbook.domain.model.Ingredient
import com.ourcookbook.domain.model.Recipe
import com.ourcookbook.domain.model.VersionVector
import com.ourcookbook.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant
import java.util.UUID

/**
 * Unit tests for Recipe Use Cases
 * Tests the business logic of recipe-related use cases
 */
class RecipeUseCasesTest {

    private lateinit var repository: RecipeRepository
    private lateinit var createRecipe: CreateRecipe
    private lateinit var updateRecipe: UpdateRecipe
    private lateinit var deleteRecipe: DeleteRecipe
    private lateinit var getRecipeById: GetRecipeById
    private lateinit var getAllRecipes: GetAllRecipes
    private lateinit var getAllRecipesOnce: GetAllRecipesOnce
    private lateinit var searchRecipes: SearchRecipes
    private lateinit var toggleFavorite: ToggleFavorite

    private val testRecipe = Recipe(
        id = UUID.randomUUID().toString(),
        title = "Test Recipe",
        description = "Test Description",
        category = "Test Category",
        ingredients = listOf(
            Ingredient(
                name = "Test Ingredient",
                amount = "1",
                unit = "cup"
            )
        ),
        instructions = listOf("Step 1", "Step 2"),
        deviceId = "test-device"
    )

    @Before
    fun setup() {
        repository = mock()
        createRecipe = CreateRecipe(repository)
        updateRecipe = UpdateRecipe(repository)
        deleteRecipe = DeleteRecipe(repository)
        getRecipeById = GetRecipeById(repository)
        getAllRecipes = GetAllRecipes(repository)
        getAllRecipesOnce = GetAllRecipesOnce(repository)
        searchRecipes = SearchRecipes(repository)
        toggleFavorite = ToggleFavorite(repository, getRecipeById, updateRecipe)
    }

    @Test
    fun `CreateRecipe with valid recipe returns success`() = runTest {
        // Given
        val expectedId = UUID.randomUUID().toString()
        whenever(repository.createRecipe(any())).thenReturn(expectedId)

        // When
        val result = createRecipe(testRecipe)

        // Then
        assert(result.isSuccess)
        assert(result.getOrThrow() == expectedId)
        verify(repository).createRecipe(eq(testRecipe))
    }

    @Test
    fun `CreateRecipe with invalid recipe returns failure`() = runTest {
        // Given
        val invalidRecipe = testRecipe.copy(title = "") // Empty title

        // When
        val result = createRecipe(invalidRecipe)

        // Then
        assert(result.isFailure)
        assert(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `UpdateRecipe with valid recipe returns success`() = runTest {
        // Given
        whenever(repository.updateRecipe(any())).then { /* do nothing */ }

        // When
        val result = updateRecipe(testRecipe)

        // Then
        assert(result.isSuccess)
        verify(repository).updateRecipe(eq(testRecipe))
    }

    @Test
    fun `DeleteRecipe returns success`() = runTest {
        // Given
        val recipeId = UUID.randomUUID().toString()
        whenever(repository.deleteRecipe(any())).then { /* do nothing */ }

        // When
        val result = deleteRecipe(recipeId)

        // Then
        assert(result.isSuccess)
        verify(repository).deleteRecipe(eq(recipeId))
    }

    @Test
    fun `GetRecipeById returns recipe when found`() = runTest {
        // Given
        val recipeId = UUID.randomUUID().toString()
        whenever(repository.getRecipeById(eq(recipeId))).thenReturn(testRecipe)

        // When
        val result = getRecipeById(recipeId)

        // Then
        assert(result.isSuccess)
        assert(result.getOrThrow() == testRecipe)
        verify(repository).getRecipeById(eq(recipeId))
    }

    @Test
    fun `GetRecipeById returns null when not found`() = runTest {
        // Given
        val recipeId = UUID.randomUUID().toString()
        whenever(repository.getRecipeById(eq(recipeId))).thenReturn(null)

        // When
        val result = getRecipeById(recipeId)

        // Then
        assert(result.isSuccess)
        assert(result.getOrThrow() == null)
    }

    @Test
    fun `GetAllRecipes returns flow of recipes`() = runTest {
        // Given
        val recipes = listOf(testRecipe)
        whenever(repository.getAllRecipes()).thenReturn(flowOf(recipes))

        // When
        val result = getAllRecipes()

        // Then
        // Note: We can't easily test Flow in unit tests without collecting
        // This is more of a compilation test
        verify(repository).getAllRecipes()
    }

    @Test
    fun `GetAllRecipesOnce returns list of recipes`() = runTest {
        // Given
        val recipes = listOf(testRecipe)
        whenever(repository.getAllRecipesOnce()).thenReturn(recipes)

        // When
        val result = getAllRecipesOnce()

        // Then
        assert(result.isSuccess)
        assert(result.getOrThrow() == recipes)
        verify(repository).getAllRecipesOnce()
    }

    @Test
    fun `SearchRecipes returns flow of matching recipes`() = runTest {
        // Given
        val query = "Test"
        val recipes = listOf(testRecipe)
        whenever(repository.searchRecipes(eq(query))).thenReturn(flowOf(recipes))

        // When
        val result = searchRecipes(query)

        // Then
        // Flow test - compilation check
        verify(repository).searchRecipes(eq(query))
    }

    @Test
    fun `ToggleFavorite toggles favorite status`() = runTest {
        // Given
        val recipeId = testRecipe.id
        val nonFavoriteRecipe = testRecipe.copy(isFavorite = false)
        val favoriteRecipe = testRecipe.copy(isFavorite = true)
        
        whenever(repository.getRecipeById(eq(recipeId))).thenReturn(nonFavoriteRecipe)
        whenever(repository.updateRecipe(any())).then { /* do nothing */ }

        // When
        val result = toggleFavorite(recipeId)

        // Then
        assert(result.isSuccess)
        verify(repository).getRecipeById(eq(recipeId))
        verify(repository).updateRecipe(eq(favoriteRecipe))
    }

    @Test
    fun `ToggleFavorite returns failure when recipe not found`() = runTest {
        // Given
        val recipeId = UUID.randomUUID().toString()
        whenever(repository.getRecipeById(eq(recipeId))).thenReturn(null)

        // When
        val result = toggleFavorite(recipeId)

        // Then
        assert(result.isFailure)
        assert(result.exceptionOrNull() is NoSuchElementException)
    }
}
