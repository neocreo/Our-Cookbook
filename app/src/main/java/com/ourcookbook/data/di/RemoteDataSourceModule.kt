package com.ourcookbook.data.di

import com.ourcookbook.data.datasource.remote.RecipeRemoteDataSource
import com.ourcookbook.data.datasource.remote.IRecipeRemoteDataSource
import com.ourcookbook.domain.service.ChecksumService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing remote data source implementations
 */
@Module
@InstallIn(SingletonComponent::class)
object RemoteDataSourceModule {

    @Provides
    @Singleton
    fun provideRecipeRemoteDataSource(
        checksumService: ChecksumService
    ): IRecipeRemoteDataSource {
        return RecipeRemoteDataSource(checksumService)
    }
}