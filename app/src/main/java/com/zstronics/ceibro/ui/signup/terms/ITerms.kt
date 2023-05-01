package com.zstronics.ceibro.ui.signup.terms

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.interfaces.IBase

interface ITerms {
    interface State : IBase.State {
        val isAgreedWithTerms: MutableLiveData<Boolean>
    }

    interface ViewModel : IBase.ViewModel<State> {
    }
}