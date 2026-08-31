package com.ourcookbook.data.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.ourcookbook.domain.model.Recipe
import com.ourcookbook.domain.model.Cookbook
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

/**
 * JSON Exporter for Recipe Export
 * Task 2.2.06: PDF Export Implementation (also handles JSON)
 *
 * Exports recipes to JSON format for easy import/export
 * Supports single recipe, multiple recipes, or entire cookbooks
 */
class JsonExporter {

    private val objectMapper: ObjectMapper = jacksonObjectMapper()

    /**
     * Export settings for JSON generation
     */
    data class JsonExportSettings(
        val prettyPrint: Boolean = true,
        val includeImages: Boolean = true,
        val includeMetadata: Boolean = true
    )

    /**
     * Result of JSON export
     */
    data class JsonExportResult(
        val file: File,
        val recipeCount: Int,
        val success: Boolean,
        val errorMessage: String? = null
    )

    /**
     * Export a single recipe to JSON
     *
     * @param recipe The recipe to export
     * @param outputStream The output stream to write to
     * @param settings Export settings
     * @return JsonExportResult with export information
     */
    suspend fun exportRecipe(
        recipe: Recipe,
        outputStream: OutputStream,
        settings: JsonExportSettings = JsonExportSettings()
    ): JsonExportResult {
        return try {
            val json = if (settings.prettyPrint) {
                objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(recipe)
            } else {
                objectMapper.writeValueAsString(recipe)
            }
            
            outputStream.write(json.toByteArray())
            
            JsonExportResult(
                file = File(""),
                recipeCount = 1,
                success = true
            )
        } catch (e: Exception) {
            JsonExportResult(
                file = File(""),
                recipeCount = 0,
                success = false,
                errorMessage = e.message
            )
        }
    }

    /**
     * Export multiple recipes to JSON
     *
     * @param recipes List of recipes to export
     * @param outputFile The output file
     * @param settings Export settings
     * @return JsonExportResult with export information
     */
    suspend fun exportRecipes(
        recipes: List<Recipe>,
        outputFile: File,
        settings: JsonExportSettings = JsonExportSettings()
    ): JsonExportResult {
        return try {
            outputFile.parentFile?.mkdirs()
            
            FileOutputStream(outputFile).use { outputStream ->
                val exportData = mapOf(
                    "version" to "1.0",
                    "exportDate" to java.time.Instant.now().toString(),
                    "recipeCount" to recipes.size,
                    "recipes" to recipes
                )
                
                val json = if (settings.prettyPrint) {
                    objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(exportData)
                } else {
                    objectMapper.writeValueAsString(exportData)
                }
                
                outputStream.write(json.toByteArray())
            }
            
            JsonExportResult(
                file = outputFile,
                recipeCount = recipes.size,
                success = true
            )
        } catch (e: Exception) {
            JsonExportResult(
                file = outputFile,
                recipeCount = 0,
                success = false,
                errorMessage = e.message
            )
        }
    }

    /**
     * Export a cookbook to JSON
     *
     * @param cookbook The cookbook to export
     * @param recipes List of recipes in the cookbook
     * @param outputFile The output file
     * @param settings Export settings
     * @return JsonExportResult with export information
     */
    suspend fun exportCookbook(
        cookbook: Cookbook,
        recipes: List<Recipe>,
        outputFile: File,
        settings: JsonExportSettings = JsonExportSettings()
    ): JsonExportResult {
        return try {
            outputFile.parentFile?.mkdirs()
            
            FileOutputStream(outputFile).use { outputStream ->
                val exportData = mapOf(
                    "version" to "1.0",
                    "exportDate" to java.time.Instant.now().toString(),
                    "cookbook" to mapOf(
                        "id" to cookbook.id,
                        "name" to cookbook.name,
                        "description" to cookbook.description,
                        "createdAt" to cookbook.createdAt.toString()
                    ),
                    "recipeCount" to recipes.size,
                    "recipes" to recipes
                )
                
                val json = if (settings.prettyPrint) {
                    objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(exportData)
                } else {
                    objectMapper.writeValueAsString(exportData)
                }
                
                outputStream.write(json.toByteArray())
            }
            
            JsonExportResult(
                file = outputFile,
                recipeCount = recipes.size,
                success = true
            )
        } catch (e: Exception) {
            JsonExportResult(
                file = outputFile,
                recipeCount = 0,
                success = false,
                errorMessage = e.message
            )
        }
    }

    /**
     * Export recipes to a JSON file at the specified path
     *
     * @param recipes List of recipes to export
     * @param filePath Full path to the output JSON file
     * @param settings Export settings
     * @return JsonExportResult with export information
     */
    suspend fun exportToFile(
        recipes: List<Recipe>,
        filePath: String,
        settings: JsonExportSettings = JsonExportSettings()
    ): JsonExportResult {
        val outputFile = File(filePath)
        return exportRecipes(recipes, outputFile, settings)
    }

    /**
     * Import recipes from a JSON file
     *
     * @param file The JSON file to import from
     * @return Pair of recipe list and error message (if any)
     */
    suspend fun importFromFile(file: File): Pair<List<Recipe>, String?> {
        return try {
            val json = file.readText()
            
            // Try to parse as direct recipe array or as export format
            val recipes = try {
                // Try parsing as export format with wrapper
                val node = objectMapper.readTree(json)
                if (node.has("recipes")) {
                    node.get("recipes").toList().map {
                        objectMapper.treeToValue(it, Recipe::class.java)
                    }
                } else {
                    // Try parsing as direct array
                    objectMapper.readValue<List<Recipe>>(json)
                }
            } catch (e: Exception) {
                // Try parsing as single recipe
                listOf(objectMapper.readValue(json))
            }
            
            Pair(recipes, null)
        } catch (e: Exception) {
            Pair(emptyList(), e.message ?: "Failed to import JSON")
        }
    }

    /**
     * Generate a file name for JSON export
     */
    fun generateFileName(recipeOrCookbookName: String): String {
        val safeName = recipeOrCookbookName
            .replace("[^a-zA-Z0-9]+".toRegex(), "_")
            .replace("__+".toRegex(), "_")
            .trim('_')
        
        val timestamp = java.time.LocalDateTime.now()
            .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        
        return "${safeName}_$timestamp.json"
    }

    /**
     * Create a temporary file for JSON export
     */
    fun createTempFile(context: android.content.Context, prefix: String = "recipe"): File {
        val timestamp = System.currentTimeMillis()
        return File.createTempFile("${prefix}_$timestamp", ".json", context.cacheDir)
    }

    /**
     * Check if JSON export is supported on this device
     */
    fun isSupported(): Boolean {
        return true
    }
}
