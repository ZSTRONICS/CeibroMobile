package com.zstronics.ceibro.ui.dashboard.myconnectionsv2

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.interfaces.IBase

interface IMyConnectionV2 {
    interface State : IBase.State {
        var isAutoSyncEnabled: MutableLiveData<Boolean>
        var searchName: MutableLiveData<String>
    }

    interface ViewModel : IBase.ViewModel<State> {
    }
}