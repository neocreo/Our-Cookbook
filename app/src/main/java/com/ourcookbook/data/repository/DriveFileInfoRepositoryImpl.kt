package com.ourcookbook.data.repository

import com.ourcookbook.data.datasource.local.IDriveFileInfoLocalDataSource
import com.ourcookbook.domain.model.DriveFileInfo
import com.ourcookbook.domain.model.DriveFileType
import com.ourcookbook.domain.repository.DriveFileInfoRepository
import com.ourcookbook.domain.service.ChecksumService
import javax.inject.Inject

/**
 * Repository implementation for DriveFileInfo operations
 */
class DriveFileInfoRepositoryImpl @Inject constructor(
    private val localDataSource: IDriveFileInfoLocalDataSource,
    private val checksumService: ChecksumService
) : DriveFileInfoRepository {
    
    override suspend fun createFileInfo(info: DriveFileInfo): String {
        if (!info.isValid()) {
            throw IllegalArgumentException("DriveFileInfo is not valid")
        }
        
        val entity = localDataSource.toEntity(info)
        val entityId = localDataSource.insert(entity)
        return info.id
    }
    
    override suspend fun updateFileInfo(info: DriveFileInfo) {
        if (!info.isValid()) {
            throw IllegalArgumentException("DriveFileInfo is not valid")
        }
        
        val entity = localDataSource.toEntity(info)
        localDataSource.update(entity)
    }
    
    override suspend fun deleteFileInfo(id: String) {
        localDataSource.delete(id)
    }
    
    override suspend fun deleteFileInfoByDriveFile(driveFileId: String) {
        localDataSource.deleteByDriveFile(driveFileId)
    }
    
    override suspend fun getFileInfoById(id: String): DriveFileInfo? {
        return localDataSource.getById(id)?.let { entity ->
            localDataSource.toDomainModel(entity)
        }
    }
    
    override suspend fun getFileInfoByDriveFile(driveFileId: String): DriveFileInfo? {
        return localDataSource.getByDriveFile(driveFileId)?.let { entity ->
            localDataSource.toDomainModel(entity)
        }
    }
    
    override suspend fun getFileInfosByType(fileType: DriveFileType): List<DriveFileInfo> {
        return localDataSource.getByType(fileType).map { entity ->
            localDataSource.toDomainModel(entity)
        }
    }
    
    override suspend fun getFileInfoByChecksum(checksum: String): DriveFileInfo? {
        return localDataSource.getByChecksum(checksum)?.let { entity ->
            localDataSource.toDomainModel(entity)
        }
    }
    
    override suspend fun getUnsyncedFileInfos(): List<DriveFileInfo> {
        return localDataSource.getUnsynced().map { entity ->
            localDataSource.toDomainModel(entity)
        }
    }
    
    override suspend fun getFileInfoCount(): Int {
        return localDataSource.getAll().size
    }
    
    override suspend fun getAllFileInfos(): List<DriveFileInfo> {
        return localDataSource.getAll().map { entity ->
            localDataSource.toDomainModel(entity)
        }
    }
    
    override suspend fun validateFileInfoChecksum(fileInfoId: String): Boolean {
        return localDataSource.getById(fileInfoId)?.let { entity ->
            localDataSource.validateChecksum(entity)
        } ?: false
    }
    
    override suspend fun updateFileInfoChecksum(fileInfoId: String): Boolean {
        return localDataSource.getById(fileInfoId)?.let { entity ->
            val updatedEntity = localDataSource.updateChecksum(entity)
            localDataSource.update(updatedEntity)
            true
        } ?: false
    }
}