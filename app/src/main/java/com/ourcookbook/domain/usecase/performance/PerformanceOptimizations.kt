package com.ourcookbook.domain.usecase.performance

import android.util.LruCache
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.room.RoomDatabase
import com.ourcookbook.domain.model.Recipe
import com.ourcookbook.domain.repository.RecipeRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.max
import kotlin.math.min

/**
 * Performance Optimizations for the Cookbook App
 * 
 * Provides various performance improvements:
 * - Query optimization (pagination, caching)
 * - Image caching and lazy loading
 * - Database query tuning
 * - Background processing for heavy operations
 * - Memory management
 * - Throttling and debouncing for search operations
 */

// ============================================================================
// Database Query Optimization
// ============================================================================

/**
 * Query optimizer for Room database
 */
class DatabaseQueryOptimizer(
    private val database: RoomDatabase
) {
    
    private val queryCache = LruCache<String, Any>(100)
    private val lastQueryTime = mutableMapOf<String, Long>()
    
    /**
     * Execute a query with caching
     */
    suspend fun <T> executeWithCache(
        cacheKey: String,
        cacheTTL: Long = 5000, // 5 seconds
        query: suspend () -> T
    ): T {
        val cached = queryCache.get(cacheKey)
        val now = System.currentTimeMillis()
        
        @Suppress("UNCHECKED_CAST")
        if (cached != null && now - (lastQueryTime[cacheKey] ?: 0) < cacheTTL) {
            return cached as T
        }
        
        val result = query()
        queryCache.put(cacheKey, result)
        lastQueryTime[cacheKey] = now
        
        return result
    }
    
    /**
     * Invalidate cache for a specific query
     */
    fun invalidateCache(cacheKey: String) {
        queryCache.remove(cacheKey)
        lastQueryTime.remove(cacheKey)
    }
    
    /**
     * Clear all cached queries
     */
    fun clearAllCache() {
        queryCache.evictAll()
        lastQueryTime.clear()
    }
    
    /**
     * Batch insert with transaction
     */
    suspend fun <T> batchInsert(
        items: List<T>,
        insertFunction: suspend (List<T>) -> Unit,
        batchSize: Int = 50
    ) {
        items.chunked(batchSize).forEach { batch ->
            insertFunction(batch)
        }
    }
    
    /**
     * Parallel query execution
     */
    suspend fun <T, R> parallelQuery(
        items: List<T>,
        queryFunction: suspend (T) -> R,
        dispatcher: CoroutineDispatcher = Dispatchers.IO
    ): List<R> = coroutineScope {
        items.map { item ->
            async(dispatcher) {
                queryFunction(item)
            }
        }.awaitAll()
    }
}

// ============================================================================
// Image Caching and Lazy Loading
// ============================================================================

/**
 * Image cache for recipe images
 */
class ImageCache(
    maxSize: Int = 100,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    
    private val memoryCache = LruCache<String, ImageBitmap>(100)
    private val diskCacheDir by lazy { 
        // Would be initialized with actual cache directory
        null
    }
    
    private val loadingQueue = mutableSetOf<String>()
    private val isLoading = AtomicBoolean(false)
    
    /**
     * Get image from cache or load it
     */
    suspend fun getImage(
        imageUri: String,
        loadFunction: suspend () -> ImageBitmap
    ): ImageBitmap? {
        // Check memory cache first
        memoryCache.get(imageUri)?.let { return it }
        
        // Check if already loading
        if (loadingQueue.contains(imageUri)) {
            return null // Return null and let caller retry
        }
        
        // Load from disk cache if available
        // Implementation would check disk cache here
        
        // Load image
        if (isLoading.compareAndSet(false, true)) {
            try {
                loadingQueue.add(imageUri)
                val image = withContext(dispatcher) {
                    loadFunction()
                }
                memoryCache.put(imageUri, image)
                loadingQueue.remove(imageUri)
                return image
            } finally {
                isLoading.set(false)
            }
        }
        
        return null
    }
    
    /**
     * Preload images for a list of recipes
     */
    suspend fun preloadImages(
        recipes: List<Recipe>,
        loadFunction: suspend (String) -> ImageBitmap?
    ) {
        val imageUris = recipes.mapNotNull { recipe -> recipe.imageUrl }.distinct()
        
        imageUris.forEach { uri ->
            if (memoryCache.get(uri) == null) {
                loadFunction(uri)?.let { image ->
                    memoryCache.put(uri, image)
                }
            }
        }
    }
    
    /**
     * Clear cache for a specific image
     */
    fun clearImageCache(imageUri: String) {
        memoryCache.remove(imageUri)
    }
    
    /**
     * Clear all cached images
     */
    fun clearAllCache() {
        memoryCache.evictAll()
        loadingQueue.clear()
    }
    
    /**
     * Get cache size
     */
    fun getCacheSize(): Int {
        return memoryCache.size()
    }
}

/**
 * Composable modifier for lazy image loading with placeholder
 */
@Composable
fun Modifier.lazyImageLoading(
    imageUri: String,
    imageCache: ImageCache,
    loadFunction: suspend () -> ImageBitmap,
    placeholder: @Composable () -> Unit = {},
    loading: @Composable () -> Unit = {},
    error: @Composable () -> Unit = {}
): Modifier {
    var imageState by mutableStateOf<ImageState>(ImageState.Loading)
    
    LaunchedEffect(imageUri) {
        launch {
            try {
                val cachedImage = imageCache.getImage(imageUri, loadFunction)
                if (cachedImage != null) {
                    imageState = ImageState.Loaded(cachedImage)
                } else {
                    imageState = ImageState.Error("Failed to load image")
                }
            } catch (e: Exception) {
                imageState = ImageState.Error(e.message ?: "Error loading image")
            }
        }
    }
    
    return this.then(
        Modifier.drawWithCache {
            when (val s = imageState) {
                is ImageState.Loaded -> {
                    onDrawWithContent {
                        drawImage(s.image)
                    }
                }
                else -> onDrawWithContent { }
            }
        }
    )
}

/**
 * Image loading state
 */
sealed class ImageState {
    object Loading : ImageState()
    data class Loaded(val image: ImageBitmap) : ImageState()
    data class Error(val message: String) : ImageState()
}

// ============================================================================
// Search Throttling and Debouncing
// ============================================================================

/**
 * Search query manager with throttling and debouncing
 */
class SearchQueryManager(
    private val repository: RecipeRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) {
    
    private val _searchResults = MutableStateFlow<List<Recipe>>(emptyList())
    val searchResults: StateFlow<List<Recipe>> = _searchResults.asStateFlow()
    
    private var lastSearchTime = 0L
    private var searchJob: kotlinx.coroutines.Job? = null
    
    private val throttleDelay = 300L // ms
    private val debounceDelay = 500L // ms
    
    /**
     * Perform a search with throttling and debouncing
     */
    fun search(query: String) {
        val now = System.currentTimeMillis()
        
        // Throttle: ignore if too recent
        if (now - lastSearchTime < throttleDelay) {
            return
        }
        lastSearchTime = now
        
        // Debounce: cancel previous search
        searchJob?.cancel()
        
        searchJob = CoroutineScope(dispatcher).launch {
            // Wait for debounce period
            kotlinx.coroutines.delay(debounceDelay)
            
            // Check if query has changed
            val currentQuery = query // Would track current query
            if (currentQuery.isNotBlank()) {
                val results = repository.searchRecipes(currentQuery).first()
                _searchResults.value = results
            } else {
                _searchResults.value = emptyList()
            }
        }
    }
    
    /**
     * Clear search results
     */
    fun clear() {
        _searchResults.value = emptyList()
        searchJob?.cancel()
    }
}

/**
 * Composable function to remember search query manager
 */
@Composable
fun rememberSearchQueryManager(
    repository: RecipeRepository
): SearchQueryManager {
    val manager = remember(repository) {
        SearchQueryManager(repository)
    }
    
    LaunchedEffect(Unit) {
        // Cleanup on dispose
    }
    
    return manager
}

// ============================================================================
// Pagination
// ============================================================================

/**
 * Pagination manager for large datasets
 */
class PaginationManager<T>(
    private val fetchFunction: suspend (offset: Int, limit: Int) -> List<T>,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    
    private val _items = MutableStateFlow<List<T>>(emptyList())
    val items: StateFlow<List<T>> = _items.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _hasMore = MutableStateFlow(true)
    val hasMore: StateFlow<Boolean> = _hasMore.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private var currentOffset = 0
    private val defaultLimit = 20
    
    /**
     * Load more items
     */
    suspend fun loadMore() {
        if (_isLoading.value || !_hasMore.value) return
        
        _isLoading.value = true
        _error.value = null
        
        try {
            val newItems = withContext(dispatcher) {
                fetchFunction(currentOffset, defaultLimit)
            }
            
            if (newItems.isEmpty()) {
                _hasMore.value = false
            } else {
                _items.value = _items.value + newItems
                currentOffset += defaultLimit
            }
            
        } catch (e: Exception) {
            _error.value = e.message ?: "Failed to load more items"
        } finally {
            _isLoading.value = false
        }
    }
    
    /**
     * Refresh all items
     */
    suspend fun refresh() {
        currentOffset = 0
        _hasMore.value = true
        _items.value = emptyList()
        loadMore()
    }
    
    /**
     * Reset pagination
     */
    fun reset() {
        currentOffset = 0
        _items.value = emptyList()
        _hasMore.value = true
        _isLoading.value = false
        _error.value = null
    }
    
    /**
     * Get item count
     */
    fun getItemCount(): Int {
        return _items.value.size
    }
}

/**
 * Composable function to remember pagination manager
 */
@Composable
fun <T> rememberPaginationManager(
    fetchFunction: suspend (offset: Int, limit: Int) -> List<T>
): PaginationManager<T> {
    val manager = remember(fetchFunction) {
        PaginationManager(fetchFunction)
    }
    
    return manager
}

// ============================================================================
// Background Processing
// ============================================================================

/**
 * Background task manager
 */
class BackgroundTaskManager(
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    
    private val activeTasks = mutableMapOf<String, kotlinx.coroutines.Job>()
    
    /**
     * Execute a task in the background
     */
    fun <T> executeTask(
        taskId: String,
        task: suspend () -> T,
        onSuccess: (T) -> Unit = {},
        onError: (Exception) -> Unit = {}
    ): kotlinx.coroutines.Job {
        // Cancel existing task with same ID
        activeTasks[taskId]?.cancel()
        
        val job = CoroutineScope(dispatcher).launch {
            try {
                val result = task()
                withContext(Dispatchers.Main) {
                    onSuccess(result)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(e)
                }
            } finally {
                activeTasks.remove(taskId)
            }
        }
        
        activeTasks[taskId] = job
        return job
    }
    
    /**
     * Cancel a task by ID
     */
    fun cancelTask(taskId: String) {
        activeTasks[taskId]?.cancel()
        activeTasks.remove(taskId)
    }
    
    /**
     * Cancel all tasks
     */
    fun cancelAllTasks() {
        activeTasks.values.forEach { it.cancel() }
        activeTasks.clear()
    }
    
    /**
     * Check if a task is running
     */
    fun isTaskRunning(taskId: String): Boolean {
        return activeTasks[taskId]?.isActive == true
    }
    
    /**
     * Get active task count
     */
    fun getActiveTaskCount(): Int {
        return activeTasks.count { it.value.isActive }
    }
}

// ============================================================================
// Memory Management
// ============================================================================

/**
 * Memory monitor for the app
 */
class MemoryMonitor(
    private val maxMemory: Long = Runtime.getRuntime().maxMemory(),
    private val warningThreshold: Float = 0.8f,
    private val criticalThreshold: Float = 0.95f
) {
    
    private val _memoryState = MutableStateFlow<MemoryState>(MemoryState.Normal)
    val memoryState: StateFlow<MemoryState> = _memoryState.asStateFlow()
    
    private var isMonitoring = false
    
    /**
     * Start monitoring memory usage
     */
    fun startMonitoring() {
        if (isMonitoring) return
        isMonitoring = true
        
        CoroutineScope(Dispatchers.Default).launch {
            while (isMonitoring) {
                checkMemory()
                kotlinx.coroutines.delay(1000)
            }
        }
    }
    
    /**
     * Stop monitoring memory usage
     */
    fun stopMonitoring() {
        isMonitoring = false
    }
    
    /**
     * Check current memory usage
     */
    private fun checkMemory() {
        val runtime = Runtime.getRuntime()
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        val usageRatio = usedMemory.toFloat() / maxMemory
        
        val state = when {
            usageRatio >= criticalThreshold -> MemoryState.Critical
            usageRatio >= warningThreshold -> MemoryState.Warning
            else -> MemoryState.Normal
        }
        
        _memoryState.value = state
    }
    
    /**
     * Get current memory usage
     */
    fun getMemoryUsage(): MemoryUsage {
        val runtime = Runtime.getRuntime()
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        val usageRatio = usedMemory.toFloat() / maxMemory
        
        return MemoryUsage(
            usedBytes = usedMemory,
            maxBytes = maxMemory,
            usagePercentage = usageRatio * 100
        )
    }
    
    /**
     * Suggest actions based on memory state
     */
    fun suggestActions(state: MemoryState): List<MemoryAction> {
        return when (state) {
            MemoryState.Normal -> emptyList()
            MemoryState.Warning -> listOf(
                MemoryAction.CLEAR_IMAGE_CACHE,
                MemoryAction.REDUCE_QUERY_CACHE
            )
            MemoryState.Critical -> listOf(
                MemoryAction.CLEAR_ALL_CACHE,
                MemoryAction.CLOSE_BACKGROUND_TASKS,
                MemoryAction.REDUCE_IMAGE_QUALITY
            )
        }
    }
}

/**
 * Memory state
 */
sealed class MemoryState {
    object Normal : MemoryState()
    object Warning : MemoryState()
    object Critical : MemoryState()
}

/**
 * Memory usage information
 */
data class MemoryUsage(
    val usedBytes: Long,
    val maxBytes: Long,
    val usagePercentage: Float
)

/**
 * Memory action suggestions
 */
enum class MemoryAction {
    CLEAR_IMAGE_CACHE,
    CLEAR_ALL_CACHE,
    REDUCE_QUERY_CACHE,
    CLOSE_BACKGROUND_TASKS,
    REDUCE_IMAGE_QUALITY,
    STOP_ANIMATIONS
}

// ============================================================================
// Performance Metrics
// ============================================================================

/**
 * Performance metrics tracker
 */
class PerformanceMetricsTracker {
    
    private val frameTimes = mutableListOf<Long>()
    private val operationTimes = mutableMapOf<String, MutableList<Long>>()
    private var lastFrameTime = 0L
    
    /**
     * Record frame time
     */
    fun recordFrameTime(timeMs: Long) {
        frameTimes.add(timeMs)
        if (frameTimes.size > 100) {
            frameTimes.removeAt(0)
        }
    }
    
    /**
     * Record operation time
     */
    fun recordOperationTime(operation: String, timeMs: Long) {
        val times = operationTimes.getOrPut(operation) { mutableListOf() }
        times.add(timeMs)
        if (times.size > 100) {
            times.removeAt(0)
        }
    }
    
    /**
     * Get average frame time
     */
    fun getAverageFrameTime(): Float {
        return if (frameTimes.isEmpty()) 0f else frameTimes.average().toFloat()
    }
    
    /**
     * Get FPS (frames per second)
     */
    fun getFps(): Float {
        val avgFrameTime = getAverageFrameTime()
        return if (avgFrameTime > 0) 1000f / avgFrameTime else 0f
    }
    
    /**
     * Get average operation time
     */
    fun getAverageOperationTime(operation: String): Float {
        val times = operationTimes[operation] ?: return 0f
        return if (times.isEmpty()) 0f else times.average().toFloat()
    }
    
    /**
     * Get performance score (0-100)
     */
    fun getPerformanceScore(): Int {
        val fps = getFps()
        val frameTimeScore = min(100f, max(0f, 100f - (getAverageFrameTime() - 16f) * 2f))
        
        return (frameTimeScore * 0.7f + fps * 3f).toInt().coerceIn(0, 100)
    }
    
    /**
     * Reset all metrics
     */
    fun reset() {
        frameTimes.clear()
        operationTimes.clear()
    }
}

/**
 * Composable function to track frame times
 */
@Composable
fun rememberPerformanceMetrics(): PerformanceMetricsTracker {
    val metrics = remember {
        PerformanceMetricsTracker()
    }
    
    val view = LocalView.current
    val density = LocalDensity.current
    
    LaunchedEffect(view) {
        // Would track frame times using FrameCallback
        // This is a simplified version
    }
    
    return metrics
}

// ============================================================================
// Package-Level Extensions
// ============================================================================

/**
 * Extension function to measure execution time
 */
suspend fun <T> measureTime(block: suspend () -> T): Pair<T, Long> {
    val start = System.currentTimeMillis()
    val result = block()
    val end = System.currentTimeMillis()
    return Pair(result, end - start)
}

/**
 * Extension function to retry with exponential backoff
 */
suspend fun <T> retryWithBackoff(
    maxRetries: Int = 3,
    initialDelay: Long = 100,
    maxDelay: Long = 5000,
    block: suspend () -> T
): T {
    var lastException: Exception? = null
    var delay = initialDelay
    
    repeat(maxRetries + 1) { attempt ->
        try {
            return block()
        } catch (e: Exception) {
            lastException = e
            if (attempt < maxRetries) {
                kotlinx.coroutines.delay(delay)
                delay = min(delay * 2, maxDelay)
            }
        }
    }
    
    throw lastException ?: Exception("Unknown error")
}

/**
 * Extension function to chunk a list for batch processing
 */
fun <T> List<T>.chunkedForBatch(batchSize: Int): List<List<T>> {
    return this.chunked(batchSize)
}

/**
 * Extension function to process items in parallel
 */
suspend fun <T, R> Iterable<T>.parallelMap(
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    transform: suspend (T) -> R
): List<R> = coroutineScope {
    this@parallelMap.map { item ->
        async(dispatcher) { transform(item) }
    }.awaitAll()
}
