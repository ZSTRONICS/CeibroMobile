package com.zstronics.ceibro.ui.forgotpassword

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.interfaces.IBase
import com.zstronics.ceibro.data.repos.auth.login.Access

interface IForgotPassword {
    interface State : IBase.State {
        var phoneNumber: MutableLiveData<String>
        var phoneCode: MutableLiveData<String>
    }

    interface ViewModel : IBase.ViewModel<State> {
        fun forgetPasswordVerifyNumber(
            phoneNumber: String,
            token: String,
            onMoveToNextScreen: () -> Unit
        )
        fun getAuthTokenAndThenNext(phoneNumber: String, clientId: String, callBack: (authToken: Access) -> Unit)
    }
}