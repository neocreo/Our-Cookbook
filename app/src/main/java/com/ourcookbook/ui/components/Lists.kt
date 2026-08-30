@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.ourcookbook.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ourcookbook.domain.model.Ingredient
import com.ourcookbook.domain.model.Recipe
import com.ourcookbook.ui.theme.CookbookColors
import com.ourcookbook.ui.theme.CookbookSpacing
import com.ourcookbook.ui.theme.CookbookTypography

/**
 * Custom LazyColumn with consistent spacing and padding
 */
@Composable
fun CookbookLazyColumn(
    modifier: Modifier = Modifier,
    contentPadding: androidx.compose.foundation.layout.PaddingValues = androidx.compose.foundation.layout.PaddingValues(CookbookSpacing.medium),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(CookbookSpacing.small),
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    content: LazyListScope.() -> Unit
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding,
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment
    ) {
        content()
    }
}

/**
 * Recipe list component for displaying a list of recipes
 */
@Composable
fun RecipeList(
    recipes: List<Recipe>,
    onRecipeClick: (Recipe) -> Unit,
    modifier: Modifier = Modifier,
    emptyContent: @Composable () -> Unit = {
        EmptyState(
            icon = Icons.Default.Search,
            title = "No recipes found",
            description = "Try adding a new recipe or adjusting your search"
        )
    },
    showFavorite: Boolean = false,
    favoriteRecipes: Set<String> = emptySet(),
    onFavoriteClick: ((Recipe) -> Unit)? = null
) {
    if (recipes.isEmpty()) {
        emptyContent()
    } else {
        CookbookLazyColumn(modifier = modifier) {
            items(recipes, key = { it.id }) { recipe ->
                RecipeCard(
                    recipe = recipe,
                    onClick = { onRecipeClick(recipe) },
                    showFavorite = showFavorite,
                    isFavorite = favoriteRecipes.contains(recipe.id),
                    onFavoriteClick = { onFavoriteClick?.invoke(recipe) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = CookbookSpacing.xSmall)
                )
            }
        }
    }
}

/**
 * Category chip for filtering recipes by category
 */
@Composable
fun CategoryChip(
    category: String,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryColor = CookbookColors.categoryColors.getOrDefault(
        category, 
        MaterialTheme.colorScheme.primary
    )

    FilterChip(
        selected = isSelected,
        onClick = onClick,
        modifier = modifier,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = categoryColor,
            selectedLabelColor = Color.White,
            containerColor = categoryColor.copy(alpha = 0.2f),
            labelColor = categoryColor
        ),
        border = FilterChipDefaults.filterChipBorder(
            borderColor = categoryColor,
            selectedBorderColor = categoryColor
        )
    ) {
        Text(
            text = category,
            style = CookbookTypography.labelMedium
        )
    }
}

/**
 * Category chip row for displaying multiple category filters
 */
@Composable
fun CategoryChipRow(
    categories: List<String>,
    selectedCategory: String? = null,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val allCategories = listOf("All") + categories
    
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(CookbookSpacing.xSmall)
    ) {
        items(allCategories) { category ->
            CategoryChip(
                category = category,
                isSelected = category == selectedCategory,
                onClick = { onCategorySelected(category) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Ingredient item for displaying ingredients in a list
 */
@Composable
fun IngredientItem(
    ingredient: Ingredient,
    showCheckbox: Boolean = false,
    checked: Boolean = false,
    onCheckedChange: (Boolean) -> Unit = {},
    showActions: Boolean = false,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = CookbookSpacing.small)
    ) {
        if (showCheckbox) {
            Checkbox(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.outline
                )
            )
            Spacer(modifier = Modifier.width(CookbookSpacing.small))
        }
        
        Column(modifier = Modifier.weight(1f)) {
            // Ingredient name and amount
            Row(
                verticalAlignment = Alignment.Baseline
            ) {
                Text(
                    text = ingredient.amount?.let { "$it " } ?: "",
                    style = CookbookTypography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = ingredient.name,
                    style = CookbookTypography.bodyLarge
                )
            }
            
            // Ingredient notes
            if (!ingredient.notes.isNullOrEmpty()) {
                Text(
                    text = ingredient.notes,
                    style = CookbookTypography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(start = CookbookSpacing.xSmall)
                )
            }
        }
        
        if (showActions) {
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.padding(start = CookbookSpacing.small)
            ) {
                onEdit?.let {
                    IconButton(onClick = it) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                onDelete?.let {
                    IconButton(onClick = it) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Instruction step for displaying recipe instructions
 */
@Composable
fun InstructionStep(
    stepNumber: Int,
    instruction: String,
    checked: Boolean = false,
    onCheckedChange: (Boolean) -> Unit = {},
    showCheckbox: Boolean = false
) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = CookbookSpacing.xSmall)
    ) {
        if (showCheckbox) {
            Checkbox(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.outline
                )
            )
            Spacer(modifier = Modifier.width(CookbookSpacing.small))
        } else {
            // Step number
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(50)
                    )
            ) {
                Text(
                    text = stepNumber.toString(),
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = CookbookTypography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(CookbookSpacing.small))
        }
        
        Text(
            text = instruction,
            style = CookbookTypography.bodyLarge
        )
    }
}

/**
 * Section header for organizing content in lists
 */
@Composable
fun SectionHeader(
    title: String,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            style = CookbookTypography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        if (actionText != null && onActionClick != null) {
            TextButton(onClick = onActionClick) {
                Text(
                    text = actionText,
                    style = CookbookTypography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * List item with drag handle for reordering
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DraggableListItem(
    content: @Composable () -> Unit,
    onDrag: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {},
                onLongClick = onDrag
            )
            .padding(vertical = CookbookSpacing.xSmall)
    ) {
        Icon(
            imageVector = Icons.Default.DragHandle,
            contentDescription = "Drag to reorder",
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier
                .size(20.dp)
                .padding(CookbookSpacing.small)
        )
        content()
    }
}

/**
 * Empty state component for empty lists
 */
@Composable
fun EmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String? = null,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxSize()
            .padding(CookbookSpacing.xxLarge)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(64.dp)
        )
        
        Spacer(modifier = Modifier.height(CookbookSpacing.medium))
        
        Text(
            text = title,
            style = CookbookTypography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        if (description != null) {
            Spacer(modifier = Modifier.height(CookbookSpacing.small))
            Text(
                text = description,
                style = CookbookTypography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

/**
 * Loading state for lists
 */
@Composable
fun LoadingListState() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(CookbookSpacing.xxLarge)
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 4.dp,
            modifier = Modifier.size(48.dp)
        )
        
        Spacer(modifier = Modifier.height(CookbookSpacing.medium))
        
        Text(
            text = "Loading...",
            style = CookbookTypography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

/**
 * Preview for list components
 */
@Preview(showBackground = true)
@Composable
fun ListsPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier.padding(CookbookSpacing.medium)
        ) {
            // Category chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(CookbookSpacing.small),
                modifier = Modifier.fillMaxWidth()
            ) {
                CategoryChip(
                    category = "Mains",
                    isSelected = true,
                    onClick = {}
                )
                CategoryChip(
                    category = "Desserts",
                    isSelected = false,
                    onClick = {}
                )
                CategoryChip(
                    category = "Breakfasts",
                    isSelected = false,
                    onClick = {}
                )
            }
            
            Spacer(modifier = Modifier.height(CookbookSpacing.medium))
            
            // Section header
            SectionHeader(
                title = "Ingredients",
                actionText = "Add",
                onActionClick = {}
            )
            
            Spacer(modifier = Modifier.height(CookbookSpacing.small))
            
            // Ingredient items
            val sampleIngredient = Ingredient(
                name = "Spaghetti",
                amount = "400g",
                notes = "or any pasta"
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
            
            Spacer(modifier = Modifier.height(CookbookSpacing.medium))
            
            // Instruction steps
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
        }
    }
}
