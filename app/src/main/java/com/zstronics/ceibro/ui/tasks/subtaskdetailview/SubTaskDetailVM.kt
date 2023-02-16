package com.zstronics.ceibro.ui.tasks.subtaskdetailview

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.database.models.subtask.AllSubtask
import com.zstronics.ceibro.data.database.models.tasks.CeibroTask
import com.zstronics.ceibro.data.local.FileAttachmentsDataSource
import com.zstronics.ceibro.data.repos.auth.login.User
import com.zstronics.ceibro.data.repos.task.ITaskRepository
import com.zstronics.ceibro.data.sessions.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SubTaskDetailVM @Inject constructor(
    override val viewState: SubTaskDetailState,
    val sessionManager: SessionManager,
    private val taskRepository: ITaskRepository,
    private val fileAttachmentsDataSource: FileAttachmentsDataSource
) : HiltBaseViewModel<ISubTaskDetail.State>(), ISubTaskDetail.ViewModel {
    private val userObj = sessionManager.getUser().value

    private val _user: MutableLiveData<User> = MutableLiveData()
    val user: LiveData<User> = _user

    private val _subtask: MutableLiveData<AllSubtask> = MutableLiveData()
    val subtask: LiveData<AllSubtask> = _subtask

//    private val _task: MutableLiveData<CeibroTask> = MutableLiveData()
//    val task: LiveData<CeibroTask> = _task

    override fun onFirsTimeUiCreate(bundle: Bundle?) {
        super.onFirsTimeUiCreate(bundle)
        _user.value = userObj

        val subtaskParcel: AllSubtask? = bundle?.getParcelable("subtask")
//        subtaskParcel?.taskId.let {
//            if (it != null) {
//                getTaskById(it)
//            }
//        }
        _subtask.value = subtaskParcel
        launch {
            val attachments = subtaskParcel?.id?.let {
                fileAttachmentsDataSource.getAttachmentsById(
                    "SubTask",
                    it
                )
            }
        }
    }

}