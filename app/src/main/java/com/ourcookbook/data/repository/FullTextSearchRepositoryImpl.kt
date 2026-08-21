package com.ourcookbook.data.repository

import com.ourcookbook.data.db.dao.RecipeFtsDao
import com.ourcookbook.data.db.entity.RecipeFtsEntity
import com.ourcookbook.data.mapper.toDomain
import com.ourcookbook.domain.model.Recipe
import com.ourcookbook.domain.repository.FullTextSearchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Implementation of FullTextSearchRepository
 * Task 2.2.01: Full-Text Search Implementation
 */
class FullTextSearchRepositoryImpl @Inject constructor(
    private val recipeFtsDao: RecipeFtsDao
) : FullTextSearchRepository {
    
    override fun searchRecipes(query: String, deviceId: String): Flow<List<Recipe>> {
        return recipeFtsDao.search(query).map { entities ->
            entities.filter { it.deviceId == deviceId }.map { it.toDomain() }
        }
    }
    
    override fun searchByCategory(category: String, deviceId: String): Flow<List<Recipe>> {
        return recipeFtsDao.searchByCategory(category).map { entities ->
            entities.filter { it.deviceId == deviceId }.map { it.toDomain() }
        }
    }
    
    override fun searchByIngredient(ingredient: String, deviceId: String): Flow<List<Recipe>> {
        return recipeFtsDao.searchByIngredient(ingredient).map { entities ->
            entities.filter { it.deviceId == deviceId }.map { it.toDomain() }
        }
    }
    
    override fun advancedSearch(
        query: String,
        category: String?,
        ingredient: String?,
        deviceId: String
    ): Flow<List<Recipe>> {
        return recipeFtsDao.search(query).map { entities ->
            entities.filter { it.deviceId == deviceId }
                .filter { entity ->
                    category?.let { c -> entity.category.contains(c, ignoreCase = true) } ?: true
                }
                .filter { entity ->
                    ingredient?.let { i -> 
                        entity.ingredients.contains(i, ignoreCase = true)
                    } ?: true
                }
                .map { it.toDomain() }
        }
    }
}
