package com.zstronics.ceibro.ui.login

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.BuildConfig
import com.zstronics.ceibro.base.state.BaseState
import javax.inject.Inject

class LoginState @Inject constructor() : BaseState(), ILogin.State {
    override val rememberMe: MutableLiveData<Boolean> = MutableLiveData(false)
    override var email: MutableLiveData<String> =
        MutableLiveData(if (BuildConfig.DEBUG) "3069261629" else "")
    override var password: MutableLiveData<String> =
        MutableLiveData(if (BuildConfig.DEBUG) "Apple@123" else "")
}