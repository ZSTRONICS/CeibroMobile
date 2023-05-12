package com.zstronics.ceibro.ui.signup.register

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.state.BaseState
import javax.inject.Inject

class RegisterState @Inject constructor() : BaseState(), IRegister.State {
    override var phoneNumber: MutableLiveData<String> = MutableLiveData("")
    override var phoneCode: MutableLiveData<String> = MutableLiveData("")
}