package com.zstronics.ceibro.ui.verifynumber

import com.zstronics.ceibro.base.interfaces.IBase

interface IVerifyNumber {
    interface State : IBase.State {
    }

    interface ViewModel : IBase.ViewModel<State> {
    }
}