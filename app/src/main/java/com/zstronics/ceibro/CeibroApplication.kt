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
        OneSignal.setAppId(ONESIGNAL_APP_ID)
    }

    companion object {
        var ceibroApplication: CeibroApplication? = null
            private set

        const val ONESIGNAL_APP_ID = "f9cac0cf-3e04-486c-b87f-f84dc2c8e517"
//        const val ONESIGNAL_APP_ID = "7ac7a441-9500-4f47-b370-c523db13de03"
    }
}