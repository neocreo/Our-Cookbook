package com.ourcookbook.domain.usecase.chromebook

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.ourcookbook.domain.model.Recipe
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * Keyboard Shortcut Handler for Chromebook and Desktop support
 * 
 * Handles global keyboard shortcuts for the Cookbook app
 * Provides Compose modifiers for keyboard event handling
 */
class KeyboardShortcutHandler(
    private val chromebookOptimizations: ChromebookOptimizations
) {
    
    private val _shortcutEvents = Channel<KeyboardShortcutEvent>(Channel.UNLIMITED)
    val shortcutEvents = _shortcutEvents.receiveAsFlow()
    
    private var isProcessing = false
    
    /**
     * Process a key event and emit shortcut events
     */
    suspend fun processKeyEvent(keyEvent: KeyEvent): Boolean {
        if (isProcessing) return false
        
        isProcessing = true
        try {
            val shortcut = chromebookOptimizations.processShortcut(keyEvent)
            if (shortcut != null) {
                _shortcutEvents.send(KeyboardShortcutEvent(shortcut, keyEvent))
                return true
            }
            return false
        } finally {
            isProcessing = false
        }
    }
    
    /**
     * Send a shortcut event programmatically
     */
    suspend fun sendShortcutEvent(shortcut: KeyboardShortcut) {
        _shortcutEvents.send(KeyboardShortcutEvent(shortcut, null))
    }
    
    /**
     * Get modifier for handling keyboard shortcuts in Compose
     */
    fun shortcutModifier(): Modifier {
        return Modifier.onPreviewKeyEvent { keyEvent ->
            // Only handle key down events
            if (keyEvent.type == KeyEventType.KeyDown) {
                processKeyEvent(keyEvent)
            }
            false
        }
    }
    
    /**
     * Get modifier for global keyboard handling
     */
    fun globalShortcutModifier(onShortcut: (KeyboardShortcut) -> Unit): Modifier {
        return Modifier.onKeyEvent { keyEvent ->
            if (keyEvent.type == KeyEventType.KeyDown) {
                val shortcut = chromebookOptimizations.processShortcut(keyEvent)
                if (shortcut != null) {
                    onShortcut(shortcut)
                    return@onKeyEvent true
                }
            }
            false
        }
    }
    
    /**
     * Check if a specific shortcut is currently pressed
     */
    fun isShortcutPressed(shortcut: KeyboardShortcut): Boolean {
        // This would need to be implemented with actual key state tracking
        // For now, return false as we don't track continuous state
        return false
    }
    
    /**
     * Get all available shortcuts grouped by category
     */
    fun getShortcutsByCategory(): Map<String, List<KeyboardShortcut>> {
        val shortcuts = chromebookOptimizations.getKeyboardShortcuts()
        
        val navigation = shortcuts.filter { 
            it.action.contains("Recipe") || it.action.contains("Cookbook") || it.action.contains("Open")
        }
        
        val editing = shortcuts.filter { 
            it.action.contains("Save") || it.action.contains("Delete") || 
            it.action.contains("Undo") || it.action.contains("Redo") ||
            it.action.contains("Select")
        }
        
        val view = shortcuts.filter { 
            it.action.contains("Zoom") || it.action.contains("Print") || it.action.contains("Export")
        }
        
        val settings = shortcuts.filter { 
            it.action.contains("Settings") || it.action.contains("Sync") || it.action.contains("Quit")
        }
        
        val search = shortcuts.filter { it.action.contains("Search") }
        
        return mapOf(
            "Navigation" to navigation,
            "Editing" to editing,
            "View" to view,
            "Settings" to settings,
            "Search" to search
        ).filterValues { it.isNotEmpty() }
    }
    
    /**
     * Get shortcut for a specific action
     */
    fun getShortcutForAction(action: String): KeyboardShortcut? {
        return chromebookOptimizations.getKeyboardShortcuts()
            .find { it.action.equals(action, ignoreCase = true) }
    }
    
    /**
     * Build a help string showing all shortcuts
     */
    fun buildHelpString(): String {
        val categories = getShortcutsByCategory()
        val builder = StringBuilder()
        
        builder.appendLine("=== Keyboard Shortcuts ===\n")
        
        categories.forEach { (category, shortcuts) ->
            builder.appendLine("$category:")
            shortcuts.sortedBy { it.action }.forEach { shortcut ->
                builder.appendLine("  ${formatShortcut(shortcut.keyCombination)}: ${shortcut.action} - ${shortcut.description}")
            }
            builder.appendLine()
        }
        
        return builder.toString()
    }
    
    /**
     * Format a key combination for display
     */
    private fun formatShortcut(combination: KeyCombination): String {
        val parts = mutableListOf<String>()
        
        combination.modifiers.forEach { modifier ->
            parts.add(when (modifier) {
                Modifier.Ctrl -> "Ctrl"
                Modifier.Shift -> "Shift"
                Modifier.Meta -> "Meta"
                Modifier.Alt -> "Alt"
            })
        }
        
        parts.add(combination.key.name)
        
        return parts.joinToString(" + ")
    }
    
    fun close() {
        _shortcutEvents.close()
    }
}

/**
 * Composable function to handle keyboard shortcuts
 */
@Composable
fun rememberKeyboardShortcutHandler(
    chromebookOptimizations: ChromebookOptimizations
): KeyboardShortcutHandler {
    val handler = remember(chromebookOptimizations) {
        KeyboardShortcutHandler(chromebookOptimizations)
    }
    
    // Cleanup on dispose
    LaunchedEffect(Unit) {
        // Handler will be closed when composable leaves composition
    }
    
    return handler
}

/**
 * Composable modifier for handling keyboard shortcuts
 */
@Composable
fun Modifier.keyboardShortcuts(
    handler: KeyboardShortcutHandler,
    onShortcut: (KeyboardShortcut) -> Unit
): Modifier {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    
    return this.then(
        Modifier.onKeyEvent { keyEvent ->
            if (keyEvent.type == KeyEventType.KeyDown) {
                val shortcut = handler.chromebookOptimizations.processShortcut(keyEvent)
                if (shortcut != null) {
                    onShortcut(shortcut)
                    return@onKeyEvent true
                }
            }
            false
        }
    )
}

/**
 * Key event types
 */
enum class KeyEventType {
    KeyDown, KeyUp
}

/**
 * Event emitted when a keyboard shortcut is triggered
 */
data class KeyboardShortcutEvent(
    val shortcut: KeyboardShortcut,
    val originalEvent: KeyEvent?
)

/**
 * Predefined shortcut actions
 */
object ShortcutActions {
    const val NEW_RECIPE = "New Recipe"
    const val OPEN_RECIPE = "Open Recipe"
    const val SAVE_RECIPE = "Save Recipe"
    const val SEARCH = "Search"
    const val PRINT = "Print"
    const val EXPORT = "Export"
    const val NEW_COOKBOOK = "New Cookbook"
    const val SYNC = "Sync"
    const val UNDO = "Undo"
    const val REDO = "Redo"
    const val DELETE = "Delete"
    const val SELECT_ALL = "Select All"
    const val ZOOM_IN = "Zoom In"
    const val ZOOM_OUT = "Zoom Out"
    const val RESET_ZOOM = "Reset Zoom"
    const val SETTINGS = "Settings"
    const val QUIT = "Quit"
}

/**
 * Extension function to check if a key event matches a specific shortcut
 */
fun KeyEvent.matchesShortcut(shortcut: KeyboardShortcut): Boolean {
    val handler = KeyboardShortcutHandler(ChromebookOptimizations(
        android.content.Context(),
        object : com.ourcookbook.domain.repository.DeviceRepository {
            override suspend fun getById(id: String): com.ourcookbook.domain.model.Device? = null
            override suspend fun getAll(): List<com.ourcookbook.domain.model.Device> = emptyList()
            override suspend fun insert(device: com.ourcookbook.domain.model.Device) = Unit
            override suspend fun update(device: com.ourcookbook.domain.model.Device) = Unit
            override suspend fun delete(id: String) = Unit
            override suspend fun getByDeviceId(deviceId: String): com.ourcookbook.domain.model.Device? = null
            override fun getDeviceFlow(deviceId: String) = kotlinx.coroutines.flow.emptyFlow()
        }
    ))
    return handler.processShortcut(this) == shortcut
}
