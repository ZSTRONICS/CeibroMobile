package com.zstronics.ceibro.ui.tasks.newsubtask

import com.zstronics.ceibro.base.interfaces.IBase

interface INewSubTask {
    interface State : IBase.State {
        var dueDate: String
        var startDate: String
    }

    interface ViewModel : IBase.ViewModel<State> {
    }
}