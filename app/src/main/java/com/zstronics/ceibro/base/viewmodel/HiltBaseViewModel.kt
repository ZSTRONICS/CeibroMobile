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
import com.zstronics.ceibro.data.repos.dashboard.attachment.AttachmentUploadRequest
import com.zstronics.ceibro.data.repos.task.TaskRootStateTags
import com.zstronics.ceibro.data.repos.task.models.TaskV2Response
import com.zstronics.ceibro.data.repos.task.models.TasksV2DatabaseEntity
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
                        EventBus.getDefault().post(LocalEvents.TaskCreatedEvent())
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
                        EventBus.getDefault().post(LocalEvents.TaskCreatedEvent())
                        EventBus.getDefault().post(LocalEvents.TaskForwardEvent(task))
                    }
                }
            }
        }
    }
}


