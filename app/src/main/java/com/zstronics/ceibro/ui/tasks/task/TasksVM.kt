package com.zstronics.ceibro.ui.tasks.task

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.database.models.tasks.CeibroTask
import com.zstronics.ceibro.data.database.models.tasks.TaskMember
import com.zstronics.ceibro.data.repos.task.TaskRepository
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.ui.socket.LocalEvents
import dagger.hilt.android.lifecycle.HiltViewModel
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject

@HiltViewModel
class TasksVM @Inject constructor(
    override val viewState: TasksState,
    val sessionManager: SessionManager,
    private val taskRepository: TaskRepository
) : HiltBaseViewModel<ITasks.State>(), ITasks.ViewModel {
    val user = sessionManager.getUser().value

    private val _tasksForStatus: MutableLiveData<List<CeibroTask>> = MutableLiveData()
    val tasksForStatus: LiveData<List<CeibroTask>> = _tasksForStatus

    private val _tasks: MutableLiveData<List<CeibroTask>> = MutableLiveData()
    val tasks: LiveData<List<CeibroTask>> = _tasks
    var originalTasks: List<CeibroTask> = listOf()

    init {
        getTasks()
        EventBus.getDefault().register(this)
    }

    override fun getTasks() {
        launch {
            originalTasks = taskRepository.tasks().reversed()
            _tasks.postValue(originalTasks)
            _tasksForStatus.postValue(originalTasks)
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
    fun onApplyFilters(filter: LocalEvents.ApplyFilterOnTask) {
        val filtered =
            originalTasks.filter {
                /// check the project filter and assigneeToMembers 2 filters
                (it.project.id == filter.projectId || filter.projectId.isEmpty()) && haveMembers(
                    it.assignedTo,
                    filter.assigneeToMembers
                ) && (it.state.equals(filter.selectedStatus, true) || filter.selectedStatus.equals("All", true))
                        && (it.dueDate == filter.selectedDueDate || filter.selectedDueDate.isEmpty())

            }
        _tasks.postValue(filtered)
        _tasksForStatus.postValue(filtered)
    }

    fun applyStatusFilter(selectedStatus: String) {
        val filtered =
            originalTasks.filter {
                // Only status filters on tasks
                (it.state.equals(selectedStatus, true) || selectedStatus.equals("ALL", true))
            }
        _tasks.postValue(filtered)

        if (tasksForStatus.value?.size != originalTasks.size) {
            _tasksForStatus.postValue(originalTasks)
        }
    }

    private fun haveMembers(
        list: List<TaskMember>,
        assigneeToMembers: List<TaskMember>?
    ): Boolean {
        // Return true if assigneeToMembers is null or empty
        if (assigneeToMembers == null || assigneeToMembers.isEmpty()) {
            return true
        }

        // Check if any of the assigneeToMembers ids are found in the list
        return assigneeToMembers.any { assignee ->
            list.any { it.id == assignee.id }
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun resetFilters(event: LocalEvents.ClearTaskFilters) {
        _tasks.postValue(originalTasks)
        _tasksForStatus.postValue(originalTasks)
    }


    override fun onCleared() {
        super.onCleared()
        EventBus.getDefault().unregister(this)
    }
}