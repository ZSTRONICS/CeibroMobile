package com.zstronics.ceibro.ui.dashboard

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.interfaces.IBase

interface IDashboard {
    interface State : IBase.State {
        val selectedItem: MutableLiveData<Int>
    }

    interface ViewModel : IBase.ViewModel<State> {
    }
}