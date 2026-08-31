package com.ourcookbook.domain.usecase.cookbook

import com.ourcookbook.domain.model.Cookbook
import com.ourcookbook.domain.model.ImportFormat
import com.ourcookbook.domain.model.Recipe
import com.ourcookbook.domain.repository.CookbookRepository
import com.ourcookbook.domain.repository.RecipeRepository
import com.ourcookbook.domain.usecase.recipe.ImportRecipeFromJson
import com.ourcookbook.domain.usecase.recipe.ImportRecipeFromMarkdown
import java.io.File
import javax.inject.Inject

/**
 * Use case for importing cookbooks
 * Task 2.2.11: Export/import cookbooks for simple sharing
 *
 * Provides functionality to import cookbooks from various formats
 */
class ImportCookbook @Inject constructor(
    private val cookbookRepository: CookbookRepository,
    private val recipeRepository: RecipeRepository,
    private val importRecipeFromJson: ImportRecipeFromJson,
    private val importRecipeFromMarkdown: ImportRecipeFromMarkdown
) {

    /**
     * Result of cookbook import operation
     */
    sealed class ImportResult {
        data class Success(
            val cookbook: Cookbook,
            val importedRecipes: List<Recipe>,
            val importedCount: Int,
            val skippedCount: Int,
            val conflictCount: Int,
            val format: ImportFormat
        ) : ImportResult()
        
        data class Failure(
            val errorMessage: String,
            val exception: Exception? = null
        ) : ImportResult()
    }

    /**
     * Import settings
     */
    data class ImportSettings(
        val overwriteExisting: Boolean = false,
        val skipDuplicates: Boolean = true,
        val createNewCookbook: Boolean = true,
        val cookbookName: String? = null
    )

    /**
     * Import a cookbook from a file
     *
     * @param file The file to import from
     * @param format The format of the file
     * @param settings Import settings
     * @return ImportResult with operation information
     */
    suspend operator fun invoke(
        file: File,
        format: ImportFormat,
        settings: ImportSettings = ImportSettings()
    ): ImportResult {
        return try {
            when (format) {
                ImportFormat.JSON -> {
                    importFromJson(file, settings)
                }
                ImportFormat.MARKDOWN -> {
                    importFromMarkdown(file, settings)
                }
            }
        } catch (e: Exception) {
            ImportResult.Failure(
                errorMessage = "Failed to import cookbook: ${e.message}",
                exception = e
            )
        }
    }

    /**
     * Import cookbook from JSON file
     */
    private suspend fun importFromJson(
        file: File,
        settings: ImportSettings
    ): ImportResult {
        // First, try to parse as cookbook export format (with cookbook metadata)
        val (cookbook, recipes, error) = parseCookbookFromJson(file)
        
        if (error == null && cookbook != null && recipes.isNotEmpty()) {
            // We have a cookbook with recipes
            return importParsedCookbook(cookbook, recipes, settings)
        }
        
        // Fall back to importing as individual recipes
        val (importedRecipes, importError) = importRecipeFromJson.previewImport(file)
        
        if (importError != null || importedRecipes.isEmpty()) {
            return ImportResult.Failure(
                errorMessage = importError ?: "No recipes found in the file"
            )
        }
        
        // Create a new cookbook for the imported recipes
        val cookbookName = settings.cookbookName ?: file.nameWithoutExtension
        val newCookbook = Cookbook.create(
            ownerDeviceId = "",
            name = cookbookName,
            description = "Imported from ${file.name}"
        )
        
        // Save the cookbook
        cookbookRepository.createCookbook(newCookbook)
        
        // Import recipes
        val result = importRecipesToCookbook(
            recipes = importedRecipes,
            cookbook = newCookbook,
            settings = settings
        )
        
        return when (result) {
            is ImportResult.Success -> {
                result.copy(format = ImportFormat.JSON)
            }
            is ImportResult.Failure -> {
                // Clean up the cookbook if import failed
                cookbookRepository.deleteCookbook(newCookbook.id)
                result
            }
        }
    }

    /**
     * Import cookbook from Markdown file
     */
    private suspend fun importFromMarkdown(
        file: File,
        settings: ImportSettings
    ): ImportResult {
        val recipes = importRecipeFromMarkdown.previewImport(file)
        
        if (recipes.isEmpty()) {
            return ImportResult.Failure("No recipes found in the Markdown file")
        }
        
        // Create a new cookbook for the imported recipes
        val cookbookName = settings.cookbookName ?: file.nameWithoutExtension
        val newCookbook = Cookbook.create(
            ownerDeviceId = "",
            name = cookbookName,
            description = "Imported from ${file.name}"
        )
        
        // Save the cookbook
        cookbookRepository.createCookbook(newCookbook)
        
        // Import recipes
        val result = importRecipesToCookbook(
            recipes = recipes,
            cookbook = newCookbook,
            settings = settings
        )
        
        return when (result) {
            is ImportResult.Success -> {
                result.copy(format = ImportFormat.MARKDOWN)
            }
            is ImportResult.Failure -> {
                // Clean up the cookbook if import failed
                cookbookRepository.deleteCookbook(newCookbook.id)
                result
            }
        }
    }

    /**
     * Parse cookbook from JSON file (with cookbook metadata)
     */
    private suspend fun parseCookbookFromJson(file: File): Triple<Cookbook?, List<Recipe>, String?> {
        return try {
            val json = file.readText()
            
            // Try to parse as cookbook export format
            // This would be enhanced with proper JSON parsing in production
            val cookbookName = file.nameWithoutExtension
            val recipes = importRecipeFromJson.previewImport(file).first
            
            val cookbook = Cookbook.create(
                ownerDeviceId = "",
                name = cookbookName,
                description = "Imported cookbook"
            )
            
            Triple(cookbook, recipes, null)
        } catch (e: Exception) {
            Triple(null, emptyList(), e.message)
        }
    }

    /**
     * Import parsed cookbook with recipes
     */
    private suspend fun importParsedCookbook(
        cookbook: Cookbook,
        recipes: List<Recipe>,
        settings: ImportSettings
    ): ImportResult {
        // Save the cookbook
        val savedCookbook = if (settings.createNewCookbook) {
            cookbookRepository.createCookbook(cookbook)
            cookbook
        } else {
            cookbook
        }
        
        // Import recipes
        val result = importRecipesToCookbook(
            recipes = recipes,
            cookbook = savedCookbook,
            settings = settings
        )
        
        return when (result) {
            is ImportResult.Success -> {
                result.copy(
                    cookbook = savedCookbook,
                    format = ImportFormat.JSON
                )
            }
            is ImportResult.Failure -> {
                if (settings.createNewCookbook) {
                    cookbookRepository.deleteCookbook(savedCookbook.id)
                }
                result
            }
        }
    }

    /**
     * Import recipes into a specific cookbook
     */
    private suspend fun importRecipesToCookbook(
        recipes: List<Recipe>,
        cookbook: Cookbook,
        settings: ImportSettings
    ): ImportResult {
        if (recipes.isEmpty()) {
            return ImportResult.Failure("No recipes to import")
        }
        
        // Check for conflicts
        val existingRecipes = recipeRepository.getAllRecipesOnce()
        val existingIds = existingRecipes.map { it.id }.toSet()
        val existingTitles = existingRecipes.map { it.title.lowercase() }.toSet()
        
        var importedCount = 0
        var skippedCount = 0
        var conflictCount = 0
        val importedRecipes = mutableListOf<Recipe>()
        
        recipes.forEach { recipe ->
            val isDuplicateId = recipe.id in existingIds
            val isDuplicateTitle = recipe.title.lowercase() in existingTitles
            
            if (isDuplicateId) {
                if (settings.overwriteExisting) {
                    // Update existing recipe
                    val updatedRecipe = recipe.copy()
                    recipeRepository.updateRecipe(updatedRecipe)
                    importedCount++
                    importedRecipes.add(updatedRecipe)
                } else if (settings.skipDuplicates) {
                    skippedCount++
                } else {
                    // Generate new ID for the duplicate
                    val newRecipe = recipe.copy(
                        id = java.util.UUID.randomUUID().toString()
                    )
                    recipeRepository.createRecipe(newRecipe)
                    importedCount++
                    importedRecipes.add(newRecipe)
                }
                conflictCount++
            } else if (isDuplicateTitle && !settings.overwriteExisting && !settings.skipDuplicates) {
                // Title duplicate but different ID - generate new ID
                val newRecipe = recipe.copy(
                    id = java.util.UUID.randomUUID().toString()
                )
                recipeRepository.createRecipe(newRecipe)
                importedCount++
                importedRecipes.add(newRecipe)
                conflictCount++
            } else {
                // No conflict - create new recipe
                val newRecipe = recipe.copy()
                recipeRepository.createRecipe(newRecipe)
                importedCount++
                importedRecipes.add(newRecipe)
            }
        }
        
        return ImportResult.Success(
            cookbook = cookbook,
            importedRecipes = importedRecipes,
            importedCount = importedCount,
            skippedCount = skippedCount,
            conflictCount = conflictCount,
            format = ImportFormat.JSON
        )
    }

    /**
     * Import cookbook from multiple files
     *
     * @param files List of files to import from
     * @param format The format of the files
     * @param settings Import settings
     * @return ImportResult with aggregated information
     */
    suspend fun importFromFiles(
        files: List<File>,
        format: ImportFormat,
        settings: ImportSettings = ImportSettings()
    ): ImportResult {
        var totalImported = 0
        var totalSkipped = 0
        var totalConflicts = 0
        val allRecipes = mutableListOf<Recipe>()
        var finalCookbook: Cookbook? = null
        var finalFormat = format
        
        files.forEach { file ->
            val result = this(file, format, settings)
            
            when (result) {
                is ImportResult.Success -> {
                    totalImported += result.importedCount
                    totalSkipped += result.skippedCount
                    totalConflicts += result.conflictCount
                    allRecipes.addAll(result.importedRecipes)
                    finalCookbook = result.cookbook
                    finalFormat = result.format
                }
                is ImportResult.Failure -> {
                    // Continue with other files
                }
            }
        }
        
        if (finalCookbook == null) {
            return ImportResult.Failure("No valid cookbooks found in the files")
        }
        
        return ImportResult.Success(
            cookbook = finalCookbook,
            importedRecipes = allRecipes,
            importedCount = totalImported,
            skippedCount = totalSkipped,
            conflictCount = totalConflicts,
            format = finalFormat
        )
    }

    /**
     * Preview import without actually importing
     *
     * @param file The file to preview
     * @param format The format of the file
     * @return Pair of (cookbook info, recipe list, error message)
     */
    suspend fun previewImport(
        file: File,
        format: ImportFormat
    ): Triple<String?, List<Recipe>, String?> {
        return try {
            when (format) {
                ImportFormat.JSON -> {
                    val (recipes, error) = importRecipeFromJson.previewImport(file)
                    if (error != null) {
                        Triple(null, emptyList(), error)
                    } else {
                        Triple(file.nameWithoutExtension, recipes, null)
                    }
                }
                ImportFormat.MARKDOWN -> {
                    val recipes = importRecipeFromMarkdown.previewImport(file)
                    Triple(file.nameWithoutExtension, recipes, null)
                }
            }
        } catch (e: Exception) {
            Triple(null, emptyList(), e.message ?: "Failed to preview import")
        }
    }

    /**
     * Check if the file is a valid cookbook file
     *
     * @param file The file to check
     * @return true if valid, false otherwise
     */
    suspend fun isValidImportFile(file: File): Boolean {
        return try {
            val (_, recipes, error) = previewImport(file, ImportFormat.JSON)
            error == null && recipes.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Detect file format from file extension
     */
    fun detectFormat(file: File): ImportFormat? {
        return when {
            file.name.endsWith(".json", ignoreCase = true) -> ImportFormat.JSON
            file.name.endsWith(".md", ignoreCase = true) || 
                    file.name.endsWith(".markdown", ignoreCase = true) -> ImportFormat.MARKDOWN
            else -> null
        }
    }

    /**
     * Get supported import formats
     */
    fun getSupportedFormats(): List<ImportFormat> {
        return listOf(
            ImportFormat.JSON,
            ImportFormat.MARKDOWN
        )
    }
}
