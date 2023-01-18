package com.zstronics.ceibro.ui.tasks.task

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.database.models.tasks.CeibroTask
import com.zstronics.ceibro.data.repos.task.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TasksVM @Inject constructor(
    override val viewState: TasksState,
    private val taskRepository: TaskRepository
) : HiltBaseViewModel<ITasks.State>(), ITasks.ViewModel {
    private val _tasks: MutableLiveData<List<CeibroTask>> = MutableLiveData()
    val tasks: LiveData<List<CeibroTask>> = _tasks

    init {
        getTasks()
    }

    override fun getTasks() {
        launch {
            _tasks.postValue(taskRepository.tasks())
        }
    }
}