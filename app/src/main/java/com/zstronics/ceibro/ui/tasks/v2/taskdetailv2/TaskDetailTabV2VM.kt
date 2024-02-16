package com.zstronics.ceibro.ui.tasks.v2.taskdetailv2

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.CeibroApplication
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.database.dao.DownloadedDrawingV2Dao
import com.zstronics.ceibro.data.database.dao.DrawingPinsV2Dao
import com.zstronics.ceibro.data.database.dao.GroupsV2Dao
import com.zstronics.ceibro.data.database.dao.InboxV2Dao
import com.zstronics.ceibro.data.database.dao.TaskV2Dao
import com.zstronics.ceibro.data.database.models.tasks.CeibroTaskV2
import com.zstronics.ceibro.data.database.models.tasks.Events
import com.zstronics.ceibro.data.database.models.tasks.TaskFiles
import com.zstronics.ceibro.data.remote.TaskRemoteDataSource
import com.zstronics.ceibro.data.repos.NotificationTaskData
import com.zstronics.ceibro.data.repos.dashboard.IDashboardRepository
import com.zstronics.ceibro.data.repos.task.ITaskRepository
import com.zstronics.ceibro.data.repos.task.TaskRootStateTags
import com.zstronics.ceibro.data.repos.task.models.v2.EventCommentOnlyUploadV2Request
import com.zstronics.ceibro.data.repos.task.models.v2.EventV2Response
import com.zstronics.ceibro.data.repos.task.models.v2.SyncTaskEventsBody
import com.zstronics.ceibro.data.repos.task.models.v2.TaskDetailEvents
import com.zstronics.ceibro.data.repos.task.models.v2.TaskSeenResponse
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.ui.socket.LocalEvents
import dagger.hilt.android.lifecycle.HiltViewModel
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

@HiltViewModel
class TaskDetailTabV2VM @Inject constructor(
    override val viewState: TaskDetailTabV2State,
    val sessionManager: SessionManager,
    private val taskRepository: ITaskRepository,
    private val remoteTask: TaskRemoteDataSource,
    val dashboardRepository: IDashboardRepository,
    val taskDao: TaskV2Dao,
    val groupsV2Dao: GroupsV2Dao,
    private val inboxV2Dao: InboxV2Dao,
    val drawingPinsDao: DrawingPinsV2Dao,
    val downloadedDrawingV2Dao: DownloadedDrawingV2Dao
) : HiltBaseViewModel<ITaskDetailTabV2.State>(), ITaskDetailTabV2.ViewModel {

    val user = sessionManager.getUser().value

    val _taskDetail: MutableLiveData<CeibroTaskV2> = MutableLiveData()
    val taskDetail: LiveData<CeibroTaskV2> = _taskDetail
    val originalTask: MutableLiveData<CeibroTaskV2> = MutableLiveData()

    val _taskEvents: MutableLiveData<MutableList<Events>> = MutableLiveData()
    val taskEvents: MutableLiveData<MutableList<Events>> = _taskEvents
    val originalEvents: MutableLiveData<MutableList<Events>> = MutableLiveData(mutableListOf())

    var notificationTaskData: MutableLiveData<NotificationTaskData?> = MutableLiveData()
    var isTaskBeingDone: MutableLiveData<Boolean> = MutableLiveData(false)

    var rootState = ""
    var selectedState = ""
    var taskId: String = ""

    init {
        if (sessionManager.getUser().value?.id.isNullOrEmpty()) {
            sessionManager.setUser()
            sessionManager.setToken()
        }
    }

    override fun onFirsTimeUiCreate(bundle: Bundle?) {
        super.onFirsTimeUiCreate(bundle)

        launch {
            val taskData: CeibroTaskV2? = CeibroApplication.CookiesManager.taskDataForDetails
            val parentRootState = CeibroApplication.CookiesManager.taskDetailRootState
            val parentSelectedState = CeibroApplication.CookiesManager.taskDetailSelectedSubState
            if (parentRootState != null) {
                rootState = parentRootState
            }
            if (parentSelectedState != null) {
                selectedState = parentSelectedState
            }

            val notificationData: NotificationTaskData? =
                bundle?.getParcelable("notificationTaskData")

            if (notificationData != null) {         //It means detail is opened via notification if not null
                notificationTaskData.postValue(notificationData)
                if (CeibroApplication.CookiesManager.jwtToken.isNullOrEmpty()) {
                    sessionManager.setUser()
                    sessionManager.setToken()
                }

                taskId = notificationData.taskId
                launch {
                    val task = taskDao.getTaskByID(notificationData.taskId)
                    task?.let { task1 ->
                        isTaskBeingDone.postValue(task.isBeingDoneByAPI)
                        rootState = TaskRootStateTags.ToMe.tagValue
                        originalTask.postValue(task1)
                        _taskDetail.postValue(task1)
                        CeibroApplication.CookiesManager.taskDataForDetailsFromNotification = task1
                        CeibroApplication.CookiesManager.taskDataForDetails = task1

//                        getAllEvents(task1.id)
                        syncEvents(task1.id)

                    } ?: run {
                        // run API call because task not found in DB
                        getTaskById(taskId) { isSuccess, task, events ->
                            if (isSuccess) {
                                isTaskBeingDone.postValue(false)
                                originalTask.postValue(task)
                                _taskDetail.postValue(task)
                                CeibroApplication.CookiesManager.taskDataForDetailsFromNotification = task
                                CeibroApplication.CookiesManager.taskDataForDetails = task
                                syncEvents(taskId)
                            } else {
                                loading(false, "No task details to show")
                            }
                        }
                    }
                }
            } else {
                taskData?.let { task ->
                    taskId = task.id
                    val isTaskBeingDone1 = taskDao.getTaskIsBeingDoneByAPI(task.id)
                    isTaskBeingDone.postValue(isTaskBeingDone1)
                    _taskDetail.postValue(task)
                    originalTask.postValue(task)
                    syncEvents(taskId)
                } ?: run {
                    alert("No details to display")
                }
            }

        }
    }


    private fun getAllEvents(taskId: String) {
        launch {
            val taskEvents = taskDao.getEventsOfTask(taskId)
            if (taskEvents.isEmpty()) {
                originalEvents.postValue(mutableListOf<Events>())
                _taskEvents.postValue(mutableListOf<Events>())
            } else {
                originalEvents.postValue(taskEvents.toMutableList())
                _taskEvents.postValue(taskEvents.toMutableList())
            }
        }
    }

    private fun syncEvents(
        taskId: String
    ) {
        launch {
            val allEvents = taskDao.getEventsOfTask(taskId).toMutableList()
            val eventsIds: MutableList<Int> = mutableListOf()
            allEvents.forEach {
                eventsIds.add(it.eventNumber)
            }
            val syncTaskEventsBody = SyncTaskEventsBody(eventsIds)
            taskRepository.syncEvents(
                taskId,
                syncTaskEventsBody
            ) { isSuccess, missingEvents, message ->
                if (isSuccess) {
                    if (missingEvents.isNotEmpty()) {
                        if (allEvents.isNotEmpty()) {
                            val newMissingEventList = mutableListOf<Events>()
                            missingEvents.forEach { event ->
                                val eventExist = allEvents.find { event.id == it.id }
                                if (eventExist == null) {  /// event not existed
                                    newMissingEventList.add(event)
                                }
                            }
                            allEvents.addAll(newMissingEventList)
                            originalEvents.postValue(allEvents)
                            _taskEvents.postValue(allEvents)

                        } else {
                            allEvents.addAll(missingEvents)
                            originalEvents.postValue(allEvents)
                            _taskEvents.postValue(allEvents)
                        }
                        launch {
                            taskDao.insertMultipleEvents(missingEvents)
                        }
                    } else {
                        originalEvents.postValue(allEvents)
                        _taskEvents.postValue(allEvents)
                    }
                } else {
                    alert("Failed to sync task events")
                }
            }
        }
    }

    private fun getTaskById(
        taskId: String,
        callBack: (isSuccess: Boolean, task: CeibroTaskV2?, taskEvents: List<Events>) -> Unit
    ) {
        launch {
            loading(true)
            when (val response = remoteTask.getTaskById(taskId)) {
                is ApiResponse.Success -> {
                    taskDao.insertTaskData(response.data.task)
                    taskDao.insertMultipleEvents(response.data.taskEvents)
                    loading(false, "")
                    callBack.invoke(true, response.data.task, response.data.taskEvents)
                }

                is ApiResponse.Error -> {
                    loading(false, response.error.message)
                    callBack.invoke(false, null, emptyList())
                }
            }
        }
    }


    fun doneTask(
        taskId: String,
        onBack: () -> Unit
    ) {
        launch {
            var isSuccess = false
            var doneData: EventV2Response.Data? = null

            val request = EventCommentOnlyUploadV2Request(
                message = ""
            )

            loading(true)
            when (val response = dashboardRepository.uploadEventWithoutFilesV2(
                event = TaskDetailEvents.DoneTask.eventValue,
                taskId = taskId,
                hasFiles = false,
                eventCommentOnlyUploadV2Request = request
            )) {
                is ApiResponse.Success -> {
                    doneData = response.data.data
                    updateTaskDoneInLocal(
                        doneData,
                        taskDao,
                        inboxV2Dao,
                        sessionManager,
                        drawingPinsDao
                    )
                    loading(false, "")
                    onBack()
                    isSuccess = true
                }

                is ApiResponse.Error -> {
                    loading(false, response.error.message)
                }
            }
        }
    }

}