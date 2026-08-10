package com.ourcookbook.data.repository

import com.ourcookbook.data.datasource.local.ISharingLinkLocalDataSource
import com.ourcookbook.domain.model.SharingLink
import com.ourcookbook.domain.repository.SharingLinkRepository
import com.ourcookbook.domain.service.ChecksumService
import java.time.Instant
import javax.inject.Inject

/**
 * Repository implementation for SharingLink operations
 */
class SharingLinkRepositoryImpl @Inject constructor(
    private val localDataSource: ISharingLinkLocalDataSource,
    private val checksumService: ChecksumService
) : SharingLinkRepository {
    
    override suspend fun createSharingLink(link: SharingLink): String {
        if (!link.isValid()) {
            throw IllegalArgumentException("SharingLink is not valid")
        }
        
        val entity = localDataSource.toEntity(link)
        val entityId = localDataSource.insert(entity)
        return link.id
    }
    
    override suspend fun updateSharingLink(link: SharingLink) {
        if (!link.isValid()) {
            throw IllegalArgumentException("SharingLink is not valid")
        }
        
        val entity = localDataSource.toEntity(link)
        localDataSource.update(entity)
    }
    
    override suspend fun deleteSharingLink(id: String) {
        localDataSource.delete(id)
    }
    
    override suspend fun deleteSharingLinksByCookbook(cookbookId: String) {
        localDataSource.deleteByCookbook(cookbookId)
    }
    
    override suspend fun getSharingLinkById(id: String): SharingLink? {
        return localDataSource.getById(id)?.let { entity ->
            localDataSource.toDomainModel(entity)
        }
    }
    
    override suspend fun getSharingLinkByToken(token: String): SharingLink? {
        return localDataSource.getByToken(token)?.let { entity ->
            localDataSource.toDomainModel(entity)
        }
    }
    
    override suspend fun getSharingLinksByCookbook(cookbookId: String): List<SharingLink> {
        return localDataSource.getByCookbook(cookbookId).map { entity ->
            localDataSource.toDomainModel(entity)
        }
    }
    
    override suspend fun getValidSharingLinks(now: Instant): List<SharingLink> {
        return localDataSource.getValid(now).map { entity ->
            localDataSource.toDomainModel(entity)
        }
    }
    
    override suspend fun incrementUsage(token: String, timestamp: Instant): Boolean {
        return localDataSource.incrementUsage(token, timestamp) > 0
    }
    
    override suspend fun getSharingLinkCount(): Int {
        return localDataSource.getByCookbook("").size // Simplified count
    }
    
    override suspend fun validateSharingLinkChecksum(linkId: String): Boolean {
        return localDataSource.getById(linkId)?.let { entity ->
            localDataSource.validateChecksum(entity)
        } ?: false
    }
    
    override suspend fun updateSharingLinkChecksum(linkId: String): Boolean {
        return localDataSource.getById(linkId)?.let { entity ->
            val updatedEntity = localDataSource.updateChecksum(entity)
            localDataSource.update(updatedEntity)
            true
        } ?: false
    }
}