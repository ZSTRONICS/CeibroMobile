package com.zstronics.ceibro.ui.tasks.subtask

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.database.models.subtask.AllSubtask
import com.zstronics.ceibro.data.database.models.tasks.CeibroTask
import com.zstronics.ceibro.data.repos.task.TaskRepository
import com.zstronics.ceibro.data.repos.task.models.UpdateSubTaskStatusRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SubTaskVM @Inject constructor(
    override val viewState: SubTaskState,
    val taskRepository: TaskRepository
) : HiltBaseViewModel<ISubTask.State>(), ISubTask.ViewModel {
    private val _subTasks: MutableLiveData<List<AllSubtask>> = MutableLiveData()
    val subTasks: LiveData<List<AllSubtask>> = _subTasks

    init {
        getSubTasks()
    }

    override fun getSubTasks() {
        launch {
            _subTasks.postValue(taskRepository.getAllSubtasks())
        }
    }

    fun rejectSubTask(
        data: AllSubtask,
        callBack: (result: Triple<Boolean, Boolean, Boolean>) -> Unit
    ) {
        val request = UpdateSubTaskStatusRequest(
            comment = "Test comment",
            state = SubTaskStatus.REJECTED.name.lowercase(),
            subTaskId = data.id,
            taskId = data.taskId
        )
        launch {
            val result = taskRepository.rejectSubtask(request)
            callBack.invoke(result)
        }
    }
}