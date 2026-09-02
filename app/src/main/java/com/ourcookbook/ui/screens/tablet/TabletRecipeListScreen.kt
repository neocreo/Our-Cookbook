@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.ourcookbook.ui.screens.tablet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ourcookbook.domain.model.Recipe
import com.ourcookbook.ui.components.ResponsiveAppBar
import com.ourcookbook.ui.components.ResponsiveNavigation
import com.ourcookbook.ui.components.ResponsiveNavItem
import com.ourcookbook.ui.navigation.Route
import com.ourcookbook.ui.theme.CookbookColors
import com.ourcookbook.ui.theme.CookbookSpacing
import com.ourcookbook.ui.theme.ResponsiveGrid
import com.ourcookbook.ui.theme.ScreenSize
import com.ourcookbook.ui.viewmodel.RecipeListEvent
import com.ourcookbook.ui.viewmodel.RecipeListState
import com.ourcookbook.ui.viewmodel.RecipeListViewModel

/**
 * Tablet-optimized Recipe List Screen
 * Task 2.2.10: Responsive design for tablets
 *
 * Features:
 * - Multi-column grid layout for tablets
 * - Navigation rail for tablet navigation
 * - Larger card sizes and spacing
 * - Split-pane preview on large screens
 */

@Composable
fun TabletRecipeListScreen(
    navController: NavController,
    viewModel: RecipeListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val recipes = (state as? RecipeListState.Success)?.recipes ?: emptyList()

    // Use navigation rail for tablets
    val navigationItems = listOf(
        ResponsiveNavItem(
            route = "recipes",
            label = "Recipes",
            icon = Icons.Default.Home
        ),
        ResponsiveNavItem(
            route = "categories",
            label = "Categories",
            icon = Icons.Default.Category
        ),
        ResponsiveNavItem(
            route = "favorites",
            label = "Favorites",
            icon = Icons.Default.Favorite
        ),
        ResponsiveNavItem(
            route = "settings",
            label = "Settings",
            icon = Icons.Default.Settings
        )
    )
    
    // For tablets, use a navigation rail + main content layout
    Row(
        modifier = Modifier.fillMaxSize()
    ) {
        // Navigation rail
        ResponsiveNavigation(
            navController = navController,
            items = navigationItems,
            modifier = Modifier.width(80.dp)
        )
        
        // Main content area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = CookbookSpacing.small)
        ) {
            TabletRecipeContent(
                recipes = recipes,
                onRecipeClick = { recipeId ->
                    navController.navigate(Route.recipeDetail(recipeId))
                },
                onFavoriteClick = { recipeId ->
                    viewModel.handleEvent(RecipeListEvent.ToggleFavorite(recipeId))
                },
                navController = navController
            )
        }
    }
}

/**
 * Tablet-specific recipe content with multi-column grid
 */
@Composable
fun TabletRecipeContent(
    recipes: List<Recipe>,
    onRecipeClick: (String) -> Unit,
    onFavoriteClick: (String) -> Unit,
    navController: NavController
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // App bar
        TabletAppBar(
            onSearchClick = { /* TODO */ },
            onSettingsClick = { /* TODO */ }
        )
        
        // Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(CookbookSpacing.medium)
        ) {
            RecipeGrid(
                recipes = recipes,
                onRecipeClick = onRecipeClick,
                onFavoriteClick = onFavoriteClick
            )
        }
    }
}

/**
 * Tablet-optimized app bar
 */
@Composable
fun TabletAppBar(
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    
    TopAppBar(
        title = {
            Text(
                text = "Our Cookbook",
                style = MaterialTheme.typography.headlineMedium
            )
        },
        actions = {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search recipes...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                },
                modifier = Modifier
                    .width(300.dp)
                    .padding(vertical = CookbookSpacing.xSmall)
            )
            
            Spacer(modifier = Modifier.width(CookbookSpacing.small))
            
            IconButton(onClick = onSettingsClick) {
                Icon(Icons.Default.Settings, contentDescription = "Settings")
            }
        },
        colors = TopAppBarDefaults.largeTopAppBarColors()
    )
}

/**
 * Multi-column recipe grid for tablets
 */
@Composable
fun RecipeGrid(
    recipes: List<Recipe>,
    onRecipeClick: (String) -> Unit,
    onFavoriteClick: (String) -> Unit
) {
    val columns = when (ScreenSize.current()) {
        ScreenSize.COMPACT -> 1
        ScreenSize.MEDIUM -> 2
        ScreenSize.EXPANDED -> 3
    }
    
    val spacing = CookbookSpacing.medium
    
    LazyVerticalGrid(
        modifier = Modifier.fillMaxSize(),
        columns = GridCells.Fixed(columns),
        state = rememberLazyGridState(),
        contentPadding = PaddingValues(all = CookbookSpacing.small),
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        items(recipes) { recipe ->
            RecipeCard(
                recipe = recipe,
                onClick = { onRecipeClick(recipe.id) },
                onFavoriteClick = { onFavoriteClick(recipe.id) }
            )
        }
    }
}

/**
 * Tablet-optimized recipe card
 */
@Composable
fun RecipeCard(
    recipe: Recipe,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .width(300.dp)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            // Image placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(CookbookColors.cream)
            ) {
                // Favorite button
                IconButton(
                    onClick = { onFavoriteClick() },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(CookbookSpacing.xSmall)
                        .size(36.dp)
                ) {
                    Icon(
                        if (recipe.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Toggle favorite",
                        tint = if (recipe.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                }
                
                // Category badge
                if (recipe.category.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(CookbookSpacing.small)
                            .background(
                                CookbookColors.categoryColors[recipe.category] ?: MaterialTheme.colorScheme.primary,
                                MaterialTheme.shapes.small
                            )
                            .padding(CookbookSpacing.xSmall)
                    ) {
                        Text(
                            text = recipe.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                }
            }
            
            // Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(CookbookSpacing.medium)
            ) {
                // Title
                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(CookbookSpacing.xSmall))
                
                // Metadata
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Servings and time
                    val metadataParts = mutableListOf<String>()
                    recipe.servingSize?.let { metadataParts.add("$it servings") }
                    recipe.totalTime?.let { metadataParts.add("$it min") }
                    
                    if (metadataParts.isNotEmpty()) {
                        Text(
                            text = metadataParts.joinToString(" | "),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    
                    // Rating
                    val ratingValue = recipe.rating
                    if (ratingValue != null && ratingValue > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            repeat(5) { index ->
                                Icon(
                                    painter = painterResource(
                                        if (index < ratingValue.toInt()) {
                                            android.R.drawable.star_on
                                        } else {
                                            android.R.drawable.star_off
                                        }
                                    ),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(CookbookSpacing.xSmall))
                
                // Description preview
                if (!recipe.description.isNullOrBlank()) {
                    Text(
                        text = recipe.description ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
                
                Spacer(modifier = Modifier.height(CookbookSpacing.xSmall))
                
                // Tags
                if (recipe.tags.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        recipe.tags.take(3).forEach { tag ->
                            Box(
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        MaterialTheme.shapes.extraSmall
                                    )
                                    .padding(CookbookSpacing.xxSmall)
                            ) {
                                Text(
                                    text = tag,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.width(CookbookSpacing.xSmall))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Split-pane layout for large tablets
 */
@Composable
fun SplitPaneLayout(
    leftContent: @Composable () -> Unit,
    rightContent: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(CookbookSpacing.medium)
    ) {
        // Left pane (list)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            leftContent()
        }
        
        // Right pane (detail)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            rightContent()
        }
    }
}

/**
 * Tablet recipe detail preview (for split-pane)
 */
@Composable
fun RecipeDetailPreview(
    recipe: Recipe?,
    modifier: Modifier = Modifier
) {
    if (recipe == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Select a recipe to view details",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
        return
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(CookbookSpacing.medium)
    ) {
        // Title and metadata
        Column {
            Text(
                text = recipe.title,
                style = MaterialTheme.typography.headlineSmall
            )
            
            Spacer(modifier = Modifier.height(CookbookSpacing.small))
            
            // Metadata
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    recipe.servingSize?.let { servingSize ->
                        Text(
                            text = "Servings: $servingSize",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    recipe.prepTime?.let { prepTime ->
                        Text(
                            text = "Prep: $prepTime min",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                
                Column {
                    recipe.cookTime?.let { cookTime ->
                        Text(
                            text = "Cook: $cookTime min",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    recipe.totalTime?.let { totalTime ->
                        Text(
                            text = "Total: $totalTime min",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(CookbookSpacing.medium))
        
        // Image placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(CookbookColors.cream)
        )
        
        Spacer(modifier = Modifier.height(CookbookSpacing.medium))
        
        // Ingredients
        Text(
            text = "Ingredients",
            style = MaterialTheme.typography.titleMedium
        )
        
        Spacer(modifier = Modifier.height(CookbookSpacing.small))
        
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(CookbookSpacing.xSmall)
        ) {
            recipe.ingredients.forEach { ingredient ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = ingredient.name,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = ingredient.amount ?: "",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(CookbookSpacing.medium))
        
        // Instructions
        Text(
            text = "Instructions",
            style = MaterialTheme.typography.titleMedium
        )
        
        Spacer(modifier = Modifier.height(CookbookSpacing.small))
        
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(CookbookSpacing.xSmall)
        ) {
            recipe.instructions.forEachIndexed { index, instruction ->
                Text(
                    text = "${index + 1}. $instruction",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

/**
 * Preview for tablet recipe list screen
 */
@Preview(showBackground = true)
@Composable
fun TabletRecipeListScreenPreview() {
    val mockRecipes = listOf(
        Recipe.create(
            title = "Spaghetti Carbonara",
            category = "Mains",
            description = "Classic Italian pasta dish with eggs, cheese, and pancetta",
            ingredients = listOf(
                com.ourcookbook.domain.model.Ingredient.create("Spaghetti", "400g", null, null, 0),
                com.ourcookbook.domain.model.Ingredient.create("Eggs", "4", null, null, 1),
                com.ourcookbook.domain.model.Ingredient.create("Pancetta", "150g", null, null, 2)
            ),
            instructions = listOf("Cook spaghetti", "Fry pancetta", "Mix eggs and cheese", "Combine all ingredients"),
            servingSize = 4,
            prepTime = 10,
            cookTime = 15,
            tags = listOf("Italian", "Pasta", "Quick"),
            deviceId = ""
        ),
        Recipe.create(
            title = "Chocolate Cake",
            category = "Desserts",
            description = "Rich and moist chocolate cake with chocolate frosting",
            ingredients = listOf(
                com.ourcookbook.domain.model.Ingredient.create("Flour", "200g", null, null, 0),
                com.ourcookbook.domain.model.Ingredient.create("Sugar", "200g", null, null, 1),
                com.ourcookbook.domain.model.Ingredient.create("Cocoa powder", "50g", null, null, 2)
            ),
            instructions = listOf("Mix dry ingredients", "Add wet ingredients", "Bake at 180C for 30 minutes"),
            servingSize = 8,
            prepTime = 20,
            cookTime = 30,
            tags = listOf("Dessert", "Chocolate", "Cake"),
            deviceId = ""
        )
    )
    
    MaterialTheme {
        TabletRecipeListScreen(
            navController = rememberNavController(),
            viewModel = androidx.lifecycle.viewmodel.compose.viewModel()
        )
    }
}
