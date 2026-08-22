package com.ourcookbook.domain.usecase.recipe

import com.ourcookbook.data.service.PdfExporter
import com.ourcookbook.domain.model.Recipe
import java.io.File
import javax.inject.Inject

/**
 * Use case for exporting recipes to PDF format
 * Task 2.2.06: PDF Export Implementation
 *
 * Provides a clean domain-layer interface for PDF export operations
 */
class ExportRecipeToPdf @Inject constructor(
    private val pdfExporter: PdfExporter
) {

    /**
     * Result of PDF export operation
     */
    sealed class ExportResult {
        data class Success(
            val file: File,
            val pageCount: Int,
            val recipeCount: Int
        ) : ExportResult()
        
        data class Failure(
            val errorMessage: String,
            val exception: Exception? = null
        ) : ExportResult()
    }

    /**
     * Export settings for PDF generation
     */
    data class ExportSettings(
        val pageSize: PdfExporter.PageSize = PdfExporter.PageSize.A4,
        val includeImages: Boolean = true,
        val includeMetadata: Boolean = true,
        val includeInstructions: Boolean = true,
        val fontSize: Float = 12f,
        val showPageNumbers: Boolean = true
    )

    /**
     * Export a single recipe to PDF
     *
     * @param recipe The recipe to export
     * @param outputFile The output file path
     * @param settings Export settings (optional)
     * @return ExportResult with success/failure information
     */
    suspend operator fun invoke(
        recipe: Recipe,
        outputFile: File,
        settings: ExportSettings = ExportSettings()
    ): ExportResult {
        return try {
            // Create parent directories if they don't exist
            outputFile.parentFile?.mkdirs()
            
            // Export the recipe
            val result = pdfExporter.exportRecipes(
                listOf(recipe),
                outputFile,
                PdfExporter.PdfExportSettings(
                    pageSize = settings.pageSize,
                    includeImages = settings.includeImages,
                    includeMetadata = settings.includeMetadata,
                    includeInstructions = settings.includeInstructions,
                    fontSize = settings.fontSize,
                    showPageNumbers = settings.showPageNumbers,
                    recipesPerPage = 1
                )
            )
            
            if (result.success) {
                ExportResult.Success(
                    file = result.file,
                    pageCount = result.pageCount,
                    recipeCount = result.recipeCount
                )
            } else {
                ExportResult.Failure(
                    errorMessage = result.errorMessage ?: "PDF export failed",
                    exception = null
                )
            }
        } catch (e: Exception) {
            ExportResult.Failure(
                errorMessage = "Failed to export recipe to PDF: ${e.message}",
                exception = e
            )
        }
    }

    /**
     * Export multiple recipes to a single PDF file
     *
     * @param recipes List of recipes to export
     * @param outputFile The output file path
     * @param settings Export settings (optional)
     * @return ExportResult with success/failure information
     */
    suspend fun exportMultiple(
        recipes: List<Recipe>,
        outputFile: File,
        settings: ExportSettings = ExportSettings()
    ): ExportResult {
        if (recipes.isEmpty()) {
            return ExportResult.Failure("No recipes to export")
        }
        
        return try {
            // Create parent directories if they don't exist
            outputFile.parentFile?.mkdirs()
            
            // Export all recipes
            val result = pdfExporter.exportRecipes(
                recipes,
                outputFile,
                PdfExporter.PdfExportSettings(
                    pageSize = settings.pageSize,
                    includeImages = settings.includeImages,
                    includeMetadata = settings.includeMetadata,
                    includeInstructions = settings.includeInstructions,
                    fontSize = settings.fontSize,
                    showPageNumbers = settings.showPageNumbers,
                    recipesPerPage = 1
                )
            )
            
            if (result.success) {
                ExportResult.Success(
                    file = result.file,
                    pageCount = result.pageCount,
                    recipeCount = result.recipeCount
                )
            } else {
                ExportResult.Failure(
                    errorMessage = result.errorMessage ?: "PDF export failed",
                    exception = null
                )
            }
        } catch (e: Exception) {
            ExportResult.Failure(
                errorMessage = "Failed to export recipes to PDF: ${e.message}",
                exception = e
            )
        }
    }

    /**
     * Export a cookbook to PDF
     *
     * @param cookbookName Name of the cookbook (for file naming)
     * @param recipes List of recipes in the cookbook
     * @param outputDirectory The output directory
     * @param settings Export settings (optional)
     * @return ExportResult with success/failure information
     */
    suspend fun exportCookbook(
        cookbookName: String,
        recipes: List<Recipe>,
        outputDirectory: File,
        settings: ExportSettings = ExportSettings()
    ): ExportResult {
        if (recipes.isEmpty()) {
            return ExportResult.Failure("Cookbook has no recipes to export")
        }
        
        val fileName = pdfExporter.generateFileName(cookbookName)
        val outputFile = File(outputDirectory, fileName)
        
        return exportMultiple(recipes, outputFile, settings)
    }

    /**
     * Generate a temporary file for PDF export
     *
     * @param context Android context
     * @param prefix File name prefix
     * @return Temporary file
     */
    fun createTempFile(context: android.content.Context, prefix: String = "recipe"): File {
        val timestamp = System.currentTimeMillis()
        return File.createTempFile("${prefix}_$timestamp", ".pdf", context.cacheDir)
    }

    /**
     * Check if PDF export is supported on this device
     */
    fun isSupported(): Boolean {
        return pdfExporter.isSupported()
    }
}
