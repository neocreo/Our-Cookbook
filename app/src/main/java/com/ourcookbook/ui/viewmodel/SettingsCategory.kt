package com.ourcookbook.ui.viewmodel

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Settings Category Definitions
 * Organizes settings into logical groups for better UX
 */

enum class SettingsCategory(
    val displayName: String,
    val icon: ImageVector? = null,
    val priority: Int = 0
) {
    // Main categories
    APPSETTINGS("App Settings", priority = 1),
    ACCOUNT("Account & Device", priority = 2),
    PRIVACY("Privacy & Security", priority = 3),
    NOTIFICATIONS("Notifications", priority = 4),
    ACCESSIBILITY("Accessibility", priority = 5),
    ABOUT("About", priority = 6),
    ADVANCED("Advanced", priority = 7);

    companion object {
        fun getAllCategories(): List<SettingsCategory> {
            return enumValues<SettingsCategory>().sortedBy { it.priority }
        }

        fun getCategoryByName(name: String): SettingsCategory? {
            return enumValues<SettingsCategory>().firstOrNull { it.displayName == name }
        }
    }
}

/**
 * Settings Item Definition
 * Represents a single setting option with all necessary metadata
 */
data class SettingsItemDefinition(
    val id: String,
    val category: SettingsCategory,
    val title: String,
    val subtitle: String? = null,
    val icon: ImageVector? = null,
    val type: SettingsItemType = SettingsItemType.ITEM,
    val defaultValue: Any? = null,
    val options: List<Any>? = null,
    val minValue: Float? = null,
    val maxValue: Float? = null,
    val step: Float? = null,
    val requiresPermission: Boolean = false,
    val isDestructive: Boolean = false,
    val confirmationRequired: Boolean = false,
    val confirmationMessage: String? = null
)

/**
 * Settings Item Types
 */
enum class SettingsItemType {
    ITEM,           // Basic item with navigation
    TOGGLE,         // Boolean toggle switch
    DROPDOWN,       // Dropdown selector
    SLIDER,         // Numeric slider
    BUTTON,         // Action button
    INFO,           // Information display only
    DIVIDER,        // Section divider
    HEADER          // Section header
}

/**
 * Settings Group Definition
 * Groups related settings together with optional description
 */
data class SettingsGroup(
    val id: String,
    val category: SettingsCategory,
    val title: String,
    val description: String? = null,
    val items: List<SettingsItemDefinition> = emptyList(),
    val isExpanded: Boolean = true
)