package com.zstronics.ceibro.di

import com.zstronics.ceibro.resourses.AndroidResourceProvider
import com.zstronics.ceibro.resourses.IResourceProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ApplicationBindsModule {

    @Binds
    @Singleton
    abstract fun provideAndroidResource(androidResourceProvider: AndroidResourceProvider): IResourceProvider

}