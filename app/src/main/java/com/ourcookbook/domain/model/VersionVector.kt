package com.ourcookbook.domain.model

import java.time.Instant

/**
 * Domain model for VersionVector
 * Tracks changes for conflict detection
 * 
 * Used to track the version history of entities for conflict detection
 * and resolution in distributed systems.
 */
data class VersionVector(
    val deviceId: String = "",
    val counter: Int = 0,
    val timestamp: Instant = Instant.now()
) {
    fun increment(deviceId: String): VersionVector {
        return copy(
            deviceId = deviceId,
            counter = counter + 1,
            timestamp = Instant.now()
        )
    }
    
    fun isNewerThan(other: VersionVector): Boolean {
        return when {
            counter > other.counter -> true
            counter < other.counter -> false
            else -> timestamp > other.timestamp
        }
    }
    
    fun isOlderThan(other: VersionVector): Boolean {
        return other.isNewerThan(this)
    }
    
    fun isSameAs(other: VersionVector): Boolean {
        return counter == other.counter && timestamp == other.timestamp
    }
    
    companion object {
        fun create(deviceId: String = ""): VersionVector {
            return VersionVector(deviceId = deviceId)
        }
    }
}

/**
 * Domain model for SyncVersionVector
 * Tracks changes across multiple devices
 * 
 * Used to track version vectors across multiple devices for distributed
 * conflict detection and resolution.
 */
data class SyncVersionVector(
    val versions: Map<String, VersionVector> = emptyMap() // deviceId -> VersionVector
) {
    fun merge(other: SyncVersionVector): SyncVersionVector {
        val merged = mutableMapOf<String, VersionVector>()
        (versions.keys + other.versions.keys).forEach { deviceId ->
            val local = versions[deviceId]
            val remote = other.versions[deviceId]
            merged[deviceId] = when {
                local == null -> remote!!
                remote == null -> local
                else -> if (local.isNewerThan(remote)) local else remote
            }
        }
        return copy(versions = merged)
    }
    
    fun getVersionForDevice(deviceId: String): VersionVector? {
        return versions[deviceId]
    }
    
    fun withUpdatedVersion(deviceId: String, versionVector: VersionVector): SyncVersionVector {
        val updated = versions.toMutableMap()
        updated[deviceId] = versionVector
        return copy(versions = updated)
    }
    
    fun withIncrementedVersion(deviceId: String): SyncVersionVector {
        val current = versions[deviceId] ?: VersionVector(deviceId = deviceId)
        return withUpdatedVersion(deviceId, current.increment(deviceId))
    }
    
    companion object {
        fun create(): SyncVersionVector {
            return SyncVersionVector()
        }
    }
}