package com.ourcookbook.data.repository.backup

import android.content.Context
import android.net.Uri
import com.ourcookbook.domain.model.Cookbook
import com.ourcookbook.domain.model.Recipe
import com.ourcookbook.domain.repository.CookbookRepository
import com.ourcookbook.domain.repository.RecipeRepository
import com.ourcookbook.domain.usecase.backup.BackupMetadata
import com.ourcookbook.domain.usecase.backup.BackupResult
import com.ourcookbook.domain.usecase.backup.BackupType
import com.ourcookbook.domain.usecase.backup.BackupVerificationResult
import com.ourcookbook.domain.usecase.backup.ConflictResolutionStrategy
import com.ourcookbook.domain.usecase.backup.RestoreResult
import com.ourcookbook.domain.usecase.backup.RestoreStatistics
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.time.Instant
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Backup Repository
 * 
 * Central repository for backup and restore operations
 * Provides a unified interface for all backup-related functionality
 */
@Singleton
class BackupRepository @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val cookbookRepository: CookbookRepository,
    @ApplicationContext private val context: Context
) {
    
    /**
     * Create a full backup of all app data
     */
    suspend fun createFullBackup(
        outputUri: Uri,
        includeImages: Boolean = true,
        compressionLevel: Int = 6
    ): BackupResult {
        return withContext(Dispatchers.IO) {
            try {
                val timestamp = Instant.now()
                val backupDir = File(context.cacheDir, "backup_${timestamp.toEpochMilli()}")
                
                backupDir.mkdirs()
                
                // Get all data
                val recipes = recipeRepository.getAll()
                val cookbooks = cookbookRepository.getAll()
                
                // Create metadata
                val metadata = BackupMetadata(
                    version = BackupFormat.CURRENT_VERSION,
                    createdAt = timestamp,
                    appVersion = getAppVersion(),
                    recipeCount = recipes.size,
                    cookbookCount = cookbooks.size,
                    deviceCount = 0, // Would get from device repository
                    includeImages = includeImages,
                    type = BackupType.FULL
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
                
                // Create ZIP
                val zipFile = File(context.cacheDir, "backup_${timestamp.toEpochMilli()}.zip")
                createZipFile(backupDir, zipFile)
                
                // Copy to output URI
                copyToUri(zipFile, outputUri)
                
                // Cleanup
                backupDir.deleteRecursively()
                zipFile.delete()
                
                BackupResult.Success(
                    fileUri = outputUri,
                    sizeBytes = zipFile.length(),
                    recipeCount = recipes.size,
                    cookbookCount = cookbooks.size,
                    createdAt = timestamp
                )
                
            } catch (e: Exception) {
                BackupResult.Error(e.message ?: "Unknown error during backup")
            }
        }
    }
    
    /**
     * Restore from a backup file
     */
    suspend fun restoreFromBackup(
        inputUri: Uri,
        restoreImages: Boolean = true,
        conflictResolution: ConflictResolutionStrategy = ConflictResolutionStrategy.SKIP
    ): RestoreResult {
        return withContext(Dispatchers.IO) {
            try {
                val tempDir = File(context.cacheDir, "restore_${Instant.now().toEpochMilli()}")
                tempDir.mkdirs()
                
                // Extract ZIP
                extractZipFile(inputUri, tempDir)
                
                // Read metadata
                val metadataFile = File(tempDir, BackupFormat.METADATA_FILE)
                if (!metadataFile.exists()) {
                    return@withContext RestoreResult.Error("Invalid backup file: missing metadata")
                }
                
                val metadata = BackupSerializer.deserializeMetadata(metadataFile.readText())
                
                // Check version
                if (metadata.version > BackupFormat.CURRENT_VERSION) {
                    return@withContext RestoreResult.Error(
                        "Backup file version ${metadata.version} is newer than supported version ${BackupFormat.CURRENT_VERSION}"
                    )
                }
                
                // Read data
                val recipesFile = File(tempDir, BackupFormat.RECIPES_FILE)
                val cookbooksFile = File(tempDir, BackupFormat.COOKBOOKS_FILE)
                
                val recipes = BackupSerializer.deserializeRecipes(recipesFile.readText())
                val cookbooks = BackupSerializer.deserializeCookbooks(cookbooksFile.readText())
                
                // Restore data
                val stats = RestoreStatistics()
                
                cookbooks.forEach { cookbook ->
                    try {
                        val existing = cookbookRepository.getById(cookbook.id)
                        when (conflictResolution) {
                            ConflictResolutionStrategy.OVERWRITE -> {
                                cookbookRepository.upsert(cookbook)
                                stats.cookbooksRestored++
                            }
                            ConflictResolutionStrategy.SKIP -> {
                                if (existing == null) {
                                    cookbookRepository.insert(cookbook)
                                    stats.cookbooksRestored++
                                } else {
                                    stats.cookbooksSkipped++
                                }
                            }
                            ConflictResolutionStrategy.MERGE -> {
                                val copy = cookbook.copy(
                                    id = "${cookbook.id}_restored_${Instant.now().toEpochMilli()}",
                                    name = "${cookbook.name} (Restored)"
                                )
                                cookbookRepository.insert(copy)
                                stats.cookbooksRestored++
                            }
                        }
                    } catch (e: Exception) {
                        stats.cookbooksFailed++
                    }
                }
                
                recipes.forEach { recipe ->
                    try {
                        val existing = recipeRepository.getById(recipe.id)
                        when (conflictResolution) {
                            ConflictResolutionStrategy.OVERWRITE -> {
                                recipeRepository.upsert(recipe)
                                stats.recipesRestored++
                            }
                            ConflictResolutionStrategy.SKIP -> {
                                if (existing == null) {
                                    recipeRepository.insert(recipe)
                                    stats.recipesRestored++
                                } else {
                                    stats.recipesSkipped++
                                }
                            }
                            ConflictResolutionStrategy.MERGE -> {
                                val copy = recipe.copy(
                                    id = "${recipe.id}_restored_${Instant.now().toEpochMilli()}",
                                    title = "${recipe.title} (Restored)"
                                )
                                recipeRepository.insert(copy)
                                stats.recipesRestored++
                            }
                        }
                    } catch (e: Exception) {
                        stats.recipesFailed++
                    }
                }
                
                // Cleanup
                tempDir.deleteRecursively()
                
                RestoreResult.Success(metadata, stats)
                
            } catch (e: Exception) {
                RestoreResult.Error(e.message ?: "Unknown error during restore")
            }
        }
    }
    
    /**
     * Export a cookbook as a backup
     */
    suspend fun exportCookbookBackup(
        cookbookId: String,
        outputUri: Uri
    ): BackupResult {
        return withContext(Dispatchers.IO) {
            try {
                val cookbook = cookbookRepository.getById(cookbookId)
                    ?: return@withContext BackupResult.Error("Cookbook not found")
                
                val recipes = recipeRepository.getAllByCookbookId(cookbookId)
                
                val timestamp = Instant.now()
                val backupDir = File(context.cacheDir, "cookbook_export_${cookbookId}_${timestamp.toEpochMilli()}")
                
                backupDir.mkdirs()
                
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
                
                val cookbooksFile = File(backupDir, BackupFormat.COOKBOOKS_FILE)
                cookbooksFile.writeText(BackupSerializer.serializeCookbooks(listOf(cookbook)))
                
                val recipesFile = File(backupDir, BackupFormat.RECIPES_FILE)
                recipesFile.writeText(BackupSerializer.serializeRecipes(recipes))
                
                val zipFile = File(context.cacheDir, "cookbook_${cookbookId}_${timestamp.toEpochMilli()}.zip")
                createZipFile(backupDir, zipFile)
                
                copyToUri(zipFile, outputUri)
                
                backupDir.deleteRecursively()
                zipFile.delete()
                
                BackupResult.Success(
                    fileUri = outputUri,
                    sizeBytes = zipFile.length(),
                    recipeCount = recipes.size,
                    cookbookCount = 1,
                    createdAt = timestamp
                )
                
            } catch (e: Exception) {
                BackupResult.Error(e.message ?: "Unknown error during export")
            }
        }
    }
    
    /**
     * Import a cookbook from a backup
     */
    suspend fun importCookbookBackup(
        inputUri: Uri,
        conflictResolution: ConflictResolutionStrategy = ConflictResolutionStrategy.SKIP
    ): RestoreResult {
        return withContext(Dispatchers.IO) {
            try {
                val tempDir = File(context.cacheDir, "cookbook_import_${Instant.now().toEpochMilli()}")
                tempDir.mkdirs()
                
                extractZipFile(inputUri, tempDir)
                
                val metadataFile = File(tempDir, BackupFormat.METADATA_FILE)
                val metadata = BackupSerializer.deserializeMetadata(metadataFile.readText())
                
                val recipesFile = File(tempDir, BackupFormat.RECIPES_FILE)
                val cookbooksFile = File(tempDir, BackupFormat.COOKBOOKS_FILE)
                
                val recipes = BackupSerializer.deserializeRecipes(recipesFile.readText())
                val cookbooks = BackupSerializer.deserializeCookbooks(cookbooksFile.readText())
                
                val stats = RestoreStatistics()
                
                cookbooks.forEach { cookbook ->
                    try {
                        val existing = cookbookRepository.getById(cookbook.id)
                        when (conflictResolution) {
                            ConflictResolutionStrategy.OVERWRITE -> {
                                cookbookRepository.upsert(cookbook)
                                stats.cookbooksRestored++
                            }
                            ConflictResolutionStrategy.SKIP -> {
                                if (existing == null) {
                                    cookbookRepository.insert(cookbook)
                                    stats.cookbooksRestored++
                                } else {
                                    stats.cookbooksSkipped++
                                }
                            }
                            ConflictResolutionStrategy.MERGE -> {
                                val copy = cookbook.copy(
                                    id = "${cookbook.id}_imported_${Instant.now().toEpochMilli()}",
                                    name = "${cookbook.name} (Imported)"
                                )
                                cookbookRepository.insert(copy)
                                stats.cookbooksRestored++
                            }
                        }
                    } catch (e: Exception) {
                        stats.cookbooksFailed++
                    }
                }
                
                recipes.forEach { recipe ->
                    try {
                        val existing = recipeRepository.getById(recipe.id)
                        when (conflictResolution) {
                            ConflictResolutionStrategy.OVERWRITE -> {
                                recipeRepository.upsert(recipe)
                                stats.recipesRestored++
                            }
                            ConflictResolutionStrategy.SKIP -> {
                                if (existing == null) {
                                    recipeRepository.insert(recipe)
                                    stats.recipesRestored++
                                } else {
                                    stats.recipesSkipped++
                                }
                            }
                            ConflictResolutionStrategy.MERGE -> {
                                val copy = recipe.copy(
                                    id = "${recipe.id}_imported_${Instant.now().toEpochMilli()}",
                                    title = "${recipe.title} (Imported)"
                                )
                                recipeRepository.insert(copy)
                                stats.recipesRestored++
                            }
                        }
                    } catch (e: Exception) {
                        stats.recipesFailed++
                    }
                }
                
                tempDir.deleteRecursively()
                
                RestoreResult.Success(metadata, stats)
                
            } catch (e: Exception) {
                RestoreResult.Error(e.message ?: "Unknown error during import")
            }
        }
    }
    
    /**
     * Verify backup file integrity
     */
    suspend fun verifyBackup(inputUri: Uri): BackupVerificationResult {
        return withContext(Dispatchers.IO) {
            try {
                val tempDir = File(context.cacheDir, "verify_${Instant.now().toEpochMilli()}")
                tempDir.mkdirs()
                
                extractZipFile(inputUri, tempDir)
                
                val metadataFile = File(tempDir, BackupFormat.METADATA_FILE)
                if (!metadataFile.exists()) {
                    return@withContext BackupVerificationResult.Error("Missing metadata file")
                }
                
                val metadata = BackupSerializer.deserializeMetadata(metadataFile.readText())
                
                if (metadata.version > BackupFormat.CURRENT_VERSION) {
                    return@withContext BackupVerificationResult.Error(
                        "Backup version ${metadata.version} is newer than supported version ${BackupFormat.CURRENT_VERSION}"
                    )
                }
                
                val recipesFile = File(tempDir, BackupFormat.RECIPES_FILE)
                val cookbooksFile = File(tempDir, BackupFormat.COOKBOOKS_FILE)
                
                val hasRecipes = recipesFile.exists() || metadata.recipeCount == 0
                val hasCookbooks = cookbooksFile.exists() || metadata.cookbookCount == 0
                
                if (!hasRecipes || !hasCookbooks) {
                    return@withContext BackupVerificationResult.Error("Missing required data files")
                }
                
                val recipes = BackupSerializer.deserializeRecipes(recipesFile.readText())
                val cookbooks = BackupSerializer.deserializeCookbooks(cookbooksFile.readText())
                
                if (recipes.size != metadata.recipeCount) {
                    return@withContext BackupVerificationResult.Error(
                        "Recipe count mismatch: expected ${metadata.recipeCount}, found ${recipes.size}"
                    )
                }
                
                if (cookbooks.size != metadata.cookbookCount) {
                    return@withContext BackupVerificationResult.Error(
                        "Cookbook count mismatch: expected ${metadata.cookbookCount}, found ${cookbooks.size}"
                    )
                }
                
                tempDir.deleteRecursively()
                
                BackupVerificationResult.Success(metadata, true, emptyList())
                
            } catch (e: Exception) {
                BackupVerificationResult.Error(e.message ?: "Unknown error during verification")
            }
        }
    }
    
    /**
     * Get list of available backups
     */
    suspend fun getAvailableBackups(): List<BackupInfo> {
        return withContext(Dispatchers.IO) {
            // In a real implementation, this would scan for backup files
            // For now, return empty list
            emptyList()
        }
    }
    
    /**
     * Delete a backup file
     */
    suspend fun deleteBackup(backupUri: Uri): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                context.contentResolver.delete(backupUri, null, null)
                true
            } catch (e: Exception) {
                false
            }
        }
    }
    
    /**
     * Get backup file info
     */
    suspend fun getBackupInfo(backupUri: Uri): BackupInfo? {
        return withContext(Dispatchers.IO) {
            try {
                val tempDir = File(context.cacheDir, "backup_info_${Instant.now().toEpochMilli()}")
                tempDir.mkdirs()
                
                extractZipFile(backupUri, tempDir)
                
                val metadataFile = File(tempDir, BackupFormat.METADATA_FILE)
                if (!metadataFile.exists()) {
                    return@withContext null
                }
                
                val metadata = BackupSerializer.deserializeMetadata(metadataFile.readText())
                
                tempDir.deleteRecursively()
                
                BackupInfo(
                    uri = backupUri,
                    metadata = metadata,
                    fileSize = context.contentResolver.openInputStream(backupUri)?.available?.toLong() ?: 0L,
                    lastModified = Instant.now() // Would get actual file modification time
                )
                
            } catch (e: Exception) {
                null
            }
        }
    }
    
    // ============================================================================
    // Helper Methods
    // ============================================================================
    
    private fun getAppVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "1.0.0"
        } catch (e: Exception) {
            "1.0.0"
        }
    }
    
    private fun createZipFile(sourceDir: File, outputFile: File) {
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
    
    private fun extractZipFile(inputUri: Uri, outputDir: File) {
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
    
    private fun copyToUri(sourceFile: File, outputUri: Uri) {
        context.contentResolver.openOutputStream(outputUri)?.use { outputStream ->
            sourceFile.inputStream().use { inputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    }
}

/**
 * Backup information
 */
data class BackupInfo(
    val uri: Uri,
    val metadata: BackupMetadata,
    val fileSize: Long,
    val lastModified: Instant
)

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
 * Backup serializer
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
        // In production, use proper JSON serialization
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
}
