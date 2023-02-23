package com.zstronics.ceibro.ui.tasks

import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.sessions.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainTasksVM @Inject constructor(
    override val viewState: MainTasksState,
    private val sessionManager: SessionManager,
) : HiltBaseViewModel<IMainTasks.State>(), IMainTasks.ViewModel {
    val user = sessionManager.getUser().value
    val projects = sessionManager.getProjects().value

}