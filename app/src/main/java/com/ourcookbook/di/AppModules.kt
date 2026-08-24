package com.ourcookbook.di

import android.content.Context
import androidx.room.Room
import com.ourcookbook.data.db.AppDatabase
import com.ourcookbook.data.db.dao.*
import com.ourcookbook.data.repository.*
import com.ourcookbook.data.service.*
import com.ourcookbook.domain.service.ChecksumService
import com.ourcookbook.domain.service.ConflictResolver
import com.ourcookbook.domain.service.SyncService
import com.ourcookbook.domain.usecase.*
import com.ourcookbook.ui.viewmodel.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import net.sqlcipher.database.SupportFactory
import javax.inject.Named
import javax.inject.Singleton

/**
 * Database module for providing database instances and DAOs
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    private const val DATABASE_PASSPHRASE = "OurCookbookSecurePassphrase123!"
    
    @Provides
    @Singleton
    @Named("database_passphrase")
    fun provideDatabasePassphrase(): String = DATABASE_PASSPHRASE
    
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        @Named("database_passphrase") passphrase: String
    ): AppDatabase {
        val factory = SupportFactory(passphrase.toByteArray())
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "cookbook-db"
        )
            .openHelperFactory(factory)
            .build()
    }
    
    @Provides
    @Singleton
    fun provideRecipeDao(database: AppDatabase): RecipeDao = database.recipeDao()
    
    @Provides
    @Singleton
    fun provideIngredientDao(database: AppDatabase): IngredientDao = database.ingredientDao()
    
    @Provides
    @Singleton
    fun provideRecipeImageDao(database: AppDatabase): RecipeImageDao = database.recipeImageDao()
    
    @Provides
    @Singleton
    fun provideDeviceDao(database: AppDatabase): DeviceDao = database.deviceDao()
    
    @Provides
    @Singleton
    fun provideDevicePreferencesDao(database: AppDatabase): DevicePreferencesDao = database.devicePreferencesDao()
    
    @Provides
    @Singleton
    fun provideCookbookDao(database: AppDatabase): CookbookDao = database.cookbookDao()
    
    @Provides
    @Singleton
    fun provideSharingLinkDao(database: AppDatabase): SharingLinkDao = database.sharingLinkDao()
    
    @Provides
    @Singleton
    fun provideSyncConflictDao(database: AppDatabase): SyncConflictDao = database.syncConflictDao()
    
    @Provides
    @Singleton
    fun provideSyncLogDao(database: AppDatabase): SyncLogDao = database.syncLogDao()
    
    @Provides
    @Singleton
    fun providePendingSyncDao(database: AppDatabase): PendingSyncDao = database.pendingSyncDao()
    
    @Provides
    @Singleton
    fun provideSyncMetadataDao(database: AppDatabase): SyncMetadataDao = database.syncMetadataDao()
    
    @Provides
    @Singleton
    fun provideDriveFileInfoDao(database: AppDatabase): DriveFileInfoDao = database.driveFileInfoDao()
    
    @Provides
    @Singleton
    fun provideTombstoneDao(database: AppDatabase): TombstoneDao = database.tombstoneDao()
}

/**
 * Repository module for providing repository implementations
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    
    @Provides
    @Singleton
    fun provideIngredientRepository(
        ingredientDao: IngredientDao
    ): IngredientRepository = IngredientRepositoryImpl(ingredientDao)
    
    @Provides
    @Singleton
    fun provideRecipeImageRepository(
        recipeImageDao: RecipeImageDao
    ): RecipeImageRepository = RecipeImageRepositoryImpl(recipeImageDao)
    
    @Provides
    @Singleton
    fun provideDeviceRepository(
        deviceDao: DeviceDao
    ): DeviceRepository = DeviceRepositoryImpl(deviceDao)
    
    @Provides
    @Singleton
    fun provideDevicePreferencesRepository(
        devicePreferencesDao: DevicePreferencesDao
    ): DevicePreferencesRepository = DevicePreferencesRepositoryImpl(devicePreferencesDao)
    
    @Provides
    @Singleton
    fun provideCookbookRepository(
        cookbookDao: CookbookDao
    ): CookbookRepository = CookbookRepositoryImpl(cookbookDao)
    
    @Provides
    @Singleton
    fun provideSharingLinkRepository(
        sharingLinkDao: SharingLinkDao
    ): SharingLinkRepository = SharingLinkRepositoryImpl(sharingLinkDao)
    
    @Provides
    @Singleton
    fun provideSyncConflictRepository(
        syncConflictDao: SyncConflictDao
    ): SyncConflictRepository = SyncConflictRepositoryImpl(syncConflictDao)
    
    @Provides
    @Singleton
    fun provideSyncLogRepository(
        syncLogDao: SyncLogDao
    ): SyncLogRepository = SyncLogRepositoryImpl(syncLogDao)
    
    @Provides
    @Singleton
    fun providePendingSyncRepository(
        pendingSyncDao: PendingSyncDao
    ): PendingSyncRepository = PendingSyncRepositoryImpl(pendingSyncDao)
    
    @Provides
    @Singleton
    fun provideSyncMetadataRepository(
        syncMetadataDao: SyncMetadataDao
    ): SyncMetadataRepository = SyncMetadataRepositoryImpl(syncMetadataDao)
    
    @Provides
    @Singleton
    fun provideDriveFileInfoRepository(
        driveFileInfoDao: DriveFileInfoDao
    ): DriveFileInfoRepository = DriveFileInfoRepositoryImpl(driveFileInfoDao)
    
    @Provides
    @Singleton
    fun provideTombstoneRepository(
        tombstoneDao: TombstoneDao
    ): TombstoneRepository = TombstoneRepositoryImpl(tombstoneDao)
}

/**
 * Service module for providing service implementations
 */
@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {
    
    @Provides
    @Singleton
    fun provideChecksumService(): ChecksumService = ChecksumServiceImpl()
    
    @Provides
    @Singleton
    fun provideSyncService(
        checksumService: ChecksumService
    ): SyncService = SyncServiceImpl(checksumService)
    
    @Provides
    @Singleton
    fun provideConflictResolver(
        checksumService: ChecksumService
    ): ConflictResolver = ConflictResolverImpl(checksumService)
    
    @Provides
    @Singleton
    fun provideSyncStatusService(
        syncLogRepository: SyncLogRepository,
        syncMetadataRepository: SyncMetadataRepository
    ): SyncStatusService = SyncStatusService(syncLogRepository, syncMetadataRepository)
    
    @Provides
    @Singleton
    fun provideDeviceInfoService(
        @ApplicationContext context: Context
    ): DeviceInfoService = DeviceInfoService(context)
    
    @Provides
    @Singleton
    fun provideSecureCredentialManager(
        @ApplicationContext context: Context
    ): SecureCredentialManager = SecureCredentialManager(context)
}

/**
 * Dispatcher module for providing coroutine dispatchers
 */
@Module
@InstallIn(SingletonComponent::class)
object DispatcherModule {
    
    @Provides
    @Singleton
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
    
    @Provides
    @Singleton
    fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main
    
    @Provides
    @Singleton
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default
    
    @Provides
    @Singleton
    fun provideUnconfinedDispatcher(): CoroutineDispatcher = Dispatchers.Unconfined
}

/**
 * UseCase module for providing use case implementations
 */
@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {
    
    // Recipe use cases
    @Provides
    @Singleton
    fun provideGetRecipes(repository: RecipeRepository): GetRecipes = GetRecipes(repository)
    
    @Provides
    @Singleton
    fun provideGetRecipeById(repository: RecipeRepository): GetRecipeById = GetRecipeById(repository)
    
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
    fun provideSearchRecipes(repository: RecipeRepository): SearchRecipes = SearchRecipes(repository)
    
    @Provides
    @Singleton
    fun provideToggleFavorite(repository: RecipeRepository): ToggleFavorite = ToggleFavorite(repository)
    
    // Ingredient use cases
    @Provides
    @Singleton
    fun provideGetIngredientsByRecipe(repository: IngredientRepository): GetIngredientsByRecipe = GetIngredientsByRecipe(repository)
    
    @Provides
    @Singleton
    fun provideAddIngredient(repository: IngredientRepository): AddIngredient = AddIngredient(repository)
    
    @Provides
    @Singleton
    fun provideUpdateIngredient(repository: IngredientRepository): UpdateIngredient = UpdateIngredient(repository)
    
    @Provides
    @Singleton
    fun provideDeleteIngredient(repository: IngredientRepository): DeleteIngredient = DeleteIngredient(repository)
    
    // Cookbook use cases
    @Provides
    @Singleton
    fun provideGetCookbooks(repository: CookbookRepository): GetCookbooks = GetCookbooks(repository)
    
    @Provides
    @Singleton
    fun provideCreateCookbook(repository: CookbookRepository): CreateCookbook = CreateCookbook(repository)
    
    @Provides
    @Singleton
    fun provideUpdateCookbook(repository: CookbookRepository): UpdateCookbook = UpdateCookbook(repository)
    
    // Sync use cases
    @Provides
    @Singleton
    fun provideSyncRecipes(
        syncService: SyncService,
        syncStatusService: SyncStatusService
    ): SyncRecipes = SyncRecipes(syncService, syncStatusService)
    
    @Provides
    @Singleton
    fun provideResolveConflict(
        conflictResolver: ConflictResolver,
        syncConflictRepository: SyncConflictRepository
    ): ResolveConflict = ResolveConflict(conflictResolver, syncConflictRepository)
    
    // Device use cases
    @Provides
    @Singleton
    fun provideRegisterDevice(
        deviceRepository: DeviceRepository,
        deviceInfoService: DeviceInfoService
    ): RegisterDevice = RegisterDevice(deviceRepository, deviceInfoService)
    
    @Provides
    @Singleton
    fun provideGetDevicePreferences(
        repository: DevicePreferencesRepository
    ): GetDevicePreferences = GetDevicePreferences(repository)
    
    @Provides
    @Singleton
    fun provideUpdateDevicePreferences(
        repository: DevicePreferencesRepository
    ): UpdateDevicePreferences = UpdateDevicePreferences(repository)
}

/**
 * ViewModel module for providing ViewModel factories
 */
@Module
@InstallIn(SingletonComponent::class)
object ViewModelModule {
    
    // Home ViewModel
    @Provides
    @Singleton
    fun provideHomeViewModel(
        getRecipes: GetRecipes,
        getCookbooks: GetCookbooks,
        searchRecipes: SearchRecipes,
        syncStatusService: SyncStatusService
    ): HomeViewModel = HomeViewModel(getRecipes, getCookbooks, searchRecipes, syncStatusService)
    
    // Recipe List ViewModel
    @Provides
    @Singleton
    fun provideRecipeListViewModel(
        getRecipes: GetRecipes,
        deleteRecipe: DeleteRecipe,
        searchRecipes: SearchRecipes
    ): RecipeListViewModel = RecipeListViewModel(getRecipes, deleteRecipe, searchRecipes)
    
    // Recipe Detail ViewModel
    @Provides
    @Singleton
    fun provideRecipeDetailViewModel(
        getRecipeById: GetRecipeById,
        toggleFavorite: ToggleFavorite,
        deleteRecipe: DeleteRecipe
    ): RecipeDetailViewModel = RecipeDetailViewModel(getRecipeById, toggleFavorite, deleteRecipe)
    
    // Recipe Edit ViewModel
    @Provides
    @Singleton
    fun provideRecipeEditViewModel(
        getRecipeById: GetRecipeById,
        createRecipe: CreateRecipe,
        updateRecipe: UpdateRecipe,
        getIngredientsByRecipe: GetIngredientsByRecipe,
        addIngredient: AddIngredient,
        updateIngredient: UpdateIngredient,
        deleteIngredient: DeleteIngredient
    ): RecipeEditViewModel = RecipeEditViewModel(
        getRecipeById, createRecipe, updateRecipe, 
        getIngredientsByRecipe, addIngredient, updateIngredient, deleteIngredient
    )
    
    // Recipe Create ViewModel
    @Provides
    @Singleton
    fun provideRecipeCreateViewModel(
        createRecipe: CreateRecipe,
        addIngredient: AddIngredient
    ): RecipeCreateViewModel = RecipeCreateViewModel(createRecipe, addIngredient)
    
    // Search ViewModel
    @Provides
    @Singleton
    fun provideSearchViewModel(
        searchRecipes: SearchRecipes
    ): SearchViewModel = SearchViewModel(searchRecipes)
    
    // Cookbook Management ViewModel
    @Provides
    @Singleton
    fun provideCookbookManagementViewModel(
        getCookbooks: GetCookbooks,
        createCookbook: CreateCookbook,
        updateCookbook: UpdateCookbook
    ): CookbookManagementViewModel = CookbookManagementViewModel(
        getCookbooks, createCookbook, updateCookbook
    )
    
    // Sync Status ViewModel
    @Provides
    @Singleton
    fun provideSyncStatusViewModel(
        syncRecipes: SyncRecipes,
        syncStatusService: SyncStatusService,
        getSyncLogs: GetSyncLogs
    ): SyncStatusViewModel = SyncStatusViewModel(syncRecipes, syncStatusService)
    
    // Conflict Resolution ViewModel
    @Provides
    @Singleton
    fun provideConflictResolutionViewModel(
        getSyncConflictById: GetSyncConflictById,
        resolveConflict: ResolveConflict
    ): ConflictResolutionViewModel = ConflictResolutionViewModel(
        getSyncConflictById, resolveConflict
    )
    
    // Settings ViewModel
    @Provides
    @Singleton
    fun provideSettingsViewModel(
        getDevicePreferences: GetDevicePreferences,
        updateDevicePreferences: UpdateDevicePreferences
    ): SettingsViewModel = SettingsViewModel(getDevicePreferences, updateDevicePreferences)
    
    // Auth ViewModel
    @Provides
    @Singleton
    fun provideAuthViewModel(
        registerDevice: RegisterDevice,
        getDevicePreferences: GetDevicePreferences
    ): AuthViewModel = AuthViewModel(registerDevice, getDevicePreferences)
    
    // Device Registration ViewModel
    @Provides
    @Singleton
    fun provideDeviceRegistrationViewModel(
        registerDevice: RegisterDevice
    ): DeviceRegistrationViewModel = DeviceRegistrationViewModel(registerDevice)
    
    // OCR Scanner ViewModel
    @Provides
    @Singleton
    fun provideOCRScannerViewModel(
        createRecipe: CreateRecipe,
        addIngredient: AddIngredient
    ): OCRScannerViewModel = OCRScannerViewModel(createRecipe, addIngredient)
}
