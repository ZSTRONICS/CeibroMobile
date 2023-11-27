package com.zstronics.ceibro.ui.signup.register

import android.os.Handler
import com.onesignal.OneSignal
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.repos.auth.IAuthRepository
import com.zstronics.ceibro.data.repos.auth.login.Access
import com.zstronics.ceibro.data.repos.auth.login.AuthTokenRequest
import com.zstronics.ceibro.data.repos.auth.login.LoginRequest
import com.zstronics.ceibro.data.repos.auth.signup.RegisterRequest
import com.zstronics.ceibro.data.sessions.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RegisterVM @Inject constructor(
    override val viewState: RegisterState,
    private val repository: IAuthRepository,
    private val sessionManager: SessionManager
) : HiltBaseViewModel<IRegister.State>(), IRegister.ViewModel {


    override fun registerNumber(
        phoneNumber: String,
        token: String,
        onNumberRegistered: () -> Unit
    ) {

        val request = RegisterRequest(phoneNumber = phoneNumber)
        launch {
            when (val response = repository.register(request, token)) {

                is ApiResponse.Success -> {
                    loading(false, response.data.message)
                    onNumberRegistered.invoke()
                }

                is ApiResponse.Error -> {
                    loading(false, response.error.message)
                }
            }
        }
    }

    override fun getAuthTokenAndThenRegister(phoneNumber: String, clientId: String, callBack: (authToken: Access) -> Unit) {
        launch {
            loading(true)
            when (val response = repository.getAuthToken(clientId)) {

                is ApiResponse.Success -> {
                    val authToken = response.data.access
                    println("Encrypted PhoneNumber: response.data.access.token ${response.data.access.token}")
                    registerNumber(phoneNumber, response.data.access.token) {
                        callBack.invoke(authToken)
                    }
                }

                is ApiResponse.Error -> {
                    loading(false, response.error.message)
                }
            }
        }
    }
}