package com.zstronics.ceibro.ui.projects

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.repos.projects.IProjectRepository
import com.zstronics.ceibro.data.repos.projects.projectsmain.AllProjectsResponse
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.ui.socket.LocalEvents
import dagger.hilt.android.lifecycle.HiltViewModel
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
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
    var originalAlProjects = mutableListOf<AllProjectsResponse.Projects>()
    var statusesList: List<String> = listOf()
    var ownersList: List<AllProjectsResponse.Projects.Owner> = listOf()
    override fun onResume() {
        super.onResume()
        loadProjects("all")
    }

    init {
        EventBus.getDefault().register(this)
    }

    override fun loadProjects(publishStatus: String) {
        launch {
            loading(true)
            when (val response = projectRepository.getProjects()) {

                is ApiResponse.Success -> {
                    loading(false)
                    val data = response.data
                    originalAlProjects = data.projects as MutableList<AllProjectsResponse.Projects>
                    prepareFiltersData(originalAlProjects)
                    _allProjects.postValue(originalAlProjects)
//                    _allProjects.postValue(data.result.projects.toMutableList())
                }

                is ApiResponse.Error -> {
                    loading(false, response.error.message)
                }
            }
        }
    }

    private fun prepareFiltersData(originalAlProjects: MutableList<AllProjectsResponse.Projects>) {
        statusesList = originalAlProjects.map { it.publishStatus ?: "Default" }.distinct()
        ownersList =
            originalAlProjects.flatMap { it.owner }.distinctBy { it.id }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onProjectCreatedEvent(event: LocalEvents.ProjectCreatedEvent?) {
        val oldProjects = allProjects.value
        val selectedProject = oldProjects?.find { it.id == event?.newProject?.id }

        if (selectedProject == null) {
            //it means new project
            event?.newProject?.let { oldProjects?.add(it) }
        } else {
            //it means an update of a project
            val index = oldProjects.indexOf(selectedProject)
            if (index > -1) {
                event?.newProject?.let { oldProjects.set(index, it) }
            }
        }
        val sortedList: MutableList<AllProjectsResponse.Projects> =
            oldProjects?.sortedBy { it.createdAt } as MutableList<AllProjectsResponse.Projects>
        val reversedProjectList = sortedList.asReversed()

        _allProjects.postValue(reversedProjectList)
        prepareFiltersData(reversedProjectList)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onProjectRefreshEvent(event: LocalEvents.ProjectRefreshEvent?) {
        loadProjects("all")
    }

    override fun onCleared() {
        super.onCleared()
        EventBus.getDefault().unregister(this)
    }

    fun searchProject(query: String?) {
        if (query?.isEmpty() == true) {
            _allProjects.postValue(originalAlProjects)
            return
        }
        val filterProjects = originalAlProjects.filter { it.title.contains(query.toString()) }
        _allProjects.postValue(filterProjects as MutableList<AllProjectsResponse.Projects>?)
    }

    fun clearFilter() {
        _allProjects.postValue(originalAlProjects as MutableList<AllProjectsResponse.Projects>?)
    }

    fun applyFilter(ownerId: String, status: String, dueDate: String) {
        val filtered =
            originalAlProjects.filter {
                (it.owner.any { owner -> owner.id == ownerId } || ownerId.isEmpty())
                        && (it.publishStatus?.lowercase() == status.lowercase() || status.isEmpty())
                        && (it.dueDate == dueDate || dueDate.isEmpty())
            }

        _allProjects.postValue(filtered as MutableList<AllProjectsResponse.Projects>?)
    }

}