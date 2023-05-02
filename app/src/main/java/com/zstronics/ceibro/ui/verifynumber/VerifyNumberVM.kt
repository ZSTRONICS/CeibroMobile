package com.zstronics.ceibro.ui.verifynumber

import android.os.Bundle
import android.os.Handler
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.repos.auth.IAuthRepository
import com.zstronics.ceibro.data.repos.auth.signup.ForgetPasswordRequest
import com.zstronics.ceibro.data.repos.auth.signup.RegisterRequest
import com.zstronics.ceibro.data.repos.auth.signup.RegisterVerifyOtpRequest
import com.zstronics.ceibro.ui.chat.extensions.getChatTitle
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class VerifyNumberVM @Inject constructor(
    override val viewState: VerifyNumberState,
    private val repository: IAuthRepository
) : HiltBaseViewModel<IVerifyNumber.State>(), IVerifyNumber.ViewModel {


    override fun onFirsTimeUiCreate(bundle: Bundle?) {
        super.onFirsTimeUiCreate(bundle)

        with(viewState) {
            previousFragment.value = bundle?.getString("fromFragment")
            phoneNumber.value = bundle?.getString("phoneNumber")
        }
    }



    override fun registerOtpVerification(phoneNumber: String, otp: String) {
        val request = RegisterVerifyOtpRequest(phoneNumber = phoneNumber, otp = otp)
        launch {
            loading(true)
            when (val response = repository.registerVerifyOtp(request)) {

                is ApiResponse.Success -> {
                    val handler = Handler()
                    handler.postDelayed(Runnable {
                        clickEvent?.postValue(103)
                    }, 30)
                    loading(false, response.data.message)
                }
                is ApiResponse.Error -> {
                    loading(false, response.error.message)
                }
            }
        }
    }


    override fun forgetPasswordOtpVerification(phoneNumber: String, otp: String) {
        val request = RegisterVerifyOtpRequest(phoneNumber = phoneNumber, otp = otp)
        launch {
            loading(true)
            when (val response = repository.forgetPassVerifyOtp(request)) {

                is ApiResponse.Success -> {
                    val handler = Handler()
                    handler.postDelayed(Runnable {
                        clickEvent?.postValue(103)
                    }, 30)
                    loading(false, response.data.message)
                }
                is ApiResponse.Error -> {
                    loading(false, response.error.message)
                }
            }
        }
    }

    override fun resendOtp(phoneNumber: String, onOtpResend: () -> Unit) {
        val request = ForgetPasswordRequest(phoneNumber = phoneNumber)
        launch {
            loading(true)
            when (val response = repository.resendOtp(request)) {

                is ApiResponse.Success -> {
                    onOtpResend.invoke()
                    loading(false, response.data.message)
                }
                is ApiResponse.Error -> {
                    loading(false, response.error.message)
                }
            }
        }
    }

}