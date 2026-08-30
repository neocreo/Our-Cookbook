package com.ourcookbook.domain.model

import java.time.Instant
import java.util.UUID

/**
 * Advisory lock used to prevent concurrent edits across devices.
 *
 * Managed by [com.ourcookbook.domain.usecase.sync.AdvisoryLockManager] and
 * synced to Drive via [com.ourcookbook.data.repository.DriveRepository].
 */
data class AdvisoryLock(
    val id: String,
    val resourceId: String,
    val lockType: String,
    val lockScope: String,
    val deviceId: String,
    val userId: String,
    val acquiredAt: Instant,
    val expiresAt: Instant,
    val ttl: Long
) {
    companion object {
        @Suppress("LongParameterList")
        fun create(
            id: String = UUID.randomUUID().toString(),
            resourceId: String,
            lockType: String,
            lockScope: String,
            deviceId: String,
            userId: String,
            acquiredAt: Instant,
            expiresAt: Instant,
            ttl: Long
        ): AdvisoryLock = AdvisoryLock(
            id = id,
            resourceId = resourceId,
            lockType = lockType,
            lockScope = lockScope,
            deviceId = deviceId,
            userId = userId,
            acquiredAt = acquiredAt,
            expiresAt = expiresAt,
            ttl = ttl
        )
    }
}
