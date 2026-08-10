package com.ourcookbook.data.repository

import com.ourcookbook.data.datasource.SavedSearchLocalDataSource
import com.ourcookbook.data.model.SavedSearchEntity
import com.ourcookbook.domain.model.SavedSearch
import com.ourcookbook.domain.repository.SavedSearchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repository implementation for Saved Search
 */
class SavedSearchRepositoryImpl(
    private val localDataSource: SavedSearchLocalDataSource
) : SavedSearchRepository {
    
    override fun getSavedSearches(deviceId: String): Flow<List<SavedSearch>> {
        return localDataSource.getSavedSearches(deviceId)
            .map { entities -> entities.map { SavedSearchEntity.toDomain(it) } }
    }
    
    override suspend fun getSavedSearchById(id: String): SavedSearch? {
        return localDataSource.getSavedSearchById(id)?.let { SavedSearchEntity.toDomain(it) }
    }
    
    override suspend fun saveSearch(savedSearch: SavedSearch) {
        val entity = SavedSearchEntity.fromDomain(savedSearch)
        localDataSource.insertSavedSearch(entity)
    }
    
    override suspend fun updateSavedSearch(savedSearch: SavedSearch) {
        val entity = SavedSearchEntity.fromDomain(savedSearch)
        localDataSource.updateSavedSearch(entity)
    }
    
    override suspend fun removeSavedSearch(id: String) {
        localDataSource.deleteSavedSearch(id)
    }
    
    override suspend fun existsWithName(name: String, deviceId: String, excludeId: String?): Boolean {
        return localDataSource.existsWithName(name, deviceId, excludeId)
    }
}