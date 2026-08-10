package com.ourcookbook.domain.usecase.sync

import java.time.LocalDateTime
import javax.inject.Inject

/**
 * Use case to get sync status for a device
 */
class GetSyncStatus @Inject constructor() {
    
    suspend operator fun invoke(deviceId: String): Result<String> {
        // In a real implementation, this would query a SyncRepository
        // For now, return a default result
        return Result.success("IDLE")
    }
}