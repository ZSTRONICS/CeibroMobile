package com.zstronics.ceibro.ui.tasks.v2.taskdetail

import com.zstronics.ceibro.base.interfaces.IBase

interface ITaskDetailV2 {
    interface State : IBase.State {
    }

    interface ViewModel : IBase.ViewModel<State> {
    }
}