package com.zstronics.ceibro.base.viewmodel


import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.CallSuper
import androidx.core.app.NotificationCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.zstronics.ceibro.CeibroApplication
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.clickevents.SingleClickEvent
import com.zstronics.ceibro.base.extensions.toCamelCase
import com.zstronics.ceibro.base.interfaces.IBase
import com.zstronics.ceibro.base.interfaces.OnClickHandler
import com.zstronics.ceibro.base.navgraph.host.NavHostPresenterActivity
import com.zstronics.ceibro.base.state.UIState
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.database.dao.DraftNewTaskV2Dao
import com.zstronics.ceibro.data.database.dao.DrawingPinsV2Dao
import com.zstronics.ceibro.data.database.dao.FloorsV2Dao
import com.zstronics.ceibro.data.database.dao.GroupsV2Dao
import com.zstronics.ceibro.data.database.dao.InboxV2Dao
import com.zstronics.ceibro.data.database.dao.ProjectsV2Dao
import com.zstronics.ceibro.data.database.dao.TaskV2Dao
import com.zstronics.ceibro.data.database.models.inbox.ActionFilesData
import com.zstronics.ceibro.data.database.models.inbox.CeibroInboxV2
import com.zstronics.ceibro.data.database.models.projects.CeibroFloorV2
import com.zstronics.ceibro.data.database.models.projects.CeibroGroupsV2
import com.zstronics.ceibro.data.database.models.projects.CeibroProjectV2
import com.zstronics.ceibro.data.database.models.tasks.AssignedToState
import com.zstronics.ceibro.data.database.models.tasks.CeibroTaskV2
import com.zstronics.ceibro.data.database.models.tasks.Events
import com.zstronics.ceibro.data.database.models.tasks.TaskFiles
import com.zstronics.ceibro.data.remote.TaskRemoteDataSource
import com.zstronics.ceibro.data.repos.dashboard.IDashboardRepository
import com.zstronics.ceibro.data.repos.dashboard.attachment.AttachmentModules
import com.zstronics.ceibro.data.repos.dashboard.attachment.AttachmentUploadRequest
import com.zstronics.ceibro.data.repos.projects.drawing.UploadedFileResponse
import com.zstronics.ceibro.data.repos.task.ITaskRepository
import com.zstronics.ceibro.data.repos.task.TaskRootStateTags
import com.zstronics.ceibro.data.repos.task.models.v2.EventV2Response
import com.zstronics.ceibro.data.repos.task.models.v2.ForwardedToMeNewTaskV2Response
import com.zstronics.ceibro.data.repos.task.models.v2.HideTaskResponse
import com.zstronics.ceibro.data.repos.task.models.v2.PinnedCommentV2Response
import com.zstronics.ceibro.data.repos.task.models.v2.SocketReSyncUpdateV2Request
import com.zstronics.ceibro.data.repos.task.models.v2.TaskDetailEvents
import com.zstronics.ceibro.data.repos.task.models.v2.TaskSeenResponse
import com.zstronics.ceibro.data.repos.task.models.v2.UpdateRequiredEvents
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.ui.attachment.SubtaskAttachment
import com.zstronics.ceibro.ui.dashboard.SharedViewModel
import com.zstronics.ceibro.ui.dashboard.TaskEventsList
import com.zstronics.ceibro.ui.socket.LocalEvents
import com.zstronics.ceibro.ui.socket.SocketHandler
import com.zstronics.ceibro.ui.tasks.task.TaskStatus
import com.zstronics.ceibro.ui.tasks.v2.newtask.CreateNewTaskService
import com.zstronics.ceibro.utils.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject


abstract class HiltBaseViewModel<VS : IBase.State> : BaseCoroutineViewModel(), IBase.ViewModel<VS>,
    OnClickHandler {
    @CallSuper
    override fun onCleared() {
        cancelAllJobs()
        super.onCleared()
    }

    override fun cancelAllJobs() {
        viewModelBGScope.close()
        viewModelScope.cancel()
        viewModelJob.cancel()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    override fun onCreate() {
        viewState.onCreate()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    override fun onStart() {
        viewState.onStart()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    override fun onResume() {
        viewState.resume()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    override fun onPause() {
        viewState.pause()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    override fun onStop() {
        viewState.onStop()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    override fun onDestroy() {
        viewState.destroy()
    }

    override fun registerLifecycleOwner(owner: LifecycleOwner?) {
        unregisterLifecycleOwner(owner)
        owner?.lifecycle?.addObserver(this)
    }

    override fun unregisterLifecycleOwner(owner: LifecycleOwner?) {
        owner?.lifecycle?.removeObserver(this)
    }

    override fun launch(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }

    fun getString(keyID: Int, appContext: Context): String = appContext.getString(keyID)

    companion object {
        val syncDraftRecords = MutableLiveData<Int>()
    }

    override val clickEvent: SingleClickEvent? = SingleClickEvent()

    /**
     * override this method when there is  no need to use its super implementation.
     * recommended to not override this method. use @see <handleOnClick> must override
     */
    override fun handlePressOnView(id: Int) {
        clickEvent?.setValue(id)
        handleOnClick(id)
    }

    private var draftRecordCallBack: ((Int) -> Unit)? = null
    override fun setCallback(callback: (Int) -> Unit) {
        this.draftRecordCallBack = callback
    }

    /**
     * Override this method in your [ViewModel]
     * you can manage your owen onclick logic by overriding this method
     */
    open fun handleOnClick(id: Int) {}

    override fun loading(isLoading: Boolean, message: String) {
        viewState.uiState.postValue(UIState.Loading(isLoading, message))
    }

    override fun alert(message: String) {
        viewState.uiState.postValue(UIState.Alert(message))
    }

    private val _fileUriList: MutableLiveData<ArrayList<SubtaskAttachment?>?> =
        MutableLiveData(arrayListOf())
    val fileUriList: MutableLiveData<ArrayList<SubtaskAttachment?>?> = _fileUriList

    private val _notificationEvent: MutableLiveData<LocalEvents.CreateNotification?> =
        MutableLiveData()
    val notificationEvent: LiveData<LocalEvents.CreateNotification?> = _notificationEvent

    fun createNotification(notification: LocalEvents.CreateNotification?) {
        _notificationEvent.postValue(notification)
    }

    val _draftRecordObserver = MutableLiveData<Int>()


    private val indeterminateNotificationID = 1

    fun createIndeterminateNotificationForFileUpload(
        activity: FragmentActivity,
        channelId: String,
        channelName: String,
        notificationTitle: String,
        isOngoing: Boolean = true,
        indeterminate: Boolean = true,
        notificationIcon: Int = R.drawable.icon_upload
    ): Pair<NotificationManager, NotificationCompat.Builder> {
        // Create a notification channel (for Android O and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            val notificationManager = activity.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        // Create a notification builder
        val builder = NotificationCompat.Builder(activity, channelId).setSmallIcon(notificationIcon)
            .setContentTitle(notificationTitle).setOngoing(isOngoing)
            .setProgress(0, 0, indeterminate)

        // Show the notification
        val notificationManager = activity.getSystemService(NotificationManager::class.java)
        notificationManager.notify(indeterminateNotificationID, builder.build())
        return Pair(notificationManager, builder)
    }

    fun hideIndeterminateNotificationForFileUpload(activity: FragmentActivity) {
        val notificationManager = activity.getSystemService(NotificationManager::class.java)
        notificationManager.cancel(indeterminateNotificationID) // Remove the notification with ID
    }

    fun addUriToList(data: SubtaskAttachment) {
        val files = fileUriList.value
        files?.add(data)
        _fileUriList.postValue(files)
    }

    fun updateUri(position: Int, updatedUri: Uri) {
        val files = fileUriList.value
        val file = files?.get(position)
        file?.attachmentUri = updatedUri
        files?.removeAt(position)
        files?.add(position, file)
        _fileUriList.postValue(files)
    }

    fun removeFile(position: Int) {
        val files = fileUriList.value
        files?.removeAt(position)
        _fileUriList.postValue(files)
    }

    private fun removeAllFiles() {
        _fileUriList.postValue(arrayListOf())
    }

    fun uploadFiles(module: String, id: String, context: Context) {
        val fileUriList = fileUriList.value
        val attachmentUriList = fileUriList?.map {
            FileUtils.getFile(
                context, it?.attachmentUri
            )
        }
        val request = AttachmentUploadRequest(
            _id = id, moduleName = module, files = attachmentUriList
        )
        EventBus.getDefault()
            .post(fileUriList?.let { LocalEvents.UploadFilesToServer(request, it) })
        removeAllFiles()
    }

    suspend fun reSyncAppData(
        reSyncData: UpdateRequiredEvents,
        callBack: (isSuccess: Boolean) -> Unit
    ) {
        val request = SocketReSyncUpdateV2Request(
            missingEventIds = reSyncData.missingEventIds,
            missingTaskIds = reSyncData.missingTaskIds
        )
        GlobalScope.launch {
            when (val response = remoteTaskInternal.syncTaskAndEvents(request)) {
                is ApiResponse.Success -> {
                    println("Heartbeat, Missing Sync Response: ${response.data}")
                    sessionManagerInternal.saveUpdatedAtTimeStamp(response.data.latestUpdatedAt)
                    if (!response.data.allTasks.isNullOrEmpty()) {
                        taskDaoInternal.insertMultipleTasks(response.data.allTasks)
                    }
                    try {
                        if (!response.data.allEvents.isNullOrEmpty()) {
                            taskDaoInternal.insertMultipleEvents(response.data.allEvents)
                        }
                    } catch (e: Exception) {
                        println("SyncException: ${e.toString()}")
                    }


                    Handler(Looper.getMainLooper()).postDelayed({
                        GlobalScope.launch {
                            updateAllTasksLists(taskDaoInternal)

                            EventBus.getDefault().post(LocalEvents.RefreshTasksData())
                            EventBus.getDefault().post(LocalEvents.RefreshAllEvents())
                            callBack.invoke(true)
                        }
                    }, 200)

                }

                is ApiResponse.Error -> {
                    println("Heartbeat, Missing Sync Response: ${response.error.message}")
//                    loading(false, response.error.message)
                    callBack.invoke(false)
                }
            }
        }.join()
    }

    @Inject
    lateinit var taskDaoInternal1: TaskV2Dao


    suspend fun updateAllTasksLists(taskDao: TaskV2Dao): Boolean {
        GlobalScope.launch(Dispatchers.IO) {

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
                            (it.toMeState.equals(TaskStatus.DONE.name, true) || it.toMeState.equals(
                                TaskDetailEvents.REJECT_CLOSED.eventValue, true
                            ))
                }
                    .sortedByDescending { it.updatedAt }.toMutableList()

            val rootClosedFromMeTasks =
                rootClosedAllTasksDB.filter {
                    it.taskRootState.equals(TaskRootStateTags.Closed.tagValue, true) &&
                            (it.fromMeState.equals(
                                TaskStatus.DONE.name,
                                true
                            ) || it.fromMeState.equals(
                                TaskDetailEvents.REJECT_CLOSED.eventValue,
                                true
                            ))
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

    suspend fun updateTasksListsForReOpen(taskDao: TaskV2Dao): Boolean {
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


            //Closed List
            val rootClosedAllTasksDB =
                taskDao.getRootAllTasks(TaskRootStateTags.Closed.tagValue)
                    .toMutableList()

            CeibroApplication.CookiesManager.rootClosedAllTasks.postValue(rootClosedAllTasksDB)

            val rootClosedToMeTasks =
                rootClosedAllTasksDB.filter {
                    it.taskRootState.equals(TaskRootStateTags.Closed.tagValue, true) &&
                            (it.toMeState.equals(TaskStatus.DONE.name, true) || it.toMeState.equals(
                                TaskDetailEvents.REJECT_CLOSED.eventValue, true
                            ))
                }
                    .sortedByDescending { it.updatedAt }.toMutableList()

            val rootClosedFromMeTasks =
                rootClosedAllTasksDB.filter {
                    it.taskRootState.equals(TaskRootStateTags.Closed.tagValue, true) &&
                            (it.fromMeState.equals(
                                TaskStatus.DONE.name,
                                true
                            ) || it.fromMeState.equals(
                                TaskDetailEvents.REJECT_CLOSED.eventValue,
                                true
                            ))
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

    private suspend fun updateAllTasksListForCommentAndForward(
        taskDao: TaskV2Dao,
        eventData: EventV2Response.Data,
        task: CeibroTaskV2
    ): Boolean {
        GlobalScope.launch {
            if (!eventData.oldTaskData.taskRootState.equals(
                    TaskRootStateTags.Canceled.tagValue.toCamelCase(),
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
                                        (it.toMeState.equals(
                                            TaskStatus.DONE.name,
                                            true
                                        ) || it.toMeState.equals(
                                            TaskDetailEvents.REJECT_CLOSED.eventValue,
                                            true
                                        ))
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
                                        (it.fromMeState.equals(
                                            TaskStatus.DONE.name,
                                            true
                                        ) || it.fromMeState.equals(
                                            TaskDetailEvents.REJECT_CLOSED.eventValue,
                                            true
                                        ))
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


    private suspend fun updateAllTasksListForTaskSeen(
        taskDao: TaskV2Dao,
        taskSeen: TaskSeenResponse.TaskSeen
    ): Boolean {
        GlobalScope.launch {
            if (!taskSeen.oldTaskData.taskRootState.equals(
                    TaskRootStateTags.Canceled.tagValue.toCamelCase(),
                    true
                )
            ) {

                if (taskSeen.newTaskData.taskRootState.equals(
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

                if (taskSeen.newTaskData.taskRootState.equals(
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

                if (taskSeen.newTaskData.taskRootState.equals(
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

                    val rootClosedToMeTasks =
                        rootClosedAllTasksDB.filter {
                            it.taskRootState.equals(TaskRootStateTags.Closed.tagValue, true) &&
                                    (it.toMeState.equals(
                                        TaskStatus.DONE.name,
                                        true
                                    ) || it.toMeState.equals(
                                        TaskDetailEvents.REJECT_CLOSED.eventValue,
                                        true
                                    ))
                        }
                            .sortedByDescending { it.updatedAt }.toMutableList()

                    CeibroApplication.CookiesManager.rootClosedToMeTasks.postValue(
                        rootClosedToMeTasks
                    )

                    val rootClosedFromMeTasks =
                        rootClosedAllTasksDB.filter {
                            it.taskRootState.equals(TaskRootStateTags.Closed.tagValue, true) &&
                                    (it.fromMeState.equals(
                                        TaskStatus.DONE.name,
                                        true
                                    ) || it.fromMeState.equals(
                                        TaskDetailEvents.REJECT_CLOSED.eventValue,
                                        true
                                    ))
                        }
                            .sortedByDescending { it.updatedAt }.toMutableList()

                    CeibroApplication.CookiesManager.rootClosedFromMeTasks.postValue(
                        rootClosedFromMeTasks
                    )

                }


                /*
                                if (taskSeen.isAssignedToMe) {
                                    if (taskSeen.stateChanged) {
                                        val toMeNewTask =
                                            taskDao.getToMeTasks(TaskStatus.NEW.name.lowercase()).toMutableList()
                                        val toMeOngoingTask =
                                            taskDao.getToMeTasks(TaskStatus.ONGOING.name.lowercase())
                                                .toMutableList()

                                        CeibroApplication.CookiesManager.toMeNewTasks.postValue(toMeNewTask)
                                        CeibroApplication.CookiesManager.toMeOngoingTasks.postValue(toMeOngoingTask)
                                    } else {
                                        if (taskSeen.oldTaskData.userSubState.equals(TaskStatus.NEW.name, true)) {
                                            val toMeNewTask =
                                                taskDao.getToMeTasks(TaskStatus.NEW.name.lowercase())
                                                    .toMutableList()
                                            CeibroApplication.CookiesManager.toMeNewTasks.postValue(toMeNewTask)

                                        } else if (taskSeen.oldTaskData.userSubState.equals(
                                                TaskStatus.ONGOING.name,
                                                true
                                            )
                                        ) {
                                            val toMeNewTask =
                                                taskDao.getToMeTasks(TaskStatus.NEW.name.lowercase())
                                                    .toMutableList()
                                            CeibroApplication.CookiesManager.toMeNewTasks.postValue(toMeNewTask)

                                            val toMeOngoingTask =
                                                taskDao.getToMeTasks(TaskStatus.ONGOING.name.lowercase())
                                                    .toMutableList()
                                            CeibroApplication.CookiesManager.toMeOngoingTasks.postValue(
                                                toMeOngoingTask
                                            )
                                        } else {
                //                            val toMeOngoingTask =
                //                                taskDao.getToMeTasks(TaskStatus.ONGOING.name.lowercase())
                //                                    .toMutableList()
                //                            CeibroApplication.CookiesManager.toMeOngoingTasks.postValue(
                //                                toMeOngoingTask
                //                            )

                                            val toMeDoneTask =
                                                taskDao.getToMeTasks(TaskStatus.DONE.name.lowercase())
                                                    .toMutableList()
                                            CeibroApplication.CookiesManager.toMeDoneTasks.postValue(toMeDoneTask)
                                        }
                                    }
                                }

                                if (taskSeen.isCreator) {
                                    if (taskSeen.creatorStateChanged) {
                                        val fromMeUnreadTask =
                                            taskDao.getFromMeTasks(TaskStatus.UNREAD.name.lowercase())
                                                .toMutableList()
                                        val fromMeOngoingTask =
                                            taskDao.getFromMeTasks(TaskStatus.ONGOING.name.lowercase())
                                                .toMutableList()
                                        CeibroApplication.CookiesManager.fromMeUnreadTasks.postValue(
                                            fromMeUnreadTask
                                        )
                                        CeibroApplication.CookiesManager.fromMeOngoingTasks.postValue(
                                            fromMeOngoingTask
                                        )
                                    } else {
                                        if (taskSeen.oldTaskData.creatorState.equals(
                                                TaskStatus.UNREAD.name,
                                                true
                                            )
                                        ) {
                                            val fromMeUnreadTask =
                                                taskDao.getFromMeTasks(TaskStatus.UNREAD.name.lowercase())
                                                    .toMutableList()
                                            CeibroApplication.CookiesManager.fromMeUnreadTasks.postValue(
                                                fromMeUnreadTask
                                            )

                                        } else if (taskSeen.oldTaskData.creatorState.equals(
                                                TaskStatus.ONGOING.name,
                                                true
                                            )
                                        ) {
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

                                        } else {
                //                            val fromMeOngoingTask =
                //                                taskDao.getFromMeTasks(TaskStatus.ONGOING.name.lowercase())
                //                                    .toMutableList()
                //                            CeibroApplication.CookiesManager.fromMeOngoingTasks.postValue(
                //                                fromMeOngoingTask
                //                            )

                                            val fromMeDoneTask =
                                                taskDao.getFromMeTasks(TaskStatus.DONE.name.lowercase())
                                                    .toMutableList()
                                            CeibroApplication.CookiesManager.fromMeDoneTasks.postValue(
                                                fromMeDoneTask
                                            )
                                        }
                                    }

                                }

                                if (!taskSeen.oldTaskData.hiddenState.equals("NA", true)) {
                                    if (taskSeen.oldTaskData.hiddenState.equals(TaskStatus.ONGOING.name, true)) {
                                        val hiddenOngoingTask =
                                            taskDao.getHiddenTasks(TaskStatus.ONGOING.name.lowercase())
                                                .toMutableList()
                                        CeibroApplication.CookiesManager.hiddenOngoingTasks.postValue(
                                            hiddenOngoingTask
                                        )

                                    } else {
                                        val hiddenDoneTask =
                                            taskDao.getHiddenTasks(TaskStatus.DONE.name.lowercase()).toMutableList()
                                        CeibroApplication.CookiesManager.hiddenDoneTasks.postValue(hiddenDoneTask)
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


    private fun getTaskById(
        taskId: String,
        callBack: (isSuccess: Boolean, task: CeibroTaskV2?, taskEvents: List<Events>) -> Unit
    ) {
        launch {
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


    fun addCreatedFloorInLocal(
        floor: CeibroFloorV2, floorV2Dao: FloorsV2Dao
    ) {
        GlobalScope.launch {
            floorV2Dao.insertFloor(floor)

            EventBus.getDefault().post(LocalEvents.RefreshFloorsData(floor.projectId))
        }
    }

    fun addGroupCreatedInLocal(
        group: CeibroGroupsV2, groupV2Dao: GroupsV2Dao, projectsV2Dao: ProjectsV2Dao
    ) {
        GlobalScope.launch {
            groupV2Dao.insertGroup(group)
            val project = projectsV2Dao.getProjectByProjectId(group.projectId)
            if (project == null) {

            }

            EventBus.getDefault().post(LocalEvents.RefreshGroupsData(group.projectId))
        }
    }

    fun deleteGroupInLocal(
        groupId: String, groupV2Dao: GroupsV2Dao
    ) {
        GlobalScope.launch {
            groupV2Dao.deleteGroupById(groupId)

            EventBus.getDefault().post(LocalEvents.RefreshDeletedGroupData(groupId))
        }
    }

    fun addUploadedDrawingInLocal(
        uploadedFile: UploadedFileResponse, groupV2Dao: GroupsV2Dao, floorV2Dao: FloorsV2Dao
    ) {
        GlobalScope.launch {
            val group = groupV2Dao.getGroupByGroupId(uploadedFile.groupId)
            if (group != null) {
                val allDrawings = group.drawings.toMutableList()
                uploadedFile.drawings.forEach { newDrawing ->
                    val index =
                        allDrawings.indexOfFirst { oldDrawing -> oldDrawing._id == newDrawing._id }
                    if (index > -1) {
                        allDrawings[index] = newDrawing
                    } else {
                        allDrawings.add(newDrawing)
                    }
                }
                group.drawings = allDrawings
                group.updatedAt = uploadedFile.groupUpdatedAt

                groupV2Dao.insertGroup(group)
            }

            val newDrawingIdsList = uploadedFile.drawings.map { it._id }
            val floor = floorV2Dao.getFloorByFloorId(uploadedFile.floorId)
            floor.updatedAt = uploadedFile.floorUpdatedAt
            val allDrawingsIDs = floor.drawings.toMutableList()
            allDrawingsIDs.addAll(newDrawingIdsList)
            floor.drawings = allDrawingsIDs
            floorV2Dao.insertFloor(floor)
//            floorV2Dao.updateFloorUpdatedAtByFloorId(uploadedFile.floorUpdatedAt, uploadedFile.floorId)

            Handler(Looper.getMainLooper()).postDelayed({
                EventBus.getDefault().post(LocalEvents.RefreshGroupsData(uploadedFile.projectId))
            }, 150)
        }
    }

    fun addCreatedProjectInLocal(
        project: CeibroProjectV2, projectDao: ProjectsV2Dao
    ) {
        GlobalScope.launch {
            projectDao.insertProject(project)

            EventBus.getDefault().post(LocalEvents.RefreshProjectsData())
        }
    }

    fun updateProjectInLocal(
        project: CeibroProjectV2, projectDao: ProjectsV2Dao
    ) {
        GlobalScope.launch {
            projectDao.insertProject(project)

            EventBus.getDefault().post(LocalEvents.RefreshProjectsData())
        }
    }

    fun updateCreatedTaskInLocal(
        task: CeibroTaskV2?,
        taskDao: TaskV2Dao,
        sessionManager: SessionManager,
        drawingPinsDao: DrawingPinsV2Dao
    ) {
        val sharedViewModel = NavHostPresenterActivity.activityInstance?.let {
            ViewModelProvider(it).get(SharedViewModel::class.java)
        }

        task?.let { newTask ->
            GlobalScope.launch {

                val dbTask = taskDao.getTaskByID(newTask.id)
                if (dbTask != null) {
                    return@launch
                }

                sessionManager.saveUpdatedAtTimeStamp(newTask.updatedAt)
                taskDao.insertTaskData(newTask)
                newTask.pinData?.let { drawingPinsDao.insertSinglePinData(it) }

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
                        val rootOngoingToMeTasks = synchronized(allTasks) {
                            allTasks.filter {
                                it.taskRootState.equals(TaskRootStateTags.Ongoing.tagValue, true) &&
                                        (it.toMeState.equals(
                                            TaskStatus.NEW.name,
                                            true
                                        ) || it.toMeState.equals(TaskStatus.ONGOING.name, true))
                            }
                                .sortedByDescending { it.updatedAt }.toMutableList()
                        }
                        CeibroApplication.CookiesManager.rootOngoingToMeTasks.postValue(
                            rootOngoingToMeTasks
                        )
                    }
                    if (newTask.isCreator) {
                        val rootOngoingFromMeTasks =
                            synchronized(allTasks) {
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

                            }
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

                    CeibroApplication.CookiesManager.rootApprovalAllTasks.postValue(allApprovalTasks)


                    val rootApprovalInReviewPendingTasks =
                        allApprovalTasks.filter {
                            it.taskRootState.equals(TaskRootStateTags.Approval.tagValue, true) &&
                                    (it.userSubState.equals(
                                        TaskRootStateTags.InReview.tagValue,
                                        true
                                    ))
                        }
                            .sortedByDescending { it.updatedAt }.toMutableList()

                    val rootApprovalToReviewTasks =
                        allApprovalTasks.filter {
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
                                CeibroApplication.CookiesManager.fromMeUnreadTasks.value
                                    ?: mutableListOf()
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
                                CeibroApplication.CookiesManager.fromMeOngoingTasks.value
                                    ?: mutableListOf()
                            val foundTask = allFromMeOngoingTasks.find { it.id == newTask.id }
                            if (foundTask != null) {
                                val index = allFromMeOngoingTasks.indexOf(foundTask)
                                allFromMeOngoingTasks.removeAt(index)
                            }
                            allFromMeOngoingTasks.add(newTask)
                            val ongoingTasks =
                                allFromMeOngoingTasks.sortedByDescending { it.updatedAt }
                                    .toMutableList()
                            CeibroApplication.CookiesManager.fromMeOngoingTasks.postValue(
                                ongoingTasks
                            )
                        }

                        TaskStatus.DONE.name.lowercase() -> {
                            val allFromMeDoneTasks =
                                CeibroApplication.CookiesManager.fromMeDoneTasks.value
                                    ?: mutableListOf()
                            val foundTask = allFromMeDoneTasks.find { it.id == newTask.id }
                            if (foundTask != null) {
                                val index = allFromMeDoneTasks.indexOf(foundTask)
                                allFromMeDoneTasks.removeAt(index)
                            }
                            allFromMeDoneTasks.add(newTask)
                            val doneTasks = allFromMeDoneTasks.sortedByDescending { it.updatedAt }
                                .toMutableList()
                            CeibroApplication.CookiesManager.fromMeDoneTasks.postValue(doneTasks)
                        }
                    }
                }

                if (newTask.isAssignedToMe) {
                    when (newTask.toMeState) {
                        TaskStatus.NEW.name.lowercase() -> {
                            val allToMeNewTasks =
                                CeibroApplication.CookiesManager.toMeNewTasks.value
                                    ?: mutableListOf()
                            val foundTask = allToMeNewTasks.find { it.id == newTask.id }
                            if (foundTask != null) {
                                val index = allToMeNewTasks.indexOf(foundTask)
                                allToMeNewTasks.removeAt(index)
                            }
                            allToMeNewTasks.add(newTask)
                            val newTasks =
                                allToMeNewTasks.sortedByDescending { it.updatedAt }.toMutableList()
                            CeibroApplication.CookiesManager.toMeNewTasks.postValue(newTasks)
                        }

                        TaskStatus.ONGOING.name.lowercase() -> {
                            val allToMeOngoingTasks =
                                CeibroApplication.CookiesManager.toMeOngoingTasks.value
                                    ?: mutableListOf()
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
                                CeibroApplication.CookiesManager.toMeDoneTasks.value
                                    ?: mutableListOf()
                            val foundTask = allToMeDoneTasks.find { it.id == newTask.id }
                            if (foundTask != null) {
                                val index = allToMeDoneTasks.indexOf(foundTask)
                                allToMeDoneTasks.removeAt(index)
                            }
                            allToMeDoneTasks.add(newTask)
                            val doneTasks =
                                allToMeDoneTasks.sortedByDescending { it.updatedAt }.toMutableList()
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

    suspend fun updateForwardedToMeNewTaskInLocal(
        completeData: ForwardedToMeNewTaskV2Response?,
        taskDao: TaskV2Dao,
        userId: String?,
        sessionManager: SessionManager,
        drawingPinsDao: DrawingPinsV2Dao
    ) {
        val sharedViewModel = NavHostPresenterActivity.activityInstance?.let {
            ViewModelProvider(it).get(SharedViewModel::class.java)
        }
        completeData?.let { newData ->
            GlobalScope.launch {
                sessionManager.saveUpdatedAtTimeStamp(newData.task.updatedAt)
                taskDao.insertTaskData(newData.task)
                taskDao.insertMultipleEvents(newData.taskEvents)
                newData.task.pinData?.let { drawingPinsDao.insertSinglePinData(it) }

                if (newData.task.taskRootState.equals(TaskRootStateTags.Ongoing.tagValue, true)) {
                    val rootOngoingAllTasks =
                        CeibroApplication.CookiesManager.rootOngoingAllTasks.value
                            ?: mutableListOf()
                    val foundTask = rootOngoingAllTasks.find { it.id == newData.task.id }
                    if (foundTask != null) {
                        val index = rootOngoingAllTasks.indexOf(foundTask)
                        rootOngoingAllTasks.removeAt(index)
                    }
                    rootOngoingAllTasks.add(newData.task)
                    val allTasks =
                        rootOngoingAllTasks.sortedByDescending { it.updatedAt }
                            .toMutableList()

                    CeibroApplication.CookiesManager.rootOngoingAllTasks.postValue(allTasks)

                    if (newData.task.isAssignedToMe) {
                        val rootOngoingToMeTasks =
                            allTasks.filter {
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
                    if (newData.task.isCreator) {
                        val rootOngoingFromMeTasks =
                            allTasks.filter {
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

//                updateAllTasksLists(taskDao)
                if (newData.task.pinData != null) {
                    EventBus.getDefault().post(LocalEvents.UpdateDrawingPins(newData.task.pinData))
                }
                EventBus.getDefault().post(LocalEvents.RefreshTasksData())


            }.join()
        }
    }


    suspend fun updateForwardTaskInLocal(
        eventData: EventV2Response.Data?,
        taskDao: TaskV2Dao,
        userId: String?,
        sessionManager: SessionManager,
        drawingPinsDao: DrawingPinsV2Dao
    ) {
        val sharedViewModel = NavHostPresenterActivity.activityInstance?.let {
            ViewModelProvider(it).get(SharedViewModel::class.java)
        }
        if (eventData != null) {
            val isExists = TaskEventsList.isExists(
                SocketHandler.TaskEvent.TASK_FORWARDED.name, eventData.id, true
            )
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
                        EventBus.getDefault().post(LocalEvents.UpdateTaskInDetails(task))
                    }
                    if (eventData.pinData != null) {
                        drawingPinsDao.insertSinglePinData(eventData.pinData)
                    }
                    taskDao.insertEventData(taskEvent)

//                    if (eventData.newTaskData.isAssignedToMe) {
//                        sharedViewModel?.isToMeUnread?.value = true
//                        sessionManager.saveToMeUnread(true)
//                    }
//                    if (eventData.newTaskData.isCreator) {
//                        sharedViewModel?.isFromMeUnread?.value = true
//                        sessionManager.saveFromMeUnread(true)
//                    }

                    if (task != null) {
                        updateAllTasksListForCommentAndForward(taskDao, eventData, task)
                    }

                }.join()

                val handler = Handler(Looper.getMainLooper())
                handler.postDelayed(Runnable {
                    EventBus.getDefault().post(LocalEvents.TaskEvent(taskEvent))
                    EventBus.getDefault().post(LocalEvents.RefreshTasksData())
                    EventBus.getDefault().post(LocalEvents.UpdateDrawingPins(eventData.pinData))
                }, 50)

                TaskEventsList.removeEvent(
                    SocketHandler.TaskEvent.TASK_FORWARDED.name,
                    eventData.id
                )
            }
        }

    }

    suspend fun updateGenericTaskSeenInLocal(
        taskSeen: TaskSeenResponse.TaskSeen?,
        taskDao: TaskV2Dao,
        userId: String?,
        sessionManager: SessionManager,
        drawingPinsDao: DrawingPinsV2Dao,
        inboxV2Dao: InboxV2Dao
    ) {
        if (taskSeen != null) {
            val exist = TaskEventsList.isExists(
                SocketHandler.TaskEvent.TASK_SEEN.name, taskSeen.taskId, true
            )
            if (!exist) {
                var updatedTask: CeibroTaskV2? = null
//                println("Heartbeat SocketEvent TASK_SEEN started ${System.currentTimeMillis()}")
                sessionManager.saveUpdatedAtTimeStamp(taskSeen.taskUpdatedAt)
                GlobalScope.launch {
                    val task = taskDao.getTaskByID(taskSeen.taskId)
                    val inboxTask = inboxV2Dao.getInboxTaskData(taskSeen.taskId)

                    if (task != null) {
                        task.seenBy = taskSeen.seenBy
                        task.isSeenByMe = taskSeen.newTaskData.isSeenByMe

                        if (taskSeen.creatorStateChanged || taskSeen.stateChanged) {
                            task.updatedAt = taskSeen.taskUpdatedAt

                            val foundState =
                                task.assignedToState.find { it.userId == taskSeen.state.userId }
                            if (foundState != null) {
                                if (taskSeen.state.state.isEmpty()) {
                                    val stateIndex = task.assignedToState.indexOf(foundState)
                                    foundState.state = taskSeen.newTaskData.userSubState
                                    task.assignedToState[stateIndex] = foundState
                                } else {
                                    val stateIndex = task.assignedToState.indexOf(foundState)
                                    task.assignedToState[stateIndex] = taskSeen.state
                                }
                            } else {
                                //else will run if state object does not contain userId
                                val foundState2 =
                                    task.assignedToState.find { it.userId == taskSeen.eventInitiator }
                                if (foundState2 != null) {
                                    if (taskSeen.state.state.isEmpty()) {
                                        val stateIndex = task.assignedToState.indexOf(foundState2)
                                        foundState2.state = taskSeen.newTaskData.userSubState
                                        task.assignedToState[stateIndex] = foundState2
                                    } else {
                                        val stateIndex = task.assignedToState.indexOf(foundState2)
                                        task.assignedToState[stateIndex] = taskSeen.state
                                    }
                                }
                            }

                            task.taskRootState = taskSeen.newTaskData.taskRootState
                            task.isCanceled = taskSeen.newTaskData.isCanceled
                            task.isHiddenByMe = taskSeen.newTaskData.isHiddenByMe
                            task.userSubState = taskSeen.newTaskData.userSubState
                            task.creatorState = taskSeen.newTaskData.creatorState
                            task.isTaskInApproval = taskSeen.newTaskData.isTaskInApproval
                            task.rootState = taskSeen.newTaskData.rootState
                            task.toMeState = taskSeen.newTaskData.toMeState
                            task.fromMeState = taskSeen.newTaskData.fromMeState
                            task.hiddenState = taskSeen.newTaskData.hiddenState
                            task.pinData = taskSeen.pinData

                            taskDao.updateTask(task)
                        } else {

                            task.taskRootState = taskSeen.newTaskData.taskRootState
                            task.isCanceled = taskSeen.newTaskData.isCanceled
                            task.isHiddenByMe = taskSeen.newTaskData.isHiddenByMe
                            task.userSubState = taskSeen.newTaskData.userSubState
                            task.creatorState = taskSeen.newTaskData.creatorState
                            task.isTaskInApproval = taskSeen.newTaskData.isTaskInApproval
                            task.rootState = taskSeen.newTaskData.rootState
                            task.toMeState = taskSeen.newTaskData.toMeState
                            task.fromMeState = taskSeen.newTaskData.fromMeState
                            task.hiddenState = taskSeen.newTaskData.hiddenState
                            task.pinData = taskSeen.pinData

                            taskDao.updateTask(task)
                        }
                    }
                    updatedTask = task

                    if (taskSeen.pinData != null) {
                        drawingPinsDao.insertSinglePinData(taskSeen.pinData)
                    }

                    if (inboxTask != null) {
                        inboxTask.isSeen = true
                        inboxTask.unSeenNotifCount = 0
                        inboxTask.taskState = taskSeen.newTaskData.userSubState
                        if (taskSeen.stateChanged) {
                            inboxTask.actionType = SocketHandler.TaskEvent.IB_STATE_CHANGED.name
                        }
                        inboxV2Dao.insertInboxItem(inboxTask)

                        EventBus.getDefault().post(LocalEvents.UpdateInboxItemSeen(inboxTask))
//                        EventBus.getDefault().post(LocalEvents.RefreshInboxData())
                    }


                    updateAllTasksListForTaskSeen(taskDao, taskSeen)


//                    val allInboxTasks = inboxV2Dao.getAllInboxItems().toMutableList()
//                    CeibroApplication.CookiesManager.allInboxTasks.postValue(allInboxTasks)

                }.join()
                if (taskSeen.creatorStateChanged || taskSeen.stateChanged) {
                    EventBus.getDefault().post(LocalEvents.TaskSeenEvent(updatedTask))
                }
                EventBus.getDefault().post(LocalEvents.RefreshTasksData())
                EventBus.getDefault().post(LocalEvents.UpdateDrawingPins(taskSeen.pinData))

                TaskEventsList.removeEvent(
                    SocketHandler.TaskEvent.TASK_SEEN.name,
                    taskSeen.taskId
                )
//                println("Heartbeat SocketEvent TASK_SEEN ended ${System.currentTimeMillis()}")
            }

        }
    }


    suspend fun updateTaskCommentInLocal(
        eventData: EventV2Response.Data?,
        taskDao: TaskV2Dao,
        inboxV2Dao: InboxV2Dao,
        userId: String?,
        sessionManager: SessionManager,
        drawingPinsDao: DrawingPinsV2Dao
    ) {
        if (eventData != null) {
            val isExists = TaskEventsList.isExists(
                SocketHandler.TaskEvent.NEW_TASK_COMMENT.name, eventData.id, true
            )
            val sharedViewModel = NavHostPresenterActivity.activityInstance?.let {
                ViewModelProvider(it).get(SharedViewModel::class.java)
            }

            if (!isExists) {
                val currentUser = sessionManager.getUserObj()
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
//                println("Heartbeat SocketEvent NEW_TASK_COMMENT started ${System.currentTimeMillis()}")
                GlobalScope.launch {
                    sessionManager.saveUpdatedAtTimeStamp(eventData.taskUpdatedAt)
                    val task = taskDao.getTaskByID(eventData.taskId)
                    val inboxTask = inboxV2Dao.getInboxTaskData(eventData.taskId)

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
                                    taskData?.pinData?.let { drawingPinsDao.insertSinglePinData(it) }


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
                        drawingPinsDao.insertSinglePinData(eventData.pinData)
                    }

//                    if (inboxTask != null && eventData.initiator.id != currentUser?.id) {
//                        inboxTask.actionBy = eventData.initiator
//                        inboxTask.createdAt = eventData.createdAt
//                        inboxTask.actionType = SocketHandler.TaskEvent.IB_NEW_TASK_COMMENT.name
//                        inboxTask.isSeen = false
//                        inboxTask.unSeenNotifCount = inboxTask.unSeenNotifCount + 1
//
//                        if (eventData.commentData != null) {
//                            val newActionFiles = if (eventData.commentData.files.isNotEmpty()) {
//                                eventData.commentData.files.map {
//                                    ActionFilesData(
//                                        fileUrl = it.fileUrl
//                                    )
//                                }
//                            } else {
//                                mutableListOf()
//                            }
//                            inboxTask.actionFiles = newActionFiles.toMutableList()
//                            inboxTask.actionDescription = eventData.commentData.message ?: ""
//                        } else {
//                            inboxTask.actionFiles = mutableListOf()
//                            inboxTask.actionDescription = ""
//                        }
//
//                        inboxV2Dao.insertInboxItem(inboxTask)
//
////                        val allInboxTasks = inboxV2Dao.getAllInboxItems().toMutableList()
////                        CeibroApplication.CookiesManager.allInboxTasks.postValue(allInboxTasks)
//
//                        EventBus.getDefault().post(LocalEvents.RefreshInboxSingleEvent(inboxTask))
//                    }

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
//                println("Heartbeat SocketEvent NEW_TASK_COMMENT ended ${System.currentTimeMillis()}")
            }
        }
    }

    suspend fun updateTaskApproveOrRejectInLocal(
        eventData: EventV2Response.Data?,
        taskDao: TaskV2Dao,
        inboxV2Dao: InboxV2Dao,
        userId: String?,
        sessionManager: SessionManager,
        drawingPinsDao: DrawingPinsV2Dao
    ) {
        if (eventData != null) {
            val isExists = TaskEventsList.isExists(
                SocketHandler.TaskEvent.NEW_TASK_COMMENT.name, eventData.id, true
            )

            if (!isExists) {
                val currentUser = sessionManager.getUserObj()
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
//                println("Heartbeat SocketEvent NEW_TASK_COMMENT started ${System.currentTimeMillis()}")
                GlobalScope.launch {
                    sessionManager.saveUpdatedAtTimeStamp(eventData.taskUpdatedAt)
                    val task = taskDao.getTaskByID(eventData.taskId)
                    val inboxTask = inboxV2Dao.getInboxTaskData(eventData.taskId)

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
                                    taskData?.pinData?.let { drawingPinsDao.insertSinglePinData(it) }


                                    updateAllTasksLists(taskDao)

                                }
                            } else {
                            }
                        }
                    }

                    if (eventData.pinData != null) {
                        drawingPinsDao.insertSinglePinData(eventData.pinData)
                    }

                    if (inboxTask != null && eventData.initiator.id != currentUser?.id) {
                        inboxTask.actionBy = eventData.initiator
                        inboxTask.createdAt = eventData.createdAt
                        inboxTask.actionType =
                            if (eventData.eventType == SocketHandler.TaskEvent.TASK_APPROVED.name) {
                                SocketHandler.TaskEvent.IB_TASK_APPROVED.name
                            } else if (eventData.eventType == SocketHandler.TaskEvent.TASK_REJECTED_CLOSED.name) {
                                SocketHandler.TaskEvent.IB_TASK_REJECTED_CLOSED.name
                            } else if (eventData.eventType == SocketHandler.TaskEvent.TASK_REJECTED_REOPENED.name) {
                                SocketHandler.TaskEvent.IB_TASK_REJECTED_REOPEND.name
                            } else {
                                eventData.eventType
                            }
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

                        inboxV2Dao.insertInboxItem(inboxTask)

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


    suspend fun updateEventInLocal(
        eventData: PinnedCommentV2Response,
        taskDao: TaskV2Dao,
        sessionManager: SessionManager
    ) {
        val isExists = TaskEventsList.isExists(
            SocketHandler.TaskEvent.TASK_EVENT_UPDATED.name, eventData.data.eventId, true
        )

        if (!isExists) {
            GlobalScope.launch {
                val currentUser = sessionManager.getUserObj()
                val event = taskDao.getSingleEvent(eventData.data.taskId, eventData.data.eventId)
                if (event != null) {
                    event.isPinned = eventData.data.isPinned
                    event.updatedAt = eventData.data.updatedAt

                    taskDao.insertEventData(event)
                }
                EventBus.getDefault().post(LocalEvents.TaskEventUpdate(event))

            }.join()

            TaskEventsList.removeEvent(
                SocketHandler.TaskEvent.TASK_EVENT_UPDATED.name,
                eventData.data.eventId
            )
        }

    }

    suspend fun updateTaskUnCanceledInLocal(
        eventData: EventV2Response.Data?,
        taskDao: TaskV2Dao,
        sessionManager: SessionManager,
        drawingPinsDao: DrawingPinsV2Dao
    ) {
        val sharedViewModel = NavHostPresenterActivity.activityInstance?.let {
            ViewModelProvider(it).get(SharedViewModel::class.java)
        }
        if (eventData != null) {

            val isExists = TaskEventsList.isExists(
                SocketHandler.TaskEvent.UN_CANCEL_TASK.name, eventData.taskId, true
            )
            if (!isExists) {
                GlobalScope.launch {
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
                    sessionManager.saveUpdatedAtTimeStamp(eventData.taskUpdatedAt)
                    val task = taskDao.getTaskByID(eventData.taskId)

                    if (task != null) {
                        task.seenBy = eventData.taskData.seenBy
                        task.hiddenBy = eventData.taskData.hiddenBy
                        task.updatedAt = eventData.taskUpdatedAt
                        val assignToList = task.assignedToState
                        assignToList.map {
                            it.state = TaskStatus.NEW.name.lowercase()
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
                        EventBus.getDefault().post(LocalEvents.UpdateTaskInDetails(task))
                    }
                    taskDao.insertEventData(taskEvent)
//                    if (eventData.pinData != null) {
//                        drawingPinsDao.insertSinglePinData(eventData.pinData)
//                    }
//
//                    if (eventData.newTaskData.isAssignedToMe) {
//                        sharedViewModel?.isToMeUnread?.value = true
//                        sessionManager.saveToMeUnread(true)
//                    }
//                    if (eventData.newTaskData.isCreator) {
//                        sharedViewModel?.isFromMeUnread?.value = true
//                        sessionManager.saveFromMeUnread(true)
//                    }

                    updateAllTasksLists(taskDao)

                    EventBus.getDefault().post(LocalEvents.TaskEvent(taskEvent))
                }.join()
                EventBus.getDefault().post(LocalEvents.RefreshTasksData())
                EventBus.getDefault().post(LocalEvents.UpdateDrawingPins(eventData.pinData))

                TaskEventsList.removeEvent(
                    SocketHandler.TaskEvent.UN_CANCEL_TASK.name,
                    eventData.taskId
                )
            }
        }
    }

    suspend fun updateTaskCanceledInLocal(
        eventData: EventV2Response.Data?,
        taskDao: TaskV2Dao,
        userId: String?,
        sessionManager: SessionManager,
        drawingPinsDao: DrawingPinsV2Dao
    ) {
        val sharedViewModel = NavHostPresenterActivity.activityInstance?.let {
            ViewModelProvider(it).get(SharedViewModel::class.java)
        }

        if (eventData != null) {
            val isExists = TaskEventsList.isExists(
                SocketHandler.TaskEvent.CANCELED_TASK.name, eventData.taskId, true
            )
            if (!isExists) {
                GlobalScope.launch {
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
                        isPinned = true
                    )
                    sessionManager.saveUpdatedAtTimeStamp(eventData.taskUpdatedAt)
                    val task = taskDao.getTaskByID(eventData.taskId)

                    if (task != null) {
                        task.seenBy = eventData.taskData.seenBy
                        task.hiddenBy = eventData.taskData.hiddenBy
                        task.updatedAt = eventData.taskUpdatedAt
                        val assignToList = task.assignedToState
                        assignToList.map {
                            it.state = eventData.newTaskData.creatorState
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
                        EventBus.getDefault().post(LocalEvents.UpdateTaskInDetails(task))
                    }
                    taskDao.insertEventData(taskEvent)
                    if (eventData.pinData != null) {
                        drawingPinsDao.insertSinglePinData(eventData.pinData)
                    }


                    updateAllTasksLists(taskDao)

//                    EventBus.getDefault().post(LocalEvents.TaskEvent(taskEvent))
                    EventBus.getDefault().post(LocalEvents.TaskCanceledEvent(task, taskEvent))
                }.join()
                EventBus.getDefault().post(LocalEvents.RefreshTasksData())
                EventBus.getDefault().post(LocalEvents.UpdateDrawingPins(eventData.pinData))
                TaskEventsList.removeEvent(
                    SocketHandler.TaskEvent.CANCELED_TASK.name,
                    eventData.taskId
                )
            }
        }
    }

    suspend fun updateTaskDoneInLocal(
        eventData: EventV2Response.Data?,
        taskDao: TaskV2Dao,
        inboxV2Dao: InboxV2Dao,
        sessionManager: SessionManager,
        drawingPinsDao: DrawingPinsV2Dao
    ): CeibroTaskV2? {
        var updatedTask: CeibroTaskV2? = null
        if (eventData != null) {
            val isExists = TaskEventsList.isExists(
                SocketHandler.TaskEvent.TASK_DONE.name, eventData.taskId, true
            )
            if (!isExists) {
//                val currentUser = sessionManager.getUserObj()
//                val sharedViewModel = NavHostPresenterActivity.activityInstance?.let {
//                    ViewModelProvider(it).get(SharedViewModel::class.java)
//                }
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
                    isPinned = true
                )
                GlobalScope.launch {
                    sessionManager.saveUpdatedAtTimeStamp(eventData.taskUpdatedAt)
                    val task = taskDao.getTaskByID(eventData.taskId)
//                    val inboxTask = inboxV2Dao.getInboxTaskData(eventData.taskId)

                    if (task != null) {
                        task.seenBy = eventData.taskData.seenBy
                        task.hiddenBy = eventData.taskData.hiddenBy
                        task.updatedAt = eventData.taskUpdatedAt
//                        task.creatorState = eventData.newTaskData.creatorState

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
                        drawingPinsDao.insertSinglePinData(eventData.pinData)
                    }

                    updatedTask = task


                    updateAllTasksLists(taskDao)

//                    if (inboxTask != null) {
//                        inboxTask.actionBy = eventData.initiator
//                        inboxTask.createdAt = eventData.createdAt
//                        inboxTask.actionType = SocketHandler.TaskEvent.IB_TASK_DONE.name
//                        inboxTask.taskState = eventData.newTaskData.creatorState
//                        inboxTask.isSeen = false
//                        inboxTask.unSeenNotifCount = inboxTask.unSeenNotifCount + 1
//
//                        if (eventData.commentData != null) {
//                            val newActionFiles = if (eventData.commentData.files.isNotEmpty()) {
//                                eventData.commentData.files.map {
//                                    ActionFilesData(
//                                        fileUrl = it.fileUrl
//                                    )
//                                }
//                            } else {
//                                mutableListOf()
//                            }
//                            inboxTask.actionFiles = newActionFiles.toMutableList()
//                            inboxTask.actionDescription = eventData.commentData.message ?: ""
//                        } else {
//                            inboxTask.actionFiles = mutableListOf()
//                            inboxTask.actionDescription = ""
//                        }
//
//                        inboxV2Dao.insertInboxItem(inboxTask)
//
////                        val allInboxTasks = inboxV2Dao.getAllInboxItems().toMutableList()
////                        CeibroApplication.CookiesManager.allInboxTasks.postValue(allInboxTasks)
//
//                        EventBus.getDefault().post(LocalEvents.RefreshInboxSingleEvent(inboxTask))
//                    }

                }.join()

                val handler = Handler(Looper.getMainLooper())
                handler.postDelayed(Runnable {
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

    suspend fun updateTaskJoinedInLocal(
        eventData: EventV2Response.Data?,
        taskDao: TaskV2Dao,
        sessionManager: SessionManager,
        drawingPinsDao: DrawingPinsV2Dao
    ) {
        val sharedViewModel = NavHostPresenterActivity.activityInstance?.let {
            ViewModelProvider(it).get(SharedViewModel::class.java)
        }

        if (eventData != null) {
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

                if (task != null) {
                    task.seenBy = eventData.taskData.seenBy
                    task.hiddenBy = eventData.taskData.hiddenBy
                    task.updatedAt = eventData.taskUpdatedAt
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

                    val invitedList = task.invitedNumbers.toMutableList()
                    val invited =
                        invitedList.find { it.phoneNumber == eventData.initiator.phoneNumber }
                    if (invited != null) {
                        invitedList.remove(invited)
                        task.invitedNumbers = invitedList
                    }
                    val assignee = AssignedToState(
                        firstName = eventData.initiator.firstName,
                        id = eventData.initiator.id,
                        phoneNumber = eventData.initiator.phoneNumber ?: "",
                        profilePic = eventData.initiator.profilePic,
                        state = TaskStatus.NEW.name.lowercase(),
                        surName = eventData.initiator.surName,
                        userId = eventData.initiator.id
                    )
                    val assignToList = task.assignedToState
                    val findAssignee = assignToList.find { it.userId == assignee.userId }
                    if (findAssignee == null) {
                        assignToList.add(assignee)
                    }
                    task.assignedToState = assignToList

                    taskDao.updateTask(task)
                }
                taskDao.insertEventData(taskEvent)
                if (eventData.pinData != null) {
                    drawingPinsDao.insertSinglePinData(eventData.pinData)
                }


                updateAllTasksLists(taskDao)

            }.join()

            EventBus.getDefault().post(LocalEvents.TaskEvent(taskEvent))
            EventBus.getDefault().post(LocalEvents.RefreshTasksData())
            EventBus.getDefault().post(LocalEvents.UpdateDrawingPins(eventData.pinData))
        }
    }


    suspend fun updateTaskHideInLocal(
        hideData: HideTaskResponse?,
        taskDao: TaskV2Dao,
        sessionManager: SessionManager,
        drawingPinsDao: DrawingPinsV2Dao
    ) {
        if (hideData != null) {
            val isExists = TaskEventsList.isExists(
                SocketHandler.TaskEvent.TASK_HIDDEN.name, hideData.taskId, true
            )
            if (!isExists) {
                GlobalScope.launch {
                    sessionManager.saveUpdatedAtTimeStamp(hideData.taskUpdatedAt)
                    taskDao.updateTaskHideUnHide(
                        taskId = hideData.taskId,
                        isHiddenByMe = hideData.newTaskData.isHiddenByMe,
                        hiddenBy = hideData.hiddenBy,
                        updatedAt = hideData.taskUpdatedAt,
                        toMeState = hideData.newTaskData.toMeState,
                        fromMeState = hideData.newTaskData.fromMeState,
                        hiddenState = hideData.newTaskData.hiddenState,
                        pinData = hideData.pinData,
                        taskRootState = hideData.newTaskData.taskRootState,
                        isCanceled = hideData.newTaskData.isCanceled,
                        isTaskInApproval = hideData.newTaskData.isTaskInApproval,
                        userSubState = hideData.newTaskData.userSubState
                    )
                    if (hideData.pinData != null) {
                        drawingPinsDao.insertSinglePinData(hideData.pinData)
                    }

                    updateAllTasksLists(taskDao)
                }.join()
                EventBus.getDefault().post(LocalEvents.RefreshTasksData())
                EventBus.getDefault().post(LocalEvents.UpdateDrawingPins(hideData.pinData))

                TaskEventsList.removeEvent(
                    SocketHandler.TaskEvent.TASK_HIDDEN.name, hideData.taskId
                )
            }
        }
    }

    suspend fun updateTaskUnHideInLocal(
        hideData: HideTaskResponse?,
        taskDao: TaskV2Dao,
        sessionManager: SessionManager,
        drawingPinsDao: DrawingPinsV2Dao
    ) {
        if (hideData != null) {
            val isExists = TaskEventsList.isExists(
                SocketHandler.TaskEvent.TASK_SHOWN.name, hideData.taskId, true
            )
            if (!isExists) {
                GlobalScope.launch {
                    sessionManager.saveUpdatedAtTimeStamp(hideData.taskUpdatedAt)
                    taskDao.updateTaskHideUnHide(
                        taskId = hideData.taskId,
                        isHiddenByMe = hideData.newTaskData.isHiddenByMe,
                        hiddenBy = hideData.hiddenBy,
                        updatedAt = hideData.taskUpdatedAt,
                        toMeState = hideData.newTaskData.toMeState,
                        fromMeState = hideData.newTaskData.fromMeState,
                        hiddenState = hideData.newTaskData.hiddenState,
                        pinData = hideData.pinData,
                        taskRootState = hideData.newTaskData.taskRootState,
                        isCanceled = hideData.newTaskData.isCanceled,
                        isTaskInApproval = hideData.newTaskData.isTaskInApproval,
                        userSubState = hideData.newTaskData.userSubState
                    )
                    if (hideData.pinData != null) {
                        drawingPinsDao.insertSinglePinData(hideData.pinData)
                    }


                    updateAllTasksLists(taskDao)

                }.join()
                EventBus.getDefault().post(LocalEvents.RefreshTasksData())
                EventBus.getDefault().post(LocalEvents.UpdateDrawingPins(hideData.pinData))

                TaskEventsList.removeEvent(
                    SocketHandler.TaskEvent.TASK_SHOWN.name, hideData.taskId
                )

            }
        }
    }


    suspend fun updateTaskReOpenedInLocal(
        eventData: EventV2Response.Data?,
        taskDao: TaskV2Dao,
        sessionManager: SessionManager,
        drawingPinsDao: DrawingPinsV2Dao
    ) {
        if (eventData != null) {
            val isExists = TaskEventsList.isExists(
                SocketHandler.TaskEvent.TASK_REOPEN.name, eventData.taskId, true
            )
            if (!isExists) {
                GlobalScope.launch {
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
                    sessionManager.saveUpdatedAtTimeStamp(eventData.taskUpdatedAt)
                    val task = taskDao.getTaskByID(eventData.taskId)

                    if (task != null) {
                        task.seenBy = eventData.taskData.seenBy
                        task.hiddenBy = eventData.taskData.hiddenBy
                        task.updatedAt = eventData.taskUpdatedAt
                        val assignToList = task.assignedToState
                        assignToList.map {
                            it.state = TaskStatus.NEW.name.lowercase()
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
                        task.taskRootState = eventData.newTaskData.taskRootState
                        task.isCanceled = eventData.newTaskData.isCanceled
                        task.isHiddenByMe = eventData.newTaskData.isHiddenByMe
                        task.userSubState = eventData.newTaskData.userSubState
                        task.creatorState = eventData.newTaskData.creatorState
                        task.isTaskInApproval = eventData.newTaskData.isTaskInApproval
                        task.isCreator = eventData.newTaskData.isCreator
                        task.rootState = eventData.newTaskData.rootState
                        task.isTaskViewer = eventData.newTaskData.isTaskViewer
                        task.isTaskConfirmer = eventData.newTaskData.isTaskConfirmer
                        task.isSeenByMe = eventData.newTaskData.isSeenByMe
                        task.fromMeState = eventData.newTaskData.fromMeState
                        task.toMeState = eventData.newTaskData.toMeState
                        task.hiddenState = eventData.newTaskData.hiddenState
                        task.eventsCount = task.eventsCount + 1
                        task.pinData = eventData.pinData

                        taskDao.updateTask(task)
                        EventBus.getDefault().post(LocalEvents.UpdateTaskInDetails(task))
                    }
                    taskDao.insertEventData(taskEvent)
                    if (eventData.pinData != null) {
                        drawingPinsDao.insertSinglePinData(eventData.pinData)
                    }

                    updateTasksListsForReOpen(taskDao)

                    EventBus.getDefault().post(LocalEvents.TaskEvent(taskEvent))
                }.join()

                EventBus.getDefault().post(LocalEvents.RefreshTasksData())
                EventBus.getDefault().post(LocalEvents.UpdateDrawingPins(eventData.pinData))
                TaskEventsList.removeEvent(
                    SocketHandler.TaskEvent.TASK_REOPEN.name,
                    eventData.taskId
                )
            }
        }
    }


    suspend fun addOrUpdateInboxTaskInLocal(
        inboxTask: CeibroInboxV2?,
        inboxV2Dao: InboxV2Dao,
        sessionManager: SessionManager
    ) {
        if (inboxTask != null) {
            val isExists = TaskEventsList.isExists(
                SocketHandler.TaskEvent.IB_TASK_CREATED.name, inboxTask.taskId, true
            )
            if (!isExists) {
                GlobalScope.launch {
                    sessionManager.saveInboxUpdatedAtTimeStamp(inboxTask.createdAt)

                    if (inboxTask.actionType.equals(
                            SocketHandler.TaskEvent.IB_TASK_DONE.name,
                            true
                        ) ||
                        inboxTask.actionType.equals(
                            SocketHandler.TaskEvent.IB_CANCELED_TASK.name,
                            true
                        )
                    ) {

                        val existingTask = inboxV2Dao.getInboxTaskData(inboxTask.taskId)
                        if (existingTask != null) {
                            inboxV2Dao.insertInboxItem(inboxTask)
                        } else {
                            // this case will be ignored because if user created task and cancelled it without any other event,
                            // there creator will not have that task in inbox list, so don't add in this case
                        }
                    } else {
                        inboxV2Dao.insertInboxItem(inboxTask)
                    }

                    val allInboxTasks = inboxV2Dao.getAllInboxItems().toMutableList()
                    CeibroApplication.CookiesManager.allInboxTasks.postValue(allInboxTasks)

                }.join()
                EventBus.getDefault().post(LocalEvents.RefreshInboxData())
//                EventBus.getDefault().post(LocalEvents.UpdateDrawingPins(hideData.pinData))

                TaskEventsList.removeEvent(
                    SocketHandler.TaskEvent.IB_TASK_CREATED.name, inboxTask.taskId
                )

            }
        }
    }

    @Inject
    lateinit var draftNewTaskV2Internal: DraftNewTaskV2Dao

    @Inject
    lateinit var taskRepositoryInternal: ITaskRepository

    @Inject
    lateinit var remoteTaskInternal: TaskRemoteDataSource

    @Inject
    lateinit var taskDaoInternal: TaskV2Dao

    @Inject
    lateinit var sessionManagerInternal: SessionManager

    @Inject
    lateinit var dashboardRepositoryInternal: IDashboardRepository

    suspend fun syncDraftTask(context: Context) {
//        Log.d("SyncDraftTask", "syncDraftTask")
//        val user = sessionManagerInternal.getUser().value
        val unSyncedRecords = draftNewTaskV2Internal.getUnSyncedRecords() ?: emptyList()


        if (unSyncedRecords.isNotEmpty()) {
            launch {
                val serviceIntent = Intent(context, CreateNewTaskService::class.java)
                serviceIntent.putExtra("ServiceRequest", "draftUploadRequest")
                context.startService(serviceIntent)
//                 viewModel.syncDraftTask(requireContext())
            }
        }

        /*suspend fun uploadDraftTaskFiles(
            listOfLocalFiles: List<LocalFilesToStore>, taskId: String
        ) {
            val list: List<PickedImages> = listOfLocalFiles.map {
                PickedImages(
                    fileUri = Uri.parse(it.fileUri),
                    comment = it.comment,
                    fileName = it.fileName,
                    fileSizeReadAble = it.fileSizeReadAble,
                    editingApplied = it.editingApplied,
                    attachmentType = it.attachmentType,
                    file = FileUtils.getFile(context, Uri.parse(it.fileUri))
                )
            }

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

                AttachmentUploadV2Request.AttachmentMetaData(
                    fileName = file.fileName,
                    orignalFileName = file.fileName,
                    tag = tag,
                    comment = file.comment.trim()
                )
            }
            val metadataString = Gson().toJson(metaData)
            val metadataString2 =
                Gson().toJson(metadataString)     //again passing to make the json to convert into json string with slashes

            val request = AttachmentUploadV2Request(
                moduleId = taskId,
                moduleName = AttachmentModules.Task.name,
                files = attachmentUriList,
                metadata = metadataString2
            )

            when (val response = dashboardRepositoryInternal.uploadFiles(request)) {
                is ApiResponse.Success -> {
                    saveFilesInDB(
                        request.moduleName,
                        request.moduleId,
                        response.data.uploadData,
                        taskDaoInternal
                    )
                }

                is ApiResponse.Error -> {
                    alert(response.error.message)
                }
            }
        }*/

        // Define a recursive function to process records one by one
        /*suspend fun processNextRecord(records: List<NewTaskV2Entity>) {
            if (records.isEmpty()) {
                // All records have been processed, exit the recursion
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
                taskRepositoryInternal.newTaskV2WithFiles(
                    newTaskRequest, list
                ) { isSuccess, task, errorMessage ->
                    if (isSuccess) {
                        launch {
                            draftNewTaskV2Internal.deleteTaskById(newTaskRequest.taskId)

                            updateCreatedTaskInLocal(
                                task, taskDaoInternal, user?.id, sessionManagerInternal
                            )
                            // Remove the processed record from the list
                            val updatedRecords = records - newTaskRequest

                            draftRecordCallBack?.invoke(updatedRecords.size)
                            _draftRecordObserver.postValue(updatedRecords.size)
                            // Recursively process the next record


                            syncDraftRecords.postValue(updatedRecords.size)
                            //  sharedViewModel.syncedRecord.postValue(updatedRecords.size)
                            processNextRecord(updatedRecords)


                        }
                    } else {
                        //alert(errorMessage)
                    }
                }
            } else {
                taskRepositoryInternal.newTaskV2WithoutFiles(newTaskRequest) { isSuccess, task, errorMessage ->
                    if (isSuccess) {
                        launch {
                            draftNewTaskV2Internal.deleteTaskById(newTaskRequest.taskId)

                            updateCreatedTaskInLocal(
                                task, taskDaoInternal, user?.id, sessionManagerInternal
                            )
                            // Remove the processed record from the list
                            val updatedRecords = records - newTaskRequest
                            draftRecordCallBack?.invoke(updatedRecords.size)
                            _draftRecordObserver.postValue(updatedRecords.size)

                            // Recursively process the next record
                            syncDraftRecords.postValue(updatedRecords.size)
                            processNextRecord(updatedRecords)
                        }
                    } else {
                        //alert(errorMessage)
                    }
                }
            }
        }

        // Start the recursive processing
        launch {
            processNextRecord(unsyncedRecords)
        }*/
    }

    fun saveFilesInDB(
        moduleName: String, moduleId: String, uploadedFiles: List<TaskFiles>, taskDao: TaskV2Dao
    ) {
        if (moduleName.equals(AttachmentModules.Task.name, true)) {
            /*launch {
                val taskToMeLocalData =
                    TaskV2DaoHelper(taskDao).getTasks(TaskRootStateTags.ToMe.tagValue)
                val taskFromMeLocalData =
                    TaskV2DaoHelper(taskDao).getTasks(TaskRootStateTags.FromMe.tagValue)

                if (!TaskV2DaoHelper(taskDao).isTaskListEmpty(
                        TaskRootStateTags.ToMe.tagValue, taskToMeLocalData
                    )
                ) {
                    val newTask = taskToMeLocalData.allTasks.new.find { it.id == moduleId }
                    val unreadTask = taskToMeLocalData.allTasks.unread.find { it.id == moduleId }
                    val ongoingTask = taskToMeLocalData.allTasks.ongoing.find { it.id == moduleId }
                    val doneTask = taskToMeLocalData.allTasks.done.find { it.id == moduleId }

                    if (newTask != null) {
                        val allTaskList = taskToMeLocalData.allTasks.new.toMutableList()
                        val taskIndex = allTaskList.indexOf(newTask)

                        val oldFiles = newTask.files
                        val combinedFiles = oldFiles + uploadedFiles
                        newTask.files = combinedFiles

                        allTaskList[taskIndex] = newTask
                        taskToMeLocalData.allTasks.new = allTaskList
                    } else if (unreadTask != null) {
                        val allTaskList = taskToMeLocalData.allTasks.unread.toMutableList()
                        val taskIndex = allTaskList.indexOf(unreadTask)

                        val oldFiles = unreadTask.files
                        val combinedFiles = oldFiles + uploadedFiles
                        unreadTask.files = combinedFiles

                        allTaskList[taskIndex] = unreadTask
                        taskToMeLocalData.allTasks.unread = allTaskList
                    } else if (ongoingTask != null) {
                        val allTaskList = taskToMeLocalData.allTasks.ongoing.toMutableList()
                        val taskIndex = allTaskList.indexOf(ongoingTask)

                        val oldFiles = ongoingTask.files
                        val combinedFiles = oldFiles + uploadedFiles
                        ongoingTask.files = combinedFiles

                        allTaskList[taskIndex] = ongoingTask
                        taskToMeLocalData.allTasks.ongoing = allTaskList
                    } else if (doneTask != null) {
                        val allTaskList = taskToMeLocalData.allTasks.done.toMutableList()
                        val taskIndex = allTaskList.indexOf(doneTask)

                        val oldFiles = doneTask.files
                        val combinedFiles = oldFiles + uploadedFiles
                        doneTask.files = combinedFiles

                        allTaskList[taskIndex] = doneTask
                        taskToMeLocalData.allTasks.done = allTaskList
                    }

                    TaskV2DaoHelper(taskDao).insertTaskData(
                        taskToMeLocalData
                    )
                }

                if (!TaskV2DaoHelper(taskDao).isTaskListEmpty(
                        TaskRootStateTags.FromMe.tagValue, taskFromMeLocalData
                    )
                ) {
                    val newTask = taskFromMeLocalData.allTasks.new.find { it.id == moduleId }
                    val unreadTask = taskFromMeLocalData.allTasks.unread.find { it.id == moduleId }
                    val ongoingTask =
                        taskFromMeLocalData.allTasks.ongoing.find { it.id == moduleId }
                    val doneTask = taskFromMeLocalData.allTasks.done.find { it.id == moduleId }

                    if (newTask != null) {
                        val allTaskList = taskFromMeLocalData.allTasks.new.toMutableList()
                        val taskIndex = allTaskList.indexOf(newTask)

                        val oldFiles = newTask.files
                        val combinedFiles = oldFiles + uploadedFiles
                        newTask.files = combinedFiles

                        allTaskList[taskIndex] = newTask
                        taskFromMeLocalData.allTasks.new = allTaskList
                    } else if (unreadTask != null) {
                        val allTaskList = taskFromMeLocalData.allTasks.unread.toMutableList()
                        val taskIndex = allTaskList.indexOf(unreadTask)

                        val oldFiles = unreadTask.files
                        val combinedFiles = oldFiles + uploadedFiles
                        unreadTask.files = combinedFiles

                        allTaskList[taskIndex] = unreadTask
                        taskFromMeLocalData.allTasks.unread = allTaskList
                    } else if (ongoingTask != null) {
                        val allTaskList = taskFromMeLocalData.allTasks.ongoing.toMutableList()
                        val taskIndex = allTaskList.indexOf(ongoingTask)

                        val oldFiles = ongoingTask.files
                        val combinedFiles = oldFiles + uploadedFiles
                        ongoingTask.files = combinedFiles

                        allTaskList[taskIndex] = ongoingTask
                        taskFromMeLocalData.allTasks.ongoing = allTaskList
                    } else if (doneTask != null) {
                        val allTaskList = taskFromMeLocalData.allTasks.done.toMutableList()
                        val taskIndex = allTaskList.indexOf(doneTask)

                        val oldFiles = doneTask.files
                        val combinedFiles = oldFiles + uploadedFiles
                        doneTask.files = combinedFiles

                        allTaskList[taskIndex] = doneTask
                        taskFromMeLocalData.allTasks.done = allTaskList
                    }

                    TaskV2DaoHelper(taskDao).insertTaskData(
                        taskFromMeLocalData
                    )
                }
            }*/
        }
    }

}


