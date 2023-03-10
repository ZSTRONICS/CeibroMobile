package com.zstronics.ceibro.ui.projects.newproject

import android.os.Bundle
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.repos.projects.IProjectRepository
import com.zstronics.ceibro.data.repos.projects.projectsmain.AllProjectsResponse
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.ui.projects.newproject.overview.ownersheet.ProjectStateHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CreateProjectMainVM @Inject constructor(
    override val viewState: CreateProjectMainState,
    val sessionManager: SessionManager,
    private val projectRepository: IProjectRepository
) : HiltBaseViewModel<ICreateProjectMain.State>(), ICreateProjectMain.ViewModel {
    override fun onFirsTimeUiCreate(bundle: Bundle?) {
        super.onFirsTimeUiCreate(bundle)
        bundle?.let {
            val project =
                it.getParcelable<AllProjectsResponse.Projects>(AllProjectsResponse.Projects::class.java.name)
            if (project != null)
                onProjectCreated(project)
        }

    }

    fun onProjectCreated(project: AllProjectsResponse.Projects?) {
        viewState.isProjectCreated.value = true
        viewState.project.value = project
    }
}