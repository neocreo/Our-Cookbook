package com.ourcookbook.domain.usecase.search

import com.ourcookbook.domain.model.SavedSearch
import com.ourcookbook.domain.repository.SavedSearchRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use cases for Saved Search operations
 */

// Get Saved Searches Use Case
class GetSavedSearches(
    private val repository: SavedSearchRepository
) {
    operator fun invoke(deviceId: String): Flow<List<SavedSearch>> {
        return repository.getSavedSearches(deviceId)
    }
}

// Get Saved Search By ID Use Case
class GetSavedSearchById(
    private val repository: SavedSearchRepository
) {
    suspend operator fun invoke(id: String): Result<SavedSearch?> {
        return try {
            val savedSearch = repository.getSavedSearchById(id)
            Result.success(savedSearch)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Save Search Use Case
class SaveSearch(
    private val repository: SavedSearchRepository
) {
    suspend operator fun invoke(name: String, query: String, filters: Map<String, String>, deviceId: String): Result<String> {
        return try {
            // Check if name already exists
            val exists = repository.existsWithName(name, deviceId)
            if (exists) {
                return Result.failure(IllegalArgumentException("A saved search with this name already exists"))
            }
            
            val savedSearch = SavedSearch.create(
                name = name,
                query = query,
                filters = filters,
                deviceId = deviceId
            )
            repository.saveSearch(savedSearch)
            Result.success(savedSearch.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Update Saved Search Use Case
class UpdateSavedSearch(
    private val repository: SavedSearchRepository
) {
    suspend operator fun invoke(id: String, name: String, query: String, filters: Map<String, String>): Result<Unit> {
        return try {
            // Check if name already exists (excluding current search)
            val exists = repository.existsWithName(name, "", id)
            if (exists) {
                return Result.failure(IllegalArgumentException("A saved search with this name already exists"))
            }
            
            val savedSearch = SavedSearch(
                id = id,
                name = name,
                query = query,
                filters = filters
            )
            repository.updateSavedSearch(savedSearch)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Remove Saved Search Use Case
class RemoveSavedSearch(
    private val repository: SavedSearchRepository
) {
    suspend operator fun invoke(id: String): Result<Unit> {
        return try {
            repository.removeSavedSearch(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}