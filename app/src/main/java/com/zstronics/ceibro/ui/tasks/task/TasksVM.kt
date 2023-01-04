package com.zstronics.ceibro.ui.tasks.task

import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TasksVM @Inject constructor(
    override val viewState: TasksState,
) : HiltBaseViewModel<ITasks.State>(), ITasks.ViewModel {
}