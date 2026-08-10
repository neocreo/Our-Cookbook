package com.ourcookbook.domain.usecase.cookbook

import com.ourcookbook.domain.model.Cookbook
import com.ourcookbook.domain.repository.CookbookRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Use case for getting cookbooks with result wrapper
 * This wraps the repository call to provide a Flow of Result types
 */
class GetCookbooks(
    private val repository: CookbookRepository
) {
    operator fun invoke(): Flow<Result<List<Cookbook>>> {
        return repository.getAllCookbooks()
            .map { cookbooks ->
                try {
                    Result.success(cookbooks)
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }
    }
}