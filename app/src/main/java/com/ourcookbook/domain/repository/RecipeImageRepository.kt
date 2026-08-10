package com.ourcookbook.domain.repository

import com.ourcookbook.domain.model.RecipeImage
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for RecipeImage operations
 * Defines the contract for recipe image data access in the domain layer
 */
interface RecipeImageRepository {
    
    // CRUD Operations
    suspend fun createRecipeImage(image: RecipeImage): String
    suspend fun updateRecipeImage(image: RecipeImage)
    suspend fun deleteRecipeImage(id: String)
    suspend fun deleteRecipeImagesByRecipe(recipeId: String)
    suspend fun getRecipeImageById(id: String): RecipeImage?
    
    // Query Operations
    suspend fun getRecipeImagesByRecipe(recipeId: String): List<RecipeImage>
    suspend fun getRecipeImagesByRecipes(recipeIds: List<String>): List<RecipeImage>
    suspend fun getAllRecipeImages(): List<RecipeImage>
    
    // Utility Operations
    suspend fun getRecipeImageCountByRecipe(recipeId: String): Int
    
    // Checksum Operations
    suspend fun validateRecipeImageChecksum(imageId: String): Boolean
    suspend fun updateRecipeImageChecksum(imageId: String): Boolean
}