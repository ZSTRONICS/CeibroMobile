package com.zstronics.ceibro.ui.tasks.v3.fragments.approval.approvalsheet

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.state.BaseState
import javax.inject.Inject

class TaskApproveOrRejectState @Inject constructor() : BaseState(), ITaskApproveOrReject.State {
    override var comment: MutableLiveData<String> = MutableLiveData("")
    override var taskTitle: MutableLiveData<String> = MutableLiveData("")
    override var title: MutableLiveData<String> = MutableLiveData("")
    override var description: MutableLiveData<String> = MutableLiveData("")
    override val isAttachLayoutOpen: MutableLiveData<Boolean> = MutableLiveData(false)
}