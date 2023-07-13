package com.zstronics.ceibro.ui.tasks.v2.hidden_tasks

import com.zstronics.ceibro.base.interfaces.IBase

interface ITaskHidden {
    interface State : IBase.State

    interface ViewModel : IBase.ViewModel<State>
}