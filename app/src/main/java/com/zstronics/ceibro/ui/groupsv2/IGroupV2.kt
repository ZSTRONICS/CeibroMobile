package com.zstronics.ceibro.ui.groupsv2

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.interfaces.IBase

interface IGroupV2 {
    interface State : IBase.State {

        val setAddTaskButtonVisibility: MutableLiveData<Boolean>
    }

    interface ViewModel : IBase.ViewModel<State> {
    }
}