package com.zstronics.ceibro.ui.tasks.task

import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.repos.tasks.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TasksVM @Inject constructor(
    override val viewState: TasksState,
    val taskRepository: TaskRepository
) : HiltBaseViewModel<ITasks.State>(), ITasks.ViewModel {
}