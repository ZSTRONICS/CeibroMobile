package com.zstronics.ceibro.ui.projects.newproject.overview

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.state.BaseState
import com.zstronics.ceibro.data.repos.projects.projectsmain.AllProjectsResponse
import javax.inject.Inject

class ProjectOverviewState @Inject constructor() : BaseState(), IProjectOverview.State {
    override val dueDate: MutableLiveData<String> = MutableLiveData("")
    override val status: MutableLiveData<String> = MutableLiveData("")
    override val projectTitle: MutableLiveData<String> = MutableLiveData("")
    override val location: MutableLiveData<String> = MutableLiveData("")
    override val description: MutableLiveData<String> = MutableLiveData("")
    override val projectOwners: MutableLiveData<ArrayList<String>> = MutableLiveData(arrayListOf())
    override var projectPhoto: MutableLiveData<Uri> = MutableLiveData()
    override val projectCreated: MutableLiveData<Boolean> = MutableLiveData(false)
    override var photoAttached: Boolean = false
}