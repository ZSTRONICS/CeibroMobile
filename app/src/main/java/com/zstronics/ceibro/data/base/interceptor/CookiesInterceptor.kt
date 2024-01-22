package com.zstronics.ceibro.data.base.interceptor

import com.zstronics.ceibro.CeibroApplication
import com.zstronics.ceibro.data.sessions.SessionManager
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import javax.inject.Inject

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

        if (!CeibroApplication.CookiesManager.jwtToken.isNullOrEmpty()) {
            builder.addHeader(
                KEY_AUTHORIZATION,
                KEY_BEARER + CeibroApplication.CookiesManager.jwtToken
            )
        } else {
            if (sessionManager.isUserLoggedIn()) {
                builder.addHeader(
                    KEY_AUTHORIZATION,
                    KEY_BEARER + CeibroApplication.CookiesManager.jwtToken
                )
            }
        }
//        builder.addHeader(KEY_ACCEPT, "*/*")
        return builder.build()
    }
}