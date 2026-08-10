package com.ourcookbook.domain.utils

import com.ourcookbook.domain.model.Ingredient
import com.ourcookbook.domain.model.Recipe
import com.ourcookbook.domain.model.VersionVector
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant
import java.util.UUID

/**
 * Comprehensive test class for ChecksumValidationUtils
 * Validates checksum validation functionality for the sync system
 */
class ChecksumValidationUtilsTest {
    
    // ==================== Recipe Checksum Validation Tests ====================
    
    @Test
    fun `validateRecipeChecksum returns true for recipe with valid checksum`() {
        val recipe = createTestRecipeWithValidChecksum()
        assertTrue("Recipe with valid checksum should validate", 
            ChecksumValidationUtils.validateRecipeChecksum(recipe))
    }
    
    @Test
    fun `validateRecipeChecksum returns false for recipe with invalid checksum`() {
        val recipe = createTestRecipeWithInvalidChecksum()
        assertFalse("Recipe with invalid checksum should not validate", 
            ChecksumValidationUtils.validateRecipeChecksum(recipe))
    }
    
    @Test
    fun `validateRecipeChecksum returns false for recipe with no checksum`() {
        val recipe = createTestRecipeWithNoChecksum()
        assertFalse("Recipe with no checksum should not validate", 
            ChecksumValidationUtils.validateRecipeChecksum(recipe))
    }
    
    @Test
    fun `validateRecipeChecksum returns false for recipe with malformed checksum`() {
        val recipe = createTestRecipeWithMalformedChecksum()
        assertFalse("Recipe with malformed checksum should not validate", 
            ChecksumValidationUtils.validateRecipeChecksum(recipe))
    }
    
    // ==================== Checksum Format Validation Tests ====================
    
    @Test
    fun `validateChecksumFormat returns true for valid SHA-256 checksum`() {
        val validChecksum = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
        assertTrue("Valid checksum format should validate", 
            ChecksumValidationUtils.validateChecksumFormat(validChecksum))
    }
    
    @Test
    fun `validateChecksumFormat returns false for too short checksum`() {
        val shortChecksum = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b8"
        assertFalse("Too short checksum should not validate", 
            ChecksumValidationUtils.validateChecksumFormat(shortChecksum))
    }
    
    @Test
    fun `validateChecksumFormat returns false for too long checksum`() {
        val longChecksum = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855aa"
        assertFalse("Too long checksum should not validate", 
            ChecksumValidationUtils.validateChecksumFormat(longChecksum))
    }
    
    @Test
    fun `validateChecksumFormat returns false for checksum with invalid characters`() {
        val invalidChecksum = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b85g"
        assertFalse("Checksum with invalid characters should not validate", 
            ChecksumValidationUtils.validateChecksumFormat(invalidChecksum))
    }
    
    // ==================== Checksum Match Validation Tests ====================
    
    @Test
    fun `validateChecksumMatch returns true for recipes with matching valid checksums`() {
        val recipe1 = createTestRecipeWithValidChecksum()
        val recipe2 = recipe1.copy(id = UUID.randomUUID().toString())
        
        assertTrue("Recipes with matching valid checksums should validate", 
            ChecksumValidationUtils.validateChecksumMatch(recipe1, recipe2))
    }
    
    @Test
    fun `validateChecksumMatch returns false when checksums do not match`() {
        val recipe1 = createTestRecipeWithValidChecksum()
        val recipe2 = createTestRecipeWithValidChecksum(title = "Different Recipe")
        
        assertFalse("Recipes with different checksums should not validate", 
            ChecksumValidationUtils.validateChecksumMatch(recipe1, recipe2))
    }
    
    @Test
    fun `validateChecksumMatch returns false when one recipe has invalid checksum`() {
        val recipe1 = createTestRecipeWithValidChecksum()
        val recipe2 = createTestRecipeWithInvalidChecksum()
        
        assertFalse("Should not validate when one recipe has invalid checksum", 
            ChecksumValidationUtils.validateChecksumMatch(recipe1, recipe2))
    }
    
    // ==================== Version Checksum Consistency Tests ====================
    
    @Test
    fun `validateVersionChecksumConsistency returns true for valid recipe`() {
        val recipe = createTestRecipeWithValidChecksum()
        assertTrue("Valid recipe should have consistent version checksum", 
            ChecksumValidationUtils.validateVersionChecksumConsistency(recipe))
    }
    
    @Test
    fun `validateVersionChecksumConsistency returns false for recipe with invalid checksum`() {
        val recipe = createTestRecipeWithInvalidChecksum()
        assertFalse("Recipe with invalid checksum should not have consistent version checksum", 
            ChecksumValidationUtils.validateVersionChecksumConsistency(recipe))
    }
    
    // ==================== Batch Validation Tests ====================
    
    @Test
    fun `validateRecipeBatch returns correct separation for mixed batch`() {
        val validRecipe1 = createTestRecipeWithValidChecksum()
        val validRecipe2 = createTestRecipeWithValidChecksum(title = "Another Valid Recipe")
        val invalidRecipe1 = createTestRecipeWithInvalidChecksum()
        val invalidRecipe2 = createTestRecipeWithNoChecksum()
        
        val recipes = listOf(validRecipe1, invalidRecipe1, validRecipe2, invalidRecipe2)
        val (valid, invalid) = ChecksumValidationUtils.validateRecipeBatch(recipes)
        
        assertEquals("Should have 2 valid recipes", 2, valid.size)
        assertEquals("Should have 2 invalid recipes", 2, invalid.size)
        assertTrue("Valid recipes should contain validRecipe1", valid.any { it.id == validRecipe1.id })
        assertTrue("Valid recipes should contain validRecipe2", valid.any { it.id == validRecipe2.id })
        assertTrue("Invalid recipes should contain invalidRecipe1", invalid.any { it.id == invalidRecipe1.id })
        assertTrue("Invalid recipes should contain invalidRecipe2", invalid.any { it.id == invalidRecipe2.id })
    }
    
    @Test
    fun `validateRecipeBatch handles empty list`() {
        val (valid, invalid) = ChecksumValidationUtils.validateRecipeBatch(emptyList())
        assertTrue("Valid list should be empty", valid.isEmpty())
        assertTrue("Invalid list should be empty", invalid.isEmpty())
    }
    
    @Test
    fun `validateRecipeBatch handles all valid recipes`() {
        val recipes = listOf(
            createTestRecipeWithValidChecksum(),
            createTestRecipeWithValidChecksum(title = "Recipe 2"),
            createTestRecipeWithValidChecksum(title = "Recipe 3")
        )
        val (valid, invalid) = ChecksumValidationUtils.validateRecipeBatch(recipes)
        assertEquals("All should be valid", recipes.size, valid.size)
        assertTrue("Invalid should be empty", invalid.isEmpty())
    }
    
    @Test
    fun `validateRecipeBatch handles all invalid recipes`() {
        val recipes = listOf(
            createTestRecipeWithInvalidChecksum(),
            createTestRecipeWithNoChecksum(),
            createTestRecipeWithMalformedChecksum()
        )
        val (valid, invalid) = ChecksumValidationUtils.validateRecipeBatch(recipes)
        assertTrue("Valid should be empty", valid.isEmpty())
        assertEquals("All should be invalid", recipes.size, invalid.size)
    }
    
    // ==================== Checksum for Version Validation Tests ====================
    
    @Test
    fun `validateChecksumForVersion returns true for new recipe with no checksum`() {
        val recipe = createTestRecipeWithNoChecksum()
        assertTrue("New recipe with no checksum should validate", 
            ChecksumValidationUtils.validateChecksumForVersion(recipe))
    }
    
    @Test
    fun `validateChecksumForVersion returns true for recipe with matching checksum`() {
        val recipe = createTestRecipeWithValidChecksum()
        assertTrue("Recipe with matching checksum should validate", 
            ChecksumValidationUtils.validateChecksumForVersion(recipe))
    }
    
    @Test
    fun `validateChecksumForVersion returns false for recipe with mismatched checksum`() {
        val recipe = createTestRecipeWithInvalidChecksum()
        assertFalse("Recipe with mismatched checksum should not validate", 
            ChecksumValidationUtils.validateChecksumForVersion(recipe))
    }
    
    // ==================== Validation Report Tests ====================
    
    @Test
    fun `generateValidationReport returns valid report for valid recipe`() {
        val recipe = createTestRecipeWithValidChecksum()
        val report = ChecksumValidationUtils.generateValidationReport(recipe)
        
        assertTrue("Report should indicate valid", report.isValid)
        assertTrue("Checksum format should be valid", report.checksumFormatValid)
        assertTrue("Checksum should match content", report.checksumMatchesContent)
        assertTrue("Version should be consistent", report.versionConsistent)
        assertTrue("Errors should be empty", report.errors.isEmpty())
    }
    
    @Test
    fun `generateValidationReport returns invalid report for invalid recipe`() {
        val recipe = createTestRecipeWithInvalidChecksum()
        val report = ChecksumValidationUtils.generateValidationReport(recipe)
        
        assertFalse("Report should indicate invalid", report.isValid)
        assertFalse("Checksum should not match content", report.checksumMatchesContent)
        assertTrue("Errors should not be empty", report.errors.isNotEmpty())
    }
    
    @Test
    fun `generateValidationReport handles recipe with no checksum`() {
        val recipe = createTestRecipeWithNoChecksum()
        val report = ChecksumValidationUtils.generateValidationReport(recipe)
        
        assertFalse("Report should indicate invalid", report.isValid)
        assertFalse("Checksum format should be invalid", report.checksumFormatValid)
        assertFalse("Checksum should not match content", report.checksumMatchesContent)
        assertTrue("Should have error about no checksum", report.errors.any { it.contains("No checksum") })
    }
    
    @Test
    fun `generateValidationReport toStringReport contains expected information`() {
        val recipe = createTestRecipeWithValidChecksum()
        val report = ChecksumValidationUtils.generateValidationReport(recipe)
        val reportString = report.toStringReport()
        
        assertTrue("Report should contain recipe ID", reportString.contains(recipe.id))
        assertTrue("Report should indicate overall valid", reportString.contains("Overall Valid: true"))
        assertTrue("Report should indicate checksum format valid", reportString.contains("Checksum Format Valid: true"))
    }
    
    // ==================== Sync Validation Tests ====================
    
    @Test
    fun `validateForSync returns true for valid sync-ready recipe`() {
        val recipe = createTestRecipeWithValidChecksum()
        assertTrue("Valid recipe should be ready for sync", 
            ChecksumValidationUtils.validateForSync(recipe))
    }
    
    @Test
    fun `validateForSync returns false for invalid recipe`() {
        val recipe = createTestRecipeWithInvalidChecksum()
        assertFalse("Invalid recipe should not be ready for sync", 
            ChecksumValidationUtils.validateForSync(recipe))
    }
    
    @Test
    fun `validateForSync returns false for recipe with no device ID`() {
        val recipe = createTestRecipeWithValidChecksum(deviceId = "")
        assertFalse("Recipe with no device ID should not be ready for sync", 
            ChecksumValidationUtils.validateForSync(recipe))
    }
    
    @Test
    fun `validateForSync returns false for recipe with invalid version vector`() {
        val recipe = createTestRecipeWithValidChecksum(
            versionVector = VersionVector(deviceId = "", counter = -1, timestamp = Instant.now())
        )
        assertFalse("Recipe with invalid version vector should not be ready for sync", 
            ChecksumValidationUtils.validateForSync(recipe))
    }
    
    @Test
    fun `validateForConflictDetection returns true for valid recipes`() {
        val localRecipe = createTestRecipeWithValidChecksum()
        val remoteRecipe = createTestRecipeWithValidChecksum(title = "Remote Recipe")
        
        assertTrue("Valid recipes should be ready for conflict detection", 
            ChecksumValidationUtils.validateForConflictDetection(localRecipe, remoteRecipe))
    }
    
    @Test
    fun `validateForConflictDetection returns false when local recipe is invalid`() {
        val localRecipe = createTestRecipeWithInvalidChecksum()
        val remoteRecipe = createTestRecipeWithValidChecksum()
        
        assertFalse("Should not be ready for conflict detection when local is invalid", 
            ChecksumValidationUtils.validateForConflictDetection(localRecipe, remoteRecipe))
    }
    
    @Test
    fun `validateForConflictDetection returns false when remote recipe is invalid`() {
        val localRecipe = createTestRecipeWithValidChecksum()
        val remoteRecipe = createTestRecipeWithInvalidChecksum()
        
        assertFalse("Should not be ready for conflict detection when remote is invalid", 
            ChecksumValidationUtils.validateForConflictDetection(localRecipe, remoteRecipe))
    }
    
    // ==================== Minimum Required Fields Tests ====================
    
    @Test
    fun `hasMinimumRequiredFields returns true for recipe with required fields`() {
        val recipe = createTestRecipeWithValidChecksum()
        assertTrue("Recipe with required fields should have minimum fields", 
            ChecksumValidationUtils.hasMinimumRequiredFields(recipe))
    }
    
    @Test
    fun `hasMinimumRequiredFields returns false for recipe with blank title`() {
        val recipe = createTestRecipeWithValidChecksum(title = "")
        assertFalse("Recipe with blank title should not have minimum fields", 
            ChecksumValidationUtils.hasMinimumRequiredFields(recipe))
    }
    
    @Test
    fun `hasMinimumRequiredFields returns false for recipe with blank category`() {
        val recipe = createTestRecipeWithValidChecksum(category = "")
        assertFalse("Recipe with blank category should not have minimum fields", 
            ChecksumValidationUtils.hasMinimumRequiredFields(recipe))
    }
    
    // ==================== Duplicate Checksum Detection Tests ====================
    
    @Test
    fun `findDuplicateChecksums returns empty map when no duplicates`() {
        val recipes = listOf(
            createTestRecipeWithValidChecksum(title = "Recipe 1"),
            createTestRecipeWithValidChecksum(title = "Recipe 2"),
            createTestRecipeWithValidChecksum(title = "Recipe 3")
        )
        val duplicates = ChecksumValidationUtils.findDuplicateChecksums(recipes)
        assertTrue("Should have no duplicates", duplicates.isEmpty())
    }
    
    @Test
    fun `findDuplicateChecksums returns duplicates when they exist`() {
        // Create two recipes with the same content (hence same checksum)
        val recipe1 = createTestRecipeWithValidChecksum(title = "Same Recipe")
        val recipe2 = recipe1.copy(id = UUID.randomUUID().toString())
        val recipe3 = createTestRecipeWithValidChecksum(title = "Different Recipe")
        
        val recipes = listOf(recipe1, recipe2, recipe3)
        val duplicates = ChecksumValidationUtils.findDuplicateChecksums(recipes)
        
        assertEquals("Should have one duplicate checksum", 1, duplicates.size)
        assertTrue("Duplicate checksum should map to both recipe IDs", 
            duplicates.values.first().size == 2)
    }
    
    @Test
    fun `findDuplicateChecksums handles recipes with no checksums`() {
        val recipe1 = createTestRecipeWithNoChecksum()
        val recipe2 = createTestRecipeWithNoChecksum()
        val recipe3 = createTestRecipeWithValidChecksum()
        
        val recipes = listOf(recipe1, recipe2, recipe3)
        val duplicates = ChecksumValidationUtils.findDuplicateChecksums(recipes)
        
        // Recipes with no checksum should not appear in duplicates
        assertTrue("Should have no duplicates (no checksums to compare)", duplicates.isEmpty())
    }
    
    // ==================== Helper Methods ====================
    
    private fun createTestRecipeWithValidChecksum(
        title: String = "Test Recipe",
        category: String = "Test Category",
        deviceId: String = "test-device",
        versionVector: VersionVector = VersionVector(deviceId = "test-device", counter = 1, timestamp = Instant.now())
    ): Recipe {
        val recipe = Recipe(
            id = UUID.randomUUID().toString(),
            title = title,
            description = "Test description",
            category = category,
            ingredients = listOf(
                Ingredient(name = "Ingredient 1", amount = "1", unit = "cup")
            ),
            instructions = listOf("Step 1", "Step 2"),
            servingSize = 4,
            prepTime = 15,
            cookTime = 30,
            rating = 4.5f,
            isFavorite = true,
            imageUrl = "https://example.com/image.jpg",
            notes = "Test notes",
            source = "Test source",
            tags = listOf("test", "recipe"),
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            versionVector = versionVector,
            checksum = "", // Will be calculated
            deviceId = deviceId
        )
        
        // Calculate and set valid checksum
        val checksumService = com.ourcookbook.data.service.ChecksumServiceImpl()
        return checksumService.updateRecipeChecksum(recipe)
    }
    
    private fun createTestRecipeWithInvalidChecksum(
        title: String = "Test Recipe",
        category: String = "Test Category"
    ): Recipe {
        return Recipe(
            id = UUID.randomUUID().toString(),
            title = title,
            description = "Test description",
            category = category,
            ingredients = listOf(
                Ingredient(name = "Ingredient 1", amount = "1", unit = "cup")
            ),
            instructions = listOf("Step 1", "Step 2"),
            servingSize = 4,
            prepTime = 15,
            cookTime = 30,
            rating = 4.5f,
            isFavorite = true,
            imageUrl = "https://example.com/image.jpg",
            notes = "Test notes",
            source = "Test source",
            tags = listOf("test", "recipe"),
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            versionVector = VersionVector(deviceId = "test-device", counter = 1, timestamp = Instant.now()),
            checksum = "0000000000000000000000000000000000000000000000000000000000000000", // Invalid checksum
            deviceId = "test-device"
        )
    }
    
    private fun createTestRecipeWithNoChecksum(
        title: String = "Test Recipe",
        category: String = "Test Category"
    ): Recipe {
        return Recipe(
            id = UUID.randomUUID().toString(),
            title = title,
            description = "Test description",
            category = category,
            ingredients = listOf(
                Ingredient(name = "Ingredient 1", amount = "1", unit = "cup")
            ),
            instructions = listOf("Step 1", "Step 2"),
            servingSize = 4,
            prepTime = 15,
            cookTime = 30,
            rating = 4.5f,
            isFavorite = true,
            imageUrl = "https://example.com/image.jpg",
            notes = "Test notes",
            source = "Test source",
            tags = listOf("test", "recipe"),
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            versionVector = VersionVector(deviceId = "test-device", counter = 1, timestamp = Instant.now()),
            checksum = "", // No checksum
            deviceId = "test-device"
        )
    }
    
    private fun createTestRecipeWithMalformedChecksum(
        title: String = "Test Recipe",
        category: String = "Test Category"
    ): Recipe {
        return Recipe(
            id = UUID.randomUUID().toString(),
            title = title,
            description = "Test description",
            category = category,
            ingredients = listOf(
                Ingredient(name = "Ingredient 1", amount = "1", unit = "cup")
            ),
            instructions = listOf("Step 1", "Step 2"),
            servingSize = 4,
            prepTime = 15,
            cookTime = 30,
            rating = 4.5f,
            isFavorite = true,
            imageUrl = "https://example.com/image.jpg",
            notes = "Test notes",
            source = "Test source",
            tags = listOf("test", "recipe"),
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            versionVector = VersionVector(deviceId = "test-device", counter = 1, timestamp = Instant.now()),
            checksum = "invalid-checksum", // Malformed checksum
            deviceId = "test-device"
        )
    }
}
