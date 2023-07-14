package com.zstronics.ceibro.base.viewmodel


import android.content.Context
import android.net.Uri
import androidx.annotation.CallSuper
import androidx.lifecycle.*
import androidx.work.*
import com.zstronics.ceibro.base.clickevents.SingleClickEvent
import com.zstronics.ceibro.base.interfaces.IBase
import com.zstronics.ceibro.base.interfaces.OnClickHandler
import com.zstronics.ceibro.base.state.UIState
import com.zstronics.ceibro.data.database.dao.TaskV2Dao
import com.zstronics.ceibro.data.database.models.tasks.CeibroTaskV2
import com.zstronics.ceibro.data.database.models.tasks.Events
import com.zstronics.ceibro.data.repos.dashboard.attachment.AttachmentUploadRequest
import com.zstronics.ceibro.data.repos.task.TaskRootStateTags
import com.zstronics.ceibro.data.repos.task.models.TaskV2Response
import com.zstronics.ceibro.data.repos.task.models.TasksV2DatabaseEntity
import com.zstronics.ceibro.data.repos.task.models.v2.EventV2Response
import com.zstronics.ceibro.data.repos.task.models.v2.TaskSeenResponse
import com.zstronics.ceibro.ui.attachment.SubtaskAttachment
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

    fun updateCreatedTaskInLocal(task: CeibroTaskV2?, taskDao: TaskV2Dao, userId: String?) {
        launch {
            if (task != null) {
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
                            oldListMutableList[index] = task
                        } else {
                            newList.addAll(oldList)
                        }
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
                    EventBus.getDefault().post(LocalEvents.RefreshTasksEvent())
                }
            }
        }
    }

    fun updateForwardTaskInLocal(task: CeibroTaskV2?, taskDao: TaskV2Dao, userId: String?) {
        launch {
            if (task != null) {
                val taskFromMe = task.creator.id == userId
                val taskToMe = !task.assignedToState.none { it.userId == userId }
                val myState = task.assignedToState.find { it.userId == userId }?.state

                if (taskFromMe) {
                    val taskFromMeLocalData = taskDao.getTasks(TaskRootStateTags.FromMe.tagValue)

                    if (taskFromMeLocalData != null) {
                        val unreadTaskIndex =
                            taskFromMeLocalData.allTasks.unread.indexOfFirst { it.id == task.id }
                        val ongoingTaskIndex =
                            taskFromMeLocalData.allTasks.ongoing.indexOfFirst { it.id == task.id }
                        val doneTaskIndex =
                            taskFromMeLocalData.allTasks.done.indexOfFirst { it.id == task.id }

                        if (unreadTaskIndex != -1) {
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
                        } else if (ongoingTaskIndex != -1) {
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
                        } else if (doneTaskIndex != -1) {
                            val doneList = taskFromMeLocalData.allTasks.done.toMutableList()
                            doneList[doneTaskIndex] = task
                            taskFromMeLocalData.allTasks.done = doneList.toList()
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

                        if (newTaskIndex != -1) {
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
                        } else if (ongoingTaskIndex != -1) {
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

                        } else if (doneTaskIndex != -1) {
                            val allTaskList = taskToMeLocalData.allTasks.done.toMutableList()
                            allTaskList[doneTaskIndex] = task
                            taskToMeLocalData.allTasks.done = allTaskList.toList()
                        } else {
                            val allTaskList = taskToMeLocalData.allTasks.new.toMutableList()
                            allTaskList.add(0, task)
                            taskToMeLocalData.allTasks.new = allTaskList.toList()
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
                        val newTask = taskToMeLocalData.allTasks.new.find { it.id == taskSeen.taskId }
                        val ongoingTask =
                            taskToMeLocalData.allTasks.ongoing.find { it.id == taskSeen.taskId }
                        val doneTask = taskToMeLocalData.allTasks.done.find { it.id == taskSeen.taskId }
                        if (newTask != null) {
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
                    }
                    EventBus.getDefault().post(LocalEvents.RefreshTasksEvent())
                }
            }

        }
    }

    fun updateTaskCommentInLocal(
        eventData: EventV2Response.Data?,
        taskDao: TaskV2Dao,
        userId: String?
    ): CeibroTaskV2? {
        var updatedTask: CeibroTaskV2? = null

        if (eventData != null) {
            val taskID = eventData.taskId

            val hiddenByCurrentUser = eventData.taskData.hiddenBy.find { it == userId }
            if (hiddenByCurrentUser != null) {
                //it means task must be searched from hidden rootState and child states[ongoing and done] and then move the task to another root state from hidden
            } else if (eventData.taskData.creatorState.equals(TaskStatus.CANCELED.name, true)) {
                // it means task must be in hidden rootState and child state will be canceled. search an update the task only
            } else {
                val taskEvent = Events(
                    id = eventData.id,
                    taskId = eventData.taskId,
                    eventType = eventData.eventType,
                    initiator = eventData.initiator,
                    eventData = eventData.eventData,
                    commentData = eventData.commentData,
                    createdAt = eventData.createdAt,
                    updatedAt = eventData.updatedAt,
                    v = null
                )
                val taskEventList: MutableList<Events> = mutableListOf()
                taskEventList.add(taskEvent)

                launch {
                    val taskToMeLocalData = taskDao.getTasks(TaskRootStateTags.ToMe.tagValue)
                    val taskFromMeLocalData = taskDao.getTasks(TaskRootStateTags.FromMe.tagValue)

                    if (taskToMeLocalData != null) {
                        val newTask = taskToMeLocalData.allTasks.new.find { it.id == taskID }
                        val unreadTask = taskToMeLocalData.allTasks.unread.find { it.id == taskID }
                        val ongoingTask =
                            taskToMeLocalData.allTasks.ongoing.find { it.id == taskID }
                        val doneTask = taskToMeLocalData.allTasks.done.find { it.id == taskID }

                        if (newTask != null) {
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
                            newTask.hiddenBy = eventData.taskData.hiddenBy
                            newTask.seenBy = eventData.taskData.seenBy

                            allTaskList[taskIndex] = newTask
                            taskToMeLocalData.allTasks.new = allTaskList.toList()
                            updatedTask = newTask
                        } else if (unreadTask != null) {
                            val allTaskList = taskToMeLocalData.allTasks.unread.toMutableList()
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
                            unreadTask.hiddenBy = eventData.taskData.hiddenBy
                            unreadTask.seenBy = eventData.taskData.seenBy

                            allTaskList[taskIndex] = unreadTask
                            taskToMeLocalData.allTasks.unread = allTaskList.toList()
                            updatedTask = unreadTask
                        } else if (ongoingTask != null) {
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
                            ongoingTask.hiddenBy = eventData.taskData.hiddenBy
                            ongoingTask.seenBy = eventData.taskData.seenBy

                            allTaskList[taskIndex] = ongoingTask
                            taskToMeLocalData.allTasks.ongoing = allTaskList.toList()
                            updatedTask = ongoingTask
                        } else if (doneTask != null) {
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
                            doneTask.hiddenBy = eventData.taskData.hiddenBy
                            doneTask.seenBy = eventData.taskData.seenBy

                            allTaskList[taskIndex] = doneTask
                            taskToMeLocalData.allTasks.done = allTaskList.toList()
                            updatedTask = doneTask
                        }

                        taskDao.insertTaskData(
                            taskToMeLocalData
                        )
                    }

                    if (taskFromMeLocalData != null) {
                        val newTask = taskFromMeLocalData.allTasks.new.find { it.id == taskID }
                        val unreadTask =
                            taskFromMeLocalData.allTasks.unread.find { it.id == taskID }
                        val ongoingTask =
                            taskFromMeLocalData.allTasks.ongoing.find { it.id == taskID }
                        val doneTask = taskFromMeLocalData.allTasks.done.find { it.id == taskID }

                        if (newTask != null) {
                            val allTaskList = taskFromMeLocalData.allTasks.new.toMutableList()
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
                            newTask.hiddenBy = eventData.taskData.hiddenBy
                            newTask.seenBy = eventData.taskData.seenBy

                            allTaskList[taskIndex] = newTask
                            taskFromMeLocalData.allTasks.new = allTaskList.toList()
                            updatedTask = newTask
                        } else if (unreadTask != null) {
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
                            unreadTask.hiddenBy = eventData.taskData.hiddenBy
                            unreadTask.seenBy = eventData.taskData.seenBy

                            allTaskList[taskIndex] = unreadTask
                            taskFromMeLocalData.allTasks.unread = allTaskList.toList()
                            updatedTask = unreadTask
                        } else if (ongoingTask != null) {
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
                            ongoingTask.hiddenBy = eventData.taskData.hiddenBy
                            ongoingTask.seenBy = eventData.taskData.seenBy

                            allTaskList[taskIndex] = ongoingTask
                            taskFromMeLocalData.allTasks.ongoing = allTaskList.toList()
                            updatedTask = ongoingTask
                        } else if (doneTask != null) {
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
                            doneTask.hiddenBy = eventData.taskData.hiddenBy
                            doneTask.seenBy = eventData.taskData.seenBy

                            allTaskList[taskIndex] = doneTask
                            taskFromMeLocalData.allTasks.done = allTaskList.toList()
                            updatedTask = doneTask
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

}


