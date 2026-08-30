package com.ourcookbook.ui.components

import androidx.compose.runtime.Composable

/**
 * Component Index for Cookbook UI Components
 * 
 * This file serves as an index for all reusable UI components in the Cookbook app.
 * Import this file to access all components with a single import.
 * 
 * Component Categories:
 * - Buttons: Primary, Secondary, Text, Icon, FAB, etc.
 * - Cards: Base, Recipe, Compact, Stats, etc.
 * - Input Fields: Text, Multiline, Number, Search, Email, Password, etc.
 * - Dialogs: Confirmation, Delete, Info, Error, Loading, Custom, ActionSheet
 * - Lists: LazyColumn, RecipeList, CategoryChip, IngredientItem, InstructionStep, etc.
 * - Navigation: BottomNavigation, TopAppBar, SearchAppBar, NavigationRail
 * - Chips: FilterChip, SuggestionChip, AssistChip, TagChip, etc.
 * - Theme: Colors, Typography, Spacing, Elevation
 * 
 * Usage:
 * import com.ourcookbook.ui.components.*
 * 
 * Then use any component like:
 * CookbookPrimaryButton(text = "Save", onClick = {})
 */

// Re-export all components for easy access

// Theme components
val CookbookColors = CookbookColors
val CookbookTypography = CookbookTypography
val CookbookSpacing = CookbookSpacing
val CookbookElevation = CookbookElevation

// Buttons
typealias PrimaryButton = CookbookPrimaryButton
typealias SecondaryButton = CookbookSecondaryButton
typealias TextButton = CookbookTextButton
typealias IconButton = CookbookIconButton
typealias FloatingActionButton = CookbookFloatingActionButton
typealias IconTextButton = CookbookIconTextButton
typealias FavoriteToggleButton = FavoriteToggleButton

// Cards
typealias Card = CookbookCard
typealias ElevatedCard = CookbookElevatedCard
typealias RecipeCard = RecipeCard
typealias CompactRecipeCard = CompactRecipeCard
typealias CategoryBadge = CategoryBadge
typealias RatingDisplay = RatingDisplay
typealias CookTimeDisplay = CookTimeDisplay
typealias ServingSizeDisplay = ServingSizeDisplay
typealias StatsCard = StatsCard

// Input Fields
typealias TextField = CookbookTextField
typealias MultilineTextField = CookbookMultilineTextField
typealias NumberField = CookbookNumberField
typealias SearchField = CookbookSearchField
typealias PasswordField = CookbookPasswordField
typealias EmailField = CookbookEmailField

// Dialogs
typealias ConfirmationDialog = CookbookConfirmationDialog
typealias DeleteDialog = CookbookDeleteDialog
typealias InfoDialog = CookbookInfoDialog
typealias ErrorDialog = CookbookErrorDialog
typealias LoadingDialog = CookbookLoadingDialog
typealias CustomDialog = CookbookCustomDialog
typealias ActionSheet = CookbookActionSheet

// Lists
typealias LazyColumn = CookbookLazyColumn
typealias RecipeList = RecipeList
typealias CategoryChip = CategoryChip
typealias CategoryChipRow = CategoryChipRow
typealias IngredientItem = IngredientItem
typealias InstructionStep = InstructionStep
typealias SectionHeader = SectionHeader
typealias EmptyState = EmptyState
typealias LoadingState = LoadingListState

// Navigation
typealias BottomNavigation = CookbookBottomNavigation
typealias TopAppBar = CookbookTopAppBar
typealias SearchAppBar = CookbookSearchAppBar
typealias NavigationRail = CookbookNavigationRail
typealias BottomAppBar = CookbookBottomAppBar

// Chips
typealias FilterChip = CookbookFilterChip
typealias SuggestionChip = CookbookSuggestionChip
typealias AssistChip = CookbookAssistChip
typealias ElevatedAssistChip = CookbookElevatedAssistChip
typealias TagChip = TagChip
typealias TagInputChip = TagInputChip
typealias TagInputField = TagInputField
typealias ChipRow = ChipRow

// Data classes
typealias BottomNavItem = BottomNavItem
typealias ActionItem = ActionItem
typealias IconPosition = IconPosition
typealias ChipType = ChipType

/**
 * Preview function to showcase all components
 * Use this in your preview composables to see all available components
 */
@Composable
fun AllComponentsPreview() {
    // This function can be used to preview all components
    // In practice, you would use individual component previews
    ButtonsPreview()
    CardsPreview()
    InputFieldsPreview()
    ListsPreview()
    ChipsPreview()
}
