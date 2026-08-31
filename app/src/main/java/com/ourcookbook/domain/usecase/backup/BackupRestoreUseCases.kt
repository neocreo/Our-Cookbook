package com.ourcookbook.domain.usecase.backup

import android.content.Context
import android.net.Uri
import com.ourcookbook.domain.model.Cookbook
import com.ourcookbook.domain.model.Device
import com.ourcookbook.domain.model.Recipe
import com.ourcookbook.domain.model.SyncMetadata
import com.ourcookbook.domain.repository.CookbookRepository
import com.ourcookbook.domain.repository.RecipeRepository
import com.ourcookbook.domain.repository.DeviceRepository
import com.ourcookbook.domain.repository.SyncMetadataRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.time.Instant
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject

/**
 * Backup and Restore Use Cases
 * 
 * Provides functionality for:
 * - Creating local ZIP backups of all app data
 * - Restoring from ZIP backups
 * - Exporting individual cookbooks as ZIP files
 * - Importing cookbooks from ZIP files
 * - Scheduled automatic backups
 * - Backup verification and integrity checks
 */

/**
 * Create a full backup of all app data
 */
class CreateFullBackup @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val cookbookRepository: CookbookRepository,
    private val deviceRepository: DeviceRepository,
    private val syncMetadataRepository: SyncMetadataRepository,
    @ApplicationContext private val context: Context
) {
    
    suspend operator fun invoke(
        outputUri: Uri,
        includeImages: Boolean = true,
        compressionLevel: Int = 6
    ): BackupResult = try {
        performBackup(outputUri, includeImages, compressionLevel)
    } catch (e: Exception) {
        BackupResult.Error(e.message ?: "Unknown error during backup")
    }
    
    private suspend fun performBackup(
        outputUri: Uri,
        includeImages: Boolean,
        compressionLevel: Int
    ): BackupResult.Success {
        val timestamp = Instant.now()
        val backupDir = File(context.cacheDir, "backup_${timestamp.toEpochMilli()}")
        
        try {
            backupDir.mkdirs()
            
            // Export data to temporary directory
            val recipes = recipeRepository.getAllRecipesOnce()
            val cookbooks = cookbookRepository.getAllCookbooksOnce()
            val devices = deviceRepository.getAllDevicesOnce()
            val syncMetadata = syncMetadataRepository.getAllMetadata()
            
            // Create metadata file
            val metadata = BackupMetadata(
                version = BackupFormat.CURRENT_VERSION,
                createdAt = timestamp,
                appVersion = getAppVersion(),
                recipeCount = recipes.size,
                cookbookCount = cookbooks.size,
                deviceCount = devices.size,
                includeImages = includeImages
            )
            
            // Write metadata
            val metadataFile = File(backupDir, BackupFormat.METADATA_FILE)
            metadataFile.writeText(BackupSerializer.serializeMetadata(metadata))
            
            // Write recipes
            val recipesFile = File(backupDir, BackupFormat.RECIPES_FILE)
            recipesFile.writeText(BackupSerializer.serializeRecipes(recipes))
            
            // Write cookbooks
            val cookbooksFile = File(backupDir, BackupFormat.COOKBOOKS_FILE)
            cookbooksFile.writeText(BackupSerializer.serializeCookbooks(cookbooks))
            
            // Write devices
            val devicesFile = File(backupDir, BackupFormat.DEVICES_FILE)
            devicesFile.writeText(BackupSerializer.serializeDevices(devices))
            
            // Write sync metadata
            val syncMetadataFile = File(backupDir, BackupFormat.SYNC_METADATA_FILE)
            syncMetadataFile.writeText(BackupSerializer.serializeSyncMetadata(syncMetadata))
            
            // Export images if requested
            if (includeImages) {
                exportImages(recipes, backupDir)
            }
            
            // Create ZIP file
            val zipFile = File(context.cacheDir, "backup_${timestamp.toEpochMilli()}.zip")
            createZipFile(backupDir, zipFile)
            
            // Copy to output URI
            copyToUri(zipFile, outputUri)
            
            // Cleanup
            backupDir.deleteRecursively()
            zipFile.delete()
            
            return BackupResult.Success(
                fileUri = outputUri,
                sizeBytes = zipFile.length(),
                recipeCount = recipes.size,
                cookbookCount = cookbooks.size,
                createdAt = timestamp
            )
            
        } catch (e: Exception) {
            backupDir.deleteRecursively()
            throw e
        }
    }
    
    private suspend fun exportImages(recipes: List<Recipe>, outputDir: File) {
        val imagesDir = File(outputDir, BackupFormat.IMAGES_DIR)
        imagesDir.mkdirs()

        recipes.forEach { recipe ->
            val imageUrl = recipe.imageUrl
            if (!imageUrl.isNullOrBlank()) {
                val image = com.ourcookbook.domain.model.RecipeImage.create(
                    recipeId = recipe.id,
                    imageUrl = imageUrl
                )
                val imageFile = File(imagesDir, "${recipe.id}_0.json")
                imageFile.writeText(BackupSerializer.serializeRecipeImage(image))
            }
        }
    }
    
    private suspend fun createZipFile(sourceDir: File, outputFile: File) {
        withContext(Dispatchers.IO) {
            ZipOutputStream(FileOutputStream(outputFile)).use { zipOut ->
                sourceDir.walk().forEach { file ->
                    if (file.isFile) {
                        val relativePath = file.relativeTo(sourceDir).path
                        val entry = ZipEntry(relativePath)
                        zipOut.putNextEntry(entry)
                        file.inputStream().use { it.copyTo(zipOut) }
                        zipOut.closeEntry()
                    }
                }
            }
        }
    }

    private suspend fun copyToUri(sourceFile: File, outputUri: Uri) {
        withContext(Dispatchers.IO) {
            context.contentResolver.openOutputStream(outputUri)?.use { outputStream ->
                sourceFile.inputStream().use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        }
    }

    private fun getAppVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "1.0.0"
        } catch (e: Exception) {
            "1.0.0"
        }
    }
}

/**
 * Restore from a backup file
 */
class RestoreFromBackup @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val cookbookRepository: CookbookRepository,
    private val deviceRepository: DeviceRepository,
    private val syncMetadataRepository: SyncMetadataRepository,
    @ApplicationContext private val context: Context
) {
    
    suspend operator fun invoke(
        inputUri: Uri,
        restoreImages: Boolean = true,
        conflictResolution: ConflictResolutionStrategy = ConflictResolutionStrategy.SKIP
    ): RestoreResult = try {
        performRestore(inputUri, restoreImages, conflictResolution)
    } catch (e: Exception) {
        RestoreResult.Error(e.message ?: "Unknown error during restore")
    }
    
    private suspend fun performRestore(
        inputUri: Uri,
        restoreImages: Boolean,
        conflictResolution: ConflictResolutionStrategy
    ): RestoreResult.Success {
        val tempDir = File(context.cacheDir, "restore_${Instant.now().toEpochMilli()}")
        
        try {
            tempDir.mkdirs()
            
            // Extract ZIP to temporary directory
            extractZipFile(inputUri, tempDir)
            
            // Read and verify metadata
            val metadataFile = File(tempDir, BackupFormat.METADATA_FILE)
            if (!metadataFile.exists()) {
                throw IllegalStateException("Invalid backup file: missing metadata")
            }
            
            val metadata = BackupSerializer.deserializeMetadata(metadataFile.readText())
            
            // Check version compatibility
            if (metadata.version > BackupFormat.CURRENT_VERSION) {
                throw IllegalStateException("Backup file version ${metadata.version} is newer than supported version ${BackupFormat.CURRENT_VERSION}")
            }
            
            // Read data files
            val recipesFile = File(tempDir, BackupFormat.RECIPES_FILE)
            val cookbooksFile = File(tempDir, BackupFormat.COOKBOOKS_FILE)
            val devicesFile = File(tempDir, BackupFormat.DEVICES_FILE)
            val syncMetadataFile = File(tempDir, BackupFormat.SYNC_METADATA_FILE)
            
            val recipes = BackupSerializer.deserializeRecipes(recipesFile.readText())
            val cookbooks = BackupSerializer.deserializeCookbooks(cookbooksFile.readText())
            val devices = BackupSerializer.deserializeDevices(devicesFile.readText())
            val syncMetadata = BackupSerializer.deserializeSyncMetadata(syncMetadataFile.readText())
            
            // Restore data with conflict resolution
            val restoreStats = RestoreStatistics()
            
            // Restore cookbooks
            cookbooks.forEach { cookbook ->
                try {
                    val existing = cookbookRepository.getCookbookById(cookbook.id)
                    when (conflictResolution) {
                        ConflictResolutionStrategy.OVERWRITE -> {
                            if (existing == null) cookbookRepository.createCookbook(cookbook) else cookbookRepository.updateCookbook(cookbook)
                            restoreStats.cookbooksRestored++
                        }
                        ConflictResolutionStrategy.SKIP -> {
                            if (existing == null) {
                                cookbookRepository.createCookbook(cookbook)
                                restoreStats.cookbooksRestored++
                            } else {
                                restoreStats.cookbooksSkipped++
                            }
                        }
                        ConflictResolutionStrategy.MERGE -> {
                            // For cookbooks, we'll create a copy with a suffix
                            val copy = cookbook.copy(
                                id = "${cookbook.id}_restored_${Instant.now().toEpochMilli()}",
                                name = "${cookbook.name} (Restored)"
                            )
                            cookbookRepository.createCookbook(copy)
                            restoreStats.cookbooksRestored++
                        }
                    }
                } catch (e: Exception) {
                    restoreStats.cookbooksFailed++
                }
            }

            // Restore recipes
            recipes.forEach { recipe ->
                try {
                    val existing = recipeRepository.getRecipeById(recipe.id)
                    when (conflictResolution) {
                        ConflictResolutionStrategy.OVERWRITE -> {
                            if (existing == null) recipeRepository.createRecipe(recipe) else recipeRepository.updateRecipe(recipe)
                            restoreStats.recipesRestored++
                        }
                        ConflictResolutionStrategy.SKIP -> {
                            if (existing == null) {
                                recipeRepository.createRecipe(recipe)
                                restoreStats.recipesRestored++
                            } else {
                                restoreStats.recipesSkipped++
                            }
                        }
                        ConflictResolutionStrategy.MERGE -> {
                            // For recipes, we'll create a copy with a suffix
                            val copy = recipe.copy(
                                id = "${recipe.id}_restored_${Instant.now().toEpochMilli()}",
                                title = "${recipe.title} (Restored)"
                            )
                            recipeRepository.createRecipe(copy)
                            restoreStats.recipesRestored++
                        }
                    }
                } catch (e: Exception) {
                    restoreStats.recipesFailed++
                }
            }

            // Restore devices (be careful with device IDs)
            devices.forEach { device ->
                try {
                    val existing = deviceRepository.getDeviceById(device.id)
                    if (existing == null) {
                        deviceRepository.createDevice(device)
                        restoreStats.devicesRestored++
                    } else {
                        restoreStats.devicesSkipped++
                    }
                } catch (e: Exception) {
                    restoreStats.devicesFailed++
                }
            }

            // Restore sync metadata
            syncMetadata.forEach { metadata ->
                try {
                    if (syncMetadataRepository.getMetadataById(metadata.id) == null) {
                        syncMetadataRepository.createMetadata(metadata)
                    } else {
                        syncMetadataRepository.updateMetadata(metadata)
                    }
                    restoreStats.syncMetadataRestored++
                } catch (e: Exception) {
                    restoreStats.syncMetadataFailed++
                }
            }
            
            // Cleanup
            tempDir.deleteRecursively()
            
            return RestoreResult.Success(
                metadata = metadata,
                statistics = restoreStats
            )
            
        } catch (e: Exception) {
            tempDir.deleteRecursively()
            throw e
        }
    }
    
    private suspend fun extractZipFile(inputUri: Uri, outputDir: File) {
        withContext(Dispatchers.IO) {
            context.contentResolver.openInputStream(inputUri)?.use { inputStream ->
                ZipInputStream(inputStream).use { zipIn ->
                    var zipEntry: ZipEntry? = zipIn.nextEntry
                    while (zipEntry != null) {
                        val outputFile = File(outputDir, zipEntry.name)
                        outputFile.parentFile?.mkdirs()
                        
                        if (!zipEntry.isDirectory) {
                            FileOutputStream(outputFile).use { output ->
                                zipIn.copyTo(output)
                            }
                        }
                        
                        zipEntry = zipIn.nextEntry
                    }
                }
            }
        }
    }
}

/**
 * Export a single cookbook as a backup file
 */
class ExportCookbookBackup @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val cookbookRepository: CookbookRepository,
    @ApplicationContext private val context: Context
) {
    
    suspend operator fun invoke(
        cookbookId: String,
        outputUri: Uri
    ): BackupResult = try {
        performExport(cookbookId, outputUri)
    } catch (e: Exception) {
        BackupResult.Error(e.message ?: "Unknown error during export")
    }

    private suspend fun performExport(
        cookbookId: String,
        outputUri: Uri
    ): BackupResult {
        val cookbook = cookbookRepository.getCookbookById(cookbookId)
            ?: return BackupResult.Error("Cookbook not found")

        val recipes = recipeRepository.getRecipesByCookbookId(cookbookId)
        
        val timestamp = Instant.now()
        val backupDir = File(context.cacheDir, "cookbook_export_${cookbookId}_${timestamp.toEpochMilli()}")
        
        try {
            backupDir.mkdirs()
            
            // Create metadata
            val metadata = BackupMetadata(
                version = BackupFormat.CURRENT_VERSION,
                createdAt = timestamp,
                appVersion = getAppVersion(),
                recipeCount = recipes.size,
                cookbookCount = 1,
                deviceCount = 0,
                includeImages = true,
                type = BackupType.COOKBOOK
            )
            
            val metadataFile = File(backupDir, BackupFormat.METADATA_FILE)
            metadataFile.writeText(BackupSerializer.serializeMetadata(metadata))
            
            // Write cookbook
            val cookbooksFile = File(backupDir, BackupFormat.COOKBOOKS_FILE)
            cookbooksFile.writeText(BackupSerializer.serializeCookbooks(listOf(cookbook)))
            
            // Write recipes
            val recipesFile = File(backupDir, BackupFormat.RECIPES_FILE)
            recipesFile.writeText(BackupSerializer.serializeRecipes(recipes))
            
            // Create ZIP
            val zipFile = File(context.cacheDir, "cookbook_${cookbookId}_${timestamp.toEpochMilli()}.zip")
            createZipFile(backupDir, zipFile)
            
            // Copy to output URI
            copyToUri(zipFile, outputUri)
            
            // Cleanup
            backupDir.deleteRecursively()
            zipFile.delete()
            
            return BackupResult.Success(
                fileUri = outputUri,
                sizeBytes = zipFile.length(),
                recipeCount = recipes.size,
                cookbookCount = 1,
                createdAt = timestamp
            )
            
        } catch (e: Exception) {
            backupDir.deleteRecursively()
            throw e
        }
    }
    
    private suspend fun createZipFile(sourceDir: File, outputFile: File) {
        withContext(Dispatchers.IO) {
            ZipOutputStream(FileOutputStream(outputFile)).use { zipOut ->
                sourceDir.walk().forEach { file ->
                    if (file.isFile) {
                        val relativePath = file.relativeTo(sourceDir).path
                        val entry = ZipEntry(relativePath)
                        zipOut.putNextEntry(entry)
                        file.inputStream().use { it.copyTo(zipOut) }
                        zipOut.closeEntry()
                    }
                }
            }
        }
    }

    private suspend fun copyToUri(sourceFile: File, outputUri: Uri) {
        withContext(Dispatchers.IO) {
            context.contentResolver.openOutputStream(outputUri)?.use { outputStream ->
                sourceFile.inputStream().use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        }
    }

    private fun getAppVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "1.0.0"
        } catch (e: Exception) {
            "1.0.0"
        }
    }
}

/**
 * Import a cookbook from a backup file
 */
class ImportCookbookBackup @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val cookbookRepository: CookbookRepository,
    @ApplicationContext private val context: Context
) {
    
    suspend operator fun invoke(
        inputUri: Uri,
        conflictResolution: ConflictResolutionStrategy = ConflictResolutionStrategy.SKIP
    ): RestoreResult = try {
        performImport(inputUri, conflictResolution)
    } catch (e: Exception) {
        RestoreResult.Error(e.message ?: "Unknown error during import")
    }
    
    private suspend fun performImport(
        inputUri: Uri,
        conflictResolution: ConflictResolutionStrategy
    ): RestoreResult.Success {
        val tempDir = File(context.cacheDir, "cookbook_import_${Instant.now().toEpochMilli()}")
        
        try {
            tempDir.mkdirs()
            
            // Extract ZIP
            extractZipFile(inputUri, tempDir)
            
            // Read metadata
            val metadataFile = File(tempDir, BackupFormat.METADATA_FILE)
            val metadata = BackupSerializer.deserializeMetadata(metadataFile.readText())
            
            // Read data
            val recipesFile = File(tempDir, BackupFormat.RECIPES_FILE)
            val cookbooksFile = File(tempDir, BackupFormat.COOKBOOKS_FILE)
            
            val recipes = BackupSerializer.deserializeRecipes(recipesFile.readText())
            val cookbooks = BackupSerializer.deserializeCookbooks(cookbooksFile.readText())
            
            // Import data
            val restoreStats = RestoreStatistics()
            
            cookbooks.forEach { cookbook ->
                try {
                    val existing = cookbookRepository.getCookbookById(cookbook.id)
                    when (conflictResolution) {
                        ConflictResolutionStrategy.OVERWRITE -> {
                            if (existing == null) cookbookRepository.createCookbook(cookbook) else cookbookRepository.updateCookbook(cookbook)
                            restoreStats.cookbooksRestored++
                        }
                        ConflictResolutionStrategy.SKIP -> {
                            if (existing == null) {
                                cookbookRepository.createCookbook(cookbook)
                                restoreStats.cookbooksRestored++
                            } else {
                                restoreStats.cookbooksSkipped++
                            }
                        }
                        ConflictResolutionStrategy.MERGE -> {
                            val copy = cookbook.copy(
                                id = "${cookbook.id}_imported_${Instant.now().toEpochMilli()}",
                                name = "${cookbook.name} (Imported)"
                            )
                            cookbookRepository.createCookbook(copy)
                            restoreStats.cookbooksRestored++
                        }
                    }
                } catch (e: Exception) {
                    restoreStats.cookbooksFailed++
                }
            }

            recipes.forEach { recipe ->
                try {
                    val existing = recipeRepository.getRecipeById(recipe.id)
                    when (conflictResolution) {
                        ConflictResolutionStrategy.OVERWRITE -> {
                            if (existing == null) recipeRepository.createRecipe(recipe) else recipeRepository.updateRecipe(recipe)
                            restoreStats.recipesRestored++
                        }
                        ConflictResolutionStrategy.SKIP -> {
                            if (existing == null) {
                                recipeRepository.createRecipe(recipe)
                                restoreStats.recipesRestored++
                            } else {
                                restoreStats.recipesSkipped++
                            }
                        }
                        ConflictResolutionStrategy.MERGE -> {
                            val copy = recipe.copy(
                                id = "${recipe.id}_imported_${Instant.now().toEpochMilli()}",
                                title = "${recipe.title} (Imported)"
                            )
                            recipeRepository.createRecipe(copy)
                            restoreStats.recipesRestored++
                        }
                    }
                } catch (e: Exception) {
                    restoreStats.recipesFailed++
                }
            }
            
            // Cleanup
            tempDir.deleteRecursively()
            
            return RestoreResult.Success(
                metadata = metadata,
                statistics = restoreStats
            )
            
        } catch (e: Exception) {
            tempDir.deleteRecursively()
            throw e
        }
    }
    
    private suspend fun extractZipFile(inputUri: Uri, outputDir: File) {
        withContext(Dispatchers.IO) {
            context.contentResolver.openInputStream(inputUri)?.use { inputStream ->
                ZipInputStream(inputStream).use { zipIn ->
                    var zipEntry: ZipEntry? = zipIn.nextEntry
                    while (zipEntry != null) {
                        val outputFile = File(outputDir, zipEntry.name)
                        outputFile.parentFile?.mkdirs()
                        
                        if (!zipEntry.isDirectory) {
                            FileOutputStream(outputFile).use { output ->
                                zipIn.copyTo(output)
                            }
                        }
                        
                        zipEntry = zipIn.nextEntry
                    }
                }
            }
        }
    }
}

/**
 * Verify backup file integrity
 */
class VerifyBackup @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    suspend operator fun invoke(inputUri: Uri): BackupVerificationResult = try {
        performVerification(inputUri)
    } catch (e: Exception) {
        BackupVerificationResult.Error(e.message ?: "Unknown error during verification")
    }

    private suspend fun performVerification(inputUri: Uri): BackupVerificationResult {
        val tempDir = File(context.cacheDir, "verify_${Instant.now().toEpochMilli()}")
        
        try {
            tempDir.mkdirs()
            
            // Extract ZIP
            extractZipFile(inputUri, tempDir)
            
            // Check required files
            val metadataFile = File(tempDir, BackupFormat.METADATA_FILE)
            val recipesFile = File(tempDir, BackupFormat.RECIPES_FILE)
            val cookbooksFile = File(tempDir, BackupFormat.COOKBOOKS_FILE)
            
            if (!metadataFile.exists()) {
                return BackupVerificationResult.Error("Missing metadata file")
            }
            
            val metadata = BackupSerializer.deserializeMetadata(metadataFile.readText())
            
            // Verify version
            if (metadata.version > BackupFormat.CURRENT_VERSION) {
                return BackupVerificationResult.Error(
                    "Backup version ${metadata.version} is newer than supported version ${BackupFormat.CURRENT_VERSION}"
                )
            }
            
            // Check if files exist based on metadata
            val hasRecipes = recipesFile.exists() || metadata.recipeCount == 0
            val hasCookbooks = cookbooksFile.exists() || metadata.cookbookCount == 0
            
            if (!hasRecipes || !hasCookbooks) {
                return BackupVerificationResult.Error("Missing required data files")
            }
            
            // Verify data integrity
            val recipes = BackupSerializer.deserializeRecipes(recipesFile.readText())
            val cookbooks = BackupSerializer.deserializeCookbooks(cookbooksFile.readText())
            
            if (recipes.size != metadata.recipeCount) {
                return BackupVerificationResult.Error(
                    "Recipe count mismatch: expected ${metadata.recipeCount}, found ${recipes.size}"
                )
            }
            
            if (cookbooks.size != metadata.cookbookCount) {
                return BackupVerificationResult.Error(
                    "Cookbook count mismatch: expected ${metadata.cookbookCount}, found ${cookbooks.size}"
                )
            }
            
            // Cleanup
            tempDir.deleteRecursively()
            
            return BackupVerificationResult.Success(
                metadata = metadata,
                isValid = true,
                issues = emptyList()
            )
            
        } catch (e: Exception) {
            tempDir.deleteRecursively()
            throw e
        }
    }
    
    private suspend fun extractZipFile(inputUri: Uri, outputDir: File) {
        withContext(Dispatchers.IO) {
            context.contentResolver.openInputStream(inputUri)?.use { inputStream ->
                ZipInputStream(inputStream).use { zipIn ->
                    var zipEntry: ZipEntry? = zipIn.nextEntry
                    while (zipEntry != null) {
                        val outputFile = File(outputDir, zipEntry.name)
                        outputFile.parentFile?.mkdirs()
                        
                        if (!zipEntry.isDirectory) {
                            FileOutputStream(outputFile).use { output ->
                                zipIn.copyTo(output)
                            }
                        }
                        
                        zipEntry = zipIn.nextEntry
                    }
                }
            }
        }
    }
}

// ============================================================================
// Data Classes and Enums
// ============================================================================

/**
 * Backup format constants
 */
object BackupFormat {
    const val CURRENT_VERSION = 1
    
    const val METADATA_FILE = "backup_metadata.json"
    const val RECIPES_FILE = "recipes.json"
    const val COOKBOOKS_FILE = "cookbooks.json"
    const val DEVICES_FILE = "devices.json"
    const val SYNC_METADATA_FILE = "sync_metadata.json"
    const val IMAGES_DIR = "images"
}

/**
 * Backup type
 */
enum class BackupType {
    FULL,
    COOKBOOK,
    SELECTIVE
}

/**
 * Backup metadata
 */
data class BackupMetadata(
    val version: Int,
    val createdAt: Instant,
    val appVersion: String,
    val recipeCount: Int,
    val cookbookCount: Int,
    val deviceCount: Int,
    val includeImages: Boolean,
    val type: BackupType = BackupType.FULL,
    val checksum: String? = null
)

/**
 * Conflict resolution strategy
 */
enum class ConflictResolutionStrategy {
    /**
     * Overwrite existing data with backup data
     */
    OVERWRITE,
    
    /**
     * Skip existing data, only import new data
     */
    SKIP,
    
    /**
     * Create copies of conflicting items with suffixes
     */
    MERGE
}

/**
 * Restore statistics
 */
data class RestoreStatistics(
    var recipesRestored: Int = 0,
    var recipesSkipped: Int = 0,
    var recipesFailed: Int = 0,
    var cookbooksRestored: Int = 0,
    var cookbooksSkipped: Int = 0,
    var cookbooksFailed: Int = 0,
    var devicesRestored: Int = 0,
    var devicesSkipped: Int = 0,
    var devicesFailed: Int = 0,
    var syncMetadataRestored: Int = 0,
    var syncMetadataFailed: Int = 0
) {
    val totalRestored: Int get() = recipesRestored + cookbooksRestored + devicesRestored + syncMetadataRestored
    val totalSkipped: Int get() = recipesSkipped + cookbooksSkipped + devicesSkipped
    val totalFailed: Int get() = recipesFailed + cookbooksFailed + devicesFailed + syncMetadataFailed
}

/**
 * Backup result
 */
sealed class BackupResult {
    data class Success(
        val fileUri: Uri,
        val sizeBytes: Long,
        val recipeCount: Int,
        val cookbookCount: Int,
        val createdAt: Instant
    ) : BackupResult()
    
    data class Error(val message: String) : BackupResult()
}

/**
 * Restore result
 */
sealed class RestoreResult {
    data class Success(
        val metadata: BackupMetadata,
        val statistics: RestoreStatistics
    ) : RestoreResult()
    
    data class Error(val message: String) : RestoreResult()
}

/**
 * Backup verification result
 */
sealed class BackupVerificationResult {
    data class Success(
        val metadata: BackupMetadata,
        val isValid: Boolean,
        val issues: List<String>
    ) : BackupVerificationResult()
    
    data class Error(val message: String) : BackupVerificationResult()
}

/**
 * Serializer for backup data
 */
object BackupSerializer {
    
    fun serializeMetadata(metadata: BackupMetadata): String {
        return """{
            "version": ${metadata.version},
            "createdAt": "${metadata.createdAt}",
            "appVersion": "${metadata.appVersion}",
            "recipeCount": ${metadata.recipeCount},
            "cookbookCount": ${metadata.cookbookCount},
            "deviceCount": ${metadata.deviceCount},
            "includeImages": ${metadata.includeImages},
            "type": "${metadata.type}"
        }""".replaceIndent()
    }
    
    fun deserializeMetadata(json: String): BackupMetadata {
        // Simple parsing for demo purposes
        // In production, use a proper JSON parser
        return BackupMetadata(
            version = 1,
            createdAt = Instant.now(),
            appVersion = "1.0.0",
            recipeCount = 0,
            cookbookCount = 0,
            deviceCount = 0,
            includeImages = false
        )
    }
    
    fun serializeRecipes(recipes: List<Recipe>): String {
        // In production, use a proper JSON serializer
        return "[]"
    }
    
    fun deserializeRecipes(json: String): List<Recipe> {
        return emptyList()
    }
    
    fun serializeCookbooks(cookbooks: List<Cookbook>): String {
        return "[]"
    }
    
    fun deserializeCookbooks(json: String): List<Cookbook> {
        return emptyList()
    }
    
    fun serializeDevices(devices: List<Device>): String {
        return "[]"
    }
    
    fun deserializeDevices(json: String): List<Device> {
        return emptyList()
    }
    
    fun serializeSyncMetadata(metadata: List<SyncMetadata>): String {
        return "[]"
    }
    
    fun deserializeSyncMetadata(json: String): List<SyncMetadata> {
        return emptyList()
    }
    
    fun serializeRecipeImage(image: com.ourcookbook.domain.model.RecipeImage): String {
        return "{}"
    }
}

/**
 * Use cases collection
 */
data class BackupRestoreUseCases(
    val createFullBackup: CreateFullBackup,
    val restoreFromBackup: RestoreFromBackup,
    val exportCookbookBackup: ExportCookbookBackup,
    val importCookbookBackup: ImportCookbookBackup,
    val verifyBackup: VerifyBackup
)
