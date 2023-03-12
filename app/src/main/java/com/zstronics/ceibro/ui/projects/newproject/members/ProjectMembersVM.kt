package com.zstronics.ceibro.ui.projects.newproject.members

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.repos.projects.IProjectRepository
import com.zstronics.ceibro.data.repos.projects.group.ProjectGroup
import com.zstronics.ceibro.data.repos.projects.member.CreateProjectMemberRequest
import com.zstronics.ceibro.data.repos.projects.member.GetProjectMemberResponse
import com.zstronics.ceibro.data.repos.projects.role.ProjectRolesResponse
import com.zstronics.ceibro.data.sessions.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProjectMembersVM @Inject constructor(
    override val viewState: ProjectMembersState,
    val sessionManager: SessionManager,
    private val projectRepository: IProjectRepository
) : HiltBaseViewModel<IProjectMembers.State>(), IProjectMembers.ViewModel {

    private val _groupMembers: MutableLiveData<ArrayList<GetProjectMemberResponse.ProjectMember>?> =
        MutableLiveData(arrayListOf())
    val groupMembers: LiveData<ArrayList<GetProjectMemberResponse.ProjectMember>?> = _groupMembers

    private val _groups: MutableLiveData<ArrayList<ProjectGroup>?> =
        MutableLiveData(arrayListOf())
    val groups: LiveData<ArrayList<ProjectGroup>?> = _groups
    private val _roles: MutableLiveData<ArrayList<ProjectRolesResponse.ProjectRole>> =
        MutableLiveData(arrayListOf())
    val roles: LiveData<ArrayList<ProjectRolesResponse.ProjectRole>> = _roles

    fun getMembers(id: String?) {
        launch {
            when (val response = projectRepository.getProjectMembers(id ?: "")) {
                is ApiResponse.Success -> {
                    _groupMembers.postValue(response.data.members as ArrayList<GetProjectMemberResponse.ProjectMember>?)
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

    fun deleteMember(position: Int) {

    }

//    fun addGroup(id: String?, group: String) {
//        val oldList = groups.value
//        val groupExist = oldList?.find { it.name == group }
//        if (groupExist != null) {
//            alert("Duplicate group")
//            return
//        }
//        oldList?.add(ProjectGroup(name = group))
//        oldList?.let {
//            _groups.value = it
//        }
//        addGroupAPI(id, group)
//    }
//
//    private fun addGroupAPI(id: String?, group: String) {
//        launch {
//            when (val response =
//                projectRepository.createGroup(id ?: "", CreateGroupRequest(group))) {
//                is ApiResponse.Success -> {
//                    getGroups(id)
//                }
//                is ApiResponse.Error -> {
//                    alert(response.error.message)
//                }
//            }
//        }
//    }
//
//    fun updateGroup(projectId: String, id: String?, group: String) {
//        val old = groups.value
//        val groupFound = old?.find { it.id == id }
//        if (groupFound != null) {
//            val index = old.indexOf(groupFound)
//            if (index != -1) {
//                old.removeAt(index)
//                groupFound.name = group
//                old.add(index, groupFound)
//            }
//            old.let {
//                _groups.value = it
//            }
//        }
//        updateGroupAPI(projectId, id, group)
//    }
//
//    private fun updateGroupAPI(projectId: String, id: String?, group: String) {
//
//        launch {
//            when (val response =
//                projectRepository.updateGroup(id ?: "", CreateGroupRequest(group))) {
//                is ApiResponse.Success -> {
//                    getGroups(projectId)
//                }
//                is ApiResponse.Error -> {
//                    alert(response.error.message)
//                }
//            }
//        }
//    }
//
//    fun deleteGroup(projectId: String, position: Int, id: String) {
//        val old = groups.value
//        old?.removeAt(position)
//        old?.let {
//            _groups.value = it
//        }
//        deleteGroupAPI(projectId, id)
//    }
//
//    private fun deleteGroupAPI(projectId: String, id: String) {
//        launch {
//            when (val response =
//                projectRepository.deleteGroup(id)) {
//                is ApiResponse.Success -> {
//                    getGroups(projectId)
//                }
//                is ApiResponse.Error -> {
//                    getGroups(projectId)
//                }
//            }
//        }
//    }
}