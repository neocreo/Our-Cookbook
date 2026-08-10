package com.ourcookbook.domain.service

import com.ourcookbook.domain.model.Ingredient
import com.ourcookbook.domain.model.Recipe
import com.ourcookbook.domain.model.VersionVector

/**
 * Service interface for checksum calculation and validation
 * Provides functionality for data integrity verification in the sync system
 */
interface ChecksumService {
    
    /**
     * Calculate SHA-256 checksum for a string
     * 
     * @param data The string data to hash
     * @return Hexadecimal string representation of the SHA-256 hash
     */
    fun calculateChecksum(data: String): String
    
    /**
     * Calculate SHA-256 checksum for a byte array
     * 
     * @param data The byte array to hash
     * @return Hexadecimal string representation of the SHA-256 hash
     */
    fun calculateChecksum(data: ByteArray): String
    
    /**
     * Calculate checksum for a Recipe object
     * Includes all relevant fields that affect the recipe content
     * 
     * @param recipe The recipe to calculate checksum for
     * @return SHA-256 checksum of the recipe content
     */
    fun calculateChecksum(recipe: Recipe): String
    
    /**
     * Calculate checksum for a VersionVector
     * 
     * @param versionVector The version vector to hash
     * @return SHA-256 checksum of the version vector
     */
    fun calculateChecksum(versionVector: VersionVector): String
    
    /**
     * Verify if a recipe's checksum matches the expected value
     * 
     * @param recipe The recipe to verify
     * @param expectedChecksum The expected checksum value
     * @return true if checksums match, false otherwise
     */
    fun verifyChecksum(recipe: Recipe, expectedChecksum: String): Boolean
    
    /**
     * Verify if a string's checksum matches the expected value
     * 
     * @param data The string data to verify
     * @param expectedChecksum The expected checksum value
     * @return true if checksums match, false otherwise
     */
    fun verifyChecksum(data: String, expectedChecksum: String): Boolean
    
    /**
     * Verify if a byte array's checksum matches the expected value
     * 
     * @param data The byte array to verify
     * @param expectedChecksum The expected checksum value
     * @return true if checksums match, false otherwise
     */
    fun verifyChecksum(data: ByteArray, expectedChecksum: String): Boolean
    
    /**
     * Check if a checksum is valid (non-empty and proper length)
     * SHA-256 checksums should be 64 characters (32 bytes * 2 hex chars per byte)
     * 
     * @param checksum The checksum to validate
     * @return true if checksum appears valid, false otherwise
     */
    fun isValidChecksum(checksum: String): Boolean
    
    /**
     * Generate a checksum for a collection of recipes
     * Useful for batch operations and sync verification
     * 
     * @param recipes The collection of recipes to hash
     * @return SHA-256 checksum of all recipe checksums concatenated
     */
    fun calculateBatchChecksum(recipes: List<Recipe>): String
    
    /**
     * Update a recipe with a new checksum based on its current content
     * 
     * @param recipe The recipe to update
     * @return A new Recipe instance with updated checksum
     */
    fun updateRecipeChecksum(recipe: Recipe): Recipe
    
    /**
     * Create a new recipe with checksum calculated from its content
     * 
     * @param recipe The recipe to process
     * @return A new Recipe instance with checksum set
     */
    fun withChecksum(recipe: Recipe): Recipe
}
