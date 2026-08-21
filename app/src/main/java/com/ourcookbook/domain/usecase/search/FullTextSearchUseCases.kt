package com.ourcookbook.domain.usecase.search

import com.ourcookbook.domain.model.Recipe
import com.ourcookbook.domain.repository.FullTextSearchRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use cases for Full-Text Search operations
 * Task 2.2.01: Full-Text Search Implementation
 */
class FullTextSearchUseCases @Inject constructor(
    private val fullTextSearchRepository: FullTextSearchRepository
) {
    
    /**
     * Search recipes by query
     */
    fun searchRecipes(query: String, deviceId: String): Flow<List<Recipe>> {
        return fullTextSearchRepository.searchRecipes(query, deviceId)
    }
    
    /**
     * Search recipes by category
     */
    fun searchByCategory(category: String, deviceId: String): Flow<List<Recipe>> {
        return fullTextSearchRepository.searchByCategory(category, deviceId)
    }
    
    /**
     * Search recipes by ingredient
     */
    fun searchByIngredient(ingredient: String, deviceId: String): Flow<List<Recipe>> {
        return fullTextSearchRepository.searchByIngredient(ingredient, deviceId)
    }
    
    /**
     * Advanced search with multiple filters
     */
    fun advancedSearch(
        query: String,
        category: String? = null,
        ingredient: String? = null,
        deviceId: String
    ): Flow<List<Recipe>> {
        return fullTextSearchRepository.advancedSearch(query, category, ingredient, deviceId)
    }
}
