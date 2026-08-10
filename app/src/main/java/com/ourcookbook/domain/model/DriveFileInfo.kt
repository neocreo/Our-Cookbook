package com.ourcookbook.domain.model

import java.time.Instant
import java.util.UUID

/**
 * Domain model for DriveFileInfo
 * Google Drive file metadata
 * 
 * Contains information about files stored in Google Drive including
 * file identifiers, names, types, sizes, checksums, and timestamps.
 */
data class DriveFileInfo(
    val id: String = UUID.randomUUID().toString(),
    val driveFileId: String,
    val fileName: String,
    val fileType: DriveFileType,
    val size: Long,
    val checksum: String,
    val createdAt: Instant,
    val modifiedAt: Instant,
    val syncedAt: Instant? = null
) {
    fun isValid(): Boolean {
        return driveFileId.isNotBlank() && 
               fileName.isNotBlank() &&
               checksum.isNotBlank()
    }
    
    // Check if file has been synced
    val isSynced: Boolean get() = syncedAt != null
    
    // Get formatted file size
    val formattedSize: String get() {
        return when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> "${size / 1024} KB"
            size < 1024 * 1024 * 1024 -> "${size / (1024 * 1024)} MB"
            else -> "${size / (1024 * 1024 * 1024)} GB"
        }
    }
    
    // Check if this is a cookbook file
    val isCookbook: Boolean get() = fileType == DriveFileType.COOKBOOK
    
    // Check if this is a recipe file
    val isRecipe: Boolean get() = fileType == DriveFileType.RECIPE
    
    // Check if this is an image file
    val isImage: Boolean get() = fileType == DriveFileType.IMAGE
    
    // Mark as synced
    fun withSynced(): DriveFileInfo {
        return this.copy(syncedAt = Instant.now())
    }
    
    companion object {
        fun create(
            driveFileId: String,
            fileName: String,
            fileType: DriveFileType,
            size: Long,
            checksum: String,
            createdAt: Instant,
            modifiedAt: Instant
        ): DriveFileInfo {
            return DriveFileInfo(
                driveFileId = driveFileId,
                fileName = fileName,
                fileType = fileType,
                size = size,
                checksum = checksum,
                createdAt = createdAt,
                modifiedAt = modifiedAt
            )
        }
    }
}

/**
 * Drive file types
 */
enum class DriveFileType {
    COOKBOOK, RECIPE, IMAGE, BACKUP, OTHER
}