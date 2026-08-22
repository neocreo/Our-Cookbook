package com.ourcookbook.data.service

import com.ourcookbook.domain.model.Recipe
import java.io.File
import java.io.OutputStream

/**
 * Markdown Exporter for Recipe Export
 * Task 2.2.06: PDF Export Implementation (also handles Markdown)
 *
 * Exports recipes to Markdown format matching the Cookbook folder style
 * Supports single recipe, multiple recipes, or entire cookbooks
 */
class MarkdownExporter {

    /**
     * Export settings for Markdown generation
     */
    data class MarkdownExportSettings(
        val includeImages: Boolean = true,
        val includeMetadata: Boolean = true,
        val includeInstructions: Boolean = true,
        val includeDescription: Boolean = true,
        val separator: String = "---"
    )

    /**
     * Result of Markdown export
     */
    data class MarkdownExportResult(
        val file: File,
        val recipeCount: Int,
        val success: Boolean,
        val errorMessage: String? = null
    )

    /**
     * Export a single recipe to Markdown
     *
     * @param recipe The recipe to export
     * @param outputStream The output stream to write to
     * @param settings Export settings
     * @return MarkdownExportResult with export information
     */
    suspend fun exportRecipe(
        recipe: Recipe,
        outputStream: OutputStream,
        settings: MarkdownExportSettings = MarkdownExportSettings()
    ): MarkdownExportResult {
        return try {
            val markdown = generateRecipeMarkdown(recipe, settings)
            outputStream.write(markdown.toByteArray())
            
            MarkdownExportResult(
                file = File(""),
                recipeCount = 1,
                success = true
            )
        } catch (e: Exception) {
            MarkdownExportResult(
                file = File(""),
                recipeCount = 0,
                success = false,
                errorMessage = e.message
            )
        }
    }

    /**
     * Export multiple recipes to Markdown
     *
     * @param recipes List of recipes to export
     * @param outputFile The output file
     * @param settings Export settings
     * @return MarkdownExportResult with export information
     */
    suspend fun exportRecipes(
        recipes: List<Recipe>,
        outputFile: File,
        settings: MarkdownExportSettings = MarkdownExportSettings()
    ): MarkdownExportResult {
        return try {
            outputFile.parentFile?.mkdirs()
            
            FileOutputStream(outputFile).use { outputStream ->
                recipes.forEachIndexed { index, recipe ->
                    val markdown = generateRecipeMarkdown(recipe, settings)
                    outputStream.write(markdown.toByteArray())
                    
                    // Add separator between recipes (except after the last one)
                    if (index < recipes.size - 1) {
                        outputStream.write("\n\n${settings.separator}\n\n".toByteArray())
                    }
                }
            }
            
            MarkdownExportResult(
                file = outputFile,
                recipeCount = recipes.size,
                success = true
            )
        } catch (e: Exception) {
            MarkdownExportResult(
                file = outputFile,
                recipeCount = 0,
                success = false,
                errorMessage = e.message
            )
        }
    }

    /**
     * Export recipes to a Markdown file at the specified path
     *
     * @param recipes List of recipes to export
     * @param filePath Full path to the output Markdown file
     * @param settings Export settings
     * @return MarkdownExportResult with export information
     */
    suspend fun exportToFile(
        recipes: List<Recipe>,
        filePath: String,
        settings: MarkdownExportSettings = MarkdownExportSettings()
    ): MarkdownExportResult {
        val outputFile = File(filePath)
        return exportRecipes(recipes, outputFile, settings)
    }

    /**
     * Generate Markdown for a single recipe
     *
     * @param recipe The recipe to generate Markdown for
     * @param settings Export settings
     * @return Markdown string
     */
    fun generateRecipeMarkdown(
        recipe: Recipe,
        settings: MarkdownExportSettings = MarkdownExportSettings()
    ): String {
        val markdown = StringBuilder()
        
        // Title
        markdown.appendLine("## ${recipe.title}")
        markdown.appendLine()
        
        // Description
        if (settings.includeDescription && recipe.description.isNotBlank()) {
            markdown.appendLine(recipe.description)
            markdown.appendLine()
        }
        
        // Metadata
        if (settings.includeMetadata) {
            val metadataParts = mutableListOf<String>()
            
            recipe.servingSize?.let { metadataParts.add("Serves: $it") }
            recipe.prepTime?.let { metadataParts.add("Prep Time: $it") }
            recipe.cookTime?.let { metadataParts.add("Cook Time: $it") }
            recipe.totalTime?.let { metadataParts.add("Total Time: $it") }
            
            if (metadataParts.isNotEmpty()) {
                markdown.appendLine("**${metadataParts.joinToString(" | ")}**")
                markdown.appendLine()
            }
        }
        
        // Category
        if (settings.includeMetadata && recipe.category.isNotBlank()) {
            markdown.appendLine("**Category:** ${recipe.category}")
            markdown.appendLine()
        }
        
        // Tags
        if (recipe.tags.isNotEmpty()) {
            markdown.appendLine("**Tags:** ${recipe.tags.joinToString(", ")}")
            markdown.appendLine()
        }
        
        // Source
        if (recipe.source.isNotBlank()) {
            markdown.appendLine("**Source:** ${recipe.source}")
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
        if (settings.includeInstructions && recipe.instructions.isNotEmpty()) {
            markdown.appendLine("### Instructions")
            markdown.appendLine()
            
            recipe.instructions.forEach { instruction ->
                markdown.appendLine(instruction)
                markdown.appendLine()
            }
        }
        
        // Notes
        if (recipe.notes.isNotBlank()) {
            markdown.appendLine("### Notes")
            markdown.appendLine()
            markdown.appendLine(recipe.notes)
            markdown.appendLine()
        }
        
        return markdown.toString()
    }

    /**
     * Generate a file name for Markdown export
     */
    fun generateFileName(recipeOrCookbookName: String): String {
        val safeName = recipeOrCookbookName
            .replace("[^a-zA-Z0-9]+".toRegex(), "_")
            .replace("__+".toRegex(), "_")
            .trim('_')
        
        val timestamp = java.time.LocalDateTime.now()
            .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        
        return "${safeName}_$timestamp.md"
    }

    /**
     * Create a temporary file for Markdown export
     */
    fun createTempFile(context: android.content.Context, prefix: String = "recipe"): File {
        val timestamp = System.currentTimeMillis()
        return File.createTempFile("${prefix}_$timestamp", ".md", context.cacheDir)
    }

    /**
     * Check if Markdown export is supported on this device
     */
    fun isSupported(): Boolean {
        return true
    }
}
