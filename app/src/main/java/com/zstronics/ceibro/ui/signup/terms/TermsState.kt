package com.zstronics.ceibro.ui.signup.terms

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.state.BaseState
import javax.inject.Inject

class TermsState @Inject constructor() : BaseState(), ITerms.State {
    override val isAgreedWithTerms: MutableLiveData<Boolean> = MutableLiveData(false)
}