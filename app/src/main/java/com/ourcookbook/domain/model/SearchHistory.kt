package com.ourcookbook.domain.model

import java.time.Instant
import java.util.UUID

/**
 * Domain model for Search History
 * Represents a user's search query with timestamp for history tracking
 */
data class SearchHistory(
    val id: String = UUID.randomUUID().toString(),
    val query: String,
    val filters: Map<String, String> = emptyMap(),
    val timestamp: Instant = Instant.now(),
    val deviceId: String = ""
) {
    companion object {
        fun create(
            query: String,
            filters: Map<String, String> = emptyMap(),
            deviceId: String = ""
        ): SearchHistory {
            return SearchHistory(
                query = query,
                filters = filters,
                deviceId = deviceId
            )
        }
    }
}

/**
 * Domain model for Saved Search
 * Represents a user-saved search query with name and filters
 */
data class SavedSearch(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val query: String,
    val filters: Map<String, String> = emptyMap(),
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
    val deviceId: String = ""
) {
    companion object {
        fun create(
            name: String,
            query: String,
            filters: Map<String, String> = emptyMap(),
            deviceId: String = ""
        ): SavedSearch {
            return SavedSearch(
                name = name,
                query = query,
                filters = filters,
                deviceId = deviceId
            )
        }
    }
}