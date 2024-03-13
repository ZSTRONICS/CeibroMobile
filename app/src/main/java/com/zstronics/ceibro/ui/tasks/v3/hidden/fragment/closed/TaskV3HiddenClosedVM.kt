package com.zstronics.ceibro.ui.tasks.v3.hidden.fragment.closed

import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.database.dao.TaskV2Dao
import com.zstronics.ceibro.data.sessions.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TaskV3HiddenClosedVM @Inject constructor(
    override val viewState: TaskV3HiddenClosedState,
    val sessionManager: SessionManager,
    val taskDao: TaskV2Dao,
) : HiltBaseViewModel<ITaskV3HiddenClosed.State>(), ITaskV3HiddenClosed.ViewModel {
    var user = sessionManager.getUser().value

}