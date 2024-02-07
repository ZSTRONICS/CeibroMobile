package com.zstronics.ceibro.ui.groupsv2

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.state.BaseState
import javax.inject.Inject

class GroupV2State @Inject constructor() : BaseState(), IGroupV2.State {


    override var setAddTaskButtonVisibility: MutableLiveData<Boolean> = MutableLiveData(true)
}