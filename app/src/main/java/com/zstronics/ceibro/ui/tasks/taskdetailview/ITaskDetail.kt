package com.zstronics.ceibro.ui.tasks.taskdetailview

import com.zstronics.ceibro.base.interfaces.IBase

interface ITaskDetail {
    interface State : IBase.State {
    }

    interface ViewModel : IBase.ViewModel<State> {
        fun getSubTasks(taskId: String)
    }
}