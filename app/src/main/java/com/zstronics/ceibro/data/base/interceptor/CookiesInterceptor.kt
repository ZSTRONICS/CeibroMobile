package com.zstronics.ceibro.data.base.interceptor
import android.text.TextUtils
import androidx.media.MediaSessionManager.getSessionManager
import com.zstronics.ceibro.data.base.CookiesManager
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.data.sessions.SharedPreferenceManager
import ee.zstronics.CeibroCameraApplication.Companion.context
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import javax.inject.Inject
import kotlin.coroutines.coroutineContext

const val KEY_AUTHORIZATION = "Authorization"

const val KEY_BEARER = "Bearer "

const val KEY_ACCEPT = "Accept"

class CookiesInterceptor @Inject constructor(_sessionManager: SessionManager) : Interceptor {
    val sessionManager = _sessionManager
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = addCookiesInRequest(chain.request())
        val response = chain.proceed(request)
        return response
    }

    private fun addCookiesInRequest(request: Request): Request {
        val builder = request.newBuilder()

        if (!TextUtils.isEmpty(CookiesManager.jwtToken)) {
            builder.addHeader(KEY_AUTHORIZATION, KEY_BEARER + CookiesManager.jwtToken)
        } else {
            sessionManager.isUserLoggedIn()
            builder.addHeader(KEY_AUTHORIZATION, KEY_BEARER + CookiesManager.jwtToken)
        }
//        builder.addHeader(KEY_ACCEPT, "*/*")
        return builder.build()
    }
}