package com.ourcookbook.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.ourcookbook.data.db.Converters
import java.time.Instant

/**
 * Room Entity for Search History
 */
@Entity(
    tableName = "search_history",
    indices = [
        Index(value = ["device_id"], unique = false),
        Index(value = ["timestamp"], unique = false),
        Index(value = ["query"], unique = false)
    ]
)
@TypeConverters(Converters::class)
data class SearchHistoryEntity(
    @PrimaryKey val id: String,
    val query: String,
    val filters: Map<String, String> = emptyMap(),
    val timestamp: Instant = Instant.now(),
    val deviceId: String = ""
) {
    companion object {
        fun fromDomain(domain: com.ourcookbook.domain.model.SearchHistory): SearchHistoryEntity {
            return SearchHistoryEntity(
                id = domain.id,
                query = domain.query,
                filters = domain.filters,
                timestamp = domain.timestamp,
                deviceId = domain.deviceId
            )
        }
        
        fun toDomain(entity: SearchHistoryEntity): com.ourcookbook.domain.model.SearchHistory {
            return com.ourcookbook.domain.model.SearchHistory(
                id = entity.id,
                query = entity.query,
                filters = entity.filters,
                timestamp = entity.timestamp,
                deviceId = entity.deviceId
            )
        }
    }
}