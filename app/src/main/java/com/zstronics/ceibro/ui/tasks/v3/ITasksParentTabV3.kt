package com.zstronics.ceibro.ui.tasks.v3

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.interfaces.IBase

interface ITasksParentTabV3 {
    interface State : IBase.State {


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