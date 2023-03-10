package com.zstronics.ceibro.ui.projects.newproject.overview

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.interfaces.IBase
import com.zstronics.ceibro.data.repos.projects.createNewProject.CreateNewProjectResponse

interface IProjectOverview {
    interface State : IBase.State {
        val dueDate: MutableLiveData<String>
        val status: MutableLiveData<String>
        val projectTitle: MutableLiveData<String>
        val location: MutableLiveData<String>
        val description: MutableLiveData<String>
        val projectOwners: MutableLiveData<ArrayList<String>>
        var projectPhoto: MutableLiveData<Uri>
        val project: MutableLiveData<CreateNewProjectResponse.CreateProject>
    }

    interface ViewModel : IBase.ViewModel<State> {
    }
}