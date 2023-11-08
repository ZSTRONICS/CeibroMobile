package com.zstronics.ceibro.ui.projectv2.locationv2

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.state.BaseState
import javax.inject.Inject

class LocationStateV2 @Inject constructor() : BaseState(), ILocationV2.State {
    override var projectName: MutableLiveData<String> = MutableLiveData("Project List")
    override var projectDescription: MutableLiveData<String> = MutableLiveData("")
}