package com.zstronics.ceibro.ui.tasks.task

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.database.models.tasks.CeibroTask
import com.zstronics.ceibro.data.repos.task.TaskRepository
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.ui.socket.LocalEvents
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TasksVM @Inject constructor(
    override val viewState: TasksState,
    val sessionManager: SessionManager,
    private val taskRepository: TaskRepository
) : HiltBaseViewModel<ITasks.State>(), ITasks.ViewModel {
    val user = sessionManager.getUser().value

    private val _tasks: MutableLiveData<List<CeibroTask>> = MutableLiveData()
    val tasks: LiveData<List<CeibroTask>> = _tasks

    init {
        getTasks()
    }

    override fun getTasks() {
        launch {
            _tasks.postValue(taskRepository.tasks().reversed())
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

    fun applyFilter(event: LocalEvents.ApplyFilterOnTask) {
        launch {
            _tasks.postValue(taskRepository.applyFilterOnTask(event).reversed())
        }
    }


}