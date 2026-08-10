package com.ourcookbook.domain.usecase.sync

import java.time.LocalDateTime
import javax.inject.Inject

/**
 * Use case to update sync in progress status for a device
 */
class UpdateSyncInProgress @Inject constructor() {
    
    suspend operator fun invoke(deviceId: String, inProgress: Boolean): Result<Unit> {
        // In a real implementation, this would update the sync status in a SyncRepository
        return Result.success(Unit)
    }
}