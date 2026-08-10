package com.ourcookbook.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFE57373),
    primaryContainer = Color(0xFFFFCDD2),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFF81C784),
    secondaryContainer = Color(0xFFC8E6C9),
    onSecondary = Color(0xFFFFFFFF),
    surface = Color(0xFF121212),
    surfaceVariant = Color(0xFF1E1E1E),
    onSurface = Color(0xFFFFFFFF),
    background = Color(0xFF121212),
    onBackground = Color(0xFFFFFFFF),
    error = Color(0xFFF44336),
    onError = Color(0xFFFFFFFF),
    outline = Color(0xFF9E9E9E),
    outlineVariant = Color(0xFF424242)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFE57373),
    primaryContainer = Color(0xFFFFCDD2),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFF81C784),
    secondaryContainer = Color(0xFFC8E6C9),
    onSecondary = Color(0xFFFFFFFF),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFF5F5F5),
    onSurface = Color(0xFF212121),
    background = Color(0xFFF5F5F5),
    onBackground = Color(0xFF212121),
    error = Color(0xFFF44336),
    onError = Color(0xFFFFFFFF),
    outline = Color(0xFF757575),
    outlineVariant = Color(0xFFC2C2C2)
)

// Category colors
val CategoryColors = mapOf(
    "Breakfasts" to Color(0xFFFFC107),
    "Mains" to Color(0xFFE57373),
    "Desserts & Snacks" to Color(0xFFE91E63),
    "Sides" to Color(0xFF81C784),
    "Sauces and Spices" to Color(0xFFFF9800)
)

@Composable
fun CookbookTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = CookbookTypography,
        content = content
    )
}