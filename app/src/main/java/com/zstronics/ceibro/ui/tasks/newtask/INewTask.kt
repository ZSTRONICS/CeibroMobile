package com.zstronics.ceibro.ui.tasks.newtask

import com.zstronics.ceibro.base.interfaces.IBase

interface INewTask {
    interface State : IBase.State {
        var dueDate: String
        var startDate: String
    }

    interface ViewModel : IBase.ViewModel<State> {
    }
}