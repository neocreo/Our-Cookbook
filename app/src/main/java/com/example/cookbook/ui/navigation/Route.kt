package com.ourcookbook.cookbook.ui.navigation

/**
 * Navigation Routes for Cookbook Android App
 * DEPRECATED: Use com.ourcookbook.ui.navigation.Route instead
 * 
 * This file is kept for backward compatibility but should not be used for new development.
 * All new navigation should use the updated Route.kt in com.ourcookbook.ui.navigation
 */

@Deprecated("Use com.ourcookbook.ui.navigation.Route instead")
object Route {
    // Main navigation routes
    const val HOME = "home"
    const val RECIPE_LIST = "recipe_list"
    const val RECIPE_DETAIL = "recipe_detail/{recipeId}"
    const val RECIPE_EDIT = "recipe_edit/{recipeId}"
    const val RECIPE_CREATE = "recipe_create"
    const val COOKBOOK_LIST = "cookbook_list"
    const val COOKBOOK_DETAIL = "cookbook_detail/{cookbookId}"
    const val COOKBOOK_CREATE = "cookbook_create"
    const val COOKBOOK_EDIT = "cookbook_edit/{cookbookId}"
    const val SEARCH = "search"
    const val OCR_SCANNER = "ocr_scanner"
    const val SYNC_STATUS = "sync_status"
    const val SETTINGS = "settings"
    const val DRIVE_AUTH = "drive_auth"
    const val FAVORITES = "favorites"
    const val CATEGORIES = "categories"
    const val CONFLICT_RESOLUTION = "conflict_resolution/{conflictId}"
    
    // Utility functions for building routes with arguments
    fun recipeDetail(recipeId: Long) = "recipe_detail/$recipeId"
    fun recipeEdit(recipeId: Long) = "recipe_edit/$recipeId"
    fun cookbookDetail(cookbookId: Long) = "cookbook_detail/$cookbookId"
    fun cookbookEdit(cookbookId: Long) = "cookbook_edit/$cookbookId"
    fun conflictResolution(conflictId: Long) = "conflict_resolution/$conflictId"
    
    // Route constants for navigation arguments
    const val ARG_RECIPE_ID = "recipeId"
    const val ARG_COOKBOOK_ID = "cookbookId"
    const val ARG_CONFLICT_ID = "conflictId"
    const val ARG_SEARCH_QUERY = "searchQuery"
    const val ARG_CATEGORY = "category"
}

// Navigation argument keys
@Deprecated("Use com.ourcookbook.ui.navigation.NavArg instead")
sealed class NavArg(val key: String) {
    object RecipeId : NavArg(Route.ARG_RECIPE_ID)
    object CookbookId : NavArg(Route.ARG_COOKBOOK_ID)
    object ConflictId : NavArg(Route.ARG_CONFLICT_ID)
    object SearchQuery : NavArg(Route.ARG_SEARCH_QUERY)
    object Category : NavArg(Route.ARG_CATEGORY)
}
