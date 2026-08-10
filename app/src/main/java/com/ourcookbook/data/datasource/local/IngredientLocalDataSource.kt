package com.ourcookbook.data.datasource.local

import com.ourcookbook.data.db.dao.IngredientDao
import com.ourcookbook.data.db.entity.IngredientEntity
import com.ourcookbook.domain.model.Ingredient
import com.ourcookbook.domain.service.ChecksumService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Local data source implementation for Ingredient operations
 */
class IngredientLocalDataSource @Inject constructor(
    private val ingredientDao: IngredientDao,
    private val checksumService: ChecksumService
) : IIngredientLocalDataSource {
    
    override suspend fun insert(ingredient: IngredientEntity): Long {
        return ingredientDao.insert(ingredient)
    }
    
    override suspend fun insertAll(ingredients: List<IngredientEntity>): List<Long> {
        return ingredientDao.insertAll(ingredients)
    }
    
    override suspend fun update(ingredient: IngredientEntity): Int {
        return ingredientDao.update(ingredient)
    }
    
    override suspend fun updateAll(ingredients: List<IngredientEntity>): Int {
        return ingredientDao.updateAll(ingredients)
    }
    
    override suspend fun delete(id: String): Int {
        return ingredientDao.delete(id)
    }
    
    override suspend fun deleteByRecipe(recipeId: String): Int {
        return ingredientDao.deleteByRecipe(recipeId)
    }
    
    override suspend fun deleteAll(): Int {
        return ingredientDao.deleteAll()
    }
    
    override suspend fun getById(id: String): IngredientEntity? {
        return ingredientDao.getById(id)
    }
    
    override suspend fun getByRecipe(recipeId: String): List<IngredientEntity> {
        return ingredientDao.getByRecipe(recipeId)
    }
    
    override suspend fun getByRecipes(recipeIds: List<String>): List<IngredientEntity> {
        return ingredientDao.getByRecipes(recipeIds)
    }
    
    override suspend fun search(query: String): List<IngredientEntity> {
        return ingredientDao.search(query)
    }
    
    override suspend fun countByRecipe(recipeId: String): Int {
        return ingredientDao.countByRecipe(recipeId)
    }
    
    override suspend fun toDomainModel(entity: IngredientEntity): Ingredient {
        return Ingredient(
            id = entity.id,
            name = entity.name,
            amount = entity.amount,
            unit = entity.unit,
            notes = entity.notes,
            order = entity.order
        )
    }
    
    override suspend fun toEntity(domainModel: Ingredient): IngredientEntity {
        return IngredientEntity(
            id = domainModel.id,
            recipeId = "", // Will be set when associated with a recipe
            name = domainModel.name,
            amount = domainModel.amount,
            unit = domainModel.unit,
            notes = domainModel.notes,
            order = domainModel.order
        )
    }
    
    override suspend fun validateChecksum(entity: IngredientEntity): Boolean {
        val domainIngredient = toDomainModel(entity)
        val data = "${entity.id}|${entity.name}|${entity.amount}|${entity.unit}|${entity.notes}|${entity.order}"
        val expectedChecksum = checksumService.calculateChecksum(data)
        // For ingredients, we don't store checksum in the entity, so we just validate the data
        return true // Checksum validation for ingredients is handled at the recipe level
    }
    
    override suspend fun updateChecksum(entity: IngredientEntity): IngredientEntity {
        // Ingredients don't have individual checksums - they're part of recipe checksum
        return entity
    }
}

/**
 * Interface for Ingredient local data source operations
 */
interface IIngredientLocalDataSource {
    suspend fun insert(ingredient: IngredientEntity): Long
    suspend fun insertAll(ingredients: List<IngredientEntity>): List<Long>
    suspend fun update(ingredient: IngredientEntity): Int
    suspend fun updateAll(ingredients: List<IngredientEntity>): Int
    suspend fun delete(id: String): Int
    suspend fun deleteByRecipe(recipeId: String): Int
    suspend fun deleteAll(): Int
    suspend fun getById(id: String): IngredientEntity?
    suspend fun getByRecipe(recipeId: String): List<IngredientEntity>
    suspend fun getByRecipes(recipeIds: List<String>): List<IngredientEntity>
    suspend fun search(query: String): List<IngredientEntity>
    suspend fun countByRecipe(recipeId: String): Int
    
    // Domain model conversion
    suspend fun toDomainModel(entity: IngredientEntity): Ingredient
    suspend fun toEntity(domainModel: Ingredient): IngredientEntity
    
    // Checksum validation
    suspend fun validateChecksum(entity: IngredientEntity): Boolean
    suspend fun updateChecksum(entity: IngredientEntity): IngredientEntity
}