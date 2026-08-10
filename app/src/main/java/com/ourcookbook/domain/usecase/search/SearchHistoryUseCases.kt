package com.ourcookbook.domain.usecase.search

import com.ourcookbook.domain.model.SearchFilter
import com.ourcookbook.domain.model.SearchHistory
import com.ourcookbook.domain.repository.SearchHistoryRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use cases for Search History operations
 */

// Get Search History Use Case
class GetSearchHistory(
    private val repository: SearchHistoryRepository
) {
    operator fun invoke(deviceId: String): Flow<List<SearchHistory>> {
        return repository.getSearchHistory(deviceId)
    }
}

// Get Recent Search History Use Case
class GetRecentSearchHistory(
    private val repository: SearchHistoryRepository
) {
    suspend operator fun invoke(deviceId: String, limit: Int = 10): List<SearchHistory> {
        return repository.getRecentSearchHistory(deviceId, limit)
    }
}

// Add To Search History Use Case
class AddToSearchHistory(
    private val repository: SearchHistoryRepository
) {
    suspend operator fun invoke(query: String, filters: Map<String, String> = emptyMap(), deviceId: String): Result<Unit> {
        return try {
            // Check if this query already exists to avoid duplicates
            val exists = repository.existsInHistory(query, deviceId)
            if (!exists) {
                val searchHistory = SearchHistory.create(
                    query = query,
                    filters = filters,
                    deviceId = deviceId
                )
                repository.addToHistory(searchHistory)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Remove From Search History Use Case
class RemoveFromSearchHistory(
    private val repository: SearchHistoryRepository
) {
    suspend operator fun invoke(id: String): Result<Unit> {
        return try {
            repository.removeFromHistory(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Clear Search History Use Case
class ClearSearchHistory(
    private val repository: SearchHistoryRepository
) {
    suspend operator fun invoke(deviceId: String): Result<Unit> {
        return try {
            repository.clearSearchHistory(deviceId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}