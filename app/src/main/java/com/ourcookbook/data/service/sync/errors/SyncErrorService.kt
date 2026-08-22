package com.ourcookbook.data.service.sync.errors

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.ourcookbook.domain.repository.SyncLogRepository
import com.ourcookbook.domain.usecase.sync.errors.SyncError
import com.ourcookbook.domain.usecase.sync.errors.SyncErrorHandler
import com.ourcookbook.domain.usecase.sync.errors.SyncErrorWithContext
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Sync Error Service Module
 * 
 * Provides error handling services for sync operations
 */
@Module
@InstallIn(SingletonComponent::class)
object SyncErrorServiceModule {
    
    @Provides
    @Singleton
    fun provideSyncErrorHandler(
        @ApplicationContext context: Context,
        syncLogRepository: SyncLogRepository
    ): SyncErrorHandler {
        return SyncErrorHandler(context, syncLogRepository)
    }
}

/**
 * Sync error notification service
 */
class SyncErrorNotificationService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val syncErrorHandler: SyncErrorHandler
) {
    
    /**
     * Show notification for a sync error
     */
    fun showErrorNotification(error: SyncErrorWithContext) {
        // Implementation would use Android NotificationManager
        // For now, just log the error
        android.util.Log.e("SyncError", "Error: ${error.error::class.simpleName} - ${syncErrorHandler.getUserFriendlyMessage(error.error)}")
    }
    
    /**
     * Show notification for multiple errors
     */
    fun showMultipleErrorsNotification(errors: List<SyncErrorWithContext>) {
        val count = errors.size
        val message = if (count == 1) {
            syncErrorHandler.getUserFriendlyMessage(errors.first().error)
        } else {
            "$count sync errors occurred"
        }
        
        // Implementation would use Android NotificationManager
        android.util.Log.e("SyncError", message)
    }
    
    /**
     * Clear error notifications
     */
    fun clearErrorNotifications() {
        // Implementation would clear notifications
    }
}

/**
 * Sync error recovery service
 */
class SyncErrorRecoveryService @Inject constructor(
    private val syncErrorHandler: SyncErrorHandler,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    
    /**
     * Attempt to recover from an error
     */
    suspend fun attemptRecovery(error: SyncErrorWithContext): Boolean {
        return when (error.error) {
            is SyncError.NetworkUnavailable -> checkNetworkAndRetry(error)
            is SyncError.TokenExpired -> refreshTokenAndRetry(error)
            is SyncError.RateLimitExceeded -> waitAndRetry(error)
            else -> false
        }
    }
    
    /**
     * Check network and retry
     */
    private suspend fun checkNetworkAndRetry(error: SyncErrorWithContext): Boolean {
        // Implementation would check network and retry
        return false
    }
    
    /**
     * Refresh token and retry
     */
    private suspend fun refreshTokenAndRetry(error: SyncErrorWithContext): Boolean {
        // Implementation would refresh token and retry
        return false
    }
    
    /**
     * Wait for rate limit and retry
     */
    private suspend fun waitAndRetry(error: SyncErrorWithContext): Boolean {
        val rateLimitError = error.error as? SyncError.RateLimitExceeded ?: return false
        
        rateLimitError.retryAfterSeconds?.let { retryAfter ->
            kotlinx.coroutines.delay(retryAfter * 1000)
        } ?: kotlinx.coroutines.delay(5000)
        
        return true
    }
    
    /**
     * Get recovery options for an error
     */
    fun getRecoveryOptions(error: SyncErrorWithContext): List<RecoveryOption> {
        val suggestions = syncErrorHandler.getRecoverySuggestions(error.error)
        val actions = syncErrorHandler.getActionButtons(error.error)
        
        return suggestions.mapIndexed { index, suggestion ->
            RecoveryOption(
                id = "${error.errorId}_option_$index",
                text = suggestion,
                action = actions.getOrNull(index)?.type ?: com.ourcookbook.domain.usecase.sync.errors.ErrorActionType.CHECK_CONNECTION
            )
        }
    }
}

/**
 * Recovery option
 */
data class RecoveryOption(
    val id: String,
    val text: String,
    val action: com.ourcookbook.domain.usecase.sync.errors.ErrorActionType
)

/**
 * Sync error reporting service
 */
class SyncErrorReportingService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    /**
     * Report an error to analytics
     */
    fun reportError(error: SyncErrorWithContext) {
        // Implementation would send error to analytics service
        // For now, just log
        android.util.Log.e(
            "SyncErrorReport",
            "Error reported: ${error.error::class.simpleName} - ${error.errorId}"
        )
    }
    
    /**
     * Report multiple errors
     */
    fun reportErrors(errors: List<SyncErrorWithContext>) {
        errors.forEach { reportError(it) }
    }
    
    /**
     * Get error statistics
     */
    fun getErrorStatistics(): ErrorStatistics {
        // Implementation would query database for statistics
        return ErrorStatistics(
            totalErrors = 0,
            resolvedErrors = 0,
            unresolvedErrors = 0,
            mostCommonError = null,
            errorByCategory = emptyMap()
        )
    }
}

/**
 * Error statistics
 */
data class ErrorStatistics(
    val totalErrors: Int,
    val resolvedErrors: Int,
    val unresolvedErrors: Int,
    val mostCommonError: String?,
    val errorByCategory: Map<String, Int>
) {
    val resolutionRate: Float get() = if (totalErrors > 0) resolvedErrors.toFloat() / totalErrors.toFloat() else 0f
}

/**
 * Sync error work manager
 */
class SyncErrorWorkManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val workManager: WorkManager
) {
    
    /**
     * Schedule a retry for an error
     */
    fun scheduleRetry(
        errorId: String,
        delayMs: Long,
        retryCount: Int
    ) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val workRequest: WorkRequest = OneTimeWorkRequestBuilder<SyncErrorRetryWorker>()
            .setConstraints(constraints)
            .setInitialDelay(delayMs, java.util.concurrent.TimeUnit.MILLISECONDS)
            .addTag("sync_error_retry")
            .addTag("sync_error_$errorId")
            .build()
        
        workManager.enqueueUniqueWork(
            "sync_error_retry_$errorId",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }
    
    /**
     * Cancel retry for an error
     */
    fun cancelRetry(errorId: String) {
        workManager.cancelAllWorkByTag("sync_error_$errorId")
    }
    
    /**
     * Cancel all retries
     */
    fun cancelAllRetries() {
        workManager.cancelAllWorkByTag("sync_error_retry")
    }
    
    /**
     * Check if a retry is scheduled
     */
    fun isRetryScheduled(errorId: String): Boolean {
        return workManager.getWorkInfosByTag("sync_error_$errorId").get()
            .any { it.state != androidx.work.WorkInfo.State.CANCELLED }
    }
}

/**
 * Worker for retrying sync errors
 */
// This would be implemented as a Worker class
// For now, we'll just define the interface
interface SyncErrorRetryWorker {
    // Implementation would be in a separate file
}

/**
 * Extension functions for easy error handling
 */

fun Context.getSyncErrorHandler(): SyncErrorHandler {
    return SyncErrorHandler(this)
}

fun Context.getSyncErrorNotificationService(): SyncErrorNotificationService {
    return SyncErrorNotificationService(this)
}

fun Context.getSyncErrorRecoveryService(): SyncErrorRecoveryService {
    return SyncErrorRecoveryService()
}

fun Context.getSyncErrorReportingService(): SyncErrorReportingService {
    return SyncErrorReportingService(this)
}
