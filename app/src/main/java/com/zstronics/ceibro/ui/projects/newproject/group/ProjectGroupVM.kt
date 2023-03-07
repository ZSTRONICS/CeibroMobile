package com.zstronics.ceibro.ui.projects.newproject.group

import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.repos.projects.IProjectRepository
import com.zstronics.ceibro.data.sessions.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProjectGroupVM @Inject constructor(
    override val viewState: ProjectGroupState,
    val sessionManager: SessionManager,
    private val projectRepository: IProjectRepository
) : HiltBaseViewModel<IProjectGroup.State>(), IProjectGroup.ViewModel {

}