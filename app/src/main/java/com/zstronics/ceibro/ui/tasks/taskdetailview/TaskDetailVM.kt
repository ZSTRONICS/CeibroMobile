package com.zstronics.ceibro.ui.tasks.taskdetailview

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.database.models.subtask.AllSubtask
import com.zstronics.ceibro.data.database.models.tasks.CeibroTask
import com.zstronics.ceibro.data.repos.task.ITaskRepository
import com.zstronics.ceibro.ui.tasks.task.TaskStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TaskDetailVM @Inject constructor(
    override val viewState: TaskDetailState,
    private val taskRepository: ITaskRepository,
) : HiltBaseViewModel<ITaskDetail.State>(), ITaskDetail.ViewModel {
    private val _task: MutableLiveData<CeibroTask> = MutableLiveData()
    val task: LiveData<CeibroTask> = _task

    private val _subTasks: MutableLiveData<List<AllSubtask>> = MutableLiveData()
    val subTasks: LiveData<List<AllSubtask>> = _subTasks

    override fun onFirsTimeUiCreate(bundle: Bundle?) {
        super.onFirsTimeUiCreate(bundle)
        val taskParcel: CeibroTask? = bundle?.getParcelable("task")
        _task.value = taskParcel

        taskParcel?._id?.let { getSubTasks(it) }
    }

    override fun getSubTasks(taskId: String) {
        launch {
            _subTasks.postValue(taskRepository.getSubTaskByTaskId(taskId))
        }
    }

    override fun onResume() {
        super.onResume()
        task.value?._id?.let { getSubTasks(it) }
    }
}