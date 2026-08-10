package com.ourcookbook.ui.components

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for UI Components
 * These tests validate the component library implementation
 */
class ComponentTests {

    @Test
    fun testCookbookColors_PrimaryColorsDefined() {
        assertNotNull("Primary color should be defined", CookbookColors.primary)
        assertNotNull("Primary variant should be defined", CookbookColors.primaryVariant)
        assertNotNull("Primary light should be defined", CookbookColors.primaryLight)
    }

    @Test
    fun testCookbookColors_SecondaryColorsDefined() {
        assertNotNull("Secondary color should be defined", CookbookColors.secondary)
        assertNotNull("Secondary variant should be defined", CookbookColors.secondaryVariant)
        assertNotNull("Secondary light should be defined", CookbookColors.secondaryLight)
    }

    @Test
    fun testCookbookColors_CategoryColorsDefined() {
        assertNotNull("Category colors map should be defined", CookbookColors.categoryColors)
        assertTrue("Category colors should contain Mains", 
            CookbookColors.categoryColors.containsKey("Mains"))
        assertTrue("Category colors should contain Desserts", 
            CookbookColors.categoryColors.containsKey("Desserts"))
        assertTrue("Category colors should contain Breakfasts", 
            CookbookColors.categoryColors.containsKey("Breakfasts"))
        assertTrue("Category colors should contain Sides", 
            CookbookColors.categoryColors.containsKey("Sides"))
        assertTrue("Category colors should contain Sauces and Spices", 
            CookbookColors.categoryColors.containsKey("Sauces and Spices"))
    }

    @Test
    fun testCookbookColors_StatusColorsDefined() {
        assertNotNull("Success color should be defined", CookbookColors.success)
        assertNotNull("Warning color should be defined", CookbookColors.warning)
        assertNotNull("Error color should be defined", CookbookColors.error)
        assertNotNull("Info color should be defined", CookbookColors.info)
    }

    @Test
    fun testCookbookTypography_AllStylesDefined() {
        assertNotNull("Display large should be defined", CookbookTypography.displayLarge)
        assertNotNull("Display medium should be defined", CookbookTypography.displayMedium)
        assertNotNull("Display small should be defined", CookbookTypography.displaySmall)
        
        assertNotNull("Headline large should be defined", CookbookTypography.headlineLarge)
        assertNotNull("Headline medium should be defined", CookbookTypography.headlineMedium)
        assertNotNull("Headline small should be defined", CookbookTypography.headlineSmall)
        
        assertNotNull("Title large should be defined", CookbookTypography.titleLarge)
        assertNotNull("Title medium should be defined", CookbookTypography.titleMedium)
        assertNotNull("Title small should be defined", CookbookTypography.titleSmall)
        
        assertNotNull("Body large should be defined", CookbookTypography.bodyLarge)
        assertNotNull("Body medium should be defined", CookbookTypography.bodyMedium)
        assertNotNull("Body small should be defined", CookbookTypography.bodySmall)
        
        assertNotNull("Label large should be defined", CookbookTypography.labelLarge)
        assertNotNull("Label medium should be defined", CookbookTypography.labelMedium)
        assertNotNull("Label small should be defined", CookbookTypography.labelSmall)
    }

    @Test
    fun testCookbookSpacing_AllValuesDefined() {
        assertNotNull("XXSmall spacing should be defined", CookbookSpacing.xxSmall)
        assertNotNull("XSmall spacing should be defined", CookbookSpacing.xSmall)
        assertNotNull("Small spacing should be defined", CookbookSpacing.small)
        assertNotNull("Medium spacing should be defined", CookbookSpacing.medium)
        assertNotNull("Large spacing should be defined", CookbookSpacing.large)
        assertNotNull("XLarge spacing should be defined", CookbookSpacing.xLarge)
        assertNotNull("XXLarge spacing should be defined", CookbookSpacing.xxLarge)
        assertNotNull("XXXLarge spacing should be defined", CookbookSpacing.xxxLarge)
        
        assertNotNull("Touch target should be defined", CookbookSpacing.touchTarget)
        assertNotNull("Card elevation should be defined", CookbookSpacing.cardElevation)
        assertNotNull("Divider height should be defined", CookbookSpacing.dividerHeight)
        assertNotNull("Border width should be defined", CookbookSpacing.borderWidth)
        
        assertNotNull("Screen margin should be defined", CookbookSpacing.screenMargin)
        assertNotNull("Screen margin large should be defined", CookbookSpacing.screenMarginLarge)
    }

    @Test
    fun testCookbookElevation_AllValuesDefined() {
        assertNotNull("None elevation should be defined", CookbookElevation.none)
        assertNotNull("Small elevation should be defined", CookbookElevation.small)
        assertNotNull("Medium elevation should be defined", CookbookElevation.medium)
        assertNotNull("Large elevation should be defined", CookbookElevation.large)
        assertNotNull("XLarge elevation should be defined", CookbookElevation.xLarge)
        
        assertNotNull("Card elevation should be defined", CookbookElevation.card)
        assertNotNull("Dialog elevation should be defined", CookbookElevation.dialog)
        assertNotNull("Bottom bar elevation should be defined", CookbookElevation.bottomBar)
        assertNotNull("FAB elevation should be defined", CookbookElevation.fab)
        assertNotNull("Button elevation should be defined", CookbookElevation.button)
        assertNotNull("Snackbar elevation should be defined", CookbookElevation.snackbar)
    }

    @Test
    fun testColorSchemes_Defined() {
        assertNotNull("Light color scheme should be defined", LightColorScheme)
        assertNotNull("Dark color scheme should be defined", DarkColorScheme)
    }

    @Test
    fun testBottomNavItem_DataClass() {
        val item = BottomNavItem(
            route = "home",
            icon = androidx.compose.material.icons.Icons.Default.Home,
            label = "Home",
            contentDescription = "Home"
        )
        
        assertEquals("Route should match", "home", item.route)
        assertEquals("Label should match", "Home", item.label)
        assertEquals("Content description should match", "Home", item.contentDescription)
    }

    @Test
    fun testActionItem_DataClass() {
        val action = ActionItem(
            text = "Delete",
            onClick = {},
            icon = androidx.compose.material.icons.Icons.Default.Delete,
            destructive = true
        )
        
        assertEquals("Text should match", "Delete", action.text)
        assertTrue("Should be destructive", action.destructive)
        assertNotNull("Icon should be defined", action.icon)
    }

    @Test
    fun testIconPosition_Enum() {
        assertEquals("START should be defined", "START", IconPosition.START.name)
        assertEquals("END should be defined", "END", IconPosition.END.name)
    }

    @Test
    fun testChipType_Enum() {
        assertEquals("FILTER should be defined", "FILTER", ChipType.FILTER.name)
        assertEquals("SUGGESTION should be defined", "SUGGESTION", ChipType.SUGGESTION.name)
        assertEquals("TAG should be defined", "TAG", ChipType.TAG.name)
    }

    @Test
    fun testComponentIndex_Reexports() {
        // Test that all components are properly re-exported
        assertNotNull("CookbookColors should be re-exported", CookbookColors)
        assertNotNull("CookbookTypography should be re-exported", CookbookTypography)
        assertNotNull("CookbookSpacing should be re-exported", CookbookSpacing)
        assertNotNull("CookbookElevation should be re-exported", CookbookElevation)
    }

    @Test
    fun testLegacyComponentCompatibility() {
        // Test that legacy aliases still work
        assertNotNull("Legacy CookbookPrimaryButton should work", CookbookPrimaryButton)
        assertNotNull("Legacy CookbookSecondaryButton should work", CookbookSecondaryButton)
        assertNotNull("Legacy CookbookTextField should work", CookbookTextField)
        assertNotNull("Legacy RecipeCard should work", RecipeCard)
    }
}
