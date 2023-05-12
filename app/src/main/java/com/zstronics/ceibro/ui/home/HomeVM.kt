package com.zstronics.ceibro.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.database.models.tasks.CeibroTask
import com.zstronics.ceibro.data.repos.projects.IProjectRepository
import com.zstronics.ceibro.data.repos.projects.projectsmain.AllProjectsResponse
import com.zstronics.ceibro.data.repos.task.TaskRepository
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.ui.socket.LocalEvents
import dagger.hilt.android.lifecycle.HiltViewModel
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject

@HiltViewModel
class HomeVM @Inject constructor(
    override val viewState: HomeState,
    val sessionManager: SessionManager,
    private val projectRepository: IProjectRepository,
    private val taskRepository: TaskRepository
) : HiltBaseViewModel<IHome.State>(), IHome.ViewModel {
    val user = sessionManager.getUser().value

    private val _homeProjects: MutableLiveData<MutableList<AllProjectsResponse.Projects>> =
        MutableLiveData()
    val homeProjects: LiveData<MutableList<AllProjectsResponse.Projects>> = _homeProjects

    private val _homeTasks: MutableLiveData<List<CeibroTask>> = MutableLiveData()
    val homeTasks: LiveData<List<CeibroTask>> = _homeTasks
    var originalTasks: List<CeibroTask> = listOf()

    override fun onResume() {
        super.onResume()
//        loadProjects("all")
//        getTasks()
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
                    _homeProjects.postValue(data.projects as MutableList<AllProjectsResponse.Projects>?)
                }

                is ApiResponse.Error -> {
                    loading(false)
                }
            }
        }
    }

    override fun getTasks() {
        launch {
            originalTasks = taskRepository.tasks().reversed()
            _homeTasks.postValue(originalTasks)
        }
    }

    fun deleteTask(taskId: String) {
        launch {
            loading(true)
            taskRepository.deleteTask(taskId) { isSuccess, message ->
                if (isSuccess) {
                    loading(false, "Task Deleted Successfully")
                    getTasks()
                } else {
                    loading(false, message)
                }
            }
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onProjectCreatedEvent(event: LocalEvents.ProjectCreatedEvent?) {
        val oldProjects = homeProjects.value
        val selectedProject = oldProjects?.find { it.id == event?.newProject?.id }

        if (selectedProject == null) {
            //it means new project
            event?.newProject?.let { oldProjects?.add(it) }
        }
        else {
            //it means an update of a project
            val index = oldProjects.indexOf(selectedProject)
            if (index > -1) {
                event?.newProject?.let { oldProjects.set(index, it) }
            }
        }
        val sortedList: MutableList<AllProjectsResponse.Projects> = oldProjects?.sortedBy { it.createdAt } as MutableList<AllProjectsResponse.Projects>
        val reversedProjectList = sortedList.asReversed()

        _homeProjects.postValue(reversedProjectList)
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onProjectRefreshEvent(event: LocalEvents.ProjectRefreshEvent?) {
//        loadProjects("all")
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTaskCreatedEvent(event: LocalEvents.TaskCreatedEvent?) {
//        getTasks()
    }

    override fun onCleared() {
        super.onCleared()
        EventBus.getDefault().unregister(this)
    }
}