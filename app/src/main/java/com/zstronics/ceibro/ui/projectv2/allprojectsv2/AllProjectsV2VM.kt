package com.zstronics.ceibro.ui.projectv2.allprojectsv2

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.database.dao.ProjectsV2Dao
import com.zstronics.ceibro.data.database.models.projects.CeibroProjectV2
import com.zstronics.ceibro.data.repos.projects.IProjectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AllProjectsV2VM @Inject constructor(
    override val viewState: AllProjectsV2State,
    val projectsV2Dao: ProjectsV2Dao,
    private val projectRepository: IProjectRepository,
) : HiltBaseViewModel<IAllProjectV2.State>(), IAllProjectV2.ViewModel {


    private val _allProjects: MutableLiveData<MutableList<CeibroProjectV2>> =
        MutableLiveData()
    val allProjects: LiveData<MutableList<CeibroProjectV2>> = _allProjects


    private val _allFavoriteProjects: MutableLiveData<MutableList<CeibroProjectV2>> =
        MutableLiveData()
    val allFavoriteProjects: LiveData<MutableList<CeibroProjectV2>> = _allFavoriteProjects

    private val _allRecentProjects: MutableLiveData<MutableList<CeibroProjectV2>> =
        MutableLiveData()
    val allRecentProjects: LiveData<MutableList<CeibroProjectV2>> = _allRecentProjects


    override fun onFirsTimeUiCreate(bundle: Bundle?) {
        super.onFirsTimeUiCreate(bundle)
        getAllProjects()
        getFavoriteProjects()
        getRecentProjects()
    }

    override fun getProjectName(context: Context) {

    }

    override fun getAllProjects() {
        launch {
            val allProjects = projectsV2Dao.getAllProjects()
            if (allProjects.isNotEmpty()) {
                _allProjects.postValue(allProjects.toMutableList())
            } else {
                _allProjects.postValue(mutableListOf())
            }
        }
    }

    override fun getFavoriteProjects() {
        launch {
            val allFavoriteProjects = projectsV2Dao.getAllFavoriteProjects()
            if (allFavoriteProjects.isNotEmpty()) {
                _allFavoriteProjects.postValue(allFavoriteProjects.toMutableList())
            } else {
                _allFavoriteProjects.postValue(mutableListOf())
            }
        }
    }

    override fun getRecentProjects() {
        launch {
            val allRecentProjects = projectsV2Dao.getAllRecentUsedProjects()
            if (allRecentProjects.isNotEmpty()) {
                _allRecentProjects.postValue(allRecentProjects.toMutableList())
            } else {
                _allRecentProjects.postValue(mutableListOf())
            }
        }
    }


    override  fun hideProject(
        hidden: Boolean,
        projectId: String,
        callBack: (isSuccess: Boolean) -> Unit
    ) {

        launch {
            loading(true)
            when (val response = projectRepository.updateHideProjectStatus(
                hidden = hidden,
                projectId = projectId
            )) {
                is ApiResponse.Success -> {
                    val ceibroProjectV2 = response.data.updatedProject
                    updateProjectInLocal(ceibroProjectV2, projectsV2Dao)
                    loading(false, "")
                    callBack(true)
                }

                is ApiResponse.Error -> {
                    loading(false, response.error.message)
                    callBack(false)
                }
            }
        }
    }

    override  fun updateFavoriteProjectStatus(
        favorite: Boolean,
        projectId: String,
        callBack: (isSuccess: Boolean) -> Unit
    ) {

        launch {
            loading(true)
            when (val response = projectRepository.updateFavoriteProjectStatus(
                favorite = favorite,
                projectId = projectId
            )) {
                is ApiResponse.Success -> {
                    val ceibroProjectV2 = response.data.updatedProject
                    updateProjectInLocal(ceibroProjectV2, projectsV2Dao)
                    loading(false, "")
                    callBack(true)
                }

                is ApiResponse.Error -> {
                    loading(false, response.error.message)
                    callBack(false)
                }
            }
        }
    }

    fun reloadData() {
        getAllProjects()
        getFavoriteProjects()
        getRecentProjects()
    }
}