package com.zstronics.ceibro.ui.projects.newproject

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.repos.chat.room.Member
import com.zstronics.ceibro.data.repos.dashboard.DashboardRepository
import com.zstronics.ceibro.data.repos.dashboard.connections.MyConnection
import com.zstronics.ceibro.data.repos.projects.IProjectRepository
import com.zstronics.ceibro.data.repos.projects.projectsmain.AllProjectsResponse
import com.zstronics.ceibro.data.sessions.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CreateProjectMainVM @Inject constructor(
    override val viewState: CreateProjectMainState,
    val sessionManager: SessionManager,
    private val projectRepository: IProjectRepository,
    private val dashboardRepository: DashboardRepository
) : HiltBaseViewModel<ICreateProjectMain.State>(), ICreateProjectMain.ViewModel {

    private val _allConnections: MutableLiveData<ArrayList<MyConnection>> =
        MutableLiveData(arrayListOf())
    val allConnections: LiveData<ArrayList<MyConnection>> = _allConnections

    private val _availableMembers: MutableLiveData<List<Member>> =
        MutableLiveData(arrayListOf())
    val availableMembers: LiveData<List<Member>> = _availableMembers

    override fun onFirsTimeUiCreate(bundle: Bundle?) {
        super.onFirsTimeUiCreate(bundle)
        bundle?.let {
            val project =
                it.getParcelable<AllProjectsResponse.Projects>(AllProjectsResponse.Projects::class.java.name)
            if (project != null)
                onProjectCreated(project)
        }
        loadConnections()
    }

    private fun loadConnections() {
        launch {
            when (val response = dashboardRepository.getAllConnections()) {
                is ApiResponse.Success -> {
                    val data = response.data
                    _allConnections.postValue(data.myConnections as ArrayList<MyConnection>?)
                }

                is ApiResponse.Error -> {
                    alert(response.error.message)
                }
            }
        }
    }

    private fun getAvailableMembers(projectId: String) {
        launch {
            when (val response = projectRepository.getAvailableMembers(projectId)) {
                is ApiResponse.Success -> {
                    val data = response.data
                    _availableMembers.postValue(data.members)
                }

                is ApiResponse.Error -> {
                    alert(response.error.message)
                }
            }
        }
    }

    fun onProjectCreated(project: AllProjectsResponse.Projects?) {
        viewState.isProjectCreated.value = true
        viewState.project.value = project
        getAvailableMembers(project?.id ?: "")
    }

    fun updateMembersList() {
        getAvailableMembers(viewState.project.value?.id ?: "")
    }
}