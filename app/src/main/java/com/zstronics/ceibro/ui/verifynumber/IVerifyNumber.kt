package com.zstronics.ceibro.ui.verifynumber

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.interfaces.IBase

interface IVerifyNumber {
    interface State : IBase.State {
        val previousFragment: MutableLiveData<String>
        var phoneNumber: MutableLiveData<String>
        var otp: MutableLiveData<String>
    }

    interface ViewModel : IBase.ViewModel<State> {
        fun registerOtpVerification(phoneNumber: String, otp: String)
        fun forgetPasswordOtpVerification(phoneNumber: String, otp: String)
        fun resendOtp(phoneNumber: String, onOtpResend: () -> Unit)
    }
}