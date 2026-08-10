package com.ourcookbook.domain.model

import java.time.Instant
import java.util.UUID

/**
 * Domain model for Device
 * Represents a user device
 * 
 * Contains information about a device including its name, unique identifier,
 * capabilities, and timestamps for tracking device activity.
 */
data class Device(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val deviceId: String, // Android device ID
    val capabilities: Set<DeviceCapability> = emptySet(),
    val createdAt: Instant = Instant.now(),
    val lastSeenAt: Instant = Instant.now()
) {
    fun isValid(): Boolean {
        return name.isNotBlank() && deviceId.isNotBlank()
    }
    
    // Check if device has a specific capability
    fun hasCapability(capability: DeviceCapability): Boolean {
        return capabilities.contains(capability)
    }
    
    // Update last seen timestamp
    fun withLastSeenUpdate(): Device {
        return this.copy(lastSeenAt = Instant.now())
    }
    
    companion object {
        fun create(
            name: String,
            deviceId: String,
            capabilities: Set<DeviceCapability> = emptySet()
        ): Device {
            return Device(
                name = name,
                deviceId = deviceId,
                capabilities = capabilities
            )
        }
    }
}

/**
 * Device capabilities
 */
enum class DeviceCapability {
    CAMERA,           // Has camera for OCR
    INTERNET,         // Has internet connectivity
    BLUETOOTH,        // Has Bluetooth
    LARGE_SCREEN,     // Tablet or Chromebook
    TOUCHSCREEN,      // Has touchscreen
    KEYBOARD          // Has physical keyboard
}