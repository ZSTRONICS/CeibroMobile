package com.zstronics.ceibro.ui.tasks.v2.taskdetail.forwardtask

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.state.BaseState
import javax.inject.Inject

class ForwardTaskState @Inject constructor() : BaseState(), IForwardTask.State {
    override var comment: MutableLiveData<String> = MutableLiveData("")
    override var forwardToText: MutableLiveData<String> = MutableLiveData("")
    override val isAttachLayoutOpen: MutableLiveData<Boolean> = MutableLiveData(false)
}