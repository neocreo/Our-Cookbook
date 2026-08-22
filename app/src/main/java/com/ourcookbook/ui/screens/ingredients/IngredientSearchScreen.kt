package com.ourcookbook.ui.screens.ingredients

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ourcookbook.domain.model.Recipe
import com.ourcookbook.ui.components.CookbookFilterChip
import com.ourcookbook.ui.components.CompactRecipeCard
import com.ourcookbook.ui.components.EmptyState
import com.ourcookbook.ui.components.LoadingState
import com.ourcookbook.ui.components.RecipeCard
import com.ourcookbook.ui.navigation.Route
import com.ourcookbook.ui.theme.CookbookTheme
import com.ourcookbook.ui.viewmodel.IngredientSearchEvent
import com.ourcookbook.ui.viewmodel.IngredientSearchState
import com.ourcookbook.ui.viewmodel.IngredientSearchViewModel
import kotlinx.coroutines.launch

/**
 * Ingredient Search Screen
 * Task 2.2.05: Ingredient-based Search Implementation
 *
 * Allows users to find recipes by selecting ingredients they have available
 * Shows matching recipes ranked by how many selected ingredients they contain
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun IngredientSearchScreen(
    viewModel: IngredientSearchViewModel = hiltViewModel(),
    navController: NavController
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ingredient Search") },
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
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val currentState = state) {
                is IngredientSearchState.Idle -> {
                    IngredientSearchIdleContent(
                        onSearch = { viewModel.handleEvent(IngredientSearchEvent.StartSearch) }
                    )
                }
                is IngredientSearchState.SelectingIngredients -> {
                    IngredientSelectionContent(
                        query = currentState.query,
                        suggestions = currentState.suggestions,
                        selectedIngredients = currentState.selectedIngredients,
                        onQueryChange = { query ->
                            viewModel.handleEvent(IngredientSearchEvent.QueryChanged(query))
                        },
                        onSelectIngredient = { ingredient ->
                            viewModel.handleEvent(IngredientSearchEvent.SelectIngredient(ingredient))
                        },
                        onRemoveIngredient = { ingredient ->
                            viewModel.handleEvent(IngredientSearchEvent.RemoveIngredient(ingredient))
                        },
                        onSearch = { viewModel.handleEvent(IngredientSearchEvent.StartSearch) },
                        onClear = { viewModel.handleEvent(IngredientSearchEvent.ClearSelection) }
                    )
                }
                is IngredientSearchState.Searching -> {
                    LoadingState()
                }
                is IngredientSearchState.Results -> {
                    IngredientSearchResultsContent(
                        recipes = currentState.recipes,
                        selectedIngredients = currentState.selectedIngredients,
                        matchScores = currentState.matchScores,
                        onRecipeClick = { recipeId ->
                            navController.navigate(Route.recipeDetail(recipeId))
                        },
                        onBack = { viewModel.handleEvent(IngredientSearchEvent.ClearSelection) }
                    )
                }
                is IngredientSearchState.Error -> {
                    EmptyState(
                        icon = Icons.Default.Close,
                        title = "Error",
                        message = currentState.message,
                        onRetry = { viewModel.handleEvent(IngredientSearchEvent.Retry) }
                    )
                }
                is IngredientSearchState.Empty -> {
                    EmptyState(
                        icon = Icons.Default.Search,
                        title = "No Recipes Found",
                        message = "No recipes match the selected ingredients. Try different ingredients.",
                        onRetry = { viewModel.handleEvent(IngredientSearchEvent.ClearSelection) }
                    )
                }
            }
        }
    }
}

/**
 * Idle content for ingredient search
 */
@Composable
fun IngredientSearchIdleContent(
    onSearch: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Ingredient Search",
            modifier = Modifier.size(96.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Find Recipes by Ingredients",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Tell us what ingredients you have, and we'll find recipes that match!",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.padding(horizontal = 32.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onSearch,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Start"
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Start Searching")
        }
    }
}

/**
 * Ingredient selection content
 */
@Composable
fun IngredientSelectionContent(
    query: String,
    suggestions: List<String>,
    selectedIngredients: List<String>,
    onQueryChange: (String) -> Unit,
    onSelectIngredient: (String) -> Unit,
    onRemoveIngredient: (String) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Search field for ingredient input
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            label = { Text("Add ingredients") },
            placeholder = { Text("Type an ingredient (e.g., tomatoes, chicken, flour)") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search ingredients"
                )
            },
            trailingIcon = {
                if (query.isNotBlank()) {
                    IconButton(onClick = { onQueryChange("") }) {
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

        // Suggestions
        if (suggestions.isNotEmpty() && query.isNotBlank()) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Suggestions:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(16.dp)
                    )
                    
                    suggestions.take(5).forEach { suggestion ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .height(48.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = suggestion,
                                modifier = Modifier.weight(1f)
                            )
                            
                            Button(
                                onClick = { onSelectIngredient(suggestion) }
                            ) {
                                Text("Add")
                            }
                        }
                        
                        if (suggestions.indexOf(suggestion) < 4) {
                            Divider(
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Selected ingredients
        if (selectedIngredients.isNotEmpty()) {
            Text(
                text = "Selected Ingredients:",
                style = MaterialTheme.typography.titleMedium
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                selectedIngredients.forEach { ingredient ->
                    CookbookFilterChip(
                        label = ingredient,
                        isSelected = true,
                        onClick = { onRemoveIngredient(ingredient) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (selectedIngredients.isNotEmpty()) {
                Button(
                    onClick = onClear,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Clear All")
                }
            }
            
            Button(
                onClick = onSearch,
                enabled = selectedIngredients.isNotEmpty(),
                modifier = Modifier.weight(1f)
            ) {
                Text("Find Recipes")
            }
        }
    }
}

/**
 * Search results content
 */
@Composable
fun IngredientSearchResultsContent(
    recipes: List<Recipe>,
    selectedIngredients: List<String>,
    matchScores: Map<String, Float>,
    onRecipeClick: (String) -> Unit,
    onBack: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header with selected ingredients
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Searching with:",
                    style = MaterialTheme.typography.titleMedium
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    selectedIngredients.forEach { ingredient ->
                        CookbookFilterChip(
                            label = ingredient,
                            isSelected = true,
                            onClick = {}
                        )
                    }
                }

                Button(
                    onClick = onBack,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Change Ingredients")
                }
            }
        }

        // Results
        if (recipes.isEmpty()) {
            EmptyState(
                icon = Icons.Default.Search,
                title = "No Recipes Found",
                message = "No recipes match the selected ingredients.",
                onRetry = onBack
            )
        } else {
            val gridColumns = if (isTablet) 3 else 2

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(recipes) { recipe ->
                    val recipeId = recipe.id
                    val matchScore = matchScores[recipeId] ?: 0f
                    
                    ElevatedCard(
                        onClick = { onRecipeClick(recipeId) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Title and match percentage
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = recipe.title,
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Spacer(modifier = Modifier.weight(1f))

                                // Match percentage badge
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = when {
                                                matchScore >= 0.75f -> MaterialTheme.colorScheme.primaryContainer
                                                matchScore >= 0.5f -> MaterialTheme.colorScheme.secondaryContainer
                                                else -> MaterialTheme.colorScheme.surfaceVariant
                                            },
                                            shape = MaterialTheme.shapes.small
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "${(matchScore * 100).toInt()}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = when {
                                            matchScore >= 0.75f -> MaterialTheme.colorScheme.onPrimaryContainer
                                            matchScore >= 0.5f -> MaterialTheme.colorScheme.onSecondaryContainer
                                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                }
                            }

                            // Category
                            if (recipe.category.isNotBlank()) {
                                Text(
                                    text = recipe.category,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }

                            // Matching ingredients
                            val recipeIngredientNames = recipe.ingredients.map { it.name.lowercase() }
                            val selectedLower = selectedIngredients.map { it.lowercase() }
                            val matchingIngredients = recipeIngredientNames.filter { it in selectedLower }

                            if (matchingIngredients.isNotEmpty()) {
                                Text(
                                    text = "Matches: ${matchingIngredients.joinToString(", ")}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Divider()

                            // Recipe metadata
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                recipe.prepTime?.let { prepTime ->
                                    Text(
                                        text = "Prep: ${prepTime}min",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }

                                recipe.cookTime?.let { cookTime ->
                                    Text(
                                        text = "Cook: ${cookTime}min",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }

                                recipe.servingSize?.let { servings ->
                                    Text(
                                        text = "Serves: $servings",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Preview for IngredientSearchScreen
 */
@Preview(showBackground = true)
@Composable
fun IngredientSearchScreenPreview() {
    CookbookTheme {
        Surface(
            modifier = Modifier.fillMaxSize()
        ) {
            IngredientSearchScreen(
                navController = rememberNavController()
            )
        }
    }
}
