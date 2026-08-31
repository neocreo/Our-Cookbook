package com.ourcookbook.di

import android.content.Context
import androidx.room.Room
import com.ourcookbook.data.db.AppDatabase
import com.ourcookbook.data.db.dao.*
import com.ourcookbook.data.repository.*
import com.ourcookbook.data.service.*
import com.ourcookbook.domain.service.ChecksumService
import com.ourcookbook.domain.service.ConflictResolver
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
object ServiceModule {
    
    @Provides
    @Singleton
    fun provideChecksumService(): ChecksumService = ChecksumServiceImpl()
    
    @Provides
    @Singleton
    fun provideConflictResolver(
        checksumService: ChecksumService
    ): ConflictResolver = ConflictResolverImpl(checksumService)
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
