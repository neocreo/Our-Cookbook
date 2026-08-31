package com.ourcookbook.domain.usecase.recipe

import com.ourcookbook.domain.model.Recipe
import java.io.File
import javax.inject.Inject

/**
 * Use case for exporting recipes to DOCX format
 * Task 2.2.07: DOCX Export Implementation
 *
 * Provides a clean domain-layer interface for DOCX export operations
 * 
 * Note: DOCX export on Android is challenging due to library limitations.
 * This implementation provides a basic approach using available libraries.
 * For production, consider using a server-side solution or a more robust library.
 */
class ExportRecipeToDocx @Inject constructor() {

    /**
     * Result of DOCX export operation
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
     * Export settings for DOCX generation
     */
    data class ExportSettings(
        val includeImages: Boolean = true,
        val includeMetadata: Boolean = true,
        val includeInstructions: Boolean = true,
        val fontSize: Int = 12,
        val fontFamily: String = "Arial"
    )

    /**
     * Export a single recipe to DOCX
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
            
            // For now, export as a simple text-based DOCX using HTML
            // In production, use a proper DOCX library like Apache POI or docx4j
            val docxContent = generateDocxContent(listOf(recipe), settings)
            
            outputFile.writeBytes(docxContent.toByteArray())
            
            ExportResult.Success(
                file = outputFile,
                recipeCount = 1
            )
        } catch (e: Exception) {
            ExportResult.Failure(
                errorMessage = "Failed to export recipe to DOCX: ${e.message}",
                exception = e
            )
        }
    }

    /**
     * Export multiple recipes to a single DOCX file
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
            
            // Generate DOCX content
            val docxContent = generateDocxContent(recipes, settings)
            
            outputFile.writeBytes(docxContent.toByteArray())
            
            ExportResult.Success(
                file = outputFile,
                recipeCount = recipes.size
            )
        } catch (e: Exception) {
            ExportResult.Failure(
                errorMessage = "Failed to export recipes to DOCX: ${e.message}",
                exception = e
            )
        }
    }

    /**
     * Export a cookbook to DOCX
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
        
        val fileName = generateFileName(cookbookName)
        val outputFile = File(outputDirectory, fileName)
        
        return exportMultiple(recipes, outputFile, settings)
    }

    /**
     * Generate DOCX content as HTML (simplified approach)
     * 
     * Note: This generates a basic HTML file that can be opened in Word.
     * For true DOCX format, a proper library would be needed.
     */
    private fun generateDocxContent(
        recipes: List<Recipe>,
        settings: ExportSettings
    ): String {
        val html = StringBuilder()
        
        html.appendLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
        html.appendLine("<html xmlns=\"http://www.w3.org/1999/xhtml\">")
        html.appendLine("<head>")
        html.appendLine("  <meta charset=\"UTF-8\" />")
        html.appendLine("  <title>Recipes</title>")
        html.appendLine("  <style>")
        html.appendLine("    body { font-family: ${settings.fontFamily}; font-size: ${settings.fontSize}pt; margin: 2in; }")
        html.appendLine("    h1 { font-size: ${settings.fontSize + 6}pt; font-weight: bold; margin-bottom: 0.5in; }")
        html.appendLine("    h2 { font-size: ${settings.fontSize + 4}pt; font-weight: bold; margin-top: 0.3in; margin-bottom: 0.2in; }")
        html.appendLine("    h3 { font-size: ${settings.fontSize + 2}pt; font-weight: bold; margin-top: 0.2in; }")
        html.appendLine("    .metadata { font-size: ${settings.fontSize}pt; color: #666; margin-bottom: 0.2in; }")
        html.appendLine("    .ingredient { margin-left: 0.3in; margin-bottom: 0.1in; }")
        html.appendLine("    .instruction { margin-left: 0.3in; margin-bottom: 0.1in; }")
        html.appendLine("    .recipe { page-break-after: always; margin-bottom: 0.5in; }")
        html.appendLine("    .tags { font-style: italic; color: #666; }")
        html.appendLine("  </style>")
        html.appendLine("</head>")
        html.appendLine("<body>")
        
        if (recipes.size > 1) {
            html.appendLine("<h1>Recipe Collection</h1>")
            html.appendLine("<p>Contains ${recipes.size} recipes</p>")
            html.appendLine("<hr />")
        }
        
        recipes.forEach { recipe ->
            html.appendLine("<div class=\"recipe\">")
            
            // Title
            html.appendLine("  <h1>${escapeHtml(recipe.title)}</h1>")
            
            // Category
            if (settings.includeMetadata && recipe.category.isNotBlank()) {
                html.appendLine("  <p class=\"metadata\">Category: ${escapeHtml(recipe.category)}</p>")
            }
            
            // Metadata
            if (settings.includeMetadata) {
                val metadataParts = mutableListOf<String>()
                recipe.servingSize?.let { metadataParts.add("Serves: $it") }
                recipe.prepTime?.let { metadataParts.add("Prep: ${it}min") }
                recipe.cookTime?.let { metadataParts.add("Cook: ${it}min") }
                recipe.totalTime?.let { metadataParts.add("Total: ${it}min") }
                
                if (metadataParts.isNotEmpty()) {
                    html.appendLine("  <p class=\"metadata\">${metadataParts.joinToString(" | ")}</p>")
                }
            }
            
            // Description
            if (recipe.description?.isNotBlank() == true) {
                html.appendLine("  <h3>Description</h3>")
                html.appendLine("  <p>${escapeHtml(recipe.description ?: "")}</p>")
            }
            
            // Ingredients
            if (recipe.ingredients.isNotEmpty()) {
                html.appendLine("  <h2>Ingredients</h2>")
                html.appendLine("  <ul>")
                recipe.ingredients.forEach { ingredient ->
                    html.appendLine("    <li class=\"ingredient\">${escapeHtml(buildIngredientText(ingredient))}</li>")
                }
                html.appendLine("  </ul>")
            }
            
            // Instructions
            if (settings.includeInstructions && recipe.instructions.isNotEmpty()) {
                html.appendLine("  <h2>Instructions</h2>")
                html.appendLine("  <ol>")
                recipe.instructions.forEach { instruction ->
                    html.appendLine("    <li class=\"instruction\">${escapeHtml(instruction)}</li>")
                }
                html.appendLine("  </ol>")
            }
            
            // Source
            if (recipe.source?.isNotBlank() == true) {
                html.appendLine("  <p class=\"metadata\">Source: ${escapeHtml(recipe.source ?: "")}</p>")
            }
            
            // Tags
            if (recipe.tags.isNotEmpty()) {
                html.appendLine("  <p class=\"tags\">Tags: ${escapeHtml(recipe.tags.joinToString(", "))}</p>")
            }
            
            html.appendLine("</div>")
            html.appendLine("<hr />")
        }
        
        html.appendLine("</body>")
        html.appendLine("</html>")
        
        return html.toString()
    }

    /**
     * Escape HTML special characters
     */
    private fun escapeHtml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }

    /**
     * Build ingredient text with amount, unit, and name
     */
    private fun buildIngredientText(ingredient: com.ourcookbook.domain.model.Ingredient): String {
        val parts = mutableListOf<String>()
        
        ingredient.amount?.let { parts.add(it) }
        ingredient.unit?.let { parts.add(it) }
        parts.add(ingredient.name)
        ingredient.notes?.let { parts.add("($it)") }
        
        return parts.joinToString(" ")
    }

    /**
     * Generate a file name for DOCX export
     */
    fun generateFileName(recipeOrCookbookName: String): String {
        val safeName = recipeOrCookbookName
            .replace("[^a-zA-Z0-9]+".toRegex(), "_")
            .replace("__+".toRegex(), "_")
            .trim('_')
        
        val timestamp = java.time.LocalDateTime.now()
            .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        
        return "${safeName}_$timestamp.docx"
    }

    /**
     * Create a temporary file for DOCX export
     */
    fun createTempFile(context: android.content.Context, prefix: String = "recipe"): File {
        val timestamp = System.currentTimeMillis()
        return File.createTempFile("${prefix}_$timestamp", ".docx", context.cacheDir)
    }

    /**
     * Check if DOCX export is supported on this device
     * 
     * Note: This simplified HTML-based approach is always supported.
     * True DOCX export would require additional libraries.
     */
    fun isSupported(): Boolean {
        return true
    }

    /**
     * Check if true DOCX format is supported (requires external libraries)
     */
    fun isTrueDocxSupported(): Boolean {
        // Check if required libraries are available
        // For now, return false as we're using HTML-based approach
        return false
    }
}
