package com.zstronics.ceibro.ui.login

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.interfaces.IBase

interface ILogin {
    interface State : IBase.State {
        val rememberMe: MutableLiveData<Boolean>
        var email: MutableLiveData<String>
        var password: MutableLiveData<String>
    }

    interface ViewModel : IBase.ViewModel<State> {
        fun doLogin(phoneNumber: String, password: String, rememberMe: Boolean)
    }
}