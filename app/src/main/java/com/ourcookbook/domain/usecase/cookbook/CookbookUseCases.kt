package com.ourcookbook.domain.usecase.cookbook

import com.ourcookbook.domain.model.Cookbook
import com.ourcookbook.domain.repository.CookbookRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use cases for Cookbook CRUD operations
 * These use cases encapsulate the business logic for cookbook management
 */

// Create Cookbook Use Case
class CreateCookbook(
    private val repository: CookbookRepository
) {
    suspend operator fun invoke(cookbook: Cookbook): Result<String> {
        return try {
            if (!cookbook.isValid()) {
                return Result.failure(IllegalArgumentException("Cookbook validation failed"))
            }
            val cookbookId = repository.createCookbook(cookbook)
            Result.success(cookbookId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Update Cookbook Use Case
class UpdateCookbook(
    private val repository: CookbookRepository
) {
    suspend operator fun invoke(cookbook: Cookbook): Result<Unit> {
        return try {
            if (!cookbook.isValid()) {
                return Result.failure(IllegalArgumentException("Cookbook validation failed"))
            }
            repository.updateCookbook(cookbook)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Delete Cookbook Use Case
class DeleteCookbook(
    private val repository: CookbookRepository
) {
    suspend operator fun invoke(cookbookId: String): Result<Unit> {
        return try {
            repository.deleteCookbook(cookbookId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Get Cookbook By ID Use Case
class GetCookbookById(
    private val repository: CookbookRepository
) {
    suspend operator fun invoke(cookbookId: String): Result<Cookbook?> {
        return try {
            val cookbook = repository.getCookbookById(cookbookId)
            Result.success(cookbook)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Get Cookbooks By Owner Use Case
class GetCookbooksByOwner(
    private val repository: CookbookRepository
) {
    operator fun invoke(deviceId: String): Flow<List<Cookbook>> {
        return repository.getCookbooksByOwner(deviceId)
    }
}

// Get Shared Cookbooks Use Case
class GetSharedCookbooks(
    private val repository: CookbookRepository
) {
    operator fun invoke(): Flow<List<Cookbook>> {
        return repository.getSharedCookbooks()
    }
}

// Get All Cookbooks Use Case
class GetAllCookbooks(
    private val repository: CookbookRepository
) {
    operator fun invoke(): Flow<List<Cookbook>> {
        return repository.getAllCookbooks()
    }
}

// Search Cookbooks Use Case
class SearchCookbooks(
    private val repository: CookbookRepository
) {
    operator fun invoke(query: String): Flow<List<Cookbook>> {
        return repository.searchCookbooks(query)
    }
}

// Get Cookbook Count Use Case
class GetCookbookCount(
    private val repository: CookbookRepository
) {
    suspend operator fun invoke(): Result<Int> {
        return try {
            val count = repository.getCookbookCount()
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Add Recipe To Cookbook Use Case
class AddRecipeToCookbook(
    private val repository: CookbookRepository,
    private val getCookbookById: GetCookbookById,
    private val updateCookbook: UpdateCookbook
) {
    suspend operator fun invoke(cookbookId: String, recipeId: String): Result<Unit> {
        return try {
            val cookbook = getCookbookById(cookbookId).getOrThrow()
                ?: return Result.failure(NoSuchElementException("Cookbook not found"))
            
            if (cookbook.containsRecipe(recipeId)) {
                return Result.success(Unit) // Already contains the recipe
            }
            
            val updatedCookbook = cookbook.withAddedRecipe(recipeId)
            updateCookbook(updatedCookbook)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Remove Recipe From Cookbook Use Case
class RemoveRecipeFromCookbook(
    private val repository: CookbookRepository,
    private val getCookbookById: GetCookbookById,
    private val updateCookbook: UpdateCookbook
) {
    suspend operator fun invoke(cookbookId: String, recipeId: String): Result<Unit> {
        return try {
            val cookbook = getCookbookById(cookbookId).getOrThrow()
                ?: return Result.failure(NoSuchElementException("Cookbook not found"))
            
            if (!cookbook.containsRecipe(recipeId)) {
                return Result.success(Unit) // Doesn't contain the recipe
            }
            
            val updatedCookbook = cookbook.withRemovedRecipe(recipeId)
            updateCookbook(updatedCookbook)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Validate Cookbook Checksum Use Case
class ValidateCookbookChecksum(
    private val repository: CookbookRepository
) {
    suspend operator fun invoke(cookbookId: String): Result<Boolean> {
        return try {
            val isValid = repository.validateCookbookChecksum(cookbookId)
            Result.success(isValid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Update Cookbook Checksum Use Case
class UpdateCookbookChecksum(
    private val repository: CookbookRepository
) {
    suspend operator fun invoke(cookbookId: String): Result<Boolean> {
        return try {
            val updated = repository.updateCookbookChecksum(cookbookId)
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Export Cookbook Use Case
class ExportCookbook(
    private val repository: CookbookRepository
) {
    suspend operator fun invoke(cookbookId: String, format: com.ourcookbook.ui.viewmodel.ExportFormat, destinationFile: java.io.File): Result<Unit> {
        return try {
            repository.exportCookbook(cookbookId, format, destinationFile)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Import Cookbook Use Case
class ImportCookbook(
    private val repository: CookbookRepository
) {
    suspend operator fun invoke(sourceFile: java.io.File, format: com.ourcookbook.ui.viewmodel.ExportFormat): Result<com.ourcookbook.domain.model.Cookbook> {
        return try {
            val cookbook = repository.importCookbook(sourceFile, format)
            Result.success(cookbook)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Share Cookbook Use Case
class ShareCookbook(
    private val repository: CookbookRepository
) {
    suspend operator fun invoke(cookbookId: String, userIds: List<String>, permissions: Set<com.ourcookbook.ui.viewmodel.Permission>): Result<Unit> {
        return try {
            repository.shareCookbook(cookbookId, userIds, permissions)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Generate Sharing Link Use Case
class GenerateSharingLink(
    private val repository: CookbookRepository
) {
    suspend operator fun invoke(cookbookId: String): Result<String> {
        return try {
            val sharingLink = repository.generateSharingLink(cookbookId)
            Result.success(sharingLink)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Get Sharing Info Use Case
class GetSharingInfo(
    private val repository: CookbookRepository
) {
    operator fun invoke(cookbookId: String): kotlinx.coroutines.flow.Flow<com.ourcookbook.ui.viewmodel.CookbookSharingInfo> {
        return repository.getSharingInfo(cookbookId)
    }
}
