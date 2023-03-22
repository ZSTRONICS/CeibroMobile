package com.zstronics.ceibro.ui.projects.newproject.role

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.repos.projects.IProjectRepository
import com.zstronics.ceibro.data.repos.projects.group.CreateGroupRequest
import com.zstronics.ceibro.data.repos.projects.projectsmain.AllProjectsResponse
import com.zstronics.ceibro.data.repos.projects.role.CreateRoleRequest
import com.zstronics.ceibro.data.repos.projects.role.ProjectRolesResponse
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.ui.socket.LocalEvents
import dagger.hilt.android.lifecycle.HiltViewModel
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject

@HiltViewModel
class ProjectRoleVM @Inject constructor(
    override val viewState: ProjectRoleState,
    val sessionManager: SessionManager,
    private val projectRepository: IProjectRepository
) : HiltBaseViewModel<IProjectRole.State>(), IProjectRole.ViewModel {

    private val _roles: MutableLiveData<ArrayList<ProjectRolesResponse.ProjectRole>> =
        MutableLiveData(arrayListOf())
    val roles: LiveData<ArrayList<ProjectRolesResponse.ProjectRole>> = _roles

    init {
        EventBus.getDefault().register(this)
    }


    fun getRoles(id: String?) {
        launch {
            when (val response = projectRepository.getRoles(id ?: "")) {
                is ApiResponse.Success -> {
                    val newRoles = response.data.roles as ArrayList<ProjectRolesResponse.ProjectRole>?

                    val adminRole = newRoles?.find { it.isDefaultRole }
                    val adminIndex = newRoles?.indexOf(adminRole)
                    val newRoleList: ArrayList<ProjectRolesResponse.ProjectRole> = arrayListOf()
                    adminRole?.let { newRoleList.add(it) }                  //keeping the admin role on top of the list

                    if (adminIndex != null && adminIndex > -1) {
                        newRoles.removeAt(adminIndex)
                    }
                    val sortedList = newRoles?.sortedBy { it.createdAt }
                    val reversedList = sortedList?.asReversed()
                    if (reversedList != null) {
                        newRoleList.addAll(reversedList)
                    }

                    _roles.postValue(newRoleList)
                }
                is ApiResponse.Error -> {
                    alert(response.error.message)
                }
            }
        }
    }

    fun createRoleAPI(
        roleData: CreateRoleRequest,
        success: () -> Unit
    ) {
        launch {
            loading(true)

            when (val response = projectRepository.createRoles(roleData.project, roleData)) {
                is ApiResponse.Success -> {
                    loading(false)
                    getRoles(roleData.project)
                    success.invoke()
                }
                is ApiResponse.Error -> {
                    loading(false, response.error.message)
                }
            }
        }
    }

    fun updateRoleAPI(
        roleId: String,
        roleData: CreateRoleRequest,
        success: () -> Unit
    ) {
        launch {
            loading(true)

            when (val response = projectRepository.updateRoles(roleId, roleData)) {
                is ApiResponse.Success -> {
                    loading(false)
                    getRoles(roleData.project)
                    success.invoke()
                }
                is ApiResponse.Error -> {
                    loading(false, response.error.message)
                }
            }
        }
    }

    fun deleteRole(position: Int, data: ProjectRolesResponse.ProjectRole) {
        if (!data.isDefaultRole) {
            val old = roles.value
            old?.removeAt(position)
            old?.let {
                _roles.value = it
            }
            deleteRoleAPI(data)
        } else {
            alert("Cannot remove default role")
        }
    }

    private fun deleteRoleAPI(data: ProjectRolesResponse.ProjectRole) {
        launch {
            loading(true)
            when (val response = projectRepository.deleteRole(data.id)) {
                is ApiResponse.Success -> {
                    loading(false)
                }
                is ApiResponse.Error -> {
//                    loading(false, response.error.message)
                }
            }
        }
    }




    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRoleCreatedEvent(event: LocalEvents.RoleCreatedEvent?) {
        val oldRoles = roles.value
        val selectedRole = oldRoles?.find { it.id == event?.newRole?.id }

        if (selectedRole == null) {
            //it means new role added
            event?.newRole?.let { oldRoles?.add(it) }
        }
        else {
            //it means an update of a role is received
            val index = oldRoles.indexOf(selectedRole)
            if (index > -1) {
                event?.newRole?.let { oldRoles.set(index, it) }
            }
        }

        val adminRole = oldRoles?.find { it.isDefaultRole }
        val adminIndex = oldRoles?.indexOf(adminRole)
        val newRoleList: ArrayList<ProjectRolesResponse.ProjectRole> = arrayListOf()
        adminRole?.let { newRoleList.add(it) }                  //keeping the admin role on top of the list

        if (adminIndex != null && adminIndex > -1) {
            oldRoles.removeAt(adminIndex)
        }
        val sortedList = oldRoles?.sortedBy { it.createdAt }
        val reversedList = sortedList?.asReversed()          //sorting that newly created comes on top

        if (reversedList != null) {
            newRoleList.addAll(reversedList)
        }

        _roles.postValue(newRoleList)
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRoleRefreshEvent(event: LocalEvents.RoleRefreshEvent?) {
        getRoles(event?.projectId ?: "")
    }

    override fun onCleared() {
        super.onCleared()
        EventBus.getDefault().unregister(this)
    }
}