package com.ourcookbook.domain.usecase.recipe

import com.ourcookbook.domain.model.Recipe
import com.ourcookbook.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use cases for Recipe search and filtering operations
 * These use cases encapsulate the business logic for recipe discovery
 */

// Search Recipes Use Case
class SearchRecipes(
    private val repository: RecipeRepository
) {
    operator fun invoke(query: String): Flow<List<Recipe>> {
        return repository.searchRecipes(query)
    }
}

// Get Recipes By Category Use Case
class GetRecipesByCategory(
    private val repository: RecipeRepository
) {
    operator fun invoke(category: String): Flow<List<Recipe>> {
        return repository.getRecipesByCategory(category)
    }
}

// Get Favorites Use Case
class GetFavorites(
    private val repository: RecipeRepository
) {
    operator fun invoke(): Flow<List<Recipe>> {
        return repository.getFavorites()
    }
}

// Get Recipes By Device Use Case
class GetRecipesByDevice(
    private val repository: RecipeRepository
) {
    operator fun invoke(deviceId: String): Flow<List<Recipe>> {
        return repository.getRecipesByDevice(deviceId)
    }
}

// Toggle Favorite Use Case
class ToggleFavorite(
    private val repository: RecipeRepository,
    private val getRecipeById: GetRecipeById,
    private val updateRecipe: UpdateRecipe
) {
    suspend operator fun invoke(recipeId: String): Result<Unit> {
        return try {
            val recipe = getRecipeById(recipeId).getOrThrow()
                ?: return Result.failure(NoSuchElementException("Recipe not found"))
            
            val updatedRecipe = recipe.copy(isFavorite = !recipe.isFavorite)
            updateRecipe(updatedRecipe)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Filter Recipes By Tags Use Case
class FilterRecipesByTags(
    private val repository: RecipeRepository
) {
    suspend operator fun invoke(tags: List<String>): Result<List<Recipe>> {
        return try {
            val allRecipes = repository.getAllRecipesOnce()
            val filtered = allRecipes.filter { recipe ->
                tags.all { tag -> recipe.tags.contains(tag) }
            }
            Result.success(filtered)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Filter Recipes By Cooking Time Use Case
class FilterRecipesByCookingTime(
    private val repository: RecipeRepository
) {
    suspend operator fun invoke(maxMinutes: Int): Result<List<Recipe>> {
        return try {
            val allRecipes = repository.getAllRecipesOnce()
            val filtered = allRecipes.filter { recipe ->
                recipe.totalTime?.let { it <= maxMinutes } ?: false
            }
            Result.success(filtered)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Filter Recipes By Serving Size Use Case
class FilterRecipesByServingSize(
    private val repository: RecipeRepository
) {
    suspend operator fun invoke(minServings: Int, maxServings: Int): Result<List<Recipe>> {
        return try {
            val allRecipes = repository.getAllRecipesOnce()
            val filtered = allRecipes.filter { recipe ->
                recipe.servingSize?.let { it in minServings..maxServings } ?: false
            }
            Result.success(filtered)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
