package com.ourcookbook.domain.usecase.recipe

import com.ourcookbook.domain.model.Recipe
import com.ourcookbook.domain.repository.RecipeRepository
import javax.inject.Inject

/**
 * Use case for finding recipes by ingredients
 * Task 2.2.05: Ingredient-based Search Implementation
 *
 * Searches for recipes that contain the specified ingredients and returns them
 * ranked by match percentage.
 */
class GetRecipesByIngredients @Inject constructor(
    private val recipeRepository: RecipeRepository
) {

    /**
     * Data class to hold recipe with its match score
     */
    data class RecipeWithScore(
        val recipe: Recipe,
        val matchScore: Float
    )

    /**
     * Execute the use case
     *
     * @param ingredients List of ingredient names to search for
     * @param minMatchPercentage Minimum match percentage (0.0-1.0) for a recipe to be included
     * @return List of RecipeWithScore objects sorted by match score descending
     */
    suspend operator fun invoke(
        ingredients: List<String>,
        minMatchPercentage: Float = 0.0f
    ): Result<List<RecipeWithScore>> {
        return try {
            if (ingredients.isEmpty()) {
                return Result.success(emptyList())
            }

            // Get all recipes
            val allRecipes = recipeRepository.getAllRecipes()
            
            if (allRecipes.isEmpty()) {
                return Result.success(emptyList())
            }

            // Normalize the search ingredients (lowercase, trim)
            val searchIngredients = ingredients
                .map { it.trim().lowercase() }
                .filter { it.isNotBlank() }
                .toSet()

            if (searchIngredients.isEmpty()) {
                return Result.success(emptyList())
            }

            // Calculate match scores for each recipe
            val recipesWithScores = allRecipes.mapNotNull { recipe ->
                val recipeIngredientNames = recipe.ingredients
                    .map { it.name.trim().lowercase() }
                    .filter { it.isNotBlank() }
                    .toSet()

                if (recipeIngredientNames.isEmpty()) {
                    return@mapNotNull null
                }

                // Count how many search ingredients match
                val matchingCount = recipeIngredientNames.count { it in searchIngredients }
                
                // Calculate match score: (matching ingredients / search ingredients) * (matching / recipe ingredients)
                val score = (matchingCount.toFloat() / searchIngredients.size.toFloat()) *
                          (matchingCount.toFloat() / recipeIngredientNames.size.toFloat())

                // Only include if score meets minimum threshold
                if (score >= minMatchPercentage) {
                    RecipeWithScore(recipe, score)
                } else {
                    null
                }
            }
            
            // Sort by match score descending, then by title
            .sortedWith(
                compareByDescending<RecipeWithScore> { it.matchScore }
                    .thenBy { it.recipe.title }
            )

            Result.success(recipesWithScores)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
