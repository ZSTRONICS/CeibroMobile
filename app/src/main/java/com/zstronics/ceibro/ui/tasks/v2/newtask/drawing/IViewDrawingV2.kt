package com.zstronics.ceibro.ui.tasks.v2.newtask.drawing

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.interfaces.IBase

interface IViewDrawingV2 {
    interface State : IBase.State {
        var isFilterVisible: MutableLiveData<Boolean>


        var isToNewClicked: MutableLiveData<Boolean>
        var isToOngoingClicked: MutableLiveData<Boolean>
        var isToDoneClicked: MutableLiveData<Boolean>

        var isFromUnreadClicked: MutableLiveData<Boolean>
        var isFromOngoingClicked: MutableLiveData<Boolean>
        var isFromDoneClicked: MutableLiveData<Boolean>

        var isHiddenOngoingClicked: MutableLiveData<Boolean>
        var isHiddenDoneClicked: MutableLiveData<Boolean>
        var isHiddenCancelled: MutableLiveData<Boolean>

    }

    interface ViewModel : IBase.ViewModel<State> {
    }
}