package com.zstronics.ceibro.ui.projects.newproject.overview

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.repos.chat.room.Member
import com.zstronics.ceibro.data.repos.projects.IProjectRepository
import com.zstronics.ceibro.data.repos.projects.createNewProject.CreateProjectRequest
import com.zstronics.ceibro.data.repos.projects.projectsmain.AllProjectsResponse
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.ui.projects.newproject.overview.ownersheet.ProjectStateHandler
import com.zstronics.ceibro.ui.socket.LocalEvents
import com.zstronics.ceibro.utils.FileUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
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

    private val _ownersMemberList: MutableLiveData<ArrayList<Member>> =
        MutableLiveData(arrayListOf())
    val ownersMemberList: LiveData<ArrayList<Member>> = _ownersMemberList
    var project: AllProjectsResponse.Projects? = null


    private val _updatedProject: MutableLiveData<AllProjectsResponse.Projects> = MutableLiveData()
    val updatedProject: LiveData<AllProjectsResponse.Projects> = _updatedProject
    init {
        EventBus.getDefault().register(this)
    }

    override fun onFirsTimeUiCreate(bundle: Bundle?) {
        super.onFirsTimeUiCreate(bundle)
        user?.id?.let {
            val member = Member(
                id = it,
                firstName = user.firstName,
                surName = user.surName,
                companyName = user.companyName,
                profilePic = user.profilePic
            )
            addOrRemoveOwner(member)
        }
    }

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

    fun addOrRemoveOwner(member: Member) {
        val ownerId = member.id
        val oldOwners = owners.value
        val ownerExist = oldOwners?.find { it == ownerId }

        if (ownerExist == null) {
            oldOwners?.add(ownerId)
        } else {
            if (!isCreator(ownerId)) {
                oldOwners.remove(ownerId)
            }
        }
        oldOwners?.let {
            _owners.value = it
        }


        /// set chips selection
        val ownersMemberList = ownersMemberList.value
        val ownersMemberExits = ownersMemberList?.find { it.id == ownerId }

        if (ownersMemberExits == null) {
            ownersMemberList?.add(member)
        } else {
            if (!isCreator(ownerId)) {
                ownersMemberList.remove(ownersMemberExits)
            }
        }
        ownersMemberList?.let {
            _ownersMemberList.value = it
        }
    }

    fun removeOwner(member: Member) {
        val ownersMemberList = ownersMemberList.value
        val oldOwners = owners.value
        if (!isCreator(member.id)) {
            ownersMemberList?.remove(member)
            oldOwners?.remove(member.id)
        } else {
            alert("The project creator cannot be removed")
        }
        ownersMemberList?.let {
            _ownersMemberList.value = it
        }

        oldOwners?.let {
            _owners.value = it
        }
    }

    private fun isCreator(id: String): Boolean {
        val creatorId = if (viewState.projectCreated.value == true) {
            project?.creator?.id.toString()
        } else user?.id.toString()
        return id == creatorId
    }

    fun createProject(context: Context, projectStateHandler: ProjectStateHandler) {
        //// api call create project
        launch {
            val request = projectStatuses.value?.map { it.status }?.let { statusList ->
                owners.value?.let { owners ->
                    CreateProjectRequest(
                        projectPhoto = if (viewState.photoAttached) FileUtils.getFile(
                            context,
                            viewState.projectPhoto.value
                        ) else null,
                        title = viewState.projectTitle.value ?: "",
                        location = viewState.location.value ?: "",
                        description = viewState.description.value ?: "",
                        dueDate = viewState.dueDate.value ?: "",
                        publishStatus = viewState.status.value ?: "",
                        extraStatus = if (statusList.isNotEmpty()) Gson().toJson(statusList) else "",
                        owner = if (owners.isNotEmpty()) Gson().toJson(owners) else ""
                    )
                }
            }

            loading(true)
            when (val response = request?.let { projectRepository.createProject(it) }) {
                is ApiResponse.Success -> {
                    viewState.projectCreated.postValue(true)
                    project = response.data.createProject
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

    fun updateProject(context: Context, projectStateHandler: ProjectStateHandler) {
        //// api call create project
        launch {
            val request = projectStatuses.value?.map { it.status }?.let { statusList ->
                owners.value?.let { owners ->

                    CreateProjectRequest(
                        projectPhoto = if (viewState.photoAttached) FileUtils.getFile(
                            context,
                            viewState.projectPhoto.value
                        ) else null,
                        title = viewState.projectTitle.value ?: "",
                        location = viewState.location.value ?: "",
                        description = viewState.description.value ?: "",
                        dueDate = viewState.dueDate.value ?: "",
                        publishStatus = viewState.status.value ?: "",
                        extraStatus = if (statusList.isNotEmpty()) Gson().toJson(statusList) else "",
                        owner = if (owners.isNotEmpty()) Gson().toJson(owners) else ""
                    )
                }
            }

            loading(true)
            when (val response =
                request?.let { projectRepository.updateProject(it, project?.id.toString()) }) {
                is ApiResponse.Success -> {
                    viewState.projectCreated.postValue(true)
                    projectStateHandler.onProjectCreated(response.data.updatedProject)
                    loading(false, "Update successful")
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

    fun preSelectMemberChip() {
        project?.owner?.let {
            val selectedMembers = it.map { owner ->
                Member(
                    id = owner.id,
                    firstName = owner.firstName,
                    surName = owner.surName,
                    companyName = owner.companyName,
                    profilePic = owner.profilePic
                )
            }
            _ownersMemberList.postValue(selectedMembers as ArrayList<Member>?)
        }
    }

    data class ProjectStatus(val status: String)




    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onProjectCreatedEvent(event: LocalEvents.ProjectCreatedEvent?) {
        val oldProjects = project
        if (oldProjects?.id == event?.newProject?.id) {
            _updatedProject.postValue(event?.newProject)
        }
    }

    override fun onCleared() {
        super.onCleared()
        EventBus.getDefault().unregister(this)
    }
}