package com.ourcookbook.data.repository

import android.content.Context
import android.net.Uri
import com.ourcookbook.data.datasource.IExportImportDataSource
import com.ourcookbook.data.datasource.local.ICookbookLocalDataSource
import com.ourcookbook.data.datasource.local.IRecipeLocalDataSource
import com.ourcookbook.domain.model.*
import com.ourcookbook.domain.repository.CookbookRepository
import com.ourcookbook.domain.repository.ExportImportRepository
import com.ourcookbook.domain.repository.RecipeRepository
import com.ourcookbook.domain.service.ChecksumService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository Implementation for Export/Import Operations
 * Task 2.1.09: Export/Import Screen Implementation
 */

@Singleton
class ExportImportRepositoryImpl @Inject constructor(
    private val exportImportDataSource: IExportImportDataSource,
    private val recipeRepository: RecipeRepository,
    private val cookbookRepository: CookbookRepository,
    private val checksumService: ChecksumService,
    @ApplicationContext private val context: Context
) : ExportImportRepository {
    
    // ==================== EXPORT OPERATIONS ====================
    
    override suspend fun exportRecipe(
        recipeId: String,
        format: ExportFormat,
        settings: ExportSettings
    ): ExportFileInfo = withContext(Dispatchers.IO) {
        val recipe = recipeRepository.getRecipeById(recipeId) ?: 
            throw IllegalArgumentException("Recipe not found: $recipeId")
        
        val fileName = exportImportDataSource.generateExportFileName(
            settings.fileNamePattern, format
        )
        
        val directory = when (settings.exportLocation) {
            "Downloads" -> context.getExternalFilesDir("Downloads")?.path ?: context.filesDir.path
            "App Storage" -> context.filesDir.path
            else -> context.filesDir.path
        }
        
        val file = File(directory, fileName)
        ensureDirectoryExists(directory)
        
        FileOutputStream(file).use { outputStream ->
            val fileInfo = exportImportDataSource.exportRecipe(
                recipe, format, outputStream, settings
            )
            return@withContext fileInfo.copy(
                filePath = file.absolutePath,
                fileName = file.name,
                fileSize = file.length()
            )
        }
    }
    
    override suspend fun exportRecipes(
        recipeIds: List<String>,
        format: ExportFormat,
        settings: ExportSettings
    ): ExportFileInfo = withContext(Dispatchers.IO) {
        val recipes = recipeRepository.getRecipesByIds(recipeIds)
        if (recipes.isEmpty()) {
            throw IllegalArgumentException("No recipes found for the specified IDs")
        }
        
        val fileName = exportImportDataSource.generateExportFileName(
            settings.fileNamePattern, format
        )
        
        val directory = when (settings.exportLocation) {
            "Downloads" -> context.getExternalFilesDir("Downloads")?.path ?: context.filesDir.path
            "App Storage" -> context.filesDir.path
            else -> context.filesDir.path
        }
        
        val file = File(directory, fileName)
        ensureDirectoryExists(directory)
        
        FileOutputStream(file).use { outputStream ->
            val fileInfo = exportImportDataSource.exportRecipes(
                recipes, format, outputStream, settings
            )
            return@withContext fileInfo.copy(
                filePath = file.absolutePath,
                fileName = file.name,
                fileSize = file.length(),
                recipeCount = recipes.size
            )
        }
    }
    
    override suspend fun exportCookbook(
        cookbookId: String,
        format: ExportFormat,
        settings: ExportSettings
    ): ExportFileInfo = withContext(Dispatchers.IO) {
        val cookbook = cookbookRepository.getCookbookById(cookbookId) ?: 
            throw IllegalArgumentException("Cookbook not found: $cookbookId")
        
        val recipes = recipeRepository.getRecipesByIds(cookbook.recipeIds)
        
        val fileName = exportImportDataSource.generateExportFileName(
            settings.fileNamePattern, format
        )
        
        val directory = when (settings.exportLocation) {
            "Downloads" -> context.getExternalFilesDir("Downloads")?.path ?: context.filesDir.path
            "App Storage" -> context.filesDir.path
            else -> context.filesDir.path
        }
        
        val file = File(directory, fileName)
        ensureDirectoryExists(directory)
        
        FileOutputStream(file).use { outputStream ->
            val fileInfo = exportImportDataSource.exportCookbook(
                cookbook, recipes, format, outputStream, settings
            )
            return@withContext fileInfo.copy(
                filePath = file.absolutePath,
                fileName = file.name,
                fileSize = file.length(),
                recipeCount = recipes.size,
                cookbookCount = 1
            )
        }
    }
    
    override suspend fun exportAllRecipes(
        format: ExportFormat,
        settings: ExportSettings
    ): ExportFileInfo = withContext(Dispatchers.IO) {
        val allRecipes = recipeRepository.getAllRecipesOnce()
        val allCookbooks = cookbookRepository.getAllCookbooksOnce()
        
        val fileName = exportImportDataSource.generateExportFileName(
            settings.fileNamePattern, format
        )
        
        val directory = when (settings.exportLocation) {
            "Downloads" -> context.getExternalFilesDir("Downloads")?.path ?: context.filesDir.path
            "App Storage" -> context.filesDir.path
            else -> context.filesDir.path
        }
        
        val file = File(directory, fileName)
        ensureDirectoryExists(directory)
        
        FileOutputStream(file).use { outputStream ->
            val fileInfo = exportImportDataSource.exportAllRecipes(
                allRecipes, allCookbooks, format, outputStream, settings
            )
            return@withContext fileInfo.copy(
                filePath = file.absolutePath,
                fileName = file.name,
                fileSize = file.length(),
                recipeCount = allRecipes.size,
                cookbookCount = allCookbooks.size
            )
        }
    }
    
    override suspend fun batchExport(
        items: List<String>,
        format: ExportFormat,
        settings: ExportSettings,
        onProgress: (Int, Int) -> Unit
    ): BatchOperationResult = withContext(Dispatchers.IO) {
        val results = mutableListOf<ExportFileInfo>()
        val failedItems = mutableListOf<String>()
        val errorMessages = mutableMapOf<String, String>()
        
        items.forEachIndexed { index, itemId ->
            try {
                val fileInfo = if (itemId.startsWith("cookbook_")) {
                    // This is a cookbook ID
                    val cookbookId = itemId.removePrefix("cookbook_")
                    exportCookbook(cookbookId, format, settings)
                } else {
                    // This is a recipe ID
                    exportRecipe(itemId, format, settings)
                }
                results.add(fileInfo)
            } catch (e: Exception) {
                failedItems.add(itemId)
                errorMessages[itemId] = e.message ?: "Unknown error"
            }
            
            onProgress(index + 1, items.size)
        }
        
        return@withContext BatchOperationResult(
            operationId = java.util.UUID.randomUUID().toString(),
            totalItems = items.size,
            successfulItems = results.size,
            failedItems = failedItems.size,
            failedItemIds = failedItems,
            errorMessages = errorMessages
        )
    }
    
    // ==================== IMPORT OPERATIONS ====================
    
    override suspend fun importFromFile(
        filePath: String,
        format: ImportFormat,
        settings: ImportSettings
    ): Pair<List<Recipe>, ImportFileInfo> = withContext(Dispatchers.IO) {
        val file = File(filePath)
        if (!file.exists()) {
            throw IllegalArgumentException("File not found: $filePath")
        }
        
        FileInputStream(file).use { inputStream ->
            val (recipes, fileInfo) = exportImportDataSource.importRecipes(
                inputStream, format, settings
            )
            return@withContext Pair(recipes, fileInfo)
        }
    }
    
    override suspend fun importCookbookFromFile(
        filePath: String,
        format: ImportFormat,
        settings: ImportSettings
    ): Triple<Cookbook, List<Recipe>, ImportFileInfo> = withContext(Dispatchers.IO) {
        val file = File(filePath)
        if (!file.exists()) {
            throw IllegalArgumentException("File not found: $filePath")
        }
        
        FileInputStream(file).use { inputStream ->
            val (cookbook, recipes, fileInfo) = exportImportDataSource.importCookbook(
                inputStream, format, settings
            )
            return@withContext Triple(cookbook, recipes, fileInfo)
        }
    }
    
    override suspend fun previewImport(
        filePath: String,
        format: ImportFormat
    ): ExportImportPreview = withContext(Dispatchers.IO) {
        exportImportDataSource.previewImport(filePath, format)
    }
    
    override suspend fun validateImportFile(
        filePath: String,
        format: ImportFormat
    ): ImportFileInfo = withContext(Dispatchers.IO) {
        exportImportDataSource.validateImportFile(filePath, format)
    }
    
    override suspend fun detectFileFormat(filePath: String): ImportFormat? = withContext(Dispatchers.IO) {
        exportImportDataSource.detectFileFormat(filePath)
    }
    
    override suspend fun detectFileFormat(uri: Uri): ImportFormat? = withContext(Dispatchers.IO) {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            exportImportDataSource.detectFileFormat(inputStream)
        }
    }
    
    override suspend fun batchImport(
        filePaths: List<String>,
        format: ImportFormat,
        settings: ImportSettings,
        onProgress: (Int, Int) -> Unit,
        onConflict: (ImportConflict) -> ConflictResolution
    ): BatchOperationResult = withContext(Dispatchers.IO) {
        val importedRecipes = mutableListOf<Recipe>()
        val failedFiles = mutableListOf<String>()
        val errorMessages = mutableMapOf<String, String>()
        
        filePaths.forEachIndexed { index, filePath ->
            try {
                val (recipes, fileInfo) = importFromFile(filePath, format, settings)
                
                // Check for conflicts
                val existingRecipes = recipeRepository.getAllRecipesOnce()
                val conflicts = exportImportDataSource.detectConflicts(existingRecipes, recipes)
                
                if (conflicts.isNotEmpty()) {
                    // For batch operations, we'll use the provided conflict resolution strategy
                    when (settings.conflictResolution) {
                        ConflictResolutionStrategy.ASK -> {
                            // In batch mode with ASK strategy, we'll skip conflicts
                            // In a real implementation, we might want to handle this differently
                            importedRecipes.addAll(recipes.filter { recipe ->
                                conflicts.none { it.newRecipe.id == recipe.id }
                            })
                        }
                        ConflictResolutionStrategy.OVERWRITE -> {
                            // Overwrite existing recipes
                            recipes.forEach { recipe ->
                                upsertRecipe(recipe)
                            }
                            importedRecipes.addAll(recipes)
                        }
                        ConflictResolutionStrategy.SKIP -> {
                            // Skip conflicting recipes
                            importedRecipes.addAll(recipes.filter { recipe ->
                                conflicts.none { it.newRecipe.id == recipe.id }
                            })
                        }
                        ConflictResolutionStrategy.MERGE -> {
                            // Try to merge - for now, just skip
                            importedRecipes.addAll(recipes.filter { recipe ->
                                conflicts.none { it.newRecipe.id == recipe.id }
                            })
                        }
                    }
                } else {
                    // No conflicts, import all recipes
                    recipes.forEach { recipe ->
                        upsertRecipe(recipe)
                    }
                    importedRecipes.addAll(recipes)
                }
                
            } catch (e: Exception) {
                failedFiles.add(filePath)
                errorMessages[filePath] = e.message ?: "Unknown error"
            }
            
            onProgress(index + 1, filePaths.size)
        }
        
        return@withContext BatchOperationResult(
            operationId = java.util.UUID.randomUUID().toString(),
            totalItems = filePaths.size,
            successfulItems = importedRecipes.size,
            failedItems = failedFiles.size,
            failedItemIds = failedFiles,
            errorMessages = errorMessages
        )
    }
    
    // ==================== CONFLICT DETECTION ====================
    
    override suspend fun detectConflicts(
        recipesToImport: List<Recipe>
    ): List<ImportConflict> = withContext(Dispatchers.IO) {
        val existingRecipes = recipeRepository.getAllRecipesOnce()
        exportImportDataSource.detectConflicts(existingRecipes, recipesToImport)
    }
    
    override suspend fun resolveConflict(
        conflict: ImportConflict,
        resolution: ConflictResolution
    ): Recipe? = withContext(Dispatchers.IO) {
        when (resolution) {
            ConflictResolution.KeepLocal -> null
            ConflictResolution.KeepRemote -> {
                upsertRecipe(conflict.newRecipe)
                conflict.newRecipe
            }
            is ConflictResolution.Merge -> {
                val mergedRecipe = mergeRecipes(conflict.existingRecipe, conflict.newRecipe)
                upsertRecipe(mergedRecipe)
                mergedRecipe
            }
        }
    }

    private suspend fun upsertRecipe(recipe: Recipe) {
        if (recipeRepository.getRecipeById(recipe.id) == null) {
            recipeRepository.createRecipe(recipe)
        } else {
            recipeRepository.updateRecipe(recipe)
        }
    }
    
    private fun mergeRecipes(existing: Recipe, new: Recipe): Recipe {
        // Simple merge strategy: prefer non-null values from new recipe
        // In a real implementation, this would be more sophisticated
        return existing.copy(
            title = new.title.takeIf { it.isNotBlank() } ?: existing.title,
            description = new.description ?: existing.description,
            category = new.category.takeIf { it.isNotBlank() } ?: existing.category,
            ingredients = if (new.ingredients.isNotEmpty()) new.ingredients else existing.ingredients,
            instructions = if (new.instructions.isNotEmpty()) new.instructions else existing.instructions,
            servingSize = new.servingSize ?: existing.servingSize,
            prepTime = new.prepTime ?: existing.prepTime,
            cookTime = new.cookTime ?: existing.cookTime,
            rating = new.rating ?: existing.rating,
            isFavorite = new.isFavorite,
            imageUrl = new.imageUrl ?: existing.imageUrl,
            notes = new.notes ?: existing.notes,
            source = new.source ?: existing.source,
            tags = if (new.tags.isNotEmpty()) new.tags else existing.tags,
            updatedAt = new.updatedAt,
            versionVector = new.versionVector,
            checksum = new.checksum,
            deviceId = new.deviceId
        )
    }
    
    // ==================== OPERATION HISTORY ====================
    
    override suspend fun saveOperationToHistory(operation: ExportImportOperation) = withContext(Dispatchers.IO) {
        exportImportDataSource.saveOperationToHistory(operation)
    }
    
    override suspend fun loadOperationHistory(limit: Int): List<ExportImportOperation> = withContext(Dispatchers.IO) {
        exportImportDataSource.loadOperationHistory(limit)
    }
    
    override suspend fun clearOperationHistory() = withContext(Dispatchers.IO) {
        exportImportDataSource.clearOperationHistory()
    }
    
    override suspend fun deleteOperationFromHistory(operationId: String) = withContext(Dispatchers.IO) {
        exportImportDataSource.deleteOperationFromHistory(operationId)
    }
    
    // ==================== FILE OPERATIONS ====================
    
    override fun getExportLocations(): List<com.ourcookbook.data.datasource.ExportLocation> {
        return exportImportDataSource.getExportLocations()
    }
    
    override fun isExportLocationAvailable(location: String): Boolean {
        return exportImportDataSource.isExportLocationAvailable(location)
    }
    
    override fun listFilesInDirectory(directory: String, extensions: List<String>): List<File> {
        return exportImportDataSource.listFilesInDirectory(directory, extensions)
    }
    
    override fun fileExists(filePath: String): Boolean {
        return exportImportDataSource.fileExists(filePath)
    }
    
    override fun getFileSize(filePath: String): Long {
        return exportImportDataSource.getFileSize(filePath)
    }
    
    override fun deleteFile(filePath: String): Boolean {
        return exportImportDataSource.deleteFile(filePath)
    }
    
    // ==================== CLOUD INTEGRATION ====================
    
    override suspend fun exportToDrive(
        filePath: String,
        folderId: String?,
        fileName: String?
    ): ExportFileInfo? = withContext(Dispatchers.IO) {
        // Implementation for Google Drive export
        // This would use the Google Drive API
        TODO("Implement Google Drive export")
    }
    
    override suspend fun importFromDrive(
        fileId: String
    ): Pair<ByteArray, String>? = withContext(Dispatchers.IO) {
        // Implementation for Google Drive import
        // This would use the Google Drive API
        TODO("Implement Google Drive import")
    }
    
    override suspend fun listDriveFiles(): List<ExportFileInfo> = withContext(Dispatchers.IO) {
        // Implementation for listing Google Drive files
        TODO("Implement Google Drive file listing")
    }
    
    override suspend fun deleteDriveFile(fileId: String): Boolean = withContext(Dispatchers.IO) {
        // Implementation for deleting Google Drive files
        TODO("Implement Google Drive file deletion")
    }
    
    // ==================== UTILITY METHODS ====================
    
    private fun ensureDirectoryExists(directoryPath: String) {
        val directory = File(directoryPath)
        if (!directory.exists()) {
            directory.mkdirs()
        }
    }
    
    override suspend fun generateChecksum(filePath: String): String = withContext(Dispatchers.IO) {
        checksumService.calculateChecksum(filePath)
    }
    
    override suspend fun generateChecksum(content: ByteArray): String = withContext(Dispatchers.IO) {
        checksumService.calculateChecksum(content)
    }
}
