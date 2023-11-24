package com.zstronics.ceibro.ui.tasks.v2.newtask

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import androidx.room.RoomDatabase
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.host.NavHostPresenterActivity
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.base.CookiesManager
import com.zstronics.ceibro.data.database.CeibroDatabase
import com.zstronics.ceibro.data.database.dao.DraftNewTaskV2Dao
import com.zstronics.ceibro.data.database.dao.TaskV2Dao
import com.zstronics.ceibro.data.database.models.tasks.CeibroTaskV2
import com.zstronics.ceibro.data.database.models.tasks.Events
import com.zstronics.ceibro.data.repos.dashboard.DashboardRepository
import com.zstronics.ceibro.data.repos.task.TaskRepository
import com.zstronics.ceibro.data.repos.task.models.v2.EventV2Response
import com.zstronics.ceibro.data.repos.task.models.v2.EventWithFileUploadV2Request
import com.zstronics.ceibro.data.repos.task.models.v2.LocalFilesToStore
import com.zstronics.ceibro.data.repos.task.models.v2.NewTaskV2Entity
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.data.sessions.SharedPreferenceManager
import com.zstronics.ceibro.ui.dashboard.SharedViewModel
import com.zstronics.ceibro.ui.dashboard.TaskEventsList
import com.zstronics.ceibro.ui.networkobserver.NetworkConnectivityObserver
import com.zstronics.ceibro.ui.socket.LocalEvents
import com.zstronics.ceibro.ui.socket.SocketHandler
import com.zstronics.ceibro.ui.tasks.task.TaskStatus
import com.zstronics.ceibro.ui.tasks.v2.newtask.CreateNewTaskService.NotificationUtils.CHANNEL_ID
import com.zstronics.ceibro.ui.tasks.v2.newtask.CreateNewTaskService.NotificationUtils.CHANNEL_NAME
import com.zstronics.ceibro.ui.tasks.v2.newtask.NewTaskV2VM.Companion.taskList
import com.zstronics.ceibro.ui.tasks.v2.newtask.NewTaskV2VM.Companion.taskRequest
import com.zstronics.ceibro.ui.tasks.v2.taskdetail.comment.CommentVM.Companion.eventWithFileUploadV2RequestData
import com.zstronics.ceibro.utils.FileUtils
import dagger.hilt.android.AndroidEntryPoint
import ee.zstronics.ceibro.camera.PickedImages
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

@AndroidEntryPoint
class CreateNewTaskService : Service() {

    private var taskCounter = 0
    private var draftTasksFailed = 0
    private var taskObjectData: NewTaskV2Entity? = null
    private var taskListData: ArrayList<PickedImages>? = null
    private var sessionManager: SessionManager? = null
    private var mContext: Context? = null
    private val errorTaskNotificationID = 111
    private val createTaskNotificationID = 1
    private val doneNotificationID = 2
    private val commentNotificationID = 3
    private val draftCreateTaskNotificationID = 4

    @Inject
    lateinit var taskRepository: TaskRepository

    @Inject
    lateinit var dashboardRepository: DashboardRepository

    @Inject
    lateinit var draftNewTaskV2DaoInternal: DraftNewTaskV2Dao

    @Inject
    lateinit var taskDaoInternal: TaskV2Dao

    @Inject
    lateinit var networkConnectivityObserver: NetworkConnectivityObserver

    override fun onCreate() {

        super.onCreate()
        println("Service Status .. onCreate state...")
        taskObjectData = taskRequest
        taskListData = taskList
        mContext = this

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        sessionManager = getSessionManager(SharedPreferenceManager(this))
        val request = intent?.getStringExtra("ServiceRequest")
        val taskId = intent?.getStringExtra("taskId")
        val event = intent?.getStringExtra("event")

        when (request) {
            "commentRequest" -> {

                taskId?.let { taskId ->
                    event?.let { event ->
                        sessionManager?.let { sessionManager ->
                            eventWithFileUploadV2RequestData?.let {

                                startForeground(
                                    commentNotificationID,
                                    createIndeterminateNotificationForFileUpload(
                                        context = this,
                                        channelId = CHANNEL_ID,
                                        channelName = CHANNEL_NAME,
                                        notificationTitle = "Replying task with files",
                                        notificationID = commentNotificationID
                                    )
                                )
                                uploadComment(it, taskId, event, sessionManager, this)

                            }
                        }
                    }
                }
            }

            "taskRequest" -> {

                startForeground(
                    createTaskNotificationID, createIndeterminateNotificationForFileUpload(
                        context = this,
                        channelId = CHANNEL_ID,
                        channelName = CHANNEL_NAME,
                        notificationTitle = "Creating task with files",
                        notificationID = createTaskNotificationID
                    )
                )
                createTask(sessionManager, this)
            }

            "draftUploadRequest" -> {

                GlobalScope.launch {
                    val unSyncedRecords =
                        draftNewTaskV2DaoInternal.getUnSyncedRecords() ?: emptyList()

                    if (unSyncedRecords.isNotEmpty()) {
                        startForeground(
                            draftCreateTaskNotificationID,
                            createIndeterminateNotificationForFileUpload(
                                context = this@CreateNewTaskService,
                                channelId = CHANNEL_ID,
                                channelName = CHANNEL_NAME,
                                notificationTitle = "Uploading draft tasks",
                                notificationID = draftCreateTaskNotificationID
                            )
                        )
                        sessionManager?.let { sessionManager ->
                            mContext?.let { mContext ->
                                syncDraftTask(sessionManager, mContext)
                            }
                        }
                    } else {

                        hideIndeterminateNotifications(
                            this@CreateNewTaskService,
                            draftCreateTaskNotificationID
                        )
                        stopSelf()
                    }
                }
            }

            "doneRequest" -> {

                taskId?.let { taskId ->
                    event?.let { event ->
                        sessionManager?.let { sessionManager ->
                            eventWithFileUploadV2RequestData?.let {

                                startForeground(
                                    doneNotificationID,
                                    createIndeterminateNotificationForFileUpload(
                                        context = this@CreateNewTaskService,
                                        channelId = CHANNEL_ID,
                                        channelName = CHANNEL_NAME,
                                        notificationTitle = "Marking task as done with files",
                                        notificationID = doneNotificationID
                                    )
                                )
                                uploadComment(it, taskId, event, sessionManager, this)

                            }
                        }
                    }
                }

            }
            // Add more cases if needed
            else -> {
                // Handle the default case or add additional cases as needed
            }
        }

        return START_STICKY
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun createTask(sessionManager: SessionManager?, context: Context) {

        taskObjectData?.let { taskRequestData ->
            taskListData?.let { taskListData ->
                GlobalScope.launch {
                    sessionManager?.let {
                        createTaskWithFiles(taskRequestData, taskListData, sessionManager, context)
                    }
                }
            }
        }
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


    private fun updateCreatedTaskInLocal(
        task: CeibroTaskV2?, taskDao: TaskV2Dao, sessionManager: SessionManager
    ) {
        val sharedViewModel = NavHostPresenterActivity.activityInstance?.let {
            ViewModelProvider(it).get(SharedViewModel::class.java)
        }

        task?.let { newTask ->
            GlobalScope.launch {
                sessionManager.saveUpdatedAtTimeStamp(newTask.updatedAt)
                taskDao.insertTaskData(newTask)

                if (newTask.isCreator) {
                    when (newTask.fromMeState) {
                        TaskStatus.UNREAD.name.lowercase() -> {
                            val allFromMeUnreadTasks =
                                CookiesManager.fromMeUnreadTasks.value ?: mutableListOf()
                            val foundTask = allFromMeUnreadTasks.find { it.id == newTask.id }
                            if (foundTask != null) {
                                val index = allFromMeUnreadTasks.indexOf(foundTask)
                                allFromMeUnreadTasks.removeAt(index)
                            }
                            allFromMeUnreadTasks.add(newTask)
                            val unreadTasks =
                                allFromMeUnreadTasks.sortedByDescending { it.updatedAt }
                                    .toMutableList()
                            CookiesManager.fromMeUnreadTasks.postValue(unreadTasks)
                        }

                        TaskStatus.ONGOING.name.lowercase() -> {
                            val allFromMeOngoingTasks =
                                CookiesManager.fromMeOngoingTasks.value ?: mutableListOf()
                            val foundTask = allFromMeOngoingTasks.find { it.id == newTask.id }
                            if (foundTask != null) {
                                val index = allFromMeOngoingTasks.indexOf(foundTask)
                                allFromMeOngoingTasks.removeAt(index)
                            }
                            allFromMeOngoingTasks.add(newTask)
                            val ongoingTasks =
                                allFromMeOngoingTasks.sortedByDescending { it.updatedAt }
                                    .toMutableList()
                            CookiesManager.fromMeOngoingTasks.postValue(ongoingTasks)
                        }

                        TaskStatus.DONE.name.lowercase() -> {
                            val allFromMeDoneTasks =
                                CookiesManager.fromMeDoneTasks.value ?: mutableListOf()
                            val foundTask = allFromMeDoneTasks.find { it.id == newTask.id }
                            if (foundTask != null) {
                                val index = allFromMeDoneTasks.indexOf(foundTask)
                                allFromMeDoneTasks.removeAt(index)
                            }
                            allFromMeDoneTasks.add(newTask)
                            val doneTasks = allFromMeDoneTasks.sortedByDescending { it.updatedAt }
                                .toMutableList()
                            CookiesManager.fromMeDoneTasks.postValue(doneTasks)
                        }
                    }
                }

                if (newTask.isAssignedToMe) {
                    when (newTask.toMeState) {
                        TaskStatus.NEW.name.lowercase() -> {
                            val allToMeNewTasks =
                                CookiesManager.toMeNewTasks.value ?: mutableListOf()
                            val foundTask = allToMeNewTasks.find { it.id == newTask.id }
                            if (foundTask != null) {
                                val index = allToMeNewTasks.indexOf(foundTask)
                                allToMeNewTasks.removeAt(index)
                            }
                            allToMeNewTasks.add(newTask)
                            val newTasks =
                                allToMeNewTasks.sortedByDescending { it.updatedAt }.toMutableList()
                            CookiesManager.toMeNewTasks.postValue(newTasks)
                        }

                        TaskStatus.ONGOING.name.lowercase() -> {
                            val allToMeOngoingTasks =
                                CookiesManager.toMeOngoingTasks.value ?: mutableListOf()
                            val foundTask = allToMeOngoingTasks.find { it.id == newTask.id }
                            if (foundTask != null) {
                                val index = allToMeOngoingTasks.indexOf(foundTask)
                                allToMeOngoingTasks.removeAt(index)
                            }
                            allToMeOngoingTasks.add(newTask)
                            val ongoingTasks =
                                allToMeOngoingTasks.sortedByDescending { it.updatedAt }
                                    .toMutableList()
                            CookiesManager.toMeOngoingTasks.postValue(ongoingTasks)
                        }

                        TaskStatus.DONE.name.lowercase() -> {
                            val allToMeDoneTasks =
                                CookiesManager.toMeDoneTasks.value ?: mutableListOf()
                            val foundTask = allToMeDoneTasks.find { it.id == newTask.id }
                            if (foundTask != null) {
                                val index = allToMeDoneTasks.indexOf(foundTask)
                                allToMeDoneTasks.removeAt(index)
                            }
                            allToMeDoneTasks.add(newTask)
                            val doneTasks =
                                allToMeDoneTasks.sortedByDescending { it.updatedAt }.toMutableList()
                            CookiesManager.toMeDoneTasks.postValue(doneTasks)
                        }
                    }
                    sharedViewModel?.isToMeUnread?.value = true
                    sessionManager.saveToMeUnread(true)
                }

                EventBus.getDefault().post(LocalEvents.RefreshTasksData())
            }
        }
    }

    private fun providesAppDatabase(context: Context): CeibroDatabase {
        return Room.databaseBuilder(context, CeibroDatabase::class.java, CeibroDatabase.DB_NAME)
            .addCallback(object : RoomDatabase.Callback() {
            })
            .fallbackToDestructiveMigration().build()
    }

    private fun getSessionManager(
        sharedPreferenceManager: SharedPreferenceManager
    ) = SessionManager(sharedPreferenceManager)

    private fun hideIndeterminateNotifications(activity: Context, notificationID: Int) {
        val notificationManager = activity.getSystemService(NotificationManager::class.java)
        notificationManager.cancel(notificationID) // Remove the notification with ID
    }

    object NotificationUtils {
        const val CHANNEL_ID = "file_upload_channel"
        const val CHANNEL_NAME = "Create Progress Channel"
    }

    private fun createIndeterminateNotificationForFileUpload(
        context: CreateNewTaskService,
        channelId: String,
        channelName: String,
        notificationTitle: String,
        isOngoing: Boolean = true,
        indeterminate: Boolean = true,
        notificationIcon: Int = R.drawable.icon_upload,
        notificationID: Int
    ): Notification {
        val channel =
            NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)

        val builder = NotificationCompat.Builder(context, channelId).setSmallIcon(notificationIcon)
            .setContentTitle(notificationTitle).setOngoing(isOngoing)
            .setProgress(0, 0, indeterminate)

        notificationManager.notify(notificationID, builder.build())
        return builder.build()
    }

    private fun createSimpleNotification(
        context: CreateNewTaskService,
        channelId: String,
        channelName: String,
        notificationTitle: String,
        isOngoing: Boolean = false,
        indeterminate: Boolean = false,
        notificationIcon: Int = R.drawable.icon_upload
    ): Notification {
        val channel =
            NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(notificationIcon)
            .setContentTitle(notificationTitle)
            .setOngoing(isOngoing)
            .setOnlyAlertOnce(true)
        if (isOngoing) {
            builder.setProgress(100, 1, indeterminate)
        }

        notificationManager.notify(errorTaskNotificationID, builder.build())
        return builder.build()
    }

    private suspend fun saveFailedTaskInDraft(
        newTaskRequest: NewTaskV2Entity,
        list: ArrayList<PickedImages>,
        errorMessage: String
    ) {
        val localFilesData = list.map {
            LocalFilesToStore(
                fileUri = it.fileUri.toString(),
                comment = it.comment,
                fileName = it.fileName,
                fileSizeReadAble = it.fileSizeReadAble,
                editingApplied = it.editingApplied,
                attachmentType = it.attachmentType
            )
        }
        newTaskRequest.apply {
            this.filesData = localFilesData
            this.isNewTaskCreationFailed = true
            this.taskCreationFailedError = errorMessage
        }
        // Use the IO dispatcher for database operations
        withContext(Dispatchers.IO) {
            // Insert or replace the data into the database
            draftNewTaskV2DaoInternal.upsert(newTaskRequest)

            HiltBaseViewModel.syncDraftRecords.postValue(draftNewTaskV2DaoInternal.getCountOfDraftRecords())
        }
    }

    private suspend fun createTaskWithFiles(
        newTask: NewTaskV2Entity,
        list: ArrayList<PickedImages>,
        sessionManager: SessionManager,
        context: Context
    ) {

        taskCounter += 1
        taskRepository.newTaskV2WithFiles(
            newTask,
            list
        ) { isSuccess, task, errorMessage ->
            if (isSuccess) {
                val room = providesAppDatabase(context)
                val taskDao = room.getTaskV2sDao()
                this.sessionManager?.let {
                    updateCreatedTaskInLocal(task, taskDao, sessionManager)
                }
                hideIndeterminateNotifications(context, createTaskNotificationID)
                println("Service Status...:Create task with success")
                taskCounter -= 1
                if (taskCounter <= 0) {
                    stopSelf()
                }
            } else {
                hideIndeterminateNotifications(context, createTaskNotificationID)

                GlobalScope.launch {
                    saveFailedTaskInDraft(newTask, list, errorMessage)
                }
                createSimpleNotification(
                    context = this,
                    channelId = CHANNEL_ID,
                    channelName = CHANNEL_NAME,
                    notificationTitle = "Task creation un-successful"
                )

                println("Service Status...:Create task with failure -> $errorMessage")
                taskCounter -= 1
                if (taskCounter <= 0) {
                    stopSelf()
                }
            }
        }

    }

    private fun uploadComment(
        request: EventWithFileUploadV2Request,
        taskId: String,
        event: String,
        sessionManager: SessionManager,
        context: CreateNewTaskService
    ) {
        GlobalScope.launch {
            taskCounter += 1
            when (val response = dashboardRepository.uploadEventWithFilesV2(
                event = event,
                taskId = taskId,
                hasFiles = true,
                eventWithFileUploadV2Request = request
            )) {
                is ApiResponse.Success -> {

                    val room = providesAppDatabase(context)
                    val taskDao = room.getTaskV2sDao()

                    if (event == "comment") {

                        updateTaskCommentInLocal(
                            response.data.data, taskDao,
                            sessionManager
                        )
                        hideIndeterminateNotifications(context, commentNotificationID)
                    } else if (event == "doneTask") {
                        updateTaskDoneInLocal(response.data.data, taskDao, sessionManager)
                        hideIndeterminateNotifications(context, doneNotificationID)
                        taskDaoInternal.updateTaskIsBeingDoneByAPI(taskId, false)
                    }

                    println("Service Status...:Upload comment with success")
                    taskCounter -= 1
                    if (taskCounter <= 0) {
                        stopSelf()
                    }
                }

                is ApiResponse.Error -> {
                    if (event == "comment") {
                        hideIndeterminateNotifications(context, commentNotificationID)
                    } else if (event == "doneTask") {
                        hideIndeterminateNotifications(context, doneNotificationID)
                        taskDaoInternal.updateTaskIsBeingDoneByAPI(taskId, false)
                        EventBus.getDefault().post(LocalEvents.TaskFailedToDone())
                    }


                    println("Service Status...:Upload comment with failure -> ${response.error.message}")
                    taskCounter -= 1
                    if (taskCounter <= 0) {
                        stopSelf()
                    }
                }
            }
        }
    }

    private fun syncDraftTask(sessionManager: SessionManager, context: Context) {

        GlobalScope.launch {
            Log.d("SyncDraftTask", "syncDraftTask")
            val allUnSyncedRecords = draftNewTaskV2DaoInternal.getCountOfDraftRecords()
            val unSyncedRecords = draftNewTaskV2DaoInternal.getUnFailedDraftRecords() ?: emptyList()
            draftTasksFailed = allUnSyncedRecords - unSyncedRecords.size
            taskCounter++


            // Define a recursive function to process records one by one
            suspend fun processNextRecord(
                records: List<NewTaskV2Entity>,
                sessionManager: SessionManager
            ) {
                if (records.isEmpty()) {
                    println("Service Status .. drafts task failed $draftTasksFailed")
                    hideIndeterminateNotifications(context, draftCreateTaskNotificationID)
                    taskCounter--
                    if (taskCounter <= 0) {
                        stopSelf()
                    }
                    return
                }
                val newTaskRequest = records.first()
                var list: ArrayList<PickedImages> = arrayListOf()

                newTaskRequest.filesData?.let { filesData ->
                    list = filesData.map {
                        PickedImages(
                            fileUri = Uri.parse(it.fileUri),
                            comment = it.comment,
                            fileName = it.fileName,
                            fileSizeReadAble = it.fileSizeReadAble,
                            editingApplied = it.editingApplied,
                            attachmentType = it.attachmentType,
                            file = FileUtils.getFile(context, Uri.parse(it.fileUri))
                        )
                    } as ArrayList<PickedImages>
                }

                if (list.isNotEmpty()) {

                    taskRepository.newTaskV2WithFiles(
                        newTaskRequest, list
                    ) { isSuccess, task, errorMessage ->
                        if (isSuccess) {

                            GlobalScope.launch {
                                draftNewTaskV2DaoInternal.deleteTaskById(newTaskRequest.taskId)

                                updateCreatedTaskInLocal(
                                    task, taskDaoInternal,
                                    sessionManager
                                )
                                // Remove the processed record from the list
                                val updatedRecords = records - newTaskRequest


                                HiltBaseViewModel.syncDraftRecords.postValue(updatedRecords.size + draftTasksFailed)
                                processNextRecord(updatedRecords, sessionManager)

                            }
                        } else {
                            GlobalScope.launch {
                                if (errorMessage.contains(
                                        "No internet",
                                        true
                                    ) || errorMessage.contains(
                                        "connection abort",
                                        true
                                    ) || networkConnectivityObserver.isNetworkAvailable().not()
                                ) {
                                    println("draft tasks error1: $errorMessage")
                                    draftTasksFailed++
                                    val updatedRecords = records - newTaskRequest
                                    processNextRecord(updatedRecords, sessionManager)
                                } else {

                                    println("draft tasks error2: $errorMessage")
                                    draftTasksFailed++
                                    draftNewTaskV2DaoInternal.updateUnSyncedRecords(
                                        taskId = newTaskRequest.taskId,
                                        isDraftTaskCreationFailed = true,
                                        isNewTaskCreationFailed = true,
                                        taskCreationFailedError = errorMessage
                                    )
                                    val updatedRecords = records - newTaskRequest
                                    processNextRecord(updatedRecords, sessionManager)
                                }
                            }
                        }

                    }
                } else {

                    taskRepository.newTaskV2WithoutFiles(newTaskRequest) { isSuccess, task, errorMessage ->
                        if (isSuccess) {
                            GlobalScope.launch {
                                draftNewTaskV2DaoInternal.deleteTaskById(newTaskRequest.taskId)

                                updateCreatedTaskInLocal(
                                    task, taskDaoInternal,
                                    sessionManager
                                )
                                // Remove the processed record from the list
                                val updatedRecords = records - newTaskRequest


                                // Recursively process the next record
                                HiltBaseViewModel.syncDraftRecords.postValue(updatedRecords.size + draftTasksFailed)
                                processNextRecord(updatedRecords, sessionManager)


                            }
                        } else {
                            GlobalScope.launch {
                                if (errorMessage.contains(
                                        "No internet",
                                        true
                                    ) || networkConnectivityObserver.isNetworkAvailable().not()
                                ) {
                                    draftTasksFailed++
                                    val updatedRecords = records - newTaskRequest
                                    processNextRecord(updatedRecords, sessionManager)
                                } else {
                                    draftTasksFailed++
                                    draftNewTaskV2DaoInternal.updateUnSyncedRecords(
                                        taskId = newTaskRequest.taskId,
                                        isDraftTaskCreationFailed = true,
                                        isNewTaskCreationFailed = true,
                                        taskCreationFailedError = errorMessage
                                    )
                                    val updatedRecords = records - newTaskRequest
                                    processNextRecord(updatedRecords, sessionManager)
                                }
                            }
                        }
                    }
                }
            }

            // Start the recursive processing
            GlobalScope.launch {
                processNextRecord(unSyncedRecords, sessionManager)
            }
        }
    }


    private suspend fun updateTaskCommentInLocal(
        eventData: EventV2Response.Data?,
        taskDao: TaskV2Dao,
        sessionManager: SessionManager
    ) {
        if (eventData != null) {
            val isExists = TaskEventsList.isExists(
                SocketHandler.TaskEvent.NEW_TASK_COMMENT.name, eventData.id, true
            )
            val sharedViewModel = NavHostPresenterActivity.activityInstance?.let {
                ViewModelProvider(it).get(SharedViewModel::class.java)
            }

            if (!isExists) {
                val taskEvent = Events(
                    id = eventData.id,
                    taskId = eventData.taskId,
                    eventType = eventData.eventType,
                    initiator = eventData.initiator,
                    eventData = eventData.eventData,
                    commentData = eventData.commentData,
                    createdAt = eventData.createdAt,
                    updatedAt = eventData.updatedAt,
                    invitedMembers = eventData.invitedMembers,
                    eventNumber = eventData.eventNumber
                )
                GlobalScope.launch {
                    sessionManager.saveUpdatedAtTimeStamp(eventData.taskUpdatedAt)
                    val task = taskDao.getTaskByID(eventData.taskId)

                    if (task != null) {
                        task.seenBy = eventData.taskData.seenBy
                        task.hiddenBy = eventData.taskData.hiddenBy
                        task.updatedAt = eventData.taskUpdatedAt
                        task.creatorState = eventData.newTaskData.creatorState
                        val newAssigneeList = if (eventData.taskData.assignedToState.isNotEmpty()) {
                            eventData.taskData.assignedToState.toMutableList()
                        } else {
                            task.assignedToState
                        }
                        task.assignedToState = newAssigneeList
                        val newInvitedList = if (eventData.taskData.invitedNumbers.isNotEmpty()) {
                            eventData.taskData.invitedNumbers.toMutableList()
                        } else {
                            task.invitedNumbers
                        }
                        task.invitedNumbers = newInvitedList
                        task.toMeState = eventData.newTaskData.toMeState
                        task.fromMeState = eventData.newTaskData.fromMeState
                        task.hiddenState = eventData.newTaskData.hiddenState
                        task.eventsCount = task.eventsCount + 1

                        taskDao.updateTask(task)
                    }
                    taskDao.insertEventData(taskEvent)

                    if (eventData.newTaskData.creatorState.equals(TaskStatus.CANCELED.name, true)) {
                        sharedViewModel?.isHiddenUnread?.value = true
                        sessionManager.saveHiddenUnread(true)
                    } else {
                        if (eventData.newTaskData.isAssignedToMe) {
                            sharedViewModel?.isToMeUnread?.value = true
                            sessionManager.saveToMeUnread(true)
                        }
                        if (eventData.newTaskData.isCreator) {
                            sharedViewModel?.isFromMeUnread?.value = true
                            sessionManager.saveFromMeUnread(true)
                        }
                    }

                    updateAllTasksLists(taskDao)

                }.join()
                val handler = Handler(Looper.getMainLooper())
                handler.postDelayed({
                    EventBus.getDefault().post(LocalEvents.TaskEvent(taskEvent))
                    EventBus.getDefault().post(LocalEvents.RefreshTasksData())
                }, 50)

                TaskEventsList.removeEvent(
                    SocketHandler.TaskEvent.NEW_TASK_COMMENT.name,
                    eventData.id
                )
            }
        }
    }

    private suspend fun updateAllTasksLists(taskDao: TaskV2Dao): Boolean {
        GlobalScope.launch {

            val toMeNewTask =
                taskDao.getToMeTasks(TaskStatus.NEW.name.lowercase()).toMutableList()
            val toMeOngoingTask =
                taskDao.getToMeTasks(TaskStatus.ONGOING.name.lowercase()).toMutableList()
            val toMeDoneTask =
                taskDao.getToMeTasks(TaskStatus.DONE.name.lowercase()).toMutableList()

            val fromMeUnreadTask =
                taskDao.getFromMeTasks(TaskStatus.UNREAD.name.lowercase()).toMutableList()
            val fromMeOngoingTask =
                taskDao.getFromMeTasks(TaskStatus.ONGOING.name.lowercase()).toMutableList()
            val fromMeDoneTask =
                taskDao.getFromMeTasks(TaskStatus.DONE.name.lowercase()).toMutableList()

            val hiddenCanceledTask =
                taskDao.getHiddenTasks(TaskStatus.CANCELED.name.lowercase()).toMutableList()
            val hiddenOngoingTask =
                taskDao.getHiddenTasks(TaskStatus.ONGOING.name.lowercase()).toMutableList()
            val hiddenDoneTask =
                taskDao.getHiddenTasks(TaskStatus.DONE.name.lowercase()).toMutableList()

            CookiesManager.toMeNewTasks.postValue(toMeNewTask)
            CookiesManager.toMeOngoingTasks.postValue(toMeOngoingTask)
            CookiesManager.toMeDoneTasks.postValue(toMeDoneTask)

            CookiesManager.fromMeUnreadTasks.postValue(fromMeUnreadTask)
            CookiesManager.fromMeOngoingTasks.postValue(fromMeOngoingTask)
            CookiesManager.fromMeDoneTasks.postValue(fromMeDoneTask)

            CookiesManager.hiddenCanceledTasks.postValue(hiddenCanceledTask)
            CookiesManager.hiddenOngoingTasks.postValue(hiddenOngoingTask)
            CookiesManager.hiddenDoneTasks.postValue(hiddenDoneTask)
        }.join()
        return true
    }

    private suspend fun updateTaskDoneInLocal(
        eventData: EventV2Response.Data?, taskDao: TaskV2Dao, sessionManager: SessionManager
    ): CeibroTaskV2? {
        var updatedTask: CeibroTaskV2? = null
        if (eventData != null) {
            val isExists = TaskEventsList.isExists(
                SocketHandler.TaskEvent.TASK_DONE.name, eventData.taskId, true
            )
            if (!isExists) {
                val sharedViewModel = NavHostPresenterActivity.activityInstance?.let {
                    ViewModelProvider(it).get(SharedViewModel::class.java)
                }
                val taskEvent = Events(
                    id = eventData.id,
                    taskId = eventData.taskId,
                    eventType = eventData.eventType,
                    initiator = eventData.initiator,
                    eventData = eventData.eventData,
                    commentData = eventData.commentData,
                    createdAt = eventData.createdAt,
                    updatedAt = eventData.updatedAt,
                    invitedMembers = eventData.invitedMembers,
                    eventNumber = eventData.eventNumber
                )
                GlobalScope.launch {
                    sessionManager.saveUpdatedAtTimeStamp(eventData.taskUpdatedAt)
                    val task = taskDao.getTaskByID(eventData.taskId)

                    if (task != null) {
                        task.seenBy = eventData.taskData.seenBy
                        task.hiddenBy = eventData.taskData.hiddenBy
                        task.updatedAt = eventData.taskUpdatedAt
                        task.creatorState = eventData.newTaskData.creatorState
                        val assignToList = task.assignedToState
                        assignToList.map {
                            it.state = eventData.newTaskData.creatorState
                        }
                        task.assignedToState = assignToList
                        task.toMeState = eventData.newTaskData.toMeState
                        task.fromMeState = eventData.newTaskData.fromMeState
                        task.hiddenState = eventData.newTaskData.hiddenState
                        task.eventsCount = task.eventsCount + 1

                        taskDao.updateTask(task)
                    }
                    taskDao.insertEventData(taskEvent)
                    updatedTask = task

                    if (eventData.newTaskData.isAssignedToMe) {
                        sharedViewModel?.isToMeUnread?.value = true
                        sessionManager.saveToMeUnread(true)
                    }
                    if (eventData.newTaskData.isCreator) {
                        sharedViewModel?.isFromMeUnread?.value = true
                        sessionManager.saveFromMeUnread(true)
                    }

                    updateAllTasksLists(taskDao)

                }.join()

                val handler = Handler(Looper.getMainLooper())
                handler.postDelayed({
                    EventBus.getDefault().post(LocalEvents.TaskDoneEvent(updatedTask, taskEvent))
                    EventBus.getDefault().post(LocalEvents.RefreshTasksData())
                }, 50)
                TaskEventsList.removeEvent(
                    SocketHandler.TaskEvent.TASK_DONE.name, eventData.taskId
                )
            }
        }
        return updatedTask
    }

    override fun onDestroy() {
        super.onDestroy()
        println("Service Status...:On Destroyed called...")
    }
}