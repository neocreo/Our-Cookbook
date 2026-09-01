package com.ourcookbook.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ourcookbook.domain.model.Recipe
import com.ourcookbook.ui.viewmodel.HomeViewModel
import com.ourcookbook.ui.viewmodel.HomeState
import com.ourcookbook.ui.components.CookbookCard
import com.ourcookbook.ui.viewmodel.HomeViewModel
import com.ourcookbook.ui.viewmodel.HomeState
import com.ourcookbook.ui.components.CookbookPrimaryButton
import com.ourcookbook.ui.viewmodel.HomeViewModel
import com.ourcookbook.ui.viewmodel.HomeState
import com.ourcookbook.ui.components.EmptyState
import com.ourcookbook.ui.viewmodel.HomeViewModel
import com.ourcookbook.ui.viewmodel.HomeState
import com.ourcookbook.ui.components.LoadingState
import com.ourcookbook.ui.viewmodel.HomeViewModel
import com.ourcookbook.ui.viewmodel.HomeState
import com.ourcookbook.ui.components.QuickActionButton
import com.ourcookbook.ui.viewmodel.HomeViewModel
import com.ourcookbook.ui.viewmodel.HomeState
import com.ourcookbook.ui.components.RecipeCard
import com.ourcookbook.ui.viewmodel.HomeViewModel
import com.ourcookbook.ui.viewmodel.HomeState
import com.ourcookbook.ui.components.SectionHeader
import com.ourcookbook.ui.viewmodel.HomeViewModel
import com.ourcookbook.ui.viewmodel.HomeState
import com.ourcookbook.ui.navigation.Route
import com.ourcookbook.ui.viewmodel.HomeViewModel
import com.ourcookbook.ui.viewmodel.HomeState
import com.ourcookbook.ui.theme.CookbookSpacing
import com.ourcookbook.ui.viewmodel.HomeViewModel
import com.ourcookbook.ui.viewmodel.HomeState
import com.ourcookbook.ui.theme.CookbookTheme
import com.ourcookbook.ui.viewmodel.HomeViewModel
import com.ourcookbook.ui.viewmodel.HomeState

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    navController: NavController
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    Scaffold(
        topBar = {
            HomeTopAppBar(
                onSearchClick = { navController.navigate(Route.SEARCH) },
                onSyncClick = { navController.navigate(Route.SYNC_STATUS) }
            )
        },
        bottomBar = {
            HomeBottomNavigation(
                currentRoute = Route.HOME,
                onNavigate = { route -> navController.navigate(route) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Route.RECIPE_CREATE) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = CookbookSpacing.large,
                    pressedElevation = CookbookSpacing.xLarge
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Recipe"
                )
            }
        }
    ) { paddingValues ->
        when {
            state.isLoading -> LoadingState()
            state.error != null -> ErrorState(
                message = state.error,
                onRetry = { viewModel.loadData() }
            )
            else -> HomeContent(
                state = state,
                onRecipeClick = { recipeId ->
                    navController.navigate("${Route.RECIPE_DETAIL}/${recipeId}")
                },
                onCategoryClick = { category ->
                    navController.navigate("${Route.RECIPE_LIST}?category=$category")
                },
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
fun HomeContent(
    state: HomeState,
    onRecipeClick: (String) -> Unit,
    onCategoryClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(CookbookSpacing.medium),
        verticalArrangement = Arrangement.spacedBy(CookbookSpacing.medium)
    ) {
        // Quick actions
        item {
            QuickActionsSection(
                onNewRecipeClick = { /* Handled by FAB */ },
                onScanClick = { /* navController.navigate(Route.OCR_SCANNER) */ },
                onImportClick = { /* navController.navigate("${Route.RECIPE_LIST}?import=true") */ },
                onSyncClick = { /* navController.navigate(Route.SYNC_STATUS) */ }
            )
        }

        // Recent recipes
        item {
            SectionHeader(
                title = "Recent Recipes",
                actionText = "View All",
                onActionClick = { /* navController.navigate(Route.RECIPE_LIST) */ }
            )
        }

        if (state.recentRecipes.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Default.Search,
                    title = "No recent recipes",
                    description = "Add your first recipe to get started"
                )
            }
        } else {
            items(state.recentRecipes, key = { it.id }) { recipe ->
                RecipeCard(
                    recipe = recipe,
                    onClick = { onRecipeClick(recipe.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = CookbookSpacing.xSmall)
                )
            }
        }

        // Categories
        item {
            SectionHeader(title = "Categories")
        }

        item {
            CategoryGrid(
                categories = state.categories,
                onCategoryClick = onCategoryClick
            )
        }

        // Favorites
        if (state.favorites.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Favorites",
                    actionText = "View All",
                    onActionClick = { /* navController.navigate("${Route.RECIPE_LIST}?favorites=true") */ }
                )
            }

            items(state.favorites, key = { it.id }) { recipe ->
                RecipeCard(
                    recipe = recipe,
                    onClick = { onRecipeClick(recipe.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = CookbookSpacing.xSmall)
                )
            }
        }
    }
}

@Composable
fun QuickActionsSection(
    onNewRecipeClick: () -> Unit,
    onScanClick: () -> Unit,
    onImportClick: () -> Unit,
    onSyncClick: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(CookbookSpacing.medium),
        modifier = Modifier.fillMaxWidth()
    ) {
        QuickActionButton(
            icon = Icons.Default.Add,
            label = "New Recipe",
            onClick = onNewRecipeClick
        )
        QuickActionButton(
            icon = Icons.Default.Camera,
            label = "Scan Recipe",
            onClick = onScanClick
        )
        QuickActionButton(
            icon = Icons.Default.FileUpload,
            label = "Import",
            onClick = onImportClick
        )
        QuickActionButton(
            icon = Icons.Default.Sync,
            label = "Sync",
            onClick = onSyncClick
        )
    }
}

@Composable
fun CategoryGrid(
    categories: List<String>,
    onCategoryClick: (String) -> Unit
) {
    // Simple grid implementation - will be enhanced with proper grid layout
    Column(
        verticalArrangement = Arrangement.spacedBy(CookbookSpacing.small),
        modifier = Modifier.fillMaxWidth()
    ) {
        val allCategories = listOf("Breakfasts", "Mains", "Desserts & Snacks", "Sides", "Sauces and Spices")
        
        allCategories.forEach { category ->
            CookbookPrimaryButton(
                text = category,
                onClick = { onCategoryClick(category) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun HomeTopAppBar(
    onSearchClick: () -> Unit,
    onSyncClick: () -> Unit
) {
    // Top app bar implementation
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(CookbookSpacing.medium)
    ) {
        Text(
            text = "Our Cookbook",
            style = MaterialTheme.typography.titleLarge
        )
        
        Row(
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onSearchClick) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search"
                )
            }
            IconButton(onClick = onSyncClick) {
                Icon(
                    imageVector = Icons.Default.Sync,
                    contentDescription = "Sync"
                )
            }
        }
    }
}

@Composable
fun HomeBottomNavigation(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    // Bottom navigation implementation
    Row(
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(CookbookSpacing.medium)
    ) {
        listOf(
            "Home" to Route.HOME,
            "Recipes" to Route.RECIPE_LIST,
            "Search" to Route.SEARCH,
            "Cookbooks" to Route.COOKBOOK_MANAGEMENT,
            "Settings" to Route.SETTINGS
        ).forEach { (label, route) ->
            CookbookPrimaryButton(
                text = label,
                onClick = { onNavigate(route) },
                modifier = Modifier.weight(1f),
                enabled = currentRoute != route
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    CookbookTheme {
        HomeScreen(navController = rememberNavController())
    }
}
