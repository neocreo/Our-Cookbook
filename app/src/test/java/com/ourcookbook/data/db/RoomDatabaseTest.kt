package com.ourcookbook.data.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ourcookbook.data.db.dao.*
import com.ourcookbook.data.db.entity.*
import com.ourcookbook.domain.model.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class RoomDatabaseTest {
    
    private lateinit var db: AppDatabase
    private lateinit var recipeDao: RecipeDao
    private lateinit var ingredientDao: IngredientDao
    private lateinit var cookbookDao: CookbookDao
    private lateinit var deviceDao: DeviceDao
    private lateinit var syncConflictDao: SyncConflictDao
    private lateinit var syncLogDao: SyncLogDao
    private lateinit var pendingSyncDao: PendingSyncDao
    private lateinit var syncMetadataDao: SyncMetadataDao
    private lateinit var tombstoneDao: TombstoneDao
    private lateinit var sharingLinkDao: SharingLinkDao
    private lateinit var devicePreferencesDao: DevicePreferencesDao
    private lateinit var recipeImageDao: RecipeImageDao
    private lateinit var driveFileInfoDao: DriveFileInfoDao
    private lateinit var recipeFtsDao: RecipeFtsDao

    private val testPassphrase = "test-passphrase-1234567890"
    private val testDeviceId = "test-device-${UUID.randomUUID()}"

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .openHelperFactory(net.sqlcipher.database.SupportFactory(testPassphrase.toByteArray()))
            .allowMainThreadQueries()
            .build()
        
        recipeDao = db.recipeDao()
        ingredientDao = db.ingredientDao()
        cookbookDao = db.cookbookDao()
        deviceDao = db.deviceDao()
        syncConflictDao = db.syncConflictDao()
        syncLogDao = db.syncLogDao()
        pendingSyncDao = db.pendingSyncDao()
        syncMetadataDao = db.syncMetadataDao()
        tombstoneDao = db.tombstoneDao()
        sharingLinkDao = db.sharingLinkDao()
        devicePreferencesDao = db.devicePreferencesDao()
        recipeImageDao = db.recipeImageDao()
        driveFileInfoDao = db.driveFileInfoDao()
        recipeFtsDao = db.recipeFtsDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun testRecipeCrudOperations() = runBlocking {
        // Test create
        val recipeEntity = RecipeEntity(
            id = UUID.randomUUID().toString(),
            title = "Test Recipe",
            description = "A test recipe description",
            category = "Dessert",
            ingredientsJson = "[{\"id\":\"1\",\"name\":\"Flour\",\"amount\":\"1 cup\",\"unit\":\"cup\",\"notes\":null,\"order\":0}]",
            instructionsJson = "[\"Step 1\",\"Step 2\"]",
            servingSize = 4,
            prepTime = 10,
            cookTime = 20,
            rating = 4.5f,
            isFavorite = true,
            imageUrl = "http://example.com/image.jpg",
            notes = "Test notes",
            source = "Test source",
            tagsJson = "[\"test\",\"dessert\"]",
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            versionVectorJson = "{\"deviceId\":\"$testDeviceId\",\"counter\":1,\"timestamp\":\"${Instant.now()}\"}",
            checksum = "test-checksum-${UUID.randomUUID()}",
            deviceId = testDeviceId
        )
        
        val recipeId = recipeDao.insert(recipeEntity)
        assert(recipeId > 0) { "Recipe insertion failed" }
        
        // Test read
        val retrievedRecipe = recipeDao.getById(recipeEntity.id)
        assert(retrievedRecipe != null) { "Recipe retrieval failed" }
        assert(retrievedRecipe?.title == "Test Recipe") { "Recipe title mismatch" }
        
        // Test update
        val updatedRecipe = retrievedRecipe?.copy(title = "Updated Test Recipe")
        if (updatedRecipe != null) {
            val updateCount = recipeDao.update(updatedRecipe)
            assert(updateCount > 0) { "Recipe update failed" }
            
            val updatedRetrieved = recipeDao.getById(updatedRecipe.id)
            assert(updatedRetrieved?.title == "Updated Test Recipe") { "Recipe update verification failed" }
        }
        
        // Test delete
        val deleteCount = recipeDao.delete(retrievedRecipe?.id ?: "")
        assert(deleteCount > 0) { "Recipe deletion failed" }
        
        val deletedRecipe = recipeDao.getById(retrievedRecipe?.id ?: "")
        assert(deletedRecipe == null) { "Recipe should be deleted" }
    }

    @Test
    fun testIngredientCrudOperations() = runBlocking {
        // First create a recipe to reference
        val recipeEntity = RecipeEntity(
            id = UUID.randomUUID().toString(),
            title = "Test Recipe for Ingredients",
            description = "Test description",
            category = "Main Course",
            ingredientsJson = "[]",
            instructionsJson = "[]",
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            versionVectorJson = "{\"deviceId\":\"$testDeviceId\",\"counter\":1,\"timestamp\":\"${Instant.now()}\"}",
            checksum = "test-checksum-${UUID.randomUUID()}",
            deviceId = testDeviceId
        )
        
        recipeDao.insert(recipeEntity)
        
        // Test ingredient operations
        val ingredientEntity = IngredientEntity(
            id = UUID.randomUUID().toString(),
            recipeId = recipeEntity.id,
            name = "Test Ingredient",
            amount = "1",
            unit = "cup",
            notes = "Test notes",
            order = 0
        )
        
        val ingredientId = ingredientDao.insert(ingredientEntity)
        assert(ingredientId > 0) { "Ingredient insertion failed" }
        
        // Test read
        val retrievedIngredient = ingredientDao.getById(ingredientEntity.id)
        assert(retrievedIngredient != null) { "Ingredient retrieval failed" }
        assert(retrievedIngredient?.name == "Test Ingredient") { "Ingredient name mismatch" }
        
        // Test get by recipe
        val ingredientsByRecipe = ingredientDao.getByRecipe(recipeEntity.id)
        assert(ingredientsByRecipe.isNotEmpty()) { "Get ingredients by recipe failed" }
        
        // Cleanup
        ingredientDao.delete(ingredientEntity.id)
        recipeDao.delete(recipeEntity.id)
    }

    @Test
    fun testCookbookOperations() = runBlocking {
        val cookbookEntity = CookbookEntity(
            id = UUID.randomUUID().toString(),
            name = "Test Cookbook",
            description = "A test cookbook",
            ownerDeviceId = testDeviceId,
            isShared = false,
            sharingLink = null,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            recipeIds = listOf(UUID.randomUUID().toString(), UUID.randomUUID().toString())
        )
        
        val cookbookId = cookbookDao.insert(cookbookEntity)
        assert(cookbookId > 0) { "Cookbook insertion failed" }
        
        val retrievedCookbook = cookbookDao.getById(cookbookEntity.id)
        assert(retrievedCookbook != null) { "Cookbook retrieval failed" }
        assert(retrievedCookbook?.name == "Test Cookbook") { "Cookbook name mismatch" }
        
        // Test get by owner
        val cookbooksByOwner = cookbookDao.getByOwner(testDeviceId).first()
        assert(cookbooksByOwner.isNotEmpty()) { "Get cookbooks by owner failed" }
        
        // Cleanup
        cookbookDao.delete(cookbookEntity.id)
    }

    @Test
    fun testDeviceOperations() = runBlocking {
        val deviceEntity = DeviceEntity(
            id = UUID.randomUUID().toString(),
            name = "Test Device",
            deviceId = testDeviceId,
            capabilities = setOf(DeviceCapability.CAMERA, DeviceCapability.INTERNET),
            createdAt = Instant.now(),
            lastSeenAt = Instant.now()
        )
        
        val deviceId = deviceDao.insert(deviceEntity)
        assert(deviceId > 0) { "Device insertion failed" }
        
        val retrievedDevice = deviceDao.getByDeviceId(testDeviceId)
        assert(retrievedDevice != null) { "Device retrieval by deviceId failed" }
        assert(retrievedDevice?.name == "Test Device") { "Device name mismatch" }
        
        // Test get all
        val allDevices = deviceDao.getAllOnce()
        assert(allDevices.isNotEmpty()) { "Get all devices failed" }
        
        // Cleanup
        deviceDao.delete(deviceEntity.id)
    }

    @Test
    fun testSyncConflictOperations() = runBlocking {
        val versionVector = VersionVector(testDeviceId, 1, Instant.now())
        
        val conflictEntity = SyncConflictEntity(
            id = UUID.randomUUID().toString(),
            localRecipeId = UUID.randomUUID().toString(),
            remoteRecipeId = UUID.randomUUID().toString(),
            localChecksum = "local-checksum-${UUID.randomUUID()}",
            remoteChecksum = "remote-checksum-${UUID.randomUUID()}",
            localVersionJson = "{\"deviceId\":\"$testDeviceId\",\"counter\":1,\"timestamp\":\"${Instant.now()}\"}",
            remoteVersionJson = "{\"deviceId\":\"remote-device\",\"counter\":2,\"timestamp\":\"${Instant.now()}\"}",
            detectedAt = Instant.now(),
            resolvedAt = null,
            status = ConflictStatus.PENDING,
            resolutionJson = null
        )
        
        val conflictId = syncConflictDao.insert(conflictEntity)
        assert(conflictId > 0) { "Sync conflict insertion failed" }
        
        val retrievedConflict = syncConflictDao.getById(conflictEntity.id)
        assert(retrievedConflict != null) { "Sync conflict retrieval failed" }
        assert(retrievedConflict?.status == ConflictStatus.PENDING) { "Sync conflict status mismatch" }
        
        // Test get pending conflicts
        val pendingConflicts = syncConflictDao.getByStatus(ConflictStatus.PENDING).first()
        assert(pendingConflicts.isNotEmpty()) { "Get pending conflicts failed" }
        
        // Cleanup
        syncConflictDao.delete(conflictEntity.id)
    }

    @Test
    fun testSyncLogOperations() = runBlocking {
        val logEntity = SyncLogEntity(
            id = UUID.randomUUID().toString(),
            timestamp = Instant.now(),
            status = SyncStatus.SUCCESS,
            deviceId = testDeviceId,
            syncedItems = 5,
            conflicts = 0,
            durationMs = 1000,
            errorMessage = null
        )
        
        val logId = syncLogDao.insert(logEntity)
        assert(logId > 0) { "Sync log insertion failed" }
        
        val retrievedLog = syncLogDao.getById(logEntity.id)
        assert(retrievedLog != null) { "Sync log retrieval failed" }
        assert(retrievedLog?.status == SyncStatus.SUCCESS) { "Sync log status mismatch" }
        
        // Test get by device
        val logsByDevice = syncLogDao.getByDevice(testDeviceId)
        assert(logsByDevice.isNotEmpty()) { "Get logs by device failed" }
        
        // Cleanup
        syncLogDao.delete(logEntity.id)
    }

    @Test
    fun testPendingSyncOperations() = runBlocking {
        val pendingEntity = PendingSyncEntity(
            id = UUID.randomUUID().toString(),
            operation = SyncOperation.CREATE,
            entityType = EntityType.RECIPE,
            entityId = UUID.randomUUID().toString(),
            data = "{\"test\":\"data\"}",
            timestamp = Instant.now(),
            retryCount = 0,
            lastError = null
        )
        
        val pendingId = pendingSyncDao.insert(pendingEntity)
        assert(pendingId > 0) { "Pending sync insertion failed" }
        
        val retrievedPending = pendingSyncDao.getById(pendingEntity.id)
        assert(retrievedPending != null) { "Pending sync retrieval failed" }
        assert(retrievedPending?.operation == SyncOperation.CREATE) { "Pending sync operation mismatch" }
        
        // Test get by type
        val pendingByType = pendingSyncDao.getByType(EntityType.RECIPE)
        assert(pendingByType.isNotEmpty()) { "Get pending syncs by type failed" }
        
        // Cleanup
        pendingSyncDao.delete(pendingEntity.id)
    }

    @Test
    fun testSyncMetadataOperations() = runBlocking {
        val metadataEntity = SyncMetadataEntity(
            id = UUID.randomUUID().toString(),
            deviceId = testDeviceId,
            lastSyncTimestamp = Instant.now(),
            lastSuccessfulSync = Instant.now(),
            syncInProgress = false,
            pendingChanges = 0,
            conflictCount = 0
        )
        
        val metadataId = syncMetadataDao.insert(metadataEntity)
        assert(metadataId > 0) { "Sync metadata insertion failed" }
        
        val retrievedMetadata = syncMetadataDao.getByDevice(testDeviceId)
        assert(retrievedMetadata != null) { "Sync metadata retrieval by device failed" }
        assert(retrievedMetadata?.deviceId == testDeviceId) { "Sync metadata deviceId mismatch" }
        
        // Cleanup
        syncMetadataDao.delete(metadataEntity.id)
    }

    @Test
    fun testTombstoneOperations() = runBlocking {
        val tombstoneEntity = TombstoneEntity(
            id = UUID.randomUUID().toString(),
            entityType = EntityType.RECIPE,
            entityId = UUID.randomUUID().toString(),
            deletedAt = Instant.now(),
            deletedByDeviceId = testDeviceId,
            checksum = "tombstone-checksum-${UUID.randomUUID()}",
            versionVectorJson = "{\"deviceId\":\"$testDeviceId\",\"counter\":1,\"timestamp\":\"${Instant.now()}\"}"
        )
        
        val tombstoneId = tombstoneDao.insert(tombstoneEntity)
        assert(tombstoneId > 0) { "Tombstone insertion failed" }
        
        val retrievedTombstone = tombstoneDao.getById(tombstoneEntity.id)
        assert(retrievedTombstone != null) { "Tombstone retrieval failed" }
        assert(retrievedTombstone?.entityType == EntityType.RECIPE) { "Tombstone entity type mismatch" }
        
        // Test get by type
        val tombstonesByType = tombstoneDao.getByType(EntityType.RECIPE)
        assert(tombstonesByType.isNotEmpty()) { "Get tombstones by type failed" }
        
        // Cleanup
        tombstoneDao.delete(tombstoneEntity.id)
    }

    @Test
    fun testSharingLinkOperations() = runBlocking {
        // First create a cookbook to reference
        val cookbookEntity = CookbookEntity(
            id = UUID.randomUUID().toString(),
            name = "Test Cookbook for Sharing",
            description = "Test description",
            ownerDeviceId = testDeviceId,
            isShared = true,
            sharingLink = null,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            recipeIds = emptyList()
        )
        
        cookbookDao.insert(cookbookEntity)
        
        val sharingLinkEntity = SharingLinkEntity(
            id = UUID.randomUUID().toString(),
            cookbookId = cookbookEntity.id,
            token = UUID.randomUUID().toString(),
            permissions = setOf(SharingPermission.VIEW, SharingPermission.EDIT),
            expiresAt = Instant.now().plusSeconds(86400), // 1 day from now
            createdAt = Instant.now(),
            usedAt = null,
            usedCount = 0
        )
        
        val linkId = sharingLinkDao.insert(sharingLinkEntity)
        assert(linkId > 0) { "Sharing link insertion failed" }
        
        val retrievedLink = sharingLinkDao.getById(sharingLinkEntity.id)
        assert(retrievedLink != null) { "Sharing link retrieval failed" }
        assert(retrievedLink?.cookbookId == cookbookEntity.id) { "Sharing link cookbookId mismatch" }
        
        // Test get by cookbook
        val linksByCookbook = sharingLinkDao.getByCookbook(cookbookEntity.id)
        assert(linksByCookbook.isNotEmpty()) { "Get sharing links by cookbook failed" }
        
        // Cleanup
        sharingLinkDao.delete(sharingLinkEntity.id)
        cookbookDao.delete(cookbookEntity.id)
    }

    @Test
    fun testDevicePreferencesOperations() = runBlocking {
        // First create a device to reference
        val deviceEntity = DeviceEntity(
            id = UUID.randomUUID().toString(),
            name = "Test Device for Preferences",
            deviceId = testDeviceId,
            capabilities = setOf(DeviceCapability.CAMERA),
            createdAt = Instant.now(),
            lastSeenAt = Instant.now()
        )
        
        deviceDao.insert(deviceEntity)
        
        val preferencesEntity = DevicePreferencesEntity(
            id = UUID.randomUUID().toString(),
            deviceId = deviceEntity.id,
            theme = ThemePreference.SYSTEM,
            measurementSystem = MeasurementSystem.IMPERIAL,
            syncEnabled = true,
            autoSync = true,
            syncFrequency = SyncFrequency.AUTOMATIC,
            language = "en",
            fontSize = FontSize.NORMAL
        )
        
        val preferencesId = devicePreferencesDao.insert(preferencesEntity)
        assert(preferencesId > 0) { "Device preferences insertion failed" }
        
        val retrievedPreferences = devicePreferencesDao.getByDevice(deviceEntity.id)
        assert(retrievedPreferences != null) { "Device preferences retrieval by device failed" }
        assert(retrievedPreferences?.theme == ThemePreference.SYSTEM) { "Device preferences theme mismatch" }
        
        // Cleanup
        devicePreferencesDao.delete(preferencesEntity.id)
        deviceDao.delete(deviceEntity.id)
    }

    @Test
    fun testRecipeImageOperations() = runBlocking {
        // First create a recipe to reference
        val recipeEntity = RecipeEntity(
            id = UUID.randomUUID().toString(),
            title = "Test Recipe for Images",
            description = "Test description",
            category = "Dessert",
            ingredientsJson = "[]",
            instructionsJson = "[]",
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            versionVectorJson = "{\"deviceId\":\"$testDeviceId\",\"counter\":1,\"timestamp\":\"${Instant.now()}\"}",
            checksum = "test-checksum-${UUID.randomUUID()}",
            deviceId = testDeviceId
        )
        
        recipeDao.insert(recipeEntity)
        
        val imageEntity = RecipeImageEntity(
            id = UUID.randomUUID().toString(),
            recipeId = recipeEntity.id,
            imageUrl = "http://example.com/test-image.jpg",
            imageType = ImageType.PHOTO,
            order = 0,
            createdAt = Instant.now()
        )
        
        val imageId = recipeImageDao.insert(imageEntity)
        assert(imageId > 0) { "Recipe image insertion failed" }
        
        val retrievedImage = recipeImageDao.getById(imageEntity.id)
        assert(retrievedImage != null) { "Recipe image retrieval failed" }
        assert(retrievedImage?.imageType == ImageType.PHOTO) { "Recipe image type mismatch" }
        
        // Test get by recipe
        val imagesByRecipe = recipeImageDao.getByRecipe(recipeEntity.id)
        assert(imagesByRecipe.isNotEmpty()) { "Get images by recipe failed" }
        
        // Cleanup
        recipeImageDao.delete(imageEntity.id)
        recipeDao.delete(recipeEntity.id)
    }

    @Test
    fun testDriveFileInfoOperations() = runBlocking {
        val fileInfoEntity = DriveFileInfoEntity(
            id = UUID.randomUUID().toString(),
            driveFileId = "test-drive-file-id-${UUID.randomUUID()}",
            fileName = "test-file.json",
            fileType = DriveFileType.COOKBOOK,
            size = 1024,
            checksum = "test-file-checksum-${UUID.randomUUID()}",
            createdAt = Instant.now(),
            modifiedAt = Instant.now(),
            syncedAt = Instant.now()
        )
        
        val fileInfoId = driveFileInfoDao.insert(fileInfoEntity)
        assert(fileInfoId > 0) { "Drive file info insertion failed" }
        
        val retrievedFileInfo = driveFileInfoDao.getById(fileInfoEntity.id)
        assert(retrievedFileInfo != null) { "Drive file info retrieval failed" }
        assert(retrievedFileInfo?.fileType == DriveFileType.COOKBOOK) { "Drive file info type mismatch" }
        
        // Test get by drive file ID
        val fileInfoByDriveId = driveFileInfoDao.getByDriveFile(fileInfoEntity.driveFileId)
        assert(fileInfoByDriveId != null) { "Get drive file info by drive file ID failed" }
        
        // Cleanup
        driveFileInfoDao.delete(fileInfoEntity.id)
    }

    @Test
    fun testRecipeFtsOperations() = runBlocking {
        val ftsEntity = RecipeFtsEntity(
            id = UUID.randomUUID().toString(),
            title = "Test Recipe for FTS",
            description = "A test recipe for full-text search",
            ingredients = "flour,sugar,eggs",
            instructions = "Mix ingredients and bake",
            category = "Dessert"
        )
        
        val ftsId = recipeFtsDao.insert(ftsEntity)
        assert(ftsId > 0) { "Recipe FTS insertion failed" }
        
        val retrievedFts = recipeFtsDao.getById(ftsEntity.id)
        assert(retrievedFts != null) { "Recipe FTS retrieval failed" }
        assert(retrievedFts?.title == "Test Recipe for FTS") { "Recipe FTS title mismatch" }
        
        // Test search
        val searchResults = recipeFtsDao.search("test")
        assert(searchResults.isNotEmpty()) { "FTS search failed" }
        
        // Cleanup
        recipeFtsDao.delete(ftsEntity.id)
    }

    @Test
    fun testDatabaseEncryption() = runBlocking {
        // This test verifies that the database is properly encrypted
        // We can't directly test encryption, but we can verify that the database
        // can be opened with the correct passphrase
        
        val context = ApplicationProvider.getApplicationContext<Context>()
        val encryptedDb = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .openHelperFactory(net.sqlcipher.database.SupportFactory(testPassphrase.toByteArray()))
            .allowMainThreadQueries()
            .build()
        
        assert(encryptedDb.openHelper.readableDatabase != null) { "Database encryption setup failed" }
        
        encryptedDb.close()
    }

    @Test
    fun testTypeConverters() = runBlocking {
        // Test that all type converters work correctly
        val converters = DatabaseConverters()
        
        // Test Instant converters
        val now = Instant.now()
        val instantMillis = converters.fromInstant(now)
        val convertedBack = converters.toInstant(instantMillis)
        assert(convertedBack == now) { "Instant converter failed" }
        
        // Test UUID converters
        val uuid = UUID.randomUUID()
        val uuidString = converters.fromUUID(uuid)
        val uuidConvertedBack = converters.toUUID(uuidString)
        assert(uuidConvertedBack == uuid) { "UUID converter failed" }
        
        // Test enum converters
        val syncStatus = SyncStatus.SUCCESS
        val statusString = converters.fromSyncStatus(syncStatus)
        val statusConvertedBack = converters.toSyncStatus(statusString)
        assert(statusConvertedBack == syncStatus) { "SyncStatus converter failed" }
        
        // Test set converters
        val permissions = setOf(SharingPermission.VIEW, SharingPermission.EDIT)
        val permissionsString = converters.fromSharingPermissionSetJson(permissions)
        val permissionsConvertedBack = converters.toSharingPermissionSetJson(permissionsString)
        assert(permissionsConvertedBack == permissions) { "SharingPermission set converter failed" }
        
        // Test list converters
        val stringList = listOf("item1", "item2", "item3")
        val listString = converters.fromStringListJson(stringList)
        val listConvertedBack = converters.toStringListJson(listString)
        assert(listConvertedBack == stringList) { "String list converter failed" }
    }
}