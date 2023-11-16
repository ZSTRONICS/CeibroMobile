package com.zstronics.ceibro.ui.projectv2.projectdetailv2.projectinfo

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.state.BaseState
import javax.inject.Inject

class ProjectInfoV2State @Inject constructor() : BaseState(), IProjectInfoV2.State {

    override var projectName: MutableLiveData<String> = MutableLiveData("")
    override var projectPhoto: MutableLiveData<String> = MutableLiveData("")
    override var projectDescription: MutableLiveData<String> = MutableLiveData("")
    override var projectdate: MutableLiveData<String> = MutableLiveData("")
    override var projectCreator: MutableLiveData<String> = MutableLiveData("")
    override var projectImagegUrl: MutableLiveData<String> = MutableLiveData("")

}