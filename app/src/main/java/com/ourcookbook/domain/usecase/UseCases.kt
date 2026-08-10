package com.ourcookbook.domain.usecase

/**
 * Central index of all use cases in the application
 * This file provides easy access to all use cases organized by domain
 */

// Recipe Use Cases
import com.ourcookbook.domain.usecase.recipe.CreateRecipe
import com.ourcookbook.domain.usecase.recipe.DeleteRecipe
import com.ourcookbook.domain.usecase.recipe.DetectConflicts
import com.ourcookbook.domain.usecase.recipe.FilterRecipesByCookingTime
import com.ourcookbook.domain.usecase.recipe.FilterRecipesByServingSize
import com.ourcookbook.domain.usecase.recipe.FilterRecipesByTags
import com.ourcookbook.domain.usecase.recipe.GetAllRecipes
import com.ourcookbook.domain.usecase.recipe.GetAllRecipesOnce
import com.ourcookbook.domain.usecase.recipe.GetRecipes
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

// Ingredient Use Cases
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

// Cookbook Use Cases
import com.ourcookbook.domain.usecase.cookbook.AddRecipeToCookbook
import com.ourcookbook.domain.usecase.cookbook.CreateCookbook
import com.ourcookbook.domain.usecase.cookbook.DeleteCookbook
import com.ourcookbook.domain.usecase.cookbook.GetAllCookbooks
import com.ourcookbook.domain.usecase.cookbook.GetCookbookById
import com.ourcookbook.domain.usecase.cookbook.GetCookbookCount
import com.ourcookbook.domain.usecase.cookbook.GetCookbooks
import com.ourcookbook.domain.usecase.cookbook.GetCookbooksByOwner
import com.ourcookbook.domain.usecase.cookbook.GetSharedCookbooks
import com.ourcookbook.domain.usecase.cookbook.RemoveRecipeFromCookbook
import com.ourcookbook.domain.usecase.cookbook.SearchCookbooks
import com.ourcookbook.domain.usecase.cookbook.UpdateCookbook
import com.ourcookbook.domain.usecase.cookbook.ValidateCookbookChecksum
import com.ourcookbook.domain.usecase.cookbook.UpdateCookbookChecksum

// Device Use Cases
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

// Sync Use Cases
import com.ourcookbook.domain.usecase.sync.CreateConflict
import com.ourcookbook.domain.usecase.sync.DeleteConflict
import com.ourcookbook.domain.usecase.sync.DeleteConflictsByStatus
import com.ourcookbook.domain.usecase.sync.GetAllConflicts
import com.ourcookbook.domain.usecase.sync.GetConflictById
import com.ourcookbook.domain.usecase.sync.GetConflictsByRecipe
import com.ourcookbook.domain.usecase.sync.GetConflictsByStatus
import com.ourcookbook.domain.usecase.sync.GetConflictsSince
import com.ourcookbook.domain.usecase.sync.GetPendingConflictCount
import com.ourcookbook.domain.usecase.sync.GetSyncStatus
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
import com.ourcookbook.domain.usecase.sync.UpdateLastSyncTimestamp
import com.ourcookbook.domain.usecase.sync.UpdateSyncInProgress
import com.ourcookbook.domain.usecase.sync.UpdateSyncMetadata
import com.ourcookbook.domain.usecase.sync.UpdateSyncMetadataChecksum
import com.ourcookbook.domain.usecase.sync.ValidateSyncMetadataChecksum

// Recipe Image Use Cases
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

// Device Preferences Use Cases
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

/**
 * Data class containing all recipe-related use cases
 */
data class RecipeUseCases(
    val createRecipe: CreateRecipe,
    val updateRecipe: UpdateRecipe,
    val deleteRecipe: DeleteRecipe,
    val getRecipeById: GetRecipeById,
    val getAllRecipes: GetAllRecipes,
    val getAllRecipesOnce: GetAllRecipesOnce,
    val getRecipesByIds: GetRecipesByIds,
    val getRecipeCount: GetRecipeCount,
    val getRecentRecipes: GetRecentRecipes,
    val getTopRatedRecipes: GetTopRatedRecipes,
    val searchRecipes: SearchRecipes,
    val getRecipesByCategory: GetRecipesByCategory,
    val getFavorites: GetFavorites,
    val getRecipesByDevice: GetRecipesByDevice,
    val toggleFavorite: ToggleFavorite,
    val filterRecipesByTags: FilterRecipesByTags,
    val filterRecipesByCookingTime: FilterRecipesByCookingTime,
    val filterRecipesByServingSize: FilterRecipesByServingSize
)

/**
 * Data class containing all recipe sync-related use cases
 */
data class RecipeSyncUseCases(
    val getUpdatedSince: GetUpdatedSince,
    val getRecipesNeedingSync: com.ourcookbook.domain.usecase.recipe.GetRecipesNeedingSync,
    val markRecipeSynced: MarkRecipeSynced,
    val validateChecksum: ValidateChecksum,
    val updateChecksum: UpdateChecksum,
    val getRecipeByChecksum: GetRecipeByChecksum,
    val detectConflicts: DetectConflicts,
    val resolveConflict: ResolveConflict,
    val syncRecipe: SyncRecipe
)

/**
 * Data class containing all ingredient-related use cases
 */
data class IngredientUseCases(
    val createIngredient: CreateIngredient,
    val updateIngredient: UpdateIngredient,
    val deleteIngredient: DeleteIngredient,
    val deleteIngredientsByRecipe: DeleteIngredientsByRecipe,
    val getIngredientById: GetIngredientById,
    val getIngredientsByRecipe: GetIngredientsByRecipe,
    val getIngredientsByRecipes: GetIngredientsByRecipes,
    val searchIngredients: SearchIngredients,
    val getIngredientCountByRecipe: GetIngredientCountByRecipe,
    val getAllIngredients: GetAllIngredients,
    val validateIngredientChecksum: ValidateIngredientChecksum,
    val updateIngredientChecksum: UpdateIngredientChecksum
)

/**
 * Data class containing all cookbook-related use cases
 */
data class CookbookUseCases(
    val createCookbook: CreateCookbook,
    val updateCookbook: UpdateCookbook,
    val deleteCookbook: DeleteCookbook,
    val getCookbookById: GetCookbookById,
    val getCookbooksByOwner: GetCookbooksByOwner,
    val getSharedCookbooks: GetSharedCookbooks,
    val getAllCookbooks: GetAllCookbooks,
    val searchCookbooks: SearchCookbooks,
    val getCookbookCount: GetCookbookCount,
    val addRecipeToCookbook: AddRecipeToCookbook,
    val removeRecipeFromCookbook: RemoveRecipeFromCookbook,
    val validateCookbookChecksum: ValidateCookbookChecksum,
    val updateCookbookChecksum: UpdateCookbookChecksum
)

/**
 * Data class containing all device-related use cases
 */
data class DeviceUseCases(
    val createDevice: CreateDevice,
    val updateDevice: UpdateDevice,
    val deleteDevice: DeleteDevice,
    val getDeviceById: GetDeviceById,
    val getDeviceByDeviceId: GetDeviceByDeviceId,
    val getAllDevices: GetAllDevices,
    val getAllDevicesOnce: GetAllDevicesOnce,
    val getActiveDevicesSince: GetActiveDevicesSince,
    val updateLastSeen: UpdateLastSeen,
    val getDeviceCount: GetDeviceCount,
    val validateDeviceChecksum: ValidateDeviceChecksum,
    val updateDeviceChecksum: UpdateDeviceChecksum
)

/**
 * Data class containing all sync-related use cases
 */
data class SyncUseCases(
    val createConflict: CreateConflict,
    val updateConflict: UpdateConflict,
    val deleteConflict: DeleteConflict,
    val deleteConflictsByStatus: DeleteConflictsByStatus,
    val getConflictById: GetConflictById,
    val getConflictsByStatus: GetConflictsByStatus,
    val getConflictsByRecipe: GetConflictsByRecipe,
    val getConflictsSince: GetConflictsSince,
    val getPendingConflictCount: GetPendingConflictCount,
    val getAllConflicts: GetAllConflicts,
    val resolveSyncConflict: ResolveSyncConflict,
    val validateConflictChecksum: ValidateConflictChecksum,
    val updateConflictChecksum: UpdateConflictChecksum
)

/**
 * Data class containing all sync metadata-related use cases
 */
data class SyncMetadataUseCases(
    val createSyncMetadata: CreateSyncMetadata,
    val updateSyncMetadata: UpdateSyncMetadata,
    val deleteSyncMetadata: DeleteSyncMetadata,
    val deleteSyncMetadataByDevice: DeleteSyncMetadataByDevice,
    val getSyncMetadataById: GetSyncMetadataById,
    val getSyncMetadataByDevice: GetSyncMetadataByDevice,
    val getAllSyncMetadata: GetAllSyncMetadata,
    val updateLastSyncTimestamp: UpdateLastSyncTimestamp,
    val updateSyncInProgress: UpdateSyncInProgress,
    val getSyncStatus: GetSyncStatus,
    val validateSyncMetadataChecksum: ValidateSyncMetadataChecksum,
    val updateSyncMetadataChecksum: UpdateSyncMetadataChecksum
)

/**
 * Data class containing all recipe image-related use cases
 */
data class RecipeImageUseCases(
    val createRecipeImage: CreateRecipeImage,
    val updateRecipeImage: UpdateRecipeImage,
    val deleteRecipeImage: DeleteRecipeImage,
    val deleteRecipeImagesByRecipe: DeleteRecipeImagesByRecipe,
    val getRecipeImageById: GetRecipeImageById,
    val getRecipeImagesByRecipe: GetRecipeImagesByRecipe,
    val getRecipeImagesByRecipes: GetRecipeImagesByRecipes,
    val getAllRecipeImages: GetAllRecipeImages,
    val getRecipeImageCountByRecipe: GetRecipeImageCountByRecipe,
    val validateRecipeImageChecksum: ValidateRecipeImageChecksum,
    val updateRecipeImageChecksum: UpdateRecipeImageChecksum
)

/**
 * Data class containing all device preferences-related use cases
 */
data class DevicePreferencesUseCases(
    val createDevicePreferences: CreateDevicePreferences,
    val updateDevicePreferences: UpdateDevicePreferences,
    val deleteDevicePreferences: DeleteDevicePreferences,
    val deleteDevicePreferencesByDevice: DeleteDevicePreferencesByDevice,
    val getDevicePreferencesById: GetDevicePreferencesById,
    val getDevicePreferencesByDevice: GetDevicePreferencesByDevice,
    val getAllDevicePreferences: GetAllDevicePreferences,
    val getDevicePreferencesCount: GetDevicePreferencesCount,
    val validateDevicePreferencesChecksum: ValidateDevicePreferencesChecksum,
    val updateDevicePreferencesChecksum: UpdateDevicePreferencesChecksum
)

/**
 * Main data class containing all use cases in the application
 * This provides a centralized way to access all use cases
 */
data class AllUseCases(
    val recipeUseCases: RecipeUseCases,
    val recipeSyncUseCases: RecipeSyncUseCases,
    val ingredientUseCases: IngredientUseCases,
    val cookbookUseCases: CookbookUseCases,
    val deviceUseCases: DeviceUseCases,
    val syncUseCases: SyncUseCases,
    val syncMetadataUseCases: SyncMetadataUseCases,
    val recipeImageUseCases: RecipeImageUseCases,
    val devicePreferencesUseCases: DevicePreferencesUseCases
)
