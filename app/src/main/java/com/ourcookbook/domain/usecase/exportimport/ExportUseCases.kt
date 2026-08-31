package com.ourcookbook.domain.usecase.exportimport

import com.ourcookbook.domain.model.*
import com.ourcookbook.domain.repository.ExportImportRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import javax.inject.Inject

/**
 * Use Cases for Export Operations
 * Task 2.1.09: Export/Import Screen Implementation
 */

class ExportUseCases @Inject constructor(
    private val repository: ExportImportRepository
) {
    
    /**
     * Export a single recipe
     */
    suspend operator fun invoke(
        recipeId: String,
        format: ExportFormat,
        settings: ExportSettings = ExportSettings()
    ): Result<ExportFileInfo> {
        return try {
            val fileInfo = repository.exportRecipe(recipeId, format, settings)
            Result.success(fileInfo)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Export multiple recipes
     */
    suspend fun exportRecipes(
        recipeIds: List<String>,
        format: ExportFormat,
        settings: ExportSettings = ExportSettings()
    ): Result<ExportFileInfo> {
        return try {
            val fileInfo = repository.exportRecipes(recipeIds, format, settings)
            Result.success(fileInfo)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Export a cookbook
     */
    suspend fun exportCookbook(
        cookbookId: String,
        format: ExportFormat,
        settings: ExportSettings = ExportSettings()
    ): Result<ExportFileInfo> {
        return try {
            val fileInfo = repository.exportCookbook(cookbookId, format, settings)
            Result.success(fileInfo)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Export all recipes
     */
    suspend fun exportAllRecipes(
        format: ExportFormat,
        settings: ExportSettings = ExportSettings()
    ): Result<ExportFileInfo> {
        return try {
            val fileInfo = repository.exportAllRecipes(format, settings)
            Result.success(fileInfo)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Batch export multiple items
     */
    suspend fun batchExport(
        items: List<String>,
        format: ExportFormat,
        settings: ExportSettings = ExportSettings(),
        onProgress: (Int, Int) -> Unit = { _, _ -> }
    ): Result<BatchOperationResult> {
        return try {
            val result = repository.batchExport(items, format, settings, onProgress)
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get available export locations
     */
    fun getExportLocations(): List<com.ourcookbook.data.datasource.ExportLocation> {
        return repository.getExportLocations()
    }
    
    /**
     * Check if export location is available
     */
    fun isExportLocationAvailable(location: String): Boolean {
        return repository.isExportLocationAvailable(location)
    }
    
    /**
     * Generate export file name
     */
    fun generateExportFileName(pattern: String, format: ExportFormat): String {
        return when (format) {
            ExportFormat.JSON -> pattern.replace("{timestamp}", 
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))) + ".json"
            ExportFormat.MARKDOWN -> pattern.replace("{timestamp}", 
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))) + ".md"
            ExportFormat.PDF -> pattern.replace("{timestamp}", 
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))) + ".pdf"
            ExportFormat.DOCX -> pattern.replace("{timestamp}", 
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))) + ".docx"
        }
    }
    
    /**
     * Validate export settings
     */
    fun validateExportSettings(settings: ExportSettings): List<String> {
        val errors = mutableListOf<String>()
        
        // Check if export location is valid
        if (!isExportLocationAvailable(settings.exportLocation)) {
            errors.add("Selected export location is not available")
        }
        
        // Check if file name pattern is valid
        if (settings.fileNamePattern.isBlank()) {
            errors.add("File name pattern cannot be empty")
        }
        
        return errors
    }
    
    /**
     * Get file size estimate for export
     */
    suspend fun getExportSizeEstimate(
        recipeIds: List<String>? = null,
        cookbookIds: List<String>? = null,
        format: ExportFormat = ExportFormat.JSON
    ): Long {
        // This is a rough estimate based on average recipe size
        val averageRecipeSize = when (format) {
            ExportFormat.JSON -> 2000L // ~2KB per recipe in JSON
            ExportFormat.MARKDOWN -> 1500L // ~1.5KB per recipe in Markdown
            ExportFormat.PDF -> 50000L // ~50KB per recipe in PDF
            ExportFormat.DOCX -> 20000L // ~20KB per recipe in DOCX
        }
        
        val count = recipeIds?.size ?: cookbookIds?.size ?: 0
        return count * averageRecipeSize
    }
    
    /**
     * Check if there's enough space for export
     */
    suspend fun checkExportSpace(
        estimatedSize: Long,
        location: String
    ): Boolean {
        val locationInfo = getExportLocations().find { it.id == location }
        if (locationInfo == null) return false
        
        try {
            val stat = android.os.StatFs(locationInfo.path)
            val availableBytes = stat.availableBytes
            return availableBytes > estimatedSize
        } catch (e: Exception) {
            return false
        }
    }
    
    /**
     * Export to Google Drive
     */
    suspend fun exportToDrive(
        filePath: String,
        folderId: String? = null,
        fileName: String? = null
    ): Result<ExportFileInfo> {
        return try {
            val fileInfo = repository.exportToDrive(filePath, folderId, fileName)
            if (fileInfo != null) {
                Result.success(fileInfo)
            } else {
                Result.failure(Exception("Failed to export to Google Drive"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Create export operation for tracking
     */
    fun createExportOperation(
        type: ExportTarget,
        format: ExportFormat,
        itemCount: Int
    ): ExportImportOperation {
        return ExportImportOperation(
            id = java.util.UUID.randomUUID().toString(),
            type = OperationType.EXPORT,
            format = format.name,
            target = type.name,
            status = OperationStatus.PENDING,
            totalItems = itemCount,
            processedItems = 0
        )
    }
    
    /**
     * Update export operation progress
     */
    fun updateExportOperationProgress(
        operation: ExportImportOperation,
        processedItems: Int,
        totalItems: Int
    ): ExportImportOperation {
        val progress = if (totalItems > 0) processedItems.toFloat() / totalItems else 0f
        return operation.copy(
            status = if (processedItems >= totalItems) OperationStatus.COMPLETED else OperationStatus.IN_PROGRESS,
            processedItems = processedItems,
            progress = progress
        )
    }
}
