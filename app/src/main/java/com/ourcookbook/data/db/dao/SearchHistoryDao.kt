package com.ourcookbook.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.ourcookbook.data.model.SearchHistoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Room DAO for Search History operations
 */
@Dao
interface SearchHistoryDao {
    
    /**
     * Get all search history entries for a device
     */
    @Query("SELECT * FROM search_history WHERE deviceId = :deviceId ORDER BY timestamp DESC")
    fun getSearchHistory(deviceId: String): Flow<List<SearchHistoryEntity>>
    
    /**
     * Get recent search history entries
     */
    @Query("SELECT * FROM search_history WHERE deviceId = :deviceId ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentSearchHistory(deviceId: String, limit: Int = 10): List<SearchHistoryEntity>
    
    /**
     * Add a search query to history
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearchHistory(entity: SearchHistoryEntity)
    
    /**
     * Remove a specific history entry
     */
    @Query("DELETE FROM search_history WHERE id = :id")
    suspend fun deleteSearchHistory(id: String)
    
    /**
     * Clear all search history for a device
     */
    @Query("DELETE FROM search_history WHERE deviceId = :deviceId")
    suspend fun clearSearchHistory(deviceId: String)
    
    /**
     * Check if a query already exists in history
     */
    @Query("SELECT EXISTS(SELECT 1 FROM search_history WHERE query = :query AND deviceId = :deviceId)")
    suspend fun existsInHistory(query: String, deviceId: String): Boolean
    
    /**
     * Get search history entries containing a specific query
     */
    @Query("SELECT * FROM search_history WHERE deviceId = :deviceId AND query LIKE '%' || :query || '%' ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getSearchHistoryByQuery(deviceId: String, query: String, limit: Int = 5): List<SearchHistoryEntity>
}