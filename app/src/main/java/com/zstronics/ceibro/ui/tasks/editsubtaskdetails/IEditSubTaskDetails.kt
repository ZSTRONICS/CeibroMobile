package com.zstronics.ceibro.ui.tasks.editsubtaskdetails

import com.zstronics.ceibro.base.interfaces.IBase

interface IEditSubTaskDetails {
    interface State : IBase.State {
    }

    interface ViewModel : IBase.ViewModel<State> {
        fun getSubTaskStatuses(subTaskId: String)
    }
}