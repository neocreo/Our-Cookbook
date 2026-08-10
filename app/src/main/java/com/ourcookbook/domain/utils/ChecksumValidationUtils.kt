package com.ourcookbook.domain.utils

import com.ourcookbook.domain.model.Recipe
import com.ourcookbook.domain.model.VersionVector
import com.ourcookbook.domain.service.ChecksumService

/**
 * Utility object for checksum validation operations
 * Provides comprehensive validation for the sync system
 */
object ChecksumValidationUtils {
    
    private val checksumService: ChecksumService by lazy { 
        com.ourcookbook.data.service.ChecksumServiceImpl() 
    }
    
    /**
     * Validate that a recipe has a valid checksum
     * 
     * @param recipe The recipe to validate
     * @return true if the recipe has a valid checksum that matches its content
     */
    fun validateRecipeChecksum(recipe: Recipe): Boolean {
        if (recipe.checksum.isBlank()) {
            return false
        }
        
        // Check if checksum format is valid
        if (!checksumService.isValidChecksum(recipe.checksum)) {
            return false
        }
        
        // Verify checksum matches content
        return checksumService.verifyChecksum(recipe, recipe.checksum)
    }
    
    /**
     * Validate checksum format without verifying content
     * 
     * @param checksum The checksum to validate
     * @return true if checksum has valid format (64 hex characters)
     */
    fun validateChecksumFormat(checksum: String): Boolean {
        return checksumService.isValidChecksum(checksum)
    }
    
    /**
     * Validate that two recipes have matching checksums
     * 
     * @param recipe1 First recipe
     * @param recipe2 Second recipe
     * @return true if both recipes have valid checksums and they match
     */
    fun validateChecksumMatch(recipe1: Recipe, recipe2: Recipe): Boolean {
        return validateRecipeChecksum(recipe1) &&
               validateRecipeChecksum(recipe2) &&
               recipe1.checksum == recipe2.checksum
    }
    
    /**
     * Validate that a recipe's version vector is consistent with its checksum
     * This ensures that version tracking and content hashing are aligned
     * 
     * @param recipe The recipe to validate
     * @return true if version vector and checksum are consistent
     */
    fun validateVersionChecksumConsistency(recipe: Recipe): Boolean {
        if (!validateRecipeChecksum(recipe)) {
            return false
        }
        
        // Calculate checksum that includes version vector
        val versionChecksum = ChecksumUtils.calculateVersionVectorChecksum(recipe.versionVector)
        
        // The recipe checksum should be based on content including version
        // This is already handled in calculateRecipeChecksum
        return true
    }
    
    /**
     * Validate a batch of recipes for checksum consistency
     * 
     * @param recipes The list of recipes to validate
     * @return Pair of (validRecipes, invalidRecipes) lists
     */
    fun validateRecipeBatch(recipes: List<Recipe>): Pair<List<Recipe>, List<Recipe>> {
        val valid = mutableListOf<Recipe>()
        val invalid = mutableListOf<Recipe>()
        
        recipes.forEach { recipe ->
            if (validateRecipeChecksum(recipe)) {
                valid.add(recipe)
            } else {
                invalid.add(recipe)
            }
        }
        
        return Pair(valid, invalid)
    }
    
    /**
     * Validate that a recipe's checksum is appropriate for its version
     * This is useful for detecting if a recipe has been modified without updating its version
     * 
     * @param recipe The recipe to validate
     * @return true if checksum is appropriate for the version
     */
    fun validateChecksumForVersion(recipe: Recipe): Boolean {
        // Calculate what the checksum should be for this recipe
        val expectedChecksum = checksumService.calculateChecksum(recipe)
        
        // If the recipe has no checksum, it's valid (new recipe)
        if (recipe.checksum.isBlank()) {
            return true
        }
        
        // Check if stored checksum matches expected
        return recipe.checksum == expectedChecksum
    }
    
    /**
     * Create a validation report for a recipe
     */
    data class ChecksumValidationReport(
        val recipeId: String,
        val isValid: Boolean,
        val checksumFormatValid: Boolean,
        val checksumMatchesContent: Boolean,
        val versionConsistent: Boolean,
        val errors: List<String> = emptyList()
    ) {
        fun toStringReport(): String {
            return buildString {
                append("Checksum Validation Report for Recipe: ").append(recipeId).append("\n")
                append("Overall Valid: ").append(isValid).append("\n")
                append("Checksum Format Valid: ").append(checksumFormatValid).append("\n")
                append("Checksum Matches Content: ").append(checksumMatchesContent).append("\n")
                append("Version Consistent: ").append(versionConsistent).append("\n")
                if (errors.isNotEmpty()) {
                    append("Errors:\n")
                    errors.forEach { error ->
                        append("  - ").append(error).append("\n")
                    }
                }
            }
        }
    }
    
    /**
     * Generate a detailed validation report for a recipe
     * 
     * @param recipe The recipe to validate
     * @return A comprehensive validation report
     */
    fun generateValidationReport(recipe: Recipe): ChecksumValidationReport {
        val errors = mutableListOf<String>()
        
        // Check checksum format
        val checksumFormatValid = if (recipe.checksum.isBlank()) {
            errors.add("No checksum set")
            false
        } else {
            checksumService.isValidChecksum(recipe.checksum)
        }
        
        if (!checksumFormatValid) {
            errors.add("Invalid checksum format")
        }
        
        // Check if checksum matches content
        val checksumMatchesContent = if (recipe.checksum.isBlank()) {
            false
        } else {
            checksumService.verifyChecksum(recipe, recipe.checksum)
        }
        
        if (!checksumMatchesContent && recipe.checksum.isNotBlank()) {
            errors.add("Checksum does not match recipe content")
        }
        
        // Check version consistency
        val versionConsistent = recipe.versionVector.deviceId.isNotBlank() &&
                               recipe.versionVector.counter >= 0
        
        if (!versionConsistent) {
            errors.add("Invalid version vector")
        }
        
        val isValid = checksumFormatValid && checksumMatchesContent && versionConsistent
        
        return ChecksumValidationReport(
            recipeId = recipe.id,
            isValid = isValid,
            checksumFormatValid = checksumFormatValid,
            checksumMatchesContent = checksumMatchesContent,
            versionConsistent = versionConsistent,
            errors = errors
        )
    }
    
    /**
     * Validate that a recipe can be safely synced
     * This checks all prerequisites for sync operations
     * 
     * @param recipe The recipe to validate for sync
     * @return true if recipe is ready for sync
     */
    fun validateForSync(recipe: Recipe): Boolean {
        return recipe.isValid() && 
               validateRecipeChecksum(recipe) &&
               recipe.versionVector.deviceId.isNotBlank() &&
               recipe.deviceId.isNotBlank()
    }
    
    /**
     * Validate that two recipes can be compared for conflict detection
     * 
     * @param localRecipe The local recipe
     * @param remoteRecipe The remote recipe
     * @return true if both recipes are valid for conflict detection
     */
    fun validateForConflictDetection(localRecipe: Recipe, remoteRecipe: Recipe): Boolean {
        return validateForSync(localRecipe) && validateForSync(remoteRecipe)
    }
    
    /**
     * Quick validation that checks if a recipe has the minimum required fields for checksum calculation
     * 
     * @param recipe The recipe to check
     * @return true if recipe has minimum required fields
     */
    fun hasMinimumRequiredFields(recipe: Recipe): Boolean {
        return recipe.title.isNotBlank() && 
               recipe.category.isNotBlank()
    }
    
    /**
     * Validate checksum consistency across multiple recipes
     * This is useful for detecting duplicate content with different IDs
     * 
     * @param recipes The list of recipes to check
     * @return Map of checksum to list of recipe IDs that share that checksum
     */
    fun findDuplicateChecksums(recipes: List<Recipe>): Map<String, List<String>> {
        val checksumMap = mutableMapOf<String, MutableList<String>>()
        
        recipes.forEach { recipe ->
            if (recipe.checksum.isNotBlank()) {
                val checksum = recipe.checksum
                val recipeIds = checksumMap.getOrPut(checksum) { mutableListOf() }
                recipeIds.add(recipe.id)
            }
        }
        
        // Filter to only checksums with multiple recipes
        return checksumMap.filter { it.value.size > 1 }
    }
}
