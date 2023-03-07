package com.zstronics.ceibro.ui.projects.newproject.overview

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

}