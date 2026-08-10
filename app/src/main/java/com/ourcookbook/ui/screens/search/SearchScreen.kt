package com.ourcookbook.ui.screens.search

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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ourcookbook.domain.model.Recipe
import com.ourcookbook.ui.components.CookbookFilterChip
import com.ourcookbook.ui.components.CookbookSearchField
import com.ourcookbook.ui.components.CompactRecipeCard
import com.ourcookbook.ui.components.EmptyState
import com.ourcookbook.ui.components.LoadingState
import com.ourcookbook.ui.components.RecipeCard
import com.ourcookbook.ui.navigation.Route
import com.ourcookbook.ui.theme.CookbookTheme
import com.ourcookbook.ui.viewmodel.SearchEvent
import com.ourcookbook.ui.viewmodel.SearchSortOption
import com.ourcookbook.ui.viewmodel.SearchState
import com.ourcookbook.ui.viewmodel.SearchViewModel
import kotlinx.coroutines.launch

/**
 * Search Screen
 * Task 2.1.04: Search Screen Implementation
 * 
 * Handles recipe search with advanced filtering and sorting options:
 * - Full-text search across recipe titles, ingredients, and descriptions
 * - Real-time search as user types
 * - Category filtering (Breakfasts, Mains, Desserts & Snacks, Sides, Sauces and Spices)
 * - Tag filtering
 * - Favorites-only toggle
 * - Advanced filter options (cooking time, serving size)
 * - Sorting options (relevance, title, date, rating, cook time)
 * 
 * Integrates with:
 * - CookbookSearchField from Task 1.8
 * - CookbookFilterChip from Task 1.8
 * - RecipeCard/CompactRecipeCard from Task 1.8
 * - LoadingState, EmptyState, ErrorState from Task 1.8
 * - RecipeListViewModel from Task 1.7
 * - Navigation routes from Task 1.9
 * - Theme from Task 1.10
 */

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    navController: NavController
) {
    val state by viewModel.state.collectAsState()
    val actions by viewModel.actions.collectAsState()
    val configuration = LocalConfiguration.current
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    
    // Handle navigation actions
    LaunchedEffect(actions) {
        actions?.let { action ->
            when (action) {
                is com.ourcookbook.ui.viewmodel.SearchAction.ShowRecipeDetail -> {
                    navController.navigate(Route.recipeDetail(action.recipeId))
                    viewModel.clearAction()
                }
                is com.ourcookbook.ui.viewmodel.SearchAction.NavigateToCreateRecipe -> {
                    navController.navigate(Route.RECIPE_CREATE)
                    viewModel.clearAction()
                }
                else -> {}
            }
        }
    }
    
    // Filter drawer state
    var showFilterDrawer by remember { mutableStateOf(false) }
    
    // Sort menu state
    var showSortMenu by remember { mutableStateOf(false) }
    
    // Search query state
    var searchQuery by remember { mutableStateOf("") }
    
    LaunchedEffect(Unit) {
        viewModel.handleEvent(SearchEvent.ClearSearch)
    }
    
    // Handle filter drawer from ViewModel actions
    LaunchedEffect(actions) {
        actions?.let { action ->
            when (action) {
                com.ourcookbook.ui.viewmodel.SearchAction.ShowFilterOptions -> {
                    showFilterDrawer = true
                    viewModel.clearAction()
                }
                com.ourcookbook.ui.viewmodel.SearchAction.HideFilterOptions -> {
                    showFilterDrawer = false
                    viewModel.clearAction()
                }
                else -> {}
            }
        }
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
                onFilter = { showFilterDrawer = true },
                onSort = { showSortMenu = true },
                currentSortOption = state.sortOption
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Active filters display
                if (state.hasActiveFilters) {
                    ActiveFiltersDisplay(
                        state = state,
                        onClearCategory = { category ->
                            viewModel.handleEvent(SearchEvent.DeselectCategory(category))
                        },
                        onClearTag = { tag ->
                            viewModel.handleEvent(SearchEvent.DeselectTag(tag))
                        },
                        onClearCookingTime = {
                            viewModel.handleEvent(SearchEvent.SetMaxCookingTime(null))
                        },
                        onClearServingSize = {
                            viewModel.handleEvent(SearchEvent.SetServingSizeRange(null, null))
                        },
                        onClearFavorites = {
                            viewModel.handleEvent(SearchEvent.SetFavoritesOnly(false))
                        },
                        onClearAll = {
                            viewModel.handleEvent(SearchEvent.ClearFilters)
                        },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Divider(modifier = Modifier.padding(horizontal = 16.dp))
                }
                
                // Search results or states
                when {
                    state.isLoading && state.recipes.isEmpty() -> {
                        LoadingState(
                            message = "Searching recipes...",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    state.error != null -> {
                        ErrorState(
                            message = state.error,
                            onRetry = { viewModel.refresh() },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    state.isEmpty -> {
                        EmptyState(
                            icon = Icons.Default.Search,
                            title = "Search for recipes",
                            description = "Enter a search term or use filters to find recipes",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    state.isNoResults -> {
                        EmptyState(
                            icon = Icons.Default.Search,
                            title = "No results found",
                            description = "Try a different search term or adjust your filters",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    else -> {
                        SearchResults(
                            recipes = state.recipes,
                            onRecipeClick = { recipeId ->
                                viewModel.navigateToRecipeDetail(recipeId)
                            },
                            modifier = Modifier.weight(1f),
                            isTablet = configuration.screenWidthDp >= 600
                        )
                    }
                }
            }
            
            // Filter bottom sheet
            if (showFilterDrawer) {
                FilterBottomSheet(
                    state = state,
                    onCategoryToggle = { category ->
                        if (state.selectedCategories.contains(category)) {
                            viewModel.handleEvent(SearchEvent.DeselectCategory(category))
                        } else {
                            viewModel.handleEvent(SearchEvent.SelectCategory(category))
                        }
                    },
                    onTagToggle = { tag ->
                        if (state.selectedTags.contains(tag)) {
                            viewModel.handleEvent(SearchEvent.DeselectTag(tag))
                        } else {
                            viewModel.handleEvent(SearchEvent.SelectTag(tag))
                        }
                    },
                    onMaxCookingTimeChange = { minutes ->
                        viewModel.handleEvent(SearchEvent.SetMaxCookingTime(minutes))
                    },
                    onServingSizeRangeChange = { min, max ->
                        viewModel.handleEvent(SearchEvent.SetServingSizeRange(min, max))
                    },
                    onFavoritesOnlyChange = { showFavorites ->
                        viewModel.handleEvent(SearchEvent.SetFavoritesOnly(showFavorites))
                    },
                    onApply = {
                        showFilterDrawer = false
                        viewModel.performSearch()
                    },
                    onDismiss = {
                        showFilterDrawer = false
                    },
                    sheetState = sheetState,
                    scope = scope
                )
            }
            
            // Sort menu
            if (showSortMenu) {
                SortDropdownMenu(
                    currentSortOption = state.sortOption,
                    onSortOptionSelected = { sortOption ->
                        viewModel.handleEvent(SearchEvent.SetSortOption(sortOption))
                        showSortMenu = false
                    },
                    onDismiss = { showSortMenu = false },
                    viewModel = viewModel
                )
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
    onFilter: () -> Unit,
    onSort: () -> Unit,
    currentSortOption: SearchSortOption,
    viewModel: SearchViewModel
) {
    TopAppBar(
        title = {
            CookbookSearchField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = "Search recipes...",
                onClear = onClear,
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
            // Sort button
            IconButton(onClick = onSort) {
                Icon(
                    imageVector = Icons.Default.Sort,
                    contentDescription = "Sort"
                )
            }
            
            // Filter button
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
    recipes: List<Recipe>,
    onRecipeClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    isTablet: Boolean = false
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Search Results (${recipes.size})",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        items(recipes, key = { it.id }) { recipe ->
            if (isTablet) {
                // For tablets, use a more compact layout
                CompactRecipeCard(
                    recipe = recipe,
                    onClick = { onRecipeClick(recipe.id) },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                // For phones, use the full recipe card
                RecipeCard(
                    recipe = recipe,
                    onClick = { onRecipeClick(recipe.id) },
                    modifier = Modifier.fillMaxWidth(),
                    showFavorite = recipe.isFavorite,
                    isFavorite = recipe.isFavorite
                )
            }
        }
    }
}

@Composable
fun ActiveFiltersDisplay(
    state: SearchState,
    onClearCategory: (String) -> Unit,
    onClearTag: (String) -> Unit,
    onClearCookingTime: () -> Unit,
    onClearServingSize: () -> Unit,
    onClearFavorites: () -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Active Filters",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Category filters
            state.selectedCategories.forEach { category ->
                FilterChipWithClose(
                    label = category,
                    onClose = { onClearCategory(category) }
                )
            }
            
            // Tag filters
            state.selectedTags.forEach { tag ->
                FilterChipWithClose(
                    label = tag,
                    onClose = { onClearTag(tag) }
                )
            }
            
            // Favorites filter
            if (state.showFavoritesOnly) {
                FilterChipWithClose(
                    label = "Favorites Only",
                    onClose = onClearFavorites
                )
            }
            
            // Cooking time filter
            state.maxCookingTime?.let { maxTime ->
                FilterChipWithClose(
                    label = "≤ ${maxTime}min",
                    onClose = onClearCookingTime
                )
            }
            
            // Serving size filter
            val (minServings, maxServings) = state.servingSizeRange
            if (minServings != null || maxServings != null) {
                val label = when {
                    minServings != null && maxServings != null -> "${minServings}-${maxServings} servings"
                    minServings != null -> "≥ ${minServings} servings"
                    else -> "≤ ${maxServings} servings"
                }
                FilterChipWithClose(
                    label = label,
                    onClose = onClearServingSize
                )
            }
            
            // Clear all button
            if (state.hasActiveFilters) {
                OutlinedButton(
                    onClick = onClearAll,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text("Clear All")
                }
            }
        }
    }
}

@Composable
fun FilterChipWithClose(
    label: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier,
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall
            )
            IconButton(
                onClick = onClose,
                modifier = Modifier.size(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove filter",
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Error",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(64.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Search Error",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(onClick = onRetry) {
            Text("Try Again")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    state: SearchState,
    onCategoryToggle: (String) -> Unit,
    onTagToggle: (String) -> Unit,
    onMaxCookingTimeChange: (Int?) -> Unit,
    onServingSizeRangeChange: (Int?, Int?) -> Unit,
    onFavoritesOnlyChange: (Boolean) -> Unit,
    onApply: () -> Unit,
    onDismiss: () -> Unit,
    sheetState: androidx.compose.material3.SheetState,
    scope: kotlinx.coroutines.CoroutineScope
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { 
            Surface(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .width(40.dp)
                    .height(4.dp),
                shape = MaterialTheme.shapes.full,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            ) {}
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Filter Recipes",
                style = MaterialTheme.typography.headlineSmall
            )
            
            // Categories section
            FilterSection(
                title = "Categories",
                items = state.categories,
                selectedItems = state.selectedCategories,
                onItemToggle = onCategoryToggle
            )
            
            Divider()
            
            // Tags section
            FilterSection(
                title = "Tags",
                items = state.availableTags,
                selectedItems = state.selectedTags,
                onItemToggle = onTagToggle
            )
            
            Divider()
            
            // Cooking time section
            CookingTimeFilter(
                maxCookingTime = state.maxCookingTime,
                onMaxCookingTimeChange = onMaxCookingTimeChange
            )
            
            Divider()
            
            // Serving size section
            ServingSizeFilter(
                servingSizeRange = state.servingSizeRange,
                onServingSizeRangeChange = onServingSizeRangeChange
            )
            
            Divider()
            
            // Favorites only toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = state.showFavoritesOnly,
                    onCheckedChange = onFavoritesOnlyChange
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Favorites Only",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Action buttons
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            onDismiss()
                        }
                    }
                ) {
                    Text("Cancel")
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Button(
                    onClick = {
                        onApply()
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            onDismiss()
                        }
                    }
                ) {
                    Text("Apply Filters")
                }
            }
        }
    }
}

@Composable
fun FilterSection(
    title: String,
    items: List<String>,
    selectedItems: List<String>,
    onItemToggle: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items.forEach { item ->
                CookbookFilterChip(
                    label = item,
                    isSelected = selectedItems.contains(item),
                    onClick = { onItemToggle(item) },
                    category = if (title == "Categories") item else null
                )
            }
        }
    }
}

@Composable
fun CookingTimeFilter(
    maxCookingTime: Int?,
    onMaxCookingTimeChange: (Int?) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Maximum Cooking Time",
            style = MaterialTheme.typography.titleMedium
        )
        
        val maxTime = maxCookingTime ?: 120f
        
        Slider(
            value = maxTime,
            onValueChange = { newValue ->
                onMaxCookingTimeChange(newValue.toInt())
            },
            valueRange = 0f..180f,
            steps = 17,
            modifier = Modifier.fillMaxWidth()
        )
        
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("0 min")
            Text("${maxTime.toInt()} min")
        }
    }
}

@Composable
fun ServingSizeFilter(
    servingSizeRange: Pair<Int?, Int?>,
    onServingSizeRangeChange: (Int?, Int?) -> Unit
) {
    val (minServings, maxServings) = servingSizeRange
    
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Serving Size",
            style = MaterialTheme.typography.titleMedium
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Minimum",
                    style = MaterialTheme.typography.labelSmall
                )
                Spacer(modifier = Modifier.height(4.dp))
                
                val minValue = minServings?.toFloat() ?: 0f
                Slider(
                    value = minValue,
                    onValueChange = { newValue ->
                        onServingSizeRangeChange(newValue.toInt(), maxServings)
                    },
                    valueRange = 0f..20f,
                    steps = 19,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("${minValue.toInt()} servings")
            }
            
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Maximum",
                    style = MaterialTheme.typography.labelSmall
                )
                Spacer(modifier = Modifier.height(4.dp))
                
                val maxValue = maxServings?.toFloat() ?: 20f
                Slider(
                    value = maxValue,
                    onValueChange = { newValue ->
                        onServingSizeRangeChange(minServings, newValue.toInt())
                    },
                    valueRange = 0f..20f,
                    steps = 19,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("${maxValue.toInt()} servings")
            }
        }
    }
}

@Composable
fun SortDropdownMenu(
    currentSortOption: SearchSortOption,
    onSortOptionSelected: (SearchSortOption) -> Unit,
    onDismiss: () -> Unit,
    viewModel: SearchViewModel
) {
    DropdownMenu(
        expanded = true,
        onDismissRequest = onDismiss,
        modifier = Modifier.width(Dp(200f))
    ) {
        viewModel.getAllSortOptions().forEach { sortOption ->
            DropdownMenuItem(
                text = { 
                    Text(viewModel.getSortOptionDisplayName(sortOption))
                },
                onClick = {
                    onSortOptionSelected(sortOption)
                    onDismiss()
                },
                leadingIcon = {
                    if (sortOption == currentSortOption) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Current sort option",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
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
