package com.zstronics.ceibro.ui.tasks.taskdetailview

import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TaskDetailVM @Inject constructor(
    override val viewState: TaskDetailState,
) : HiltBaseViewModel<ITaskDetail.State>(), ITaskDetail.ViewModel {
}