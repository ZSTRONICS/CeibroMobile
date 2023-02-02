package com.zstronics.ceibro.ui.tasks.subtaskrejections

import com.zstronics.ceibro.base.interfaces.IBase

interface ISubTaskRejection {
    interface State : IBase.State {
    }

    interface ViewModel : IBase.ViewModel<State> {
    }
}