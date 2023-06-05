package com.zstronics.ceibro.ui.tasks.v2.newtask.taskproject

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.repos.projects.IProjectRepository
import com.zstronics.ceibro.data.repos.projects.projectsmain.AllProjectsResponse
import com.zstronics.ceibro.data.repos.task.models.TopicsResponse
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.ui.tasks.v2.newtask.topic.TopicVM
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TaskProjectVM @Inject constructor(
    override val viewState: TaskProjectState,
    val sessionManager: SessionManager,
    private val projectRepository: IProjectRepository
) : HiltBaseViewModel<ITaskProject.State>(), ITaskProject.ViewModel {
    val user = sessionManager.getUser().value


    private val _allProjects: MutableLiveData<MutableList<AllProjectsResponse.Projects>> =
        MutableLiveData()
    val allProjects: MutableLiveData<MutableList<AllProjectsResponse.Projects>> = _allProjects
    var originalAllProjects = mutableListOf<AllProjectsResponse.Projects>()

    private var _allProjectsGrouped: MutableLiveData<MutableList<CeibroProjectGroup>> =
        MutableLiveData()
    val allProjectsGrouped: MutableLiveData<MutableList<CeibroProjectGroup>> =
        _allProjectsGrouped


    fun loadProjects(callBack: () -> Unit) {
        launch {
            when (val response = projectRepository.getProjects()) {

                is ApiResponse.Success -> {
                    val data = response.data.projects
                    if (data != null) {
                        originalAllProjects =
                            (data as MutableList<AllProjectsResponse.Projects>).toMutableList()
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

    fun searchProject(query: String) {
        if (query.isEmpty()) {
            _allProjects.postValue(originalAllProjects)
            return
        }
        val filterProjects =
            originalAllProjects.filter { it.title.contains(query.trim(), true) }
        _allProjects.postValue(filterProjects as MutableList<AllProjectsResponse.Projects>?)
    }


    fun groupDataByFirstLetter(data: List<AllProjectsResponse.Projects>) {
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
        val items: List<AllProjectsResponse.Projects>
    )

}