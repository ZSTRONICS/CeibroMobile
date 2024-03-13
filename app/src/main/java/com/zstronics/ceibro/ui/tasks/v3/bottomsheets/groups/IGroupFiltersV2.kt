package com.zstronics.ceibro.ui.tasks.v3.bottomsheets.groups

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.interfaces.IBase

interface IGroupFiltersV2 {
    interface State : IBase.State {

        val setAddTaskButtonVisibility: MutableLiveData<Boolean>
    }

    interface ViewModel : IBase.ViewModel<State> {
    }
}
