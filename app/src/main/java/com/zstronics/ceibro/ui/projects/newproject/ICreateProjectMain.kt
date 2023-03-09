package com.zstronics.ceibro.ui.projects.newproject

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.interfaces.IBase
import com.zstronics.ceibro.data.repos.projects.createNewProject.CreateNewProjectResponse

interface ICreateProjectMain {
    interface State : IBase.State {
        val isProjectCreated: MutableLiveData<Boolean>
        val project: MutableLiveData<CreateNewProjectResponse.CreateProject>
        val selectedTabId: MutableLiveData<String>
    }

    interface ViewModel : IBase.ViewModel<State> {
    }
}