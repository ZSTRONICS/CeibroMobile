package com.zstronics.ceibro.ui.projectv2.locationv2

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.interfaces.IBase


interface ILocationV2 {
    interface State : IBase.State {
        var projectName: MutableLiveData<String>
        var projectDescription: MutableLiveData<String>
    }
    interface ViewModel : IBase.ViewModel<State> {

        fun getProjectName(context: Context)
    }
}