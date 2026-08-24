package com.ourcookbook.ui.theme

import androidx.compose.runtime.Composable

/**
 * Cookbook Theme System Index
 * 
 * This file serves as an index to all theme-related components in the Cookbook app.
 * Import this file to access the complete theme system.
 */

// Re-export all theme components for easy access

// Theme and Color System
val CookbookTheme: @Composable (darkTheme: Boolean, dynamicColor: Boolean, content: @Composable () -> Unit) -> Unit 
    get() = com.ourcookbook.ui.theme.CookbookTheme

val LightColorScheme: androidx.compose.material3.ColorScheme 
    get() = com.ourcookbook.ui.theme.LightColorScheme

val DarkColorScheme: androidx.compose.material3.ColorScheme 
    get() = com.ourcookbook.ui.theme.DarkColorScheme

val CookbookColors 
    get() = com.ourcookbook.ui.theme.CookbookColors

// Typography System
val CookbookTypography: androidx.compose.material3.Typography 
    get() = com.ourcookbook.ui.theme.CookbookTypography

val RobotoFamily: androidx.compose.ui.text.font.FontFamily 
    get() = com.ourcookbook.ui.theme.RobotoFamily

val MonospaceFamily: androidx.compose.ui.text.font.FontFamily 
    get() = com.ourcookbook.ui.theme.MonospaceFamily

val CookbookTextStyles 
    get() = com.ourcookbook.ui.theme.CookbookTextStyles

val LegacyCookbookTypography: androidx.compose.material3.Typography 
    get() = com.ourcookbook.ui.theme.LegacyCookbookTypography

// Shape System
val CookbookShapes: androidx.compose.material3.Shapes 
    get() = com.ourcookbook.ui.theme.CookbookShapes

val ShapeTokens 
    get() = com.ourcookbook.ui.theme.ShapeTokens

val ComponentShapes 
    get() = com.ourcookbook.ui.theme.ComponentShapes

// Spacing System
val CookbookSpacing 
    get() = com.ourcookbook.ui.theme.CookbookSpacing

val SpacingTokens 
    get() = com.ourcookbook.ui.theme.SpacingTokens

val ScreenSpacing 
    get() = com.ourcookbook.ui.theme.ScreenSpacing

val ComponentSpacing 
    get() = com.ourcookbook.ui.theme.ComponentSpacing

val LayoutSpacing 
    get() = com.ourcookbook.ui.theme.LayoutSpacing

val RecipeSpacing 
    get() = com.ourcookbook.ui.theme.RecipeSpacing

// Elevation System
val CookbookElevation 
    get() = com.ourcookbook.ui.theme.CookbookElevation

val ElevationTokens 
    get() = com.ourcookbook.ui.theme.ElevationTokens

val ComponentElevation 
    get() = com.ourcookbook.ui.theme.ComponentElevation

val ElevationStates 
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
val ShadowUtils 
    get() = com.ourcookbook.ui.theme.ShadowUtils

val OverlayUtils 
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
