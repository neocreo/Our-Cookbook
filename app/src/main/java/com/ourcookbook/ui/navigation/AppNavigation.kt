package com.ourcookbook.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.ourcookbook.ui.screens.auth.AuthScreen
import com.ourcookbook.ui.viewmodel.AuthState
import com.ourcookbook.ui.viewmodel.AuthViewModel
import com.ourcookbook.ui.viewmodel.DeviceRegistrationState
import com.ourcookbook.ui.viewmodel.DeviceRegistrationViewModel
import com.ourcookbook.ui.screens.cookbook.CookbookManagementScreen
import com.ourcookbook.ui.screens.home.HomeScreen
import com.ourcookbook.ui.screens.ocr.OCRScannerScreen
import com.ourcookbook.ui.screens.recipe.RecipeCreateScreen
import com.ourcookbook.ui.screens.recipe.RecipeDetailScreen
import com.ourcookbook.ui.screens.recipe.RecipeEditScreen
import com.ourcookbook.ui.screens.recipe.RecipeListScreen
import com.ourcookbook.ui.screens.search.SearchScreen
import com.ourcookbook.ui.screens.settings.SettingsScreen
import com.ourcookbook.ui.screens.sync.ConflictResolutionScreen
import com.ourcookbook.ui.screens.auth.DeviceRegistrationScreen
import com.ourcookbook.ui.screens.sync.SyncStatusScreen

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Route.AUTH,
        modifier = modifier
    ) {
        // Authentication flow
        composable(Route.AUTH) {
            AuthScreen(state = com.ourcookbook.ui.viewmodel.AuthState.Idle, onEvent = {}, onNavigateToDeviceRegistration = { navController.navigate(Route.DEVICE_REGISTRATION) }, onNavigateToHome = { navController.navigate(Route.HOME) })
        }

        composable(Route.DEVICE_REGISTRATION) {
            DeviceRegistrationScreen(state = com.ourcookbook.ui.viewmodel.DeviceRegistrationState(), onEvent = {}, onNavigateBack = { navController.popBackStack() }, onNavigateToHome = { navController.navigate(Route.HOME) })
        }

        // Main flow
        composable(Route.HOME) {
            HomeScreen(navController = navController)
        }

        composable(Route.RECIPE_LIST) {
            RecipeListScreen(navController = navController)
        }

        composable(
            route = Route.RECIPE_DETAIL,
            arguments = listOf(navArgument("recipeId") { 
                type = androidx.navigation.NavType.StringType
            })
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getString("recipeId") ?: ""
            RecipeDetailScreen(
                recipeId = recipeId,
                navController = navController
            )
        }

        composable(
            route = Route.RECIPE_EDIT,
            arguments = listOf(navArgument("recipeId") { 
                type = androidx.navigation.NavType.StringType
            })
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getString("recipeId")
            RecipeEditScreen(
                recipeId = recipeId ?: "",
                navController = navController
            )
        }

        composable(Route.RECIPE_CREATE) {
            RecipeCreateScreen(navController = navController)
        }

        composable(Route.COOKBOOK_MANAGEMENT) {
            CookbookManagementScreen(viewModel = hiltViewModel(), navController = navController)
        }

        composable(Route.SEARCH) {
            SearchScreen(viewModel = hiltViewModel(), navController = navController)
        }

        composable(Route.SETTINGS) {
            SettingsScreen(viewModel = hiltViewModel(), navController = navController)
        }

        composable(Route.SYNC_STATUS) {
            SyncStatusScreen(viewModel = hiltViewModel(), navController = navController)
        }

        composable(
            route = Route.CONFLICT_RESOLUTION,
            arguments = listOf(navArgument("conflictId") { 
                type = androidx.navigation.NavType.StringType
            })
        ) { backStackEntry ->
            val conflictId = backStackEntry.arguments?.getString("conflictId")
            ConflictResolutionScreen(
                viewModel = hiltViewModel(),
                conflictId = conflictId ?: "",
                navController = navController
            )
        }

        composable(Route.OCR_SCANNER) {
            OCRScannerScreen(navController = navController)
        }
    }
}
