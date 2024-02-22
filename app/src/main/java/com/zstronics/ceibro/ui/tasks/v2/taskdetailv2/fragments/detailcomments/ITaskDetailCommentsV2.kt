package com.zstronics.ceibro.ui.tasks.v2.taskdetailv2.fragments.detailcomments

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.interfaces.IBase

interface ITaskDetailCommentsV2 {
    interface State : IBase.State {
        var comment: MutableLiveData<String>
    }

    interface ViewModel : IBase.ViewModel<State> {
    }
}