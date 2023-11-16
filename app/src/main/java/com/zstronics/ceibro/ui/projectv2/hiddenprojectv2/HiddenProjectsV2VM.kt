package com.zstronics.ceibro.ui.projectv2.hiddenprojectv2

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
class HiddenProjectsV2VM @Inject constructor(
    override val viewState: HiddenProjectsV2State,
    val projectsV2Dao: ProjectsV2Dao,
    private val projectRepository: IProjectRepository,
) : HiltBaseViewModel<IHiddenProjectV2.State>(), IHiddenProjectV2.ViewModel {


    private val _allHiddenProjects: MutableLiveData<MutableList<CeibroProjectV2>> =
        MutableLiveData()
    val allHiddenProjects: LiveData<MutableList<CeibroProjectV2>> = _allHiddenProjects

    override fun getProjectName(context: Context) {


    }

    override fun onFirsTimeUiCreate(bundle: Bundle?) {
        super.onFirsTimeUiCreate(bundle)
        getHiddenProjects()

    }


    override fun getHiddenProjects() {
        launch {
            val hiddenProjects = projectsV2Dao.getAllHiddenProjects(true)
            if (hiddenProjects.isNotEmpty()) {
                _allHiddenProjects.postValue(hiddenProjects.toMutableList())
            } else {
                _allHiddenProjects.postValue(mutableListOf())
            }

        }
    }

    override fun unHideProject(
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
//                    projectsV2Dao.insertProject(ceibroProjectV2)
//                    getHiddenProjects()
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

}