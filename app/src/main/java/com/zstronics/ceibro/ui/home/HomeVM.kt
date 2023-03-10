package com.zstronics.ceibro.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.repos.projects.IProjectRepository
import com.zstronics.ceibro.data.repos.projects.projectsmain.AllProjectsResponse
import com.zstronics.ceibro.data.sessions.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeVM @Inject constructor(
    override val viewState: HomeState,
    val sessionManager: SessionManager,
    private val projectRepository: IProjectRepository
) : HiltBaseViewModel<IHome.State>(), IHome.ViewModel {

    private val _homeProjects: MutableLiveData<MutableList<AllProjectsResponse.Projects>> =
        MutableLiveData()
    val homeProjects: LiveData<MutableList<AllProjectsResponse.Projects>> = _homeProjects

    override fun onResume() {
        super.onResume()
        loadProjects("all")
    }

    override fun loadProjects(publishStatus: String) {
        launch {
            loading(true)
            when (val response = projectRepository.getProjects()) {

                is ApiResponse.Success -> {
                    loading(false)
                    val data = response.data
                    _homeProjects.postValue(data.projects as MutableList<AllProjectsResponse.Projects>?)
                }

                is ApiResponse.Error -> {
                    loading(false)
                }
            }
        }
    }

}