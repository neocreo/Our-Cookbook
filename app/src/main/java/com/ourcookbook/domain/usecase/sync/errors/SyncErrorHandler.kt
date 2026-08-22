package com.ourcookbook.domain.usecase.sync.errors

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.ourcookbook.domain.model.Cookbook
import com.ourcookbook.domain.repository.SyncLogRepository
import com.ourcookbook.domain.repository.SyncMetadataRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min
import kotlin.math.pow

/**
 * Sync Error Handler
 * 
 * Provides comprehensive error handling for sync operations with:
 * - Error classification and categorization
 * - Automatic retry with exponential backoff
 * - User-friendly error messages
 * - Error logging and reporting
 * - Recovery suggestions
 */

/**
 * Sync error types
 */
sealed class SyncError {
    // Network-related errors
    data class NetworkUnavailable(val message: String? = null) : SyncError()
    data class NetworkTimeout(val message: String? = null) : SyncError()
    data class NetworkSlow(val speedKbps: Float) : SyncError()
    
    // Authentication errors
    data class AuthenticationRequired(val account: String? = null) : SyncError()
    data class TokenExpired(val tokenType: String = "access_token") : SyncError()
    data class TokenInvalid(val reason: String? = null) : SyncError()
    data class PermissionDenied(val permission: String, val required: Boolean = true) : SyncError()
    
    // Drive API errors
    data class DriveApiError(
        val code: Int,
        val message: String,
        val isRetryable: Boolean = false
    ) : SyncError()
    
    data class RateLimitExceeded(
        val retryAfterSeconds: Long? = null,
        val limitType: RateLimitType = RateLimitType.UNKNOWN
    ) : SyncError()
    
    data class QuotaExceeded(
        val quotaType: QuotaType,
        val currentUsage: Long,
        val quotaLimit: Long
    ) : SyncError()
    
    // File errors
    data class FileNotFound(val fileId: String, val fileName: String? = null) : SyncError()
    data class FileConflict(val fileId: String, val localChecksum: String, val remoteChecksum: String) : SyncError()
    data class FileCorrupted(val fileId: String, val reason: String? = null) : SyncError()
    data class FileTooLarge(val fileId: String, val fileSize: Long, val maxSize: Long) : SyncError()
    
    // Data errors
    data class DataCorrupted(val entityType: String, val entityId: String, val reason: String) : SyncError()
    data class SchemaMismatch(val expectedVersion: Int, val actualVersion: Int) : SyncError()
    data class IncompatibleData(val reason: String) : SyncError()
    
    // Device errors
    data class DeviceNotRegistered(val deviceId: String) : SyncError()
    data class DeviceInactive(val deviceId: String, val lastActive: Instant? = null) : SyncError()
    
    // Cookbook errors
    data class CookbookNotFound(val cookbookId: String) : SyncError()
    data class CookbookNotSynced(val cookbookId: String) : SyncError()
    data class CookbookDeleted(val cookbookId: String, val deletedAt: Instant? = null) : SyncError()
    
    // General errors
    data class UnknownError(val message: String? = null, val cause: Throwable? = null) : SyncError()
    data class ServerError(val code: Int, val message: String) : SyncError()
    data class ServiceUnavailable(val retryAfter: Instant? = null) : SyncError()
    data class MaintenanceMode(val message: String? = null, val estimatedDuration: Long? = null) : SyncError()
}

/**
 * Rate limit types
 */
enum class RateLimitType {
    USER,
    PROJECT,
    PER_MINUTE,
    PER_HOUR,
    PER_DAY,
    UNKNOWN
}

/**
 * Quota types
 */
enum class QuotaType {
    STORAGE,
    READ_REQUESTS,
    WRITE_REQUESTS,
    TOTAL_REQUESTS,
    BANDWIDTH
}

/**
 * Error severity levels
 */
enum class ErrorSeverity {
    INFO,
    WARNING,
    ERROR,
    CRITICAL
}

/**
 * Error category
 */
enum class ErrorCategory {
    NETWORK,
    AUTHENTICATION,
    PERMISSION,
    API_LIMIT,
    FILE,
    DATA,
    DEVICE,
    COOKBOOK,
    UNKNOWN
}

/**
 * Sync error with metadata
 */
data class SyncErrorWithContext(
    val error: SyncError,
    val cookbookId: String? = null,
    val recipeId: String? = null,
    val deviceId: String? = null,
    val timestamp: Instant = Instant.now(),
    val attempt: Int = 0,
    val severity: ErrorSeverity = ErrorSeverity.ERROR,
    val category: ErrorCategory = getCategory(error),
    val isRetryable: Boolean = isRetryable(error),
    val retryCount: Int = 0,
    val maxRetries: Int = getMaxRetries(error),
    val nextRetryDelay: Long? = getNextRetryDelay(error, retryCount)
) {
    val errorId: String = "${timestamp.toEpochMilli()}_${cookbookId ?: "global"}_${attempt}"
    
    fun toSyncLogEntry(): SyncLogEntry {
        return SyncLogEntry(
            id = errorId,
            timestamp = timestamp,
            cookbookId = cookbookId,
            recipeId = recipeId,
            deviceId = deviceId,
            action = "ERROR",
            status = "FAILED",
            details = getErrorMessage(error),
            errorType = error::class.simpleName ?: "Unknown",
            severity = severity,
            category = category,
            retryCount = retryCount,
            resolved = false
        )
    }
}

/**
 * Sync log entry
 */
data class SyncLogEntry(
    val id: String,
    val timestamp: Instant,
    val cookbookId: String?,
    val recipeId: String?,
    val deviceId: String?,
    val action: String,
    val status: String,
    val details: String,
    val errorType: String? = null,
    val severity: ErrorSeverity = ErrorSeverity.INFO,
    val category: ErrorCategory = ErrorCategory.UNKNOWN,
    val retryCount: Int = 0,
    val resolved: Boolean = false
)

/**
 * Error handler for sync operations
 */
@Singleton
class SyncErrorHandler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val syncLogRepository: SyncLogRepository,
    private val syncMetadataRepository: SyncMetadataRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    
    private val activeErrors = mutableMapOf<String, SyncErrorWithContext>()
    private val retryQueue = mutableListOf<SyncErrorWithContext>()
    private var isProcessingQueue = false
    
    /**
     * Handle a sync error
     */
    suspend fun handleError(
        error: SyncError,
        cookbookId: String? = null,
        recipeId: String? = null,
        deviceId: String? = null,
        attempt: Int = 0
    ): SyncErrorWithContext {
        val errorWithContext = SyncErrorWithContext(
            error = error,
            cookbookId = cookbookId,
            recipeId = recipeId,
            deviceId = deviceId,
            attempt = attempt,
            severity = getSeverity(error),
            category = getCategory(error),
            isRetryable = isRetryable(error),
            maxRetries = getMaxRetries(error)
        )
        
        // Log the error
        logError(errorWithContext)
        
        // Store active error
        activeErrors[errorWithContext.errorId] = errorWithContext
        
        // Queue for retry if retryable
        if (errorWithContext.isRetryable) {
            queueForRetry(errorWithContext)
        }
        
        return errorWithContext
    }
    
    /**
     * Log an error to the database
     */
    private suspend fun logError(error: SyncErrorWithContext) {
        withContext(dispatcher) {
            syncLogRepository.insert(error.toSyncLogEntry())
        }
    }
    
    /**
     * Queue an error for retry
     */
    private suspend fun queueForRetry(error: SyncErrorWithContext) {
        retryQueue.add(error)
        processRetryQueue()
    }
    
    /**
     * Process the retry queue
     */
    private suspend fun processRetryQueue() {
        if (isProcessingQueue) return
        
        isProcessingQueue = true
        
        while (retryQueue.isNotEmpty()) {
            val error = retryQueue.firstOrNull() ?: break
            
            if (error.retryCount >= error.maxRetries) {
                // Max retries reached, remove from queue
                retryQueue.remove(error)
                markAsUnresolved(error)
                continue
            }
            
            val delay = error.nextRetryDelay ?: continue
            
            // Wait for the delay
            delay(delay)
            
            // Increment retry count
            val updatedError = error.copy(
                retryCount = error.retryCount + 1,
                nextRetryDelay = getNextRetryDelay(error.error, error.retryCount + 1)
            )
            
            retryQueue[0] = updatedError
            activeErrors[updatedError.errorId] = updatedError
            
            // Try to retry the operation
            val success = retryOperation(updatedError)
            
            if (success) {
                // Operation succeeded, remove from queue
                retryQueue.removeAt(0)
                markAsResolved(updatedError)
            } else {
                // Operation failed again, will be retried
                // Move to end of queue for fairness
                retryQueue.removeAt(0)
                retryQueue.add(updatedError)
            }
        }
        
        isProcessingQueue = false
    }
    
    /**
     * Retry the operation associated with an error
     */
    private suspend fun retryOperation(error: SyncErrorWithContext): Boolean {
        // This would be implemented to actually retry the sync operation
        // For now, we'll just check if network is available
        
        if (!isNetworkAvailable()) {
            return false
        }
        
        // Check for specific error types that can be resolved
        return when (error.error) {
            is SyncError.NetworkUnavailable -> isNetworkAvailable()
            is SyncError.TokenExpired -> {
                // Would trigger re-authentication
                false
            }
            is SyncError.RateLimitExceeded -> {
                // Check if rate limit has passed
                (error.error as SyncError.RateLimitExceeded).retryAfterSeconds?.let { retryAfter ->
                    val now = System.currentTimeMillis()
                    val retryTime = error.timestamp.toEpochMilli() + (retryAfter * 1000)
                    now >= retryTime
                } ?: true
            }
            else -> true // Assume can retry
        }
    }
    
    /**
     * Mark an error as resolved
     */
    private suspend fun markAsResolved(error: SyncErrorWithContext) {
        withContext(dispatcher) {
            val resolvedEntry = error.toSyncLogEntry().copy(
                status = "RESOLVED",
                resolved = true
            )
            syncLogRepository.update(resolvedEntry)
        }
        
        activeErrors.remove(error.errorId)
    }
    
    /**
     * Mark an error as unresolved (max retries reached)
     */
    private suspend fun markAsUnresolved(error: SyncErrorWithContext) {
        withContext(dispatcher) {
            val unresolvedEntry = error.toSyncLogEntry().copy(
                status = "UNRESOLVED",
                details = "${error.toSyncLogEntry().details} - Max retries (${error.maxRetries}) reached"
            )
            syncLogRepository.update(unresolvedEntry)
        }
        
        activeErrors.remove(error.errorId)
    }
    
    /**
     * Get all active errors
     */
    fun getActiveErrors(): List<SyncErrorWithContext> {
        return activeErrors.values.toList()
    }
    
    /**
     * Get errors for a specific cookbook
     */
    fun getErrorsForCookbook(cookbookId: String): List<SyncErrorWithContext> {
        return activeErrors.values.filter { it.cookbookId == cookbookId }
    }
    
    /**
     * Get retryable errors
     */
    fun getRetryableErrors(): List<SyncErrorWithContext> {
        return activeErrors.values.filter { it.isRetryable }
    }
    
    /**
     * Get unresolved errors
     */
    fun getUnresolvedErrors(): List<SyncErrorWithContext> {
        return activeErrors.values.filter { it.retryCount >= it.maxRetries }
    }
    
    /**
     * Clear an error
     */
    suspend fun clearError(errorId: String) {
        activeErrors.remove(errorId)
        retryQueue.removeAll { it.errorId == errorId }
        
        withContext(dispatcher) {
            syncLogRepository.delete(errorId)
        }
    }
    
    /**
     * Clear all errors
     */
    suspend fun clearAllErrors() {
        activeErrors.clear()
        retryQueue.clear()
        
        withContext(dispatcher) {
            syncLogRepository.deleteAll()
        }
    }
    
    /**
     * Force retry an error
     */
    suspend fun forceRetry(errorId: String) {
        val error = activeErrors[errorId] ?: return
        
        val updatedError = error.copy(
            retryCount = 0,
            nextRetryDelay = getNextRetryDelay(error.error, 0)
        )
        
        activeErrors[errorId] = updatedError
        retryQueue.add(0, updatedError)
        processRetryQueue()
    }
    
    /**
     * Get user-friendly error message
     */
    fun getUserFriendlyMessage(error: SyncError): String {
        return when (error) {
            is SyncError.NetworkUnavailable -> "No network connection. Please check your internet and try again."
            is SyncError.NetworkTimeout -> "Connection timed out. Please try again."
            is SyncError.NetworkSlow -> "Network is slow. Sync may take longer than usual."
            is SyncError.AuthenticationRequired -> "Please sign in to continue syncing."
            is SyncError.TokenExpired -> "Session expired. Please sign in again."
            is SyncError.TokenInvalid -> "Invalid credentials. Please sign in again."
            is SyncError.PermissionDenied -> "Permission denied: ${error.permission}. Please grant the required permissions."
            is SyncError.DriveApiError -> getDriveErrorMessage(error)
            is SyncError.RateLimitExceeded -> "Too many requests. Please wait and try again."
            is SyncError.QuotaExceeded -> getQuotaErrorMessage(error)
            is SyncError.FileNotFound -> "File not found: ${error.fileName ?: error.fileId}"
            is SyncError.FileConflict -> "File conflict detected. Please resolve conflicts to continue."
            is SyncError.FileCorrupted -> "File is corrupted: ${error.fileId}. Please try again."
            is SyncError.FileTooLarge -> "File is too large (${error.fileSize / (1024 * 1024)}MB). Maximum size is ${error.maxSize / (1024 * 1024)}MB."
            is SyncError.DataCorrupted -> "Data is corrupted. Please try again."
            is SyncError.SchemaMismatch -> "Data format is incompatible. Please update the app."
            is SyncError.IncompatibleData -> "Data is incompatible: ${error.reason}"
            is SyncError.DeviceNotRegistered -> "Device not registered. Please set up sync for this device."
            is SyncError.DeviceInactive -> "Device is inactive. Please reactivate the device."
            is SyncError.CookbookNotFound -> "Cookbook not found. It may have been deleted."
            is SyncError.CookbookNotSynced -> "Cookbook is not configured for sync."
            is SyncError.CookbookDeleted -> "Cookbook has been deleted."
            is SyncError.UnknownError -> error.message ?: "An unknown error occurred. Please try again."
            is SyncError.ServerError -> "Server error (${error.code}): ${error.message}"
            is SyncError.ServiceUnavailable -> "Service is temporarily unavailable. Please try again later."
            is SyncError.MaintenanceMode -> "Service is under maintenance. Please try again later."
        }
    }
    
    /**
     * Get recovery suggestions for an error
     */
    fun getRecoverySuggestions(error: SyncError): List<String> {
        return when (error) {
            is SyncError.NetworkUnavailable -> listOf(
                "Check your internet connection",
                "Enable Wi-Fi or mobile data",
                "Try again later"
            )
            is SyncError.NetworkTimeout -> listOf(
                "Check your internet connection",
                "Try again",
                "Restart your router"
            )
            is SyncError.NetworkSlow -> listOf(
                "Wait for a better connection",
                "Try again later",
                "Use a different network"
            )
            is SyncError.AuthenticationRequired -> listOf(
                "Sign in to your Google account",
                "Check your credentials",
                "Grant required permissions"
            )
            is SyncError.TokenExpired -> listOf(
                "Sign in again",
                "Refresh your session"
            )
            is SyncError.PermissionDenied -> listOf(
                "Grant the required permission",
                "Check app settings",
                "Re-authenticate"
            )
            is SyncError.DriveApiError -> listOf(
                "Check Google Drive permissions",
                "Try again later",
                "Contact support if problem persists"
            )
            is SyncError.RateLimitExceeded -> listOf(
                "Wait for ${error.retryAfterSeconds ?: 60} seconds",
                "Try again later",
                "Reduce sync frequency"
            )
            is SyncError.QuotaExceeded -> listOf(
                "Free up some Google Drive space",
                "Upgrade your Google Drive storage",
                "Delete old backups"
            )
            is SyncError.FileNotFound -> listOf(
                "Check if the file was deleted",
                "Restore from a different backup",
                "Contact support if problem persists"
            )
            is SyncError.FileConflict -> listOf(
                "Resolve the conflict in Conflict Resolution screen",
                "Choose which version to keep",
                "Merge the changes manually"
            )
            is SyncError.FileCorrupted -> listOf(
                "Try again",
                "Restore from a different backup",
                "Contact support if problem persists"
            )
            is SyncError.FileTooLarge -> listOf(
                "Reduce the file size",
                "Split the file into smaller parts",
                "Use a different export format"
            )
            is SyncError.SchemaMismatch -> listOf(
                "Update the app to the latest version",
                "Contact support for assistance"
            )
            is SyncError.DeviceNotRegistered -> listOf(
                "Set up sync for this device",
                "Check device registration"
            )
            is SyncError.CookbookNotFound -> listOf(
                "Check if the cookbook was deleted",
                "Create a new cookbook"
            )
            else -> listOf("Try again", "Check your connection", "Contact support if problem persists")
        }
    }
    
    /**
     * Get action buttons for an error
     */
    fun getActionButtons(error: SyncError): List<ErrorAction> {
        return when (error) {
            is SyncError.NetworkUnavailable -> listOf(
                ErrorAction("Retry", ErrorActionType.RETRY),
                ErrorAction("Check Connection", ErrorActionType.CHECK_CONNECTION),
                ErrorAction("Cancel", ErrorActionType.CANCEL)
            )
            is SyncError.AuthenticationRequired -> listOf(
                ErrorAction("Sign In", ErrorActionType.SIGN_IN),
                ErrorAction("Cancel", ErrorActionType.CANCEL)
            )
            is SyncError.TokenExpired -> listOf(
                ErrorAction("Refresh", ErrorActionType.REFRESH_TOKEN),
                ErrorAction("Sign In Again", ErrorActionType.SIGN_IN),
                ErrorAction("Cancel", ErrorActionType.CANCEL)
            )
            is SyncError.PermissionDenied -> listOf(
                ErrorAction("Grant Permission", ErrorActionType.GRANT_PERMISSION),
                ErrorAction("Cancel", ErrorActionType.CANCEL)
            )
            is SyncError.RateLimitExceeded -> listOf(
                ErrorAction("Retry", ErrorActionType.RETRY),
                ErrorAction("Cancel", ErrorActionType.CANCEL)
            )
            is SyncError.FileConflict -> listOf(
                ErrorAction("Resolve", ErrorActionType.RESOLVE_CONFLICT),
                ErrorAction("Skip", ErrorActionType.SKIP),
                ErrorAction("Cancel", ErrorActionType.CANCEL)
            )
            else -> listOf(
                ErrorAction("Retry", ErrorActionType.RETRY),
                ErrorAction("Cancel", ErrorActionType.CANCEL)
            )
        }
    }
    
    /**
     * Check if network is available
     */
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE)
            as ConnectivityManager
        
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network)
            ?: return false
        
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
    
    /**
     * Get severity for an error
     */
    private fun getSeverity(error: SyncError): ErrorSeverity {
        return when (error) {
            is SyncError.NetworkUnavailable -> ErrorSeverity.WARNING
            is SyncError.NetworkTimeout -> ErrorSeverity.WARNING
            is SyncError.NetworkSlow -> ErrorSeverity.INFO
            is SyncError.AuthenticationRequired -> ErrorSeverity.ERROR
            is SyncError.TokenExpired -> ErrorSeverity.ERROR
            is SyncError.TokenInvalid -> ErrorSeverity.ERROR
            is SyncError.PermissionDenied -> ErrorSeverity.ERROR
            is SyncError.DriveApiError -> {
                when (error.code) {
                    401, 403 -> ErrorSeverity.ERROR
                    404 -> ErrorSeverity.WARNING
                    429 -> ErrorSeverity.WARNING
                    in 500..599 -> ErrorSeverity.CRITICAL
                    else -> ErrorSeverity.ERROR
                }
            }
            is SyncError.RateLimitExceeded -> ErrorSeverity.WARNING
            is SyncError.QuotaExceeded -> ErrorSeverity.ERROR
            is SyncError.FileNotFound -> ErrorSeverity.WARNING
            is SyncError.FileConflict -> ErrorSeverity.ERROR
            is SyncError.FileCorrupted -> ErrorSeverity.ERROR
            is SyncError.FileTooLarge -> ErrorSeverity.WARNING
            is SyncError.DataCorrupted -> ErrorSeverity.ERROR
            is SyncError.SchemaMismatch -> ErrorSeverity.CRITICAL
            is SyncError.IncompatibleData -> ErrorSeverity.ERROR
            is SyncError.DeviceNotRegistered -> ErrorSeverity.ERROR
            is SyncError.DeviceInactive -> ErrorSeverity.WARNING
            is SyncError.CookbookNotFound -> ErrorSeverity.WARNING
            is SyncError.CookbookNotSynced -> ErrorSeverity.INFO
            is SyncError.CookbookDeleted -> ErrorSeverity.WARNING
            is SyncError.UnknownError -> ErrorSeverity.ERROR
            is SyncError.ServerError -> {
                when (error.code) {
                    in 500..599 -> ErrorSeverity.CRITICAL
                    in 400..499 -> ErrorSeverity.ERROR
                    else -> ErrorSeverity.ERROR
                }
            }
            is SyncError.ServiceUnavailable -> ErrorSeverity.CRITICAL
            is SyncError.MaintenanceMode -> ErrorSeverity.INFO
        }
    }
    
    /**
     * Get category for an error
     */
    private fun getCategory(error: SyncError): ErrorCategory {
        return when (error) {
            is SyncError.NetworkUnavailable -> ErrorCategory.NETWORK
            is SyncError.NetworkTimeout -> ErrorCategory.NETWORK
            is SyncError.NetworkSlow -> ErrorCategory.NETWORK
            is SyncError.AuthenticationRequired -> ErrorCategory.AUTHENTICATION
            is SyncError.TokenExpired -> ErrorCategory.AUTHENTICATION
            is SyncError.TokenInvalid -> ErrorCategory.AUTHENTICATION
            is SyncError.PermissionDenied -> ErrorCategory.PERMISSION
            is SyncError.DriveApiError -> ErrorCategory.API_LIMIT
            is SyncError.RateLimitExceeded -> ErrorCategory.API_LIMIT
            is SyncError.QuotaExceeded -> ErrorCategory.API_LIMIT
            is SyncError.FileNotFound -> ErrorCategory.FILE
            is SyncError.FileConflict -> ErrorCategory.FILE
            is SyncError.FileCorrupted -> ErrorCategory.FILE
            is SyncError.FileTooLarge -> ErrorCategory.FILE
            is SyncError.DataCorrupted -> ErrorCategory.DATA
            is SyncError.SchemaMismatch -> ErrorCategory.DATA
            is SyncError.IncompatibleData -> ErrorCategory.DATA
            is SyncError.DeviceNotRegistered -> ErrorCategory.DEVICE
            is SyncError.DeviceInactive -> ErrorCategory.DEVICE
            is SyncError.CookbookNotFound -> ErrorCategory.COOKBOOK
            is SyncError.CookbookNotSynced -> ErrorCategory.COOKBOOK
            is SyncError.CookbookDeleted -> ErrorCategory.COOKBOOK
            else -> ErrorCategory.UNKNOWN
        }
    }
    
    /**
     * Check if an error is retryable
     */
    private fun isRetryable(error: SyncError): Boolean {
        return when (error) {
            is SyncError.NetworkUnavailable -> true
            is SyncError.NetworkTimeout -> true
            is SyncError.NetworkSlow -> true
            is SyncError.AuthenticationRequired -> false
            is SyncError.TokenExpired -> true
            is SyncError.TokenInvalid -> false
            is SyncError.PermissionDenied -> false
            is SyncError.DriveApiError -> error.isRetryable
            is SyncError.RateLimitExceeded -> true
            is SyncError.QuotaExceeded -> false
            is SyncError.FileNotFound -> false
            is SyncError.FileConflict -> false
            is SyncError.FileCorrupted -> true
            is SyncError.FileTooLarge -> false
            is SyncError.DataCorrupted -> true
            is SyncError.SchemaMismatch -> false
            is SyncError.IncompatibleData -> false
            is SyncError.DeviceNotRegistered -> false
            is SyncError.DeviceInactive -> false
            is SyncError.CookbookNotFound -> false
            is SyncError.CookbookNotSynced -> false
            is SyncError.CookbookDeleted -> false
            is SyncError.UnknownError -> true
            is SyncError.ServerError -> error.code in 500..599
            is SyncError.ServiceUnavailable -> true
            is SyncError.MaintenanceMode -> false
        }
    }
    
    /**
     * Get max retries for an error
     */
    private fun getMaxRetries(error: SyncError): Int {
        return when (error) {
            is SyncError.NetworkUnavailable -> 3
            is SyncError.NetworkTimeout -> 5
            is SyncError.NetworkSlow -> 2
            is SyncError.TokenExpired -> 1
            is SyncError.DriveApiError -> {
                when (error.code) {
                    429 -> 5 // Rate limit
                    in 500..599 -> 3 // Server errors
                    else -> 2
                }
            }
            is SyncError.RateLimitExceeded -> 5
            is SyncError.FileCorrupted -> 3
            is SyncError.DataCorrupted -> 3
            is SyncError.ServerError -> if (error.code in 500..599) 3 else 1
            is SyncError.ServiceUnavailable -> 5
            else -> 2
        }
    }
    
    /**
     * Get next retry delay using exponential backoff
     */
    private fun getNextRetryDelay(error: SyncError, retryCount: Int): Long {
        val baseDelay = when (error) {
            is SyncError.NetworkUnavailable -> 1000L
            is SyncError.NetworkTimeout -> 2000L
            is SyncError.NetworkSlow -> 5000L
            is SyncError.TokenExpired -> 5000L
            is SyncError.DriveApiError -> {
                when (error.code) {
                    429 -> (error as SyncError.RateLimitExceeded).retryAfterSeconds?.times(1000) ?: 5000L
                    else -> 2000L
                }
            }
            is SyncError.RateLimitExceeded -> error.retryAfterSeconds?.times(1000) ?: 5000L
            is SyncError.FileCorrupted -> 3000L
            is SyncError.DataCorrupted -> 3000L
            is SyncError.ServerError -> if (error.code in 500..599) 3000L else 2000L
            is SyncError.ServiceUnavailable -> 5000L
            else -> 2000L
        }
        
        // Exponential backoff: baseDelay * 2^retryCount
        // Cap at 30 seconds
        return min(baseDelay * 2.toDouble().pow(retryCount).toLong(), 30000L)
    }
    
    /**
     * Get Drive API error message
     */
    private fun getDriveErrorMessage(error: SyncError.DriveApiError): String {
        return when (error.code) {
            400 -> "Bad request. Please check your request and try again."
            401 -> "Unauthorized. Please sign in again."
            403 -> "Access denied. Please check your permissions."
            404 -> "File not found. It may have been deleted."
            409 -> "Conflict detected. Please resolve conflicts."
            429 -> "Too many requests. Please wait and try again."
            500 -> "Server error. Please try again later."
            503 -> "Service unavailable. Please try again later."
            else -> "Drive API error (${error.code}): ${error.message}"
        }
    }
    
    /**
     * Get quota error message
     */
    private fun getQuotaErrorMessage(error: SyncError.QuotaExceeded): String {
        val usedMB = error.currentUsage / (1024 * 1024)
        val limitMB = error.quotaLimit / (1024 * 1024)
        
        return when (error.quotaType) {
            QuotaType.STORAGE -> "Storage quota exceeded. Used $usedMB MB of $limitMB MB."
            QuotaType.READ_REQUESTS -> "Read requests quota exceeded."
            QuotaType.WRITE_REQUESTS -> "Write requests quota exceeded."
            QuotaType.TOTAL_REQUESTS -> "Total requests quota exceeded."
            QuotaType.BANDWIDTH -> "Bandwidth quota exceeded."
        }
    }
    
    /**
     * Get error message for logging
     */
    private fun getErrorMessage(error: SyncError): String {
        return when (error) {
            is SyncError.NetworkUnavailable -> "Network unavailable: ${error.message}"
            is SyncError.NetworkTimeout -> "Network timeout: ${error.message}"
            is SyncError.NetworkSlow -> "Network slow: ${error.speedKbps} Kbps"
            is SyncError.AuthenticationRequired -> "Authentication required for account: ${error.account}"
            is SyncError.TokenExpired -> "Token expired: ${error.tokenType}"
            is SyncError.TokenInvalid -> "Token invalid: ${error.reason}"
            is SyncError.PermissionDenied -> "Permission denied: ${error.permission}"
            is SyncError.DriveApiError -> "Drive API error (${error.code}): ${error.message}"
            is SyncError.RateLimitExceeded -> "Rate limit exceeded: ${error.limitType}"
            is SyncError.QuotaExceeded -> "Quota exceeded (${error.quotaType}): ${error.currentUsage}/${error.quotaLimit}"
            is SyncError.FileNotFound -> "File not found: ${error.fileId}"
            is SyncError.FileConflict -> "File conflict: ${error.fileId} (local: ${error.localChecksum}, remote: ${error.remoteChecksum})"
            is SyncError.FileCorrupted -> "File corrupted: ${error.fileId}"
            is SyncError.FileTooLarge -> "File too large: ${error.fileId} (${error.fileSize}/${error.maxSize})"
            is SyncError.DataCorrupted -> "Data corrupted: ${error.entityType}/${error.entityId}"
            is SyncError.SchemaMismatch -> "Schema mismatch: expected ${error.expectedVersion}, actual ${error.actualVersion}"
            is SyncError.IncompatibleData -> "Incompatible data: ${error.reason}"
            is SyncError.DeviceNotRegistered -> "Device not registered: ${error.deviceId}"
            is SyncError.DeviceInactive -> "Device inactive: ${error.deviceId}"
            is SyncError.CookbookNotFound -> "Cookbook not found: ${error.cookbookId}"
            is SyncError.CookbookNotSynced -> "Cookbook not synced: ${error.cookbookId}"
            is SyncError.CookbookDeleted -> "Cookbook deleted: ${error.cookbookId}"
            is SyncError.UnknownError -> "Unknown error: ${error.message}"
            is SyncError.ServerError -> "Server error (${error.code}): ${error.message}"
            is SyncError.ServiceUnavailable -> "Service unavailable"
            is SyncError.MaintenanceMode -> "Maintenance mode"
        }
    }
}

/**
 * Error action types
 */
enum class ErrorActionType {
    RETRY,
    SIGN_IN,
    REFRESH_TOKEN,
    GRANT_PERMISSION,
    RESOLVE_CONFLICT,
    SKIP,
    CANCEL,
    CHECK_CONNECTION,
    OPEN_SETTINGS,
    CONTACT_SUPPORT
}

/**
 * Error action
 */
data class ErrorAction(
    val text: String,
    val type: ErrorActionType
)
