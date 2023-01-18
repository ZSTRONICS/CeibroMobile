package com.zstronics.ceibro.ui.tasks.task

import com.zstronics.ceibro.base.interfaces.IBase

interface ITasks {
    interface State : IBase.State {
    }

    interface ViewModel : IBase.ViewModel<State> {
        fun getTasks()
    }
}