package com.ourcookbook.domain.usecase.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.WorkManager
import com.ourcookbook.domain.model.Cookbook
import com.ourcookbook.domain.model.Recipe
import com.ourcookbook.domain.model.Device
import com.ourcookbook.domain.repository.CookbookRepository
import com.ourcookbook.domain.repository.RecipeRepository
import com.ourcookbook.domain.repository.DeviceRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cookbook Notification Manager
 * 
 * Manages all notifications for the Cookbook app:
 * - Sync status notifications
 * - New recipe notifications
 * - Cookbook sharing notifications
 * - Conflict detection notifications
 * - Error notifications
 * - Reminder notifications
 */

// Notification channel IDs
object NotificationChannels {
    const val SYNC = "sync_channel"
    const val RECIPES = "recipes_channel"
    const val SHARING = "sharing_channel"
    const val CONFLICTS = "conflicts_channel"
    const val ERRORS = "errors_channel"
    const val REMINDERS = "reminders_channel"
    const val GENERAL = "general_channel"
}

// Notification IDs
object NotificationIds {
    const val SYNC_IN_PROGRESS = 1001
    const val SYNC_COMPLETE = 1002
    const val SYNC_FAILED = 1003
    const val NEW_RECIPE = 2001
    const val RECIPE_UPDATED = 2002
    const val COOKBOOK_SHARED = 3001
    const val COOKBOOK_INVITE = 3002
    const val CONFLICT_DETECTED = 4001
    const val ERROR_OCCURRED = 5001
    const val REMINDER = 6001
}

/**
 * Notification priority levels
 */
enum class NotificationPriority {
    LOW,
    DEFAULT,
    HIGH,
    MAX
}

/**
 * Notification type
 */
enum class NotificationType {
    SYNC_IN_PROGRESS,
    SYNC_COMPLETE,
    SYNC_FAILED,
    NEW_RECIPE,
    RECIPE_UPDATED,
    COOKBOOK_SHARED,
    COOKBOOK_INVITE,
    CONFLICT_DETECTED,
    ERROR_OCCURRED,
    REMINDER,
    GENERAL
}

/**
 * Notification action
 */
data class NotificationAction(
    val id: Int,
    val title: String,
    val icon: Int? = null,
    val intent: PendingIntent? = null,
    val action: () -> Unit = {}
)

/**
 * Cookbook notification
 */
data class CookbookNotification(
    val id: Int,
    val type: NotificationType,
    val title: String,
    val message: String,
    val timestamp: Instant = Instant.now(),
    val cookbookId: String? = null,
    val recipeId: String? = null,
    val deviceId: String? = null,
    val data: Map<String, String> = emptyMap(),
    val priority: NotificationPriority = NotificationPriority.DEFAULT,
    val channelId: String = NotificationChannels.GENERAL,
    @DrawableRes val icon: Int? = null,
    @StringRes val soundUri: Int? = null,
    val vibrate: Boolean = true,
    val actions: List<NotificationAction> = emptyList(),
    val autoCancel: Boolean = true,
    val ongoing: Boolean = false
)

/**
 * Notification settings
 */
data class NotificationSettings(
    val enabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val showInStatusBar: Boolean = true,
    val showWhenScreenOn: Boolean = true,
    val priority: NotificationPriority = NotificationPriority.DEFAULT,
    val syncNotifications: Boolean = true,
    val recipeNotifications: Boolean = true,
    val sharingNotifications: Boolean = true,
    val conflictNotifications: Boolean = true,
    val errorNotifications: Boolean = true
)

/**
 * Notification statistics
 */
data class NotificationStatistics(
    val totalNotifications: Int = 0,
    val syncNotifications: Int = 0,
    val recipeNotifications: Int = 0,
    val sharingNotifications: Int = 0,
    val conflictNotifications: Int = 0,
    val errorNotifications: Int = 0,
    val lastNotificationTime: Instant? = null
)

/**
 * Notification manager for cookbook app
 */
@Singleton
class CookbookNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val cookbookRepository: CookbookRepository,
    private val recipeRepository: RecipeRepository,
    private val deviceRepository: DeviceRepository,
    private val workManager: WorkManager,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    
    private val notificationManager by lazy {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
    
    private val _notificationSettings = MutableStateFlow(NotificationSettings())
    val notificationSettings: StateFlow<NotificationSettings> = _notificationSettings.asStateFlow()
    
    private val _activeNotifications = MutableStateFlow<List<CookbookNotification>>(emptyList())
    val activeNotifications: StateFlow<List<CookbookNotification>> = _activeNotifications.asStateFlow()
    
    private val _notificationStatistics = MutableStateFlow(NotificationStatistics())
    val notificationStatistics: StateFlow<NotificationStatistics> = _notificationStatistics.asStateFlow()
    
    init {
        createNotificationChannels()
    }
    
    /**
     * Create notification channels (required for Android 8.0+)
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    NotificationChannels.SYNC,
                    "Sync",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Sync status notifications"
                    enableLights(false)
                    enableVibration(false)
                },
                NotificationChannel(
                    NotificationChannels.RECIPES,
                    "Recipes",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "New and updated recipe notifications"
                    enableLights(true)
                    enableVibration(true)
                },
                NotificationChannel(
                    NotificationChannels.SHARING,
                    "Sharing",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Cookbook sharing notifications"
                    enableLights(true)
                    enableVibration(true)
                },
                NotificationChannel(
                    NotificationChannels.CONFLICTS,
                    "Conflicts",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Sync conflict notifications"
                    enableLights(true)
                    enableVibration(true)
                },
                NotificationChannel(
                    NotificationChannels.ERRORS,
                    "Errors",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Sync error notifications"
                    enableLights(true)
                    enableVibration(true)
                },
                NotificationChannel(
                    NotificationChannels.REMINDERS,
                    "Reminders",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Reminder notifications"
                    enableLights(true)
                    enableVibration(true)
                },
                NotificationChannel(
                    NotificationChannels.GENERAL,
                    "General",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "General notifications"
                    enableLights(true)
                    enableVibration(true)
                }
            )
            
            channels.forEach { channel ->
                notificationManager.createNotificationChannel(channel)
            }
        }
    }
    
    /**
     * Show a notification
     */
    fun showNotification(notification: CookbookNotification) {
        if (!_notificationSettings.value.enabled) return
        
        val settings = _notificationSettings.value
        
        // Check if this type of notification is enabled
        val typeEnabled = when (notification.type) {
            NotificationType.SYNC_IN_PROGRESS -> settings.syncNotifications
            NotificationType.SYNC_COMPLETE -> settings.syncNotifications
            NotificationType.SYNC_FAILED -> settings.syncNotifications
            NotificationType.NEW_RECIPE -> settings.recipeNotifications
            NotificationType.RECIPE_UPDATED -> settings.recipeNotifications
            NotificationType.COOKBOOK_SHARED -> settings.sharingNotifications
            NotificationType.COOKBOOK_INVITE -> settings.sharingNotifications
            NotificationType.CONFLICT_DETECTED -> settings.conflictNotifications
            NotificationType.ERROR_OCCURRED -> settings.errorNotifications
            NotificationType.REMINDER -> true
            NotificationType.GENERAL -> true
        }
        
        if (!typeEnabled) return
        
        // Update statistics
        updateStatistics(notification)
        
        // Build notification
        val builder = NotificationCompat.Builder(context, notification.channelId)
            .setSmallIcon(notification.icon ?: android.R.drawable.ic_dialog_info)
            .setContentTitle(notification.title)
            .setContentText(notification.message)
            .setPriority(getNotificationPriority(notification.priority))
            .setAutoCancel(notification.autoCancel)
            .setOngoing(notification.ongoing)
            .setWhen(notification.timestamp.toEpochMilli())
            .setShowWhen(true)
        
        // Add sound if enabled
        if (settings.soundEnabled && notification.soundUri != null) {
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            builder.setSound(soundUri)
        }
        
        // Add vibration if enabled
        if (settings.vibrationEnabled) {
            builder.setVibrate(longArrayOf(0, 250, 250, 250))
        }
        
        // Add actions
        notification.actions.forEach { action ->
            builder.addAction(
                action.icon ?: 0,
                action.title,
                action.intent
            )
        }
        
        // Add large icon if available
        notification.icon?.let { iconRes ->
            val bitmap = BitmapFactory.decodeResource(context.resources, iconRes)
            builder.setLargeIcon(bitmap)
        }
        
        // Add style for big text
        if (notification.message.length > 100) {
            builder.setStyle(NotificationCompat.BigTextStyle()
                .bigText(notification.message))
        }
        
        // Show notification
        notificationManager.notify(notification.id, builder.build())
        
        // Add to active notifications
        val currentNotifications = _activeNotifications.value.toMutableList()
        currentNotifications.add(notification)
        _activeNotifications.value = currentNotifications
    }
    
    /**
     * Cancel a notification
     */
    fun cancelNotification(notificationId: Int) {
        notificationManager.cancel(notificationId)
        
        val currentNotifications = _activeNotifications.value.toMutableList()
        currentNotifications.removeAll { it.id == notificationId }
        _activeNotifications.value = currentNotifications
    }
    
    /**
     * Cancel all notifications
     */
    fun cancelAllNotifications() {
        notificationManager.cancelAll()
        _activeNotifications.value = emptyList()
    }
    
    /**
     * Cancel notifications of a specific type
     */
    fun cancelNotificationsOfType(type: NotificationType) {
        _activeNotifications.value
            .filter { it.type == type }
            .forEach { cancelNotification(it.id) }
    }
    
    /**
     * Update a notification
     */
    fun updateNotification(notification: CookbookNotification) {
        showNotification(notification)
    }
    
    /**
     * Show sync in progress notification
     */
    suspend fun showSyncInProgress(cookbookId: String, deviceId: String) {
        withContext(dispatcher) {
            val cookbook = cookbookRepository.getById(cookbookId)
            val device = deviceRepository.getById(deviceId)
            
            val notification = CookbookNotification(
                id = NotificationIds.SYNC_IN_PROGRESS,
                type = NotificationType.SYNC_IN_PROGRESS,
                title = "Syncing Cookbook",
                message = "Syncing '${cookbook?.name ?: "Unknown"}' from ${device?.name ?: "Unknown"}",
                cookbookId = cookbookId,
                deviceId = deviceId,
                channelId = NotificationChannels.SYNC,
                priority = NotificationPriority.LOW,
                ongoing = true,
                autoCancel = false
            )
            
            showNotification(notification)
        }
    }
    
    /**
     * Show sync complete notification
     */
    suspend fun showSyncComplete(cookbookId: String, recipeCount: Int) {
        withContext(dispatcher) {
            val cookbook = cookbookRepository.getById(cookbookId)
            
            val notification = CookbookNotification(
                id = NotificationIds.SYNC_COMPLETE,
                type = NotificationType.SYNC_COMPLETE,
                title = "Sync Complete",
                message = "Successfully synced '${cookbook?.name ?: "Unknown"}' ($recipeCount recipes)",
                cookbookId = cookbookId,
                channelId = NotificationChannels.SYNC,
                priority = NotificationPriority.DEFAULT,
                autoCancel = true
            )
            
            showNotification(notification)
            cancelNotification(NotificationIds.SYNC_IN_PROGRESS)
        }
    }
    
    /**
     * Show sync failed notification
     */
    suspend fun showSyncFailed(cookbookId: String, errorMessage: String) {
        withContext(dispatcher) {
            val cookbook = cookbookRepository.getById(cookbookId)
            
            val notification = CookbookNotification(
                id = NotificationIds.SYNC_FAILED,
                type = NotificationType.SYNC_FAILED,
                title = "Sync Failed",
                message = "Failed to sync '${cookbook?.name ?: "Unknown"}': $errorMessage",
                cookbookId = cookbookId,
                channelId = NotificationChannels.ERRORS,
                priority = NotificationPriority.HIGH,
                autoCancel = true,
                actions = listOf(
                    NotificationAction(
                        id = 0,
                        title = "Retry",
                        intent = null,
                        action = { /* Retry sync */ }
                    ),
                    NotificationAction(
                        id = 1,
                        title = "View Details",
                        intent = null,
                        action = { /* Show error details */ }
                    )
                )
            )
            
            showNotification(notification)
            cancelNotification(NotificationIds.SYNC_IN_PROGRESS)
        }
    }
    
    /**
     * Show new recipe notification
     */
    suspend fun showNewRecipeNotification(recipe: Recipe, cookbookId: String) {
        withContext(dispatcher) {
            val cookbook = cookbookRepository.getById(cookbookId)
            
            val notification = CookbookNotification(
                id = NotificationIds.NEW_RECIPE,
                type = NotificationType.NEW_RECIPE,
                title = "New Recipe",
                message = "'${recipe.title}' added to '${cookbook?.name ?: "Unknown"}'",
                cookbookId = cookbookId,
                recipeId = recipe.id,
                channelId = NotificationChannels.RECIPES,
                priority = NotificationPriority.DEFAULT,
                autoCancel = true,
                actions = listOf(
                    NotificationAction(
                        id = 0,
                        title = "View Recipe",
                        intent = null,
                        action = { /* Open recipe */ }
                    )
                )
            )
            
            showNotification(notification)
        }
    }
    
    /**
     * Show recipe updated notification
     */
    suspend fun showRecipeUpdatedNotification(recipe: Recipe, cookbookId: String) {
        withContext(dispatcher) {
            val cookbook = cookbookRepository.getById(cookbookId)
            
            val notification = CookbookNotification(
                id = NotificationIds.RECIPE_UPDATED,
                type = NotificationType.RECIPE_UPDATED,
                title = "Recipe Updated",
                message = "'${recipe.title}' updated in '${cookbook?.name ?: "Unknown"}'",
                cookbookId = cookbookId,
                recipeId = recipe.id,
                channelId = NotificationChannels.RECIPES,
                priority = NotificationPriority.DEFAULT,
                autoCancel = true,
                actions = listOf(
                    NotificationAction(
                        id = 0,
                        title = "View Changes",
                        intent = null,
                        action = { /* Show recipe changes */ }
                    )
                )
            )
            
            showNotification(notification)
        }
    }
    
    /**
     * Show cookbook shared notification
     */
    suspend fun showCookbookSharedNotification(cookbook: Cookbook, sharedWith: List<String>) {
        withContext(dispatcher) {
            val message = if (sharedWith.size == 1) {
                "Cookbook '${cookbook.name}' shared with ${sharedWith.first()}"
            } else {
                "Cookbook '${cookbook.name}' shared with ${sharedWith.size} people"
            }
            
            val notification = CookbookNotification(
                id = NotificationIds.COOKBOOK_SHARED,
                type = NotificationType.COOKBOOK_SHARED,
                title = "Cookbook Shared",
                message = message,
                cookbookId = cookbook.id,
                channelId = NotificationChannels.SHARING,
                priority = NotificationPriority.DEFAULT,
                autoCancel = true,
                actions = listOf(
                    NotificationAction(
                        id = 0,
                        title = "View Cookbook",
                        intent = null,
                        action = { /* Open cookbook */ }
                    )
                )
            )
            
            showNotification(notification)
        }
    }
    
    /**
     * Show cookbook invite notification
     */
    suspend fun showCookbookInviteNotification(cookbook: Cookbook, invitedBy: String) {
        withContext(dispatcher) {
            val notification = CookbookNotification(
                id = NotificationIds.COOKBOOK_INVITE,
                type = NotificationType.COOKBOOK_INVITE,
                title = "Cookbook Invitation",
                message = "$invitedBy invited you to join '${cookbook.name}'",
                cookbookId = cookbook.id,
                channelId = NotificationChannels.SHARING,
                priority = NotificationPriority.DEFAULT,
                autoCancel = true,
                actions = listOf(
                    NotificationAction(
                        id = 0,
                        title = "Accept",
                        intent = null,
                        action = { /* Accept invitation */ }
                    ),
                    NotificationAction(
                        id = 1,
                        title = "Decline",
                        intent = null,
                        action = { /* Decline invitation */ }
                    )
                )
            )
            
            showNotification(notification)
        }
    }
    
    /**
     * Show conflict detected notification
     */
    suspend fun showConflictDetectedNotification(cookbookId: String, conflictCount: Int) {
        withContext(dispatcher) {
            val cookbook = cookbookRepository.getById(cookbookId)
            
            val message = if (conflictCount == 1) {
                "1 conflict detected in '${cookbook?.name ?: "Unknown"}'"
            } else {
                "$conflictCount conflicts detected in '${cookbook?.name ?: "Unknown"}'"
            }
            
            val notification = CookbookNotification(
                id = NotificationIds.CONFLICT_DETECTED,
                type = NotificationType.CONFLICT_DETECTED,
                title = "Sync Conflicts Detected",
                message = message,
                cookbookId = cookbookId,
                channelId = NotificationChannels.CONFLICTS,
                priority = NotificationPriority.HIGH,
                autoCancel = true,
                actions = listOf(
                    NotificationAction(
                        id = 0,
                        title = "Resolve",
                        intent = null,
                        action = { /* Open conflict resolution */ }
                    )
                )
            )
            
            showNotification(notification)
        }
    }
    
    /**
     * Show error notification
     */
    suspend fun showErrorNotification(errorMessage: String, cookbookId: String? = null) {
        withContext(dispatcher) {
            val message = if (cookbookId != null) {
                val cookbook = cookbookRepository.getById(cookbookId)
                "Error in '${cookbook?.name ?: "Unknown"}': $errorMessage"
            } else {
                errorMessage
            }
            
            val notification = CookbookNotification(
                id = NotificationIds.ERROR_OCCURRED,
                type = NotificationType.ERROR_OCCURRED,
                title = "Sync Error",
                message = message,
                cookbookId = cookbookId,
                channelId = NotificationChannels.ERRORS,
                priority = NotificationPriority.HIGH,
                autoCancel = true,
                actions = listOf(
                    NotificationAction(
                        id = 0,
                        title = "Retry",
                        intent = null,
                        action = { /* Retry sync */ }
                    ),
                    NotificationAction(
                        id = 1,
                        title = "Details",
                        intent = null,
                        action = { /* Show error details */ }
                    )
                )
            )
            
            showNotification(notification)
        }
    }
    
    /**
     * Update notification settings
     */
    suspend fun updateNotificationSettings(settings: NotificationSettings) {
        _notificationSettings.value = settings
        
        // Update notification channels based on settings
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = notificationManager.notificationChannels
            channels.forEach { channel ->
                val importance = when {
                    !settings.enabled -> NotificationManager.IMPORTANCE_NONE
                    settings.vibrationEnabled && settings.soundEnabled -> NotificationManager.IMPORTANCE_HIGH
                    settings.vibrationEnabled || settings.soundEnabled -> NotificationManager.IMPORTANCE_DEFAULT
                    else -> NotificationManager.IMPORTANCE_LOW
                }
                
                notificationManager.createNotificationChannel(
                    channel.apply {
                        this.importance = importance
                        enableLights(settings.showInStatusBar)
                        enableVibration(settings.vibrationEnabled)
                    }
                )
            }
        }
    }
    
    /**
     * Get current notification settings
     */
    fun getNotificationSettings(): NotificationSettings {
        return _notificationSettings.value
    }
    
    /**
     * Get notification statistics
     */
    fun getNotificationStatistics(): NotificationStatistics {
        return _notificationStatistics.value
    }
    
    /**
     * Update notification statistics
     */
    private fun updateStatistics(notification: CookbookNotification) {
        val currentStats = _notificationStatistics.value
        
        val newStats = currentStats.copy(
            totalNotifications = currentStats.totalNotifications + 1,
            syncNotifications = currentStats.syncNotifications + 
                (if (notification.type in listOf(
                    NotificationType.SYNC_IN_PROGRESS,
                    NotificationType.SYNC_COMPLETE,
                    NotificationType.SYNC_FAILED
                )) 1 else 0),
            recipeNotifications = currentStats.recipeNotifications + 
                (if (notification.type in listOf(
                    NotificationType.NEW_RECIPE,
                    NotificationType.RECIPE_UPDATED
                )) 1 else 0),
            sharingNotifications = currentStats.sharingNotifications + 
                (if (notification.type in listOf(
                    NotificationType.COOKBOOK_SHARED,
                    NotificationType.COOKBOOK_INVITE
                )) 1 else 0),
            conflictNotifications = currentStats.conflictNotifications + 
                (if (notification.type == NotificationType.CONFLICT_DETECTED) 1 else 0),
            errorNotifications = currentStats.errorNotifications + 
                (if (notification.type == NotificationType.ERROR_OCCURRED) 1 else 0),
            lastNotificationTime = notification.timestamp
        )
        
        _notificationStatistics.value = newStats
    }
    
    /**
     * Get notification priority
     */
    private fun getNotificationPriority(priority: NotificationPriority): Int {
        return when (priority) {
            NotificationPriority.LOW -> NotificationCompat.PRIORITY_LOW
            NotificationPriority.DEFAULT -> NotificationCompat.PRIORITY_DEFAULT
            NotificationPriority.HIGH -> NotificationCompat.PRIORITY_HIGH
            NotificationPriority.MAX -> NotificationCompat.PRIORITY_MAX
        }
    }
    
    /**
     * Check if notifications are enabled
     */
    fun areNotificationsEnabled(): Boolean {
        return _notificationSettings.value.enabled &&
               NotificationManagerCompat.from(context).areNotificationsEnabled()
    }
    
    /**
     * Request notification permission
     */
    fun requestNotificationPermission() {
        // Implementation would use ActivityCompat.requestPermissions
        // For now, just log
        android.util.Log.i("NotificationManager", "Requesting notification permission")
    }
    
    /**
     * Check if a specific notification is active
     */
    fun isNotificationActive(notificationId: Int): Boolean {
        return _activeNotifications.value.any { it.id == notificationId }
    }
    
    /**
     * Get active notifications of a specific type
     */
    fun getActiveNotificationsOfType(type: NotificationType): List<CookbookNotification> {
        return _activeNotifications.value.filter { it.type == type }
    }
}

/**
 * Notification helper functions
 */

fun Context.showSimpleNotification(
    title: String,
    message: String,
    channelId: String = NotificationChannels.GENERAL,
    notificationId: Int = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
) {
    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    val builder = NotificationCompat.Builder(this, channelId)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle(title)
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)
    
    notificationManager.notify(notificationId, builder.build())
}

fun Context.cancelNotification(notificationId: Int) {
    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.cancel(notificationId)
}

fun Context.cancelAllNotifications() {
    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.cancelAll()
}
