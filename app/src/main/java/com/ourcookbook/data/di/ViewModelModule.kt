package com.ourcookbook.data.di

import com.ourcookbook.domain.usecase.AllUseCases
import com.ourcookbook.domain.usecase.cookbook.AddRecipeToCookbook
import com.ourcookbook.domain.usecase.cookbook.CreateCookbook
import com.ourcookbook.domain.usecase.cookbook.DeleteCookbook
import com.ourcookbook.domain.usecase.cookbook.ExportCookbook
import com.ourcookbook.domain.usecase.cookbook.GetAllCookbooks
import com.ourcookbook.domain.usecase.cookbook.GetCookbookById
import com.ourcookbook.domain.usecase.cookbook.GetCookbooksByOwner
import com.ourcookbook.domain.usecase.cookbook.GetSharedCookbooks
import com.ourcookbook.domain.usecase.cookbook.GetSharingInfo
import com.ourcookbook.domain.usecase.cookbook.GenerateSharingLink
import com.ourcookbook.domain.usecase.cookbook.ImportCookbook
import com.ourcookbook.domain.usecase.cookbook.RemoveRecipeFromCookbook
import com.ourcookbook.domain.usecase.cookbook.SearchCookbooks
import com.ourcookbook.domain.usecase.cookbook.ShareCookbook
import com.ourcookbook.domain.usecase.cookbook.UpdateCookbook
import com.ourcookbook.domain.usecase.device.CreateDevice
import com.ourcookbook.domain.usecase.device.GetDeviceByDeviceId
import com.ourcookbook.domain.usecase.device.UpdateDevice
import com.ourcookbook.domain.usecase.devicepreferences.CreateDevicePreferences
import com.ourcookbook.domain.usecase.devicepreferences.GetDevicePreferencesByDevice
import com.ourcookbook.domain.usecase.devicepreferences.UpdateDevicePreferences
import com.ourcookbook.domain.usecase.ingredient.CreateIngredient
import com.ourcookbook.domain.usecase.ingredient.DeleteIngredient
import com.ourcookbook.domain.usecase.ingredient.GetIngredientsByRecipe
import com.ourcookbook.domain.usecase.recipe.CreateRecipe
import com.ourcookbook.domain.usecase.recipe.DeleteRecipe
import com.ourcookbook.domain.usecase.recipe.GetAllRecipes
import com.ourcookbook.domain.usecase.recipe.GetFavorites
import com.ourcookbook.domain.usecase.recipe.GetRecipeById
import com.ourcookbook.domain.usecase.recipe.GetRecipesByCategory
import com.ourcookbook.domain.usecase.recipe.SearchRecipes
import com.ourcookbook.domain.usecase.recipe.ToggleFavorite
import com.ourcookbook.domain.usecase.recipe.UpdateRecipe
import com.ourcookbook.domain.usecase.recipe.SyncRecipe
import com.ourcookbook.domain.usecase.recipe.GetRecipesNeedingSync
import com.ourcookbook.domain.usecase.recipe.MarkRecipeSynced
import com.ourcookbook.domain.usecase.recipe.GetUpdatedSince
import com.ourcookbook.domain.usecase.recipe.DetectConflicts
import com.ourcookbook.domain.usecase.recipe.ResolveConflict
import com.ourcookbook.domain.usecase.recipe.ValidateChecksum
import com.ourcookbook.domain.usecase.recipe.UpdateChecksum
import com.ourcookbook.domain.usecase.recipe.GetRecipeByChecksum
import com.ourcookbook.domain.usecase.recipe.GetRecipesByDevice
import com.ourcookbook.domain.usecase.recipe.GetRecentRecipes
import com.ourcookbook.domain.usecase.recipe.GetTopRatedRecipes
import com.ourcookbook.domain.usecase.recipe.GetRecipeCount
import com.ourcookbook.domain.usecase.recipe.GetRecipesByIds
import com.ourcookbook.domain.usecase.recipe.GetAllRecipesOnce
import com.ourcookbook.domain.usecase.recipe.FilterRecipesByTags
import com.ourcookbook.domain.usecase.recipe.FilterRecipesByCookingTime
import com.ourcookbook.domain.usecase.recipe.FilterRecipesByServingSize
import com.ourcookbook.domain.usecase.recipeimage.CreateRecipeImage
import com.ourcookbook.domain.usecase.sync.GetSyncStatus
import com.ourcookbook.domain.usecase.sync.GetPendingConflictCount
import com.ourcookbook.domain.usecase.sync.GetAllConflicts
import com.ourcookbook.domain.usecase.sync.GetConflictById
import com.ourcookbook.domain.usecase.sync.ResolveSyncConflict
import com.ourcookbook.domain.usecase.sync.UpdateSyncInProgress
import com.ourcookbook.domain.usecase.sync.UpdateLastSyncTimestamp
import com.ourcookbook.domain.usecase.sync.GetAllSyncMetadata
import com.ourcookbook.domain.usecase.device.GetAllDevices
import com.ourcookbook.domain.repository.SyncLogRepository
import com.ourcookbook.ui.service.SyncStatusService
import com.ourcookbook.ui.viewmodel.AuthViewModel
import com.ourcookbook.ui.viewmodel.CookbookManagementViewModel
import com.ourcookbook.ui.viewmodel.ConflictResolutionViewModel
import com.ourcookbook.ui.viewmodel.DeviceRegistrationViewModel
import com.ourcookbook.ui.viewmodel.HomeViewModel
import com.ourcookbook.ui.viewmodel.RecipeDetailViewModel
import com.ourcookbook.ui.viewmodel.RecipeEditViewModel
import com.ourcookbook.ui.viewmodel.RecipeListViewModel
import com.ourcookbook.ui.viewmodel.ScanViewModel
import com.ourcookbook.ui.viewmodel.SearchViewModel
import com.ourcookbook.ui.viewmodel.SettingsViewModel
import com.ourcookbook.ui.viewmodel.SyncStatusViewModel
import com.ourcookbook.ui.viewmodel.SyncViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.components.ViewModelComponent.ViewModelSubcomponent

/**
 * Hilt module for providing all ViewModel dependencies
 * This module provides all the ViewModels for dependency injection
 */
@Module
@InstallIn(ViewModelComponent::class)
object ViewModelModule {

    // Home ViewModel
    @Provides
    fun provideHomeViewModel(
        getRecipes: GetAllRecipes,
        getCookbooks: GetAllCookbooks,
        searchRecipes: SearchRecipes,
        syncStatusService: SyncStatusService
    ): HomeViewModel = HomeViewModel(getRecipes, getCookbooks, searchRecipes, syncStatusService)

    // Recipe List ViewModel
    @Provides
    fun provideRecipeListViewModel(
        getAllRecipes: GetAllRecipes,
        getFavorites: GetFavorites,
        getRecipesByCategory: GetRecipesByCategory,
        searchRecipes: SearchRecipes,
        toggleFavorite: ToggleFavorite
    ): RecipeListViewModel = RecipeListViewModel(
        getAllRecipes, getFavorites, getRecipesByCategory, searchRecipes, toggleFavorite
    )

    // Recipe Detail ViewModel
    @Provides
    fun provideRecipeDetailViewModel(
        getRecipeById: GetRecipeById,
        toggleFavorite: ToggleFavorite,
        updateRecipe: UpdateRecipe,
        deleteRecipe: DeleteRecipe
    ): RecipeDetailViewModel = RecipeDetailViewModel(
        getRecipeById, toggleFavorite, updateRecipe, deleteRecipe
    )

    // Recipe Edit ViewModel
    @Provides
    fun provideRecipeEditViewModel(
        getRecipeById: GetRecipeById,
        createRecipe: CreateRecipe,
        updateRecipe: UpdateRecipe,
        createIngredient: CreateIngredient,
        updateIngredient: UpdateIngredient,
        deleteIngredient: DeleteIngredient,
        getIngredientsByRecipe: GetIngredientsByRecipe
    ): RecipeEditViewModel = RecipeEditViewModel(
        getRecipeById, createRecipe, updateRecipe, createIngredient, 
        updateIngredient, deleteIngredient, getIngredientsByRecipe
    )

    // Search ViewModel
    @Provides
    fun provideSearchViewModel(
        searchRecipes: SearchRecipes,
        getRecipesByCategory: GetRecipesByCategory,
        filterRecipesByTags: FilterRecipesByTags,
        filterRecipesByCookingTime: FilterRecipesByCookingTime,
        filterRecipesByServingSize: FilterRecipesByServingSize,
        getFavorites: GetFavorites
    ): SearchViewModel = SearchViewModel(
        searchRecipes, getRecipesByCategory, filterRecipesByTags, 
        filterRecipesByCookingTime, filterRecipesByServingSize, getFavorites
    )

    // Scan ViewModel
    @Provides
    fun provideScanViewModel(
        createRecipe: CreateRecipe,
        createRecipeImage: CreateRecipeImage
    ): ScanViewModel = ScanViewModel(createRecipe, createRecipeImage)

     // Sync ViewModel
    @Provides
    fun provideSyncViewModel(
        getSyncStatus: GetSyncStatus,
        getPendingConflictCount: GetPendingConflictCount,
        getAllConflicts: GetAllConflicts,
        getAllSyncMetadata: com.ourcookbook.domain.usecase.sync.GetAllSyncMetadata,
        updateSyncInProgress: UpdateSyncInProgress,
        updateLastSyncTimestamp: UpdateLastSyncTimestamp,
        syncRecipe: SyncRecipe,
        getRecipesNeedingSync: GetRecipesNeedingSync
    ): SyncViewModel = SyncViewModel(
        getSyncStatus, getPendingConflictCount, getAllConflicts, getAllSyncMetadata,
        updateSyncInProgress, updateLastSyncTimestamp, syncRecipe, getRecipesNeedingSync
    )

    // Sync Status ViewModel
    @Provides
    fun provideSyncStatusViewModel(
        getSyncStatus: GetSyncStatus,
        getPendingConflictCount: GetPendingConflictCount,
        getAllConflicts: GetAllConflicts,
        getAllSyncMetadata: GetAllSyncMetadata,
        updateSyncInProgress: UpdateSyncInProgress,
        updateLastSyncTimestamp: UpdateLastSyncTimestamp,
        syncRecipe: SyncRecipe,
        getRecipesNeedingSync: GetRecipesNeedingSync,
        resolveSyncConflict: ResolveSyncConflict,
        getAllDevices: GetAllDevices,
        syncLogRepository: SyncLogRepository
    ): SyncStatusViewModel = SyncStatusViewModel(
        getSyncStatus, getPendingConflictCount, getAllConflicts, getAllSyncMetadata,
        updateSyncInProgress, updateLastSyncTimestamp, syncRecipe, getRecipesNeedingSync,
        resolveSyncConflict, getAllDevices, syncLogRepository
    )

    // Cookbook Management ViewModel
    @Provides
    fun provideCookbookManagementViewModel(
        createCookbook: CreateCookbook,
        updateCookbook: UpdateCookbook,
        deleteCookbook: DeleteCookbook,
        getAllCookbooks: GetAllCookbooks,
        getCookbooksByOwner: GetCookbooksByOwner,
        getSharedCookbooks: GetSharedCookbooks,
        searchCookbooks: SearchCookbooks,
        addRecipeToCookbook: AddRecipeToCookbook,
        removeRecipeFromCookbook: RemoveRecipeFromCookbook,
        exportCookbook: ExportCookbook,
        importCookbook: ImportCookbook,
        shareCookbook: ShareCookbook,
        generateSharingLink: GenerateSharingLink,
        getSharingInfo: GetSharingInfo
    ): CookbookManagementViewModel = CookbookManagementViewModel(
        createCookbook, updateCookbook, deleteCookbook, getAllCookbooks,
        getCookbooksByOwner, getSharedCookbooks, searchCookbooks,
        addRecipeToCookbook, removeRecipeFromCookbook, exportCookbook, importCookbook,
        shareCookbook, generateSharingLink, getSharingInfo
    )

    // Conflict Resolution ViewModel
    @Provides
    fun provideConflictResolutionViewModel(
        getConflictById: GetConflictById,
        resolveSyncConflict: ResolveSyncConflict,
        getConflictsByRecipe: GetConflictsByRecipe,
        updateConflict: com.ourcookbook.domain.usecase.sync.UpdateConflict
    ): ConflictResolutionViewModel = ConflictResolutionViewModel(
        getConflictById, resolveSyncConflict, getConflictsByRecipe, updateConflict
    )

    // Auth ViewModel
    @Provides
    fun provideAuthViewModel(
        createDevice: CreateDevice,
        getDeviceByDeviceId: GetDeviceByDeviceId,
        updateDevice: UpdateDevice,
        createDevicePreferences: CreateDevicePreferences,
        getDevicePreferencesByDevice: GetDevicePreferencesByDevice
    ): AuthViewModel = AuthViewModel(
        createDevice, getDeviceByDeviceId, updateDevice, 
        createDevicePreferences, getDevicePreferencesByDevice
    )

    // Device Registration ViewModel
    @Provides
    fun provideDeviceRegistrationViewModel(
        createDevice: CreateDevice,
        getDeviceByDeviceId: GetDeviceByDeviceId,
        updateDevice: UpdateDevice,
        createDevicePreferences: CreateDevicePreferences
    ): DeviceRegistrationViewModel = DeviceRegistrationViewModel(
        createDevice, getDeviceByDeviceId, updateDevice, createDevicePreferences
    )

    // Settings ViewModel
    @Provides
    fun provideSettingsViewModel(
        getDevicePreferencesByDevice: GetDevicePreferencesByDevice,
        updateDevicePreferences: UpdateDevicePreferences,
        createDevicePreferences: CreateDevicePreferences,
        getSyncStatus: GetSyncStatus,
        updateSyncInProgress: UpdateSyncInProgress,
        updateLastSyncTimestamp: UpdateLastSyncTimestamp
    ): SettingsViewModel = SettingsViewModel(
        getDevicePreferencesByDevice, updateDevicePreferences, createDevicePreferences,
        getSyncStatus, updateSyncInProgress, updateLastSyncTimestamp
    )
}