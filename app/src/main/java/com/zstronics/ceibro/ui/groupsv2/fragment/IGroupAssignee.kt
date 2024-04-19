package com.zstronics.ceibro.ui.groupsv2.fragment

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.interfaces.IBase

interface IGroupAssignee {
    interface State : IBase.State {
        val isSelfAssigned: MutableLiveData<Boolean>
    }

    interface ViewModel : IBase.ViewModel<State> {
    }
}