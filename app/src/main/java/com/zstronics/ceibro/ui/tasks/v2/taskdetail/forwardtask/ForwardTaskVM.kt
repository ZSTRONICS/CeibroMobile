package com.zstronics.ceibro.ui.tasks.v2.taskdetail.forwardtask

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.CookiesManager
import com.zstronics.ceibro.data.database.dao.TaskV2Dao
import com.zstronics.ceibro.data.database.models.tasks.CeibroTaskV2
import com.zstronics.ceibro.data.repos.NotificationTaskData
import com.zstronics.ceibro.data.repos.dashboard.IDashboardRepository
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.AllCeibroConnections
import com.zstronics.ceibro.data.repos.task.ITaskRepository
import com.zstronics.ceibro.data.repos.task.models.v2.EventV2Response
import com.zstronics.ceibro.data.repos.task.models.v2.ForwardTaskV2Request
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.ui.socket.LocalEvents
import com.zstronics.ceibro.ui.tasks.task.TaskStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import org.greenrobot.eventbus.EventBus
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
    var notificationTaskData: MutableLiveData<NotificationTaskData?> = MutableLiveData()

    var selectedContacts: MutableLiveData<MutableList<AllCeibroConnections.CeibroConnection>> =
        MutableLiveData()
    var oldSelectedContacts: ArrayList<String> = arrayListOf()

    var taskId: String = ""
    override fun onFirsTimeUiCreate(bundle: Bundle?) {
        super.onFirsTimeUiCreate(bundle)
        val selectedContact = bundle?.getStringArrayList("assignToContacts")
        val tasksId = bundle?.getString("taskId")
        tasksId?.let {
            taskId = it
        }
        if (!selectedContact.isNullOrEmpty()) {
            oldSelectedContacts = selectedContact
        }


        //Following code will only execute if forward screen is opened from notification
        val notificationData: NotificationTaskData? = bundle?.getParcelable("notificationTaskData")
        notificationTaskData.postValue(notificationData)
        notificationData?.let {
            if (CookiesManager.jwtToken.isNullOrEmpty()) {
                sessionManager.setUser()
                sessionManager.isUserLoggedIn()
            }
            taskId = it.taskId
            launch {
                val task = taskDao.getTaskByID(it.taskId)
                task?.let { task1 ->
                    val assignTo = task1.assignedToState.map {assignee -> assignee.phoneNumber }
                    val invited = task1.invitedNumbers.map {invited -> invited.phoneNumber }
                    val combinedList = arrayListOf<String>()
                    combinedList.addAll(assignTo)
                    combinedList.addAll(invited)
                    oldSelectedContacts = combinedList
                }
            }
        }
    }


    fun forwardTask(
        onBack: (event: EventV2Response.Data) -> Unit,
    ) {
        val selectedContactList = selectedContacts.value
        if (!selectedContactList.isNullOrEmpty()) {
            val state = TaskStatus.NEW.name.lowercase()

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
                    taskId,
                    forwardTaskRequest
                ) { isSuccess, forwardEvent, errorMsg ->
                    if (isSuccess) {
                        launch {
                            updateForwardTaskInLocal(
                                forwardEvent,
                                taskDao,
                                user?.id,
                                sessionManager
                            )
                            loading(false, "")
                            if (forwardEvent != null) {
                                onBack(forwardEvent)
                            }
                        }
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