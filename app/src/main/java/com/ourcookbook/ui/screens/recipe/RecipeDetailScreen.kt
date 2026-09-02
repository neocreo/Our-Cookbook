package com.ourcookbook.ui.screens.recipe

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ourcookbook.R
import com.ourcookbook.ui.components.SectionHeader
import com.ourcookbook.domain.model.Ingredient
import com.ourcookbook.ui.components.SectionHeader
import com.ourcookbook.domain.model.Recipe
import com.ourcookbook.ui.components.SectionHeader
import com.ourcookbook.ui.components.CookbookDeleteDialog
import com.ourcookbook.ui.components.SectionHeader
import com.ourcookbook.ui.components.CookbookIconButton
import com.ourcookbook.ui.components.SectionHeader
import com.ourcookbook.ui.components.CookbookPrimaryButton
import com.ourcookbook.ui.components.SectionHeader
import com.ourcookbook.ui.components.LoadingState
import com.ourcookbook.ui.components.SectionHeader
import com.ourcookbook.ui.navigation.Route
import com.ourcookbook.ui.components.SectionHeader
import com.ourcookbook.ui.theme.CookbookTheme
import com.ourcookbook.ui.components.SectionHeader
import com.ourcookbook.ui.theme.getCategoryColor
import com.ourcookbook.ui.components.SectionHeader
import com.ourcookbook.ui.viewmodel.RecipeDetailAction
import com.ourcookbook.ui.components.SectionHeader
import com.ourcookbook.ui.viewmodel.RecipeDetailEvent
import com.ourcookbook.ui.components.SectionHeader
import com.ourcookbook.ui.viewmodel.RecipeDetailState
import com.ourcookbook.ui.components.SectionHeader
import com.ourcookbook.ui.viewmodel.RecipeDetailViewModel
import com.ourcookbook.ui.components.SectionHeader
import kotlinx.coroutines.launch

/**
 * Recipe Detail Screen
 * Task 2.1.02: Recipe Detail Screen Implementation
 * 
 * Displays detailed information about a specific recipe with full integration
 * with RecipeDetailViewModel, UI components, navigation, and theme.
 * 
 * Features:
 * - Recipe metadata display (title, category, times, servings)
 * - Recipe image with placeholder
 * - Ingredients list with quantities
 * - Step-by-step instructions
 * - Favorite toggle functionality
 * - Edit, delete, share, and print actions
 * - Error and loading states
 * - Responsive design
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    recipeId: String,
    navController: NavController,
    viewModel: RecipeDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val actions by viewModel.actions.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    // State for dialogs
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    
    // Handle actions from ViewModel
    LaunchedEffect(actions) {
        actions?.let { action ->
            when (action) {
                is RecipeDetailAction.ShowEditScreen -> {
                    navController.navigate(Route.recipeEdit(action.recipeId))
                    viewModel.clearAction()
                }
                is RecipeDetailAction.ShowDeleteConfirmation -> {
                    showDeleteDialog = true
                    viewModel.clearAction()
                }
                is RecipeDetailAction.ShowShareDialog -> {
                    showShareDialog = true
                    viewModel.clearAction()
                }
                is RecipeDetailAction.ShowPrintDialog -> {
                    // Handle print action
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Print functionality coming soon")
                    }
                    viewModel.clearAction()
                }
                is RecipeDetailAction.ShowError -> {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(action.message)
                    }
                    viewModel.clearAction()
                }
                RecipeDetailAction.NavigateBack -> {
                    navController.popBackStack()
                    viewModel.clearAction()
                }
                else -> {}
            }
        }
    }
    
    // Load recipe on startup
    LaunchedEffect(recipeId) {
        viewModel.handleEvent(RecipeDetailEvent.LoadRecipe(recipeId))
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            when (val currentState = state) {
                is RecipeDetailState.Success -> {
                    val recipe = currentState.recipe
                    RecipeDetailTopAppBar(
                        recipe = recipe,
                        onBackClick = { navController.popBackStack() },
                        onMenuClick = { showMenu = true },
                        onFavoriteClick = { viewModel.handleEvent(RecipeDetailEvent.ToggleFavorite) },
                        onShareClick = { viewModel.handleEvent(RecipeDetailEvent.ShareRecipe) },
                        showMenu = showMenu,
                        onMenuDismiss = { showMenu = false },
                        onEditClick = { 
                            showMenu = false
                            viewModel.handleEvent(RecipeDetailEvent.EditRecipe)
                        },
                        onDeleteClick = { 
                            showMenu = false
                            viewModel.handleEvent(RecipeDetailEvent.DeleteRecipe)
                        },
                        onPrintClick = { 
                            showMenu = false
                            viewModel.handleEvent(RecipeDetailEvent.PrintRecipe)
                        }
                    )
                }
                else -> {
                    TopAppBar(
                        title = { Text("Recipe") },
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
            }
        }
    ) { paddingValues ->
        when (val currentState = state) {
            is RecipeDetailState.Loading -> {
                LoadingState(
                    message = "Loading recipe...",
                    modifier = Modifier.padding(paddingValues)
                )
            }
            is RecipeDetailState.Error -> {
                ErrorState(
                    message = currentState.message,
                    onRetry = { viewModel.refresh() },
                    modifier = Modifier.padding(paddingValues)
                )
            }
            is RecipeDetailState.NotFound -> {
                NotFoundState(
                    onBack = { navController.popBackStack() },
                    modifier = Modifier.padding(paddingValues)
                )
            }
            is RecipeDetailState.Success -> {
                RecipeDetailContent(
                    recipe = currentState.recipe,
                    onEditClick = { viewModel.handleEvent(RecipeDetailEvent.EditRecipe) },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteDialog) {
        CookbookDeleteDialog(
            title = "Delete Recipe",
            message = "Are you sure you want to delete this recipe? This action cannot be undone.",
            onConfirm = { 
                showDeleteDialog = false
                viewModel.confirmDeleteRecipe()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
    
    // Share dialog
    if (showShareDialog && state is RecipeDetailState.Success) {
        val recipe = (state as RecipeDetailState.Success).recipe
        ShareRecipeDialog(
            recipe = recipe,
            onDismiss = { showShareDialog = false },
            onShare = { shareText ->
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, shareText)
                }
                context.startActivity(Intent.createChooser(intent, "Share Recipe"))
                showShareDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailTopAppBar(
    recipe: Recipe,
    onBackClick: () -> Unit,
    onMenuClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onShareClick: () -> Unit,
    showMenu: Boolean,
    onMenuDismiss: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onPrintClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = recipe.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleLarge
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        actions = {
            // Share button
            IconButton(onClick = onShareClick) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share"
                )
            }
            
            // Favorite button
            IconButton(onClick = onFavoriteClick) {
                Icon(
                    imageVector = if (recipe.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (recipe.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
            }
            
            // Overflow menu
            Box {
                IconButton(onClick = onMenuClick) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options"
                    )
                }
                
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = onMenuDismiss
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit Recipe") },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                        onClick = onEditClick
                    )
                    DropdownMenuItem(
                        text = { Text("Print Recipe") },
                        leadingIcon = { Icon(Icons.Default.Print, contentDescription = null) },
                        onClick = onPrintClick
                    )
                    DropdownMenuItem(
                        text = { Text("Delete Recipe", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = { 
                            Icon(
                                Icons.Default.Delete, 
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        },
                        onClick = onDeleteClick
                    )
                }
            }
        }
    )
}

@Composable
fun RecipeDetailContent(
    recipe: Recipe,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Recipe image
        item {
            RecipeImage(recipe = recipe)
        }
        
        // Recipe header with metadata
        item {
            RecipeHeader(
                recipe = recipe,
                onEditClick = onEditClick
            )
        }
        
        // Ingredients section
        item {
            SectionHeader(title = "Ingredients")
            IngredientsList(ingredients = recipe.ingredients)
        }
        
        // Instructions section
        item {
            SectionHeader(title = "Instructions")
            InstructionsList(instructions = recipe.instructions)
        }
        
        // Additional info section
        item {
            AdditionalInfo(recipe = recipe)
        }
        
        // Spacer at bottom
        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun RecipeImage(recipe: Recipe) {
    val categoryColor = getCategoryColor(recipe.category)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f),
        shape = MaterialTheme.shapes.medium
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            if (!recipe.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(recipe.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = recipe.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(MaterialTheme.shapes.medium),
                    placeholder = painterResource(R.drawable.placeholder_recipe),
                    error = painterResource(R.drawable.error_recipe)
                )
            } else {
                // Placeholder for recipe without image
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            color = categoryColor.copy(alpha = 0.1f),
                            shape = MaterialTheme.shapes.medium
                        ),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Restaurant,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = categoryColor.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No image available",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            // Category badge
            if (!recipe.category.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .padding(16.dp)
                        .background(
                            color = categoryColor.copy(alpha = 0.9f),
                            shape = MaterialTheme.shapes.small
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = recipe.category,
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@Composable
fun RecipeHeader(
    recipe: Recipe,
    onEditClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.headlineMedium
                )
                
                if (!recipe.description.isNullOrBlank()) {
                    Text(
                        text = recipe.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }
        }
        
        // Recipe metadata
        RecipeMetadata(recipe = recipe)
        
        // Rating display
        if (recipe.rating != null && recipe.rating > 0) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Rating",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "%.1f".format(recipe.rating),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun RecipeMetadata(recipe: Recipe) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Serving size
        recipe.servingSize?.let { servingSize ->
            MetadataItem(
                icon = Icons.Outlined.Person,
                text = "Serves: $servingSize",
                modifier = Modifier.weight(1f)
            )
        }
        
        // Prep time
        recipe.prepTime?.let { prepTime ->
            MetadataItem(
                icon = Icons.Outlined.Timer,
                text = "Prep: ${prepTime}min",
                modifier = Modifier.weight(1f)
            )
        }
        
        // Cook time
        recipe.cookTime?.let { cookTime ->
            MetadataItem(
                icon = Icons.Outlined.Timer,
                text = "Cook: ${cookTime}min",
                modifier = Modifier.weight(1f)
            )
        }
        
        // Total time
        recipe.totalTime?.let { totalTime ->
            if (recipe.prepTime == null && recipe.cookTime == null) {
                MetadataItem(
                    icon = Icons.Outlined.Timer,
                    text = "Time: ${totalTime}min",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun MetadataItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
    }
}

@Composable
fun IngredientsList(ingredients: List<Ingredient>) {
    if (ingredients.isEmpty()) {
        Text(
            text = "No ingredients listed",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.padding(vertical = 8.dp)
        )
    } else {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ingredients.forEachIndexed { index, ingredient ->
                IngredientItem(
                    ingredient = ingredient,
                    index = index + 1
                )
            }
        }
    }
}

@Composable
fun IngredientItem(
    ingredient: Ingredient,
    index: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Ingredient number
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = MaterialTheme.shapes.small
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = index.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        // Ingredient details
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = ingredient.name,
                style = MaterialTheme.typography.bodyMedium
            )
            
            val quantityText = buildString {
                if (!ingredient.amount.isNullOrBlank()) {
                    append(ingredient.amount)
                    if (!ingredient.unit.isNullOrBlank()) {
                        append(" ").append(ingredient.unit)
                    }
                }
            }
            
            if (quantityText.isNotBlank()) {
                Text(
                    text = quantityText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            
            if (!ingredient.notes.isNullOrBlank()) {
                Text(
                    text = ingredient.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}

@Composable
fun InstructionsList(instructions: List<String>) {
    if (instructions.isEmpty()) {
        Text(
            text = "No instructions provided",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.padding(vertical = 8.dp)
        )
    } else {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            instructions.forEachIndexed { index, instruction ->
                InstructionItem(
                    number = index + 1,
                    instruction = instruction
                )
            }
        }
    }
}

@Composable
fun InstructionItem(number: Int, instruction: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Step number
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = MaterialTheme.shapes.small
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number.toString(),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
        
        // Instruction text
        Text(
            text = instruction,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun AdditionalInfo(recipe: Recipe) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Notes
        if (!recipe.notes.isNullOrBlank()) {
            SectionHeader(title = "Notes")
            Text(
                text = recipe.notes,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
        
        // Source
        if (!recipe.source.isNullOrBlank()) {
            SectionHeader(title = "Source")
            Text(
                text = recipe.source,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
        
        // Tags
        if (recipe.tags.isNotEmpty()) {
            SectionHeader(title = "Tags")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                recipe.tags.forEach { tag ->
                    Box(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                shape = MaterialTheme.shapes.small
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = tag,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
        
        // Created and updated timestamps
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Created: ${formatDate(recipe.createdAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = "Updated: ${formatDate(recipe.updatedAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
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
            imageVector = Icons.Default.Error,
            contentDescription = "Error",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(64.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Error loading recipe",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.error
        )
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        CookbookPrimaryButton(
            text = "Retry",
            onClick = onRetry
        )
    }
}

@Composable
fun NotFoundState(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Not found",
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
            modifier = Modifier.size(64.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Recipe Not Found",
            style = MaterialTheme.typography.headlineSmall
        )
        
        Text(
            text = "The recipe you're looking for doesn't exist or has been deleted",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        CookbookPrimaryButton(
            text = "Go Back",
            onClick = onBack
        )
    }
}

@Composable
fun ShareRecipeDialog(
    recipe: Recipe,
    onDismiss: () -> Unit,
    onShare: (String) -> Unit
) {
    val shareText = buildString {
        append("📝 ").append(recipe.title).append("\n\n")
        append("📋 Ingredients:\n")
        recipe.ingredients.forEach { ingredient ->
            append("• ").append(ingredient.displayString).append("\n")
        }
        append("\n👨‍🍳 Instructions:\n")
        recipe.instructions.forEachIndexed { index, instruction ->
            append("${index + 1}. ").append(instruction).append("\n")
        }
        append("\n🔗 From Our Cookbook App")
    }
    
    // Immediately share without showing dialog
    LaunchedEffect(Unit) {
        onShare(shareText)
        onDismiss()
    }
}

// Utility function to format date
fun formatDate(instant: java.time.Instant): String {
    val date = java.util.Date.from(instant)
    val format = java.text.SimpleDateFormat.getDateInstance()
    return format.format(date)
}

// Preview for RecipeDetailScreen
@Preview(showBackground = true)
@Composable
fun RecipeDetailScreenPreview() {
    CookbookTheme {
        val mockRecipe = Recipe.create(
            title = "Spaghetti Carbonara",
            category = "Mains",
            description = "Classic Italian pasta dish with eggs, cheese, pancetta, and black pepper",
            ingredients = listOf(
                Ingredient.create("Spaghetti", "400g", "g"),
                Ingredient.create("Pancetta", "150g", "g", "or guanciale"),
                Ingredient.create("Eggs", "3", ""),
                Ingredient.create("Pecorino Romano", "50g", "g", "grated"),
                Ingredient.create("Parmesan", "50g", "g", "grated"),
                Ingredient.create("Black pepper", "1", "tsp", "freshly ground"),
                Ingredient.create("Salt", "", "", "to taste")
            ),
            instructions = listOf(
                "Bring a large pot of salted water to a boil and cook the spaghetti according to package instructions until al dente.",
                "While the pasta is cooking, heat a large skillet over medium heat and cook the pancetta until crispy.",
                "In a bowl, whisk together the eggs, grated Pecorino Romano, Parmesan, and a generous amount of black pepper.",
                "Drain the pasta, reserving some of the pasta water. Immediately add the hot pasta to the skillet with the pancetta and toss to coat.",
                "Remove the skillet from heat and let it cool slightly. Pour the egg and cheese mixture over the pasta and stir quickly to create a creamy sauce. Add a little pasta water if needed to achieve the right consistency.",
                "Serve immediately with additional grated cheese and black pepper."
            ),
            servingSize = 4,
            prepTime = 10,
            cookTime = 15,
            source = "Traditional Italian Recipe",
            tags = listOf("Italian", "Pasta", "Quick", "Classic"),
            deviceId = "preview-device"
        )
        
        RecipeDetailScreen(
            recipeId = mockRecipe.id,
            navController = rememberNavController()
        )
    }
}