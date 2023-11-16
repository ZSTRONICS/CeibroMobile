package com.zstronics.ceibro.ui.projectv2.projectdetailv2.projectinfo

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.interfaces.IBase

interface IProjectInfoV2 {
    interface State : IBase.State {
        var projectName: MutableLiveData<String>
        var projectPhoto: MutableLiveData<String>
        var projectDescription: MutableLiveData<String>
        var projectdate: MutableLiveData<String>
        var projectCreator: MutableLiveData<String>
        var projectImagegUrl: MutableLiveData<String>

    }

    interface ViewModel : IBase.ViewModel<State> {
    }
}