package com.zstronics.ceibro.ui.signup

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.interfaces.IBase

interface ISignUp {
    interface State : IBase.State {
        var firstName: MutableLiveData<String>
        var surname: MutableLiveData<String>
        var email: MutableLiveData<String>
        var companyName: MutableLiveData<String>
        var jobTitle: MutableLiveData<String>
        var password: MutableLiveData<String>
        var confirmPassword: MutableLiveData<String>
    }

    interface ViewModel : IBase.ViewModel<State> {
        fun onSignUp()
        fun doSignUp(firstName: String, surname: String, email: String, companyName: String, jobTitle: String,
                     phoneNumber: String, password: String, confirmPassword: String)
    }
}