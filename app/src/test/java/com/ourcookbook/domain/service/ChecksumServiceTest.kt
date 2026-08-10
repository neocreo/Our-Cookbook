package com.ourcookbook.domain.service

import com.ourcookbook.data.service.ChecksumServiceImpl
import com.ourcookbook.domain.model.Ingredient
import com.ourcookbook.domain.model.Recipe
import com.ourcookbook.domain.model.VersionVector
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.util.UUID

/**
 * Comprehensive test class for ChecksumService implementation
 * Validates SHA-256 hashing, version vector integration, and checksum validation
 */
class ChecksumServiceTest {
    
    private lateinit var checksumService: ChecksumService
    
    @Before
    fun setUp() {
        checksumService = ChecksumServiceImpl()
    }
    
    // ==================== SHA-256 Hashing Tests ====================
    
    @Test
    fun `calculateChecksum for empty string returns valid SHA-256 hash`() {
        val checksum = checksumService.calculateChecksum("")
        assertTrue("Empty string checksum should be valid", checksumService.isValidChecksum(checksum))
        assertEquals("Expected checksum for empty string", 
            "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", 
            checksum)
    }
    
    @Test
    fun `calculateChecksum for known string returns correct hash`() {
        val testString = "hello world"
        val expectedChecksum = "b94d27b9934d3e08a52e52d7da7dabfac484efe37a5380ee9088f7ace2efcde9"
        val actualChecksum = checksumService.calculateChecksum(testString)
        assertEquals("Checksum for 'hello world' should match expected", expectedChecksum, actualChecksum)
    }
    
    @Test
    fun `calculateChecksum for same input produces same output`() {
        val testString = "test data for checksum"
        val checksum1 = checksumService.calculateChecksum(testString)
        val checksum2 = checksumService.calculateChecksum(testString)
        assertEquals("Same input should produce same checksum", checksum1, checksum2)
    }
    
    @Test
    fun `calculateChecksum for different inputs produces different outputs`() {
        val checksum1 = checksumService.calculateChecksum("input1")
        val checksum2 = checksumService.calculateChecksum("input2")
        assertNotEquals("Different inputs should produce different checksums", checksum1, checksum2)
    }
    
    @Test
    fun `calculateChecksum for byte array produces valid hash`() {
        val data = "test data".toByteArray(Charsets.UTF_8)
        val checksum = checksumService.calculateChecksum(data)
        assertTrue("Byte array checksum should be valid", checksumService.isValidChecksum(checksum))
        assertEquals("Byte array checksum should match string checksum", 
            checksumService.calculateChecksum("test data"), checksum)
    }
    
    // ==================== Checksum Validation Tests ====================
    
    @Test
    fun `verifyChecksum returns true for matching checksum`() {
        val data = "test data"
        val checksum = checksumService.calculateChecksum(data)
        assertTrue("Checksum should verify correctly", checksumService.verifyChecksum(data, checksum))
    }
    
    @Test
    fun `verifyChecksum returns false for non-matching checksum`() {
        val data = "test data"
        val wrongChecksum = "0000000000000000000000000000000000000000000000000000000000000000"
        assertFalse("Checksum should not verify with wrong checksum", 
            checksumService.verifyChecksum(data, wrongChecksum))
    }
    
    @Test
    fun `isValidChecksum returns true for valid SHA-256 checksum`() {
        val validChecksum = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
        assertTrue("Valid checksum should be recognized", checksumService.isValidChecksum(validChecksum))
    }
    
    @Test
    fun `isValidChecksum returns false for too short checksum`() {
        val shortChecksum = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b8"
        assertFalse("Too short checksum should be invalid", checksumService.isValidChecksum(shortChecksum))
    }
    
    @Test
    fun `isValidChecksum returns false for too long checksum`() {
        val longChecksum = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855aa"
        assertFalse("Too long checksum should be invalid", checksumService.isValidChecksum(longChecksum))
    }
    
    @Test
    fun `isValidChecksum returns false for checksum with invalid characters`() {
        val invalidChecksum = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b85g"
        assertFalse("Checksum with invalid characters should be invalid", 
            checksumService.isValidChecksum(invalidChecksum))
    }
    
    // ==================== Recipe Checksum Tests ====================
    
    @Test
    fun `calculateChecksum for Recipe produces valid hash`() {
        val recipe = createTestRecipe()
        val checksum = checksumService.calculateChecksum(recipe)
        assertTrue("Recipe checksum should be valid", checksumService.isValidChecksum(checksum))
    }
    
    @Test
    fun `calculateChecksum for same Recipe produces same checksum`() {
        val recipe = createTestRecipe()
        val checksum1 = checksumService.calculateChecksum(recipe)
        val checksum2 = checksumService.calculateChecksum(recipe)
        assertEquals("Same recipe should produce same checksum", checksum1, checksum2)
    }
    
    @Test
    fun `calculateChecksum for different Recipes produces different checksums`() {
        val recipe1 = createTestRecipe(title = "Recipe 1")
        val recipe2 = createTestRecipe(title = "Recipe 2")
        val checksum1 = checksumService.calculateChecksum(recipe1)
        val checksum2 = checksumService.calculateChecksum(recipe2)
        assertNotEquals("Different recipes should produce different checksums", checksum1, checksum2)
    }
    
    @Test
    fun `verifyChecksum for Recipe returns true when checksum matches`() {
        val recipe = createTestRecipe()
        val checksum = checksumService.calculateChecksum(recipe)
        assertTrue("Recipe checksum should verify correctly", 
            checksumService.verifyChecksum(recipe, checksum))
    }
    
    @Test
    fun `verifyChecksum for Recipe returns false when checksum does not match`() {
        val recipe = createTestRecipe()
        val wrongChecksum = "0000000000000000000000000000000000000000000000000000000000000000"
        assertFalse("Recipe checksum should not verify with wrong checksum", 
            checksumService.verifyChecksum(recipe, wrongChecksum))
    }
    
    @Test
    fun `updateRecipeChecksum creates recipe with valid checksum`() {
        val recipe = createTestRecipe(checksum = "")
        val updatedRecipe = checksumService.updateRecipeChecksum(recipe)
        assertTrue("Updated recipe should have valid checksum", 
            checksumService.isValidChecksum(updatedRecipe.checksum))
        assertTrue("Updated recipe checksum should verify", 
            checksumService.verifyChecksum(updatedRecipe, updatedRecipe.checksum))
    }
    
    @Test
    fun `withChecksum creates recipe with valid checksum`() {
        val recipe = createTestRecipe(checksum = "")
        val recipeWithChecksum = checksumService.withChecksum(recipe)
        assertTrue("Recipe with checksum should have valid checksum", 
            checksumService.isValidChecksum(recipeWithChecksum.checksum))
        assertTrue("Recipe with checksum should verify", 
            checksumService.verifyChecksum(recipeWithChecksum, recipeWithChecksum.checksum))
    }
    
    // ==================== Version Vector Checksum Tests ====================
    
    @Test
    fun `calculateChecksum for VersionVector produces valid hash`() {
        val versionVector = VersionVector(deviceId = "device1", counter = 1, timestamp = Instant.now())
        val checksum = checksumService.calculateChecksum(versionVector)
        assertTrue("VersionVector checksum should be valid", checksumService.isValidChecksum(checksum))
    }
    
    @Test
    fun `calculateChecksum for same VersionVector produces same checksum`() {
        val instant = Instant.parse("2023-01-01T00:00:00Z")
        val versionVector = VersionVector(deviceId = "device1", counter = 1, timestamp = instant)
        val checksum1 = checksumService.calculateChecksum(versionVector)
        val checksum2 = checksumService.calculateChecksum(versionVector)
        assertEquals("Same version vector should produce same checksum", checksum1, checksum2)
    }
    
    @Test
    fun `calculateChecksum for different VersionVectors produces different checksums`() {
        val instant = Instant.parse("2023-01-01T00:00:00Z")
        val versionVector1 = VersionVector(deviceId = "device1", counter = 1, timestamp = instant)
        val versionVector2 = VersionVector(deviceId = "device2", counter = 1, timestamp = instant)
        val checksum1 = checksumService.calculateChecksum(versionVector1)
        val checksum2 = checksumService.calculateChecksum(versionVector2)
        assertNotEquals("Different version vectors should produce different checksums", checksum1, checksum2)
    }
    
    // ==================== Batch Checksum Tests ====================
    
    @Test
    fun `calculateBatchChecksum for empty list produces valid hash`() {
        val batchChecksum = checksumService.calculateBatchChecksum(emptyList())
        assertTrue("Empty batch checksum should be valid", checksumService.isValidChecksum(batchChecksum))
    }
    
    @Test
    fun `calculateBatchChecksum for single recipe produces valid hash`() {
        val recipes = listOf(createTestRecipe())
        val batchChecksum = checksumService.calculateBatchChecksum(recipes)
        assertTrue("Single recipe batch checksum should be valid", checksumService.isValidChecksum(batchChecksum))
    }
    
    @Test
    fun `calculateBatchChecksum for multiple recipes produces valid hash`() {
        val recipes = listOf(
            createTestRecipe(title = "Recipe 1"),
            createTestRecipe(title = "Recipe 2"),
            createTestRecipe(title = "Recipe 3")
        )
        val batchChecksum = checksumService.calculateBatchChecksum(recipes)
        assertTrue("Multiple recipe batch checksum should be valid", checksumService.isValidChecksum(batchChecksum))
    }
    
    @Test
    fun `calculateBatchChecksum produces same result for same recipes in same order`() {
        val recipes = listOf(
            createTestRecipe(title = "Recipe 1"),
            createTestRecipe(title = "Recipe 2")
        )
        val batchChecksum1 = checksumService.calculateBatchChecksum(recipes)
        val batchChecksum2 = checksumService.calculateBatchChecksum(recipes)
        assertEquals("Same recipes in same order should produce same batch checksum", 
            batchChecksum1, batchChecksum2)
    }
    
    @Test
    fun `calculateBatchChecksum produces different result for different recipe order`() {
        val recipe1 = createTestRecipe(title = "Recipe 1")
        val recipe2 = createTestRecipe(title = "Recipe 2")
        
        val batch1 = checksumService.calculateBatchChecksum(listOf(recipe1, recipe2))
        val batch2 = checksumService.calculateBatchChecksum(listOf(recipe2, recipe1))
        
        // Note: This might be the same if the recipes have the same checksum, 
        // but with different titles they should have different checksums
        assertNotEquals("Different recipe order should produce different batch checksum", 
            batch1, batch2)
    }
    
    // ==================== Edge Case Tests ====================
    
    @Test
    fun `calculateChecksum handles unicode characters correctly`() {
        val unicodeString = "Hello 世界 🌍"
        val checksum = checksumService.calculateChecksum(unicodeString)
        assertTrue("Unicode string checksum should be valid", checksumService.isValidChecksum(checksum))
        assertTrue("Unicode string checksum should verify", 
            checksumService.verifyChecksum(unicodeString, checksum))
    }
    
    @Test
    fun `calculateChecksum handles very long strings`() {
        val longString = "a".repeat(10000)
        val checksum = checksumService.calculateChecksum(longString)
        assertTrue("Long string checksum should be valid", checksumService.isValidChecksum(checksum))
        assertTrue("Long string checksum should verify", 
            checksumService.verifyChecksum(longString, checksum))
    }
    
    @Test
    fun `calculateChecksum handles special characters`() {
        val specialString = "!@#$%^&*()_+-=[]{}|;':\",./<>?"
        val checksum = checksumService.calculateChecksum(specialString)
        assertTrue("Special characters checksum should be valid", checksumService.isValidChecksum(checksum))
        assertTrue("Special characters checksum should verify", 
            checksumService.verifyChecksum(specialString, checksum))
    }
    
    @Test
    fun `calculateChecksum handles null-like strings`() {
        val nullString = "null"
        val emptyString = ""
        val blankString = "   "
        
        val nullChecksum = checksumService.calculateChecksum(nullString)
        val emptyChecksum = checksumService.calculateChecksum(emptyString)
        val blankChecksum = checksumService.calculateChecksum(blankString)
        
        assertNotEquals("null string should have different checksum than empty", nullChecksum, emptyChecksum)
        assertNotEquals("null string should have different checksum than blank", nullChecksum, blankChecksum)
        assertNotEquals("empty string should have different checksum than blank", emptyChecksum, blankChecksum)
    }
    
    // ==================== Helper Methods ====================
    
    private fun createTestRecipe(
        title: String = "Test Recipe",
        category: String = "Test Category",
        ingredients: List<Ingredient> = listOf(
            Ingredient(name = "Ingredient 1", amount = "1", unit = "cup"),
            Ingredient(name = "Ingredient 2", amount = "2", unit = "tablespoons")
        ),
        instructions: List<String> = listOf("Step 1", "Step 2", "Step 3"),
        checksum: String = ""
    ): Recipe {
        return Recipe(
            id = UUID.randomUUID().toString(),
            title = title,
            description = "Test description",
            category = category,
            ingredients = ingredients,
            instructions = instructions,
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
            checksum = checksum,
            deviceId = "test-device"
        )
    }
}
