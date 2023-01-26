package com.zstronics.ceibro.ui.dashboard

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.local.SubTaskLocalDataSource
import com.zstronics.ceibro.data.local.TaskLocalDataSource
import com.zstronics.ceibro.data.repos.chat.messages.SocketReceiveMessageResponse
import com.zstronics.ceibro.data.repos.chat.messages.socket.SocketEventTypeResponse
import com.zstronics.ceibro.data.repos.task.ITaskRepository
import com.zstronics.ceibro.data.repos.task.models.SocketSubTaskCreatedResponse
import com.zstronics.ceibro.data.repos.task.models.SocketTaskCreatedResponse
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.ui.socket.SocketHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DashboardVM @Inject constructor(
    override val viewState: DashboardState,
    val sessionManager: SessionManager,
    private val localTask: TaskLocalDataSource,
    private val localSubTask: SubTaskLocalDataSource
) : HiltBaseViewModel<IDashboard.State>(), IDashboard.ViewModel {
    init {
        sessionManager.setUser()
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
                        }
                        SocketHandler.TaskEvent.TASK_UPDATE_PRIVATE.name -> {
                            val taskUpdatedData = gson.fromJson<SocketTaskCreatedResponse>(
                                arguments,
                                object : TypeToken<SocketTaskCreatedResponse>() {}.type
                            )
                            localTask.updateTask(taskUpdatedData.data)
                        }
                        SocketHandler.TaskEvent.SUB_TASK_CREATED.name -> {
                            val subtask = gson.fromJson<SocketSubTaskCreatedResponse>(
                                arguments,
                                object : TypeToken<SocketSubTaskCreatedResponse>() {}.type
                            )
                            localSubTask.insertSubTask(subtask.data)
                        }

                        SocketHandler.TaskEvent.SUB_TASK_UPDATE_PRIVATE.name -> {
                            val subtask = gson.fromJson<SocketSubTaskCreatedResponse>(
                                arguments,
                                object : TypeToken<SocketSubTaskCreatedResponse>() {}.type
                            )
                            localSubTask.updateSubTask(subtask.data)
                        }

                        SocketHandler.TaskEvent.TASK_UPDATE_PUBLIC.name -> {
                            alert(socketData.eventType)
                        }
                        SocketHandler.TaskEvent.SUB_TASK_UPDATE_PUBLIC.name -> {
                            alert(socketData.eventType)
                        }
                    }
                }
            }
        }
    }
}