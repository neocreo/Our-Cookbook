package com.ourcookbook.domain.model

import java.time.Instant
import java.util.UUID

/**
 * Domain model for Cookbook
 * Represents a collection of recipes
 * 
 * Contains information about a cookbook including its name, description,
 * owner, sharing status, and the list of recipe IDs it contains.
 */
data class Cookbook(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String? = null,
    val ownerDeviceId: String,
    val isShared: Boolean = false,
    val sharingLink: String? = null,
    val imageUri: String? = null,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
    val recipeIds: List<String> = emptyList()
) {
    fun isValid(): Boolean {
        return name.isNotBlank() && ownerDeviceId.isNotBlank()
    }
    
    // Check if cookbook contains a specific recipe
    fun containsRecipe(recipeId: String): Boolean {
        return recipeIds.contains(recipeId)
    }
    
    // Get recipe count
    val recipeCount: Int get() = recipeIds.size
    
    // Check if cookbook is shareable
    val isShareable: Boolean get() = isShared && sharingLink != null
    
    // Add a recipe to the cookbook
    fun withAddedRecipe(recipeId: String): Cookbook {
        val updatedRecipeIds = if (recipeIds.contains(recipeId)) {
            recipeIds
        } else {
            recipeIds + recipeId
        }
        return this.copy(
            recipeIds = updatedRecipeIds,
            updatedAt = Instant.now()
        )
    }
    
    // Remove a recipe from the cookbook
    fun withRemovedRecipe(recipeId: String): Cookbook {
        val updatedRecipeIds = recipeIds.filter { it != recipeId }
        return this.copy(
            recipeIds = updatedRecipeIds,
            updatedAt = Instant.now()
        )
    }
    
    companion object {
        fun create(
            name: String,
            ownerDeviceId: String,
            description: String? = null,
            isShared: Boolean = false,
            sharingLink: String? = null,
            imageUri: String? = null,
            recipeIds: List<String> = emptyList()
        ): Cookbook {
            return Cookbook(
                name = name,
                description = description,
                ownerDeviceId = ownerDeviceId,
                isShared = isShared,
                sharingLink = sharingLink,
                imageUri = imageUri,
                recipeIds = recipeIds
            )
        }
    }
}