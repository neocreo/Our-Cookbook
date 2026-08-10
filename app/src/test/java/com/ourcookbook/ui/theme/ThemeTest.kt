package com.ourcookbook.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * Unit tests for the Cookbook Theme System
 * 
 * Tests the theme system implementation for Task 1.10 (Theme and Styling)
 */
class ThemeTest {

    // ============================================================================
    // COLOR SYSTEM TESTS
    // ============================================================================

    @Test
    fun testCookbookColors_PrimaryColors_NotNull() {
        assertNotNull("Primary color should not be null", CookbookColors.primary)
        assertNotNull("Primary variant color should not be null", CookbookColors.primaryVariant)
        assertNotNull("Primary light color should not be null", CookbookColors.primaryLight)
        assertNotNull("On primary color should not be null", CookbookColors.onPrimary)
    }

    @Test
    fun testCookbookColors_SecondaryColors_NotNull() {
        assertNotNull("Secondary color should not be null", CookbookColors.secondary)
        assertNotNull("Secondary variant color should not be null", CookbookColors.secondaryVariant)
        assertNotNull("Secondary light color should not be null", CookbookColors.secondaryLight)
        assertNotNull("On secondary color should not be null", CookbookColors.onSecondary)
    }

    @Test
    fun testCookbookColors_TertiaryColors_NotNull() {
        assertNotNull("Tertiary color should not be null", CookbookColors.tertiary)
        assertNotNull("Tertiary variant color should not be null", CookbookColors.tertiaryVariant)
        assertNotNull("Tertiary light color should not be null", CookbookColors.tertiaryLight)
        assertNotNull("On tertiary color should not be null", CookbookColors.onTertiary)
    }

    @Test
    fun testCookbookColors_StatusColors_NotNull() {
        assertNotNull("Success color should not be null", CookbookColors.success)
        assertNotNull("Warning color should not be null", CookbookColors.warning)
        assertNotNull("Error color should not be null", CookbookColors.error)
        assertNotNull("Info color should not be null", CookbookColors.info)
    }

    @Test
    fun testCookbookColors_CategoryColors_NotEmpty() {
        assertNotNull("Category colors should not be null", CookbookColors.categoryColors)
        assertEquals("Category colors should have expected entries", 
            11, CookbookColors.categoryColors.size)
    }

    @Test
    fun testCookbookColors_PrimaryColor_Value() {
        val expectedColor = Color(0xFFE57373)
        assertEquals("Primary color should match design token", 
            expectedColor, CookbookColors.primary)
    }

    @Test
    fun testCookbookColors_SecondaryColor_Value() {
        val expectedColor = Color(0xFF81C784)
        assertEquals("Secondary color should match design token", 
            expectedColor, CookbookColors.secondary)
    }

    @Test
    fun testGetCategoryColor_KnownCategory() {
        val color = getCategoryColor("Mains")
        assertNotNull("Category color should not be null", color)
        assertEquals("Mains category should be red", 
            Color(0xFFE57373), color)
    }

    @Test
    fun testGetCategoryColor_UnknownCategory() {
        val color = getCategoryColor("Unknown")
        assertNotNull("Unknown category should return primary color", color)
        assertEquals("Unknown category should return primary color", 
            CookbookColors.primary, color)
    }

    @Test
    fun testGetStatusColor_Success() {
        val color = getStatusColor("success")
        assertNotNull("Success status color should not be null", color)
        assertEquals("Success status should be green", 
            CookbookColors.success, color)
    }

    @Test
    fun testGetStatusColor_Warning() {
        val color = getStatusColor("warning")
        assertNotNull("Warning status color should not be null", color)
        assertEquals("Warning status should be amber", 
            CookbookColors.warning, color)
    }

    @Test
    fun testGetStatusColor_Error() {
        val color = getStatusColor("error")
        assertNotNull("Error status color should not be null", color)
        assertEquals("Error status should be red", 
            CookbookColors.error, color)
    }

    @Test
    fun testGetStatusColor_Unknown() {
        val color = getStatusColor("unknown")
        assertNotNull("Unknown status color should not be null", color)
        assertEquals("Unknown status should return primary color", 
            CookbookColors.primary, color)
    }

    // ============================================================================
    // TYPOGRAPHY SYSTEM TESTS
    // ============================================================================

    @Test
    fun testCookbookTypography_NotNull() {
        assertNotNull("Cookbook typography should not be null", CookbookTypography)
    }

    @Test
    fun testCookbookTypography_DisplayStyles_NotNull() {
        assertNotNull("Display large should not be null", CookbookTypography.displayLarge)
        assertNotNull("Display medium should not be null", CookbookTypography.displayMedium)
        assertNotNull("Display small should not be null", CookbookTypography.displaySmall)
    }

    @Test
    fun testCookbookTypography_HeadlineStyles_NotNull() {
        assertNotNull("Headline large should not be null", CookbookTypography.headlineLarge)
        assertNotNull("Headline medium should not be null", CookbookTypography.headlineMedium)
        assertNotNull("Headline small should not be null", CookbookTypography.headlineSmall)
    }

    @Test
    fun testCookbookTypography_TitleStyles_NotNull() {
        assertNotNull("Title large should not be null", CookbookTypography.titleLarge)
        assertNotNull("Title medium should not be null", CookbookTypography.titleMedium)
        assertNotNull("Title small should not be null", CookbookTypography.titleSmall)
    }

    @Test
    fun testCookbookTypography_BodyStyles_NotNull() {
        assertNotNull("Body large should not be null", CookbookTypography.bodyLarge)
        assertNotNull("Body medium should not be null", CookbookTypography.bodyMedium)
        assertNotNull("Body small should not be null", CookbookTypography.bodySmall)
    }

    @Test
    fun testCookbookTypography_LabelStyles_NotNull() {
        assertNotNull("Label large should not be null", CookbookTypography.labelLarge)
        assertNotNull("Label medium should not be null", CookbookTypography.labelMedium)
        assertNotNull("Label small should not be null", CookbookTypography.labelSmall)
    }

    @Test
    fun testCookbookTypography_DisplayLarge_Properties() {
        val style = CookbookTypography.displayLarge
        assertEquals("Display large font size should be 57sp", 57.sp, style.fontSize)
        assertEquals("Display large font weight should be Bold", 
            FontWeight.Bold, style.fontWeight)
    }

    @Test
    fun testCookbookTypography_BodyMedium_Properties() {
        val style = CookbookTypography.bodyMedium
        assertEquals("Body medium font size should be 16sp", 16.sp, style.fontSize)
        assertEquals("Body medium font weight should be Normal", 
            FontWeight.Normal, style.fontWeight)
    }

    @Test
    fun testCookbookTextStyles_NotNull() {
        assertNotNull("Cookbook text styles should not be null", CookbookTextStyles)
    }

    @Test
    fun testCookbookTextStyles_RecipeStyles_NotNull() {
        assertNotNull("Recipe title style should not be null", CookbookTextStyles.recipeTitle)
        assertNotNull("Recipe ingredient style should not be null", CookbookTextStyles.recipeIngredient)
        assertNotNull("Recipe instruction style should not be null", CookbookTextStyles.recipeInstruction)
    }

    @Test
    fun testLegacyCookbookTypography_NotNull() {
        assertNotNull("Legacy typography should not be null", LegacyCookbookTypography)
    }

    @Test
    fun testLegacyCookbookTypography_H1_Properties() {
        val style = LegacyCookbookTypography.h1
        assertEquals("Legacy h1 font size should be 28sp", 28.sp, style.fontSize)
        assertEquals("Legacy h1 font weight should be Bold", 
            FontWeight.Bold, style.fontWeight)
    }

    // ============================================================================
    // SHAPE SYSTEM TESTS
    // ============================================================================

    @Test
    fun testCookbookShapes_NotNull() {
        assertNotNull("Cookbook shapes should not be null", CookbookShapes)
    }

    @Test
    fun testCookbookShapes_AllShapes_NotNull() {
        assertNotNull("Extra small shape should not be null", CookbookShapes.extraSmall)
        assertNotNull("Small shape should not be null", CookbookShapes.small)
        assertNotNull("Medium shape should not be null", CookbookShapes.medium)
        assertNotNull("Large shape should not be null", CookbookShapes.large)
        assertNotNull("Extra large shape should not be null", CookbookShapes.extraLarge)
    }

    @Test
    fun testShapeTokens_NotNull() {
        assertNotNull("Shape tokens should not be null", ShapeTokens)
    }

    @Test
    fun testShapeTokens_Values() {
        assertEquals("Shape token xxs should be 4dp", 4.dp, ShapeTokens.xxs)
        assertEquals("Shape token xs should be 8dp", 8.dp, ShapeTokens.xs)
        assertEquals("Shape token sm should be 12dp", 12.dp, ShapeTokens.sm)
        assertEquals("Shape token md should be 16dp", 16.dp, ShapeTokens.md)
        assertEquals("Shape token lg should be 24dp", 24.dp, ShapeTokens.lg)
    }

    @Test
    fun testComponentShapes_NotNull() {
        assertNotNull("Component shapes should not be null", ComponentShapes)
    }

    @Test
    fun testComponentShapes_CommonShapes_NotNull() {
        assertNotNull("Recipe card shape should not be null", ComponentShapes.recipeCard)
        assertNotNull("Primary button shape should not be null", ComponentShapes.primaryButton)
        assertNotNull("Text field shape should not be null", ComponentShapes.textField)
        assertNotNull("Filter chip shape should not be null", ComponentShapes.filterChip)
    }

    // ============================================================================
    // SPACING SYSTEM TESTS
    // ============================================================================

    @Test
    fun testCookbookSpacing_NotNull() {
        assertNotNull("Cookbook spacing should not be null", CookbookSpacing)
    }

    @Test
    fun testCookbookSpacing_AllValues_NotNull() {
        assertNotNull("XXSmall spacing should not be null", CookbookSpacing.xxSmall)
        assertNotNull("XSmall spacing should not be null", CookbookSpacing.xSmall)
        assertNotNull("Small spacing should not be null", CookbookSpacing.small)
        assertNotNull("Medium spacing should not be null", CookbookSpacing.medium)
        assertNotNull("Large spacing should not be null", CookbookSpacing.large)
        assertNotNull("XLarge spacing should not be null", CookbookSpacing.xLarge)
        assertNotNull("XXLarge spacing should not be null", CookbookSpacing.xxLarge)
        assertNotNull("XXXLarge spacing should not be null", CookbookSpacing.xxxLarge)
    }

    @Test
    fun testCookbookSpacing_Values() {
        assertEquals("XXSmall spacing should be 4dp", 4.dp, CookbookSpacing.xxSmall)
        assertEquals("XSmall spacing should be 8dp", 8.dp, CookbookSpacing.xSmall)
        assertEquals("Small spacing should be 12dp", 12.dp, CookbookSpacing.small)
        assertEquals("Medium spacing should be 16dp", 16.dp, CookbookSpacing.medium)
        assertEquals("Large spacing should be 24dp", 24.dp, CookbookSpacing.large)
        assertEquals("XLarge spacing should be 32dp", 32.dp, CookbookSpacing.xLarge)
        assertEquals("XXLarge spacing should be 48dp", 48.dp, CookbookSpacing.xxLarge)
        assertEquals("XXXLarge spacing should be 64dp", 64.dp, CookbookSpacing.xxxLarge)
    }

    @Test
    fun testCookbookSpacing_SpecialValues() {
        assertEquals("Touch target should be 48dp", 48.dp, CookbookSpacing.touchTarget)
        assertEquals("Card elevation should be 4dp", 4.dp, CookbookSpacing.cardElevation)
        assertEquals("Divider height should be 1dp", 1.dp, CookbookSpacing.dividerHeight)
        assertEquals("Border width should be 1dp", 1.dp, CookbookSpacing.borderWidth)
    }

    @Test
    fun testSpacingTokens_NotNull() {
        assertNotNull("Spacing tokens should not be null", SpacingTokens)
    }

    @Test
    fun testScreenSpacing_NotNull() {
        assertNotNull("Screen spacing should not be null", ScreenSpacing)
    }

    @Test
    fun testComponentSpacing_NotNull() {
        assertNotNull("Component spacing should not be null", ComponentSpacing)
    }

    @Test
    fun testLayoutSpacing_NotNull() {
        assertNotNull("Layout spacing should not be null", LayoutSpacing)
    }

    @Test
    fun testRecipeSpacing_NotNull() {
        assertNotNull("Recipe spacing should not be null", RecipeSpacing)
    }

    // ============================================================================
    // ELEVATION SYSTEM TESTS
    // ============================================================================

    @Test
    fun testCookbookElevation_NotNull() {
        assertNotNull("Cookbook elevation should not be null", CookbookElevation)
    }

    @Test
    fun testCookbookElevation_AllValues_NotNull() {
        assertNotNull("None elevation should not be null", CookbookElevation.none)
        assertNotNull("Small elevation should not be null", CookbookElevation.small)
        assertNotNull("Medium elevation should not be null", CookbookElevation.medium)
        assertNotNull("Large elevation should not be null", CookbookElevation.large)
        assertNotNull("XLarge elevation should not be null", CookbookElevation.xLarge)
        assertNotNull("XXLarge elevation should not be null", CookbookElevation.xxLarge)
    }

    @Test
    fun testCookbookElevation_Values() {
        assertEquals("None elevation should be 0dp", 0.dp, CookbookElevation.none)
        assertEquals("Small elevation should be 2dp", 2.dp, CookbookElevation.small)
        assertEquals("Medium elevation should be 4dp", 4.dp, CookbookElevation.medium)
        assertEquals("Large elevation should be 8dp", 8.dp, CookbookElevation.large)
        assertEquals("XLarge elevation should be 12dp", 12.dp, CookbookElevation.xLarge)
    }

    @Test
    fun testCookbookElevation_ComponentElevations() {
        assertNotNull("Card elevation should not be null", CookbookElevation.card)
        assertNotNull("Dialog elevation should not be null", CookbookElevation.dialog)
        assertNotNull("FAB elevation should not be null", CookbookElevation.fab)
        assertNotNull("Button elevation should not be null", CookbookElevation.button)
        assertNotNull("Snackbar elevation should not be null", CookbookElevation.snackbar)
    }

    @Test
    fun testElevationTokens_NotNull() {
        assertNotNull("Elevation tokens should not be null", ElevationTokens)
    }

    @Test
    fun testComponentElevation_NotNull() {
        assertNotNull("Component elevation should not be null", ComponentElevation)
    }

    @Test
    fun testElevationStates_NotNull() {
        assertNotNull("Elevation states should not be null", ElevationStates)
    }

    @Test
    fun testElevationStates_ButtonElevation() {
        val elevation = ElevationStates.button()
        assertNotNull("Button elevation should not be null", elevation)
        assertEquals("Button default elevation should be 2dp", 2.dp, elevation.default)
        assertEquals("Button pressed elevation should be 4dp", 4.dp, elevation.pressed)
    }

    @Test
    fun testElevationStates_CardElevation() {
        val elevation = ElevationStates.card()
        assertNotNull("Card elevation should not be null", elevation)
        assertEquals("Card default elevation should be 4dp", 4.dp, elevation.default)
        assertEquals("Card pressed elevation should be 8dp", 8.dp, elevation.pressed)
    }

    @Test
    fun testElevationStates_FabElevation() {
        val elevation = ElevationStates.fab()
        assertNotNull("FAB elevation should not be null", elevation)
        assertEquals("FAB default elevation should be 8dp", 8.dp, elevation.default)
        assertEquals("FAB pressed elevation should be 12dp", 12.dp, elevation.pressed)
    }

    // ============================================================================
    // COLOR SCHEME TESTS
    // ============================================================================

    @Test
    fun testLightColorScheme_NotNull() {
        assertNotNull("Light color scheme should not be null", LightColorScheme)
    }

    @Test
    fun testDarkColorScheme_NotNull() {
        assertNotNull("Dark color scheme should not be null", DarkColorScheme)
    }

    @Test
    fun testLightColorScheme_PrimaryColor() {
        assertEquals("Light color scheme primary should match", 
            CookbookColors.primary, LightColorScheme.primary)
    }

    @Test
    fun testLightColorScheme_SurfaceColor() {
        assertEquals("Light color scheme surface should be white", 
            CookbookColors.surfaceLight, LightColorScheme.surface)
    }

    @Test
    fun testDarkColorScheme_PrimaryColor() {
        assertEquals("Dark color scheme primary should match", 
            CookbookColors.primary, DarkColorScheme.primary)
    }

    @Test
    fun testDarkColorScheme_SurfaceColor() {
        assertEquals("Dark color scheme surface should be dark", 
            CookbookColors.surfaceDark, DarkColorScheme.surface)
    }

    // ============================================================================
    // UTILITY FUNCTION TESTS
    // ============================================================================

    @Test
    fun testDpExtension_Int() {
        val dp: Dp = 16.dp
        assertEquals("Int.dp extension should work", 16.dp, dp)
    }

    @Test
    fun testDpExtension_Float() {
        val dp: Dp = 16.5f.dp
        assertEquals("Float.dp extension should work", 16.5f.dp, dp)
    }

    @Test
    fun testDpToPaddingValues() {
        val padding = 16.dp.toPaddingValues()
        assertNotNull("PaddingValues should not be null", padding)
    }

    @Test
    fun testPaddingValuesFunction() {
        val padding = paddingValues(horizontal = 16.dp, vertical = 8.dp)
        assertNotNull("PaddingValues should not be null", padding)
    }

    @Test
    fun testDpScale() {
        val scaled = 16.dp.scale(2f)
        assertEquals("Scaled Dp should be 32dp", 32.dp, scaled)
    }

    @Test
    fun testDpClampElevation() {
        val clamped = 20.dp.clampElevation(16.dp)
        assertEquals("Clamped Dp should be 16dp", 16.dp, clamped)
    }

    @Test
    fun testDpWithPressedState() {
        val (default, pressed) = 4.dp.withPressedState(2.dp)
        assertEquals("Default elevation should be 4dp", 4.dp, default)
        assertEquals("Pressed elevation should be 6dp", 6.dp, pressed)
    }

    @Test
    fun testDpWithHoverState() {
        val (default, hover) = 4.dp.withHoverState(2.dp)
        assertEquals("Default elevation should be 4dp", 4.dp, default)
        assertEquals("Hover elevation should be 6dp", 6.dp, hover)
    }

    @Test
    fun testDpWithStates() {
        val states = 4.dp.withStates(
            pressedIncrement = 2.dp,
            hoverIncrement = 2.dp,
            focusedIncrement = 1.dp
        )
        assertNotNull("State map should not be null", states)
        assertEquals("Default state should be 4dp", 4.dp, states["default"])
        assertEquals("Pressed state should be 6dp", 6.dp, states["pressed"])
        assertEquals("Hover state should be 6dp", 6.dp, states["hover"])
        assertEquals("Focused state should be 5dp", 5.dp, states["focused"])
    }

    // ============================================================================
    // COLOR UTILITY TESTS
    // ============================================================================

    @Test
    fun testColorToHex_Red() {
        val color = Color.Red
        val hex = color.toHex()
        assertEquals("Red color should be FF0000", "FF0000", hex)
    }

    @Test
    fun testColorToHex_Primary() {
        val color = CookbookColors.primary
        val hex = color.toHex()
        assertEquals("Primary color should be E57373", "E57373", hex)
    }

    @Test
    fun testColorToHex_White() {
        val color = Color.White
        val hex = color.toHex()
        assertEquals("White color should be FFFFFF", "FFFFFF", hex)
    }

    @Test
    fun testColorToHex_Black() {
        val color = Color.Black
        val hex = color.toHex()
        assertEquals("Black color should be 000000", "000000", hex)
    }
}

// Type alias for testing
private typealias Dp = androidx.compose.ui.unit.Dp