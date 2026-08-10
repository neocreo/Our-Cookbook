package com.ourcookbook.domain.model

import java.time.Instant
import java.util.UUID

/**
 * Domain model for Recipe
 * Represents a complete recipe with all metadata
 * 
 * This is the core domain model for recipes in the Cookbook application.
 * It contains all the essential information about a recipe including
 * ingredients, instructions, metadata, and synchronization information.
 */
data class Recipe(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String? = null,
    val category: String,
    val ingredients: List<Ingredient> = emptyList(),
    val instructions: List<String> = emptyList(),
    val servingSize: Int? = null,
    val prepTime: Int? = null, // in minutes
    val cookTime: Int? = null, // in minutes
    val rating: Float? = null,
    val isFavorite: Boolean = false,
    val imageUrl: String? = null,
    val notes: String? = null,
    val source: String? = null,
    val tags: List<String> = emptyList(),
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
    val versionVector: VersionVector = VersionVector(),
    val checksum: String = "",
    val deviceId: String = ""
) {
    // Validate recipe has required fields
    fun isValid(): Boolean {
        return title.isNotBlank() && 
               category.isNotBlank() &&
               ingredients.isNotEmpty() &&
               instructions.isNotEmpty()
    }
    
    // Calculate total time (prep + cook)
    val totalTime: Int? get() {
        return when {
            prepTime == null && cookTime == null -> null
            prepTime == null -> cookTime
            cookTime == null -> prepTime
            else -> prepTime + cookTime
        }
    }
    
    // Check if recipe has an image
    val hasImage: Boolean get() = imageUrl != null && imageUrl.isNotBlank()
    
    // Get all ingredient names as a comma-separated string
    val ingredientNames: String get() = ingredients.joinToString(", ") { it.name }
    
    // Get formatted instructions as a single string
    val formattedInstructions: String get() = instructions.joinToString("\n\n")
    
    // Create a copy with updated timestamp and version
    fun withUpdate(deviceId: String): Recipe {
        return this.copy(
            versionVector = versionVector.increment(deviceId),
            updatedAt = Instant.now()
        )
    }
    
    companion object {
        fun create(
            title: String,
            category: String,
            ingredients: List<Ingredient> = emptyList(),
            instructions: List<String> = emptyList(),
            description: String? = null,
            servingSize: Int? = null,
            prepTime: Int? = null,
            cookTime: Int? = null,
            deviceId: String = ""
        ): Recipe {
            return Recipe(
                title = title,
                description = description,
                category = category,
                ingredients = ingredients,
                instructions = instructions,
                servingSize = servingSize,
                prepTime = prepTime,
                cookTime = cookTime,
                deviceId = deviceId
            )
        }
    }
}