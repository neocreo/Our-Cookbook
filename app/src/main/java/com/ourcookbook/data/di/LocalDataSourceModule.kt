package com.ourcookbook.data.di

import com.ourcookbook.data.db.AppDatabase
import com.ourcookbook.data.db.DatabaseConverters
import com.ourcookbook.data.db.dao.*
import com.ourcookbook.data.datasource.local.*
import com.ourcookbook.domain.service.ChecksumService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing local data source implementations
 * Binds local data source interfaces to their implementations
 */
@Module
@InstallIn(SingletonComponent::class)
object LocalDataSourceModule {

    // Recipe Local Data Source
    @Provides
    @Singleton
    fun provideRecipeLocalDataSource(
        recipeDao: RecipeDao,
        checksumService: ChecksumService,
        converters: DatabaseConverters
    ): IRecipeLocalDataSource {
        return RecipeLocalDataSource(recipeDao, checksumService, converters)
    }

    // Ingredient Local Data Source
    @Provides
    @Singleton
    fun provideIngredientLocalDataSource(
        ingredientDao: IngredientDao,
        checksumService: ChecksumService
    ): IIngredientLocalDataSource {
        return IngredientLocalDataSource(ingredientDao, checksumService)
    }

    // RecipeImage Local Data Source
    @Provides
    @Singleton
    fun provideRecipeImageLocalDataSource(
        recipeImageDao: RecipeImageDao,
        checksumService: ChecksumService
    ): IRecipeImageLocalDataSource {
        return RecipeImageLocalDataSource(recipeImageDao, checksumService)
    }

    // Device Local Data Source
    @Provides
    @Singleton
    fun provideDeviceLocalDataSource(
        deviceDao: DeviceDao,
        checksumService: ChecksumService,
        converters: DatabaseConverters
    ): IDeviceLocalDataSource {
        return DeviceLocalDataSource(deviceDao, checksumService, converters)
    }

    // DevicePreferences Local Data Source
    @Provides
    @Singleton
    fun provideDevicePreferencesLocalDataSource(
        devicePreferencesDao: DevicePreferencesDao,
        checksumService: ChecksumService
    ): IDevicePreferencesLocalDataSource {
        return DevicePreferencesLocalDataSource(devicePreferencesDao, checksumService)
    }

    // Cookbook Local Data Source
    @Provides
    @Singleton
    fun provideCookbookLocalDataSource(
        cookbookDao: CookbookDao,
        checksumService: ChecksumService,
        converters: DatabaseConverters
    ): ICookbookLocalDataSource {
        return CookbookLocalDataSource(cookbookDao, checksumService, converters)
    }

    // SharingLink Local Data Source
    @Provides
    @Singleton
    fun provideSharingLinkLocalDataSource(
        sharingLinkDao: SharingLinkDao,
        checksumService: ChecksumService,
        converters: DatabaseConverters
    ): ISharingLinkLocalDataSource {
        return SharingLinkLocalDataSource(sharingLinkDao, checksumService, converters)
    }

    // SyncConflict Local Data Source
    @Provides
    @Singleton
    fun provideSyncConflictLocalDataSource(
        syncConflictDao: SyncConflictDao,
        checksumService: ChecksumService,
        converters: DatabaseConverters
    ): ISyncConflictLocalDataSource {
        return SyncConflictLocalDataSource(syncConflictDao, checksumService, converters)
    }

    // SyncLog Local Data Source
    @Provides
    @Singleton
    fun provideSyncLogLocalDataSource(
        syncLogDao: SyncLogDao,
        checksumService: ChecksumService
    ): ISyncLogLocalDataSource {
        return SyncLogLocalDataSource(syncLogDao, checksumService)
    }

    // PendingSync Local Data Source
    @Provides
    @Singleton
    fun providePendingSyncLocalDataSource(
        pendingSyncDao: PendingSyncDao,
        checksumService: ChecksumService
    ): IPendingSyncLocalDataSource {
        return PendingSyncLocalDataSource(pendingSyncDao, checksumService)
    }

    // SyncMetadata Local Data Source
    @Provides
    @Singleton
    fun provideSyncMetadataLocalDataSource(
        syncMetadataDao: SyncMetadataDao,
        checksumService: ChecksumService
    ): ISyncMetadataLocalDataSource {
        return SyncMetadataLocalDataSource(syncMetadataDao, checksumService)
    }

    // DriveFileInfo Local Data Source
    @Provides
    @Singleton
    fun provideDriveFileInfoLocalDataSource(
        driveFileInfoDao: DriveFileInfoDao,
        checksumService: ChecksumService
    ): IDriveFileInfoLocalDataSource {
        return DriveFileInfoLocalDataSource(driveFileInfoDao, checksumService)
    }

    // Tombstone Local Data Source
    @Provides
    @Singleton
    fun provideTombstoneLocalDataSource(
        tombstoneDao: TombstoneDao,
        checksumService: ChecksumService,
        converters: DatabaseConverters
    ): ITombstoneLocalDataSource {
        return TombstoneLocalDataSource(tombstoneDao, checksumService, converters)
    }

    // DatabaseConverters
    @Provides
    @Singleton
    fun provideDatabaseConverters(): DatabaseConverters {
        return DatabaseConverters()
    }
    
    // Export/Import Data Source
    @Provides
    @Singleton
    fun provideExportImportDataSource(
        @ApplicationContext context: android.content.Context
    ): com.ourcookbook.data.datasource.IExportImportDataSource {
        return com.ourcookbook.data.datasource.ExportImportDataSourceImpl(context)
    }
}