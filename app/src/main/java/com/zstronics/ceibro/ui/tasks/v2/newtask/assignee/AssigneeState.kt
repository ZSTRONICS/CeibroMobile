package com.zstronics.ceibro.ui.tasks.v2.newtask.assignee

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.state.BaseState
import javax.inject.Inject

class AssigneeState @Inject constructor() : BaseState(), IAssignee.State {
    override val isSelfAssigned: MutableLiveData<Boolean> = MutableLiveData(false)
}