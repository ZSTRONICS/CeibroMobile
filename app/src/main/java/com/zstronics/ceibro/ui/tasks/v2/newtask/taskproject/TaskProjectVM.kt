package com.zstronics.ceibro.ui.tasks.v2.newtask.taskproject

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.database.dao.ProjectsV2Dao
import com.zstronics.ceibro.data.repos.projects.IProjectRepository
import com.zstronics.ceibro.data.repos.projects.projectsmain.AllProjectsResponseV2
import com.zstronics.ceibro.data.sessions.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TaskProjectVM @Inject constructor(
    override val viewState: TaskProjectState,
    val sessionManager: SessionManager,
    private val projectRepository: IProjectRepository,
    private val projectDao: ProjectsV2Dao,
) : HiltBaseViewModel<ITaskProject.State>(), ITaskProject.ViewModel {
    val user = sessionManager.getUser().value


    private val _allProjects: MutableLiveData<MutableList<AllProjectsResponseV2.ProjectsV2>> =
        MutableLiveData()
    val allProjects: MutableLiveData<MutableList<AllProjectsResponseV2.ProjectsV2>> = _allProjects
    var originalAllProjects = mutableListOf<AllProjectsResponseV2.ProjectsV2>()

    private var _allProjectsGrouped: MutableLiveData<MutableList<CeibroProjectGroup>> =
        MutableLiveData()
    val allProjectsGrouped: MutableLiveData<MutableList<CeibroProjectGroup>> =
        _allProjectsGrouped


    fun loadProjects(callBack: () -> Unit) {
        launch {
            val projectsData = projectDao.getAll()
            if (projectsData != null) {
                originalAllProjects =
                    projectsData.projects as MutableList<AllProjectsResponseV2.ProjectsV2>
                _allProjects.postValue(originalAllProjects)
                callBack.invoke()
            } else {
                when (val response = projectRepository.getProjectsV2()) {
                    is ApiResponse.Success -> {
                        val data = response.data.projects
                        if (data != null) {
                            originalAllProjects =
                                (data as MutableList<AllProjectsResponseV2.ProjectsV2>).toMutableList()
                            _allProjects.postValue(originalAllProjects)
                        }
                        callBack.invoke()
                    }

                    is ApiResponse.Error -> {
                        alert(response.error.message)
                        callBack.invoke()
                    }
                }
            }
        }
    }

    fun searchProject(query: String) {
        if (query.isEmpty()) {
            _allProjects.postValue(originalAllProjects)
            return
        }
        val filterProjects =
            originalAllProjects.filter { it.title.contains(query.trim(), true) }
        _allProjects.postValue(filterProjects as MutableList<AllProjectsResponseV2.ProjectsV2>?)
    }


    fun groupDataByFirstLetter(data: List<AllProjectsResponseV2.ProjectsV2>) {
        val sections = mutableListOf<CeibroProjectGroup>()

        val groupedData = data.groupBy {
            if (it.title.firstOrNull()?.isLetter() == true) {
                it.title.first().lowercase()
            } else {
                '#'.toString()
            }
        }.toSortedMap(
            compareBy<String> { it != "#" }
                .then(compareBy { it.lowercase() })
                .then(compareByDescending { it == "#" })
        )

        for (mapKey in groupedData.keys) {
            sections.add(
                CeibroProjectGroup(
                    mapKey.toString().uppercase()[0],
                    groupedData[mapKey]?.sortedBy { it.title.lowercase() }
                        ?: emptyList()
                )
            )
        }
        _allProjectsGrouped.value = sections
    }

    data class CeibroProjectGroup(
        val sectionLetter: Char,
        val items: List<AllProjectsResponseV2.ProjectsV2>
    )

}