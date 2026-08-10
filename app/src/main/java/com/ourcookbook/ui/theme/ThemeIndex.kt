package com.ourcookbook.ui.theme

/**
 * Cookbook Theme System Index
 * 
 * This file serves as an index to all theme-related components in the Cookbook app.
 * Import this file to access the complete theme system.
 * 
 * Theme System Components:
 * - Theme.kt: Main theme composable and color schemes
 * - Typography.kt: Typography system and text styles
 * - Shapes.kt: Shape system and component shapes
 * - Spacing.kt: Spacing system and layout utilities
 * - Elevation.kt: Elevation system and depth utilities
 * - ThemePreview.kt: Preview composables for theme visualization
 * 
 * Usage:
 * ```kotlin
 * import com.ourcookbook.ui.theme.*
 * 
 * @Composable
 * fun MyScreen() {
 *     CookbookTheme {
 *         // Your content here
 *     }
 * }
 * ```
 */

// Re-export all theme components for easy access

// Theme and Color System
val CookbookTheme: @Composable (darkTheme: Boolean, dynamicColor: Boolean, content: @Composable () -> Unit) -> Unit 
    get() = com.ourcookbook.ui.theme.CookbookTheme

val LightColorScheme: androidx.compose.material3.ColorScheme 
    get() = com.ourcookbook.ui.theme.LightColorScheme

val DarkColorScheme: androidx.compose.material3.ColorScheme 
    get() = com.ourcookbook.ui.theme.DarkColorScheme

object CookbookColors 
    get() = com.ourcookbook.ui.theme.CookbookColors

// Typography System
val CookbookTypography: androidx.compose.material3.Typography 
    get() = com.ourcookbook.ui.theme.CookbookTypography

val RobotoFamily: androidx.compose.ui.text.font.FontFamily 
    get() = com.ourcookbook.ui.theme.RobotoFamily

val MonospaceFamily: androidx.compose.ui.text.font.FontFamily 
    get() = com.ourcookbook.ui.theme.MonospaceFamily

object CookbookTextStyles 
    get() = com.ourcookbook.ui.theme.CookbookTextStyles

val LegacyCookbookTypography: androidx.compose.material3.Typography 
    get() = com.ourcookbook.ui.theme.LegacyCookbookTypography

// Shape System
val CookbookShapes: androidx.compose.material3.Shapes 
    get() = com.ourcookbook.ui.theme.CookbookShapes

object ShapeTokens 
    get() = com.ourcookbook.ui.theme.ShapeTokens

object ComponentShapes 
    get() = com.ourcookbook.ui.theme.ComponentShapes

// Spacing System
object CookbookSpacing 
    get() = com.ourcookbook.ui.theme.CookbookSpacing

object SpacingTokens 
    get() = com.ourcookbook.ui.theme.SpacingTokens

object ScreenSpacing 
    get() = com.ourcookbook.ui.theme.ScreenSpacing

object ComponentSpacing 
    get() = com.ourcookbook.ui.theme.ComponentSpacing

object LayoutSpacing 
    get() = com.ourcookbook.ui.theme.LayoutSpacing

object RecipeSpacing 
    get() = com.ourcookbook.ui.theme.RecipeSpacing

// Elevation System
object CookbookElevation 
    get() = com.ourcookbook.ui.theme.CookbookElevation

object ElevationTokens 
    get() = com.ourcookbook.ui.theme.ElevationTokens

object ComponentElevation 
    get() = com.ourcookbook.ui.theme.ComponentElevation

object ElevationStates 
    get() = com.ourcookbook.ui.theme.ElevationStates

// Data classes
data class ButtonElevation(
    val default: androidx.compose.ui.unit.Dp,
    val pressed: androidx.compose.ui.unit.Dp,
    val hover: androidx.compose.ui.unit.Dp,
    val focused: androidx.compose.ui.unit.Dp,
    val disabled: androidx.compose.ui.unit.Dp
)

data class CardElevation(
    val default: androidx.compose.ui.unit.Dp,
    val pressed: androidx.compose.ui.unit.Dp,
    val hover: androidx.compose.ui.unit.Dp,
    val focused: androidx.compose.ui.unit.Dp
)

data class FabElevation(
    val default: androidx.compose.ui.unit.Dp,
    val pressed: androidx.compose.ui.unit.Dp,
    val hover: androidx.compose.ui.unit.Dp,
    val focused: androidx.compose.ui.unit.Dp
)

// Utility objects
object ShadowUtils 
    get() = com.ourcookbook.ui.theme.ShadowUtils

object OverlayUtils 
    get() = com.ourcookbook.ui.theme.OverlayUtils

// Utility functions
fun getCategoryColor(category: String): androidx.compose.ui.graphics.Color {
    return com.ourcookbook.ui.theme.getCategoryColor(category)
}

fun getStatusColor(status: String): androidx.compose.ui.graphics.Color {
    return com.ourcookbook.ui.theme.getStatusColor(status)
}

@Composable
fun currentColorScheme(): androidx.compose.material3.ColorScheme {
    return com.ourcookbook.ui.theme.currentColorScheme()
}

@Composable
fun currentTypography() = com.ourcookbook.ui.theme.currentTypography()

@Composable
fun currentShapes() = com.ourcookbook.ui.theme.currentShapes()

@Composable
fun isDarkTheme(): Boolean {
    return com.ourcookbook.ui.theme.isDarkTheme()
}

// Type aliases for better readability
typealias Dp = androidx.compose.ui.unit.Dp