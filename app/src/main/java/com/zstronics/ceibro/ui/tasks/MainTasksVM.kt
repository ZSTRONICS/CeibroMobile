package com.zstronics.ceibro.ui.tasks

import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainTasksVM @Inject constructor(
    override val viewState: MainTasksState,
) : HiltBaseViewModel<IMainTasks.State>(), IMainTasks.ViewModel {
}