package com.zstronics.ceibro.ui.verifynumber

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.state.BaseState
import javax.inject.Inject

class VerifyNumberState @Inject constructor() : BaseState(), IVerifyNumber.State {
    override val previousFragment: MutableLiveData<String> = MutableLiveData()
}