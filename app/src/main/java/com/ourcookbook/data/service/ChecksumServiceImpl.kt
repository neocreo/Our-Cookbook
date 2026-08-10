package com.ourcookbook.data.service

import com.ourcookbook.domain.model.Ingredient
import com.ourcookbook.domain.model.Recipe
import com.ourcookbook.domain.model.VersionVector
import com.ourcookbook.domain.service.ChecksumService
import java.security.MessageDigest

/**
 * Implementation of ChecksumService using SHA-256 hashing
 * Provides data integrity verification for the sync system
 */
class ChecksumServiceImpl : ChecksumService {
    
    private companion object {
        private const val HASH_ALGORITHM = "SHA-256"
        private const val HEX_CHARS = "0123456789abcdef"
    }
    
    override fun calculateChecksum(data: String): String {
        val bytes = data.toByteArray(Charsets.UTF_8)
        val md = MessageDigest.getInstance(HASH_ALGORITHM)
        val digest = md.digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }
    }
    
    override fun calculateChecksum(data: ByteArray): String {
        val md = MessageDigest.getInstance(HASH_ALGORITHM)
        val digest = md.digest(data)
        return digest.joinToString("") { "%02x".format(it) }
    }
    
    override fun calculateChecksum(recipe: Recipe): String {
        val data = buildString {
            // Core content fields
            append(recipe.title)
            append("|")
            append(recipe.description ?: "")
            append("|")
            append(recipe.category)
            append("|")
            
            // Ingredients - sorted by ID for consistent ordering
            recipe.ingredients.sortedBy { it.id }.forEach { ingredient ->
                append(ingredient.id)
                append("|")
                append(ingredient.name)
                append("|")
                append(ingredient.amount ?: "")
                append("|")
                append(ingredient.unit ?: "")
                append("|")
                append(ingredient.notes ?: "")
                append("|")
                append(ingredient.order)
                append(";")
            }
            append("|")
            
            // Instructions
            recipe.instructions.forEach { instruction ->
                append(instruction)
                append(";")
            }
            append("|")
            
            // Metadata that affects content
            append(recipe.servingSize ?: "")
            append("|")
            append(recipe.prepTime ?: "")
            append("|")
            append(recipe.cookTime ?: "")
            append("|")
            append(recipe.rating ?: "")
            append("|")
            append(recipe.notes ?: "")
            append("|")
            append(recipe.source ?: "")
            append("|")
            
            // Tags - sorted for consistent ordering
            recipe.tags.sorted().forEach { tag ->
                append(tag)
                append(",")
            }
            append("|")
            
            // Image URL
            append(recipe.imageUrl ?: "")
            append("|")
            
            // Device ID and version vector for sync tracking
            append(recipe.deviceId)
            append("|")
            append(recipe.versionVector.deviceId)
            append("|")
            append(recipe.versionVector.counter)
            append("|")
            append(recipe.versionVector.timestamp.toString())
        }
        
        return calculateChecksum(data)
    }
    
    override fun calculateChecksum(versionVector: VersionVector): String {
        val data = "${versionVector.deviceId}|${versionVector.counter}|${versionVector.timestamp}"
        return calculateChecksum(data)
    }
    
    override fun verifyChecksum(recipe: Recipe, expectedChecksum: String): Boolean {
        val actualChecksum = calculateChecksum(recipe)
        return actualChecksum == expectedChecksum
    }
    
    override fun verifyChecksum(data: String, expectedChecksum: String): Boolean {
        val actualChecksum = calculateChecksum(data)
        return actualChecksum == expectedChecksum
    }
    
    override fun verifyChecksum(data: ByteArray, expectedChecksum: String): Boolean {
        val actualChecksum = calculateChecksum(data)
        return actualChecksum == expectedChecksum
    }
    
    override fun isValidChecksum(checksum: String): Boolean {
        return checksum.length == 64 && 
               checksum.all { c -> HEX_CHARS.contains(c) }
    }
    
    override fun calculateBatchChecksum(recipes: List<Recipe>): String {
        val data = recipes.sortedBy { it.id }.joinToString("") { calculateChecksum(it) }
        return calculateChecksum(data)
    }
    
    override fun updateRecipeChecksum(recipe: Recipe): Recipe {
        val checksum = calculateChecksum(recipe)
        return recipe.copy(checksum = checksum)
    }
    
    override fun withChecksum(recipe: Recipe): Recipe {
        return updateRecipeChecksum(recipe)
    }
    
    /**
     * Additional utility method to create a recipe with proper checksum from scratch
     * This is useful when creating new recipes that need checksums
     */
    fun createRecipeWithChecksum(
        title: String,
        category: String,
        ingredients: List<Ingredient> = emptyList(),
        instructions: List<String> = emptyList(),
        description: String? = null,
        servingSize: Int? = null,
        prepTime: Int? = null,
        cookTime: Int? = null,
        rating: Float? = null,
        isFavorite: Boolean = false,
        imageUrl: String? = null,
        notes: String? = null,
        source: String? = null,
        tags: List<String> = emptyList(),
        deviceId: String = "",
        versionVector: VersionVector = VersionVector()
    ): Recipe {
        val recipe = Recipe(
            title = title,
            description = description,
            category = category,
            ingredients = ingredients,
            instructions = instructions,
            servingSize = servingSize,
            prepTime = prepTime,
            cookTime = cookTime,
            rating = rating,
            isFavorite = isFavorite,
            imageUrl = imageUrl,
            notes = notes,
            source = source,
            tags = tags,
            deviceId = deviceId,
            versionVector = versionVector
        )
        
        return updateRecipeChecksum(recipe)
    }
    
    /**
     * Validate that a recipe's stored checksum matches its current content
     * This is useful for detecting local modifications that haven't been saved
     */
    fun validateRecipeIntegrity(recipe: Recipe): Boolean {
        if (recipe.checksum.isBlank()) {
            // No checksum set, consider it valid (new recipe)
            return true
        }
        return verifyChecksum(recipe, recipe.checksum)
    }
    
    /**
     * Check if two recipes have the same content (based on checksum)
     */
    fun haveSameContent(recipe1: Recipe, recipe2: Recipe): Boolean {
        if (recipe1.id == recipe2.id) {
            return true // Same recipe
        }
        return calculateChecksum(recipe1) == calculateChecksum(recipe2)
    }
    
    /**
     * Generate a deterministic checksum for a set of ingredients
     * Useful for ingredient-based search and matching
     */
    fun calculateIngredientSetChecksum(ingredients: List<Ingredient>): String {
        val data = ingredients.sortedBy { it.name.lowercase() }
            .joinToString(";") { "${it.name}|${it.amount}|${it.unit}" }
        return calculateChecksum(data)
    }
}
