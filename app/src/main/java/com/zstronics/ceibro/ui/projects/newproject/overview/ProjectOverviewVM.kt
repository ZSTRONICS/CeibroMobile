package com.zstronics.ceibro.ui.projects.newproject.overview

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.repos.projects.IProjectRepository
import com.zstronics.ceibro.data.sessions.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProjectOverviewVM @Inject constructor(
    override val viewState: ProjectOverviewState,
    val sessionManager: SessionManager,
    private val projectRepository: IProjectRepository
) : HiltBaseViewModel<IProjectOverview.State>(), IProjectOverview.ViewModel {
    private val _projectStatuses: MutableLiveData<List<ProjectStatus>> = MutableLiveData()
    val projectStatuses: LiveData<List<ProjectStatus>> = _projectStatuses

    init {
        _projectStatuses.value = arrayListOf(
            ProjectStatus("Ongoing"),
            ProjectStatus("Approved"),
            ProjectStatus("In Sale")
        )
    }

    data class ProjectStatus(val status: String)
}