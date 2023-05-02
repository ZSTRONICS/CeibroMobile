package com.zstronics.ceibro.ui.signup.register

import android.os.Handler
import com.onesignal.OneSignal
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.repos.auth.IAuthRepository
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


    override fun registerNumber(phoneNumber: String) {

        val request = RegisterRequest(phoneNumber = phoneNumber)
        launch {
            loading(true)
            when (val response = repository.register(request)) {

                is ApiResponse.Success -> {
                    val handler = Handler()
                    handler.postDelayed(Runnable {
                        clickEvent?.postValue(102)
                    }, 30)
                    loading(false, response.data.message)
                }
                is ApiResponse.Error -> {
                    loading(false, response.error.message)
                }
            }
        }
    }
}