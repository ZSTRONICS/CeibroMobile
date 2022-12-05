package com.zstronics.ceibro.data.base.interceptor
import android.text.TextUtils
import com.zstronics.ceibro.data.base.CookiesManager
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import javax.inject.Inject

const val KEY_AUTHORIZATION = "Authorization"

const val KEY_BEARER = "Bearer "

const val KEY_ACCEPT = "Accept"

class CookiesInterceptor @Inject constructor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = addCookiesInRequest(chain.request())
        val response = chain.proceed(request)
        return response
    }

    private fun addCookiesInRequest(request: Request): Request {
        val builder = request.newBuilder()

        if (!TextUtils.isEmpty(CookiesManager.jwtToken)) {
            builder.addHeader(KEY_AUTHORIZATION, KEY_BEARER + CookiesManager.jwtToken)
        }
//        builder.addHeader(KEY_ACCEPT, "*/*")
        return builder.build()
    }
}