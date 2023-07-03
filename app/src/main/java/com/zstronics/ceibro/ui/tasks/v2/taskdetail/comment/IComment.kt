package com.zstronics.ceibro.ui.tasks.v2.taskdetail.comment

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.interfaces.IBase

interface IComment {
    interface State : IBase.State {
        var comment: MutableLiveData<String>
        val isAttachLayoutOpen: MutableLiveData<Boolean>
    }

    interface ViewModel : IBase.ViewModel<State> {
    }
}