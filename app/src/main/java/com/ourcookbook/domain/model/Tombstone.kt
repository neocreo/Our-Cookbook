package com.ourcookbook.domain.model

import java.time.Instant
import java.util.UUID

/**
 * Domain model for Tombstone
 * Deletion markers for sync conflict resolution
 * 
 * Represents a marker for deleted entities that is used for sync conflict
 * resolution. Tombstones allow the system to track deletions across devices
 * and handle conflicts appropriately.
 */
data class Tombstone(
    val id: String = UUID.randomUUID().toString(),
    val entityType: EntityType,
    val entityId: String,
    val deletedAt: Instant = Instant.now(),
    val deletedByDeviceId: String,
    val checksum: String,
    val versionVector: VersionVector
) {
    fun isValid(): Boolean {
        return entityId.isNotBlank() && 
               deletedByDeviceId.isNotBlank() &&
               checksum.isNotBlank()
    }
    
    // Check if this tombstone is for a recipe
    val isForRecipe: Boolean get() = entityType == EntityType.RECIPE
    
    // Check if this tombstone is for an ingredient
    val isForIngredient: Boolean get() = entityType == EntityType.INGREDIENT
    
    // Check if this tombstone is for a recipe image
    val isForRecipeImage: Boolean get() = entityType == EntityType.RECIPE_IMAGE
    
    // Check if this tombstone is for a cookbook
    val isForCookbook: Boolean get() = entityType == EntityType.COOKBOOK
    
    // Check if this tombstone is for a device
    val isForDevice: Boolean get() = entityType == EntityType.DEVICE
    
    // Check if this tombstone is for a sharing link
    val isForSharingLink: Boolean get() = entityType == EntityType.SHARING_LINK
    
    companion object {
        fun create(
            entityType: EntityType,
            entityId: String,
            deletedByDeviceId: String,
            checksum: String,
            versionVector: VersionVector
        ): Tombstone {
            return Tombstone(
                entityType = entityType,
                entityId = entityId,
                deletedByDeviceId = deletedByDeviceId,
                checksum = checksum,
                versionVector = versionVector
            )
        }
    }
}