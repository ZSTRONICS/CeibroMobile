package com.zstronics.ceibro.ui.tasks.subtask

import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SubTaskVM @Inject constructor(
    override val viewState: SubTaskState,
) : HiltBaseViewModel<ISubTask.State>(), ISubTask.ViewModel {
}