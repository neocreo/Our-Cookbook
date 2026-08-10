package com.ourcookbook.domain.repository

import com.ourcookbook.domain.model.SharingLink
import kotlinx.coroutines.flow.Flow
import java.time.Instant

/**
 * Repository interface for SharingLink operations
 * Defines the contract for sharing link data access in the domain layer
 */
interface SharingLinkRepository {
    
    // CRUD Operations
    suspend fun createSharingLink(link: SharingLink): String
    suspend fun updateSharingLink(link: SharingLink)
    suspend fun deleteSharingLink(id: String)
    suspend fun deleteSharingLinksByCookbook(cookbookId: String)
    suspend fun getSharingLinkById(id: String): SharingLink?
    suspend fun getSharingLinkByToken(token: String): SharingLink?
    
    // Query Operations
    suspend fun getSharingLinksByCookbook(cookbookId: String): List<SharingLink>
    suspend fun getValidSharingLinks(now: Instant): List<SharingLink>
    
    // Utility Operations
    suspend fun incrementUsage(token: String, timestamp: Instant): Boolean
    suspend fun getSharingLinkCount(): Int
    
    // Checksum Operations
    suspend fun validateSharingLinkChecksum(linkId: String): Boolean
    suspend fun updateSharingLinkChecksum(linkId: String): Boolean
}