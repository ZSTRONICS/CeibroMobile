package com.zstronics.ceibro.ui.signup.register

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.interfaces.IBase
import com.zstronics.ceibro.data.repos.auth.login.Access

interface IRegister {
    interface State : IBase.State {
        var phoneNumber: MutableLiveData<String>
        var phoneCode: MutableLiveData<String>
    }

    interface ViewModel : IBase.ViewModel<State> {
        fun registerNumber(phoneNumber: String, token: String, onNumberRegistered: () -> Unit)
        fun getAuthTokenAndThenRegister(phoneNumber: String, clientId: String, callBack: (authToken: Access) -> Unit)
    }
}