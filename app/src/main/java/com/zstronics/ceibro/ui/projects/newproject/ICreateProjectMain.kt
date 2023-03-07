package com.zstronics.ceibro.ui.projects.newproject

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.interfaces.IBase

interface ICreateProjectMain {
    interface State : IBase.State {
        val isProjectCreated: MutableLiveData<Boolean>
        val selectedTabId: MutableLiveData<String>
    }

    interface ViewModel : IBase.ViewModel<State> {
    }
}