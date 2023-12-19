package com.zstronics.ceibro.ui.locationv2.locationproject

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.interfaces.IBase


interface ILocationProjectV2 {
    interface State : IBase.State {

        var projectName: MutableLiveData<String>
        var projectDescription: MutableLiveData<String>
        var containProject: MutableLiveData<Boolean>
    }

    interface ViewModel : IBase.ViewModel<State> {
        fun getProjectName(context: Context)
        fun getAllProjects()
        fun getFavoriteProjects()
        fun getRecentProjects()

        fun hideProject(
            hidden: Boolean,
            projectId: String,
            callBack: (isSuccess: Boolean) -> Unit
        )

        fun updateFavoriteProjectStatus(
            favorite: Boolean,
            projectId: String,
            callBack: (isSuccess: Boolean) -> Unit
        )
    }
}