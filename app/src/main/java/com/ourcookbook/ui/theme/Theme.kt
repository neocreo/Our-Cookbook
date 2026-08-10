package com.ourcookbook.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Cookbook Theme System - Material Design 3 Implementation
 * 
 * This file contains the complete theme system for the Our Cookbook Android app,
 * following the design tokens specified in project-docs/cookbook-ux-foundation.md
 * and the architecture requirements in project-docs/cookbook-android-architecture.md.
 * 
 * Features:
 * - Complete Material Design 3 color schemes (light/dark)
 * - Custom food-inspired color palette
 * - Dynamic color support for Android 12+
 * - Theme switching with system preference integration
 * - Window styling for immersive experience
 */

// ============================================================================
// COLOR PALETTE - Food-Inspired Design Tokens
// ============================================================================

/**
 * Cookbook Color Palette
 * Food-inspired color system following Material Design 3 guidelines
 * Based on design tokens from project-docs/cookbook-ux-foundation.md
 */
object CookbookColors {
    // Primary colors - Soft red (tomatoes, peppers)
    val primary: Color = Color(0xFFE57373)
    val primaryVariant: Color = Color(0xFFC62828)  // Deep red
    val primaryLight: Color = Color(0xFFFFCDD2)  // Light red
    val primaryDark: Color = Color(0xFFB71C1C)    // Deep tomato red
    val primaryContainer: Color = Color(0xFFFFDAD6)
    val onPrimary: Color = Color(0xFFFFFFFF)
    val onPrimaryContainer: Color = Color(0xFF410002)

    // Secondary colors - Earth tones (herbs)
    val secondary: Color = Color(0xFF81C784)     // Soft green
    val secondaryVariant: Color = Color(0xFF388E3C) // Deep green
    val secondaryLight: Color = Color(0xFFC8E6C9) // Light green
    val secondaryDark: Color = Color(0xFF4CAF50)   // Fresh green
    val secondaryContainer: Color = Color(0xFFC8E6C9)
    val onSecondary: Color = Color(0xFFFFFFFF)
    val onSecondaryContainer: Color = Color(0xFF003800)

    // Tertiary colors - Warm accents
    val tertiary: Color = Color(0xFFFFC107)      // Amber (morning)
    val tertiaryVariant: Color = Color(0xFFFF9800) // Orange (spicy)
    val tertiaryLight: Color = Color(0xFFFFECB3) // Light amber
    val tertiaryDark: Color = Color(0xFFFF8C00)   // Warning orange
    val tertiaryContainer: Color = Color(0xFFFFDBCB)
    val onTertiary: Color = Color(0xFF3E1A00)
    val onTertiaryContainer: Color = Color(0xFFFFFFFF)

    // Surface colors
    val surfaceLight: Color = Color(0xFFFFFFFF)     // White
    val surfaceVariantLight: Color = Color(0xFFF5F5F5) // Light gray
    val backgroundLight: Color = Color(0xFFF5F5F5)   // Light background
    val onSurfaceLight: Color = Color(0xFF212121)   // Dark text
    val onBackgroundLight: Color = Color(0xFF212121) // Dark text

    // Dark theme surface colors
    val surfaceDark: Color = Color(0xFF121212)       // Dark surface
    val surfaceVariantDark: Color = Color(0xFF1E1E1E) // Darker surface
    val backgroundDark: Color = Color(0xFF121212)    // Dark background
    val onSurfaceDark: Color = Color(0xFFFFFFFF)    // Light text
    val onBackgroundDark: Color = Color(0xFFFFFFFF) // Light text

    // Status colors
    val success: Color = Color(0xFF4CAF50)     // Success green
    val warning: Color = Color(0xFFFFC107)     // Warning amber
    val error: Color = Color(0xFFF44336)       // Error red
    val info: Color = Color(0xFF2196F3)        // Info blue

    // Category colors (semantic colors for recipe categories)
    val categoryColors: Map<String, Color> = mapOf(
        "Breakfasts" to Color(0xFFFFC107),      // Amber (morning)
        "Mains" to Color(0xFFE57373),           // Red (main dishes)
        "Desserts & Snacks" to Color(0xFFE91E63), // Pink (sweet)
        "Sides" to Color(0xFF81C784),           // Green (fresh)
        "Sauces and Spices" to Color(0xFFFF9800), // Orange (spicy)
        "Breakfast" to Color(0xFFFFC107),       // Amber
        "Main" to Color(0xFFE57373),            // Red
        "Desserts" to Color(0xFFE91E63),        // Pink
        "Snacks" to Color(0xFFFF9800),         // Orange
        "Side" to Color(0xFF81C784),            // Green
        "Sauce" to Color(0xFFFF9800)           // Orange
    )

    // Additional semantic colors
    val cream: Color = Color(0xFFFFF9C4)        // Light cream for backgrounds
    val warmBrown: Color = Color(0xFF795548)    // Warm brown for accents
    val goldenYellow: Color = Color(0xFFFFC107) // Golden yellow for highlights
    val deepPurple: Color = Color(0xFF673AB7)   // Deep purple for secondary accents

    // Semantic colors for better readability
    val successGreen: Color = Color(0xFF006400)
    val warningOrange: Color = Color(0xFFFF8C00)
    val infoBlue: Color = Color(0xFF0288D1)
}

// ============================================================================
// LIGHT COLOR SCHEME
// ============================================================================

/**
 * Light Color Scheme for Cookbook App
 * Follows Material Design 3 specifications
 */
val LightColorScheme: ColorScheme = lightColorScheme(
    // Primary colors
    primary = CookbookColors.primary,
    primaryContainer = CookbookColors.primaryContainer,
    onPrimary = CookbookColors.onPrimary,
    onPrimaryContainer = CookbookColors.onPrimaryContainer,
    
    // Secondary colors
    secondary = CookbookColors.secondary,
    secondaryContainer = CookbookColors.secondaryContainer,
    onSecondary = CookbookColors.onSecondary,
    onSecondaryContainer = CookbookColors.onSecondaryContainer,
    
    // Tertiary colors
    tertiary = CookbookColors.tertiary,
    tertiaryContainer = CookbookColors.tertiaryContainer,
    onTertiary = CookbookColors.onTertiary,
    onTertiaryContainer = CookbookColors.onTertiaryContainer,
    
    // Surface colors
    surface = CookbookColors.surfaceLight,
    surfaceVariant = CookbookColors.surfaceVariantLight,
    onSurface = CookbookColors.onSurfaceLight,
    onSurfaceVariant = Color(0xFF49454F),
    
    // Background colors
    background = CookbookColors.backgroundLight,
    onBackground = CookbookColors.onBackgroundLight,
    
    // Error colors
    error = CookbookColors.error,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    
    // Outline colors
    outline = Color(0xFF757575),
    outlineVariant = Color(0xFFC2C2C2),
    
    // Other colors
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFF313033),
    inverseOnSurface = Color(0xFFF5EFF4),
    inversePrimary = Color(0xFFE6B7B8),
    
    // Surface tint for theming
    surfaceTint = CookbookColors.primary
)

// ============================================================================
// DARK COLOR SCHEME
// ============================================================================

/**
 * Dark Color Scheme for Cookbook App
 * Follows Material Design 3 specifications with elevated surfaces
 */
val DarkColorScheme: ColorScheme = darkColorScheme(
    // Primary colors - Adjusted for dark theme
    primary = CookbookColors.primary,
    primaryContainer = CookbookColors.primaryLight,
    onPrimary = CookbookColors.onPrimary,
    onPrimaryContainer = CookbookColors.primary,
    
    // Secondary colors
    secondary = CookbookColors.secondary,
    secondaryContainer = CookbookColors.secondaryLight,
    onSecondary = CookbookColors.onSecondary,
    onSecondaryContainer = CookbookColors.secondary,
    
    // Tertiary colors
    tertiary = CookbookColors.tertiary,
    tertiaryContainer = CookbookColors.tertiaryLight,
    onTertiary = CookbookColors.onTertiary,
    onTertiaryContainer = CookbookColors.tertiary,
    
    // Surface colors - Elevated surfaces in dark theme
    surface = CookbookColors.surfaceDark,
    surfaceVariant = CookbookColors.surfaceVariantDark,
    onSurface = CookbookColors.onSurfaceDark,
    onSurfaceVariant = Color(0xFFCAC4D0),
    
    // Background colors
    background = CookbookColors.backgroundDark,
    onBackground = CookbookColors.onBackgroundDark,
    
    // Error colors
    error = CookbookColors.error,
    onError = Color.White,
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFB4AB),
    
    // Outline colors
    outline = Color(0xFF9E9E9E),
    outlineVariant = Color(0xFF424242),
    
    // Other colors
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFFE6E1E5),
    inverseOnSurface = Color(0xFF313033),
    inversePrimary = Color(0xFF8F0000),
    
    // Surface tint for theming
    surfaceTint = CookbookColors.primary
)

// ============================================================================
// MAIN THEME COMPOSABLE
// ============================================================================

/**
 * Main Cookbook Theme Composable
 * 
 * Applies the Material Design 3 theme with:
 * - Color scheme (light/dark based on system preference)
 * - Typography system
 * - Shape system
 * - Window styling for immersive experience
 * 
 * @param darkTheme Whether to use dark theme. Defaults to system preference.
 * @param dynamicColor Whether to use dynamic colors on Android 12+. Defaults to true.
 * @param content The content to be themed.
 */
@Composable
fun CookbookTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    // Use dynamic colors on Android 12+ if enabled
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Apply window styling for immersive experience
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Update window background color to match surface color
            window.statusBarColor = colorScheme.surface.toArgb()
            // Update status bar icons based on theme
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            // Update navigation bar color
            window.navigationBarColor = colorScheme.surface.toArgb()
            // Update navigation bar icons based on theme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    // Apply Material Theme with all design tokens
    MaterialTheme(
        colorScheme = colorScheme,
        typography = CookbookTypography,
        shapes = CookbookShapes,
        content = content
    )
}

// ============================================================================
// THEME UTILITIES
// ============================================================================

/**
 * Get the current color scheme from MaterialTheme
 */
@Composable
fun currentColorScheme(): ColorScheme {
    return MaterialTheme.colorScheme
}

/**
 * Get the current typography from MaterialTheme
 */
@Composable
fun currentTypography() = MaterialTheme.typography

/**
 * Get the current shapes from MaterialTheme
 */
@Composable
fun currentShapes() = MaterialTheme.shapes

/**
 * Check if dark theme is currently active
 */
@Composable
fun isDarkTheme(): Boolean {
    return MaterialTheme.colorScheme.surface == DarkColorScheme.surface
}

/**
 * Get category color based on category name
 */
fun getCategoryColor(category: String): Color {
    return CookbookColors.categoryColors[category] ?: CookbookColors.primary
}

/**
 * Get status color based on status type
 */
fun getStatusColor(status: String): Color {
    return when (status.lowercase()) {
        "success" -> CookbookColors.success
        "warning" -> CookbookColors.warning
        "error" -> CookbookColors.error
        "info" -> CookbookColors.info
        else -> CookbookColors.primary
    }
}