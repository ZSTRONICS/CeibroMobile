package com.zstronics.ceibro.ui.tasks.v2.taskdetailv2.fragments.detailcomments

import android.os.Bundle
import com.zstronics.ceibro.CeibroApplication
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.database.dao.DownloadedDrawingV2Dao
import com.zstronics.ceibro.data.database.dao.DrawingPinsV2Dao
import com.zstronics.ceibro.data.database.dao.GroupsV2Dao
import com.zstronics.ceibro.data.database.dao.InboxV2Dao
import com.zstronics.ceibro.data.database.dao.TaskV2Dao
import com.zstronics.ceibro.data.database.models.tasks.CeibroTaskV2
import com.zstronics.ceibro.data.database.models.tasks.Events
import com.zstronics.ceibro.data.remote.TaskRemoteDataSource
import com.zstronics.ceibro.data.repos.NotificationTaskData
import com.zstronics.ceibro.data.repos.dashboard.IDashboardRepository
import com.zstronics.ceibro.data.repos.task.ITaskRepository
import com.zstronics.ceibro.data.repos.task.TaskRootStateTags
import com.zstronics.ceibro.data.sessions.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
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

/*
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

            val notificationData: NotificationTaskData? =
                bundle?.getParcelable("notificationTaskData")

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
//                        if (seenByMe == null) {
                        taskSeen(task1.id) { }
//                        }
                    } ?: run {
                        // run API call because task not found in DB
                        getTaskById(taskId) { isSuccess, task, events ->
                            if (isSuccess) {
                                isTaskBeingDone.postValue(false)
                                _taskDetail.postValue(task)
                                originalTask.postValue(task)
                                originalEvents.postValue(events.toMutableList())
                                _taskEvents.postValue(events.toMutableList())

                                val progres = progress.value?.plus(1);
                                progress.postValue(progres)

                                val seenByMe = task?.seenBy?.find { it1 -> it1 == user?.id }
//                                if (seenByMe == null) {
                                taskSeen(taskId) { }
//                                }
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
//                    if (seenByMe == null) {
                    taskSeen(task.id) { }
//                    } else {
//                        launch {
//                            val inboxTask = inboxV2Dao.getInboxTaskData(task.id)
//                            if (inboxTask != null && !inboxTask.isSeen) {
//                                inboxTask.isSeen = true
//                                inboxTask.unSeenNotifCount = 0
//                                inboxV2Dao.insertInboxItem(inboxTask)
//
//                                EventBus.getDefault()
//                                    .post(LocalEvents.UpdateInboxItemSeen(inboxTask))
//                            }
//                        }
//                    }
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
    }*/

}