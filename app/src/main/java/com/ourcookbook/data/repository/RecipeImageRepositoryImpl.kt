package com.ourcookbook.data.repository

import com.ourcookbook.data.datasource.local.IRecipeImageLocalDataSource
import com.ourcookbook.domain.model.RecipeImage
import com.ourcookbook.domain.repository.RecipeImageRepository
import com.ourcookbook.domain.service.ChecksumService
import javax.inject.Inject

/**
 * Repository implementation for RecipeImage operations
 */
class RecipeImageRepositoryImpl @Inject constructor(
    private val localDataSource: IRecipeImageLocalDataSource,
    private val checksumService: ChecksumService
) : RecipeImageRepository {
    
    override suspend fun createRecipeImage(image: RecipeImage): String {
        if (!image.isValid()) {
            throw IllegalArgumentException("RecipeImage is not valid")
        }
        
        val entity = localDataSource.toEntity(image)
        val entityId = localDataSource.insert(entity)
        return image.id
    }
    
    override suspend fun updateRecipeImage(image: RecipeImage) {
        if (!image.isValid()) {
            throw IllegalArgumentException("RecipeImage is not valid")
        }
        
        val entity = localDataSource.toEntity(image)
        localDataSource.update(entity)
    }
    
    override suspend fun deleteRecipeImage(id: String) {
        localDataSource.delete(id)
    }
    
    override suspend fun deleteRecipeImagesByRecipe(recipeId: String) {
        localDataSource.deleteByRecipe(recipeId)
    }
    
    override suspend fun getRecipeImageById(id: String): RecipeImage? {
        return localDataSource.getById(id)?.let { entity ->
            localDataSource.toDomainModel(entity)
        }
    }
    
    override suspend fun getRecipeImagesByRecipe(recipeId: String): List<RecipeImage> {
        return localDataSource.getByRecipe(recipeId).map { entity ->
            localDataSource.toDomainModel(entity)
        }
    }
    
    override suspend fun getRecipeImagesByRecipes(recipeIds: List<String>): List<RecipeImage> {
        return localDataSource.getByRecipes(recipeIds).map { entity ->
            localDataSource.toDomainModel(entity)
        }
    }
    
    override suspend fun getAllRecipeImages(): List<RecipeImage> {
        return localDataSource.getAll().map { entity ->
            localDataSource.toDomainModel(entity)
        }
    }
    
    override suspend fun getRecipeImageCountByRecipe(recipeId: String): Int {
        return localDataSource.getByRecipe(recipeId).size
    }
    
    override suspend fun validateRecipeImageChecksum(imageId: String): Boolean {
        return localDataSource.getById(imageId)?.let { entity ->
            localDataSource.validateChecksum(entity)
        } ?: false
    }
    
    override suspend fun updateRecipeImageChecksum(imageId: String): Boolean {
        return localDataSource.getById(imageId)?.let { entity ->
            val updatedEntity = localDataSource.updateChecksum(entity)
            localDataSource.update(updatedEntity)
            true
        } ?: false
    }
}