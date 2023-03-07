package com.zstronics.ceibro.ui.projects.newproject.overview

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.interfaces.IBase

interface IProjectOverview {
    interface State : IBase.State {
        val dueDate: MutableLiveData<String>
        val status: MutableLiveData<String>
        val projectTitle: MutableLiveData<String>
        val location: MutableLiveData<String>
        val description: MutableLiveData<String>
        val projectOwners: MutableLiveData<ArrayList<String>>
        var projectPhoto: MutableLiveData<String>
    }

    interface ViewModel : IBase.ViewModel<State> {
    }
}