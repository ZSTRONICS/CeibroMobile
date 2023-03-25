package com.zstronics.ceibro

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
open class CeibroApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ceibroApplication = this
    }

    companion object {
        var ceibroApplication: CeibroApplication? = null
            private set
    }
}