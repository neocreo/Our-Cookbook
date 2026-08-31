package com.ourcookbook.domain.usecase.sync

import com.ourcookbook.data.repository.DriveRepository
import com.ourcookbook.domain.model.AdvisoryLock
import com.ourcookbook.domain.repository.AdvisoryLockRepository
import java.time.Instant
import javax.inject.Inject

/**
 * Use case for managing advisory lock files for concurrent edit prevention
 * Task 2.2.15: Advisory lock files for concurrent edit prevention
 *
 * Prevents concurrent edits by using advisory locks stored in Drive
 */
class AdvisoryLockManager @Inject constructor(
    private val driveRepository: DriveRepository,
    private val advisoryLockRepository: AdvisoryLockRepository
) {

    /**
     * Result of lock operation
     */
    sealed class LockResult {
        data class Success(
            val lock: AdvisoryLock,
            val message: String
        ) : LockResult()
        
        data class AlreadyLocked(
            val lock: AdvisoryLock,
            val message: String
        ) : LockResult()
        
        data class Expired(
            val lock: AdvisoryLock,
            val message: String
        ) : LockResult()
        
        data class Failure(
            val errorMessage: String,
            val exception: Exception? = null
        ) : LockResult()
    }

    /**
     * Lock type for different operations
     */
    enum class LockType {
        RECIPE_EDIT,       // Editing a specific recipe
        COOKBOOK_EDIT,    // Editing a cookbook
        FULL_SYNC,        // Full sync operation
        EXPORT,           // Export operation
        IMPORT            // Import operation
    }

    /**
     * Lock scope for different levels of locking
     */
    enum class LockScope {
        GLOBAL,           // Lock everything
        COOKBOOK,        // Lock a specific cookbook
        RECIPE,          // Lock a specific recipe
        DEVICE           // Lock for a specific device
    }

    /**
     * Acquire an advisory lock
     *
     * @param resourceId ID of the resource to lock (recipe ID, cookbook ID, etc.)
     * @param lockType Type of lock
     * @param lockScope Scope of the lock
     * @param deviceId ID of the device requesting the lock
     * @param userId ID of the user requesting the lock
     * @param ttl Time-to-live in seconds (default: 300 = 5 minutes)
     * @return LockResult with operation information
     */
    suspend operator fun invoke(
        resourceId: String,
        lockType: LockType,
        lockScope: LockScope = LockScope.RECIPE,
        deviceId: String,
        userId: String,
        ttl: Long = 300
    ): LockResult {
        return try {
            // Check if already locked
            val existingLock = advisoryLockRepository.getLock(resourceId, lockScope.name)
            
            if (existingLock != null) {
                // Check if lock is expired
                if (isLockExpired(existingLock)) {
                    // Try to release the expired lock
                    val releaseResult = releaseLock(existingLock.id, deviceId, userId)
                    
                    if (releaseResult is LockResult.Success || releaseResult is LockResult.Expired) {
                        // Lock was released, try to acquire again
                        return acquireLock(resourceId, lockType, lockScope, deviceId, userId, ttl)
                    } else {
                        return LockResult.AlreadyLocked(
                            lock = existingLock,
                            message = "Resource is already locked by another device"
                        )
                    }
                } else {
                    // Lock is still valid
                    return LockResult.AlreadyLocked(
                        lock = existingLock,
                        message = "Resource is already locked by ${existingLock.deviceId}"
                    )
                }
            }
            
            // No existing lock, acquire new one
            acquireLock(resourceId, lockType, lockScope, deviceId, userId, ttl)
            
        } catch (e: Exception) {
            LockResult.Failure(
                errorMessage = "Failed to acquire lock: ${e.message}",
                exception = e
            )
        }
    }

    /**
     * Acquire a new lock
     */
    private suspend fun acquireLock(
        resourceId: String,
        lockType: LockType,
        lockScope: LockScope,
        deviceId: String,
        userId: String,
        ttl: Long
    ): LockResult {
        return try {
            val lockId = generateLockId(resourceId, lockScope, deviceId)
            val now = Instant.now()
            val expiresAt = now.plusSeconds(ttl)
            
            val lock = AdvisoryLock.create(
                id = lockId,
                resourceId = resourceId,
                lockType = lockType.name,
                lockScope = lockScope.name,
                deviceId = deviceId,
                userId = userId,
                acquiredAt = now,
                expiresAt = expiresAt,
                ttl = ttl
            )
            
            // Save lock locally
            advisoryLockRepository.createLock(lock)
            
            // Sync lock to Drive
            driveRepository.syncAdvisoryLock(lock)
            
            LockResult.Success(
                lock = lock,
                message = "Lock acquired successfully"
            )
            
        } catch (e: Exception) {
            LockResult.Failure(
                errorMessage = "Failed to create lock: ${e.message}",
                exception = e
            )
        }
    }

    /**
     * Release an advisory lock
     *
     * @param lockId ID of the lock to release
     * @param deviceId ID of the device releasing the lock
     * @param userId ID of the user releasing the lock
     * @return LockResult with operation information
     */
    suspend fun releaseLock(
        lockId: String,
        deviceId: String,
        userId: String
    ): LockResult {
        return try {
            val lock = advisoryLockRepository.getLockById(lockId)
            
            if (lock == null) {
                return LockResult.Failure("Lock not found: $lockId")
            }
            
            // Check if the lock belongs to this device/user
            if (lock.deviceId != deviceId || lock.userId != userId) {
                return LockResult.Failure("Cannot release lock owned by another device/user")
            }
            
            // Check if lock is already expired
            if (isLockExpired(lock)) {
                // Clean up expired lock
                advisoryLockRepository.deleteLock(lockId)
                driveRepository.deleteAdvisoryLock(lockId)
                
                return LockResult.Expired(
                    lock = lock,
                    message = "Lock was already expired"
                )
            }
            
            // Release the lock
            advisoryLockRepository.deleteLock(lockId)
            driveRepository.deleteAdvisoryLock(lockId)
            
            LockResult.Success(
                lock = lock,
                message = "Lock released successfully"
            )
            
        } catch (e: Exception) {
            LockResult.Failure(
                errorMessage = "Failed to release lock: ${e.message}",
                exception = e
            )
        }
    }

    /**
     * Check if a lock is expired
     */
    fun isLockExpired(lock: AdvisoryLock): Boolean {
        return lock.expiresAt.isBefore(Instant.now())
    }

    /**
     * Generate a unique lock ID
     */
    private fun generateLockId(
        resourceId: String,
        lockScope: LockScope,
        deviceId: String
    ): String {
        return "${lockScope.name}_${resourceId}_${deviceId}_${System.currentTimeMillis()}"
    }

    /**
     * Renew an existing lock
     *
     * @param lockId ID of the lock to renew
     * @param ttl New time-to-live in seconds
     * @param deviceId ID of the device renewing the lock
     * @param userId ID of the user renewing the lock
     * @return LockResult with operation information
     */
    suspend fun renewLock(
        lockId: String,
        ttl: Long,
        deviceId: String,
        userId: String
    ): LockResult {
        return try {
            val lock = advisoryLockRepository.getLockById(lockId)
            
            if (lock == null) {
                return LockResult.Failure("Lock not found: $lockId")
            }
            
            // Check if the lock belongs to this device/user
            if (lock.deviceId != deviceId || lock.userId != userId) {
                return LockResult.Failure("Cannot renew lock owned by another device/user")
            }
            
            // Check if lock is already expired
            if (isLockExpired(lock)) {
                return LockResult.Expired(
                    lock = lock,
                    message = "Cannot renew expired lock"
                )
            }
            
            // Update lock expiration
            val now = Instant.now()
            val expiresAt = now.plusSeconds(ttl)
            
            val updatedLock = lock.copy(
                acquiredAt = now,
                expiresAt = expiresAt,
                ttl = ttl
            )
            
            advisoryLockRepository.updateLock(updatedLock)
            driveRepository.syncAdvisoryLock(updatedLock)
            
            LockResult.Success(
                lock = updatedLock,
                message = "Lock renewed successfully"
            )
            
        } catch (e: Exception) {
            LockResult.Failure(
                errorMessage = "Failed to renew lock: ${e.message}",
                exception = e
            )
        }
    }

    /**
     * Check if a resource is currently locked
     *
     * @param resourceId ID of the resource to check
     * @param lockScope Scope of the lock
     * @return true if locked, false otherwise
     */
    suspend fun isLocked(
        resourceId: String,
        lockScope: LockScope = LockScope.RECIPE
    ): Boolean {
        val lock = advisoryLockRepository.getLock(resourceId, lockScope.name)
        return lock != null && !isLockExpired(lock)
    }

    /**
     * Get the current lock for a resource
     *
     * @param resourceId ID of the resource
     * @param lockScope Scope of the lock
     * @return AdvisoryLock if locked, null otherwise
     */
    suspend fun getCurrentLock(
        resourceId: String,
        lockScope: LockScope = LockScope.RECIPE
    ): AdvisoryLock? {
        val lock = advisoryLockRepository.getLock(resourceId, lockScope.name)
        return if (lock != null && !isLockExpired(lock)) lock else null
    }

    /**
     * Get all active locks
     *
     * @return List of active AdvisoryLock objects
     */
    suspend fun getAllActiveLocks(): List<AdvisoryLock> {
        val allLocks = advisoryLockRepository.getAllLocks()
        return allLocks.filter { !isLockExpired(it) }
    }

    /**
     * Get locks for a specific device
     *
     * @param deviceId ID of the device
     * @return List of AdvisoryLock objects for the device
     */
    suspend fun getLocksByDevice(deviceId: String): List<AdvisoryLock> {
        val allLocks = advisoryLockRepository.getLocksByDevice(deviceId)
        return allLocks.filter { !isLockExpired(it) }
    }

    /**
     * Get locks for a specific user
     *
     * @param userId ID of the user
     * @return List of AdvisoryLock objects for the user
     */
    suspend fun getLocksByUser(userId: String): List<AdvisoryLock> {
        val allLocks = advisoryLockRepository.getLocksByUser(userId)
        return allLocks.filter { !isLockExpired(it) }
    }

    /**
     * Force release all locks for a device (e.g., on app crash)
     *
     * @param deviceId ID of the device
     * @param userId ID of the user
     * @return Number of locks released
     */
    suspend fun forceReleaseAllLocks(
        deviceId: String,
        userId: String
    ): Int {
        val locks = getLocksByDevice(deviceId)
        var count = 0
        
        locks.forEach { lock ->
            if (lock.userId == userId) {
                advisoryLockRepository.deleteLock(lock.id)
                driveRepository.deleteAdvisoryLock(lock.id)
                count++
            }
        }
        
        return count
    }

    /**
     * Clean up expired locks
     *
     * @return Number of locks cleaned up
     */
    suspend fun cleanupExpiredLocks(): Int {
        val allLocks = advisoryLockRepository.getAllLocks()
        val expiredLocks = allLocks.filter { isLockExpired(it) }
        
        expiredLocks.forEach { lock ->
            advisoryLockRepository.deleteLock(lock.id)
            driveRepository.deleteAdvisoryLock(lock.id)
        }
        
        return expiredLocks.size
    }

    /**
     * Sync all locks with Drive
     *
     * @return Number of locks synced
     */
    suspend fun syncAllLocks(): Int {
        val allLocks = advisoryLockRepository.getAllLocks()
        var count = 0
        
        allLocks.forEach { lock ->
            try {
                driveRepository.syncAdvisoryLock(lock)
                count++
            } catch (e: Exception) {
                // Continue with other locks
            }
        }
        
        return count
    }

    /**
     * Check if the current device has any locks
     *
     * @param deviceId ID of the device
     * @param userId ID of the user
     * @return true if device has active locks
     */
    suspend fun hasActiveLocks(
        deviceId: String,
        userId: String
    ): Boolean {
        val locks = getLocksByDevice(deviceId)
        return locks.any { it.userId == userId }
    }

    /**
     * Get lock statistics
     *
     * @return Map of statistics
     */
    suspend fun getLockStatistics(): Map<String, Any> {
        val allLocks = advisoryLockRepository.getAllLocks()
        val activeLocks = allLocks.filter { !isLockExpired(it) }
        val expiredLocks = allLocks.filter { isLockExpired(it) }
        
        return mapOf(
            "total_locks" to allLocks.size,
            "active_locks" to activeLocks.size,
            "expired_locks" to expiredLocks.size,
            "devices_with_locks" to activeLocks.map { it.deviceId }.distinct().size,
            "users_with_locks" to activeLocks.map { it.userId }.distinct().size
        )
    }
}
