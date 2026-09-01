package com.ourcookbook.ui.screens.cookbook

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ourcookbook.ui.components.CookbookCard
import com.ourcookbook.ui.components.EmptyState
import com.ourcookbook.ui.components.LoadingState
import com.ourcookbook.ui.navigation.Route
import com.ourcookbook.ui.theme.CookbookTheme
import com.ourcookbook.ui.viewmodel.CookbookManagementEvent
import com.ourcookbook.ui.viewmodel.CookbookManagementState
import com.ourcookbook.ui.viewmodel.CookbookManagementViewModel

/**
 * Cookbook Detail Screen
 * Task 1.9: Complete Navigation Setup
 * 
 * Displays detailed information about a specific cookbook
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookbookDetailScreen(
    viewModel: CookbookManagementViewModel,
    cookbookId: String,
    navController: NavController
) {
    val state by viewModel.state.collectAsState()
    
    LaunchedEffect(cookbookId) {
        viewModel.handleEvent(CookbookManagementEvent.SelectCookbook(cookbookId))
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    when (val currentState = state) {
                        is CookbookManagementState.Success -> {
                            currentState.selectedCookbook?.let { cookbook ->
                                Text(cookbook.name)
                            } ?: Text("Cookbook")
                        }
                        else -> Text("Cookbook")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    when (val currentState = state) {
                        is CookbookManagementState.Success -> {
                            currentState.selectedCookbook?.let { cookbook ->
                                IconButton(onClick = { 
                                    navController.navigate(Route.cookbookEdit(cookbook.id))
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowForward,
                                        contentDescription = "Edit"
                                    )
                                }
                            }
                        }
                        else -> {}
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val currentState = state) {
            is CookbookManagementState.Loading -> {
                LoadingState()
            }
            is CookbookManagementState.Error -> {
                ErrorState(
                    message = currentState.message,
                    onRetry = { viewModel.handleEvent(CookbookManagementEvent.LoadCookbooks) }
                )
            }
            is CookbookManagementState.Success -> {
                currentState.selectedCookbook?.let { cookbook ->
                    CookbookDetailContent(
                        cookbook = cookbook,
                        recipes = currentState.recipesInSelectedCookbook,
                        onRecipeClick = { recipeId ->
                            navController.navigate(Route.recipeDetail(recipeId))
                        },
                        modifier = Modifier.padding(paddingValues)
                    )
                } ?: run {
                    EmptyState(
                        icon = Icons.Default.ArrowBack,
                        title = "Cookbook not found",
                        description = "The selected cookbook doesn't exist"
                    )
                }
            }
            is CookbookManagementState.Empty -> {
                EmptyState(
                    icon = Icons.Default.ArrowBack,
                    title = "No cookbook selected",
                    description = "Please select a cookbook"
                )
            }
        }
    }
}

@Composable
fun CookbookDetailContent(
    cookbook: com.ourcookbook.domain.model.Cookbook,
    recipes: List<com.ourcookbook.domain.model.Recipe>,
    onRecipeClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Cookbook header
        item {
            CookbookHeader(cookbook = cookbook)
        }
        
        // Recipes in cookbook
        item {
            Text(
                text = "Recipes (${recipes.size})",
                style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                color = androidx.compose.material3.MaterialTheme.colorScheme.primary
            )
        }
        
        if (recipes.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Default.ArrowForward,
                    title = "No recipes in this cookbook",
                    description = "Add recipes to this cookbook"
                )
            }
        } else {
            items(recipes, key = { it.id }) { recipe ->
                com.ourcookbook.ui.components.RecipeCard(
                    recipe = recipe,
                    onClick = { onRecipeClick(recipe.id) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun CookbookHeader(cookbook: com.ourcookbook.domain.model.Cookbook) {
    androidx.compose.foundation.layout.Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = cookbook.name,
            style = androidx.compose.material3.MaterialTheme.typography.headlineMedium
        )
        
        if (!cookbook.description.isNullOrBlank()) {
            Text(
                text = cookbook.description,
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
fun CookbookDetailScreenPreview() {
    CookbookTheme {
        // Preview would need proper setup
        Text("Cookbook Detail Screen Preview")
    }
}
