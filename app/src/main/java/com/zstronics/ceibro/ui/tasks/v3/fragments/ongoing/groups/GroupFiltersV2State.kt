package com.zstronics.ceibro.ui.tasks.v3.fragments.ongoing.groups

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.state.BaseState
import javax.inject.Inject


class GroupFiltersV2State @Inject constructor() : BaseState(), IGroupFiltersV2.State {

    override var setAddTaskButtonVisibility: MutableLiveData<Boolean> = MutableLiveData(true)
}