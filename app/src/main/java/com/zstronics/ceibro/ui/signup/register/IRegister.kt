package com.zstronics.ceibro.ui.signup.register

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.interfaces.IBase

interface IRegister {
    interface State : IBase.State {
        var phoneNumber: MutableLiveData<String>
        var phoneCode: MutableLiveData<String>
    }

    interface ViewModel : IBase.ViewModel<State> {
        fun registerNumber(phoneNumber: String, onNumberRegistered: () -> Unit)
    }
}