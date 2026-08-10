package com.ourcookbook.domain.usecase.recipe

import com.ourcookbook.domain.model.Recipe
import com.ourcookbook.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Use case for getting recipes with result wrapper
 * This wraps the repository call to provide a Flow of Result types
 */
class GetRecipes(
    private val repository: RecipeRepository
) {
    operator fun invoke(): Flow<Result<List<Recipe>>> {
        return repository.getAllRecipes()
            .map { recipes ->
                try {
                    Result.success(recipes)
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }
    }
}