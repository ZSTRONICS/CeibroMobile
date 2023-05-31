package com.zstronics.ceibro.ui.tasks.v2.newtask.assignee

import com.zstronics.ceibro.base.interfaces.IBase

interface IAssignee {
    interface State : IBase.State {
    }

    interface ViewModel : IBase.ViewModel<State> {
    }
}