package com.ourcookbook.domain.repository

import com.ourcookbook.domain.model.DriveFileInfo
import com.ourcookbook.domain.model.DriveFileType
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for DriveFileInfo operations
 * Defines the contract for Google Drive file metadata access in the domain layer
 */
interface DriveFileInfoRepository {
    
    // CRUD Operations
    suspend fun createFileInfo(info: DriveFileInfo): String
    suspend fun updateFileInfo(info: DriveFileInfo)
    suspend fun deleteFileInfo(id: String)
    suspend fun deleteFileInfoByDriveFile(driveFileId: String)
    suspend fun getFileInfoById(id: String): DriveFileInfo?
    suspend fun getFileInfoByDriveFile(driveFileId: String): DriveFileInfo?
    
    // Query Operations
    suspend fun getFileInfosByType(fileType: DriveFileType): List<DriveFileInfo>
    suspend fun getFileInfoByChecksum(checksum: String): DriveFileInfo?
    suspend fun getUnsyncedFileInfos(): List<DriveFileInfo>
    
    // Utility Operations
    suspend fun getFileInfoCount(): Int
    suspend fun getAllFileInfos(): List<DriveFileInfo>
    
    // Checksum Operations
    suspend fun validateFileInfoChecksum(fileInfoId: String): Boolean
    suspend fun updateFileInfoChecksum(fileInfoId: String): Boolean
}