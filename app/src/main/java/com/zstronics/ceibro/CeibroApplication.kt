package com.zstronics.ceibro

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager
import com.onesignal.OneSignal
import com.zstronics.ceibro.data.base.interceptor.SessionValidator
import com.zstronics.ceibro.data.repos.auth.IAuthRepository
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.ui.socket.SocketHandler
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.Executors
import javax.inject.Inject

@HiltAndroidApp
open class CeibroApplication : Application() {
    @Inject
    lateinit var sessionValidator: SessionValidator

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var authApi: IAuthRepository

    override fun onCreate() {
        super.onCreate()
        ceibroApplication = this
        sessionValidator.setAuthRepository(authApi)
        sessionValidator.setSessionManager(sessionManager)

        // Logging set to help debug issues, remove before releasing your app.
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE)

        // OneSignal Initialization
        OneSignal.initWithContext(this)
        OneSignal.setAppId(BuildConfig.ONE_SIGNAL_ID)
        val customExecutor = Executors.newSingleThreadExecutor { runnable ->
            val thread = Thread(runnable)
            thread.priority = android.os.Process.THREAD_PRIORITY_BACKGROUND
            thread
        }

        val workManagerConfiguration = Configuration.Builder()
            .setExecutor(customExecutor)
            .build()

        WorkManager.initialize(applicationContext, workManagerConfiguration)
    }

    companion object {
        var ceibroApplication: CeibroApplication? = null
            private set
    }

    override fun onTerminate() {
        super.onTerminate()
        SocketHandler.sendLogout()
        SocketHandler.closeConnectionAndRemoveObservers()
    }
}