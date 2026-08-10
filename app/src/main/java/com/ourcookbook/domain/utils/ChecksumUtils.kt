package com.ourcookbook.domain.utils

import com.ourcookbook.domain.model.Ingredient
import com.ourcookbook.domain.model.Recipe
import com.ourcookbook.domain.model.VersionVector
import java.security.MessageDigest

/**
 * Utility object for checksum calculations and validations
 * Provides SHA-256 hashing functionality for data integrity verification
 */
object ChecksumUtils {
    
    private const val HASH_ALGORITHM = "SHA-256"
    private const val HEX_CHARS = "0123456789abcdef"
    
    /**
     * Calculate SHA-256 checksum for a string
     * 
     * @param data The string data to hash
     * @return Hexadecimal string representation of the SHA-256 hash
     */
    fun calculateChecksum(data: String): String {
        val bytes = data.toByteArray(Charsets.UTF_8)
        val md = MessageDigest.getInstance(HASH_ALGORITHM)
        val digest = md.digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }
    }
    
    /**
     * Calculate SHA-256 checksum for a byte array
     * 
     * @param data The byte array to hash
     * @return Hexadecimal string representation of the SHA-256 hash
     */
    fun calculateChecksum(data: ByteArray): String {
        val md = MessageDigest.getInstance(HASH_ALGORITHM)
        val digest = md.digest(data)
        return digest.joinToString("") { "%02x".format(it) }
    }
    
    /**
     * Calculate checksum for a Recipe object
     * Includes all relevant fields that affect the recipe content
     * 
     * @param recipe The recipe to calculate checksum for
     * @return SHA-256 checksum of the recipe content
     */
    fun calculateRecipeChecksum(recipe: Recipe): String {
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
    
    /**
     * Calculate checksum for a VersionVector
     * 
     * @param versionVector The version vector to hash
     * @return SHA-256 checksum of the version vector
     */
    fun calculateVersionVectorChecksum(versionVector: VersionVector): String {
        val data = "${versionVector.deviceId}|${versionVector.counter}|${versionVector.timestamp}"
        return calculateChecksum(data)
    }
    
    /**
     * Verify if a recipe's checksum matches the expected value
     * 
     * @param recipe The recipe to verify
     * @param expectedChecksum The expected checksum value
     * @return true if checksums match, false otherwise
     */
    fun verifyRecipeChecksum(recipe: Recipe, expectedChecksum: String): Boolean {
        val actualChecksum = calculateRecipeChecksum(recipe)
        return actualChecksum == expectedChecksum
    }
    
    /**
     * Verify if a string's checksum matches the expected value
     * 
     * @param data The string data to verify
     * @param expectedChecksum The expected checksum value
     * @return true if checksums match, false otherwise
     */
    fun verifyChecksum(data: String, expectedChecksum: String): Boolean {
        val actualChecksum = calculateChecksum(data)
        return actualChecksum == expectedChecksum
    }
    
    /**
     * Verify if a byte array's checksum matches the expected value
     * 
     * @param data The byte array to verify
     * @param expectedChecksum The expected checksum value
     * @return true if checksums match, false otherwise
     */
    fun verifyChecksum(data: ByteArray, expectedChecksum: String): Boolean {
        val actualChecksum = calculateChecksum(data)
        return actualChecksum == expectedChecksum
    }
    
    /**
     * Check if a checksum is valid (non-empty and proper length)
     * SHA-256 checksums should be 64 characters (32 bytes * 2 hex chars per byte)
     * 
     * @param checksum The checksum to validate
     * @return true if checksum appears valid, false otherwise
     */
    fun isValidChecksum(checksum: String): Boolean {
        return checksum.length == 64 && 
               checksum.all { c -> HEX_CHARS.contains(c) }
    }
    
    /**
     * Generate a checksum for a collection of recipes
     * Useful for batch operations and sync verification
     * 
     * @param recipes The collection of recipes to hash
     * @return SHA-256 checksum of all recipe checksums concatenated
     */
    fun calculateBatchChecksum(recipes: List<Recipe>): String {
        val data = recipes.sortedBy { it.id }.joinToString("") { it.checksum }
        return calculateChecksum(data)
    }
    
    /**
     * Create a checksummed wrapper for data that includes both the data and its checksum
     */
    data class ChecksummedData<T>(
        val data: T,
        val checksum: String
    ) {
        fun verify(): Boolean {
            return when (data) {
                is String -> verifyChecksum(data, checksum)
                is ByteArray -> verifyChecksum(data, checksum)
                else -> false
            }
        }
        
        companion object {
            fun <T> create(data: T, checksum: String): ChecksummedData<T> {
                return ChecksummedData(data, checksum)
            }
            
            fun fromString(data: String): ChecksummedData<String> {
                val checksum = calculateChecksum(data)
                return ChecksummedData(data, checksum)
            }
            
            fun fromByteArray(data: ByteArray): ChecksummedData<ByteArray> {
                val checksum = calculateChecksum(data)
                return ChecksummedData(data, checksum)
            }
        }
    }
}
