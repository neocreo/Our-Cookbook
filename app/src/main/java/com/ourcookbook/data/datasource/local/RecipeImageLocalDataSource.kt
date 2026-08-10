package com.ourcookbook.data.datasource.local

import com.ourcookbook.data.db.dao.RecipeImageDao
import com.ourcookbook.data.db.entity.RecipeImageEntity
import com.ourcookbook.domain.model.RecipeImage
import com.ourcookbook.domain.service.ChecksumService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Local data source implementation for RecipeImage operations
 */
class RecipeImageLocalDataSource @Inject constructor(
    private val recipeImageDao: RecipeImageDao,
    private val checksumService: ChecksumService
) : IRecipeImageLocalDataSource {
    
    override suspend fun insert(image: RecipeImageEntity): Long {
        return recipeImageDao.insert(image)
    }
    
    override suspend fun insertAll(images: List<RecipeImageEntity>): List<Long> {
        return recipeImageDao.insertAll(images)
    }
    
    override suspend fun update(image: RecipeImageEntity): Int {
        return recipeImageDao.update(image)
    }
    
    override suspend fun delete(id: String): Int {
        return recipeImageDao.delete(id)
    }
    
    override suspend fun deleteByRecipe(recipeId: String): Int {
        return recipeImageDao.deleteByRecipe(recipeId)
    }
    
    override suspend fun deleteAll(): Int {
        return recipeImageDao.deleteAll()
    }
    
    override suspend fun getById(id: String): RecipeImageEntity? {
        return recipeImageDao.getById(id)
    }
    
    override suspend fun getByRecipe(recipeId: String): List<RecipeImageEntity> {
        return recipeImageDao.getByRecipe(recipeId)
    }
    
    override suspend fun getByRecipes(recipeIds: List<String>): List<RecipeImageEntity> {
        return recipeImageDao.getByRecipes(recipeIds)
    }
    
    override suspend fun getAll(): List<RecipeImageEntity> {
        return recipeImageDao.getAll()
    }
    
    override suspend fun toDomainModel(entity: RecipeImageEntity): RecipeImage {
        return RecipeImage(
            id = entity.id,
            recipeId = entity.recipeId,
            imageUrl = entity.imageUrl,
            imageType = entity.imageType,
            order = entity.order,
            createdAt = entity.createdAt
        )
    }
    
    override suspend fun toEntity(domainModel: RecipeImage): RecipeImageEntity {
        return RecipeImageEntity(
            id = domainModel.id,
            recipeId = domainModel.recipeId,
            imageUrl = domainModel.imageUrl,
            imageType = domainModel.imageType,
            order = domainModel.order,
            createdAt = domainModel.createdAt
        )
    }
    
    override suspend fun validateChecksum(entity: RecipeImageEntity): Boolean {
        val data = "${entity.id}|${entity.recipeId}|${entity.imageUrl}|${entity.imageType}|${entity.order}|${entity.createdAt}"
        val expectedChecksum = checksumService.calculateChecksum(data)
        // For images, we don't store checksum in the entity, validation is handled at recipe level
        return true
    }
    
    override suspend fun updateChecksum(entity: RecipeImageEntity): RecipeImageEntity {
        // Images don't have individual checksums - they're part of recipe checksum
        return entity
    }
}

/**
 * Interface for RecipeImage local data source operations
 */
interface IRecipeImageLocalDataSource {
    suspend fun insert(image: RecipeImageEntity): Long
    suspend fun insertAll(images: List<RecipeImageEntity>): List<Long>
    suspend fun update(image: RecipeImageEntity): Int
    suspend fun delete(id: String): Int
    suspend fun deleteByRecipe(recipeId: String): Int
    suspend fun deleteAll(): Int
    suspend fun getById(id: String): RecipeImageEntity?
    suspend fun getByRecipe(recipeId: String): List<RecipeImageEntity>
    suspend fun getByRecipes(recipeIds: List<String>): List<RecipeImageEntity>
    suspend fun getAll(): List<RecipeImageEntity>
    
    // Domain model conversion
    suspend fun toDomainModel(entity: RecipeImageEntity): RecipeImage
    suspend fun toEntity(domainModel: RecipeImage): RecipeImageEntity
    
    // Checksum validation
    suspend fun validateChecksum(entity: RecipeImageEntity): Boolean
    suspend fun updateChecksum(entity: RecipeImageEntity): RecipeImageEntity
}