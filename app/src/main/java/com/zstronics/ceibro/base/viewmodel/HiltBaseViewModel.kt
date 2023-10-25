package com.zstronics.ceibro.base.viewmodel


import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.CallSuper
import androidx.core.app.NotificationCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*
import androidx.work.*
import com.google.gson.Gson
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.clickevents.SingleClickEvent
import com.zstronics.ceibro.base.interfaces.IBase
import com.zstronics.ceibro.base.interfaces.OnClickHandler
import com.zstronics.ceibro.base.navgraph.host.NavHostPresenterActivity
import com.zstronics.ceibro.base.state.UIState
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.database.dao.DraftNewTaskV2Dao
import com.zstronics.ceibro.data.database.dao.TaskV2Dao
import com.zstronics.ceibro.data.database.dao.TaskV2DaoHelper
import com.zstronics.ceibro.data.database.models.tasks.AssignedToState
import com.zstronics.ceibro.data.database.models.tasks.CeibroTaskV2
import com.zstronics.ceibro.data.database.models.tasks.Events
import com.zstronics.ceibro.data.database.models.tasks.TaskFiles
import com.zstronics.ceibro.data.repos.dashboard.IDashboardRepository
import com.zstronics.ceibro.data.repos.dashboard.attachment.AttachmentModules
import com.zstronics.ceibro.data.repos.dashboard.attachment.AttachmentTags
import com.zstronics.ceibro.data.repos.dashboard.attachment.AttachmentUploadRequest
import com.zstronics.ceibro.data.repos.dashboard.attachment.v2.AttachmentUploadV2Request
import com.zstronics.ceibro.data.repos.task.ITaskRepository
import com.zstronics.ceibro.data.repos.task.TaskRootStateTags
import com.zstronics.ceibro.data.repos.task.models.TaskV2Response
import com.zstronics.ceibro.data.repos.task.models.TasksV2DatabaseEntity
import com.zstronics.ceibro.data.repos.task.models.v2.EventV2Response
import com.zstronics.ceibro.data.repos.task.models.v2.HideTaskResponse
import com.zstronics.ceibro.data.repos.task.models.v2.LocalFilesToStore
import com.zstronics.ceibro.data.repos.task.models.v2.NewTaskV2Entity
import com.zstronics.ceibro.data.repos.task.models.v2.TaskSeenResponse
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.ui.attachment.SubtaskAttachment
import com.zstronics.ceibro.ui.dashboard.SharedViewModel
import com.zstronics.ceibro.ui.dashboard.TaskEventsList
import com.zstronics.ceibro.ui.socket.LocalEvents
import com.zstronics.ceibro.ui.socket.SocketHandler
import com.zstronics.ceibro.ui.tasks.task.TaskStatus
import com.zstronics.ceibro.utils.FileUtils
import ee.zstronics.ceibro.camera.AttachmentTypes
import ee.zstronics.ceibro.camera.PickedImages
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


    fun updateCreatedTaskInLocal(
        task: CeibroTaskV2?, taskDao: TaskV2Dao, userId: String?, sessionManager: SessionManager
    ) {
        val sharedViewModel = NavHostPresenterActivity.activityInstance?.let {
            ViewModelProvider(it).get(SharedViewModel::class.java)
        }

        task?.let {
            launch {
                sessionManager.saveUpdatedAtTimeStamp(task.updatedAt)
                if (task.isCreator) {
                    val taskLocalData =
                        TaskV2DaoHelper(taskDao).getTasks(TaskRootStateTags.FromMe.tagValue)
                    val unreadList = mutableListOf(task)
                    taskLocalData.allTasks.unread.let { oldList ->
                        val oldListMutableList = oldList.toMutableList()
                        val index = oldList.indexOfFirst { it.id == task.id }
                        if (index >= 0) {
                            oldListMutableList.removeAt(index)
                        }
                        unreadList.addAll(oldListMutableList)
                    }

                    taskLocalData.allTasks.unread = unreadList
                    TaskV2DaoHelper(taskDao).insertTaskDataUpdated(taskLocalData, "unread")
                    //   EventBus.getDefault().post(LocalEvents.RefreshTasksEvent())
                }

                if (task.isAssignedToMe) {
                    val taskLocalData =
                        TaskV2DaoHelper(taskDao).getTasks(TaskRootStateTags.ToMe.tagValue)
                    val newList = mutableListOf(task)
                    taskLocalData.allTasks.new.let { oldList ->
                        val oldListMutableList = oldList.toMutableList()
                        val index = oldList.indexOfFirst { it.id == task.id }
                        if (index >= 0) {
                            oldListMutableList.removeAt(index)
                        }
                        newList.addAll(oldListMutableList)
                    }
                    taskLocalData.allTasks.new = newList
                    TaskV2DaoHelper(taskDao).insertTaskDataUpdated(taskLocalData, "new")

                    sharedViewModel?.isToMeUnread?.value = true
                    sessionManager.saveToMeUnread(true)

                }
                EventBus.getDefault().post(LocalEvents.RefreshTasksEvent())

            }
        }

    }


    suspend fun updateForwardTaskInLocal(
        task: CeibroTaskV2?, taskDao: TaskV2Dao, userId: String?, sessionManager: SessionManager
    ) {
        val sharedViewModel = NavHostPresenterActivity.activityInstance?.let {
            ViewModelProvider(it).get(SharedViewModel::class.java)
        }
        launch {
            if (task != null) {
                val taskDaoTaskV2DaoHelper = TaskV2DaoHelper(taskDao);
                val exist = TaskEventsList.isExists(
                    SocketHandler.TaskEvent.TASK_FORWARDED.name, task.id, true
                )
                if (!exist) {
                    sessionManager.saveUpdatedAtTimeStamp(task.updatedAt)
                    val isHidden = task.isHiddenByMe
                    val taskFromMe = task.isCreator
                    val taskToMe = task.isAssignedToMe
                    val myState = task.assignedToState.find { it.userId == userId }?.state

                    //first check hidden and move task from hidden
                    val taskHiddenLocalData =
                        taskDaoTaskV2DaoHelper.getTasks(TaskRootStateTags.Hidden.tagValue)
                    val taskToMeLocalData =
                        taskDaoTaskV2DaoHelper.getTasks(TaskRootStateTags.ToMe.tagValue)
                    val taskFromMeLocalData =
                        taskDaoTaskV2DaoHelper.getTasks(TaskRootStateTags.FromMe.tagValue)

                    if (!taskDaoTaskV2DaoHelper.isTaskListEmpty(
                            TaskRootStateTags.Hidden.tagValue, taskHiddenLocalData
                        )
                    ) {
                        val ongoingTaskIndex =
                            taskHiddenLocalData.allTasks.ongoing.indexOfFirst { it.id == task.id }
                        val doneTaskIndex = taskHiddenLocalData.allTasks.done.indexOfFirst { it.id == task.id }

                        if (ongoingTaskIndex != -1) {
                            val allOngoingTaskList =
                                taskHiddenLocalData.allTasks.ongoing.toMutableList()

                            allOngoingTaskList.removeAt(ongoingTaskIndex)
                            taskHiddenLocalData.allTasks.ongoing = allOngoingTaskList

                            val ongoingTaskToMe =
                                taskToMeLocalData.allTasks.ongoing.find { it.id == task.id }
                            val allOngoingToMeTaskList =
                                taskToMeLocalData.allTasks.ongoing.toMutableList()
                            if (ongoingTaskToMe != null) {
                                val index = allOngoingToMeTaskList.indexOf(ongoingTaskToMe)
                                allOngoingToMeTaskList[index] = task
                                taskToMeLocalData.allTasks.ongoing = allOngoingToMeTaskList
                            } else {
                                allOngoingToMeTaskList.add(0, task)
                                taskToMeLocalData.allTasks.ongoing = allOngoingToMeTaskList
                            }
                            taskDaoTaskV2DaoHelper.insertTaskDataUpdated(taskHiddenLocalData, "ongoing")
                            taskDaoTaskV2DaoHelper.insertTaskDataUpdated(taskToMeLocalData, "ongoing")
                        }
                        else if (doneTaskIndex != -1) {
                            val allDoneTaskList = taskHiddenLocalData.allTasks.done.toMutableList()

                            allDoneTaskList.removeAt(doneTaskIndex)
                            taskHiddenLocalData.allTasks.done = allDoneTaskList

                            val doneTaskToMe =
                                taskToMeLocalData.allTasks.done.find { it.id == task.id }
                            val allDoneToMeTaskList =
                                taskToMeLocalData.allTasks.done.toMutableList()
                            if (doneTaskToMe != null) {
                                val index = allDoneToMeTaskList.indexOf(doneTaskToMe)
                                allDoneToMeTaskList[index] = task
                                taskToMeLocalData.allTasks.done = allDoneToMeTaskList
                            } else {
                                allDoneToMeTaskList.add(0, task)
                                taskToMeLocalData.allTasks.done = allDoneToMeTaskList
                            }
                            taskDaoTaskV2DaoHelper.insertTaskDataUpdated(taskHiddenLocalData, "done")
                            taskDaoTaskV2DaoHelper.insertTaskDataUpdated(taskToMeLocalData, "done")
                        }
                    }

                    if (task.creatorState.equals(TaskStatus.CANCELED.name, true)) {
                        // it means task must be in hidden rootState and child state will be canceled. search an update the task only

                        if (!TaskV2DaoHelper(taskDao).isTaskListEmpty(
                                TaskRootStateTags.Hidden.tagValue, taskHiddenLocalData
                            )
                        ) {
                            val canceledTask =
                                taskHiddenLocalData.allTasks.canceled.find { it.id == task.id }
                            if (canceledTask != null) {
                                val allCancelTaskList =
                                    taskHiddenLocalData.allTasks.canceled.toMutableList()
                                val taskIndex = allCancelTaskList.indexOf(canceledTask)

                                allCancelTaskList[taskIndex] = task
                                taskHiddenLocalData.allTasks.canceled = allCancelTaskList

                                TaskV2DaoHelper(taskDao).insertTaskDataUpdated(taskHiddenLocalData, "canceled")
                                sharedViewModel?.isHiddenUnread?.value = true
                                sessionManager.saveHiddenUnread(true)
                            }
                        }
                    }

                    if (taskFromMe) {

                        if (!taskDaoTaskV2DaoHelper.isTaskListEmpty(
                                TaskRootStateTags.FromMe.tagValue, taskFromMeLocalData
                            )
                        ) {
                            val unreadTaskIndex =
                                taskFromMeLocalData.allTasks.unread.indexOfFirst { it.id == task.id }
                            val ongoingTaskIndex =
                                taskFromMeLocalData.allTasks.ongoing.indexOfFirst { it.id == task.id }
                            val doneTaskIndex =
                                taskFromMeLocalData.allTasks.done.indexOfFirst { it.id == task.id }

                            if (unreadTaskIndex >= 0) {
                                val unreadList = taskFromMeLocalData.allTasks.unread.toMutableList()
                                if (task.creatorState.equals(TaskStatus.ONGOING.name, true)) {
                                    /// remove from unread
                                    unreadList.removeAt(unreadTaskIndex)
                                    // push into ongoing
                                    val ongoingList =
                                        taskFromMeLocalData.allTasks.ongoing.toMutableList()
                                    val ongoingTaskFromMe =
                                        taskFromMeLocalData.allTasks.ongoing.find { it.id == task.id }
                                    if (ongoingTaskFromMe != null) {
                                        val index = ongoingList.indexOf(ongoingTaskFromMe)
                                        ongoingList[index] = task
                                        taskFromMeLocalData.allTasks.ongoing = ongoingList
                                    } else {
                                        ongoingList.add(0, task)
                                        taskFromMeLocalData.allTasks.ongoing = ongoingList
                                    }
                                    taskFromMeLocalData.allTasks.unread = unreadList
                                } else {
                                    unreadList[unreadTaskIndex] = task
                                    taskFromMeLocalData.allTasks.unread = unreadList
                                }
                                taskDaoTaskV2DaoHelper.insertTaskDataUpdated(taskFromMeLocalData, "unread")
                                taskDaoTaskV2DaoHelper.insertTaskDataUpdated(taskFromMeLocalData, "ongoing")
                                sharedViewModel?.isFromMeUnread?.value = true
                                sessionManager.saveFromMeUnread(true)

                            } else if (ongoingTaskIndex >= 0) {
                                val ongoingList =
                                    taskFromMeLocalData.allTasks.ongoing.toMutableList()
                                if (task.creatorState.equals(TaskStatus.DONE.name, true)) {
                                    /// remove from unread
                                    ongoingList.removeAt(ongoingTaskIndex)
                                    // push into ongoing
                                    val doneList = taskFromMeLocalData.allTasks.done.toMutableList()
                                    val doneTaskFromMe =
                                        taskFromMeLocalData.allTasks.done.find { it.id == task.id }
                                    if (doneTaskFromMe != null) {
                                        val index = doneList.indexOf(doneTaskFromMe)
                                        doneList[index] = task
                                        taskFromMeLocalData.allTasks.done = doneList
                                    } else {
                                        doneList.add(0, task)
                                        taskFromMeLocalData.allTasks.done = doneList
                                    }
                                    taskFromMeLocalData.allTasks.ongoing = ongoingList
                                } else {
                                    ongoingList[ongoingTaskIndex] = task
                                    taskFromMeLocalData.allTasks.ongoing = ongoingList
                                }
                                sharedViewModel?.isFromMeUnread?.value = true
                                sessionManager.saveFromMeUnread(true)
                                taskDaoTaskV2DaoHelper.insertTaskDataUpdated(taskFromMeLocalData, "ongoing")
                                taskDaoTaskV2DaoHelper.insertTaskDataUpdated(taskFromMeLocalData, "done")
                            } else if (doneTaskIndex >= 0) {
                                val doneList = taskFromMeLocalData.allTasks.done.toMutableList()
                                doneList[doneTaskIndex] = task
                                taskFromMeLocalData.allTasks.done = doneList
                                taskDaoTaskV2DaoHelper.insertTaskDataUpdated(taskFromMeLocalData, "done")
                                sharedViewModel?.isFromMeUnread?.value = true
                                sessionManager.saveFromMeUnread(true)
                            }
                        }
                    }

                    if (taskToMe) {

                        if (!taskDaoTaskV2DaoHelper.isTaskListEmpty(
                                TaskRootStateTags.ToMe.tagValue, taskToMeLocalData
                            )
                        ) {
                            val newTaskIndex =
                                taskToMeLocalData.allTasks.new.indexOfFirst { it.id == task.id }
                            val ongoingTaskIndex =
                                taskToMeLocalData.allTasks.ongoing.indexOfFirst { it.id == task.id }
                            val doneTaskIndex =
                                taskToMeLocalData.allTasks.done.indexOfFirst { it.id == task.id }

                            if (newTaskIndex >= 0) {
                                if (myState.equals(TaskStatus.ONGOING.name, true)) {
                                    val newTaskList = taskToMeLocalData.allTasks.new.toMutableList()
                                    newTaskList.removeAt(newTaskIndex)
                                    taskToMeLocalData.allTasks.new = newTaskList

                                    val ongoingTaskList =
                                        taskToMeLocalData.allTasks.ongoing.toMutableList()
                                    val ongoingTaskFromMe =
                                        taskToMeLocalData.allTasks.ongoing.find { it.id == task.id }
                                    if (ongoingTaskFromMe != null) {
                                        val index = ongoingTaskList.indexOf(ongoingTaskFromMe)
                                        ongoingTaskList[index] = task
                                        taskToMeLocalData.allTasks.ongoing = ongoingTaskList
                                    } else {
                                        ongoingTaskList.add(0, task)
                                        taskToMeLocalData.allTasks.ongoing = ongoingTaskList
                                    }
                                    taskDaoTaskV2DaoHelper.insertTaskDataUpdated(taskToMeLocalData, "new")
                                    taskDaoTaskV2DaoHelper.insertTaskDataUpdated(taskToMeLocalData, "ongoing")
                                }
                                else if (myState.equals(TaskStatus.DONE.name, true)) {
                                    val newTaskList = taskToMeLocalData.allTasks.new.toMutableList()
                                    newTaskList.removeAt(newTaskIndex)
                                    taskToMeLocalData.allTasks.new = newTaskList

                                    val doneTaskList =
                                        taskToMeLocalData.allTasks.done.toMutableList()
                                    val doneTaskFromMe =
                                        taskToMeLocalData.allTasks.done.find { it.id == task.id }
                                    if (doneTaskFromMe != null) {
                                        val index = doneTaskList.indexOf(doneTaskFromMe)
                                        doneTaskList[index] = task
                                        taskToMeLocalData.allTasks.done = doneTaskList
                                    } else {
                                        doneTaskList.add(0, task)
                                        taskToMeLocalData.allTasks.done = doneTaskList
                                    }
                                    taskDaoTaskV2DaoHelper.insertTaskDataUpdated(taskToMeLocalData, "new")
                                    taskDaoTaskV2DaoHelper.insertTaskDataUpdated(taskToMeLocalData, "done")
                                }
                                else {
                                    val allTaskList = taskToMeLocalData.allTasks.new.toMutableList()
                                    allTaskList[newTaskIndex] = task
                                    taskToMeLocalData.allTasks.new = allTaskList
                                    taskDaoTaskV2DaoHelper.insertTaskDataUpdated(taskToMeLocalData, "new")
                                }
                                sharedViewModel?.isToMeUnread?.value = true
                                sessionManager.saveToMeUnread(true)

                            }
                            else if (ongoingTaskIndex >= 0) {
                                if (myState.equals(TaskStatus.DONE.name, true)) {
                                    val ongoingTaskList =
                                        taskToMeLocalData.allTasks.ongoing.toMutableList()
                                    ongoingTaskList.removeAt(ongoingTaskIndex)
                                    taskToMeLocalData.allTasks.ongoing = ongoingTaskList

                                    val doneTaskList =
                                        taskToMeLocalData.allTasks.done.toMutableList()
                                    val doneTaskFromMe =
                                        taskToMeLocalData.allTasks.done.find { it.id == task.id }
                                    if (doneTaskFromMe != null) {
                                        val index = doneTaskList.indexOf(doneTaskFromMe)
                                        doneTaskList[index] = task
                                        taskToMeLocalData.allTasks.done = doneTaskList
                                    } else {
                                        doneTaskList.add(0, task)
                                        taskToMeLocalData.allTasks.done = doneTaskList
                                    }
                                    taskDaoTaskV2DaoHelper.insertTaskDataUpdated(taskToMeLocalData, "ongoing")
                                    taskDaoTaskV2DaoHelper.insertTaskDataUpdated(taskToMeLocalData, "done")
                                } else {
                                    val allTaskList =
                                        taskToMeLocalData.allTasks.ongoing.toMutableList()
                                    allTaskList[ongoingTaskIndex] = task
                                    taskToMeLocalData.allTasks.ongoing = allTaskList
                                    taskDaoTaskV2DaoHelper.insertTaskDataUpdated(taskToMeLocalData, "ongoing")
                                }
                                sharedViewModel?.isToMeUnread?.value = true
                                sessionManager.saveToMeUnread(true)

                            } else if (doneTaskIndex >= 0) {
                                val allTaskList = taskToMeLocalData.allTasks.done.toMutableList()
                                allTaskList[doneTaskIndex] = task
                                taskToMeLocalData.allTasks.done = allTaskList
                                taskDaoTaskV2DaoHelper.insertTaskDataUpdated(taskToMeLocalData, "done")
                                sharedViewModel?.isToMeUnread?.value = true
                                sessionManager.saveToMeUnread(true)

                            } else {
                                if (myState.equals(TaskStatus.NEW.name, true)) {
                                    val allTaskList = taskToMeLocalData.allTasks.new.toMutableList()
                                    allTaskList.add(0, task)
                                    taskToMeLocalData.allTasks.new = allTaskList
                                    taskDaoTaskV2DaoHelper.insertTaskDataUpdated(taskToMeLocalData, "new")
                                    sharedViewModel?.isToMeUnread?.value = true
                                    sessionManager.saveToMeUnread(true)

                                }
                            }
                        }
                    }


//                    if(isHidden) {
//                        taskDaoTaskV2DaoHelper.insertTaskData(
//                            taskHiddenLocalData
//                        )
//                    }
//                    if (taskToMe) {
//                        taskDaoTaskV2DaoHelper.insertTaskData(
//                            taskToMeLocalData
//                        )
//                    }
//                    if (taskFromMe) {
//                        taskDaoTaskV2DaoHelper.insertTaskData(
//                            taskFromMeLocalData
//                        )
//                    }
                    // send task data for ui update
                    EventBus.getDefault().post(LocalEvents.TaskForwardEvent(task))
                    EventBus.getDefault().post(LocalEvents.RefreshTasksEvent())

                    TaskEventsList.removeEvent(
                        SocketHandler.TaskEvent.TASK_FORWARDED.name,
                        task.id
                    )
                }
            }
        }
    }

    fun updateGenericTaskSeenInLocal(
        taskSeen: TaskSeenResponse.TaskSeen?,
        taskDao: TaskV2Dao,
        userId: String?,
        sessionManager: SessionManager
    ) {
        var updatedTask: CeibroTaskV2? = null
        if (taskSeen != null) {

            val exist = TaskEventsList.isExists(
                SocketHandler.TaskEvent.TASK_SEEN.name, taskSeen.taskId, true
            )
            if (!exist) {
                sessionManager.saveUpdatedAtTimeStamp(taskSeen.updatedAt)
                launch {

                    val taskHiddenLocalData =
                        TaskV2DaoHelper(taskDao).getTasks(TaskRootStateTags.Hidden.tagValue)
                    val taskFromMeLocalData =
                        TaskV2DaoHelper(taskDao).getTasks(TaskRootStateTags.FromMe.tagValue)
                    val taskToMeLocalData =
                        TaskV2DaoHelper(taskDao).getTasks(TaskRootStateTags.ToMe.tagValue)

                    if (taskSeen.oldTaskData.isHiddenByMe) {
                        // it means task must be in hidden rootState and child state will be canceled. search an update the task only
                        launch {
                            if (!TaskV2DaoHelper(taskDao).isTaskListEmpty(
                                    TaskRootStateTags.Hidden.tagValue, taskHiddenLocalData
                                )
                            ) {
                                val ongoingTask =
                                    taskHiddenLocalData.allTasks.ongoing.find { it.id == taskSeen.taskId }
                                val doneTask =
                                    taskHiddenLocalData.allTasks.done.find { it.id == taskSeen.taskId }

                                if (ongoingTask != null) {
                                    val allOngoingTaskList =
                                        taskHiddenLocalData.allTasks.ongoing.toMutableList()
                                    val taskIndex = allOngoingTaskList.indexOf(ongoingTask)

                                    ongoingTask.seenBy = taskSeen.seenBy
                                    ongoingTask.updatedAt = taskSeen.updatedAt

                                    allOngoingTaskList[taskIndex] = ongoingTask
                                    taskHiddenLocalData.allTasks.ongoing = allOngoingTaskList

                                    updatedTask = ongoingTask

                                } else if (doneTask != null) {
                                    val allDoneTaskList =
                                        taskHiddenLocalData.allTasks.done.toMutableList()
                                    val taskIndex = allDoneTaskList.indexOf(doneTask)

                                    doneTask.seenBy = taskSeen.seenBy
                                    doneTask.updatedAt = taskSeen.updatedAt

                                    allDoneTaskList[taskIndex] = doneTask
                                    taskHiddenLocalData.allTasks.done = allDoneTaskList

                                    updatedTask = doneTask
                                }
                            }
                        }
                    }

                    if (taskSeen.creatorState.equals(TaskStatus.CANCELED.name, true)) {
                        // it means task must be in hidden rootState and child state will be canceled. search an update the task only
                        launch {

                            if (!TaskV2DaoHelper(taskDao).isTaskListEmpty(
                                    TaskRootStateTags.Hidden.tagValue, taskHiddenLocalData
                                )
                            ) {
                                val canceledTask =
                                    taskHiddenLocalData.allTasks.canceled.find { it.id == taskSeen.taskId }
                                if (canceledTask != null) {
                                    val allCancelTaskList =
                                        taskHiddenLocalData.allTasks.canceled.toMutableList()
                                    val taskIndex = allCancelTaskList.indexOf(canceledTask)

                                    canceledTask.seenBy = taskSeen.seenBy
                                    canceledTask.updatedAt = taskSeen.updatedAt

                                    if (taskSeen.creatorStateChanged || taskSeen.stateChanged) {
                                        canceledTask.creatorState = taskSeen.creatorState

                                        val foundState =
                                            canceledTask.assignedToState.find { it.userId == taskSeen.state.userId }
                                        if (foundState != null) {
                                            val stateIndex =
                                                canceledTask.assignedToState.indexOf(foundState)
                                            canceledTask.assignedToState[stateIndex] =
                                                taskSeen.state
                                        }

                                        allCancelTaskList[taskIndex] = canceledTask
                                        taskHiddenLocalData.allTasks.canceled = allCancelTaskList

                                    } else {
                                        allCancelTaskList[taskIndex] = canceledTask
                                        taskHiddenLocalData.allTasks.canceled = allCancelTaskList
                                    }
                                    updatedTask = canceledTask

                                }
                            }
                        }
                    }

                    if (taskSeen.isCreator) {

                        if (!TaskV2DaoHelper(taskDao).isTaskListEmpty(
                                TaskRootStateTags.FromMe.tagValue, taskFromMeLocalData
                            )
                        ) {
                            val unreadTask =
                                taskFromMeLocalData.allTasks.unread.find { it.id == taskSeen.taskId }
                            val ongoingTask =
                                taskFromMeLocalData.allTasks.ongoing.find { it.id == taskSeen.taskId }
                            val doneTask =
                                taskFromMeLocalData.allTasks.done.find { it.id == taskSeen.taskId }
                            if (unreadTask != null) {
                                val allTaskList =
                                    taskFromMeLocalData.allTasks.unread.toMutableList()
                                val taskIndex = allTaskList.indexOf(unreadTask)

                                unreadTask.seenBy = taskSeen.seenBy
                                unreadTask.updatedAt = taskSeen.updatedAt

                                if (taskSeen.creatorStateChanged || taskSeen.stateChanged) {
                                    unreadTask.creatorState = taskSeen.creatorState

                                    val foundState =
                                        unreadTask.assignedToState.find { it.userId == taskSeen.state.userId }
                                    if (foundState != null) {
                                        val stateIndex =
                                            unreadTask.assignedToState.indexOf(foundState)
                                        unreadTask.assignedToState[stateIndex] = taskSeen.state
                                    }


                                    if (taskSeen.creatorState.equals(
                                            TaskStatus.ONGOING.name,
                                            true
                                        )
                                    ) {
                                        val allOngoingTaskList =
                                            taskFromMeLocalData.allTasks.ongoing.toMutableList()
                                        val newAllOngoingTaskList: MutableList<CeibroTaskV2> =
                                            mutableListOf()
                                        newAllOngoingTaskList.add(unreadTask)
                                        newAllOngoingTaskList.addAll(allOngoingTaskList)

                                        allTaskList.removeAt(taskIndex)
                                        taskFromMeLocalData.allTasks.unread = allTaskList
                                        taskFromMeLocalData.allTasks.ongoing = newAllOngoingTaskList

                                    } else if (taskSeen.creatorState.equals(
                                            TaskStatus.DONE.name, true
                                        )
                                    ) {
                                        val allDoneTaskList =
                                            taskFromMeLocalData.allTasks.done.toMutableList()
                                        val newAllDoneTaskList: MutableList<CeibroTaskV2> =
                                            mutableListOf()
                                        newAllDoneTaskList.add(unreadTask)
                                        newAllDoneTaskList.addAll(allDoneTaskList)

                                        allTaskList.removeAt(taskIndex)
                                        taskFromMeLocalData.allTasks.unread = allTaskList
                                        taskFromMeLocalData.allTasks.done = newAllDoneTaskList

                                    } else {
                                        allTaskList[taskIndex] = unreadTask
                                        taskFromMeLocalData.allTasks.unread = allTaskList
                                    }
                                } else {
                                    allTaskList[taskIndex] = unreadTask
                                    taskFromMeLocalData.allTasks.unread = allTaskList
                                }
                                updatedTask = unreadTask

                            } else if (ongoingTask != null) {
                                val allTaskList =
                                    taskFromMeLocalData.allTasks.ongoing.toMutableList()
                                val taskIndex = allTaskList.indexOf(ongoingTask)

                                ongoingTask.seenBy = taskSeen.seenBy
                                ongoingTask.updatedAt = taskSeen.updatedAt

                                if (taskSeen.creatorStateChanged || taskSeen.stateChanged) {
                                    ongoingTask.creatorState = taskSeen.creatorState

                                    val foundState =
                                        ongoingTask.assignedToState.find { it.userId == taskSeen.state.userId }
                                    if (foundState != null) {
                                        val stateIndex =
                                            ongoingTask.assignedToState.indexOf(foundState)
                                        ongoingTask.assignedToState[stateIndex] = taskSeen.state
                                    }


                                    if (taskSeen.creatorState.equals(
                                            TaskStatus.UNREAD.name,
                                            true
                                        )
                                    ) {
                                        val allUnreadTaskList =
                                            taskFromMeLocalData.allTasks.unread.toMutableList()
                                        val newAllUnreadTaskList: MutableList<CeibroTaskV2> =
                                            mutableListOf()
                                        newAllUnreadTaskList.add(ongoingTask)
                                        newAllUnreadTaskList.addAll(allUnreadTaskList)

                                        allTaskList.removeAt(taskIndex)
                                        taskFromMeLocalData.allTasks.ongoing = allTaskList
                                        taskFromMeLocalData.allTasks.unread = newAllUnreadTaskList

                                    } else if (taskSeen.creatorState.equals(
                                            TaskStatus.DONE.name, true
                                        )
                                    ) {
                                        val allDoneTaskList =
                                            taskFromMeLocalData.allTasks.done.toMutableList()
                                        val newAllDoneTaskList: MutableList<CeibroTaskV2> =
                                            mutableListOf()
                                        newAllDoneTaskList.add(ongoingTask)
                                        newAllDoneTaskList.addAll(allDoneTaskList)

                                        allTaskList.removeAt(taskIndex)
                                        taskFromMeLocalData.allTasks.ongoing = allTaskList
                                        taskFromMeLocalData.allTasks.done = newAllDoneTaskList

                                    } else {
                                        allTaskList[taskIndex] = ongoingTask
                                        taskFromMeLocalData.allTasks.ongoing = allTaskList
                                    }
                                } else {
                                    allTaskList[taskIndex] = ongoingTask
                                    taskFromMeLocalData.allTasks.ongoing = allTaskList
                                }
                                updatedTask = ongoingTask

                            } else if (doneTask != null) {
                                val allTaskList = taskFromMeLocalData.allTasks.done.toMutableList()
                                val taskIndex = allTaskList.indexOf(doneTask)

                                doneTask.seenBy = taskSeen.seenBy
                                doneTask.updatedAt = taskSeen.updatedAt

                                if (taskSeen.creatorStateChanged || taskSeen.stateChanged) {
                                    doneTask.creatorState = taskSeen.creatorState

                                    val foundState =
                                        doneTask.assignedToState.find { it.userId == taskSeen.state.userId }
                                    if (foundState != null) {
                                        val stateIndex =
                                            doneTask.assignedToState.indexOf(foundState)
                                        doneTask.assignedToState[stateIndex] = taskSeen.state
                                    }


                                    if (taskSeen.creatorState.equals(
                                            TaskStatus.UNREAD.name,
                                            true
                                        )
                                    ) {
                                        val allUnreadTaskList =
                                            taskFromMeLocalData.allTasks.unread.toMutableList()
                                        val newAllUnreadTaskList: MutableList<CeibroTaskV2> =
                                            mutableListOf()
                                        newAllUnreadTaskList.add(doneTask)
                                        newAllUnreadTaskList.addAll(allUnreadTaskList)

                                        allTaskList.removeAt(taskIndex)
                                        taskFromMeLocalData.allTasks.done = allTaskList
                                        taskFromMeLocalData.allTasks.unread = newAllUnreadTaskList

                                    } else if (taskSeen.creatorState.equals(
                                            TaskStatus.ONGOING.name, true
                                        )
                                    ) {
                                        val allOngoingTaskList =
                                            taskFromMeLocalData.allTasks.ongoing.toMutableList()
                                        val newAllOngoingTaskList: MutableList<CeibroTaskV2> =
                                            mutableListOf()
                                        newAllOngoingTaskList.add(doneTask)
                                        newAllOngoingTaskList.addAll(allOngoingTaskList)

                                        allTaskList.removeAt(taskIndex)
                                        taskFromMeLocalData.allTasks.done = allTaskList
                                        taskFromMeLocalData.allTasks.ongoing = newAllOngoingTaskList

                                    } else {
                                        allTaskList[taskIndex] = doneTask
                                        taskFromMeLocalData.allTasks.done = allTaskList
                                    }
                                } else {
                                    allTaskList[taskIndex] = doneTask
                                    taskFromMeLocalData.allTasks.done = allTaskList
                                }
                                updatedTask = doneTask
                            }
                        }
                    }

                    if (taskSeen.isAssignedToMe) {

                        if (!TaskV2DaoHelper(taskDao).isTaskListEmpty(
                                TaskRootStateTags.ToMe.tagValue, taskToMeLocalData
                            )
                        ) {
                            val newTask =
                                taskToMeLocalData.allTasks.new.find { it.id == taskSeen.taskId }
                            val ongoingTask =
                                taskToMeLocalData.allTasks.ongoing.find { it.id == taskSeen.taskId }
                            val doneTask =
                                taskToMeLocalData.allTasks.done.find { it.id == taskSeen.taskId }
                            if (newTask != null) {
                                val allTaskList = taskToMeLocalData.allTasks.new.toMutableList()
                                val taskIndex = allTaskList.indexOf(newTask)

                                newTask.seenBy = taskSeen.seenBy
                                newTask.updatedAt = taskSeen.updatedAt

                                if (taskSeen.creatorStateChanged || taskSeen.stateChanged) {
                                    newTask.creatorState = taskSeen.creatorState

                                    val foundState =
                                        newTask.assignedToState.find { it.userId == taskSeen.state.userId }
                                    if (foundState != null) {
                                        val stateIndex = newTask.assignedToState.indexOf(foundState)
                                        newTask.assignedToState[stateIndex] = taskSeen.state
                                    }


                                    if (taskSeen.state.userId == userId && taskSeen.state.state.equals(
                                            TaskStatus.ONGOING.name, true
                                        )
                                    ) {
                                        val allOngoingTaskList =
                                            taskToMeLocalData.allTasks.ongoing.toMutableList()
                                        val newAllOngoingTaskList: MutableList<CeibroTaskV2> =
                                            mutableListOf()
                                        newAllOngoingTaskList.add(newTask)
                                        newAllOngoingTaskList.addAll(allOngoingTaskList)

                                        allTaskList.removeAt(taskIndex)
                                        taskToMeLocalData.allTasks.new = allTaskList
                                        taskToMeLocalData.allTasks.ongoing = newAllOngoingTaskList

                                    } else if (taskSeen.state.userId == userId && taskSeen.state.state.equals(
                                            TaskStatus.DONE.name, true
                                        )
                                    ) {
                                        val allDoneTaskList =
                                            taskToMeLocalData.allTasks.done.toMutableList()
                                        val newAllDoneTaskList: MutableList<CeibroTaskV2> =
                                            mutableListOf()
                                        newAllDoneTaskList.add(newTask)
                                        newAllDoneTaskList.addAll(allDoneTaskList)

                                        allTaskList.removeAt(taskIndex)
                                        taskToMeLocalData.allTasks.new = allTaskList
                                        taskToMeLocalData.allTasks.done = newAllDoneTaskList

                                    } else {
                                        allTaskList[taskIndex] = newTask
                                        taskToMeLocalData.allTasks.new = allTaskList
                                    }
                                } else {
                                    allTaskList[taskIndex] = newTask
                                    taskToMeLocalData.allTasks.new = allTaskList
                                }
                                updatedTask = newTask

                            } else if (ongoingTask != null) {
                                val allTaskList = taskToMeLocalData.allTasks.ongoing.toMutableList()
                                val taskIndex = allTaskList.indexOf(ongoingTask)

                                ongoingTask.seenBy = taskSeen.seenBy
                                ongoingTask.updatedAt = taskSeen.updatedAt

                                if (taskSeen.creatorStateChanged || taskSeen.stateChanged) {
                                    ongoingTask.creatorState = taskSeen.creatorState

                                    val foundState =
                                        ongoingTask.assignedToState.find { it.userId == taskSeen.state.userId }
                                    if (foundState != null) {
                                        val stateIndex =
                                            ongoingTask.assignedToState.indexOf(foundState)
                                        ongoingTask.assignedToState[stateIndex] = taskSeen.state
                                    }


                                    if (taskSeen.state.userId == userId && taskSeen.state.state.equals(
                                            TaskStatus.UNREAD.name, true
                                        )
                                    ) {
                                        val allUnreadTaskList =
                                            taskToMeLocalData.allTasks.unread.toMutableList()
                                        val newAllUnreadTaskList: MutableList<CeibroTaskV2> =
                                            mutableListOf()
                                        newAllUnreadTaskList.add(ongoingTask)
                                        newAllUnreadTaskList.addAll(allUnreadTaskList)

                                        allTaskList.removeAt(taskIndex)
                                        taskToMeLocalData.allTasks.ongoing = allTaskList
                                        taskToMeLocalData.allTasks.unread = newAllUnreadTaskList

                                    } else if (taskSeen.state.userId == userId && taskSeen.state.state.equals(
                                            TaskStatus.DONE.name, true
                                        )
                                    ) {
                                        val allDoneTaskList =
                                            taskToMeLocalData.allTasks.done.toMutableList()
                                        val newAllDoneTaskList: MutableList<CeibroTaskV2> =
                                            mutableListOf()
                                        newAllDoneTaskList.add(ongoingTask)
                                        newAllDoneTaskList.addAll(allDoneTaskList)

                                        allTaskList.removeAt(taskIndex)
                                        taskToMeLocalData.allTasks.ongoing = allTaskList
                                        taskToMeLocalData.allTasks.done = newAllDoneTaskList

                                    } else {
                                        allTaskList[taskIndex] = ongoingTask
                                        taskToMeLocalData.allTasks.ongoing = allTaskList
                                    }
                                } else {
                                    allTaskList[taskIndex] = ongoingTask
                                    taskToMeLocalData.allTasks.ongoing = allTaskList
                                }
                                updatedTask = ongoingTask

                            } else if (doneTask != null) {
                                val allTaskList = taskToMeLocalData.allTasks.done.toMutableList()
                                val taskIndex = allTaskList.indexOf(doneTask)

                                doneTask.seenBy = taskSeen.seenBy
                                doneTask.updatedAt = taskSeen.updatedAt

                                if (taskSeen.creatorStateChanged || taskSeen.stateChanged) {
                                    doneTask.creatorState = taskSeen.creatorState

                                    val foundState =
                                        doneTask.assignedToState.find { it.userId == taskSeen.state.userId }
                                    if (foundState != null) {
                                        val stateIndex =
                                            doneTask.assignedToState.indexOf(foundState)
                                        doneTask.assignedToState[stateIndex] = taskSeen.state
                                    }


                                    if (taskSeen.state.userId == userId && taskSeen.state.state.equals(
                                            TaskStatus.UNREAD.name, true
                                        )
                                    ) {
                                        val allUnreadTaskList =
                                            taskToMeLocalData.allTasks.unread.toMutableList()
                                        val newAllUnreadTaskList: MutableList<CeibroTaskV2> =
                                            mutableListOf()
                                        newAllUnreadTaskList.add(doneTask)
                                        newAllUnreadTaskList.addAll(allUnreadTaskList)

                                        allTaskList.removeAt(taskIndex)
                                        taskToMeLocalData.allTasks.done = allTaskList
                                        taskToMeLocalData.allTasks.unread = newAllUnreadTaskList

                                    } else if (taskSeen.state.userId == userId && taskSeen.state.state.equals(
                                            TaskStatus.ONGOING.name, true
                                        )
                                    ) {
                                        val allOngoingTaskList =
                                            taskToMeLocalData.allTasks.ongoing.toMutableList()
                                        val newAllOngoingTaskList: MutableList<CeibroTaskV2> =
                                            mutableListOf()
                                        newAllOngoingTaskList.add(doneTask)
                                        newAllOngoingTaskList.addAll(allOngoingTaskList)

                                        allTaskList.removeAt(taskIndex)
                                        taskToMeLocalData.allTasks.done = allTaskList
                                        taskToMeLocalData.allTasks.ongoing = newAllOngoingTaskList

                                    } else {
                                        allTaskList[taskIndex] = doneTask
                                        taskToMeLocalData.allTasks.done = allTaskList
                                    }
                                } else {
                                    allTaskList[taskIndex] = doneTask
                                    taskToMeLocalData.allTasks.done = allTaskList
                                }
                                updatedTask = doneTask
                            }
                        }

                    }



                    TaskV2DaoHelper(taskDao).insertTaskData(
                        taskToMeLocalData
                    )
                    TaskV2DaoHelper(taskDao).insertTaskData(
                        taskFromMeLocalData
                    )
                    TaskV2DaoHelper(taskDao).insertTaskData(
                        taskHiddenLocalData
                    )
                    // send task data for ui update
                    EventBus.getDefault()
                        .post(LocalEvents.TaskForwardEvent(updatedTask))
                    EventBus.getDefault().post(LocalEvents.RefreshTasksEvent())

                    TaskEventsList.removeEvent(
                        SocketHandler.TaskEvent.TASK_SEEN.name,
                        taskSeen.taskId
                    )
                }
            }
        }
    }


    fun updateTaskCommentInLocal(
        eventData: EventV2Response.Data?,
        taskDao: TaskV2Dao,
        userId: String?,
        sessionManager: SessionManager
    ) {
        var updatedTask: CeibroTaskV2? = null
        val sharedViewModel = NavHostPresenterActivity.activityInstance?.let {
            ViewModelProvider(it).get(SharedViewModel::class.java)
        }

        if (eventData != null) {
            val isExists = TaskEventsList.isExists(
                    SocketHandler.TaskEvent.NEW_TASK_COMMENT.name, eventData.id, true
                )

            if (!isExists) {
                launch {
                    val taskID = eventData.taskId
                    val taskEvent = Events(
                        id = eventData.id,
                        taskId = eventData.taskId,
                        eventType = eventData.eventType,
                        initiator = eventData.initiator,
                        eventData = eventData.eventData,
                        commentData = eventData.commentData,
                        createdAt = eventData.createdAt,
                        updatedAt = eventData.updatedAt,
                        invitedMembers = eventData.invitedMembers
                    )
                    val taskEventList: MutableList<Events> = mutableListOf()
                    taskEventList.add(taskEvent)
                    sessionManager.saveUpdatedAtTimeStamp(eventData.taskUpdatedAt)

                    val taskHiddenLocalData =
                        TaskV2DaoHelper(taskDao).getTasks(TaskRootStateTags.Hidden.tagValue)
                    val taskToMeLocalData =
                        TaskV2DaoHelper(taskDao).getTasks(TaskRootStateTags.ToMe.tagValue)
                    val taskFromMeLocalData =
                        TaskV2DaoHelper(taskDao).getTasks(TaskRootStateTags.FromMe.tagValue)

                    val hiddenByCurrentUser = eventData.oldTaskData.isHiddenByMe
                    if (hiddenByCurrentUser) {
                        //it means task must be searched from hidden rootState and child states[ongoing and done] and then move the task to another root state from hidden
                        launch {

                            if (!TaskV2DaoHelper(taskDao).isTaskListEmpty(
                                    TaskRootStateTags.Hidden.tagValue, taskHiddenLocalData
                                )
                            ) {
                                val ongoingTask =
                                    taskHiddenLocalData.allTasks.ongoing.find { it.id == taskID }
                                val doneTask =
                                    taskHiddenLocalData.allTasks.done.find { it.id == taskID }

                                if (ongoingTask != null) {
                                    val allOngoingTaskList =
                                        taskHiddenLocalData.allTasks.ongoing.toMutableList()
                                    val taskIndex = allOngoingTaskList.indexOf(ongoingTask)

//                                    var oldEvents = ongoingTask.events.toMutableList()
//                                    if (oldEvents.isNotEmpty()) {
//                                        val oldOnlyEvent = oldEvents.find { it.id == eventData.id }
//                                        if (oldOnlyEvent != null) {     //means event already exist, so replace it
//                                            val oldEventIndex = oldEvents.indexOf(oldOnlyEvent)
//                                            oldEvents[oldEventIndex] = taskEvent
//                                            ongoingTask.events = oldEvents
//                                        } else {
//                                            oldEvents.add(taskEvent)
//                                            ongoingTask.events = oldEvents
//                                        }
//                                    } else {
//                                        oldEvents = taskEventList
//                                        ongoingTask.events = oldEvents
//                                    }
                                    ongoingTask.hiddenBy = listOf()
                                    ongoingTask.seenBy = eventData.taskData.seenBy
                                    ongoingTask.updatedAt = eventData.taskUpdatedAt

                                    allOngoingTaskList.removeAt(taskIndex)
                                    taskHiddenLocalData.allTasks.ongoing = allOngoingTaskList

                                    if (eventData.oldTaskData.isAssignedToMe) {
                                        val ongoingTaskToMe =
                                            taskToMeLocalData.allTasks.ongoing.find { it.id == taskID }
                                        val allOngoingToMeTaskList =
                                            taskToMeLocalData.allTasks.ongoing.toMutableList()
                                        if (ongoingTaskToMe != null) {
                                            val index =
                                                allOngoingToMeTaskList.indexOf(ongoingTaskToMe)
                                            allOngoingToMeTaskList[index] = ongoingTask
                                            taskToMeLocalData.allTasks.ongoing =
                                                allOngoingToMeTaskList
                                        } else {
                                            allOngoingToMeTaskList.add(0, ongoingTask)
                                            taskToMeLocalData.allTasks.ongoing =
                                                allOngoingToMeTaskList
                                        }
                                        sharedViewModel?.isToMeUnread?.value = true
                                        sessionManager.saveToMeUnread(true)

                                    }
                                    if (eventData.oldTaskData.isCreator) {
                                        val ongoingTaskFromMe =
                                            taskFromMeLocalData.allTasks.ongoing.find { it.id == taskID }
                                        val allOngoingFromMeTaskList =
                                            taskFromMeLocalData.allTasks.ongoing.toMutableList()
                                        if (ongoingTaskFromMe != null) {
                                            val index =
                                                allOngoingFromMeTaskList.indexOf(
                                                    ongoingTaskFromMe
                                                )
                                            allOngoingFromMeTaskList[index] = ongoingTask
                                            taskFromMeLocalData.allTasks.ongoing =
                                                allOngoingFromMeTaskList

                                            sharedViewModel?.isFromMeUnread?.value = true
                                            sessionManager.saveFromMeUnread(true)
                                        }
                                    }
                                    updatedTask = ongoingTask

                                }
                                else if (doneTask != null) {
                                    val allDoneTaskList =
                                        taskHiddenLocalData.allTasks.done.toMutableList()
                                    val taskIndex = allDoneTaskList.indexOf(doneTask)

//                                    var oldEvents = doneTask.events.toMutableList()
//                                    if (oldEvents.isNotEmpty()) {
//                                        val oldOnlyEvent = oldEvents.find { it.id == eventData.id }
//                                        if (oldOnlyEvent != null) {     //means event already exist, so replace it
//                                            val oldEventIndex = oldEvents.indexOf(oldOnlyEvent)
//                                            oldEvents[oldEventIndex] = taskEvent
//                                            doneTask.events = oldEvents
//                                        } else {
//                                            oldEvents.add(taskEvent)
//                                            doneTask.events = oldEvents
//                                        }
//                                    } else {
//                                        oldEvents = taskEventList
//                                        doneTask.events = oldEvents
//                                    }
                                    doneTask.hiddenBy = listOf()
                                    doneTask.seenBy = eventData.taskData.seenBy
                                    doneTask.updatedAt = eventData.taskUpdatedAt

                                    allDoneTaskList.removeAt(taskIndex)
                                    taskHiddenLocalData.allTasks.done = allDoneTaskList

                                    if (eventData.oldTaskData.isAssignedToMe) {
                                        val doneTaskToMe =
                                            taskToMeLocalData.allTasks.done.find { it.id == taskID }
                                        val allDoneToMeTaskList =
                                            taskToMeLocalData.allTasks.done.toMutableList()
                                        if (doneTaskToMe != null) {
                                            val index =
                                                allDoneToMeTaskList.indexOf(doneTaskToMe)
                                            allDoneToMeTaskList[index] = doneTask
                                            taskToMeLocalData.allTasks.done =
                                                allDoneToMeTaskList
                                        } else {
                                            allDoneToMeTaskList.add(0, doneTask)
                                            taskToMeLocalData.allTasks.done =
                                                allDoneToMeTaskList
                                        }
                                        sharedViewModel?.isToMeUnread?.value = true
                                        sessionManager.saveToMeUnread(true)
                                    }
                                    if (eventData.oldTaskData.isCreator) {

                                        val doneTaskFromMe =
                                            taskFromMeLocalData.allTasks.done.find { it.id == taskID }
                                        val allDoneFromMeTaskList =
                                            taskFromMeLocalData.allTasks.done.toMutableList()
                                        if (doneTaskFromMe != null) {
                                            val index =
                                                allDoneFromMeTaskList.indexOf(doneTaskFromMe)
                                            allDoneFromMeTaskList[index] = doneTask
                                            taskFromMeLocalData.allTasks.done =
                                                allDoneFromMeTaskList

                                            sharedViewModel?.isFromMeUnread?.value = true
                                            sessionManager.saveFromMeUnread(true)
                                        }

                                    }
                                    updatedTask = doneTask
                                }
                            }
                        }
                    } else if (eventData.oldTaskData.creatorState.equals(
                            TaskStatus.CANCELED.name,
                            true
                        )
                    ) {
                        // it means task must be in hidden rootState and child state will be canceled. search an update the task only
                        launch {
                            if (!TaskV2DaoHelper(taskDao).isTaskListEmpty(
                                    TaskRootStateTags.Hidden.tagValue, taskHiddenLocalData
                                )
                            ) {
                                val canceledTask =
                                    taskHiddenLocalData.allTasks.canceled.find { it.id == taskID }
                                if (canceledTask != null) {
                                    val allCancelTaskList =
                                        taskHiddenLocalData.allTasks.canceled.toMutableList()
                                    val taskIndex = allCancelTaskList.indexOf(canceledTask)

//                                    var oldEvents = canceledTask.events.toMutableList()
//                                    if (oldEvents.isNotEmpty()) {
//                                        val oldOnlyEvent = oldEvents.find { it.id == eventData.id }
//                                        if (oldOnlyEvent != null) {     //means event already exist, so replace it
//                                            val oldEventIndex = oldEvents.indexOf(oldOnlyEvent)
//                                            oldEvents[oldEventIndex] = taskEvent
//                                            canceledTask.events = oldEvents
//                                        } else {
//                                            oldEvents.add(taskEvent)
//                                            canceledTask.events = oldEvents
//                                        }
//                                    } else {
//                                        oldEvents = taskEventList
//                                        canceledTask.events = oldEvents
//                                    }
                                    canceledTask.hiddenBy = listOf()
                                    canceledTask.seenBy = eventData.taskData.seenBy
                                    canceledTask.updatedAt = eventData.taskUpdatedAt

                                    allCancelTaskList[taskIndex] = canceledTask
                                    taskHiddenLocalData.allTasks.canceled = allCancelTaskList

                                    updatedTask = canceledTask
                                    sharedViewModel?.isHiddenUnread?.value = true
                                    sessionManager.saveHiddenUnread(true)
                                }
                            }
                        }
                    }
                    launch {
                        if (eventData.oldTaskData.isAssignedToMe) {
                            if (!TaskV2DaoHelper(taskDao).isTaskListEmpty(
                                    TaskRootStateTags.ToMe.tagValue, taskToMeLocalData
                                )
                            ) {
                                val newTask =
                                    taskToMeLocalData.allTasks.new.find { it.id == taskID }
                                val ongoingTask =
                                    taskToMeLocalData.allTasks.ongoing.find { it.id == taskID }
                                val doneTask =
                                    taskToMeLocalData.allTasks.done.find { it.id == taskID }

                                if (newTask != null) {
                                    val allTaskList = taskToMeLocalData.allTasks.new.toMutableList()
                                    val taskIndex = allTaskList.indexOf(newTask)

//                                    var oldEvents = newTask.events.toMutableList()
//                                    if (oldEvents.isNotEmpty()) {
//                                        val oldOnlyEvent = oldEvents.find { it.id == eventData.id }
//                                        if (oldOnlyEvent != null) {     //means event already exist, so replace it
//                                            val oldEventIndex = oldEvents.indexOf(oldOnlyEvent)
//                                            oldEvents[oldEventIndex] = taskEvent
//                                            newTask.events = oldEvents
//                                        } else {
//                                            oldEvents.add(taskEvent)
//                                            newTask.events = oldEvents
//                                        }
//                                    } else {
//                                        oldEvents = taskEventList
//                                        newTask.events = oldEvents
//                                    }
                                    newTask.hiddenBy = listOf()
                                    newTask.seenBy = eventData.taskData.seenBy
                                    newTask.updatedAt = eventData.taskUpdatedAt

                                    allTaskList[taskIndex] = newTask
                                    taskToMeLocalData.allTasks.new = allTaskList
                                    updatedTask = newTask
                                    sharedViewModel?.isToMeUnread?.postValue(true)
                                    sessionManager.saveToMeUnread(true)

                                } else if (ongoingTask != null) {
                                    val allTaskList =
                                        taskToMeLocalData.allTasks.ongoing.toMutableList()
                                    val taskIndex = allTaskList.indexOf(ongoingTask)

//                                    var oldEvents = ongoingTask.events.toMutableList()
//                                    if (oldEvents.isNotEmpty()) {
//                                        val oldOnlyEvent = oldEvents.find { it.id == eventData.id }
//                                        if (oldOnlyEvent != null) {     //means event already exist, so replace it
//                                            val oldEventIndex = oldEvents.indexOf(oldOnlyEvent)
//                                            oldEvents[oldEventIndex] = taskEvent
//                                            ongoingTask.events = oldEvents
//                                        } else {
//                                            oldEvents.add(taskEvent)
//                                            ongoingTask.events = oldEvents
//                                        }
//                                    } else {
//                                        oldEvents = taskEventList
//                                        ongoingTask.events = oldEvents
//                                    }
                                    ongoingTask.hiddenBy = listOf()
                                    ongoingTask.seenBy = eventData.taskData.seenBy
                                    ongoingTask.updatedAt = eventData.taskUpdatedAt

                                    allTaskList[taskIndex] = ongoingTask
                                    taskToMeLocalData.allTasks.ongoing = allTaskList

                                    updatedTask = ongoingTask
                                    sharedViewModel?.isToMeUnread?.postValue(true)
                                    sessionManager.saveToMeUnread(true)

                                } else if (doneTask != null) {
                                    val allTaskList =
                                        taskToMeLocalData.allTasks.done.toMutableList()
                                    val taskIndex = allTaskList.indexOf(doneTask)

//                                    var oldEvents = doneTask.events.toMutableList()
//                                    if (oldEvents.isNotEmpty()) {
//                                        val oldOnlyEvent = oldEvents.find { it.id == eventData.id }
//                                        if (oldOnlyEvent != null) {     //means event already exist, so replace it
//                                            val oldEventIndex = oldEvents.indexOf(oldOnlyEvent)
//                                            oldEvents[oldEventIndex] = taskEvent
//                                            doneTask.events = oldEvents
//                                        } else {
//                                            oldEvents.add(taskEvent)
//                                            doneTask.events = oldEvents
//                                        }
//                                    } else {
//                                        oldEvents = taskEventList
//                                        doneTask.events = oldEvents
//                                    }
                                    doneTask.hiddenBy = listOf()
                                    doneTask.seenBy = eventData.taskData.seenBy
                                    doneTask.updatedAt = eventData.taskUpdatedAt

                                    allTaskList[taskIndex] = doneTask
                                    taskToMeLocalData.allTasks.done = allTaskList

                                    updatedTask = doneTask
                                    sharedViewModel?.isToMeUnread?.postValue(true)
                                    sessionManager.saveToMeUnread(true)
                                }
                            }
                        }

                        if (eventData.oldTaskData.isCreator) {
                            if (!TaskV2DaoHelper(taskDao).isTaskListEmpty(
                                    TaskRootStateTags.FromMe.tagValue, taskFromMeLocalData
                                )
                            ) {
                                val unreadTask =
                                    taskFromMeLocalData.allTasks.unread.find { it.id == taskID }
                                val ongoingTask =
                                    taskFromMeLocalData.allTasks.ongoing.find { it.id == taskID }
                                val doneTask =
                                    taskFromMeLocalData.allTasks.done.find { it.id == taskID }

                                if (unreadTask != null) {
                                    val allTaskList =
                                        taskFromMeLocalData.allTasks.unread.toMutableList()
                                    val taskIndex = allTaskList.indexOf(unreadTask)

//                                    var oldEvents = unreadTask.events.toMutableList()
//                                    if (oldEvents.isNotEmpty()) {
//                                        val oldOnlyEvent = oldEvents.find { it.id == eventData.id }
//                                        if (oldOnlyEvent != null) {     //means event already exist, so replace it
//                                            val oldEventIndex = oldEvents.indexOf(oldOnlyEvent)
//                                            oldEvents[oldEventIndex] = taskEvent
//                                            unreadTask.events = oldEvents
//                                        } else {
//                                            oldEvents.add(taskEvent)
//                                            unreadTask.events = oldEvents
//                                        }
//                                    } else {
//                                        oldEvents = taskEventList
//                                        unreadTask.events = oldEvents
//                                    }
                                    unreadTask.hiddenBy = listOf()
                                    unreadTask.seenBy = eventData.taskData.seenBy
                                    unreadTask.updatedAt = eventData.taskUpdatedAt

                                    allTaskList[taskIndex] = unreadTask
                                    taskFromMeLocalData.allTasks.unread = allTaskList

                                    updatedTask = unreadTask
                                    sharedViewModel?.isFromMeUnread?.value = true
                                    sessionManager.saveFromMeUnread(true)

                                } else if (ongoingTask != null) {
                                    val allTaskList =
                                        taskFromMeLocalData.allTasks.ongoing.toMutableList()
                                    val taskIndex = allTaskList.indexOf(ongoingTask)

//                                    var oldEvents = ongoingTask.events.toMutableList()
//                                    if (oldEvents.isNotEmpty()) {
//                                        val oldOnlyEvent = oldEvents.find { it.id == eventData.id }
//                                        if (oldOnlyEvent != null) {     //means event already exist, so replace it
//                                            val oldEventIndex = oldEvents.indexOf(oldOnlyEvent)
//                                            oldEvents[oldEventIndex] = taskEvent
//                                            ongoingTask.events = oldEvents
//                                        } else {
//                                            oldEvents.add(taskEvent)
//                                            ongoingTask.events = oldEvents
//                                        }
//                                    } else {
//                                        oldEvents = taskEventList
//                                        ongoingTask.events = oldEvents
//                                    }
                                    ongoingTask.hiddenBy = listOf()
                                    ongoingTask.seenBy = eventData.taskData.seenBy
                                    ongoingTask.updatedAt = eventData.taskUpdatedAt

                                    allTaskList[taskIndex] = ongoingTask
                                    taskFromMeLocalData.allTasks.ongoing = allTaskList

                                    updatedTask = ongoingTask
                                    sharedViewModel?.isFromMeUnread?.value = true
                                    sessionManager.saveFromMeUnread(true)

                                } else if (doneTask != null) {
                                    val allTaskList =
                                        taskFromMeLocalData.allTasks.done.toMutableList()
                                    val taskIndex = allTaskList.indexOf(doneTask)

//                                    var oldEvents = doneTask.events.toMutableList()
//                                    if (oldEvents.isNotEmpty()) {
//                                        val oldOnlyEvent = oldEvents.find { it.id == eventData.id }
//                                        if (oldOnlyEvent != null) {     //means event already exist, so replace it
//                                            val oldEventIndex = oldEvents.indexOf(oldOnlyEvent)
//                                            oldEvents[oldEventIndex] = taskEvent
//                                            doneTask.events = oldEvents
//                                        } else {
//                                            oldEvents.add(taskEvent)
//                                            doneTask.events = oldEvents
//                                        }
//                                    } else {
//                                        oldEvents = taskEventList
//                                        doneTask.events = oldEvents
//                                    }
                                    doneTask.hiddenBy = listOf()
                                    doneTask.seenBy = eventData.taskData.seenBy
                                    doneTask.updatedAt = eventData.taskUpdatedAt

                                    allTaskList[taskIndex] = doneTask
                                    taskFromMeLocalData.allTasks.done = allTaskList

                                    updatedTask = doneTask
                                    sharedViewModel?.isFromMeUnread?.value = true
                                    sessionManager.saveFromMeUnread(true)
                                }
                            }
                        }
                    }

                    TaskV2DaoHelper(taskDao).insertTaskData(
                        taskHiddenLocalData
                    )
                    TaskV2DaoHelper(taskDao).insertTaskData(
                        taskToMeLocalData
                    )
                    TaskV2DaoHelper(taskDao).insertTaskData(
                        taskFromMeLocalData
                    )

                    // send task data for ui update
                    EventBus.getDefault().post(LocalEvents.TaskEvent(taskEvent))
                    EventBus.getDefault().post(LocalEvents.RefreshTasksEvent())

                    if (eventData.initiator.id == userId) {
                        TaskEventsList.removeEvent(
                            SocketHandler.TaskEvent.NEW_TASK_COMMENT.name,
                            eventData.taskId
                        )
                    }
                }
            }
        }
//        return updatedTask
    }

    fun updateTaskUnCanceledInLocal(
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
                val taskID = eventData.taskId

                val taskEvent = Events(
                    id = eventData.id,
                    taskId = eventData.taskId,
                    eventType = eventData.eventType,
                    initiator = eventData.initiator,
                    eventData = eventData.eventData,
                    commentData = eventData.commentData,
                    createdAt = eventData.createdAt,
                    updatedAt = eventData.updatedAt,
                    invitedMembers = eventData.invitedMembers
                )
                val taskEventList: MutableList<Events> = mutableListOf()
                taskEventList.add(taskEvent)
                sessionManager.saveUpdatedAtTimeStamp(eventData.taskUpdatedAt)

                launch {
                    val taskHiddenLocalData =
                        TaskV2DaoHelper(taskDao).getTasks(TaskRootStateTags.Hidden.tagValue)
                    val taskToMeLocalData =
                        TaskV2DaoHelper(taskDao).getTasks(TaskRootStateTags.ToMe.tagValue)
                    val taskFromMeLocalData =
                        TaskV2DaoHelper(taskDao).getTasks(TaskRootStateTags.FromMe.tagValue)

                    val removingTask =
                        taskHiddenLocalData.allTasks.canceled.find { it.id == taskID }

                    removingTask?.let { task ->
                        val cancelledList =
                            taskHiddenLocalData.allTasks.canceled.toMutableList()
                        val index = cancelledList.indexOf(removingTask)

//                        var oldEvents = task.events.toMutableList()
//                        if (oldEvents.isNotEmpty()) {
//                            val oldOnlyEvent = oldEvents.find { it.id == eventData.id }
//                            if (oldOnlyEvent != null) {     //means event already exist, so replace it
//                                val oldEventIndex = oldEvents.indexOf(oldOnlyEvent)
//                                oldEvents[oldEventIndex] = taskEvent
//                                task.events = oldEvents
//                            } else {
//                                oldEvents.add(taskEvent)
//                                task.events = oldEvents
//                            }
//                        } else {
//                            oldEvents = taskEventList
//                            task.events = oldEvents
//                        }
                        task.hiddenBy = listOf()
                        task.seenBy = eventData.taskData.seenBy
                        task.creatorState = eventData.taskData.creatorState
                        task.updatedAt = eventData.taskUpdatedAt
                        val assignToList = task.assignedToState
                        assignToList.map {
                            it.state = TaskStatus.NEW.name
                        }
                        task.assignedToState = assignToList

                        cancelledList.removeAt(index)
                        taskHiddenLocalData.allTasks.canceled = cancelledList


                        if (eventData.oldTaskData.isAssignedToMe) {
                            val newTaskList = taskToMeLocalData.allTasks.new.toMutableList()
                            val newTask = newTaskList.find { it.id == taskID }

                            if (newTask == null) {
                                newTaskList.add(0, task)
                                taskToMeLocalData.allTasks.new = newTaskList
                            } else {
                                val newTaskIndex = newTaskList.indexOf(newTask)
                                newTaskList[newTaskIndex] = task
                                taskToMeLocalData.allTasks.new = newTaskList
                            }

                            sharedViewModel?.isToMeUnread?.postValue(true)
                        }

                        if (eventData.oldTaskData.isCreator) {
                            val unreadTaskList =
                                taskFromMeLocalData.allTasks.unread.toMutableList()
                            val unreadTask = unreadTaskList.find { it.id == taskID }

                            if (unreadTask == null) {
                                unreadTaskList.add(0, task)
                                taskFromMeLocalData.allTasks.unread = unreadTaskList
                            } else {
                                val unreadTaskIndex = unreadTaskList.indexOf(unreadTask)
                                unreadTaskList[unreadTaskIndex] = task
                                taskFromMeLocalData.allTasks.unread = unreadTaskList
                            }

                            sharedViewModel?.isFromMeUnread?.postValue(true)
                        }

                        TaskV2DaoHelper(taskDao).insertTaskData(taskFromMeLocalData)
                        TaskV2DaoHelper(taskDao).insertTaskData(taskToMeLocalData)
                        TaskV2DaoHelper(taskDao).insertTaskData(taskHiddenLocalData)

                        EventBus.getDefault().post(LocalEvents.RefreshTasksEvent())
                        EventBus.getDefault().post(LocalEvents.TaskForwardEvent(task))

                        TaskEventsList.removeEvent(
                            SocketHandler.TaskEvent.UN_CANCEL_TASK.name,
                            taskID
                        )
                    }
                }
            }
        }
    }

    fun updateTaskCanceledInLocal(
        eventData: EventV2Response.Data?,
        taskDao: TaskV2Dao,
        userId: String?,
        sessionManager: SessionManager
    ): CeibroTaskV2? {
        var updatedTask: CeibroTaskV2? = null
        val sharedViewModel = NavHostPresenterActivity.activityInstance?.let {
            ViewModelProvider(it).get(SharedViewModel::class.java)
        }

        if (eventData != null) {
            val isExists = TaskEventsList.isExists(
                SocketHandler.TaskEvent.CANCELED_TASK.name, eventData.taskId, true
            )
            if (!isExists) {
                launch {
                    val taskID = eventData.taskId

                    val taskEvent = Events(
                        id = eventData.id,
                        taskId = eventData.taskId,
                        eventType = eventData.eventType,
                        initiator = eventData.initiator,
                        eventData = eventData.eventData,
                        commentData = eventData.commentData,
                        createdAt = eventData.createdAt,
                        updatedAt = eventData.updatedAt,
                        invitedMembers = eventData.invitedMembers
                    )
                    val taskEventList: MutableList<Events> = mutableListOf()
                    taskEventList.add(taskEvent)
                    sessionManager.saveUpdatedAtTimeStamp(eventData.taskUpdatedAt)

                    val taskToMeLocalData =
                        TaskV2DaoHelper(taskDao).getTasks(TaskRootStateTags.ToMe.tagValue)
                    val taskFromMeLocalData =
                        TaskV2DaoHelper(taskDao).getTasks(TaskRootStateTags.FromMe.tagValue)
                    val taskHiddenLocalData =
                        TaskV2DaoHelper(taskDao).getTasks(TaskRootStateTags.Hidden.tagValue)


                    val hiddenByCurrentUser = eventData.oldTaskData.isHiddenByMe
                    if (hiddenByCurrentUser) {
                        //it means task must be searched from hidden rootState and child states[ongoing and done] and then move the task to another root state from hidden
                        launch {

                            if (!TaskV2DaoHelper(taskDao).isTaskListEmpty(
                                    TaskRootStateTags.Hidden.tagValue, taskHiddenLocalData
                                )
                            ) {
                                val ongoingTask =
                                    taskHiddenLocalData.allTasks.ongoing.find { it.id == taskID }
                                val doneTask =
                                    taskHiddenLocalData.allTasks.done.find { it.id == taskID }

                                if (ongoingTask != null) {
                                    val allOngoingTaskList =
                                        taskHiddenLocalData.allTasks.ongoing.toMutableList()
                                    val taskIndex = allOngoingTaskList.indexOf(ongoingTask)

//                                    var oldEvents = ongoingTask.events.toMutableList()
//                                    if (oldEvents.isNotEmpty()) {
//                                        val oldOnlyEvent = oldEvents.find { it.id == eventData.id }
//                                        if (oldOnlyEvent != null) {     //means event already exist, so replace it
//                                            val oldEventIndex = oldEvents.indexOf(oldOnlyEvent)
//                                            oldEvents[oldEventIndex] = taskEvent
//                                            ongoingTask.events = oldEvents
//                                        } else {
//                                            oldEvents.add(taskEvent)
//                                            ongoingTask.events = oldEvents
//                                        }
//                                    } else {
//                                        oldEvents = taskEventList
//                                        ongoingTask.events = oldEvents
//                                    }
                                    ongoingTask.hiddenBy = listOf()
                                    ongoingTask.seenBy = eventData.taskData.seenBy
                                    ongoingTask.updatedAt = eventData.taskUpdatedAt
                                    ongoingTask.creatorState = eventData.taskData.creatorState
                                    val assignToList = ongoingTask.assignedToState
                                    assignToList.map {
                                        it.state = eventData.taskData.creatorState
                                    }     //as creator state is canceled, so all assignee will be canceled
                                    ongoingTask.assignedToState = assignToList

                                    allOngoingTaskList.removeAt(taskIndex)
                                    taskHiddenLocalData.allTasks.ongoing = allOngoingTaskList

                                    val allCanceledTaskList =
                                        taskHiddenLocalData.allTasks.canceled.toMutableList()
                                    val canceledTask =
                                        taskHiddenLocalData.allTasks.canceled.find { it.id == taskID }
                                    if (canceledTask != null) {
                                        val canceledTaskIndex =
                                            allCanceledTaskList.indexOf(canceledTask)
                                        allCanceledTaskList[canceledTaskIndex] = ongoingTask
                                        taskHiddenLocalData.allTasks.canceled = allCanceledTaskList
                                    } else {
                                        allCanceledTaskList.add(0, ongoingTask)
                                        taskHiddenLocalData.allTasks.canceled = allCanceledTaskList
                                    }
                                    updatedTask = ongoingTask
                                    if (ongoingTask.creator.id != userId) {
                                        sharedViewModel?.isHiddenUnread?.postValue(true)
                                        sessionManager.saveHiddenUnread(true)
                                    }

                                } else if (doneTask != null) {
                                    val allDoneTaskList =
                                        taskHiddenLocalData.allTasks.done.toMutableList()
                                    val taskIndex = allDoneTaskList.indexOf(doneTask)

//                                    var oldEvents = doneTask.events.toMutableList()
//                                    if (oldEvents.isNotEmpty()) {
//                                        val oldOnlyEvent = oldEvents.find { it.id == eventData.id }
//                                        if (oldOnlyEvent != null) {     //means event already exist, so replace it
//                                            val oldEventIndex = oldEvents.indexOf(oldOnlyEvent)
//                                            oldEvents[oldEventIndex] = taskEvent
//                                            doneTask.events = oldEvents
//                                        } else {
//                                            oldEvents.add(taskEvent)
//                                            doneTask.events = oldEvents
//                                        }
//                                    } else {
//                                        oldEvents = taskEventList
//                                        doneTask.events = oldEvents
//                                    }
                                    doneTask.hiddenBy = listOf()
                                    doneTask.seenBy = eventData.taskData.seenBy
                                    doneTask.updatedAt = eventData.taskUpdatedAt
                                    doneTask.creatorState = eventData.taskData.creatorState
                                    val assignToList = doneTask.assignedToState
                                    assignToList.map {
                                        it.state = eventData.taskData.creatorState
                                    }     //as creator state is canceled, so all assignee will be canceled
                                    doneTask.assignedToState = assignToList

                                    allDoneTaskList.removeAt(taskIndex)
                                    taskHiddenLocalData.allTasks.done = allDoneTaskList

                                    val allCanceledTaskList =
                                        taskHiddenLocalData.allTasks.canceled.toMutableList()
                                    val canceledTask =
                                        taskHiddenLocalData.allTasks.canceled.find { it.id == taskID }
                                    if (canceledTask != null) {
                                        val canceledTaskIndex =
                                            allCanceledTaskList.indexOf(canceledTask)
                                        allCanceledTaskList[canceledTaskIndex] = doneTask
                                        taskHiddenLocalData.allTasks.canceled = allCanceledTaskList
                                    } else {
                                        allCanceledTaskList.add(0, doneTask)
                                        taskHiddenLocalData.allTasks.canceled = allCanceledTaskList
                                    }
                                    updatedTask = doneTask
                                    if (doneTask.creator.id != userId) {
                                        sharedViewModel?.isHiddenUnread?.postValue(true)
                                        sessionManager.saveHiddenUnread(true)
                                    }
                                }

                            }

                        }
                    } else if (eventData.oldTaskData.creatorState.equals(
                            TaskStatus.CANCELED.name,
                            true
                        )
                    ) {
                        // it means task must be in hidden rootState and child state will be canceled. search and update the task only
                        launch {
                            if (!TaskV2DaoHelper(taskDao).isTaskListEmpty(
                                    TaskRootStateTags.Hidden.tagValue, taskHiddenLocalData
                                )
                            ) {
                                val canceledTask =
                                    taskHiddenLocalData.allTasks.canceled.find { it.id == taskID }
                                if (canceledTask != null) {
                                    val allCancelTaskList =
                                        taskHiddenLocalData.allTasks.canceled.toMutableList()
                                    val taskIndex = allCancelTaskList.indexOf(canceledTask)

                                    canceledTask.hiddenBy = listOf()
                                    canceledTask.seenBy = eventData.taskData.seenBy
                                    canceledTask.updatedAt = eventData.taskUpdatedAt
                                    canceledTask.creatorState = eventData.taskData.creatorState
                                    val assignToList = canceledTask.assignedToState
                                    assignToList.map {
                                        it.state = eventData.taskData.creatorState
                                    }     //as creator state is canceled, so all assignee will be canceled
                                    canceledTask.assignedToState = assignToList

                                    allCancelTaskList[taskIndex] = canceledTask
                                    taskHiddenLocalData.allTasks.canceled = allCancelTaskList

                                    updatedTask = canceledTask

                                    if (canceledTask.creator.id != userId) {
                                        sharedViewModel?.isHiddenUnread?.postValue(true)
                                        sessionManager.saveHiddenUnread(true)
                                    }
                                }
                            }
                        }
                    }

                    launch {
                        if (eventData.oldTaskData.isAssignedToMe) {
                            if (!TaskV2DaoHelper(taskDao).isTaskListEmpty(
                                    TaskRootStateTags.ToMe.tagValue, taskToMeLocalData
                                )
                            ) {
                                val newTask =
                                    taskToMeLocalData.allTasks.new.find { it.id == taskID }
                                val ongoingTask =
                                    taskToMeLocalData.allTasks.ongoing.find { it.id == taskID }
                                val doneTask =
                                    taskToMeLocalData.allTasks.done.find { it.id == taskID }

                                if (newTask != null) {
                                    val allTaskList =
                                        taskToMeLocalData.allTasks.new.toMutableList()
                                    val taskIndex = allTaskList.indexOf(newTask)

//                                    var oldEvents = newTask.events.toMutableList()
//                                    if (oldEvents.isNotEmpty()) {
//                                        val oldOnlyEvent =
//                                            oldEvents.find { it.id == eventData.id }
//                                        if (oldOnlyEvent != null) {     //means event already exist, so replace it
//                                            val oldEventIndex = oldEvents.indexOf(oldOnlyEvent)
//                                            oldEvents[oldEventIndex] = taskEvent
//                                            newTask.events = oldEvents
//                                        } else {
//                                            oldEvents.add(taskEvent)
//                                            newTask.events = oldEvents
//                                        }
//                                    } else {
//                                        oldEvents = taskEventList
//                                        newTask.events = oldEvents
//                                    }
                                    newTask.hiddenBy = listOf()
                                    newTask.seenBy = eventData.taskData.seenBy
                                    newTask.updatedAt = eventData.taskUpdatedAt
                                    newTask.creatorState = eventData.taskData.creatorState
                                    val assignToList = newTask.assignedToState
                                    assignToList.map {
                                        it.state = eventData.taskData.creatorState
                                    }     //as creator state is canceled, so all assignee will be canceled
                                    newTask.assignedToState = assignToList

                                    allTaskList.removeAt(taskIndex)
                                    taskToMeLocalData.allTasks.new = allTaskList

                                    val allCanceledTaskList =
                                        taskHiddenLocalData.allTasks.canceled.toMutableList()
                                    val canceledTask =
                                        taskHiddenLocalData.allTasks.canceled.find { it.id == taskID }
                                    if (canceledTask != null) {
                                        val canceledTaskIndex =
                                            allCanceledTaskList.indexOf(canceledTask)
                                        allCanceledTaskList[canceledTaskIndex] = newTask
                                        taskHiddenLocalData.allTasks.canceled =
                                            allCanceledTaskList
                                    } else {
                                        allCanceledTaskList.add(0, newTask)
                                        taskHiddenLocalData.allTasks.canceled =
                                            allCanceledTaskList
                                    }
                                    updatedTask = newTask
                                    if (newTask.creator.id != userId) {
                                        sharedViewModel?.isHiddenUnread?.postValue(true)
                                        sessionManager.saveHiddenUnread(true)
                                    }

                                } else if (ongoingTask != null) {
                                    val allTaskList =
                                        taskToMeLocalData.allTasks.ongoing.toMutableList()
                                    val taskIndex = allTaskList.indexOf(ongoingTask)

//                                    var oldEvents = ongoingTask.events.toMutableList()
//                                    if (oldEvents.isNotEmpty()) {
//                                        val oldOnlyEvent =
//                                            oldEvents.find { it.id == eventData.id }
//                                        if (oldOnlyEvent != null) {     //means event already exist, so replace it
//                                            val oldEventIndex = oldEvents.indexOf(oldOnlyEvent)
//                                            oldEvents[oldEventIndex] = taskEvent
//                                            ongoingTask.events = oldEvents
//                                        } else {
//                                            oldEvents.add(taskEvent)
//                                            ongoingTask.events = oldEvents
//                                        }
//                                    } else {
//                                        oldEvents = taskEventList
//                                        ongoingTask.events = oldEvents
//                                    }
                                    ongoingTask.hiddenBy = listOf()
                                    ongoingTask.seenBy = eventData.taskData.seenBy
                                    ongoingTask.updatedAt = eventData.taskUpdatedAt
                                    ongoingTask.creatorState = eventData.taskData.creatorState
                                    val assignToList = ongoingTask.assignedToState
                                    assignToList.map {
                                        it.state = eventData.taskData.creatorState
                                    }     //as creator state is canceled, so all assignee will be canceled
                                    ongoingTask.assignedToState = assignToList

                                    allTaskList.removeAt(taskIndex)
                                    taskToMeLocalData.allTasks.ongoing = allTaskList

                                    val allCanceledTaskList =
                                        taskHiddenLocalData.allTasks.canceled.toMutableList()
                                    val canceledTask =
                                        taskHiddenLocalData.allTasks.canceled.find { it.id == taskID }
                                    if (canceledTask != null) {
                                        val canceledTaskIndex =
                                            allCanceledTaskList.indexOf(canceledTask)
                                        allCanceledTaskList[canceledTaskIndex] = ongoingTask
                                        taskHiddenLocalData.allTasks.canceled =
                                            allCanceledTaskList
                                    } else {
                                        allCanceledTaskList.add(0, ongoingTask)
                                        taskHiddenLocalData.allTasks.canceled =
                                            allCanceledTaskList
                                    }
                                    updatedTask = ongoingTask
                                    if (ongoingTask.creator.id != userId) {
                                        sharedViewModel?.isHiddenUnread?.postValue(true)
                                        sessionManager.saveHiddenUnread(true)
                                    }
                                } else if (doneTask != null) {
                                    val allTaskList =
                                        taskToMeLocalData.allTasks.done.toMutableList()
                                    val taskIndex = allTaskList.indexOf(doneTask)

//                                    var oldEvents = doneTask.events.toMutableList()
//                                    if (oldEvents.isNotEmpty()) {
//                                        val oldOnlyEvent =
//                                            oldEvents.find { it.id == eventData.id }
//                                        if (oldOnlyEvent != null) {     //means event already exist, so replace it
//                                            val oldEventIndex = oldEvents.indexOf(oldOnlyEvent)
//                                            oldEvents[oldEventIndex] = taskEvent
//                                            doneTask.events = oldEvents
//                                        } else {
//                                            oldEvents.add(taskEvent)
//                                            doneTask.events = oldEvents
//                                        }
//                                    } else {
//                                        oldEvents = taskEventList
//                                        doneTask.events = oldEvents
//                                    }
                                    doneTask.hiddenBy = listOf()
                                    doneTask.seenBy = eventData.taskData.seenBy
                                    doneTask.updatedAt = eventData.taskUpdatedAt
                                    doneTask.creatorState = eventData.taskData.creatorState
                                    val assignToList = doneTask.assignedToState
                                    assignToList.map {
                                        it.state = eventData.taskData.creatorState
                                    }     //as creator state is canceled, so all assignee will be canceled
                                    doneTask.assignedToState = assignToList

                                    allTaskList.removeAt(taskIndex)
                                    taskToMeLocalData.allTasks.done = allTaskList

                                    val allCanceledTaskList =
                                        taskHiddenLocalData.allTasks.canceled.toMutableList()
                                    val canceledTask =
                                        taskHiddenLocalData.allTasks.canceled.find { it.id == taskID }
                                    if (canceledTask != null) {
                                        val canceledTaskIndex =
                                            allCanceledTaskList.indexOf(canceledTask)
                                        allCanceledTaskList[canceledTaskIndex] = doneTask
                                        taskHiddenLocalData.allTasks.canceled =
                                            allCanceledTaskList
                                    } else {
                                        allCanceledTaskList.add(0, doneTask)
                                        taskHiddenLocalData.allTasks.canceled =
                                            allCanceledTaskList
                                    }
                                    updatedTask = doneTask
                                    if (doneTask.creator.id != userId) {
                                        sharedViewModel?.isHiddenUnread?.postValue(true)
                                        sessionManager.saveHiddenUnread(true)
                                    }
                                }
                            }
                        }

                        if (eventData.oldTaskData.isCreator) {
                            if (!TaskV2DaoHelper(taskDao).isTaskListEmpty(
                                    TaskRootStateTags.FromMe.tagValue, taskFromMeLocalData
                                )
                            ) {
                                val unreadTask =
                                    taskFromMeLocalData.allTasks.unread.find { it.id == taskID }
                                val ongoingTask =
                                    taskFromMeLocalData.allTasks.ongoing.find { it.id == taskID }
                                val doneTask =
                                    taskFromMeLocalData.allTasks.done.find { it.id == taskID }

                                if (unreadTask != null) {
                                    val allTaskList =
                                        taskFromMeLocalData.allTasks.unread.toMutableList()
                                    val taskIndex = allTaskList.indexOf(unreadTask)

//                                    var oldEvents = unreadTask.events.toMutableList()
//                                    if (oldEvents.isNotEmpty()) {
//                                        val oldOnlyEvent =
//                                            oldEvents.find { it.id == eventData.id }
//                                        if (oldOnlyEvent != null) {     //means event already exist, so replace it
//                                            val oldEventIndex = oldEvents.indexOf(oldOnlyEvent)
//                                            oldEvents[oldEventIndex] = taskEvent
//                                            unreadTask.events = oldEvents
//                                        } else {
//                                            oldEvents.add(taskEvent)
//                                            unreadTask.events = oldEvents
//                                        }
//                                    } else {
//                                        oldEvents = taskEventList
//                                        unreadTask.events = oldEvents
//                                    }
                                    unreadTask.hiddenBy = listOf()
                                    unreadTask.seenBy = eventData.taskData.seenBy
                                    unreadTask.updatedAt = eventData.taskUpdatedAt
                                    unreadTask.creatorState = eventData.taskData.creatorState
                                    val assignToList = unreadTask.assignedToState
                                    assignToList.map {
                                        it.state = eventData.taskData.creatorState
                                    }     //as creator state is canceled, so all assignee will be canceled
                                    unreadTask.assignedToState = assignToList

                                    allTaskList.removeAt(taskIndex)
                                    taskFromMeLocalData.allTasks.unread = allTaskList

                                    val allCanceledTaskList =
                                        taskHiddenLocalData.allTasks.canceled.toMutableList()
                                    val canceledTask =
                                        taskHiddenLocalData.allTasks.canceled.find { it.id == taskID }
                                    if (canceledTask != null) {
                                        val canceledTaskIndex =
                                            allCanceledTaskList.indexOf(canceledTask)
                                        allCanceledTaskList[canceledTaskIndex] = unreadTask
                                        taskHiddenLocalData.allTasks.canceled =
                                            allCanceledTaskList
                                    } else {
                                        allCanceledTaskList.add(0, unreadTask)
                                        taskHiddenLocalData.allTasks.canceled =
                                            allCanceledTaskList
                                    }
                                    updatedTask = unreadTask
                                    if (unreadTask.creator.id != userId) {
                                        sharedViewModel?.isHiddenUnread?.postValue(true)
                                        sessionManager.saveHiddenUnread(true)
                                    }

                                } else if (ongoingTask != null) {
                                    val allTaskList =
                                        taskFromMeLocalData.allTasks.ongoing.toMutableList()
                                    val taskIndex = allTaskList.indexOf(ongoingTask)

//                                    var oldEvents = ongoingTask.events.toMutableList()
//                                    if (oldEvents.isNotEmpty()) {
//                                        val oldOnlyEvent =
//                                            oldEvents.find { it.id == eventData.id }
//                                        if (oldOnlyEvent != null) {     //means event already exist, so replace it
//                                            val oldEventIndex = oldEvents.indexOf(oldOnlyEvent)
//                                            oldEvents[oldEventIndex] = taskEvent
//                                            ongoingTask.events = oldEvents
//                                        } else {
//                                            oldEvents.add(taskEvent)
//                                            ongoingTask.events = oldEvents
//                                        }
//                                    } else {
//                                        oldEvents = taskEventList
//                                        ongoingTask.events = oldEvents
//                                    }
                                    ongoingTask.hiddenBy = listOf()
                                    ongoingTask.seenBy = eventData.taskData.seenBy
                                    ongoingTask.updatedAt = eventData.taskUpdatedAt
                                    ongoingTask.creatorState = eventData.taskData.creatorState
                                    val assignToList = ongoingTask.assignedToState
                                    assignToList.map {
                                        it.state = eventData.taskData.creatorState
                                    }     //as creator state is canceled, so all assignee will be canceled
                                    ongoingTask.assignedToState = assignToList

                                    allTaskList.removeAt(taskIndex)
                                    taskFromMeLocalData.allTasks.ongoing = allTaskList

                                    val allCanceledTaskList =
                                        taskHiddenLocalData.allTasks.canceled.toMutableList()
                                    val canceledTask =
                                        taskHiddenLocalData.allTasks.canceled.find { it.id == taskID }
                                    if (canceledTask != null) {
                                        val canceledTaskIndex =
                                            allCanceledTaskList.indexOf(canceledTask)
                                        allCanceledTaskList[canceledTaskIndex] = ongoingTask
                                        taskHiddenLocalData.allTasks.canceled =
                                            allCanceledTaskList
                                    } else {
                                        allCanceledTaskList.add(0, ongoingTask)
                                        taskHiddenLocalData.allTasks.canceled =
                                            allCanceledTaskList
                                    }
                                    updatedTask = ongoingTask
                                    if (ongoingTask.creator.id != userId) {
                                        sharedViewModel?.isHiddenUnread?.postValue(true)
                                        sessionManager.saveHiddenUnread(true)
                                    }

                                } else if (doneTask != null) {
                                    val allTaskList =
                                        taskFromMeLocalData.allTasks.done.toMutableList()
                                    val taskIndex = allTaskList.indexOf(doneTask)

//                                    var oldEvents = doneTask.events.toMutableList()
//                                    if (oldEvents.isNotEmpty()) {
//                                        val oldOnlyEvent =
//                                            oldEvents.find { it.id == eventData.id }
//                                        if (oldOnlyEvent != null) {     //means event already exist, so replace it
//                                            val oldEventIndex = oldEvents.indexOf(oldOnlyEvent)
//                                            oldEvents[oldEventIndex] = taskEvent
//                                            doneTask.events = oldEvents
//                                        } else {
//                                            oldEvents.add(taskEvent)
//                                            doneTask.events = oldEvents
//                                        }
//                                    } else {
//                                        oldEvents = taskEventList
//                                        doneTask.events = oldEvents
//                                    }
                                    doneTask.hiddenBy = listOf()
                                    doneTask.seenBy = eventData.taskData.seenBy
                                    doneTask.updatedAt = eventData.taskUpdatedAt
                                    doneTask.creatorState = eventData.taskData.creatorState
                                    val assignToList = doneTask.assignedToState
                                    assignToList.map {
                                        it.state = eventData.taskData.creatorState
                                    }     //as creator state is canceled, so all assignee will be canceled
                                    doneTask.assignedToState = assignToList

                                    allTaskList.removeAt(taskIndex)
                                    taskFromMeLocalData.allTasks.done = allTaskList

                                    val allCanceledTaskList =
                                        taskHiddenLocalData.allTasks.canceled.toMutableList()
                                    val canceledTask =
                                        taskHiddenLocalData.allTasks.canceled.find { it.id == taskID }
                                    if (canceledTask != null) {
                                        val canceledTaskIndex =
                                            allCanceledTaskList.indexOf(canceledTask)
                                        allCanceledTaskList[canceledTaskIndex] = doneTask
                                        taskHiddenLocalData.allTasks.canceled =
                                            allCanceledTaskList
                                    } else {
                                        allCanceledTaskList.add(0, doneTask)
                                        taskHiddenLocalData.allTasks.canceled =
                                            allCanceledTaskList
                                    }
                                    updatedTask = doneTask
                                    if (doneTask.creator.id != userId) {
                                        sharedViewModel?.isHiddenUnread?.postValue(true)
                                        sessionManager.saveHiddenUnread(true)
                                    }
                                }
                            }
                        }


                        TaskV2DaoHelper(taskDao).insertTaskData(
                            taskToMeLocalData
                        )
                        TaskV2DaoHelper(taskDao).insertTaskData(
                            taskFromMeLocalData
                        )
                        TaskV2DaoHelper(taskDao).insertTaskData(
                            taskHiddenLocalData
                        )
                        // send task data for ui update
                        EventBus.getDefault()
                            .post(LocalEvents.TaskForwardEvent(updatedTask))
                        EventBus.getDefault().post(LocalEvents.RefreshTasksEvent())
                    }

                    TaskEventsList.removeEvent(SocketHandler.TaskEvent.CANCELED_TASK.name, taskID)
                }
            }
        }

        return updatedTask
    }

    fun updateTaskDoneInLocal(
        eventData: EventV2Response.Data?, taskDao: TaskV2Dao, sessionManager: SessionManager
    ): CeibroTaskV2? {
        var updatedTask: CeibroTaskV2? = null
        val sharedViewModel = NavHostPresenterActivity.activityInstance?.let {
            ViewModelProvider(it).get(SharedViewModel::class.java)
        }
        launch {
            if (eventData != null) {
                val isExists = TaskEventsList.isExists(
                    SocketHandler.TaskEvent.TASK_DONE.name, eventData.taskId, true
                )
                if (!isExists) {
                    val taskID = eventData.taskId
                    val taskEvent = Events(
                        id = eventData.id,
                        taskId = eventData.taskId,
                        eventType = eventData.eventType,
                        initiator = eventData.initiator,
                        eventData = eventData.eventData,
                        commentData = eventData.commentData,
                        createdAt = eventData.createdAt,
                        updatedAt = eventData.updatedAt,
                        invitedMembers = eventData.invitedMembers
                    )
                    val taskEventList: MutableList<Events> = mutableListOf()
                    taskEventList.add(taskEvent)
                    sessionManager.saveUpdatedAtTimeStamp(eventData.taskUpdatedAt)

                    val taskHiddenLocalData =
                        TaskV2DaoHelper(taskDao).getTasks(TaskRootStateTags.Hidden.tagValue)
                    val taskToMeLocalData =
                        TaskV2DaoHelper(taskDao).getTasks(TaskRootStateTags.ToMe.tagValue)
                    val taskFromMeLocalData =
                        TaskV2DaoHelper(taskDao).getTasks(TaskRootStateTags.FromMe.tagValue)

                    val hiddenByCurrentUser = eventData.oldTaskData.isHiddenByMe
                    if (hiddenByCurrentUser) {
                        //it means task must be searched from hidden rootState and child states[ongoing and done] and then move the task to another root state from hidden

                        if (!TaskV2DaoHelper(taskDao).isTaskListEmpty(
                                TaskRootStateTags.Hidden.tagValue, taskHiddenLocalData
                            )
                        ) {
                            val ongoingTask =
                                taskHiddenLocalData.allTasks.ongoing.find { it.id == taskID }
                            val doneTask =
                                taskHiddenLocalData.allTasks.done.find { it.id == taskID }

                            if (ongoingTask != null) {
                                val allOngoingTaskList =
                                    taskHiddenLocalData.allTasks.ongoing.toMutableList()
                                val taskIndex = allOngoingTaskList.indexOf(ongoingTask)

//                                var oldEvents = ongoingTask.events.toMutableList()
//                                if (oldEvents.isNotEmpty()) {
//                                    val oldOnlyEvent = oldEvents.find { it.id == eventData.id }
//                                    if (oldOnlyEvent != null) {     //means event already exist, so replace it
//                                        val oldEventIndex = oldEvents.indexOf(oldOnlyEvent)
//                                        oldEvents[oldEventIndex] = taskEvent
//                                        ongoingTask.events = oldEvents
//                                    } else {
//                                        oldEvents.add(taskEvent)
//                                        ongoingTask.events = oldEvents
//                                    }
//                                } else {
//                                    oldEvents = taskEventList
//                                    ongoingTask.events = oldEvents
//                                }
                                ongoingTask.hiddenBy = listOf()
                                ongoingTask.seenBy = eventData.taskData.seenBy
                                ongoingTask.updatedAt = eventData.taskUpdatedAt
                                ongoingTask.creatorState = eventData.taskData.creatorState
                                val assignToList = ongoingTask.assignedToState
                                assignToList.map {
                                    it.state = eventData.taskData.creatorState
                                }     //as creator state is canceled, so all assignee will be canceled
                                ongoingTask.assignedToState = assignToList

                                allOngoingTaskList.removeAt(taskIndex)
                                taskHiddenLocalData.allTasks.ongoing = allOngoingTaskList

                                if (eventData.oldTaskData.isAssignedToMe) {

                                    val doneTaskToMe =
                                        taskToMeLocalData.allTasks.done.find { it.id == taskID }
                                    val allDoneToMeTaskList =
                                        taskToMeLocalData.allTasks.done.toMutableList()
                                    if (doneTaskToMe != null) {
                                        val index = allDoneToMeTaskList.indexOf(doneTaskToMe)
                                        allDoneToMeTaskList[index] = ongoingTask
                                        taskToMeLocalData.allTasks.done = allDoneToMeTaskList
                                    } else {
                                        allDoneToMeTaskList.add(0, ongoingTask)
                                        taskToMeLocalData.allTasks.done = allDoneToMeTaskList
                                    }
                                    sharedViewModel?.isToMeUnread?.postValue(true)
                                    sessionManager.saveToMeUnread(true)

                                }
                                if (eventData.oldTaskData.isCreator) {

                                    val doneTaskFromMe =
                                        taskFromMeLocalData.allTasks.done.find { it.id == taskID }
                                    val allDoneFromMeTaskList =
                                        taskFromMeLocalData.allTasks.done.toMutableList()
                                    if (doneTaskFromMe != null) {
                                        val index =
                                            allDoneFromMeTaskList.indexOf(doneTaskFromMe)
                                        allDoneFromMeTaskList[index] = ongoingTask
                                        taskFromMeLocalData.allTasks.done =
                                            allDoneFromMeTaskList
                                    } else {
                                        allDoneFromMeTaskList.add(0, ongoingTask)
                                        taskFromMeLocalData.allTasks.done = allDoneFromMeTaskList
                                    }

                                    sharedViewModel?.isFromMeUnread?.postValue(true)
                                    sessionManager.saveFromMeUnread(true)

                                }
                                updatedTask = ongoingTask

                            } else if (doneTask != null) {
                                val allDoneTaskList =
                                    taskHiddenLocalData.allTasks.done.toMutableList()
                                val taskIndex = allDoneTaskList.indexOf(doneTask)

//                                var oldEvents = doneTask.events.toMutableList()
//                                if (oldEvents.isNotEmpty()) {
//                                    val oldOnlyEvent = oldEvents.find { it.id == eventData.id }
//                                    if (oldOnlyEvent != null) {     //means event already exist, so replace it
//                                        val oldEventIndex = oldEvents.indexOf(oldOnlyEvent)
//                                        oldEvents[oldEventIndex] = taskEvent
//                                        doneTask.events = oldEvents
//                                    } else {
//                                        oldEvents.add(taskEvent)
//                                        doneTask.events = oldEvents
//                                    }
//                                } else {
//                                    oldEvents = taskEventList
//                                    doneTask.events = oldEvents
//                                }
                                doneTask.hiddenBy = listOf()
                                doneTask.seenBy = eventData.taskData.seenBy
                                doneTask.updatedAt = eventData.taskUpdatedAt
                                doneTask.creatorState = eventData.taskData.creatorState
                                val assignToList = doneTask.assignedToState
                                assignToList.map {
                                    it.state = eventData.taskData.creatorState
                                }     //as creator state is canceled, so all assignee will be canceled
                                doneTask.assignedToState = assignToList

                                allDoneTaskList.removeAt(taskIndex)
                                taskHiddenLocalData.allTasks.done = allDoneTaskList

                                if (eventData.oldTaskData.isAssignedToMe) {

                                    val doneTaskToMe =
                                        taskToMeLocalData.allTasks.done.find { it.id == taskID }
                                    val allDoneToMeTaskList =
                                        taskToMeLocalData.allTasks.done.toMutableList()
                                    if (doneTaskToMe != null) {
                                        val index = allDoneToMeTaskList.indexOf(doneTaskToMe)
                                        allDoneToMeTaskList[index] = doneTask
                                        taskToMeLocalData.allTasks.done = allDoneToMeTaskList
                                    } else {
                                        allDoneToMeTaskList.add(0, doneTask)
                                        taskToMeLocalData.allTasks.done = allDoneToMeTaskList
                                    }
                                    sharedViewModel?.isToMeUnread?.postValue(true)
                                    sessionManager.saveToMeUnread(true)

                                }
                                if (eventData.oldTaskData.isCreator) {

                                    val doneTaskFromMe =
                                        taskFromMeLocalData.allTasks.done.find { it.id == taskID }
                                    val allDoneFromMeTaskList =
                                        taskFromMeLocalData.allTasks.done.toMutableList()
                                    if (doneTaskFromMe != null) {
                                        val index =
                                            allDoneFromMeTaskList.indexOf(doneTaskFromMe)
                                        allDoneFromMeTaskList[index] = doneTask
                                        taskFromMeLocalData.allTasks.done =
                                            allDoneFromMeTaskList

                                    } else {
                                        allDoneFromMeTaskList.add(0, doneTask)
                                        taskFromMeLocalData.allTasks.done = allDoneFromMeTaskList
                                    }
                                    sharedViewModel?.isFromMeUnread?.postValue(true)
                                    sessionManager.saveFromMeUnread(true)

                                }
                                updatedTask = doneTask
                            }
                        }
                    }

                    if (eventData.oldTaskData.isAssignedToMe) {
                        if (!TaskV2DaoHelper(taskDao).isTaskListEmpty(
                                TaskRootStateTags.ToMe.tagValue, taskToMeLocalData
                            )
                        ) {
                            val newTask = taskToMeLocalData.allTasks.new.find { it.id == taskID }
                            val ongoingTask =
                                taskToMeLocalData.allTasks.ongoing.find { it.id == taskID }
                            val doneTask = taskToMeLocalData.allTasks.done.find { it.id == taskID }


                            if (newTask != null) {
                                val allTaskList = taskToMeLocalData.allTasks.new.toMutableList()
                                val taskIndex = allTaskList.indexOf(newTask)

//                                var oldEvents = newTask.events.toMutableList()
//                                if (oldEvents.isNotEmpty()) {
//                                    val oldOnlyEvent = oldEvents.find { it.id == eventData.id }
//                                    if (oldOnlyEvent != null) {     //means event already exist, so replace it
//                                        val oldEventIndex = oldEvents.indexOf(oldOnlyEvent)
//                                        oldEvents[oldEventIndex] = taskEvent
//                                        newTask.events = oldEvents
//                                    } else {
//                                        oldEvents.add(taskEvent)
//                                        newTask.events = oldEvents
//                                    }
//                                } else {
//                                    oldEvents = taskEventList
//                                    newTask.events = oldEvents
//                                }
                                newTask.hiddenBy = listOf()
                                newTask.seenBy = eventData.taskData.seenBy
                                newTask.updatedAt = eventData.taskUpdatedAt
                                newTask.creatorState = eventData.taskData.creatorState
                                val assignToList = newTask.assignedToState
                                assignToList.map {
                                    it.state = eventData.taskData.creatorState
                                }     //as creator state is done, so all assignee will be done
                                newTask.assignedToState = assignToList

                                allTaskList.removeAt(taskIndex)
                                taskToMeLocalData.allTasks.new = allTaskList

                                val doneTaskToMe =
                                    taskToMeLocalData.allTasks.done.find { it.id == taskID }
                                val allDoneToMeTaskList =
                                    taskToMeLocalData.allTasks.done.toMutableList()
                                if (doneTaskToMe != null) {
                                    val index = allDoneToMeTaskList.indexOf(doneTaskToMe)
                                    allDoneToMeTaskList[index] = newTask
                                    taskToMeLocalData.allTasks.done = allDoneToMeTaskList
                                } else {
                                    allDoneToMeTaskList.add(0, newTask)
                                    taskToMeLocalData.allTasks.done = allDoneToMeTaskList
                                }
                                updatedTask = newTask
                                sharedViewModel?.isToMeUnread?.postValue(true)
                                sessionManager.saveToMeUnread(true)

                            } else if (ongoingTask != null) {
                                val allTaskList = taskToMeLocalData.allTasks.ongoing.toMutableList()
                                val taskIndex = allTaskList.indexOf(ongoingTask)

//                                var oldEvents = ongoingTask.events.toMutableList()
//                                if (oldEvents.isNotEmpty()) {
//                                    val oldOnlyEvent = oldEvents.find { it.id == eventData.id }
//                                    if (oldOnlyEvent != null) {     //means event already exist, so replace it
//                                        val oldEventIndex = oldEvents.indexOf(oldOnlyEvent)
//                                        oldEvents[oldEventIndex] = taskEvent
//                                        ongoingTask.events = oldEvents
//                                    } else {
//                                        oldEvents.add(taskEvent)
//                                        ongoingTask.events = oldEvents
//                                    }
//                                } else {
//                                    oldEvents = taskEventList
//                                    ongoingTask.events = oldEvents
//                                }
                                ongoingTask.hiddenBy = listOf()
                                ongoingTask.seenBy = eventData.taskData.seenBy
                                ongoingTask.updatedAt = eventData.taskUpdatedAt
                                ongoingTask.creatorState = eventData.taskData.creatorState
                                val assignToList = ongoingTask.assignedToState
                                assignToList.map {
                                    it.state = eventData.taskData.creatorState
                                }     //as creator state is canceled, so all assignee will be canceled
                                ongoingTask.assignedToState = assignToList

                                allTaskList.removeAt(taskIndex)
                                taskToMeLocalData.allTasks.ongoing = allTaskList

                                val doneTaskToMe =
                                    taskToMeLocalData.allTasks.done.find { it.id == taskID }
                                val allDoneToMeTaskList =
                                    taskToMeLocalData.allTasks.done.toMutableList()
                                if (doneTaskToMe != null) {
                                    val index = allDoneToMeTaskList.indexOf(doneTaskToMe)
                                    allDoneToMeTaskList[index] = ongoingTask
                                    taskToMeLocalData.allTasks.done = allDoneToMeTaskList
                                } else {
                                    allDoneToMeTaskList.add(0, ongoingTask)
                                    taskToMeLocalData.allTasks.done = allDoneToMeTaskList
                                }
                                updatedTask = ongoingTask
                                sharedViewModel?.isToMeUnread?.postValue(true)
                                sessionManager.saveToMeUnread(true)

                            } else if (doneTask != null) {
                                val allTaskList = taskToMeLocalData.allTasks.done.toMutableList()
                                val taskIndex = allTaskList.indexOf(doneTask)

//                                var oldEvents = doneTask.events.toMutableList()
//                                if (oldEvents.isNotEmpty()) {
//                                    val oldOnlyEvent = oldEvents.find { it.id == eventData.id }
//                                    if (oldOnlyEvent != null) {     //means event already exist, so replace it
//                                        val oldEventIndex = oldEvents.indexOf(oldOnlyEvent)
//                                        oldEvents[oldEventIndex] = taskEvent
//                                        doneTask.events = oldEvents
//                                    } else {
//                                        oldEvents.add(taskEvent)
//                                        doneTask.events = oldEvents
//                                    }
//                                } else {
//                                    oldEvents = taskEventList
//                                    doneTask.events = oldEvents
//                                }
                                doneTask.hiddenBy = listOf()
                                doneTask.seenBy = eventData.taskData.seenBy
                                doneTask.updatedAt = eventData.taskUpdatedAt
                                doneTask.creatorState = eventData.taskData.creatorState
                                val assignToList = doneTask.assignedToState
                                assignToList.map {
                                    it.state = eventData.taskData.creatorState
                                }     //as creator state is canceled, so all assignee will be canceled
                                doneTask.assignedToState = assignToList

                                allTaskList[taskIndex] = doneTask
                                taskToMeLocalData.allTasks.done = allTaskList

                                updatedTask = doneTask
                                sharedViewModel?.isToMeUnread?.postValue(true)
                                sessionManager.saveToMeUnread(true)
                            }
                        }
                    }

                    if (eventData.oldTaskData.isCreator) {
                        if (!TaskV2DaoHelper(taskDao).isTaskListEmpty(
                                TaskRootStateTags.FromMe.tagValue, taskFromMeLocalData
                            )
                        ) {
                            val unreadTask =
                                taskFromMeLocalData.allTasks.unread.find { it.id == taskID }
                            val ongoingTask =
                                taskFromMeLocalData.allTasks.ongoing.find { it.id == taskID }
                            val doneTask =
                                taskFromMeLocalData.allTasks.done.find { it.id == taskID }


                            if (unreadTask != null) {
                                val allTaskList =
                                    taskFromMeLocalData.allTasks.unread.toMutableList()
                                val taskIndex = allTaskList.indexOf(unreadTask)

//                                var oldEvents = unreadTask.events.toMutableList()
//                                if (oldEvents.isNotEmpty()) {
//                                    val oldOnlyEvent = oldEvents.find { it.id == eventData.id }
//                                    if (oldOnlyEvent != null) {     //means event already exist, so replace it
//                                        val oldEventIndex = oldEvents.indexOf(oldOnlyEvent)
//                                        oldEvents[oldEventIndex] = taskEvent
//                                        unreadTask.events = oldEvents
//                                    } else {
//                                        oldEvents.add(taskEvent)
//                                        unreadTask.events = oldEvents
//                                    }
//                                } else {
//                                    oldEvents = taskEventList
//                                    unreadTask.events = oldEvents
//                                }
                                unreadTask.hiddenBy = listOf()
                                unreadTask.seenBy = eventData.taskData.seenBy
                                unreadTask.updatedAt = eventData.taskUpdatedAt
                                unreadTask.creatorState = eventData.taskData.creatorState
                                val assignToList = unreadTask.assignedToState
                                assignToList.map {
                                    it.state = eventData.taskData.creatorState
                                }     //as creator state is canceled, so all assignee will be canceled
                                unreadTask.assignedToState = assignToList

                                allTaskList.removeAt(taskIndex)
                                taskFromMeLocalData.allTasks.unread = allTaskList

                                val doneTaskFromMe =
                                    taskFromMeLocalData.allTasks.done.find { it.id == taskID }
                                val allDoneFromMeTaskList =
                                    taskFromMeLocalData.allTasks.done.toMutableList()
                                if (doneTaskFromMe != null) {
                                    val index = allDoneFromMeTaskList.indexOf(doneTaskFromMe)
                                    allDoneFromMeTaskList[index] = unreadTask
                                    taskFromMeLocalData.allTasks.done = allDoneFromMeTaskList
                                } else {
                                    allDoneFromMeTaskList.add(0, unreadTask)
                                    taskFromMeLocalData.allTasks.done = allDoneFromMeTaskList
                                }
                                updatedTask = unreadTask
                                sharedViewModel?.isFromMeUnread?.postValue(true)
                                sessionManager.saveFromMeUnread(true)

                            } else if (ongoingTask != null) {
                                val allTaskList =
                                    taskFromMeLocalData.allTasks.ongoing.toMutableList()
                                val taskIndex = allTaskList.indexOf(ongoingTask)

//                                var oldEvents = ongoingTask.events.toMutableList()
//                                if (oldEvents.isNotEmpty()) {
//                                    val oldOnlyEvent = oldEvents.find { it.id == eventData.id }
//                                    if (oldOnlyEvent != null) {     //means event already exist, so replace it
//                                        val oldEventIndex = oldEvents.indexOf(oldOnlyEvent)
//                                        oldEvents[oldEventIndex] = taskEvent
//                                        ongoingTask.events = oldEvents
//                                    } else {
//                                        oldEvents.add(taskEvent)
//                                        ongoingTask.events = oldEvents
//                                    }
//                                } else {
//                                    oldEvents = taskEventList
//                                    ongoingTask.events = oldEvents
//                                }
                                ongoingTask.hiddenBy = listOf()
                                ongoingTask.seenBy = eventData.taskData.seenBy
                                ongoingTask.updatedAt = eventData.taskUpdatedAt
                                ongoingTask.creatorState = eventData.taskData.creatorState
                                val assignToList = ongoingTask.assignedToState
                                assignToList.map {
                                    it.state = eventData.taskData.creatorState
                                }     //as creator state is canceled, so all assignee will be canceled
                                ongoingTask.assignedToState = assignToList

                                allTaskList.removeAt(taskIndex)
                                taskFromMeLocalData.allTasks.ongoing = allTaskList

                                val doneTaskFromMe =
                                    taskFromMeLocalData.allTasks.done.find { it.id == taskID }
                                val allDoneFromMeTaskList =
                                    taskFromMeLocalData.allTasks.done.toMutableList()
                                if (doneTaskFromMe != null) {
                                    val index = allDoneFromMeTaskList.indexOf(doneTaskFromMe)
                                    allDoneFromMeTaskList[index] = ongoingTask
                                    taskFromMeLocalData.allTasks.done = allDoneFromMeTaskList
                                } else {
                                    allDoneFromMeTaskList.add(0, ongoingTask)
                                    taskFromMeLocalData.allTasks.done = allDoneFromMeTaskList
                                }
                                updatedTask = ongoingTask
                                sharedViewModel?.isFromMeUnread?.postValue(true)
                                sessionManager.saveFromMeUnread(true)

                            } else if (doneTask != null) {
                                val allTaskList = taskFromMeLocalData.allTasks.done.toMutableList()
                                val taskIndex = allTaskList.indexOf(doneTask)

//                                var oldEvents = doneTask.events.toMutableList()
//                                if (oldEvents.isNotEmpty()) {
//                                    val oldOnlyEvent = oldEvents.find { it.id == eventData.id }
//                                    if (oldOnlyEvent != null) {     //means event already exist, so replace it
//                                        val oldEventIndex = oldEvents.indexOf(oldOnlyEvent)
//                                        oldEvents[oldEventIndex] = taskEvent
//                                        doneTask.events = oldEvents
//                                    } else {
//                                        oldEvents.add(taskEvent)
//                                        doneTask.events = oldEvents
//                                    }
//                                } else {
//                                    oldEvents = taskEventList
//                                    doneTask.events = oldEvents
//                                }
                                doneTask.hiddenBy = listOf()
                                doneTask.seenBy = eventData.taskData.seenBy
                                doneTask.updatedAt = eventData.taskUpdatedAt
                                doneTask.creatorState = eventData.taskData.creatorState
                                val assignToList = doneTask.assignedToState
                                assignToList.map {
                                    it.state = eventData.taskData.creatorState
                                }     //as creator state is canceled, so all assignee will be canceled
                                doneTask.assignedToState = assignToList

                                allTaskList[taskIndex] = doneTask
                                taskFromMeLocalData.allTasks.done = allTaskList

                                updatedTask = doneTask
                                sharedViewModel?.isFromMeUnread?.postValue(true)
                                sessionManager.saveFromMeUnread(true)
                            }
                        }
                    }

                    TaskV2DaoHelper(taskDao).insertTaskData(
                        taskHiddenLocalData
                    )
                    TaskV2DaoHelper(taskDao).insertTaskData(
                        taskToMeLocalData
                    )
                    TaskV2DaoHelper(taskDao).insertTaskData(
                        taskFromMeLocalData
                    )

                    // send task data for ui update
                    EventBus.getDefault()
                        .post(LocalEvents.TaskDoneEvent(updatedTask, taskEvent))
                    EventBus.getDefault().post(LocalEvents.RefreshTasksEvent())

                    TaskEventsList.removeEvent(
                        SocketHandler.TaskEvent.TASK_DONE.name, eventData.taskId
                    )
                }
            }
        }
        return updatedTask
    }

    fun updateTaskJoinedInLocal(
        eventData: EventV2Response.Data?, taskDao: TaskV2Dao, sessionManager: SessionManager
    ) {
        launch {
            var updatedTask: CeibroTaskV2? = null
            val sharedViewModel = NavHostPresenterActivity.activityInstance?.let {
                ViewModelProvider(it).get(SharedViewModel::class.java)
            }

            if (eventData != null) {
                val taskID = eventData.taskId
                val taskEvent = Events(
                    id = eventData.id,
                    taskId = eventData.taskId,
                    eventType = eventData.eventType,
                    initiator = eventData.initiator,
                    eventData = eventData.eventData,
                    commentData = eventData.commentData,
                    createdAt = eventData.createdAt,
                    updatedAt = eventData.updatedAt,
                    invitedMembers = eventData.invitedMembers
                )
                val taskEventList: MutableList<Events> = mutableListOf()
                taskEventList.add(taskEvent)
                sessionManager.saveUpdatedAtTimeStamp(eventData.taskUpdatedAt)

                if (eventData.oldTaskData.isAssignedToMe) {
                    val taskToMeLocalData =
                        TaskV2DaoHelper(taskDao).getTasks(TaskRootStateTags.ToMe.tagValue)
                    taskToMeLocalData?.let {
                        val ongoingTask =
                            taskToMeLocalData.allTasks.ongoing.find { it.id == taskID }
                        val newTask = taskToMeLocalData.allTasks.new.find { it.id == taskID }
                        val doneTask = taskToMeLocalData.allTasks.done.find { it.id == taskID }
                        if (ongoingTask != null) {
                            val index = taskToMeLocalData.allTasks.ongoing.indexOf(ongoingTask)

//                            var oldEvents = ongoingTask.events.toMutableList()
//                            if (oldEvents.isNotEmpty()) {
//                                val oldOnlyEvent = oldEvents.find { it.id == eventData.id }
//                                if (oldOnlyEvent != null) {     //means event already exist, so replace it
//                                    val oldEventIndex = oldEvents.indexOf(oldOnlyEvent)
//                                    oldEvents[oldEventIndex] = taskEvent
//                                    ongoingTask.events = oldEvents
//                                } else {
//                                    oldEvents.add(taskEvent)
//                                    ongoingTask.events = oldEvents
//                                }
//                            } else {
//                                oldEvents = taskEventList
//                                ongoingTask.events = oldEvents
//                            }
                            ongoingTask.hiddenBy = listOf()
                            ongoingTask.seenBy = eventData.taskData.seenBy
                            ongoingTask.creatorState = eventData.taskData.creatorState
                            ongoingTask.updatedAt = eventData.updatedAt

                            val invitedList = ongoingTask.invitedNumbers.toMutableList()
                            val invited =
                                invitedList.find { it.phoneNumber == eventData.initiator.phoneNumber }
                            if (invited != null) {
                                invitedList.remove(invited)
                                ongoingTask.invitedNumbers = invitedList
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
                            val assignToList = ongoingTask.assignedToState
                            val findAssignee = assignToList.find { it.userId == assignee.userId }
                            if (findAssignee == null) {
                                assignToList.add(assignee)
                            }
                            ongoingTask.assignedToState = assignToList

                            val toUpdateOngoingList =
                                taskToMeLocalData.allTasks.ongoing.toMutableList()
                            toUpdateOngoingList[index] = ongoingTask
                            taskToMeLocalData.allTasks.ongoing = toUpdateOngoingList
                            updatedTask = ongoingTask

                        } else if (newTask != null) {
                            val index = taskToMeLocalData.allTasks.new.indexOf(newTask)

//                            var oldEvents = newTask.events.toMutableList()
//                            if (oldEvents.isNotEmpty()) {
//                                val oldOnlyEvent = oldEvents.find { it.id == eventData.id }
//                                if (oldOnlyEvent != null) {     //means event already exist, so replace it
//                                    val oldEventIndex = oldEvents.indexOf(oldOnlyEvent)
//                                    oldEvents[oldEventIndex] = taskEvent
//                                    newTask.events = oldEvents
//                                } else {
//                                    oldEvents.add(taskEvent)
//                                    newTask.events = oldEvents
//                                }
//                            } else {
//                                oldEvents = taskEventList
//                                newTask.events = oldEvents
//                            }
                            newTask.hiddenBy = listOf()
                            newTask.seenBy = eventData.taskData.seenBy
                            newTask.creatorState = eventData.taskData.creatorState
                            newTask.updatedAt = eventData.updatedAt

                            val invitedList = newTask.invitedNumbers.toMutableList()
                            val invited =
                                invitedList.find { it.phoneNumber == eventData.initiator.phoneNumber }
                            if (invited != null) {
                                invitedList.remove(invited)
                                newTask.invitedNumbers = invitedList
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
                            val assignToList = newTask.assignedToState
                            val findAssignee = assignToList.find { it.userId == assignee.userId }
                            if (findAssignee == null) {
                                assignToList.add(assignee)
                            }
                            newTask.assignedToState = assignToList

                            val toUpdateNewList = taskToMeLocalData.allTasks.new.toMutableList()
                            toUpdateNewList[index] = newTask
                            taskToMeLocalData.allTasks.new = toUpdateNewList
                            updatedTask = newTask

                        } else if (doneTask != null) {
                            val index = taskToMeLocalData.allTasks.done.indexOf(doneTask)

//                            var oldEvents = doneTask.events.toMutableList()
//                            if (oldEvents.isNotEmpty()) {
//                                val oldOnlyEvent = oldEvents.find { it.id == eventData.id }
//                                if (oldOnlyEvent != null) {     //means event already exist, so replace it
//                                    val oldEventIndex = oldEvents.indexOf(oldOnlyEvent)
//                                    oldEvents[oldEventIndex] = taskEvent
//                                    doneTask.events = oldEvents
//                                } else {
//                                    oldEvents.add(taskEvent)
//                                    doneTask.events = oldEvents
//                                }
//                            } else {
//                                oldEvents = taskEventList
//                                doneTask.events = oldEvents
//                            }
                            doneTask.hiddenBy = listOf()
                            doneTask.seenBy = eventData.taskData.seenBy
                            doneTask.creatorState = eventData.taskData.creatorState
                            doneTask.updatedAt = eventData.updatedAt

                            val invitedList = doneTask.invitedNumbers.toMutableList()
                            val invited =
                                invitedList.find { it.phoneNumber == eventData.initiator.phoneNumber }
                            if (invited != null) {
                                invitedList.remove(invited)
                                doneTask.invitedNumbers = invitedList
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
                            val assignToList = doneTask.assignedToState
                            val findAssignee = assignToList.find { it.userId == assignee.userId }
                            if (findAssignee == null) {
                                assignToList.add(assignee)
                            }
                            doneTask.assignedToState = assignToList

                            val toUpdateDoneList = taskToMeLocalData.allTasks.done.toMutableList()
                            toUpdateDoneList[index] = doneTask
                            taskToMeLocalData.allTasks.done = toUpdateDoneList
                            updatedTask = doneTask
                        }
                        TaskV2DaoHelper(taskDao).insertTaskData(taskToMeLocalData)
                    }
                }

                if (eventData.oldTaskData.isCreator) {
                    val taskFromMeLocalData =
                        TaskV2DaoHelper(taskDao).getTasks(TaskRootStateTags.FromMe.tagValue)
                    taskFromMeLocalData?.let {
                        val unread = taskFromMeLocalData.allTasks.unread.find { it.id == taskID }
                        val ongoingTask =
                            taskFromMeLocalData.allTasks.ongoing.find { it.id == taskID }
                        val doneTask = taskFromMeLocalData.allTasks.done.find { it.id == taskID }

                        if (unread != null) {
                            val index = taskFromMeLocalData.allTasks.unread.indexOf(unread)

//                            var oldEvents = unread.events.toMutableList()
//                            if (oldEvents.isNotEmpty()) {
//                                val oldOnlyEvent = oldEvents.find { it.id == eventData.id }
//                                if (oldOnlyEvent != null) {     //means event already exist, so replace it
//                                    val oldEventIndex = oldEvents.indexOf(oldOnlyEvent)
//                                    oldEvents[oldEventIndex] = taskEvent
//                                    unread.events = oldEvents
//                                } else {
//                                    oldEvents.add(taskEvent)
//                                    unread.events = oldEvents
//                                }
//                            } else {
//                                oldEvents = taskEventList
//                                unread.events = oldEvents
//                            }
                            unread.hiddenBy = listOf()
                            unread.seenBy = eventData.taskData.seenBy
                            unread.creatorState = eventData.taskData.creatorState
                            unread.updatedAt = eventData.updatedAt

                            val invitedList = unread.invitedNumbers.toMutableList()
                            val invited =
                                invitedList.find { it.phoneNumber == eventData.initiator.phoneNumber }
                            if (invited != null) {
                                invitedList.remove(invited)
                                unread.invitedNumbers = invitedList
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
                            val assignToList = unread.assignedToState
                            val findAssignee = assignToList.find { it.userId == assignee.userId }
                            if (findAssignee == null) {
                                assignToList.add(assignee)
                            }
                            unread.assignedToState = assignToList

                            val toUpdateUnreadList =
                                taskFromMeLocalData.allTasks.unread.toMutableList()
                            toUpdateUnreadList[index] = unread
                            taskFromMeLocalData.allTasks.unread = toUpdateUnreadList
                            updatedTask = unread

                        } else if (ongoingTask != null) {
                            val index = taskFromMeLocalData.allTasks.ongoing.indexOf(ongoingTask)

//                            var oldEvents = ongoingTask.events.toMutableList()
//                            if (oldEvents.isNotEmpty()) {
//                                val oldOnlyEvent = oldEvents.find { it.id == eventData.id }
//                                if (oldOnlyEvent != null) {     //means event already exist, so replace it
//                                    val oldEventIndex = oldEvents.indexOf(oldOnlyEvent)
//                                    oldEvents[oldEventIndex] = taskEvent
//                                    ongoingTask.events = oldEvents
//                                } else {
//                                    oldEvents.add(taskEvent)
//                                    ongoingTask.events = oldEvents
//                                }
//                            } else {
//                                oldEvents = taskEventList
//                                ongoingTask.events = oldEvents
//                            }
                            ongoingTask.hiddenBy = listOf()
                            ongoingTask.seenBy = eventData.taskData.seenBy
                            ongoingTask.creatorState = eventData.taskData.creatorState
                            ongoingTask.updatedAt = eventData.updatedAt

                            val invitedList = ongoingTask.invitedNumbers.toMutableList()
                            val invited =
                                invitedList.find { it.phoneNumber == eventData.initiator.phoneNumber }
                            if (invited != null) {
                                invitedList.remove(invited)
                                ongoingTask.invitedNumbers = invitedList
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
                            val assignToList = ongoingTask.assignedToState
                            val findAssignee = assignToList.find { it.userId == assignee.userId }
                            if (findAssignee == null) {
                                assignToList.add(assignee)
                            }
                            ongoingTask.assignedToState = assignToList

                            val toUpdateOngoingList =
                                taskFromMeLocalData.allTasks.ongoing.toMutableList()
                            toUpdateOngoingList[index] = ongoingTask
                            taskFromMeLocalData.allTasks.ongoing = toUpdateOngoingList
                            updatedTask = ongoingTask

                        } else if (doneTask != null) {
                            val index = taskFromMeLocalData.allTasks.done.indexOf(doneTask)

//                            var oldEvents = doneTask.events.toMutableList()
//                            if (oldEvents.isNotEmpty()) {
//                                val oldOnlyEvent = oldEvents.find { it.id == eventData.id }
//                                if (oldOnlyEvent != null) {     //means event already exist, so replace it
//                                    val oldEventIndex = oldEvents.indexOf(oldOnlyEvent)
//                                    oldEvents[oldEventIndex] = taskEvent
//                                    doneTask.events = oldEvents
//                                } else {
//                                    oldEvents.add(taskEvent)
//                                    doneTask.events = oldEvents
//                                }
//                            } else {
//                                oldEvents = taskEventList
//                                doneTask.events = oldEvents
//                            }
                            doneTask.hiddenBy = listOf()
                            doneTask.seenBy = eventData.taskData.seenBy
                            doneTask.creatorState = eventData.taskData.creatorState
                            doneTask.updatedAt = eventData.updatedAt

                            val invitedList = doneTask.invitedNumbers.toMutableList()
                            val invited =
                                invitedList.find { it.phoneNumber == eventData.initiator.phoneNumber }
                            if (invited != null) {
                                invitedList.remove(invited)
                                doneTask.invitedNumbers = invitedList
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
                            val assignToList = doneTask.assignedToState
                            val findAssignee = assignToList.find { it.userId == assignee.userId }
                            if (findAssignee == null) {
                                assignToList.add(assignee)
                            }
                            doneTask.assignedToState = assignToList

                            val toUpdateDoneList = taskFromMeLocalData.allTasks.done.toMutableList()
                            toUpdateDoneList[index] = doneTask
                            taskFromMeLocalData.allTasks.done = toUpdateDoneList
                            updatedTask = doneTask

                        }
                        TaskV2DaoHelper(taskDao).insertTaskData(taskFromMeLocalData)
                    }
                }

                if (eventData.oldTaskData.isHiddenByMe) {
                    val taskHiddenLocalData =
                        TaskV2DaoHelper(taskDao).getTasks(TaskRootStateTags.Hidden.tagValue)
                    taskHiddenLocalData?.let {
                        val taskToMeLocalData =
                            TaskV2DaoHelper(taskDao).getTasks(TaskRootStateTags.ToMe.tagValue)

                        if (!TaskV2DaoHelper(taskDao).isTaskListEmpty(
                                TaskRootStateTags.ToMe.tagValue, taskToMeLocalData
                            )
                        ) {
                            val ongoingTask =
                                taskHiddenLocalData.allTasks.ongoing.find { it.id == taskID }
                            val doneTask =
                                taskHiddenLocalData.allTasks.done.find { it.id == taskID }

                            if (ongoingTask != null) {
                                val toUpdateOngoingList =
                                    taskHiddenLocalData.allTasks.ongoing.toMutableList()
                                val index = toUpdateOngoingList.indexOf(ongoingTask)

//                                var oldEvents = ongoingTask.events.toMutableList()
//                                if (oldEvents.isNotEmpty()) {
//                                    val oldOnlyEvent = oldEvents.find { it.id == eventData.id }
//                                    if (oldOnlyEvent != null) {     //means event already exist, so replace it
//                                        val oldEventIndex = oldEvents.indexOf(oldOnlyEvent)
//                                        oldEvents[oldEventIndex] = taskEvent
//                                        ongoingTask.events = oldEvents
//                                    } else {
//                                        oldEvents.add(taskEvent)
//                                        ongoingTask.events = oldEvents
//                                    }
//                                } else {
//                                    oldEvents = taskEventList
//                                    ongoingTask.events = oldEvents
//                                }
                                ongoingTask.hiddenBy = listOf()
                                ongoingTask.seenBy = eventData.taskData.seenBy
                                ongoingTask.creatorState = eventData.taskData.creatorState
                                ongoingTask.updatedAt = eventData.updatedAt

                                val invitedList = ongoingTask.invitedNumbers.toMutableList()
                                val invited =
                                    invitedList.find { it.phoneNumber == eventData.initiator.phoneNumber }
                                if (invited != null) {
                                    invitedList.remove(invited)
                                    ongoingTask.invitedNumbers = invitedList
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
                                val assignToList = ongoingTask.assignedToState
                                val findAssignee =
                                    assignToList.find { it.userId == assignee.userId }
                                if (findAssignee == null) {
                                    assignToList.add(assignee)
                                }
                                ongoingTask.assignedToState = assignToList

                                toUpdateOngoingList.removeAt(index)
                                taskHiddenLocalData.allTasks.ongoing = toUpdateOngoingList

                                if (eventData.oldTaskData.isAssignedToMe) {
                                    val ongoingTaskToMe =
                                        taskToMeLocalData.allTasks.ongoing.find { it.id == taskID }
                                    val allOngoingToMeTaskList =
                                        taskToMeLocalData.allTasks.ongoing.toMutableList()
                                    if (ongoingTaskToMe != null) {
                                        val index2 = allOngoingToMeTaskList.indexOf(ongoingTaskToMe)
                                        allOngoingToMeTaskList[index2] = ongoingTask
                                        taskToMeLocalData.allTasks.ongoing = allOngoingToMeTaskList
                                    } else {
                                        allOngoingToMeTaskList.add(0, ongoingTask)
                                        taskToMeLocalData.allTasks.ongoing = allOngoingToMeTaskList
                                    }
                                    sharedViewModel?.isToMeUnread?.value = true
                                    sessionManager.saveToMeUnread(true)
                                }
                                updatedTask = ongoingTask

                            } else if (doneTask != null) {
                                val toUpdateDoneList =
                                    taskHiddenLocalData.allTasks.done.toMutableList()
                                val index = taskHiddenLocalData.allTasks.done.indexOf(doneTask)

//                                var oldEvents = doneTask.events.toMutableList()
//                                if (oldEvents.isNotEmpty()) {
//                                    val oldOnlyEvent = oldEvents.find { it.id == eventData.id }
//                                    if (oldOnlyEvent != null) {     //means event already exist, so replace it
//                                        val oldEventIndex = oldEvents.indexOf(oldOnlyEvent)
//                                        oldEvents[oldEventIndex] = taskEvent
//                                        doneTask.events = oldEvents
//                                    } else {
//                                        oldEvents.add(taskEvent)
//                                        doneTask.events = oldEvents
//                                    }
//                                } else {
//                                    oldEvents = taskEventList
//                                    doneTask.events = oldEvents
//                                }
                                doneTask.hiddenBy = listOf()
                                doneTask.seenBy = eventData.taskData.seenBy
                                doneTask.creatorState = eventData.taskData.creatorState
                                doneTask.updatedAt = eventData.updatedAt

                                val invitedList = doneTask.invitedNumbers.toMutableList()
                                val invited =
                                    invitedList.find { it.phoneNumber == eventData.initiator.phoneNumber }
                                if (invited != null) {
                                    invitedList.remove(invited)
                                    doneTask.invitedNumbers = invitedList
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
                                val assignToList = doneTask.assignedToState
                                val findAssignee =
                                    assignToList.find { it.userId == assignee.userId }
                                if (findAssignee == null) {
                                    assignToList.add(assignee)
                                }
                                doneTask.assignedToState = assignToList

                                toUpdateDoneList.removeAt(index)
                                taskHiddenLocalData.allTasks.done = toUpdateDoneList

                                if (eventData.oldTaskData.isAssignedToMe) {
                                    val doneTaskToMe =
                                        taskToMeLocalData.allTasks.done.find { it.id == taskID }
                                    val allDoneToMeTaskList =
                                        taskToMeLocalData.allTasks.done.toMutableList()
                                    if (doneTaskToMe != null) {
                                        val index2 = allDoneToMeTaskList.indexOf(doneTaskToMe)
                                        allDoneToMeTaskList[index2] = doneTask
                                        taskToMeLocalData.allTasks.done = allDoneToMeTaskList
                                    } else {
                                        allDoneToMeTaskList.add(0, doneTask)
                                        taskToMeLocalData.allTasks.done = allDoneToMeTaskList
                                    }
                                    sharedViewModel?.isToMeUnread?.value = true
                                    sessionManager.saveToMeUnread(true)
                                }
                                updatedTask = doneTask
                            }

                            TaskV2DaoHelper(taskDao).insertTaskData(taskToMeLocalData)
                        }
                        TaskV2DaoHelper(taskDao).insertTaskData(taskHiddenLocalData)
                    }
                }

                if (eventData.oldTaskData.creatorState.equals(TaskStatus.CANCELED.name, true)) {
                    val taskHiddenLocalData =
                        TaskV2DaoHelper(taskDao).getTasks(TaskRootStateTags.Hidden.tagValue)
                    taskHiddenLocalData?.let {
                        val canceled =
                            taskHiddenLocalData.allTasks.canceled.find { it.id == taskID }
                        if (canceled != null) {
                            val index = taskHiddenLocalData.allTasks.canceled.indexOf(canceled)
//                            var oldEvents = canceled.events.toMutableList()
//                            if (oldEvents.isNotEmpty()) {
//                                val oldOnlyEvent = oldEvents.find { it.id == eventData.id }
//                                if (oldOnlyEvent != null) {     //means event already exist, so replace it
//                                    val oldEventIndex = oldEvents.indexOf(oldOnlyEvent)
//                                    oldEvents[oldEventIndex] = taskEvent
//                                    canceled.events = oldEvents
//                                } else {
//                                    oldEvents.add(taskEvent)
//                                    canceled.events = oldEvents
//                                }
//                            } else {
//                                oldEvents = taskEventList
//                                canceled.events = oldEvents
//                            }
                            canceled.hiddenBy = listOf()
                            canceled.seenBy = eventData.taskData.seenBy
                            canceled.creatorState = eventData.taskData.creatorState
                            canceled.updatedAt = eventData.updatedAt

                            val invitedList = canceled.invitedNumbers.toMutableList()
                            val invited =
                                invitedList.find { it.phoneNumber == eventData.initiator.phoneNumber }
                            if (invited != null) {
                                invitedList.remove(invited)
                                canceled.invitedNumbers = invitedList
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
                            val assignToList = canceled.assignedToState
                            val findAssignee = assignToList.find { it.userId == assignee.userId }
                            if (findAssignee == null) {
                                assignToList.add(assignee)
                            }
                            canceled.assignedToState = assignToList

                            val toUpdateCancelledList =
                                taskHiddenLocalData.allTasks.canceled.toMutableList()
                            toUpdateCancelledList[index] = canceled
                            taskHiddenLocalData.allTasks.canceled = toUpdateCancelledList
                            updatedTask = canceled
                        }

                        TaskV2DaoHelper(taskDao).insertTaskData(taskHiddenLocalData)
                    }
                }

                EventBus.getDefault().post(LocalEvents.TaskForwardEvent(updatedTask))
                EventBus.getDefault().post(LocalEvents.RefreshTasksEvent())
            }
        }
    }


    fun updateTaskHideInLocal(
        hideData: HideTaskResponse?, taskDao: TaskV2Dao, sessionManager: SessionManager
    ) {
        launch {
            if (hideData != null) {
                val isExists = TaskEventsList.isExists(
                    SocketHandler.TaskEvent.TASK_HIDDEN.name, hideData.taskId, true
                )
                if (!isExists) {
                    val taskToMeLocalData =
                        TaskV2DaoHelper(taskDao).getTasks(TaskRootStateTags.ToMe.tagValue)
                    val taskHiddenLocalData =
                        TaskV2DaoHelper(taskDao).getTasks(TaskRootStateTags.Hidden.tagValue)

                    sessionManager.saveUpdatedAtTimeStamp(hideData.updatedAt)

                    if (!TaskV2DaoHelper(taskDao).isTaskListEmpty(
                            TaskRootStateTags.ToMe.tagValue, taskToMeLocalData
                        )
                    ) {
                        val ongoingTask =
                            taskToMeLocalData.allTasks.ongoing.find { it.id == hideData.taskId }
                        val doneTask =
                            taskToMeLocalData.allTasks.done.find { it.id == hideData.taskId }


                        if (ongoingTask != null) {
                            val allTaskList = taskToMeLocalData.allTasks.ongoing.toMutableList()
                            val taskIndex = allTaskList.indexOf(ongoingTask)

                            ongoingTask.hiddenBy = hideData.hiddenBy
                            ongoingTask.updatedAt = hideData.updatedAt

                            allTaskList.removeAt(taskIndex)
                            taskToMeLocalData.allTasks.ongoing = allTaskList


                            val allOngoingTaskList =
                                taskHiddenLocalData.allTasks.ongoing.toMutableList()
                            val hiddenOngoingTask =
                                taskHiddenLocalData.allTasks.ongoing.find { it.id == hideData.taskId }
                            if (hiddenOngoingTask != null) {

                                val ongoingTaskIndex =
                                    allOngoingTaskList.indexOf(hiddenOngoingTask)
                                allOngoingTaskList[ongoingTaskIndex] = ongoingTask
                                taskHiddenLocalData.allTasks.ongoing = allOngoingTaskList
                            } else {
                                allOngoingTaskList.add(0, ongoingTask)
                                taskHiddenLocalData.allTasks.ongoing = allOngoingTaskList
                            }
                        }
                        if (doneTask != null) {
                            val allTaskList = taskToMeLocalData.allTasks.done.toMutableList()
                            val taskIndex = allTaskList.indexOf(doneTask)

                            doneTask.hiddenBy = hideData.hiddenBy
                            doneTask.updatedAt = hideData.updatedAt

                            allTaskList.removeAt(taskIndex)
                            taskToMeLocalData.allTasks.done = allTaskList


                            val allDoneTaskList =
                                taskHiddenLocalData.allTasks.done.toMutableList()
                            val hiddenDoneTask =
                                taskHiddenLocalData.allTasks.done.find { it.id == hideData.taskId }
                            if (hiddenDoneTask != null) {
                                val doneTaskIndex = allDoneTaskList.indexOf(hiddenDoneTask)
                                allDoneTaskList[doneTaskIndex] = doneTask
                                taskHiddenLocalData.allTasks.done = allDoneTaskList
                            } else {
                                allDoneTaskList.add(0, doneTask)
                                taskHiddenLocalData.allTasks.done = allDoneTaskList
                            }
                        }

                    }

                    if (!TaskV2DaoHelper(taskDao).isTaskListEmpty(
                            TaskRootStateTags.Hidden.tagValue, taskHiddenLocalData
                        )
                    ) {
                        val ongoingTask =
                            taskHiddenLocalData.allTasks.ongoing.find { it.id == hideData.taskId }
                        val doneTask =
                            taskHiddenLocalData.allTasks.done.find { it.id == hideData.taskId }

                        if (ongoingTask != null) {
                            val allTaskList = taskHiddenLocalData.allTasks.ongoing.toMutableList()
                            val taskIndex = allTaskList.indexOf(ongoingTask)

                            ongoingTask.hiddenBy = hideData.hiddenBy
                            ongoingTask.updatedAt = hideData.updatedAt

                            allTaskList[taskIndex] = ongoingTask
                            taskHiddenLocalData.allTasks.ongoing = allTaskList
                        }
                        if (doneTask != null) {
                            val allTaskList = taskHiddenLocalData.allTasks.done.toMutableList()
                            val taskIndex = allTaskList.indexOf(doneTask)

                            doneTask.hiddenBy = hideData.hiddenBy
                            doneTask.updatedAt = hideData.updatedAt

                            allTaskList[taskIndex] = doneTask
                            taskHiddenLocalData.allTasks.done = allTaskList
                        }
                    }

                    TaskV2DaoHelper(taskDao).insertTaskData(
                        taskToMeLocalData
                    )
                    TaskV2DaoHelper(taskDao).insertTaskData(
                        taskHiddenLocalData
                    )

                    EventBus.getDefault().post(LocalEvents.RefreshTasksEvent())

                    TaskEventsList.removeEvent(
                        SocketHandler.TaskEvent.TASK_HIDDEN.name, hideData.taskId
                    )
                }
            }
        }

    }

    fun updateTaskUnHideInLocal(
        hideData: HideTaskResponse?, taskDao: TaskV2Dao, sessionManager: SessionManager
    ) {
        launch {
            if (hideData != null) {

                val isExists = TaskEventsList.isExists(
                    SocketHandler.TaskEvent.TASK_SHOWN.name, hideData.taskId, true
                )

                if (!isExists) {
                    val taskToMeLocalData =
                        TaskV2DaoHelper(taskDao).getTasks(TaskRootStateTags.ToMe.tagValue)
                    val taskHiddenLocalData =
                        TaskV2DaoHelper(taskDao).getTasks(TaskRootStateTags.Hidden.tagValue)

                    sessionManager.saveUpdatedAtTimeStamp(hideData.updatedAt)

                    if (!TaskV2DaoHelper(taskDao).isTaskListEmpty(
                            TaskRootStateTags.Hidden.tagValue, taskHiddenLocalData
                        )
                    ) {
                        val ongoingTask =
                            taskHiddenLocalData.allTasks.ongoing.find { it.id == hideData.taskId }
                        val doneTask =
                            taskHiddenLocalData.allTasks.done.find { it.id == hideData.taskId }

                        if (ongoingTask != null) {
                            val allTaskList =
                                taskHiddenLocalData.allTasks.ongoing.toMutableList()
                            val taskIndex = allTaskList.indexOf(ongoingTask)

                            ongoingTask.hiddenBy = hideData.hiddenBy
                            ongoingTask.updatedAt = hideData.updatedAt

                            allTaskList.removeAt(taskIndex)
                            taskHiddenLocalData.allTasks.ongoing = allTaskList


                            val allOngoingTaskList =
                                taskToMeLocalData.allTasks.ongoing.toMutableList()
                            val toMeOngoingTask =
                                taskToMeLocalData.allTasks.ongoing.find { it.id == hideData.taskId }
                            if (toMeOngoingTask != null) {
                                val ongoingTaskIndex =
                                    allOngoingTaskList.indexOf(toMeOngoingTask)
                                allOngoingTaskList[ongoingTaskIndex] = ongoingTask
                                taskToMeLocalData.allTasks.ongoing = allOngoingTaskList
                            } else {
                                allOngoingTaskList.add(0, ongoingTask)
                                taskToMeLocalData.allTasks.ongoing = allOngoingTaskList
                            }
                        }
                        if (doneTask != null) {
                            val allTaskList =
                                taskHiddenLocalData.allTasks.done.toMutableList()
                            val taskIndex = allTaskList.indexOf(doneTask)

                            doneTask.hiddenBy = hideData.hiddenBy
                            doneTask.updatedAt = hideData.updatedAt

                            allTaskList.removeAt(taskIndex)
                            taskHiddenLocalData.allTasks.done = allTaskList


                            val allDoneTaskList =
                                taskToMeLocalData.allTasks.done.toMutableList()
                            val toMeDoneTask =
                                taskToMeLocalData.allTasks.done.find { it.id == hideData.taskId }
                            if (toMeDoneTask != null) {
                                val doneTaskIndex = allDoneTaskList.indexOf(toMeDoneTask)
                                allDoneTaskList[doneTaskIndex] = doneTask
                                taskToMeLocalData.allTasks.done = allDoneTaskList
                            } else {
                                allDoneTaskList.add(0, doneTask)
                                taskToMeLocalData.allTasks.done = allDoneTaskList
                            }
                        }

                    }

                    if (!TaskV2DaoHelper(taskDao).isTaskListEmpty(
                            TaskRootStateTags.ToMe.tagValue, taskToMeLocalData
                        )
                    ) {
                        val ongoingTask =
                            taskToMeLocalData.allTasks.ongoing.find { it.id == hideData.taskId }
                        val doneTask =
                            taskToMeLocalData.allTasks.done.find { it.id == hideData.taskId }

                        if (ongoingTask != null) {
                            val allTaskList = taskToMeLocalData.allTasks.ongoing.toMutableList()
                            val taskIndex = allTaskList.indexOf(ongoingTask)

                            ongoingTask.hiddenBy = hideData.hiddenBy
                            ongoingTask.updatedAt = hideData.updatedAt

                            allTaskList[taskIndex] = ongoingTask
                            taskToMeLocalData.allTasks.ongoing = allTaskList
                        }
                        if (doneTask != null) {
                            val allTaskList = taskToMeLocalData.allTasks.done.toMutableList()
                            val taskIndex = allTaskList.indexOf(doneTask)

                            doneTask.hiddenBy = hideData.hiddenBy
                            doneTask.updatedAt = hideData.updatedAt

                            allTaskList[taskIndex] = doneTask
                            taskToMeLocalData.allTasks.done = allTaskList
                        }

                    }

                    TaskV2DaoHelper(taskDao).insertTaskData(
                        taskToMeLocalData
                    )
                    TaskV2DaoHelper(taskDao).insertTaskData(
                        taskHiddenLocalData
                    )

                    EventBus.getDefault().post(LocalEvents.RefreshTasksEvent())


                    TaskEventsList.removeEvent(
                        SocketHandler.TaskEvent.TASK_SHOWN.name, hideData.taskId
                    )

                }
            }
        }

    }

    @Inject
    lateinit var draftNewTaskV2Internal: DraftNewTaskV2Dao

    @Inject
    lateinit var taskRepositoryInternal: ITaskRepository

    @Inject
    lateinit var taskDaoInternal: TaskV2Dao

    @Inject
    lateinit var sessionManagerInternal: SessionManager

    @Inject
    lateinit var dashboardRepositoryInternal: IDashboardRepository
    suspend fun syncDraftTask(context: Context) {


        Log.d("SyncDraftTask", "syncDraftTask")
        val user = sessionManagerInternal.getUser().value
        val unsyncedRecords = draftNewTaskV2Internal.getUnSyncedRecords() ?: emptyList()

        suspend fun uploadDraftTaskFiles(
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
        }

        // Define a recursive function to process records one by one
        suspend fun processNextRecord(records: List<NewTaskV2Entity>) {
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


//            taskRepositoryInternal.newTaskV2(newTaskRequest) { isSuccess, task, errorMessage ->
//                if (isSuccess) {
//                    launch {
//                        draftNewTaskV2Internal.deleteTaskById(newTaskRequest.taskId)
//                        updateCreatedTaskInLocal(
//                            task,
//                            taskDaoInternal,
//                            user?.id,
//                            sessionManagerInternal
//                        )
//                        newTaskRequest.filesData?.let { filesData ->
//                            task?.id?.let { uploadDraftTaskFiles(filesData, it) }
//                        }
//                        // Remove the processed record from the list
//                        val updatedRecords = records - newTaskRequest
//                        // Recursively process the next record
//                        processNextRecord(updatedRecords)
//                    }
//                }
//            }
        }

        // Start the recursive processing
        launch {
            processNextRecord(unsyncedRecords)
        }
    }

    fun saveFilesInDB(
        moduleName: String, moduleId: String, uploadedFiles: List<TaskFiles>, taskDao: TaskV2Dao
    ) {
        if (moduleName.equals(AttachmentModules.Task.name, true)) {
            launch {
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
            }
        }
    }

}


