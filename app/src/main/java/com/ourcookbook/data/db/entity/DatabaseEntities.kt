package com.ourcookbook.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
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
    val ingredientsJson: String, // JSON array of ingredients
    val instructionsJson: String, // JSON array of instructions
    val servingSize: Int? = null,
    val prepTime: Int? = null, // in minutes
    val cookTime: Int? = null, // in minutes
    val rating: Float? = null,
    val isFavorite: Boolean = false,
    val imageUrl: String? = null,
    val notes: String? = null,
    val source: String? = null,
    val tagsJson: String? = null, // JSON array of tags
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
    val versionVectorJson: String, // JSON representation of VersionVector
    val checksum: String,
    val deviceId: String
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
    val recipeId: String,
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
    val recipeId: String,
    val imageUrl: String,
    val imageType: ImageType = ImageType.PHOTO,
    val order: Int = 0,
    val createdAt: Instant = Instant.now()
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
    val deviceId: String, // Android device ID
    val capabilities: Set<DeviceCapability> = emptySet(),
    val createdAt: Instant = Instant.now(),
    val lastSeenAt: Instant = Instant.now()
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
    val deviceId: String,
    val theme: ThemePreference = ThemePreference.SYSTEM,
    val measurementSystem: MeasurementSystem = MeasurementSystem.IMPERIAL,
    val syncEnabled: Boolean = true,
    val autoSync: Boolean = true,
    val syncFrequency: SyncFrequency = SyncFrequency.AUTOMATIC,
    val language: String = "en",
    val fontSize: FontSize = FontSize.NORMAL
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
    val ownerDeviceId: String,
    val isShared: Boolean = false,
    val sharingLink: String? = null,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
    val recipeIds: List<String> = emptyList()
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
    val cookbookId: String,
    val token: String = UUID.randomUUID().toString(),
    val permissions: Set<SharingPermission> = emptySet(),
    val expiresAt: Instant? = null,
    val createdAt: Instant = Instant.now(),
    val usedAt: Instant? = null,
    val usedCount: Int = 0
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
    val localRecipeId: String,
    val remoteRecipeId: String,
    val localChecksum: String,
    val remoteChecksum: String,
    val localVersionJson: String, // JSON representation of VersionVector
    val remoteVersionJson: String, // JSON representation of VersionVector
    val detectedAt: Instant = Instant.now(),
    val resolvedAt: Instant? = null,
    val status: ConflictStatus,
    val resolutionJson: String? = null // JSON representation of ConflictResolution
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
    val deviceId: String,
    val syncedItems: Int = 0,
    val conflicts: Int = 0,
    val durationMs: Long = 0,
    val errorMessage: String? = null
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
    val entityType: EntityType,
    val entityId: String,
    val data: String, // JSON representation of the entity
    val timestamp: Instant = Instant.now(),
    val retryCount: Int = 0,
    val lastError: String? = null
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
    val deviceId: String,
    val lastSyncTimestamp: Instant? = null,
    val lastSuccessfulSync: Instant? = null,
    val syncInProgress: Boolean = false,
    val pendingChanges: Int = 0,
    val conflictCount: Int = 0
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
    val driveFileId: String,
    val fileName: String,
    val fileType: DriveFileType,
    val size: Long,
    val checksum: String,
    val createdAt: Instant,
    val modifiedAt: Instant,
    val syncedAt: Instant? = null
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
    val entityType: EntityType,
    val entityId: String,
    val deletedAt: Instant = Instant.now(),
    val deletedByDeviceId: String,
    val checksum: String,
    val versionVectorJson: String // JSON representation of VersionVector
)

/**
 * Room entity for FTS5 full-text search
 * Used for efficient text search across recipes
 */
@Entity(tableName = "recipes_fts")
data class RecipeFtsEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String?,
    val ingredients: String,
    val instructions: String,
    val category: String,
    val deviceId: String
)
