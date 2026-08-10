package com.ourcookbook.domain.repository

import com.ourcookbook.domain.model.Tombstone
import com.ourcookbook.domain.model.EntityType
import kotlinx.coroutines.flow.Flow
import java.time.Instant

/**
 * Repository interface for Tombstone operations
 * Defines the contract for tombstone (deletion marker) data access in the domain layer
 */
interface TombstoneRepository {
    
    // CRUD Operations
    suspend fun createTombstone(tombstone: Tombstone): String
    suspend fun deleteTombstone(id: String)
    suspend fun deleteTombstoneByEntity(entityType: EntityType, entityId: String)
    suspend fun getTombstoneById(id: String): Tombstone?
    suspend fun getTombstoneByEntity(entityType: EntityType, entityId: String): Tombstone?
    
    // Query Operations
    suspend fun getTombstonesByType(entityType: EntityType): List<Tombstone>
    suspend fun getTombstonesByDevice(deviceId: String): List<Tombstone>
    suspend fun getTombstonesSince(since: Instant): List<Tombstone>
    
    // Utility Operations
    suspend fun getTombstoneCount(): Int
    suspend fun deleteTombstonesBefore(before: Instant)
    suspend fun getAllTombstones(): List<Tombstone>
    
    // Checksum Operations
    suspend fun validateTombstoneChecksum(tombstoneId: String): Boolean
    suspend fun updateTombstoneChecksum(tombstoneId: String): Boolean
}