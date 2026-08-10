package com.ourcookbook.data.repository

import com.ourcookbook.data.datasource.local.ICookbookLocalDataSource
import com.ourcookbook.domain.model.Cookbook
import com.ourcookbook.domain.repository.CookbookRepository
import com.ourcookbook.domain.service.ChecksumService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Repository implementation for Cookbook operations
 */
class CookbookRepositoryImpl @Inject constructor(
    private val localDataSource: ICookbookLocalDataSource,
    private val checksumService: ChecksumService
) : CookbookRepository {
    
    override suspend fun createCookbook(cookbook: Cookbook): String {
        if (!cookbook.isValid()) {
            throw IllegalArgumentException("Cookbook is not valid")
        }
        
        val entity = localDataSource.toEntity(cookbook)
        val entityId = localDataSource.insert(entity)
        return cookbook.id
    }
    
    override suspend fun updateCookbook(cookbook: Cookbook) {
        if (!cookbook.isValid()) {
            throw IllegalArgumentException("Cookbook is not valid")
        }
        
        val entity = localDataSource.toEntity(cookbook)
        localDataSource.update(entity)
    }
    
    override suspend fun deleteCookbook(id: String) {
        localDataSource.delete(id)
    }
    
    override suspend fun getCookbookById(id: String): Cookbook? {
        return localDataSource.getById(id)?.let { entity ->
            localDataSource.toDomainModel(entity)
        }
    }
    
    override fun getCookbooksByOwner(deviceId: String): Flow<List<Cookbook>> {
        return localDataSource.getByOwner(deviceId).map { entities ->
            entities.map { entity -> localDataSource.toDomainModel(entity) }
        }
    }
    
    override fun getSharedCookbooks(): Flow<List<Cookbook>> {
        return localDataSource.getShared().map { entities ->
            entities.map { entity -> localDataSource.toDomainModel(entity) }
        }
    }
    
    override fun getAllCookbooks(): Flow<List<Cookbook>> {
        return localDataSource.getAll().map { entities ->
            entities.map { entity -> localDataSource.toDomainModel(entity) }
        }
    }
    
    override fun searchCookbooks(query: String): Flow<List<Cookbook>> {
        return localDataSource.search(query).map { entities ->
            entities.map { entity -> localDataSource.toDomainModel(entity) }
        }
    }
    
    override suspend fun getCookbookCount(): Int {
        return localDataSource.getAll().map { entity -> localDataSource.toDomainModel(entity) }.let { it.size }
    }
    
    override suspend fun addRecipeToCookbook(cookbookId: String, recipeId: String): Boolean {
        val cookbook = getCookbookById(cookbookId) ?: return false
        val updatedCookbook = cookbook.withAddedRecipe(recipeId)
        updateCookbook(updatedCookbook)
        return true
    }
    
    override suspend fun removeRecipeFromCookbook(cookbookId: String, recipeId: String): Boolean {
        val cookbook = getCookbookById(cookbookId) ?: return false
        val updatedCookbook = cookbook.withRemovedRecipe(recipeId)
        updateCookbook(updatedCookbook)
        return true
    }
    
    override suspend fun validateCookbookChecksum(cookbookId: String): Boolean {
        return localDataSource.getById(cookbookId)?.let { entity ->
            localDataSource.validateChecksum(entity)
        } ?: false
    }
    
    override suspend fun updateCookbookChecksum(cookbookId: String): Boolean {
        return localDataSource.getById(cookbookId)?.let { entity ->
            val updatedEntity = localDataSource.updateChecksum(entity)
            localDataSource.update(updatedEntity)
            true
        } ?: false
    }
}