package com.zstronics.ceibro.ui.dashboard

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.KEY_USER
import com.zstronics.ceibro.base.viewmodel.Dispatcher
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.database.dao.ProjectsV2Dao
import com.zstronics.ceibro.data.database.dao.TaskV2Dao
import com.zstronics.ceibro.data.database.dao.TopicsV2Dao
import com.zstronics.ceibro.data.local.FileAttachmentsDataSource
import com.zstronics.ceibro.data.local.SubTaskLocalDataSource
import com.zstronics.ceibro.data.local.TaskLocalDataSource
import com.zstronics.ceibro.data.remote.TaskRemoteDataSource
import com.zstronics.ceibro.data.repos.auth.IAuthRepository
import com.zstronics.ceibro.data.repos.auth.login.UserUpdatedSocketResponse
import com.zstronics.ceibro.data.repos.chat.messages.socket.SocketEventTypeResponse
import com.zstronics.ceibro.data.repos.dashboard.IDashboardRepository
import com.zstronics.ceibro.data.repos.dashboard.attachment.AttachmentModules
import com.zstronics.ceibro.data.repos.projects.IProjectRepository
import com.zstronics.ceibro.data.repos.projects.documents.RefreshFolderSocketResponse
import com.zstronics.ceibro.data.repos.projects.documents.RefreshRootDocumentSocketResponse
import com.zstronics.ceibro.data.repos.projects.group.GroupCreatedSocketResponse
import com.zstronics.ceibro.data.repos.projects.group.GroupRefreshSocketResponse
import com.zstronics.ceibro.data.repos.projects.member.MemberAddedSocketResponse
import com.zstronics.ceibro.data.repos.projects.member.MemberRefreshSocketResponse
import com.zstronics.ceibro.data.repos.projects.member.MemberUpdatedSocketResponse
import com.zstronics.ceibro.data.repos.projects.projectsmain.ProjectCreatedSocketResponse
import com.zstronics.ceibro.data.repos.projects.projectsmain.ProjectsV2DatabaseEntity
import com.zstronics.ceibro.data.repos.projects.projectsmain.ProjectsWithMembersResponse
import com.zstronics.ceibro.data.repos.projects.role.RoleCreatedSocketResponse
import com.zstronics.ceibro.data.repos.projects.role.RoleRefreshSocketResponse
import com.zstronics.ceibro.data.repos.task.TaskRepository
import com.zstronics.ceibro.data.repos.task.models.*
import com.zstronics.ceibro.data.repos.task.models.v2.SocketTaskV2CreatedResponse
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
    val localSubTask: SubTaskLocalDataSource,
    private val taskRepository: TaskRepository,
    private val projectRepository: IProjectRepository,
    val dashboardRepository: IDashboardRepository,
    private val authRepository: IAuthRepository,
    val fileAttachmentsDataSource: FileAttachmentsDataSource,
    private val remoteTask: TaskRemoteDataSource,
    private val taskDao: TaskV2Dao,
    private val topicsV2Dao: TopicsV2Dao,
    private val projectDao: ProjectsV2Dao,
) : HiltBaseViewModel<IDashboard.State>(), IDashboard.ViewModel {

    init {
        sessionManager.setUser()
//        sessionManager.setProject()
//        loadProjectsWithMembers()
//        getOverallConnectionCount()
//        launch {
//            repository.syncTasksAndSubTasks()
//        }
        val user = sessionManager.sharedPreferenceManager.getCompleteUserObj(KEY_USER)
        EventBus.getDefault().register(this)
        loadAppData()
    }

    init {
    }

    override fun handleSocketEvents() {

        SocketHandler.getSocket()?.on(SocketHandler.CEIBRO_LIVE_EVENT_BY_SERVER) { args ->
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
                            val taskCreatedData = gson.fromJson<SocketTaskV2CreatedResponse>(
                                arguments,
                                object : TypeToken<SocketTaskV2CreatedResponse>() {}.type
                            )
//                            taskCreatedData.data?.let { localTask.insertTask(it) }

                            var notificationTitle = ""
                            notificationTitle =
                                if (taskCreatedData.data?.topic?.topic.isNullOrEmpty()) {
                                    ""
                                } else {
                                    taskCreatedData.data?.topic?.topic.toString()
                                }

                            EventBus.getDefault().post(
                                LocalEvents.CreateSimpleNotification(
                                    moduleId = taskCreatedData.data?.id ?: "",
                                    moduleName = socketData.module,
                                    notificationTitle =
                                    if (notificationTitle.isNotEmpty()) {
                                        "New task created as \"$notificationTitle\""
                                    } else {
                                        "New task created"
                                    },
                                    isOngoing = false,
                                    indeterminate = false,
                                    notificationIcon = R.drawable.app_logo
                                )
                            )
                            EventBus.getDefault().post(LocalEvents.TaskCreatedEvent())

//                            println("TASK_CREATED $arguments")
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
                } else if (socketData.module == "SubTaskComments") {
                    if (socketData.eventType == SocketHandler.TaskEvent.SUBTASK_NEW_COMMENT.name) {
                        val newComment =
                            gson.fromJson<CommentsFilesUploadedSocketEventResponse>(
                                arguments,
                                object :
                                    TypeToken<CommentsFilesUploadedSocketEventResponse>() {}.type
                            ).data
                        localSubTask.addFilesUnderComment(
                            newComment.subTaskId,
                            newComment,
                            newComment.id
                        )
                        EventBus.getDefault()
                            .post(LocalEvents.NewSubTaskComment(newComment, newComment.id))

                        EventBus.getDefault().post(
                            LocalEvents.CreateNotification(
                                moduleName = socketData.module,
                                moduleId = newComment.id,
                                notificationTitle = "New Comment received",
                                isOngoing = false,
                                indeterminate = false,
                                notificationIcon = R.drawable.icon_chat
                            )
                        )
                    }
                } else if (socketData.module == "project") {
                    when (socketData.eventType) {
                        SocketHandler.ProjectEvent.PROJECT_CREATED.name -> {
                            val newProject =
                                gson.fromJson<ProjectCreatedSocketResponse>(
                                    arguments,
                                    object : TypeToken<ProjectCreatedSocketResponse>() {}.type
                                ).data

                            EventBus.getDefault().post(LocalEvents.ProjectCreatedEvent(newProject))

                            EventBus.getDefault().post(
                                LocalEvents.CreateNotification(
                                    moduleName = socketData.module,
                                    moduleId = newProject.id,
                                    notificationTitle = "New Project Created as ${newProject.title}",
                                    isOngoing = false,
                                    indeterminate = false,
                                    notificationIcon = R.drawable.app_logo
                                )
                            )
                        }
                        SocketHandler.ProjectEvent.PROJECT_UPDATED.name -> {
                            val updatedProject =
                                gson.fromJson<ProjectCreatedSocketResponse>(
                                    arguments,
                                    object : TypeToken<ProjectCreatedSocketResponse>() {}.type
                                ).data

                            EventBus.getDefault()
                                .post(LocalEvents.ProjectCreatedEvent(updatedProject))

                            EventBus.getDefault().post(
                                LocalEvents.CreateNotification(
                                    moduleName = socketData.module,
                                    moduleId = updatedProject.id,
                                    notificationTitle = "${updatedProject.title} Updated",
                                    isOngoing = false,
                                    indeterminate = false,
                                    notificationIcon = R.drawable.app_logo
                                )
                            )
                        }
                        SocketHandler.ProjectEvent.REFRESH_PROJECTS.name -> {

                            EventBus.getDefault().post(LocalEvents.ProjectRefreshEvent())

                        }

                        SocketHandler.ProjectEvent.ROLE_CREATED.name -> {
                            val newRole =
                                gson.fromJson<RoleCreatedSocketResponse>(
                                    arguments,
                                    object : TypeToken<RoleCreatedSocketResponse>() {}.type
                                ).data

                            EventBus.getDefault().post(LocalEvents.RoleCreatedEvent(newRole))
                        }
                        SocketHandler.ProjectEvent.ROLE_UPDATED.name -> {
                            try {
                                val updatedRole =
                                    gson.fromJson<RoleCreatedSocketResponse>(
                                        arguments,
                                        object : TypeToken<RoleCreatedSocketResponse>() {}.type
                                    ).data

                                EventBus.getDefault()
                                    .post(LocalEvents.RoleCreatedEvent(updatedRole))
                            } catch (e: Exception) {
                                print("Some data error")
                            }
                        }
                        SocketHandler.ProjectEvent.REFRESH_ROLES.name -> {
                            val refreshRole =
                                gson.fromJson<RoleRefreshSocketResponse>(
                                    arguments,
                                    object : TypeToken<RoleRefreshSocketResponse>() {}.type
                                ).data

                            EventBus.getDefault()
                                .post(LocalEvents.RoleRefreshEvent(refreshRole.projectId))
                        }

                        SocketHandler.ProjectEvent.PROJECT_GROUP_CREATED.name -> {
                            val newGroup =
                                gson.fromJson<GroupCreatedSocketResponse>(
                                    arguments,
                                    object : TypeToken<GroupCreatedSocketResponse>() {}.type
                                ).data

                            EventBus.getDefault().post(LocalEvents.GroupCreatedEvent(newGroup))
                        }
                        SocketHandler.ProjectEvent.PROJECT_GROUP_UPDATED.name -> {
                            try {
                                val updatedGroup =
                                    gson.fromJson<GroupCreatedSocketResponse>(
                                        arguments,
                                        object : TypeToken<GroupCreatedSocketResponse>() {}.type
                                    ).data

                                EventBus.getDefault()
                                    .post(LocalEvents.GroupCreatedEvent(updatedGroup))
                            } catch (e: Exception) {
                                print("Some data error")
                            }
                        }
                        SocketHandler.ProjectEvent.REFRESH_PROJECT_GROUP.name -> {
                            val refreshGroup =
                                gson.fromJson<GroupRefreshSocketResponse>(
                                    arguments,
                                    object : TypeToken<GroupRefreshSocketResponse>() {}.type
                                ).data

                            EventBus.getDefault()
                                .post(LocalEvents.GroupRefreshEvent(refreshGroup.projectId))
                        }

                        SocketHandler.ProjectEvent.PROJECT_MEMBERS_ADDED.name -> {
                            val newMember =
                                gson.fromJson<MemberAddedSocketResponse>(
                                    arguments,
                                    object : TypeToken<MemberAddedSocketResponse>() {}.type
                                ).data

                            EventBus.getDefault()
                                .post(LocalEvents.ProjectMemberAddedEvent(newMember))
                        }
                        SocketHandler.ProjectEvent.PROJECT_MEMBERS_UPDATED.name -> {
                            val updatedMember =
                                gson.fromJson<MemberUpdatedSocketResponse>(
                                    arguments,
                                    object : TypeToken<MemberUpdatedSocketResponse>() {}.type
                                ).data

                            EventBus.getDefault()
                                .post(LocalEvents.ProjectMemberUpdatedEvent(updatedMember))
                        }
                        SocketHandler.ProjectEvent.REFRESH_PROJECT_MEMBERS.name -> {
                            val refreshMember =
                                gson.fromJson<MemberRefreshSocketResponse>(
                                    arguments,
                                    object : TypeToken<MemberRefreshSocketResponse>() {}.type
                                ).data

                            EventBus.getDefault()
                                .post(LocalEvents.ProjectMemberRefreshEvent(refreshMember.projectId))
                        }
                        SocketHandler.ProjectEvent.REFRESH_ROOT_DOCUMENTS.name -> {
                            val refreshRootDoc =
                                gson.fromJson<RefreshRootDocumentSocketResponse>(
                                    arguments,
                                    object : TypeToken<RefreshRootDocumentSocketResponse>() {}.type
                                ).data

                            EventBus.getDefault()
                                .post(LocalEvents.RefreshRootDocumentEvent(refreshRootDoc.projectId))
                        }
                        SocketHandler.ProjectEvent.REFRESH_FOLDER.name -> {
                            val refreshFolder =
                                gson.fromJson<RefreshFolderSocketResponse>(
                                    arguments,
                                    object : TypeToken<RefreshFolderSocketResponse>() {}.type
                                ).data

                            EventBus.getDefault().post(
                                LocalEvents.RefreshFolderEvent(
                                    refreshFolder.projectId,
                                    refreshFolder.folderId
                                )
                            )
                        }
                    }
                } else if (socketData.module == "user") {
                    when (socketData.eventType) {
                        SocketHandler.UserEvent.USER_INFO_UPDATED.name -> {
                            val updatedUser =
                                gson.fromJson<UserUpdatedSocketResponse>(
                                    arguments,
                                    object : TypeToken<UserUpdatedSocketResponse>() {}.type
                                ).data

                            sessionManager.updateUser(updatedUser)
                            EventBus.getDefault().post(LocalEvents.UserDataUpdated())
                        }
                        SocketHandler.UserEvent.REFRESH_ALL_USERS.name -> {
//                            getProfile()
                        }
                        SocketHandler.UserEvent.REFRESH_CONNECTIONS.name -> {
                            getOverallConnectionCount()
                            EventBus.getDefault().post(LocalEvents.ConnectionRefreshEvent())
                        }
                        SocketHandler.UserEvent.REFRESH_INVITATIONS.name -> {
                            EventBus.getDefault().post(LocalEvents.InvitationRefreshEvent())
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUploadFilesToV2Server(uploadFilesToServer: LocalEvents.UploadFilesToV2Server) {
        launch(Dispatcher.Main) {

            val filesCount = uploadFilesToServer.request.files?.size ?: 1

            var notificationTitle =
                if (filesCount > 1) "$filesCount files are uploading" else "$filesCount file is uploading"
            createNotification(
                LocalEvents.CreateNotification(
                    moduleName = AttachmentModules.Task.name,
                    moduleId = uploadFilesToServer.request.moduleId,
                    notificationTitle = notificationTitle,
                    isOngoing = true,
                    indeterminate = true,
                    notificationIcon = R.drawable.icon_upload
                )
            )

            when (val response = dashboardRepository.uploadFiles(uploadFilesToServer.request)) {
                is ApiResponse.Success -> {
                    notificationTitle =
                        if (filesCount > 1) "$filesCount files has been uploaded" else "$filesCount file has been uploaded"

                    createNotification(
                        LocalEvents.CreateNotification(
                            moduleName = uploadFilesToServer.request.moduleName,
                            moduleId = uploadFilesToServer.request.moduleId,
                            notificationTitle = notificationTitle,
                            isOngoing = false,
                            indeterminate = false,
                            notificationIcon = R.drawable.icon_upload
                        )
                    )

                }
                is ApiResponse.Error -> {
                    alert(response.error.message)

                    createNotification(
                        LocalEvents.CreateNotification(
                            moduleName = uploadFilesToServer.request.moduleName,
                            moduleId = uploadFilesToServer.request.moduleId,
                            notificationTitle = response.error.message,
                            isOngoing = false,
                            indeterminate = false,
                            notificationIcon = R.drawable.icon_upload
                        )
                    )
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

    private fun getOverallConnectionCount() {
        launch {
            when (val response = dashboardRepository.getConnectionCount()) {
                is ApiResponse.Success -> {
                    viewState.connectionCount.value = response.data.count
                }
                is ApiResponse.Error -> {

                }
            }
        }
    }

    private fun getProfile() {
        launch {
            when (val response = authRepository.getUserProfile()) {

                is ApiResponse.Success -> {
                    sessionManager.updateUser(response.data.user)
                }

                is ApiResponse.Error -> {
                }
            }
        }
    }

    fun endUserSession() {
        launch {
            taskRepository.eraseTaskTable()
            taskRepository.eraseSubTaskTable()
        }
        sessionManager.endUserSession()
    }

    private fun loadAppData() {
        launch {
            when (val response = remoteTask.getAllTasks("to-me")) {
                is ApiResponse.Success -> {
                    taskDao.insertTaskData(
                        TasksV2DatabaseEntity(
                            rootState = "to-me",
                            allTasks = response.data.allTasks
                        )
                    )
                }
                is ApiResponse.Error -> {
                }
            }

            when (val response = remoteTask.getAllTasks("from-me")) {
                is ApiResponse.Success -> {
                    taskDao.insertTaskData(
                        TasksV2DatabaseEntity(
                            rootState = "from-me",
                            allTasks = response.data.allTasks
                        )
                    )
                }
                is ApiResponse.Error -> {
                }
            }

            when (val response = remoteTask.getAllTopics()) {
                is ApiResponse.Success -> {
                    topicsV2Dao.insertTopicData(
                        TopicsV2DatabaseEntity(
                            0,
                            topicsData = response.data
                        )
                    )
                }
                is ApiResponse.Error -> {
                }
            }

            when (val response = projectRepository.getProjectsV2()) {
                is ApiResponse.Success -> {
                    val data = response.data.projects
                    if (data != null) {
                        projectDao.insert(ProjectsV2DatabaseEntity(1, projects = data))
                    }
                }

                is ApiResponse.Error -> {
                }
            }
        }
    }
}