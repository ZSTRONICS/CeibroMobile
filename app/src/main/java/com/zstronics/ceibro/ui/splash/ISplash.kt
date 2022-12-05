package com.zstronics.ceibro.ui.splash

import com.zstronics.ceibro.base.interfaces.IBase

interface ISplash {
    interface State : IBase.State {
    }

    interface ViewModel : IBase.ViewModel<State> {
        fun getProfile()
    }
}