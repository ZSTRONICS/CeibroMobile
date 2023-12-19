package com.zstronics.ceibro.ui.locationv2.locationdrawing

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.interfaces.IBase

interface ILocationDrawingV2 {
    interface State : IBase.State {
        var isFilterVisible: MutableLiveData<Boolean>


    }

    interface ViewModel : IBase.ViewModel<State> {
    }
}