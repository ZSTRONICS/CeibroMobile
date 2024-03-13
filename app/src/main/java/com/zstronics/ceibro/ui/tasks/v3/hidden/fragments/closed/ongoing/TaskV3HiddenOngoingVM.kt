package com.zstronics.ceibro.ui.tasks.v3.hidden.fragments.closed.ongoing

import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.database.dao.TaskV2Dao
import com.zstronics.ceibro.data.sessions.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TaskV3HiddenOngoingVM @Inject constructor(
    override val viewState: TaskV3HiddenOngoingState,
    val sessionManager: SessionManager,
    val taskDao: TaskV2Dao,
) : HiltBaseViewModel<ITaskV3HiddenOngoing.State>(), ITaskV3HiddenOngoing.ViewModel {
    var user = sessionManager.getUser().value

}