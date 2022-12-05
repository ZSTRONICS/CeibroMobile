package com.zstronics.ceibro.ui.works

import com.zstronics.ceibro.base.interfaces.IBase

interface IWorks {
    interface State : IBase.State {
    }

    interface ViewModel : IBase.ViewModel<State> {
    }
}