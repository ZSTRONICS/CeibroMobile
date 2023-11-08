package com.zstronics.ceibro.ui.projectv2.newprojectv2

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.state.BaseState
import javax.inject.Inject


class NewProjectStateV2 @Inject constructor() : BaseState(), INewProjectV2.State {
    override var projectName: MutableLiveData<String> = MutableLiveData("")
    override var projectDescription: MutableLiveData<String> = MutableLiveData("")
}