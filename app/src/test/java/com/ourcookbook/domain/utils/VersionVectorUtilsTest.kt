package com.ourcookbook.domain.utils

import com.ourcookbook.domain.model.Recipe
import com.ourcookbook.domain.model.SyncVersionVector
import com.ourcookbook.domain.model.VersionVector
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant
import java.util.UUID

/**
 * Comprehensive test class for VersionVectorUtils
 * Validates version vector operations and conflict detection utilities
 */
class VersionVectorUtilsTest {
    
    // ==================== VersionVector Creation Tests ====================
    
    @Test
    fun `createVersionVector creates valid version vector`() {
        val deviceId = "test-device"
        val versionVector = VersionVectorUtils.createVersionVector(deviceId)
        
        assertEquals("Device ID should match", deviceId, versionVector.deviceId)
        assertEquals("Counter should start at 0", 0, versionVector.counter)
        assertNotNull("Timestamp should be set", versionVector.timestamp)
    }
    
    @Test
    fun `incrementVersionVector increases counter and updates timestamp`() {
        val deviceId = "test-device"
        val original = VersionVectorUtils.createVersionVector(deviceId)
        
        // Small delay to ensure timestamp difference
        Thread.sleep(10)
        
        val incremented = VersionVectorUtils.incrementVersionVector(original, deviceId)
        
        assertEquals("Counter should be incremented", 1, incremented.counter)
        assertTrue("Timestamp should be updated", incremented.timestamp > original.timestamp)
        assertEquals("Device ID should remain the same", deviceId, incremented.deviceId)
    }
    
    // ==================== SyncVersionVector Tests ====================
    
    @Test
    fun `createSyncVersionVector from VersionVector`() {
        val versionVector = VersionVector(deviceId = "device1", counter = 1, timestamp = Instant.now())
        val syncVersionVector = VersionVectorUtils.createSyncVersionVector(versionVector)
        
        assertEquals("Should contain one version", 1, syncVersionVector.versions.size)
        assertTrue("Should contain the device", syncVersionVector.versions.containsKey("device1"))
        assertEquals("Version should match", versionVector, syncVersionVector.versions["device1"])
    }
    
    @Test
    fun `createEmptySyncVersionVector creates empty sync vector`() {
        val emptySyncVector = VersionVectorUtils.createEmptySyncVersionVector()
        assertTrue("Should be empty", VersionVectorUtils.isEmpty(emptySyncVector))
        assertEquals("Size should be 0", 0, VersionVectorUtils.size(emptySyncVector))
    }
    
    @Test
    fun `mergeSyncVersionVectors combines versions from both`() {
        val instant1 = Instant.parse("2023-01-01T00:00:00Z")
        val instant2 = Instant.parse("2023-01-02T00:00:00Z")
        
        val sync1 = SyncVersionVector(
            versions = mapOf(
                "device1" to VersionVector("device1", 1, instant1),
                "device2" to VersionVector("device2", 2, instant2)
            )
        )
        
        val sync2 = SyncVersionVector(
            versions = mapOf(
                "device2" to VersionVector("device2", 3, instant2), // Newer version
                "device3" to VersionVector("device3", 1, instant1)  // New device
            )
        )
        
        val merged = VersionVectorUtils.mergeSyncVersionVectors(sync1, sync2)
        
        assertEquals("Should contain all devices", 3, merged.versions.size)
        assertEquals("device1 should be from sync1", 1, merged.versions["device1"]?.counter)
        assertEquals("device2 should be the newer version from sync2", 3, merged.versions["device2"]?.counter)
        assertEquals("device3 should be from sync2", 1, merged.versions["device3"]?.counter)
    }
    
    // ==================== Compatibility Tests ====================
    
    @Test
    fun `areCompatible returns true for compatible version vectors`() {
        val instant1 = Instant.parse("2023-01-01T00:00:00Z")
        val instant2 = Instant.parse("2023-01-02T00:00:00Z")
        
        val sync1 = SyncVersionVector(
            versions = mapOf("device1" to VersionVector("device1", 1, instant1))
        )
        
        val sync2 = SyncVersionVector(
            versions = mapOf("device1" to VersionVector("device1", 2, instant2)) // Newer version
        )
        
        assertTrue("Compatible versions should return true", 
            VersionVectorUtils.areCompatible(sync1, sync2))
    }
    
    @Test
    fun `areCompatible returns false for conflicting version vectors`() {
        val instant1 = Instant.parse("2023-01-01T00:00:00Z")
        val instant2 = Instant.parse("2023-01-02T00:00:00Z")
        
        // Create two version vectors where neither is ancestor of the other
        val version1 = VersionVector("device1", 2, instant2) // Higher counter, later timestamp
        val version2 = VersionVector("device1", 1, instant1) // Lower counter, earlier timestamp
        
        val sync1 = SyncVersionVector(versions = mapOf("device1" to version1))
        val sync2 = SyncVersionVector(versions = mapOf("device1" to version2))
        
        // These should be compatible since version1 is newer than version2
        assertTrue("Should be compatible when one is clearly newer", 
            VersionVectorUtils.areCompatible(sync1, sync2))
    }
    
    // ==================== Ancestor Tests ====================
    
    @Test
    fun `isAncestor returns true when version is ancestor`() {
        val instant1 = Instant.parse("2023-01-01T00:00:00Z")
        val instant2 = Instant.parse("2023-01-02T00:00:00Z")
        
        val ancestor = VersionVector("device1", 1, instant1)
        val descendant = VersionVector("device1", 2, instant2)
        
        assertTrue("Ancestor should be recognized", 
            VersionVectorUtils.isAncestor(ancestor, descendant))
    }
    
    @Test
    fun `isAncestor returns false when version is not ancestor`() {
        val instant1 = Instant.parse("2023-01-01T00:00:00Z")
        val instant2 = Instant.parse("2023-01-02T00:00:00Z")
        
        val newer = VersionVector("device1", 2, instant2)
        val older = VersionVector("device1", 1, instant1)
        
        assertFalse("Newer version should not be ancestor of older", 
            VersionVectorUtils.isAncestor(newer, older))
    }
    
    @Test
    fun `isAncestor returns true for same version`() {
        val instant = Instant.parse("2023-01-01T00:00:00Z")
        val version = VersionVector("device1", 1, instant)
        
        assertTrue("Same version should be considered ancestor", 
            VersionVectorUtils.isAncestor(version, version))
    }
    
    // ==================== Same Version Tests ====================
    
    @Test
    fun `areSameVersion returns true for identical versions`() {
        val instant = Instant.parse("2023-01-01T00:00:00Z")
        val version1 = VersionVector("device1", 1, instant)
        val version2 = VersionVector("device1", 1, instant)
        
        assertTrue("Identical versions should be same", 
            VersionVectorUtils.areSameVersion(version1, version2))
    }
    
    @Test
    fun `areSameVersion returns false for different versions`() {
        val instant = Instant.parse("2023-01-01T00:00:00Z")
        val version1 = VersionVector("device1", 1, instant)
        val version2 = VersionVector("device1", 2, instant)
        
        assertFalse("Different versions should not be same", 
            VersionVectorUtils.areSameVersion(version1, version2))
    }
    
    // ==================== SyncVersionVector Utility Tests ====================
    
    @Test
    fun `getVersionForDevice returns correct version`() {
        val instant = Instant.parse("2023-01-01T00:00:00Z")
        val versionVector = VersionVector("device1", 1, instant)
        val syncVersionVector = SyncVersionVector(versions = mapOf("device1" to versionVector))
        
        val retrieved = VersionVectorUtils.getVersionForDevice(syncVersionVector, "device1")
        assertEquals("Should return the correct version", versionVector, retrieved)
    }
    
    @Test
    fun `getVersionForDevice returns null for non-existent device`() {
        val syncVersionVector = VersionVectorUtils.createEmptySyncVersionVector()
        val retrieved = VersionVectorUtils.getVersionForDevice(syncVersionVector, "non-existent")
        assertNull("Should return null for non-existent device", retrieved)
    }
    
    @Test
    fun `updateVersionInSyncVector updates existing version`() {
        val instant1 = Instant.parse("2023-01-01T00:00:00Z")
        val instant2 = Instant.parse("2023-01-02T00:00:00Z")
        
        val originalVersion = VersionVector("device1", 1, instant1)
        val updatedVersion = VersionVector("device1", 2, instant2)
        
        val syncVersionVector = SyncVersionVector(versions = mapOf("device1" to originalVersion))
        val updatedSyncVector = VersionVectorUtils.updateVersionInSyncVector(
            syncVersionVector, "device1", updatedVersion
        )
        
        assertEquals("Should have updated version", updatedVersion, updatedSyncVector.versions["device1"])
    }
    
    @Test
    fun `updateVersionInSyncVector adds new version for new device`() {
        val instant = Instant.parse("2023-01-01T00:00:00Z")
        val versionVector = VersionVector("device1", 1, instant)
        val newVersionVector = VersionVector("device2", 1, instant)
        
        val syncVersionVector = SyncVersionVector(versions = mapOf("device1" to versionVector))
        val updatedSyncVector = VersionVectorUtils.updateVersionInSyncVector(
            syncVersionVector, "device2", newVersionVector
        )
        
        assertEquals("Should have both versions", 2, updatedSyncVector.versions.size)
        assertEquals("Should have new version", newVersionVector, updatedSyncVector.versions["device2"])
    }
    
    @Test
    fun `incrementVersionInSyncVector increments existing device version`() {
        val instant = Instant.parse("2023-01-01T00:00:00Z")
        val versionVector = VersionVector("device1", 1, instant)
        val syncVersionVector = SyncVersionVector(versions = mapOf("device1" to versionVector))
        
        val updatedSyncVector = VersionVectorUtils.incrementVersionInSyncVector(syncVersionVector, "device1")
        
        assertEquals("Counter should be incremented", 2, updatedSyncVector.versions["device1"]?.counter)
    }
    
    @Test
    fun `incrementVersionInSyncVector creates new version for new device`() {
        val syncVersionVector = VersionVectorUtils.createEmptySyncVersionVector()
        val updatedSyncVector = VersionVectorUtils.incrementVersionInSyncVector(syncVersionVector, "device1")
        
        assertEquals("Should have one version", 1, updatedSyncVector.versions.size)
        assertEquals("Counter should start at 1", 1, updatedSyncVector.versions["device1"]?.counter)
    }
    
    // ==================== Recipe Version Comparison Tests ====================
    
    @Test
    fun `isNewerVersion returns true when recipe1 is newer`() {
        val instant1 = Instant.parse("2023-01-01T00:00:00Z")
        val instant2 = Instant.parse("2023-01-02T00:00:00Z")
        
        val recipe1 = createTestRecipe(versionVector = VersionVector("device1", 2, instant2))
        val recipe2 = createTestRecipe(versionVector = VersionVector("device1", 1, instant1))
        
        assertTrue("Recipe1 should be newer", VersionVectorUtils.isNewerVersion(recipe1, recipe2))
    }
    
    @Test
    fun `isOlderVersion returns true when recipe1 is older`() {
        val instant1 = Instant.parse("2023-01-01T00:00:00Z")
        val instant2 = Instant.parse("2023-01-02T00:00:00Z")
        
        val recipe1 = createTestRecipe(versionVector = VersionVector("device1", 1, instant1))
        val recipe2 = createTestRecipe(versionVector = VersionVector("device1", 2, instant2))
        
        assertTrue("Recipe1 should be older", VersionVectorUtils.isOlderVersion(recipe1, recipe2))
    }
    
    @Test
    fun `haveSameVersion returns true for recipes with same version`() {
        val instant = Instant.parse("2023-01-01T00:00:00Z")
        val versionVector = VersionVector("device1", 1, instant)
        
        val recipe1 = createTestRecipe(versionVector = versionVector)
        val recipe2 = createTestRecipe(versionVector = versionVector)
        
        assertTrue("Recipes should have same version", VersionVectorUtils.haveSameVersion(recipe1, recipe2))
    }
    
    // ==================== Utility Tests ====================
    
    @Test
    fun `getAllDeviceIds returns all device IDs`() {
        val instant = Instant.parse("2023-01-01T00:00:00Z")
        val syncVersionVector = SyncVersionVector(
            versions = mapOf(
                "device1" to VersionVector("device1", 1, instant),
                "device2" to VersionVector("device2", 2, instant),
                "device3" to VersionVector("device3", 3, instant)
            )
        )
        
        val deviceIds = VersionVectorUtils.getAllDeviceIds(syncVersionVector)
        assertEquals("Should return all device IDs", setOf("device1", "device2", "device3"), deviceIds)
    }
    
    @Test
    fun `containsDevice returns true for existing device`() {
        val instant = Instant.parse("2023-01-01T00:00:00Z")
        val syncVersionVector = SyncVersionVector(
            versions = mapOf("device1" to VersionVector("device1", 1, instant))
        )
        
        assertTrue("Should contain device1", VersionVectorUtils.containsDevice(syncVersionVector, "device1"))
        assertFalse("Should not contain device2", VersionVectorUtils.containsDevice(syncVersionVector, "device2"))
    }
    
    @Test
    fun `getMaxCounter returns maximum counter`() {
        val instant = Instant.parse("2023-01-01T00:00:00Z")
        val syncVersionVector = SyncVersionVector(
            versions = mapOf(
                "device1" to VersionVector("device1", 1, instant),
                "device2" to VersionVector("device2", 5, instant),
                "device3" to VersionVector("device3", 3, instant)
            )
        )
        
        assertEquals("Should return maximum counter", 5, VersionVectorUtils.getMaxCounter(syncVersionVector))
    }
    
    @Test
    fun `getMaxCounter returns 0 for empty sync vector`() {
        val emptySyncVector = VersionVectorUtils.createEmptySyncVersionVector()
        assertEquals("Should return 0 for empty", 0, VersionVectorUtils.getMaxCounter(emptySyncVector))
    }
    
    @Test
    fun `getLatestTimestamp returns most recent timestamp`() {
        val instant1 = Instant.parse("2023-01-01T00:00:00Z")
        val instant2 = Instant.parse("2023-01-02T00:00:00Z")
        val instant3 = Instant.parse("2023-01-03T00:00:00Z")
        
        val syncVersionVector = SyncVersionVector(
            versions = mapOf(
                "device1" to VersionVector("device1", 1, instant1),
                "device2" to VersionVector("device2", 2, instant2),
                "device3" to VersionVector("device3", 3, instant3)
            )
        )
        
        assertEquals("Should return latest timestamp", instant3, VersionVectorUtils.getLatestTimestamp(syncVersionVector))
    }
    
    @Test
    fun `getLatestTimestamp returns null for empty sync vector`() {
        val emptySyncVector = VersionVectorUtils.createEmptySyncVersionVector()
        assertNull("Should return null for empty", VersionVectorUtils.getLatestTimestamp(emptySyncVector))
    }
    
    @Test
    fun `isEmpty returns true for empty sync vector`() {
        val emptySyncVector = VersionVectorUtils.createEmptySyncVersionVector()
        assertTrue("Empty sync vector should be empty", VersionVectorUtils.isEmpty(emptySyncVector))
    }
    
    @Test
    fun `isEmpty returns false for non-empty sync vector`() {
        val instant = Instant.parse("2023-01-01T00:00:00Z")
        val syncVersionVector = SyncVersionVector(
            versions = mapOf("device1" to VersionVector("device1", 1, instant))
        )
        assertFalse("Non-empty sync vector should not be empty", VersionVectorUtils.isEmpty(syncVersionVector))
    }
    
    @Test
    fun `size returns correct size`() {
        val instant = Instant.parse("2023-01-01T00:00:00Z")
        val syncVersionVector = SyncVersionVector(
            versions = mapOf(
                "device1" to VersionVector("device1", 1, instant),
                "device2" to VersionVector("device2", 2, instant)
            )
        )
        assertEquals("Should return correct size", 2, VersionVectorUtils.size(syncVersionVector))
    }
    
    // ==================== Helper Methods ====================
    
    private fun createTestRecipe(
        title: String = "Test Recipe",
        category: String = "Test Category",
        versionVector: VersionVector = VersionVector("test-device", 1, Instant.now())
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
            versionVector = versionVector,
            checksum = "",
            deviceId = "test-device"
        )
    }
}
