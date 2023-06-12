package com.zstronics.ceibro.ui.tasks.v2.taskfromme

import com.zstronics.ceibro.base.interfaces.IBase

interface ITaskFromMe {
    interface State : IBase.State {
    }

    interface ViewModel : IBase.ViewModel<State> {
    }
}