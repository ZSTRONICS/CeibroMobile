package com.zstronics.ceibro.ui.tasks.v3.fragments.approval

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.database.dao.TaskV2Dao
import com.zstronics.ceibro.data.database.models.tasks.CeibroTaskV2
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.ui.tasks.task.TaskStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TaskV3ApprovalVM @Inject constructor(
    override val viewState: TaskV3ApprovalState,
    val sessionManager: SessionManager,
    val taskDao: TaskV2Dao,
) : HiltBaseViewModel<ITaskV3Approval.State>(), ITaskV3Approval.ViewModel {
    var user = sessionManager.getUser().value

}