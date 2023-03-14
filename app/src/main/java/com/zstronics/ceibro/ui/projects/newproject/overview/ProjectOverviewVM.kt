package com.zstronics.ceibro.ui.projects.newproject.overview

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.repos.projects.IProjectRepository
import com.zstronics.ceibro.data.repos.projects.createNewProject.CreateProjectRequest
import com.zstronics.ceibro.data.repos.projects.projectsmain.AllProjectsResponse
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.ui.projects.newproject.overview.ownersheet.ProjectStateHandler
import com.zstronics.ceibro.utils.FileUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProjectOverviewVM @Inject constructor(
    override val viewState: ProjectOverviewState,
    val sessionManager: SessionManager,
    private val projectRepository: IProjectRepository,
) : HiltBaseViewModel<IProjectOverview.State>(), IProjectOverview.ViewModel {
    val user = sessionManager.getUser().value

    private val _projectStatuses: MutableLiveData<ArrayList<ProjectStatus>> =
        MutableLiveData(arrayListOf())
    val projectStatuses: LiveData<ArrayList<ProjectStatus>> = _projectStatuses

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
        val statusExist = oldStatusList?.find { it.status == status }
        if (statusExist != null) {
            alert("Duplicate status")
            return
        }
        oldStatusList?.add(ProjectStatus(status))
        oldStatusList?.let {
            _projectStatuses.value = it
        }
    }

    fun updateStatus(position: Int, status: String) {
        val oldStatusList = projectStatuses.value
        oldStatusList?.removeAt(position)
        oldStatusList?.add(position, ProjectStatus(status))
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
            if (ownerId != user?.id) {
                oldOwners.remove(ownerId)
            }
        }
        oldOwners?.let {
            _owners.value = it
        }
    }

    override fun onFirsTimeUiCreate(bundle: Bundle?) {
        super.onFirsTimeUiCreate(bundle)
        user?.id?.let { addOrRemoveOwner(it) }
    }

    fun createProject(context: Context, projectStateHandler: ProjectStateHandler) {
        //// api call create project
        launch {
            val request = projectStatuses.value?.map { it.status }?.let { statusList ->
                owners.value?.let { owners ->
                    CreateProjectRequest(
                        projectPhoto = FileUtils.getFile(
                            context,
                            viewState.projectPhoto.value
                        ),
                        title = viewState.projectTitle.value.toString(),
                        location = viewState.location.value.toString(),
                        description = viewState.description.value.toString(),
                        dueDate = viewState.dueDate.value.toString(),
                        publishStatus = viewState.status.value.toString(),
                        extraStatus = Gson().toJson(statusList),
                        owner = Gson().toJson(owners)
                    )
                }
            }

            loading(true)
            when (val response = request?.let { projectRepository.createProject(it) }) {
                is ApiResponse.Success -> {
                    viewState.projectCreated.postValue(true)
                    projectStateHandler.onProjectCreated(response.data.createProject)
                    loading(false, "")
                }
                is ApiResponse.Error -> {
                    loading(false, response.error.message)
                }
                else -> loading(false, "")
            }
        }
    }

    fun addAllStatus(extraStatus: List<String>) {
        val statusList = extraStatus.map { ProjectStatus(it) } as ArrayList<ProjectStatus>
        _projectStatuses.value = statusList
    }

    fun setSelectedOwners(owner: List<AllProjectsResponse.Projects.Owner>) {
        _owners.postValue(owner.map { it.id } as ArrayList<String>?)
    }

    data class ProjectStatus(val status: String)
}