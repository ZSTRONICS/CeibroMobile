package com.zstronics.ceibro.ui.forgotpassword

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.state.BaseState
import javax.inject.Inject

class ForgotPasswordState @Inject constructor() : BaseState(), IForgotPassword.State {
    override var phoneNumber: MutableLiveData<String> = MutableLiveData("")
    override var phoneCode: MutableLiveData<String> = MutableLiveData("")
}