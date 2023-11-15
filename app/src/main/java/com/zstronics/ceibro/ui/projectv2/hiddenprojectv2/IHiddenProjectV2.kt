package com.zstronics.ceibro.ui.projectv2.hiddenprojectv2

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.interfaces.IBase
import com.zstronics.ceibro.data.database.models.projects.CeibroProjectV2


interface IHiddenProjectV2 {
    interface State : IBase.State {

        var projectName: MutableLiveData<String>
        var projectDescription: MutableLiveData<String>
        var containProject: MutableLiveData<Boolean>
    }

    interface ViewModel : IBase.ViewModel<State> {
        fun getProjectName(context: Context)
        fun getHiddenProjects()
        fun hideProject( hidden: Boolean, projectId: String,callBack: (isSuccess: Boolean) -> Unit)
    }
}