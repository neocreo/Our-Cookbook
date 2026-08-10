package com.ourcookbook.data.datasource

import com.ourcookbook.domain.model.*
import java.io.File
import java.io.InputStream
import java.io.OutputStream

/**
 * Data Source Interface for Export/Import Operations
 * Task 2.1.09: Export/Import Screen Implementation
 */

interface IExportImportDataSource {
    
    // ==================== EXPORT OPERATIONS ====================
    
    /**
     * Export a single recipe to the specified format
     */
    suspend fun exportRecipe(
        recipe: Recipe,
        format: ExportFormat,
        outputStream: OutputStream,
        settings: ExportSettings = ExportSettings()
    ): ExportFileInfo
    
    /**
     * Export multiple recipes to the specified format
     */
    suspend fun exportRecipes(
        recipes: List<Recipe>,
        format: ExportFormat,
        outputStream: OutputStream,
        settings: ExportSettings = ExportSettings()
    ): ExportFileInfo
    
    /**
     * Export a cookbook (all recipes in the cookbook)
     */
    suspend fun exportCookbook(
        cookbook: Cookbook,
        recipes: List<Recipe>,
        format: ExportFormat,
        outputStream: OutputStream,
        settings: ExportSettings = ExportSettings()
    ): ExportFileInfo
    
    /**
     * Export all recipes across all cookbooks
     */
    suspend fun exportAllRecipes(
        recipes: List<Recipe>,
        cookbooks: List<Cookbook>,
        format: ExportFormat,
        outputStream: OutputStream,
        settings: ExportSettings = ExportSettings()
    ): ExportFileInfo
    
    /**
     * Create export file at the specified location
     */
    suspend fun createExportFile(
        fileName: String,
        directory: String,
        content: ByteArray
    ): File
    
    /**
     * Get available export locations
     */
    fun getExportLocations(): List<ExportLocation>
    
    /**
     * Check if export location is available
     */
    fun isExportLocationAvailable(location: String): Boolean
    
    /**
     * Generate file name based on pattern and timestamp
     */
    fun generateExportFileName(pattern: String, format: ExportFormat): String
    
    // ==================== IMPORT OPERATIONS ====================
    
    /**
     * Import a single recipe from the specified format
     */
    suspend fun importRecipe(
        inputStream: InputStream,
        format: ImportFormat,
        settings: ImportSettings = ImportSettings()
    ): Pair<Recipe, ImportFileInfo>
    
    /**
     * Import multiple recipes from a file
     */
    suspend fun importRecipes(
        inputStream: InputStream,
        format: ImportFormat,
        settings: ImportSettings = ImportSettings()
    ): Pair<List<Recipe>, ImportFileInfo>
    
    /**
     * Import a cookbook from a file
     */
    suspend fun importCookbook(
        inputStream: InputStream,
        format: ImportFormat,
        settings: ImportSettings = ImportSettings()
    ): Pair<Cookbook, List<Recipe>, ImportFileInfo>
    
    /**
     * Preview import content without actually importing
     */
    suspend fun previewImport(
        filePath: String,
        format: ImportFormat
    ): ExportImportPreview
    
    /**
     * Validate import file
     */
    suspend fun validateImportFile(
        filePath: String,
        format: ImportFormat
    ): ImportFileInfo
    
    /**
     * Detect file format from file extension or content
     */
    fun detectFileFormat(filePath: String): ImportFormat?
    
    /**
     * Detect file format from input stream
     */
    suspend fun detectFileFormat(inputStream: InputStream): ImportFormat?
    
    // ==================== FILE OPERATIONS ====================
    
    /**
     * List files in a directory
     */
    fun listFilesInDirectory(directory: String, extensions: List<String> = emptyList()): List<File>
    
    /**
     * Check if file exists
     */
    fun fileExists(filePath: String): Boolean
    
    /**
     * Get file size
     */
    fun getFileSize(filePath: String): Long
    
    /**
     * Delete file
     */
    fun deleteFile(filePath: String): Boolean
    
    /**
     * Copy file
     */
    fun copyFile(sourcePath: String, destinationPath: String): Boolean
    
    /**
     * Move file
     */
    fun moveFile(sourcePath: String, destinationPath: String): Boolean
    
    // ==================== OPERATION HISTORY ====================
    
    /**
     * Save operation to history
     */
    suspend fun saveOperationToHistory(operation: ExportImportOperation)
    
    /**
     * Load operation history
     */
    suspend fun loadOperationHistory(limit: Int = 50): List<ExportImportOperation>
    
    /**
     * Clear operation history
     */
    suspend fun clearOperationHistory()
    
    /**
     * Delete specific operation from history
     */
    suspend fun deleteOperationFromHistory(operationId: String)
    
    // ==================== CONFLICT DETECTION ====================
    
    /**
     * Detect conflicts between existing recipes and recipes to be imported
     */
    suspend fun detectConflicts(
        existingRecipes: List<Recipe>,
        recipesToImport: List<Recipe>
    ): List<ImportConflict>
    
    /**
     * Check for duplicate recipe IDs
     */
    fun detectDuplicateIds(
        existingRecipes: List<Recipe>,
        recipesToImport: List<Recipe>
    ): List<ImportConflict>
    
    /**
     * Check for duplicate recipe titles
     */
    fun detectDuplicateTitles(
        existingRecipes: List<Recipe>,
        recipesToImport: List<Recipe>
    ): List<ImportConflict>
}

/**
 * Export location information
 */
data class ExportLocation(
    val id: String,
    val name: String,
    val path: String,
    val isAvailable: Boolean,
    val isWritable: Boolean,
    val requiresPermission: Boolean = false
)

/**
 * Implementation of Export/Import Data Source
 */
class ExportImportDataSourceImpl(
    private val context: android.content.Context
) : IExportImportDataSource {
    
    override suspend fun exportRecipe(
        recipe: Recipe,
        format: ExportFormat,
        outputStream: OutputStream,
        settings: ExportSettings
    ): ExportFileInfo {
        // Implementation will be provided by format-specific exporters
        TODO("Implement recipe export for format: $format")
    }
    
    override suspend fun exportRecipes(
        recipes: List<Recipe>,
        format: ExportFormat,
        outputStream: OutputStream,
        settings: ExportSettings
    ): ExportFileInfo {
        TODO("Implement multiple recipes export for format: $format")
    }
    
    override suspend fun exportCookbook(
        cookbook: Cookbook,
        recipes: List<Recipe>,
        format: ExportFormat,
        outputStream: OutputStream,
        settings: ExportSettings
    ): ExportFileInfo {
        TODO("Implement cookbook export for format: $format")
    }
    
    override suspend fun exportAllRecipes(
        recipes: List<Recipe>,
        cookbooks: List<Cookbook>,
        format: ExportFormat,
        outputStream: OutputStream,
        settings: ExportSettings
    ): ExportFileInfo {
        TODO("Implement all recipes export for format: $format")
    }
    
    override suspend fun createExportFile(
        fileName: String,
        directory: String,
        content: ByteArray
    ): File {
        val dir = File(directory)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val file = File(dir, fileName)
        file.writeBytes(content)
        return file
    }
    
    override fun getExportLocations(): List<ExportLocation> {
        return listOf(
            ExportLocation(
                id = "downloads",
                name = "Downloads",
                path = context.getExternalFilesDir(null)?.path + "/Downloads",
                isAvailable = true,
                isWritable = true
            ),
            ExportLocation(
                id = "app_storage",
                name = "App Storage",
                path = context.filesDir.path,
                isAvailable = true,
                isWritable = true
            ),
            ExportLocation(
                id = "external_storage",
                name = "External Storage",
                path = context.getExternalFilesDir(null)?.path ?: "",
                isAvailable = context.getExternalFilesDir(null) != null,
                isWritable = true,
                requiresPermission = true
            )
        )
    }
    
    override fun isExportLocationAvailable(location: String): Boolean {
        return getExportLocations().any { it.id == location && it.isAvailable }
    }
    
    override fun generateExportFileName(pattern: String, format: ExportFormat): String {
        val timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        val extension = when (format) {
            ExportFormat.JSON -> ".json"
            ExportFormat.MARKDOWN -> ".md"
            ExportFormat.PDF -> ".pdf"
            ExportFormat.DOCX -> ".docx"
        }
        return pattern.replace("{timestamp}", timestamp) + extension
    }
    
    override suspend fun importRecipe(
        inputStream: InputStream,
        format: ImportFormat,
        settings: ImportSettings
    ): Pair<Recipe, ImportFileInfo> {
        TODO("Implement recipe import for format: $format")
    }
    
    override suspend fun importRecipes(
        inputStream: InputStream,
        format: ImportFormat,
        settings: ImportSettings
    ): Pair<List<Recipe>, ImportFileInfo> {
        TODO("Implement multiple recipes import for format: $format")
    }
    
    override suspend fun importCookbook(
        inputStream: InputStream,
        format: ImportFormat,
        settings: ImportSettings
    ): Pair<Cookbook, List<Recipe>, ImportFileInfo> {
        TODO("Implement cookbook import for format: $format")
    }
    
    override suspend fun previewImport(
        filePath: String,
        format: ImportFormat
    ): ExportImportPreview {
        TODO("Implement import preview for format: $format")
    }
    
    override suspend fun validateImportFile(
        filePath: String,
        format: ImportFormat
    ): ImportFileInfo {
        TODO("Implement file validation for format: $format")
    }
    
    override fun detectFileFormat(filePath: String): ImportFormat? {
        return when {
            filePath.endsWith(".json", ignoreCase = true) -> ImportFormat.JSON
            filePath.endsWith(".md", ignoreCase = true) || 
                    filePath.endsWith(".markdown", ignoreCase = true) -> ImportFormat.MARKDOWN
            else -> null
        }
    }
    
    override suspend fun detectFileFormat(inputStream: InputStream): ImportFormat? {
        // Read first few bytes to detect format
        val bytes = ByteArray(8)
        val read = inputStream.read(bytes)
        inputStream.reset()
        
        if (read > 0) {
            val header = String(bytes, 0, read)
            if (header.trim().startsWith("{")) {
                return ImportFormat.JSON
            } else if (header.startsWith("#")) {
                return ImportFormat.MARKDOWN
            }
        }
        return null
    }
    
    override fun listFilesInDirectory(directory: String, extensions: List<String>): List<File> {
        val dir = File(directory)
        if (!dir.exists() || !dir.isDirectory) {
            return emptyList()
        }
        
        return dir.listFiles { file ->
            if (extensions.isEmpty()) {
                true
            } else {
                extensions.any { file.name.endsWith(it, ignoreCase = true) }
            }
        }?.toList() ?: emptyList()
    }
    
    override fun fileExists(filePath: String): Boolean {
        return File(filePath).exists()
    }
    
    override fun getFileSize(filePath: String): Long {
        return File(filePath).length()
    }
    
    override fun deleteFile(filePath: String): Boolean {
        return File(filePath).delete()
    }
    
    override fun copyFile(sourcePath: String, destinationPath: String): Boolean {
        return try {
            File(sourcePath).copyTo(File(destinationPath), overwrite = true)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    override fun moveFile(sourcePath: String, destinationPath: String): Boolean {
        return try {
            File(sourcePath).renameTo(File(destinationPath))
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun saveOperationToHistory(operation: ExportImportOperation) {
        // Save to preferences or database
        // Implementation will use shared preferences for simplicity
        val prefs = context.getSharedPreferences("export_import_history", android.content.Context.MODE_PRIVATE)
        val historyJson = prefs.getString("operation_history", "[]") ?: "[]"
        
        // Simple JSON serialization for history
        // In production, use proper serialization
        val operations = mutableListOf<ExportImportOperation>()
        try {
            // This is a simplified approach - in production use proper JSON parsing
            operations.add(operation)
        } catch (e: Exception) {
            operations.add(operation)
        }
        
        // Limit history to 50 operations
        val limitedOperations = operations.takeLast(50)
        
        // Save back to preferences
        prefs.edit().putString("operation_history", "history").apply()
    }
    
    override suspend fun loadOperationHistory(limit: Int): List<ExportImportOperation> {
        // Load from preferences or database
        val prefs = context.getSharedPreferences("export_import_history", android.content.Context.MODE_PRIVATE)
        val historyJson = prefs.getString("operation_history", "[]") ?: "[]"
        
        // Simple JSON deserialization
        // In production, use proper serialization
        return emptyList()
    }
    
    override suspend fun clearOperationHistory() {
        val prefs = context.getSharedPreferences("export_import_history", android.content.Context.MODE_PRIVATE)
        prefs.edit().remove("operation_history").apply()
    }
    
    override suspend fun deleteOperationFromHistory(operationId: String) {
        val prefs = context.getSharedPreferences("export_import_history", android.content.Context.MODE_PRIVATE)
        val historyJson = prefs.getString("operation_history", "[]") ?: "[]"
        
        // Simple implementation - in production use proper filtering
        prefs.edit().putString("operation_history", "[]").apply()
    }
    
    override suspend fun detectConflicts(
        existingRecipes: List<Recipe>,
        recipesToImport: List<Recipe>
    ): List<ImportConflict> {
        val conflicts = mutableListOf<ImportConflict>()
        
        // Check for duplicate IDs
        conflicts.addAll(detectDuplicateIds(existingRecipes, recipesToImport))
        
        // Check for duplicate titles (only if not already detected as ID duplicates)
        val nonIdDuplicateRecipes = recipesToImport.filter { recipe ->
            existingRecipes.none { it.id == recipe.id }
        }
        conflicts.addAll(detectDuplicateTitles(existingRecipes, nonIdDuplicateRecipes))
        
        return conflicts
    }
    
    override fun detectDuplicateIds(
        existingRecipes: List<Recipe>,
        recipesToImport: List<Recipe>
    ): List<ImportConflict> {
        val conflicts = mutableListOf<ImportConflict>()
        
        recipesToImport.forEach { newRecipe ->
            existingRecipes.find { it.id == newRecipe.id }?.let { existingRecipe ->
                conflicts.add(
                    ImportConflict(
                        recipeId = newRecipe.id,
                        existingRecipe = existingRecipe,
                        newRecipe = newRecipe,
                        conflictType = ConflictType.DUPLICATE_ID
                    )
                )
            }
        }
        
        return conflicts
    }
    
    override fun detectDuplicateTitles(
        existingRecipes: List<Recipe>,
        recipesToImport: List<Recipe>
    ): List<ImportConflict> {
        val conflicts = mutableListOf<ImportConflict>()
        
        recipesToImport.forEach { newRecipe ->
            existingRecipes.find { 
                it.title.equals(newRecipe.title, ignoreCase = true) && 
                it.id != newRecipe.id 
            }?.let { existingRecipe ->
                conflicts.add(
                    ImportConflict(
                        recipeId = newRecipe.id,
                        existingRecipe = existingRecipe,
                        newRecipe = newRecipe,
                        conflictType = ConflictType.DUPLICATE_TITLE
                    )
                )
            }
        }
        
        return conflicts
    }
}
