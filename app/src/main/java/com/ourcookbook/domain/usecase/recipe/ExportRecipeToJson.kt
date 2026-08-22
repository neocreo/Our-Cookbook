package com.ourcookbook.domain.usecase.recipe

import com.ourcookbook.data.service.JsonExporter
import com.ourcookbook.domain.model.Recipe
import java.io.File
import javax.inject.Inject

/**
 * Use case for exporting recipes to JSON format
 * Task 2.2.06: PDF Export Implementation (also handles JSON)
 *
 * Provides a clean domain-layer interface for JSON export operations
 */
class ExportRecipeToJson @Inject constructor(
    private val jsonExporter: JsonExporter
) {

    /**
     * Result of JSON export operation
     */
    sealed class ExportResult {
        data class Success(
            val file: File,
            val recipeCount: Int
        ) : ExportResult()
        
        data class Failure(
            val errorMessage: String,
            val exception: Exception? = null
        ) : ExportResult()
    }

    /**
     * Export settings for JSON generation
     */
    data class ExportSettings(
        val prettyPrint: Boolean = true,
        val includeImages: Boolean = true,
        val includeMetadata: Boolean = true
    )

    /**
     * Export a single recipe to JSON
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
            val result = jsonExporter.exportRecipes(
                listOf(recipe),
                outputFile,
                JsonExporter.JsonExportSettings(
                    prettyPrint = settings.prettyPrint,
                    includeImages = settings.includeImages,
                    includeMetadata = settings.includeMetadata
                )
            )
            
            if (result.success) {
                ExportResult.Success(
                    file = result.file,
                    recipeCount = result.recipeCount
                )
            } else {
                ExportResult.Failure(
                    errorMessage = result.errorMessage ?: "JSON export failed",
                    exception = null
                )
            }
        } catch (e: Exception) {
            ExportResult.Failure(
                errorMessage = "Failed to export recipe to JSON: ${e.message}",
                exception = e
            )
        }
    }

    /**
     * Export multiple recipes to a single JSON file
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
            val result = jsonExporter.exportRecipes(
                recipes,
                outputFile,
                JsonExporter.JsonExportSettings(
                    prettyPrint = settings.prettyPrint,
                    includeImages = settings.includeImages,
                    includeMetadata = settings.includeMetadata
                )
            )
            
            if (result.success) {
                ExportResult.Success(
                    file = result.file,
                    recipeCount = result.recipeCount
                )
            } else {
                ExportResult.Failure(
                    errorMessage = result.errorMessage ?: "JSON export failed",
                    exception = null
                )
            }
        } catch (e: Exception) {
            ExportResult.Failure(
                errorMessage = "Failed to export recipes to JSON: ${e.message}",
                exception = e
            )
        }
    }

    /**
     * Export a cookbook to JSON
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
        
        val fileName = jsonExporter.generateFileName(cookbookName)
        val outputFile = File(outputDirectory, fileName)
        
        return exportMultiple(recipes, outputFile, settings)
    }

    /**
     * Import recipes from a JSON file
     *
     * @param file The JSON file to import from
     * @return Pair of recipe list and error message (if any)
     */
    suspend fun importFromFile(file: File): Pair<List<Recipe>, String?> {
        return jsonExporter.importFromFile(file)
    }

    /**
     * Generate a temporary file for JSON export
     */
    fun createTempFile(context: android.content.Context, prefix: String = "recipe"): File {
        return jsonExporter.createTempFile(context, prefix)
    }

    /**
     * Check if JSON export is supported on this device
     */
    fun isSupported(): Boolean {
        return jsonExporter.isSupported()
    }
}
