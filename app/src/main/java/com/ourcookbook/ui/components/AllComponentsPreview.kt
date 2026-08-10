package com.ourcookbook.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ourcookbook.domain.model.Ingredient
import com.ourcookbook.domain.model.Recipe
import java.time.Instant

/**
 * Comprehensive preview of all UI components for EvidenceQA validation
 * This preview demonstrates all reusable components in a single scrollable screen
 */
@Preview(showBackground = true, name = "All Components Preview")
@Composable
fun AllComponentsPreviewScreen() {
    CookbookTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(CookbookSpacing.medium)
        ) {
            // Header
            Text(
                text = "Cookbook UI Components Library",
                style = CookbookTypography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = "Material Design 3 Components for Recipe Management",
                style = CookbookTypography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            
            Spacer(modifier = Modifier.height(CookbookSpacing.xLarge))
            
            // ===== BUTTONS SECTION =====
            ComponentSection(title = "Buttons")
            
            CookbookPrimaryButton(
                text = "Primary Button",
                onClick = {},
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(CookbookSpacing.small))
            
            CookbookSecondaryButton(
                text = "Secondary Button",
                onClick = {},
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(CookbookSpacing.small))
            
            CookbookTextButton(
                text = "Text Button",
                onClick = {}
            )
            
            Spacer(modifier = Modifier.height(CookbookSpacing.small))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(CookbookSpacing.medium),
                modifier = Modifier.fillMaxWidth()
            ) {
                CookbookIconButton(
                    icon = Icons.Default.Favorite,
                    onClick = {},
                    contentDescription = "Favorite"
                )
                CookbookIconButton(
                    icon = Icons.Default.Delete,
                    onClick = {},
                    contentDescription = "Delete"
                )
                CookbookIconButton(
                    icon = Icons.Default.Edit,
                    onClick = {},
                    contentDescription = "Edit"
                )
                CookbookIconButton(
                    icon = Icons.Default.Share,
                    onClick = {},
                    contentDescription = "Share"
                )
            }
            
            Spacer(modifier = Modifier.height(CookbookSpacing.small))
            
            CookbookIconTextButton(
                text = "Save Recipe",
                icon = Icons.Default.Check,
                onClick = {}
            )
            
            Spacer(modifier = Modifier.height(CookbookSpacing.small))
            
            FavoriteToggleButton(
                isFavorite = false,
                onClick = {}
            )
            
            Spacer(modifier = Modifier.height(CookbookSpacing.medium))
            
            // ===== CARDS SECTION =====
            ComponentSection(title = "Cards")
            
            // Sample recipe for card previews
            val sampleRecipe = Recipe(
                id = "1",
                title = "Spaghetti Carbonara",
                description = "Classic Italian pasta dish with eggs, cheese, pancetta, and black pepper",
                category = "Mains",
                rating = 4.5f,
                cookTime = 30,
                servingSize = 4,
                imageUrl = null,
                ingredients = emptyList(),
                instructions = emptyList(),
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
                versionVector = "",
                checksum = "",
                deviceId = ""
            )
            
            RecipeCard(
                recipe = sampleRecipe,
                onClick = {},
                showFavorite = true,
                isFavorite = true,
                onFavoriteClick = {}
            )
            
            Spacer(modifier = Modifier.height(CookbookSpacing.small))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(CookbookSpacing.medium),
                modifier = Modifier.fillMaxWidth()
            ) {
                CompactRecipeCard(
                    recipe = sampleRecipe,
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )
                CompactRecipeCard(
                    recipe = sampleRecipe.copy(title = "Pasta", category = "Sides"),
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(CookbookSpacing.small))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(CookbookSpacing.medium),
                modifier = Modifier.fillMaxWidth()
            ) {
                StatsCard(
                    title = "Total Recipes",
                    value = "42",
                    icon = Icons.Default.Info,
                    modifier = Modifier.weight(1f)
                )
                StatsCard(
                    title = "Favorites",
                    value = "12",
                    icon = Icons.Default.Favorite,
                    iconTint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(CookbookSpacing.medium))
            
            // ===== INPUT FIELDS SECTION =====
            ComponentSection(title = "Input Fields")
            
            CookbookTextField(
                value = "Spaghetti Carbonara",
                onValueChange = {},
                label = "Recipe Title",
                placeholder = "Enter recipe title"
            )
            
            Spacer(modifier = Modifier.height(CookbookSpacing.small))
            
            CookbookMultilineTextField(
                value = "Classic Italian pasta dish with eggs, cheese, pancetta, and black pepper",
                onValueChange = {},
                label = "Description",
                placeholder = "Enter recipe description"
            )
            
            Spacer(modifier = Modifier.height(CookbookSpacing.small))
            
            CookbookNumberField(
                value = "30",
                onValueChange = {},
                label = "Cook Time",
                suffix = "minutes"
            )
            
            Spacer(modifier = Modifier.height(CookbookSpacing.small))
            
            CookbookSearchField(
                value = "pasta",
                onValueChange = {},
                placeholder = "Search recipes..."
            )
            
            Spacer(modifier = Modifier.height(CookbookSpacing.small))
            
            CookbookPasswordField(
                value = "",
                onValueChange = {},
                label = "Password"
            )
            
            Spacer(modifier = Modifier.height(CookbookSpacing.small))
            
            CookbookEmailField(
                value = "",
                onValueChange = {},
                label = "Email"
            )
            
            Spacer(modifier = Modifier.height(CookbookSpacing.small))
            
            CookbookTextField(
                value = "",
                onValueChange = {},
                label = "Error Field",
                isError = true,
                errorMessage = "Please enter a valid value"
            )
            
            Spacer(modifier = Modifier.height(CookbookSpacing.medium))
            
            // ===== CHIPS SECTION =====
            ComponentSection(title = "Chips")
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(CookbookSpacing.small),
                modifier = Modifier.fillMaxWidth()
            ) {
                CookbookFilterChip(
                    label = "Mains",
                    isSelected = true,
                    onClick = {},
                    category = "Mains"
                )
                CookbookFilterChip(
                    label = "Desserts",
                    isSelected = false,
                    onClick = {},
                    category = "Desserts"
                )
                CookbookFilterChip(
                    label = "Breakfasts",
                    isSelected = false,
                    onClick = {}
                )
            }
            
            Spacer(modifier = Modifier.height(CookbookSpacing.small))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(CookbookSpacing.small),
                modifier = Modifier.fillMaxWidth()
            ) {
                CookbookSuggestionChip(
                    label = "Italian",
                    onClick = {}
                )
                CookbookSuggestionChip(
                    label = "Quick",
                    onClick = {}
                )
                CookbookSuggestionChip(
                    label = "Vegetarian",
                    onClick = {}
                )
            }
            
            Spacer(modifier = Modifier.height(CookbookSpacing.small))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(CookbookSpacing.small),
                modifier = Modifier.fillMaxWidth()
            ) {
                CookbookAssistChip(
                    label = "Add Tag",
                    onClick = {},
                    icon = Icons.Default.Add
                )
                CookbookAssistChip(
                    label = "Help",
                    onClick = {},
                    icon = Icons.Default.Info
                )
            }
            
            Spacer(modifier = Modifier.height(CookbookSpacing.small))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(CookbookSpacing.small),
                modifier = Modifier.fillMaxWidth()
            ) {
                TagChip(tag = "Pasta", onClick = {})
                TagChip(tag = "Italian", onClick = {})
                TagChip(tag = "Quick", onClick = {})
            }
            
            Spacer(modifier = Modifier.height(CookbookSpacing.small))
            
            TagInputField(
                tags = listOf("Pasta", "Italian", "Quick"),
                onTagsChange = {}
            )
            
            Spacer(modifier = Modifier.height(CookbookSpacing.medium))
            
            // ===== LISTS SECTION =====
            ComponentSection(title = "List Components")
            
            SectionHeader(
                title = "Ingredients",
                actionText = "Add",
                onActionClick = {}
            )
            
            Spacer(modifier = Modifier.height(CookbookSpacing.small))
            
            val sampleIngredient = Ingredient(
                id = "1",
                name = "Spaghetti",
                amount = "400g",
                notes = "or any pasta",
                recipeId = "1"
            )
            
            IngredientItem(
                ingredient = sampleIngredient,
                showCheckbox = true,
                checked = false,
                onCheckedChange = {}
            )
            
            IngredientItem(
                ingredient = sampleIngredient.copy(name = "Eggs", amount = "4", notes = null),
                showCheckbox = true,
                checked = true,
                onCheckedChange = {}
            )
            
            Spacer(modifier = Modifier.height(CookbookSpacing.small))
            
            SectionHeader(title = "Instructions")
            
            Spacer(modifier = Modifier.height(CookbookSpacing.small))
            
            InstructionStep(
                stepNumber = 1,
                instruction = "Boil water in a large pot and add salt.",
                showCheckbox = true,
                checked = false,
                onCheckedChange = {}
            )
            
            InstructionStep(
                stepNumber = 2,
                instruction = "Cook spaghetti according to package instructions until al dente.",
                showCheckbox = true,
                checked = true,
                onCheckedChange = {}
            )
            
            Spacer(modifier = Modifier.height(CookbookSpacing.medium))
            
            // ===== NAVIGATION SECTION =====
            ComponentSection(title = "Navigation Components")
            
            CookbookTopAppBar(
                title = "Our Cookbook",
                navigationIcon = Icons.Default.Menu,
                onNavigationClick = {},
                actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    }
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Sync"
                        )
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(CookbookSpacing.small))
            
            CookbookSearchAppBar(
                searchQuery = "pasta",
                onSearchQueryChange = {},
                onSearch = {},
                onBack = {}
            )
            
            Spacer(modifier = Modifier.height(CookbookSpacing.small))
            
            CookbookBottomNavigation(
                currentRoute = "home",
                onNavigate = {}
            )
            
            Spacer(modifier = Modifier.height(CookbookSpacing.medium))
            
            // ===== STATES SECTION =====
            ComponentSection(title = "State Components")
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                EmptyState(
                    icon = Icons.Default.Search,
                    title = "No recipes found",
                    description = "Try adding a new recipe or adjusting your search"
                )
            }
            
            Spacer(modifier = Modifier.height(CookbookSpacing.small))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                LoadingState()
            }
            
            Spacer(modifier = Modifier.height(CookbookSpacing.small))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                ErrorState(
                    message = "Failed to load recipes. Please check your connection.",
                    onRetry = {}
                )
            }
            
            Spacer(modifier = Modifier.height(CookbookSpacing.xLarge))
            
            // Footer
            Text(
                text = "All components implemented with Material Design 3",
                style = CookbookTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Text(
                text = "Ready for EvidenceQA validation",
                style = CookbookTypography.bodySmall,
                color = MaterialTheme.colorScheme.success,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

/**
 * Section header for component categories
 */
@Composable
fun ComponentSection(title: String) {
    Text(
        text = title,
        style = CookbookTypography.titleLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
    )
    Divider(
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .height(2.dp)
            .padding(vertical = CookbookSpacing.xSmall)
    )
    Spacer(modifier = Modifier.height(CookbookSpacing.small))
}

/**
 * Preview for individual component categories
 */
@Preview(showBackground = true, name = "Buttons Preview")
@Composable
fun ButtonsPreviewScreen() {
    CookbookTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(CookbookSpacing.medium)
        ) {
            ComponentSection(title = "Buttons")
            ButtonsPreview()
        }
    }
}

@Preview(showBackground = true, name = "Cards Preview")
@Composable
fun CardsPreviewScreen() {
    CookbookTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(CookbookSpacing.medium)
        ) {
            ComponentSection(title = "Cards")
            CardsPreview()
        }
    }
}

@Preview(showBackground = true, name = "Input Fields Preview")
@Composable
fun InputFieldsPreviewScreen() {
    CookbookTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(CookbookSpacing.medium)
        ) {
            ComponentSection(title = "Input Fields")
            InputFieldsPreview()
        }
    }
}

@Preview(showBackground = true, name = "Lists Preview")
@Composable
fun ListsPreviewScreen() {
    CookbookTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(CookbookSpacing.medium)
        ) {
            ComponentSection(title = "Lists & Chips")
            ListsPreview()
        }
    }
}

@Preview(showBackground = true, name = "Chips Preview")
@Composable
fun ChipsPreviewScreen() {
    CookbookTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(CookbookSpacing.medium)
        ) {
            ComponentSection(title = "Chips")
            ChipsPreview()
        }
    }
}
