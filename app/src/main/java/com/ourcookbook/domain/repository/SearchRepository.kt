package com.ourcookbook.domain.repository

import com.ourcookbook.domain.model.SearchFilter
import com.ourcookbook.domain.model.SearchHistory
import com.ourcookbook.domain.model.SavedSearch
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Search History operations
 */
interface SearchHistoryRepository {
    
    /**
     * Get all search history entries for a device
     */
    fun getSearchHistory(deviceId: String): Flow<List<SearchHistory>>
    
    /**
     * Get recent search history entries
     */
    suspend fun getRecentSearchHistory(deviceId: String, limit: Int = 10): List<SearchHistory>
    
    /**
     * Add a search query to history
     */
    suspend fun addToHistory(searchHistory: SearchHistory)
    
    /**
     * Remove a specific history entry
     */
    suspend fun removeFromHistory(id: String)
    
    /**
     * Clear all search history for a device
     */
    suspend fun clearSearchHistory(deviceId: String)
    
    /**
     * Check if a query already exists in history
     */
    suspend fun existsInHistory(query: String, deviceId: String): Boolean
}

/**
 * Repository interface for Saved Search operations
 */
interface SavedSearchRepository {
    
    /**
     * Get all saved searches for a device
     */
    fun getSavedSearches(deviceId: String): Flow<List<SavedSearch>>
    
    /**
     * Get a specific saved search by ID
     */
    suspend fun getSavedSearchById(id: String): SavedSearch?
    
    /**
     * Save a new search
     */
    suspend fun saveSearch(savedSearch: SavedSearch)
    
    /**
     * Update an existing saved search
     */
    suspend fun updateSavedSearch(savedSearch: SavedSearch)
    
    /**
     * Remove a saved search
     */
    suspend fun removeSavedSearch(id: String)
    
    /**
     * Check if a search name already exists
     */
    suspend fun existsWithName(name: String, deviceId: String, excludeId: String? = null): Boolean
}

/**
 * Combined Search Repository interface
 */
interface SearchRepository : SearchHistoryRepository, SavedSearchRepository {
    
    /**
     * Get search suggestions based on history and popular searches
     */
    suspend fun getSearchSuggestions(deviceId: String, query: String, limit: Int = 5): List<String>
    
    /**
     * Get popular search filters
     */
    suspend fun getPopularFilters(deviceId: String): SearchFilter
}