package com.zstronics.ceibro.ui.projects.newproject.overview

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.repos.dashboard.DashboardRepository
import com.zstronics.ceibro.data.repos.dashboard.connections.MyConnection
import com.zstronics.ceibro.data.repos.projects.IProjectRepository
import com.zstronics.ceibro.data.repos.projects.createNewProject.CreateProjectRequest
import com.zstronics.ceibro.data.sessions.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProjectOverviewVM @Inject constructor(
    override val viewState: ProjectOverviewState,
    val sessionManager: SessionManager,
    private val projectRepository: IProjectRepository,
    private val dashboardRepository: DashboardRepository
) : HiltBaseViewModel<IProjectOverview.State>(), IProjectOverview.ViewModel {
    private val _projectStatuses: MutableLiveData<ArrayList<ProjectStatus>> =
        MutableLiveData(arrayListOf())
    val projectStatuses: LiveData<ArrayList<ProjectStatus>> = _projectStatuses

    private val _allConnections: MutableLiveData<ArrayList<MyConnection>> =
        MutableLiveData(arrayListOf())
    val allConnections: LiveData<ArrayList<MyConnection>> = _allConnections

    private val _owners: MutableLiveData<ArrayList<String>> = MutableLiveData(arrayListOf())
    val owners: LiveData<ArrayList<String>> = _owners

    fun deleteStatus(position: Int) {
        val oldStatusList = projectStatuses.value
        oldStatusList?.removeAt(position)
        oldStatusList?.let {
            _projectStatuses.value = it
        }
    }

    fun addStatus(status: String) {
        val oldStatusList = projectStatuses.value
        oldStatusList?.add(ProjectStatus(status))
        oldStatusList?.let {
            _projectStatuses.value = it
        }
    }

    fun addOrRemoveOwner(ownerId: String) {
        val oldOwners = owners.value
        val ownerExist = oldOwners?.find { it == ownerId }

        if (ownerExist == null) {
            oldOwners?.add(ownerId)
        } else {
            oldOwners.remove(ownerId)
        }
        oldOwners?.let {
            _owners.value = it
        }
    }

    override fun onFirsTimeUiCreate(bundle: Bundle?) {
        super.onFirsTimeUiCreate(bundle)
        loadConnections()
    }

    private fun loadConnections() {
        launch {
            loading(true)
            when (val response = dashboardRepository.getAllConnections()) {
                is ApiResponse.Success -> {
                    loading(false)
                    val data = response.data
                    _allConnections.postValue(data.myConnections as ArrayList<MyConnection>?)
                }

                is ApiResponse.Error -> {
                    loading(false)
                }
            }
        }
    }

    fun createProject() {
        //// api call create project
        launch {
            val request = projectStatuses.value?.map { it.status }?.let {
                owners.value?.let { it1 ->
                    CreateProjectRequest(
                        projectPhoto = viewState.projectPhoto.value.toString(),
                        title = viewState.projectTitle.value.toString(),
                        location = viewState.location.value.toString(),
                        description = viewState.description.value.toString(),
                        dueDate = viewState.dueDate.value.toString(),
                        publishStatus = viewState.status.value.toString(),
                        extraStatus = it,
                        owners = it1
                    )
                }
            }
            loading(true)
            when (val response = request?.let { projectRepository.createProject(it) }) {
                is ApiResponse.Success -> {
                    loading(false, "Project created")
                }
                is ApiResponse.Error -> {
                    loading(false, response.error.message)
                }
                else -> loading(false, "")
            }
        }
    }

    data class ProjectStatus(val status: String)
}