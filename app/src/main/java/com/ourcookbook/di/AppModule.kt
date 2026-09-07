package com.ourcookbook.di

import android.content.Context
import com.ourcookbook.data.repository.DevicePreferencesRepositoryImpl
import com.ourcookbook.data.repository.SettingsRepository
import com.ourcookbook.domain.repository.DevicePreferencesRepository
import com.ourcookbook.domain.usecase.devicepreferences.CreateDevicePreferences
import com.ourcookbook.domain.usecase.devicepreferences.GetDevicePreferencesByDevice
import com.ourcookbook.domain.usecase.devicepreferences.UpdateDevicePreferences
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
    // REPOSITORIES
    // ========================================================================

    @Provides
    @Singleton
    fun provideSettingsRepository(
        @ApplicationContext context: Context
    ): SettingsRepository {
        return SettingsRepository(context)
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