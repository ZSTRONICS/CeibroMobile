package com.zstronics.ceibro.ui.tasks.v2.taskdetail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.github.tntkhang.fullscreenimageview.library.FullScreenImageViewActivity
import com.zstronics.ceibro.CeibroApplication
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.database.dao.DownloadedDrawingV2Dao
import com.zstronics.ceibro.data.database.dao.DrawingPinsV2Dao
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
import com.zstronics.ceibro.ui.attachment.imageExtensions
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TaskDetailV2VM @Inject constructor(
    override val viewState: TaskDetailV2State,
    val sessionManager: SessionManager,
    private val taskRepository: ITaskRepository,
    private val remoteTask: TaskRemoteDataSource,
    val dashboardRepository: IDashboardRepository,
    val taskDao: TaskV2Dao,
    private val inboxV2Dao: InboxV2Dao,
    val drawingPinsDao: DrawingPinsV2Dao,
    val downloadedDrawingV2Dao:DownloadedDrawingV2Dao
) : HiltBaseViewModel<ITaskDetailV2.State>(), ITaskDetailV2.ViewModel {
    val user = sessionManager.getUser().value

    val _taskDetail: MutableLiveData<CeibroTaskV2> = MutableLiveData()
    val taskDetail: LiveData<CeibroTaskV2> = _taskDetail
    val originalTask: MutableLiveData<CeibroTaskV2> = MutableLiveData()

    private val _onlyImages: MutableLiveData<ArrayList<TaskFiles>> = MutableLiveData(arrayListOf())
    val onlyImages: MutableLiveData<ArrayList<TaskFiles>> = _onlyImages

    private val _imagesWithComments: MutableLiveData<ArrayList<TaskFiles>> =
        MutableLiveData(arrayListOf())
    val imagesWithComments: MutableLiveData<ArrayList<TaskFiles>> = _imagesWithComments

    private val _documents: MutableLiveData<ArrayList<TaskFiles>> = MutableLiveData(arrayListOf())
    val documents: MutableLiveData<ArrayList<TaskFiles>> = _documents

    val _taskEvents: MutableLiveData<MutableList<Events>> = MutableLiveData()
    val taskEvents: MutableLiveData<MutableList<Events>> = _taskEvents
    val originalEvents: MutableLiveData<MutableList<Events>> = MutableLiveData(mutableListOf())

    private val _missingEvents: MutableLiveData<MutableList<Events>> =
        MutableLiveData(mutableListOf())
    val missingEvents: MutableLiveData<MutableList<Events>> = _missingEvents

    var notificationTaskData: MutableLiveData<NotificationTaskData?> = MutableLiveData()
    var isTaskBeingDone: MutableLiveData<Boolean> = MutableLiveData(false)

    var rootState = ""
    var selectedState = ""
    var taskId: String = ""
    var descriptionExpanded = false

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
            val events = CeibroApplication.CookiesManager.taskDetailEvents
            val parentRootState = CeibroApplication.CookiesManager.taskDetailRootState
            val parentSelectedState = CeibroApplication.CookiesManager.taskDetailSelectedSubState
            if (parentRootState != null) {
                rootState = parentRootState
            }
            if (parentSelectedState != null) {
                selectedState = parentSelectedState
            }

            val notificationData: NotificationTaskData? = bundle?.getParcelable("notificationTaskData")

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
                        _taskDetail.postValue(task1)
                        originalTask.postValue(task1)
                        getAllEvents(task1.id)
                        syncEvents(task1.id)

                        val seenByMe = task1.seenBy.find { it1 -> it1 == user?.id }
                        if (seenByMe == null) {
                            taskSeen(task1.id) { }
                        }
                    } ?: run {
                        // run API call because task not found in DB
                        getTaskById(taskId) { isSuccess, task, events ->
                            if (isSuccess) {
                                isTaskBeingDone.postValue(false)
                                _taskDetail.postValue(task)
                                originalTask.postValue(task)
                                originalEvents.postValue(events.toMutableList())
                                _taskEvents.postValue(events.toMutableList())

                                val seenByMe = task?.seenBy?.find { it1 -> it1 == user?.id }
                                if (seenByMe == null) {
                                    taskSeen(taskId) { }
                                }
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

                    val seenByMe = task.seenBy.find { it == user?.id }
                    if (seenByMe == null) {
                        taskSeen(task.id) { }
                    }
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
                    println("Heartbeat taskSeenData: ${taskSeenData}")
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

                } else {
                    println("Heartbeat taskSeenData: ${taskSeenData}")
                    //loading(false, "")
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
                    updateTaskDoneInLocal(doneData, taskDao, sessionManager, drawingPinsDao)
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


    fun isSameTask(newEvent: Events, taskId: String) = newEvent.taskId == taskId

//    override fun onCleared() {
//        super.onCleared()
//        EventBus.getDefault().unregister(this)
//    }


    fun openImageViewer(context: Context, fileUrl: ArrayList<String>?, position: Int) {
        if (!fileUrl.isNullOrEmpty()) {
            val fileExtension = fileUrl[0].substringAfterLast(".")
            if (imageExtensions.contains(".$fileExtension")) {
                val fullImageIntent = Intent(
                    context,
                    FullScreenImageViewActivity::class.java
                )

                val uriString: ArrayList<String> = arrayListOf()
                uriString.addAll(fileUrl)
                fullImageIntent.putExtra(FullScreenImageViewActivity.URI_LIST_DATA, uriString)

                fullImageIntent.putExtra(
                    FullScreenImageViewActivity.IMAGE_FULL_SCREEN_CURRENT_POS, position
                )
                context.startActivity(fullImageIntent)
            } else {
                // Handle other file types
            }
        }
    }


//    fun getTaskWithUpdatedTimeStamp(
//        timeStamp: String,
//        onLoggedIn: () -> Unit
//    ) {
//
//        launch {
//            loading(true)
//
//            taskRepository.getTaskWithUpdatedTimeStamp(timeStamp) { isSuccess, taskEvents, message ->
//                if (isSuccess) {
//
//                } else {
//
//                }
//            }
//        }
//    }
}