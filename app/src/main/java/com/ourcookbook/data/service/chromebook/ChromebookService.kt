package com.ourcookbook.data.service.chromebook

import android.content.Context
import com.ourcookbook.domain.repository.DeviceRepository
import com.ourcookbook.domain.usecase.chromebook.ChromebookConfig
import com.ourcookbook.domain.usecase.chromebook.ChromebookOptimizations
import com.ourcookbook.domain.usecase.chromebook.KeyboardShortcutHandler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Singleton

/**
 * Chromebook-specific services module
 * 
 * Provides Chromebook detection, optimization, and input handling services
 */
@Module
@InstallIn(SingletonComponent::class)
object ChromebookServiceModule {
    
    @Provides
    @Singleton
    fun provideChromebookOptimizations(
        @ApplicationContext context: Context,
        deviceRepository: DeviceRepository
    ): ChromebookOptimizations {
        return ChromebookOptimizations(context, deviceRepository)
    }
    
    @Provides
    @Singleton
    fun provideKeyboardShortcutHandler(
        chromebookOptimizations: ChromebookOptimizations
    ): KeyboardShortcutHandler {
        return KeyboardShortcutHandler(chromebookOptimizations)
    }
    
    @Provides
    fun provideChromebookConfig(
        @ApplicationContext context: Context,
        deviceRepository: DeviceRepository
    ): Flow<ChromebookConfig> {
        return createChromebookConfig(context, deviceRepository)
    }
}

/**
 * Factory function to create ChromebookConfig flow
 */
fun createChromebookConfig(
    context: Context,
    deviceRepository: DeviceRepository
): Flow<ChromebookConfig> {
    return flow {
        val optimizations = ChromebookOptimizations(context, deviceRepository)
        
        val config = ChromebookConfig(
            deviceType = optimizations.getDeviceType(),
            screenWidthDp = optimizations.getScreenWidthDp(),
            screenHeightDp = optimizations.getScreenHeightDp(),
            hasKeyboard = optimizations.hasHardwareKeyboard(),
            hasMouse = optimizations.hasMouseSupport(),
            hasStylus = optimizations.hasStylusSupport(),
            isChromebook = optimizations.isChromebook(),
            useMultiPane = optimizations.shouldUseMultiPaneLayout(),
            useExpandedLayout = optimizations.shouldUseExpandedLayout(),
            supportsFreeformWindow = optimizations.supportsFreeformWindow(),
            supportsPictureInPicture = optimizations.supportsPictureInPicture(),
            keyboardShortcuts = optimizations.getKeyboardShortcuts()
        )
        
        emit(config)
    }
}

/**
 * Chromebook detection utilities
 */
object ChromebookDetection {
    
    /**
     * Quick check if running on Chromebook without needing full context
     */
    fun isLikelyChromebook(): Boolean {
        return android.os.Build.BRAND?.contains("chrome", ignoreCase = true) == true ||
               android.os.Build.MANUFACTURER?.contains("chrome", ignoreCase = true) == true ||
               android.os.Build.DEVICE?.contains("chrome", ignoreCase = true) == true
    }
    
    /**
     * Check if Chrome OS based on build properties
     */
    fun isChromeOS(): Boolean {
        return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M &&
               (android.os.Build.BRAND?.equals("chromeos", ignoreCase = true) == true ||
                System.getProperty("os.name")?.contains("chrome", ignoreCase = true) == true)
    }
}

/**
 * Chromebook capabilities provider
 */
class ChromebookCapabilitiesProvider(
    private val context: Context
) {
    
    private val optimizations by lazy { 
        ChromebookOptimizations(
            context,
            object : DeviceRepository {
                override suspend fun getById(id: String): com.ourcookbook.domain.model.Device? = null
                override suspend fun getAll(): List<com.ourcookbook.domain.model.Device> = emptyList()
                override suspend fun insert(device: com.ourcookbook.domain.model.Device) = Unit
                override suspend fun update(device: com.ourcookbook.domain.model.Device) = Unit
                override suspend fun delete(id: String) = Unit
                override suspend fun getByDeviceId(deviceId: String): com.ourcookbook.domain.model.Device? = null
                override fun getDeviceFlow(deviceId: String) = kotlinx.coroutines.flow.emptyFlow()
            }
        )
    }
    
    /**
     * Get all Chromebook capabilities
     */
    fun getCapabilities(): ChromebookCapabilities {
        return ChromebookCapabilities(
            isChromebook = optimizations.isChromebook(),
            isChromeOS = optimizations.isChromeOS(),
            deviceType = optimizations.getDeviceType(),
            screenWidthDp = optimizations.getScreenWidthDp(),
            screenHeightDp = optimizations.getScreenHeightDp(),
            hasKeyboard = optimizations.hasHardwareKeyboard(),
            hasMouse = optimizations.hasMouseSupport(),
            hasStylus = optimizations.hasStylusSupport(),
            supportsMultiPane = optimizations.shouldUseMultiPaneLayout(),
            supportsExpandedLayout = optimizations.shouldUseExpandedLayout(),
            supportsFreeformWindow = optimizations.supportsFreeformWindow(),
            supportsPictureInPicture = optimizations.supportsPictureInPicture()
        )
    }
    
    /**
     * Check if a specific capability is available
     */
    fun hasCapability(capability: ChromebookCapability): Boolean {
        val caps = getCapabilities()
        return when (capability) {
            ChromebookCapability.CHROMEBOOK -> caps.isChromebook
            ChromebookCapability.CHROME_OS -> caps.isChromeOS
            ChromebookCapability.KEYBOARD -> caps.hasKeyboard
            ChromebookCapability.MOUSE -> caps.hasMouse
            ChromebookCapability.STYLUS -> caps.hasStylus
            ChromebookCapability.MULTI_PANE -> caps.supportsMultiPane
            ChromebookCapability.EXPANDED_LAYOUT -> caps.supportsExpandedLayout
            ChromebookCapability.FREEFORM_WINDOW -> caps.supportsFreeformWindow
            ChromebookCapability.PICTURE_IN_PICTURE -> caps.supportsPictureInPicture
        }
    }
}

/**
 * Chromebook capabilities data class
 */
data class ChromebookCapabilities(
    val isChromebook: Boolean,
    val isChromeOS: Boolean,
    val deviceType: String,
    val screenWidthDp: Int,
    val screenHeightDp: Int,
    val hasKeyboard: Boolean,
    val hasMouse: Boolean,
    val hasStylus: Boolean,
    val supportsMultiPane: Boolean,
    val supportsExpandedLayout: Boolean,
    val supportsFreeformWindow: Boolean,
    val supportsPictureInPicture: Boolean
)

/**
 * Chromebook capability types
 */
enum class ChromebookCapability {
    CHROMEBOOK,
    CHROME_OS,
    KEYBOARD,
    MOUSE,
    STYLUS,
    MULTI_PANE,
    EXPANDED_LAYOUT,
    FREEFORM_WINDOW,
    PICTURE_IN_PICTURE
}

/**
 * Extension functions for easy capability checks
 */

fun Context.isChromebook(): Boolean {
    return ChromebookDetection.isLikelyChromebook()
}

fun Context.isChromeOS(): Boolean {
    return ChromebookDetection.isChromeOS()
}

fun Context.getChromebookCapabilities(): ChromebookCapabilities {
    return ChromebookCapabilitiesProvider(this).getCapabilities()
}

fun Context.hasCapability(capability: ChromebookCapability): Boolean {
    return ChromebookCapabilitiesProvider(this).hasCapability(capability)
}
