package com.zstronics.ceibro.ui.tasks.v2.newtask.taskproject

import com.zstronics.ceibro.base.interfaces.IBase

interface ITaskProject {
    interface State : IBase.State {
    }

    interface ViewModel : IBase.ViewModel<State> {
    }
}