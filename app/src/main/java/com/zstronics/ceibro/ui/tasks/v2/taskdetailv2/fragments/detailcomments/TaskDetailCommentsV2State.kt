package com.zstronics.ceibro.ui.tasks.v2.taskdetailv2.fragments.detailcomments

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.state.BaseState
import javax.inject.Inject

class TaskDetailCommentsV2State @Inject constructor() : BaseState(), ITaskDetailCommentsV2.State {
    override var comment: MutableLiveData<String> = MutableLiveData("")
}