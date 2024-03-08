package com.zstronics.ceibro.ui.tasks.v3.fragments.closed

import com.zstronics.ceibro.base.interfaces.IBase

interface ITaskV3Closed {
    interface State : IBase.State {
    }

    interface ViewModel : IBase.ViewModel<State> {
    }
}