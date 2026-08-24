package com.ourcookbook.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Cookbook Elevation System
 * 
 * Complete elevation implementation following Material Design 3 guidelines
 * and the design tokens specified in project-docs/cookbook-ux-foundation.md.
 * 
 * Features:
 * - Consistent elevation scale for depth hierarchy
 * - Component-specific elevation presets
 * - Shadow and overlay utilities
 * - Elevation tokens for easy theming
 */

// ============================================================================
// MAIN ELEVATION SYSTEM
// ============================================================================

/**
 * Cookbook Elevation
 * 
 * Main elevation system for the Cookbook app following Material Design 3.
 * Defines elevation levels for different component types.
 */
object CookbookElevation {
    // Base elevation levels
    val none: Dp = 0.dp        // No elevation (flat)
    val small: Dp = 2.dp       // Subtle elevation
    val medium: Dp = 4.dp      // Default elevation
    val large: Dp = 8.dp       // Prominent elevation
    val xLarge: Dp = 12.dp     // Strong elevation
    val xxLarge: Dp = 16.dp    // Very strong elevation
    val xxxLarge: Dp = 24.dp   // Maximum elevation

    // Component elevations
    val card: Dp = medium       // Card elevation
    val dialog: Dp = large      // Dialog elevation
    val bottomBar: Dp = large   // Bottom app bar elevation
    val fab: Dp = large         // Floating action button elevation
    val button: Dp = small      // Button elevation
    val snackbar: Dp = large    // Snackbar elevation
    val dropdown: Dp = large    // Dropdown menu elevation
    val tooltip: Dp = large     // Tooltip elevation
    val bottomSheet: Dp = large // Bottom sheet elevation
}

// ============================================================================
// ELEVATION TOKENS
// ============================================================================

/**
 * Elevation tokens for consistent usage across the app
 */
object ElevationTokens {
    // Level tokens (Material Design 3 elevation levels)
    val level0: Dp = 0.dp   // Surface level
    val level1: Dp = 1.dp   // Raised surface
    val level2: Dp = 3.dp   // Floating surface
    val level3: Dp = 6.dp   // Elevated surface
    val level4: Dp = 8.dp   // Higher elevation
    val level5: Dp = 12.dp  // Very elevated

    // Semantic tokens
    val flat: Dp = 0.dp
    val raised: Dp = 2.dp
    val floating: Dp = 4.dp
    val elevated: Dp = 8.dp
    val high: Dp = 12.dp
    val veryHigh: Dp = 16.dp
}

// ============================================================================
// COMPONENT-SPECIFIC ELEVATION
// ============================================================================

/**
 * Component-specific elevation presets
 */
object ComponentElevation {
    
    // Card elevations
    val recipeCard: Dp = CookbookElevation.medium
    val recipeCardHover: Dp = CookbookElevation.large
    val recipeCardActive: Dp = CookbookElevation.xLarge
    
    val categoryCard: Dp = CookbookElevation.small
    val categoryCardHover: Dp = CookbookElevation.medium
    
    val cookbookCard: Dp = CookbookElevation.medium
    val settingsCard: Dp = CookbookElevation.small
    
    // Button elevations
    val primaryButton: Dp = CookbookElevation.small
    val primaryButtonPressed: Dp = CookbookElevation.medium
    val primaryButtonHover: Dp = CookbookElevation.medium
    
    val secondaryButton: Dp = CookbookElevation.none
    val outlinedButton: Dp = CookbookElevation.none
    val textButton: Dp = CookbookElevation.none
    
    val iconButton: Dp = CookbookElevation.small
    val iconButtonPressed: Dp = CookbookElevation.medium
    
    val floatingActionButton: Dp = CookbookElevation.large
    val floatingActionButtonPressed: Dp = CookbookElevation.xLarge
    
    // Input field elevations
    val textField: Dp = CookbookElevation.none
    val textFieldFocused: Dp = CookbookElevation.small
    
    // Navigation elevations
    val bottomNavigation: Dp = CookbookElevation.large
    val topAppBar: Dp = CookbookElevation.small
    val navigationRail: Dp = CookbookElevation.medium
    
    // Dialog and modal elevations
    val dialog: Dp = CookbookElevation.large
    val dialogOverlay: Dp = CookbookElevation.xLarge
    
    val bottomSheet: Dp = CookbookElevation.large
    val bottomSheetModal: Dp = CookbookElevation.xLarge
    
    val modal: Dp = CookbookElevation.large
    
    // List elevations
    val listItem: Dp = CookbookElevation.none
    val listItemHover: Dp = CookbookElevation.small
    
    // Image elevations
    val recipeImage: Dp = CookbookElevation.none
    val recipeImageOverlay: Dp = CookbookElevation.small
    
    // Status and feedback elevations
    val snackbar: Dp = CookbookElevation.large
    val toast: Dp = CookbookElevation.large
    
    val tooltip: Dp = CookbookElevation.large
    val contextMenu: Dp = CookbookElevation.large
    
    // Chip elevations
    val chip: Dp = CookbookElevation.none
    val chipHover: Dp = CookbookElevation.small
    
    // Badge elevations
    val badge: Dp = CookbookElevation.none
    
    // Divider elevations
    val divider: Dp = CookbookElevation.none
}

// ============================================================================
// ELEVATION STATES
// ============================================================================

/**
 * Elevation states for interactive components
 */
object ElevationStates {
    
    // Default state elevations
    fun button(default: Dp = CookbookElevation.small): ButtonElevation {
        return ButtonElevation(
            default = default,
            pressed = default + 2.dp,
            hover = default + 2.dp,
            focused = default + 2.dp,
            disabled = CookbookElevation.none
        )
    }
    
    fun card(default: Dp = CookbookElevation.medium): CardElevation {
        return CardElevation(
            default = default,
            pressed = default + 4.dp,
            hover = default + 4.dp,
            focused = default + 2.dp
        )
    }
    
    fun fab(default: Dp = CookbookElevation.large): FabElevation {
        return FabElevation(
            default = default,
            pressed = default + 4.dp,
            hover = default + 4.dp,
            focused = default + 2.dp
        )
    }
}

/**
 * Button elevation states
 */
data class ButtonElevation(
    val default: Dp,
    val pressed: Dp,
    val hover: Dp,
    val focused: Dp,
    val disabled: Dp
)

/**
 * Card elevation states
 */
data class CardElevation(
    val default: Dp,
    val pressed: Dp,
    val hover: Dp,
    val focused: Dp
)

/**
 * Floating Action Button elevation states
 */
data class FabElevation(
    val default: Dp,
    val pressed: Dp,
    val hover: Dp,
    val focused: Dp
)

// ============================================================================
// ELEVATION UTILITIES
// ============================================================================

/**
 * Elevation utility functions
 */

/**
 * Create an elevation with pressed state
 */
fun Dp.withPressedState(pressedIncrement: Dp = 2.dp): Pair<Dp, Dp> {
    return this to (this + pressedIncrement)
}

/**
 * Create an elevation with hover state
 */
fun Dp.withHoverState(hoverIncrement: Dp = 2.dp): Pair<Dp, Dp> {
    return this to (this + hoverIncrement)
}

/**
 * Create a full elevation state set
 */
fun Dp.withStates(
    pressedIncrement: Dp = 2.dp,
    hoverIncrement: Dp = 2.dp,
    focusedIncrement: Dp = 1.dp
): Map<String, Dp> {
    return mapOf(
        "default" to this,
        "pressed" to (this + pressedIncrement),
        "hover" to (this + hoverIncrement),
        "focused" to (this + focusedIncrement)
    )
}

/**
 * Scale an elevation value
 */
fun Dp.scaleElevation(factor: Float): Dp {
    return (this.value * factor).dp
}

/**
 * Clamp elevation to a maximum value
 */
fun Dp.clampElevation(max: Dp): Dp {
    return if (this > max) max else this
}

// ============================================================================
// SHADOW UTILITIES
// ============================================================================

/**
 * Shadow utilities for elevation
 */
object ShadowUtils {
    
    // Shadow colors (using Material Design 3 shadow colors)
    val shadowColorLight = Color(0xFF000000).copy(alpha = 0.2f)
    val shadowColorDark = Color(0xFF000000).copy(alpha = 0.4f)
    
    // Shadow blur radii for different elevation levels
    val shadowBlurSmall = 4.dp
    val shadowBlurMedium = 8.dp
    val shadowBlurLarge = 12.dp
    val shadowBlurXLarge = 16.dp
    
    // Shadow offsets for different elevation levels
    val shadowOffsetXSmall = 0.dp
    val shadowOffsetYSmall = 1.dp
    val shadowOffsetXMedium = 0.dp
    val shadowOffsetYMedium = 2.dp
    val shadowOffsetXLarge = 0.dp
    val shadowOffsetYLarge = 4.dp
    val shadowOffsetXXLarge = 0.dp
    val shadowOffsetYXLarge = 8.dp
}

// ============================================================================
// OVERLAY UTILITIES
// ============================================================================

/**
 * Overlay utilities for elevation (Material Design 3 overlay concept)
 */
object OverlayUtils {
    
    // Overlay colors for different elevation levels
    // These are used to create the "elevated surface" effect in dark theme
    val overlayColor0 = Color(0xFF000000).copy(alpha = 0.0f)
    val overlayColor1 = Color(0xFF000000).copy(alpha = 0.05f)
    val overlayColor2 = Color(0xFF000000).copy(alpha = 0.08f)
    val overlayColor3 = Color(0xFF000000).copy(alpha = 0.11f)
    val overlayColor4 = Color(0xFF000000).copy(alpha = 0.12f)
    val overlayColor5 = Color(0xFF000000).copy(alpha = 0.14f)
    
    /**
     * Get overlay color for a specific elevation level
     */
    fun getOverlayColor(elevation: Dp): Color {
        return when {
            elevation >= 24.dp -> overlayColor5
            elevation >= 12.dp -> overlayColor4
            elevation >= 8.dp -> overlayColor3
            elevation >= 4.dp -> overlayColor2
            elevation >= 1.dp -> overlayColor1
            else -> overlayColor0
        }
    }
}