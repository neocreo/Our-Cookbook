package com.ourcookbook.ui.screens.search

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ourcookbook.ui.components.EmptyState
import com.ourcookbook.ui.components.LoadingState
import com.ourcookbook.ui.components.RecipeCard
import com.ourcookbook.ui.navigation.Route
import com.ourcookbook.ui.theme.CookbookTheme
import com.ourcookbook.ui.viewmodel.SearchEvent
import com.ourcookbook.ui.viewmodel.SearchState
import com.ourcookbook.ui.viewmodel.SearchViewModel

/**
 * Search Screen
 * Task 1.9: Complete Navigation Setup
 * 
 * Handles recipe search with advanced filtering options
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    navController: NavController
) {
    val state by viewModel.state.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    
    LaunchedEffect(Unit) {
        viewModel.handleEvent(SearchEvent.ClearSearch)
    }
    
    Scaffold(
        topBar = {
            SearchTopAppBar(
                query = searchQuery,
                onQueryChange = { 
                    searchQuery = it
                    viewModel.handleEvent(SearchEvent.UpdateQuery(it))
                },
                onClear = {
                    searchQuery = ""
                    viewModel.handleEvent(SearchEvent.ClearSearch)
                },
                onBack = { navController.popBackStack() },
                onFilter = { viewModel.showFilterOptions() }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when {
                state.isLoading && state.recipes.isEmpty() -> {
                    LoadingState()
                }
                state.error != null -> {
                    ErrorState(
                        message = state.error,
                        onRetry = { viewModel.refresh() }
                    )
                }
                state.recipes.isEmpty() && searchQuery.isNotBlank() -> {
                    EmptyState(
                        icon = Icons.Default.Search,
                        title = "No results found",
                        description = "Try a different search term"
                    )
                }
                state.recipes.isEmpty() -> {
                    EmptyState(
                        icon = Icons.Default.Search,
                        title = "Search for recipes",
                        description = "Enter a search term to find recipes"
                    )
                }
                else -> {
                    SearchResults(
                        recipes = state.recipes,
                        onRecipeClick = { recipeId ->
                            navController.navigate(Route.recipeDetail(recipeId))
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTopAppBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    onBack: () -> Unit,
    onFilter: () -> Unit
) {
    TopAppBar(
        title = {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text("Search recipes...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                },
                trailingIcon = {
                    if (query.isNotBlank()) {
                        IconButton(onClick = onClear) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear"
                            )
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        actions = {
            IconButton(onClick = onFilter) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Filter"
                )
            }
        }
    )
}

@Composable
fun SearchResults(
    recipes: List<com.ourcookbook.domain.model.Recipe>,
    onRecipeClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Search Results (${recipes.size})",
                style = MaterialTheme.typography.titleMedium
            )
        }
        
        items(recipes, key = { it.id }) { recipe ->
            RecipeCard(
                recipe = recipe,
                onClick = { onRecipeClick(recipe.id) },
                modifier = Modifier.fillMaxWidth()
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
            text = "Search Error",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.error
        )
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Retry button would go here
    }
}

@Preview(showBackground = true)
@Composable
fun SearchScreenPreview() {
    CookbookTheme {
        SearchScreen(
            viewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
            navController = rememberNavController()
        )
    }
}
