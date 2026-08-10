package com.ourcookbook.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.ourcookbook.ui.screens.auth.AuthScreen
import com.ourcookbook.ui.screens.auth.DeviceRegistrationScreen
import com.ourcookbook.ui.screens.auth.DriveAuthScreen
import com.ourcookbook.ui.screens.cookbook.CookbookDetailScreen
import com.ourcookbook.ui.screens.cookbook.CookbookEditScreen
import com.ourcookbook.ui.screens.cookbook.CookbookListScreen
import com.ourcookbook.ui.screens.cookbook.CookbookManagementScreen
import com.ourcookbook.ui.screens.favorites.FavoritesScreen
import com.ourcookbook.ui.screens.home.HomeScreen
import com.ourcookbook.ui.screens.ocr.OcrScannerScreen
import com.ourcookbook.ui.screens.recipe.RecipeCreateScreen
import com.ourcookbook.ui.screens.recipe.RecipeDetailScreen
import com.ourcookbook.ui.screens.recipe.RecipeEditScreen
import com.ourcookbook.ui.screens.recipe.RecipeListScreen
import com.ourcookbook.ui.screens.search.SearchScreen
import com.ourcookbook.ui.screens.settings.SettingsScreen
import com.ourcookbook.ui.screens.sync.ConflictResolutionScreen
import com.ourcookbook.ui.screens.sync.SyncStatusScreen
import com.ourcookbook.ui.viewmodel.AuthViewModel
import com.ourcookbook.ui.viewmodel.ConflictResolutionViewModel
import com.ourcookbook.ui.viewmodel.CookbookManagementViewModel
import com.ourcookbook.ui.viewmodel.DeviceRegistrationViewModel
import com.ourcookbook.ui.viewmodel.HomeViewModel
import com.ourcookbook.ui.viewmodel.RecipeDetailViewModel
import com.ourcookbook.ui.viewmodel.RecipeEditViewModel
import com.ourcookbook.ui.viewmodel.RecipeListViewModel
import com.ourcookbook.ui.viewmodel.SearchViewModel
import com.ourcookbook.ui.viewmodel.SyncViewModel

/**
 * Cookbook Navigation Graph
 * Task 1.9: Complete Navigation Setup
 * 
 * Main navigation host for the cookbook application
 * Handles all navigation routes and integrates with ViewModels from Task 1.7
 */

@Composable
fun CookbookNavHost(
    navController: NavHostController,
    startDestination: String = Route.AUTH
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // ==================== AUTHENTICATION FLOW ====================
        
        composable(Route.AUTH) {
            val viewModel: AuthViewModel = hiltViewModel()
            val state by viewModel.state.collectAsState()
            val actions by viewModel.actions.collectAsState()
            
            AuthScreen(
                state = state,
                onEvent = { event -> viewModel.handleEvent(event) },
                onNavigateToDeviceRegistration = { deviceId ->
                    navController.navigate(Route.deviceRegistration(deviceId))
                },
                onNavigateToHome = { deviceId ->
                    navController.navigate(Route.HOME) {
                        popUpTo(Route.AUTH) { inclusive = true }
                    }
                }
            )
            
            // Handle navigation actions from ViewModel
            actions?.let { action ->
                when (action) {
                    is com.ourcookbook.ui.viewmodel.AuthAction.NavigateToDeviceRegistration -> {
                        navController.navigate(Route.deviceRegistration(action.deviceId))
                        viewModel.clearAction()
                    }
                    is com.ourcookbook.ui.viewmodel.AuthAction.NavigateToHome -> {
                        navController.navigate(Route.HOME) {
                            popUpTo(Route.AUTH) { inclusive = true }
                        }
                        viewModel.clearAction()
                    }
                    else -> {}
                }
            }
        }
        
        composable(
            route = Route.DEVICE_REGISTRATION,
            arguments = listOf(navArgument(Route.ARG_DEVICE_ID) { 
                type = NavType.StringType
                nullable = true
            })
        ) { backStackEntry ->
            val deviceId = backStackEntry.arguments?.getString(Route.ARG_DEVICE_ID)
            val viewModel: DeviceRegistrationViewModel = hiltViewModel()
            
            // Set existing device ID if provided
            LaunchedEffect(deviceId) {
                deviceId?.let { viewModel.setExistingDeviceId(it) }
            }
            
            val state by viewModel.state.collectAsState()
            val actions by viewModel.actions.collectAsState()
            
            DeviceRegistrationScreen(
                state = state,
                onEvent = { event -> viewModel.handleEvent(event) },
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHome = { deviceId ->
                    navController.navigate(Route.HOME) {
                        popUpTo(Route.AUTH) { inclusive = true }
                    }
                }
            )
            
            // Handle navigation actions from ViewModel
            actions?.let { action ->
                when (action) {
                    is com.ourcookbook.ui.viewmodel.DeviceRegistrationAction.NavigateToHome -> {
                        navController.navigate(Route.HOME) {
                            popUpTo(Route.AUTH) { inclusive = true }
                        }
                        viewModel.clearAction()
                    }
                    is com.ourcookbook.ui.viewmodel.DeviceRegistrationAction.NavigateBack -> {
                        navController.popBackStack()
                        viewModel.clearAction()
                    }
                    else -> {}
                }
            }
        }
        
        composable(Route.DRIVE_AUTH) {
            DriveAuthScreen(
                onAuthSuccess = { 
                    navController.popBackStack() 
                },
                onAuthFailed = { 
                    navController.popBackStack() 
                }
            )
        }
        
        // ==================== MAIN APP FLOW ====================
        
        composable(Route.HOME) {
            val viewModel: HomeViewModel = hiltViewModel()
            val state by viewModel.state.collectAsState()
            
            HomeScreen(
                viewModel = viewModel,
                navController = navController
            )
        }
        
        // ==================== RECIPE FLOW ====================
        
        composable(Route.RECIPE_LIST) {
            val viewModel: RecipeListViewModel = hiltViewModel()
            val state by viewModel.state.collectAsState()
            val actions by viewModel.actions.collectAsState()
            
            RecipeListScreen(
                viewModel = viewModel,
                navController = navController
            )
            
            // Handle navigation actions from ViewModel
            actions?.let { action ->
                when (action) {
                    is com.ourcookbook.ui.viewmodel.RecipeListAction.ShowRecipeDetail -> {
                        navController.navigate(Route.recipeDetail(action.recipeId))
                        viewModel.clearAction()
                    }
                    else -> {}
                }
            }
        }
        
        composable(
            route = Route.RECIPE_DETAIL,
            arguments = listOf(navArgument(Route.ARG_RECIPE_ID) { 
                type = NavType.StringType 
            })
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getString(Route.ARG_RECIPE_ID) ?: return@composable
            
            RecipeDetailScreen(
                recipeId = recipeId,
                navController = navController
            )
        }
        
        composable(Route.RECIPE_CREATE) {
            val viewModel: RecipeEditViewModel = hiltViewModel()
            val state by viewModel.state.collectAsState()
            val actions by viewModel.actions.collectAsState()
            
            RecipeEditScreen(
                viewModel = viewModel,
                navController = navController,
                recipeId = null
            )
            
            // Handle navigation actions from ViewModel
            actions?.let { action ->
                when (action) {
                    is com.ourcookbook.ui.viewmodel.RecipeEditAction.NavigateToRecipeDetail -> {
                        navController.navigate(Route.recipeDetail(action.recipeId)) {
                            popUpTo(Route.RECIPE_CREATE) { inclusive = true }
                        }
                        viewModel.clearAction()
                    }
                    is com.ourcookbook.ui.viewmodel.RecipeEditAction.NavigateBack -> {
                        navController.popBackStack()
                        viewModel.clearAction()
                    }
                    else -> {}
                }
            }
        }
        
        composable(
            route = Route.RECIPE_EDIT,
            arguments = listOf(navArgument(Route.ARG_RECIPE_ID) { 
                type = NavType.StringType 
            })
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getString(Route.ARG_RECIPE_ID) ?: return@composable
            val viewModel: RecipeEditViewModel = hiltViewModel()
            val state by viewModel.state.collectAsState()
            val actions by viewModel.actions.collectAsState()
            
            RecipeEditScreen(
                viewModel = viewModel,
                navController = navController,
                recipeId = recipeId
            )
            
            // Handle navigation actions from ViewModel
            actions?.let { action ->
                when (action) {
                    is com.ourcookbook.ui.viewmodel.RecipeEditAction.NavigateToRecipeDetail -> {
                        navController.navigate(Route.recipeDetail(action.recipeId)) {
                            popUpTo(Route.RECIPE_EDIT) { inclusive = true }
                        }
                        viewModel.clearAction()
                    }
                    is com.ourcookbook.ui.viewmodel.RecipeEditAction.NavigateBack -> {
                        navController.popBackStack()
                        viewModel.clearAction()
                    }
                    else -> {}
                }
            }
        }
        
        // ==================== COOKBOOK FLOW ====================
        
        composable(Route.COOKBOOK_MANAGEMENT) {
            val viewModel: CookbookManagementViewModel = hiltViewModel()
            val state by viewModel.state.collectAsState()
            val actions by viewModel.actions.collectAsState()
            
            CookbookManagementScreen(
                viewModel = viewModel,
                navController = navController
            )
            
            // Handle navigation actions from ViewModel
            actions?.let { action ->
                when (action) {
                    is com.ourcookbook.ui.viewmodel.CookbookManagementAction.ShowCookbookDetail -> {
                        navController.navigate(Route.cookbookDetail(action.cookbookId))
                        viewModel.clearAction()
                    }
                    else -> {}
                }
            }
        }
        
        composable(Route.COOKBOOK_LIST) {
            val viewModel: CookbookManagementViewModel = hiltViewModel()
            val state by viewModel.state.collectAsState()
            val actions by viewModel.actions.collectAsState()
            
            CookbookListScreen(
                viewModel = viewModel,
                navController = navController
            )
            
            // Handle navigation actions from ViewModel
            actions?.let { action ->
                when (action) {
                    is com.ourcookbook.ui.viewmodel.CookbookManagementAction.ShowCookbookDetail -> {
                        navController.navigate(Route.cookbookDetail(action.cookbookId))
                        viewModel.clearAction()
                    }
                    else -> {}
                }
            }
        }
        
        composable(
            route = Route.COOKBOOK_DETAIL,
            arguments = listOf(navArgument(Route.ARG_COOKBOOK_ID) { 
                type = NavType.StringType 
            })
        ) { backStackEntry ->
            val cookbookId = backStackEntry.arguments?.getString(Route.ARG_COOKBOOK_ID) ?: return@composable
            val viewModel: CookbookManagementViewModel = hiltViewModel()
            
            LaunchedEffect(cookbookId) {
                viewModel.handleEvent(com.ourcookbook.ui.viewmodel.CookbookManagementEvent.SelectCookbook(cookbookId))
            }
            
            CookbookDetailScreen(
                viewModel = viewModel,
                cookbookId = cookbookId,
                navController = navController
            )
        }
        
        composable(Route.COOKBOOK_CREATE) {
            val viewModel: CookbookManagementViewModel = hiltViewModel()
            val state by viewModel.state.collectAsState()
            val actions by viewModel.actions.collectAsState()
            
            CookbookEditScreen(
                viewModel = viewModel,
                isCreating = true,
                navController = navController
            )
            
            // Handle navigation actions from ViewModel
            actions?.let { action ->
                when (action) {
                    is com.ourcookbook.ui.viewmodel.CookbookManagementAction.ShowCookbookDetail -> {
                        navController.navigate(Route.cookbookDetail(action.cookbookId)) {
                            popUpTo(Route.COOKBOOK_CREATE) { inclusive = true }
                        }
                        viewModel.clearAction()
                    }
                    else -> {}
                }
            }
        }
        
        composable(
            route = Route.COOKBOOK_EDIT,
            arguments = listOf(navArgument(Route.ARG_COOKBOOK_ID) { 
                type = NavType.StringType 
            })
        ) { backStackEntry ->
            val cookbookId = backStackEntry.arguments?.getString(Route.ARG_COOKBOOK_ID) ?: return@composable
            val viewModel: CookbookManagementViewModel = hiltViewModel()
            
            CookbookEditScreen(
                viewModel = viewModel,
                cookbookId = cookbookId,
                isCreating = false,
                navController = navController
            )
        }
        
        // ==================== SEARCH FLOW ====================
        
        composable(Route.SEARCH) {
            val viewModel: SearchViewModel = hiltViewModel()
            val state by viewModel.state.collectAsState()
            val actions by viewModel.actions.collectAsState()
            
            SearchScreen(
                viewModel = viewModel,
                navController = navController
            )
            
            // Handle navigation actions from ViewModel
            actions?.let { action ->
                when (action) {
                    is com.ourcookbook.ui.viewmodel.SearchAction.ShowRecipeDetail -> {
                        navController.navigate(Route.recipeDetail(action.recipeId))
                        viewModel.clearAction()
                    }
                    else -> {}
                }
            }
        }
        
        composable(Route.FAVORITES) {
            val viewModel: RecipeListViewModel = hiltViewModel()
            val state by viewModel.state.collectAsState()
            
            LaunchedEffect(Unit) {
                viewModel.handleEvent(com.ourcookbook.ui.viewmodel.RecipeListEvent.FilterByFavorites(true))
            }
            
            FavoritesScreen(
                viewModel = viewModel,
                navController = navController
            )
        }
        
        composable(Route.CATEGORIES) {
            // Categories screen - navigate to recipe list with category filter
            // This will be implemented in a future task
            // For now, redirect to recipe list
            LaunchedEffect(Unit) {
                navController.navigate(Route.RECIPE_LIST) {
                    popUpTo(Route.CATEGORIES) { inclusive = true }
                }
            }
        }
        
        // ==================== SYNC FLOW ====================
        
        composable(Route.SYNC_STATUS) {
            val viewModel: SyncViewModel = hiltViewModel()
            val state by viewModel.state.collectAsState()
            val actions by viewModel.actions.collectAsState()
            
            SyncStatusScreen(
                viewModel = viewModel,
                navController = navController
            )
            
            // Handle navigation actions from ViewModel
            actions?.let { action ->
                when (action) {
                    is com.ourcookbook.ui.viewmodel.SyncAction.ShowConflictResolution -> {
                        navController.navigate(Route.conflictResolution(action.conflictId))
                        viewModel.clearAction()
                    }
                    else -> {}
                }
            }
        }
        
        composable(
            route = Route.CONFLICT_RESOLUTION,
            arguments = listOf(navArgument(Route.ARG_CONFLICT_ID) { 
                type = NavType.StringType 
            })
        ) { backStackEntry ->
            val conflictId = backStackEntry.arguments?.getString(Route.ARG_CONFLICT_ID) ?: return@composable
            val viewModel: ConflictResolutionViewModel = hiltViewModel()
            val state by viewModel.state.collectAsState()
            val actions by viewModel.actions.collectAsState()
            
            LaunchedEffect(conflictId) {
                viewModel.handleEvent(com.ourcookbook.ui.viewmodel.ConflictResolutionEvent.LoadConflict(conflictId))
            }
            
            ConflictResolutionScreen(
                viewModel = viewModel,
                conflictId = conflictId,
                navController = navController
            )
            
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
        }
        
        // ==================== UTILITY FLOW ====================
        
        composable(Route.OCR_SCANNER) {
            val viewModel: ScanViewModel = hiltViewModel()
            val state by viewModel.state.collectAsState()
            val actions by viewModel.actions.collectAsState()
            
            OcrScannerScreen(
                viewModel = viewModel,
                navController = navController
            )
            
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
        }
        
        composable(Route.SETTINGS) {
            val viewModel: SettingsViewModel = hiltViewModel()
            val state by viewModel.state.collectAsState()
            val actions by viewModel.actions.collectAsState()
            
            SettingsScreen(
                viewModel = viewModel,
                navController = navController
            )
            
            // Handle navigation actions from ViewModel
            actions?.let { action ->
                when (action) {
                    is com.ourcookbook.ui.viewmodel.SettingsAction.NavigateToSyncStatus -> {
                        navController.navigate(Route.SYNC_STATUS)
                        viewModel.clearAction()
                    }
                    is com.ourcookbook.ui.viewmodel.SettingsAction.NavigateToDriveAuth -> {
                        navController.navigate(Route.DRIVE_AUTH)
                        viewModel.clearAction()
                    }
                    else -> {}
                }
            }
        }
    }
}

/**
 * Navigation utility functions for handling common navigation patterns
 */
object NavigationUtils {
    fun navigateToRecipeDetail(navController: NavController, recipeId: String) {
        navController.navigate(Route.recipeDetail(recipeId))
    }
    
    fun navigateToRecipeEdit(navController: NavController, recipeId: String) {
        navController.navigate(Route.recipeEdit(recipeId))
    }
    
    fun navigateToCookbookDetail(navController: NavController, cookbookId: String) {
        navController.navigate(Route.cookbookDetail(cookbookId))
    }
    
    fun navigateToConflictResolution(navController: NavController, conflictId: String) {
        navController.navigate(Route.conflictResolution(conflictId))
    }
    
    fun navigateWithClearBackStack(navController: NavController, route: String) {
        navController.navigate(route) {
            popUpTo(0)
        }
    }
}
