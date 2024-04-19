package com.zstronics.ceibro.ui.groupsv2.fragment

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.state.BaseState
import javax.inject.Inject

class GroupAssigneeState @Inject constructor() : BaseState(), IGroupAssignee.State {
    override val isSelfAssigned: MutableLiveData<Boolean> = MutableLiveData(false)
}