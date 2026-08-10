package com.ourcookbook.domain.usecase.recipe

import com.ourcookbook.domain.model.Recipe
import com.ourcookbook.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import java.time.Instant

/**
 * Use cases for Recipe CRUD operations
 * These use cases encapsulate the business logic for recipe management
 */

// Create Recipe Use Case
class CreateRecipe(
    private val repository: RecipeRepository
) {
    suspend operator fun invoke(recipe: Recipe): Result<String> {
        return try {
            if (!recipe.isValid()) {
                return Result.failure(IllegalArgumentException("Recipe validation failed"))
            }
            val recipeId = repository.createRecipe(recipe)
            Result.success(recipeId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Update Recipe Use Case
class UpdateRecipe(
    private val repository: RecipeRepository
) {
    suspend operator fun invoke(recipe: Recipe): Result<Unit> {
        return try {
            if (!recipe.isValid()) {
                return Result.failure(IllegalArgumentException("Recipe validation failed"))
            }
            repository.updateRecipe(recipe)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Delete Recipe Use Case
class DeleteRecipe(
    private val repository: RecipeRepository
) {
    suspend operator fun invoke(recipeId: String): Result<Unit> {
        return try {
            repository.deleteRecipe(recipeId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Get Recipe By ID Use Case
class GetRecipeById(
    private val repository: RecipeRepository
) {
    suspend operator fun invoke(recipeId: String): Result<Recipe?> {
        return try {
            val recipe = repository.getRecipeById(recipeId)
            Result.success(recipe)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Get All Recipes Use Case
class GetAllRecipes(
    private val repository: RecipeRepository
) {
    operator fun invoke(): Flow<List<Recipe>> {
        return repository.getAllRecipes()
    }
}

// Get All Recipes Once Use Case
class GetAllRecipesOnce(
    private val repository: RecipeRepository
) {
    suspend operator fun invoke(): Result<List<Recipe>> {
        return try {
            val recipes = repository.getAllRecipesOnce()
            Result.success(recipes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Get Recipes By IDs Use Case
class GetRecipesByIds(
    private val repository: RecipeRepository
) {
    suspend operator fun invoke(ids: List<String>): Result<List<Recipe>> {
        return try {
            val recipes = repository.getRecipesByIds(ids)
            Result.success(recipes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Get Recipe Count Use Case
class GetRecipeCount(
    private val repository: RecipeRepository
) {
    suspend operator fun invoke(): Result<Int> {
        return try {
            val count = repository.getRecipeCount()
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Get Recent Recipes Use Case
class GetRecentRecipes(
    private val repository: RecipeRepository
) {
    suspend operator fun invoke(limit: Int): Result<List<Recipe>> {
        return try {
            val recipes = repository.getRecentRecipes(limit)
            Result.success(recipes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Get Top Rated Recipes Use Case
class GetTopRatedRecipes(
    private val repository: RecipeRepository
) {
    suspend operator fun invoke(limit: Int): Result<List<Recipe>> {
        return try {
            val recipes = repository.getTopRatedRecipes(limit)
            Result.success(recipes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
