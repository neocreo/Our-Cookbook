package com.ourcookbook.data.service.performance

import android.content.Context
import androidx.room.RoomDatabase
import com.ourcookbook.domain.repository.RecipeRepository
import com.ourcookbook.domain.usecase.performance.BackgroundTaskManager
import com.ourcookbook.domain.usecase.performance.DatabaseQueryOptimizer
import com.ourcookbook.domain.usecase.performance.ImageCache
import com.ourcookbook.domain.usecase.performance.MemoryMonitor
import com.ourcookbook.domain.usecase.performance.PerformanceMetricsTracker
import com.ourcookbook.domain.usecase.performance.SearchQueryManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Performance Service Module
 * 
 * Provides performance optimization services for the app
 */
@Module
@InstallIn(SingletonComponent::class)
object PerformanceServiceModule {
    
    @Provides
    @Singleton
    fun provideDatabaseQueryOptimizer(
        database: RoomDatabase
    ): DatabaseQueryOptimizer {
        return DatabaseQueryOptimizer(database)
    }
    
    @Provides
    @Singleton
    fun provideImageCache(): ImageCache {
        return ImageCache(maxSize = 200)
    }
    
    @Provides
    @Singleton
    fun provideBackgroundTaskManager(): BackgroundTaskManager {
        return BackgroundTaskManager()
    }
    
    @Provides
    @Singleton
    fun provideMemoryMonitor(): MemoryMonitor {
        return MemoryMonitor()
    }
    
    @Provides
    @Singleton
    fun providePerformanceMetricsTracker(): PerformanceMetricsTracker {
        return PerformanceMetricsTracker()
    }
    
    @Provides
    fun provideSearchQueryManager(
        recipeRepository: RecipeRepository
    ): SearchQueryManager {
        return SearchQueryManager(recipeRepository)
    }
}

/**
 * Performance monitoring service
 */
class PerformanceMonitoringService(
    @ApplicationContext private val context: Context
) {
    
    private val metricsTracker = PerformanceMetricsTracker()
    private val memoryMonitor = MemoryMonitor()
    
    init {
        memoryMonitor.startMonitoring()
    }
    
    /**
     * Get current performance metrics
     */
    fun getCurrentMetrics(): PerformanceReport {
        return PerformanceReport(
            fps = metricsTracker.getFps(),
            averageFrameTime = metricsTracker.getAverageFrameTime(),
            performanceScore = metricsTracker.getPerformanceScore(),
            memoryUsage = memoryMonitor.getMemoryUsage(),
            memoryState = memoryMonitor.memoryState.value,
            suggestedActions = memoryMonitor.suggestActions(memoryMonitor.memoryState.value)
        )
    }
    
    /**
     * Record frame time
     */
    fun recordFrameTime(timeMs: Long) {
        metricsTracker.recordFrameTime(timeMs)
    }
    
    /**
     * Record operation time
     */
    fun recordOperationTime(operation: String, timeMs: Long) {
        metricsTracker.recordOperationTime(operation, timeMs)
    }
    
    /**
     * Reset metrics
     */
    fun resetMetrics() {
        metricsTracker.reset()
    }
    
    /**
     * Stop monitoring
     */
    fun stopMonitoring() {
        memoryMonitor.stopMonitoring()
    }
}

/**
 * Performance report data class
 */
data class PerformanceReport(
    val fps: Float,
    val averageFrameTime: Float,
    val performanceScore: Int,
    val memoryUsage: com.ourcookbook.domain.usecase.performance.MemoryUsage,
    val memoryState: com.ourcookbook.domain.usecase.performance.MemoryState,
    val suggestedActions: List<com.ourcookbook.domain.usecase.performance.MemoryAction>
) {
    val isPerformanceGood: Boolean get() = performanceScore >= 80
    val isPerformanceAcceptable: Boolean get() = performanceScore >= 60
    val isPerformancePoor: Boolean get() = performanceScore < 60
}

/**
 * Performance tuning utilities
 */
object PerformanceTuning {
    
    // Recommended settings based on device capabilities
    const val RECOMMENDED_PAGE_SIZE = 20
    const val RECOMMENDED_IMAGE_CACHE_SIZE = 200
    const val RECOMMENDED_QUERY_CACHE_SIZE = 100
    const val RECOMMENDED_SEARCH_DEBOUNCE = 500L // ms
    const val RECOMMENDED_SEARCH_THROTTLE = 300L // ms
    
    /**
     * Get recommended settings based on device memory
     */
    fun getRecommendedSettings(memoryMB: Long): PerformanceSettings {
        return when {
            memoryMB >= 6000 -> PerformanceSettings(
                pageSize = 30,
                imageCacheSize = 300,
                queryCacheSize = 200,
                searchDebounce = 300,
                searchThrottle = 200,
                enableAnimations = true,
                enableBackgroundSync = true
            )
            memoryMB >= 4000 -> PerformanceSettings(
                pageSize = 25,
                imageCacheSize = 250,
                queryCacheSize = 150,
                searchDebounce = 400,
                searchThrottle = 250,
                enableAnimations = true,
                enableBackgroundSync = true
            )
            memoryMB >= 2000 -> PerformanceSettings(
                pageSize = 20,
                imageCacheSize = 200,
                queryCacheSize = 100,
                searchDebounce = 500,
                searchThrottle = 300,
                enableAnimations = true,
                enableBackgroundSync = true
            )
            else -> PerformanceSettings(
                pageSize = 15,
                imageCacheSize = 100,
                queryCacheSize = 50,
                searchDebounce = 700,
                searchThrottle = 400,
                enableAnimations = false,
                enableBackgroundSync = false
            )
        }
    }
    
    /**
     * Optimize settings for low-end devices
     */
    fun getLowEndSettings(): PerformanceSettings {
        return PerformanceSettings(
            pageSize = 10,
            imageCacheSize = 50,
            queryCacheSize = 30,
            searchDebounce = 1000,
            searchThrottle = 500,
            enableAnimations = false,
            enableBackgroundSync = false
        )
    }
    
    /**
     * Optimize settings for high-end devices
     */
    fun getHighEndSettings(): PerformanceSettings {
        return PerformanceSettings(
            pageSize = 40,
            imageCacheSize = 500,
            queryCacheSize = 300,
            searchDebounce = 200,
            searchThrottle = 100,
            enableAnimations = true,
            enableBackgroundSync = true
        )
    }
}

/**
 * Performance settings data class
 */
data class PerformanceSettings(
    val pageSize: Int,
    val imageCacheSize: Int,
    val queryCacheSize: Int,
    val searchDebounce: Long,
    val searchThrottle: Long,
    val enableAnimations: Boolean,
    val enableBackgroundSync: Boolean
)

/**
 * Extension functions for easy performance tuning
 */

fun Context.getPerformanceMonitoringService(): PerformanceMonitoringService {
    return PerformanceMonitoringService(this)
}

fun Context.getRecommendedPerformanceSettings(): PerformanceSettings {
    val memoryMB = Runtime.getRuntime().maxMemory() / (1024 * 1024)
    return PerformanceTuning.getRecommendedSettings(memoryMB)
}

/**
 * Performance logging utility
 */
object PerformanceLogger {
    
    private const val TAG = "Performance"
    private var enabled = true
    
    fun logOperation(operation: String, timeMs: Long) {
        if (!enabled) return
        android.util.Log.d(TAG, "$operation: ${timeMs}ms")
    }
    
    fun logFps(fps: Float) {
        if (!enabled) return
        android.util.Log.d(TAG, "FPS: $fps")
    }
    
    fun logMemoryUsage(usage: com.ourcookbook.domain.usecase.performance.MemoryUsage) {
        if (!enabled) return
        android.util.Log.d(TAG, "Memory: ${usage.usagePercentage}% (${usage.usedBytes / (1024 * 1024)}MB / ${usage.maxBytes / (1024 * 1024)}MB)")
    }
    
    fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
    }
}
