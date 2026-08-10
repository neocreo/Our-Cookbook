package com.ourcookbook.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.ourcookbook.data.db.entity.*
import com.ourcookbook.domain.model.ConflictStatus
import com.ourcookbook.domain.model.DriveFileType
import com.ourcookbook.domain.model.EntityType
import com.ourcookbook.domain.model.SyncOperation
import com.ourcookbook.domain.model.SyncStatus
import kotlinx.coroutines.flow.Flow
import java.time.Instant

/**
 * DAO for Recipe operations
 */
@Dao
interface RecipeDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recipe: RecipeEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(recipes: List<RecipeEntity>): List<Long>
    
    @Update
    suspend fun update(recipe: RecipeEntity): Int
    
    @Update
    suspend fun updateAll(recipes: List<RecipeEntity>): Int
    
    @Query("DELETE FROM recipes WHERE id = :id")
    suspend fun delete(id: String): Int
    
    @Query("DELETE FROM recipes WHERE id IN (:ids)")
    suspend fun deleteAll(ids: List<String>): Int
    
    @Query("DELETE FROM recipes")
    suspend fun deleteAll(): Int
    
    @Query("SELECT * FROM recipes WHERE id = :id")
    suspend fun getById(id: String): RecipeEntity?
    
    @Query("SELECT * FROM recipes WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<String>): List<RecipeEntity>
    
    @Query("SELECT * FROM recipes ORDER BY updated_at DESC")
    fun getAll(): Flow<List<RecipeEntity>>
    
    @Query("SELECT * FROM recipes ORDER BY updated_at DESC")
    suspend fun getAllOnce(): List<RecipeEntity>
    
    @Query("SELECT * FROM recipes WHERE is_favorite = 1 ORDER BY updated_at DESC")
    fun getFavorites(): Flow<List<RecipeEntity>>
    
    @Query("SELECT * FROM recipes WHERE category = :category ORDER BY updated_at DESC")
    fun getByCategory(category: String): Flow<List<RecipeEntity>>
    
    @Query("SELECT * FROM recipes WHERE device_id = :deviceId ORDER BY updated_at DESC")
    fun getByDevice(deviceId: String): Flow<List<RecipeEntity>>
    
    @Query("SELECT * FROM recipes WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' ORDER BY updated_at DESC")
    fun search(query: String): Flow<List<RecipeEntity>>
    
    @Query("SELECT * FROM recipes WHERE checksum = :checksum")
    suspend fun getByChecksum(checksum: String): RecipeEntity?
    
    @Query("SELECT * FROM recipes WHERE updated_at > :since ORDER BY updated_at DESC")
    suspend fun getUpdatedSince(since: Instant): List<RecipeEntity>
    
    @Query("SELECT COUNT(*) FROM recipes")
    suspend fun count(): Int
    
    @Query("SELECT * FROM recipes ORDER BY created_at DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<RecipeEntity>
    
    @Query("SELECT * FROM recipes WHERE rating IS NOT NULL ORDER BY rating DESC LIMIT :limit")
    suspend fun getTopRated(limit: Int): List<RecipeEntity>
}

/**
 * DAO for Ingredient operations
 */
@Dao
interface IngredientDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ingredient: IngredientEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(ingredients: List<IngredientEntity>): List<Long>
    
    @Update
    suspend fun update(ingredient: IngredientEntity): Int
    
    @Update
    suspend fun updateAll(ingredients: List<IngredientEntity>): Int
    
    @Query("DELETE FROM ingredients WHERE id = :id")
    suspend fun delete(id: String): Int
    
    @Query("DELETE FROM ingredients WHERE recipe_id = :recipeId")
    suspend fun deleteByRecipe(recipeId: String): Int
    
    @Query("DELETE FROM ingredients")
    suspend fun deleteAll(): Int
    
    @Query("SELECT * FROM ingredients WHERE id = :id")
    suspend fun getById(id: String): IngredientEntity?
    
    @Query("SELECT * FROM ingredients WHERE recipe_id = :recipeId ORDER BY `order`")
    suspend fun getByRecipe(recipeId: String): List<IngredientEntity>
    
    @Query("SELECT * FROM ingredients WHERE recipe_id IN (:recipeIds) ORDER BY `order`")
    suspend fun getByRecipes(recipeIds: List<String>): List<IngredientEntity>
    
    @Query("SELECT * FROM ingredients WHERE name LIKE '%' || :query || '%'")
    suspend fun search(query: String): List<IngredientEntity>
    
    @Query("SELECT COUNT(*) FROM ingredients WHERE recipe_id = :recipeId")
    suspend fun countByRecipe(recipeId: String): Int
}

/**
 * DAO for RecipeImage operations
 */
@Dao
interface RecipeImageDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(image: RecipeImageEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(images: List<RecipeImageEntity>): List<Long>
    
    @Update
    suspend fun update(image: RecipeImageEntity): Int
    
    @Query("DELETE FROM recipe_images WHERE id = :id")
    suspend fun delete(id: String): Int
    
    @Query("DELETE FROM recipe_images WHERE recipe_id = :recipeId")
    suspend fun deleteByRecipe(recipeId: String): Int
    
    @Query("DELETE FROM recipe_images")
    suspend fun deleteAll(): Int
    
    @Query("SELECT * FROM recipe_images WHERE id = :id")
    suspend fun getById(id: String): RecipeImageEntity?
    
    @Query("SELECT * FROM recipe_images WHERE recipe_id = :recipeId ORDER BY `order`")
    suspend fun getByRecipe(recipeId: String): List<RecipeImageEntity>
    
    @Query("SELECT * FROM recipe_images WHERE recipe_id IN (:recipeIds) ORDER BY `order`")
    suspend fun getByRecipes(recipeIds: List<String>): List<RecipeImageEntity>
    
    @Query("SELECT * FROM recipe_images ORDER BY created_at DESC")
    suspend fun getAll(): List<RecipeImageEntity>
}

/**
 * DAO for Device operations
 */
@Dao
interface DeviceDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(device: DeviceEntity): Long
    
    @Update
    suspend fun update(device: DeviceEntity): Int
    
    @Query("DELETE FROM devices WHERE id = :id")
    suspend fun delete(id: String): Int
    
    @Query("DELETE FROM devices")
    suspend fun deleteAll(): Int
    
    @Query("SELECT * FROM devices WHERE id = :id")
    suspend fun getById(id: String): DeviceEntity?
    
    @Query("SELECT * FROM devices WHERE device_id = :deviceId")
    suspend fun getByDeviceId(deviceId: String): DeviceEntity?
    
    @Query("SELECT * FROM devices ORDER BY last_seen_at DESC")
    fun getAll(): Flow<List<DeviceEntity>>
    
    @Query("SELECT * FROM devices ORDER BY last_seen_at DESC")
    suspend fun getAllOnce(): List<DeviceEntity>
    
    @Query("SELECT * FROM devices WHERE last_seen_at > :since ORDER BY last_seen_at DESC")
    suspend fun getActiveSince(since: Instant): List<DeviceEntity>
    
    @Query("UPDATE devices SET last_seen_at = :timestamp WHERE device_id = :deviceId")
    suspend fun updateLastSeen(deviceId: String, timestamp: Instant): Int
}

/**
 * DAO for DevicePreferences operations
 */
@Dao
interface DevicePreferencesDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(preferences: DevicePreferencesEntity): Long
    
    @Update
    suspend fun update(preferences: DevicePreferencesEntity): Int
    
    @Query("DELETE FROM device_preferences WHERE id = :id")
    suspend fun delete(id: String): Int
    
    @Query("DELETE FROM device_preferences WHERE device_id = :deviceId")
    suspend fun deleteByDevice(deviceId: String): Int
    
    @Query("DELETE FROM device_preferences")
    suspend fun deleteAll(): Int
    
    @Query("SELECT * FROM device_preferences WHERE id = :id")
    suspend fun getById(id: String): DevicePreferencesEntity?
    
    @Query("SELECT * FROM device_preferences WHERE device_id = :deviceId")
    suspend fun getByDevice(deviceId: String): DevicePreferencesEntity?
    
    @Query("SELECT * FROM device_preferences")
    suspend fun getAll(): List<DevicePreferencesEntity>
}

/**
 * DAO for Cookbook operations
 */
@Dao
interface CookbookDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cookbook: CookbookEntity): Long
    
    @Update
    suspend fun update(cookbook: CookbookEntity): Int
    
    @Query("DELETE FROM cookbooks WHERE id = :id")
    suspend fun delete(id: String): Int
    
    @Query("DELETE FROM cookbooks")
    suspend fun deleteAll(): Int
    
    @Query("SELECT * FROM cookbooks WHERE id = :id")
    suspend fun getById(id: String): CookbookEntity?
    
    @Query("SELECT * FROM cookbooks WHERE owner_device_id = :deviceId ORDER BY updated_at DESC")
    fun getByOwner(deviceId: String): Flow<List<CookbookEntity>>
    
    @Query("SELECT * FROM cookbooks WHERE is_shared = 1 ORDER BY updated_at DESC")
    fun getShared(): Flow<List<CookbookEntity>>
    
    @Query("SELECT * FROM cookbooks ORDER BY updated_at DESC")
    fun getAll(): Flow<List<CookbookEntity>>
    
    @Query("SELECT * FROM cookbooks WHERE name LIKE '%' || :query || '%' ORDER BY updated_at DESC")
    fun search(query: String): Flow<List<CookbookEntity>>
}

/**
 * DAO for SharingLink operations
 */
@Dao
interface SharingLinkDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(link: SharingLinkEntity): Long
    
    @Update
    suspend fun update(link: SharingLinkEntity): Int
    
    @Query("DELETE FROM sharing_links WHERE id = :id")
    suspend fun delete(id: String): Int
    
    @Query("DELETE FROM sharing_links WHERE cookbook_id = :cookbookId")
    suspend fun deleteByCookbook(cookbookId: String): Int
    
    @Query("DELETE FROM sharing_links")
    suspend fun deleteAll(): Int
    
    @Query("SELECT * FROM sharing_links WHERE id = :id")
    suspend fun getById(id: String): SharingLinkEntity?
    
    @Query("SELECT * FROM sharing_links WHERE token = :token")
    suspend fun getByToken(token: String): SharingLinkEntity?
    
    @Query("SELECT * FROM sharing_links WHERE cookbook_id = :cookbookId")
    suspend fun getByCookbook(cookbookId: String): List<SharingLinkEntity>
    
    @Query("SELECT * FROM sharing_links WHERE expires_at IS NULL OR expires_at > :now")
    suspend fun getValid(now: Instant): List<SharingLinkEntity>
    
    @Query("UPDATE sharing_links SET used_count = used_count + 1, used_at = :timestamp WHERE token = :token")
    suspend fun incrementUsage(token: String, timestamp: Instant): Int
}

/**
 * DAO for SyncConflict operations
 */
@Dao
interface SyncConflictDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(conflict: SyncConflictEntity): Long
    
    @Update
    suspend fun update(conflict: SyncConflictEntity): Int
    
    @Query("DELETE FROM sync_conflicts WHERE id = :id")
    suspend fun delete(id: String): Int
    
    @Query("DELETE FROM sync_conflicts WHERE status = :status")
    suspend fun deleteByStatus(status: ConflictStatus): Int
    
    @Query("DELETE FROM sync_conflicts")
    suspend fun deleteAll(): Int
    
    @Query("SELECT * FROM sync_conflicts WHERE id = :id")
    suspend fun getById(id: String): SyncConflictEntity?
    
    @Query("SELECT * FROM sync_conflicts WHERE status = :status ORDER BY detected_at")
    fun getByStatus(status: ConflictStatus): Flow<List<SyncConflictEntity>>
    
    @Query("SELECT * FROM sync_conflicts WHERE local_recipe_id = :recipeId OR remote_recipe_id = :recipeId")
    suspend fun getByRecipe(recipeId: String): List<SyncConflictEntity>
    
    @Query("SELECT * FROM sync_conflicts WHERE detected_at > :since")
    suspend fun getSince(since: Instant): List<SyncConflictEntity>
    
    @Query("SELECT COUNT(*) FROM sync_conflicts WHERE status = 'PENDING'")
    suspend fun countPending(): Int
}

/**
 * DAO for SyncLog operations
 */
@Dao
interface SyncLogDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: SyncLogEntity): Long
    
    @Query("DELETE FROM sync_logs WHERE id = :id")
    suspend fun delete(id: String): Int
    
    @Query("DELETE FROM sync_logs WHERE timestamp < :before")
    suspend fun deleteBefore(before: Instant): Int
    
    @Query("DELETE FROM sync_logs")
    suspend fun deleteAll(): Int
    
    @Query("SELECT * FROM sync_logs WHERE id = :id")
    suspend fun getById(id: String): SyncLogEntity?
    
    @Query("SELECT * FROM sync_logs WHERE device_id = :deviceId ORDER BY timestamp DESC")
    suspend fun getByDevice(deviceId: String): List<SyncLogEntity>
    
    @Query("SELECT * FROM sync_logs ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<SyncLogEntity>
    
    @Query("SELECT * FROM sync_logs WHERE status = :status ORDER BY timestamp DESC")
    suspend fun getByStatus(status: SyncStatus): List<SyncLogEntity>
}

/**
 * DAO for PendingSync operations
 */
@Dao
interface PendingSyncDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pending: PendingSyncEntity): Long
    
    @Update
    suspend fun update(pending: PendingSyncEntity): Int
    
    @Query("DELETE FROM pending_syncs WHERE id = :id")
    suspend fun delete(id: String): Int
    
    @Query("DELETE FROM pending_syncs WHERE entity_id = :entityId AND entity_type = :entityType")
    suspend fun deleteByEntity(entityId: String, entityType: String): Int
    
    @Query("DELETE FROM pending_syncs WHERE timestamp < :before")
    suspend fun deleteBefore(before: Instant): Int
    
    @Query("DELETE FROM pending_syncs")
    suspend fun deleteAll(): Int
    
    @Query("SELECT * FROM pending_syncs WHERE id = :id")
    suspend fun getById(id: String): PendingSyncEntity?
    
    @Query("SELECT * FROM pending_syncs WHERE entity_type = :entityType ORDER BY timestamp")
    suspend fun getByType(entityType: EntityType): List<PendingSyncEntity>

    @Query("SELECT * FROM pending_syncs WHERE entity_id = :entityId AND entity_type = :entityType ORDER BY timestamp")
    suspend fun getByEntity(entityId: String, entityType: EntityType): List<PendingSyncEntity>
    
    @Query("SELECT * FROM pending_syncs ORDER BY timestamp")
    suspend fun getAll(): List<PendingSyncEntity>
    
    @Query("SELECT * FROM pending_syncs WHERE retry_count < 3 ORDER BY timestamp")
    suspend fun getRetryable(): List<PendingSyncEntity>
    
    @Query("SELECT COUNT(*) FROM pending_syncs")
    suspend fun count(): Int
}

/**
 * DAO for SyncMetadata operations
 */
@Dao
interface SyncMetadataDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(metadata: SyncMetadataEntity): Long
    
    @Update
    suspend fun update(metadata: SyncMetadataEntity): Int
    
    @Query("DELETE FROM sync_metadata WHERE id = :id")
    suspend fun delete(id: String): Int
    
    @Query("DELETE FROM sync_metadata WHERE device_id = :deviceId")
    suspend fun deleteByDevice(deviceId: String): Int
    
    @Query("DELETE FROM sync_metadata")
    suspend fun deleteAll(): Int
    
    @Query("SELECT * FROM sync_metadata WHERE id = :id")
    suspend fun getById(id: String): SyncMetadataEntity?
    
    @Query("SELECT * FROM sync_metadata WHERE device_id = :deviceId")
    suspend fun getByDevice(deviceId: String): SyncMetadataEntity?
    
    @Query("SELECT * FROM sync_metadata")
    suspend fun getAll(): List<SyncMetadataEntity>
    
    @Query("UPDATE sync_metadata SET last_sync_timestamp = :timestamp, sync_in_progress = 0 WHERE device_id = :deviceId")
    suspend fun updateLastSync(deviceId: String, timestamp: Instant): Int
    
    @Query("UPDATE sync_metadata SET sync_in_progress = :inProgress WHERE device_id = :deviceId")
    suspend fun updateSyncInProgress(deviceId: String, inProgress: Boolean): Int
}

/**
 * DAO for DriveFileInfo operations
 */
@Dao
interface DriveFileInfoDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(info: DriveFileInfoEntity): Long
    
    @Update
    suspend fun update(info: DriveFileInfoEntity): Int
    
    @Query("DELETE FROM drive_file_infos WHERE id = :id")
    suspend fun delete(id: String): Int
    
    @Query("DELETE FROM drive_file_infos WHERE drive_file_id = :driveFileId")
    suspend fun deleteByDriveFile(driveFileId: String): Int
    
    @Query("DELETE FROM drive_file_infos")
    suspend fun deleteAll(): Int
    
    @Query("SELECT * FROM drive_file_infos WHERE id = :id")
    suspend fun getById(id: String): DriveFileInfoEntity?
    
    @Query("SELECT * FROM drive_file_infos WHERE drive_file_id = :driveFileId")
    suspend fun getByDriveFile(driveFileId: String): DriveFileInfoEntity?
    
    @Query("SELECT * FROM drive_file_infos WHERE file_type = :fileType ORDER BY modified_at DESC")
    suspend fun getByType(fileType: DriveFileType): List<DriveFileInfoEntity>
    
    @Query("SELECT * FROM drive_file_infos WHERE checksum = :checksum")
    suspend fun getByChecksum(checksum: String): DriveFileInfoEntity?
    
    @Query("SELECT * FROM drive_file_infos WHERE synced_at IS NULL OR synced_at < modified_at")
    suspend fun getUnsynced(): List<DriveFileInfoEntity>
}

/**
 * DAO for Tombstone operations
 */
@Dao
interface TombstoneDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tombstone: TombstoneEntity): Long
    
    @Query("DELETE FROM tombstones WHERE id = :id")
    suspend fun delete(id: String): Int
    
    @Query("DELETE FROM tombstones WHERE entity_type = :entityType AND entity_id = :entityId")
    suspend fun deleteByEntity(entityType: EntityType, entityId: String): Int
    
    @Query("DELETE FROM tombstones WHERE deleted_at < :before")
    suspend fun deleteBefore(before: Instant): Int
    
    @Query("DELETE FROM tombstones")
    suspend fun deleteAll(): Int
    
    @Query("SELECT * FROM tombstones WHERE id = :id")
    suspend fun getById(id: String): TombstoneEntity?
    
    @Query("SELECT * FROM tombstones WHERE entity_type = :entityType AND entity_id = :entityId")
    suspend fun getByEntity(entityType: EntityType, entityId: String): TombstoneEntity?

    @Query("SELECT * FROM tombstones WHERE entity_type = :entityType")
    suspend fun getByType(entityType: EntityType): List<TombstoneEntity>
    
    @Query("SELECT * FROM tombstones WHERE deleted_by_device_id = :deviceId")
    suspend fun getByDevice(deviceId: String): List<TombstoneEntity>
    
    @Query("SELECT * FROM tombstones WHERE deleted_at > :since")
    suspend fun getSince(since: Instant): List<TombstoneEntity>
}

/**
 * DAO for FTS5 operations
 */
@Dao
interface RecipeFtsDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(fts: RecipeFtsEntity): Long
    
    @Query("DELETE FROM recipes_fts WHERE id = :id")
    suspend fun delete(id: String): Int
    
    @Query("DELETE FROM recipes_fts")
    suspend fun deleteAll(): Int
    
    @Query("SELECT * FROM recipes_fts WHERE id = :id")
    suspend fun getById(id: String): RecipeFtsEntity?
    
    @Query("SELECT * FROM recipes_fts WHERE title MATCH :query OR description MATCH :query OR ingredients MATCH :query OR instructions MATCH :query OR category MATCH :query ORDER BY rank")
    suspend fun search(query: String): List<RecipeFtsEntity>
    
    @Query("SELECT * FROM recipes_fts WHERE category = :category ORDER BY rank")
    suspend fun searchByCategory(category: String): List<RecipeFtsEntity>
    
    @Query("SELECT * FROM recipes_fts WHERE ingredients MATCH :ingredient ORDER BY rank")
    suspend fun searchByIngredient(ingredient: String): List<RecipeFtsEntity>
}
