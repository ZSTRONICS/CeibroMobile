package com.zstronics.ceibro.ui.locationv2

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.interfaces.IBase

interface ILocations {
    interface State : IBase.State {
        var isFilterVisible: MutableLiveData<Boolean>

    }

    interface ViewModel : IBase.ViewModel<State> {
    }
}