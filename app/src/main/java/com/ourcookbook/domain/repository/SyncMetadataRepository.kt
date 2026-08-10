package com.ourcookbook.domain.repository

import com.ourcookbook.domain.model.SyncMetadata
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for SyncMetadata operations
 * Defines the contract for sync metadata data access in the domain layer
 */
interface SyncMetadataRepository {
    
    // CRUD Operations
    suspend fun createMetadata(metadata: SyncMetadata): String
    suspend fun updateMetadata(metadata: SyncMetadata)
    suspend fun deleteMetadata(id: String)
    suspend fun deleteMetadataByDevice(deviceId: String)
    suspend fun getMetadataById(id: String): SyncMetadata?
    suspend fun getMetadataByDevice(deviceId: String): SyncMetadata?
    
    // Query Operations
    suspend fun getAllMetadata(): List<SyncMetadata>
    
    // Utility Operations
    suspend fun updateLastSyncTimestamp(deviceId: String, timestamp: java.time.Instant): Boolean
    suspend fun updateSyncInProgress(deviceId: String, inProgress: Boolean): Boolean
    
    // Checksum Operations
    suspend fun validateMetadataChecksum(metadataId: String): Boolean
    suspend fun updateMetadataChecksum(metadataId: String): Boolean
}