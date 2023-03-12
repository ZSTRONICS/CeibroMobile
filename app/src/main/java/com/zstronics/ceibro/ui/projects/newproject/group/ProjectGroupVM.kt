package com.zstronics.ceibro.ui.projects.newproject.group

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.repos.projects.IProjectRepository
import com.zstronics.ceibro.data.repos.projects.group.ProjectGroup
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.data.repos.projects.group.CreateGroupRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProjectGroupVM @Inject constructor(
    override val viewState: ProjectGroupState,
    val sessionManager: SessionManager,
    private val projectRepository: IProjectRepository
) : HiltBaseViewModel<IProjectGroup.State>(), IProjectGroup.ViewModel {

    private val _groups: MutableLiveData<ArrayList<ProjectGroup>> =
        MutableLiveData(arrayListOf())
    val groups: LiveData<ArrayList<ProjectGroup>> = _groups
    fun getProjects(id: String?) {
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

    fun addGroup(id: String?, group: String) {
        val oldList = groups.value
        val groupExist = oldList?.find { it.name == group }
        if (groupExist != null) {
            alert("Duplicate group")
            return
        }
        oldList?.add(ProjectGroup(name = group))
        oldList?.let {
            _groups.value = it
        }
        addGroupAPI(id, group)
    }

    private fun addGroupAPI(id: String?, group: String) {
        launch {
            when (val response =
                projectRepository.createGroup(id ?: "", CreateGroupRequest(group))) {
                is ApiResponse.Success -> {
                    getProjects(id)
                }
                is ApiResponse.Error -> {
                    alert(response.error.message)
                }
            }
        }
    }
}