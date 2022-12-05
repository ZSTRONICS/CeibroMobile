package com.zstronics.ceibro.di

import androidx.appcompat.app.AppCompatActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ActivityContext

@Module
@InstallIn(ActivityComponent::class)
class ActivityInjectorsModule {
    @Provides
    fun provideActivityContext(@ActivityContext context: AppCompatActivity) = context

}