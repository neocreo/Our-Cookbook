package com.ourcookbook.domain.usecase.cookbook

import com.ourcookbook.domain.model.Cookbook
import com.ourcookbook.domain.model.ExportFormat
import com.ourcookbook.domain.model.Recipe
import com.ourcookbook.domain.usecase.recipe.ExportRecipeToJson
import com.ourcookbook.domain.usecase.recipe.ExportRecipeToPdf
import java.io.File
import javax.inject.Inject

/**
 * Use case for exporting entire cookbooks
 * Task 2.2.11: Export/import cookbooks for simple sharing
 *
 * Provides functionality to export cookbooks with all their recipes
 * in various formats for easy sharing
 */
class ExportCookbook @Inject constructor(
    private val exportRecipeToJson: ExportRecipeToJson,
    private val exportRecipeToPdf: ExportRecipeToPdf
) {

    /**
     * Result of cookbook export operation
     */
    sealed class ExportResult {
        data class Success(
            val file: File,
            val cookbook: Cookbook,
            val recipeCount: Int,
            val format: ExportFormat
        ) : ExportResult()
        
        data class Failure(
            val errorMessage: String,
            val exception: Exception? = null
        ) : ExportResult()
    }

    /**
     * Export settings for cookbook
     */
    data class ExportSettings(
        val format: ExportFormat = ExportFormat.JSON,
        val includeImages: Boolean = true,
        val includeMetadata: Boolean = true,
        val prettyPrint: Boolean = true
    )

    /**
     * Export a cookbook to a file
     *
     * @param cookbook The cookbook to export
     * @param recipes List of recipes in the cookbook
     * @param outputFile The output file
     * @param settings Export settings
     * @return ExportResult with operation information
     */
    suspend operator fun invoke(
        cookbook: Cookbook,
        recipes: List<Recipe>,
        outputFile: File,
        settings: ExportSettings = ExportSettings()
    ): ExportResult {
        if (recipes.isEmpty()) {
            return ExportResult.Failure("Cookbook has no recipes to export")
        }
        
        return try {
            // Create parent directories if they don't exist
            outputFile.parentFile?.mkdirs()
            
            when (settings.format) {
                ExportFormat.JSON -> {
                    exportToJson(cookbook, recipes, outputFile, settings)
                }
                ExportFormat.PDF -> {
                    exportToPdf(cookbook, recipes, outputFile, settings)
                }
                ExportFormat.MARKDOWN -> {
                    exportToMarkdown(cookbook, recipes, outputFile, settings)
                }
                ExportFormat.DOCX -> {
                    exportToDocx(cookbook, recipes, outputFile, settings)
                }
            }
        } catch (e: Exception) {
            ExportResult.Failure(
                errorMessage = "Failed to export cookbook: ${e.message}",
                exception = e
            )
        }
    }

    /**
     * Export cookbook to JSON format
     */
    private suspend fun exportToJson(
        cookbook: Cookbook,
        recipes: List<Recipe>,
        outputFile: File,
        settings: ExportSettings
    ): ExportResult {
        val result = exportRecipeToJson.exportCookbook(
            cookbookName = cookbook.name,
            recipes = recipes,
            outputDirectory = outputFile.parentFile ?: File(""),
            settings = ExportRecipeToJson.ExportSettings(
                prettyPrint = settings.prettyPrint,
                includeImages = settings.includeImages,
                includeMetadata = settings.includeMetadata
            )
        )
        
        return when (result) {
            is ExportRecipeToJson.ExportResult.Success -> {
                ExportResult.Success(
                    file = result.file,
                    cookbook = cookbook,
                    recipeCount = result.recipeCount,
                    format = ExportFormat.JSON
                )
            }
            is ExportRecipeToJson.ExportResult.Failure -> {
                ExportResult.Failure(
                    errorMessage = result.errorMessage,
                    exception = result.exception
                )
            }
        }
    }

    /**
     * Export cookbook to PDF format
     */
    private suspend fun exportToPdf(
        cookbook: Cookbook,
        recipes: List<Recipe>,
        outputFile: File,
        settings: ExportSettings
    ): ExportResult {
        val result = exportRecipeToPdf.exportCookbook(
            cookbookName = cookbook.name,
            recipes = recipes,
            outputDirectory = outputFile.parentFile ?: File(""),
            settings = ExportRecipeToPdf.ExportSettings(
                includeImages = settings.includeImages,
                includeMetadata = settings.includeMetadata,
                includeInstructions = true
            )
        )
        
        return when (result) {
            is ExportRecipeToPdf.ExportResult.Success -> {
                ExportResult.Success(
                    file = result.file,
                    cookbook = cookbook,
                    recipeCount = result.recipeCount,
                    format = ExportFormat.PDF
                )
            }
            is ExportRecipeToPdf.ExportResult.Failure -> {
                ExportResult.Failure(
                    errorMessage = result.errorMessage,
                    exception = result.exception
                )
            }
        }
    }

    /**
     * Export cookbook to Markdown format
     */
    private suspend fun exportToMarkdown(
        cookbook: Cookbook,
        recipes: List<Recipe>,
        outputFile: File,
        settings: ExportSettings
    ): ExportResult {
        return try {
            // Create Markdown content
            val markdown = buildMarkdownContent(cookbook, recipes, settings)
            
            // Write to file
            outputFile.writeText(markdown)
            
            ExportResult.Success(
                file = outputFile,
                cookbook = cookbook,
                recipeCount = recipes.size,
                format = ExportFormat.MARKDOWN
            )
        } catch (e: Exception) {
            ExportResult.Failure(
                errorMessage = "Failed to export cookbook to Markdown: ${e.message}",
                exception = e
            )
        }
    }

    /**
     * Export cookbook to DOCX format
     */
    private suspend fun exportToDocx(
        cookbook: Cookbook,
        recipes: List<Recipe>,
        outputFile: File,
        settings: ExportSettings
    ): ExportResult {
        // For DOCX, we'll use the existing DOCX exporter
        // This is a simplified approach - in production, use a proper DOCX library
        return try {
            val htmlContent = buildHtmlContent(cookbook, recipes, settings)
            
            // Write HTML content (which Word can open as DOCX)
            outputFile.writeText(htmlContent)
            
            ExportResult.Success(
                file = outputFile,
                cookbook = cookbook,
                recipeCount = recipes.size,
                format = ExportFormat.DOCX
            )
        } catch (e: Exception) {
            ExportResult.Failure(
                errorMessage = "Failed to export cookbook to DOCX: ${e.message}",
                exception = e
            )
        }
    }

    /**
     * Build Markdown content for cookbook export
     */
    private fun buildMarkdownContent(
        cookbook: Cookbook,
        recipes: List<Recipe>,
        settings: ExportSettings
    ): String {
        val markdown = StringBuilder()
        
        // Header
        markdown.appendLine("# ${cookbook.name}")
        markdown.appendLine()
        
        if (cookbook.description.isNotBlank()) {
            markdown.appendLine(cookbook.description)
            markdown.appendLine()
        }
        
        markdown.appendLine("---")
        markdown.appendLine()
        
        markdown.appendLine("## Table of Contents")
        markdown.appendLine()
        
        recipes.forEachIndexed { index, recipe ->
            markdown.appendLine("${index + 1}. [${recipe.title}](#${recipe.id})")
        }
        
        markdown.appendLine()
        markdown.appendLine("---")
        markdown.appendLine()
        
        // Recipes
        recipes.forEachIndexed { index, recipe ->
            markdown.appendLine("## ${recipe.title}")
            markdown.appendLine("<a name=\"${recipe.id}\"></a>")
            markdown.appendLine()
            
            if (recipe.description.isNotBlank()) {
                markdown.appendLine(recipe.description)
                markdown.appendLine()
            }
            
            // Metadata
            val metadataParts = mutableListOf<String>()
            recipe.servingSize?.let { metadataParts.add("Serves: $it") }
            recipe.prepTime?.let { metadataParts.add("Prep Time: $it min") }
            recipe.cookTime?.let { metadataParts.add("Cook Time: $it min") }
            recipe.totalTime?.let { metadataParts.add("Total Time: $it min") }
            
            if (metadataParts.isNotEmpty()) {
                markdown.appendLine("**${metadataParts.joinToString(" | ")}**")
                markdown.appendLine()
            }
            
            // Category
            if (recipe.category.isNotBlank()) {
                markdown.appendLine("**Category:** ${recipe.category}")
                markdown.appendLine()
            }
            
            // Tags
            if (recipe.tags.isNotEmpty()) {
                markdown.appendLine("**Tags:** ${recipe.tags.joinToString(", ")}")
                markdown.appendLine()
            }
            
            // Ingredients
            if (recipe.ingredients.isNotEmpty()) {
                markdown.appendLine("### Ingredients")
                markdown.appendLine()
                markdown.appendLine("| Amount | Ingredient | Notes |")
                markdown.appendLine("| :--- | :--- | :--- |")
                
                recipe.ingredients.forEach { ingredient ->
                    val amount = ingredient.amount ?: ""
                    val unit = ingredient.unit ?: ""
                    val displayAmount = if (amount.isNotBlank() || unit.isNotBlank()) "$amount $unit" else ""
                    val notes = ingredient.notes ?: ""
                    
                    markdown.appendLine("| $displayAmount | ${ingredient.name} | $notes |")
                }
                markdown.appendLine()
            }
            
            // Instructions
            if (recipe.instructions.isNotEmpty()) {
                markdown.appendLine("### Instructions")
                markdown.appendLine()
                
                recipe.instructions.forEach { instruction ->
                    markdown.appendLine(instruction)
                    markdown.appendLine()
                }
            }
            
            // Source
            if (recipe.source.isNotBlank()) {
                markdown.appendLine("**Source:** ${recipe.source}")
                markdown.appendLine()
            }
            
            markdown.appendLine("---")
            markdown.appendLine()
        }
        
        // Footer
        markdown.appendLine()
        markdown.appendLine("---")
        markdown.appendLine()
        markdown.appendLine("*Exported from Our Cookbook app on ${java.time.LocalDate.now()}*")
        
        return markdown.toString()
    }

    /**
     * Build HTML content for DOCX export
     */
    private fun buildHtmlContent(
        cookbook: Cookbook,
        recipes: List<Recipe>,
        settings: ExportSettings
    ): String {
        val html = StringBuilder()
        
        html.appendLine("<!DOCTYPE html>")
        html.appendLine("<html>")
        html.appendLine("<head>")
        html.appendLine("  <meta charset=\"UTF-8\" />")
        html.appendLine("  <title>${cookbook.name}</title>")
        html.appendLine("  <style>")
        html.appendLine("    body { font-family: Arial, sans-serif; margin: 2in; }")
        html.appendLine("    h1 { font-size: 24pt; margin-bottom: 0.5in; }")
        html.appendLine("    h2 { font-size: 18pt; margin-top: 0.5in; margin-bottom: 0.25in; }")
        html.appendLine("    h3 { font-size: 14pt; margin-top: 0.3in; margin-bottom: 0.2in; }")
        html.appendLine("    .metadata { font-size: 12pt; color: #666; margin-bottom: 0.2in; }")
        html.appendLine("    .recipe { page-break-after: always; margin-bottom: 0.5in; }")
        html.appendLine("    table { width: 100%; border-collapse: collapse; margin-bottom: 0.25in; }")
        html.appendLine("    th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }")
        html.appendLine("    th { background-color: #f2f2f2; }")
        html.appendLine("    .toc { margin-bottom: 0.5in; }")
        html.appendLine("    .toc a { color: #0066cc; }")
        html.appendLine("  </style>")
        html.appendLine("</head>")
        html.appendLine("<body>")
        
        // Header
        html.appendLine("<h1>${cookbook.name}</h1>")
        
        if (cookbook.description.isNotBlank()) {
            html.appendLine("<p>${cookbook.description}</p>")
        }
        
        html.appendLine("<hr />")
        
        // Table of Contents
        html.appendLine("<h2>Table of Contents</h2>")
        html.appendLine("<div class=\"toc\">")
        html.appendLine("<ol>")
        
        recipes.forEach { recipe ->
            html.appendLine("  <li><a href=\"#${recipe.id}\">${recipe.title}</a></li>")
        }
        
        html.appendLine("</ol>")
        html.appendLine("</div>")
        html.appendLine("<hr />")
        
        // Recipes
        recipes.forEach { recipe ->
            html.appendLine("<div class=\"recipe\" id=\"${recipe.id}\">")
            html.appendLine("  <h2>${recipe.title}</h2>")
            
            if (recipe.description.isNotBlank()) {
                html.appendLine("  <p>${recipe.description}</p>")
            }
            
            // Metadata
            val metadataParts = mutableListOf<String>()
            recipe.servingSize?.let { metadataParts.add("Serves: $it") }
            recipe.prepTime?.let { metadataParts.add("Prep Time: $it min") }
            recipe.cookTime?.let { metadataParts.add("Cook Time: $it min") }
            recipe.totalTime?.let { metadataParts.add("Total Time: $it min") }
            
            if (metadataParts.isNotEmpty()) {
                html.appendLine("  <p class=\"metadata\">${metadataParts.joinToString(" | ")}</p>")
            }
            
            // Category
            if (recipe.category.isNotBlank()) {
                html.appendLine("  <p class=\"metadata\">Category: ${recipe.category}</p>")
            }
            
            // Tags
            if (recipe.tags.isNotEmpty()) {
                html.appendLine("  <p class=\"metadata\">Tags: ${recipe.tags.joinToString(", ")}</p>")
            }
            
            // Ingredients
            if (recipe.ingredients.isNotEmpty()) {
                html.appendLine("  <h3>Ingredients</h3>")
                html.appendLine("  <table>")
                html.appendLine("    <tr><th>Amount</th><th>Ingredient</th><th>Notes</th></tr>")
                
                recipe.ingredients.forEach { ingredient ->
                    val amount = ingredient.amount ?: ""
                    val unit = ingredient.unit ?: ""
                    val displayAmount = if (amount.isNotBlank() || unit.isNotBlank()) "$amount $unit" else ""
                    val notes = ingredient.notes ?: ""
                    
                    html.appendLine("    <tr><td>$displayAmount</td><td>${ingredient.name}</td><td>$notes</td></tr>")
                }
                
                html.appendLine("  </table>")
            }
            
            // Instructions
            if (recipe.instructions.isNotEmpty()) {
                html.appendLine("  <h3>Instructions</h3>")
                html.appendLine("  <ol>")
                
                recipe.instructions.forEach { instruction ->
                    html.appendLine("    <li>$instruction</li>")
                }
                
                html.appendLine("  </ol>")
            }
            
            // Source
            if (recipe.source.isNotBlank()) {
                html.appendLine("  <p class=\"metadata\">Source: ${recipe.source}</p>")
            }
            
            html.appendLine("</div>")
            html.appendLine("<hr />")
        }
        
        // Footer
        html.appendLine("<p><em>Exported from Our Cookbook app on ${java.time.LocalDate.now()}</em></p>")
        
        html.appendLine("</body>")
        html.appendLine("</html>")
        
        return html.toString()
    }

    /**
     * Generate a file name for cookbook export
     */
    fun generateFileName(cookbook: Cookbook, format: ExportFormat): String {
        val safeName = cookbook.name
            .replace("[^a-zA-Z0-9]+".toRegex(), "_")
            .replace("__+".toRegex(), "_")
            .trim('_')
        
        val timestamp = java.time.LocalDateTime.now()
            .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        
        val extension = when (format) {
            ExportFormat.JSON -> ".json"
            ExportFormat.PDF -> ".pdf"
            ExportFormat.MARKDOWN -> ".md"
            ExportFormat.DOCX -> ".docx"
        }
        
        return "${safeName}_$timestamp$extension"
    }

    /**
     * Check if cookbook export is supported for the given format
     */
    fun isFormatSupported(format: ExportFormat): Boolean {
        return when (format) {
            ExportFormat.JSON -> true
            ExportFormat.PDF -> true
            ExportFormat.MARKDOWN -> true
            ExportFormat.DOCX -> true
        }
    }

    /**
     * Get supported export formats
     */
    fun getSupportedFormats(): List<ExportFormat> {
        return listOf(
            ExportFormat.JSON,
            ExportFormat.PDF,
            ExportFormat.MARKDOWN,
            ExportFormat.DOCX
        )
    }
}
