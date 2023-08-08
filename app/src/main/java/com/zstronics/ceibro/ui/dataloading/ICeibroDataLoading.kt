package com.zstronics.ceibro.ui.dataloading

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.interfaces.IBase

interface ICeibroDataLoading {
    interface State : IBase.State {
        val syncProgress: MutableLiveData<Int>
    }

    interface ViewModel : IBase.ViewModel<State> {
    }
}