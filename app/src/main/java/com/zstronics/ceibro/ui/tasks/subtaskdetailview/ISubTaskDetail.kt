package com.zstronics.ceibro.ui.tasks.subtaskdetailview

import com.zstronics.ceibro.base.interfaces.IBase

interface ISubTaskDetail {
    interface State : IBase.State {
    }

    interface ViewModel : IBase.ViewModel<State> {
        fun getTaskById(taskId: String)
    }
}