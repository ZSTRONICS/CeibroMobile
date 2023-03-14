package com.zstronics.ceibro.ui.projects.newproject

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.state.BaseState
import com.zstronics.ceibro.data.repos.projects.createNewProject.CreateNewProjectResponse
import com.zstronics.ceibro.data.repos.projects.projectsmain.AllProjectsResponse
import javax.inject.Inject

class CreateProjectMainState @Inject constructor() : BaseState(), ICreateProjectMain.State {
    override val isProjectCreated: MutableLiveData<Boolean> = MutableLiveData(false)
    override val project: MutableLiveData<AllProjectsResponse.Projects> =
        MutableLiveData()
    override val selectedTabId: MutableLiveData<String> =
        MutableLiveData(CreateProjectMainFragment.CreateProjectTabs.Overview.name)
}