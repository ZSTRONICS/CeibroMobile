package com.zstronics.ceibro.ui.tasks.subtask

import com.zstronics.ceibro.base.interfaces.IBase

interface ISubTask {
    interface State : IBase.State {
    }

    interface ViewModel : IBase.ViewModel<State> {
    }
}