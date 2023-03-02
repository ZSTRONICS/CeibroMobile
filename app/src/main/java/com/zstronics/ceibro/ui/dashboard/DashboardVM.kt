package com.zstronics.ceibro.ui.dashboard

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zstronics.ceibro.base.viewmodel.Dispatcher
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.local.FileAttachmentsDataSource
import com.zstronics.ceibro.data.local.SubTaskLocalDataSource
import com.zstronics.ceibro.data.local.TaskLocalDataSource
import com.zstronics.ceibro.data.repos.chat.messages.socket.SocketEventTypeResponse
import com.zstronics.ceibro.data.repos.dashboard.IDashboardRepository
import com.zstronics.ceibro.data.repos.projects.IProjectRepository
import com.zstronics.ceibro.data.repos.projects.projectsmain.AllProjectsResponse
import com.zstronics.ceibro.data.repos.projects.projectsmain.ProjectsWithMembersResponse
import com.zstronics.ceibro.data.repos.task.ITaskRepository
import com.zstronics.ceibro.data.repos.task.models.*
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.ui.socket.LocalEvents
import com.zstronics.ceibro.ui.socket.SocketHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject

@HiltViewModel
class DashboardVM @Inject constructor(
    override val viewState: DashboardState,
    val sessionManager: SessionManager,
    private val localTask: TaskLocalDataSource,
    private val localSubTask: SubTaskLocalDataSource,
    private val repository: ITaskRepository,
    private val projectRepository: IProjectRepository,
    val dashboardRepository: IDashboardRepository,
    val fileAttachmentsDataSource: FileAttachmentsDataSource
) : HiltBaseViewModel<IDashboard.State>(), IDashboard.ViewModel {
    init {
        sessionManager.setUser()
        sessionManager.setProject()
        loadProjectsWithMembers()
        launch {
            repository.syncTasksAndSubTasks()
        }
    }

    init {
        EventBus.getDefault().register(this)
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
                            taskCreatedData.data?.let { localTask.insertTask(it) }
                            EventBus.getDefault().post(LocalEvents.TaskCreatedEvent())
                        }
                        SocketHandler.TaskEvent.TASK_UPDATE_PRIVATE.name -> {
                            val taskUpdatedData = gson.fromJson<SocketTaskCreatedResponse>(
                                arguments,
                                object : TypeToken<SocketTaskCreatedResponse>() {}.type
                            )
                            // Need to check if task data object is null then don't do anything
                            taskUpdatedData.data?._id?.let {
                                //Following Code will run if the data object would not be null
                                val taskCount =
                                    localTask.getSingleTaskCount(taskUpdatedData.data._id)
                                if (taskCount < 1) {
                                    localTask.insertTask(taskUpdatedData.data)
                                } else {
                                    localTask.updateTask(taskUpdatedData.data)
                                }
                            }
                            EventBus.getDefault().post(LocalEvents.TaskCreatedEvent())
                        }
                        SocketHandler.TaskEvent.SUB_TASK_CREATED.name -> {
                            val subtask = gson.fromJson<SocketSubTaskCreatedResponse>(
                                arguments,
                                object : TypeToken<SocketSubTaskCreatedResponse>() {}.type
                            )
                            subtask.data?.let { localSubTask.insertSubTask(it) }
                            EventBus.getDefault()
                                .post(subtask.data?.let { LocalEvents.SubTaskCreatedEvent(it.taskId) })
                        }

                        SocketHandler.TaskEvent.SUB_TASK_UPDATE_PRIVATE.name -> {
                            val subtask = gson.fromJson<SocketSubTaskCreatedResponse>(
                                arguments,
                                object : TypeToken<SocketSubTaskCreatedResponse>() {}.type
                            )
                            // Need to check if subtask data object is null then don't do anything
                            subtask.data?.id?.let {
                                val subtaskCount =
                                    localSubTask.getSingleSubTaskCount(subtask.data.id)
                                if (subtaskCount < 1) {
                                    localSubTask.insertSubTask(subtask.data)
                                } else {
                                    localSubTask.updateSubTask(subtask.data)
                                }
                            }
                            EventBus.getDefault()
                                .post(subtask.data?.let { LocalEvents.SubTaskCreatedEvent(it.taskId) })
                        }

                        SocketHandler.TaskEvent.TASK_UPDATE_PUBLIC.name -> {
                            val taskUpdatedData = gson.fromJson<SocketTaskCreatedResponse>(
                                arguments,
                                object : TypeToken<SocketTaskCreatedResponse>() {}.type
                            )
                            // Need to check if task data object is null then don't do anything
                            taskUpdatedData.data?._id?.let {
                                //Following Code will run if the data object would not be null
                                val taskCount =
                                    localTask.getSingleTaskCount(taskUpdatedData.data._id)
                                if (taskCount < 1) {
                                    localTask.insertTask(taskUpdatedData.data)
                                } else {
                                    localTask.updateTask(taskUpdatedData.data)
                                }
                            }
                            EventBus.getDefault().post(LocalEvents.TaskCreatedEvent())
                        }
                        SocketHandler.TaskEvent.SUB_TASK_UPDATE_PUBLIC.name -> {
                            val subtask = gson.fromJson<SocketSubTaskCreatedResponse>(
                                arguments,
                                object : TypeToken<SocketSubTaskCreatedResponse>() {}.type
                            )
                            // Need to check if subtask data object is null then don't do anything
                            subtask.data?.id?.let {
                                val subtaskCount =
                                    localSubTask.getSingleSubTaskCount(subtask.data.id)
                                if (subtaskCount < 1) {
                                    localSubTask.insertSubTask(subtask.data)
                                } else {
                                    localSubTask.updateSubTask(subtask.data)
                                }
                            }
                            EventBus.getDefault()
                                .post(subtask.data?.let { LocalEvents.SubTaskCreatedEvent(it.taskId) })
                        }

                        SocketHandler.TaskEvent.TASK_SUBTASK_UPDATED.name -> {
                            val taskSubtaskUpdateResponse =
                                gson.fromJson<SocketTaskSubtaskUpdateResponse>(
                                    arguments,
                                    object : TypeToken<SocketTaskSubtaskUpdateResponse>() {}.type
                                )
                            val taskSubtaskUpdatedData = taskSubtaskUpdateResponse.data.results

                            val subTasks = taskSubtaskUpdatedData.subtasks
                            val task = taskSubtaskUpdatedData.task

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

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUploadFilesToServer(uploadFilesToServer: LocalEvents.UploadFilesToServer) {
        launch(Dispatcher.Main) {
            when (val response = dashboardRepository.uploadFiles(uploadFilesToServer.request)) {
                is ApiResponse.Success -> {
                    val allFiles = response.data.results.files
                    val updatedFiles = allFiles.mapIndexed { index, file ->
                        if (uploadFilesToServer.fileUriList.size > index) {
                            file.copy(fileUrl = uploadFilesToServer.fileUriList[index]?.attachmentUri.toString())
                        } else {
                            file // return the original file if no URI is available at the corresponding index
                        }
                    }
                    fileAttachmentsDataSource.insertAll(updatedFiles)
                }
                is ApiResponse.Error -> {
                    alert(response.error.message)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        EventBus.getDefault().unregister(this)
    }


    private fun loadProjectsWithMembers() {
        launch {
            when (val response = projectRepository.getProjectsWithMembers(true)) {

                is ApiResponse.Success -> {
                    response.data.projectDetails.let { projects ->
                        if (projects.isNotEmpty()) {
                            sessionManager.setNewProjectList(projects as MutableList<ProjectsWithMembersResponse.ProjectDetail>?)
                        }
                    }
                }
                is ApiResponse.Error -> {

                }
            }
        }
    }
}