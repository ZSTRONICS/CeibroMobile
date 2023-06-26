package com.zstronics.ceibro.ui.tasks.v2.taskdetail.forward

import com.zstronics.ceibro.base.interfaces.IBase

interface IForward {
    interface State : IBase.State {
    }

    interface ViewModel : IBase.ViewModel<State> {
    }
}