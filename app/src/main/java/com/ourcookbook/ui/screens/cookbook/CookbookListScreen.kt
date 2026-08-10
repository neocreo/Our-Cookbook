package com.ourcookbook.ui.screens.cookbook

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController

/**
 * Cookbook List Screen
 * Task 1.9: Complete Navigation Setup
 * 
 * Displays a list of user's cookbooks
 * For now, redirects to CookbookManagementScreen
 */

@Composable
fun CookbookListScreen(
    viewModel: com.ourcookbook.ui.viewmodel.CookbookManagementViewModel,
    navController: NavController
) {
    // For now, redirect to the management screen
    // This could be a simplified list view in the future
    LaunchedEffect(Unit) {
        navController.navigate(com.ourcookbook.ui.navigation.Route.COOKBOOK_MANAGEMENT) {
            popUpTo(com.ourcookbook.ui.navigation.Route.COOKBOOK_LIST) { inclusive = true }
        }
    }
}
