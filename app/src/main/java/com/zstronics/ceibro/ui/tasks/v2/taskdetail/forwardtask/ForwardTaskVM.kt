package com.zstronics.ceibro.ui.tasks.v2.taskdetail.forwardtask

import android.content.Context
import android.os.Bundle
import android.os.Handler
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.database.dao.TaskV2Dao
import com.zstronics.ceibro.data.database.models.tasks.CeibroTaskV2
import com.zstronics.ceibro.data.repos.dashboard.IDashboardRepository
import com.zstronics.ceibro.data.repos.dashboard.attachment.AttachmentTags
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.AllCeibroConnections
import com.zstronics.ceibro.data.repos.task.ITaskRepository
import com.zstronics.ceibro.data.repos.task.models.v2.EventCommentOnlyUploadV2Request
import com.zstronics.ceibro.data.repos.task.models.v2.EventV2Response
import com.zstronics.ceibro.data.repos.task.models.v2.EventWithFileUploadV2Request
import com.zstronics.ceibro.data.repos.task.models.v2.ForwardTaskV2Request
import com.zstronics.ceibro.data.repos.task.models.v2.TaskDetailEvents
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.ui.tasks.task.TaskStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import ee.zstronics.ceibro.camera.AttachmentTypes
import ee.zstronics.ceibro.camera.PickedImages
import javax.inject.Inject

@HiltViewModel
class ForwardTaskVM @Inject constructor(
    override val viewState: ForwardTaskState,
    val sessionManager: SessionManager,
    private val taskRepository: ITaskRepository,
    val dashboardRepository: IDashboardRepository,
    private val taskDao: TaskV2Dao,
) : HiltBaseViewModel<IForwardTask.State>(), IForwardTask.ViewModel {
    val user = sessionManager.getUser().value
    var taskData: CeibroTaskV2? = null

    val _taskDetail: MutableLiveData<CeibroTaskV2> = MutableLiveData()
    val taskDetail: LiveData<CeibroTaskV2> = _taskDetail

    var selectedContacts: MutableLiveData<MutableList<AllCeibroConnections.CeibroConnection>> =
        MutableLiveData()
    var oldSelectedContacts: ArrayList<String> = arrayListOf()


    override fun onFirsTimeUiCreate(bundle: Bundle?) {
        super.onFirsTimeUiCreate(bundle)
        val selectedContact = bundle?.getStringArrayList("assignToContacts")
        val taskData: CeibroTaskV2? = bundle?.getParcelable("taskDetail")

        if (!selectedContact.isNullOrEmpty()) {
            oldSelectedContacts = selectedContact
        }
        taskData.let { _taskDetail.postValue(it) }
    }


    fun forwardTask(
        onBack: (task: CeibroTaskV2) -> Unit,
    ) {
        val taskData = taskDetail.value
        val selectedContactList = selectedContacts.value
        if (!selectedContactList.isNullOrEmpty()) {
            val state = TaskStatus.NEW.name.lowercase()
//            if (taskData != null) {
//                state = if (user?.id == taskData.creator.id) {
//                    taskData.creatorState
//                } else {
//                    taskData.assignedToState.find { it.userId == user?.id }?.state
//                        ?: "new"
//                }
//            }
//            if (state.equals(TaskStatus.UNREAD.name, true)) {
//                state = "new"
//            }

            val assignedToCeibroUsers =
                selectedContactList.filter { it.isCeiborUser }
                    .map {
                        ForwardTaskV2Request.AssignedToStateRequest(
                            phoneNumber = it.phoneNumber,
                            userId = it.userCeibroData?.id.toString(),
                            state = state
                        )
                    } ?: listOf()
            val invitedNumbers = selectedContactList.filter { !it.isCeiborUser }
                .map { it.phoneNumber } ?: listOf()


            val forwardTaskRequest = ForwardTaskV2Request(
                assignedToState = assignedToCeibroUsers,
                invitedNumbers = invitedNumbers,
                comment = viewState.comment.value.toString()
            )

            launch {
                loading(true)
                taskRepository.forwardTask(
                    taskData?.id ?: "",
                    forwardTaskRequest
                ) { isSuccess, task, errorMsg ->
                    if (isSuccess) {
                        if (task != null) {
                            _taskDetail.postValue(task)
                            onBack(task)
                        }
                        loading(false, "")
                        updateForwardTaskInLocal(task, taskDao, user?.id, sessionManager)
                    } else {
                        loading(false, errorMsg)
                    }
                }
            }
        } else {
            alert("Please select contacts to forward")
        }
    }
}