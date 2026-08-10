package com.ourcookbook.domain.utils

import com.ourcookbook.domain.model.Recipe
import com.ourcookbook.domain.model.SyncVersionVector
import com.ourcookbook.domain.model.VersionVector
import java.time.Instant

/**
 * Utility object for VersionVector operations
 * Provides helper methods for version tracking and conflict detection
 */
object VersionVectorUtils {
    
    /**
     * Create a new VersionVector for a device
     * 
     * @param deviceId The device identifier
     * @return A new VersionVector with counter 0 and current timestamp
     */
    fun createVersionVector(deviceId: String): VersionVector {
        return VersionVector(
            deviceId = deviceId,
            counter = 0,
            timestamp = Instant.now()
        )
    }
    
    /**
     * Increment a VersionVector for a specific device
     * 
     * @param versionVector The version vector to increment
     * @param deviceId The device ID to associate with the increment
     * @return A new VersionVector with incremented counter and updated timestamp
     */
    fun incrementVersionVector(versionVector: VersionVector, deviceId: String): VersionVector {
        return versionVector.copy(
            deviceId = deviceId,
            counter = versionVector.counter + 1,
            timestamp = Instant.now()
        )
    }
    
    /**
     * Create a SyncVersionVector from a single VersionVector
     * 
     * @param versionVector The version vector to wrap
     * @return A SyncVersionVector containing the provided version vector
     */
    fun createSyncVersionVector(versionVector: VersionVector): SyncVersionVector {
        return SyncVersionVector(
            versions = mapOf(versionVector.deviceId to versionVector)
        )
    }
    
    /**
     * Create an empty SyncVersionVector
     */
    fun createEmptySyncVersionVector(): SyncVersionVector {
        return SyncVersionVector(emptyMap())
    }
    
    /**
     * Merge two SyncVersionVectors and return the result
     * This implements the merge semantics where for each device,
     * we take the version with the highest counter (or latest timestamp if counters are equal)
     * 
     * @param local The local SyncVersionVector
     * @param remote The remote SyncVersionVector
     * @return A new SyncVersionVector representing the merge of both
     */
    fun mergeSyncVersionVectors(
        local: SyncVersionVector,
        remote: SyncVersionVector
    ): SyncVersionVector {
        return local.merge(remote)
    }
    
    /**
     * Check if two SyncVersionVectors are compatible (can be merged without conflicts)
     * 
     * @param local The local SyncVersionVector
     * @param remote The remote SyncVersionVector
     * @return true if compatible, false if there are conflicting versions
     */
    fun areCompatible(local: SyncVersionVector, remote: SyncVersionVector): Boolean {
        // For each device that appears in both, check if versions are compatible
        val commonDevices = local.versions.keys.intersect(remote.versions.keys)
        
        for (deviceId in commonDevices) {
            val localVersion = local.versions[deviceId]!!
            val remoteVersion = remote.versions[deviceId]!!
            
            // If neither is ancestor of the other, they're incompatible
            if (!isAncestor(localVersion, remoteVersion) && !isAncestor(remoteVersion, localVersion)) {
                return false
            }
        }
        
        return true
    }
    
    /**
     * Check if one VersionVector is an ancestor of another
     * A is ancestor of B if A.counter <= B.counter and A.timestamp <= B.timestamp
     * 
     * @param ancestor The potential ancestor version
     * @param descendant The potential descendant version
     * @return true if ancestor is indeed an ancestor of descendant
     */
    fun isAncestor(ancestor: VersionVector, descendant: VersionVector): Boolean {
        return ancestor.counter <= descendant.counter && 
               ancestor.timestamp <= descendant.timestamp
    }
    
    /**
     * Check if two VersionVectors represent the same version
     * 
     * @param v1 First version vector
     * @param v2 Second version vector
     * @return true if they represent the same version
     */
    fun areSameVersion(v1: VersionVector, v2: VersionVector): Boolean {
        return v1.counter == v2.counter && 
               v1.timestamp == v2.timestamp &&
               v1.deviceId == v2.deviceId
    }
    
    /**
     * Get the latest VersionVector from a SyncVersionVector for a specific device
     * 
     * @param syncVersionVector The sync version vector
     * @param deviceId The device ID to look up
     * @return The VersionVector for the device, or null if not found
     */
    fun getVersionForDevice(syncVersionVector: SyncVersionVector, deviceId: String): VersionVector? {
        return syncVersionVector.versions[deviceId]
    }
    
    /**
     * Update a SyncVersionVector with a new VersionVector for a device
     * 
     * @param syncVersionVector The sync version vector to update
     * @param deviceId The device ID
     * @param versionVector The new version vector for the device
     * @return A new SyncVersionVector with the updated version
     */
    fun updateVersionInSyncVector(
        syncVersionVector: SyncVersionVector,
        deviceId: String,
        versionVector: VersionVector
    ): SyncVersionVector {
        val updated = syncVersionVector.versions.toMutableMap()
        updated[deviceId] = versionVector
        return syncVersionVector.copy(versions = updated)
    }
    
    /**
     * Increment a specific device's version in a SyncVersionVector
     * 
     * @param syncVersionVector The sync version vector to update
     * @param deviceId The device ID to increment
     * @return A new SyncVersionVector with the incremented version
     */
    fun incrementVersionInSyncVector(
        syncVersionVector: SyncVersionVector,
        deviceId: String
    ): SyncVersionVector {
        val current = syncVersionVector.versions[deviceId] ?: VersionVector(deviceId = deviceId)
        val incremented = current.increment(deviceId)
        return updateVersionInSyncVector(syncVersionVector, deviceId, incremented)
    }
    
    /**
     * Check if a recipe's version is newer than another recipe's version
     * 
     * @param recipe1 First recipe
     * @param recipe2 Second recipe
     * @return true if recipe1's version is newer than recipe2's version
     */
    fun isNewerVersion(recipe1: Recipe, recipe2: Recipe): Boolean {
        return recipe1.versionVector.isNewerThan(recipe2.versionVector)
    }
    
    /**
     * Check if a recipe's version is older than another recipe's version
     * 
     * @param recipe1 First recipe
     * @param recipe2 Second recipe
     * @return true if recipe1's version is older than recipe2's version
     */
    fun isOlderVersion(recipe1: Recipe, recipe2: Recipe): Boolean {
        return recipe1.versionVector.isOlderThan(recipe2.versionVector)
    }
    
    /**
     * Check if two recipes have the same version
     * 
     * @param recipe1 First recipe
     * @param recipe2 Second recipe
     * @return true if both recipes have the same version
     */
    fun haveSameVersion(recipe1: Recipe, recipe2: Recipe): Boolean {
        return recipe1.versionVector.isSameAs(recipe2.versionVector)
    }
    
    /**
     * Get all device IDs from a SyncVersionVector
     * 
     * @param syncVersionVector The sync version vector
     * @return Set of all device IDs in the sync version vector
     */
    fun getAllDeviceIds(syncVersionVector: SyncVersionVector): Set<String> {
        return syncVersionVector.versions.keys
    }
    
    /**
     * Check if a SyncVersionVector contains a specific device
     * 
     * @param syncVersionVector The sync version vector
     * @param deviceId The device ID to check for
     * @return true if the device is present in the sync version vector
     */
    fun containsDevice(syncVersionVector: SyncVersionVector, deviceId: String): Boolean {
        return syncVersionVector.versions.containsKey(deviceId)
    }
    
    /**
     * Get the maximum counter value across all devices in a SyncVersionVector
     * 
     * @param syncVersionVector The sync version vector
     * @return The maximum counter value, or 0 if empty
     */
    fun getMaxCounter(syncVersionVector: SyncVersionVector): Int {
        return syncVersionVector.versions.values.maxOfOrNull { it.counter } ?: 0
    }
    
    /**
     * Get the most recent timestamp across all devices in a SyncVersionVector
     * 
     * @param syncVersionVector The sync version vector
     * @return The most recent timestamp, or null if empty
     */
    fun getLatestTimestamp(syncVersionVector: SyncVersionVector): Instant? {
        return syncVersionVector.versions.values.maxOfOrNull { it.timestamp }
    }
    
    /**
     * Create a VersionVector with a specific counter and timestamp
     * Useful for testing and specific version creation
     * 
     * @param deviceId The device identifier
     * @param counter The counter value
     * @param timestamp The timestamp
     * @return A VersionVector with the specified values
     */
    fun createVersionVector(deviceId: String, counter: Int, timestamp: Instant): VersionVector {
        return VersionVector(
            deviceId = deviceId,
            counter = counter,
            timestamp = timestamp
        )
    }
    
    /**
     * Check if a SyncVersionVector is empty (contains no device versions)
     * 
     * @param syncVersionVector The sync version vector to check
     * @return true if empty, false otherwise
     */
    fun isEmpty(syncVersionVector: SyncVersionVector): Boolean {
        return syncVersionVector.versions.isEmpty()
    }
    
    /**
     * Get the size (number of devices) in a SyncVersionVector
     * 
     * @param syncVersionVector The sync version vector
     * @return The number of devices in the sync version vector
     */
    fun size(syncVersionVector: SyncVersionVector): Int {
        return syncVersionVector.versions.size
    }
}
