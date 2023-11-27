package com.zstronics.ceibro.ui.verifynumber

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.state.BaseState
import javax.inject.Inject

class VerifyNumberState @Inject constructor() : BaseState(), IVerifyNumber.State {
    override val previousFragment: MutableLiveData<String> = MutableLiveData()
    override var phoneNumber: MutableLiveData<String> = MutableLiveData("")
    override var phoneCode: MutableLiveData<String> = MutableLiveData("")
    override var otp: MutableLiveData<String> = MutableLiveData("")
    override var authToken: MutableLiveData<String> = MutableLiveData("")
    override var authTokenExpiry: MutableLiveData<String> = MutableLiveData("")
    override var clientId: MutableLiveData<String> = MutableLiveData("")
}