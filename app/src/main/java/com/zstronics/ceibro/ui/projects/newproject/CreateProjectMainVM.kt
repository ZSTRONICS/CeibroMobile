package com.zstronics.ceibro.ui.projects.newproject

import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.repos.projects.IProjectRepository
import com.zstronics.ceibro.data.sessions.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CreateProjectMainVM @Inject constructor(
    override val viewState: CreateProjectMainState,
    val sessionManager: SessionManager,
    private val projectRepository: IProjectRepository
) : HiltBaseViewModel<ICreateProjectMain.State>(), ICreateProjectMain.ViewModel {

}