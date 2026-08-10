package com.ourcookbook.domain.repository

import com.ourcookbook.domain.model.SyncConflict
import com.ourcookbook.domain.model.ConflictStatus
import kotlinx.coroutines.flow.Flow
import java.time.Instant

/**
 * Repository interface for SyncConflict operations
 * Defines the contract for sync conflict data access in the domain layer
 */
interface SyncConflictRepository {
    
    // CRUD Operations
    suspend fun createConflict(conflict: SyncConflict): String
    suspend fun updateConflict(conflict: SyncConflict)
    suspend fun deleteConflict(id: String)
    suspend fun deleteConflictsByStatus(status: ConflictStatus)
    suspend fun getConflictById(id: String): SyncConflict?
    
    // Query Operations
    fun getConflictsByStatus(status: ConflictStatus): Flow<List<SyncConflict>>
    suspend fun getConflictsByRecipe(recipeId: String): List<SyncConflict>
    suspend fun getConflictsSince(since: Instant): List<SyncConflict>
    
    // Utility Operations
    suspend fun getPendingConflictCount(): Int
    suspend fun getAllConflicts(): List<SyncConflict>
    
    // Checksum Operations
    suspend fun validateConflictChecksum(conflictId: String): Boolean
    suspend fun updateConflictChecksum(conflictId: String): Boolean
}