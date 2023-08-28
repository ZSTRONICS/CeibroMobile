package com.zstronics.ceibro.base.viewmodel


import android.content.Context
import android.net.Uri
import androidx.annotation.CallSuper
import androidx.lifecycle.*
import androidx.work.*
import com.zstronics.ceibro.base.clickevents.SingleClickEvent
import com.zstronics.ceibro.base.interfaces.IBase
import com.zstronics.ceibro.base.interfaces.OnClickHandler
import com.zstronics.ceibro.base.navgraph.host.NavHostPresenterActivity
import com.zstronics.ceibro.base.state.UIState
import com.zstronics.ceibro.data.database.dao.TaskV2Dao
import com.zstronics.ceibro.data.database.models.tasks.CeibroTaskV2
import com.zstronics.ceibro.data.database.models.tasks.Events
import com.zstronics.ceibro.data.repos.dashboard.attachment.AttachmentUploadRequest
import com.zstronics.ceibro.data.repos.task.TaskRootStateTags
import com.zstronics.ceibro.data.repos.task.models.TaskV2Response
import com.zstronics.ceibro.data.repos.task.models.TasksV2DatabaseEntity
import com.zstronics.ceibro.data.repos.task.models.v2.EventV2Response
import com.zstronics.ceibro.data.repos.task.models.v2.HideTaskResponse
import com.zstronics.ceibro.data.repos.task.models.v2.TaskSeenResponse
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.ui.attachment.SubtaskAttachment
import com.zstronics.ceibro.ui.dashboard.SharedViewModel
import com.zstronics.ceibro.ui.socket.LocalEvents
import com.zstronics.ceibro.ui.tasks.task.TaskStatus
import com.zstronics.ceibro.utils.FileUtils
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus


abstract class HiltBaseViewModel<VS : IBase.State> : BaseCoroutineViewModel(),
    IBase.ViewModel<VS>, OnClickHandler {
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

    fun getString(keyID: Int, appContext: Context): String =
        appContext.getString(keyID)


    override val clickEvent: SingleClickEvent? = SingleClickEvent()

    /**
     * override this method when there is  no need to use its super implementation.
     * recommended to not override this method. use @see <handleOnClick> must override
     */
    override fun handlePressOnView(id: Int) {
        clickEvent?.setValue(id)
        handleOnClick(id)
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
    val notificationEvent: LiveData<LocalEvents.CreateNotification?> =
        _notificationEvent

    fun createNotification(notification: LocalEvents.CreateNotification?) {
        _notificationEvent.postValue(notification)
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
                context,
                it?.attachmentUri
            )
        }
        val request = AttachmentUploadRequest(
            _id = id,
            moduleName = module,
            files = attachmentUriList
        )
        EventBus.getDefault()
            .post(fileUriList?.let { LocalEvents.UploadFilesToServer(request, it) })
        removeAllFiles()
    }

    fun updateCreatedTaskInLocal(
        task: CeibroTaskV2?,
        taskDao: TaskV2Dao,
        userId: String?,
        sessionManager: SessionManager
    ) {
        val sharedViewModel = NavHostPresenterActivity.activityInstance?.let {
            ViewModelProvider(it).get(SharedViewModel::class.java)
        }
        launch {
            if (task != null) {
                /// Update record updated_at
                task.updateUpdatedAt()
                val taskFromMe = task.creator.id == userId
                val taskToMe = !task.assignedToState.none { it.userId == userId }

                if (taskFromMe) {
                    val taskLocalData =
                        taskDao.getTasks(TaskRootStateTags.FromMe.tagValue)
                    val unreadList = taskLocalData?.allTasks?.unread?.toMutableList()
                        ?: mutableListOf()

                    val index = unreadList.indexOfFirst { it.id == task.id }
                    if (index != -1) {
                        unreadList[index] = task
                    } else {
                        unreadList.add(0, task)
                    }

                    taskLocalData?.allTasks?.unread = unreadList.toList()
                    taskDao.insertTaskData(
                        taskLocalData ?: TasksV2DatabaseEntity(
                            rootState = TaskRootStateTags.FromMe.tagValue,
                            allTasks = TaskV2Response.AllTasks(
                                new = listOf(),
                                unread = unreadList.toList(),
                                ongoing = listOf(),
                                done = listOf()
                            )
                        )
                    )
                    EventBus.getDefault().post(LocalEvents.RefreshTasksEvent())
                }

                if (taskToMe) {
                    val taskLocalData =
                        taskDao.getTasks(TaskRootStateTags.ToMe.tagValue)
                    val newList = mutableListOf(task)
                    taskLocalData?.allTasks?.new?.let { oldList ->
                        val oldListMutableList = oldList.toMutableList()
                        val index = oldList.indexOfFirst { it.id == task.id }
                        if (index >= 0) {
                            oldListMutableList.removeAt(index)
                        }
                        newList.addAll(oldListMutableList)
                    }
                    taskLocalData?.allTasks?.new = newList.toList()
                    taskDao.insertTaskData(
                        taskLocalData ?: TasksV2DatabaseEntity(
                            rootState = TaskRootStateTags.ToMe.tagValue,
                            allTasks = TaskV2Response.AllTasks(
                                unread = listOf(),
                                new = newList.toList(),
                                ongoing = listOf(),
                                done = listOf()
                            )
                        )
                    )

                    sharedViewModel?.isToMeUnread?.value = true
                    sessionManager.saveToMeUnread(true)

                    EventBus.getDefault().post(LocalEvents.RefreshTasksEvent())
                }
            }
        }
    }

    fun updateForwardTaskInLocal(
        task: CeibroTaskV2?,
        taskDao: TaskV2Dao,
        userId: String?,
        sessionManager: SessionManager
    ) {
        val sharedViewModel = NavHostPresenterActivity.activityInstance?.let {
            ViewModelProvider(it).get(SharedViewModel::class.java)
        }
        launch {
            if (task != null) {
                /// Update record updated_at
                task.updateUpdatedAt()
                val taskFromMe = task.creator.id == userId
                val taskToMe = !task.assignedToState.none { it.userId == userId }
                val myState = task.assignedToState.find { it.userId == userId }?.state
                val hiddenByMe = task.hiddenBy.find { it == userId }.toBoolean()

                //first check hidden and move task from hidden
                launch {
                    val taskHiddenLocalData = taskDao.getTasks(TaskRootStateTags.Hidden.tagValue)
                    val taskToMeLocalData = taskDao.getTasks(TaskRootStateTags.ToMe.tagValue)

                    if (taskHiddenLocalData != null) {
                        val ongoingTask =
                            taskHiddenLocalData.allTasks.ongoing.find { it.id == task.id }
                        val doneTask = taskHiddenLocalData.allTasks.done.find { it.id == task.id }

                        if (ongoingTask != null) {
                            val allOngoingTaskList =
                                taskHiddenLocalData.allTasks.ongoing.toMutableList()
                            val taskIndex = allOngoingTaskList.indexOf(ongoingTask)

                            allOngoingTaskList.removeAt(taskIndex)
                            taskHiddenLocalData.allTasks.ongoing = allOngoingTaskList.toList()

                            if (taskToMeLocalData != null) {
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
                            }

                        } else if (doneTask != null) {
                            val allDoneTaskList = taskHiddenLocalData.allTasks.done.toMutableList()
                            val taskIndex = allDoneTaskList.indexOf(doneTask)

                            allDoneTaskList.removeAt(taskIndex)
                            taskHiddenLocalData.allTasks.done = allDoneTaskList.toList()

                            if (taskToMeLocalData != null) {
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
                            }
                        }

                        taskDao.insertTaskData(
                            taskHiddenLocalData
                        )
                        if (taskToMeLocalData != null) {
                            taskDao.insertTaskData(
                                taskToMeLocalData
                            )
                        }
                        // send task data for ui update
                        EventBus.getDefault().post(LocalEvents.TaskForwardEvent(task))
                        EventBus.getDefault().post(LocalEvents.RefreshTasksEvent())
                    }

                }

                if (task.creatorState.equals(TaskStatus.CANCELED.name, true)) {
                    // it means task must be in hidden rootState and child state will be canceled. search an update the task only
                    launch {
                        val taskHiddenLocalData =
                            taskDao.getTasks(TaskRootStateTags.Hidden.tagValue)

                        if (taskHiddenLocalData != null) {
                            val canceledTask =
                                taskHiddenLocalData.allTasks.canceled.find { it.id == task.id }
                            if (canceledTask != null) {
                                val allCancelTaskList =
                                    taskHiddenLocalData.allTasks.canceled.toMutableList()
                                val taskIndex = allCancelTaskList.indexOf(canceledTask)

                                allCancelTaskList[taskIndex] = task
                                taskHiddenLocalData.allTasks.canceled =
                                    allCancelTaskList.toList()

                                sharedViewModel?.isHiddenUnread?.value = true
                                sessionManager.saveHiddenUnread(true)

                                taskDao.insertTaskData(
                                    taskHiddenLocalData
                                )
                                // send task data for ui update
                                EventBus.getDefault()
                                    .post(LocalEvents.TaskForwardEvent(canceledTask))
                                EventBus.getDefault().post(LocalEvents.RefreshTasksEvent())
                            }
                        }
                    }
                }

                if (taskFromMe) {
                    val taskFromMeLocalData = taskDao.getTasks(TaskRootStateTags.FromMe.tagValue)

                    if (taskFromMeLocalData != null) {
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
                                ongoingList.add(0, task)
                                taskFromMeLocalData.allTasks.ongoing = ongoingList.toList()
                            } else {
                                unreadList[unreadTaskIndex] = task
                                taskFromMeLocalData.allTasks.unread = unreadList.toList()
                            }
                            sharedViewModel?.isFromMeUnread?.value = true
                            sessionManager.saveFromMeUnread(true)

                        } else if (ongoingTaskIndex >= 0) {
                            val ongoingList = taskFromMeLocalData.allTasks.ongoing.toMutableList()
                            if (task.creatorState.equals(TaskStatus.DONE.name, true)) {
                                /// remove from unread
                                ongoingList.removeAt(ongoingTaskIndex)
                                // push into ongoing
                                val doneList =
                                    taskFromMeLocalData.allTasks.done.toMutableList()
                                doneList.add(0, task)
                                taskFromMeLocalData.allTasks.done = doneList.toList()
                            } else {
                                ongoingList[ongoingTaskIndex] = task
                                taskFromMeLocalData.allTasks.ongoing = ongoingList.toList()
                            }
                            sharedViewModel?.isFromMeUnread?.value = true
                            sessionManager.saveFromMeUnread(true)

                        } else if (doneTaskIndex >= 0) {
                            val doneList = taskFromMeLocalData.allTasks.done.toMutableList()
                            doneList[doneTaskIndex] = task
                            taskFromMeLocalData.allTasks.done = doneList.toList()

                            sharedViewModel?.isFromMeUnread?.value = true
                            sessionManager.saveFromMeUnread(true)
                        }

                        taskDao.insertTaskData(
                            taskFromMeLocalData
                        )
                        EventBus.getDefault().post(LocalEvents.RefreshTasksEvent())
                        EventBus.getDefault().post(LocalEvents.TaskForwardEvent(task))
                    }
                }

                if (taskToMe) {
                    val taskToMeLocalData = taskDao.getTasks(TaskRootStateTags.ToMe.tagValue)

                    if (taskToMeLocalData != null) {
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
                                taskToMeLocalData.allTasks.new = newTaskList.toList()

                                val ongoingTaskList =
                                    taskToMeLocalData.allTasks.ongoing.toMutableList()
                                ongoingTaskList.add(0, task)
                                taskToMeLocalData.allTasks.ongoing = ongoingTaskList.toList()

                            } else if (myState.equals(TaskStatus.DONE.name, true)) {
                                val newTaskList = taskToMeLocalData.allTasks.new.toMutableList()
                                newTaskList.removeAt(newTaskIndex)
                                taskToMeLocalData.allTasks.new = newTaskList.toList()

                                val doneTaskList = taskToMeLocalData.allTasks.done.toMutableList()
                                doneTaskList.add(0, task)
                                taskToMeLocalData.allTasks.done = doneTaskList.toList()
                            } else {
                                val allTaskList = taskToMeLocalData.allTasks.new.toMutableList()
                                allTaskList[newTaskIndex] = task
                                taskToMeLocalData.allTasks.new = allTaskList.toList()
                            }
                            sharedViewModel?.isToMeUnread?.value = true
                            sessionManager.saveToMeUnread(true)

                        } else if (ongoingTaskIndex >= 0) {
                            if (myState.equals(TaskStatus.DONE.name, true)) {
                                val ongoingTaskList =
                                    taskToMeLocalData.allTasks.ongoing.toMutableList()
                                ongoingTaskList.removeAt(ongoingTaskIndex)
                                taskToMeLocalData.allTasks.ongoing = ongoingTaskList.toList()

                                val doneTaskList = taskToMeLocalData.allTasks.done.toMutableList()
                                doneTaskList.add(0, task)
                                taskToMeLocalData.allTasks.done = doneTaskList.toList()
                            } else {
                                val allTaskList = taskToMeLocalData.allTasks.ongoing.toMutableList()
                                allTaskList[ongoingTaskIndex] = task
                                taskToMeLocalData.allTasks.ongoing = allTaskList.toList()
                            }
                            sharedViewModel?.isToMeUnread?.value = true
                            sessionManager.saveToMeUnread(true)

                        } else if (doneTaskIndex >= 0) {
                            val allTaskList = taskToMeLocalData.allTasks.done.toMutableList()
                            allTaskList[doneTaskIndex] = task
                            taskToMeLocalData.allTasks.done = allTaskList.toList()
                            sharedViewModel?.isToMeUnread?.value = true
                            sessionManager.saveToMeUnread(true)
                        } else {
                            if (myState.equals(TaskStatus.NEW.name, true)) {
                                val allTaskList = taskToMeLocalData.allTasks.new.toMutableList()
                                allTaskList.add(0, task)
                                taskToMeLocalData.allTasks.new = allTaskList.toList()

                                sharedViewModel?.isToMeUnread?.value = true
                                sessionManager.saveToMeUnread(true)
                            }
                        }

                        taskDao.insertTaskData(
                            taskToMeLocalData
                        )
                        EventBus.getDefault().post(LocalEvents.RefreshTasksEvent())
                        EventBus.getDefault().post(LocalEvents.TaskForwardEvent(task))
                    }
                }
            }
        }
    }

    fun updateGenericTaskSeenInLocal(
        taskSeen: TaskSeenResponse.TaskSeen?,
        taskDao: TaskV2Dao,
        userId: String?
    ) {
        if (taskSeen != null) {
            launch {
                if (taskSeen.creatorState.equals(TaskStatus.CANCELED.name, true)) {
                    // it means task must be in hidden rootState and child state will be canceled. search an update the task only
                    launch {
                        val taskHiddenLocalData =
                            taskDao.getTasks(TaskRootStateTags.Hidden.tagValue)

                        if (taskHiddenLocalData != null) {
                            val canceledTask =
                                taskHiddenLocalData.allTasks.canceled.find { it.id == taskSeen.taskId }
                            /// Update record updated_at
                            canceledTask?.updateUpdatedAt()
                            if (canceledTask != null) {
                                val allCancelTaskList =
                                    taskHiddenLocalData.allTasks.canceled.toMutableList()
                                val taskIndex = allCancelTaskList.indexOf(canceledTask)

                                canceledTask.seenBy = taskSeen.seenBy

                                if (taskSeen.creatorStateChanged || taskSeen.stateChanged) {
                                    canceledTask.creatorState = taskSeen.creatorState

                                    val foundState =
                                        canceledTask.assignedToState.find { it.userId == taskSeen.state.userId }
                                    if (foundState != null) {
                                        val stateIndex =
                                            canceledTask.assignedToState.indexOf(foundState)
                                        canceledTask.assignedToState[stateIndex] = taskSeen.state
                                    }

                                    allCancelTaskList[taskIndex] = canceledTask
                                    taskHiddenLocalData.allTasks.canceled =
                                        allCancelTaskList.toList()

                                } else {
                                    allCancelTaskList[taskIndex] = canceledTask
                                    taskHiddenLocalData.allTasks.canceled =
                                        allCancelTaskList.toList()
                                }

                                taskDao.insertTaskData(
                                    taskHiddenLocalData
                                )
                                // send task data for ui update
                                EventBus.getDefault()
                                    .post(LocalEvents.TaskForwardEvent(canceledTask))
                                EventBus.getDefault().post(LocalEvents.RefreshTasksEvent())
                            }
                        }
                    }
                }

                if (taskSeen.isCreator) {
                    val taskFromMeLocalData = taskDao.getTasks(TaskRootStateTags.FromMe.tagValue)

                    if (taskFromMeLocalData != null) {
                        val unreadTask =
                            taskFromMeLocalData.allTasks.unread.find { it.id == taskSeen.taskId }
                        val ongoingTask =
                            taskFromMeLocalData.allTasks.ongoing.find { it.id == taskSeen.taskId }
                        val doneTask =
                            taskFromMeLocalData.allTasks.done.find { it.id == taskSeen.taskId }
                        if (unreadTask != null) {
                            /// Update record updated_at
                            unreadTask.updateUpdatedAt()
                            val allTaskList = taskFromMeLocalData.allTasks.unread.toMutableList()
                            val taskIndex = allTaskList.indexOf(unreadTask)

                            unreadTask.seenBy = taskSeen.seenBy

                            if (taskSeen.creatorStateChanged || taskSeen.stateChanged) {
                                unreadTask.creatorState = taskSeen.creatorState

                                val foundState =
                                    unreadTask.assignedToState.find { it.userId == taskSeen.state.userId }
                                if (foundState != null) {
                                    val stateIndex = unreadTask.assignedToState.indexOf(foundState)
                                    unreadTask.assignedToState[stateIndex] = taskSeen.state
                                }


                                if (taskSeen.creatorState.equals(TaskStatus.ONGOING.name, true)) {
                                    val allOngoingTaskList =
                                        taskFromMeLocalData.allTasks.ongoing.toMutableList()
                                    val newAllOngoingTaskList: MutableList<CeibroTaskV2> =
                                        mutableListOf()
                                    newAllOngoingTaskList.add(unreadTask)
                                    newAllOngoingTaskList.addAll(allOngoingTaskList)

                                    allTaskList.removeAt(taskIndex)
                                    taskFromMeLocalData.allTasks.unread = allTaskList.toList()
                                    taskFromMeLocalData.allTasks.ongoing =
                                        newAllOngoingTaskList.toList()

                                } else if (taskSeen.creatorState.equals(
                                        TaskStatus.DONE.name,
                                        true
                                    )
                                ) {
                                    val allDoneTaskList =
                                        taskFromMeLocalData.allTasks.done.toMutableList()
                                    val newAllDoneTaskList: MutableList<CeibroTaskV2> =
                                        mutableListOf()
                                    newAllDoneTaskList.add(unreadTask)
                                    newAllDoneTaskList.addAll(allDoneTaskList)

                                    allTaskList.removeAt(taskIndex)
                                    taskFromMeLocalData.allTasks.unread = allTaskList.toList()
                                    taskFromMeLocalData.allTasks.done = newAllDoneTaskList.toList()

                                } else {
                                    allTaskList[taskIndex] = unreadTask
                                    taskFromMeLocalData.allTasks.unread = allTaskList.toList()
                                }
                            } else {
                                allTaskList[taskIndex] = unreadTask
                                taskFromMeLocalData.allTasks.unread = allTaskList.toList()
                            }
                            EventBus.getDefault().post(LocalEvents.TaskForwardEvent(unreadTask))

                        } else if (ongoingTask != null) {
                            /// Update record updated_at
                            ongoingTask.updateUpdatedAt()
                            val allTaskList = taskFromMeLocalData.allTasks.ongoing.toMutableList()
                            val taskIndex = allTaskList.indexOf(ongoingTask)

                            ongoingTask.seenBy = taskSeen.seenBy

                            if (taskSeen.creatorStateChanged || taskSeen.stateChanged) {
                                ongoingTask.creatorState = taskSeen.creatorState

                                val foundState =
                                    ongoingTask.assignedToState.find { it.userId == taskSeen.state.userId }
                                if (foundState != null) {
                                    val stateIndex = ongoingTask.assignedToState.indexOf(foundState)
                                    ongoingTask.assignedToState[stateIndex] = taskSeen.state
                                }


                                if (taskSeen.creatorState.equals(TaskStatus.UNREAD.name, true)) {
                                    val allUnreadTaskList =
                                        taskFromMeLocalData.allTasks.unread.toMutableList()
                                    val newAllUnreadTaskList: MutableList<CeibroTaskV2> =
                                        mutableListOf()
                                    newAllUnreadTaskList.add(ongoingTask)
                                    newAllUnreadTaskList.addAll(allUnreadTaskList)

                                    allTaskList.removeAt(taskIndex)
                                    taskFromMeLocalData.allTasks.ongoing = allTaskList.toList()
                                    taskFromMeLocalData.allTasks.unread =
                                        newAllUnreadTaskList.toList()

                                } else if (taskSeen.creatorState.equals(
                                        TaskStatus.DONE.name,
                                        true
                                    )
                                ) {
                                    val allDoneTaskList =
                                        taskFromMeLocalData.allTasks.done.toMutableList()
                                    val newAllDoneTaskList: MutableList<CeibroTaskV2> =
                                        mutableListOf()
                                    newAllDoneTaskList.add(ongoingTask)
                                    newAllDoneTaskList.addAll(allDoneTaskList)

                                    allTaskList.removeAt(taskIndex)
                                    taskFromMeLocalData.allTasks.ongoing = allTaskList.toList()
                                    taskFromMeLocalData.allTasks.done = newAllDoneTaskList.toList()

                                } else {
                                    allTaskList[taskIndex] = ongoingTask
                                    taskFromMeLocalData.allTasks.ongoing = allTaskList.toList()
                                }
                            } else {
                                allTaskList[taskIndex] = ongoingTask
                                taskFromMeLocalData.allTasks.ongoing = allTaskList.toList()
                            }
                            EventBus.getDefault().post(LocalEvents.TaskForwardEvent(ongoingTask))
                        } else if (doneTask != null) {
                            /// Update record updated_at
                            doneTask.updateUpdatedAt()
                            val allTaskList = taskFromMeLocalData.allTasks.done.toMutableList()
                            val taskIndex = allTaskList.indexOf(doneTask)

                            doneTask.seenBy = taskSeen.seenBy

                            if (taskSeen.creatorStateChanged || taskSeen.stateChanged) {
                                doneTask.creatorState = taskSeen.creatorState

                                val foundState =
                                    doneTask.assignedToState.find { it.userId == taskSeen.state.userId }
                                if (foundState != null) {
                                    val stateIndex = doneTask.assignedToState.indexOf(foundState)
                                    doneTask.assignedToState[stateIndex] = taskSeen.state
                                }


                                if (taskSeen.creatorState.equals(TaskStatus.UNREAD.name, true)) {
                                    val allUnreadTaskList =
                                        taskFromMeLocalData.allTasks.unread.toMutableList()
                                    val newAllUnreadTaskList: MutableList<CeibroTaskV2> =
                                        mutableListOf()
                                    newAllUnreadTaskList.add(doneTask)
                                    newAllUnreadTaskList.addAll(allUnreadTaskList)

                                    allTaskList.removeAt(taskIndex)
                                    taskFromMeLocalData.allTasks.done = allTaskList.toList()
                                    taskFromMeLocalData.allTasks.unread =
                                        newAllUnreadTaskList.toList()

                                } else if (taskSeen.creatorState.equals(
                                        TaskStatus.ONGOING.name,
                                        true
                                    )
                                ) {
                                    val allOngoingTaskList =
                                        taskFromMeLocalData.allTasks.ongoing.toMutableList()
                                    val newAllOngoingTaskList: MutableList<CeibroTaskV2> =
                                        mutableListOf()
                                    newAllOngoingTaskList.add(doneTask)
                                    newAllOngoingTaskList.addAll(allOngoingTaskList)

                                    allTaskList.removeAt(taskIndex)
                                    taskFromMeLocalData.allTasks.done = allTaskList.toList()
                                    taskFromMeLocalData.allTasks.ongoing =
                                        newAllOngoingTaskList.toList()

                                } else {
                                    allTaskList[taskIndex] = doneTask
                                    taskFromMeLocalData.allTasks.done = allTaskList.toList()
                                }
                            } else {
                                allTaskList[taskIndex] = doneTask
                                taskFromMeLocalData.allTasks.done = allTaskList.toList()
                            }
                            EventBus.getDefault().post(LocalEvents.TaskForwardEvent(doneTask))
                        }

                        taskDao.insertTaskData(
                            taskFromMeLocalData
                        )
                        EventBus.getDefault().post(LocalEvents.RefreshTasksEvent())
                    }
                }

                if (taskSeen.isAssignedToMe) {
                    val taskToMeLocalData = taskDao.getTasks(TaskRootStateTags.ToMe.tagValue)

                    if (taskToMeLocalData != null) {
                        val newTask =
                            taskToMeLocalData.allTasks.new.find { it.id == taskSeen.taskId }
                        val ongoingTask =
                            taskToMeLocalData.allTasks.ongoing.find { it.id == taskSeen.taskId }
                        val doneTask =
                            taskToMeLocalData.allTasks.done.find { it.id == taskSeen.taskId }
                        if (newTask != null) {
                            /// Update record updated_at
                            newTask.updateUpdatedAt()
                            val allTaskList = taskToMeLocalData.allTasks.new.toMutableList()
                            val taskIndex = allTaskList.indexOf(newTask)

                            newTask.seenBy = taskSeen.seenBy

                            if (taskSeen.creatorStateChanged || taskSeen.stateChanged) {
                                newTask.creatorState = taskSeen.creatorState

                                val foundState =
                                    newTask.assignedToState.find { it.userId == taskSeen.state.userId }
                                if (foundState != null) {
                                    val stateIndex = newTask.assignedToState.indexOf(foundState)
                                    newTask.assignedToState[stateIndex] = taskSeen.state
                                }


                                if (taskSeen.state.userId == userId && taskSeen.state.state.equals(
                                        TaskStatus.ONGOING.name,
                                        true
                                    )
                                ) {
                                    val allOngoingTaskList =
                                        taskToMeLocalData.allTasks.ongoing.toMutableList()
                                    val newAllOngoingTaskList: MutableList<CeibroTaskV2> =
                                        mutableListOf()
                                    newAllOngoingTaskList.add(newTask)
                                    newAllOngoingTaskList.addAll(allOngoingTaskList)

                                    allTaskList.removeAt(taskIndex)
                                    taskToMeLocalData.allTasks.new = allTaskList.toList()
                                    taskToMeLocalData.allTasks.ongoing =
                                        newAllOngoingTaskList.toList()

                                } else if (taskSeen.state.userId == userId && taskSeen.state.state.equals(
                                        TaskStatus.DONE.name,
                                        true
                                    )
                                ) {
                                    val allDoneTaskList =
                                        taskToMeLocalData.allTasks.done.toMutableList()
                                    val newAllDoneTaskList: MutableList<CeibroTaskV2> =
                                        mutableListOf()
                                    newAllDoneTaskList.add(newTask)
                                    newAllDoneTaskList.addAll(allDoneTaskList)

                                    allTaskList.removeAt(taskIndex)
                                    taskToMeLocalData.allTasks.new = allTaskList.toList()
                                    taskToMeLocalData.allTasks.done = newAllDoneTaskList.toList()

                                } else {
                                    allTaskList[taskIndex] = newTask
                                    taskToMeLocalData.allTasks.new = allTaskList.toList()
                                }
                            } else {
                                allTaskList[taskIndex] = newTask
                                taskToMeLocalData.allTasks.new = allTaskList.toList()
                            }
                            EventBus.getDefault().post(LocalEvents.TaskForwardEvent(newTask))
                        } else if (ongoingTask != null) {
                            /// Update record updated_at
                            ongoingTask.updateUpdatedAt()
                            val allTaskList = taskToMeLocalData.allTasks.ongoing.toMutableList()
                            val taskIndex = allTaskList.indexOf(ongoingTask)

                            ongoingTask.seenBy = taskSeen.seenBy

                            if (taskSeen.creatorStateChanged || taskSeen.stateChanged) {
                                ongoingTask.creatorState = taskSeen.creatorState

                                val foundState =
                                    ongoingTask.assignedToState.find { it.userId == taskSeen.state.userId }
                                if (foundState != null) {
                                    val stateIndex = ongoingTask.assignedToState.indexOf(foundState)
                                    ongoingTask.assignedToState[stateIndex] = taskSeen.state
                                }


                                if (taskSeen.state.userId == userId && taskSeen.state.state.equals(
                                        TaskStatus.UNREAD.name,
                                        true
                                    )
                                ) {
                                    val allUnreadTaskList =
                                        taskToMeLocalData.allTasks.unread.toMutableList()
                                    val newAllUnreadTaskList: MutableList<CeibroTaskV2> =
                                        mutableListOf()
                                    newAllUnreadTaskList.add(ongoingTask)
                                    newAllUnreadTaskList.addAll(allUnreadTaskList)

                                    allTaskList.removeAt(taskIndex)
                                    taskToMeLocalData.allTasks.ongoing = allTaskList.toList()
                                    taskToMeLocalData.allTasks.unread =
                                        newAllUnreadTaskList.toList()

                                } else if (taskSeen.state.userId == userId && taskSeen.state.state.equals(
                                        TaskStatus.DONE.name,
                                        true
                                    )
                                ) {
                                    val allDoneTaskList =
                                        taskToMeLocalData.allTasks.done.toMutableList()
                                    val newAllDoneTaskList: MutableList<CeibroTaskV2> =
                                        mutableListOf()
                                    newAllDoneTaskList.add(ongoingTask)
                                    newAllDoneTaskList.addAll(allDoneTaskList)

                                    allTaskList.removeAt(taskIndex)
                                    taskToMeLocalData.allTasks.ongoing = allTaskList.toList()
                                    taskToMeLocalData.allTasks.done = newAllDoneTaskList.toList()

                                } else {
                                    allTaskList[taskIndex] = ongoingTask
                                    taskToMeLocalData.allTasks.ongoing = allTaskList.toList()
                                }
                            } else {
                                allTaskList[taskIndex] = ongoingTask
                                taskToMeLocalData.allTasks.ongoing = allTaskList.toList()
                            }
                            EventBus.getDefault().post(LocalEvents.TaskForwardEvent(ongoingTask))

                        } else if (doneTask != null) {
                            /// Update record updated_at
                            doneTask.updateUpdatedAt()
                            val allTaskList = taskToMeLocalData.allTasks.done.toMutableList()
                            val taskIndex = allTaskList.indexOf(doneTask)

                            doneTask.seenBy = taskSeen.seenBy

                            if (taskSeen.creatorStateChanged || taskSeen.stateChanged) {
                                doneTask.creatorState = taskSeen.creatorState

                                val foundState =
                                    doneTask.assignedToState.find { it.userId == taskSeen.state.userId }
                                if (foundState != null) {
                                    val stateIndex = doneTask.assignedToState.indexOf(foundState)
                                    doneTask.assignedToState[stateIndex] = taskSeen.state
                                }


                                if (taskSeen.state.userId == userId && taskSeen.state.state.equals(
                                        TaskStatus.UNREAD.name,
                                        true
                                    )
                                ) {
                                    val allUnreadTaskList =
                                        taskToMeLocalData.allTasks.unread.toMutableList()
                                    val newAllUnreadTaskList: MutableList<CeibroTaskV2> =
                                        mutableListOf()
                                    newAllUnreadTaskList.add(doneTask)
                                    newAllUnreadTaskList.addAll(allUnreadTaskList)

                                    allTaskList.removeAt(taskIndex)
                                    taskToMeLocalData.allTasks.done = allTaskList.toList()
                                    taskToMeLocalData.allTasks.unread =
                                        newAllUnreadTaskList.toList()

                                } else if (taskSeen.state.userId == userId && taskSeen.state.state.equals(
                                        TaskStatus.ONGOING.name,
                                        true
                                    )
                                ) {
                                    val allOngoingTaskList =
                                        taskToMeLocalData.allTasks.ongoing.toMutableList()
                                    val newAllOngoingTaskList: MutableList<CeibroTaskV2> =
                                        mutableListOf()
                                    newAllOngoingTaskList.add(doneTask)
                                    newAllOngoingTaskList.addAll(allOngoingTaskList)

                                    allTaskList.removeAt(taskIndex)
                                    taskToMeLocalData.allTasks.done = allTaskList.toList()
                                    taskToMeLocalData.allTasks.ongoing =
                                        newAllOngoingTaskList.toList()

                                } else {
                                    allTaskList[taskIndex] = doneTask
                                    taskToMeLocalData.allTasks.done = allTaskList.toList()
                                }
                            } else {
                                allTaskList[taskIndex] = doneTask
                                taskToMeLocalData.allTasks.done = allTaskList.toList()
                            }
                            EventBus.getDefault().post(LocalEvents.TaskForwardEvent(doneTask))
                        }

                        taskDao.insertTaskData(
                            taskToMeLocalData
                        )
                        EventBus.getDefault().post(LocalEvents.RefreshTasksEvent())
                    }

                }
            }

        }
    }

    fun updateTaskCommentInLocal(
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
                invitedMembers = eventData.invitedMembers,
                v = null
            )
            val taskEventList: MutableList<Events> = mutableListOf()
            taskEventList.add(taskEvent)

            val hiddenByCurrentUser = eventData.oldTaskData.isHiddenByMe
            if (hiddenByCurrentUser) {
                //it means task must be searched from hidden rootState and child states[ongoing and done] and then move the task to another root state from hidden
                launch {
                    val taskHiddenLocalData = taskDao.getTasks(TaskRootStateTags.Hidden.tagValue)
                    val taskToMeLocalData = taskDao.getTasks(TaskRootStateTags.ToMe.tagValue)
                    val taskFromMeLocalData = taskDao.getTasks(TaskRootStateTags.FromMe.tagValue)

                    if (taskHiddenLocalData != null) {
                        val ongoingTask =
                            taskHiddenLocalData.allTasks.ongoing.find { it.id == taskID }
                        val doneTask = taskHiddenLocalData.allTasks.done.find { it.id == taskID }

                        if (ongoingTask != null) {
                            /// Update record updated_at
                            ongoingTask.updateUpdatedAt()
                            val allOngoingTaskList =
                                taskHiddenLocalData.allTasks.ongoing.toMutableList()
                            val taskIndex = allOngoingTaskList.indexOf(ongoingTask)

                            var oldEvents = ongoingTask.events.toMutableList()
                            if (oldEvents.isNotEmpty()) {
                                val oldOnlyEvent = oldEvents.find { it.id == eventData.id }
                                if (oldOnlyEvent != null) {     //means event already exist, so replace it
                                    val oldEventIndex = oldEvents.indexOf(oldOnlyEvent)
                                    oldEvents[oldEventIndex] = taskEvent
                                    ongoingTask.events = oldEvents.toList()
                                } else {
                                    oldEvents.add(taskEvent)
                                    ongoingTask.events = oldEvents.toList()
                                }
                            } else {
                                oldEvents = taskEventList
                                ongoingTask.events = oldEvents.toList()
                            }
                            ongoingTask.hiddenBy = listOf()
                            ongoingTask.seenBy = eventData.taskData.seenBy

                            allOngoingTaskList.removeAt(taskIndex)
                            taskHiddenLocalData.allTasks.ongoing = allOngoingTaskList.toList()

                            if (eventData.oldTaskData.isAssignedToMe) {
                                if (taskToMeLocalData != null) {
                                    val ongoingTaskToMe =
                                        taskToMeLocalData.allTasks.ongoing.find { it.id == taskID }
                                    val allOngoingToMeTaskList =
                                        taskToMeLocalData.allTasks.ongoing.toMutableList()
                                    if (ongoingTaskToMe != null) {
                                        val index = allOngoingToMeTaskList.indexOf(ongoingTaskToMe)
                                        allOngoingToMeTaskList[index] = ongoingTask
                                        taskToMeLocalData.allTasks.ongoing = allOngoingToMeTaskList
                                    } else {
                                        allOngoingToMeTaskList.add(0, ongoingTask)
                                        taskToMeLocalData.allTasks.ongoing = allOngoingToMeTaskList
                                    }
                                    sharedViewModel?.isToMeUnread?.value = true
                                    sessionManager.saveToMeUnread(true)
                                }
                            }
                            if (eventData.oldTaskData.isCreator) {
                                if (taskFromMeLocalData != null) {
                                    val ongoingTaskFromMe =
                                        taskFromMeLocalData.allTasks.ongoing.find { it.id == taskID }
                                    val allOngoingFromMeTaskList =
                                        taskFromMeLocalData.allTasks.ongoing.toMutableList()
                                    if (ongoingTaskFromMe != null) {
                                        val index =
                                            allOngoingFromMeTaskList.indexOf(ongoingTaskFromMe)
                                        allOngoingFromMeTaskList[index] = ongoingTask
                                        taskFromMeLocalData.allTasks.ongoing =
                                            allOngoingFromMeTaskList

                                        sharedViewModel?.isFromMeUnread?.value = true
                                        sessionManager.saveFromMeUnread(true)
                                    }
                                }
                            }
                            updatedTask = ongoingTask

                        } else if (doneTask != null) {
                            /// Update record updated_at
                            doneTask.updateUpdatedAt()
                            val allDoneTaskList = taskHiddenLocalData.allTasks.done.toMutableList()
                            val taskIndex = allDoneTaskList.indexOf(doneTask)

                            var oldEvents = doneTask.events.toMutableList()
                            if (oldEvents.isNotEmpty()) {
                                val oldOnlyEvent = oldEvents.find { it.id == eventData.id }
                                if (oldOnlyEvent != null) {     //means event already exist, so replace it
                                    val oldEventIndex = oldEvents.indexOf(oldOnlyEvent)
                                    oldEvents[oldEventIndex] = taskEvent
                                    doneTask.events = oldEvents.toList()
                                } else {
                                    oldEvents.add(taskEvent)
                                    doneTask.events = oldEvents.toList()
                                }
                            } else {
                                oldEvents = taskEventList
                                doneTask.events = oldEvents.toList()
                            }
                            doneTask.hiddenBy = listOf()
                            doneTask.seenBy = eventData.taskData.seenBy

                            allDoneTaskList.removeAt(taskIndex)
                            taskHiddenLocalData.allTasks.done = allDoneTaskList.toList()

                            if (eventData.oldTaskData.isAssignedToMe) {
                                if (taskToMeLocalData != null) {
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
                                    sharedViewModel?.isToMeUnread?.value = true
                                    sessionManager.saveToMeUnread(true)
                                }
                            }
                            if (eventData.oldTaskData.isCreator) {
                                if (taskFromMeLocalData != null) {
                                    val doneTaskFromMe =
                                        taskFromMeLocalData.allTasks.done.find { it.id == taskID }
                                    val allDoneFromMeTaskList =
                                        taskFromMeLocalData.allTasks.done.toMutableList()
                                    if (doneTaskFromMe != null) {
                                        val index = allDoneFromMeTaskList.indexOf(doneTaskFromMe)
                                        allDoneFromMeTaskList[index] = doneTask
                                        taskFromMeLocalData.allTasks.done = allDoneFromMeTaskList

                                        sharedViewModel?.isFromMeUnread?.value = true
                                        sessionManager.saveFromMeUnread(true)
                                    }
                                }
                            }
                            updatedTask = doneTask
                        }

                        taskDao.insertTaskData(
                            taskHiddenLocalData
                        )
                        if (taskToMeLocalData != null) {
                            taskDao.insertTaskData(
                                taskToMeLocalData
                            )
                        }
                        if (taskFromMeLocalData != null) {
                            taskDao.insertTaskData(
                                taskFromMeLocalData
                            )
                        }
                        // send task data for ui update
                        EventBus.getDefault().post(LocalEvents.TaskForwardEvent(updatedTask))
                        EventBus.getDefault().post(LocalEvents.RefreshTasksEvent())
                    }

                }
            } else if (eventData.oldTaskData.creatorState.equals(TaskStatus.CANCELED.name, true)) {
                // it means task must be in hidden rootState and child state will be canceled. search an update the task only
                launch {
                    val taskHiddenLocalData = taskDao.getTasks(TaskRootStateTags.Hidden.tagValue)

                    if (taskHiddenLocalData != null) {
                        val canceledTask =
                            taskHiddenLocalData.allTasks.canceled.find { it.id == taskID }
                        if (canceledTask != null) {
                            /// Update record updated_at
                            canceledTask.updateUpdatedAt()
                            val allCancelTaskList =
                                taskHiddenLocalData.allTasks.canceled.toMutableList()
                            val taskIndex = allCancelTaskList.indexOf(canceledTask)

                            var oldEvents = canceledTask.events.toMutableList()
                            if (oldEvents.isNotEmpty()) {
                                val oldOnlyEvent = oldEvents.find { it.id == eventData.id }
                                if (oldOnlyEvent != null) {     //means event already exist, so replace it
                                    val oldEventIndex = oldEvents.indexOf(oldOnlyEvent)
                                    oldEvents[oldEventIndex] = taskEvent
                                    canceledTask.events = oldEvents.toList()
                                } else {
                                    oldEvents.add(taskEvent)
                                    canceledTask.events = oldEvents.toList()
                                }
                            } else {
                                oldEvents = taskEventList
                                canceledTask.events = oldEvents.toList()
                            }
                            canceledTask.hiddenBy = listOf()
                            canceledTask.seenBy = eventData.taskData.seenBy

                            allCancelTaskList[taskIndex] = canceledTask
                            taskHiddenLocalData.allTasks.canceled = allCancelTaskList.toList()

                            updatedTask = canceledTask
                            taskDao.insertTaskData(
                                taskHiddenLocalData
                            )
                            sharedViewModel?.isHiddenUnread?.value = true
                            sessionManager.saveHiddenUnread(true)
                            // send task data for ui update
                            EventBus.getDefault().post(LocalEvents.TaskForwardEvent(updatedTask))
                            EventBus.getDefault().post(LocalEvents.RefreshTasksEvent())
                        }
                    }
                }
            }
            launch {
                val taskToMeLocalData = taskDao.getTasks(TaskRootStateTags.ToMe.tagValue)
                val taskFromMeLocalData = taskDao.getTasks(TaskRootStateTags.FromMe.tagValue)

                if (eventData.oldTaskData.isAssignedToMe) {
                    if (taskToMeLocalData != null) {
                        val newTask = taskToMeLocalData.allTasks.new.find { it.id == taskID }
                        val ongoingTask =
                            taskToMeLocalData.allTasks.ongoing.find { it.id == taskID }
                        val doneTask = taskToMeLocalData.allTasks.done.find { it.id == taskID }

                        if (newTask != null) {
                            /// Update record updated_at
                            newTask.updateUpdatedAt()
                            val allTaskList = taskToMeLocalData.allTasks.new.toMutableList()
                            val taskIndex = allTaskList.indexOf(newTask)

                            var oldEvents = newTask.events.toMutableList()
                            if (oldEvents.isNotEmpty()) {
                                val oldOnlyEvent = oldEvents.find { it.id == eventData.id }
                                if (oldOnlyEvent != null) {     //means event already exist, so replace it
                                    val oldEventIndex = oldEvents.indexOf(oldOnlyEvent)
                                    oldEvents[oldEventIndex] = taskEvent
                                    newTask.events = oldEvents.toList()
                                } else {
                                    oldEvents.add(taskEvent)
                                    newTask.events = oldEvents.toList()
                                }
                            } else {
                                oldEvents = taskEventList
                                newTask.events = oldEvents.toList()
                            }
                            newTask.hiddenBy = listOf()
                            newTask.seenBy = eventData.taskData.seenBy

                            allTaskList[taskIndex] = newTask
                            taskToMeLocalData.allTasks.new = allTaskList.toList()
                            updatedTask = newTask
                            sharedViewModel?.isToMeUnread?.postValue(true)
                            sessionManager.saveToMeUnread(true)
                        } else if (ongoingTask != null) {
                            /// Update record updated_at
                            ongoingTask.updateUpdatedAt()
                            val allTaskList = taskToMeLocalData.allTasks.ongoing.toMutableList()
                            val taskIndex = allTaskList.indexOf(ongoingTask)

                            var oldEvents = ongoingTask.events.toMutableList()
                            if (oldEvents.isNotEmpty()) {
                                val oldOnlyEvent = oldEvents.find { it.id == eventData.id }
                                if (oldOnlyEvent != null) {     //means event already exist, so replace it
                                    val oldEventIndex = oldEvents.indexOf(oldOnlyEvent)
                                    oldEvents[oldEventIndex] = taskEvent
                                    ongoingTask.events = oldEvents.toList()
                                } else {
                                    oldEvents.add(taskEvent)
                                    ongoingTask.events = oldEvents.toList()
                                }
                            } else {
                                oldEvents = taskEventList
                                ongoingTask.events = oldEvents.toList()
                            }
                            ongoingTask.hiddenBy = listOf()
                            ongoingTask.seenBy = eventData.taskData.seenBy

                            allTaskList[taskIndex] = ongoingTask
                            taskToMeLocalData.allTasks.ongoing = allTaskList.toList()
                            updatedTask = ongoingTask
                            sharedViewModel?.isToMeUnread?.postValue(true)
                            sessionManager.saveToMeUnread(true)
                        } else if (doneTask != null) {
                            /// Update record updated_at
                            doneTask.updateUpdatedAt()
                            val allTaskList = taskToMeLocalData.allTasks.done.toMutableList()
                            val taskIndex = allTaskList.indexOf(doneTask)

                            var oldEvents = doneTask.events.toMutableList()
                            if (oldEvents.isNotEmpty()) {
                                val oldOnlyEvent = oldEvents.find { it.id == eventData.id }
                                if (oldOnlyEvent != null) {     //means event already exist, so replace it
                                    val oldEventIndex = oldEvents.indexOf(oldOnlyEvent)
                                    oldEvents[oldEventIndex] = taskEvent
                                    doneTask.events = oldEvents.toList()
                                } else {
                                    oldEvents.add(taskEvent)
                                    doneTask.events = oldEvents.toList()
                                }
                            } else {
                                oldEvents = taskEventList
                                doneTask.events = oldEvents.toList()
                            }
                            doneTask.hiddenBy = listOf()
                            doneTask.seenBy = eventData.taskData.seenBy

                            allTaskList[taskIndex] = doneTask
                            taskToMeLocalData.allTasks.done = allTaskList.toList()
                            updatedTask = doneTask
                            sharedViewModel?.isToMeUnread?.postValue(true)
                            sessionManager.saveToMeUnread(true)
                        }

                        taskDao.insertTaskData(
                            taskToMeLocalData
                        )
                        // send task data for ui update
                        EventBus.getDefault().post(LocalEvents.TaskForwardEvent(updatedTask))
                        EventBus.getDefault().post(LocalEvents.RefreshTasksEvent())
                    }
                }

                if (eventData.oldTaskData.isCreator) {
                    if (taskFromMeLocalData != null) {
                        val unreadTask =
                            taskFromMeLocalData.allTasks.unread.find { it.id == taskID }
                        val ongoingTask =
                            taskFromMeLocalData.allTasks.ongoing.find { it.id == taskID }
                        val doneTask = taskFromMeLocalData.allTasks.done.find { it.id == taskID }

                        if (unreadTask != null) {
                            /// Update record updated_at
                            unreadTask.updateUpdatedAt()
                            val allTaskList = taskFromMeLocalData.allTasks.unread.toMutableList()
                            val taskIndex = allTaskList.indexOf(unreadTask)

                            var oldEvents = unreadTask.events.toMutableList()
                            if (oldEvents.isNotEmpty()) {
                                val oldOnlyEvent = oldEvents.find { it.id == eventData.id }
                                if (oldOnlyEvent != null) {     //means event already exist, so replace it
                                    val oldEventIndex = oldEvents.indexOf(oldOnlyEvent)
                                    oldEvents[oldEventIndex] = taskEvent
                                    unreadTask.events = oldEvents.toList()
                                } else {
                                    oldEvents.add(taskEvent)
                                    unreadTask.events = oldEvents.toList()
                                }
                            } else {
                                oldEvents = taskEventList
                                unreadTask.events = oldEvents.toList()
                            }
                            unreadTask.hiddenBy = listOf()
                            unreadTask.seenBy = eventData.taskData.seenBy

                            allTaskList[taskIndex] = unreadTask
                            taskFromMeLocalData.allTasks.unread = allTaskList.toList()
                            updatedTask = unreadTask
                            sharedViewModel?.isFromMeUnread?.value = true
                            sessionManager.saveFromMeUnread(true)
                        } else if (ongoingTask != null) {
                            /// Update record updated_at
                            ongoingTask.updateUpdatedAt()
                            val allTaskList = taskFromMeLocalData.allTasks.ongoing.toMutableList()
                            val taskIndex = allTaskList.indexOf(ongoingTask)

                            var oldEvents = ongoingTask.events.toMutableList()
                            if (oldEvents.isNotEmpty()) {
                                val oldOnlyEvent = oldEvents.find { it.id == eventData.id }
                                if (oldOnlyEvent != null) {     //means event already exist, so replace it
                                    val oldEventIndex = oldEvents.indexOf(oldOnlyEvent)
                                    oldEvents[oldEventIndex] = taskEvent
                                    ongoingTask.events = oldEvents.toList()
                                } else {
                                    oldEvents.add(taskEvent)
                                    ongoingTask.events = oldEvents.toList()
                                }
                            } else {
                                oldEvents = taskEventList
                                ongoingTask.events = oldEvents.toList()
                            }
                            ongoingTask.hiddenBy = listOf()
                            ongoingTask.seenBy = eventData.taskData.seenBy

                            allTaskList[taskIndex] = ongoingTask
                            taskFromMeLocalData.allTasks.ongoing = allTaskList.toList()
                            updatedTask = ongoingTask
                            sharedViewModel?.isFromMeUnread?.value = true
                            sessionManager.saveFromMeUnread(true)
                        } else if (doneTask != null) {
                            /// Update record updated_at
                            doneTask.updateUpdatedAt()
                            val allTaskList = taskFromMeLocalData.allTasks.done.toMutableList()
                            val taskIndex = allTaskList.indexOf(doneTask)

                            var oldEvents = doneTask.events.toMutableList()
                            if (oldEvents.isNotEmpty()) {
                                val oldOnlyEvent = oldEvents.find { it.id == eventData.id }
                                if (oldOnlyEvent != null) {     //means event already exist, so replace it
                                    val oldEventIndex = oldEvents.indexOf(oldOnlyEvent)
                                    oldEvents[oldEventIndex] = taskEvent
                                    doneTask.events = oldEvents.toList()
                                } else {
                                    oldEvents.add(taskEvent)
                                    doneTask.events = oldEvents.toList()
                                }
                            } else {
                                oldEvents = taskEventList
                                doneTask.events = oldEvents.toList()
                            }
                            doneTask.hiddenBy = listOf()
                            doneTask.seenBy = eventData.taskData.seenBy

                            allTaskList[taskIndex] = doneTask
                            taskFromMeLocalData.allTasks.done = allTaskList.toList()
                            updatedTask = doneTask
                            sharedViewModel?.isFromMeUnread?.value = true
                            sessionManager.saveFromMeUnread(true)
                        }
                        taskDao.insertTaskData(
                            taskFromMeLocalData
                        )
                        // send task data for ui update
                        EventBus.getDefault().post(LocalEvents.TaskForwardEvent(updatedTask))
                        EventBus.getDefault().post(LocalEvents.RefreshTasksEvent())
                    }
                }
            }
        }
        /// Update record updated_at
        updatedTask?.updateUpdatedAt()
        return updatedTask
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
                invitedMembers = eventData.invitedMembers,
                v = null
            )
            val taskEventList: MutableList<Events> = mutableListOf()
            taskEventList.add(taskEvent)

            val hiddenByCurrentUser = eventData.oldTaskData.isHiddenByMe
            if (hiddenByCurrentUser) {
                //it means task must be searched from hidden rootState and child states[ongoing and done] and then move the task to another root state from hidden
                launch {
                    val taskHiddenLocalData = taskDao.getTasks(TaskRootStateTags.Hidden.tagValue)

                    if (taskHiddenLocalData != null) {
                        val ongoingTask =
                            taskHiddenLocalData.allTasks.ongoing.find { it.id == taskID }
                        val doneTask = taskHiddenLocalData.allTasks.done.find { it.id == taskID }

                        if (ongoingTask != null) {
                            /// Update record updated_at
                            ongoingTask.updateUpdatedAt()
                            val allOngoingTaskList =
                                taskHiddenLocalData.allTasks.ongoing.toMutableList()
                            val taskIndex = allOngoingTaskList.indexOf(ongoingTask)

                            var oldEvents = ongoingTask.events.toMutableList()
                            if (oldEvents.isNotEmpty()) {
                                val oldOnlyEvent = oldEvents.find { it.id == eventData.id }
                                if (oldOnlyEvent != null) {     //means event already exist, so replace it
                                    val oldEventIndex = oldEvents.indexOf(oldOnlyEvent)
                                    oldEvents[oldEventIndex] = taskEvent
                                    ongoingTask.events = oldEvents.toList()
                                } else {
                                    oldEvents.add(taskEvent)
                                    ongoingTask.events = oldEvents.toList()
                                }
                            } else {
                                oldEvents = taskEventList
                                ongoingTask.events = oldEvents.toList()
                            }
                            ongoingTask.hiddenBy = listOf()
                            ongoingTask.seenBy = eventData.taskData.seenBy
                            ongoingTask.creatorState = eventData.taskData.creatorState
                            val assignToList = ongoingTask.assignedToState
                            assignToList.map {
                                it.state = eventData.taskData.creatorState
                            }     //as creator state is canceled, so all assignee will be canceled
                            ongoingTask.assignedToState = assignToList

                            allOngoingTaskList.removeAt(taskIndex)
                            taskHiddenLocalData.allTasks.ongoing = allOngoingTaskList.toList()

                            val allCanceledTaskList =
                                taskHiddenLocalData.allTasks.canceled.toMutableList()
                            val canceledTask =
                                taskHiddenLocalData.allTasks.canceled.find { it.id == taskID }
                            if (canceledTask != null) {
                                /// Update record updated_at
                                canceledTask.updateUpdatedAt()
                                val canceledTaskIndex =
                                    allCanceledTaskList.indexOf(canceledTask)
                                allCanceledTaskList[canceledTaskIndex] = ongoingTask
                                taskHiddenLocalData.allTasks.canceled =
                                    allCanceledTaskList.toList()
                            } else {
                                allCanceledTaskList.add(0, ongoingTask)
                                taskHiddenLocalData.allTasks.canceled =
                                    allCanceledTaskList.toList()
                            }
                            updatedTask = ongoingTask
                            if (ongoingTask.creator.id != userId) {
                                sharedViewModel?.isHiddenUnread?.postValue(true)
                                sessionManager.saveHiddenUnread(true)
                            }

                        } else if (doneTask != null) {
                            /// Update record updated_at
                            doneTask.updateUpdatedAt()
                            val allDoneTaskList = taskHiddenLocalData.allTasks.done.toMutableList()
                            val taskIndex = allDoneTaskList.indexOf(doneTask)

                            var oldEvents = doneTask.events.toMutableList()
                            if (oldEvents.isNotEmpty()) {
                                val oldOnlyEvent = oldEvents.find { it.id == eventData.id }
                                if (oldOnlyEvent != null) {     //means event already exist, so replace it
                                    val oldEventIndex = oldEvents.indexOf(oldOnlyEvent)
                                    oldEvents[oldEventIndex] = taskEvent
                                    doneTask.events = oldEvents.toList()
                                } else {
                                    oldEvents.add(taskEvent)
                                    doneTask.events = oldEvents.toList()
                                }
                            } else {
                                oldEvents = taskEventList
                                doneTask.events = oldEvents.toList()
                            }
                            doneTask.hiddenBy = listOf()
                            doneTask.seenBy = eventData.taskData.seenBy
                            doneTask.creatorState = eventData.taskData.creatorState
                            val assignToList = doneTask.assignedToState
                            assignToList.map {
                                it.state = eventData.taskData.creatorState
                            }     //as creator state is canceled, so all assignee will be canceled
                            doneTask.assignedToState = assignToList

                            allDoneTaskList.removeAt(taskIndex)
                            taskHiddenLocalData.allTasks.done = allDoneTaskList.toList()

                            val allCanceledTaskList =
                                taskHiddenLocalData.allTasks.canceled.toMutableList()
                            val canceledTask =
                                taskHiddenLocalData.allTasks.canceled.find { it.id == taskID }
                            if (canceledTask != null) {
                                /// Update record updated_at
                                canceledTask.updateUpdatedAt()
                                val canceledTaskIndex =
                                    allCanceledTaskList.indexOf(canceledTask)
                                allCanceledTaskList[canceledTaskIndex] = doneTask
                                taskHiddenLocalData.allTasks.canceled =
                                    allCanceledTaskList.toList()
                            } else {
                                allCanceledTaskList.add(0, doneTask)
                                taskHiddenLocalData.allTasks.canceled =
                                    allCanceledTaskList.toList()
                            }
                            updatedTask = doneTask
                            if (doneTask.creator.id != userId) {
                                sharedViewModel?.isHiddenUnread?.postValue(true)
                                sessionManager.saveHiddenUnread(true)
                            }
                        }

                        taskDao.insertTaskData(
                            taskHiddenLocalData
                        )
                        // send task data for ui update
                        EventBus.getDefault().post(LocalEvents.TaskForwardEvent(updatedTask))
                        EventBus.getDefault().post(LocalEvents.RefreshTasksEvent())
                    }

                }
            } else if (eventData.oldTaskData.creatorState.equals(TaskStatus.CANCELED.name, true)) {
                // it means task must be in hidden rootState and child state will be canceled. search and update the task only
                launch {
                    val taskHiddenLocalData = taskDao.getTasks(TaskRootStateTags.Hidden.tagValue)

                    if (taskHiddenLocalData != null) {
                        val canceledTask =
                            taskHiddenLocalData.allTasks.canceled.find { it.id == taskID }
                        if (canceledTask != null) {
                            /// Update record updated_at
                            canceledTask.updateUpdatedAt()
                            val allCancelTaskList =
                                taskHiddenLocalData.allTasks.canceled.toMutableList()
                            val taskIndex = allCancelTaskList.indexOf(canceledTask)

                            canceledTask.hiddenBy = listOf()
                            canceledTask.seenBy = eventData.taskData.seenBy
                            canceledTask.creatorState = eventData.taskData.creatorState
                            val assignToList = canceledTask.assignedToState
                            assignToList.map {
                                it.state = eventData.taskData.creatorState
                            }     //as creator state is canceled, so all assignee will be canceled
                            canceledTask.assignedToState = assignToList

                            allCancelTaskList[taskIndex] = canceledTask
                            taskHiddenLocalData.allTasks.canceled = allCancelTaskList.toList()

                            updatedTask = canceledTask
                            taskDao.insertTaskData(
                                taskHiddenLocalData
                            )

                            if (canceledTask.creator.id != userId) {
                                sharedViewModel?.isHiddenUnread?.postValue(true)
                                sessionManager.saveHiddenUnread(true)
                            }
                            // send task data for ui update
                            EventBus.getDefault().post(LocalEvents.TaskForwardEvent(updatedTask))
                            EventBus.getDefault().post(LocalEvents.RefreshTasksEvent())
                        }
                    }
                }
            }
            launch {
                val taskToMeLocalData = taskDao.getTasks(TaskRootStateTags.ToMe.tagValue)
                val taskFromMeLocalData = taskDao.getTasks(TaskRootStateTags.FromMe.tagValue)
                val taskHiddenLocalData = taskDao.getTasks(TaskRootStateTags.Hidden.tagValue)

                if (eventData.oldTaskData.isAssignedToMe) {
                    if (taskToMeLocalData != null) {
                        val newTask = taskToMeLocalData.allTasks.new.find { it.id == taskID }
                        val ongoingTask =
                            taskToMeLocalData.allTasks.ongoing.find { it.id == taskID }
                        val doneTask = taskToMeLocalData.allTasks.done.find { it.id == taskID }

                        if (taskHiddenLocalData != null) {
                            if (newTask != null) {
                                /// Update record updated_at
                                newTask.updateUpdatedAt()
                                val allTaskList = taskToMeLocalData.allTasks.new.toMutableList()
                                val taskIndex = allTaskList.indexOf(newTask)

                                var oldEvents = newTask.events.toMutableList()
                                if (oldEvents.isNotEmpty()) {
                                    val oldOnlyEvent = oldEvents.find { it.id == eventData.id }
                                    if (oldOnlyEvent != null) {     //means event already exist, so replace it
                                        val oldEventIndex = oldEvents.indexOf(oldOnlyEvent)
                                        oldEvents[oldEventIndex] = taskEvent
                                        newTask.events = oldEvents.toList()
                                    } else {
                                        oldEvents.add(taskEvent)
                                        newTask.events = oldEvents.toList()
                                    }
                                } else {
                                    oldEvents = taskEventList
                                    newTask.events = oldEvents.toList()
                                }
                                newTask.hiddenBy = listOf()
                                newTask.seenBy = eventData.taskData.seenBy
                                newTask.creatorState = eventData.taskData.creatorState
                                val assignToList = newTask.assignedToState
                                assignToList.map {
                                    it.state = eventData.taskData.creatorState
                                }     //as creator state is canceled, so all assignee will be canceled
                                newTask.assignedToState = assignToList

                                allTaskList.removeAt(taskIndex)
                                taskToMeLocalData.allTasks.new = allTaskList.toList()

                                val allCanceledTaskList =
                                    taskHiddenLocalData.allTasks.canceled.toMutableList()
                                val canceledTask =
                                    taskHiddenLocalData.allTasks.canceled.find { it.id == taskID }
                                if (canceledTask != null) {
                                    /// Update record updated_at
                                    canceledTask.updateUpdatedAt()
                                    val canceledTaskIndex =
                                        allCanceledTaskList.indexOf(canceledTask)
                                    allCanceledTaskList[canceledTaskIndex] = newTask
                                    taskHiddenLocalData.allTasks.canceled =
                                        allCanceledTaskList.toList()
                                } else {
                                    allCanceledTaskList.add(0, newTask)
                                    taskHiddenLocalData.allTasks.canceled =
                                        allCanceledTaskList.toList()
                                }
                                updatedTask = newTask
                                if (newTask.creator.id != userId) {
                                    sharedViewModel?.isHiddenUnread?.postValue(true)
                                    sessionManager.saveHiddenUnread(true)
                                }

                            } else if (ongoingTask != null) {
                                /// Update record updated_at
                                ongoingTask.updateUpdatedAt()
                                val allTaskList =
                                    taskToMeLocalData.allTasks.ongoing.toMutableList()
                                val taskIndex = allTaskList.indexOf(ongoingTask)

                                var oldEvents = ongoingTask.events.toMutableList()
                                if (oldEvents.isNotEmpty()) {
                                    val oldOnlyEvent = oldEvents.find { it.id == eventData.id }
                                    if (oldOnlyEvent != null) {     //means event already exist, so replace it
                                        val oldEventIndex = oldEvents.indexOf(oldOnlyEvent)
                                        oldEvents[oldEventIndex] = taskEvent
                                        ongoingTask.events = oldEvents.toList()
                                    } else {
                                        oldEvents.add(taskEvent)
                                        ongoingTask.events = oldEvents.toList()
                                    }
                                } else {
                                    oldEvents = taskEventList
                                    ongoingTask.events = oldEvents.toList()
                                }
                                ongoingTask.hiddenBy = listOf()
                                ongoingTask.seenBy = eventData.taskData.seenBy
                                ongoingTask.creatorState = eventData.taskData.creatorState
                                val assignToList = ongoingTask.assignedToState
                                assignToList.map {
                                    it.state = eventData.taskData.creatorState
                                }     //as creator state is canceled, so all assignee will be canceled
                                ongoingTask.assignedToState = assignToList

                                allTaskList.removeAt(taskIndex)
                                taskToMeLocalData.allTasks.ongoing = allTaskList.toList()

                                val allCanceledTaskList =
                                    taskHiddenLocalData.allTasks.canceled.toMutableList()
                                val canceledTask =
                                    taskHiddenLocalData.allTasks.canceled.find { it.id == taskID }
                                if (canceledTask != null) {
                                    /// Update record updated_at
                                    canceledTask.updateUpdatedAt()
                                    val canceledTaskIndex =
                                        allCanceledTaskList.indexOf(canceledTask)
                                    allCanceledTaskList[canceledTaskIndex] = ongoingTask
                                    taskHiddenLocalData.allTasks.canceled =
                                        allCanceledTaskList.toList()
                                } else {
                                    allCanceledTaskList.add(0, ongoingTask)
                                    taskHiddenLocalData.allTasks.canceled =
                                        allCanceledTaskList.toList()
                                }
                                updatedTask = ongoingTask
                                if (ongoingTask.creator.id != userId) {
                                    sharedViewModel?.isHiddenUnread?.postValue(true)
                                    sessionManager.saveHiddenUnread(true)
                                }

                            } else if (doneTask != null) {
                                /// Update record updated_at
                                doneTask.updateUpdatedAt()
                                val allTaskList =
                                    taskToMeLocalData.allTasks.done.toMutableList()
                                val taskIndex = allTaskList.indexOf(doneTask)

                                var oldEvents = doneTask.events.toMutableList()
                                if (oldEvents.isNotEmpty()) {
                                    val oldOnlyEvent = oldEvents.find { it.id == eventData.id }
                                    if (oldOnlyEvent != null) {     //means event already exist, so replace it
                                        val oldEventIndex = oldEvents.indexOf(oldOnlyEvent)
                                        oldEvents[oldEventIndex] = taskEvent
                                        doneTask.events = oldEvents.toList()
                                    } else {
                                        oldEvents.add(taskEvent)
                                        doneTask.events = oldEvents.toList()
                                    }
                                } else {
                                    oldEvents = taskEventList
                                    doneTask.events = oldEvents.toList()
                                }
                                doneTask.hiddenBy = listOf()
                                doneTask.seenBy = eventData.taskData.seenBy
                                doneTask.creatorState = eventData.taskData.creatorState
                                val assignToList = doneTask.assignedToState
                                assignToList.map {
                                    it.state = eventData.taskData.creatorState
                                }     //as creator state is canceled, so all assignee will be canceled
                                doneTask.assignedToState = assignToList

                                allTaskList.removeAt(taskIndex)
                                taskToMeLocalData.allTasks.done = allTaskList.toList()

                                val allCanceledTaskList =
                                    taskHiddenLocalData.allTasks.canceled.toMutableList()
                                val canceledTask =
                                    taskHiddenLocalData.allTasks.canceled.find { it.id == taskID }
                                if (canceledTask != null) {
                                    /// Update record updated_at
                                    canceledTask.updateUpdatedAt()
                                    val canceledTaskIndex =
                                        allCanceledTaskList.indexOf(canceledTask)
                                    allCanceledTaskList[canceledTaskIndex] = doneTask
                                    taskHiddenLocalData.allTasks.canceled =
                                        allCanceledTaskList.toList()
                                } else {
                                    allCanceledTaskList.add(0, doneTask)
                                    taskHiddenLocalData.allTasks.canceled =
                                        allCanceledTaskList.toList()
                                }
                                updatedTask = doneTask
                                if (doneTask.creator.id != userId) {
                                    sharedViewModel?.isHiddenUnread?.postValue(true)
                                    sessionManager.saveHiddenUnread(true)
                                }
                            }

                            taskDao.insertTaskData(
                                taskToMeLocalData
                            )
                            taskDao.insertTaskData(
                                taskHiddenLocalData
                            )
                        }
                        // send task data for ui update
                        EventBus.getDefault().post(LocalEvents.TaskForwardEvent(updatedTask))
                        EventBus.getDefault().post(LocalEvents.RefreshTasksEvent())
                    }
                }

                if (eventData.oldTaskData.isCreator) {
                    if (taskFromMeLocalData != null) {
                        val unreadTask =
                            taskFromMeLocalData.allTasks.unread.find { it.id == taskID }
                        val ongoingTask =
                            taskFromMeLocalData.allTasks.ongoing.find { it.id == taskID }
                        val doneTask =
                            taskFromMeLocalData.allTasks.done.find { it.id == taskID }

                        if (taskHiddenLocalData != null) {
                            if (unreadTask != null) {
                                /// Update record updated_at
                                unreadTask.updateUpdatedAt()
                                val allTaskList =
                                    taskFromMeLocalData.allTasks.unread.toMutableList()
                                val taskIndex = allTaskList.indexOf(unreadTask)

                                var oldEvents = unreadTask.events.toMutableList()
                                if (oldEvents.isNotEmpty()) {
                                    val oldOnlyEvent = oldEvents.find { it.id == eventData.id }
                                    if (oldOnlyEvent != null) {     //means event already exist, so replace it
                                        val oldEventIndex = oldEvents.indexOf(oldOnlyEvent)
                                        oldEvents[oldEventIndex] = taskEvent
                                        unreadTask.events = oldEvents.toList()
                                    } else {
                                        oldEvents.add(taskEvent)
                                        unreadTask.events = oldEvents.toList()
                                    }
                                } else {
                                    oldEvents = taskEventList
                                    unreadTask.events = oldEvents.toList()
                                }
                                unreadTask.hiddenBy = listOf()
                                unreadTask.seenBy = eventData.taskData.seenBy
                                unreadTask.creatorState = eventData.taskData.creatorState
                                val assignToList = unreadTask.assignedToState
                                assignToList.map {
                                    it.state = eventData.taskData.creatorState
                                }     //as creator state is canceled, so all assignee will be canceled
                                unreadTask.assignedToState = assignToList

                                allTaskList.removeAt(taskIndex)
                                taskFromMeLocalData.allTasks.unread = allTaskList.toList()

                                val allCanceledTaskList =
                                    taskHiddenLocalData.allTasks.canceled.toMutableList()
                                val canceledTask =
                                    taskHiddenLocalData.allTasks.canceled.find { it.id == taskID }
                                if (canceledTask != null) {
                                    /// Update record updated_at
                                    canceledTask.updateUpdatedAt()
                                    val canceledTaskIndex =
                                        allCanceledTaskList.indexOf(canceledTask)
                                    allCanceledTaskList[canceledTaskIndex] = unreadTask
                                    taskHiddenLocalData.allTasks.canceled =
                                        allCanceledTaskList.toList()
                                } else {
                                    allCanceledTaskList.add(0, unreadTask)
                                    taskHiddenLocalData.allTasks.canceled =
                                        allCanceledTaskList.toList()
                                }
                                updatedTask = unreadTask
                                if (unreadTask.creator.id != userId) {
                                    sharedViewModel?.isHiddenUnread?.postValue(true)
                                    sessionManager.saveHiddenUnread(true)
                                }

                            } else if (ongoingTask != null) {
                                /// Update record updated_at
                                ongoingTask.updateUpdatedAt()
                                val allTaskList =
                                    taskFromMeLocalData.allTasks.ongoing.toMutableList()
                                val taskIndex = allTaskList.indexOf(ongoingTask)

                                var oldEvents = ongoingTask.events.toMutableList()
                                if (oldEvents.isNotEmpty()) {
                                    val oldOnlyEvent = oldEvents.find { it.id == eventData.id }
                                    if (oldOnlyEvent != null) {     //means event already exist, so replace it
                                        val oldEventIndex = oldEvents.indexOf(oldOnlyEvent)
                                        oldEvents[oldEventIndex] = taskEvent
                                        ongoingTask.events = oldEvents.toList()
                                    } else {
                                        oldEvents.add(taskEvent)
                                        ongoingTask.events = oldEvents.toList()
                                    }
                                } else {
                                    oldEvents = taskEventList
                                    ongoingTask.events = oldEvents.toList()
                                }
                                ongoingTask.hiddenBy = listOf()
                                ongoingTask.seenBy = eventData.taskData.seenBy
                                ongoingTask.creatorState = eventData.taskData.creatorState
                                val assignToList = ongoingTask.assignedToState
                                assignToList.map {
                                    it.state = eventData.taskData.creatorState
                                }     //as creator state is canceled, so all assignee will be canceled
                                ongoingTask.assignedToState = assignToList

                                allTaskList.removeAt(taskIndex)
                                taskFromMeLocalData.allTasks.ongoing = allTaskList.toList()

                                val allCanceledTaskList =
                                    taskHiddenLocalData.allTasks.canceled.toMutableList()
                                val canceledTask =
                                    taskHiddenLocalData.allTasks.canceled.find { it.id == taskID }
                                if (canceledTask != null) {
                                    /// Update record updated_at
                                    canceledTask.updateUpdatedAt()
                                    val canceledTaskIndex =
                                        allCanceledTaskList.indexOf(canceledTask)
                                    allCanceledTaskList[canceledTaskIndex] = ongoingTask
                                    taskHiddenLocalData.allTasks.canceled =
                                        allCanceledTaskList.toList()
                                } else {
                                    allCanceledTaskList.add(0, ongoingTask)
                                    taskHiddenLocalData.allTasks.canceled =
                                        allCanceledTaskList.toList()
                                }
                                updatedTask = ongoingTask
                                if (ongoingTask.creator.id != userId) {
                                    sharedViewModel?.isHiddenUnread?.postValue(true)
                                    sessionManager.saveHiddenUnread(true)
                                }

                            } else if (doneTask != null) {
                                /// Update record updated_at
                                doneTask.updateUpdatedAt()
                                val allTaskList =
                                    taskFromMeLocalData.allTasks.done.toMutableList()
                                val taskIndex = allTaskList.indexOf(doneTask)

                                var oldEvents = doneTask.events.toMutableList()
                                if (oldEvents.isNotEmpty()) {
                                    val oldOnlyEvent = oldEvents.find { it.id == eventData.id }
                                    if (oldOnlyEvent != null) {     //means event already exist, so replace it
                                        val oldEventIndex = oldEvents.indexOf(oldOnlyEvent)
                                        oldEvents[oldEventIndex] = taskEvent
                                        doneTask.events = oldEvents.toList()
                                    } else {
                                        oldEvents.add(taskEvent)
                                        doneTask.events = oldEvents.toList()
                                    }
                                } else {
                                    oldEvents = taskEventList
                                    doneTask.events = oldEvents.toList()
                                }
                                doneTask.hiddenBy = listOf()
                                doneTask.seenBy = eventData.taskData.seenBy
                                doneTask.creatorState = eventData.taskData.creatorState
                                val assignToList = doneTask.assignedToState
                                assignToList.map {
                                    it.state = eventData.taskData.creatorState
                                }     //as creator state is canceled, so all assignee will be canceled
                                doneTask.assignedToState = assignToList

                                allTaskList.removeAt(taskIndex)
                                taskFromMeLocalData.allTasks.done = allTaskList.toList()

                                val allCanceledTaskList =
                                    taskHiddenLocalData.allTasks.canceled.toMutableList()
                                val canceledTask =
                                    taskHiddenLocalData.allTasks.canceled.find { it.id == taskID }
                                if (canceledTask != null) {
                                    /// Update record updated_at
                                    canceledTask.updateUpdatedAt()
                                    val canceledTaskIndex =
                                        allCanceledTaskList.indexOf(canceledTask)
                                    allCanceledTaskList[canceledTaskIndex] = doneTask
                                    taskHiddenLocalData.allTasks.canceled =
                                        allCanceledTaskList.toList()
                                } else {
                                    allCanceledTaskList.add(0, doneTask)
                                    taskHiddenLocalData.allTasks.canceled =
                                        allCanceledTaskList.toList()
                                }
                                updatedTask = doneTask
                                if (doneTask.creator.id != userId) {
                                    sharedViewModel?.isHiddenUnread?.postValue(true)
                                    sessionManager.saveHiddenUnread(true)
                                }
                            }

                            taskDao.insertTaskData(
                                taskFromMeLocalData
                            )

                            taskDao.insertTaskData(
                                taskHiddenLocalData
                            )
                        }
                        // send task data for ui update
                        EventBus.getDefault().post(LocalEvents.TaskForwardEvent(updatedTask))
                        EventBus.getDefault().post(LocalEvents.RefreshTasksEvent())
                    }
                }
            }
        }
        return updatedTask
    }

    fun updateTaskDoneInLocal(
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
                invitedMembers = eventData.invitedMembers,
                v = null
            )
            val taskEventList: MutableList<Events> = mutableListOf()
            taskEventList.add(taskEvent)

            val hiddenByCurrentUser = eventData.oldTaskData.isHiddenByMe
            if (hiddenByCurrentUser) {
                //it means task must be searched from hidden rootState and child states[ongoing and done] and then move the task to another root state from hidden
                launch {
                    val taskHiddenLocalData = taskDao.getTasks(TaskRootStateTags.Hidden.tagValue)
                    val taskToMeLocalData = taskDao.getTasks(TaskRootStateTags.ToMe.tagValue)
                    val taskFromMeLocalData = taskDao.getTasks(TaskRootStateTags.FromMe.tagValue)

                    if (taskHiddenLocalData != null) {
                        val ongoingTask =
                            taskHiddenLocalData.allTasks.ongoing.find { it.id == taskID }
                        val doneTask = taskHiddenLocalData.allTasks.done.find { it.id == taskID }

                        if (ongoingTask != null) {
                            /// Update record updated_at
                            ongoingTask.updateUpdatedAt()
                            val allOngoingTaskList =
                                taskHiddenLocalData.allTasks.ongoing.toMutableList()
                            val taskIndex = allOngoingTaskList.indexOf(ongoingTask)

                            var oldEvents = ongoingTask.events.toMutableList()
                            if (oldEvents.isNotEmpty()) {
                                val oldOnlyEvent = oldEvents.find { it.id == eventData.id }
                                if (oldOnlyEvent != null) {     //means event already exist, so replace it
                                    val oldEventIndex = oldEvents.indexOf(oldOnlyEvent)
                                    oldEvents[oldEventIndex] = taskEvent
                                    ongoingTask.events = oldEvents.toList()
                                } else {
                                    oldEvents.add(taskEvent)
                                    ongoingTask.events = oldEvents.toList()
                                }
                            } else {
                                oldEvents = taskEventList
                                ongoingTask.events = oldEvents.toList()
                            }
                            ongoingTask.hiddenBy = listOf()
                            ongoingTask.seenBy = eventData.taskData.seenBy
                            ongoingTask.creatorState = eventData.taskData.creatorState
                            val assignToList = ongoingTask.assignedToState
                            assignToList.map {
                                it.state = eventData.taskData.creatorState
                            }     //as creator state is canceled, so all assignee will be canceled
                            ongoingTask.assignedToState = assignToList

                            allOngoingTaskList.removeAt(taskIndex)
                            taskHiddenLocalData.allTasks.ongoing = allOngoingTaskList.toList()

                            if (eventData.oldTaskData.isAssignedToMe) {
                                if (taskToMeLocalData != null) {
                                    val doneTaskToMe =
                                        taskToMeLocalData.allTasks.done.find { it.id == taskID }
                                    val allDoneToMeTaskList =
                                        taskToMeLocalData.allTasks.done.toMutableList()
                                    if (doneTaskToMe != null) {
                                        /// Update record updated_at
                                        doneTaskToMe.updateUpdatedAt()
                                        val index = allDoneToMeTaskList.indexOf(doneTaskToMe)
                                        allDoneToMeTaskList[index] = ongoingTask
                                        taskToMeLocalData.allTasks.done =
                                            allDoneToMeTaskList.toList()
                                    } else {
                                        allDoneToMeTaskList.add(0, ongoingTask)
                                        taskToMeLocalData.allTasks.done =
                                            allDoneToMeTaskList.toList()
                                    }
                                    sharedViewModel?.isToMeUnread?.postValue(true)
                                    sessionManager.saveToMeUnread(true)
                                }
                            }
                            if (eventData.oldTaskData.isCreator) {
                                if (taskFromMeLocalData != null) {
                                    val doneTaskFromMe =
                                        taskFromMeLocalData.allTasks.done.find { it.id == taskID }
                                    val allDoneFromMeTaskList =
                                        taskFromMeLocalData.allTasks.done.toMutableList()
                                    if (doneTaskFromMe != null) {
                                        /// Update record updated_at
                                        doneTaskFromMe.updateUpdatedAt()
                                        val index =
                                            allDoneFromMeTaskList.indexOf(doneTaskFromMe)
                                        allDoneFromMeTaskList[index] = ongoingTask
                                        taskFromMeLocalData.allTasks.done =
                                            allDoneFromMeTaskList.toList()

                                        sharedViewModel?.isFromMeUnread?.postValue(true)
                                        sessionManager.saveFromMeUnread(true)
                                    }
                                }
                            }
                            updatedTask = ongoingTask

                        } else if (doneTask != null) {
                            /// Update record updated_at
                            doneTask.updateUpdatedAt()
                            val allDoneTaskList = taskHiddenLocalData.allTasks.done.toMutableList()
                            val taskIndex = allDoneTaskList.indexOf(doneTask)

                            var oldEvents = doneTask.events.toMutableList()
                            if (oldEvents.isNotEmpty()) {
                                val oldOnlyEvent = oldEvents.find { it.id == eventData.id }
                                if (oldOnlyEvent != null) {     //means event already exist, so replace it
                                    val oldEventIndex = oldEvents.indexOf(oldOnlyEvent)
                                    oldEvents[oldEventIndex] = taskEvent
                                    doneTask.events = oldEvents.toList()
                                } else {
                                    oldEvents.add(taskEvent)
                                    doneTask.events = oldEvents.toList()
                                }
                            } else {
                                oldEvents = taskEventList
                                doneTask.events = oldEvents.toList()
                            }
                            doneTask.hiddenBy = listOf()
                            doneTask.seenBy = eventData.taskData.seenBy
                            doneTask.creatorState = eventData.taskData.creatorState
                            val assignToList = doneTask.assignedToState
                            assignToList.map {
                                it.state = eventData.taskData.creatorState
                            }     //as creator state is canceled, so all assignee will be canceled
                            doneTask.assignedToState = assignToList

                            allDoneTaskList.removeAt(taskIndex)
                            taskHiddenLocalData.allTasks.done = allDoneTaskList.toList()

                            if (eventData.oldTaskData.isAssignedToMe) {
                                if (taskToMeLocalData != null) {
                                    val doneTaskToMe =
                                        taskToMeLocalData.allTasks.done.find { it.id == taskID }
                                    val allDoneToMeTaskList =
                                        taskToMeLocalData.allTasks.done.toMutableList()
                                    if (doneTaskToMe != null) {
                                        /// Update record updated_at
                                        doneTaskToMe.updateUpdatedAt()
                                        val index = allDoneToMeTaskList.indexOf(doneTaskToMe)
                                        allDoneToMeTaskList[index] = doneTask
                                        taskToMeLocalData.allTasks.done =
                                            allDoneToMeTaskList.toList()
                                    } else {
                                        allDoneToMeTaskList.add(0, doneTask)
                                        taskToMeLocalData.allTasks.done =
                                            allDoneToMeTaskList.toList()
                                    }
                                    sharedViewModel?.isToMeUnread?.postValue(true)
                                    sessionManager.saveToMeUnread(true)
                                }
                            }
                            if (eventData.oldTaskData.isCreator) {
                                if (taskFromMeLocalData != null) {
                                    val doneTaskFromMe =
                                        taskFromMeLocalData.allTasks.done.find { it.id == taskID }
                                    val allDoneFromMeTaskList =
                                        taskFromMeLocalData.allTasks.done.toMutableList()
                                    if (doneTaskFromMe != null) {
                                        /// Update record updated_at
                                        doneTaskFromMe.updateUpdatedAt()
                                        val index =
                                            allDoneFromMeTaskList.indexOf(doneTaskFromMe)
                                        allDoneFromMeTaskList[index] = doneTask
                                        taskFromMeLocalData.allTasks.done =
                                            allDoneFromMeTaskList.toList()

                                        sharedViewModel?.isFromMeUnread?.postValue(true)
                                        sessionManager.saveFromMeUnread(true)
                                    }
                                }
                            }
                            updatedTask = doneTask
                        }

                        taskDao.insertTaskData(
                            taskHiddenLocalData
                        )
                        if (taskToMeLocalData != null) {
                            taskDao.insertTaskData(
                                taskToMeLocalData
                            )
                        }
                        if (taskFromMeLocalData != null) {
                            taskDao.insertTaskData(
                                taskFromMeLocalData
                            )
                        }
                        // send task data for ui update
                        EventBus.getDefault().post(LocalEvents.TaskForwardEvent(updatedTask))
                        EventBus.getDefault().post(LocalEvents.RefreshTasksEvent())
                    }

                }
            }
            launch {
                val taskToMeLocalData = taskDao.getTasks(TaskRootStateTags.ToMe.tagValue)
                val taskFromMeLocalData = taskDao.getTasks(TaskRootStateTags.FromMe.tagValue)
                val taskHiddenLocalData = taskDao.getTasks(TaskRootStateTags.Hidden.tagValue)

                if (eventData.oldTaskData.isAssignedToMe) {
                    if (taskToMeLocalData != null) {
                        val newTask = taskToMeLocalData.allTasks.new.find { it.id == taskID }
                        val ongoingTask =
                            taskToMeLocalData.allTasks.ongoing.find { it.id == taskID }
                        val doneTask = taskToMeLocalData.allTasks.done.find { it.id == taskID }


                        if (newTask != null) {
                            /// Update record updated_at
                            newTask.updateUpdatedAt()
                            val allTaskList = taskToMeLocalData.allTasks.new.toMutableList()
                            val taskIndex = allTaskList.indexOf(newTask)

                            var oldEvents = newTask.events.toMutableList()
                            if (oldEvents.isNotEmpty()) {
                                val oldOnlyEvent = oldEvents.find { it.id == eventData.id }
                                if (oldOnlyEvent != null) {     //means event already exist, so replace it
                                    val oldEventIndex = oldEvents.indexOf(oldOnlyEvent)
                                    oldEvents[oldEventIndex] = taskEvent
                                    newTask.events = oldEvents.toList()
                                } else {
                                    oldEvents.add(taskEvent)
                                    newTask.events = oldEvents.toList()
                                }
                            } else {
                                oldEvents = taskEventList
                                newTask.events = oldEvents.toList()
                            }
                            newTask.hiddenBy = listOf()
                            newTask.seenBy = eventData.taskData.seenBy
                            newTask.creatorState = eventData.taskData.creatorState
                            val assignToList = newTask.assignedToState
                            assignToList.map {
                                it.state = eventData.taskData.creatorState
                            }     //as creator state is done, so all assignee will be done
                            newTask.assignedToState = assignToList

                            allTaskList.removeAt(taskIndex)
                            taskToMeLocalData.allTasks.new = allTaskList.toList()

                            val doneTaskToMe =
                                taskToMeLocalData.allTasks.done.find { it.id == taskID }
                            val allDoneToMeTaskList =
                                taskToMeLocalData.allTasks.done.toMutableList()
                            if (doneTaskToMe != null) {
                                /// Update record updated_at
                                doneTaskToMe.updateUpdatedAt()
                                val index = allDoneToMeTaskList.indexOf(doneTaskToMe)
                                allDoneToMeTaskList[index] = newTask
                                taskToMeLocalData.allTasks.done = allDoneToMeTaskList.toList()
                            } else {
                                allDoneToMeTaskList.add(0, newTask)
                                taskToMeLocalData.allTasks.done = allDoneToMeTaskList.toList()
                            }
                            updatedTask = newTask
                            sharedViewModel?.isToMeUnread?.postValue(true)
                            sessionManager.saveToMeUnread(true)

                        } else if (ongoingTask != null) {
                            /// Update record updated_at
                            ongoingTask.updateUpdatedAt()
                            val allTaskList =
                                taskToMeLocalData.allTasks.ongoing.toMutableList()
                            val taskIndex = allTaskList.indexOf(ongoingTask)

                            var oldEvents = ongoingTask.events.toMutableList()
                            if (oldEvents.isNotEmpty()) {
                                val oldOnlyEvent = oldEvents.find { it.id == eventData.id }
                                if (oldOnlyEvent != null) {     //means event already exist, so replace it
                                    val oldEventIndex = oldEvents.indexOf(oldOnlyEvent)
                                    oldEvents[oldEventIndex] = taskEvent
                                    ongoingTask.events = oldEvents.toList()
                                } else {
                                    oldEvents.add(taskEvent)
                                    ongoingTask.events = oldEvents.toList()
                                }
                            } else {
                                oldEvents = taskEventList
                                ongoingTask.events = oldEvents.toList()
                            }
                            ongoingTask.hiddenBy = listOf()
                            ongoingTask.seenBy = eventData.taskData.seenBy
                            ongoingTask.creatorState = eventData.taskData.creatorState
                            val assignToList = ongoingTask.assignedToState
                            assignToList.map {
                                it.state = eventData.taskData.creatorState
                            }     //as creator state is canceled, so all assignee will be canceled
                            ongoingTask.assignedToState = assignToList

                            allTaskList.removeAt(taskIndex)
                            taskToMeLocalData.allTasks.ongoing = allTaskList.toList()

                            val doneTaskToMe =
                                taskToMeLocalData.allTasks.done.find { it.id == taskID }
                            val allDoneToMeTaskList =
                                taskToMeLocalData.allTasks.done.toMutableList()
                            if (doneTaskToMe != null) {
                                /// Update record updated_at
                                doneTaskToMe.updateUpdatedAt()
                                val index = allDoneToMeTaskList.indexOf(doneTaskToMe)
                                allDoneToMeTaskList[index] = ongoingTask
                                taskToMeLocalData.allTasks.done = allDoneToMeTaskList.toList()
                            } else {
                                allDoneToMeTaskList.add(0, ongoingTask)
                                taskToMeLocalData.allTasks.done = allDoneToMeTaskList.toList()
                            }
                            updatedTask = ongoingTask
                            sharedViewModel?.isToMeUnread?.postValue(true)
                            sessionManager.saveToMeUnread(true)

                        } else if (doneTask != null) {
                            /// Update record updated_at
                            doneTask.updateUpdatedAt()
                            val allTaskList =
                                taskToMeLocalData.allTasks.done.toMutableList()
                            val taskIndex = allTaskList.indexOf(doneTask)

                            var oldEvents = doneTask.events.toMutableList()
                            if (oldEvents.isNotEmpty()) {
                                val oldOnlyEvent = oldEvents.find { it.id == eventData.id }
                                if (oldOnlyEvent != null) {     //means event already exist, so replace it
                                    val oldEventIndex = oldEvents.indexOf(oldOnlyEvent)
                                    oldEvents[oldEventIndex] = taskEvent
                                    doneTask.events = oldEvents.toList()
                                } else {
                                    oldEvents.add(taskEvent)
                                    doneTask.events = oldEvents.toList()
                                }
                            } else {
                                oldEvents = taskEventList
                                doneTask.events = oldEvents.toList()
                            }
                            doneTask.hiddenBy = listOf()
                            doneTask.seenBy = eventData.taskData.seenBy
                            doneTask.creatorState = eventData.taskData.creatorState
                            val assignToList = doneTask.assignedToState
                            assignToList.map {
                                it.state = eventData.taskData.creatorState
                            }     //as creator state is canceled, so all assignee will be canceled
                            doneTask.assignedToState = assignToList

                            allTaskList[taskIndex] = doneTask
                            taskToMeLocalData.allTasks.done = allTaskList.toList()

                            updatedTask = doneTask
                            sharedViewModel?.isToMeUnread?.postValue(true)
                            sessionManager.saveToMeUnread(true)
                        }

                        taskDao.insertTaskData(
                            taskToMeLocalData
                        )

                        // send task data for ui update
                        EventBus.getDefault().post(LocalEvents.TaskForwardEvent(updatedTask))
                        EventBus.getDefault().post(LocalEvents.RefreshTasksEvent())
                    }
                }

                if (eventData.oldTaskData.isCreator) {
                    if (taskFromMeLocalData != null) {
                        val unreadTask =
                            taskFromMeLocalData.allTasks.unread.find { it.id == taskID }
                        val ongoingTask =
                            taskFromMeLocalData.allTasks.ongoing.find { it.id == taskID }
                        val doneTask =
                            taskFromMeLocalData.allTasks.done.find { it.id == taskID }


                        if (unreadTask != null) {
                            /// Update record updated_at
                            unreadTask.updateUpdatedAt()
                            val allTaskList =
                                taskFromMeLocalData.allTasks.unread.toMutableList()
                            val taskIndex = allTaskList.indexOf(unreadTask)

                            var oldEvents = unreadTask.events.toMutableList()
                            if (oldEvents.isNotEmpty()) {
                                val oldOnlyEvent = oldEvents.find { it.id == eventData.id }
                                if (oldOnlyEvent != null) {     //means event already exist, so replace it
                                    val oldEventIndex = oldEvents.indexOf(oldOnlyEvent)
                                    oldEvents[oldEventIndex] = taskEvent
                                    unreadTask.events = oldEvents.toList()
                                } else {
                                    oldEvents.add(taskEvent)
                                    unreadTask.events = oldEvents.toList()
                                }
                            } else {
                                oldEvents = taskEventList
                                unreadTask.events = oldEvents.toList()
                            }
                            unreadTask.hiddenBy = listOf()
                            unreadTask.seenBy = eventData.taskData.seenBy
                            unreadTask.creatorState = eventData.taskData.creatorState
                            val assignToList = unreadTask.assignedToState
                            assignToList.map {
                                it.state = eventData.taskData.creatorState
                            }     //as creator state is canceled, so all assignee will be canceled
                            unreadTask.assignedToState = assignToList

                            allTaskList.removeAt(taskIndex)
                            taskFromMeLocalData.allTasks.unread = allTaskList.toList()

                            val doneTaskFromMe =
                                taskFromMeLocalData.allTasks.done.find { it.id == taskID }
                            val allDoneFromMeTaskList =
                                taskFromMeLocalData.allTasks.done.toMutableList()
                            if (doneTaskFromMe != null) {
                                /// Update record updated_at
                                doneTaskFromMe.updateUpdatedAt()
                                val index =
                                    allDoneFromMeTaskList.indexOf(doneTaskFromMe)
                                allDoneFromMeTaskList[index] = unreadTask
                                taskFromMeLocalData.allTasks.done =
                                    allDoneFromMeTaskList.toList()
                            } else {
                                allDoneFromMeTaskList.add(0, unreadTask)
                                taskFromMeLocalData.allTasks.done =
                                    allDoneFromMeTaskList.toList()
                            }
                            updatedTask = unreadTask
                            sharedViewModel?.isFromMeUnread?.postValue(true)
                            sessionManager.saveFromMeUnread(true)

                        } else if (ongoingTask != null) {
                            /// Update record updated_at
                            ongoingTask.updateUpdatedAt()
                            val allTaskList =
                                taskFromMeLocalData.allTasks.ongoing.toMutableList()
                            val taskIndex = allTaskList.indexOf(ongoingTask)

                            var oldEvents = ongoingTask.events.toMutableList()
                            if (oldEvents.isNotEmpty()) {
                                val oldOnlyEvent = oldEvents.find { it.id == eventData.id }
                                if (oldOnlyEvent != null) {     //means event already exist, so replace it
                                    val oldEventIndex = oldEvents.indexOf(oldOnlyEvent)
                                    oldEvents[oldEventIndex] = taskEvent
                                    ongoingTask.events = oldEvents.toList()
                                } else {
                                    oldEvents.add(taskEvent)
                                    ongoingTask.events = oldEvents.toList()
                                }
                            } else {
                                oldEvents = taskEventList
                                ongoingTask.events = oldEvents.toList()
                            }
                            ongoingTask.hiddenBy = listOf()
                            ongoingTask.seenBy = eventData.taskData.seenBy
                            ongoingTask.creatorState = eventData.taskData.creatorState
                            val assignToList = ongoingTask.assignedToState
                            assignToList.map {
                                it.state = eventData.taskData.creatorState
                            }     //as creator state is canceled, so all assignee will be canceled
                            ongoingTask.assignedToState = assignToList

                            allTaskList.removeAt(taskIndex)
                            taskFromMeLocalData.allTasks.ongoing = allTaskList.toList()

                            val doneTaskFromMe =
                                taskFromMeLocalData.allTasks.done.find { it.id == taskID }
                            val allDoneFromMeTaskList =
                                taskFromMeLocalData.allTasks.done.toMutableList()
                            if (doneTaskFromMe != null) {
                                /// Update record updated_at
                                doneTaskFromMe.updateUpdatedAt()
                                val index =
                                    allDoneFromMeTaskList.indexOf(doneTaskFromMe)
                                allDoneFromMeTaskList[index] = ongoingTask
                                taskFromMeLocalData.allTasks.done =
                                    allDoneFromMeTaskList.toList()
                            } else {
                                allDoneFromMeTaskList.add(0, ongoingTask)
                                taskFromMeLocalData.allTasks.done =
                                    allDoneFromMeTaskList.toList()
                            }
                            updatedTask = ongoingTask
                            sharedViewModel?.isFromMeUnread?.postValue(true)
                            sessionManager.saveFromMeUnread(true)

                        } else if (doneTask != null) {
                            /// Update record updated_at
                            doneTask.updateUpdatedAt()
                            val allTaskList =
                                taskFromMeLocalData.allTasks.done.toMutableList()
                            val taskIndex = allTaskList.indexOf(doneTask)

                            var oldEvents = doneTask.events.toMutableList()
                            if (oldEvents.isNotEmpty()) {
                                val oldOnlyEvent = oldEvents.find { it.id == eventData.id }
                                if (oldOnlyEvent != null) {     //means event already exist, so replace it
                                    val oldEventIndex = oldEvents.indexOf(oldOnlyEvent)
                                    oldEvents[oldEventIndex] = taskEvent
                                    doneTask.events = oldEvents.toList()
                                } else {
                                    oldEvents.add(taskEvent)
                                    doneTask.events = oldEvents.toList()
                                }
                            } else {
                                oldEvents = taskEventList
                                doneTask.events = oldEvents.toList()
                            }
                            doneTask.hiddenBy = listOf()
                            doneTask.seenBy = eventData.taskData.seenBy
                            doneTask.creatorState = eventData.taskData.creatorState
                            val assignToList = doneTask.assignedToState
                            assignToList.map {
                                it.state = eventData.taskData.creatorState
                            }     //as creator state is canceled, so all assignee will be canceled
                            doneTask.assignedToState = assignToList

                            allTaskList[taskIndex] = doneTask
                            taskFromMeLocalData.allTasks.done = allTaskList.toList()

                            updatedTask = doneTask
                            sharedViewModel?.isFromMeUnread?.postValue(true)
                            sessionManager.saveFromMeUnread(true)
                        }

                        taskDao.insertTaskData(
                            taskFromMeLocalData
                        )

                        // send task data for ui update
                        EventBus.getDefault().post(LocalEvents.TaskForwardEvent(updatedTask))
                        EventBus.getDefault().post(LocalEvents.RefreshTasksEvent())
                    }
                }
            }
        }
        return updatedTask
    }


    fun updateTaskHideInLocal(hideData: HideTaskResponse?, taskDao: TaskV2Dao, userId: String?) {
        launch {
            val taskToMeLocalData = taskDao.getTasks(TaskRootStateTags.ToMe.tagValue)
            val taskHiddenLocalData = taskDao.getTasks(TaskRootStateTags.Hidden.tagValue)

            if (hideData != null) {
                if (taskToMeLocalData != null) {
                    val ongoingTask =
                        taskToMeLocalData.allTasks.ongoing.find { it.id == hideData.taskId }
                    val doneTask = taskToMeLocalData.allTasks.done.find { it.id == hideData.taskId }

                    if (taskHiddenLocalData != null) {
                        if (ongoingTask != null) {
                            /// Update record updated_at
                            ongoingTask.updateUpdatedAt()
                            val allTaskList = taskToMeLocalData.allTasks.ongoing.toMutableList()
                            val taskIndex = allTaskList.indexOf(ongoingTask)

                            ongoingTask.hiddenBy = hideData.hiddenBy

                            allTaskList.removeAt(taskIndex)
                            taskToMeLocalData.allTasks.ongoing = allTaskList.toList()


                            val allOngoingTaskList =
                                taskHiddenLocalData.allTasks.ongoing.toMutableList()
                            val hiddenOngoingTask =
                                taskHiddenLocalData.allTasks.ongoing.find { it.id == hideData.taskId }
                            if (hiddenOngoingTask != null) {
                                /// Update record updated_at
                                hiddenOngoingTask.updateUpdatedAt()
                                val ongoingTaskIndex = allOngoingTaskList.indexOf(hiddenOngoingTask)
                                allOngoingTaskList[ongoingTaskIndex] = ongoingTask
                                taskHiddenLocalData.allTasks.ongoing =
                                    allOngoingTaskList.toList()
                            } else {
                                allOngoingTaskList.add(0, ongoingTask)
                                taskHiddenLocalData.allTasks.ongoing =
                                    allOngoingTaskList.toList()
                            }
                        }
                        if (doneTask != null) {
                            /// Update record updated_at
                            doneTask.updateUpdatedAt()
                            val allTaskList = taskToMeLocalData.allTasks.done.toMutableList()
                            val taskIndex = allTaskList.indexOf(doneTask)

                            doneTask.hiddenBy = hideData.hiddenBy

                            allTaskList.removeAt(taskIndex)
                            taskToMeLocalData.allTasks.done = allTaskList.toList()


                            val allDoneTaskList =
                                taskHiddenLocalData.allTasks.done.toMutableList()
                            val hiddenDoneTask =
                                taskHiddenLocalData.allTasks.done.find { it.id == hideData.taskId }
                            if (hiddenDoneTask != null) {
                                /// Update record updated_at
                                hiddenDoneTask.updateUpdatedAt()
                                val doneTaskIndex = allDoneTaskList.indexOf(hiddenDoneTask)
                                allDoneTaskList[doneTaskIndex] = doneTask
                                taskHiddenLocalData.allTasks.done =
                                    allDoneTaskList.toList()
                            } else {
                                allDoneTaskList.add(0, doneTask)
                                taskHiddenLocalData.allTasks.done =
                                    allDoneTaskList.toList()
                            }
                        }

                        taskDao.insertTaskData(
                            taskToMeLocalData
                        )
                        taskDao.insertTaskData(
                            taskHiddenLocalData
                        )

                        EventBus.getDefault().post(LocalEvents.RefreshTasksEvent())
                    }
                }

                if (taskHiddenLocalData != null) {
                    val ongoingTask =
                        taskHiddenLocalData.allTasks.ongoing.find { it.id == hideData.taskId }
                    val doneTask =
                        taskHiddenLocalData.allTasks.done.find { it.id == hideData.taskId }

                    if (ongoingTask != null) {
                        /// Update record updated_at
                        ongoingTask.updateUpdatedAt()
                        val allTaskList = taskHiddenLocalData.allTasks.ongoing.toMutableList()
                        val taskIndex = allTaskList.indexOf(ongoingTask)

                        ongoingTask.hiddenBy = hideData.hiddenBy

                        allTaskList[taskIndex] = ongoingTask
                        taskHiddenLocalData.allTasks.ongoing =
                            allTaskList.toList()
                    }
                    if (doneTask != null) {
                        /// Update record updated_at
                        doneTask.updateUpdatedAt()
                        val allTaskList = taskHiddenLocalData.allTasks.done.toMutableList()
                        val taskIndex = allTaskList.indexOf(doneTask)

                        doneTask.hiddenBy = hideData.hiddenBy

                        allTaskList[taskIndex] = doneTask
                        taskHiddenLocalData.allTasks.done =
                            allTaskList.toList()
                    }

                    taskDao.insertTaskData(
                        taskHiddenLocalData
                    )

                    EventBus.getDefault().post(LocalEvents.RefreshTasksEvent())
                }
            }
        }

    }

    fun updateTaskUnHideInLocal(hideData: HideTaskResponse?, taskDao: TaskV2Dao, userId: String?) {
        launch {
            val taskToMeLocalData = taskDao.getTasks(TaskRootStateTags.ToMe.tagValue)
            val taskHiddenLocalData = taskDao.getTasks(TaskRootStateTags.Hidden.tagValue)

            if (hideData != null) {
                if (taskHiddenLocalData != null) {
                    val ongoingTask =
                        taskHiddenLocalData.allTasks.ongoing.find { it.id == hideData.taskId }
                    val doneTask =
                        taskHiddenLocalData.allTasks.done.find { it.id == hideData.taskId }

                    if (taskToMeLocalData != null) {
                        if (ongoingTask != null) {
                            /// Update record updated_at
                            ongoingTask.updateUpdatedAt()
                            val allTaskList = taskHiddenLocalData.allTasks.ongoing.toMutableList()
                            val taskIndex = allTaskList.indexOf(ongoingTask)

                            ongoingTask.hiddenBy = hideData.hiddenBy

                            allTaskList.removeAt(taskIndex)
                            taskHiddenLocalData.allTasks.ongoing = allTaskList.toList()


                            val allOngoingTaskList =
                                taskToMeLocalData.allTasks.ongoing.toMutableList()
                            val toMeOngoingTask =
                                taskToMeLocalData.allTasks.ongoing.find { it.id == hideData.taskId }
                            if (toMeOngoingTask != null) {
                                /// Update record updated_at
                                toMeOngoingTask.updateUpdatedAt()
                                val ongoingTaskIndex = allOngoingTaskList.indexOf(toMeOngoingTask)
                                allOngoingTaskList[ongoingTaskIndex] = ongoingTask
                                taskToMeLocalData.allTasks.ongoing =
                                    allOngoingTaskList.toList()
                            } else {
                                allOngoingTaskList.add(0, ongoingTask)
                                taskToMeLocalData.allTasks.ongoing =
                                    allOngoingTaskList.toList()
                            }
                        }
                        if (doneTask != null) {
                            /// Update record updated_at
                            doneTask.updateUpdatedAt()
                            val allTaskList = taskHiddenLocalData.allTasks.done.toMutableList()
                            val taskIndex = allTaskList.indexOf(doneTask)

                            doneTask.hiddenBy = hideData.hiddenBy

                            allTaskList.removeAt(taskIndex)
                            taskHiddenLocalData.allTasks.done = allTaskList.toList()


                            val allDoneTaskList =
                                taskToMeLocalData.allTasks.done.toMutableList()
                            val toMeDoneTask =
                                taskToMeLocalData.allTasks.done.find { it.id == hideData.taskId }
                            if (toMeDoneTask != null) {
                                /// Update record updated_at
                                toMeDoneTask.updateUpdatedAt()
                                val doneTaskIndex = allDoneTaskList.indexOf(toMeDoneTask)
                                allDoneTaskList[doneTaskIndex] = doneTask
                                taskToMeLocalData.allTasks.done =
                                    allDoneTaskList.toList()
                            } else {
                                allDoneTaskList.add(0, doneTask)
                                taskToMeLocalData.allTasks.done =
                                    allDoneTaskList.toList()
                            }
                        }

                        taskDao.insertTaskData(
                            taskToMeLocalData
                        )
                        taskDao.insertTaskData(
                            taskHiddenLocalData
                        )

                        EventBus.getDefault().post(LocalEvents.RefreshTasksEvent())
                    }
                }

                if (taskToMeLocalData != null) {
                    val ongoingTask =
                        taskToMeLocalData.allTasks.ongoing.find { it.id == hideData.taskId }
                    val doneTask = taskToMeLocalData.allTasks.done.find { it.id == hideData.taskId }

                    if (ongoingTask != null) {
                        /// Update record updated_at
                        ongoingTask.updateUpdatedAt()
                        val allTaskList = taskToMeLocalData.allTasks.ongoing.toMutableList()
                        val taskIndex = allTaskList.indexOf(ongoingTask)

                        ongoingTask.hiddenBy = hideData.hiddenBy

                        allTaskList[taskIndex] = ongoingTask
                        taskToMeLocalData.allTasks.ongoing =
                            allTaskList.toList()
                    }
                    if (doneTask != null) {
                        /// Update record updated_at
                        doneTask.updateUpdatedAt()
                        val allTaskList = taskToMeLocalData.allTasks.done.toMutableList()
                        val taskIndex = allTaskList.indexOf(doneTask)

                        doneTask.hiddenBy = hideData.hiddenBy

                        allTaskList[taskIndex] = doneTask
                        taskToMeLocalData.allTasks.done =
                            allTaskList.toList()
                    }

                    taskDao.insertTaskData(
                        taskToMeLocalData
                    )

                    EventBus.getDefault().post(LocalEvents.RefreshTasksEvent())
                }
            }
        }

    }
}


