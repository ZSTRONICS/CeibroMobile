package com.zstronics.ceibro.ui.tasks.v3.fragments.ongoing

import com.zstronics.ceibro.base.interfaces.IBase

interface IOngoing {
    interface State : IBase.State {
    }

    interface ViewModel : IBase.ViewModel<State> {
    }
}