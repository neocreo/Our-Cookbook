package com.ourcookbook.ui.theme

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Cookbook Spacing System
 * 
 * Complete spacing implementation following the design tokens
 * specified in project-docs/cookbook-ux-foundation.md.
 * 
 * Features:
 * - Consistent 4dp base unit spacing scale
 * - Semantic spacing names for different use cases
 * - Component-specific spacing presets
 * - Screen layout spacing utilities
 */

// ============================================================================
// BASE SPACING SYSTEM
// ============================================================================

/**
 * Cookbook Spacing
 * 
 * Main spacing system with 4dp base unit following Material Design guidelines.
 * All spacing values are multiples of 4dp for consistency.
 */
object CookbookSpacing {
    // Base unit: 4dp
    val xxSmall: Dp = 4.dp    // 4dp - Tight spacing
    val xSmall: Dp = 8.dp     // 8dp - Compact spacing
    val small: Dp = 12.dp     // 12dp - Small spacing
    val medium: Dp = 16.dp    // 16dp - Default spacing
    val large: Dp = 24.dp     // 24dp - Generous spacing
    val xLarge: Dp = 32.dp    // 32dp - Large spacing
    val xxLarge: Dp = 48.dp   // 48dp - Extra large spacing
    val xxxLarge: Dp = 64.dp  // 64dp - Major section spacing

    // Special spacing values
    val touchTarget: Dp = 48.dp  // Minimum touch target size (Material Design spec)
    val cardElevation: Dp = 4.dp  // Default card elevation
    val dividerHeight: Dp = 1.dp  // Divider line height
    val borderWidth: Dp = 1.dp   // Border stroke width
}

// ============================================================================
// SPACING TOKENS
// ============================================================================

/**
 * Spacing tokens for consistent usage across the app
 */
object SpacingTokens {
    // Base tokens
    val base = 4.dp
    val xs = 8.dp
    val sm = 12.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp
    val xxl = 48.dp
    val xxxl = 64.dp

    // Semantic tokens
    val micro = 2.dp
    val tiny = 4.dp
    val compact = 8.dp
    val standard = 16.dp
    val spacious = 24.dp
    val generous = 32.dp
    val extensive = 48.dp
    val massive = 64.dp
}

// ============================================================================
// SCREEN SPACING
// ============================================================================

/**
 * Screen-level spacing utilities for consistent layout margins and padding
 */
object ScreenSpacing {
    // Screen margins (outer spacing)
    val marginNone: Dp = 0.dp
    val marginSmall: Dp = CookbookSpacing.small
    val marginMedium: Dp = CookbookSpacing.medium
    val marginLarge: Dp = CookbookSpacing.large

    // Screen padding (inner spacing)
    val paddingNone: Dp = 0.dp
    val paddingSmall: Dp = CookbookSpacing.small
    val paddingMedium: Dp = CookbookSpacing.medium
    val paddingLarge: Dp = CookbookSpacing.large

    // Combined screen spacing
    val screenMargin: Dp = CookbookSpacing.medium
    val screenMarginLarge: Dp = CookbookSpacing.large
    val screenPadding: Dp = CookbookSpacing.medium
    val screenPaddingLarge: Dp = CookbookSpacing.large

    // Safe area insets
    val safeAreaHorizontal: Dp = CookbookSpacing.medium
    val safeAreaVertical: Dp = CookbookSpacing.medium
}

// ============================================================================
// COMPONENT SPACING
// ============================================================================

/**
 * Component-specific spacing presets for consistent layout
 */
object ComponentSpacing {
    
    // Button spacing
    val buttonPadding: PaddingValues = PaddingValues(
        horizontal = CookbookSpacing.large,
        vertical = CookbookSpacing.medium
    )
    val buttonPaddingSmall: PaddingValues = PaddingValues(
        horizontal = CookbookSpacing.medium,
        vertical = CookbookSpacing.small
    )
    val buttonPaddingIcon: PaddingValues = PaddingValues(
        all = CookbookSpacing.small
    )
    val buttonIconSpacing: Dp = CookbookSpacing.small
    
    // Card spacing
    val cardPadding: PaddingValues = PaddingValues(
        all = CookbookSpacing.medium
    )
    val cardPaddingSmall: PaddingValues = PaddingValues(
        all = CookbookSpacing.small
    )
    val cardContentSpacing: Dp = CookbookSpacing.small
    
    // Input field spacing
    val textFieldPadding: PaddingValues = PaddingValues(
        horizontal = CookbookSpacing.medium,
        vertical = CookbookSpacing.small
    )
    val textFieldIconPadding: Dp = CookbookSpacing.small
    val textFieldLabelPadding: Dp = CookbookSpacing.xSmall
    
    // List item spacing
    val listItemPadding: PaddingValues = PaddingValues(
        horizontal = CookbookSpacing.medium,
        vertical = CookbookSpacing.small
    )
    val listItemSpacing: Dp = CookbookSpacing.xSmall
    
    // Image spacing
    val imagePadding: PaddingValues = PaddingValues(
        all = CookbookSpacing.small
    )
    val imageCaptionSpacing: Dp = CookbookSpacing.xSmall
    
    // Dialog spacing
    val dialogPadding: PaddingValues = PaddingValues(
        all = CookbookSpacing.large
    )
    val dialogButtonSpacing: Dp = CookbookSpacing.medium
    
    // Navigation spacing
    val navigationPadding: PaddingValues = PaddingValues(
        horizontal = CookbookSpacing.medium,
        vertical = CookbookSpacing.small
    )
    val navigationItemSpacing: Dp = CookbookSpacing.small
    
    // Chip spacing
    val chipPadding: PaddingValues = PaddingValues(
        horizontal = CookbookSpacing.small,
        vertical = CookbookSpacing.xSmall
    )
    val chipSpacing: Dp = CookbookSpacing.xSmall
    
    // Badge spacing
    val badgePadding: PaddingValues = PaddingValues(
        horizontal = CookbookSpacing.xSmall,
        vertical = CookbookSpacing.xxSmall
    )
    
    // Divider spacing
    val dividerSpacing: Dp = CookbookSpacing.medium
    
    // Section spacing
    val sectionSpacing: Dp = CookbookSpacing.large
    val sectionHeaderSpacing: Dp = CookbookSpacing.medium
}

// ============================================================================
// LAYOUT SPACING
// ============================================================================

/**
 * Layout-specific spacing utilities
 */
object LayoutSpacing {
    
    // Between elements in a row
    val rowSpacingSmall: Dp = CookbookSpacing.small
    val rowSpacingMedium: Dp = CookbookSpacing.medium
    val rowSpacingLarge: Dp = CookbookSpacing.large
    
    // Between elements in a column
    val columnSpacingSmall: Dp = CookbookSpacing.small
    val columnSpacingMedium: Dp = CookbookSpacing.medium
    val columnSpacingLarge: Dp = CookbookSpacing.large
    
    // Grid spacing
    val gridSpacingSmall: Dp = CookbookSpacing.small
    val gridSpacingMedium: Dp = CookbookSpacing.medium
    val gridSpacingLarge: Dp = CookbookSpacing.large
    
    // Lazy list spacing
    val lazyListItemSpacing: Dp = CookbookSpacing.small
    val lazyListContentPadding: PaddingValues = PaddingValues(
        all = CookbookSpacing.medium
    )
    
    // Form spacing
    val formFieldSpacing: Dp = CookbookSpacing.medium
    val formSectionSpacing: Dp = CookbookSpacing.large
    
    // Text spacing
    val textLineSpacing: Dp = CookbookSpacing.xSmall
    val paragraphSpacing: Dp = CookbookSpacing.medium
}

// ============================================================================
// RECIPE-SPECIFIC SPACING
// ============================================================================

/**
 * Recipe-specific spacing for consistent recipe layout
 */
object RecipeSpacing {
    
    // Recipe card spacing
    val recipeCardPadding: PaddingValues = PaddingValues(
        all = CookbookSpacing.medium
    )
    val recipeImageSpacing: Dp = CookbookSpacing.small
    val recipeTitleSpacing: Dp = CookbookSpacing.small
    val recipeMetadataSpacing: Dp = CookbookSpacing.xSmall
    val recipeDescriptionSpacing: Dp = CookbookSpacing.small
    
    // Recipe detail spacing
    val recipeDetailPadding: PaddingValues = PaddingValues(
        all = CookbookSpacing.medium
    )
    val recipeSectionSpacing: Dp = CookbookSpacing.large
    val recipeSectionHeaderSpacing: Dp = CookbookSpacing.medium
    
    // Ingredients spacing
    val ingredientItemSpacing: Dp = CookbookSpacing.xSmall
    val ingredientCheckboxSpacing: Dp = CookbookSpacing.small
    val ingredientDividerspacing: Dp = CookbookSpacing.xSmall
    
    // Instructions spacing
    val instructionStepSpacing: Dp = CookbookSpacing.small
    val instructionCheckboxSpacing: Dp = CookbookSpacing.small
    val instructionStepNumberSpacing: Dp = CookbookSpacing.small
    
    // Recipe metadata spacing
    val metadataItemSpacing: Dp = CookbookSpacing.medium
    val metadataLabelSpacing: Dp = CookbookSpacing.xSmall
}

// ============================================================================
// SPACING UTILITIES
// ============================================================================

/**
 * Spacing utility functions
 */

// Extension property for Int to Dp
val Int.dp: Dp
    get() = this.dp

// Extension property for Float to Dp
val Float.dp: Dp
    get() = this.dp

/**
 * Create a PaddingValues with equal padding on all sides
 */
fun Dp.toPaddingValues(): PaddingValues {
    return PaddingValues(all = this)
}

/**
 * Create a PaddingValues with horizontal and vertical padding
 */
fun paddingValues(horizontal: Dp, vertical: Dp): PaddingValues {
    return PaddingValues(horizontal = horizontal, vertical = vertical)
}

/**
 * Create a PaddingValues with individual padding values
 */
fun paddingValues(
    start: Dp = 0.dp,
    top: Dp = 0.dp,
    end: Dp = 0.dp,
    bottom: Dp = 0.dp
): PaddingValues {
    return PaddingValues(start = start, top = top, end = end, bottom = bottom)
}

/**
 * Scale a Dp value by a factor
 */
fun Dp.scale(factor: Float): Dp {
    return (this.value * factor).dp
}