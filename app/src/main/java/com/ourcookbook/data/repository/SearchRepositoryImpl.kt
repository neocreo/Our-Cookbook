package com.ourcookbook.data.repository

import com.ourcookbook.domain.model.SearchFilter
import com.ourcookbook.domain.repository.SearchRepository
import kotlinx.coroutines.flow.Flow

/**
 * Combined Search Repository implementation
 */
class SearchRepositoryImpl(
    private val searchHistoryRepository: SearchHistoryRepository,
    private val savedSearchRepository: SavedSearchRepository
) : SearchRepository {
    
    // Search History operations
    override fun getSearchHistory(deviceId: String): Flow<List<com.ourcookbook.domain.model.SearchHistory>> {
        return searchHistoryRepository.getSearchHistory(deviceId)
    }
    
    override suspend fun getRecentSearchHistory(deviceId: String, limit: Int): List<com.ourcookbook.domain.model.SearchHistory> {
        return searchHistoryRepository.getRecentSearchHistory(deviceId, limit)
    }
    
    override suspend fun addToHistory(searchHistory: com.ourcookbook.domain.model.SearchHistory) {
        searchHistoryRepository.addToHistory(searchHistory)
    }
    
    override suspend fun removeFromHistory(id: String) {
        searchHistoryRepository.removeFromHistory(id)
    }
    
    override suspend fun clearSearchHistory(deviceId: String) {
        searchHistoryRepository.clearSearchHistory(deviceId)
    }
    
    override suspend fun existsInHistory(query: String, deviceId: String): Boolean {
        return searchHistoryRepository.existsInHistory(query, deviceId)
    }
    
    // Saved Search operations
    override fun getSavedSearches(deviceId: String): Flow<List<com.ourcookbook.domain.model.SavedSearch>> {
        return savedSearchRepository.getSavedSearches(deviceId)
    }
    
    override suspend fun getSavedSearchById(id: String): com.ourcookbook.domain.model.SavedSearch? {
        return savedSearchRepository.getSavedSearchById(id)
    }
    
    override suspend fun saveSearch(savedSearch: com.ourcookbook.domain.model.SavedSearch) {
        savedSearchRepository.saveSearch(savedSearch)
    }
    
    override suspend fun updateSavedSearch(savedSearch: com.ourcookbook.domain.model.SavedSearch) {
        savedSearchRepository.updateSavedSearch(savedSearch)
    }
    
    override suspend fun removeSavedSearch(id: String) {
        savedSearchRepository.removeSavedSearch(id)
    }
    
    override suspend fun existsWithName(name: String, deviceId: String, excludeId: String?): Boolean {
        return savedSearchRepository.existsWithName(name, deviceId, excludeId)
    }
    
    // Combined operations
    override suspend fun getSearchSuggestions(deviceId: String, query: String, limit: Int): List<String> {
        // Get recent history that matches the query
        val history = searchHistoryRepository.getRecentSearchHistory(deviceId, limit * 2)
        val matchingHistory = history
            .filter { it.query.contains(query, ignoreCase = true) }
            .map { it.query }
            .distinct()
            .take(limit)
        
        // Get saved searches that match the query
        val savedSearches = savedSearchRepository.getSavedSearches(deviceId)
        // For now, just return history suggestions
        // In a real implementation, we'd also include popular searches, etc.
        
        return matchingHistory
    }
    
    override suspend fun getPopularFilters(deviceId: String): SearchFilter {
        // This would analyze search history to find popular filters
        // For now, return empty filter
        return SearchFilter.EMPTY
    }
}