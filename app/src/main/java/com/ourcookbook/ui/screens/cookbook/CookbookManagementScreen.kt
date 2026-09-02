package com.ourcookbook.ui.screens.cookbook

import android.widget.Toast
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import java.io.File
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.clickable
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ourcookbook.domain.model.Cookbook
import com.ourcookbook.ui.components.CookbookCard
import com.ourcookbook.ui.components.CookbookPrimaryButton
import com.ourcookbook.ui.components.CookbookSecondaryButton
import com.ourcookbook.ui.components.EmptyState
import com.ourcookbook.ui.components.LoadingState
import com.ourcookbook.ui.navigation.Route
import com.ourcookbook.ui.theme.CookbookSpacing
import com.ourcookbook.ui.theme.CookbookTheme
import com.ourcookbook.ui.theme.CookbookTypography
import com.ourcookbook.ui.viewmodel.CookbookManagementAction
import com.ourcookbook.ui.viewmodel.CookbookManagementEvent
import com.ourcookbook.ui.viewmodel.CookbookManagementState
import com.ourcookbook.ui.viewmodel.CookbookManagementViewModel
import com.ourcookbook.ui.viewmodel.ExportFormat
import com.ourcookbook.ui.viewmodel.SortOrder
import kotlinx.coroutines.launch

/**
 * Cookbook Management Screen
 * Task 2.1.07: Cookbook Management Screen Implementation
 * 
 * Comprehensive cookbook management with CRUD, sharing, import/export functionality
 */

/**
 * Top App Bar for Cookbook Management Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookbookManagementTopBar(
    onBackClick: () -> Unit,
    onSearch: (String) -> Unit,
    onSortClick: () -> Unit,
    onFilterClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    
    TopAppBar(
        title = { 
            Text(
                text = "Cookbooks",
                style = MaterialTheme.typography.headlineSmall
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
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { 
                    searchQuery = it
                    onSearch(it)
                },
                label = { Text("Search") },
                placeholder = { Text("Search cookbooks...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                },
                modifier = Modifier
                    .width(200.dp)
                    .padding(end = CookbookSpacing.small),
                shape = MaterialTheme.shapes.small,
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
            
            IconButton(onClick = onSortClick) {
                Icon(
                    imageVector = Icons.Default.Sort,
                    contentDescription = "Sort"
                )
            }
            
            IconButton(onClick = onFilterClick) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Filter"
                )
            }
            
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More Options"
                )
            }
        }
    )
}

/**
 * Bottom Bar for selected cookbooks
 */
@Composable
fun CookbookSelectionBottomBar(
    selectedCount: Int,
    onDeselectAll: () -> Unit,
    onDeleteSelected: () -> Unit,
    onShareSelected: () -> Unit
) {
    BottomAppBar(
        modifier = Modifier.height(64.dp),
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(CookbookSpacing.small)
            ) {
                Icon(
                    imageVector = Icons.Default.Book,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "$selectedCount selected",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(CookbookSpacing.small)
            ) {
                IconButton(onClick = onDeselectAll) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Deselect All"
                    )
                }
                
                IconButton(onClick = onShareSelected) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share Selected"
                    )
                }
                
                IconButton(onClick = onDeleteSelected) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Selected",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

/**
 * Bottom Sheet Action Item
 */
@Composable
fun BottomSheetActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(CookbookSpacing.medium),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CookbookSpacing.medium)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

/**
 * Main Cookbook Management Screen
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CookbookManagementScreen(
    viewModel: CookbookManagementViewModel,
    navController: NavController
) {
    val state by viewModel.state.collectAsState()
    val actions by viewModel.actions.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    // Dialog states
    var showCreateDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showBulkDeleteDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showSharingLinkDialog by remember { mutableStateOf(false) }
    var showProgressDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    
    // Selected items for bulk operations
    var selectedCookbooks by remember { mutableStateOf(setOf<String>()) }
    
    // Dialog data
    var dialogCookbook by remember { mutableStateOf<Cookbook?>(null) }
    var dialogMessage by remember { mutableStateOf("") }
    var dialogTitle by remember { mutableStateOf("") }
    var dialogProgress by remember { mutableStateOf(0) }
    var dialogSharingLink by remember { mutableStateOf("") }
    var dialogQrCodeData by remember { mutableStateOf<String?>(null) }
    var dialogSuccessMessage by remember { mutableStateOf("") }
    var dialogSuccessTitle by remember { mutableStateOf("") }
    var bulkDeleteCookbookIds by remember { mutableStateOf(listOf<String>()) }
    
    // Sort and filter states
    var showSortMenu by remember { mutableStateOf(false) }
    var showFilterMenu by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    
    // Bottom sheet state
    val bottomSheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        viewModel.handleEvent(CookbookManagementEvent.LoadCookbooks)
    }
    
    // Handle actions from ViewModel
    LaunchedEffect(actions) {
        actions?.let { action ->
            when (action) {
                is CookbookManagementAction.ShowCreateCookbookDialog -> {
                    showCreateDialog = true
                    viewModel.clearAction()
                }
                is CookbookManagementAction.ShowEditCookbookDialog -> {
                    dialogCookbook = action.cookbook
                    showEditDialog = true
                    viewModel.clearAction()
                }
                is CookbookManagementAction.ShowDeleteConfirmation -> {
                    dialogCookbook = viewModel.getCookbookById(action.cookbookId)
                    dialogMessage = if (action.hasRecipes) {
                        "This cookbook contains recipes. Deleting it will remove all recipes from this collection."
                    } else {
                        "Are you sure you want to delete this cookbook?"
                    }
                    showDeleteDialog = true
                    viewModel.clearAction()
                }
                is CookbookManagementAction.ShowBulkDeleteConfirmation -> {
                    bulkDeleteCookbookIds = action.cookbookIds
                    showBulkDeleteDialog = true
                    viewModel.clearAction()
                }
                is CookbookManagementAction.ShowShareDialog -> {
                    dialogCookbook = action.cookbook
                    showShareDialog = true
                    viewModel.clearAction()
                }
                is CookbookManagementAction.ShowSharingLinkDialog -> {
                    dialogSharingLink = action.sharingLink
                    dialogQrCodeData = action.qrCodeData
                    showSharingLinkDialog = true
                    viewModel.clearAction()
                }
                is CookbookManagementAction.ShowExportDialog -> {
                    dialogCookbook = action.cookbook
                    showExportDialog = true
                    viewModel.clearAction()
                }
                is CookbookManagementAction.ShowImportDialog -> {
                    showImportDialog = true
                    viewModel.clearAction()
                }
                is CookbookManagementAction.ShowExportProgress -> {
                    dialogProgress = action.progress
                    dialogTitle = "Exporting Cookbook"
                    dialogMessage = "Please wait while we export your cookbook..."
                    showProgressDialog = true
                    viewModel.clearAction()
                }
                is CookbookManagementAction.ShowImportProgress -> {
                    dialogProgress = action.progress
                    dialogTitle = "Importing Cookbook"
                    dialogMessage = "Please wait while we import your cookbook..."
                    showProgressDialog = true
                    viewModel.clearAction()
                }
                is CookbookManagementAction.ShowExportSuccess -> {
                    dialogSuccessTitle = "Export Successful"
                    dialogSuccessMessage = "Cookbook exported to ${action.filePath} as ${action.format.name}"
                    showSuccessDialog = true
                    viewModel.clearAction()
                }
                is CookbookManagementAction.ShowImportSuccess -> {
                    dialogSuccessTitle = "Import Successful"
                    dialogSuccessMessage = "Imported cookbook '${action.cookbookName}' with ${action.recipeCount} recipes"
                    showSuccessDialog = true
                    viewModel.clearAction()
                }
                is CookbookManagementAction.ShowError -> {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(action.message)
                    }
                    viewModel.clearAction()
                }
                is CookbookManagementAction.ShowSnackbar -> {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(action.message)
                    }
                    viewModel.clearAction()
                }
                is CookbookManagementAction.ShowUndoSnackbar -> {
                    coroutineScope.launch {
                        val result = snackbarHostState.showSnackbar(
                            message = action.message,
                            actionLabel = "UNDO",
                            withDismissAction = true
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            action.onUndo()
                        }
                    }
                    viewModel.clearAction()
                }
                is CookbookManagementAction.NavigateToCookbookSettings -> {
                    navController.navigate(Route.cookbookEdit(action.cookbookId))
                    viewModel.clearAction()
                }
                else -> {}
            }
        }
    }
    
    Scaffold(
        topBar = {
            CookbookManagementTopBar(
                onBackClick = { navController.popBackStack() },
                onSearch = { searchQuery = it },
                onSortClick = { showSortMenu = true },
                onFilterClick = { showFilterMenu = true },
                onMenuClick = { showBottomSheet = true }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.showCreateCookbookDialog() },
                icon = { 
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create Cookbook"
                    )
                },
                text = { Text("Create Cookbook") },
                modifier = Modifier.padding(CookbookSpacing.small)
            )
        },
        bottomBar = {
            if (selectedCookbooks.isNotEmpty()) {
                CookbookSelectionBottomBar(
                    selectedCount = selectedCookbooks.size,
                    onDeselectAll = { selectedCookbooks = emptySet() },
                    onDeleteSelected = {
                        viewModel.handleEvent(CookbookManagementEvent.BulkDeleteCookbooks(selectedCookbooks.toList()))
                    },
                    onShareSelected = {
                        // Share selected cookbooks
                    }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
                    selectedCookbooks = selectedCookbooks,
                    onCookbookClick = { cookbookId ->
                        navController.navigate(Route.cookbookDetail(cookbookId))
                    },
                    onCookbookLongClick = { cookbookId ->
                        selectedCookbooks = if (selectedCookbooks.contains(cookbookId)) {
                            selectedCookbooks - cookbookId
                        } else {
                            selectedCookbooks + cookbookId
                        }
                    },
                    onEditCookbook = { cookbook ->
                        viewModel.showEditCookbookDialog(cookbook)
                    },
                    onShareCookbook = { cookbook ->
                        viewModel.showShareDialog(cookbook)
                    },
                    onDeleteCookbook = { cookbookId ->
                        viewModel.handleEvent(CookbookManagementEvent.DeleteCookbook(cookbookId))
                    },
                    onExportCookbook = { cookbook ->
                        viewModel.showExportDialog(cookbook)
                    },
                    onSortChange = { sortOrder ->
                        viewModel.handleEvent(CookbookManagementEvent.SortCookbooks(sortOrder))
                    },
                    onRefresh = { viewModel.handleEvent(CookbookManagementEvent.Refresh) },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
    
    // Dialogs
    if (showCreateDialog) {
        CookbookCreationDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name, description, imageUri ->
                viewModel.handleEvent(CookbookManagementEvent.CreateCookbook(name, description, imageUri))
                showCreateDialog = false
            }
        )
    }
    
    dialogCookbook?.let { cookbook ->
        if (showEditDialog) {
            CookbookEditingDialog(
                cookbook = cookbook,
                onDismiss = { 
                    showEditDialog = false
                    dialogCookbook = null
                },
                onSave = { name, description, imageUri ->
                    val updatedCookbook = cookbook.copy(
                        name = name,
                        description = description
                    )
                    viewModel.handleEvent(CookbookManagementEvent.UpdateCookbook(updatedCookbook, imageUri))
                    showEditDialog = false
                    dialogCookbook = null
                }
            )
        }
        
        if (showDeleteDialog) {
            CookbookDeleteConfirmationDialog(
                cookbookName = cookbook.name,
                hasRecipes = cookbook.recipeCount > 0,
                onDismiss = { 
                    showDeleteDialog = false
                    dialogCookbook = null
                },
                onConfirm = {
                    viewModel.confirmDeleteCookbook(cookbook.id)
                    showDeleteDialog = false
                    dialogCookbook = null
                }
            )
        }
        
        if (showShareDialog) {
            ShareCookbookDialog(
                cookbook = cookbook,
                onDismiss = { 
                    showShareDialog = false
                    dialogCookbook = null
                },
                onShareWithUsers = { userIds, permissions ->
                    viewModel.handleEvent(CookbookManagementEvent.ShareCookbook(cookbook.id, userIds, permissions))
                    showShareDialog = false
                    dialogCookbook = null
                },
                onGenerateLink = {
                    viewModel.handleEvent(CookbookManagementEvent.GenerateSharingLink(cookbook.id))
                    showShareDialog = false
                }
            )
        }
        
        if (showExportDialog) {
            ExportCookbookDialog(
                cookbook = cookbook,
                onDismiss = { 
                    showExportDialog = false
                    dialogCookbook = null
                },
                onExport = { format ->
                    // In production, this would open a file picker
                    // For now, just show progress
                    viewModel.handleEvent(CookbookManagementEvent.ExportCookbook(
                        cookbook.id, 
                        format, 
                        java.io.File("${cookbook.name.replace(" ", "_")}.${format.name.lowercase()}")
                    ))
                    showExportDialog = false
                    dialogCookbook = null
                }
            )
        }
    }
    
    if (showBulkDeleteDialog) {
        BulkDeleteConfirmationDialog(
            cookbookCount = bulkDeleteCookbookIds.size,
            onDismiss = { 
                showBulkDeleteDialog = false
                bulkDeleteCookbookIds = emptyList()
            },
            onConfirm = {
                viewModel.confirmBulkDeleteCookbooks(bulkDeleteCookbookIds)
                showBulkDeleteDialog = false
                bulkDeleteCookbookIds = emptyList()
                selectedCookbooks = emptySet()
            }
        )
    }
    
    if (showImportDialog) {
        ImportCookbookDialog(
            supportedFormats = listOf(ExportFormat.JSON, ExportFormat.MARKDOWN),
            onDismiss = { showImportDialog = false },
            onImport = { file, format ->
                viewModel.handleEvent(CookbookManagementEvent.ImportCookbook(file, format))
                showImportDialog = false
            }
        )
    }
    
    if (showSharingLinkDialog) {
        SharingLinkDialog(
            sharingLink = dialogSharingLink,
            qrCodeData = dialogQrCodeData,
            onDismiss = { 
                showSharingLinkDialog = false
                dialogSharingLink = ""
                dialogQrCodeData = null
            }
        )
    }
    
    if (showProgressDialog) {
        ProgressDialog(
            title = dialogTitle,
            message = dialogMessage,
            progress = dialogProgress,
            onDismiss = { showProgressDialog = false }
        )
    }
    
    if (showSuccessDialog) {
        SuccessDialog(
            title = dialogSuccessTitle,
            message = dialogSuccessMessage,
            onDismiss = { showSuccessDialog = false }
        )
    }
    
    // Sort menu
    if (showSortMenu) {
        DropdownMenu(
            expanded = showSortMenu,
            onDismissRequest = { showSortMenu = false }
        ) {
            SortOrder.entries.forEach { sortOrder ->
                DropdownMenuItem(
                    text = { 
                        Text(
                            text = when (sortOrder) {
                                SortOrder.NAME_ASC -> "A-Z"
                                SortOrder.NAME_DESC -> "Z-A"
                                SortOrder.RECENTLY_UPDATED -> "Recently Updated"
                                SortOrder.MOST_RECIPES -> "Most Recipes"
                            }
                        )
                    },
                    onClick = {
                        viewModel.handleEvent(CookbookManagementEvent.SortCookbooks(sortOrder))
                        showSortMenu = false
                    }
                )
            }
        }
    }
    
    // Filter menu
    if (showFilterMenu) {
        DropdownMenu(
            expanded = showFilterMenu,
            onDismissRequest = { showFilterMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("All Cookbooks") },
                onClick = { 
                    // Apply filter
                    showFilterMenu = false
                }
            )
            DropdownMenuItem(
                text = { Text("My Cookbooks") },
                onClick = { 
                    // Apply filter
                    showFilterMenu = false
                }
            )
            DropdownMenuItem(
                text = { Text("Shared Cookbooks") },
                onClick = { 
                    // Apply filter
                    showFilterMenu = false
                }
            )
        }
    }
    
    // Bottom sheet for additional actions
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = bottomSheetState
        ) {
            Column(
                modifier = Modifier
                    .padding(CookbookSpacing.large)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(CookbookSpacing.medium)
            ) {
                Text(
                    text = "Cookbook Actions",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(CookbookSpacing.small)
                ) {
                    BottomSheetActionItem(
                        icon = Icons.Default.Upload,
                        text = "Import Cookbook",
                        onClick = {
                            viewModel.showImportDialog()
                            showBottomSheet = false
                        }
                    )
                    
                    BottomSheetActionItem(
                        icon = Icons.Default.Download,
                        text = "Export All Cookbooks",
                        onClick = {
                            // Export all cookbooks
                            showBottomSheet = false
                        }
                    )
                    
                    BottomSheetActionItem(
                        icon = Icons.Default.Settings,
                        text = "Cookbook Settings",
                        onClick = {
                            // Navigate to settings
                            showBottomSheet = false
                        }
                    )
                    
                    BottomSheetActionItem(
                        icon = Icons.Default.Refresh,
                        text = "Refresh",
                        onClick = {
                            viewModel.handleEvent(CookbookManagementEvent.Refresh)
                            showBottomSheet = false
                        }
                    )
                }

                Spacer(modifier = Modifier.height(CookbookSpacing.medium))

                OutlinedButton(
                    onClick = { showBottomSheet = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close")
                }
            }
        }
    }
}

/**
 * Cookbook Management Content
 */
@Composable
fun CookbookManagementContent(
    state: CookbookManagementState.Success,
    selectedCookbooks: Set<String>,
    onCookbookClick: (String) -> Unit,
    onCookbookLongClick: (String) -> Unit,
    onEditCookbook: (Cookbook) -> Unit,
    onShareCookbook: (Cookbook) -> Unit,
    onDeleteCookbook: (String) -> Unit,
    onExportCookbook: (Cookbook) -> Unit,
    onSortChange: (SortOrder) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(CookbookSpacing.medium),
        verticalArrangement = Arrangement.spacedBy(CookbookSpacing.medium)
    ) {
        // My Cookbooks section
        item {
            SectionHeader(
                title = "My Cookbooks",
                count = state.cookbooks.size,
                onSortChange = onSortChange,
                onRefresh = onRefresh
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
                CookbookManagementCard(
                    cookbook = cookbook,
                    onClick = { onCookbookClick(cookbook.id) },
                    onEdit = { onEditCookbook(cookbook) },
                    onShare = { onShareCookbook(cookbook) },
                    onDelete = { onDeleteCookbook(cookbook.id) },
                    onExport = { onExportCookbook(cookbook) },
                    onSync = { /* Sync action */ },
                    isSelected = selectedCookbooks.contains(cookbook.id),
                    syncStatus = com.ourcookbook.ui.screens.cookbook.SyncStatus.SYNCED,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCookbookClick(cookbook.id) }
                )
            }
        }
        
        // Shared Cookbooks section
        if (state.sharedCookbooks.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Shared Cookbooks",
                    count = state.sharedCookbooks.size
                )
            }
            
            items(state.sharedCookbooks, key = { it.id }) { cookbook ->
                CookbookManagementCard(
                    cookbook = cookbook,
                    onClick = { onCookbookClick(cookbook.id) },
                    onEdit = { onEditCookbook(cookbook) },
                    onShare = { onShareCookbook(cookbook) },
                    onDelete = { onDeleteCookbook(cookbook.id) },
                    onExport = { onExportCookbook(cookbook) },
                    onSync = { /* Sync action */ },
                    isSelected = selectedCookbooks.contains(cookbook.id),
                    syncStatus = com.ourcookbook.ui.screens.cookbook.SyncStatus.SYNCED,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCookbookClick(cookbook.id) }
                )
            }
        }
        
        // Loading more indicator
        if (state.isLoadingMore) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(CookbookSpacing.medium),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        
        // Bottom spacer
        item {
            Spacer(modifier = Modifier.height(CookbookSpacing.large))
        }
    }
}

/**
 * Section Header with sort and refresh options
 */
@Composable
fun SectionHeader(
    title: String,
    count: Int,
    onSortChange: (SortOrder) -> Unit = {},
    onRefresh: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CookbookSpacing.small)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = "($count)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CookbookSpacing.xSmall)
        ) {
            IconButton(
                onClick = onRefresh,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(CookbookSpacing.xSmall))
}

/**
 * Error State
 */
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
            imageVector = Icons.Default.Warning,
            contentDescription = "Error",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(64.dp)
        )
        
        Spacer(modifier = Modifier.height(CookbookSpacing.medium))
        
        Text(
            text = "Error loading cookbooks",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.error
        )
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium
        )
        
        Spacer(modifier = Modifier.height(CookbookSpacing.medium))
        
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

/**
 * Preview for Cookbook Management Screen
 */
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