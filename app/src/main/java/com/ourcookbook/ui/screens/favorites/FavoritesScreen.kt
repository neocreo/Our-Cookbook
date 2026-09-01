package com.ourcookbook.ui.screens.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ourcookbook.ui.components.EmptyState
import com.ourcookbook.ui.components.LoadingState
import com.ourcookbook.ui.components.RecipeCard
import com.ourcookbook.ui.navigation.Route
import com.ourcookbook.ui.theme.CookbookTheme
import com.ourcookbook.ui.viewmodel.RecipeListViewModel

/**
 * Favorites Screen
 * Task 1.9: Complete Navigation Setup
 * 
 * Displays a list of user's favorite recipes
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    viewModel: RecipeListViewModel,
    navController: NavController
) {
    val state by viewModel.state.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Favorites") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val currentState = state) {
            is com.ourcookbook.ui.viewmodel.RecipeListState.Loading -> {
                LoadingState()
            }
            is com.ourcookbook.ui.viewmodel.RecipeListState.Error -> {
                ErrorState(
                    message = currentState.message,
                    onRetry = { viewModel.handleEvent(com.ourcookbook.ui.viewmodel.RecipeListEvent.LoadRecipes) }
                )
            }
            is com.ourcookbook.ui.viewmodel.RecipeListState.Empty -> {
                EmptyState(
                    icon = Icons.Default.Favorite,
                    title = "No favorites yet",
                    description = "Tap the heart icon on recipes to add them to your favorites"
                )
            }
            is com.ourcookbook.ui.viewmodel.RecipeListState.Success -> {
                FavoritesContent(
                    recipes = currentState.recipes,
                    onRecipeClick = { recipeId ->
                        navController.navigate(Route.recipeDetail(recipeId))
                    },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
fun FavoritesContent(
    recipes: List<com.ourcookbook.domain.model.Recipe>,
    onRecipeClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Your Favorite Recipes (${recipes.size})",
                style = MaterialTheme.typography.titleMedium
            )
        }
        
        items(recipes, key = { it.id }) { recipe ->
            RecipeCard(
                recipe = recipe,
                onClick = { onRecipeClick(recipe.id) },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Error loading favorites",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.error
        )
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium
        )
        
        // Retry button would go here
    }
}

@Preview(showBackground = true)
@Composable
fun FavoritesScreenPreview() {
    CookbookTheme {
        FavoritesScreen(
            viewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
            navController = rememberNavController()
        )
    }
}
