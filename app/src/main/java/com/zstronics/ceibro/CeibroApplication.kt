package com.zstronics.ceibro

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
open class CeibroApplication : Application() {
//    @Inject
//    lateinit var workerFactory: HiltWorkerFactory
//
////    override fun onCreate() {
////        super.onCreate()
////        WorkManager.initialize(this, Configuration.Builder().setWorkerFactory(workerFactory).build())
////    }
//
//    override fun getWorkManagerConfiguration(): Configuration {
//        return Configuration.Builder()
//            .setWorkerFactory(workerFactory)
//            .build()
//    }
}