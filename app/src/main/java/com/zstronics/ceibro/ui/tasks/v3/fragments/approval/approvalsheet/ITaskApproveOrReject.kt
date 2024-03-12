package com.zstronics.ceibro.ui.tasks.v3.fragments.approval.approvalsheet

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.interfaces.IBase

interface ITaskApproveOrReject {
    interface State : IBase.State {
        var comment: MutableLiveData<String>
        var taskTitle: MutableLiveData<String>
        var title: MutableLiveData<String>
        var description: MutableLiveData<String>
        val isAttachLayoutOpen: MutableLiveData<Boolean>
    }

    interface ViewModel : IBase.ViewModel<State> {
    }
}