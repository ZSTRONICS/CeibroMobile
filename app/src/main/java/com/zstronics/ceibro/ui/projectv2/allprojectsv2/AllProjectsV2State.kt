package com.zstronics.ceibro.ui.projectv2.allprojectsv2

import com.zstronics.ceibro.ui.projectv2.IProjectsV2


import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.state.BaseState
import javax.inject.Inject


class AllProjectsV2State @Inject constructor() : BaseState(), IAllProjectV2.State {
    override var projectName: MutableLiveData<String> = MutableLiveData("Project List")
    override var projectDescription: MutableLiveData<String> = MutableLiveData("")
    override var containProject: MutableLiveData<Boolean> = MutableLiveData(false)

}