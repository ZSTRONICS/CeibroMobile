package com.zstronics.ceibro.ui.tasks.v2.newtask.assignee

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.interfaces.IBase

interface IAssignee {
    interface State : IBase.State {
        val isSelfAssigned: MutableLiveData<Boolean>
    }

    interface ViewModel : IBase.ViewModel<State> {
    }
}