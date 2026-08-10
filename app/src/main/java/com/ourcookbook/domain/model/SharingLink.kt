package com.ourcookbook.domain.model

import java.time.Instant
import java.util.UUID

/**
 * Domain model for SharingLink
 * Token-based sharing with permissions
 * 
 * Represents a shareable link for a cookbook with configurable permissions,
 * expiration, and usage tracking.
 */
data class SharingLink(
    val id: String = UUID.randomUUID().toString(),
    val cookbookId: String,
    val token: String = UUID.randomUUID().toString(),
    val permissions: Set<SharingPermission> = setOf(SharingPermission.VIEW),
    val expiresAt: Instant? = null,
    val createdAt: Instant = Instant.now(),
    val usedAt: Instant? = null,
    val usedCount: Int = 0
) {
    fun isValid(): Boolean {
        return cookbookId.isNotBlank() && token.isNotBlank()
    }
    
    // Check if link has expired
    val isExpired: Boolean get() {
        return expiresAt != null && expiresAt.isBefore(Instant.now())
    }
    
    // Check if link has been used
    val isUsed: Boolean get() = usedAt != null || usedCount > 0
    
    // Check if link has a specific permission
    fun hasPermission(permission: SharingPermission): Boolean {
        return permissions.contains(permission)
    }
    
    // Check if link can be used (not expired and has permissions)
    fun canBeUsed(): Boolean {
        return !isExpired && permissions.isNotEmpty()
    }
    
    // Increment usage count
    fun withUsageIncrement(): SharingLink {
        return this.copy(
            usedCount = usedCount + 1,
            usedAt = if (usedAt == null) Instant.now() else usedAt
        )
    }
    
    companion object {
        fun create(
            cookbookId: String,
            permissions: Set<SharingPermission> = setOf(SharingPermission.VIEW),
            expiresAt: Instant? = null
        ): SharingLink {
            return SharingLink(
                cookbookId = cookbookId,
                permissions = permissions,
                expiresAt = expiresAt
            )
        }
    }
}

/**
 * Sharing permissions
 */
enum class SharingPermission {
    VIEW,        // Can view recipes
    EDIT,        // Can edit recipes
    DELETE,      // Can delete recipes
    SHARE,       // Can share with others
    ADMIN        // Full control
}