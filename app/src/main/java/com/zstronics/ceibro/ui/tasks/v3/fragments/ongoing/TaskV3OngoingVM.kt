package com.zstronics.ceibro.ui.tasks.v3.fragments.ongoing

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.database.dao.TaskV2Dao
import com.zstronics.ceibro.data.database.models.tasks.CeibroTaskV2
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.ui.tasks.task.TaskStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TaskV3OngoingVM @Inject constructor(
    override val viewState: TaskV3OngoingState,
    val sessionManager: SessionManager,
    val taskDao: TaskV2Dao,
) : HiltBaseViewModel<ITaskV3Ongoing.State>(), ITaskV3Ongoing.ViewModel {
    var user = sessionManager.getUser().value

}