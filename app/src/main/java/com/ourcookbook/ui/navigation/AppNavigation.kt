package com.ourcookbook.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.ourcookbook.ui.screens.auth.AuthScreen
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
import com.ourcookbook.ui.screens.sync.DeviceRegistrationScreen
import com.ourcookbook.ui.screens.sync.SyncStatusScreen

object Route {
    const val HOME = "home"
    const val RECIPE_LIST = "recipe_list"
    const val RECIPE_DETAIL = "recipe_detail/{recipeId}"
    const val RECIPE_EDIT = "recipe_edit/{recipeId}"
    const val RECIPE_CREATE = "recipe_create"
    const val COOKBOOK_MANAGEMENT = "cookbook_management"
    const val SEARCH = "search"
    const val SETTINGS = "settings"
    const val SYNC_STATUS = "sync_status"
    const val CONFLICT_RESOLUTION = "conflict_resolution/{conflictId}"
    const val OCR_SCANNER = "ocr_scanner"
    const val AUTH = "auth"
    const val DEVICE_REGISTRATION = "device_registration"
}

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
            AuthScreen(navController = navController)
        }

        composable(Route.DEVICE_REGISTRATION) {
            DeviceRegistrationScreen(navController = navController)
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
            CookbookManagementScreen(navController = navController)
        }

        composable(Route.SEARCH) {
            SearchScreen(navController = navController)
        }

        composable(Route.SETTINGS) {
            SettingsScreen(navController = navController)
        }

        composable(Route.SYNC_STATUS) {
            SyncStatusScreen(navController = navController)
        }

        composable(
            route = Route.CONFLICT_RESOLUTION,
            arguments = listOf(navArgument("conflictId") { 
                type = androidx.navigation.NavType.StringType
            })
        ) { backStackEntry ->
            val conflictId = backStackEntry.arguments?.getString("conflictId")
            ConflictResolutionScreen(
                conflictId = conflictId ?: "",
                navController = navController
            )
        }

        composable(Route.OCR_SCANNER) {
            OCRScannerScreen(navController = navController)
        }
    }
}
