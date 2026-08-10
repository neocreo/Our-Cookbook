package com.ourcookbook.ui.screens.recipe

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ourcookbook.ui.components.EmptyState
import com.ourcookbook.ui.components.LoadingState
import com.ourcookbook.ui.components.RecipeCard
import com.ourcookbook.ui.navigation.Route
import com.ourcookbook.ui.theme.CookbookTheme
import com.ourcookbook.ui.viewmodel.RecipeListEvent
import com.ourcookbook.ui.viewmodel.RecipeListState
import com.ourcookbook.ui.viewmodel.RecipeListViewModel

/**
 * Recipe List Screen
 * Task 1.9: Complete Navigation Setup
 * 
 * Displays a list of recipes with filtering and sorting options
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeListScreen(
    viewModel: RecipeListViewModel,
    navController: NavController
) {
    val state by viewModel.state.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.handleEvent(RecipeListEvent.LoadRecipes)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recipes") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Route.SEARCH) }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Route.RECIPE_CREATE) }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Recipe"
                )
            }
        }
    ) { paddingValues ->
        when (val currentState = state) {
            is RecipeListState.Loading -> {
                LoadingState()
            }
            is RecipeListState.Error -> {
                ErrorState(
                    message = currentState.message,
                    onRetry = { viewModel.handleEvent(RecipeListEvent.LoadRecipes) }
                )
            }
            is RecipeListState.Empty -> {
                EmptyState(
                    icon = Icons.Default.Search,
                    title = "No recipes found",
                    description = "Add your first recipe to get started"
                )
            }
            is RecipeListState.Success -> {
                RecipeListContent(
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
fun RecipeListContent(
    recipes: List<com.ourcookbook.domain.model.Recipe>,
    onRecipeClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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
            text = "Error loading recipes",
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
fun RecipeListScreenPreview() {
    CookbookTheme {
        RecipeListScreen(
            viewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
            navController = rememberNavController()
        )
    }
}
