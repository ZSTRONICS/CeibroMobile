package com.zstronics.ceibro.ui.projectv2.allprojectsv2

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.database.dao.ProjectsV2Dao
import com.zstronics.ceibro.data.database.models.projects.CeibroProjectV2
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.AllCeibroConnections
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
    var originalAllProjects: MutableList<CeibroProjectV2> = mutableListOf()


    private val _allFavoriteProjects: MutableLiveData<MutableList<CeibroProjectV2>> =
        MutableLiveData()
    val allFavoriteProjects: LiveData<MutableList<CeibroProjectV2>> = _allFavoriteProjects
    var originalFavoriteProjects: MutableList<CeibroProjectV2> = mutableListOf()

    private val _allRecentProjects: MutableLiveData<MutableList<CeibroProjectV2>> =
        MutableLiveData()
    val allRecentProjects: LiveData<MutableList<CeibroProjectV2>> = _allRecentProjects
    var originalRecentProjects: MutableList<CeibroProjectV2> = mutableListOf()


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
            if (allProjects?.isNotEmpty() == true) {
                _allProjects.postValue(allProjects.toMutableList())
                originalAllProjects = allProjects.toMutableList()
            } else {
                _allProjects.postValue(mutableListOf())
                originalAllProjects = mutableListOf()
            }
        }
    }

    override fun getFavoriteProjects() {
        launch {
            val allFavoriteProjects = projectsV2Dao.getAllFavoriteProjects()
            if (allFavoriteProjects.isNotEmpty()) {
                _allFavoriteProjects.postValue(allFavoriteProjects.toMutableList())
                originalFavoriteProjects = allFavoriteProjects.toMutableList()
            } else {
                _allFavoriteProjects.postValue(mutableListOf())
                originalFavoriteProjects = mutableListOf()
            }
        }
    }

    override fun getRecentProjects() {
        launch {
            val allRecentProjects = projectsV2Dao.getAllRecentUsedProjects()
            if (allRecentProjects.isNotEmpty()) {
                _allRecentProjects.postValue(allRecentProjects.toMutableList())
                originalRecentProjects = allRecentProjects.toMutableList()
            } else {
                _allRecentProjects.postValue(mutableListOf())
                originalRecentProjects = mutableListOf()
            }
        }
    }


    override fun hideProject(
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

    override fun updateFavoriteProjectStatus(
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


    fun filterAllProjects(search: String) {
        if (search.isEmpty()) {
            if (originalAllProjects.isNotEmpty()) {
                _allProjects.postValue(originalAllProjects as MutableList<CeibroProjectV2>)
            }
            return
        }
        val filtered = originalAllProjects.filter {
            (it.title.isNotEmpty() && it.title.lowercase().contains(search, true)) ||
                    (("${it.creator.firstName} ${it.creator.surName}").isNotEmpty() && ("${it.creator.firstName} ${it.creator.surName}").lowercase()
                        .contains(search, true)) ||
                    (!it.creator.companyName.isNullOrEmpty() && it.creator.companyName.lowercase()
                        .contains(search, true))
        }
        if (filtered.isNotEmpty())
            _allProjects.postValue(filtered as MutableList<CeibroProjectV2>)
        else
            _allProjects.postValue(mutableListOf())
    }

    fun filterRecentProjects(search: String) {
        if (search.isEmpty()) {
            if (originalRecentProjects.isNotEmpty()) {
                _allRecentProjects.postValue(originalRecentProjects as MutableList<CeibroProjectV2>)
            }
            return
        }
        val filtered = originalRecentProjects.filter {
            (it.title.isNotEmpty() && it.title.lowercase().contains(search, true)) ||
                    (("${it.creator.firstName} ${it.creator.surName}").isNotEmpty() && ("${it.creator.firstName} ${it.creator.surName}").lowercase()
                        .contains(search, true)) ||
                    (!it.creator.companyName.isNullOrEmpty() && it.creator.companyName.lowercase()
                        .contains(search, true))
        }
        if (filtered.isNotEmpty())
            _allRecentProjects.postValue(filtered as MutableList<CeibroProjectV2>)
        else
            _allRecentProjects.postValue(mutableListOf())
    }

    fun filterFavoriteProjects(search: String) {
        if (search.isEmpty()) {
            if (originalFavoriteProjects.isNotEmpty()) {
                _allFavoriteProjects.postValue(originalFavoriteProjects as MutableList<CeibroProjectV2>)
            }
            return
        }
        val filtered = originalFavoriteProjects.filter {
            (it.title.isNotEmpty() && it.title.lowercase().contains(search, true)) ||
                    (("${it.creator.firstName} ${it.creator.surName}").isNotEmpty() && ("${it.creator.firstName} ${it.creator.surName}").lowercase()
                        .contains(search, true)) ||
                    (!it.creator.companyName.isNullOrEmpty() && it.creator.companyName.lowercase()
                        .contains(search, true))
        }
        if (filtered.isNotEmpty())
            _allFavoriteProjects.postValue(filtered as MutableList<CeibroProjectV2>)
        else
            _allFavoriteProjects.postValue(mutableListOf())
    }
}