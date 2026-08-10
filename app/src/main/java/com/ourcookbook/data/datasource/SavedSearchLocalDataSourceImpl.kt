package com.ourcookbook.data.datasource

import com.ourcookbook.data.db.dao.SavedSearchDao
import com.ourcookbook.data.model.SavedSearchEntity
import kotlinx.coroutines.flow.Flow

/**
 * Local Data Source implementation for Saved Search
 */
class SavedSearchLocalDataSourceImpl(
    private val savedSearchDao: SavedSearchDao
) : SavedSearchLocalDataSource {
    
    override fun getSavedSearches(deviceId: String): Flow<List<SavedSearchEntity>> {
        return savedSearchDao.getSavedSearches(deviceId)
    }
    
    override suspend fun getSavedSearchById(id: String): SavedSearchEntity? {
        return savedSearchDao.getSavedSearchById(id)
    }
    
    override suspend fun insertSavedSearch(entity: SavedSearchEntity) {
        savedSearchDao.insertSavedSearch(entity)
    }
    
    override suspend fun updateSavedSearch(entity: SavedSearchEntity) {
        savedSearchDao.updateSavedSearch(entity)
    }
    
    override suspend fun deleteSavedSearch(id: String) {
        savedSearchDao.deleteSavedSearch(id)
    }
    
    override suspend fun existsWithName(name: String, deviceId: String, excludeId: String?): Boolean {
        return savedSearchDao.existsWithName(name, deviceId, excludeId)
    }
}