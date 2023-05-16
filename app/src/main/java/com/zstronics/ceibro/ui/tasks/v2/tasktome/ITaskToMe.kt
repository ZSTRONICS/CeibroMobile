package com.zstronics.ceibro.ui.tasks.v2.tasktome

import com.zstronics.ceibro.base.interfaces.IBase

interface ITaskToMe {
    interface State : IBase.State {
    }

    interface ViewModel : IBase.ViewModel<State> {
    }
}