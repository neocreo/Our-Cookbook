package com.ourcookbook.domain.usecase.recipe

import com.ourcookbook.domain.model.Recipe
import com.ourcookbook.domain.repository.RecipeRepository
import java.io.File
import javax.inject.Inject

/**
 * Use case for importing recipes from JSON files
 * Task 2.2.08: File Import Implementation
 *
 * Handles JSON file import with conflict detection and resolution
 */
class ImportRecipeFromJson @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val exportRecipeToJson: ExportRecipeToJson
) {

    /**
     * Result of JSON import operation
     */
    sealed class ImportResult {
        data class Success(
            val importedCount: Int,
            val skippedCount: Int,
            val conflictCount: Int,
            val recipes: List<Recipe>
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
        val importImages: Boolean = true
    )

    /**
     * Import recipes from a JSON file
     *
     * @param file The JSON file to import from
     * @param settings Import settings (optional)
     * @return ImportResult with import information
     */
    suspend operator fun invoke(
        file: File,
        settings: ImportSettings = ImportSettings()
    ): ImportResult {
        return try {
            // Import from file
            val (recipes, error) = exportRecipeToJson.importFromFile(file)
            
            if (error != null) {
                return ImportResult.Failure(
                    errorMessage = "Failed to parse JSON: $error"
                )
            }
            
            if (recipes.isEmpty()) {
                return ImportResult.Failure(
                    errorMessage = "No recipes found in the file"
                )
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
                        recipeRepository.updateRecipe(recipe)
                        importedCount++
                        importedRecipes.add(recipe)
                    } else if (settings.skipDuplicates) {
                        skippedCount++
                    } else {
                        // Generate new ID for the duplicate
                        val newRecipe = recipe.copy(id = java.util.UUID.randomUUID().toString())
                        recipeRepository.createRecipe(newRecipe)
                        importedCount++
                        importedRecipes.add(newRecipe)
                    }
                    conflictCount++
                } else if (isDuplicateTitle && !settings.overwriteExisting && !settings.skipDuplicates) {
                    // Title duplicate but different ID - generate new ID
                    val newRecipe = recipe.copy(id = java.util.UUID.randomUUID().toString())
                    recipeRepository.createRecipe(newRecipe)
                    importedCount++
                    importedRecipes.add(newRecipe)
                    conflictCount++
                } else {
                    // No conflict - create new recipe
                    recipeRepository.createRecipe(recipe)
                    importedCount++
                    importedRecipes.add(recipe)
                }
            }
            
            ImportResult.Success(
                importedCount = importedCount,
                skippedCount = skippedCount,
                conflictCount = conflictCount,
                recipes = importedRecipes
            )
            
        } catch (e: Exception) {
            ImportResult.Failure(
                errorMessage = "Failed to import recipes: ${e.message}",
                exception = e
            )
        }
    }

    /**
     * Import recipes from multiple JSON files
     *
     * @param files List of JSON files to import from
     * @param settings Import settings (optional)
     * @return ImportResult with aggregated import information
     */
    suspend fun importFromFiles(
        files: List<File>,
        settings: ImportSettings = ImportSettings()
    ): ImportResult {
        var totalImported = 0
        var totalSkipped = 0
        var totalConflicts = 0
        val allRecipes = mutableListOf<Recipe>()
        
        files.forEach { file ->
            val result = this(file, settings)
            
            when (result) {
                is ImportResult.Success -> {
                    totalImported += result.importedCount
                    totalSkipped += result.skippedCount
                    totalConflicts += result.conflictCount
                    allRecipes.addAll(result.recipes)
                }
                is ImportResult.Failure -> {
                    // Continue with other files
                }
            }
        }
        
        return ImportResult.Success(
            importedCount = totalImported,
            skippedCount = totalSkipped,
            conflictCount = totalConflicts,
            recipes = allRecipes
        )
    }

    /**
     * Preview import without actually importing
     *
     * @param file The JSON file to preview
     * @return Pair of recipe list and error message (if any)
     */
    suspend fun previewImport(file: File): Pair<List<Recipe>, String?> {
        return exportRecipeToJson.importFromFile(file)
    }

    /**
     * Check if the file is a valid JSON recipe file
     *
     * @param file The file to check
     * @return true if valid, false otherwise
     */
    suspend fun isValidImportFile(file: File): Boolean {
        return try {
            val (recipes, error) = exportRecipeToJson.importFromFile(file)
            error == null && recipes.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get recipe count from a JSON file without importing
     *
     * @param file The JSON file to check
     * @return Number of recipes in the file, or -1 if invalid
     */
    suspend fun getRecipeCount(file: File): Int {
        return try {
            val (recipes, _) = exportRecipeToJson.importFromFile(file)
            recipes.size
        } catch (e: Exception) {
            -1
        }
    }
}
