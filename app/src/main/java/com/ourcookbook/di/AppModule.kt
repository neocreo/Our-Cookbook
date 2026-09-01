package com.ourcookbook.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.ourcookbook.data.repository.DevicePreferencesRepositoryImpl
import com.ourcookbook.data.repository.SettingsRepository
import com.ourcookbook.domain.repository.DevicePreferencesRepository
import com.ourcookbook.domain.usecase.devicepreferences.CreateDevicePreferences
import com.ourcookbook.domain.usecase.devicepreferences.GetDevicePreferencesByDevice
import com.ourcookbook.domain.usecase.devicepreferences.UpdateDevicePreferences
import com.ourcookbook.domain.usecase.sync.GetSyncStatus
import com.ourcookbook.domain.usecase.sync.UpdateLastSyncTimestamp
import com.ourcookbook.domain.usecase.sync.UpdateSyncInProgress
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * App Module for Dependency Injection
 * Provides application-wide dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // ========================================================================
    // DATASTORE
    // ========================================================================

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }

    // ========================================================================
    // REPOSITORIES
    // ========================================================================

    @Provides
    @Singleton
    fun provideSettingsRepository(
        @ApplicationContext context: Context
    ): SettingsRepository {
        return SettingsRepository(context)
    }

    @Provides
    @Singleton
    fun provideDevicePreferencesRepository(
        settingsRepository: SettingsRepository
    ): DevicePreferencesRepository {
        return DevicePreferencesRepositoryImpl(settingsRepository)
    }

    // ========================================================================
    // USE CASES - Device Preferences
    // ========================================================================

    @Provides
    @Singleton
    fun provideGetDevicePreferencesByDevice(
        repository: DevicePreferencesRepository
    ): GetDevicePreferencesByDevice {
        return GetDevicePreferencesByDevice(repository)
    }

    @Provides
    @Singleton
    fun provideUpdateDevicePreferences(
        repository: DevicePreferencesRepository
    ): UpdateDevicePreferences {
        return UpdateDevicePreferences(repository)
    }

    @Provides
    @Singleton
    fun provideCreateDevicePreferences(
        repository: DevicePreferencesRepository
    ): CreateDevicePreferences {
        return CreateDevicePreferences(repository)
    }

    // ========================================================================
    // USE CASES - Sync
    // ========================================================================

}

// ============================================================================
// DATASTORE EXTENSION
// ============================================================================

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")