package com.ourcookbook.domain.usecase.ingredient

import com.ourcookbook.domain.model.Ingredient
import com.ourcookbook.domain.repository.IngredientRepository

/**
 * Use cases for Ingredient CRUD operations
 * These use cases encapsulate the business logic for ingredient management
 */

// Create Ingredient Use Case
class CreateIngredient(
    private val repository: IngredientRepository
) {
    suspend operator fun invoke(ingredient: Ingredient): Result<String> {
        return try {
            if (!ingredient.isValid()) {
                return Result.failure(IllegalArgumentException("Ingredient validation failed"))
            }
            val ingredientId = repository.createIngredient(ingredient)
            Result.success(ingredientId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Update Ingredient Use Case
class UpdateIngredient(
    private val repository: IngredientRepository
) {
    suspend operator fun invoke(ingredient: Ingredient): Result<Unit> {
        return try {
            if (!ingredient.isValid()) {
                return Result.failure(IllegalArgumentException("Ingredient validation failed"))
            }
            repository.updateIngredient(ingredient)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Delete Ingredient Use Case
class DeleteIngredient(
    private val repository: IngredientRepository
) {
    suspend operator fun invoke(ingredientId: String): Result<Unit> {
        return try {
            repository.deleteIngredient(ingredientId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Delete Ingredients By Recipe Use Case
class DeleteIngredientsByRecipe(
    private val repository: IngredientRepository
) {
    suspend operator fun invoke(recipeId: String): Result<Unit> {
        return try {
            repository.deleteIngredientsByRecipe(recipeId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Get Ingredient By ID Use Case
class GetIngredientById(
    private val repository: IngredientRepository
) {
    suspend operator fun invoke(ingredientId: String): Result<Ingredient?> {
        return try {
            val ingredient = repository.getIngredientById(ingredientId)
            Result.success(ingredient)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Get Ingredients By Recipe Use Case
class GetIngredientsByRecipe(
    private val repository: IngredientRepository
) {
    suspend operator fun invoke(recipeId: String): Result<List<Ingredient>> {
        return try {
            val ingredients = repository.getIngredientsByRecipe(recipeId)
            Result.success(ingredients)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Get Ingredients By Recipes Use Case
class GetIngredientsByRecipes(
    private val repository: IngredientRepository
) {
    suspend operator fun invoke(recipeIds: List<String>): Result<List<Ingredient>> {
        return try {
            val ingredients = repository.getIngredientsByRecipes(recipeIds)
            Result.success(ingredients)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Search Ingredients Use Case
class SearchIngredients(
    private val repository: IngredientRepository
) {
    suspend operator fun invoke(query: String): Result<List<Ingredient>> {
        return try {
            val ingredients = repository.searchIngredients(query)
            Result.success(ingredients)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Get Ingredient Count By Recipe Use Case
class GetIngredientCountByRecipe(
    private val repository: IngredientRepository
) {
    suspend operator fun invoke(recipeId: String): Result<Int> {
        return try {
            val count = repository.getIngredientCountByRecipe(recipeId)
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Get All Ingredients Use Case
class GetAllIngredients(
    private val repository: IngredientRepository
) {
    suspend operator fun invoke(): Result<List<Ingredient>> {
        return try {
            val ingredients = repository.getAllIngredients()
            Result.success(ingredients)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Validate Ingredient Checksum Use Case
class ValidateIngredientChecksum(
    private val repository: IngredientRepository
) {
    suspend operator fun invoke(ingredientId: String): Result<Boolean> {
        return try {
            val isValid = repository.validateIngredientChecksum(ingredientId)
            Result.success(isValid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Update Ingredient Checksum Use Case
class UpdateIngredientChecksum(
    private val repository: IngredientRepository
) {
    suspend operator fun invoke(ingredientId: String): Result<Boolean> {
        return try {
            val updated = repository.updateIngredientChecksum(ingredientId)
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
