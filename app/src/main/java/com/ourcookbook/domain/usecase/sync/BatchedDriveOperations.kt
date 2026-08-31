package com.ourcookbook.domain.usecase.sync

import com.ourcookbook.data.repository.DriveRepository
import com.ourcookbook.domain.model.Recipe
import com.ourcookbook.domain.repository.RecipeRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import javax.inject.Inject

/**
 * Use case for batched Drive operations with rate limiting
 * Task 2.2.14: Batched Drive operations for rate limiting
 *
 * Prevents rate limiting by implementing proper batching and delays
 */
class BatchedDriveOperations @Inject constructor(
    private val driveRepository: DriveRepository,
    private val recipeRepository: RecipeRepository,
    private val pushToDriveWithChecksum: PushToDriveWithChecksum
) {

    /**
     * Result of batched operation
     */
    sealed class BatchResult {
        data class Success(
            val totalOperations: Int,
            val successfulOperations: Int,
            val failedOperations: Int,
            val failedIds: List<String>,
            val durationMs: Long
        ) : BatchResult()
        
        data class Failure(
            val errorMessage: String,
            val exception: Exception? = null
        ) : BatchResult()
    }

    /**
     * Batch operation settings
     */
    data class BatchSettings(
        val batchSize: Int = 10,           // Number of operations per batch
        val delayBetweenBatches: Long = 1000, // Delay between batches in ms
        val maxRetries: Int = 3,           // Max retries for failed operations
        val retryDelay: Long = 2000,       // Delay between retries in ms
        val timeout: Long = 30000          // Timeout for each operation in ms
    )

    /**
     * Rate limiting strategy
     */
    enum class RateLimitStrategy {
        FIXED_DELAY,        // Fixed delay between batches
        EXPONENTIAL_BACKOFF, // Exponential backoff on failures
        ADAPTIVE,           // Adaptive based on response times
        NONE                // No rate limiting
    }

    /**
     * Perform batched push operations with rate limiting
     *
     * @param recipeIds List of recipe IDs to push
     * @param settings Batch settings
     * @param strategy Rate limiting strategy
     * @return BatchResult with operation information
     */
    suspend operator fun invoke(
        recipeIds: List<String> = emptyList(),
        settings: BatchSettings = BatchSettings(),
        strategy: RateLimitStrategy = RateLimitStrategy.FIXED_DELAY
    ): BatchResult {
        if (recipeIds.isEmpty()) {
            return BatchResult.Failure("No recipe IDs provided")
        }
        
        return try {
            val startTime = System.currentTimeMillis()
            
            // Get all recipes to push
            val recipes = recipeRepository.getRecipesByIds(recipeIds)
            
            if (recipes.isEmpty()) {
                return BatchResult.Failure("No recipes found for the provided IDs")
            }
            
            // Process in batches
            val results = mutableListOf<Boolean>()
            val failedIds = mutableListOf<String>()
            
            recipes.chunked(settings.batchSize).forEachIndexed { batchIndex, batch ->
                // Apply rate limiting
                applyRateLimiting(batchIndex, settings, strategy)
                
                // Process batch
                val batchResults = processBatch(batch, settings)
                
                results.addAll(batchResults.map { it.success })
                failedIds.addAll(batchResults.filter { !it.success }.map { it.recipeId })
            }
            
            val durationMs = System.currentTimeMillis() - startTime
            
            BatchResult.Success(
                totalOperations = recipes.size,
                successfulOperations = results.count { it },
                failedOperations = results.count { !it },
                failedIds = failedIds,
                durationMs = durationMs
            )
            
        } catch (e: Exception) {
            BatchResult.Failure(
                errorMessage = "Failed to perform batched operations: ${e.message}",
                exception = e
            )
        }
    }

    /**
     * Process a single batch of recipes
     */
    private suspend fun processBatch(
        batch: List<Recipe>,
        settings: BatchSettings
    ): List<OperationResult> {
        return coroutineScope {
            batch.map { recipe ->
                async {
                    var retries = 0
                    var success = false
                    
                    while (retries < settings.maxRetries && !success) {
                        try {
                            val checksum = pushToDriveWithChecksum.calculateChecksum(recipe)
                            driveRepository.pushRecipe(
                                recipe,
                                checksum,
                                java.util.UUID.randomUUID().toString()
                            )
                            
                            // Update sync status
                            recipeRepository.markRecipeSynced(recipe.id)

                            success = true
                        } catch (e: Exception) {
                            retries++
                            if (retries < settings.maxRetries) {
                                delay(settings.retryDelay)
                            }
                        }
                    }
                    
                    OperationResult(recipe.id, success)
                }
            }.awaitAll()
        }
    }

    /**
     * Apply rate limiting between batches
     */
    private suspend fun applyRateLimiting(
        batchIndex: Int,
        settings: BatchSettings,
        strategy: RateLimitStrategy
    ) {
        if (batchIndex == 0) return // No delay before first batch
        
        when (strategy) {
            RateLimitStrategy.FIXED_DELAY -> {
                delay(settings.delayBetweenBatches)
            }
            RateLimitStrategy.EXPONENTIAL_BACKOFF -> {
                // Exponential backoff: delay = baseDelay * 2^batchIndex
                val backoffDelay = settings.delayBetweenBatches * (1L shl batchIndex)
                delay(backoffDelay.coerceAtMost(10000)) // Cap at 10 seconds
            }
            RateLimitStrategy.ADAPTIVE -> {
                // Adaptive: Use fixed delay for now (would track response times in production)
                delay(settings.delayBetweenBatches)
            }
            RateLimitStrategy.NONE -> {
                // No delay
            }
        }
    }

    /**
     * Result of individual operation within a batch
     */
    private data class OperationResult(
        val recipeId: String,
        val success: Boolean
    )

    /**
     * Perform batched pull operations with rate limiting
     *
     * @param recipeIds List of recipe IDs to pull
     * @param settings Batch settings
     * @param strategy Rate limiting strategy
     * @return BatchResult with operation information
     */
    suspend fun pullBatch(
        recipeIds: List<String> = emptyList(),
        settings: BatchSettings = BatchSettings(),
        strategy: RateLimitStrategy = RateLimitStrategy.FIXED_DELAY
    ): BatchResult {
        if (recipeIds.isEmpty()) {
            return BatchResult.Failure("No recipe IDs provided")
        }
        
        return try {
            val startTime = System.currentTimeMillis()
            
            val results = mutableListOf<Boolean>()
            val failedIds = mutableListOf<String>()
            
            recipeIds.chunked(settings.batchSize).forEachIndexed { batchIndex, batch ->
                // Apply rate limiting
                applyRateLimiting(batchIndex, settings, strategy)
                
                // Process batch
                val batchResults = processPullBatch(batch, settings)
                
                results.addAll(batchResults.map { it.success })
                failedIds.addAll(batchResults.filter { !it.success }.map { it.recipeId })
            }
            
            val durationMs = System.currentTimeMillis() - startTime
            
            BatchResult.Success(
                totalOperations = recipeIds.size,
                successfulOperations = results.count { it },
                failedOperations = results.count { !it },
                failedIds = failedIds,
                durationMs = durationMs
            )
            
        } catch (e: Exception) {
            BatchResult.Failure(
                errorMessage = "Failed to perform batched pull: ${e.message}",
                exception = e
            )
        }
    }

    /**
     * Process a single batch of pull operations
     */
    private suspend fun processPullBatch(
        batch: List<String>,
        settings: BatchSettings
    ): List<OperationResult> {
        return coroutineScope {
            batch.map { recipeId ->
                async {
                    var retries = 0
                    var success = false
                    
                    while (retries < settings.maxRetries && !success) {
                        try {
                            val recipe = driveRepository.pullRecipe(recipeId)
                            
                            if (recipe != null) {
                                // Save the pulled recipe
                                if (recipeRepository.getRecipeById(recipe.id) == null) {
                                    recipeRepository.createRecipe(recipe)
                                } else {
                                    recipeRepository.updateRecipe(recipe)
                                }
                                
                                // Update sync status
                                recipeRepository.markRecipeSynced(recipe.id)

                                success = true
                            }
                        } catch (e: Exception) {
                            retries++
                            if (retries < settings.maxRetries) {
                                delay(settings.retryDelay)
                            }
                        }
                    }
                    
                    OperationResult(recipeId, success)
                }
            }.awaitAll()
        }
    }

    /**
     * Sync all recipes with rate limiting
     *
     * @param settings Batch settings
     * @param strategy Rate limiting strategy
     * @return BatchResult with operation information
     */
    suspend fun syncAll(
        settings: BatchSettings = BatchSettings(),
        strategy: RateLimitStrategy = RateLimitStrategy.FIXED_DELAY
    ): BatchResult {
        // Get all recipes that need sync
        val recipesToSync = recipeRepository.getRecipesNeedingSync()
        
        if (recipesToSync.isEmpty()) {
            return BatchResult.Failure("No recipes need sync")
        }
        
        // Push new/changed recipes (all in recipesToSync need syncing)
        val newOrChanged = recipesToSync
        val pushResult = this(newOrChanged.map { it.id }, settings, strategy)
        
        if (pushResult is BatchResult.Failure) {
            return pushResult
        }
        
        // Pull any recipes that might have been changed remotely
        val allRecipeIds = recipeRepository.getAllRecipesOnce().map { it.id }
        val pullResult = pullBatch(allRecipeIds, settings, strategy)
        
        if (pullResult is BatchResult.Failure) {
            return pullResult
        }
        
        // Combine results
        return if (pushResult is BatchResult.Success && pullResult is BatchResult.Success) {
            BatchResult.Success(
                totalOperations = pushResult.totalOperations + pullResult.totalOperations,
                successfulOperations = pushResult.successfulOperations + pullResult.successfulOperations,
                failedOperations = pushResult.failedOperations + pullResult.failedOperations,
                failedIds = pushResult.failedIds + pullResult.failedIds,
                durationMs = pushResult.durationMs + pullResult.durationMs
            )
        } else {
            BatchResult.Failure("Sync completed with some failures")
        }
    }

    /**
     * Get recommended batch settings based on network conditions
     */
    suspend fun getRecommendedSettings(): BatchSettings {
        // In production, this would check network speed and adjust accordingly
        // For now, return default settings
        return BatchSettings()
    }

    /**
     * Check if rate limiting is needed
     */
    suspend fun isRateLimitingNeeded(): Boolean {
        // Check if we've been rate limited recently
        // In production, this would track API responses
        return false
    }

    /**
     * Get current rate limit status
     */
    suspend fun getRateLimitStatus(): RateLimitStatus {
        // In production, this would check the actual rate limit status from Drive API
        return RateLimitStatus(
            isRateLimited = false,
            remainingQuota = 1000,
            resetTime = null
        )
    }

    /**
     * Rate limit status information
     */
    data class RateLimitStatus(
        val isRateLimited: Boolean,
        val remainingQuota: Int,
        val resetTime: Long?
    )

    /**
     * Calculate optimal batch size based on recipe size
     */
    fun calculateOptimalBatchSize(recipes: List<Recipe>): Int {
        // Estimate average recipe size (in KB)
        val avgSize = recipes.map { estimateRecipeSize(it) }.average().toInt()
        
        // Target batch size: ~1MB per batch (Drive API has limits)
        val targetBatchSize = 1000 // KB
        val optimalCount = targetBatchSize / maxOf(avgSize, 1)
        
        return maxOf(1, minOf(optimalCount, 50)) // Between 1 and 50
    }

    /**
     * Estimate recipe size in KB
     */
    private fun estimateRecipeSize(recipe: Recipe): Int {
        // Rough estimate based on content
        var size = 0
        
        size += recipe.title.length
        size += recipe.description?.length ?: 0
        size += recipe.category.length
        size += recipe.source?.length ?: 0
        
        size += recipe.ingredients.sumOf { 
            it.name.length + (it.amount?.length ?: 0) + (it.unit?.length ?: 0) + (it.notes?.length ?: 0)
        }
        
        size += recipe.instructions.sumOf { it.length }
        size += recipe.tags.sumOf { it.length }
        
        // Convert to KB and add some overhead
        return (size / 1024) + 1
    }
}
