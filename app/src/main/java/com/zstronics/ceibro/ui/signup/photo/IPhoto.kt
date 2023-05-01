package com.zstronics.ceibro.ui.signup.photo

import com.zstronics.ceibro.base.interfaces.IBase

interface IPhoto {
    interface State : IBase.State {
    }

    interface ViewModel : IBase.ViewModel<State> {
    }
}