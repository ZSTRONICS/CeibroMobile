package com.zstronics.ceibro.ui.forgotpassword

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.interfaces.IBase

interface IForgotPassword {
    interface State : IBase.State {
        var phoneNumber: MutableLiveData<String>
    }

    interface ViewModel : IBase.ViewModel<State> {
        fun forgetPasswordVerifyNumber(phoneNumber: String, onMoveToNextScreen: () -> Unit)
    }
}