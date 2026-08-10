package com.ourcookbook.data.datasource.local

import com.ourcookbook.data.db.dao.DriveFileInfoDao
import com.ourcookbook.data.db.entity.DriveFileInfoEntity
import com.ourcookbook.domain.model.DriveFileInfo
import com.ourcookbook.domain.model.DriveFileType
import com.ourcookbook.domain.service.ChecksumService
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import javax.inject.Inject

/**
 * Local data source implementation for DriveFileInfo operations
 */
class DriveFileInfoLocalDataSource @Inject constructor(
    private val driveFileInfoDao: DriveFileInfoDao,
    private val checksumService: ChecksumService
) : IDriveFileInfoLocalDataSource {
    
    override suspend fun insert(info: DriveFileInfoEntity): Long {
        return driveFileInfoDao.insert(info)
    }
    
    override suspend fun update(info: DriveFileInfoEntity): Int {
        return driveFileInfoDao.update(info)
    }
    
    override suspend fun delete(id: String): Int {
        return driveFileInfoDao.delete(id)
    }
    
    override suspend fun deleteByDriveFile(driveFileId: String): Int {
        return driveFileInfoDao.deleteByDriveFile(driveFileId)
    }
    
    override suspend fun deleteAll(): Int {
        return driveFileInfoDao.deleteAll()
    }
    
    override suspend fun getById(id: String): DriveFileInfoEntity? {
        return driveFileInfoDao.getById(id)
    }
    
    override suspend fun getByDriveFile(driveFileId: String): DriveFileInfoEntity? {
        return driveFileInfoDao.getByDriveFile(driveFileId)
    }
    
    override suspend fun getByType(fileType: DriveFileType): List<DriveFileInfoEntity> {
        return driveFileInfoDao.getByType(fileType)
    }
    
    override suspend fun getByChecksum(checksum: String): DriveFileInfoEntity? {
        return driveFileInfoDao.getByChecksum(checksum)
    }
    
    override suspend fun getUnsynced(): List<DriveFileInfoEntity> {
        return driveFileInfoDao.getUnsynced()
    }
    
    override suspend fun toDomainModel(entity: DriveFileInfoEntity): DriveFileInfo {
        return DriveFileInfo(
            id = entity.id,
            driveFileId = entity.driveFileId,
            fileName = entity.fileName,
            fileType = entity.fileType,
            size = entity.size,
            checksum = entity.checksum,
            createdAt = entity.createdAt,
            modifiedAt = entity.modifiedAt,
            syncedAt = entity.syncedAt
        )
    }
    
    override suspend fun toEntity(domainModel: DriveFileInfo): DriveFileInfoEntity {
        return DriveFileInfoEntity(
            id = domainModel.id,
            driveFileId = domainModel.driveFileId,
            fileName = domainModel.fileName,
            fileType = domainModel.fileType,
            size = domainModel.size,
            checksum = domainModel.checksum,
            createdAt = domainModel.createdAt,
            modifiedAt = domainModel.modifiedAt,
            syncedAt = domainModel.syncedAt
        )
    }
    
    override suspend fun validateChecksum(entity: DriveFileInfoEntity): Boolean {
        // DriveFileInfo has its own checksum field
        val data = "${entity.id}|${entity.driveFileId}|${entity.fileName}|${entity.fileType}|${entity.size}|${entity.createdAt}|${entity.modifiedAt}|${entity.syncedAt}"
        val expectedChecksum = checksumService.calculateChecksum(data)
        return entity.checksum == expectedChecksum
    }
    
    override suspend fun updateChecksum(entity: DriveFileInfoEntity): DriveFileInfoEntity {
        val data = "${entity.id}|${entity.driveFileId}|${entity.fileName}|${entity.fileType}|${entity.size}|${entity.createdAt}|${entity.modifiedAt}|${entity.syncedAt}"
        val newChecksum = checksumService.calculateChecksum(data)
        return entity.copy(checksum = newChecksum)
    }
}

/**
 * Interface for DriveFileInfo local data source operations
 */
interface IDriveFileInfoLocalDataSource {
    suspend fun insert(info: DriveFileInfoEntity): Long
    suspend fun update(info: DriveFileInfoEntity): Int
    suspend fun delete(id: String): Int
    suspend fun deleteByDriveFile(driveFileId: String): Int
    suspend fun deleteAll(): Int
    suspend fun getById(id: String): DriveFileInfoEntity?
    suspend fun getByDriveFile(driveFileId: String): DriveFileInfoEntity?
    suspend fun getByType(fileType: DriveFileType): List<DriveFileInfoEntity>
    suspend fun getByChecksum(checksum: String): DriveFileInfoEntity?
    suspend fun getUnsynced(): List<DriveFileInfoEntity>
    
    // Domain model conversion
    suspend fun toDomainModel(entity: DriveFileInfoEntity): DriveFileInfo
    suspend fun toEntity(domainModel: DriveFileInfo): DriveFileInfoEntity
    
    // Checksum validation
    suspend fun validateChecksum(entity: DriveFileInfoEntity): Boolean
    suspend fun updateChecksum(entity: DriveFileInfoEntity): DriveFileInfoEntity
}