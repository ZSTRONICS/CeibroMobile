package com.zstronics.ceibro.ui.projects.newproject.role

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.repos.projects.IProjectRepository
import com.zstronics.ceibro.data.repos.projects.role.ProjectRolesResponse
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.ui.projects.newproject.group.addnewgroup.CreateGroupRequest
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


    private fun addRoleAPI(id: String?, group: String) {
        launch {
            when (val response =
                projectRepository.createGroup(id ?: "", CreateGroupRequest(group))) {
                is ApiResponse.Success -> {
                    getRoles(id)
                }
                is ApiResponse.Error -> {
                    alert(response.error.message)
                }
            }
        }
    }
}