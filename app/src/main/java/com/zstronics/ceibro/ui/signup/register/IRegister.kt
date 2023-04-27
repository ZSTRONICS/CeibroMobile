package com.zstronics.ceibro.ui.signup.register

import com.zstronics.ceibro.base.interfaces.IBase

interface IRegister {
    interface State : IBase.State {
    }

    interface ViewModel : IBase.ViewModel<State> {
    }
}