package com.zstronics.ceibro.ui.projects.newproject.role

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.repos.projects.IProjectRepository
import com.zstronics.ceibro.data.repos.projects.group.CreateGroupRequest
import com.zstronics.ceibro.data.repos.projects.role.CreateRoleRequest
import com.zstronics.ceibro.data.repos.projects.role.ProjectRolesResponse
import com.zstronics.ceibro.data.sessions.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
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
                    loading(false, response.error.message)
                }
            }
        }
    }
}