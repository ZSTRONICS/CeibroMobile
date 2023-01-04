package com.zstronics.ceibro.ui.tasks

import com.zstronics.ceibro.base.interfaces.IBase

interface IMainTasks {
    interface State : IBase.State {
    }

    interface ViewModel : IBase.ViewModel<State> {
    }
}