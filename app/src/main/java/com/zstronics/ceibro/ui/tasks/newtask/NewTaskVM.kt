package com.zstronics.ceibro.ui.tasks.newtask

import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NewTaskVM @Inject constructor(
    override val viewState: NewTaskState,
) : HiltBaseViewModel<INewTask.State>(), INewTask.ViewModel {
}