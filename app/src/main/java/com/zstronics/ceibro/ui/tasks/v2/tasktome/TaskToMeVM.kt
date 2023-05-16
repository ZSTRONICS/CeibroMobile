package com.zstronics.ceibro.ui.tasks.v2.tasktome

import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TaskToMeVM @Inject constructor(
    override val viewState: TaskToMeState,
) : HiltBaseViewModel<ITaskToMe.State>(), ITaskToMe.ViewModel {
}