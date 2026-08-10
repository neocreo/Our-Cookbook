package com.ourcookbook.domain.model

import java.time.Instant
import java.util.UUID

/**
 * Domain model for RecipeImage
 * Represents an image associated with a recipe
 * 
 * Contains metadata about recipe images including the image URL/type,
 * order for display, and creation timestamp.
 */
data class RecipeImage(
    val id: String = UUID.randomUUID().toString(),
    val recipeId: String,
    val imageUrl: String,
    val imageType: ImageType = ImageType.PHOTO,
    val order: Int = 0,
    val createdAt: Instant = Instant.now()
) {
    fun isValid(): Boolean {
        return recipeId.isNotBlank() && imageUrl.isNotBlank()
    }
    
    companion object {
        fun create(
            recipeId: String,
            imageUrl: String,
            imageType: ImageType = ImageType.PHOTO,
            order: Int = 0
        ): RecipeImage {
            return RecipeImage(
                recipeId = recipeId,
                imageUrl = imageUrl,
                imageType = imageType,
                order = order
            )
        }
    }
}

/**
 * Image types for recipe images
 */
enum class ImageType {
    PHOTO,       // User uploaded photo
    OCR_SCAN,    // Image from OCR scanning
    WEB_URL,     // Image from web URL
    GENERATED    // AI generated image
}