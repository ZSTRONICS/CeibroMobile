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
import com.zstronics.ceibro.CeibroApplication
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.host.NavHostPresenterActivity
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.database.CeibroDatabase
import com.zstronics.ceibro.data.database.dao.DraftNewTaskV2Dao
import com.zstronics.ceibro.data.database.dao.DrawingPinsV2Dao
import com.zstronics.ceibro.data.database.dao.InboxV2Dao
import com.zstronics.ceibro.data.database.dao.TaskV2Dao
import com.zstronics.ceibro.data.database.models.inbox.ActionFilesData
import com.zstronics.ceibro.data.database.models.tasks.CeibroTaskV2
import com.zstronics.ceibro.data.database.models.tasks.Events
import com.zstronics.ceibro.data.remote.TaskRemoteDataSource
import com.zstronics.ceibro.data.repos.dashboard.DashboardRepository
import com.zstronics.ceibro.data.repos.task.TaskRepository
import com.zstronics.ceibro.data.repos.task.TaskRootStateTags
import com.zstronics.ceibro.data.repos.task.models.v2.ApproveOrRejectTaskRequest
import com.zstronics.ceibro.data.repos.task.models.v2.EventV2Response
import com.zstronics.ceibro.data.repos.task.models.v2.EventWithFileUploadV2Request
import com.zstronics.ceibro.data.repos.task.models.v2.LocalFilesToStore
import com.zstronics.ceibro.data.repos.task.models.v2.NewTaskV2Entity
import com.zstronics.ceibro.data.repos.task.models.v2.TaskDetailEvents
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.data.sessions.SharedPreferenceManager
import com.zstronics.ceibro.ui.dashboard.SharedViewModel
import com.zstronics.ceibro.ui.dashboard.TaskEventsList
import com.zstronics.ceibro.ui.locationv2.usage.AddLocationTask
import com.zstronics.ceibro.ui.networkobserver.NetworkConnectivityObserver
import com.zstronics.ceibro.ui.socket.LocalEvents
import com.zstronics.ceibro.ui.socket.SocketHandler
import com.zstronics.ceibro.ui.tasks.task.TaskStatus
import com.zstronics.ceibro.ui.tasks.v2.newtask.CreateNewTaskService.NotificationUtils.CHANNEL_ID
import com.zstronics.ceibro.ui.tasks.v2.newtask.CreateNewTaskService.NotificationUtils.CHANNEL_NAME
import com.zstronics.ceibro.ui.tasks.v2.newtask.NewTaskV2VM.Companion.locationPinData
import com.zstronics.ceibro.ui.tasks.v2.newtask.NewTaskV2VM.Companion.taskList
import com.zstronics.ceibro.ui.tasks.v2.newtask.NewTaskV2VM.Companion.taskRequest
import com.zstronics.ceibro.ui.tasks.v2.taskdetail.comment.CommentVM.Companion.eventWithFileUploadV2RequestData
import com.zstronics.ceibro.ui.tasks.v3.fragments.approval.approvalsheet.TaskApproveOrRejectVM.Companion.approveOrRejectTaskRequest
import com.zstronics.ceibro.utils.FileUtils
import dagger.hilt.android.AndroidEntryPoint
import ee.zstronics.ceibro.camera.PickedImages
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

@AndroidEntryPoint
class CreateNewTaskService : Service() {

    private var apiCounter = 0
    private var draftTasksFailed = 0
    private var taskObjectData: NewTaskV2Entity? = null
    private var taskLocationPinData: AddLocationTask? = null
    private var taskListData: ArrayList<PickedImages>? = null
    private var sessionManager: SessionManager? = null
    private var mContext: Context? = null
    private val errorTaskNotificationID = 111
    private val createTaskNotificationID = 1
    private val doneNotificationID = 2
    private val commentNotificationID = 3
    private val approveNotificationID = 5
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
    lateinit var remoteTaskInternal: TaskRemoteDataSource

    @Inject
    lateinit var drawingPinsDaoInternal: DrawingPinsV2Dao

    @Inject
    lateinit var inboxV2DaoInternal: InboxV2Dao

    @Inject
    lateinit var networkConnectivityObserver: NetworkConnectivityObserver

    override fun onCreate() {

        super.onCreate()
        println("Service Status .. onCreate state...")
        taskObjectData = taskRequest
        taskLocationPinData = locationPinData
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

            "approveRequest" -> {

                taskId?.let { taskId ->
                    event?.let { event ->
                        sessionManager?.let { sessionManager ->
                            approveOrRejectTaskRequest?.let {
                                startForeground(
                                    approveNotificationID,
                                    createIndeterminateNotificationForFileUpload(
                                        context = this,
                                        channelId = CHANNEL_ID,
                                        channelName = CHANNEL_NAME,
                                        notificationTitle = "updating task status",
                                        notificationID = approveNotificationID
                                    )
                                )
                                approveTask(it, taskId, sessionManager, this)

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
        task: CeibroTaskV2?,
        taskDao: TaskV2Dao,
        sessionManager: SessionManager,
        drawingPinsDaoInternal: DrawingPinsV2Dao
    ) {
        val sharedViewModel = NavHostPresenterActivity.activityInstance?.let {
            ViewModelProvider(it).get(SharedViewModel::class.java)
        }

        task?.let { newTask ->
            GlobalScope.launch {

                val dbTask = taskDao.getTaskByID(newTask.id)
                if (dbTask == null) {
                    sessionManager.saveUpdatedAtTimeStamp(newTask.updatedAt)
                    taskDao.insertTaskData(newTask)
                    newTask.pinData?.let { drawingPinsDaoInternal.insertSinglePinData(it) }

                    if (newTask.taskRootState.equals(TaskRootStateTags.Ongoing.tagValue, true)) {
                        val rootOngoingAllTasks =
                            CeibroApplication.CookiesManager.rootOngoingAllTasks.value
                                ?: mutableListOf()
                        val foundTask = rootOngoingAllTasks.find { it.id == newTask.id }
                        if (foundTask != null) {
                            val index = rootOngoingAllTasks.indexOf(foundTask)
                            rootOngoingAllTasks.removeAt(index)
                        }
                        rootOngoingAllTasks.add(newTask)
                        val allTasks =
                            rootOngoingAllTasks.sortedByDescending { it.updatedAt }
                                .toMutableList()

                        CeibroApplication.CookiesManager.rootOngoingAllTasks.postValue(allTasks)

                        if (newTask.isAssignedToMe) {
                            val rootOngoingToMeTasks =
                                allTasks.filter {
                                    it.taskRootState.equals(
                                        TaskRootStateTags.Ongoing.tagValue,
                                        true
                                    ) &&
                                            (it.toMeState.equals(
                                                TaskStatus.NEW.name,
                                                true
                                            ) || it.toMeState.equals(TaskStatus.ONGOING.name, true))
                                }
                                    .sortedByDescending { it.updatedAt }.toMutableList()

                            CeibroApplication.CookiesManager.rootOngoingToMeTasks.postValue(
                                rootOngoingToMeTasks
                            )
                        }
                        if (newTask.isCreator) {
                            val rootOngoingFromMeTasks =
                                allTasks.filter {
                                    it.taskRootState.equals(
                                        TaskRootStateTags.Ongoing.tagValue,
                                        true
                                    ) &&
                                            (it.fromMeState.equals(
                                                TaskStatus.UNREAD.name,
                                                true
                                            ) || it.fromMeState.equals(
                                                TaskStatus.ONGOING.name,
                                                true
                                            ))
                                }
                                    .sortedByDescending { it.updatedAt }.toMutableList()


                            CeibroApplication.CookiesManager.rootOngoingFromMeTasks.postValue(
                                rootOngoingFromMeTasks
                            )
                        }
                    }

                    if (newTask.taskRootState.equals(TaskRootStateTags.Approval.tagValue, true)) {
                        val rootApprovalAllTasks =
                            CeibroApplication.CookiesManager.rootApprovalAllTasks.value
                                ?: mutableListOf()
                        val foundTask = rootApprovalAllTasks.find { it.id == newTask.id }
                        if (foundTask != null) {
                            val index = rootApprovalAllTasks.indexOf(foundTask)
                            rootApprovalAllTasks.removeAt(index)
                        }
                        rootApprovalAllTasks.add(newTask)
                        val allApprovalTasks =
                            rootApprovalAllTasks.sortedByDescending { it.updatedAt }
                                .toMutableList()

                        CeibroApplication.CookiesManager.rootApprovalAllTasks.postValue(
                            allApprovalTasks
                        )


                        val rootApprovalInReviewPendingTasks =
                            allApprovalTasks.filter {
                                it.taskRootState.equals(
                                    TaskRootStateTags.Approval.tagValue,
                                    true
                                ) &&
                                        (it.userSubState.equals(
                                            TaskRootStateTags.InReview.tagValue,
                                            true
                                        ))
                            }
                                .sortedByDescending { it.updatedAt }.toMutableList()

                        val rootApprovalToReviewTasks =
                            allApprovalTasks.filter {
                                it.taskRootState.equals(
                                    TaskRootStateTags.Approval.tagValue,
                                    true
                                ) &&
                                        (it.userSubState.equals(
                                            TaskRootStateTags.ToReview.tagValue,
                                            true
                                        ))
                            }
                                .sortedByDescending { it.updatedAt }.toMutableList()

                        CeibroApplication.CookiesManager.rootApprovalInReviewPendingTasks.postValue(
                            rootApprovalInReviewPendingTasks
                        )
                        CeibroApplication.CookiesManager.rootApprovalToReviewTasks.postValue(
                            rootApprovalToReviewTasks
                        )
                    }

//                if (newTask.taskRootState.equals(TaskRootStateTags.Closed.tagValue, true)) {
//                    val rootClosedAllTasks =
//                        CeibroApplication.CookiesManager.rootClosedAllTasks.value
//                            ?: mutableListOf()
//                    val foundTask = rootClosedAllTasks.find { it.id == newTask.id }
//                    if (foundTask != null) {
//                        val index = rootClosedAllTasks.indexOf(foundTask)
//                        rootClosedAllTasks.removeAt(index)
//                    }
//                    rootClosedAllTasks.add(newTask)
//                    val allClosedTasks =
//                        rootClosedAllTasks.sortedByDescending { it.updatedAt }
//                            .toMutableList()
//
//                    CeibroApplication.CookiesManager.rootClosedAllTasks.postValue(allClosedTasks)
//
//                    if (newTask.isAssignedToMe) {
//                        val rootClosedToMeTasks =
//                            allClosedTasks.filter {
//                                it.taskRootState.equals(TaskRootStateTags.Closed.tagValue, true) &&
//                                        (it.toMeState.equals(TaskStatus.DONE.name, true))
//                            }
//                                .sortedByDescending { it.updatedAt }.toMutableList()
//
//                        CeibroApplication.CookiesManager.rootClosedToMeTasks.postValue(rootClosedToMeTasks)
//                    }
//                    if (newTask.isCreator) {
//                        val rootClosedFromMeTasks =
//                            allClosedTasks.filter {
//                                it.taskRootState.equals(TaskRootStateTags.Closed.tagValue, true) &&
//                                        (it.fromMeState.equals(TaskStatus.DONE.name, true))
//                            }
//                                .sortedByDescending { it.updatedAt }.toMutableList()
//
//                        CeibroApplication.CookiesManager.rootClosedFromMeTasks.postValue(
//                            rootClosedFromMeTasks
//                        )
//                    }
//                }


                    /*if (newTask.isCreator) {
                        when (newTask.fromMeState) {
                            TaskStatus.UNREAD.name.lowercase() -> {
                                val allFromMeUnreadTasks =
                                    CeibroApplication.CookiesManager.fromMeUnreadTasks.value ?: mutableListOf()
                                val foundTask = allFromMeUnreadTasks.find { it.id == newTask.id }
                                if (foundTask != null) {
                                    val index = allFromMeUnreadTasks.indexOf(foundTask)
                                    allFromMeUnreadTasks.removeAt(index)
                                }
                                allFromMeUnreadTasks.add(newTask)
                                val unreadTasks =
                                    allFromMeUnreadTasks.sortedByDescending { it.updatedAt }
                                        .toMutableList()
                                CeibroApplication.CookiesManager.fromMeUnreadTasks.postValue(unreadTasks)
                            }

                            TaskStatus.ONGOING.name.lowercase() -> {
                                val allFromMeOngoingTasks =
                                    CeibroApplication.CookiesManager.fromMeOngoingTasks.value ?: mutableListOf()
                                val foundTask = allFromMeOngoingTasks.find { it.id == newTask.id }
                                if (foundTask != null) {
                                    val index = allFromMeOngoingTasks.indexOf(foundTask)
                                    allFromMeOngoingTasks.removeAt(index)
                                }
                                allFromMeOngoingTasks.add(newTask)
                                val ongoingTasks =
                                    allFromMeOngoingTasks.sortedByDescending { it.updatedAt }
                                        .toMutableList()
                                CeibroApplication.CookiesManager.fromMeOngoingTasks.postValue(ongoingTasks)
                            }

                            TaskStatus.DONE.name.lowercase() -> {
                                val allFromMeDoneTasks =
                                    CeibroApplication.CookiesManager.fromMeDoneTasks.value ?: mutableListOf()
                                val foundTask = allFromMeDoneTasks.find { it.id == newTask.id }
                                if (foundTask != null) {
                                    val index = allFromMeDoneTasks.indexOf(foundTask)
                                    allFromMeDoneTasks.removeAt(index)
                                }
                                allFromMeDoneTasks.add(newTask)
                                val doneTasks =
                                    allFromMeDoneTasks.sortedByDescending { it.updatedAt }
                                        .toMutableList()
                                CeibroApplication.CookiesManager.fromMeDoneTasks.postValue(doneTasks)
                            }
                        }
                    }

                    if (newTask.isAssignedToMe) {
                        when (newTask.toMeState) {
                            TaskStatus.NEW.name.lowercase() -> {
                                val allToMeNewTasks =
                                    CeibroApplication.CookiesManager.toMeNewTasks.value ?: mutableListOf()
                                val foundTask = allToMeNewTasks.find { it.id == newTask.id }
                                if (foundTask != null) {
                                    val index = allToMeNewTasks.indexOf(foundTask)
                                    allToMeNewTasks.removeAt(index)
                                }
                                allToMeNewTasks.add(newTask)
                                val newTasks =
                                    allToMeNewTasks.sortedByDescending { it.updatedAt }
                                        .toMutableList()
                                CeibroApplication.CookiesManager.toMeNewTasks.postValue(newTasks)
                            }

                            TaskStatus.ONGOING.name.lowercase() -> {
                                val allToMeOngoingTasks =
                                    CeibroApplication.CookiesManager.toMeOngoingTasks.value ?: mutableListOf()
                                val foundTask = allToMeOngoingTasks.find { it.id == newTask.id }
                                if (foundTask != null) {
                                    val index = allToMeOngoingTasks.indexOf(foundTask)
                                    allToMeOngoingTasks.removeAt(index)
                                }
                                allToMeOngoingTasks.add(newTask)
                                val ongoingTasks =
                                    allToMeOngoingTasks.sortedByDescending { it.updatedAt }
                                        .toMutableList()
                                CeibroApplication.CookiesManager.toMeOngoingTasks.postValue(ongoingTasks)
                            }

                            TaskStatus.DONE.name.lowercase() -> {
                                val allToMeDoneTasks =
                                    CeibroApplication.CookiesManager.toMeDoneTasks.value ?: mutableListOf()
                                val foundTask = allToMeDoneTasks.find { it.id == newTask.id }
                                if (foundTask != null) {
                                    val index = allToMeDoneTasks.indexOf(foundTask)
                                    allToMeDoneTasks.removeAt(index)
                                }
                                allToMeDoneTasks.add(newTask)
                                val doneTasks =
                                    allToMeDoneTasks.sortedByDescending { it.updatedAt }
                                        .toMutableList()
                                CeibroApplication.CookiesManager.toMeDoneTasks.postValue(doneTasks)
                            }
                        }
                        sharedViewModel?.isToMeUnread?.value = true
                        sessionManager.saveToMeUnread(true)
                    }*/

                    EventBus.getDefault().post(LocalEvents.RefreshTasksData())
                    EventBus.getDefault().post(LocalEvents.RefreshDrawingPins(newTask.pinData))
                }
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
            NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
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
        notificationDescription: String,
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
            .setContentText(notificationDescription)
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
        errorMessage: String,
        taskLocationPinData: AddLocationTask?
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
            this.locationTaskData = taskLocationPinData
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

        apiCounter++
        taskRepository.newTaskV2WithFiles(
            newTask,
            list,
            taskLocationPinData,
            sessionManager.getUserId()
        ) { isSuccess, task, errorMessage ->
            if (isSuccess) {

                this.sessionManager?.let {
                    updateCreatedTaskInLocal(
                        task,
                        taskDaoInternal,
                        sessionManager,
                        drawingPinsDaoInternal
                    )
                }
                hideIndeterminateNotifications(context, createTaskNotificationID)
                println("Service Status...:Create task with success")
                apiCounter--
                if (apiCounter <= 0) {
                    stopServiceAndClearNotification()
                }
            } else {
                hideIndeterminateNotifications(context, createTaskNotificationID)

                GlobalScope.launch {
                    saveFailedTaskInDraft(newTask, list, errorMessage, taskLocationPinData)
                }
                createSimpleNotification(
                    context = this,
                    channelId = CHANNEL_ID,
                    channelName = CHANNEL_NAME,
                    notificationTitle = "Task creation un-successful",
                    notificationDescription = errorMessage
                )

                println("Service Status...:Create task with failure -> $errorMessage")
                apiCounter--
                if (apiCounter <= 0) {
                    stopServiceAndClearNotification()
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
            apiCounter++
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
                        println("Service Status...:Upload comment with success")
                        updateTaskCommentInLocal(
                            response.data.data, taskDao,
                            sessionManager, drawingPinsDaoInternal
                        )
                        hideIndeterminateNotifications(context, commentNotificationID)
                    } else if (event == "doneTask") {
                        println("Service Status...:Done task with success")
                        updateTaskDoneInLocal(response.data.data, taskDao, sessionManager)
                        hideIndeterminateNotifications(context, doneNotificationID)
                        taskDaoInternal.updateTaskIsBeingDoneByAPI(taskId, false)
                    }


                    apiCounter--
                    if (apiCounter <= 0) {
                        stopServiceAndClearNotification()
                    }
                }

                is ApiResponse.Error -> {
                    if (event == "comment") {
                        println("Service Status...:Upload comment with failure -> ${response.error.message}")
                        hideIndeterminateNotifications(context, commentNotificationID)
                    } else if (event == "doneTask") {
                        println("Service Status...:Done task with failure -> ${response.error.message}")
                        hideIndeterminateNotifications(context, doneNotificationID)
                        taskDaoInternal.updateTaskIsBeingDoneByAPI(taskId, false)
                        EventBus.getDefault().post(LocalEvents.TaskFailedToDone())
                    }



                    apiCounter--
                    if (apiCounter <= 0) {
                        stopServiceAndClearNotification()
                    }
                }
            }
        }
    }

    private fun approveTask(
        request: ApproveOrRejectTaskRequest,
        taskId: String,
        sessionManager: SessionManager,
        context: CreateNewTaskService
    ) {
        GlobalScope.launch {
            apiCounter++


            val message = request.message.toRequestBody("text/plain".toMediaTypeOrNull())

            val metadata =
                request.metadata.toRequestBody("text/plain".toMediaTypeOrNull())

            val parts = request.files?.map { file ->
                val reqFile = file.asRequestBody(("image/" + file.extension).toMediaTypeOrNull())
                MultipartBody.Part.createFormData("files", file.name, reqFile)
            }
            when (val response = remoteTaskInternal.approveOrRejectTask(
                approvalEvent = request.approvalEvent,
                taskId = taskId,
                hasFiles = true,
                comment = message,
                files = parts,
                metadata = metadata
            )) {
                is ApiResponse.Success -> {

                    val room = providesAppDatabase(context)
                    val taskDao = room.getTaskV2sDao()

                    println("Service Status...:success updating task status for approved")
                    updateTaskApproveInLocal(
                        response.data.data, taskDao,
                        sessionManager, drawingPinsDaoInternal
                    )
                    hideIndeterminateNotifications(context, approveNotificationID)
                    apiCounter--
                    if (apiCounter <= 0) {
                        stopServiceAndClearNotification()
                    }


                }

                is ApiResponse.Error -> {

                    println("Service Status...: failed toupde task status for approved")
                    hideIndeterminateNotifications(context, approveNotificationID)
                    apiCounter--
                    if (apiCounter <= 0) {
                        stopServiceAndClearNotification()
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
            apiCounter++


            // Define a recursive function to process records one by one
            suspend fun processNextRecord(
                records: List<NewTaskV2Entity>,
                sessionManager: SessionManager
            ) {
                if (records.isEmpty()) {
                    println("Service Status .. drafts task failed $draftTasksFailed")
                    hideIndeterminateNotifications(context, draftCreateTaskNotificationID)
                    apiCounter--
                    if (apiCounter <= 0) {
                        stopServiceAndClearNotification()
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
                        newTaskRequest,
                        list,
                        newTaskRequest.locationTaskData,
                        sessionManager.getUserId()
                    ) { isSuccess, task, errorMessage ->
                        if (isSuccess) {

                            GlobalScope.launch {
                                draftNewTaskV2DaoInternal.deleteTaskById(newTaskRequest.taskId)

                                updateCreatedTaskInLocal(
                                    task, taskDaoInternal,
                                    sessionManager,
                                    drawingPinsDaoInternal
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
                                    sessionManager,
                                    drawingPinsDaoInternal
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
                                    ) || errorMessage.contains(
                                        "connection abort",
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

    private suspend fun updateAllTasksListForCommentAndForward(
        taskDao: TaskV2Dao,
        eventData: EventV2Response.Data,
        task: CeibroTaskV2
    ): Boolean {
        GlobalScope.launch {
            if (!eventData.oldTaskData.hiddenState.equals(
                    TaskStatus.CANCELED.name.lowercase(),
                    true
                )
            ) {

                if (eventData.newTaskData.taskRootState.equals(
                        TaskRootStateTags.Ongoing.tagValue,
                        true
                    )
                ) {
                    val rootOngoingAllTasksDB =
                        taskDao.getRootAllTasks(TaskRootStateTags.Ongoing.tagValue)
                            .toMutableList()

                    CeibroApplication.CookiesManager.rootOngoingAllTasks.postValue(
                        rootOngoingAllTasksDB
                    )

                    if (task.isAssignedToMe) {
                        val rootOngoingToMeTasks =
                            rootOngoingAllTasksDB.filter {
                                it.taskRootState.equals(TaskRootStateTags.Ongoing.tagValue, true) &&
                                        (it.toMeState.equals(
                                            TaskStatus.NEW.name,
                                            true
                                        ) || it.toMeState.equals(TaskStatus.ONGOING.name, true))
                            }
                                .sortedByDescending { it.updatedAt }.toMutableList()

                        CeibroApplication.CookiesManager.rootOngoingToMeTasks.postValue(
                            rootOngoingToMeTasks
                        )
                    }
                    if (task.isCreator) {
                        val rootOngoingFromMeTasks =
                            rootOngoingAllTasksDB.filter {
                                it.taskRootState.equals(TaskRootStateTags.Ongoing.tagValue, true) &&
                                        (it.fromMeState.equals(
                                            TaskStatus.UNREAD.name,
                                            true
                                        ) || it.fromMeState.equals(TaskStatus.ONGOING.name, true))
                            }
                                .sortedByDescending { it.updatedAt }.toMutableList()


                        CeibroApplication.CookiesManager.rootOngoingFromMeTasks.postValue(
                            rootOngoingFromMeTasks
                        )
                    }
                }

                if (eventData.newTaskData.taskRootState.equals(
                        TaskRootStateTags.Approval.tagValue,
                        true
                    )
                ) {
                    val rootApprovalAllTasksDB =
                        taskDao.getRootAllTasks(TaskRootStateTags.Approval.tagValue)
                            .toMutableList()

                    CeibroApplication.CookiesManager.rootApprovalAllTasks.postValue(
                        rootApprovalAllTasksDB
                    )

                    val rootApprovalInReviewPendingTasks =
                        rootApprovalAllTasksDB.filter {
                            it.taskRootState.equals(TaskRootStateTags.Approval.tagValue, true) &&
                                    (it.userSubState.equals(
                                        TaskRootStateTags.InReview.tagValue,
                                        true
                                    ))
                        }
                            .sortedByDescending { it.updatedAt }.toMutableList()

                    val rootApprovalToReviewTasks =
                        rootApprovalAllTasksDB.filter {
                            it.taskRootState.equals(TaskRootStateTags.Approval.tagValue, true) &&
                                    (it.userSubState.equals(
                                        TaskRootStateTags.ToReview.tagValue,
                                        true
                                    ))
                        }
                            .sortedByDescending { it.updatedAt }.toMutableList()

                    CeibroApplication.CookiesManager.rootApprovalInReviewPendingTasks.postValue(
                        rootApprovalInReviewPendingTasks
                    )
                    CeibroApplication.CookiesManager.rootApprovalToReviewTasks.postValue(
                        rootApprovalToReviewTasks
                    )
                }

                if (eventData.newTaskData.taskRootState.equals(
                        TaskRootStateTags.Closed.tagValue,
                        true
                    )
                ) {
                    val rootClosedAllTasksDB =
                        taskDao.getRootAllTasks(TaskRootStateTags.Closed.tagValue)
                            .toMutableList()

                    CeibroApplication.CookiesManager.rootClosedAllTasks.postValue(
                        rootClosedAllTasksDB
                    )

                    if (task.isAssignedToMe) {
                        val rootClosedToMeTasks =
                            rootClosedAllTasksDB.filter {
                                it.taskRootState.equals(TaskRootStateTags.Closed.tagValue, true) &&
                                        (it.toMeState.equals(TaskStatus.DONE.name, true) || it.toMeState.equals(
                                            TaskDetailEvents.REJECT_CLOSED.eventValue, true))
                            }
                                .sortedByDescending { it.updatedAt }.toMutableList()

                        CeibroApplication.CookiesManager.rootClosedToMeTasks.postValue(
                            rootClosedToMeTasks
                        )
                    }
                    if (task.isCreator) {
                        val rootClosedFromMeTasks =
                            rootClosedAllTasksDB.filter {
                                it.taskRootState.equals(TaskRootStateTags.Closed.tagValue, true) &&
                                        (it.fromMeState.equals(TaskStatus.DONE.name, true) || it.fromMeState.equals(TaskDetailEvents.REJECT_CLOSED.eventValue, true))
                            }
                                .sortedByDescending { it.updatedAt }.toMutableList()

                        CeibroApplication.CookiesManager.rootClosedFromMeTasks.postValue(
                            rootClosedFromMeTasks
                        )
                    }
                }


                /*

                                if (eventData.newTaskData.isAssignedToMe) {
                                    if (eventData.oldTaskData.userSubState.equals(TaskStatus.NEW.name, true)) {
                                        val toMeNewTask =
                                            taskDao.getToMeTasks(TaskStatus.NEW.name.lowercase()).toMutableList()
                                        CeibroApplication.CookiesManager.toMeNewTasks.postValue(toMeNewTask)
                                        val toMeOngoingTask =
                                            taskDao.getToMeTasks(TaskStatus.ONGOING.name.lowercase())
                                                .toMutableList()
                                        CeibroApplication.CookiesManager.toMeOngoingTasks.postValue(toMeOngoingTask)

                                    } else if (eventData.oldTaskData.userSubState.equals(
                                            TaskStatus.ONGOING.name,
                                            true
                                        )
                                    ) {
                                        val toMeOngoingTask =
                                            taskDao.getToMeTasks(TaskStatus.ONGOING.name.lowercase())
                                                .toMutableList()
                                        CeibroApplication.CookiesManager.toMeOngoingTasks.postValue(toMeOngoingTask)
                //                        val toMeDoneTask =
                //                            taskDao.getToMeTasks(TaskStatus.DONE.name.lowercase()).toMutableList()
                //                        CeibroApplication.CookiesManager.toMeDoneTasks.postValue(toMeDoneTask)

                                    } else {
                                        val toMeDoneTask =
                                            taskDao.getToMeTasks(TaskStatus.DONE.name.lowercase()).toMutableList()
                                        CeibroApplication.CookiesManager.toMeDoneTasks.postValue(toMeDoneTask)
                                    }
                                }

                                if (eventData.newTaskData.isCreator) {
                                    if (eventData.oldTaskData.creatorState.equals(TaskStatus.UNREAD.name, true)) {
                                        val fromMeUnreadTask =
                                            taskDao.getFromMeTasks(TaskStatus.UNREAD.name.lowercase())
                                                .toMutableList()
                                        CeibroApplication.CookiesManager.fromMeUnreadTasks.postValue(
                                            fromMeUnreadTask
                                        )
                                        val fromMeOngoingTask =
                                            taskDao.getFromMeTasks(TaskStatus.ONGOING.name.lowercase())
                                                .toMutableList()
                                        CeibroApplication.CookiesManager.fromMeOngoingTasks.postValue(
                                            fromMeOngoingTask
                                        )

                                    } else if (eventData.oldTaskData.creatorState.equals(
                                            TaskStatus.ONGOING.name,
                                            true
                                        )
                                    ) {
                                        val fromMeOngoingTask =
                                            taskDao.getFromMeTasks(TaskStatus.ONGOING.name.lowercase())
                                                .toMutableList()
                                        CeibroApplication.CookiesManager.fromMeOngoingTasks.postValue(
                                            fromMeOngoingTask
                                        )
                //                        val fromMeDoneTask =
                //                            taskDao.getFromMeTasks(TaskStatus.DONE.name.lowercase()).toMutableList()
                //                        CeibroApplication.CookiesManager.fromMeDoneTasks.postValue(fromMeDoneTask)

                                    } else {
                                        val fromMeDoneTask =
                                            taskDao.getFromMeTasks(TaskStatus.DONE.name.lowercase()).toMutableList()
                                        CeibroApplication.CookiesManager.fromMeDoneTasks.postValue(fromMeDoneTask)
                                    }
                                }

                                if (!eventData.oldTaskData.hiddenState.equals("NA", true)) {
                                    if (eventData.oldTaskData.hiddenState.equals(TaskStatus.ONGOING.name, true)) {
                                        val hiddenOngoingTask =
                                            taskDao.getHiddenTasks(TaskStatus.ONGOING.name.lowercase())
                                                .toMutableList()
                                        CeibroApplication.CookiesManager.hiddenOngoingTasks.postValue(
                                            hiddenOngoingTask
                                        )
                                        //Also update to-me so that when task removed from hidden, it should show in any other tab also which will be to-me ongoing
                                        val toMeOngoingTask =
                                            taskDao.getToMeTasks(TaskStatus.ONGOING.name.lowercase())
                                                .toMutableList()
                                        CeibroApplication.CookiesManager.toMeOngoingTasks.postValue(toMeOngoingTask)

                                    } else {
                                        val hiddenDoneTask =
                                            taskDao.getHiddenTasks(TaskStatus.DONE.name.lowercase()).toMutableList()
                                        CeibroApplication.CookiesManager.hiddenDoneTasks.postValue(hiddenDoneTask)
                                        val toMeDoneTask =
                                            taskDao.getToMeTasks(TaskStatus.DONE.name.lowercase()).toMutableList()
                                        CeibroApplication.CookiesManager.toMeDoneTasks.postValue(toMeDoneTask)
                                    }
                                }
                */

            } else {
//                val hiddenCanceledTask =
//                    taskDao.getHiddenTasks(TaskStatus.CANCELED.name.lowercase()).toMutableList()
//                CeibroApplication.CookiesManager.hiddenCanceledTasks.postValue(hiddenCanceledTask)
            }

        }.join()
        return true
    }

    private suspend fun updateTaskCommentInLocal(
        eventData: EventV2Response.Data?,
        taskDao: TaskV2Dao,
        sessionManager: SessionManager,
        drawingPinsDaoInternal: DrawingPinsV2Dao
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
                    eventNumber = eventData.eventNumber,
                    isPinned = eventData.isPinned
                )
                GlobalScope.launch {
                    sessionManager.saveUpdatedAtTimeStamp(eventData.taskUpdatedAt)
                    val task = taskDao.getTaskByID(eventData.taskId)
                    val inboxTask = inboxV2DaoInternal.getInboxTaskData(eventData.taskId)

                    if (task != null) {
                        task.seenBy = eventData.taskData.seenBy
                        task.hiddenBy = eventData.taskData.hiddenBy
                        task.updatedAt = eventData.taskUpdatedAt

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
                        task.isSeenByMe = eventData.newTaskData.isSeenByMe
                        task.taskRootState = eventData.newTaskData.taskRootState
                        task.isCanceled = eventData.newTaskData.isCanceled
                        task.isHiddenByMe = eventData.newTaskData.isHiddenByMe
                        task.userSubState = eventData.newTaskData.userSubState
                        task.creatorState = eventData.newTaskData.creatorState
                        task.isTaskInApproval = eventData.newTaskData.isTaskInApproval
                        task.rootState = eventData.newTaskData.rootState
                        task.toMeState = eventData.newTaskData.toMeState
                        task.fromMeState = eventData.newTaskData.fromMeState
                        task.hiddenState = eventData.newTaskData.hiddenState
                        task.eventsCount = task.eventsCount + 1
                        task.pinData = eventData.pinData

                        taskDao.updateTask(task)
                        taskDao.insertEventData(taskEvent)

//                        if (eventData.newTaskData.creatorState.equals(
//                                TaskStatus.CANCELED.name,
//                                true
//                            )
//                        ) {
//                            sharedViewModel?.isHiddenUnread?.value = true
//                            sessionManager.saveHiddenUnread(true)
//                        } else {
//                            if (eventData.newTaskData.isAssignedToMe) {
//                                sharedViewModel?.isToMeUnread?.value = true
//                                sessionManager.saveToMeUnread(true)
//                            }
//                            if (eventData.newTaskData.isCreator) {
//                                sharedViewModel?.isFromMeUnread?.value = true
//                                sessionManager.saveFromMeUnread(true)
//                            }
//                        }

                        updateAllTasksListForCommentAndForward(taskDao, eventData, task)

                    } else {
                        getTaskById(eventData.taskId) { isSuccess, taskData, events ->
                            if (isSuccess) {
                                launch {
                                    taskDao.insertEventData(taskEvent)
                                    taskData?.pinData?.let {
                                        drawingPinsDaoInternal.insertSinglePinData(
                                            it
                                        )
                                    }


                                    if (eventData.newTaskData.creatorState.equals(
                                            TaskStatus.CANCELED.name,
                                            true
                                        )
                                    ) {
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

                                    if (taskData != null) {
                                        updateAllTasksListForCommentAndForward(
                                            taskDao,
                                            eventData,
                                            taskData
                                        )
                                    }
                                }
                            } else {
                            }
                        }
                    }

                    if (eventData.pinData != null) {
                        drawingPinsDaoInternal.insertSinglePinData(eventData.pinData)
                    }

                    if (inboxTask != null && eventData.initiator.id != sessionManager.getUserObj()?.id) {
                        inboxTask.actionBy = eventData.initiator
                        inboxTask.createdAt = eventData.createdAt
                        inboxTask.actionType = SocketHandler.TaskEvent.IB_NEW_TASK_COMMENT.name
                        inboxTask.isSeen = false
                        inboxTask.unSeenNotifCount = inboxTask.unSeenNotifCount + 1

                        if (eventData.commentData != null) {
                            val newActionFiles = if (eventData.commentData.files.isNotEmpty()) {
                                eventData.commentData.files.map {
                                    ActionFilesData(
                                        fileUrl = it.fileUrl
                                    )
                                }
                            } else {
                                mutableListOf()
                            }
                            inboxTask.actionFiles = newActionFiles.toMutableList()
                            inboxTask.actionDescription = eventData.commentData.message ?: ""
                        } else {
                            inboxTask.actionFiles = mutableListOf()
                            inboxTask.actionDescription = ""
                        }

                        inboxV2DaoInternal.insertInboxItem(inboxTask)

//                        val allInboxTasks = inboxV2Dao.getAllInboxItems().toMutableList()
//                        CeibroApplication.CookiesManager.allInboxTasks.postValue(allInboxTasks)

                        EventBus.getDefault().post(LocalEvents.RefreshInboxSingleEvent(inboxTask))
                    }

                }.join()
                val handler = Handler(Looper.getMainLooper())
                handler.postDelayed(Runnable {
                    EventBus.getDefault().post(LocalEvents.TaskEvent(taskEvent))
                    EventBus.getDefault().post(LocalEvents.RefreshTasksData())
                    EventBus.getDefault().post(LocalEvents.UpdateDrawingPins(eventData.pinData))
                }, 50)

                TaskEventsList.removeEvent(
                    SocketHandler.TaskEvent.NEW_TASK_COMMENT.name,
                    eventData.id
                )
            }
        }
    }


    private suspend fun updateTaskApproveInLocal(
        eventData: EventV2Response.Data?,
        taskDao: TaskV2Dao,
        sessionManager: SessionManager,
        drawingPinsDaoInternal: DrawingPinsV2Dao
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
                    eventNumber = eventData.eventNumber,
                    isPinned = eventData.isPinned
                )
                GlobalScope.launch {
                    sessionManager.saveUpdatedAtTimeStamp(eventData.taskUpdatedAt)
                    val task = taskDao.getTaskByID(eventData.taskId)
                    val inboxTask = inboxV2DaoInternal.getInboxTaskData(eventData.taskId)

                    if (task != null) {
                        task.seenBy = eventData.taskData.seenBy
                        task.hiddenBy = eventData.taskData.hiddenBy
                        task.updatedAt = eventData.taskUpdatedAt
                        val assignToList = task.assignedToState
                        assignToList.map {
                            it.state = eventData.newTaskData.userSubState
                        }

                        val newAssigneeList = if (eventData.taskData.assignedToState.isNotEmpty()) {
                            eventData.taskData.assignedToState.toMutableList()
                        } else {
                            assignToList
                        }
                        task.assignedToState = newAssigneeList
                        val newInvitedList = if (eventData.taskData.invitedNumbers.isNotEmpty()) {
                            eventData.taskData.invitedNumbers.toMutableList()
                        } else {
                            task.invitedNumbers
                        }
                        task.invitedNumbers = newInvitedList
                        task.isSeenByMe = eventData.newTaskData.isSeenByMe
                        task.taskRootState = eventData.newTaskData.taskRootState
                        task.isCanceled = eventData.newTaskData.isCanceled
                        task.isHiddenByMe = eventData.newTaskData.isHiddenByMe
                        task.userSubState = eventData.newTaskData.userSubState
                        task.creatorState = eventData.newTaskData.creatorState
                        task.isTaskInApproval = eventData.newTaskData.isTaskInApproval
                        task.rootState = eventData.newTaskData.rootState
                        task.toMeState = eventData.newTaskData.toMeState
                        task.fromMeState = eventData.newTaskData.fromMeState
                        task.hiddenState = eventData.newTaskData.hiddenState
                        task.eventsCount = task.eventsCount + 1
                        task.pinData = eventData.pinData

                        taskDao.updateTask(task)
                        taskDao.insertEventData(taskEvent)

                        updateAllTasksLists(taskDao)

                    } else {
                        getTaskById(eventData.taskId) { isSuccess, taskData, events ->
                            if (isSuccess) {
                                launch {
                                    taskDao.insertEventData(taskEvent)
                                    taskData?.pinData?.let {
                                        drawingPinsDaoInternal.insertSinglePinData(
                                            it
                                        )
                                    }

                                    updateAllTasksLists(taskDao)

                                }
                            } else {
                            }
                        }
                    }

                    if (eventData.pinData != null) {
                        drawingPinsDaoInternal.insertSinglePinData(eventData.pinData)
                    }

                    if (inboxTask != null && eventData.initiator.id != sessionManager.getUserObj()?.id) {
                        inboxTask.actionBy = eventData.initiator
                        inboxTask.createdAt = eventData.createdAt
                        inboxTask.actionType = eventData.eventType
                        inboxTask.isSeen = false
                        inboxTask.unSeenNotifCount = inboxTask.unSeenNotifCount + 1

                        if (eventData.commentData != null) {
                            val newActionFiles = if (eventData.commentData.files.isNotEmpty()) {
                                eventData.commentData.files.map {
                                    ActionFilesData(
                                        fileUrl = it.fileUrl
                                    )
                                }
                            } else {
                                mutableListOf()
                            }
                            inboxTask.actionFiles = newActionFiles.toMutableList()
                            inboxTask.actionDescription = eventData.commentData.message ?: ""
                        } else {
                            inboxTask.actionFiles = mutableListOf()
                            inboxTask.actionDescription = ""
                        }

                        inboxV2DaoInternal.insertInboxItem(inboxTask)

//                        val allInboxTasks = inboxV2Dao.getAllInboxItems().toMutableList()
//                        CeibroApplication.CookiesManager.allInboxTasks.postValue(allInboxTasks)

                        EventBus.getDefault().post(LocalEvents.RefreshInboxSingleEvent(inboxTask))
                    }

                }.join()
                val handler = Handler(Looper.getMainLooper())
                handler.postDelayed(Runnable {
                    EventBus.getDefault().post(LocalEvents.TaskEvent(taskEvent))
                    EventBus.getDefault().post(LocalEvents.RefreshTasksData())
                    EventBus.getDefault().post(LocalEvents.UpdateDrawingPins(eventData.pinData))
                }, 50)

                TaskEventsList.removeEvent(
                    SocketHandler.TaskEvent.NEW_TASK_COMMENT.name,
                    eventData.id
                )
            }
        }
    }

    private fun getTaskById(
        taskId: String,
        callBack: (isSuccess: Boolean, task: CeibroTaskV2?, taskEvents: List<Events>) -> Unit
    ) {
        GlobalScope.launch {
            when (val response = remoteTaskInternal.getTaskById(taskId)) {
                is ApiResponse.Success -> {
                    taskDaoInternal.insertTaskData(response.data.task)
                    taskDaoInternal.insertMultipleEvents(response.data.taskEvents)
                    callBack.invoke(true, response.data.task, response.data.taskEvents)
                }

                is ApiResponse.Error -> {
                    callBack.invoke(false, null, emptyList())
                }
            }
        }
    }

    private suspend fun updateAllTasksLists(taskDao: TaskV2Dao): Boolean {
        GlobalScope.launch {

            //Ongoing List
            val rootOngoingAllTasksDB =
                taskDao.getRootAllTasks(TaskRootStateTags.Ongoing.tagValue)
                    .toMutableList()

            CeibroApplication.CookiesManager.rootOngoingAllTasks.postValue(rootOngoingAllTasksDB)

            val rootOngoingToMeTasks =
                rootOngoingAllTasksDB.filter {
                    it.taskRootState.equals(TaskRootStateTags.Ongoing.tagValue, true) &&
                            (it.toMeState.equals(
                                TaskStatus.NEW.name,
                                true
                            ) || it.toMeState.equals(TaskStatus.ONGOING.name, true))
                }
                    .sortedByDescending { it.updatedAt }.toMutableList()

            val rootOngoingFromMeTasks =
                rootOngoingAllTasksDB.filter {
                    it.taskRootState.equals(TaskRootStateTags.Ongoing.tagValue, true) &&
                            (it.fromMeState.equals(
                                TaskStatus.UNREAD.name,
                                true
                            ) || it.fromMeState.equals(TaskStatus.ONGOING.name, true))
                }
                    .sortedByDescending { it.updatedAt }.toMutableList()


            CeibroApplication.CookiesManager.rootOngoingToMeTasks.postValue(rootOngoingToMeTasks)
            CeibroApplication.CookiesManager.rootOngoingFromMeTasks.postValue(rootOngoingFromMeTasks)


            //Approval List
            val rootApprovalAllTasksDB =
                taskDao.getRootAllTasks(TaskRootStateTags.Approval.tagValue)
                    .toMutableList()

            CeibroApplication.CookiesManager.rootApprovalAllTasks.postValue(
                rootApprovalAllTasksDB
            )

            val rootApprovalInReviewPendingTasks =
                rootApprovalAllTasksDB.filter {
                    it.taskRootState.equals(TaskRootStateTags.Approval.tagValue, true) &&
                            (it.userSubState.equals(TaskRootStateTags.InReview.tagValue, true))
                }
                    .sortedByDescending { it.updatedAt }.toMutableList()

            val rootApprovalToReviewTasks =
                rootApprovalAllTasksDB.filter {
                    it.taskRootState.equals(TaskRootStateTags.Approval.tagValue, true) &&
                            (it.userSubState.equals(TaskRootStateTags.ToReview.tagValue, true))
                }
                    .sortedByDescending { it.updatedAt }.toMutableList()

            CeibroApplication.CookiesManager.rootApprovalInReviewPendingTasks.postValue(
                rootApprovalInReviewPendingTasks
            )
            CeibroApplication.CookiesManager.rootApprovalToReviewTasks.postValue(
                rootApprovalToReviewTasks
            )


            //Closed List
            val rootClosedAllTasksDB =
                taskDao.getRootAllTasks(TaskRootStateTags.Closed.tagValue)
                    .toMutableList()

            CeibroApplication.CookiesManager.rootClosedAllTasks.postValue(rootClosedAllTasksDB)

            val rootClosedToMeTasks =
                rootClosedAllTasksDB.filter {
                    it.taskRootState.equals(TaskRootStateTags.Closed.tagValue, true) &&
                            (it.toMeState.equals(TaskStatus.DONE.name, true) || it.toMeState.equals(TaskDetailEvents.REJECT_CLOSED.eventValue, true))
                }
                    .sortedByDescending { it.updatedAt }.toMutableList()

            val rootClosedFromMeTasks =
                rootClosedAllTasksDB.filter {
                    it.taskRootState.equals(TaskRootStateTags.Closed.tagValue, true) &&
                            (it.fromMeState.equals(TaskStatus.DONE.name, true) || it.fromMeState.equals(TaskDetailEvents.REJECT_CLOSED.eventValue, true))
                }
                    .sortedByDescending { it.updatedAt }.toMutableList()

            CeibroApplication.CookiesManager.rootClosedToMeTasks.postValue(
                rootClosedToMeTasks
            )
            CeibroApplication.CookiesManager.rootClosedFromMeTasks.postValue(
                rootClosedFromMeTasks
            )

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
                    eventNumber = eventData.eventNumber,
                    isPinned = eventData.isPinned
                )
                GlobalScope.launch {
                    sessionManager.saveUpdatedAtTimeStamp(eventData.taskUpdatedAt)
                    val task = taskDao.getTaskByID(eventData.taskId)
                    val inboxTask = inboxV2DaoInternal.getInboxTaskData(eventData.taskId)

                    if (task != null) {
                        task.seenBy = eventData.taskData.seenBy
                        task.hiddenBy = eventData.taskData.hiddenBy
                        task.updatedAt = eventData.taskUpdatedAt
                        task.creatorState = eventData.newTaskData.creatorState

                        val assignToList = task.assignedToState
                        if (task.confirmer != null) {
                            assignToList.map {
                                it.state = TaskRootStateTags.InReview.tagValue
                            }
                        } else {
                            assignToList.map {
                                it.state = eventData.newTaskData.creatorState
                            }
                        }

                        val newAssigneeList = if (eventData.taskData.assignedToState.isNotEmpty()) {
                            eventData.taskData.assignedToState.toMutableList()
                        } else {
                            assignToList
                        }
                        task.assignedToState = newAssigneeList
                        val newInvitedList = if (eventData.taskData.invitedNumbers.isNotEmpty()) {
                            eventData.taskData.invitedNumbers.toMutableList()
                        } else {
                            task.invitedNumbers
                        }
                        task.invitedNumbers = newInvitedList
                        task.isSeenByMe = eventData.newTaskData.isSeenByMe
                        task.taskRootState = eventData.newTaskData.taskRootState
                        task.isCanceled = eventData.newTaskData.isCanceled
                        task.isHiddenByMe = eventData.newTaskData.isHiddenByMe
                        task.userSubState = eventData.newTaskData.userSubState
                        task.creatorState = eventData.newTaskData.creatorState
                        task.isTaskInApproval = eventData.newTaskData.isTaskInApproval
                        task.rootState = eventData.newTaskData.rootState
                        task.toMeState = eventData.newTaskData.toMeState
                        task.fromMeState = eventData.newTaskData.fromMeState
                        task.hiddenState = eventData.newTaskData.hiddenState
                        task.eventsCount = task.eventsCount + 1
                        task.pinData = eventData.pinData

                        taskDao.updateTask(task)
                    }
                    taskDao.insertEventData(taskEvent)
                    if (eventData.pinData != null) {
                        drawingPinsDaoInternal.insertSinglePinData(eventData.pinData)
                    }

                    updatedTask = task


                    updateAllTasksLists(taskDao)

                    if (inboxTask != null) {
                        inboxTask.actionBy = eventData.initiator
                        inboxTask.createdAt = eventData.createdAt
                        inboxTask.actionType = SocketHandler.TaskEvent.IB_TASK_DONE.name
                        inboxTask.taskState = eventData.newTaskData.creatorState
                        inboxTask.isSeen = false
                        inboxTask.unSeenNotifCount = inboxTask.unSeenNotifCount + 1

                        if (eventData.commentData != null) {
                            val newActionFiles = if (eventData.commentData.files.isNotEmpty()) {
                                eventData.commentData.files.map {
                                    ActionFilesData(
                                        fileUrl = it.fileUrl
                                    )
                                }
                            } else {
                                mutableListOf()
                            }
                            inboxTask.actionFiles = newActionFiles.toMutableList()
                            inboxTask.actionDescription = eventData.commentData.message ?: ""
                        } else {
                            inboxTask.actionFiles = mutableListOf()
                            inboxTask.actionDescription = ""
                        }

                        inboxV2DaoInternal.insertInboxItem(inboxTask)

//                        val allInboxTasks = inboxV2Dao.getAllInboxItems().toMutableList()
//                        CeibroApplication.CookiesManager.allInboxTasks.postValue(allInboxTasks)

                        EventBus.getDefault().post(LocalEvents.RefreshInboxSingleEvent(inboxTask))
                    }

                }.join()

                val handler = Handler(Looper.getMainLooper())
                handler.postDelayed({
                    EventBus.getDefault().post(LocalEvents.TaskDoneEvent(updatedTask, taskEvent))
                    EventBus.getDefault().post(LocalEvents.RefreshTasksData())
                    EventBus.getDefault().post(LocalEvents.UpdateDrawingPins(eventData.pinData))
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

    private fun stopServiceAndClearNotification() {
        hideIndeterminateNotifications(
            this@CreateNewTaskService,
            createTaskNotificationID
        )
        hideIndeterminateNotifications(
            this@CreateNewTaskService,
            doneNotificationID
        )
        hideIndeterminateNotifications(
            this@CreateNewTaskService,
            commentNotificationID
        )
        hideIndeterminateNotifications(
            this@CreateNewTaskService,
            draftCreateTaskNotificationID
        )
        stopSelf()
    }

}

