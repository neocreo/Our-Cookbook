package com.ourcookbook.ui.screens.recipe

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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ourcookbook.ui.components.CookbookPrimaryButton
import com.ourcookbook.ui.components.LoadingState
import com.ourcookbook.ui.theme.CookbookTheme
import com.ourcookbook.ui.viewmodel.RecipeEditEvent
import com.ourcookbook.ui.viewmodel.RecipeEditState
import com.ourcookbook.ui.viewmodel.RecipeEditViewModel

/**
 * Recipe Edit Screen (Create/Edit Recipe)
 * Task 1.9: Complete Navigation Setup
 * 
 * Handles recipe creation and editing with ingredient management
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeEditScreen(
    viewModel: RecipeEditViewModel,
    navController: NavController
) {
    val state by viewModel.state.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(if (state.isNewRecipe) "Create Recipe" else "Edit Recipe") 
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
                    CookbookPrimaryButton(
                        text = "Save",
                        onClick = { viewModel.handleEvent(RecipeEditEvent.SaveRecipe) },
                        enabled = !state.isSaving
                    )
                }
            )
        }
    ) { paddingValues ->
        when {
            state.isLoading -> {
                LoadingState()
            }
            else -> {
                RecipeEditContent(
                    state = state,
                    onEvent = { event -> viewModel.handleEvent(event) },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
fun RecipeEditContent(
    state: RecipeEditState,
    onEvent: (RecipeEditEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Basic information
        item {
            SectionHeader(title = "Basic Information")
            
            OutlinedTextField(
                value = state.title,
                onValueChange = { onEvent(RecipeEditEvent.UpdateTitle(it)) },
                label = { Text("Title") },
                placeholder = { Text("Recipe title") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = state.description ?: "",
                onValueChange = { onEvent(RecipeEditEvent.UpdateDescription(it)) },
                label = { Text("Description") },
                placeholder = { Text("Brief description of the recipe") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text
                )
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Category dropdown (simplified for now)
            var categoryExpanded by remember { mutableStateOf(false) }
            val categories = listOf("Breakfasts", "Mains", "Desserts & Snacks", "Sides", "Sauces and Spices", "Appetizers", "Soups", "Salads", "Beverages", "Baking")
            
            OutlinedTextField(
                value = state.category,
                onValueChange = { onEvent(RecipeEditEvent.UpdateCategory(it)) },
                label = { Text("Category") },
                placeholder = { Text("Select category") },
                trailingIcon = {
                    IconButton(onClick = { categoryExpanded = !categoryExpanded }) {
                        Icon(
                            imageVector = if (categoryExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                            contentDescription = "Toggle categories"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        // Recipe details
        item {
            SectionHeader(title = "Recipe Details")
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = state.servingSize?.toString() ?: "",
                    onValueChange = { 
                        onEvent(RecipeEditEvent.UpdateServingSize(it.toIntOrNull())) 
                    },
                    label = { Text("Servings") },
                    placeholder = { Text("Number of servings") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    modifier = Modifier.weight(1f)
                )
                
                OutlinedTextField(
                    value = state.prepTime?.toString() ?: "",
                    onValueChange = { 
                        onEvent(RecipeEditEvent.UpdatePrepTime(it.toIntOrNull())) 
                    },
                    label = { Text("Prep Time (min)") },
                    placeholder = { Text("Preparation time") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = state.cookTime?.toString() ?: "",
                    onValueChange = { 
                        onEvent(RecipeEditEvent.UpdateCookTime(it.toIntOrNull())) 
                    },
                    label = { Text("Cook Time (min)") },
                    placeholder = { Text("Cooking time") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    modifier = Modifier.weight(1f)
                )
                
                // Favorite toggle
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Favorite:")
                    Spacer(modifier = Modifier.height(8.dp))
                    IconButton(
                        onClick = { onEvent(RecipeEditEvent.UpdateFavorite(!state.isFavorite)) }
                    ) {
                        Icon(
                            imageVector = if (state.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Toggle Favorite",
                            tint = if (state.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
        
        // Ingredients
        item {
            SectionHeader(title = "Ingredients")
            
            if (state.ingredients.isEmpty()) {
                Text(
                    text = "No ingredients added yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            } else {
                IngredientsList(
                    ingredients = state.ingredients,
                    onDeleteIngredient = { ingredientId ->
                        onEvent(RecipeEditEvent.DeleteIngredient(ingredientId))
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            CookbookPrimaryButton(
                text = "Add Ingredient",
                onClick = { 
                    // In production, this would open an ingredient dialog
                    // For now, add a placeholder ingredient
                    val newIngredient = com.ourcookbook.domain.model.Ingredient(
                        id = "",
                        name = "New Ingredient",
                        quantity = null,
                        unit = null,
                        recipeId = state.recipe?.id ?: "",
                        order = state.ingredients.size
                    )
                    onEvent(RecipeEditEvent.AddIngredient(newIngredient))
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        // Instructions
        item {
            SectionHeader(title = "Instructions")
            
            if (state.instructions.isEmpty()) {
                Text(
                    text = "No instructions added yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            } else {
                InstructionsList(
                    instructions = state.instructions,
                    onDeleteInstruction = { index ->
                        val updatedInstructions = state.instructions.toMutableList().apply {
                            removeAt(index)
                        }
                        onEvent(RecipeEditEvent.UpdateInstructions(updatedInstructions))
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            CookbookPrimaryButton(
                text = "Add Instruction",
                onClick = { 
                    val updatedInstructions = state.instructions.toMutableList().apply {
                        add("")
                    }
                    onEvent(RecipeEditEvent.UpdateInstructions(updatedInstructions))
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        // Additional info
        item {
            SectionHeader(title = "Additional Information")
            
            OutlinedTextField(
                value = state.notes ?: "",
                onValueChange = { onEvent(RecipeEditEvent.UpdateNotes(it)) },
                label = { Text("Notes") },
                placeholder = { Text("Additional notes about the recipe") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = state.source ?: "",
                onValueChange = { onEvent(RecipeEditEvent.UpdateSource(it)) },
                label = { Text("Source") },
                placeholder = { Text("Where did this recipe come from?") },
                modifier = Modifier.fillMaxWidth()
            )
        }
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
fun IngredientsList(
    ingredients: List<com.ourcookbook.domain.model.Ingredient>,
    onDeleteIngredient: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ingredients.forEach { ingredient ->
            IngredientItem(
                ingredient = ingredient,
                onDelete = { onDeleteIngredient(ingredient.id) }
            )
        }
    }
}

@Composable
fun IngredientItem(
    ingredient: com.ourcookbook.domain.model.Ingredient,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
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
            
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Ingredient",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun InstructionsList(
    instructions: List<String>,
    onDeleteInstruction: (Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        instructions.forEachIndexed { index, instruction ->
            InstructionItem(
                number = index + 1,
                instruction = instruction,
                onDelete = { onDeleteInstruction(index) }
            )
        }
    }
}

@Composable
fun InstructionItem(
    number: Int,
    instruction: String,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = number.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Text(
                    text = instruction.ifBlank { "Add instruction text" },
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Instruction",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun RecipeCreateScreen(
    viewModel: RecipeEditViewModel,
    navController: NavController
) {
    // For create screen, we just use the same edit screen but with a new recipe
    LaunchedEffect(Unit) {
        viewModel.handleEvent(RecipeEditEvent.LoadRecipe(null))
    }
    
    RecipeEditScreen(
        viewModel = viewModel,
        navController = navController
    )
}

@Preview(showBackground = true)
@Composable
fun RecipeEditScreenPreview() {
    CookbookTheme {
        RecipeEditScreen(
            viewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
            navController = rememberNavController()
        )
    }
}
