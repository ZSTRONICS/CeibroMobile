package com.zstronics.ceibro.ui.projectv2.hiddenprojectv2

import com.zstronics.ceibro.ui.projectv2.IProjectsV2


import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.state.BaseState
import javax.inject.Inject


class HiddenProjectsV2State @Inject constructor() : BaseState(), IHiddenProjectV2.State {
    override var projectName: MutableLiveData<String> = MutableLiveData("")
    override var projectDescription: MutableLiveData<String> = MutableLiveData("")
    override var containProject: MutableLiveData<Boolean> = MutableLiveData(false)

}