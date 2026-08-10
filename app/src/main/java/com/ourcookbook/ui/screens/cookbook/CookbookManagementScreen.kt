package com.ourcookbook.ui.screens.cookbook

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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ourcookbook.ui.components.CookbookCard
import com.ourcookbook.ui.components.CookbookPrimaryButton
import com.ourcookbook.ui.components.EmptyState
import com.ourcookbook.ui.components.LoadingState
import com.ourcookbook.ui.navigation.Route
import com.ourcookbook.ui.theme.CookbookTheme
import com.ourcookbook.ui.viewmodel.CookbookManagementEvent
import com.ourcookbook.ui.viewmodel.CookbookManagementState
import com.ourcookbook.ui.viewmodel.CookbookManagementViewModel

/**
 * Cookbook Management Screen
 * Task 1.9: Complete Navigation Setup
 * 
 * Displays and manages user's cookbooks
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookbookManagementScreen(
    viewModel: CookbookManagementViewModel,
    navController: NavController
) {
    val state by viewModel.state.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.handleEvent(CookbookManagementEvent.LoadCookbooks)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cookbooks") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Route.COOKBOOK_CREATE) }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Cookbook"
                )
            }
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
            is CookbookManagementState.Empty -> {
                EmptyState(
                    icon = Icons.Default.Add,
                    title = "No cookbooks yet",
                    description = "Create your first cookbook to organize your recipes"
                )
            }
            is CookbookManagementState.Success -> {
                CookbookManagementContent(
                    state = currentState,
                    onCookbookClick = { cookbookId ->
                        navController.navigate(Route.cookbookDetail(cookbookId))
                    },
                    onCreateCookbook = { 
                        navController.navigate(Route.COOKBOOK_CREATE) 
                    },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
fun CookbookManagementContent(
    state: CookbookManagementState.Success,
    onCookbookClick: (String) -> Unit,
    onCreateCookbook: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // My Cookbooks section
        item {
            SectionHeader(
                title = "My Cookbooks",
                count = state.cookbooks.size
            )
        }
        
        if (state.cookbooks.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Default.Add,
                    title = "No personal cookbooks",
                    description = "Create a cookbook to organize your recipes"
                )
            }
        } else {
            items(state.cookbooks, key = { it.id }) { cookbook ->
                CookbookCard(
                    cookbook = cookbook,
                    onClick = { onCookbookClick(cookbook.id) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        
        // Shared Cookbooks section
        item {
            SectionHeader(
                title = "Shared Cookbooks",
                count = state.sharedCookbooks.size
            )
        }
        
        if (state.sharedCookbooks.isEmpty()) {
            item {
                Text(
                    text = "No shared cookbooks available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        } else {
            items(state.sharedCookbooks, key = { it.id }) { cookbook ->
                CookbookCard(
                    cookbook = cookbook,
                    onClick = { onCookbookClick(cookbook.id) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        
        // Create cookbook button
        item {
            Spacer(modifier = Modifier.height(16.dp))
            CookbookPrimaryButton(
                text = "Create New Cookbook",
                onClick = onCreateCookbook,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun CookbookListScreen(
    viewModel: CookbookManagementViewModel,
    navController: NavController
) {
    // For now, just redirect to the management screen
    // This could be a simplified list view in the future
    LaunchedEffect(Unit) {
        navController.navigate(Route.COOKBOOK_MANAGEMENT) {
            popUpTo(Route.COOKBOOK_LIST) { inclusive = true }
        }
    }
}

@Composable
fun CookbookDetailScreen(
    viewModel: CookbookManagementViewModel,
    cookbookId: String,
    navController: NavController
) {
    val state by viewModel.state.collectAsState()
    
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
            SectionHeader(
                title = "Recipes",
                count = recipes.size
            )
        }
        
        if (recipes.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Default.Add,
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
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = cookbook.name,
            style = MaterialTheme.typography.headlineMedium
        )
        
        if (!cookbook.description.isNullOrBlank()) {
            Text(
                text = cookbook.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Created: ${cookbook.createdAt}",
                style = MaterialTheme.typography.bodySmall
            )
            
            Text(
                text = "Updated: ${cookbook.updatedAt}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun CookbookEditScreen(
    viewModel: CookbookManagementViewModel,
    cookbookId: String? = null,
    isCreating: Boolean,
    navController: NavController
) {
    val state by viewModel.state.collectAsState()
    
    // For now, implement a simple edit screen
    // This would be enhanced in a future task
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(if (isCreating) "Create Cookbook" else "Edit Cookbook") 
                },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (isCreating) "Create a new cookbook" else "Edit cookbook",
                style = MaterialTheme.typography.headlineSmall
            )
            
            // Cookbook edit form would go here
            // For now, just show a message
            Text(
                text = "Cookbook editing functionality will be implemented in a future task.",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            CookbookPrimaryButton(
                text = if (isCreating) "Create Cookbook" else "Save Changes",
                onClick = { 
                    if (isCreating) {
                        viewModel.showCreateCookbookDialog()
                    } else {
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun SectionHeader(title: String, count: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = "$count",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
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
            text = "Error loading cookbooks",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.error
        )
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        CookbookPrimaryButton(
            text = "Retry",
            onClick = onRetry
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CookbookManagementScreenPreview() {
    CookbookTheme {
        CookbookManagementScreen(
            viewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
            navController = rememberNavController()
        )
    }
}
