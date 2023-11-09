package com.zstronics.ceibro.ui.projectv2

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.interfaces.IBase

interface IProjectsV2 {
    interface State : IBase.State {

        var projectName: MutableLiveData<String>
        var projectDescription: MutableLiveData<String>
        var containProject: MutableLiveData<Boolean>
    }

    interface ViewModel : IBase.ViewModel<State> {
        fun getProjectName(context: Context)
    }
}