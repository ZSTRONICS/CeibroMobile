package com.zstronics.ceibro.ui.tasks

import com.zstronics.ceibro.base.interfaces.IBase

interface ITasks {
    interface State : IBase.State {
    }

    interface ViewModel : IBase.ViewModel<State> {
    }
}