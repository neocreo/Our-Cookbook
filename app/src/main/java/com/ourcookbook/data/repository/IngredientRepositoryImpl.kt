package com.ourcookbook.data.repository

import com.ourcookbook.data.datasource.local.IIngredientLocalDataSource
import com.ourcookbook.domain.model.Ingredient
import com.ourcookbook.domain.repository.IngredientRepository
import com.ourcookbook.domain.service.ChecksumService
import javax.inject.Inject

/**
 * Repository implementation for Ingredient operations
 */
class IngredientRepositoryImpl @Inject constructor(
    private val localDataSource: IIngredientLocalDataSource,
    private val checksumService: ChecksumService
) : IngredientRepository {
    
    override suspend fun createIngredient(ingredient: Ingredient): String {
        if (!ingredient.isValid()) {
            throw IllegalArgumentException("Ingredient is not valid")
        }
        
        val entity = localDataSource.toEntity(ingredient)
        val entityId = localDataSource.insert(entity)
        return ingredient.id
    }
    
    override suspend fun updateIngredient(ingredient: Ingredient) {
        if (!ingredient.isValid()) {
            throw IllegalArgumentException("Ingredient is not valid")
        }
        
        val entity = localDataSource.toEntity(ingredient)
        localDataSource.update(entity)
    }
    
    override suspend fun deleteIngredient(id: String) {
        localDataSource.delete(id)
    }
    
    override suspend fun deleteIngredientsByRecipe(recipeId: String) {
        localDataSource.deleteByRecipe(recipeId)
    }
    
    override suspend fun getIngredientById(id: String): Ingredient? {
        return localDataSource.getById(id)?.let { entity ->
            localDataSource.toDomainModel(entity)
        }
    }
    
    override suspend fun getIngredientsByRecipe(recipeId: String): List<Ingredient> {
        return localDataSource.getByRecipe(recipeId).map { entity ->
            localDataSource.toDomainModel(entity)
        }
    }
    
    override suspend fun getIngredientsByRecipes(recipeIds: List<String>): List<Ingredient> {
        return localDataSource.getByRecipes(recipeIds).map { entity ->
            localDataSource.toDomainModel(entity)
        }
    }
    
    override suspend fun searchIngredients(query: String): List<Ingredient> {
        return localDataSource.search(query).map { entity ->
            localDataSource.toDomainModel(entity)
        }
    }
    
    override suspend fun getIngredientCountByRecipe(recipeId: String): Int {
        return localDataSource.countByRecipe(recipeId)
    }
    
    override suspend fun getAllIngredients(): List<Ingredient> {
        // Get all ingredients from all recipes
        // This is a simplified implementation - in a real app, you might want to paginate
        return localDataSource.getByRecipes(emptyList()).map { entity ->
            localDataSource.toDomainModel(entity)
        }
    }
    
    override suspend fun validateIngredientChecksum(ingredientId: String): Boolean {
        return localDataSource.getById(ingredientId)?.let { entity ->
            localDataSource.validateChecksum(entity)
        } ?: false
    }
    
    override suspend fun updateIngredientChecksum(ingredientId: String): Boolean {
        return localDataSource.getById(ingredientId)?.let { entity ->
            val updatedEntity = localDataSource.updateChecksum(entity)
            localDataSource.update(updatedEntity)
            true
        } ?: false
    }
}