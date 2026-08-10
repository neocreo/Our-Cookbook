package com.ourcookbook.ui.screens.sync

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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ourcookbook.ui.components.CookbookPrimaryButton
import com.ourcookbook.ui.components.LoadingState
import com.ourcookbook.ui.navigation.Route
import com.ourcookbook.ui.theme.CookbookTheme
import com.ourcookbook.ui.viewmodel.ConflictResolutionEvent
import com.ourcookbook.ui.viewmodel.ConflictResolutionState
import com.ourcookbook.ui.viewmodel.ConflictResolutionViewModel

/**
 * Conflict Resolution Screen
 * Task 1.9: Complete Navigation Setup
 * 
 * Handles conflict detection, display, and resolution between local and remote recipes
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConflictResolutionScreen(
    viewModel: ConflictResolutionViewModel,
    conflictId: String,
    navController: NavController
) {
    val state by viewModel.state.collectAsState()
    val actions by viewModel.actions.collectAsState()
    
    LaunchedEffect(conflictId) {
        viewModel.handleEvent(ConflictResolutionEvent.LoadConflict(conflictId))
    }
    
    // Handle navigation actions from ViewModel
    actions?.let { action ->
        when (action) {
            is com.ourcookbook.ui.viewmodel.ConflictResolutionAction.NavigateBack -> {
                navController.popBackStack()
                viewModel.clearAction()
            }
            is com.ourcookbook.ui.viewmodel.ConflictResolutionAction.NavigateToNextConflict -> {
                action.conflictId?.let { nextConflictId ->
                    navController.navigate(Route.conflictResolution(nextConflictId)) {
                        popUpTo(Route.CONFLICT_RESOLUTION) { inclusive = true }
                    }
                } ?: navController.popBackStack()
                viewModel.clearAction()
            }
            else -> {}
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resolve Conflict") },
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
        when (val currentState = state) {
            is ConflictResolutionState.Loading -> {
                LoadingState()
            }
            is ConflictResolutionState.Error -> {
                ErrorState(
                    message = currentState.message,
                    onRetry = { viewModel.refresh() }
                )
            }
            is ConflictResolutionState.NotFound -> {
                NotFoundState(
                    onBack = { navController.popBackStack() }
                )
            }
            is ConflictResolutionState.Success -> {
                ConflictResolutionContent(
                    state = currentState,
                    onSelectResolution = { resolution ->
                        viewModel.handleEvent(ConflictResolutionEvent.SelectResolution(resolution))
                    },
                    onApplyResolution = { 
                        viewModel.handleEvent(ConflictResolutionEvent.ApplyResolution) 
                    },
                    onSkipConflict = { 
                        viewModel.handleEvent(ConflictResolutionEvent.SkipConflict) 
                    },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
fun ConflictResolutionContent(
    state: ConflictResolutionState.Success,
    onSelectResolution: (com.ourcookbook.domain.model.ConflictResolution) -> Unit,
    onApplyResolution: () -> Unit,
    onSkipConflict: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Conflict header
        ConflictHeader(conflict = state.conflict)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Conflict description
        ConflictDescription(state = state)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Resolution options
        ResolutionOptions(
            selectedResolution = state.resolution,
            onSelectResolution = onSelectResolution
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Recipe comparison
        RecipeComparison(
            localRecipe = state.localRecipe,
            remoteRecipe = state.remoteRecipe
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Action buttons
        ActionButtons(
            isResolving = state.isResolving,
            onApplyResolution = onApplyResolution,
            onSkipConflict = onSkipConflict
        )
    }
}

@Composable
fun ConflictHeader(conflict: com.ourcookbook.domain.model.SyncConflict) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Sync Conflict Detected",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.error
        )
        
        Text(
            text = "Conflict ID: ${conflict.id.take(8)}...",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        
        Text(
            text = "Detected: ${conflict.detectedAt}",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun ConflictDescription(state: ConflictResolutionState.Success) {
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
                text = "Conflict Details",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = "Local Recipe ID: ${state.conflict.localRecipeId}",
                style = MaterialTheme.typography.bodySmall
            )
            
            Text(
                text = "Remote Recipe ID: ${state.conflict.remoteRecipeId}",
                style = MaterialTheme.typography.bodySmall
            )
            
            Text(
                text = "Local Checksum: ${state.conflict.localChecksum.take(16)}...",
                style = MaterialTheme.typography.bodySmall
            )
            
            Text(
                text = "Remote Checksum: ${state.conflict.remoteChecksum.take(16)}...",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun ResolutionOptions(
    selectedResolution: com.ourcookbook.domain.model.ConflictResolution?,
    onSelectResolution: (com.ourcookbook.domain.model.ConflictResolution) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Choose Resolution",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        
        // Keep Local option
        ResolutionOption(
            resolution = com.ourcookbook.domain.model.ConflictResolution.KEEP_LOCAL,
            selected = selectedResolution == com.ourcookbook.domain.model.ConflictResolution.KEEP_LOCAL,
            onSelect = onSelectResolution,
            title = "Keep Local Version",
            description = "Keep your local changes and overwrite the remote version"
        )
        
        // Keep Remote option
        ResolutionOption(
            resolution = com.ourcookbook.domain.model.ConflictResolution.KEEP_REMOTE,
            selected = selectedResolution == com.ourcookbook.domain.model.ConflictResolution.KEEP_REMOTE,
            onSelect = onSelectResolution,
            title = "Keep Remote Version",
            description = "Discard your local changes and keep the remote version"
        )
        
        // Merge option (would be implemented in future)
        ResolutionOption(
            resolution = com.ourcookbook.domain.model.ConflictResolution.MERGE,
            selected = selectedResolution == com.ourcookbook.domain.model.ConflictResolution.MERGE,
            onSelect = onSelectResolution,
            title = "Merge Changes",
            description = "Combine changes from both versions (coming soon)",
            enabled = false
        )
    }
}

@Composable
fun ResolutionOption(
    resolution: com.ourcookbook.domain.model.ConflictResolution,
    selected: Boolean,
    onSelect: (com.ourcookbook.domain.model.ConflictResolution) -> Unit,
    title: String,
    description: String,
    enabled: Boolean = true
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { if (enabled) onSelect(resolution) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            RadioButton(
                selected = selected,
                onClick = { if (enabled) onSelect(resolution) },
                enabled = enabled
            )
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun RecipeComparison(
    localRecipe: com.ourcookbook.domain.model.Recipe?,
    remoteRecipe: com.ourcookbook.domain.model.Recipe?
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Recipe Comparison",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        
        // Local recipe
        localRecipe?.let { recipe ->
            RecipeCard(
                title = "Local Version",
                recipe = recipe,
                isLocal = true
            )
        } ?: run {
            Text(
                text = "Local recipe not available",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Remote recipe
        remoteRecipe?.let { recipe ->
            RecipeCard(
                title = "Remote Version",
                recipe = recipe,
                isLocal = false
            )
        } ?: run {
            Text(
                text = "Remote recipe not available",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun RecipeCard(
    title: String,
    recipe: com.ourcookbook.domain.model.Recipe,
    isLocal: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (isLocal) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                )
                
                Text(
                    text = if (isLocal) "Local" else "Remote",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isLocal) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                )
            }
            
            Text(
                text = recipe.title,
                style = MaterialTheme.typography.bodyMedium
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
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            
            Text(
                text = "Updated: ${recipe.updatedAt}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun ActionButtons(
    isResolving: Boolean,
    onApplyResolution: () -> Unit,
    onSkipConflict: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CookbookPrimaryButton(
            text = if (isResolving) "Resolving..." else "Apply Resolution",
            onClick = onApplyResolution,
            enabled = !isResolving,
            modifier = Modifier.fillMaxWidth()
        )
        
        if (isResolving) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
            }
        }
        
        CookbookPrimaryButton(
            text = "Skip Conflict",
            onClick = onSkipConflict,
            enabled = !isResolving,
            modifier = Modifier.fillMaxWidth()
        )
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
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Error",
            modifier = Modifier.height(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        
        Text(
            text = "Error Loading Conflict",
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

@Composable
fun NotFoundState(
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Not Found",
            modifier = Modifier.height(64.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        )
        
        Text(
            text = "Conflict Not Found",
            style = MaterialTheme.typography.headlineSmall
        )
        
        Text(
            text = "The conflict you're looking for doesn't exist",
            style = MaterialTheme.typography.bodyMedium
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        CookbookPrimaryButton(
            text = "Go Back",
            onClick = onBack
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ConflictResolutionScreenPreview() {
    CookbookTheme {
        // This would need proper setup for preview
        Text("Conflict Resolution Screen Preview")
    }
}
