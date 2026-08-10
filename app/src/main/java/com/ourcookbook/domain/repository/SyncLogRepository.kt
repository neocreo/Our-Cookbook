package com.ourcookbook.domain.repository

import com.ourcookbook.domain.model.SyncLog
import com.ourcookbook.domain.model.SyncStatus
import kotlinx.coroutines.flow.Flow
import java.time.Instant

/**
 * Repository interface for SyncLog operations
 * Defines the contract for sync log data access in the domain layer
 */
interface SyncLogRepository {
    
    // CRUD Operations
    suspend fun createLog(log: SyncLog): String
    suspend fun deleteLog(id: String)
    suspend fun deleteLogsBefore(before: Instant)
    suspend fun getLogById(id: String): SyncLog?
    
    // Query Operations
    suspend fun getLogsByDevice(deviceId: String): List<SyncLog>
    suspend fun getRecentLogs(limit: Int): List<SyncLog>
    suspend fun getLogsByStatus(status: SyncStatus): List<SyncLog>
    
    // Utility Operations
    suspend fun getLogCount(): Int
    suspend fun getAllLogs(): List<SyncLog>
    
    // Checksum Operations
    suspend fun validateLogChecksum(logId: String): Boolean
    suspend fun updateLogChecksum(logId: String): Boolean
}