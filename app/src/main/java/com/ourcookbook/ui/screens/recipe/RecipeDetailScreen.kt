package com.ourcookbook.ui.screens.recipe

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ourcookbook.ui.components.CookbookPrimaryButton
import com.ourcookbook.ui.components.LoadingState
import com.ourcookbook.ui.navigation.Route
import com.ourcookbook.ui.theme.CookbookTheme
import com.ourcookbook.ui.viewmodel.RecipeDetailEvent
import com.ourcookbook.ui.viewmodel.RecipeDetailState
import com.ourcookbook.ui.viewmodel.RecipeDetailViewModel

/**
 * Recipe Detail Screen
 * Task 1.9: Complete Navigation Setup
 * 
 * Displays detailed information about a specific recipe
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    viewModel: RecipeDetailViewModel,
    navController: NavController
) {
    val state by viewModel.state.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    when (val currentState = state) {
                        is RecipeDetailState.Success -> Text(currentState.recipe.title)
                        else -> Text("Recipe")
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
                        is RecipeDetailState.Success -> {
                            val recipe = currentState.recipe
                            IconButton(onClick = { 
                                viewModel.handleEvent(RecipeDetailEvent.ShareRecipe) 
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share"
                                )
                            }
                            IconButton(onClick = { 
                                viewModel.handleEvent(RecipeDetailEvent.ToggleFavorite) 
                            }) {
                                Icon(
                                    imageVector = if (recipe.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Favorite",
                                    tint = if (recipe.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        else -> {}
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val currentState = state) {
            is RecipeDetailState.Loading -> {
                LoadingState()
            }
            is RecipeDetailState.Error -> {
                ErrorState(
                    message = currentState.message,
                    onRetry = { viewModel.refresh() }
                )
            }
            is RecipeDetailState.NotFound -> {
                NotFoundState(
                    onBack = { navController.popBackStack() }
                )
            }
            is RecipeDetailState.Success -> {
                RecipeDetailContent(
                    recipe = currentState.recipe,
                    onEditClick = { 
                        navController.navigate(Route.recipeEdit(currentState.recipe.id))
                    },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
fun RecipeDetailContent(
    recipe: com.ourcookbook.domain.model.Recipe,
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
        
        // Recipe header
        item {
            RecipeHeader(
                recipe = recipe,
                onEditClick = onEditClick
            )
        }
        
        // Ingredients
        item {
            SectionHeader(title = "Ingredients")
            IngredientsList(ingredients = recipe.ingredients)
        }
        
        // Instructions
        item {
            SectionHeader(title = "Instructions")
            InstructionsList(instructions = recipe.instructions)
        }
        
        // Additional info
        item {
            AdditionalInfo(recipe = recipe)
        }
    }
}

@Composable
fun RecipeImage(recipe: com.ourcookbook.domain.model.Recipe) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
    ) {
        if (!recipe.imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(recipe.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = recipe.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Placeholder for recipe without image
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    modifier = Modifier.height(64.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No image available",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun RecipeHeader(
    recipe: com.ourcookbook.domain.model.Recipe,
    onEditClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.headlineMedium
                )
                
                if (!recipe.category.isNullOrBlank()) {
                    Text(
                        text = recipe.category,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            IconButton(onClick = onEditClick) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Recipe"
                )
            }
        }
        
        if (!recipe.description.isNullOrBlank()) {
            Text(
                text = recipe.description,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        
        // Recipe metadata
        RecipeMetadata(recipe = recipe)
    }
}

@Composable
fun RecipeMetadata(recipe: com.ourcookbook.domain.model.Recipe) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        recipe.servingSize?.let { servingSize ->
            MetadataItem(
                icon = Icons.Default.FavoriteBorder, // Placeholder icon
                text = "Serves: $servingSize"
            )
        }
        
        recipe.prepTime?.let { prepTime ->
            MetadataItem(
                icon = Icons.Default.FavoriteBorder, // Placeholder icon
                text = "Prep: ${prepTime}min"
            )
        }
        
        recipe.cookTime?.let { cookTime ->
            MetadataItem(
                icon = Icons.Default.FavoriteBorder, // Placeholder icon
                text = "Cook: ${cookTime}min"
            )
        }
    }
}

@Composable
fun MetadataItem(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.height(16.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary
    )
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
fun IngredientsList(ingredients: List<com.ourcookbook.domain.model.Ingredient>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (ingredients.isEmpty()) {
            Text(
                text = "No ingredients listed",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        } else {
            ingredients.forEach { ingredient ->
                IngredientItem(ingredient = ingredient)
            }
        }
    }
}

@Composable
fun IngredientItem(ingredient: com.ourcookbook.domain.model.Ingredient) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Checkbox would go here
        Spacer(modifier = Modifier.height(24.dp))
        
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = ingredient.name,
                style = MaterialTheme.typography.bodyMedium
            )
            
            if (!ingredient.quantity.isNullOrBlank() && !ingredient.unit.isNullOrBlank()) {
                Text(
                    text = "${ingredient.quantity} ${ingredient.unit}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun InstructionsList(instructions: List<String>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (instructions.isEmpty()) {
            Text(
                text = "No instructions provided",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        } else {
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
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = number.toString(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = instruction,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun AdditionalInfo(recipe: com.ourcookbook.domain.model.Recipe) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (!recipe.notes.isNullOrBlank()) {
            SectionHeader(title = "Notes")
            Text(
                text = recipe.notes,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        
        if (!recipe.source.isNullOrBlank()) {
            SectionHeader(title = "Source")
            Text(
                text = recipe.source,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        
        if (recipe.tags.isNotEmpty()) {
            SectionHeader(title = "Tags")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                recipe.tags.forEach { tag ->
                    CookbookPrimaryButton(
                        text = tag,
                        onClick = { /* Tag click action */ },
                        modifier = Modifier.height(32.dp)
                    )
                }
            }
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
            text = "Error loading recipe",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.error
        )
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium
        )
        
        CookbookPrimaryButton(
            text = "Retry",
            onClick = onRetry
        )
    }
}

@Composable
fun NotFoundState(
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Recipe Not Found",
            style = MaterialTheme.typography.headlineSmall
        )
        
        Text(
            text = "The recipe you're looking for doesn't exist",
            style = MaterialTheme.typography.bodyMedium
        )
        
        CookbookPrimaryButton(
            text = "Go Back",
            onClick = onBack
        )
    }
}

@Preview(showBackground = true)
@Composable
fun RecipeDetailScreenPreview() {
    CookbookTheme {
        // Preview with mock data
        val mockRecipe = com.ourcookbook.domain.model.Recipe.create(
            title = "Sample Recipe",
            category = "Desserts",
            description = "A delicious sample recipe"
        )
        
        // This would need a proper viewModel setup for preview
        // For now, just show the content
        RecipeDetailContent(
            recipe = mockRecipe,
            onEditClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
