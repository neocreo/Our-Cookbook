package com.ourcookbook.domain.repository

import com.ourcookbook.domain.model.AdvisoryLock

/**
 * Repository for persisting advisory locks locally.
 *
 * Consumed by [com.ourcookbook.domain.usecase.sync.AdvisoryLockManager].
 */
interface AdvisoryLockRepository {
    suspend fun getLock(resourceId: String, lockScope: String): AdvisoryLock?
    suspend fun getLockById(lockId: String): AdvisoryLock?
    suspend fun createLock(lock: AdvisoryLock): AdvisoryLock
    suspend fun updateLock(lock: AdvisoryLock): AdvisoryLock
    suspend fun deleteLock(lockId: String): Boolean
    suspend fun getAllLocks(): List<AdvisoryLock>
    suspend fun getLocksByDevice(deviceId: String): List<AdvisoryLock>
    suspend fun getLocksByUser(userId: String): List<AdvisoryLock>
}
