package com.ourcookbook.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Fts4
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ourcookbook.domain.model.ConflictStatus
import com.ourcookbook.domain.model.DeviceCapability
import com.ourcookbook.domain.model.DriveFileType
import com.ourcookbook.domain.model.EntityType
import com.ourcookbook.domain.model.FontSize
import com.ourcookbook.domain.model.ImageType
import com.ourcookbook.domain.model.MeasurementSystem
import com.ourcookbook.domain.model.SharingPermission
import com.ourcookbook.domain.model.SyncFrequency
import com.ourcookbook.domain.model.SyncOperation
import com.ourcookbook.domain.model.SyncStatus
import com.ourcookbook.domain.model.ThemePreference
import java.time.Instant
import java.util.UUID

/**
 * Room entity for Recipe
 * Represents a recipe in the database
 */
@Entity(
    tableName = "recipes",
    indices = [
        Index(value = ["title"], unique = false),
        Index(value = ["category"], unique = false),
        Index(value = ["created_at"], unique = false),
        Index(value = ["updated_at"], unique = false),
        Index(value = ["is_favorite"], unique = false),
        Index(value = ["device_id"], unique = false),
        Index(value = ["checksum"], unique = true)
    ]
)
data class RecipeEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String? = null,
    val category: String,
    @ColumnInfo(name = "ingredients_json") val ingredientsJson: String, // JSON array of ingredients
    @ColumnInfo(name = "instructions_json") val instructionsJson: String, // JSON array of instructions
    @ColumnInfo(name = "serving_size") val servingSize: Int? = null,
    @ColumnInfo(name = "prep_time") val prepTime: Int? = null, // in minutes
    @ColumnInfo(name = "cook_time") val cookTime: Int? = null, // in minutes
    val rating: Float? = null,
    @ColumnInfo(name = "is_favorite") val isFavorite: Boolean = false,
    @ColumnInfo(name = "image_url") val imageUrl: String? = null,
    val notes: String? = null,
    val source: String? = null,
    @ColumnInfo(name = "tags_json") val tagsJson: String? = null, // JSON array of tags
    @ColumnInfo(name = "created_at") val createdAt: Instant = Instant.now(),
    @ColumnInfo(name = "updated_at") val updatedAt: Instant = Instant.now(),
    @ColumnInfo(name = "version_vector_json") val versionVectorJson: String, // JSON representation of VersionVector
    val checksum: String,
    @ColumnInfo(name = "device_id") val deviceId: String
)

/**
 * Room entity for Ingredient
 * Represents an ingredient in a recipe
 */
@Entity(
    tableName = "ingredients",
    foreignKeys = [
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["recipe_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["recipe_id"], unique = false),
        Index(value = ["name"], unique = false),
        Index(value = ["order"], unique = false)
    ]
)
data class IngredientEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "recipe_id") val recipeId: String,
    val name: String,
    val amount: String? = null,
    val unit: String? = null,
    val notes: String? = null,
    val order: Int = 0
)

/**
 * Room entity for RecipeImage
 * Represents an image associated with a recipe
 */
@Entity(
    tableName = "recipe_images",
    foreignKeys = [
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["recipe_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["recipe_id"], unique = false),
        Index(value = ["image_type"], unique = false),
        Index(value = ["order"], unique = false)
    ]
)
data class RecipeImageEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "recipe_id") val recipeId: String,
    @ColumnInfo(name = "image_url") val imageUrl: String,
    @ColumnInfo(name = "image_type") val imageType: ImageType = ImageType.PHOTO,
    val order: Int = 0,
    @ColumnInfo(name = "created_at") val createdAt: Instant = Instant.now()
)

/**
 * Room entity for Device
 * Represents a user device
 */
@Entity(
    tableName = "devices",
    indices = [
        Index(value = ["device_id"], unique = true),
        Index(value = ["name"], unique = false)
    ]
)
data class DeviceEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    @ColumnInfo(name = "device_id") val deviceId: String, // Android device ID
    val capabilities: Set<DeviceCapability> = emptySet(),
    @ColumnInfo(name = "created_at") val createdAt: Instant = Instant.now(),
    @ColumnInfo(name = "last_seen_at") val lastSeenAt: Instant = Instant.now()
)

/**
 * Room entity for DevicePreferences
 * User preferences for a specific device
 */
@Entity(
    tableName = "device_preferences",
    foreignKeys = [
        ForeignKey(
            entity = DeviceEntity::class,
            parentColumns = ["id"],
            childColumns = ["device_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["device_id"], unique = true)
    ]
)
data class DevicePreferencesEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "device_id") val deviceId: String,
    val theme: ThemePreference = ThemePreference.SYSTEM,
    @ColumnInfo(name = "measurement_system") val measurementSystem: MeasurementSystem = MeasurementSystem.IMPERIAL,
    @ColumnInfo(name = "sync_enabled") val syncEnabled: Boolean = true,
    @ColumnInfo(name = "auto_sync") val autoSync: Boolean = true,
    @ColumnInfo(name = "sync_frequency") val syncFrequency: SyncFrequency = SyncFrequency.AUTOMATIC,
    val language: String = "en",
    @ColumnInfo(name = "font_size") val fontSize: FontSize = FontSize.NORMAL
)

/**
 * Room entity for Cookbook
 * Represents a collection of recipes
 */
@Entity(
    tableName = "cookbooks",
    indices = [
        Index(value = ["name"], unique = false),
        Index(value = ["owner_device_id"], unique = false),
        Index(value = ["is_shared"], unique = false)
    ]
)
data class CookbookEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String? = null,
    @ColumnInfo(name = "owner_device_id") val ownerDeviceId: String,
    @ColumnInfo(name = "is_shared") val isShared: Boolean = false,
    @ColumnInfo(name = "sharing_link") val sharingLink: String? = null,
    @ColumnInfo(name = "created_at") val createdAt: Instant = Instant.now(),
    @ColumnInfo(name = "updated_at") val updatedAt: Instant = Instant.now(),
    @ColumnInfo(name = "recipe_ids") val recipeIds: List<String> = emptyList()
)

/**
 * Room entity for SharingLink
 * Token-based sharing with permissions
 */
@Entity(
    tableName = "sharing_links",
    foreignKeys = [
        ForeignKey(
            entity = CookbookEntity::class,
            parentColumns = ["id"],
            childColumns = ["cookbook_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["cookbook_id"], unique = false),
        Index(value = ["token"], unique = true),
        Index(value = ["expires_at"], unique = false)
    ]
)
data class SharingLinkEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "cookbook_id") val cookbookId: String,
    val token: String = UUID.randomUUID().toString(),
    val permissions: Set<SharingPermission> = emptySet(),
    @ColumnInfo(name = "expires_at") val expiresAt: Instant? = null,
    @ColumnInfo(name = "created_at") val createdAt: Instant = Instant.now(),
    @ColumnInfo(name = "used_at") val usedAt: Instant? = null,
    @ColumnInfo(name = "used_count") val usedCount: Int = 0
)

/**
 * Room entity for SyncConflict
 * Represents a conflict between local and remote data
 */
@Entity(
    tableName = "sync_conflicts",
    indices = [
        Index(value = ["local_recipe_id"], unique = false),
        Index(value = ["remote_recipe_id"], unique = false),
        Index(value = ["status"], unique = false),
        Index(value = ["detected_at"], unique = false)
    ]
)
data class SyncConflictEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "local_recipe_id") val localRecipeId: String,
    @ColumnInfo(name = "remote_recipe_id") val remoteRecipeId: String,
    @ColumnInfo(name = "local_checksum") val localChecksum: String,
    @ColumnInfo(name = "remote_checksum") val remoteChecksum: String,
    @ColumnInfo(name = "local_version_json") val localVersionJson: String, // JSON representation of VersionVector
    @ColumnInfo(name = "remote_version_json") val remoteVersionJson: String, // JSON representation of VersionVector
    @ColumnInfo(name = "detected_at") val detectedAt: Instant = Instant.now(),
    @ColumnInfo(name = "resolved_at") val resolvedAt: Instant? = null,
    val status: ConflictStatus,
    @ColumnInfo(name = "resolution_json") val resolutionJson: String? = null // JSON representation of ConflictResolution
)

/**
 * Room entity for SyncLog
 * Audit trail for sync operations
 */
@Entity(
    tableName = "sync_logs",
    indices = [
        Index(value = ["timestamp"], unique = false),
        Index(value = ["status"], unique = false),
        Index(value = ["device_id"], unique = false)
    ]
)
data class SyncLogEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val timestamp: Instant = Instant.now(),
    val status: SyncStatus,
    @ColumnInfo(name = "device_id") val deviceId: String,
    @ColumnInfo(name = "synced_items") val syncedItems: Int = 0,
    val conflicts: Int = 0,
    @ColumnInfo(name = "duration_ms") val durationMs: Long = 0,
    @ColumnInfo(name = "error_message") val errorMessage: String? = null
)

/**
 * Room entity for PendingSync
 * Queue for offline changes to be synced
 */
@Entity(
    tableName = "pending_syncs",
    indices = [
        Index(value = ["operation"], unique = false),
        Index(value = ["entity_type"], unique = false),
        Index(value = ["entity_id"], unique = false),
        Index(value = ["timestamp"], unique = false)
    ]
)
data class PendingSyncEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val operation: SyncOperation,
    @ColumnInfo(name = "entity_type") val entityType: EntityType,
    @ColumnInfo(name = "entity_id") val entityId: String,
    val data: String, // JSON representation of the entity
    val timestamp: Instant = Instant.now(),
    @ColumnInfo(name = "retry_count") val retryCount: Int = 0,
    @ColumnInfo(name = "last_error") val lastError: String? = null
)

/**
 * Room entity for SyncMetadata
 * Per-device sync state
 */
@Entity(
    tableName = "sync_metadata",
    indices = [
        Index(value = ["device_id"], unique = true)
    ]
)
data class SyncMetadataEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "device_id") val deviceId: String,
    @ColumnInfo(name = "last_sync_timestamp") val lastSyncTimestamp: Instant? = null,
    @ColumnInfo(name = "last_successful_sync") val lastSuccessfulSync: Instant? = null,
    @ColumnInfo(name = "sync_in_progress") val syncInProgress: Boolean = false,
    @ColumnInfo(name = "pending_changes") val pendingChanges: Int = 0,
    @ColumnInfo(name = "conflict_count") val conflictCount: Int = 0,
    @ColumnInfo(name = "sync_count") val syncCount: Int = 0
)

/**
 * Room entity for DriveFileInfo
 * Google Drive file metadata
 */
@Entity(
    tableName = "drive_file_infos",
    indices = [
        Index(value = ["drive_file_id"], unique = true),
        Index(value = ["file_name"], unique = false),
        Index(value = ["file_type"], unique = false)
    ]
)
data class DriveFileInfoEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "drive_file_id") val driveFileId: String,
    @ColumnInfo(name = "file_name") val fileName: String,
    @ColumnInfo(name = "file_type") val fileType: DriveFileType,
    val size: Long,
    val checksum: String,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
    @ColumnInfo(name = "modified_at") val modifiedAt: Instant,
    @ColumnInfo(name = "synced_at") val syncedAt: Instant? = null
)

/**
 * Room entity for Tombstone
 * Deletion markers for sync conflict resolution
 */
@Entity(
    tableName = "tombstones",
    indices = [
        Index(value = ["entity_type"], unique = false),
        Index(value = ["entity_id"], unique = false),
        Index(value = ["deleted_at"], unique = false),
        Index(value = ["deleted_by_device_id"], unique = false)
    ]
)
data class TombstoneEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "entity_type") val entityType: EntityType,
    @ColumnInfo(name = "entity_id") val entityId: String,
    @ColumnInfo(name = "deleted_at") val deletedAt: Instant = Instant.now(),
    @ColumnInfo(name = "deleted_by_device_id") val deletedByDeviceId: String,
    val checksum: String,
    @ColumnInfo(name = "version_vector_json") val versionVectorJson: String // JSON representation of VersionVector
)

/**
 * Room entity for FTS5 full-text search
 * Used for efficient text search across recipes
 */
@Entity(tableName = "recipes_fts")
@Fts4
data class RecipeFtsEntity(
    val id: String,
    val title: String,
    val description: String?,
    val ingredients: String,
    val instructions: String,
    val category: String,
    val deviceId: String
)
