package com.ourcookbook.domain.usecase.recipeimage

import com.ourcookbook.domain.model.RecipeImage
import com.ourcookbook.domain.repository.RecipeImageRepository

/**
 * Use cases for Recipe Image operations
 * These use cases encapsulate the business logic for recipe image management
 */

// Create Recipe Image Use Case
class CreateRecipeImage(
    private val repository: RecipeImageRepository
) {
    suspend operator fun invoke(image: RecipeImage): Result<String> {
        return try {
            val imageId = repository.createRecipeImage(image)
            Result.success(imageId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Update Recipe Image Use Case
class UpdateRecipeImage(
    private val repository: RecipeImageRepository
) {
    suspend operator fun invoke(image: RecipeImage): Result<Unit> {
        return try {
            repository.updateRecipeImage(image)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Delete Recipe Image Use Case
class DeleteRecipeImage(
    private val repository: RecipeImageRepository
) {
    suspend operator fun invoke(imageId: String): Result<Unit> {
        return try {
            repository.deleteRecipeImage(imageId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Delete Recipe Images By Recipe Use Case
class DeleteRecipeImagesByRecipe(
    private val repository: RecipeImageRepository
) {
    suspend operator fun invoke(recipeId: String): Result<Unit> {
        return try {
            repository.deleteRecipeImagesByRecipe(recipeId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Get Recipe Image By ID Use Case
class GetRecipeImageById(
    private val repository: RecipeImageRepository
) {
    suspend operator fun invoke(imageId: String): Result<RecipeImage?> {
        return try {
            val image = repository.getRecipeImageById(imageId)
            Result.success(image)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Get Recipe Images By Recipe Use Case
class GetRecipeImagesByRecipe(
    private val repository: RecipeImageRepository
) {
    suspend operator fun invoke(recipeId: String): Result<List<RecipeImage>> {
        return try {
            val images = repository.getRecipeImagesByRecipe(recipeId)
            Result.success(images)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Get Recipe Images By Recipes Use Case
class GetRecipeImagesByRecipes(
    private val repository: RecipeImageRepository
) {
    suspend operator fun invoke(recipeIds: List<String>): Result<List<RecipeImage>> {
        return try {
            val images = repository.getRecipeImagesByRecipes(recipeIds)
            Result.success(images)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Get All Recipe Images Use Case
class GetAllRecipeImages(
    private val repository: RecipeImageRepository
) {
    suspend operator fun invoke(): Result<List<RecipeImage>> {
        return try {
            val images = repository.getAllRecipeImages()
            Result.success(images)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Get Recipe Image Count By Recipe Use Case
class GetRecipeImageCountByRecipe(
    private val repository: RecipeImageRepository
) {
    suspend operator fun invoke(recipeId: String): Result<Int> {
        return try {
            val count = repository.getRecipeImageCountByRecipe(recipeId)
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Validate Recipe Image Checksum Use Case
class ValidateRecipeImageChecksum(
    private val repository: RecipeImageRepository
) {
    suspend operator fun invoke(imageId: String): Result<Boolean> {
        return try {
            val isValid = repository.validateRecipeImageChecksum(imageId)
            Result.success(isValid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Update Recipe Image Checksum Use Case
class UpdateRecipeImageChecksum(
    private val repository: RecipeImageRepository
) {
    suspend operator fun invoke(imageId: String): Result<Boolean> {
        return try {
            val updated = repository.updateRecipeImageChecksum(imageId)
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
