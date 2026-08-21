package com.ourcookbook.domain.repository

import com.ourcookbook.domain.model.Recipe
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Full-Text Search operations
 * Task 2.2.01: Full-Text Search Implementation
 */
interface FullTextSearchRepository {
    
    /**
     * Search recipes by query using FTS5
     * @param query The search query
     * @param deviceId Filter by device ID
     * @return Flow of matching recipes
     */
    fun searchRecipes(query: String, deviceId: String): Flow<List<Recipe>>
    
    /**
     * Search recipes by category using FTS5
     * @param category The category to search
     * @param deviceId Filter by device ID
     * @return Flow of matching recipes
     */
    fun searchByCategory(category: String, deviceId: String): Flow<List<Recipe>>
    
    /**
     * Search recipes by ingredient using FTS5
     * @param ingredient The ingredient to search
     * @param deviceId Filter by device ID
     * @return Flow of matching recipes
     */
    fun searchByIngredient(ingredient: String, deviceId: String): Flow<List<Recipe>>
    
    /**
     * Advanced search with filters using FTS5
     * @param query The search query
     * @param category Optional category filter
     * @param ingredient Optional ingredient filter
     * @param deviceId Filter by device ID
     * @return Flow of matching recipes
     */
    fun advancedSearch(
        query: String,
        category: String? = null,
        ingredient: String? = null,
        deviceId: String
    ): Flow<List<Recipe>>
}
