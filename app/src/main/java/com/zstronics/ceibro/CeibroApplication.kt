package com.zstronics.ceibro

import android.app.Application
import com.onesignal.OneSignal
import com.zstronics.ceibro.data.base.interceptor.SessionValidator
import com.zstronics.ceibro.data.repos.auth.IAuthRepository
import com.zstronics.ceibro.data.sessions.SessionManager
import dagger.hilt.android.HiltAndroidApp
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
        OneSignal.promptForPushNotifications()
    }

    companion object {
        var ceibroApplication: CeibroApplication? = null
            private set
    }
}