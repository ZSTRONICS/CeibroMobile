package com.zstronics.ceibro.ui.tasks.v2.taskdetail.comment

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.state.BaseState
import javax.inject.Inject

class CommentState @Inject constructor() : BaseState(), IComment.State {
    override var comment: MutableLiveData<String> = MutableLiveData("")
    override val isAttachLayoutOpen: MutableLiveData<Boolean> = MutableLiveData(false)
}