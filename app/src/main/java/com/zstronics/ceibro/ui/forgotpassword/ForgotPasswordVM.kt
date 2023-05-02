package com.zstronics.ceibro.ui.forgotpassword

import android.os.Handler
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.repos.auth.IAuthRepository
import com.zstronics.ceibro.data.repos.auth.signup.ForgetPasswordRequest
import com.zstronics.ceibro.data.repos.auth.signup.RegisterRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ForgotPasswordVM @Inject constructor(
    override val viewState: ForgotPasswordState,
    private val repository: IAuthRepository
) : HiltBaseViewModel<IForgotPassword.State>(), IForgotPassword.ViewModel {


    override fun forgetPasswordVerifyNumber(phoneNumber: String) {
        val request = ForgetPasswordRequest(phoneNumber = phoneNumber)
        launch {
            loading(true)
            when (val response = repository.forgetPassword(request)) {

                is ApiResponse.Success -> {
                    val handler = Handler()
                    handler.postDelayed(Runnable {
                        clickEvent?.postValue(104)
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