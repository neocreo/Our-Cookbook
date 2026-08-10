package com.ourcookbook.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.ourcookbook.data.db.Converters
import java.time.Instant

/**
 * Room Entity for Saved Search
 */
@Entity(
    tableName = "saved_searches",
    indices = [
        Index(value = ["device_id"], unique = false),
        Index(value = ["name"], unique = false),
        Index(value = ["created_at"], unique = false)
    ]
)
@TypeConverters(Converters::class)
data class SavedSearchEntity(
    @PrimaryKey val id: String,
    val name: String,
    val query: String,
    val filters: Map<String, String> = emptyMap(),
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
    val deviceId: String = ""
) {
    companion object {
        fun fromDomain(domain: com.ourcookbook.domain.model.SavedSearch): SavedSearchEntity {
            return SavedSearchEntity(
                id = domain.id,
                name = domain.name,
                query = domain.query,
                filters = domain.filters,
                createdAt = domain.createdAt,
                updatedAt = domain.updatedAt,
                deviceId = domain.deviceId
            )
        }
        
        fun toDomain(entity: SavedSearchEntity): com.ourcookbook.domain.model.SavedSearch {
            return com.ourcookbook.domain.model.SavedSearch(
                id = entity.id,
                name = entity.name,
                query = entity.query,
                filters = entity.filters,
                createdAt = entity.createdAt,
                updatedAt = entity.updatedAt,
                deviceId = entity.deviceId
            )
        }
    }
}