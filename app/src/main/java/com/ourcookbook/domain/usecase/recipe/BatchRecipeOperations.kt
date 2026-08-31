package com.ourcookbook.domain.usecase.recipe

import com.ourcookbook.domain.model.Recipe
import com.ourcookbook.domain.repository.RecipeRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

/**
 * Use case for batch operations on recipes
 * Task 2.2.09: Batch Operations Implementation
 *
 * Provides functionality for performing operations on multiple recipes at once:
 * - Batch delete
 * - Batch update (category, tags, etc.)
 * - Batch export
 * - Batch move between cookbooks
 */
class BatchRecipeOperations @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val exportRecipeToJson: ExportRecipeToJson,
    private val exportRecipeToPdf: ExportRecipeToPdf,
    private val exportRecipeToDocx: ExportRecipeToDocx
) {

    /**
     * Result of batch operation
     */
    sealed class BatchResult {
        data class Success(
            val affectedCount: Int,
            val failedCount: Int,
            val failedIds: List<String> = emptyList(),
            val message: String? = null
        ) : BatchResult()
        
        data class Failure(
            val errorMessage: String,
            val exception: Exception? = null
        ) : BatchResult()
    }

    /**
     * Batch delete recipes
     *
     * @param recipeIds List of recipe IDs to delete
     * @return BatchResult with operation information
     */
    suspend fun deleteRecipes(recipeIds: List<String>): BatchResult {
        if (recipeIds.isEmpty()) {
            return BatchResult.Failure("No recipe IDs provided")
        }
        
        return try {
            var deletedCount = 0
            var failedCount = 0
            val failedIds = mutableListOf<String>()
            
            recipeIds.forEach { recipeId ->
                try {
                    recipeRepository.deleteRecipe(recipeId)
                    deletedCount++
                } catch (e: Exception) {
                    failedCount++
                    failedIds.add(recipeId)
                }
            }
            
            BatchResult.Success(
                affectedCount = deletedCount,
                failedCount = failedCount,
                failedIds = failedIds,
                message = if (failedCount > 0) {
                    "Deleted $deletedCount recipes, failed to delete $failedCount"
                } else {
                    "Successfully deleted $deletedCount recipes"
                }
            )
        } catch (e: Exception) {
            BatchResult.Failure(
                errorMessage = "Failed to delete recipes: ${e.message}",
                exception = e
            )
        }
    }

    /**
     * Batch update recipe category
     *
     * @param recipeIds List of recipe IDs to update
     * @param newCategory The new category to set
     * @return BatchResult with operation information
     */
    suspend fun updateCategory(
        recipeIds: List<String>,
        newCategory: String
    ): BatchResult {
        if (recipeIds.isEmpty()) {
            return BatchResult.Failure("No recipe IDs provided")
        }
        
        return try {
            var updatedCount = 0
            var failedCount = 0
            val failedIds = mutableListOf<String>()
            
            recipeIds.forEach { recipeId ->
                try {
                    val recipe = recipeRepository.getRecipeById(recipeId)
                    if (recipe != null) {
                        val updatedRecipe = recipe.copy(category = newCategory)
                        recipeRepository.updateRecipe(updatedRecipe)
                        updatedCount++
                    } else {
                        failedCount++
                        failedIds.add(recipeId)
                    }
                } catch (e: Exception) {
                    failedCount++
                    failedIds.add(recipeId)
                }
            }
            
            BatchResult.Success(
                affectedCount = updatedCount,
                failedCount = failedCount,
                failedIds = failedIds,
                message = if (failedCount > 0) {
                    "Updated $updatedCount recipes, failed to update $failedCount"
                } else {
                    "Successfully updated $updatedCount recipes to category: $newCategory"
                }
            )
        } catch (e: Exception) {
            BatchResult.Failure(
                errorMessage = "Failed to update categories: ${e.message}",
                exception = e
            )
        }
    }

    /**
     * Batch add tags to recipes
     *
     * @param recipeIds List of recipe IDs to update
     * @param tagsToAdd List of tags to add
     * @param replaceExisting If true, replace existing tags; if false, add to existing tags
     * @return BatchResult with operation information
     */
    suspend fun addTags(
        recipeIds: List<String>,
        tagsToAdd: List<String>,
        replaceExisting: Boolean = false
    ): BatchResult {
        if (recipeIds.isEmpty()) {
            return BatchResult.Failure("No recipe IDs provided")
        }
        if (tagsToAdd.isEmpty()) {
            return BatchResult.Failure("No tags provided")
        }
        
        return try {
            var updatedCount = 0
            var failedCount = 0
            val failedIds = mutableListOf<String>()
            
            recipeIds.forEach { recipeId ->
                try {
                    val recipe = recipeRepository.getRecipeById(recipeId)
                    if (recipe != null) {
                        val newTags = if (replaceExisting) {
                            tagsToAdd
                        } else {
                            (recipe.tags + tagsToAdd).distinct()
                        }
                        val updatedRecipe = recipe.copy(tags = newTags)
                        recipeRepository.updateRecipe(updatedRecipe)
                        updatedCount++
                    } else {
                        failedCount++
                        failedIds.add(recipeId)
                    }
                } catch (e: Exception) {
                    failedCount++
                    failedIds.add(recipeId)
                }
            }
            
            BatchResult.Success(
                affectedCount = updatedCount,
                failedCount = failedCount,
                failedIds = failedIds,
                message = if (failedCount > 0) {
                    "Updated $updatedCount recipes, failed to update $failedCount"
                } else {
                    "Successfully added tags to $updatedCount recipes"
                }
            )
        } catch (e: Exception) {
            BatchResult.Failure(
                errorMessage = "Failed to add tags: ${e.message}",
                exception = e
            )
        }
    }

    /**
     * Batch remove tags from recipes
     *
     * @param recipeIds List of recipe IDs to update
     * @param tagsToRemove List of tags to remove
     * @return BatchResult with operation information
     */
    suspend fun removeTags(
        recipeIds: List<String>,
        tagsToRemove: List<String>
    ): BatchResult {
        if (recipeIds.isEmpty()) {
            return BatchResult.Failure("No recipe IDs provided")
        }
        if (tagsToRemove.isEmpty()) {
            return BatchResult.Failure("No tags provided")
        }
        
        return try {
            var updatedCount = 0
            var failedCount = 0
            val failedIds = mutableListOf<String>()
            
            recipeIds.forEach { recipeId ->
                try {
                    val recipe = recipeRepository.getRecipeById(recipeId)
                    if (recipe != null) {
                        val newTags = recipe.tags.filter { it !in tagsToRemove }
                        val updatedRecipe = recipe.copy(tags = newTags)
                        recipeRepository.updateRecipe(updatedRecipe)
                        updatedCount++
                    } else {
                        failedCount++
                        failedIds.add(recipeId)
                    }
                } catch (e: Exception) {
                    failedCount++
                    failedIds.add(recipeId)
                }
            }
            
            BatchResult.Success(
                affectedCount = updatedCount,
                failedCount = failedCount,
                failedIds = failedIds,
                message = if (failedCount > 0) {
                    "Updated $updatedCount recipes, failed to update $failedCount"
                } else {
                    "Successfully removed tags from $updatedCount recipes"
                }
            )
        } catch (e: Exception) {
            BatchResult.Failure(
                errorMessage = "Failed to remove tags: ${e.message}",
                exception = e
            )
        }
    }

    /**
     * Batch export recipes to JSON
     *
     * @param recipeIds List of recipe IDs to export
     * @param outputFile The output JSON file
     * @param settings Export settings
     * @return BatchResult with operation information
     */
    suspend fun exportToJson(
        recipeIds: List<String>,
        outputFile: java.io.File,
        settings: ExportRecipeToJson.ExportSettings = ExportRecipeToJson.ExportSettings()
    ): BatchResult {
        if (recipeIds.isEmpty()) {
            return BatchResult.Failure("No recipe IDs provided")
        }
        
        return try {
            // Fetch all recipes
            val recipes = recipeRepository.getRecipesByIds(recipeIds)
            
            if (recipes.isEmpty()) {
                return BatchResult.Failure("No recipes found for the provided IDs")
            }
            
            // Export to JSON
            val result = exportRecipeToJson.exportMultiple(
                recipes,
                outputFile,
                settings
            )
            
            when (result) {
                is ExportRecipeToJson.ExportResult.Success -> {
                    BatchResult.Success(
                        affectedCount = result.recipeCount,
                        failedCount = 0,
                        message = "Successfully exported ${result.recipeCount} recipes to JSON"
                    )
                }
                is ExportRecipeToJson.ExportResult.Failure -> {
                    BatchResult.Failure(
                        errorMessage = result.errorMessage,
                        exception = result.exception
                    )
                }
            }
        } catch (e: Exception) {
            BatchResult.Failure(
                errorMessage = "Failed to export recipes to JSON: ${e.message}",
                exception = e
            )
        }
    }

    /**
     * Batch export recipes to PDF
     *
     * @param recipeIds List of recipe IDs to export
     * @param outputFile The output PDF file
     * @param settings Export settings
     * @return BatchResult with operation information
     */
    suspend fun exportToPdf(
        recipeIds: List<String>,
        outputFile: java.io.File,
        settings: ExportRecipeToPdf.ExportSettings = ExportRecipeToPdf.ExportSettings()
    ): BatchResult {
        if (recipeIds.isEmpty()) {
            return BatchResult.Failure("No recipe IDs provided")
        }
        
        return try {
            // Fetch all recipes
            val recipes = recipeRepository.getRecipesByIds(recipeIds)
            
            if (recipes.isEmpty()) {
                return BatchResult.Failure("No recipes found for the provided IDs")
            }
            
            // Export to PDF
            val result = exportRecipeToPdf.exportMultiple(
                recipes,
                outputFile,
                settings
            )
            
            when (result) {
                is ExportRecipeToPdf.ExportResult.Success -> {
                    BatchResult.Success(
                        affectedCount = result.recipeCount,
                        failedCount = 0,
                        message = "Successfully exported ${result.recipeCount} recipes to PDF"
                    )
                }
                is ExportRecipeToPdf.ExportResult.Failure -> {
                    BatchResult.Failure(
                        errorMessage = result.errorMessage,
                        exception = result.exception
                    )
                }
            }
        } catch (e: Exception) {
            BatchResult.Failure(
                errorMessage = "Failed to export recipes to PDF: ${e.message}",
                exception = e
            )
        }
    }

    /**
     * Batch update recipe rating
     *
     * @param recipeIds List of recipe IDs to update
     * @param newRating The new rating to set (1-5)
     * @return BatchResult with operation information
     */
    suspend fun updateRating(
        recipeIds: List<String>,
        newRating: Float
    ): BatchResult {
        if (recipeIds.isEmpty()) {
            return BatchResult.Failure("No recipe IDs provided")
        }
        if (newRating < 1f || newRating > 5f) {
            return BatchResult.Failure("Rating must be between 1 and 5")
        }
        
        return try {
            var updatedCount = 0
            var failedCount = 0
            val failedIds = mutableListOf<String>()
            
            recipeIds.forEach { recipeId ->
                try {
                    val recipe = recipeRepository.getRecipeById(recipeId)
                    if (recipe != null) {
                        val updatedRecipe = recipe.copy(rating = newRating)
                        recipeRepository.updateRecipe(updatedRecipe)
                        updatedCount++
                    } else {
                        failedCount++
                        failedIds.add(recipeId)
                    }
                } catch (e: Exception) {
                    failedCount++
                    failedIds.add(recipeId)
                }
            }
            
            BatchResult.Success(
                affectedCount = updatedCount,
                failedCount = failedCount,
                failedIds = failedIds,
                message = if (failedCount > 0) {
                    "Updated $updatedCount recipes, failed to update $failedCount"
                } else {
                    "Successfully updated rating for $updatedCount recipes"
                }
            )
        } catch (e: Exception) {
            BatchResult.Failure(
                errorMessage = "Failed to update ratings: ${e.message}",
                exception = e
            )
        }
    }

    /**
     * Batch set favorite status
     *
     * @param recipeIds List of recipe IDs to update
     * @param isFavorite The favorite status to set
     * @return BatchResult with operation information
     */
    suspend fun setFavorite(
        recipeIds: List<String>,
        isFavorite: Boolean
    ): BatchResult {
        if (recipeIds.isEmpty()) {
            return BatchResult.Failure("No recipe IDs provided")
        }
        
        return try {
            var updatedCount = 0
            var failedCount = 0
            val failedIds = mutableListOf<String>()
            
            recipeIds.forEach { recipeId ->
                try {
                    val recipe = recipeRepository.getRecipeById(recipeId)
                    if (recipe != null) {
                        val updatedRecipe = recipe.copy(isFavorite = isFavorite)
                        recipeRepository.updateRecipe(updatedRecipe)
                        updatedCount++
                    } else {
                        failedCount++
                        failedIds.add(recipeId)
                    }
                } catch (e: Exception) {
                    failedCount++
                    failedIds.add(recipeId)
                }
            }
            
            BatchResult.Success(
                affectedCount = updatedCount,
                failedCount = failedCount,
                failedIds = failedIds,
                message = if (isFavorite) {
                    if (failedCount > 0) {
                        "Favorited $updatedCount recipes, failed to favorite $failedCount"
                    } else {
                        "Successfully favorited $updatedCount recipes"
                    }
                } else {
                    if (failedCount > 0) {
                        "Unfavorited $updatedCount recipes, failed to unfavorite $failedCount"
                    } else {
                        "Successfully unfavorited $updatedCount recipes"
                    }
                }
            )
        } catch (e: Exception) {
            BatchResult.Failure(
                errorMessage = "Failed to update favorites: ${e.message}",
                exception = e
            )
        }
    }

    /**
     * Parallel batch delete for better performance
     *
     * @param recipeIds List of recipe IDs to delete
     * @param maxParallel Maximum number of parallel operations
     * @return BatchResult with operation information
     */
    suspend fun parallelDelete(
        recipeIds: List<String>,
        maxParallel: Int = 5
    ): BatchResult {
        if (recipeIds.isEmpty()) {
            return BatchResult.Failure("No recipe IDs provided")
        }
        
        return try {
            coroutineScope {
                val results = recipeIds
                    .chunked(maxParallel)
                    .map { chunk ->
                        async {
                            chunk.map { recipeId ->
                                try {
                                    recipeRepository.deleteRecipe(recipeId)
                                    Pair(recipeId, true)
                                } catch (e: Exception) {
                                    Pair(recipeId, false)
                                }
                            }
                        }
                    }
                    .awaitAll()
                    .flatten()
                
                val (successes, failures) = results.partition { it.second }
                
                BatchResult.Success(
                    affectedCount = successes.size,
                    failedCount = failures.size,
                    failedIds = failures.map { it.first },
                    message = "Deleted ${successes.size} recipes, failed to delete ${failures.size}"
                )
            }
        } catch (e: Exception) {
            BatchResult.Failure(
                errorMessage = "Failed to delete recipes in parallel: ${e.message}",
                exception = e
            )
        }
    }

    /**
     * Get recipes by IDs for batch operations
     *
     * @param recipeIds List of recipe IDs to fetch
     * @return List of Recipe objects
     */
    suspend fun getRecipesByIds(recipeIds: List<String>): List<Recipe> {
        return recipeRepository.getRecipesByIds(recipeIds)
    }

    /**
     * Validate recipe IDs exist
     *
     * @param recipeIds List of recipe IDs to validate
     * @return Pair of (validIds, invalidIds)
     */
    suspend fun validateRecipeIds(recipeIds: List<String>): Pair<List<String>, List<String>> {
        val allRecipes = recipeRepository.getAllRecipesOnce()
        val validIds = allRecipes.map { it.id }.toSet()
        
        val (valid, invalid) = recipeIds.partition { it in validIds }
        return Pair(valid, invalid)
    }
}
