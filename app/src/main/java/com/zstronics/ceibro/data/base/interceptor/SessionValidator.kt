package com.zstronics.ceibro.data.base.interceptor

import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.base.CookiesManager
import com.zstronics.ceibro.data.repos.auth.IAuthRepository
import com.zstronics.ceibro.data.repos.auth.refreshtoken.RefreshTokenRequest
import com.zstronics.ceibro.data.sessions.SessionManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

open class SessionValidator :
    TokenValidator, Interceptor {
    private lateinit var authRepository: IAuthRepository
    private lateinit var sessionManager: SessionManager
    fun setAuthRepository(authRepository: IAuthRepository) {
        this.authRepository = authRepository
    }

    fun setSessionManager(sessionManager: SessionManager) {
        this.sessionManager = sessionManager
    }

    override var tokenRefreshInProgress: Boolean = false
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val refreshToken = sessionManager.getRefreshToken() ?: ""
        var response = chain.proceed(originalRequest)
        if (CookiesManager.isLoggedIn && response.code == 401) {
            if ((!tokenRefreshInProgress)) {
                tokenRefreshInProgress = true
                when (runBlocking {
                    authRepository.refreshJWTToken(
                        RefreshTokenRequest(
                            refreshToken
                        )
                    )
                }) {
                    is ApiResponse.Success -> {
                        CookiesManager.tokens?.let { sessionManager.refreshToken(it) }
                        val builder = originalRequest.newBuilder()
                            .header(KEY_AUTHORIZATION, KEY_BEARER + CookiesManager.jwtToken)
                            .method(originalRequest.method, originalRequest.body)
                        try {
                            response.close() // Close the previous response
                        } catch (e: Exception) {

                        }
                        response = chain.proceed(builder.build())
                    }
                    is ApiResponse.Error -> {

                    }
                }
                tokenRefreshInProgress = false
            }
        }
        return response
    }

}