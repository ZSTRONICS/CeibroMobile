package com.zstronics.ceibro.ui.dashboard

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.local.SubTaskLocalDataSource
import com.zstronics.ceibro.data.local.TaskLocalDataSource
import com.zstronics.ceibro.data.repos.chat.messages.socket.SocketEventTypeResponse
import com.zstronics.ceibro.data.repos.task.ITaskRepository
import com.zstronics.ceibro.data.repos.task.models.SocketSubTaskCreatedResponse
import com.zstronics.ceibro.data.repos.task.models.SocketTaskCreatedResponse
import com.zstronics.ceibro.data.repos.task.models.SubTaskByTaskResponse
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.ui.socket.LocalEvents
import com.zstronics.ceibro.ui.socket.SocketHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

@HiltViewModel
class DashboardVM @Inject constructor(
    override val viewState: DashboardState,
    val sessionManager: SessionManager,
    private val localTask: TaskLocalDataSource,
    private val localSubTask: SubTaskLocalDataSource,
    private val repository: ITaskRepository
) : HiltBaseViewModel<IDashboard.State>(), IDashboard.ViewModel {
    init {
        sessionManager.setUser()
        launch {
            repository.syncTasksAndSubTasks()
        }
    }

    override fun handleSocketEvents() {

        SocketHandler.getSocket().on(SocketHandler.CEIBRO_LIVE_EVENT_BY_SERVER) { args ->
            val gson = Gson()
            val arguments = args[0].toString()
            val socketData: SocketEventTypeResponse = gson.fromJson(
                arguments,
                object : TypeToken<SocketEventTypeResponse>() {}.type
            )
            launch {
                if (socketData.module == "task") {
                    when (socketData.eventType) {
                        SocketHandler.TaskEvent.TASK_CREATED.name -> {
                            val taskCreatedData = gson.fromJson<SocketTaskCreatedResponse>(
                                arguments,
                                object : TypeToken<SocketTaskCreatedResponse>() {}.type
                            )
                            localTask.insertTask(taskCreatedData.data)
                            EventBus.getDefault().post(LocalEvents.TaskCreatedEvent())
                        }
                        SocketHandler.TaskEvent.TASK_UPDATE_PRIVATE.name -> {
                            val taskUpdatedData = gson.fromJson<SocketTaskCreatedResponse>(
                                arguments,
                                object : TypeToken<SocketTaskCreatedResponse>() {}.type
                            )
                            // Need to check if task data object is null then don't do anything
                            val taskCount = localTask.getSingleTaskCount(taskUpdatedData.data._id)
                            if (taskCount < 1) {
                                localTask.insertTask(taskUpdatedData.data)
                            }
                            else {
                                localTask.updateTask(taskUpdatedData.data)
                            }
                            EventBus.getDefault().post(LocalEvents.TaskCreatedEvent())
                        }
                        SocketHandler.TaskEvent.SUB_TASK_CREATED.name -> {
                            val subtask = gson.fromJson<SocketSubTaskCreatedResponse>(
                                arguments,
                                object : TypeToken<SocketSubTaskCreatedResponse>() {}.type
                            )
                            localSubTask.insertSubTask(subtask.data)
                            EventBus.getDefault()
                                .post(LocalEvents.SubTaskCreatedEvent(subtask.data.taskId))
                        }

                        SocketHandler.TaskEvent.SUB_TASK_UPDATE_PRIVATE.name -> {
                            val subtask = gson.fromJson<SocketSubTaskCreatedResponse>(
                                arguments,
                                object : TypeToken<SocketSubTaskCreatedResponse>() {}.type
                            )
                            // Need to check if subtask data object is null then don't do anything
                            val subtaskCount = localSubTask.getSingleSubTaskCount(subtask.data.id)
                            if (subtaskCount < 1) {
                                localSubTask.insertSubTask(subtask.data)
                            }
                            else {
                                localSubTask.updateSubTask(subtask.data)
                            }
                            EventBus.getDefault()
                                .post(LocalEvents.SubTaskCreatedEvent(subtask.data.taskId))
                        }

                        SocketHandler.TaskEvent.TASK_UPDATE_PUBLIC.name -> {
                            alert(socketData.eventType)
                        }
                        SocketHandler.TaskEvent.SUB_TASK_UPDATE_PUBLIC.name -> {
                            alert(socketData.eventType)
                        }

                        SocketHandler.TaskEvent.TASK_SUBTASK_UPDATED.name -> {
                            val subTaskByTaskResponse = gson.fromJson<SubTaskByTaskResponse>(
                                arguments,
                                object : TypeToken<SubTaskByTaskResponse>() {}.type
                            )
                            val subtaskUpdatedData = subTaskByTaskResponse.results

                            val subTasks = subtaskUpdatedData.subtasks
                            val task = subtaskUpdatedData.task

                            if (subTasks.isNotEmpty()) {
                                val subTask = subTasks[0]
                                localSubTask.updateSubTask(subTask)
                                EventBus.getDefault()
                                    .post(LocalEvents.SubTaskCreatedEvent(subTask.taskId))
                            }

                            if (task != null) {
                                localTask.updateTask(task)
                                EventBus.getDefault().post(LocalEvents.TaskCreatedEvent())
                            }
                        }
                    }
                }
            }
        }
    }
}