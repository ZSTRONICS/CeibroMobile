package com.zstronics.ceibro.di

import com.zstronics.ceibro.base.validator.Validator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
class ViewModelModule {

    @Provides
    fun provideValidator(): Validator {
        return Validator(null)
    }
}