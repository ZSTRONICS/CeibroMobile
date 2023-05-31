package com.zstronics.ceibro.ui.tasks.v2.newtask.taskproject

import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TaskProjectVM @Inject constructor(
    override val viewState: TaskProjectState,
) : HiltBaseViewModel<ITaskProject.State>(), ITaskProject.ViewModel {
}