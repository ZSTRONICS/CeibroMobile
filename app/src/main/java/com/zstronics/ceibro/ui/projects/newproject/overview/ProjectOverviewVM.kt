package com.zstronics.ceibro.ui.projects.newproject.overview

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.repos.dashboard.DashboardRepository
import com.zstronics.ceibro.data.repos.dashboard.connections.MyConnection
import com.zstronics.ceibro.data.repos.projects.IProjectRepository
import com.zstronics.ceibro.data.repos.projects.createNewProject.CreateNewProjectResponse
import com.zstronics.ceibro.data.repos.projects.createNewProject.CreateProjectRequest
import com.zstronics.ceibro.data.repos.projects.createNewProject.JsonWithDataObject
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
    private val dashboardRepository: DashboardRepository
) : HiltBaseViewModel<IProjectOverview.State>(), IProjectOverview.ViewModel {
    val user = sessionManager.getUser().value

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

        /// DO not add same status value
        val oldStatusList = projectStatuses.value
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
        loadConnections()
        user?.id?.let { addOrRemoveOwner(it) }
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

    fun createProject(context: Context, projectStateHandler: ProjectStateHandler) {
        //// api call create project
        launch {
            val request = projectStatuses.value?.map { it.status }?.let { statusList ->
                owners.value?.let { owners ->
                    CreateProjectRequest(
                        projectPhoto = FileUtils.getFileAsBase64(
                            FileUtils.getFile(
                                context,
                                viewState.projectPhoto.value
                            )
                        ),
                        title = viewState.projectTitle.value.toString(),
                        location = viewState.location.value.toString(),
                        description = viewState.description.value.toString(),
                        dueDate = viewState.dueDate.value.toString(),
                        publishStatus = viewState.status.value.toString(),
                        extraStatus = JsonWithDataObject(statusList),
                        owner = JsonWithDataObject(owners)
                    )
                }
            }

            loading(true)
            when (val response = request?.let { projectRepository.createProject(it) }) {
                is ApiResponse.Success -> {
                    viewState.project.postValue(response.data.createProject)
                    projectStateHandler.onProjectCreated(response.data.createProject)
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

    fun getMockedProject(): CreateNewProjectResponse? {
        return Gson().fromJson(
            "{\n" +
                    "                        \"createProject\": {\n" +
                    "                        \"owner\": [\n" +
                    "                        \"63ace21de108b7fac50ad335\",\n" +
                    "                        \"63b29684e108b7fac50adc4a\",\n" +
                    "                        \"63b42db1adc12f7c2980d7d3\"\n" +
                    "                        ],\n" +
                    "                        \"isDefault\": false,\n" +
                    "                        \"usersCount\": 0,\n" +
                    "                        \"docsCount\": 0,\n" +
                    "                        \"tasksCount\": 0,\n" +
                    "                        \"chatCount\": 0,\n" +
                    "                        \"publishStatus\": \"In Sales\",\n" +
                    "                        \"extraStatus\": [\n" +
                    "                        \"Ongoing\",\n" +
                    "                        \"Testing\",\n" +
                    "                        \"checking\"\n" +
                    "                        ],\n" +
                    "                        \"inDraftState\": false,\n" +
                    "                        \"_id\": \"6406e70db4fa0d534825ee85\",\n" +
                    "                        \"title\": \"Testing\",\n" +
                    "                        \"location\": \"location\",\n" +
                    "                        \"description\": \"description\",\n" +
                    "                        \"dueDate\": \"2023-03-02T19:00:00.000Z\",\n" +
                    "                        \"projectPhoto\": \"https://ceibrolive.s3.eu-north-1.amazonaws.com/projects/IMG-20221130\",\n" +
                    "                        \"createdAt\": \"2023-03-07T07:26:05.618Z\",\n" +
                    "                        \"updatedAt\": \"2023-03-07T07:26:05.618Z\"\n" +
                    "                    }\n" +
                    "                    }", CreateNewProjectResponse::class.java
        )
    }
}