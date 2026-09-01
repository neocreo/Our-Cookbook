package com.ourcookbook.ui.screens.ocr

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ourcookbook.ui.components.CookbookPrimaryButton
import com.ourcookbook.ui.components.LoadingState
import com.ourcookbook.ui.navigation.Route
import com.ourcookbook.ui.theme.CookbookTheme
import com.ourcookbook.ui.viewmodel.ScanEvent
import com.ourcookbook.ui.viewmodel.ScanState
import com.ourcookbook.ui.viewmodel.ScanViewModel

/**
 * OCR Scanner Screen
 * Task 1.9: Complete Navigation Setup
 * 
 * Handles OCR scanning, text extraction, and recipe creation from scanned text
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OcrScannerScreen(
    viewModel: ScanViewModel,
    navController: NavController
) {
    val state by viewModel.state.collectAsState()
    val actions by viewModel.actions.collectAsState()
    
    // Handle navigation actions from ViewModel
    actions?.let { action ->
        when (action) {
            is com.ourcookbook.ui.viewmodel.ScanAction.NavigateToRecipeDetail -> {
                navController.navigate(Route.recipeDetail(action.recipeId)) {
                    popUpTo(Route.OCR_SCANNER) { inclusive = true }
                }
                viewModel.clearAction()
            }
            is com.ourcookbook.ui.viewmodel.ScanAction.NavigateBack -> {
                navController.popBackStack()
                viewModel.clearAction()
            }
            else -> {}
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan Recipe") },
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
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (val currentState = state) {
                is ScanState.Idle -> {
                    ScannerIdleContent(
                        onStartScan = { viewModel.handleEvent(ScanEvent.StartScan) }
                    )
                }
                is ScanState.Scanning -> {
                    ScannerActiveContent(
                        onStopScan = { viewModel.handleEvent(ScanEvent.StopScan) }
                    )
                }
                is ScanState.Processing -> {
                    ProcessingContent()
                }
                is ScanState.ImageCaptured -> {
                    ImageCapturedContent(
                        imagePath = currentState.imagePath,
                        onRetry = { viewModel.handleEvent(ScanEvent.RetryScan) }
                    )
                }
                is ScanState.ScannedText -> {
                    ScannedTextContent(
                        text = currentState.text,
                        onSaveRecipe = { viewModel.handleEvent(ScanEvent.SaveRecipe) },
                        onDiscard = { viewModel.handleEvent(ScanEvent.DiscardRecipe) },
                        onEdit = { viewModel.navigateToRecipeEdit() }
                    )
                }
                is ScanState.ExtractedRecipe -> {
                    ExtractedRecipeContent(
                        recipe = currentState.recipe,
                        onSaveRecipe = { viewModel.handleEvent(ScanEvent.SaveRecipe) },
                        onDiscard = { viewModel.handleEvent(ScanEvent.DiscardRecipe) },
                        onEdit = { viewModel.navigateToRecipeEdit() }
                    )
                }
                is ScanState.Error -> {
                    ErrorContent(
                        message = currentState.message,
                        onRetry = { viewModel.handleEvent(ScanEvent.RetryScan) },
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

@Composable
fun ScannerIdleContent(
    onStartScan: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Camera,
            contentDescription = "Camera",
            modifier = Modifier.height(128.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = "Scan a Recipe",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Text(
            text = "Point your camera at a recipe card or printed recipe to automatically extract the text",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        CookbookPrimaryButton(
            text = "Start Scanning",
            onClick = onStartScan,
            modifier = Modifier.fillMaxWidth(0.8f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Or paste text manually",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun ScannerActiveContent(
    onStopScan: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // In production, this would show the camera preview
        // For now, show a placeholder
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Camera,
                    contentDescription = "Camera Preview",
                    modifier = Modifier.height(64.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Camera Preview")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        CookbookPrimaryButton(
            text = "Stop Scanning",
            onClick = onStopScan
        )
    }
}

@Composable
fun ProcessingContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LoadingState()
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Processing image...",
            style = MaterialTheme.typography.bodyLarge
        )
        
        Text(
            text = "Extracting text from the captured image",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun ImageCapturedContent(
    imagePath: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // In production, this would show the captured image
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Camera,
                    contentDescription = "Captured Image",
                    modifier = Modifier.height(64.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Image: $imagePath")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Image captured successfully!",
            style = MaterialTheme.typography.bodyLarge
        )
        
        Text(
            text = "Processing the image to extract recipe text...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        CookbookPrimaryButton(
            text = "Retry",
            onClick = onRetry
        )
    }
}

@Composable
fun ScannedTextContent(
    text: String,
    onSaveRecipe: () -> Unit,
    onDiscard: () -> Unit,
    onEdit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Extracted Text",
            style = MaterialTheme.typography.headlineSmall
        )
        
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CookbookPrimaryButton(
                text = "Edit",
                onClick = onEdit,
                modifier = Modifier.weight(1f)
            )
            
            CookbookPrimaryButton(
                text = "Discard",
                onClick = onDiscard,
                modifier = Modifier.weight(1f)
            )
        }
        
        CookbookPrimaryButton(
            text = "Save as Recipe",
            onClick = onSaveRecipe,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun ExtractedRecipeContent(
    recipe: com.ourcookbook.domain.model.Recipe,
    onSaveRecipe: () -> Unit,
    onDiscard: () -> Unit,
    onEdit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Recipe Preview",
            style = MaterialTheme.typography.headlineSmall
        )
        
        // Recipe preview
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.titleMedium
                )
                
                if (!recipe.category.isNullOrBlank()) {
                    Text(
                        text = "Category: ${recipe.category}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                if (!recipe.description.isNullOrBlank()) {
                    Text(
                        text = recipe.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                if (recipe.ingredients.isNotEmpty()) {
                    Text(
                        text = "Ingredients:",
                        style = MaterialTheme.typography.titleSmall
                    )
                    
                    recipe.ingredients.forEach { ingredient ->
                        Text(
                            text = "- ${ingredient.name}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                
                if (recipe.instructions.isNotEmpty()) {
                    Text(
                        text = "Instructions:",
                        style = MaterialTheme.typography.titleSmall
                    )
                    
                    recipe.instructions.forEachIndexed { index, instruction ->
                        Text(
                            text = "${index + 1}. $instruction",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit"
                )
            }
            
            IconButton(onClick = onDiscard) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Discard",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
        
        CookbookPrimaryButton(
            text = "Save Recipe",
            onClick = onSaveRecipe,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Scanning Error",
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
        
        Spacer(modifier = Modifier.height(8.dp))
        
        CookbookPrimaryButton(
            text = "Back",
            onClick = onBack
        )
    }
}

@Preview(showBackground = true)
@Composable
fun OcrScannerScreenPreview() {
    CookbookTheme {
        OcrScannerScreen(
            viewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
            navController = rememberNavController()
        )
    }
}
