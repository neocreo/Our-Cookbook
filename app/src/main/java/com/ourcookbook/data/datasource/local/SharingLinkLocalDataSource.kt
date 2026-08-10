package com.ourcookbook.data.datasource.local

import com.ourcookbook.data.db.dao.SharingLinkDao
import com.ourcookbook.data.db.entity.SharingLinkEntity
import com.ourcookbook.domain.model.SharingLink
import com.ourcookbook.domain.service.ChecksumService
import com.ourcookbook.data.db.DatabaseConverters
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import javax.inject.Inject

/**
 * Local data source implementation for SharingLink operations
 */
class SharingLinkLocalDataSource @Inject constructor(
    private val sharingLinkDao: SharingLinkDao,
    private val checksumService: ChecksumService,
    private val converters: DatabaseConverters
) : ISharingLinkLocalDataSource {
    
    override suspend fun insert(link: SharingLinkEntity): Long {
        return sharingLinkDao.insert(link)
    }
    
    override suspend fun update(link: SharingLinkEntity): Int {
        return sharingLinkDao.update(link)
    }
    
    override suspend fun delete(id: String): Int {
        return sharingLinkDao.delete(id)
    }
    
    override suspend fun deleteByCookbook(cookbookId: String): Int {
        return sharingLinkDao.deleteByCookbook(cookbookId)
    }
    
    override suspend fun deleteAll(): Int {
        return sharingLinkDao.deleteAll()
    }
    
    override suspend fun getById(id: String): SharingLinkEntity? {
        return sharingLinkDao.getById(id)
    }
    
    override suspend fun getByToken(token: String): SharingLinkEntity? {
        return sharingLinkDao.getByToken(token)
    }
    
    override suspend fun getByCookbook(cookbookId: String): List<SharingLinkEntity> {
        return sharingLinkDao.getByCookbook(cookbookId)
    }
    
    override suspend fun getValid(now: Instant): List<SharingLinkEntity> {
        return sharingLinkDao.getValid(now)
    }
    
    override suspend fun incrementUsage(token: String, timestamp: Instant): Int {
        return sharingLinkDao.incrementUsage(token, timestamp)
    }
    
    override suspend fun toDomainModel(entity: SharingLinkEntity): SharingLink {
        return SharingLink(
            id = entity.id,
            cookbookId = entity.cookbookId,
            token = entity.token,
            permissions = entity.permissions,
            expiresAt = entity.expiresAt,
            createdAt = entity.createdAt,
            usedAt = entity.usedAt,
            usedCount = entity.usedCount
        )
    }
    
    override suspend fun toEntity(domainModel: SharingLink): SharingLinkEntity {
        return SharingLinkEntity(
            id = domainModel.id,
            cookbookId = domainModel.cookbookId,
            token = domainModel.token,
            permissions = domainModel.permissions,
            expiresAt = domainModel.expiresAt,
            createdAt = domainModel.createdAt,
            usedAt = domainModel.usedAt,
            usedCount = domainModel.usedCount
        )
    }
    
    override suspend fun validateChecksum(entity: SharingLinkEntity): Boolean {
        val data = "${entity.id}|${entity.cookbookId}|${entity.token}|${entity.permissions}|${entity.expiresAt}|${entity.createdAt}|${entity.usedAt}|${entity.usedCount}"
        val expectedChecksum = checksumService.calculateChecksum(data)
        return true // Sharing links don't have stored checksums
    }
    
    override suspend fun updateChecksum(entity: SharingLinkEntity): SharingLinkEntity {
        return entity
    }
}

/**
 * Interface for SharingLink local data source operations
 */
interface ISharingLinkLocalDataSource {
    suspend fun insert(link: SharingLinkEntity): Long
    suspend fun update(link: SharingLinkEntity): Int
    suspend fun delete(id: String): Int
    suspend fun deleteByCookbook(cookbookId: String): Int
    suspend fun deleteAll(): Int
    suspend fun getById(id: String): SharingLinkEntity?
    suspend fun getByToken(token: String): SharingLinkEntity?
    suspend fun getByCookbook(cookbookId: String): List<SharingLinkEntity>
    suspend fun getValid(now: Instant): List<SharingLinkEntity>
    suspend fun incrementUsage(token: String, timestamp: Instant): Int
    
    // Domain model conversion
    suspend fun toDomainModel(entity: SharingLinkEntity): SharingLink
    suspend fun toEntity(domainModel: SharingLink): SharingLinkEntity
    
    // Checksum validation
    suspend fun validateChecksum(entity: SharingLinkEntity): Boolean
    suspend fun updateChecksum(entity: SharingLinkEntity): SharingLinkEntity
}