package com.ourcookbook.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ourcookbook.data.model.SavedSearchEntity
import kotlinx.coroutines.flow.Flow

/**
 * Room DAO for Saved Search operations
 */
@Dao
interface SavedSearchDao {
    
    /**
     * Get all saved searches for a device
     */
    @Query("SELECT * FROM saved_searches WHERE deviceId = :deviceId ORDER BY updatedAt DESC")
    fun getSavedSearches(deviceId: String): Flow<List<SavedSearchEntity>>
    
    /**
     * Get a specific saved search by ID
     */
    @Query("SELECT * FROM saved_searches WHERE id = :id")
    suspend fun getSavedSearchById(id: String): SavedSearchEntity?
    
    /**
     * Save a new search
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedSearch(entity: SavedSearchEntity)
    
    /**
     * Update an existing saved search
     */
    @Update
    suspend fun updateSavedSearch(entity: SavedSearchEntity)
    
    /**
     * Remove a saved search
     */
    @Query("DELETE FROM saved_searches WHERE id = :id")
    suspend fun deleteSavedSearch(id: String)
    
    /**
     * Check if a search name already exists
     */
    @Query("SELECT EXISTS(SELECT 1 FROM saved_searches WHERE name = :name AND deviceId = :deviceId AND id != :excludeId)")
    suspend fun existsWithName(name: String, deviceId: String, excludeId: String? = null): Boolean
    
    /**
     * Get saved searches by name pattern
     */
    @Query("SELECT * FROM saved_searches WHERE deviceId = :deviceId AND name LIKE '%' || :name || '%' ORDER BY updatedAt DESC")
    suspend fun getSavedSearchesByName(deviceId: String, name: String): List<SavedSearchEntity>
}