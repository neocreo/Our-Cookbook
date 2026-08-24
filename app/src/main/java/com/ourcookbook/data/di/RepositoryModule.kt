package com.ourcookbook.data.di

import com.ourcookbook.data.datasource.IExportImportDataSource
import com.ourcookbook.data.datasource.local.*
import com.ourcookbook.data.db.dao.RecipeFtsDao
import com.ourcookbook.data.repository.*
import com.ourcookbook.domain.repository.*
import com.ourcookbook.domain.service.ChecksumService
import com.ourcookbook.domain.service.ConflictResolver
import com.ourcookbook.domain.service.SyncService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing repository implementations
 * Binds repository interfaces to their implementations
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    // Recipe Repository
    @Provides
    @Singleton
    fun provideRecipeRepository(
        localDataSource: IRecipeLocalDataSource,
        remoteDataSource: IRecipeRemoteDataSource,
        checksumService: ChecksumService,
        conflictResolver: ConflictResolver,
        syncService: SyncService
    ): RecipeRepository {
        return RecipeRepositoryImpl(localDataSource, remoteDataSource, checksumService, conflictResolver, syncService)
    }

    // Ingredient Repository
    @Provides
    @Singleton
    fun provideIngredientRepository(
        localDataSource: IIngredientLocalDataSource,
        checksumService: ChecksumService
    ): IngredientRepository {
        return IngredientRepositoryImpl(localDataSource, checksumService)
    }

    // RecipeImage Repository
    @Provides
    @Singleton
    fun provideRecipeImageRepository(
        localDataSource: IRecipeImageLocalDataSource,
        checksumService: ChecksumService
    ): RecipeImageRepository {
        return RecipeImageRepositoryImpl(localDataSource, checksumService)
    }

    // Device Repository
    @Provides
    @Singleton
    fun provideDeviceRepository(
        localDataSource: IDeviceLocalDataSource,
        checksumService: ChecksumService
    ): DeviceRepository {
        return DeviceRepositoryImpl(localDataSource, checksumService)
    }

    // DevicePreferences Repository
    @Provides
    @Singleton
    fun provideDevicePreferencesRepository(
        settingsRepository: SettingsRepository
    ): DevicePreferencesRepository {
        return DevicePreferencesRepositoryImpl(settingsRepository)
    }

    // Cookbook Repository
    @Provides
    @Singleton
    fun provideCookbookRepository(
        localDataSource: ICookbookLocalDataSource,
        checksumService: ChecksumService
    ): CookbookRepository {
        return CookbookRepositoryImpl(localDataSource, checksumService)
    }

    // SharingLink Repository
    @Provides
    @Singleton
    fun provideSharingLinkRepository(
        localDataSource: ISharingLinkLocalDataSource,
        checksumService: ChecksumService
    ): SharingLinkRepository {
        return SharingLinkRepositoryImpl(localDataSource, checksumService)
    }

    // SyncConflict Repository
    @Provides
    @Singleton
    fun provideSyncConflictRepository(
        localDataSource: ISyncConflictLocalDataSource,
        checksumService: ChecksumService
    ): SyncConflictRepository {
        return SyncConflictRepositoryImpl(localDataSource, checksumService)
    }

    // SyncLog Repository
    @Provides
    @Singleton
    fun provideSyncLogRepository(
        localDataSource: ISyncLogLocalDataSource,
        checksumService: ChecksumService
    ): SyncLogRepository {
        return SyncLogRepositoryImpl(localDataSource, checksumService)
    }

    // PendingSync Repository
    @Provides
    @Singleton
    fun providePendingSyncRepository(
        localDataSource: IPendingSyncLocalDataSource,
        checksumService: ChecksumService
    ): PendingSyncRepository {
        return PendingSyncRepositoryImpl(localDataSource, checksumService)
    }

    // SyncMetadata Repository
    @Provides
    @Singleton
    fun provideSyncMetadataRepository(
        localDataSource: ISyncMetadataLocalDataSource,
        checksumService: ChecksumService
    ): SyncMetadataRepository {
        return SyncMetadataRepositoryImpl(localDataSource, checksumService)
    }

    // DriveFileInfo Repository
    @Provides
    @Singleton
    fun provideDriveFileInfoRepository(
        localDataSource: IDriveFileInfoLocalDataSource,
        checksumService: ChecksumService
    ): DriveFileInfoRepository {
        return DriveFileInfoRepositoryImpl(localDataSource, checksumService)
    }

    // Tombstone Repository
    @Provides
    @Singleton
    fun provideTombstoneRepository(
        localDataSource: ITombstoneLocalDataSource,
        checksumService: ChecksumService
    ): TombstoneRepository {
        return TombstoneRepositoryImpl(localDataSource, checksumService)
    }

    // FullTextSearch Repository
    @Provides
    @Singleton
    fun provideFullTextSearchRepository(
        recipeFtsDao: RecipeFtsDao
    ): FullTextSearchRepository {
        return FullTextSearchRepositoryImpl(recipeFtsDao)
    }
    
    // Export/Import Repository
    @Provides
    @Singleton
    fun provideExportImportRepository(
        exportImportDataSource: IExportImportDataSource,
        recipeRepository: RecipeRepository,
        cookbookRepository: CookbookRepository,
        checksumService: ChecksumService,
        @ApplicationContext context: android.content.Context
    ): ExportImportRepository {
        return ExportImportRepositoryImpl(
            exportImportDataSource, recipeRepository, cookbookRepository, checksumService, context
        )
    }
}