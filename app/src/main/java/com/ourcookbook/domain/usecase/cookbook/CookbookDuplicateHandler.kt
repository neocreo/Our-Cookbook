package com.ourcookbook.domain.usecase.cookbook

import com.ourcookbook.domain.model.Cookbook
import com.ourcookbook.domain.model.Recipe
import com.ourcookbook.domain.repository.CookbookRepository
import com.ourcookbook.domain.repository.RecipeRepository
import javax.inject.Inject

/**
 * Use case for handling cookbook duplicates
 * Task 2.2.12: Cookbook duplicate handling
 *
 * Provides functionality to detect, prevent, and resolve duplicate cookbooks
 */
class CookbookDuplicateHandler @Inject constructor(
    private val cookbookRepository: CookbookRepository,
    private val recipeRepository: RecipeRepository
) {

    /**
     * Duplicate detection result
     */
    sealed class DuplicateCheckResult {
        data class NoDuplicates(val cookbook: Cookbook) : DuplicateCheckResult()
        data class NameDuplicate(
            val newCookbook: Cookbook,
            val existingCookbook: Cookbook
        ) : DuplicateCheckResult()
        data class ContentDuplicate(
            val newCookbook: Cookbook,
            val existingCookbook: Cookbook,
            val similarityScore: Float
        ) : DuplicateCheckResult()
    }

    /**
     * Conflict resolution strategy
     */
    enum class ConflictResolutionStrategy {
        SKIP,           // Skip the duplicate
        REPLACE,        // Replace the existing one
        MERGE,          // Merge content
        RENAME,         // Rename the new one
        KEEP_BOTH       // Keep both with different names
    }

    /**
     * Result of duplicate handling
     */
    sealed class HandleResult {
        data class Success(
            val cookbook: Cookbook,
            val strategyUsed: ConflictResolutionStrategy,
            val message: String? = null
        ) : HandleResult()
        
        data class Failure(
            val errorMessage: String,
            val exception: Exception? = null
        ) : HandleResult()
    }

    /**
     * Check for duplicate cookbook by name
     *
     * @param cookbook The cookbook to check
     * @return DuplicateCheckResult with detection information
     */
    suspend fun checkForDuplicates(cookbook: Cookbook): DuplicateCheckResult {
        val existingCookbooks = cookbookRepository.getAllCookbooksOnce()
        
        // Check for exact name match
        existingCookbooks.find { 
            it.name.equals(cookbook.name, ignoreCase = true) && 
            it.id != cookbook.id 
        }?.let { existing ->
            return DuplicateCheckResult.NameDuplicate(cookbook, existing)
        }
        
        // Check for similar content (by recipe overlap)
        val existingWithRecipes = existingCookbooks.associateWith { cb ->
            recipeRepository.getRecipesByCookbookId(cb.id)
        }
        
        existingWithRecipes.forEach { (existingCookbook, existingRecipes) ->
            if (existingCookbook.id != cookbook.id) {
                val newRecipes = recipeRepository.getRecipesByCookbookId(cookbook.id)
                val similarity = calculateSimilarity(newRecipes, existingRecipes)
                
                if (similarity >= 0.7f) {  // 70% similarity threshold
                    return DuplicateCheckResult.ContentDuplicate(
                        cookbook,
                        existingCookbook,
                        similarity
                    )
                }
            }
        }
        
        return DuplicateCheckResult.NoDuplicates(cookbook)
    }

    /**
     * Calculate similarity score between two sets of recipes
     *
     * @param recipes1 First set of recipes
     * @param recipes2 Second set of recipes
     * @return Similarity score (0.0 to 1.0)
     */
    private suspend fun calculateSimilarity(
        recipes1: List<Recipe>,
        recipes2: List<Recipe>
    ): Float {
        if (recipes1.isEmpty() || recipes2.isEmpty()) {
            return 0f
        }
        
        // Count matching recipes by ID
        val ids1 = recipes1.map { it.id }.toSet()
        val ids2 = recipes2.map { it.id }.toSet()
        val matchingIds = ids1.intersect(ids2)
        
        // Count matching recipes by title
        val titles1 = recipes1.map { it.title.lowercase() }.toSet()
        val titles2 = recipes2.map { it.title.lowercase() }.toSet()
        val matchingTitles = titles1.intersect(titles2)
        
        // Calculate similarity based on both ID and title matches
        val idSimilarity = matchingIds.size.toFloat() / maxOf(ids1.size, ids2.size)
        val titleSimilarity = matchingTitles.size.toFloat() / maxOf(titles1.size, titles2.size)
        
        // Weighted average
        return (idSimilarity * 0.8f) + (titleSimilarity * 0.2f)
    }

    /**
     * Handle duplicate cookbook based on the specified strategy
     *
     * @param newCookbook The new cookbook to add
     * @param strategy The strategy to use for conflict resolution
     * @return HandleResult with operation information
     */
    suspend fun handleDuplicate(
        newCookbook: Cookbook,
        strategy: ConflictResolutionStrategy
    ): HandleResult {
        val checkResult = checkForDuplicates(newCookbook)
        
        return when (checkResult) {
            is DuplicateCheckResult.NoDuplicates -> {
                // No duplicates found, create the cookbook
                try {
                    cookbookRepository.createCookbook(newCookbook)
                    val created = newCookbook
                    HandleResult.Success(
                        cookbook = created,
                        strategyUsed = strategy,
                        message = "Cookbook created successfully"
                    )
                } catch (e: Exception) {
                    HandleResult.Failure(
                        errorMessage = "Failed to create cookbook: ${e.message}",
                        exception = e
                    )
                }
            }
            is DuplicateCheckResult.NameDuplicate -> {
                handleNameDuplicate(newCookbook, checkResult.existingCookbook, strategy)
            }
            is DuplicateCheckResult.ContentDuplicate -> {
                handleContentDuplicate(newCookbook, checkResult.existingCookbook, strategy)
            }
        }
    }

    /**
     * Handle name duplicate
     */
    private suspend fun handleNameDuplicate(
        newCookbook: Cookbook,
        existingCookbook: Cookbook,
        strategy: ConflictResolutionStrategy
    ): HandleResult {
        return when (strategy) {
            ConflictResolutionStrategy.SKIP -> {
                HandleResult.Success(
                    cookbook = existingCookbook,
                    strategyUsed = strategy,
                    message = "Skipped duplicate cookbook"
                )
            }
            ConflictResolutionStrategy.REPLACE -> {
                try {
                    // Replace the existing cookbook
                    cookbookRepository.updateCookbook(newCookbook)
                    val updated = newCookbook
                    HandleResult.Success(
                        cookbook = updated,
                        strategyUsed = strategy,
                        message = "Replaced existing cookbook"
                    )
                } catch (e: Exception) {
                    HandleResult.Failure(
                        errorMessage = "Failed to replace cookbook: ${e.message}",
                        exception = e
                    )
                }
            }
            ConflictResolutionStrategy.MERGE -> {
                try {
                    // Merge recipes from new cookbook into existing one
                    val newRecipes = recipeRepository.getRecipesByCookbookId(newCookbook.id)
                    
                    // Update recipes to point to existing cookbook
                    newRecipes.forEach { recipe ->
                        val updatedRecipe = recipe.copy()
                        recipeRepository.updateRecipe(updatedRecipe)
                    }
                    
                    // Delete the new cookbook (its recipes are now in the existing one)
                    cookbookRepository.deleteCookbook(newCookbook.id)
                    
                    HandleResult.Success(
                        cookbook = existingCookbook,
                        strategyUsed = strategy,
                        message = "Merged recipes into existing cookbook"
                    )
                } catch (e: Exception) {
                    HandleResult.Failure(
                        errorMessage = "Failed to merge cookbooks: ${e.message}",
                        exception = e
                    )
                }
            }
            ConflictResolutionStrategy.RENAME -> {
                try {
                    // Generate a unique name
                    val uniqueName = generateUniqueName(newCookbook.name)
                    val renamedCookbook = newCookbook.copy(name = uniqueName)
                    cookbookRepository.createCookbook(renamedCookbook)
                    val created = renamedCookbook
                    
                    HandleResult.Success(
                        cookbook = created,
                        strategyUsed = strategy,
                        message = "Renamed cookbook to: $uniqueName"
                    )
                } catch (e: Exception) {
                    HandleResult.Failure(
                        errorMessage = "Failed to rename cookbook: ${e.message}",
                        exception = e
                    )
                }
            }
            ConflictResolutionStrategy.KEEP_BOTH -> {
                try {
                    // Rename the new cookbook to make it unique
                    val uniqueName = generateUniqueName(newCookbook.name)
                    val renamedCookbook = newCookbook.copy(name = uniqueName)
                    cookbookRepository.createCookbook(renamedCookbook)
                    val created = renamedCookbook
                    
                    HandleResult.Success(
                        cookbook = created,
                        strategyUsed = strategy,
                        message = "Kept both cookbooks, new one renamed to: $uniqueName"
                    )
                } catch (e: Exception) {
                    HandleResult.Failure(
                        errorMessage = "Failed to keep both cookbooks: ${e.message}",
                        exception = e
                    )
                }
            }
        }
    }

    /**
     * Handle content duplicate
     */
    private suspend fun handleContentDuplicate(
        newCookbook: Cookbook,
        existingCookbook: Cookbook,
        strategy: ConflictResolutionStrategy
    ): HandleResult {
        return when (strategy) {
            ConflictResolutionStrategy.SKIP -> {
                HandleResult.Success(
                    cookbook = existingCookbook,
                    strategyUsed = strategy,
                    message = "Skipped cookbook with duplicate content"
                )
            }
            ConflictResolutionStrategy.REPLACE -> {
                try {
                    // Replace the existing cookbook and its recipes
                    val newRecipes = recipeRepository.getRecipesByCookbookId(newCookbook.id)
                    
                    // Delete existing cookbook's recipes
                    recipeRepository.deleteRecipesByCookbookId(existingCookbook.id)
                    
                    // Update new recipes to point to existing cookbook
                    newRecipes.forEach { recipe ->
                        val updatedRecipe = recipe.copy()
                        recipeRepository.updateRecipe(updatedRecipe)
                    }
                    
                    // Update the existing cookbook
                    val updatedCookbook = existingCookbook.copy(
                        name = newCookbook.name,
                        description = newCookbook.description
                    )
                    cookbookRepository.updateCookbook(updatedCookbook)
                    val updated = updatedCookbook
                    
                    // Delete the new cookbook
                    cookbookRepository.deleteCookbook(newCookbook.id)
                    
                    HandleResult.Success(
                        cookbook = updated,
                        strategyUsed = strategy,
                        message = "Replaced existing cookbook with new content"
                    )
                } catch (e: Exception) {
                    HandleResult.Failure(
                        errorMessage = "Failed to replace cookbook content: ${e.message}",
                        exception = e
                    )
                }
            }
            ConflictResolutionStrategy.MERGE -> {
                try {
                    // Merge recipes from new cookbook into existing one
                    val newRecipes = recipeRepository.getRecipesByCookbookId(newCookbook.id)
                    val existingRecipes = recipeRepository.getRecipesByCookbookId(existingCookbook.id)
                    
                    // Find recipes that don't already exist in the existing cookbook
                    val existingRecipeIds = existingRecipes.map { it.id }.toSet()
                    val newUniqueRecipes = newRecipes.filter { it.id !in existingRecipeIds }
                    
                    // Update new unique recipes to point to existing cookbook
                    newUniqueRecipes.forEach { recipe ->
                        val updatedRecipe = recipe.copy()
                        recipeRepository.createRecipe(updatedRecipe)
                    }
                    
                    // Delete the new cookbook
                    cookbookRepository.deleteCookbook(newCookbook.id)
                    
                    HandleResult.Success(
                        cookbook = existingCookbook,
                        strategyUsed = strategy,
                        message = "Merged ${newUniqueRecipes.size} new recipes into existing cookbook"
                    )
                } catch (e: Exception) {
                    HandleResult.Failure(
                        errorMessage = "Failed to merge cookbook content: ${e.message}",
                        exception = e
                    )
                }
            }
            ConflictResolutionStrategy.RENAME -> {
                try {
                    // Generate a unique name
                    val uniqueName = generateUniqueName(newCookbook.name)
                    val renamedCookbook = newCookbook.copy(name = uniqueName)
                    cookbookRepository.createCookbook(renamedCookbook)
                    val created = renamedCookbook
                    
                    HandleResult.Success(
                        cookbook = created,
                        strategyUsed = strategy,
                        message = "Renamed cookbook to: $uniqueName"
                    )
                } catch (e: Exception) {
                    HandleResult.Failure(
                        errorMessage = "Failed to rename cookbook: ${e.message}",
                        exception = e
                    )
                }
            }
            ConflictResolutionStrategy.KEEP_BOTH -> {
                try {
                    // Rename the new cookbook to make it unique
                    val uniqueName = generateUniqueName(newCookbook.name)
                    val renamedCookbook = newCookbook.copy(name = uniqueName)
                    cookbookRepository.createCookbook(renamedCookbook)
                    val created = renamedCookbook
                    
                    HandleResult.Success(
                        cookbook = created,
                        strategyUsed = strategy,
                        message = "Kept both cookbooks, new one renamed to: $uniqueName"
                    )
                } catch (e: Exception) {
                    HandleResult.Failure(
                        errorMessage = "Failed to keep both cookbooks: ${e.message}",
                        exception = e
                    )
                }
            }
        }
    }

    /**
     * Generate a unique name for a cookbook
     */
    private suspend fun generateUniqueName(baseName: String): String {
        val existingCookbooks = cookbookRepository.getAllCookbooksOnce()
        val existingNames = existingCookbooks.map { it.name.lowercase() }.toSet()
        
        var newName = baseName
        var counter = 1
        
        while (newName.lowercase() in existingNames) {
            newName = "$baseName $counter"
            counter++
        }
        
        return newName
    }

    /**
     * Check if a cookbook name is unique
     */
    suspend fun isNameUnique(name: String, excludeId: String? = null): Boolean {
        val existingCookbooks = cookbookRepository.getAllCookbooksOnce()
        return existingCookbooks.none { 
            it.name.equals(name, ignoreCase = true) && it.id != excludeId 
        }
    }

    /**
     * Find all cookbooks with similar content
     *
     * @param cookbook The cookbook to compare against
     * @param threshold Similarity threshold (0.0 to 1.0)
     * @return List of similar cookbooks with their similarity scores
     */
    suspend fun findSimilarCookbooks(
        cookbook: Cookbook,
        threshold: Float = 0.5f
    ): List<Pair<Cookbook, Float>> {
        val allCookbooks = cookbookRepository.getAllCookbooksOnce()
        val newRecipes = recipeRepository.getRecipesByCookbookId(cookbook.id)
        
        return allCookbooks
            .filter { it.id != cookbook.id }
            .mapNotNull { existingCookbook ->
                val existingRecipes = recipeRepository.getRecipesByCookbookId(existingCookbook.id)
                val similarity = calculateSimilarity(newRecipes, existingRecipes)
                
                if (similarity >= threshold) {
                    Pair(existingCookbook, similarity)
                } else {
                    null
                }
            }
            .sortedByDescending { it.second }
    }

    /**
     * Get duplicate detection statistics
     */
    suspend fun getDuplicateStatistics(): Map<String, Any> {
        val allCookbooks = cookbookRepository.getAllCookbooksOnce()
        
        var nameDuplicates = 0
        var contentDuplicates = 0
        var highSimilarity = 0
        
        allCookbooks.forEachIndexed { i, cookbook1 ->
            allCookbooks.drop(i + 1).forEach { cookbook2 ->
                if (cookbook1.name.equals(cookbook2.name, ignoreCase = true)) {
                    nameDuplicates++
                }
                
                val recipes1 = recipeRepository.getRecipesByCookbookId(cookbook1.id)
                val recipes2 = recipeRepository.getRecipesByCookbookId(cookbook2.id)
                val similarity = calculateSimilarity(recipes1, recipes2)
                
                if (similarity >= 0.9f) {
                    contentDuplicates++
                } else if (similarity >= 0.7f) {
                    highSimilarity++
                }
            }
        }
        
        return mapOf(
            "total_cookbooks" to allCookbooks.size,
            "name_duplicates" to nameDuplicates,
            "content_duplicates" to contentDuplicates,
            "high_similarity" to highSimilarity
        )
    }
}
