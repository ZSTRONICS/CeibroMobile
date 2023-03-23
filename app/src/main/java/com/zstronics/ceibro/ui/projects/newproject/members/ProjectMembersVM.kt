package com.zstronics.ceibro.ui.projects.newproject.members

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.repos.projects.IProjectRepository
import com.zstronics.ceibro.data.repos.projects.group.ProjectGroup
import com.zstronics.ceibro.data.repos.projects.member.CreateProjectMemberRequest
import com.zstronics.ceibro.data.repos.projects.member.EditProjectMemberRequest
import com.zstronics.ceibro.data.repos.projects.member.GetProjectMemberResponse
import com.zstronics.ceibro.data.repos.projects.role.ProjectRolesResponse
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.ui.projects.newproject.overview.ownersheet.ProjectStateHandler
import com.zstronics.ceibro.ui.socket.LocalEvents
import dagger.hilt.android.lifecycle.HiltViewModel
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject

@HiltViewModel
class ProjectMembersVM @Inject constructor(
    override val viewState: ProjectMembersState,
    val sessionManager: SessionManager,
    private val projectRepository: IProjectRepository
) : HiltBaseViewModel<IProjectMembers.State>(), IProjectMembers.ViewModel {

    private val _groupMembers: MutableLiveData<MutableList<GetProjectMemberResponse.ProjectMember>?> =
        MutableLiveData(arrayListOf())
    val groupMembers: LiveData<MutableList<GetProjectMemberResponse.ProjectMember>?> = _groupMembers

    private val _groups: MutableLiveData<ArrayList<ProjectGroup>?> =
        MutableLiveData(arrayListOf())
    val groups: LiveData<ArrayList<ProjectGroup>?> = _groups

    private val _roles: MutableLiveData<ArrayList<ProjectRolesResponse.ProjectRole>> =
        MutableLiveData(arrayListOf())
    val roles: LiveData<ArrayList<ProjectRolesResponse.ProjectRole>> = _roles

    init {
        EventBus.getDefault().register(this)
    }

    fun getMembers(id: String?) {
        launch {
            when (val response = projectRepository.getProjectMembers(id ?: "")) {
                is ApiResponse.Success -> {
                    val pureMembers = response.data.members.filter { it.user != null }
                    _groupMembers.postValue(pureMembers as MutableList<GetProjectMemberResponse.ProjectMember>)
                }
                is ApiResponse.Error -> {
                    alert(response.error.message)
                }
            }
        }
    }

    fun getGroups(id: String?) {
        launch {
            when (val response = projectRepository.getGroups(id ?: "")) {
                is ApiResponse.Success -> {
                    _groups.postValue(response.data.result as ArrayList<ProjectGroup>?)
                }
                is ApiResponse.Error -> {
                    alert(response.error.message)
                }
            }
        }
    }

    fun getRoles(id: String?) {
        launch {
            when (val response = projectRepository.getRoles(id ?: "")) {
                is ApiResponse.Success -> {
                    _roles.postValue(response.data.roles as ArrayList<ProjectRolesResponse.ProjectRole>?)
                }
                is ApiResponse.Error -> {
                    alert(response.error.message)
                }
            }
        }
    }

    fun createMember(id: String?, body: CreateProjectMemberRequest, success: () -> Unit) {
        launch {
            when (val response = projectRepository.createProjectMember(id ?: "", body)) {
                is ApiResponse.Success -> {
                    getMembers(id)
                    success()
                }
                is ApiResponse.Error -> {
                    alert(response.error.message)
                }
            }
        }
    }

    fun updateMember(projectId: String, memberId: String, groupText: String) {

    }

    fun deleteMember(
        position: Int,
        member: GetProjectMemberResponse.ProjectMember,
        projectStateHandler: ProjectStateHandler
    ) {
        if (!member.isOwner) {
            deleteMemberAPI(position, member.id, projectStateHandler)
        } else {
            alert("Owner cannot be removed")
        }
    }

    private fun deleteMemberAPI(position: Int, id: String, projectStateHandler: ProjectStateHandler) {
        launch {
            when (val response = projectRepository.deleteMember(id)) {
                is ApiResponse.Success -> {
                    val old = groupMembers.value
                    old?.removeAt(position)
                    old?.let {
                        _groupMembers.value = it
                    }
                    alert(response.data.message)
                    projectStateHandler.onMemberDelete()
                }
                is ApiResponse.Error -> {
                    alert(response.error.message)
                }
            }
        }
    }

    fun editMember(
        projectId: String,
        id: String,
        body: EditProjectMemberRequest,
        success: () -> Unit?
    ) {
        launch {
            loading(true)
            when (val response = projectRepository.updateProjectMember(id, body)) {
                is ApiResponse.Success -> {
                    getMembers(projectId)
                    // update local member
                    val updatedMember = response.data.updatedMember
                    val old = groupMembers.value
                    val oldMember = old?.find { it.id == id }
                    if (oldMember != null) {
                        val index = old.indexOf(oldMember)
                        if (index != -1) {
                            old.removeAt(index)
                            old.add(index, updatedMember)
                        }
                    }
                    success()
                    loading(false)
                }
                is ApiResponse.Error -> {
                    loading(false, response.error.message)
                }
            }
        }
    }




    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onProjectMemberAddedEvent(event: LocalEvents.ProjectMemberAddedEvent?) {
        val oldGroupMembers = groupMembers.value
        var projectId = ""

        event?.newMember?.map { member ->
            projectId = member.project
            val selectedGroupMember = oldGroupMembers?.find { it.id == member.id }
            if (member.user != null) {
                if (selectedGroupMember == null) {
                    //it means new member added
                    oldGroupMembers?.add(member)
                } else {
                    //it means an update of a member is received
                    val index = oldGroupMembers.indexOf(selectedGroupMember)
                    if (index > -1) {
                        oldGroupMembers.set(index, member)
                    }
                }
            }
        }
        _groupMembers.postValue(oldGroupMembers)

        getGroups(projectId)
        getRoles(projectId)
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onProjectMemberUpdatedEvent(event: LocalEvents.ProjectMemberUpdatedEvent?) {
        val oldGroupMembers = groupMembers.value
        val selectedGroupMember = oldGroupMembers?.find { it.id == event?.updatedMember?.id }

        if (event?.updatedMember?.user != null) {
            if (selectedGroupMember == null) {
                //it means new member added
                oldGroupMembers?.add(event.updatedMember)
            } else {
                //it means an update of a member is received
                val index = oldGroupMembers.indexOf(selectedGroupMember)
                if (index > -1) {
                    oldGroupMembers.set(index, event.updatedMember)
                }
            }
        }
        _groupMembers.postValue(oldGroupMembers)

        getGroups(event?.updatedMember?.project)
        getRoles(event?.updatedMember?.project)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onProjectMemberRefreshEvent(event: LocalEvents.ProjectMemberRefreshEvent?) {
        getMembers(event?.projectId ?: "")
        getGroups(event?.projectId ?: "")
        getRoles(event?.projectId ?: "")
    }

    override fun onCleared() {
        super.onCleared()
        EventBus.getDefault().unregister(this)
    }
}