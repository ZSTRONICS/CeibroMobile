package com.zstronics.ceibro.ui.verifynumber

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.interfaces.IBase

interface IVerifyNumber {
    interface State : IBase.State {
        val previousFragment: MutableLiveData<String>
        var phoneNumber: MutableLiveData<String>
        var phoneCode: MutableLiveData<String>
        var otp: MutableLiveData<String>
        var authToken: MutableLiveData<String>
        var authTokenExpiry: MutableLiveData<String>
        var clientId: MutableLiveData<String>
    }

    interface ViewModel : IBase.ViewModel<State> {
        fun registerOtpVerification(phoneNumber: String, otp: String, onOtpVerified: () -> Unit)
        fun forgetPasswordOtpVerification(phoneNumber: String, otp: String, onOtpVerified: () -> Unit)
        fun resendOtp(phoneNumber: String, onOtpResend: () -> Unit)
        fun resetPassword(phoneNumber: String, password: String, otp: String, onPasswordReset: () -> Unit)
    }
}