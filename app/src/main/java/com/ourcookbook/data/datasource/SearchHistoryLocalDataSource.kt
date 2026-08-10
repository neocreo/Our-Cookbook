package com.ourcookbook.data.datasource

import com.ourcookbook.data.model.SearchHistoryEntity
import com.ourcookbook.domain.model.SearchHistory
import kotlinx.coroutines.flow.Flow

/**
 * Local Data Source interface for Search History
 */
interface SearchHistoryLocalDataSource {
    
    /**
     * Get all search history entries for a device
     */
    fun getSearchHistory(deviceId: String): Flow<List<SearchHistoryEntity>>
    
    /**
     * Get recent search history entries
     */
    suspend fun getRecentSearchHistory(deviceId: String, limit: Int = 10): List<SearchHistoryEntity>
    
    /**
     * Add a search query to history
     */
    suspend fun insertSearchHistory(entity: SearchHistoryEntity)
    
    /**
     * Remove a specific history entry
     */
    suspend fun deleteSearchHistory(id: String)
    
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
 * Local Data Source interface for Saved Search
 */
interface SavedSearchLocalDataSource {
    
    /**
     * Get all saved searches for a device
     */
    fun getSavedSearches(deviceId: String): Flow<List<com.ourcookbook.data.model.SavedSearchEntity>>
    
    /**
     * Get a specific saved search by ID
     */
    suspend fun getSavedSearchById(id: String): com.ourcookbook.data.model.SavedSearchEntity?
    
    /**
     * Save a new search
     */
    suspend fun insertSavedSearch(entity: com.ourcookbook.data.model.SavedSearchEntity)
    
    /**
     * Update an existing saved search
     */
    suspend fun updateSavedSearch(entity: com.ourcookbook.data.model.SavedSearchEntity)
    
    /**
     * Remove a saved search
     */
    suspend fun deleteSavedSearch(id: String)
    
    /**
     * Check if a search name already exists
     */
    suspend fun existsWithName(name: String, deviceId: String, excludeId: String? = null): Boolean
}