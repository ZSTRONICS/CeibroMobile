package com.zstronics.ceibro.ui.projectv2.projectdetailv2.drawing

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.interfaces.IBase

interface IDrawingV2 {
    interface State : IBase.State {
        var isFilterVisible: MutableLiveData<Boolean>
        var isVisible: MutableLiveData<Boolean>


    }

    interface ViewModel : IBase.ViewModel<State> {
    }
}