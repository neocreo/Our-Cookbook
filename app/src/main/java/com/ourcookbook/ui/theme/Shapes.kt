package com.ourcookbook.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Cookbook Shape System
 * 
 * Complete shape implementation following Material Design 3 guidelines
 * and the design tokens specified in project-docs/cookbook-ux-foundation.md.
 * 
 * Features:
 * - Full Material Design 3 shape scale
 * - Custom shapes for different component types
 * - Consistent corner radii across the app
 * - Shape tokens for easy theming
 */

// ============================================================================
// MAIN SHAPE SYSTEM
// ============================================================================

/**
 * Cookbook Shapes
 * 
 * Main shape system for the Cookbook app following Material Design 3.
 * Defines corner radii for different component sizes.
 */
val CookbookShapes = Shapes(
    // Extra small - Tight corners (4dp)
    extraSmall = RoundedCornerShape(4.dp),
    
    // Small - Compact corners (8dp)
    small = RoundedCornerShape(8.dp),
    
    // Medium - Default corners (12dp)
    medium = RoundedCornerShape(12.dp),
    
    // Large - Generous corners (16dp)
    large = RoundedCornerShape(16.dp),
    
    // Extra large - Very rounded corners (24dp)
    extraLarge = RoundedCornerShape(24.dp)
)

// ============================================================================
// SHAPE TOKENS
// ============================================================================

/**
 * Shape tokens for consistent usage across the app
 */
object ShapeTokens {
    // Corner radii
    val xxs = 4.dp      // Extra extra small
    val xs = 8.dp       // Extra small
    val sm = 12.dp      // Small
    val md = 16.dp      // Medium
    val lg = 24.dp      // Large
    val xl = 32.dp      // Extra large
    val full = 50.dp   // For circular shapes
}

// ============================================================================
// COMPONENT-SPECIFIC SHAPES
// ============================================================================

/**
 * Component-specific shapes for consistent styling
 */
object ComponentShapes {
    
    // Card shapes
    val recipeCard = RoundedCornerShape(12.dp)
    val categoryCard = RoundedCornerShape(16.dp)
    val cookbookCard = RoundedCornerShape(12.dp)
    val settingsCard = RoundedCornerShape(8.dp)
    
    // Button shapes
    val primaryButton = RoundedCornerShape(8.dp)
    val secondaryButton = RoundedCornerShape(8.dp)
    val outlinedButton = RoundedCornerShape(8.dp)
    val textButton = RoundedCornerShape(4.dp)
    val iconButton = RoundedCornerShape(8.dp)
    val floatingActionButton = RoundedCornerShape(16.dp)
    
    // Input field shapes
    val textField = RoundedCornerShape(8.dp)
    val outlinedTextField = RoundedCornerShape(8.dp)
    val searchField = RoundedCornerShape(24.dp) // Pill shape
    
    // Chip shapes
    val filterChip = RoundedCornerShape(16.dp) // Pill shape
    val suggestionChip = RoundedCornerShape(16.dp)
    val actionChip = RoundedCornerShape(16.dp)
    val categoryChip = RoundedCornerShape(16.dp)
    
    // Image shapes
    val recipeImage = RoundedCornerShape(8.dp)
    val categoryImage = RoundedCornerShape(12.dp)
    val profileImage = CircleShape
    val iconImage = CircleShape
    
    // Dialog and modal shapes
    val dialog = RoundedCornerShape(24.dp)
    val bottomSheet = RoundedCornerShape(16.dp, 16.dp, 0.dp, 0.dp)
    val modal = RoundedCornerShape(16.dp)
    val popup = RoundedCornerShape(8.dp)
    
    // Navigation shapes
    val bottomNavigation = RoundedCornerShape(0.dp) // No rounding for bottom nav
    val topAppBar = RoundedCornerShape(0.dp)
    val navigationRail = RoundedCornerShape(8.dp)
    
    // List item shapes
    val listItem = RoundedCornerShape(8.dp)
    val listItemAvatar = CircleShape
    val listItemIcon = CircleShape
    
    // Status and badge shapes
    val badge = RoundedCornerShape(4.dp)
    val statusIndicator = CircleShape
    val progressIndicator = CircleShape
    
    // Divider and border shapes
    val divider = RoundedCornerShape(0.dp)
    val border = RoundedCornerShape(0.dp)
    
    // Special shapes
    val tooltip = RoundedCornerShape(4.dp)
    val snackbar = RoundedCornerShape(8.dp)
    val toast = RoundedCornerShape(8.dp)
}

// ============================================================================
// CUSTOM SHAPE UTILITIES
// ============================================================================

/**
 * Create a rounded corner shape with custom corner sizes
 */
fun customRoundedCornerShape(
    topLeft: CornerSize = CornerSize(12.dp),
    topRight: CornerSize = CornerSize(12.dp),
    bottomRight: CornerSize = CornerSize(12.dp),
    bottomLeft: CornerSize = CornerSize(12.dp)
) = RoundedCornerShape(topLeft, topRight, bottomRight, bottomLeft)

/**
 * Create a shape with different corner radii for top and bottom
 */
fun topBottomRoundedShape(
    topRadius: CornerSize = CornerSize(16.dp),
    bottomRadius: CornerSize = CornerSize(0.dp)
) = RoundedCornerShape(topRadius, topRadius, bottomRadius, bottomRadius)

/**
 * Create a pill shape (fully rounded)
 */
fun pillShape() = RoundedCornerShape(50.dp)

/**
 * Create a squared shape (no rounding)
 */
fun squaredShape() = RoundedCornerShape(0.dp)

// ============================================================================
// SHAPE EXTENSIONS
// ============================================================================

/**
 * Extension function to create a shape with scaled corner radius
 */
fun CornerSize.scale(factor: Float): CornerSize {
    return CornerSize(this.value * factor)
}

/**
 * Extension function to create a rounded corner shape from a single dp value
 */
fun Int.dp.toRoundedCornerShape(): RoundedCornerShape {
    return RoundedCornerShape(this.dp)
}

/**
 * Extension function to create a rounded corner shape from a dp value
 */
fun Float.dp.toRoundedCornerShape(): RoundedCornerShape {
    return RoundedCornerShape(this.dp)
}