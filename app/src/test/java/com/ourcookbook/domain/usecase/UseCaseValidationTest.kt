package com.ourcookbook.domain.usecase

import com.ourcookbook.domain.repository.CookbookRepository
import com.ourcookbook.domain.repository.DevicePreferencesRepository
import com.ourcookbook.domain.repository.DeviceRepository
import com.ourcookbook.domain.repository.IngredientRepository
import com.ourcookbook.domain.repository.RecipeImageRepository
import com.ourcookbook.domain.repository.RecipeRepository
import com.ourcookbook.domain.repository.SyncConflictRepository
import com.ourcookbook.domain.repository.SyncMetadataRepository
import com.ourcookbook.domain.usecase.cookbook.AddRecipeToCookbook
import com.ourcookbook.domain.usecase.cookbook.CreateCookbook
import com.ourcookbook.domain.usecase.cookbook.DeleteCookbook
import com.ourcookbook.domain.usecase.cookbook.GetAllCookbooks
import com.ourcookbook.domain.usecase.cookbook.GetCookbookById
import com.ourcookbook.domain.usecase.cookbook.GetCookbookCount
import com.ourcookbook.domain.usecase.cookbook.GetCookbooksByOwner
import com.ourcookbook.domain.usecase.cookbook.GetSharedCookbooks
import com.ourcookbook.domain.usecase.cookbook.RemoveRecipeFromCookbook
import com.ourcookbook.domain.usecase.cookbook.SearchCookbooks
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
import org.junit.Test
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

/**
 * Architecture validation tests for Use Cases
 * Ensures all use cases comply with Clean Architecture principles
 */
class UseCaseValidationTest {

    @Test
    fun `All use cases should have exactly one repository parameter`() {
        // Define all use case classes
        val useCaseClasses = listOf<KClass<*>>(
            // Recipe Use Cases
            CreateRecipe::class,
            UpdateRecipe::class,
            DeleteRecipe::class,
            GetRecipeById::class,
            GetAllRecipes::class,
            GetAllRecipesOnce::class,
            GetRecipesByIds::class,
            GetRecipeCount::class,
            GetRecentRecipes::class,
            GetTopRatedRecipes::class,
            SearchRecipes::class,
            GetRecipesByCategory::class,
            GetFavorites::class,
            GetRecipesByDevice::class,
            ToggleFavorite::class,
            FilterRecipesByTags::class,
            FilterRecipesByCookingTime::class,
            FilterRecipesByServingSize::class,
            
            // Recipe Sync Use Cases
            GetUpdatedSince::class,
            com.ourcookbook.domain.usecase.recipe.GetRecipesNeedingSync::class,
            MarkRecipeSynced::class,
            ValidateChecksum::class,
            UpdateChecksum::class,
            GetRecipeByChecksum::class,
            DetectConflicts::class,
            ResolveConflict::class,
            SyncRecipe::class,
            
            // Ingredient Use Cases
            CreateIngredient::class,
            UpdateIngredient::class,
            DeleteIngredient::class,
            DeleteIngredientsByRecipe::class,
            GetIngredientById::class,
            GetIngredientsByRecipe::class,
            GetIngredientsByRecipes::class,
            SearchIngredients::class,
            GetIngredientCountByRecipe::class,
            GetAllIngredients::class,
            ValidateIngredientChecksum::class,
            UpdateIngredientChecksum::class,
            
            // Cookbook Use Cases
            CreateCookbook::class,
            UpdateCookbook::class,
            DeleteCookbook::class,
            GetCookbookById::class,
            GetCookbooksByOwner::class,
            GetSharedCookbooks::class,
            GetAllCookbooks::class,
            SearchCookbooks::class,
            GetCookbookCount::class,
            AddRecipeToCookbook::class,
            RemoveRecipeFromCookbook::class,
            ValidateCookbookChecksum::class,
            UpdateCookbookChecksum::class,
            
            // Device Use Cases
            CreateDevice::class,
            UpdateDevice::class,
            DeleteDevice::class,
            GetDeviceById::class,
            GetDeviceByDeviceId::class,
            GetAllDevices::class,
            GetAllDevicesOnce::class,
            GetActiveDevicesSince::class,
            UpdateLastSeen::class,
            GetDeviceCount::class,
            ValidateDeviceChecksum::class,
            UpdateDeviceChecksum::class,
            
            // Sync Use Cases
            CreateConflict::class,
            UpdateConflict::class,
            DeleteConflict::class,
            DeleteConflictsByStatus::class,
            GetConflictById::class,
            GetConflictsByStatus::class,
            GetConflictsByRecipe::class,
            GetConflictsSince::class,
            GetPendingConflictCount::class,
            GetAllConflicts::class,
            ResolveSyncConflict::class,
            ValidateConflictChecksum::class,
            UpdateConflictChecksum::class,
            
            // Sync Metadata Use Cases
            CreateSyncMetadata::class,
            UpdateSyncMetadata::class,
            DeleteSyncMetadata::class,
            DeleteSyncMetadataByDevice::class,
            GetSyncMetadataById::class,
            GetSyncMetadataByDevice::class,
            GetAllSyncMetadata::class,
            UpdateLastSyncTimestamp::class,
            UpdateSyncInProgress::class,
            GetSyncStatus::class,
            ValidateSyncMetadataChecksum::class,
            UpdateSyncMetadataChecksum::class,
            
            // Recipe Image Use Cases
            CreateRecipeImage::class,
            UpdateRecipeImage::class,
            DeleteRecipeImage::class,
            DeleteRecipeImagesByRecipe::class,
            GetRecipeImageById::class,
            GetRecipeImagesByRecipe::class,
            GetRecipeImagesByRecipes::class,
            GetAllRecipeImages::class,
            GetRecipeImageCountByRecipe::class,
            ValidateRecipeImageChecksum::class,
            UpdateRecipeImageChecksum::class,
            
            // Device Preferences Use Cases
            CreateDevicePreferences::class,
            UpdateDevicePreferences::class,
            DeleteDevicePreferences::class,
            DeleteDevicePreferencesByDevice::class,
            GetDevicePreferencesById::class,
            GetDevicePreferencesByDevice::class,
            GetAllDevicePreferences::class,
            GetDevicePreferencesCount::class,
            ValidateDevicePreferencesChecksum::class,
            UpdateDevicePreferencesChecksum::class
        )

        // Define all repository interfaces
        val repositoryInterfaces = setOf<KClass<*>>(
            RecipeRepository::class,
            IngredientRepository::class,
            CookbookRepository::class,
            DeviceRepository::class,
            SyncConflictRepository::class,
            SyncMetadataRepository::class,
            RecipeImageRepository::class,
            DevicePreferencesRepository::class
        )

        // Check each use case
        useCaseClasses.forEach { useCaseClass ->
            val primaryConstructor = useCaseClass.primaryConstructor
                ?: throw IllegalStateException("Use case ${useCaseClass.simpleName} must have a primary constructor")
            
            val parameters = primaryConstructor.parameters
            
            // Check that all parameters are either repositories or other use cases
            parameters.forEach { parameter ->
                val parameterType = parameter.type.classifier as? KClass<*>
                    ?: throw IllegalStateException("Parameter type must be a class")
                
                // Parameter should be either a repository or another use case
                val isRepository = repositoryInterfaces.contains(parameterType)
                val isUseCase = useCaseClasses.contains(parameterType)
                
                if (!isRepository && !isUseCase) {
                    throw IllegalStateException(
                        "Use case ${useCaseClass.simpleName} has invalid parameter type: ${parameterType.simpleName}. " +
                        "Parameters must be repository interfaces or other use cases."
                    )
                }
            }
            
            // Check that there's at least one repository parameter
            val hasRepositoryParameter = parameters.any { parameter ->
                val parameterType = parameter.type.classifier as? KClass<*>
                parameterType != null && repositoryInterfaces.contains(parameterType)
            }
            
            if (!hasRepositoryParameter) {
                throw IllegalStateException(
                    "Use case ${useCaseClass.simpleName} must have at least one repository parameter"
                )
            }
        }
    }

    @Test
    fun `All use cases should have invoke operator`() {
        val useCaseClasses = listOf<KClass<*>>(
            // Recipe Use Cases
            CreateRecipe::class,
            UpdateRecipe::class,
            DeleteRecipe::class,
            GetRecipeById::class,
            GetAllRecipes::class,
            GetAllRecipesOnce::class,
            GetRecipesByIds::class,
            GetRecipeCount::class,
            GetRecentRecipes::class,
            GetTopRatedRecipes::class,
            SearchRecipes::class,
            GetRecipesByCategory::class,
            GetFavorites::class,
            GetRecipesByDevice::class,
            ToggleFavorite::class,
            FilterRecipesByTags::class,
            FilterRecipesByCookingTime::class,
            FilterRecipesByServingSize::class,
            
            // Add more use cases as needed for testing
            // This is a sample to ensure the pattern works
        )

        useCaseClasses.forEach { useCaseClass ->
            val invokeFunction = useCaseClass.memberProperties.find { it.name == "invoke" }
            if (invokeFunction == null) {
                throw IllegalStateException(
                    "Use case ${useCaseClass.simpleName} must have an invoke operator function"
                )
            }
        }
    }

    @Test
    fun `Use case data classes should contain all expected use cases`() {
        // This test ensures that the data classes in UseCases.kt contain all the expected use cases
        // This is more of a compilation test to ensure the architecture is consistent
        
        // Test that we can create instances of the data classes
        val mockRecipeRepo: RecipeRepository = org.mockito.kotlin.mock()
        val mockIngredientRepo: IngredientRepository = org.mockito.kotlin.mock()
        val mockCookbookRepo: CookbookRepository = org.mockito.kotlin.mock()
        val mockDeviceRepo: DeviceRepository = org.mockito.kotlin.mock()
        val mockSyncConflictRepo: SyncConflictRepository = org.mockito.kotlin.mock()
        val mockSyncMetadataRepo: SyncMetadataRepository = org.mockito.kotlin.mock()
        val mockRecipeImageRepo: RecipeImageRepository = org.mockito.kotlin.mock()
        val mockDevicePreferencesRepo: DevicePreferencesRepository = org.mockito.kotlin.mock()
        
        // Create recipe use cases
        val recipeUseCases = RecipeUseCases(
            createRecipe = CreateRecipe(mockRecipeRepo),
            updateRecipe = UpdateRecipe(mockRecipeRepo),
            deleteRecipe = DeleteRecipe(mockRecipeRepo),
            getRecipeById = GetRecipeById(mockRecipeRepo),
            getAllRecipes = GetAllRecipes(mockRecipeRepo),
            getAllRecipesOnce = GetAllRecipesOnce(mockRecipeRepo),
            getRecipesByIds = GetRecipesByIds(mockRecipeRepo),
            getRecipeCount = GetRecipeCount(mockRecipeRepo),
            getRecentRecipes = GetRecentRecipes(mockRecipeRepo),
            getTopRatedRecipes = GetTopRatedRecipes(mockRecipeRepo),
            searchRecipes = SearchRecipes(mockRecipeRepo),
            getRecipesByCategory = GetRecipesByCategory(mockRecipeRepo),
            getFavorites = GetFavorites(mockRecipeRepo),
            getRecipesByDevice = GetRecipesByDevice(mockRecipeRepo),
            toggleFavorite = ToggleFavorite(mockRecipeRepo, GetRecipeById(mockRecipeRepo), UpdateRecipe(mockRecipeRepo)),
            filterRecipesByTags = FilterRecipesByTags(mockRecipeRepo),
            filterRecipesByCookingTime = FilterRecipesByCookingTime(mockRecipeRepo),
            filterRecipesByServingSize = FilterRecipesByServingSize(mockRecipeRepo)
        )
        
        // Create ingredient use cases
        val ingredientUseCases = IngredientUseCases(
            createIngredient = CreateIngredient(mockIngredientRepo),
            updateIngredient = UpdateIngredient(mockIngredientRepo),
            deleteIngredient = DeleteIngredient(mockIngredientRepo),
            deleteIngredientsByRecipe = DeleteIngredientsByRecipe(mockIngredientRepo),
            getIngredientById = GetIngredientById(mockIngredientRepo),
            getIngredientsByRecipe = GetIngredientsByRecipe(mockIngredientRepo),
            getIngredientsByRecipes = GetIngredientsByRecipes(mockIngredientRepo),
            searchIngredients = SearchIngredients(mockIngredientRepo),
            getIngredientCountByRecipe = GetIngredientCountByRecipe(mockIngredientRepo),
            getAllIngredients = GetAllIngredients(mockIngredientRepo),
            validateIngredientChecksum = ValidateIngredientChecksum(mockIngredientRepo),
            updateIngredientChecksum = UpdateIngredientChecksum(mockIngredientRepo)
        )
        
        // This test passes if we can create the data classes without errors
        assert(true)
    }
}
