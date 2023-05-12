package com.zstronics.ceibro.ui.forgotpassword

import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.repos.auth.IAuthRepository
import com.zstronics.ceibro.data.repos.auth.signup.ForgetPasswordRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ForgotPasswordVM @Inject constructor(
    override val viewState: ForgotPasswordState,
    private val repository: IAuthRepository
) : HiltBaseViewModel<IForgotPassword.State>(), IForgotPassword.ViewModel {


    override fun forgetPasswordVerifyNumber(phoneNumber: String, onMoveToNextScreen: () -> Unit) {
        val request = ForgetPasswordRequest(phoneNumber = phoneNumber)
        launch {
            loading(true)
            when (val response = repository.forgetPassword(request)) {

                is ApiResponse.Success -> {
                    loading(false, response.data.message)
                    onMoveToNextScreen.invoke()
                }

                is ApiResponse.Error -> {
                    loading(false, response.error.message)
                }
            }
        }
    }

}