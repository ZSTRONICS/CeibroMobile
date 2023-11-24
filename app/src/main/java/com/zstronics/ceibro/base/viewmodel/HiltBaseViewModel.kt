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
import androidx.lifecycle.*
import androidx.work.*
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.clickevents.SingleClickEvent
import com.zstronics.ceibro.base.interfaces.IBase
import com.zstronics.ceibro.base.interfaces.OnClickHandler
import com.zstronics.ceibro.base.navgraph.host.NavHostPresenterActivity
import com.zstronics.ceibro.base.state.UIState
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.base.CookiesManager
import com.zstronics.ceibro.data.database.dao.DraftNewTaskV2Dao
import com.zstronics.ceibro.data.database.dao.ProjectsV2Dao
import com.zstronics.ceibro.data.database.dao.TaskV2Dao
import com.zstronics.ceibro.data.database.models.projects.CeibroProjectV2
import com.zstronics.ceibro.data.database.models.tasks.AssignedToState
import com.zstronics.ceibro.data.database.models.tasks.CeibroTaskV2
import com.zstronics.ceibro.data.database.models.tasks.Events
import com.zstronics.ceibro.data.database.models.tasks.TaskFiles
import com.zstronics.ceibro.data.remote.TaskRemoteDataSource
import com.zstronics.ceibro.data.repos.dashboard.IDashboardRepository
import com.zstronics.ceibro.data.repos.dashboard.attachment.AttachmentModules
import com.zstronics.ceibro.data.repos.dashboard.attachment.AttachmentUploadRequest
import com.zstronics.ceibro.data.repos.task.ITaskRepository
import com.zstronics.ceibro.data.repos.task.models.v2.EventV2Response
import com.zstronics.ceibro.data.repos.task.models.v2.ForwardedToMeNewTaskV2Response
import com.zstronics.ceibro.data.repos.task.models.v2.HideTaskResponse
import com.zstronics.ceibro.data.repos.task.models.v2.SocketReSyncUpdateV2Request
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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.cancel
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
                    if (!response.data.allEvents.isNullOrEmpty()) {
                        taskDaoInternal.insertMultipleEvents(response.data.allEvents)
                    }

                    Handler(Looper.getMainLooper()).postDelayed({
                        GlobalScope.launch {
                            val newTasks =
                                taskDaoInternal.getToMeTasks(TaskStatus.NEW.name.lowercase())
                                    .toMutableList()
                            val ongoingTasks =
                                taskDaoInternal.getToMeTasks(TaskStatus.ONGOING.name.lowercase())
                                    .toMutableList()
                            val doneTasks =
                                taskDaoInternal.getToMeTasks(TaskStatus.DONE.name.lowercase())
                                    .toMutableList()
                            val fromMeUnreadTasks =
                                taskDaoInternal.getFromMeTasks(TaskStatus.UNREAD.name.lowercase())
                                    .toMutableList()
                            val fromMeOngoingTasks =
                                taskDaoInternal.getFromMeTasks(TaskStatus.ONGOING.name.lowercase())
                                    .toMutableList()
                            val fromMeDoneTasks =
                                taskDaoInternal.getFromMeTasks(TaskStatus.DONE.name.lowercase())
                                    .toMutableList()
                            val hiddenCanceledTasks =
                                taskDaoInternal.getHiddenTasks(TaskStatus.CANCELED.name.lowercase())
                                    .toMutableList()
                            val hiddenOngoingTasks =
                                taskDaoInternal.getHiddenTasks(TaskStatus.ONGOING.name.lowercase())
                                    .toMutableList()
                            val hiddenDoneTasks =
                                taskDaoInternal.getHiddenTasks(TaskStatus.DONE.name.lowercase())
                                    .toMutableList()

                            CookiesManager.toMeNewTasks.postValue(newTasks)
                            CookiesManager.toMeOngoingTasks.postValue(ongoingTasks)
                            CookiesManager.toMeDoneTasks.postValue(doneTasks)

                            CookiesManager.fromMeUnreadTasks.postValue(fromMeUnreadTasks)
                            CookiesManager.fromMeOngoingTasks.postValue(fromMeOngoingTasks)
                            CookiesManager.fromMeDoneTasks.postValue(fromMeDoneTasks)

                            CookiesManager.hiddenCanceledTasks.postValue(hiddenCanceledTasks)
                            CookiesManager.hiddenOngoingTasks.postValue(hiddenOngoingTasks)
                            CookiesManager.hiddenDoneTasks.postValue(hiddenDoneTasks)

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


    private suspend fun updateAllTasksLists(taskDao: TaskV2Dao): Boolean {
        GlobalScope.launch {
            val toMeNewTask = taskDao.getToMeTasks(TaskStatus.NEW.name.lowercase()).toMutableList()
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

    private suspend fun updateAllTasksListForComment(
        taskDao: TaskV2Dao,
        eventData: EventV2Response.Data
    ): Boolean {
        GlobalScope.launch {
            if (!eventData.oldTaskData.hiddenState.equals(TaskStatus.CANCELED.name.lowercase(), true)) {

                if (eventData.newTaskData.isAssignedToMe) {
                    if (eventData.oldTaskData.userSubState.equals(TaskStatus.NEW.name, true)) {
                        val toMeNewTask =
                            taskDao.getToMeTasks(TaskStatus.NEW.name.lowercase()).toMutableList()
                        CookiesManager.toMeNewTasks.postValue(toMeNewTask)

                    } else if (eventData.oldTaskData.userSubState.equals(
                            TaskStatus.ONGOING.name,
                            true
                        )
                    ) {
                        val toMeOngoingTask =
                            taskDao.getToMeTasks(TaskStatus.ONGOING.name.lowercase())
                                .toMutableList()
                        CookiesManager.toMeOngoingTasks.postValue(toMeOngoingTask)

                    } else {
                        val toMeDoneTask =
                            taskDao.getToMeTasks(TaskStatus.DONE.name.lowercase()).toMutableList()
                        CookiesManager.toMeDoneTasks.postValue(toMeDoneTask)
                    }
                }

                if (eventData.newTaskData.isCreator) {
                    if (eventData.oldTaskData.creatorState.equals(TaskStatus.UNREAD.name, true)) {
                        val fromMeUnreadTask =
                            taskDao.getFromMeTasks(TaskStatus.UNREAD.name.lowercase())
                                .toMutableList()
                        CookiesManager.fromMeUnreadTasks.postValue(fromMeUnreadTask)

                    } else if (eventData.oldTaskData.creatorState.equals(
                            TaskStatus.ONGOING.name,
                            true
                        )
                    ) {
                        val fromMeOngoingTask =
                            taskDao.getFromMeTasks(TaskStatus.ONGOING.name.lowercase())
                                .toMutableList()
                        CookiesManager.fromMeOngoingTasks.postValue(fromMeOngoingTask)

                    } else {
                        val fromMeDoneTask =
                            taskDao.getFromMeTasks(TaskStatus.DONE.name.lowercase()).toMutableList()
                        CookiesManager.fromMeDoneTasks.postValue(fromMeDoneTask)
                    }
                }

                if (!eventData.oldTaskData.hiddenState.equals("NA", true)) {
                    if (eventData.oldTaskData.hiddenState.equals(TaskStatus.ONGOING.name, true)) {
                        val hiddenOngoingTask =
                            taskDao.getHiddenTasks(TaskStatus.ONGOING.name.lowercase()).toMutableList()
                        CookiesManager.hiddenOngoingTasks.postValue(hiddenOngoingTask)

                    } else {
                        val hiddenDoneTask =
                            taskDao.getHiddenTasks(TaskStatus.DONE.name.lowercase()).toMutableList()
                        CookiesManager.hiddenDoneTasks.postValue(hiddenDoneTask)
                    }
                }
            } else {
                val hiddenCanceledTask =
                    taskDao.getHiddenTasks(TaskStatus.CANCELED.name.lowercase()).toMutableList()
                CookiesManager.hiddenCanceledTasks.postValue(hiddenCanceledTask)
            }

        }.join()
        return true
    }


    private suspend fun updateAllTasksListForTaskSeen(
        taskDao: TaskV2Dao,
        taskSeen: TaskSeenResponse.TaskSeen
    ): Boolean {
        GlobalScope.launch {
            if (!taskSeen.oldTaskData.hiddenState.equals(TaskStatus.CANCELED.name.lowercase(), true)) {
                if (taskSeen.isAssignedToMe) {
                    if (taskSeen.stateChanged) {
                        val toMeNewTask =
                            taskDao.getToMeTasks(TaskStatus.NEW.name.lowercase()).toMutableList()
                        val toMeOngoingTask =
                            taskDao.getToMeTasks(TaskStatus.ONGOING.name.lowercase())
                                .toMutableList()

                        CookiesManager.toMeNewTasks.postValue(toMeNewTask)
                        CookiesManager.toMeOngoingTasks.postValue(toMeOngoingTask)
                    } else {
                        if (taskSeen.oldTaskData.userSubState.equals(TaskStatus.NEW.name, true)) {
                            val toMeNewTask =
                                taskDao.getToMeTasks(TaskStatus.NEW.name.lowercase())
                                    .toMutableList()
                            CookiesManager.toMeNewTasks.postValue(toMeNewTask)

                        } else if (taskSeen.oldTaskData.userSubState.equals(
                                TaskStatus.ONGOING.name,
                                true
                            )
                        ) {
                            val toMeNewTask =
                            taskDao.getToMeTasks(TaskStatus.NEW.name.lowercase())
                                .toMutableList()
                            CookiesManager.toMeNewTasks.postValue(toMeNewTask)

                            val toMeOngoingTask =
                                taskDao.getToMeTasks(TaskStatus.ONGOING.name.lowercase())
                                    .toMutableList()
                            CookiesManager.toMeOngoingTasks.postValue(toMeOngoingTask)
                        } else {
                            val toMeOngoingTask =
                                taskDao.getToMeTasks(TaskStatus.ONGOING.name.lowercase())
                                    .toMutableList()
                            CookiesManager.toMeOngoingTasks.postValue(toMeOngoingTask)

                            val toMeDoneTask =
                                taskDao.getToMeTasks(TaskStatus.DONE.name.lowercase())
                                    .toMutableList()
                            CookiesManager.toMeDoneTasks.postValue(toMeDoneTask)
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
                        CookiesManager.fromMeUnreadTasks.postValue(fromMeUnreadTask)
                        CookiesManager.fromMeOngoingTasks.postValue(fromMeOngoingTask)
                    } else {
                        if (taskSeen.oldTaskData.creatorState.equals(
                                TaskStatus.UNREAD.name,
                                true
                            )
                        ) {
                            val fromMeUnreadTask =
                                taskDao.getFromMeTasks(TaskStatus.UNREAD.name.lowercase())
                                    .toMutableList()
                            CookiesManager.fromMeUnreadTasks.postValue(fromMeUnreadTask)

                        } else if (taskSeen.oldTaskData.creatorState.equals(
                                TaskStatus.ONGOING.name,
                                true
                            )
                        ) {
                            val fromMeUnreadTask =
                                taskDao.getFromMeTasks(TaskStatus.UNREAD.name.lowercase())
                                    .toMutableList()
                            CookiesManager.fromMeUnreadTasks.postValue(fromMeUnreadTask)

                            val fromMeOngoingTask =
                                taskDao.getFromMeTasks(TaskStatus.ONGOING.name.lowercase())
                                    .toMutableList()
                            CookiesManager.fromMeOngoingTasks.postValue(fromMeOngoingTask)

                        } else {
                            val fromMeOngoingTask =
                                taskDao.getFromMeTasks(TaskStatus.ONGOING.name.lowercase())
                                    .toMutableList()
                            CookiesManager.fromMeOngoingTasks.postValue(fromMeOngoingTask)

                            val fromMeDoneTask =
                                taskDao.getFromMeTasks(TaskStatus.DONE.name.lowercase())
                                    .toMutableList()
                            CookiesManager.fromMeDoneTasks.postValue(fromMeDoneTask)
                        }
                    }

                }

                if (!taskSeen.oldTaskData.hiddenState.equals("NA", true)) {
                    if (taskSeen.oldTaskData.hiddenState.equals(TaskStatus.ONGOING.name, true)) {
                        val hiddenOngoingTask =
                            taskDao.getHiddenTasks(TaskStatus.ONGOING.name.lowercase()).toMutableList()
                        CookiesManager.hiddenOngoingTasks.postValue(hiddenOngoingTask)

                    } else {
                        val hiddenDoneTask =
                            taskDao.getHiddenTasks(TaskStatus.DONE.name.lowercase()).toMutableList()
                        CookiesManager.hiddenDoneTasks.postValue(hiddenDoneTask)
                    }
                }
            } else {
                val hiddenCanceledTask =
                    taskDao.getHiddenTasks(TaskStatus.CANCELED.name.lowercase()).toMutableList()
                CookiesManager.hiddenCanceledTasks.postValue(hiddenCanceledTask)
            }

        }.join()
        return true
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
        task: CeibroTaskV2?, taskDao: TaskV2Dao, userId: String?, sessionManager: SessionManager
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

    suspend fun updateForwardedToMeNewTaskInLocal(
        completeData: ForwardedToMeNewTaskV2Response?,
        taskDao: TaskV2Dao,
        userId: String?,
        sessionManager: SessionManager
    ) {
        val sharedViewModel = NavHostPresenterActivity.activityInstance?.let {
            ViewModelProvider(it).get(SharedViewModel::class.java)
        }
        completeData?.let { newData ->
            GlobalScope.launch {
                sessionManager.saveUpdatedAtTimeStamp(newData.task.updatedAt)
                taskDao.insertTaskData(newData.task)
                taskDao.insertMultipleEvents(newData.taskEvents)

                if (newData.task.isAssignedToMe) {
                    sharedViewModel?.isToMeUnread?.value = true
                    sessionManager.saveToMeUnread(true)
                }
                val toMeNewTask =
                    taskDao.getToMeTasks(TaskStatus.NEW.name.lowercase()).toMutableList()
                CookiesManager.toMeNewTasks.postValue(toMeNewTask)

//                updateAllTasksLists(taskDao)
                EventBus.getDefault().post(LocalEvents.RefreshTasksData())

            }.join()
        }
    }


    suspend fun updateForwardTaskInLocal(
        eventData: EventV2Response.Data?,
        taskDao: TaskV2Dao,
        userId: String?,
        sessionManager: SessionManager
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
                handler.postDelayed(Runnable {
                    EventBus.getDefault().post(LocalEvents.TaskEvent(taskEvent))
                    EventBus.getDefault().post(LocalEvents.RefreshTasksData())
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
        sessionManager: SessionManager
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

                    if (task != null) {
                        task.seenBy = taskSeen.seenBy
                        task.updatedAt = taskSeen.taskUpdatedAt

                        if (taskSeen.creatorStateChanged || taskSeen.stateChanged) {
                            task.creatorState = taskSeen.newTaskData.creatorState

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

                            task.fromMeState = taskSeen.newTaskData.fromMeState
                            task.toMeState = taskSeen.newTaskData.toMeState
                            task.hiddenState = taskSeen.newTaskData.hiddenState

                            taskDao.updateTask(task)
                        } else {

                            task.fromMeState = taskSeen.newTaskData.fromMeState
                            task.toMeState = taskSeen.newTaskData.toMeState
                            task.hiddenState = taskSeen.newTaskData.hiddenState

                            taskDao.updateTask(task)
                        }
                    }
                    updatedTask = task
//                    println("Heartbeat SocketEvent TASK_SEEN DB update started3 ${System.currentTimeMillis()}")
                    updateAllTasksListForTaskSeen(taskDao, taskSeen)
//                    println("Heartbeat SocketEvent TASK_SEEN DB update ended3 ${System.currentTimeMillis()}")

                }.join()
                EventBus.getDefault()
                    .post(LocalEvents.TaskSeenEvent(updatedTask))
                EventBus.getDefault().post(LocalEvents.RefreshTasksData())

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
        userId: String?,
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
//                println("Heartbeat SocketEvent NEW_TASK_COMMENT started ${System.currentTimeMillis()}")
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

//                    println("Heartbeat SocketEvent NEW_TASK_COMMENT DB operation started ${System.currentTimeMillis()}")
                    updateAllTasksListForComment(taskDao, eventData)
//                    println("Heartbeat SocketEvent NEW_TASK_COMMENT DB operation ended ${System.currentTimeMillis()}")

                }.join()
                val handler = Handler(Looper.getMainLooper())
                handler.postDelayed(Runnable {
                    EventBus.getDefault().post(LocalEvents.TaskEvent(taskEvent))
                    EventBus.getDefault().post(LocalEvents.RefreshTasksData())
                }, 50)

                TaskEventsList.removeEvent(
                    SocketHandler.TaskEvent.NEW_TASK_COMMENT.name,
                    eventData.id
                )
//                println("Heartbeat SocketEvent NEW_TASK_COMMENT ended ${System.currentTimeMillis()}")
            }
        }
    }

    suspend fun updateTaskUnCanceledInLocal(
        eventData: EventV2Response.Data?, taskDao: TaskV2Dao, sessionManager: SessionManager
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
                        eventNumber = eventData.eventNumber
                    )
                    sessionManager.saveUpdatedAtTimeStamp(eventData.taskUpdatedAt)
                    val task = taskDao.getTaskByID(eventData.taskId)

                    if (task != null) {
                        task.seenBy = eventData.taskData.seenBy
                        task.hiddenBy = eventData.taskData.hiddenBy
                        task.updatedAt = eventData.taskUpdatedAt
                        task.creatorState = eventData.newTaskData.creatorState
                        val assignToList = task.assignedToState
                        assignToList.map {
                            it.state = TaskStatus.NEW.name
                        }
                        task.assignedToState = assignToList
                        task.toMeState = eventData.newTaskData.toMeState
                        task.fromMeState = eventData.newTaskData.fromMeState
                        task.hiddenState = eventData.newTaskData.hiddenState
                        task.isCanceled = false
                        task.eventsCount = task.eventsCount + 1

                        taskDao.updateTask(task)
                    }
                    taskDao.insertEventData(taskEvent)

                    if (eventData.newTaskData.isAssignedToMe) {
                        sharedViewModel?.isToMeUnread?.value = true
                        sessionManager.saveToMeUnread(true)
                    }
                    if (eventData.newTaskData.isCreator) {
                        sharedViewModel?.isFromMeUnread?.value = true
                        sessionManager.saveFromMeUnread(true)
                    }

                    updateAllTasksLists(taskDao)

                    EventBus.getDefault().post(LocalEvents.TaskEvent(taskEvent))
                }.join()
                EventBus.getDefault().post(LocalEvents.RefreshTasksData())

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
        sessionManager: SessionManager
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
                        eventNumber = eventData.eventNumber
                    )
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
                        task.isCanceled = true
                        task.eventsCount = task.eventsCount + 1

                        taskDao.updateTask(task)
                    }
                    taskDao.insertEventData(taskEvent)

                    if (task?.creator?.id != userId) {
                        sharedViewModel?.isHiddenUnread?.postValue(true)
                        sessionManager.saveHiddenUnread(true)
                    }

                    updateAllTasksLists(taskDao)

                    EventBus.getDefault().post(LocalEvents.TaskEvent(taskEvent))
                }.join()

                EventBus.getDefault().post(LocalEvents.RefreshTasksData())
                TaskEventsList.removeEvent(
                    SocketHandler.TaskEvent.CANCELED_TASK.name,
                    eventData.taskId
                )
            }
        }
    }

    suspend fun updateTaskDoneInLocal(
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
                handler.postDelayed(Runnable {
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

    suspend fun updateTaskJoinedInLocal(
        eventData: EventV2Response.Data?, taskDao: TaskV2Dao, sessionManager: SessionManager
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
                    task.toMeState = eventData.newTaskData.toMeState
                    task.fromMeState = eventData.newTaskData.fromMeState
                    task.hiddenState = eventData.newTaskData.hiddenState
                    task.eventsCount = task.eventsCount + 1

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
                        state = TaskStatus.NEW.name,
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

            EventBus.getDefault().post(LocalEvents.TaskEvent(taskEvent))
            EventBus.getDefault().post(LocalEvents.RefreshTasksData())
        }
    }


    suspend fun updateTaskHideInLocal(
        hideData: HideTaskResponse?, taskDao: TaskV2Dao, sessionManager: SessionManager
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
                        isHiddenByMe = true,
                        hiddenBy = hideData.hiddenBy,
                        updatedAt = hideData.taskUpdatedAt,
                        toMeState = hideData.toMeState,
                        fromMeState = hideData.fromMeState,
                        hiddenState = hideData.hiddenState
                    )

                    val toMeOngoingTask =
                        taskDao.getToMeTasks(TaskStatus.ONGOING.name.lowercase()).toMutableList()
                    val toMeDoneTask =
                        taskDao.getToMeTasks(TaskStatus.DONE.name.lowercase()).toMutableList()

                    val hiddenOngoingTask =
                        taskDao.getHiddenTasks(TaskStatus.ONGOING.name.lowercase()).toMutableList()
                    val hiddenDoneTask =
                        taskDao.getHiddenTasks(TaskStatus.DONE.name.lowercase()).toMutableList()

                    CookiesManager.toMeOngoingTasks.postValue(toMeOngoingTask)
                    CookiesManager.toMeDoneTasks.postValue(toMeDoneTask)

                    CookiesManager.hiddenOngoingTasks.postValue(hiddenOngoingTask)
                    CookiesManager.hiddenDoneTasks.postValue(hiddenDoneTask)

//                    updateAllTasksLists(taskDao)
                }.join()
                EventBus.getDefault().post(LocalEvents.RefreshTasksData())

                TaskEventsList.removeEvent(
                    SocketHandler.TaskEvent.TASK_HIDDEN.name, hideData.taskId
                )
            }
        }
    }

    suspend fun updateTaskUnHideInLocal(
        hideData: HideTaskResponse?, taskDao: TaskV2Dao, sessionManager: SessionManager
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
                        isHiddenByMe = false,
                        hiddenBy = hideData.hiddenBy,
                        updatedAt = hideData.taskUpdatedAt,
                        toMeState = hideData.toMeState,
                        fromMeState = hideData.fromMeState,
                        hiddenState = hideData.hiddenState
                    )

                    val toMeOngoingTask =
                        taskDao.getToMeTasks(TaskStatus.ONGOING.name.lowercase()).toMutableList()
                    val toMeDoneTask =
                        taskDao.getToMeTasks(TaskStatus.DONE.name.lowercase()).toMutableList()

                    val hiddenOngoingTask =
                        taskDao.getHiddenTasks(TaskStatus.ONGOING.name.lowercase()).toMutableList()
                    val hiddenDoneTask =
                        taskDao.getHiddenTasks(TaskStatus.DONE.name.lowercase()).toMutableList()

                    CookiesManager.toMeOngoingTasks.postValue(toMeOngoingTask)
                    CookiesManager.toMeDoneTasks.postValue(toMeDoneTask)

                    CookiesManager.hiddenOngoingTasks.postValue(hiddenOngoingTask)
                    CookiesManager.hiddenDoneTasks.postValue(hiddenDoneTask)

//                    updateAllTasksLists(taskDao)

                }.join()
                EventBus.getDefault().post(LocalEvents.RefreshTasksData())

                TaskEventsList.removeEvent(
                    SocketHandler.TaskEvent.TASK_SHOWN.name, hideData.taskId
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


