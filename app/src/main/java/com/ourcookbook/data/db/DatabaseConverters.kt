package com.ourcookbook.data.db

import androidx.room.TypeConverter
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.ourcookbook.domain.model.ConflictResolution
import com.ourcookbook.domain.model.DeviceCapability
import com.ourcookbook.domain.model.DriveFileType
import com.ourcookbook.domain.model.EntityType
import com.ourcookbook.domain.model.FontSize
import com.ourcookbook.domain.model.ImageType
import com.ourcookbook.domain.model.MeasurementSystem
import com.ourcookbook.domain.model.SharingPermission
import com.ourcookbook.domain.model.SyncFrequency
import com.ourcookbook.domain.model.SyncOperation
import com.ourcookbook.domain.model.SyncStatus
import com.ourcookbook.domain.model.ThemePreference
import com.ourcookbook.domain.model.VersionVector
import java.time.Instant
import java.util.UUID

/**
 * Type converters for Room database
 * Handles conversion between complex types and database storage
 */
class DatabaseConverters {
    private val mapper = jacksonObjectMapper()

    // Instant converters
    @TypeConverter
    fun fromInstant(value: Instant?): Long? {
        return value?.toEpochMilli()
    }

    @TypeConverter
    fun toInstant(value: Long?): Instant? {
        return value?.let { Instant.ofEpochMilli(it) }
    }

    // UUID converters
    @TypeConverter
    fun fromUUID(value: UUID?): String? {
        return value?.toString()
    }

    @TypeConverter
    fun toUUID(value: String?): UUID? {
        return value?.let { UUID.fromString(it) }
    }

    // Set of strings converters
    @TypeConverter
    fun fromStringSet(value: Set<String>?): String? {
        return value?.joinToString(",")
    }

    @TypeConverter
    fun toStringSet(value: String?): Set<String>? {
        return value?.split(",")?.filter { it.isNotBlank() }?.toSet()
    }

    // VersionVector JSON converters
    @TypeConverter
    fun fromVersionVector(value: VersionVector?): String? {
        return value?.let { mapper.writeValueAsString(it) }
    }

    @TypeConverter
    fun toVersionVector(value: String?): VersionVector? {
        return value?.let { 
            try {
                mapper.readValue<VersionVector>(it)
            } catch (e: Exception) {
                null
            }
        }
    }

    // ConflictResolution JSON converters
    @TypeConverter
    fun fromConflictResolution(value: ConflictResolution?): String? {
        return value?.let { mapper.writeValueAsString(it) }
    }

    @TypeConverter
    fun toConflictResolution(value: String?): ConflictResolution? {
        return value?.let { 
            try {
                mapper.readValue<ConflictResolution>(it)
            } catch (e: Exception) {
                null
            }
        }
    }

    // List of Ingredient JSON converters
    @TypeConverter
    fun fromIngredientList(value: List<com.ourcookbook.domain.model.Ingredient>?): String? {
        return value?.let { mapper.writeValueAsString(it) }
    }

    @TypeConverter
    fun toIngredientList(value: String?): List<com.ourcookbook.domain.model.Ingredient>? {
        return value?.let { 
            try {
                mapper.readValue<List<com.ourcookbook.domain.model.Ingredient>>(it)
            } catch (e: Exception) {
                null
            }
        }
    }

    // List of String JSON converters (for instructions, tags, etc.)
    @TypeConverter
    fun fromStringListJson(value: List<String>?): String? {
        return value?.let { mapper.writeValueAsString(it) }
    }

    @TypeConverter
    fun toStringListJson(value: String?): List<String>? {
        return value?.let { 
            try {
                mapper.readValue<List<String>>(it)
            } catch (e: Exception) {
                null
            }
        }
    }

    // Set of SharingPermission JSON converters
    @TypeConverter
    fun fromSharingPermissionSetJson(value: Set<SharingPermission>?): String? {
        return value?.let { mapper.writeValueAsString(it.toList()) }
    }

    @TypeConverter
    fun toSharingPermissionSetJson(value: String?): Set<SharingPermission>? {
        return value?.let { 
            try {
                mapper.readValue<List<String>>(it).map { 
                    SharingPermission.valueOf(it)
                }.toSet()
            } catch (e: Exception) {
                null
            }
        }
    }

    // Set of DeviceCapability JSON converters
    @TypeConverter
    fun fromDeviceCapabilitySetJson(value: Set<DeviceCapability>?): String? {
        return value?.let { mapper.writeValueAsString(it.toList()) }
    }

    @TypeConverter
    fun toDeviceCapabilitySetJson(value: String?): Set<DeviceCapability>? {
        return value?.let { 
            try {
                mapper.readValue<List<String>>(it).map { 
                    DeviceCapability.valueOf(it)
                }.toSet()
            } catch (e: Exception) {
                null
            }
        }
    }
    
    // Enum converters for ThemePreference
    @TypeConverter
    fun fromThemePreference(value: ThemePreference?): String? {
        return value?.name
    }

    @TypeConverter
    fun toThemePreference(value: String?): ThemePreference? {
        return value?.let { 
            try {
                ThemePreference.valueOf(it)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }

    // Enum converters for MeasurementSystem
    @TypeConverter
    fun fromMeasurementSystem(value: MeasurementSystem?): String? {
        return value?.name
    }

    @TypeConverter
    fun toMeasurementSystem(value: String?): MeasurementSystem? {
        return value?.let { 
            try {
                MeasurementSystem.valueOf(it)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }

    // Enum converters for SyncFrequency
    @TypeConverter
    fun fromSyncFrequency(value: SyncFrequency?): String? {
        return value?.name
    }

    @TypeConverter
    fun toSyncFrequency(value: String?): SyncFrequency? {
        return value?.let { 
            try {
                SyncFrequency.valueOf(it)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }

    // Enum converters for FontSize
    @TypeConverter
    fun fromFontSize(value: FontSize?): String? {
        return value?.name
    }

    @TypeConverter
    fun toFontSize(value: String?): FontSize? {
        return value?.let { 
            try {
                FontSize.valueOf(it)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }

    // Enum converters for SyncStatus
    @TypeConverter
    fun fromSyncStatus(value: SyncStatus?): String? {
        return value?.name
    }

    @TypeConverter
    fun toSyncStatus(value: String?): SyncStatus? {
        return value?.let { 
            try {
                SyncStatus.valueOf(it)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }

    // Enum converters for ConflictStatus
    @TypeConverter
    fun fromConflictStatus(value: com.ourcookbook.domain.model.ConflictStatus?): String? {
        return value?.name
    }

    @TypeConverter
    fun toConflictStatus(value: String?): com.ourcookbook.domain.model.ConflictStatus? {
        return value?.let { 
            try {
                com.ourcookbook.domain.model.ConflictStatus.valueOf(it)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }

    // Enum converters for SyncOperation
    @TypeConverter
    fun fromSyncOperation(value: SyncOperation?): String? {
        return value?.name
    }

    @TypeConverter
    fun toSyncOperation(value: String?): SyncOperation? {
        return value?.let { 
            try {
                SyncOperation.valueOf(it)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }

    // Enum converters for EntityType
    @TypeConverter
    fun fromEntityType(value: EntityType?): String? {
        return value?.name
    }

    @TypeConverter
    fun toEntityType(value: String?): EntityType? {
        return value?.let { 
            try {
                EntityType.valueOf(it)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }

    // Enum converters for DriveFileType
    @TypeConverter
    fun fromDriveFileType(value: DriveFileType?): String? {
        return value?.name
    }

    @TypeConverter
    fun toDriveFileType(value: String?): DriveFileType? {
        return value?.let { 
            try {
                DriveFileType.valueOf(it)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }

     // Enum converters for ImageType
     @TypeConverter
     fun fromImageType(value: ImageType?): String? {
         return value?.name
     }

     @TypeConverter
     fun toImageType(value: String?): ImageType? {
         return value?.let { 
             try {
                 ImageType.valueOf(it)
             } catch (e: IllegalArgumentException) {
                 null
             }
         }
     }

    // Map converters for search filters
    @TypeConverter
    fun fromStringMap(value: Map<String, String>?): String? {
        return value?.let { mapper.writeValueAsString(it) }
    }

    @TypeConverter
    fun toStringMap(value: String?): Map<String, String>? {
        return value?.let { 
            try {
                mapper.readValue<Map<String, String>>(it)
            } catch (e: Exception) {
                null
            }
        }
    }

    // Additional conversion methods for repository implementations
    fun fromJsonToIngredients(json: String): List<com.ourcookbook.domain.model.Ingredient> {
        return json.let { 
            try {
                mapper.readValue<List<com.ourcookbook.domain.model.Ingredient>>(it)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    fun fromIngredientsToJson(ingredients: List<com.ourcookbook.domain.model.Ingredient>): String {
        return mapper.writeValueAsString(ingredients)
    }

     fun fromJsonToInstructions(json: String): List<String> {
         return json.let { 
             try {
                 mapper.readValue<List<String>>(it)
             } catch (e: Exception) {
                 emptyList()
             }
         }
     }

     fun fromInstructionsToJson(instructions: List<String>): String {
         return mapper.writeValueAsString(instructions)
     }

     fun fromJsonToTags(json: String): List<String> {
         return json.let { 
             try {
                 mapper.readValue<List<String>>(it)
             } catch (e: Exception) {
                 emptyList()
             }
         }
     }

     fun fromTagsToJson(tags: List<String>): String {
         return mapper.writeValueAsString(tags)
     }

     fun fromJsonToVersionVector(json: String): VersionVector {
         return json.let { 
             try {
                 mapper.readValue<VersionVector>(it)
             } catch (e: Exception) {
                 VersionVector()
             }
         }
     }

     fun fromVersionVectorToJson(versionVector: VersionVector): String {
         return mapper.writeValueAsString(versionVector)
     }

     fun fromJsonToConflictResolution(json: String): ConflictResolution {
         return json.let { 
             try {
                 mapper.readValue<ConflictResolution>(it)
             } catch (e: Exception) {
                 ConflictResolution.KeepLocal
             }
         }
     }

     fun fromConflictResolutionToJson(resolution: ConflictResolution): String {
         return mapper.writeValueAsString(resolution)
     }
}