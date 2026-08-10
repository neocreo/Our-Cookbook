package com.ourcookbook.ui.components

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp

/**
 * Cookbook typography system following Material Design 3
 */
val CookbookTypography = androidx.compose.material3.Typography(
    // Default font family - using system fonts as fallback
    // In production, you would use custom fonts loaded from resources
    defaultFontFamily = FontFamily.Default,
    
    // Headings
    displayLarge = TextStyle(
        fontSize = 57.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontSize = 45.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontSize = 36.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),
    
    // Headlines
    headlineLarge = TextStyle(
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    
    // Titles
    titleLarge = TextStyle(
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 24.sp,
        letterSpacing = 0.1.sp
    ),
    titleSmall = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    
    // Body text
    bodyLarge = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    
    // Labels
    labelLarge = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 20.sp,
        letterSpacing = 1.25.sp
    ),
    labelMedium = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 16.sp,
        letterSpacing = 1.25.sp
    ),
    labelSmall = TextStyle(
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 16.sp,
        letterSpacing = 1.5.sp
    )
)

/**
 * Spacing system for consistent layout
 */
object CookbookSpacing {
    // Base unit: 4dp
    val xxSmall: androidx.compose.ui.unit.Dp = 4.dp
    val xSmall: androidx.compose.ui.unit.Dp = 8.dp
    val small: androidx.compose.ui.unit.Dp = 12.dp
    val medium: androidx.compose.ui.unit.Dp = 16.dp
    val large: androidx.compose.ui.unit.Dp = 24.dp
    val xLarge: androidx.compose.ui.unit.Dp = 32.dp
    val xxLarge: androidx.compose.ui.unit.Dp = 48.dp
    val xxxLarge: androidx.compose.ui.unit.Dp = 64.dp
    
    // Special spacing
    val touchTarget: androidx.compose.ui.unit.Dp = 48.dp  // Minimum touch target size
    val cardElevation: androidx.compose.ui.unit.Dp = 4.dp
    val dividerHeight: androidx.compose.ui.unit.Dp = 1.dp
    val borderWidth: androidx.compose.ui.unit.Dp = 1.dp
    
    // Screen margins
    val screenMargin: androidx.compose.ui.unit.Dp = 16.dp
    val screenMarginLarge: androidx.compose.ui.unit.Dp = 24.dp
    
    // Component spacing
    val buttonPadding = androidx.compose.foundation.layout.PaddingValues(
        horizontal = large,
        vertical = medium
    )
    val cardPadding = androidx.compose.foundation.layout.PaddingValues(all = medium)
    val listItemPadding = androidx.compose.foundation.layout.PaddingValues(
        horizontal = medium,
        vertical = small
    )
}

/**
 * Elevation system for depth and hierarchy
 */
object CookbookElevation {
    val none: androidx.compose.ui.unit.Dp = 0.dp
    val small: androidx.compose.ui.unit.Dp = 2.dp
    val medium: androidx.compose.ui.unit.Dp = 4.dp
    val large: androidx.compose.ui.unit.Dp = 8.dp
    val xLarge: androidx.compose.ui.unit.Dp = 12.dp
    
    // Component elevations
    val card: androidx.compose.ui.unit.Dp = medium
    val dialog: androidx.compose.ui.unit.Dp = large
    val bottomBar: androidx.compose.ui.unit.Dp = large
    val fab: androidx.compose.ui.unit.Dp = large
    val button: androidx.compose.ui.unit.Dp = small
    val snackbar: androidx.compose.ui.unit.Dp = large
}
