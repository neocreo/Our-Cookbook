package com.ourcookbook.domain.usecase.chromebook

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.view.WindowManager
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.ourcookbook.domain.model.Device
import com.ourcookbook.domain.model.DeviceCapability
import com.ourcookbook.domain.repository.DeviceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.Instant

/**
 * Chromebook-specific optimizations for the Cookbook app
 * 
 * Handles:
 * - Large screen layout detection
 * - Keyboard shortcut registration
 * - Desktop-style navigation
 * - Mouse/trackpad support
 * - Stylus input handling
 * - Window management
 */
class ChromebookOptimizations(
    private val context: Context,
    private val deviceRepository: DeviceRepository
) {
    
    companion object {
        // Chromebook device types
        const val DEVICE_TYPE_CHROMEBOOK = "CHROMEBOOK"
        const val DEVICE_TYPE_TABLET = "TABLET"
        const val DEVICE_TYPE_PHONE = "PHONE"
        const val DEVICE_TYPE_DESKTOP = "DESKTOP"
        
        // Screen size thresholds (in dp)
        const val COMPACT_SCREEN_WIDTH = 600
        const val MEDIUM_SCREEN_WIDTH = 900
        const val EXPANDED_SCREEN_WIDTH = 1200
        
        // Minimum width for multi-pane layout
        const val MULTI_PANE_THRESHOLD = 900
    }
    
    /**
     * Detect if running on a Chromebook
     */
    fun isChromebook(): Boolean {
        return Build.BRAND?.contains("chrome", ignoreCase = true) == true ||
               Build.MANUFACTURER?.contains("chrome", ignoreCase = true) == true ||
               Build.DEVICE?.contains("chrome", ignoreCase = true) == true
    }
    
    /**
     * Detect if running on Chrome OS
     */
    fun isChromeOS(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
               (Build.BRAND?.equals("chromeos", ignoreCase = true) == true ||
                System.getProperty("os.name")?.contains("chrome", ignoreCase = true) == true)
    }
    
    /**
     * Get the device type based on screen size and capabilities
     */
    fun getDeviceType(): String {
        val screenWidth = getScreenWidthDp()
        val hasKeyboard = hasHardwareKeyboard()
        val hasMouse = hasMouseSupport()
        
        return when {
            isChromeOS() -> DEVICE_TYPE_CHROMEBOOK
            screenWidth >= EXPANDED_SCREEN_WIDTH && hasKeyboard -> DEVICE_TYPE_DESKTOP
            screenWidth >= MEDIUM_SCREEN_WIDTH -> DEVICE_TYPE_TABLET
            hasKeyboard && hasMouse -> DEVICE_TYPE_CHROMEBOOK
            else -> DEVICE_TYPE_PHONE
        }
    }
    
    /**
     * Get screen width in density-independent pixels
     */
    fun getScreenWidthDp(): Int {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = context.resources.displayMetrics
        val screenWidthPx = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            windowManager.currentWindowMetrics.bounds.width()
        } else {
            @Suppress("DEPRECATION")
            displayMetrics.widthPixels
        }
        return (screenWidthPx / displayMetrics.density).toInt()
    }
    
    /**
     * Get screen height in density-independent pixels
     */
    fun getScreenHeightDp(): Int {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = context.resources.displayMetrics
        val screenHeightPx = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            windowManager.currentWindowMetrics.bounds.height()
        } else {
            @Suppress("DEPRECATION")
            displayMetrics.heightPixels
        }
        return (screenHeightPx / displayMetrics.density).toInt()
    }
    
    /**
     * Check if device has a hardware keyboard
     */
    fun hasHardwareKeyboard(): Boolean {
        val config = context.resources.configuration
        return config.keyboard != Configuration.KEYBOARD_NOKEYS &&
               config.keyboardHidden == Configuration.KEYBOARDHIDDEN_NO
    }
    
    /**
     * Check if device supports mouse input
     */
    fun hasMouseSupport(): Boolean {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            windowManager.currentWindowMetrics
        } else {
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay
        }
        
        // Check if the device has a mouse or trackpad
        return try {
            val inputManager = context.getSystemService(Context.INPUT_SERVICE)
            val hasMouse = inputManager?.let { im ->
                // Check for mouse/trackpad
                val deviceIds = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    @Suppress("DEPRECATION")
                    android.view.InputDevice.getDeviceIds()
                } else {
                    intArrayOf()
                }
                deviceIds.any { id ->
                    val device = android.view.InputDevice.getDevice(id)
                    device?.sources?.and(android.view.InputDevice.SOURCE_MOUSE) != 0 ||
                    device?.sources?.and(android.view.InputDevice.SOURCE_TRACKBALL) != 0 ||
                    device?.sources?.and(android.view.InputDevice.SOURCE_TOUCHPAD) != 0
                }
            } ?: false
            hasMouse
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Check if multi-pane layout should be used
     */
    fun shouldUseMultiPaneLayout(): Boolean {
        return getScreenWidthDp() >= MULTI_PANE_THRESHOLD
    }
    
    /**
     * Check if expanded layout should be used (for very large screens)
     */
    fun shouldUseExpandedLayout(): Boolean {
        return getScreenWidthDp() >= EXPANDED_SCREEN_WIDTH
    }
    
    /**
     * Get screen size category
     */
    fun getScreenSizeCategory(): String {
        val width = getScreenWidthDp()
        return when {
            width >= EXPANDED_SCREEN_WIDTH -> "expanded"
            width >= MEDIUM_SCREEN_WIDTH -> "medium"
            width >= COMPACT_SCREEN_WIDTH -> "compact"
            else -> "small"
        }
    }
    
    /**
     * Check if device supports stylus input
     */
    fun hasStylusSupport(): Boolean {
        return try {
            val inputManager = context.getSystemService(Context.INPUT_SERVICE)
            val deviceIds = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                @Suppress("DEPRECATION")
                android.view.InputDevice.getDeviceIds()
            } else {
                intArrayOf()
            }
            deviceIds.any { id ->
                val device = android.view.InputDevice.getDevice(id)
                device?.sources?.and(android.view.InputDevice.SOURCE_STYLUS) != 0
            }
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Check if device is in desktop mode (Chrome OS desktop mode)
     */
    fun isDesktopMode(): Boolean {
        return isChromeOS() &&
               context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE &&
               hasHardwareKeyboard()
    }
    
    /**
     * Update device information for Chromebook
     */
    suspend fun updateDeviceForChromebook(deviceId: String) {
        val device = deviceRepository.getDeviceById(deviceId) ?: return
        val capabilities = device.capabilities.toMutableSet()
        if (hasHardwareKeyboard()) capabilities.add(DeviceCapability.KEYBOARD)
        if (getScreenWidthDp() >= MEDIUM_SCREEN_WIDTH) capabilities.add(DeviceCapability.LARGE_SCREEN)
        val updatedDevice = device.copy(
            capabilities = capabilities,
            lastSeenAt = Instant.now()
        )
        deviceRepository.updateDevice(updatedDevice)
    }
    
    /**
     * Get all registered keyboard shortcuts
     */
    fun getKeyboardShortcuts(): List<KeyboardShortcut> {
        return listOf(
            // Navigation shortcuts
            KeyboardShortcut(
                keyCombination = KeyCombination.Ctrl + Key.N,
                action = "New Recipe",
                description = "Create a new recipe"
            ),
            KeyboardShortcut(
                keyCombination = KeyCombination.Ctrl + Key.O,
                action = "Open Recipe",
                description = "Open selected recipe"
            ),
            KeyboardShortcut(
                keyCombination = KeyCombination.Ctrl + Key.S,
                action = "Save Recipe",
                description = "Save current recipe"
            ),
            KeyboardShortcut(
                keyCombination = KeyCombination.Ctrl + Key.F,
                action = "Search",
                description = "Focus search bar"
            ),
            KeyboardShortcut(
                keyCombination = KeyCombination.Ctrl + Key.P,
                action = "Print",
                description = "Print current recipe"
            ),
            KeyboardShortcut(
                keyCombination = KeyCombination.Ctrl + Key.E,
                action = "Export",
                description = "Export current recipe"
            ),
            // Cookbook shortcuts
            KeyboardShortcut(
                keyCombination = KeyCombination.Ctrl + Modifier.Shift + Key.N,
                action = "New Cookbook",
                description = "Create a new cookbook"
            ),
            KeyboardShortcut(
                keyCombination = KeyCombination.Ctrl + Modifier.Shift + Key.S,
                action = "Sync",
                description = "Sync all cookbooks"
            ),
            // Editing shortcuts
            KeyboardShortcut(
                keyCombination = KeyCombination.Ctrl + Key.Z,
                action = "Undo",
                description = "Undo last action"
            ),
            KeyboardShortcut(
                keyCombination = KeyCombination.Ctrl + Key.Y,
                action = "Redo",
                description = "Redo last action"
            ),
            KeyboardShortcut(
                keyCombination = KeyCombination.Ctrl + Key.D,
                action = "Delete",
                description = "Delete selected item"
            ),
            KeyboardShortcut(
                keyCombination = KeyCombination.Ctrl + Key.A,
                action = "Select All",
                description = "Select all items"
            ),
            // View shortcuts
            KeyboardShortcut(
                keyCombination = KeyCombination.Ctrl + Key.Plus,
                action = "Zoom In",
                description = "Increase zoom level"
            ),
            KeyboardShortcut(
                keyCombination = KeyCombination.Ctrl + Key.Minus,
                action = "Zoom Out",
                description = "Decrease zoom level"
            ),
            KeyboardShortcut(
                keyCombination = KeyCombination.Ctrl + Key.Zero,
                action = "Reset Zoom",
                description = "Reset zoom to default"
            ),
            // Settings shortcuts
            KeyboardShortcut(
                keyCombination = KeyCombination.Ctrl + Key.Comma,
                action = "Settings",
                description = "Open settings"
            ),
            KeyboardShortcut(
                keyCombination = KeyCombination.Ctrl + Key.Q,
                action = "Quit",
                description = "Close application"
            )
        )
    }
    
    /**
     * Process keyboard shortcut
     */
    fun processShortcut(keyEvent: KeyEvent): KeyboardShortcut? {
        val shortcuts = getKeyboardShortcuts()
        
        for (shortcut in shortcuts) {
            if (matchesShortcut(keyEvent, shortcut.keyCombination)) {
                return shortcut
            }
        }
        return null
    }
    
    /**
     * Check if key event matches a shortcut combination
     */
    private fun matchesShortcut(keyEvent: KeyEvent, combination: KeyCombination): Boolean {
        val ctrlPressed = keyEvent.isCtrlPressed
        val shiftPressed = keyEvent.isShiftPressed
        val metaPressed = keyEvent.isMetaPressed
        val key = keyEvent.key
        
        val expectedCtrl = combination.modifiers.contains(Modifier.Ctrl)
        val expectedShift = combination.modifiers.contains(Modifier.Shift)
        val expectedMeta = combination.modifiers.contains(Modifier.Meta)
        val expectedKey = combination.key
        
        return ctrlPressed == expectedCtrl &&
               shiftPressed == expectedShift &&
               metaPressed == expectedMeta &&
               key == expectedKey
    }
    
    /**
     * Check if device supports freeform window mode (Chrome OS)
     */
    fun supportsFreeformWindow(): Boolean {
        return isChromeOS() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
    }
    
    /**
     * Check if device supports picture-in-picture mode
     */
    fun supportsPictureInPicture(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
               context.packageManager.hasSystemFeature("android.hardware.type.television") == false
    }
    
    /**
     * Get recommended window size for multi-window mode
     */
    fun getRecommendedWindowSize(): Pair<Int, Int> {
        val width = getScreenWidthDp()
        val height = getScreenHeightDp()
        
        return when {
            width >= EXPANDED_SCREEN_WIDTH -> Pair(1200, 800)
            width >= MEDIUM_SCREEN_WIDTH -> Pair(1000, 700)
            else -> Pair(800, 600)
        }
    }
}

/**
 * Data class representing a keyboard shortcut
 */
data class KeyboardShortcut(
    val keyCombination: KeyCombination,
    val action: String,
    val description: String
)

/**
 * Data class representing a key combination
 */
data class KeyCombination(
    val key: Key,
    val modifiers: Set<Modifier> = emptySet()
) {
    operator fun plus(key: Key): KeyCombination {
        return KeyCombination(key, this.modifiers)
    }

    operator fun plus(modifier: Modifier): KeyCombination {
        return KeyCombination(this.key, this.modifiers + modifier)
    }

    operator fun plus(combination: KeyCombination): KeyCombination {
        return KeyCombination(
            combination.key,
            this.modifiers + combination.modifiers
        )
    }

    companion object {
        val Ctrl = KeyCombination(Key.DirectionLeft, setOf(Modifier.Ctrl))
        val Shift = KeyCombination(Key.DirectionLeft, setOf(Modifier.Shift))
        val Meta = KeyCombination(Key.DirectionLeft, setOf(Modifier.Meta))
    }
}

/**
 * Modifier keys
 */
enum class Modifier {
    Ctrl, Shift, Meta, Alt
}

/**
 * Chromebook-specific configuration
 */
data class ChromebookConfig(
    val deviceType: String,
    val screenWidthDp: Int,
    val screenHeightDp: Int,
    val hasKeyboard: Boolean,
    val hasMouse: Boolean,
    val hasStylus: Boolean,
    val isChromebook: Boolean,
    val useMultiPane: Boolean,
    val useExpandedLayout: Boolean,
    val supportsFreeformWindow: Boolean,
    val supportsPictureInPicture: Boolean,
    val keyboardShortcuts: List<KeyboardShortcut>
)

/**
 * Factory function to create ChromebookConfig
 */
fun createChromebookConfig(context: Context, deviceRepository: DeviceRepository): Flow<ChromebookConfig> {
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
