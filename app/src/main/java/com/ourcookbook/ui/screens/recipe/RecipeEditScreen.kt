@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.ourcookbook.ui.screens.recipe

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.ourcookbook.R
import com.ourcookbook.domain.model.Ingredient
import com.ourcookbook.domain.model.Recipe
import com.ourcookbook.ui.components.CookbookAssistChip
import com.ourcookbook.ui.components.CookbookFilterChip
import com.ourcookbook.ui.components.CookbookIconButton
import com.ourcookbook.ui.components.CookbookMultilineTextField
import com.ourcookbook.ui.components.CookbookNumberField
import com.ourcookbook.ui.components.CookbookPrimaryButton
import com.ourcookbook.ui.components.CookbookSecondaryButton
import com.ourcookbook.ui.components.CookbookTextButton
import com.ourcookbook.ui.components.CookbookTextField
import com.ourcookbook.ui.components.LoadingState
import com.ourcookbook.ui.components.TagChip
import com.ourcookbook.ui.components.TagInputChip
import com.ourcookbook.ui.navigation.Route
import com.ourcookbook.ui.theme.CookbookColors
import com.ourcookbook.ui.theme.CookbookSpacing
import com.ourcookbook.ui.theme.CookbookTheme
import com.ourcookbook.ui.theme.CookbookTypography
import com.ourcookbook.ui.theme.getCategoryColor
import com.ourcookbook.ui.viewmodel.RecipeEditAction
import com.ourcookbook.ui.viewmodel.RecipeEditEvent
import com.ourcookbook.ui.viewmodel.RecipeEditState
import com.ourcookbook.ui.viewmodel.RecipeEditViewModel
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

/**
 * Recipe Edit Screen (Create/Edit Recipe)
 * Task 2.1.03: Recipe Create/Edit Screen Implementation
 * 
 * Complete implementation of the Recipe Create/Edit screen with:
 * - Form validation
 * - Ingredient management
 * - Instruction steps
 * - Image capture/selection
 * - Category/tag selection
 * - Full integration with RecipeEditViewModel
 * - Compliance with cookbook-android-architecture.md
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeEditScreen(
    viewModel: RecipeEditViewModel = hiltViewModel(),
    navController: NavController,
    recipeId: String? = null
) {
    val state by viewModel.state.collectAsState()
    val actions by viewModel.actions.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // Handle recipe loading
    LaunchedEffect(recipeId) {
        viewModel.handleEvent(RecipeEditEvent.LoadRecipe(recipeId))
    }
    
    // Handle actions from ViewModel
    actions?.let { action ->
        when (action) {
            is RecipeEditAction.ShowValidationError -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = action.errors.joinToString(", ")
                    )
                }
                viewModel.clearAction()
            }
            is RecipeEditAction.ShowError -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = action.message
                    )
                }
                viewModel.clearAction()
            }
            is RecipeEditAction.NavigateToRecipeDetail -> {
                navController.navigate(Route.recipeDetail(action.recipeId)) {
                    popUpTo(Route.RECIPE_CREATE) { inclusive = true }
                    popUpTo(Route.RECIPE_EDIT) { inclusive = true }
                }
                viewModel.clearAction()
            }
            is RecipeEditAction.NavigateBack -> {
                navController.popBackStack()
                viewModel.clearAction()
            }
        }
    }
    
    Scaffold(
        topBar = {
            RecipeEditTopAppBar(
                state = state,
                onBackClick = { navController.popBackStack() },
                onSaveClick = { viewModel.handleEvent(RecipeEditEvent.SaveRecipe) },
                onValidateClick = { viewModel.handleEvent(RecipeEditEvent.ValidateRecipe) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        when {
            state.isLoading -> {
                LoadingState()
            }
            else -> {
                RecipeEditContent(
                    state = state,
                    onEvent = { event -> viewModel.handleEvent(event) },
                    modifier = Modifier.padding(paddingValues),
                    context = context
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeEditTopAppBar(
    state: RecipeEditState,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit,
    onValidateClick: () -> Unit
) {
    TopAppBar(
        title = { 
            Text(
                text = if (state.isNewRecipe) "Create Recipe" else "Edit Recipe",
                style = CookbookTypography.headlineSmall
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
            if (state.isSaving) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                CookbookTextButton(
                    text = "Validate",
                    onClick = onValidateClick,
                    modifier = Modifier.padding(end = CookbookSpacing.small)
                )
                CookbookPrimaryButton(
                    text = "Save",
                    onClick = onSaveClick,
                    enabled = !state.isSaving,
                    modifier = Modifier.padding(end = CookbookSpacing.small)
                )
            }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RecipeEditContent(
    state: RecipeEditState,
    onEvent: (RecipeEditEvent) -> Unit,
    modifier: Modifier = Modifier,
    context: Context
) {
    val lazyListState = rememberLazyListState()
    
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(CookbookSpacing.medium),
        verticalArrangement = Arrangement.spacedBy(CookbookSpacing.medium),
        state = lazyListState
    ) {
        // Header with image
        item {
            RecipeImageSection(
                imageUrl = state.imageUrl,
                onImageChange = { newImageUrl ->
                    onEvent(RecipeEditEvent.UpdateImageUrl(newImageUrl))
                },
                context = context
            )
        }
        
        // Basic Information Section
        item {
            RecipeSectionHeader(
                title = "Basic Information",
                icon = Icons.Default.Info
            )
            
            Column(
                verticalArrangement = Arrangement.spacedBy(CookbookSpacing.small)
            ) {
                // Title
                CookbookTextField(
                    value = state.title,
                    onValueChange = { onEvent(RecipeEditEvent.UpdateTitle(it)) },
                    label = "Recipe Title",
                    placeholder = "Enter recipe title",
                    isError = state.error != null && state.title.isBlank(),
                    errorMessage = if (state.title.isBlank()) "Title is required" else null,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                )
                
                // Description
                CookbookMultilineTextField(
                    value = state.description ?: "",
                    onValueChange = { onEvent(RecipeEditEvent.UpdateDescription(it)) },
                    label = "Description",
                    placeholder = "Brief description of the recipe",
                    minLines = 3,
                    maxLines = 5
                )
                
                // Category Selection
                CategorySelectionSection(
                    selectedCategory = state.category,
                    onCategorySelected = { category ->
                        onEvent(RecipeEditEvent.UpdateCategory(category))
                    },
                    isError = state.error != null && state.category.isBlank(),
                    errorMessage = if (state.category.isBlank()) "Category is required" else null
                )
            }
        }
        
        // Recipe Details Section
        item {
            RecipeSectionHeader(
                title = "Recipe Details",
                icon = Icons.Default.Edit
            )
            
            Column(
                verticalArrangement = Arrangement.spacedBy(CookbookSpacing.small)
            ) {
                // Time and Servings Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(CookbookSpacing.medium)
                ) {
                    CookbookNumberField(
                        value = state.servingSize?.toString() ?: "",
                        onValueChange = { 
                            onEvent(RecipeEditEvent.UpdateServingSize(it.toIntOrNull())) 
                        },
                        label = "Servings",
                        placeholder = "Number of servings",
                        suffix = "servings",
                        modifier = Modifier.weight(1f)
                    )
                    
                    CookbookNumberField(
                        value = state.prepTime?.toString() ?: "",
                        onValueChange = { 
                            onEvent(RecipeEditEvent.UpdatePrepTime(it.toIntOrNull())) 
                        },
                        label = "Prep Time",
                        placeholder = "Preparation time",
                        suffix = "min",
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(CookbookSpacing.medium)
                ) {
                    CookbookNumberField(
                        value = state.cookTime?.toString() ?: "",
                        onValueChange = { 
                            onEvent(RecipeEditEvent.UpdateCookTime(it.toIntOrNull())) 
                        },
                        label = "Cook Time",
                        placeholder = "Cooking time",
                        suffix = "min",
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Favorite toggle
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .height(72.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Favorite",
                            style = CookbookTypography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(CookbookSpacing.xSmall))
                        IconButton(
                            onClick = { onEvent(RecipeEditEvent.UpdateFavorite(!state.isFavorite)) },
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = if (state.isFavorite) 
                                        MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                                    else 
                                        MaterialTheme.colorScheme.surfaceVariant,
                                    shape = CircleShape
                                )
                        ) {
                        Icon(
                            imageVector = if (state.isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Toggle Favorite",
                            tint = if (state.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                        }
                    }
                }
                
                // Source
                CookbookTextField(
                    value = state.source ?: "",
                    onValueChange = { onEvent(RecipeEditEvent.UpdateSource(it)) },
                    label = "Source",
                    placeholder = "Where did this recipe come from? (e.g., Grandma's cookbook, Food Network)",
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                )
            }
        }
        
        // Ingredients Section
        item {
            IngredientsSection(
                ingredients = state.ingredients,
                onAddIngredient = { ingredient ->
                    onEvent(RecipeEditEvent.AddIngredient(ingredient))
                },
                onUpdateIngredient = { ingredient ->
                    onEvent(RecipeEditEvent.UpdateIngredient(ingredient))
                },
                onDeleteIngredient = { ingredientId ->
                    onEvent(RecipeEditEvent.DeleteIngredient(ingredientId))
                },
                isError = state.error != null && state.ingredients.isEmpty(),
                errorMessage = if (state.ingredients.isEmpty()) "At least one ingredient is required" else null
            )
        }
        
        // Instructions Section
        item {
            InstructionsSection(
                instructions = state.instructions,
                onInstructionsChange = { instructions ->
                    onEvent(RecipeEditEvent.UpdateInstructions(instructions))
                },
                isError = state.error != null && state.instructions.isEmpty(),
                errorMessage = if (state.instructions.isEmpty()) "At least one instruction is required" else null
            )
        }
        
        // Tags Section
        item {
            TagsSection(
                tags = state.tags,
                onTagsChange = { tags ->
                    onEvent(RecipeEditEvent.UpdateTags(tags))
                }
            )
        }
        
        // Additional Information Section
        item {
            RecipeSectionHeader(
                title = "Additional Information",
                icon = Icons.Default.Info
            )
            
            CookbookMultilineTextField(
                value = state.notes ?: "",
                onValueChange = { onEvent(RecipeEditEvent.UpdateNotes(it)) },
                label = "Notes",
                placeholder = "Additional notes about the recipe, variations, or tips",
                minLines = 3,
                maxLines = 5
            )
        }
        
        // Validation Summary
        if (state.error != null) {
            item {
                ValidationSummary(
                    errors = listOf(state.error),
                    modifier = Modifier.padding(top = CookbookSpacing.small)
                )
            }
        }
        
        // Save Success Message
        if (state.saveSuccess) {
            item {
                SuccessMessage(
                    message = "Recipe saved successfully!",
                    modifier = Modifier.padding(vertical = CookbookSpacing.medium)
                )
            }
        }
    }
}

@Composable
fun RecipeSectionHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CookbookSpacing.small),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = CookbookSpacing.small)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = title,
            style = CookbookTypography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun RecipeImageSection(
    imageUrl: String?,
    onImageChange: (String?) -> Unit,
    context: Context
) {
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let { onImageChange(it.toString()) }
        }
    )
    
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
        onResult = { bitmap ->
            bitmap?.let { 
                val uri = bitmapToUri(context, bitmap)
                onImageChange(uri?.toString())
            }
        }
    )
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                cameraLauncher.launch(null)
            }
        }
    )
    
    var showImageOptions by remember { mutableStateOf(false) }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Image Preview
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(CookbookSpacing.medium)
                )
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(CookbookSpacing.medium)
                )
                .clickable { showImageOptions = true }
        ) {
            if (!imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Recipe Image",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(CookbookSpacing.medium)),
                    contentScale = ContentScale.Crop,
                    error = painterResource(R.drawable.placeholder_recipe),
                    placeholder = painterResource(R.drawable.placeholder_recipe)
                )
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "Add Recipe Image",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(CookbookSpacing.small))
                    Text(
                        text = "Add Recipe Image",
                        style = CookbookTypography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "Tap to add photo",
                        style = CookbookTypography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }
        }
        
        // Image Options Bottom Sheet
        if (showImageOptions) {
            ModalBottomSheet(
                onDismissRequest = { showImageOptions = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                Column(
                    modifier = Modifier
                        .padding(CookbookSpacing.large)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Add Recipe Image",
                        style = CookbookTypography.headlineSmall,
                        modifier = Modifier.padding(bottom = CookbookSpacing.medium)
                    )
                    
                    // Camera Option
                    ElevatedCard(
                        onClick = {
                            val permission = Manifest.permission.CAMERA
                            if (ContextCompat.checkSelfPermission(
                                    context,
                                    permission
                                ) == PackageManager.PERMISSION_GRANTED
                            ) {
                                cameraLauncher.launch(null)
                            } else {
                                permissionLauncher.launch(permission)
                            }
                            showImageOptions = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = CookbookSpacing.xSmall),
                        shape = RoundedCornerShape(CookbookSpacing.medium)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(CookbookSpacing.medium)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = "Take Photo",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(CookbookSpacing.small))
                            Text(
                                text = "Take Photo",
                                style = CookbookTypography.bodyLarge
                            )
                        }
                    }
                    
                    // Gallery Option
                    ElevatedCard(
                        onClick = {
                            imagePickerLauncher.launch(
                                ActivityResultContracts.PickVisualMedia.Request(
                                    mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                            showImageOptions = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = CookbookSpacing.xSmall),
                        shape = RoundedCornerShape(CookbookSpacing.medium)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(CookbookSpacing.medium)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = "Choose from Gallery",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(CookbookSpacing.small))
                            Text(
                                text = "Choose from Gallery",
                                style = CookbookTypography.bodyLarge
                            )
                        }
                    }
                    
                    // Remove Image Option (if image exists)
                    if (!imageUrl.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(CookbookSpacing.small))
                        CookbookTextButton(
                            text = "Remove Image",
                            onClick = {
                                onImageChange(null)
                                showImageOptions = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(CookbookSpacing.small))
                    CookbookSecondaryButton(
                        text = "Cancel",
                        onClick = { showImageOptions = false },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun CategorySelectionSection(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    val categories = listOf(
        "Breakfasts", "Mains", "Desserts & Snacks", "Sides", 
        "Sauces and Spices", "Appetizers", "Soups", "Salads", 
        "Beverages", "Baking"
    )
    
    var expanded by remember { mutableStateOf(false) }
    
    Column {
        OutlinedTextField(
            value = selectedCategory,
            onValueChange = {},
            label = { Text("Category") },
            placeholder = { Text("Select category") },
            trailingIcon = {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                        contentDescription = "Toggle categories"
                    )
                }
            },
            isError = isError,
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            )
        )
        
        // Category Dropdown
        if (expanded) {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = { 
                            Text(
                                text = category,
                                style = CookbookTypography.bodyMedium
                            )
                        },
                        onClick = {
                            onCategorySelected(category)
                            expanded = false
                        },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(
                                        color = getCategoryColor(category),
                                        shape = CircleShape
                                    )
                            )
                        }
                    )
                }
            }
        }
        
        // Error message
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = CookbookTypography.labelSmall,
                modifier = Modifier.padding(start = CookbookSpacing.small, top = CookbookSpacing.xSmall)
            )
        }
    }
}

@Composable
fun IngredientsSection(
    ingredients: List<Ingredient>,
    onAddIngredient: (Ingredient) -> Unit,
    onUpdateIngredient: (Ingredient) -> Unit,
    onDeleteIngredient: (String) -> Unit,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    var showAddIngredientDialog by remember { mutableStateOf(false) }
    var editingIngredient by remember { mutableStateOf<Ingredient?>(null) }
    
    Column(
        verticalArrangement = Arrangement.spacedBy(CookbookSpacing.small)
    ) {
        RecipeSectionHeader(
            title = "Ingredients",
            icon = Icons.Default.Add
        )
        
        // Error message
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = CookbookTypography.labelSmall,
                modifier = Modifier.padding(start = CookbookSpacing.small, top = CookbookSpacing.xSmall)
            )
        }
        
        // Ingredients List
        if (ingredients.isEmpty()) {
            EmptyState(
                icon = Icons.Default.Info,
                title = "No ingredients added yet",
                message = "Add ingredients to your recipe",
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(CookbookSpacing.xSmall),
                modifier = Modifier.fillMaxWidth()
            ) {
                ingredients.forEach { ingredient ->
                    IngredientItem(
                        ingredient = ingredient,
                        onEdit = { editingIngredient = it },
                        onDelete = { onDeleteIngredient(it.id) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        
        // Add Ingredient Button
        CookbookPrimaryButton(
            text = "Add Ingredient",
            onClick = { showAddIngredientDialog = true },
            icon = Icons.Default.Add,
            modifier = Modifier.fillMaxWidth()
        )
        
        // Add/Edit Ingredient Dialog
        if (showAddIngredientDialog || editingIngredient != null) {
            IngredientDialog(
                ingredient = editingIngredient,
                onDismiss = {
                    showAddIngredientDialog = false
                    editingIngredient = null
                },
                onSave = { ingredient ->
                    if (editingIngredient != null) {
                        onUpdateIngredient(ingredient)
                    } else {
                        onAddIngredient(ingredient)
                    }
                    showAddIngredientDialog = false
                    editingIngredient = null
                }
            )
        }
    }
}

@Composable
fun IngredientItem(
    ingredient: Ingredient,
    onEdit: (Ingredient) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier,
        shape = RoundedCornerShape(CookbookSpacing.small),
        onClick = { onEdit(ingredient) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CookbookSpacing.medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(CookbookSpacing.xxSmall)
            ) {
                Text(
                    text = ingredient.name,
                    style = CookbookTypography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                if (!ingredient.amount.isNullOrBlank() || !ingredient.unit.isNullOrBlank()) {
                    Text(
                        text = "${ingredient.amount ?: ""} ${ingredient.unit ?: ""}".trim(),
                        style = CookbookTypography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                
                if (!ingredient.notes.isNullOrBlank()) {
                    Text(
                        text = ingredient.notes,
                        style = CookbookTypography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }
            
                Row(
                    horizontalArrangement = Arrangement.spacedBy(CookbookSpacing.xxSmall)
                ) {
                    IconButton(
                        onClick = { onEdit(ingredient) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit ingredient",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete ingredient",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientDialog(
    ingredient: Ingredient?,
    onDismiss: () -> Unit,
    onSave: (Ingredient) -> Unit
) {
    var name by remember { mutableStateOf(ingredient?.name ?: "") }
    var amount by remember { mutableStateOf(ingredient?.amount ?: "") }
    var unit by remember { mutableStateOf(ingredient?.unit ?: "") }
    var notes by remember { mutableStateOf(ingredient?.notes ?: "") }
    var order by remember { mutableStateOf(ingredient?.order?.toString() ?: "0") }
    
    val commonUnits = listOf(
        "", "cup", "cups", "tbsp", "tsp", "oz", "lb", "g", "kg", 
        "ml", "L", "piece", "pieces", "slice", "slices", "can", "bunch"
    )
    
    var unitExpanded by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (ingredient == null) "Add Ingredient" else "Edit Ingredient",
                style = CookbookTypography.headlineSmall
            )
        },
        confirmButton = {
            CookbookPrimaryButton(
                text = "Save",
                onClick = {
                    val newIngredient = Ingredient(
                        id = ingredient?.id ?: "",
                        name = name,
                        amount = amount.ifBlank { null },
                        unit = unit.ifBlank { null },
                        notes = notes.ifBlank { null },
                        order = order.toIntOrNull() ?: 0
                    )
                    onSave(newIngredient)
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.padding(horizontal = CookbookSpacing.small)
            )
        },
        dismissButton = {
            CookbookSecondaryButton(
                text = "Cancel",
                onClick = onDismiss,
                modifier = Modifier.padding(horizontal = CookbookSpacing.small)
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(CookbookSpacing.medium),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Name
            CookbookTextField(
                value = name,
                onValueChange = { name = it },
                label = "Ingredient Name",
                placeholder = "e.g., Flour, Sugar, Salt",
                isError = name.isBlank(),
                errorMessage = if (name.isBlank()) "Ingredient name is required" else null,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            )
            
            // Amount and Unit Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(CookbookSpacing.medium)
            ) {
                CookbookTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = "Amount",
                    placeholder = "e.g., 1, 2.5, 1/2",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f)
                )
                
                // Unit Dropdown
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Unit") },
                        placeholder = { Text("Select unit") },
                        trailingIcon = {
                            IconButton(onClick = { unitExpanded = !unitExpanded }) {
                                Icon(
                                    imageVector = if (unitExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                    contentDescription = "Toggle units"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true
                    )
                    
                    DropdownMenu(
                        expanded = unitExpanded,
                        onDismissRequest = { unitExpanded = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        commonUnits.forEach { u ->
                            DropdownMenuItem(
                                text = { Text(u.ifBlank { "None" }) },
                                onClick = {
                                    unit = u
                                    unitExpanded = false
                                }
                            )
                        }
                    }
                }
            }
            
            // Notes
            CookbookTextField(
                value = notes,
                onValueChange = { notes = it },
                label = "Notes",
                placeholder = "e.g., optional, for garnish, divided",
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done
            )
            
            // Order
            CookbookNumberField(
                value = order,
                onValueChange = { order = it },
                label = "Order",
                placeholder = "Display order",
                keyboardType = KeyboardType.Number
            )
        }
    }
}

@Composable
fun InstructionsSection(
    instructions: List<String>,
    onInstructionsChange: (List<String>) -> Unit,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    var editingIndex by remember { mutableStateOf<Int?>(null) }
    var editingText by remember { mutableStateOf("") }
    
    Column(
        verticalArrangement = Arrangement.spacedBy(CookbookSpacing.small)
    ) {
        RecipeSectionHeader(
            title = "Instructions",
            icon = Icons.Default.Edit
        )
        
        // Error message
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = CookbookTypography.labelSmall,
                modifier = Modifier.padding(start = CookbookSpacing.small, top = CookbookSpacing.xSmall)
            )
        }
        
        // Instructions List
        if (instructions.isEmpty()) {
            EmptyState(
                icon = Icons.Default.Info,
                title = "No instructions added yet",
                message = "Add step-by-step instructions",
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(CookbookSpacing.xSmall),
                modifier = Modifier.fillMaxWidth()
            ) {
                instructions.forEachIndexed { index, instruction ->
                    InstructionItem(
                        number = index + 1,
                        instruction = instruction,
                        isEditing = editingIndex == index,
                        editingText = editingText,
                        onEditClick = {
                            editingIndex = index
                            editingText = instruction
                        },
                        onEditTextChange = { editingText = it },
                        onEditSave = {
                            val updatedInstructions = instructions.toMutableList()
                            updatedInstructions[index] = editingText
                            onInstructionsChange(updatedInstructions)
                            editingIndex = null
                            editingText = ""
                        },
                        onEditCancel = {
                            editingIndex = null
                            editingText = ""
                        },
                        onDelete = {
                            val updatedInstructions = instructions.toMutableList()
                            updatedInstructions.removeAt(index)
                            onInstructionsChange(updatedInstructions)
                        },
                        onMoveUp = {
                            if (index > 0) {
                                val updatedInstructions = instructions.toMutableList()
                                val temp = updatedInstructions[index]
                                updatedInstructions[index] = updatedInstructions[index - 1]
                                updatedInstructions[index - 1] = temp
                                onInstructionsChange(updatedInstructions)
                            }
                        },
                        onMoveDown = {
                            if (index < instructions.size - 1) {
                                val updatedInstructions = instructions.toMutableList()
                                val temp = updatedInstructions[index]
                                updatedInstructions[index] = updatedInstructions[index + 1]
                                updatedInstructions[index + 1] = temp
                                onInstructionsChange(updatedInstructions)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        
        // Add Instruction Button
        CookbookPrimaryButton(
            text = "Add Instruction",
            onClick = {
                val updatedInstructions = instructions.toMutableList()
                updatedInstructions.add("")
                onInstructionsChange(updatedInstructions)
            },
            icon = Icons.Default.Add,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun InstructionItem(
    number: Int,
    instruction: String,
    isEditing: Boolean,
    editingText: String,
    onEditClick: () -> Unit,
    onEditTextChange: (String) -> Unit,
    onEditSave: () -> Unit,
    onEditCancel: () -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier,
        shape = RoundedCornerShape(CookbookSpacing.small)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CookbookSpacing.medium)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(CookbookSpacing.small)
                ) {
                    // Step Number
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(28.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape
                            )
                    ) {
                        Text(
                            text = number.toString(),
                            style = CookbookTypography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                    }
                    
                    // Instruction Text
                    if (isEditing) {
                        OutlinedTextField(
                            value = editingText,
                            onValueChange = onEditTextChange,
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Done
                            )
                        )
                    } else {
                        Text(
                            text = instruction.ifBlank { "Add instruction text" },
                            style = CookbookTypography.bodyMedium,
                            color = if (instruction.isBlank()) 
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            else 
                                MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                // Action Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(CookbookSpacing.xxSmall)
                ) {
                    if (isEditing) {
                        IconButton(
                            onClick = onEditSave,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Save",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(
                            onClick = onEditCancel,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancel",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else {
                        IconButton(
                            onClick = onEditClick,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    
                    // Move buttons
                    IconButton(
                        onClick = onMoveUp,
                        modifier = Modifier.size(36.dp),
                        enabled = number > 1
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = "Move up",
                            tint = if (number > 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onMoveDown,
                        modifier = Modifier.size(36.dp),
                        enabled = true // Always enabled, logic handled in click
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Move down",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TagsSection(
    tags: List<String>,
    onTagsChange: (List<String>) -> Unit
) {
    var newTag by remember { mutableStateOf("") }
    
    Column(
        verticalArrangement = Arrangement.spacedBy(CookbookSpacing.small)
    ) {
        RecipeSectionHeader(
            title = "Tags",
            icon = Icons.Default.Tag
        )
        
        // Tags Display
        if (tags.isEmpty()) {
            Text(
                text = "No tags added yet",
                style = CookbookTypography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(CookbookSpacing.xSmall),
                modifier = Modifier.fillMaxWidth()
            ) {
                tags.forEach { tag ->
                    TagInputChip(
                        tag = tag,
                        onDelete = {
                            onTagsChange(tags.filter { it != tag })
                        }
                    )
                }
            }
        }
        
        // Add Tag Input
        Row(
            horizontalArrangement = Arrangement.spacedBy(CookbookSpacing.small),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = newTag,
                onValueChange = { newTag = it },
                label = { Text("Add Tag") },
                placeholder = { Text("Enter tag and press Add") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                trailingIcon = {
                    if (newTag.isNotBlank()) {
                        IconButton(onClick = {
                            if (newTag.isNotBlank() && !tags.contains(newTag)) {
                                onTagsChange(tags + newTag)
                                newTag = ""
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add tag"
                            )
                        }
                    }
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun ValidationSummary(
    errors: List<String?>,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CookbookSpacing.small),
        colors = androidx.compose.material3.CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CookbookSpacing.medium)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(CookbookSpacing.small)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Validation Errors",
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = "Please fix the following issues:",
                    style = CookbookTypography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            
            Spacer(modifier = Modifier.height(CookbookSpacing.small))
            
            Column(
                verticalArrangement = Arrangement.spacedBy(CookbookSpacing.xSmall)
            ) {
                errors.filterNotNull().forEach { error ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(CookbookSpacing.small),
                        modifier = Modifier.padding(start = CookbookSpacing.small)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    shape = CircleShape
                                )
                        )
                        Text(
                            text = error,
                            style = CookbookTypography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SuccessMessage(
    message: String,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CookbookSpacing.small),
        colors = androidx.compose.material3.CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CookbookSpacing.small),
            modifier = Modifier
                .fillMaxWidth()
                .padding(CookbookSpacing.medium)
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Success",
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = message,
                style = CookbookTypography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun EmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.padding(CookbookSpacing.large)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(CookbookSpacing.small))
        Text(
            text = title,
            style = CookbookTypography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
        Text(
            text = message,
            style = CookbookTypography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

// Utility function for bitmap to URI conversion
private fun bitmapToUri(context: Context, bitmap: Bitmap): Uri? {
    return try {
        val bytes = ByteArrayOutputStream().use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.toByteArray()
        }
        
        val path = MediaStore.Images.Media.insertImage(
            context.contentResolver,
            bitmap,
            "recipe_${System.currentTimeMillis()}",
            "Recipe Image"
        )
        
        if (path != null) Uri.parse(path) else null
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@Composable
fun RecipeCreateScreen(
    viewModel: RecipeEditViewModel = hiltViewModel(),
    navController: NavController
) {
    RecipeEditScreen(
        viewModel = viewModel,
        navController = navController,
        recipeId = null
    )
}

@Preview(showBackground = true)
@Composable
fun RecipeEditScreenPreview() {
    CookbookTheme {
        Surface {
            // Preview with mock state
            val mockState = RecipeEditState(
                isLoading = false,
                error = null,
                recipe = Recipe.create(
                    title = "Spaghetti Carbonara",
                    category = "Mains",
                    description = "Classic Italian pasta dish with eggs, cheese, pancetta, and black pepper",
                    servingSize = 4,
                    prepTime = 15,
                    cookTime = 15
                ),
                ingredients = listOf(
                    Ingredient.create("Spaghetti", "400", "g", null, 0),
                    Ingredient.create("Pancetta", "150", "g", "or guanciale", 1),
                    Ingredient.create("Eggs", "4", "large", null, 2),
                    Ingredient.create("Pecorino Romano", "50", "g", "grated", 3),
                    Ingredient.create("Parmigiano Reggiano", "50", "g", "grated", 4),
                    Ingredient.create("Black pepper", null, null, "freshly ground", 5)
                ),
                isSaving = false,
                saveSuccess = false
            )
            
            RecipeEditContent(
                state = mockState,
                onEvent = {},
                context = LocalContext.current
            )
        }
    }
}