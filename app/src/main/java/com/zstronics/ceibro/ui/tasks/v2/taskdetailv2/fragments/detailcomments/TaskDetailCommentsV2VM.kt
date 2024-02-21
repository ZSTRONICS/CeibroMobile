package com.zstronics.ceibro.ui.tasks.v2.taskdetailv2.fragments.detailcomments

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
import ee.zstronics.ceibro.camera.PickedImages
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

@HiltViewModel
class TaskDetailCommentsV2VM @Inject constructor(
    override val viewState: TaskDetailCommentsV2State,
    val sessionManager: SessionManager,
    private val taskRepository: ITaskRepository,
    private val remoteTask: TaskRemoteDataSource,
    val dashboardRepository: IDashboardRepository,
    val taskDao: TaskV2Dao,
    val groupsV2Dao: GroupsV2Dao,
    private val inboxV2Dao: InboxV2Dao,
    val drawingPinsDao: DrawingPinsV2Dao,
    val downloadedDrawingV2Dao: DownloadedDrawingV2Dao
) : HiltBaseViewModel<ITaskDetailCommentsV2.State>(), ITaskDetailCommentsV2.ViewModel {
    val user = sessionManager.getUser().value
    val listOfImages: MutableLiveData<ArrayList<PickedImages>> = MutableLiveData(arrayListOf())
    val documents: MutableLiveData<ArrayList<PickedImages>> = MutableLiveData(arrayListOf())

    val _taskDetail: MutableLiveData<CeibroTaskV2> = MutableLiveData()
    val taskDetail: LiveData<CeibroTaskV2> = _taskDetail
    val originalTask: MutableLiveData<CeibroTaskV2> = MutableLiveData()

    val _taskEvents: MutableLiveData<MutableList<Events>> = MutableLiveData()
    val taskEvents: MutableLiveData<MutableList<Events>> = _taskEvents
    val originalEvents: MutableLiveData<MutableList<Events>> = MutableLiveData(mutableListOf())

    private val _missingEvents: MutableLiveData<MutableList<Events>> =
        MutableLiveData(mutableListOf())
    val missingEvents: MutableLiveData<MutableList<Events>> = _missingEvents

    var isTaskBeingDone: MutableLiveData<Boolean> = MutableLiveData(false)

    var rootState = ""
    var selectedState = ""
    var taskId: String = ""


    override fun onFirsTimeUiCreate(bundle: Bundle?) {
        super.onFirsTimeUiCreate(bundle)

        launch {
            val taskData: CeibroTaskV2? = CeibroApplication.CookiesManager.taskDataForDetails
            val events = CeibroApplication.CookiesManager.taskDetailEvents
            val parentRootState = CeibroApplication.CookiesManager.taskDetailRootState
            val parentSelectedState = CeibroApplication.CookiesManager.taskDetailSelectedSubState
            if (parentRootState != null) {
                rootState = parentRootState
            }
            if (parentSelectedState != null) {
                selectedState = parentSelectedState
            }

            val taskDataFromNotification: CeibroTaskV2? =
                CeibroApplication.CookiesManager.taskDataForDetailsFromNotification

            if (taskDataFromNotification != null) {         //It means detail is opened via notification if not null

                taskId = taskDataFromNotification.id
                isTaskBeingDone.postValue(taskDataFromNotification.isBeingDoneByAPI)
                rootState = TaskRootStateTags.ToMe.tagValue
                _taskDetail.postValue(taskDataFromNotification!!)
                originalTask.postValue(taskDataFromNotification!!)

                getAllEvents(taskDataFromNotification.id)
                syncEvents(taskDataFromNotification.id)

            } else {
                taskData?.let { task ->
                    taskId = task.id
                    val isTaskBeingDone1 = taskDao.getTaskIsBeingDoneByAPI(task.id)
                    isTaskBeingDone.postValue(isTaskBeingDone1)
                    _taskDetail.postValue(task)
                    originalTask.postValue(task)
                    if (!events.isNullOrEmpty()) {
                        originalEvents.postValue(events.toMutableList())
                        _taskEvents.postValue(events.toMutableList())
                    } else {
                        if (task.eventsCount > 0) {
                            getAllEvents(task.id)
                        } else {
                            originalEvents.postValue(mutableListOf<Events>())
                            _taskEvents.postValue(mutableListOf<Events>())
                        }
                    }
                    syncEvents(task.id)

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

    fun getAllEventsFromLocalEvents() {
        launch {
            if (taskId.isNotEmpty()) {
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
    }

    fun updateTaskAndAllEvents(taskEvent: Events, allEvents: MutableList<Events>) {
        launch {
            val task = taskDao.getTaskByID(taskEvent.taskId)
            task?.let {
                originalTask.postValue(it)
                _taskDetail.postValue(it)
            }
            originalEvents.postValue(allEvents)
            _taskEvents.postValue(allEvents)

            if (taskEvent.initiator.id != user?.id) {
                val seenByMe = task?.seenBy?.find { it == user?.id }
                if (seenByMe == null) {
                    taskSeen(taskEvent.taskId) { }
                } else {
                    launch {
                        val inboxTask = inboxV2Dao.getInboxTaskData(task.id)
                        if (inboxTask != null && !inboxTask.isSeen) {
                            inboxTask.isSeen = true
                            inboxTask.unSeenNotifCount = 0
                            inboxV2Dao.insertInboxItem(inboxTask)

                            EventBus.getDefault().post(LocalEvents.UpdateInboxItemSeen(inboxTask))
                        }
                    }
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

    fun taskSeen(
        taskId: String,
        onBack: (taskSeenData: TaskSeenResponse.TaskSeen) -> Unit,
    ) {
        launch {
            //loading(true)
            taskRepository.taskSeen(taskId) { isSuccess, taskSeenData ->
                if (isSuccess) {
                    if (taskSeenData != null) {
                        launch {
                            updateGenericTaskSeenInLocal(
                                taskSeenData,
                                taskDao,
                                user?.id,
                                sessionManager,
                                drawingPinsDao,
                                inboxV2Dao
                            )
                        }
                        onBack(taskSeenData)
                    }
                }
            }
        }
    }

    private fun syncEvents(
        taskId: String
    ) {
        launch {
            val allEvents = taskDao.getEventsOfTask(taskId)
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
                        _missingEvents.postValue(missingEvents.toMutableList())
                        launch {
                            taskDao.insertMultipleEvents(missingEvents)
                        }
                    }
                } else {
                    alert("Failed to sync task events")
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


    fun markInboxTaskSeen(taskId: String?) {
        launch {
            if (!taskId.isNullOrEmpty()) {
                val inboxTask = inboxV2Dao.getInboxTaskData(taskId)
                if (inboxTask != null) {
                    inboxTask.isSeen = true
                    inboxTask.unSeenNotifCount = 0

                    inboxV2Dao.insertInboxItem(inboxTask)

                    EventBus.getDefault().post(LocalEvents.UpdateInboxItemSeen(inboxTask))
                }
            }
        }
    }


}