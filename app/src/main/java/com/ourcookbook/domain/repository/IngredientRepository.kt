package com.ourcookbook.domain.repository

import com.ourcookbook.domain.model.Ingredient
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Ingredient operations
 * Defines the contract for ingredient data access in the domain layer
 */
interface IngredientRepository {
    
    // CRUD Operations
    suspend fun createIngredient(ingredient: Ingredient): String
    suspend fun updateIngredient(ingredient: Ingredient)
    suspend fun deleteIngredient(id: String)
    suspend fun deleteIngredientsByRecipe(recipeId: String)
    suspend fun getIngredientById(id: String): Ingredient?
    
    // Query Operations
    suspend fun getIngredientsByRecipe(recipeId: String): List<Ingredient>
    suspend fun getIngredientsByRecipes(recipeIds: List<String>): List<Ingredient>
    suspend fun searchIngredients(query: String): List<Ingredient>
    
    // Utility Operations
    suspend fun getIngredientCountByRecipe(recipeId: String): Int
    suspend fun getAllIngredients(): List<Ingredient>
    
    // Checksum Operations
    suspend fun validateIngredientChecksum(ingredientId: String): Boolean
    suspend fun updateIngredientChecksum(ingredientId: String): Boolean
}