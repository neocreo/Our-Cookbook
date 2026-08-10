package com.ourcookbook.ui.navigation

/**
 * Navigation Routes for Cookbook Android App
 * Task 1.9: Complete Navigation Setup
 * 
 * Centralized route definitions for Jetpack Compose Navigation
 * Includes all required routes: Home, Recipe List, Recipe Detail, Create/Edit Recipe,
 * Search, Scan, Sync, Cookbook Management, Settings, Auth, Conflict Resolution, Device Registration
 */

object Route {
    // Authentication Routes
    const val AUTH = "auth"
    const val DEVICE_REGISTRATION = "device_registration"
    const val DRIVE_AUTH = "drive_auth"
    
    // Main App Routes
    const val HOME = "home"
    const val RECIPE_LIST = "recipe_list"
    const val RECIPE_DETAIL = "recipe_detail/{recipeId}"
    const val RECIPE_CREATE = "recipe_create"
    const val RECIPE_EDIT = "recipe_edit/{recipeId}"
    
    // Cookbook Management Routes
    const val COOKBOOK_MANAGEMENT = "cookbook_management"
    const val COOKBOOK_LIST = "cookbook_list"
    const val COOKBOOK_DETAIL = "cookbook_detail/{cookbookId}"
    const val COOKBOOK_CREATE = "cookbook_create"
    const val COOKBOOK_EDIT = "cookbook_edit/{cookbookId}"
    
    // Search and Discovery Routes
    const val SEARCH = "search"
    const val FAVORITES = "favorites"
    const val CATEGORIES = "categories"
    
    // Sync and Conflict Routes
    const val SYNC_STATUS = "sync_status"
    const val CONFLICT_RESOLUTION = "conflict_resolution/{conflictId}"
    const val SYNC_DETAILS = "sync_details/{syncId}"
    const val DEVICE_MANAGEMENT = "device_management"
    const val DEVICE_DETAIL = "device_detail/{deviceId}"
    
    // Utility Routes
    const val OCR_SCANNER = "ocr_scanner"
    const val SETTINGS = "settings"
    const val EXPORT_IMPORT = "export_import"
    const val EXPORT_IMPORT_FORMAT = "export_import_format"
    const val EXPORT_IMPORT_HISTORY = "export_import_history"
    
    // Route constants for navigation arguments
    const val ARG_RECIPE_ID = "recipeId"
    const val ARG_COOKBOOK_ID = "cookbookId"
    const val ARG_CONFLICT_ID = "conflictId"
    const val ARG_SYNC_ID = "syncId"
    const val ARG_SEARCH_QUERY = "searchQuery"
    const val ARG_CATEGORY = "category"
    const val ARG_DEVICE_ID = "deviceId"
    
    // Utility functions for building routes with arguments
    fun recipeDetail(recipeId: String) = "recipe_detail/$recipeId"
    fun recipeEdit(recipeId: String) = "recipe_edit/$recipeId"
    fun cookbookDetail(cookbookId: String) = "cookbook_detail/$cookbookId"
    fun cookbookEdit(cookbookId: String) = "cookbook_edit/$cookbookId"
    fun conflictResolution(conflictId: String) = "conflict_resolution/$conflictId"
    fun syncDetails(syncId: String) = "sync_details/$syncId"
    fun deviceDetail(deviceId: String) = "device_detail/$deviceId"
    fun deviceRegistration(deviceId: String? = null) = deviceId?.let { "device_registration/$it" } ?: DEVICE_REGISTRATION
    
    // Route patterns for navigation with optional arguments
    fun recipeListWithCategory(category: String) = "recipe_list?category=$category"
    fun exportImportFormat(isExport: Boolean) = "export_import_format?isExport=$isExport"
    fun recipeListWithFavorites() = "recipe_list?favorites=true"
    fun searchWithQuery(query: String) = "search?query=$query"
}

// Navigation argument keys
sealed class NavArg(val key: String) {
    object RecipeId : NavArg(Route.ARG_RECIPE_ID)
    object CookbookId : NavArg(Route.ARG_COOKBOOK_ID)
    object ConflictId : NavArg(Route.ARG_CONFLICT_ID)
    object SyncId : NavArg(Route.ARG_SYNC_ID)
    object SearchQuery : NavArg(Route.ARG_SEARCH_QUERY)
    object Category : NavArg(Route.ARG_CATEGORY)
    object DeviceId : NavArg(Route.ARG_DEVICE_ID)
}

// Navigation destinations grouped by feature
object AuthDestinations {
    const val AUTH = Route.AUTH
    const val DEVICE_REGISTRATION = Route.DEVICE_REGISTRATION
    const val DRIVE_AUTH = Route.DRIVE_AUTH
}

object RecipeDestinations {
    const val HOME = Route.HOME
    const val RECIPE_LIST = Route.RECIPE_LIST
    const val RECIPE_DETAIL = Route.RECIPE_DETAIL
    const val RECIPE_CREATE = Route.RECIPE_CREATE
    const val RECIPE_EDIT = Route.RECIPE_EDIT
    const val FAVORITES = Route.FAVORITES
    const val CATEGORIES = Route.CATEGORIES
}

object CookbookDestinations {
    const val COOKBOOK_MANAGEMENT = Route.COOKBOOK_MANAGEMENT
    const val COOKBOOK_LIST = Route.COOKBOOK_LIST
    const val COOKBOOK_DETAIL = Route.COOKBOOK_DETAIL
    const val COOKBOOK_CREATE = Route.COOKBOOK_CREATE
    const val COOKBOOK_EDIT = Route.COOKBOOK_EDIT
}

object SyncDestinations {
    const val SYNC_STATUS = Route.SYNC_STATUS
    const val CONFLICT_RESOLUTION = Route.CONFLICT_RESOLUTION
    const val SYNC_DETAILS = Route.SYNC_DETAILS
    const val DEVICE_MANAGEMENT = Route.DEVICE_MANAGEMENT
    const val DEVICE_DETAIL = Route.DEVICE_DETAIL
}

object UtilityDestinations {
    const val SEARCH = Route.SEARCH
    const val OCR_SCANNER = Route.OCR_SCANNER
    const val SETTINGS = Route.SETTINGS
    const val EXPORT_IMPORT = Route.EXPORT_IMPORT
    const val EXPORT_IMPORT_FORMAT = Route.EXPORT_IMPORT_FORMAT
    const val EXPORT_IMPORT_HISTORY = Route.EXPORT_IMPORT_HISTORY
}
