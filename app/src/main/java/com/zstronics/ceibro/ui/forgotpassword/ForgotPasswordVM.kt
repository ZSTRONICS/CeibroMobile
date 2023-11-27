package com.zstronics.ceibro.ui.forgotpassword

import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.repos.auth.IAuthRepository
import com.zstronics.ceibro.data.repos.auth.login.Access
import com.zstronics.ceibro.data.repos.auth.signup.ForgetPasswordRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ForgotPasswordVM @Inject constructor(
    override val viewState: ForgotPasswordState,
    private val repository: IAuthRepository
) : HiltBaseViewModel<IForgotPassword.State>(), IForgotPassword.ViewModel {


    override fun forgetPasswordVerifyNumber(
        phoneNumber: String,
        token: String,
        onMoveToNextScreen: () -> Unit
    ) {
        val request = ForgetPasswordRequest(phoneNumber = phoneNumber)
        launch {
            when (val response = repository.forgetPassword(request, token)) {

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


    override fun getAuthTokenAndThenNext(phoneNumber: String, clientId: String, callBack: (authToken: Access) -> Unit) {
        launch {
            loading(true)
            when (val response = repository.getAuthToken(clientId)) {

                is ApiResponse.Success -> {
                    val authToken = response.data.access
                    forgetPasswordVerifyNumber(phoneNumber, response.data.access.token) {
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