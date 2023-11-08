package com.zstronics.ceibro.ui.login

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.interfaces.IBase

interface ILogin {
    interface State : IBase.State {
        val rememberMe: MutableLiveData<Boolean>
        var email: MutableLiveData<String>
        var password: MutableLiveData<String>
    }

    interface ViewModel : IBase.ViewModel<State> {
        fun doLogin(context: Context, phoneNumber: String, password: String, rememberMe: Boolean, onLoggedIn: () -> Unit)
//        fun getTaskWithUpdatedTimeStamp(timeStamp: String, onLoggedIn: () -> Unit)
    }
}