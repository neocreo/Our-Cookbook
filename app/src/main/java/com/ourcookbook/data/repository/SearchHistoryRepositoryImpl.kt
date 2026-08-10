package com.ourcookbook.data.repository

import com.ourcookbook.data.datasource.SearchHistoryLocalDataSource
import com.ourcookbook.data.model.SearchHistoryEntity
import com.ourcookbook.domain.model.SearchHistory
import com.ourcookbook.domain.repository.SearchHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repository implementation for Search History
 */
class SearchHistoryRepositoryImpl(
    private val localDataSource: SearchHistoryLocalDataSource
) : SearchHistoryRepository {
    
    override fun getSearchHistory(deviceId: String): Flow<List<SearchHistory>> {
        return localDataSource.getSearchHistory(deviceId)
            .map { entities -> entities.map { SearchHistoryEntity.toDomain(it) } }
    }
    
    override suspend fun getRecentSearchHistory(deviceId: String, limit: Int): List<SearchHistory> {
        return localDataSource.getRecentSearchHistory(deviceId, limit)
            .map { SearchHistoryEntity.toDomain(it) }
    }
    
    override suspend fun addToHistory(searchHistory: SearchHistory) {
        val entity = SearchHistoryEntity.fromDomain(searchHistory)
        localDataSource.insertSearchHistory(entity)
    }
    
    override suspend fun removeFromHistory(id: String) {
        localDataSource.deleteSearchHistory(id)
    }
    
    override suspend fun clearSearchHistory(deviceId: String) {
        localDataSource.clearSearchHistory(deviceId)
    }
    
    override suspend fun existsInHistory(query: String, deviceId: String): Boolean {
        return localDataSource.existsInHistory(query, deviceId)
    }
}