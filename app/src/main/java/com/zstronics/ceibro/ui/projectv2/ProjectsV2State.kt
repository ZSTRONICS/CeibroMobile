package com.zstronics.ceibro.ui.projectv2

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.state.BaseState
import javax.inject.Inject


class ProjectsV2State @Inject constructor() : BaseState(), IProjectsV2.State {
    override var projectName: MutableLiveData<String> = MutableLiveData("")
    override var projectDescription: MutableLiveData<String> = MutableLiveData("")
    override var searchProjectText: MutableLiveData<String> = MutableLiveData("")
    override var containProject: MutableLiveData<Boolean> = MutableLiveData(false)

}