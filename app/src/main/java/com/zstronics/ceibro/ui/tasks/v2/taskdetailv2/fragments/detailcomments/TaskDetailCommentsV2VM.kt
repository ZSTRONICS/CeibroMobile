package com.zstronics.ceibro.ui.tasks.v2.taskdetailv2.fragments.detailcomments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.zstronics.ceibro.CeibroApplication
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.cancelAndMakeToast
import com.zstronics.ceibro.base.viewmodel.Dispatcher
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.database.dao.DownloadedDrawingV2Dao
import com.zstronics.ceibro.data.database.dao.DrawingPinsV2Dao
import com.zstronics.ceibro.data.database.dao.GroupsV2Dao
import com.zstronics.ceibro.data.database.dao.InboxV2Dao
import com.zstronics.ceibro.data.database.dao.TaskV2Dao
import com.zstronics.ceibro.data.database.models.tasks.CeibroTaskV2
import com.zstronics.ceibro.data.database.models.tasks.Events
import com.zstronics.ceibro.data.remote.TaskRemoteDataSource
import com.zstronics.ceibro.data.repos.dashboard.IDashboardRepository
import com.zstronics.ceibro.data.repos.dashboard.attachment.AttachmentTags
import com.zstronics.ceibro.data.repos.task.ITaskRepository
import com.zstronics.ceibro.data.repos.task.TaskRootStateTags
import com.zstronics.ceibro.data.repos.task.models.v2.EventCommentOnlyUploadV2Request
import com.zstronics.ceibro.data.repos.task.models.v2.EventV2Response
import com.zstronics.ceibro.data.repos.task.models.v2.EventWithFileUploadV2Request
import com.zstronics.ceibro.data.repos.task.models.v2.SyncTaskEventsBody
import com.zstronics.ceibro.data.repos.task.models.v2.TaskDetailEvents
import com.zstronics.ceibro.data.repos.task.models.v2.TaskSeenResponse
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.ui.socket.LocalEvents
import com.zstronics.ceibro.ui.tasks.v2.newtask.CreateNewTaskService
import com.zstronics.ceibro.ui.tasks.v2.taskdetail.comment.CommentVM
import dagger.hilt.android.lifecycle.HiltViewModel
import ee.zstronics.ceibro.camera.AttachmentTypes
import ee.zstronics.ceibro.camera.PickedImages
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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
    var taskFromNotification: CeibroTaskV2? = null

    var rootState = ""
    var selectedState = ""
    var taskId: String = ""
    var isResumedCalled = false

    var isTaskScrolled: Events? =null

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
                taskFromNotification = taskDataFromNotification
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

//                    if (!events.isNullOrEmpty()) {
//                        originalEvents.postValue(events.toMutableList())
//                        _taskEvents.postValue(events.toMutableList())
//                    }
//                    else {
//                        if (task.eventsCount > 0) {
                            getAllEvents(task.id)
//                        } else {
//                            originalEvents.postValue(mutableListOf<Events>())
//                            _taskEvents.postValue(mutableListOf<Events>())
//                        }
//                    }
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

    fun pinOrUnpinComment(
        taskId: String,
        eventId: String,
        isPinned: Boolean,
        callBack: (isSuccess: Boolean, event: Events?) -> Unit
    ) {
        launch {
            loading(true)
            when (val response = remoteTask.pinOrUnpinComment(taskId, eventId, isPinned)) {
                is ApiResponse.Success -> {
                    val commentPinnedData = response.data.data
                    val event =
                        taskDao.getSingleEvent(commentPinnedData.taskId, commentPinnedData.eventId)
                    if (event != null) {
                        event.isPinned = commentPinnedData.isPinned
                        event.updatedAt = commentPinnedData.updatedAt

                        taskDao.insertEventData(event)
                    }

                    loading(false, "")
                    callBack.invoke(true, event)
                }

                is ApiResponse.Error -> {
                    loading(false, response.error.message)
                    callBack.invoke(false, null)
                }
            }
        }
    }


    fun uploadComment(
        context: Context,
        onBack: (eventData: EventV2Response.Data?) -> Unit
    ) {
        val list = getCombinedList()
        if (viewState.comment.value.toString().trim() == "" && list.isEmpty()) {
            alert(context.getString(R.string.please_add_comment_or_files))
        } else {
            GlobalScope.launch {
                var eventData: EventV2Response.Data? = null
                var isSuccess = false

                if (list.isNotEmpty()) {
                    loading(true)
                    val attachmentUriList = list.map {
                        it.file
                    }
                    val metaData = list.map { file ->
                        var tag = ""
                        if (file.attachmentType == AttachmentTypes.Image) {
                            tag = if (file.comment.isNotEmpty()) {
                                AttachmentTags.ImageWithComment.tagValue
                            } else {
                                AttachmentTags.Image.tagValue
                            }
                        } else if (file.attachmentType == AttachmentTypes.Pdf || file.attachmentType == AttachmentTypes.Doc) {
                            tag = AttachmentTags.File.tagValue
                        }

                        EventWithFileUploadV2Request.AttachmentMetaData(
                            fileName = file.fileName,
                            orignalFileName = file.fileName,
                            tag = tag,
                            comment = file.comment.trim()
                        )
                    }
                    val metadataString = Gson().toJson(metaData)
                    val metadataString2 =
                        Gson().toJson(metadataString)     //again passing to make the json to convert into json string with slashes

                    val request = EventWithFileUploadV2Request(
                        files = attachmentUriList,
                        message = viewState.comment.value.toString(),
                        metadata = metadataString2
                    )

                    eventData = null

                    CommentVM.eventWithFileUploadV2RequestData = request
                    val serviceIntent = Intent(context, CreateNewTaskService::class.java)
                    serviceIntent.putExtra("ServiceRequest", "commentRequest")
                    serviceIntent.putExtra("taskId", taskId)
                    serviceIntent.putExtra("event", TaskDetailEvents.Comment.eventValue)
                    val bundle = Bundle()
                    bundle.putParcelableArray("uploadingFileList", list.toTypedArray())
                    serviceIntent.putExtra("uploadingFileBundle", bundle)
                    context.startService(serviceIntent)

                    loading(false, "")
                    launch(Dispatcher.Main) {
                        onBack.invoke(eventData)
                    }


//                    when (val response = dashboardRepository.uploadEventWithFilesV2(
//                        event = TaskDetailEvents.Comment.eventValue,
//                        taskId = taskId ?: "",
//                        hasFiles = true,
//                        eventWithFileUploadV2Request = request
//                    )) {
//                        is ApiResponse.Success -> {
//                            val commentData = response.data.data
//                            isSuccess = true
//                            eventData = commentData
//                        }
//
//                        is ApiResponse.Error -> {
//                            cancelAndMakeToast(context, response.error.message, Toast.LENGTH_SHORT)
//                        }
//                    }

                } else {        //if list is empty, moving to else part
                    val request = EventCommentOnlyUploadV2Request(
                        message = viewState.comment.value.toString()
                    )

                    loading(true)
                    when (val response = dashboardRepository.uploadEventWithoutFilesV2(
                        event = TaskDetailEvents.Comment.eventValue,
                        taskId = taskId ?: "",
                        hasFiles = false,
                        eventCommentOnlyUploadV2Request = request
                    )) {
                        is ApiResponse.Success -> {
                            val commentData = response.data.data
                            isSuccess = true
                            eventData = commentData
                        }

                        is ApiResponse.Error -> {
                            isSuccess = false
                            launch(Dispatcher.Main) {
                                cancelAndMakeToast(
                                    context,
                                    response.error.message,
                                    Toast.LENGTH_SHORT
                                )
                            }
                        }
                    }
                }
                updateTaskCommentInLocal(
                    eventData,
                    taskDao,
                    inboxV2Dao,
                    user?.id,
                    sessionManager,
                    drawingPinsDao
                )

                Handler(Looper.getMainLooper()).postDelayed({
                    loading(false, "")
                    if (isSuccess) {
                        onBack(eventData)
                    }
                }, 10)

            }
        }
    }

    private fun getCombinedList(): ArrayList<PickedImages> {
        val listOfImages = listOfImages.value
        val documents = documents.value
        val combinedList = arrayListOf<PickedImages>()
        if (listOfImages != null) {
            combinedList.addAll(listOfImages)
        }
        if (documents != null) {
            combinedList.addAll(documents)
        }
        return combinedList
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

     fun syncEventsOne(
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


     fun syncEventsOnFragmentResume(
        taskId: String
    ) {

     }


     fun syncEvents(
        taskId: String
    ) {
        launch {
            val allEvents = taskDao.getEventsOfTask(taskId).toMutableList()
//            allEvents.sortedByDescending { it.createdAt }
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
                            val allEventsSorted =
                                allEvents.sortedByDescending { it.createdAt }
                                    .toMutableList()
                            originalEvents.postValue(allEventsSorted)
                            _taskEvents.postValue(allEventsSorted)

                        } else {
                            allEvents.addAll(missingEvents)
                            val allEventsSorted =
                                allEvents.sortedByDescending { it.createdAt }
                                    .toMutableList()
                            originalEvents.postValue(allEventsSorted)
                            _taskEvents.postValue(allEventsSorted)
                        }
                        launch {
                            taskDao.insertMultipleEvents(missingEvents)
                        }
                    } else {
//                        originalEvents.postValue(allEvents)
//                        _taskEvents.postValue(allEvents)
                    }
                } else {
                    alert("Failed to sync task events")
//                    originalEvents.postValue(allEvents)
//                    _taskEvents.postValue(allEvents)
                }
            }
        }
    }

}