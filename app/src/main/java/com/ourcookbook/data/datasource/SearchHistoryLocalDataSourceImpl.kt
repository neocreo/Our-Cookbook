package com.ourcookbook.data.datasource

import com.ourcookbook.data.db.dao.SearchHistoryDao
import com.ourcookbook.data.model.SearchHistoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Local Data Source implementation for Search History
 */
class SearchHistoryLocalDataSourceImpl(
    private val searchHistoryDao: SearchHistoryDao
) : SearchHistoryLocalDataSource {
    
    override fun getSearchHistory(deviceId: String): Flow<List<SearchHistoryEntity>> {
        return searchHistoryDao.getSearchHistory(deviceId)
    }
    
    override suspend fun getRecentSearchHistory(deviceId: String, limit: Int): List<SearchHistoryEntity> {
        return searchHistoryDao.getRecentSearchHistory(deviceId, limit)
    }
    
    override suspend fun insertSearchHistory(entity: SearchHistoryEntity) {
        searchHistoryDao.insertSearchHistory(entity)
    }
    
    override suspend fun deleteSearchHistory(id: String) {
        searchHistoryDao.deleteSearchHistory(id)
    }
    
    override suspend fun clearSearchHistory(deviceId: String) {
        searchHistoryDao.clearSearchHistory(deviceId)
    }
    
    override suspend fun existsInHistory(query: String, deviceId: String): Boolean {
        return searchHistoryDao.existsInHistory(query, deviceId)
    }
}