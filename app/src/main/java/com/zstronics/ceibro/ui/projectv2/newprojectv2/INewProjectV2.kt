package com.zstronics.ceibro.ui.projectv2.newprojectv2

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.interfaces.IBase


interface INewProjectV2 {
    interface State : IBase.State {
        var projectName: MutableLiveData<String>
        var projectPhoto: MutableLiveData<String>
        var projectDescription: MutableLiveData<String>
    }
    interface ViewModel : IBase.ViewModel<State> {

        fun getProjectName(context: Context)
        fun addNewProject(context: Context, callBack: (isSuccess: Boolean) -> Unit)
    }
}