package com.ourcookbook.data.di

import com.ourcookbook.domain.repository.CookbookRepository
import com.ourcookbook.domain.repository.DevicePreferencesRepository
import com.ourcookbook.domain.repository.DeviceRepository
import com.ourcookbook.domain.repository.IngredientRepository
import com.ourcookbook.domain.repository.RecipeImageRepository
import com.ourcookbook.domain.repository.RecipeRepository
import com.ourcookbook.domain.repository.SyncConflictRepository
import com.ourcookbook.domain.repository.SyncMetadataRepository
import com.ourcookbook.domain.repository.FullTextSearchRepository
import com.ourcookbook.domain.usecase.cookbook.AddRecipeToCookbook
import com.ourcookbook.domain.usecase.cookbook.CreateCookbook
import com.ourcookbook.domain.usecase.cookbook.DeleteCookbook
import com.ourcookbook.domain.usecase.cookbook.ExportCookbook
import com.ourcookbook.domain.usecase.cookbook.GetAllCookbooks
import com.ourcookbook.domain.usecase.cookbook.GetCookbookById
import com.ourcookbook.domain.usecase.cookbook.GetCookbookCount
import com.ourcookbook.domain.usecase.cookbook.GetCookbooksByOwner
import com.ourcookbook.domain.usecase.cookbook.GetSharedCookbooks
import com.ourcookbook.domain.usecase.cookbook.GetSharingInfo
import com.ourcookbook.domain.usecase.cookbook.GenerateSharingLink
import com.ourcookbook.domain.usecase.cookbook.ImportCookbook
import com.ourcookbook.domain.usecase.cookbook.RemoveRecipeFromCookbook
import com.ourcookbook.domain.usecase.cookbook.SearchCookbooks
import com.ourcookbook.domain.usecase.cookbook.ShareCookbook
import com.ourcookbook.domain.usecase.cookbook.UpdateCookbook
import com.ourcookbook.domain.usecase.cookbook.ValidateCookbookChecksum
import com.ourcookbook.domain.usecase.cookbook.UpdateCookbookChecksum
import com.ourcookbook.domain.usecase.device.CreateDevice
import com.ourcookbook.domain.usecase.device.DeleteDevice
import com.ourcookbook.domain.usecase.device.GetActiveDevicesSince
import com.ourcookbook.domain.usecase.device.GetAllDevices
import com.ourcookbook.domain.usecase.device.GetAllDevicesOnce
import com.ourcookbook.domain.usecase.device.GetDeviceByDeviceId
import com.ourcookbook.domain.usecase.device.GetDeviceById
import com.ourcookbook.domain.usecase.device.GetDeviceCount
import com.ourcookbook.domain.usecase.device.UpdateDevice
import com.ourcookbook.domain.usecase.device.UpdateDeviceChecksum
import com.ourcookbook.domain.usecase.device.UpdateLastSeen
import com.ourcookbook.domain.usecase.device.ValidateDeviceChecksum
import com.ourcookbook.domain.usecase.devicepreferences.CreateDevicePreferences
import com.ourcookbook.domain.usecase.devicepreferences.DeleteDevicePreferences
import com.ourcookbook.domain.usecase.devicepreferences.DeleteDevicePreferencesByDevice
import com.ourcookbook.domain.usecase.devicepreferences.GetAllDevicePreferences
import com.ourcookbook.domain.usecase.devicepreferences.GetDevicePreferencesByDevice
import com.ourcookbook.domain.usecase.devicepreferences.GetDevicePreferencesById
import com.ourcookbook.domain.usecase.devicepreferences.GetDevicePreferencesCount
import com.ourcookbook.domain.usecase.devicepreferences.UpdateDevicePreferences
import com.ourcookbook.domain.usecase.devicepreferences.UpdateDevicePreferencesChecksum
import com.ourcookbook.domain.usecase.devicepreferences.ValidateDevicePreferencesChecksum
import com.ourcookbook.domain.usecase.ingredient.CreateIngredient
import com.ourcookbook.domain.usecase.ingredient.DeleteIngredient
import com.ourcookbook.domain.usecase.ingredient.DeleteIngredientsByRecipe
import com.ourcookbook.domain.usecase.ingredient.GetAllIngredients
import com.ourcookbook.domain.usecase.ingredient.GetIngredientById
import com.ourcookbook.domain.usecase.ingredient.GetIngredientCountByRecipe
import com.ourcookbook.domain.usecase.ingredient.GetIngredientsByRecipe
import com.ourcookbook.domain.usecase.ingredient.GetIngredientsByRecipes
import com.ourcookbook.domain.usecase.ingredient.SearchIngredients
import com.ourcookbook.domain.usecase.ingredient.UpdateIngredient
import com.ourcookbook.domain.usecase.ingredient.UpdateIngredientChecksum
import com.ourcookbook.domain.usecase.ingredient.ValidateIngredientChecksum
import com.ourcookbook.domain.usecase.recipe.CreateRecipe
import com.ourcookbook.domain.usecase.recipe.DeleteRecipe
import com.ourcookbook.domain.usecase.recipe.DetectConflicts
import com.ourcookbook.domain.usecase.recipe.FilterRecipesByCookingTime
import com.ourcookbook.domain.usecase.recipe.FilterRecipesByServingSize
import com.ourcookbook.domain.usecase.recipe.FilterRecipesByTags
import com.ourcookbook.domain.usecase.recipe.GetAllRecipes
import com.ourcookbook.domain.usecase.recipe.GetAllRecipesOnce
import com.ourcookbook.domain.usecase.recipe.GetFavorites
import com.ourcookbook.domain.usecase.recipe.GetRecipeByChecksum
import com.ourcookbook.domain.usecase.recipe.GetRecipeById
import com.ourcookbook.domain.usecase.recipe.GetRecipesByCategory
import com.ourcookbook.domain.usecase.recipe.GetRecipesByDevice
import com.ourcookbook.domain.usecase.recipe.GetRecipesByIds
import com.ourcookbook.domain.usecase.recipe.GetRecipeCount
import com.ourcookbook.domain.usecase.recipe.GetRecentRecipes
import com.ourcookbook.domain.usecase.recipe.GetTopRatedRecipes
import com.ourcookbook.domain.usecase.recipe.GetUpdatedSince
import com.ourcookbook.domain.usecase.recipe.MarkRecipeSynced
import com.ourcookbook.domain.usecase.recipe.ResolveConflict
import com.ourcookbook.domain.usecase.recipe.SearchRecipes
import com.ourcookbook.domain.usecase.recipe.SyncRecipe
import com.ourcookbook.domain.usecase.recipe.ToggleFavorite
import com.ourcookbook.domain.usecase.recipe.UpdateChecksum
import com.ourcookbook.domain.usecase.recipe.UpdateRecipe
import com.ourcookbook.domain.usecase.recipe.ValidateChecksum
import com.ourcookbook.domain.usecase.recipeimage.CreateRecipeImage
import com.ourcookbook.domain.usecase.recipeimage.DeleteRecipeImage
import com.ourcookbook.domain.usecase.recipeimage.DeleteRecipeImagesByRecipe
import com.ourcookbook.domain.usecase.recipeimage.GetAllRecipeImages
import com.ourcookbook.domain.usecase.recipeimage.GetRecipeImageById
import com.ourcookbook.domain.usecase.recipeimage.GetRecipeImageCountByRecipe
import com.ourcookbook.domain.usecase.recipeimage.GetRecipeImagesByRecipe
import com.ourcookbook.domain.usecase.recipeimage.GetRecipeImagesByRecipes
import com.ourcookbook.domain.usecase.recipeimage.UpdateRecipeImage
import com.ourcookbook.domain.usecase.recipeimage.UpdateRecipeImageChecksum
import com.ourcookbook.domain.usecase.recipeimage.ValidateRecipeImageChecksum
import com.ourcookbook.domain.usecase.sync.CreateConflict
import com.ourcookbook.domain.usecase.sync.DeleteConflict
import com.ourcookbook.domain.usecase.sync.DeleteConflictsByStatus
import com.ourcookbook.domain.usecase.sync.GetAllConflicts
import com.ourcookbook.domain.usecase.sync.GetConflictById
import com.ourcookbook.domain.usecase.sync.GetConflictsByRecipe
import com.ourcookbook.domain.usecase.sync.GetConflictsByStatus
import com.ourcookbook.domain.usecase.sync.GetConflictsSince
import com.ourcookbook.domain.usecase.sync.GetPendingConflictCount
import com.ourcookbook.domain.usecase.sync.ResolveSyncConflict
import com.ourcookbook.domain.usecase.sync.UpdateConflict
import com.ourcookbook.domain.usecase.sync.UpdateConflictChecksum
import com.ourcookbook.domain.usecase.sync.ValidateConflictChecksum
import com.ourcookbook.domain.usecase.sync.CreateSyncMetadata
import com.ourcookbook.domain.usecase.sync.DeleteSyncMetadata
import com.ourcookbook.domain.usecase.sync.DeleteSyncMetadataByDevice
import com.ourcookbook.domain.usecase.sync.GetAllSyncMetadata
import com.ourcookbook.domain.usecase.sync.GetSyncMetadataByDevice
import com.ourcookbook.domain.usecase.sync.GetSyncMetadataById
import com.ourcookbook.domain.usecase.sync.GetSyncStatus
import com.ourcookbook.domain.usecase.sync.UpdateLastSyncTimestamp
import com.ourcookbook.domain.usecase.sync.UpdateSyncInProgress
import com.ourcookbook.domain.usecase.sync.UpdateSyncMetadata
import com.ourcookbook.domain.usecase.sync.UpdateSyncMetadataChecksum
import com.ourcookbook.domain.usecase.sync.ValidateSyncMetadataChecksum
import com.ourcookbook.domain.usecase.search.FullTextSearchUseCases
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing all use case dependencies
 * This module provides all the use cases as singletons for dependency injection
 */
@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    // Recipe Use Cases
    @Provides
    @Singleton
    fun provideCreateRecipe(repository: RecipeRepository): CreateRecipe = CreateRecipe(repository)

    @Provides
    @Singleton
    fun provideUpdateRecipe(repository: RecipeRepository): UpdateRecipe = UpdateRecipe(repository)

    @Provides
    @Singleton
    fun provideDeleteRecipe(repository: RecipeRepository): DeleteRecipe = DeleteRecipe(repository)

    @Provides
    @Singleton
    fun provideGetRecipeById(repository: RecipeRepository): GetRecipeById = GetRecipeById(repository)

    @Provides
    @Singleton
    fun provideGetAllRecipes(repository: RecipeRepository): GetAllRecipes = GetAllRecipes(repository)

    @Provides
    @Singleton
    fun provideGetAllRecipesOnce(repository: RecipeRepository): GetAllRecipesOnce = GetAllRecipesOnce(repository)

    @Provides
    @Singleton
    fun provideGetRecipes(repository: RecipeRepository): com.ourcookbook.domain.usecase.recipe.GetRecipes = com.ourcookbook.domain.usecase.recipe.GetRecipes(repository)

    @Provides
    @Singleton
    fun provideGetRecipesByIds(repository: RecipeRepository): GetRecipesByIds = GetRecipesByIds(repository)

    @Provides
    @Singleton
    fun provideGetRecipeCount(repository: RecipeRepository): GetRecipeCount = GetRecipeCount(repository)

    @Provides
    @Singleton
    fun provideGetRecentRecipes(repository: RecipeRepository): GetRecentRecipes = GetRecentRecipes(repository)

    @Provides
    @Singleton
    fun provideGetTopRatedRecipes(repository: RecipeRepository): GetTopRatedRecipes = GetTopRatedRecipes(repository)

    @Provides
    @Singleton
    fun provideSearchRecipes(repository: RecipeRepository): SearchRecipes = SearchRecipes(repository)

    @Provides
    @Singleton
    fun provideGetRecipesByCategory(repository: RecipeRepository): GetRecipesByCategory = GetRecipesByCategory(repository)

    @Provides
    @Singleton
    fun provideGetFavorites(repository: RecipeRepository): GetFavorites = GetFavorites(repository)

    @Provides
    @Singleton
    fun provideGetRecipesByDevice(repository: RecipeRepository): GetRecipesByDevice = GetRecipesByDevice(repository)

    @Provides
    @Singleton
    fun provideToggleFavorite(
        repository: RecipeRepository,
        getRecipeById: GetRecipeById,
        updateRecipe: UpdateRecipe
    ): ToggleFavorite = ToggleFavorite(repository, getRecipeById, updateRecipe)

    @Provides
    @Singleton
    fun provideFilterRecipesByTags(repository: RecipeRepository): FilterRecipesByTags = FilterRecipesByTags(repository)

    @Provides
    @Singleton
    fun provideFilterRecipesByCookingTime(repository: RecipeRepository): FilterRecipesByCookingTime = FilterRecipesByCookingTime(repository)

    @Provides
    @Singleton
    fun provideFilterRecipesByServingSize(repository: RecipeRepository): FilterRecipesByServingSize = FilterRecipesByServingSize(repository)

    // Recipe Sync Use Cases
    @Provides
    @Singleton
    fun provideGetUpdatedSince(repository: RecipeRepository): GetUpdatedSince = GetUpdatedSince(repository)

    @Provides
    @Singleton
    fun provideGetRecipesNeedingSync(repository: RecipeRepository): com.ourcookbook.domain.usecase.recipe.GetRecipesNeedingSync = com.ourcookbook.domain.usecase.recipe.GetRecipesNeedingSync(repository)

    @Provides
    @Singleton
    fun provideMarkRecipeSynced(repository: RecipeRepository): MarkRecipeSynced = MarkRecipeSynced(repository)

    @Provides
    @Singleton
    fun provideValidateChecksum(repository: RecipeRepository): ValidateChecksum = ValidateChecksum(repository)

    @Provides
    @Singleton
    fun provideUpdateChecksum(repository: RecipeRepository): UpdateChecksum = UpdateChecksum(repository)

    @Provides
    @Singleton
    fun provideGetRecipeByChecksum(repository: RecipeRepository): GetRecipeByChecksum = GetRecipeByChecksum(repository)

    @Provides
    @Singleton
    fun provideDetectConflicts(repository: RecipeRepository): DetectConflicts = DetectConflicts(repository)

    @Provides
    @Singleton
    fun provideResolveConflict(repository: RecipeRepository): ResolveConflict = ResolveConflict(repository)

    @Provides
    @Singleton
    fun provideSyncRecipe(
        repository: RecipeRepository,
        getRecipesNeedingSync: com.ourcookbook.domain.usecase.recipe.GetRecipesNeedingSync,
        markRecipeSynced: MarkRecipeSynced,
        detectConflicts: DetectConflicts,
        resolveConflict: ResolveConflict
    ): SyncRecipe = SyncRecipe(repository, getRecipesNeedingSync, markRecipeSynced, detectConflicts, resolveConflict)

    // Ingredient Use Cases
    @Provides
    @Singleton
    fun provideCreateIngredient(repository: IngredientRepository): CreateIngredient = CreateIngredient(repository)

    @Provides
    @Singleton
    fun provideUpdateIngredient(repository: IngredientRepository): UpdateIngredient = UpdateIngredient(repository)

    @Provides
    @Singleton
    fun provideDeleteIngredient(repository: IngredientRepository): DeleteIngredient = DeleteIngredient(repository)

    @Provides
    @Singleton
    fun provideDeleteIngredientsByRecipe(repository: IngredientRepository): DeleteIngredientsByRecipe = DeleteIngredientsByRecipe(repository)

    @Provides
    @Singleton
    fun provideGetIngredientById(repository: IngredientRepository): GetIngredientById = GetIngredientById(repository)

    @Provides
    @Singleton
    fun provideGetIngredientsByRecipe(repository: IngredientRepository): GetIngredientsByRecipe = GetIngredientsByRecipe(repository)

    @Provides
    @Singleton
    fun provideGetIngredientsByRecipes(repository: IngredientRepository): GetIngredientsByRecipes = GetIngredientsByRecipes(repository)

    @Provides
    @Singleton
    fun provideSearchIngredients(repository: IngredientRepository): SearchIngredients = SearchIngredients(repository)

    @Provides
    @Singleton
    fun provideGetIngredientCountByRecipe(repository: IngredientRepository): GetIngredientCountByRecipe = GetIngredientCountByRecipe(repository)

    @Provides
    @Singleton
    fun provideGetAllIngredients(repository: IngredientRepository): GetAllIngredients = GetAllIngredients(repository)

    @Provides
    @Singleton
    fun provideValidateIngredientChecksum(repository: IngredientRepository): ValidateIngredientChecksum = ValidateIngredientChecksum(repository)

    @Provides
    @Singleton
    fun provideUpdateIngredientChecksum(repository: IngredientRepository): UpdateIngredientChecksum = UpdateIngredientChecksum(repository)

    // Cookbook Use Cases
    @Provides
    @Singleton
    fun provideCreateCookbook(repository: CookbookRepository): CreateCookbook = CreateCookbook(repository)

    @Provides
    @Singleton
    fun provideUpdateCookbook(repository: CookbookRepository): UpdateCookbook = UpdateCookbook(repository)

    @Provides
    @Singleton
    fun provideDeleteCookbook(repository: CookbookRepository): DeleteCookbook = DeleteCookbook(repository)

    @Provides
    @Singleton
    fun provideGetCookbookById(repository: CookbookRepository): GetCookbookById = GetCookbookById(repository)

    @Provides
    @Singleton
    fun provideGetCookbooksByOwner(repository: CookbookRepository): GetCookbooksByOwner = GetCookbooksByOwner(repository)

    @Provides
    @Singleton
    fun provideGetSharedCookbooks(repository: CookbookRepository): GetSharedCookbooks = GetSharedCookbooks(repository)

    @Provides
    @Singleton
    fun provideGetAllCookbooks(repository: CookbookRepository): GetAllCookbooks = GetAllCookbooks(repository)

    @Provides
    @Singleton
    fun provideGetCookbooks(repository: CookbookRepository): com.ourcookbook.domain.usecase.cookbook.GetCookbooks = com.ourcookbook.domain.usecase.cookbook.GetCookbooks(repository)

    @Provides
    @Singleton
    fun provideSearchCookbooks(repository: CookbookRepository): SearchCookbooks = SearchCookbooks(repository)

    @Provides
    @Singleton
    fun provideGetCookbookCount(repository: CookbookRepository): GetCookbookCount = GetCookbookCount(repository)

    @Provides
    @Singleton
    fun provideAddRecipeToCookbook(
        repository: CookbookRepository,
        getCookbookById: GetCookbookById,
        updateCookbook: UpdateCookbook
    ): AddRecipeToCookbook = AddRecipeToCookbook(repository, getCookbookById, updateCookbook)

    @Provides
    @Singleton
    fun provideRemoveRecipeFromCookbook(
        repository: CookbookRepository,
        getCookbookById: GetCookbookById,
        updateCookbook: UpdateCookbook
    ): RemoveRecipeFromCookbook = RemoveRecipeFromCookbook(repository, getCookbookById, updateCookbook)

    @Provides
    @Singleton
    fun provideValidateCookbookChecksum(repository: CookbookRepository): ValidateCookbookChecksum = ValidateCookbookChecksum(repository)

    @Provides
    @Singleton
    fun provideUpdateCookbookChecksum(repository: CookbookRepository): UpdateCookbookChecksum = UpdateCookbookChecksum(repository)

    // Export/Import Use Cases
    @Provides
    @Singleton
    fun provideExportCookbook(repository: CookbookRepository): com.ourcookbook.domain.usecase.cookbook.ExportCookbook = com.ourcookbook.domain.usecase.cookbook.ExportCookbook(repository)

    @Provides
    @Singleton
    fun provideImportCookbook(repository: CookbookRepository): com.ourcookbook.domain.usecase.cookbook.ImportCookbook = com.ourcookbook.domain.usecase.cookbook.ImportCookbook(repository)

    // Sharing Use Cases
    @Provides
    @Singleton
    fun provideShareCookbook(repository: CookbookRepository): com.ourcookbook.domain.usecase.cookbook.ShareCookbook = com.ourcookbook.domain.usecase.cookbook.ShareCookbook(repository)

    @Provides
    @Singleton
    fun provideGenerateSharingLink(repository: CookbookRepository): com.ourcookbook.domain.usecase.cookbook.GenerateSharingLink = com.ourcookbook.domain.usecase.cookbook.GenerateSharingLink(repository)

    @Provides
    @Singleton
    fun provideGetSharingInfo(repository: CookbookRepository): com.ourcookbook.domain.usecase.cookbook.GetSharingInfo = com.ourcookbook.domain.usecase.cookbook.GetSharingInfo(repository)

    // Device Use Cases
    @Provides
    @Singleton
    fun provideCreateDevice(repository: DeviceRepository): CreateDevice = CreateDevice(repository)

    @Provides
    @Singleton
    fun provideUpdateDevice(repository: DeviceRepository): UpdateDevice = UpdateDevice(repository)

    @Provides
    @Singleton
    fun provideDeleteDevice(repository: DeviceRepository): DeleteDevice = DeleteDevice(repository)

    @Provides
    @Singleton
    fun provideGetDeviceById(repository: DeviceRepository): GetDeviceById = GetDeviceById(repository)

    @Provides
    @Singleton
    fun provideGetDeviceByDeviceId(repository: DeviceRepository): GetDeviceByDeviceId = GetDeviceByDeviceId(repository)

    @Provides
    @Singleton
    fun provideGetAllDevices(repository: DeviceRepository): GetAllDevices = GetAllDevices(repository)

    @Provides
    @Singleton
    fun provideGetAllDevicesOnce(repository: DeviceRepository): GetAllDevicesOnce = GetAllDevicesOnce(repository)

    @Provides
    @Singleton
    fun provideGetActiveDevicesSince(repository: DeviceRepository): GetActiveDevicesSince = GetActiveDevicesSince(repository)

    @Provides
    @Singleton
    fun provideUpdateLastSeen(repository: DeviceRepository): UpdateLastSeen = UpdateLastSeen(repository)

    @Provides
    @Singleton
    fun provideGetDeviceCount(repository: DeviceRepository): GetDeviceCount = GetDeviceCount(repository)

    @Provides
    @Singleton
    fun provideValidateDeviceChecksum(repository: DeviceRepository): ValidateDeviceChecksum = ValidateDeviceChecksum(repository)

    @Provides
    @Singleton
    fun provideUpdateDeviceChecksum(repository: DeviceRepository): UpdateDeviceChecksum = UpdateDeviceChecksum(repository)

    // Sync Conflict Use Cases
    @Provides
    @Singleton
    fun provideCreateConflict(repository: SyncConflictRepository): CreateConflict = CreateConflict(repository)

    @Provides
    @Singleton
    fun provideUpdateConflict(repository: SyncConflictRepository): UpdateConflict = UpdateConflict(repository)

    @Provides
    @Singleton
    fun provideDeleteConflict(repository: SyncConflictRepository): DeleteConflict = DeleteConflict(repository)

    @Provides
    @Singleton
    fun provideDeleteConflictsByStatus(repository: SyncConflictRepository): DeleteConflictsByStatus = DeleteConflictsByStatus(repository)

    @Provides
    @Singleton
    fun provideGetConflictById(repository: SyncConflictRepository): GetConflictById = GetConflictById(repository)

    @Provides
    @Singleton
    fun provideGetConflictsByStatus(repository: SyncConflictRepository): GetConflictsByStatus = GetConflictsByStatus(repository)

    @Provides
    @Singleton
    fun provideGetConflictsByRecipe(repository: SyncConflictRepository): GetConflictsByRecipe = GetConflictsByRecipe(repository)

    @Provides
    @Singleton
    fun provideGetConflictsSince(repository: SyncConflictRepository): GetConflictsSince = GetConflictsSince(repository)

    @Provides
    @Singleton
    fun provideGetPendingConflictCount(repository: SyncConflictRepository): GetPendingConflictCount = GetPendingConflictCount(repository)

    @Provides
    @Singleton
    fun provideGetAllConflicts(repository: SyncConflictRepository): GetAllConflicts = GetAllConflicts(repository)

    @Provides
    @Singleton
    fun provideResolveSyncConflict(
        repository: SyncConflictRepository,
        getConflictById: GetConflictById,
        updateConflict: UpdateConflict
    ): ResolveSyncConflict = ResolveSyncConflict(repository, getConflictById, updateConflict)

    @Provides
    @Singleton
    fun provideValidateConflictChecksum(repository: SyncConflictRepository): ValidateConflictChecksum = ValidateConflictChecksum(repository)

    @Provides
    @Singleton
    fun provideUpdateConflictChecksum(repository: SyncConflictRepository): UpdateConflictChecksum = UpdateConflictChecksum(repository)

    // Sync Metadata Use Cases
    @Provides
    @Singleton
    fun provideCreateSyncMetadata(repository: SyncMetadataRepository): CreateSyncMetadata = CreateSyncMetadata(repository)

    @Provides
    @Singleton
    fun provideUpdateSyncMetadata(repository: SyncMetadataRepository): UpdateSyncMetadata = UpdateSyncMetadata(repository)

    @Provides
    @Singleton
    fun provideDeleteSyncMetadata(repository: SyncMetadataRepository): DeleteSyncMetadata = DeleteSyncMetadata(repository)

    @Provides
    @Singleton
    fun provideDeleteSyncMetadataByDevice(repository: SyncMetadataRepository): DeleteSyncMetadataByDevice = DeleteSyncMetadataByDevice(repository)

    @Provides
    @Singleton
    fun provideGetSyncMetadataById(repository: SyncMetadataRepository): GetSyncMetadataById = GetSyncMetadataById(repository)

    @Provides
    @Singleton
    fun provideGetSyncMetadataByDevice(repository: SyncMetadataRepository): GetSyncMetadataByDevice = GetSyncMetadataByDevice(repository)

    @Provides
    @Singleton
    fun provideGetAllSyncMetadata(repository: SyncMetadataRepository): GetAllSyncMetadata = GetAllSyncMetadata(repository)

    @Provides
    @Singleton
    fun provideUpdateLastSyncTimestamp(repository: SyncMetadataRepository): UpdateLastSyncTimestamp = UpdateLastSyncTimestamp(repository)

    @Provides
    @Singleton
    fun provideUpdateSyncInProgress(repository: SyncMetadataRepository): UpdateSyncInProgress = UpdateSyncInProgress(repository)

    @Provides
    @Singleton
    fun provideGetSyncStatus(
        repository: SyncMetadataRepository,
        getSyncMetadataByDevice: GetSyncMetadataByDevice
    ): GetSyncStatus = GetSyncStatus(repository, getSyncMetadataByDevice)

    @Provides
    @Singleton
    fun provideValidateSyncMetadataChecksum(repository: SyncMetadataRepository): ValidateSyncMetadataChecksum = ValidateSyncMetadataChecksum(repository)

    @Provides
    @Singleton
    fun provideUpdateSyncMetadataChecksum(repository: SyncMetadataRepository): UpdateSyncMetadataChecksum = UpdateSyncMetadataChecksum(repository)

    // Recipe Image Use Cases
    @Provides
    @Singleton
    fun provideCreateRecipeImage(repository: RecipeImageRepository): CreateRecipeImage = CreateRecipeImage(repository)

    @Provides
    @Singleton
    fun provideUpdateRecipeImage(repository: RecipeImageRepository): UpdateRecipeImage = UpdateRecipeImage(repository)

    @Provides
    @Singleton
    fun provideDeleteRecipeImage(repository: RecipeImageRepository): DeleteRecipeImage = DeleteRecipeImage(repository)

    @Provides
    @Singleton
    fun provideDeleteRecipeImagesByRecipe(repository: RecipeImageRepository): DeleteRecipeImagesByRecipe = DeleteRecipeImagesByRecipe(repository)

    @Provides
    @Singleton
    fun provideGetRecipeImageById(repository: RecipeImageRepository): GetRecipeImageById = GetRecipeImageById(repository)

    @Provides
    @Singleton
    fun provideGetRecipeImagesByRecipe(repository: RecipeImageRepository): GetRecipeImagesByRecipe = GetRecipeImagesByRecipe(repository)

    @Provides
    @Singleton
    fun provideGetRecipeImagesByRecipes(repository: RecipeImageRepository): GetRecipeImagesByRecipes = GetRecipeImagesByRecipes(repository)

    @Provides
    @Singleton
    fun provideGetAllRecipeImages(repository: RecipeImageRepository): GetAllRecipeImages = GetAllRecipeImages(repository)

    @Provides
    @Singleton
    fun provideGetRecipeImageCountByRecipe(repository: RecipeImageRepository): GetRecipeImageCountByRecipe = GetRecipeImageCountByRecipe(repository)

    @Provides
    @Singleton
    fun provideValidateRecipeImageChecksum(repository: RecipeImageRepository): ValidateRecipeImageChecksum = ValidateRecipeImageChecksum(repository)

    @Provides
    @Singleton
    fun provideUpdateRecipeImageChecksum(repository: RecipeImageRepository): UpdateRecipeImageChecksum = UpdateRecipeImageChecksum(repository)

    // Device Preferences Use Cases
    @Provides
    @Singleton
    fun provideDeleteDevicePreferences(repository: DevicePreferencesRepository): DeleteDevicePreferences = DeleteDevicePreferences(repository)

    @Provides
    @Singleton
    fun provideDeleteDevicePreferencesByDevice(repository: DevicePreferencesRepository): DeleteDevicePreferencesByDevice = DeleteDevicePreferencesByDevice(repository)

    @Provides
    @Singleton
    fun provideGetDevicePreferencesById(repository: DevicePreferencesRepository): GetDevicePreferencesById = GetDevicePreferencesById(repository)

    @Provides
    @Singleton
    fun provideGetAllDevicePreferences(repository: DevicePreferencesRepository): GetAllDevicePreferences = GetAllDevicePreferences(repository)

    @Provides
    @Singleton
    fun provideGetDevicePreferencesCount(repository: DevicePreferencesRepository): GetDevicePreferencesCount = GetDevicePreferencesCount(repository)

    @Provides
    @Singleton
    fun provideValidateDevicePreferencesChecksum(repository: DevicePreferencesRepository): ValidateDevicePreferencesChecksum = ValidateDevicePreferencesChecksum(repository)

    @Provides
    @Singleton
    fun provideUpdateDevicePreferencesChecksum(repository: DevicePreferencesRepository): UpdateDevicePreferencesChecksum = UpdateDevicePreferencesChecksum(repository)
    
    // Export/Import Use Cases
    @Provides
    @Singleton
    fun provideExportUseCases(
        repository: com.ourcookbook.domain.repository.ExportImportRepository
    ): com.ourcookbook.domain.usecase.exportimport.ExportUseCases {
        return com.ourcookbook.domain.usecase.exportimport.ExportUseCases(repository)
    }
    
    @Provides
    @Singleton
    fun provideImportUseCases(
        repository: com.ourcookbook.domain.repository.ExportImportRepository
    ): com.ourcookbook.domain.usecase.exportimport.ImportUseCases {
        return com.ourcookbook.domain.usecase.exportimport.ImportUseCases(repository)
    }

    // FullTextSearch UseCases
    @Provides
    @Singleton
    fun provideFullTextSearchUseCases(repository: FullTextSearchRepository): FullTextSearchUseCases {
        return FullTextSearchUseCases(repository)
    }
}