package com.ourcookbook.data.service.notifications

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.ourcookbook.domain.repository.CookbookRepository
import com.ourcookbook.domain.repository.RecipeRepository
import com.ourcookbook.domain.repository.DeviceRepository
import com.ourcookbook.domain.usecase.notifications.CookbookNotificationManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Notification Service Module
 * 
 * Provides notification-related services for the app
 */
@Module
@InstallIn(SingletonComponent::class)
object NotificationServiceModule {
    
    @Provides
    @Singleton
    fun provideCookbookNotificationManager(
        @ApplicationContext context: Context,
        cookbookRepository: CookbookRepository,
        recipeRepository: RecipeRepository,
        deviceRepository: DeviceRepository,
        workManager: WorkManager
    ): CookbookNotificationManager {
        return CookbookNotificationManager(
            context,
            cookbookRepository,
            recipeRepository,
            deviceRepository,
            workManager
        )
    }
}

/**
 * Notification scheduler for background notifications
 */
class NotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val workManager: WorkManager
) {
    
    /**
     * Schedule a one-time notification
     */
    fun scheduleOneTimeNotification(
        delayMinutes: Long,
        notificationId: Int,
        channelId: String = "general"
    ) {
        // TODO: Implement with a proper ListenableWorker subclass
    }
    
    /**
     * Schedule a periodic notification
     */
    fun schedulePeriodicNotification(
        intervalHours: Long,
        notificationId: Int,
        channelId: String = "general"
    ) {
        val constraints = Constraints.Builder()
        // TODO: Implement with a proper ListenableWorker subclass
    }
    /**
     * Cancel a scheduled notification
     */
    fun cancelScheduledNotification(notificationId: Int) {
        workManager.cancelAllWorkByTag("notification_$notificationId")
    }
    
    /**
     * Cancel all scheduled notifications
     */
    fun cancelAllScheduledNotifications() {
        workManager.cancelAllWorkByTag("notification")
    }
    
    /**
     * Check if a notification is scheduled
     */
    fun isNotificationScheduled(notificationId: Int): Boolean {
        return workManager.getWorkInfosByTag("notification_$notificationId").get()
            .any { it.state != androidx.work.WorkInfo.State.CANCELLED }
    }
}

/**
 * Notification preference manager
 */
class NotificationPreferenceManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    /**
     * Get notification preferences
     */
    fun getNotificationPreferences(): com.ourcookbook.domain.usecase.notifications.NotificationSettings {
        return com.ourcookbook.domain.usecase.notifications.NotificationSettings()
    }
    
    /**
     * Reset to default preferences
     */
    fun resetToDefaults() {
        // Implementation would reset preferences to defaults
    }
}

/**
 * Notification analytics service
 */
class NotificationAnalyticsService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    /**
     * Track notification shown
     */
    fun trackNotificationShown(notificationType: com.ourcookbook.domain.usecase.notifications.NotificationType) {
        // Implementation would send analytics event
    }
    
    /**
     * Track notification clicked
     */
    fun trackNotificationClicked(notificationType: com.ourcookbook.domain.usecase.notifications.NotificationType) {
        // Implementation would send analytics event
    }
    
    /**
     * Track notification dismissed
     */
    fun trackNotificationDismissed(notificationType: com.ourcookbook.domain.usecase.notifications.NotificationType) {
        // Implementation would send analytics event
    }
    
    /**
     * Get notification analytics
     */
    fun getNotificationAnalytics(): NotificationAnalytics {
        // Implementation would return analytics data
        return NotificationAnalytics(
            totalShown = 0,
            totalClicked = 0,
            totalDismissed = 0,
            byType = emptyMap()
        )
    }
}

/**
 * Notification analytics data
 */
data class NotificationAnalytics(
    val totalShown: Int,
    val totalClicked: Int,
    val totalDismissed: Int,
    val byType: Map<String, TypeAnalytics>
) {
    val clickThroughRate: Float get() = if (totalShown > 0) totalClicked.toFloat() / totalShown.toFloat() else 0f
}

/**
 * Analytics by notification type
 */
data class TypeAnalytics(
    val shown: Int,
    val clicked: Int,
    val dismissed: Int
) {
    val clickThroughRate: Float get() = if (shown > 0) clicked.toFloat() / shown.toFloat() else 0f
}

/**
 * Extension functions for easy notification management
 */

fun Context.getNotificationScheduler(): NotificationScheduler {
    val workManager = WorkManager.getInstance(this)
    return NotificationScheduler(this, workManager)
}

fun Context.getNotificationPreferenceManager(): NotificationPreferenceManager {
    return NotificationPreferenceManager(this)
}

fun Context.getNotificationAnalyticsService(): NotificationAnalyticsService {
    return NotificationAnalyticsService(this)
}
