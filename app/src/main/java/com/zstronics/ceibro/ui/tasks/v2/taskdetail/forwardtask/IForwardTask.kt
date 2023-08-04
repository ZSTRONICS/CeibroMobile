package com.zstronics.ceibro.ui.tasks.v2.taskdetail.forwardtask

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.interfaces.IBase

interface IForwardTask {
    interface State : IBase.State {
        var comment: MutableLiveData<String>
        var forwardToText: MutableLiveData<String>
        val isAttachLayoutOpen: MutableLiveData<Boolean>
    }

    interface ViewModel : IBase.ViewModel<State> {
    }
}