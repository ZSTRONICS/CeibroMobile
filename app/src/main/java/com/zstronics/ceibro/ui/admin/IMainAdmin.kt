package com.zstronics.ceibro.ui.admin

import com.zstronics.ceibro.base.interfaces.IBase

interface IMainAdmin {
    interface State : IBase.State {
    }

    interface ViewModel : IBase.ViewModel<State> {
    }
}