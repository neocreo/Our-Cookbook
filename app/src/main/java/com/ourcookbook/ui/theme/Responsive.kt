package com.ourcookbook.ui.theme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.window.layout.DisplayFeature
import androidx.window.layout.FoldingFeature
import com.ourcookbook.ui.components.ResponsiveAppBar
import com.ourcookbook.ui.components.ResponsiveNavigation
import com.ourcookbook.ui.components.ResponsiveNavigationType

/**
 * Responsive Design System for Cookbook App
 * Task 2.2.10: Responsive design for tablets
 *
 * Provides utilities for creating responsive layouts that adapt to different screen sizes
 * and device configurations (phones, tablets, foldables).
 */

// ============================================================================
// DEVICE SIZE DEFINITIONS
// ============================================================================

/**
 * Screen size categories based on width in dp
 */
enum class ScreenSize {
    COMPACT,      // < 600dp (phones in portrait)
    MEDIUM,      // 600dp - 840dp (phones in landscape, small tablets)
    EXPANDED,    // > 840dp (tablets, desktop)
    ;
    
    companion object {
        /**
         * Get screen size based on width in dp
         */
        fun fromWidth(widthDp: Int): ScreenSize {
            return when {
                widthDp < 600 -> COMPACT
                widthDp < 840 -> MEDIUM
                else -> EXPANDED
            }
        }
        
        /**
         * Get screen size based on configuration
         */
        @Composable
        fun current(): ScreenSize {
            val configuration = LocalConfiguration.current
            val screenWidthDp = configuration.screenWidthDp
            return fromWidth(screenWidthDp)
        }
    }
}

/**
 * Device type based on screen size and configuration
 */
enum class DeviceType {
    PHONE,       // Compact screen, typically portrait
    PHONE_LANDSCAPE, // Medium screen in landscape
    TABLET,      // Expanded screen, typically tablet
    DESKTOP,     // Very large screen
    FOLDABLE,    // Device with folding display
    ;
    
    companion object {
        /**
         * Determine device type based on screen configuration
         */
        @Composable
        fun current(): DeviceType {
            val configuration = LocalConfiguration.current
            val screenWidthDp = configuration.screenWidthDp
            val screenHeightDp = configuration.screenHeightDp
            val orientation = configuration.orientation
            
            // Check for foldable devices
            // This would be enhanced with WindowManager API in production
            
            return when {
                screenWidthDp >= 1200 -> DESKTOP
                screenWidthDp >= 840 -> TABLET
                orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE && screenWidthDp >= 600 -> PHONE_LANDSCAPE
                else -> PHONE
            }
        }
    }
}

// ============================================================================
// BREAKPOINT SYSTEM
// ============================================================================

/**
 * Breakpoint definitions for responsive design
 */
object CookbookBreakpoints {
    val compact: Int = 600   // dp
    val medium: Int = 840    // dp
    val expanded: Int = 1200  // dp
    
    /**
     * Check if current screen width is at least the given breakpoint
     */
    @Composable
    fun isAtLeast(breakpoint: Int): Boolean {
        val screenWidthDp = LocalConfiguration.current.screenWidthDp
        return screenWidthDp >= breakpoint
    }
    
    /**
     * Check if current screen width is at most the given breakpoint
     */
    @Composable
    fun isAtMost(breakpoint: Int): Boolean {
        val screenWidthDp = LocalConfiguration.current.screenWidthDp
        return screenWidthDp <= breakpoint
    }
    
    /**
     * Check if current screen is in compact mode
     */
    @Composable
    fun isCompact(): Boolean {
        return isAtMost(compact)
    }
    
    /**
     * Check if current screen is in medium mode
     */
    @Composable
    fun isMedium(): Boolean {
        val screenWidthDp = LocalConfiguration.current.screenWidthDp
        return screenWidthDp in compact..<medium
    }
    
    /**
     * Check if current screen is in expanded mode
     */
    @Composable
    fun isExpanded(): Boolean {
        return isAtLeast(expanded)
    }
}

// ============================================================================
// RESPONSIVE DIMENSIONS
// ============================================================================

/**
 * Responsive dimension system that adapts to screen size
 */
object ResponsiveDimensions {
    
    /**
     * Get responsive padding based on screen size
     */
    @Composable
    fun padding(): PaddingValues {
        return when (ScreenSize.current()) {
            ScreenSize.COMPACT -> CookbookSpacing.small.toPaddingValues()
            ScreenSize.MEDIUM -> CookbookSpacing.medium.toPaddingValues()
            ScreenSize.EXPANDED -> CookbookSpacing.large.toPaddingValues()
        }
    }
    
    /**
     * Get responsive horizontal padding based on screen size
     */
    @Composable
    fun horizontalPadding(): Dp {
        return when (ScreenSize.current()) {
            ScreenSize.COMPACT -> CookbookSpacing.medium
            ScreenSize.MEDIUM -> CookbookSpacing.large
            ScreenSize.EXPANDED -> CookbookSpacing.xLarge
        }
    }
    
    /**
     * Get responsive vertical padding based on screen size
     */
    @Composable
    fun verticalPadding(): Dp {
        return when (ScreenSize.current()) {
            ScreenSize.COMPACT -> CookbookSpacing.small
            ScreenSize.MEDIUM -> CookbookSpacing.medium
            ScreenSize.EXPANDED -> CookbookSpacing.large
        }
    }
    
    /**
     * Get responsive spacing between items
     */
    @Composable
    fun itemSpacing(): Dp {
        return when (ScreenSize.current()) {
            ScreenSize.COMPACT -> CookbookSpacing.small
            ScreenSize.MEDIUM -> CookbookSpacing.medium
            ScreenSize.EXPANDED -> CookbookSpacing.large
        }
    }
    
    /**
     * Get responsive grid column count
     */
    @Composable
    fun gridColumns(): Int {
        return when (ScreenSize.current()) {
            ScreenSize.COMPACT -> 1
            ScreenSize.MEDIUM -> 2
            ScreenSize.EXPANDED -> 3
        }
    }
    
    /**
     * Get responsive max width for content
     */
    @Composable
    fun maxContentWidth(): Dp {
        return when (ScreenSize.current()) {
            ScreenSize.COMPACT -> 1000.dp
            ScreenSize.MEDIUM -> 1200.dp
            ScreenSize.EXPANDED -> 1400.dp
        }
    }
    
    /**
     * Get responsive image size
     */
    @Composable
    fun imageSize(): Dp {
        return when (ScreenSize.current()) {
            ScreenSize.COMPACT -> 200.dp
            ScreenSize.MEDIUM -> 250.dp
            ScreenSize.EXPANDED -> 300.dp
        }
    }
}

// ============================================================================
// RESPONSIVE LAYOUT COMPONENTS
// ============================================================================

/**
 * Responsive container that adapts its layout based on screen size
 */
@Composable
fun ResponsiveContainer(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

/**
 * Responsive row that adapts to column on small screens
 */
@Composable
fun ResponsiveRowColumn(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    spacing: Dp = ResponsiveDimensions.itemSpacing(),
    content: @Composable () -> Unit
) {
    when (ScreenSize.current()) {
        ScreenSize.COMPACT -> {
            Column(
                modifier = modifier,
                verticalArrangement = Arrangement.spacedBy(spacing),
                horizontalAlignment = horizontalArrangement
            ) {
                content()
            }
        }
        else -> {
            Row(
                modifier = modifier,
                horizontalArrangement = horizontalArrangement,
                verticalAlignment = verticalAlignment
            ) {
                content()
            }
        }
    }
}

/**
 * Responsive layout that shows different content based on screen size
 */
@Composable
fun ResponsiveLayout(
    modifier: Modifier = Modifier,
    compactContent: @Composable () -> Unit = {},
    mediumContent: @Composable () -> Unit = compactContent,
    expandedContent: @Composable () -> Unit = mediumContent
) {
    Box(modifier = modifier.fillMaxSize()) {
        when (ScreenSize.current()) {
            ScreenSize.COMPACT -> compactContent()
            ScreenSize.MEDIUM -> mediumContent()
            ScreenSize.EXPANDED -> expandedContent()
        }
    }
}

/**
 * Responsive navigation type based on screen size
 */
@Composable
fun responsiveNavigationType(): ResponsiveNavigationType {
    return when (ScreenSize.current()) {
        ScreenSize.COMPACT -> ResponsiveNavigationType.BOTTOM_NAVIGATION
        ScreenSize.MEDIUM -> ResponsiveNavigationType.BOTTOM_NAVIGATION
        ScreenSize.EXPANDED -> ResponsiveNavigationType.NAVIGATION_RAIL
    }
}

/**
 * Responsive app bar configuration
 */
@Composable
fun responsiveAppBarConfiguration(): ResponsiveAppBar.Configuration {
    return when (ScreenSize.current()) {
        ScreenSize.COMPACT -> ResponsiveAppBar.Configuration(
            showTitle = true,
            showNavigationIcon = true,
            showActions = true
        )
        ScreenSize.MEDIUM -> ResponsiveAppBar.Configuration(
            showTitle = true,
            showNavigationIcon = true,
            showActions = true
        )
        ScreenSize.EXPANDED -> ResponsiveAppBar.Configuration(
            showTitle = true,
            showNavigationIcon = false,
            showActions = true
        )
    }
}

// ============================================================================
// RESPONSIVE TEXT
// ============================================================================

/**
 * Responsive text styles
 */
object ResponsiveTypography {
    
    /**
     * Get responsive text style for titles
     */
    @Composable
    fun title() = when (ScreenSize.current()) {
        ScreenSize.COMPACT -> MaterialTheme.typography.headlineSmall
        ScreenSize.MEDIUM -> MaterialTheme.typography.headlineMedium
        ScreenSize.EXPANDED -> MaterialTheme.typography.headlineLarge
    }
    
    /**
     * Get responsive text style for body text
     */
    @Composable
    fun body() = when (ScreenSize.current()) {
        ScreenSize.COMPACT -> MaterialTheme.typography.bodySmall
        ScreenSize.MEDIUM -> MaterialTheme.typography.bodyMedium
        ScreenSize.EXPANDED -> MaterialTheme.typography.bodyLarge
    }
    
    /**
     * Get responsive text style for captions
     */
    @Composable
    fun caption() = when (ScreenSize.current()) {
        ScreenSize.COMPACT -> MaterialTheme.typography.labelSmall
        ScreenSize.MEDIUM -> MaterialTheme.typography.labelMedium
        ScreenSize.EXPANDED -> MaterialTheme.typography.labelLarge
    }
}

// ============================================================================
// RESPONSIVE GRID
// ============================================================================

/**
 * Responsive grid layout configuration
 */
object ResponsiveGrid {
    
    /**
     * Get responsive column count for recipe grid
     */
    @Composable
    fun recipeColumns(): Int {
        return when (ScreenSize.current()) {
            ScreenSize.COMPACT -> 1
            ScreenSize.MEDIUM -> 2
            ScreenSize.EXPANDED -> 3
        }
    }
    
    /**
     * Get responsive column count for category grid
     */
    @Composable
    fun categoryColumns(): Int {
        return when (ScreenSize.current()) {
            ScreenSize.COMPACT -> 2
            ScreenSize.MEDIUM -> 3
            ScreenSize.EXPANDED -> 4
        }
    }
    
    /**
     * Get responsive spacing between grid items
     */
    @Composable
    fun gridSpacing(): Dp {
        return when (ScreenSize.current()) {
            ScreenSize.COMPACT -> CookbookSpacing.small
            ScreenSize.MEDIUM -> CookbookSpacing.medium
            ScreenSize.EXPANDED -> CookbookSpacing.large
        }
    }
}

// ============================================================================
// RESPONSIVE DIALOG
// ============================================================================

/**
 * Responsive dialog configuration
 */
object ResponsiveDialog {
    
    /**
     * Get responsive dialog width
     */
    @Composable
    fun maxWidth(): Dp {
        return when (ScreenSize.current()) {
            ScreenSize.COMPACT -> 300.dp
            ScreenSize.MEDIUM -> 400.dp
            ScreenSize.EXPANDED -> 500.dp
        }
    }
    
    /**
     * Get responsive dialog padding
     */
    @Composable
    fun padding(): PaddingValues {
        return when (ScreenSize.current()) {
            ScreenSize.COMPACT -> CookbookSpacing.small.toPaddingValues()
            ScreenSize.MEDIUM -> CookbookSpacing.medium.toPaddingValues()
            ScreenSize.EXPANDED -> CookbookSpacing.large.toPaddingValues()
        }
    }
}

// ============================================================================
// FOLDABLE DEVICE SUPPORT
// ============================================================================

/**
 * Foldable device utilities
 */
object FoldableSupport {
    
    /**
     * Check if device has folding feature
     */
    fun hasFoldingFeature(displayFeatures: List<DisplayFeature>): Boolean {
        return displayFeatures.any { it is FoldingFeature }
    }
    
    /**
     * Get folding features from display features
     */
    fun getFoldingFeatures(displayFeatures: List<DisplayFeature>): List<FoldingFeature> {
        return displayFeatures.filterIsInstance<FoldingFeature>()
    }
    
    /**
     * Check if device is in folded state
     */
    fun isFolded(foldingFeatures: List<FoldingFeature>): Boolean {
        return foldingFeatures.any { 
            it.state == FoldingFeature.State.HALF_OPENED || 
            it.state == FoldingFeature.State.FLAT 
        }
    }
}

// ============================================================================
// RESPONSIVE UTILITIES
// ============================================================================

/**
 * Utility to convert dp to pixels
 */
@Composable
fun dpToPx(dp: Dp): Int {
    val density = LocalDensity.current.density
    return (dp.value * density).toInt()
}

/**
 * Utility to convert pixels to dp
 */
@Composable
fun pxToDp(px: Int): Dp {
    val density = LocalDensity.current.density
    return (px / density).dp
}

/**
 * Check if current orientation is portrait
 */
@Composable
fun isPortrait(): Boolean {
    val configuration = LocalConfiguration.current
    return configuration.orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT
}

/**
 * Check if current orientation is landscape
 */
@Composable
fun isLandscape(): Boolean {
    val configuration = LocalConfiguration.current
    return configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
}

/**
 * Get screen width in dp
 */
@Composable
fun screenWidthDp(): Int {
    return LocalConfiguration.current.screenWidthDp
}

/**
 * Get screen height in dp
 */
@Composable
fun screenHeightDp(): Int {
    return LocalConfiguration.current.screenHeightDp
}

/**
 * Get screen density
 */
@Composable
fun screenDensity(): Float {
    return LocalDensity.current.density
}

/**
 * Get screen font scale
 */
@Composable
fun screenFontScale(): Float {
    return LocalDensity.current.fontScale
}
