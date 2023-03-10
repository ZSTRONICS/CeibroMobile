package com.zstronics.ceibro.ui.projects

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.repos.chat.IChatRepository
import com.zstronics.ceibro.data.repos.chat.room.ChatRoom
import com.zstronics.ceibro.data.repos.projects.IProjectRepository
import com.zstronics.ceibro.data.repos.projects.projectsmain.AllProjectsResponse
import com.zstronics.ceibro.data.sessions.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProjectsVM @Inject constructor(
    override val viewState: ProjectsState,
    val sessionManager: SessionManager,
    private val projectRepository: IProjectRepository
) : HiltBaseViewModel<IProjects.State>(), IProjects.ViewModel {

    private val _allProjects: MutableLiveData<MutableList<AllProjectsResponse.Projects>> =
        MutableLiveData()
    val allProjects: LiveData<MutableList<AllProjectsResponse.Projects>> = _allProjects

    override fun onResume() {
        super.onResume()
        loadProjects("all")
    }

    override fun loadProjects(publishStatus: String) {
        launch {
            loading(true)
            when (val response = projectRepository.getProjects(publishStatus)) {

                is ApiResponse.Success -> {
                    loading(false)
                    val data = response.data
                    _allProjects.postValue(data.projects as MutableList<AllProjectsResponse.Projects>?)
//                    _allProjects.postValue(data.result.projects.toMutableList())
                }

                is ApiResponse.Error -> {
                    loading(false)
                }
            }
        }
    }

}