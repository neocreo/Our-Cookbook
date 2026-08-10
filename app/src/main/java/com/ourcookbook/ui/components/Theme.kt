package com.ourcookbook.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Cookbook color palette following the design system
 */
object CookbookColors {
    // Primary colors - Food inspired
    val primary: Color = Color(0xFFE57373)      // Soft red (tomatoes, peppers)
    val primaryVariant: Color = Color(0xFFC62828) // Deep red
    val primaryLight: Color = Color(0xFFFFCDD2) // Light red
    
    // Secondary colors - Earth tones
    val secondary: Color = Color(0xFF81C784)    // Soft green (herbs)
    val secondaryVariant: Color = Color(0xFF388E3C) // Deep green
    val secondaryLight: Color = Color(0xFFC8E6C9) // Light green
    
    // Surface colors
    val surface: Color = Color(0xFFFFFFFF)     // White
    val surfaceVariant: Color = Color(0xFFF5F5F5) // Light gray
    val background: Color = Color(0xFFF5F5F5)   // Light background
    
    // Text colors
    val onPrimary: Color = Color(0xFFFFFFFF)   // White text on primary
    val onSecondary: Color = Color(0xFFFFFFFF) // White text on secondary
    val onSurface: Color = Color(0xFF212121)   // Dark text on surface
    val onBackground: Color = Color(0xFF212121) // Dark text on background
    
    // Status colors
    val success: Color = Color(0xFF4CAF50)     // Success green
    val warning: Color = Color(0xFFFFC107)     // Warning amber
    val error: Color = Color(0xFFF44336)       // Error red
    val info: Color = Color(0xFF2196F3)        // Info blue
    
    // Category colors
    val categoryColors: Map<String, Color> = mapOf(
        "Breakfasts" to Color(0xFFFFC107),   // Amber (morning)
        "Mains" to Color(0xFFE57373),       // Red (main dishes)
        "Desserts & Snacks" to Color(0xFFE91E63),    // Pink (sweet)
        "Sides" to Color(0xFF81C784),       // Green (fresh)
        "Sauces and Spices" to Color(0xFFFF9800),      // Orange (spicy)
        "Desserts" to Color(0xFFE91E63),    // Pink (sweet)
        "Snacks" to Color(0xFFFF9800),      // Orange
        "Breakfast" to Color(0xFFFFC107),   // Amber
        "Main" to Color(0xFFE57373),       // Red
        "Side" to Color(0xFF81C784),       // Green
        "Sauce" to Color(0xFFFF9800)      // Orange
    )
}

/**
 * Light color scheme for the app
 */
val LightColorScheme: ColorScheme = lightColorScheme(
    primary = CookbookColors.primary,
    primaryContainer = CookbookColors.primaryLight,
    onPrimary = CookbookColors.onPrimary,
    secondary = CookbookColors.secondary,
    secondaryContainer = CookbookColors.secondaryLight,
    onSecondary = CookbookColors.onSecondary,
    surface = CookbookColors.surface,
    surfaceVariant = CookbookColors.surfaceVariant,
    onSurface = CookbookColors.onSurface,
    background = CookbookColors.background,
    onBackground = CookbookColors.onBackground,
    error = CookbookColors.error,
    onError = Color.White,
    outline = Color(0xFF757575),
    outlineVariant = Color(0xFFC2C2C2)
)

/**
 * Dark color scheme for the app
 */
val DarkColorScheme: ColorScheme = darkColorScheme(
    primary = CookbookColors.primary,
    primaryContainer = CookbookColors.primaryLight,
    onPrimary = CookbookColors.onPrimary,
    secondary = CookbookColors.secondary,
    secondaryContainer = CookbookColors.secondaryLight,
    onSecondary = CookbookColors.onSecondary,
    surface = Color(0xFF121212),
    surfaceVariant = Color(0xFF1E1E1E),
    onSurface = Color(0xFFFFFFFF),
    background = Color(0xFF121212),
    onBackground = Color(0xFFFFFFFF),
    error = CookbookColors.error,
    onError = Color.White,
    outline = Color(0xFF9E9E9E),
    outlineVariant = Color(0xFF424242)
)

/**
 * Main theme for the Cookbook app
 */
@Composable
fun CookbookTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = CookbookTypography,
        content = content
    )
}
