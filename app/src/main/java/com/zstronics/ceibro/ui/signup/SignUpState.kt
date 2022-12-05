package com.zstronics.ceibro.ui.signup

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.state.BaseState
import javax.inject.Inject

class SignUpState @Inject constructor() : BaseState(), ISignUp.State {
    override var firstName: MutableLiveData<String> = MutableLiveData("")
    override var surname: MutableLiveData<String> = MutableLiveData("")
    override var email: MutableLiveData<String> = MutableLiveData("")
    override var password: MutableLiveData<String> = MutableLiveData("")
    override var confirmPassword: MutableLiveData<String> = MutableLiveData("")
}