package com.zstronics.ceibro.ui.dashboard

import android.content.Context
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.work.WorkManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zstronics.ceibro.BuildConfig
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.viewmodel.Dispatcher
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.database.dao.ConnectionsV2Dao
import com.zstronics.ceibro.data.database.dao.ProjectsV2Dao
import com.zstronics.ceibro.data.database.dao.TaskV2Dao
import com.zstronics.ceibro.data.database.dao.TopicsV2Dao
import com.zstronics.ceibro.data.local.FileAttachmentsDataSource
import com.zstronics.ceibro.data.local.SubTaskLocalDataSource
import com.zstronics.ceibro.data.repos.auth.IAuthRepository
import com.zstronics.ceibro.data.repos.auth.login.UserUpdatedSocketResponse
import com.zstronics.ceibro.data.repos.chat.messages.socket.SocketEventTypeResponse
import com.zstronics.ceibro.data.repos.dashboard.IDashboardRepository
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.AllCeibroConnections
import com.zstronics.ceibro.data.repos.projects.IProjectRepository
import com.zstronics.ceibro.data.repos.projects.documents.RefreshFolderSocketResponse
import com.zstronics.ceibro.data.repos.projects.documents.RefreshRootDocumentSocketResponse
import com.zstronics.ceibro.data.repos.projects.group.GroupCreatedSocketResponse
import com.zstronics.ceibro.data.repos.projects.group.GroupRefreshSocketResponse
import com.zstronics.ceibro.data.repos.projects.member.MemberAddedSocketResponse
import com.zstronics.ceibro.data.repos.projects.member.MemberRefreshSocketResponse
import com.zstronics.ceibro.data.repos.projects.member.MemberUpdatedSocketResponse
import com.zstronics.ceibro.data.repos.projects.projectsmain.ProjectCreatedSocketResponse
import com.zstronics.ceibro.data.repos.projects.projectsmain.ProjectsWithMembersResponse
import com.zstronics.ceibro.data.repos.projects.role.RoleCreatedSocketResponse
import com.zstronics.ceibro.data.repos.projects.role.RoleRefreshSocketResponse
import com.zstronics.ceibro.data.repos.task.TaskRepository
import com.zstronics.ceibro.data.repos.task.models.CommentsFilesUploadedSocketEventResponse
import com.zstronics.ceibro.data.repos.task.models.TopicsV2DatabaseEntity
import com.zstronics.ceibro.data.repos.task.models.v2.NewTaskV2Entity
import com.zstronics.ceibro.data.repos.task.models.v2.SocketHideUnHideTaskResponse
import com.zstronics.ceibro.data.repos.task.models.v2.SocketNewTaskEventV2Response
import com.zstronics.ceibro.data.repos.task.models.v2.SocketTaskSeenV2Response
import com.zstronics.ceibro.data.repos.task.models.v2.SocketTaskV2CreatedResponse
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.ui.contacts.ContactSyncWorker
import com.zstronics.ceibro.ui.contacts.toLightContacts
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
    val localSubTask: SubTaskLocalDataSource,
    private val taskRepository: TaskRepository,
    private val projectRepository: IProjectRepository,
    val dashboardRepository: IDashboardRepository,
    private val authRepository: IAuthRepository,
    val fileAttachmentsDataSource: FileAttachmentsDataSource,
    private val taskDao: TaskV2Dao,
    private val topicsV2Dao: TopicsV2Dao,
    private val projectsV2Dao: ProjectsV2Dao,
    private val connectionsV2Dao: ConnectionsV2Dao,
) : HiltBaseViewModel<IDashboard.State>(), IDashboard.ViewModel {
    var user = sessionManager.getUser().value
    var userId: String? = ""

    init {
        userId = sessionManager.getUserId()
        user = sessionManager.getUser().value
        EventBus.getDefault().register(this)
    }


    suspend fun getDraftTasks(): List<NewTaskV2Entity> {
        return draftNewTaskV2Internal.getUnSyncedRecords() ?: emptyList()
    }

    suspend fun getTopicList(): TopicsV2DatabaseEntity? {

        val topic: TopicsV2DatabaseEntity? = topicsV2Dao.getTopicsData()

      return topic

    }


    suspend fun getContactsList(): List<AllCeibroConnections.CeibroConnection> {
        val contactsFromDatabase: List<AllCeibroConnections.CeibroConnection> = connectionsV2Dao.getAll()

        return contactsFromDatabase

    }


    override fun handleSocketEvents() {

        SocketHandler.getSocket()?.on(SocketHandler.CEIBRO_LIVE_EVENT_BY_SERVER) { args ->
            val gson = Gson()
            val arguments = args[0].toString()
            val socketData: SocketEventTypeResponse = gson.fromJson(
                arguments,
                object : TypeToken<SocketEventTypeResponse>() {}.type
            )
            socketData.uuid?.let { SocketHandler.sendEventAck(it) }
            if (BuildConfig.DEBUG) {
                alert("Socket: ${socketData.eventType}")
                println("Socket: ${socketData.eventType}")
            }
            if (socketData.module == "task") {
                when (socketData.eventType) {


                    SocketHandler.TaskEvent.TASK_CREATED.name -> {

                        val taskCreatedData = gson.fromJson<SocketTaskV2CreatedResponse>(
                            arguments,
                            object : TypeToken<SocketTaskV2CreatedResponse>() {}.type
                        )
                        Log.d("TASK_CREATED", taskCreatedData.data?.taskUID.toString())
                        updateCreatedTaskInLocal(
                            taskCreatedData.data,
                            taskDao,
                            userId,
                            sessionManager
                        )

                        val notificationTitle: String =
                            if (taskCreatedData.data?.topic?.topic.isNullOrEmpty())
                                "" else taskCreatedData.data?.topic?.topic.toString()


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

                    }

                    SocketHandler.TaskEvent.TASK_FORWARDED.name -> {
                        val task = gson.fromJson<SocketTaskV2CreatedResponse>(
                            arguments,
                            object : TypeToken<SocketTaskV2CreatedResponse>() {}.type
                        ).data
                        launch {
                            updateForwardTaskInLocal(task, taskDao, userId, sessionManager)
                        }
                    }

                    SocketHandler.TaskEvent.TASK_SEEN.name -> {
                        val taskSeen = gson.fromJson<SocketTaskSeenV2Response>(
                            arguments,
                            object : TypeToken<SocketTaskSeenV2Response>() {}.type
                        ).data
                        updateGenericTaskSeenInLocal(taskSeen, taskDao, userId, sessionManager)
                    }

                    SocketHandler.TaskEvent.NEW_TASK_COMMENT.name, SocketHandler.TaskEvent.TASK_DONE.name, SocketHandler.TaskEvent.CANCELED_TASK.name,
                    SocketHandler.TaskEvent.UN_CANCEL_TASK.name, SocketHandler.TaskEvent.JOINED_TASK.name -> {
                        val commentData = gson.fromJson<SocketNewTaskEventV2Response>(
                            arguments,
                            object : TypeToken<SocketNewTaskEventV2Response>() {}.type
                        ).data
                        Log.d("TASK_EVENT", commentData?.taskId.toString())

                        if (socketData.eventType == SocketHandler.TaskEvent.NEW_TASK_COMMENT.name) {
                            updateTaskCommentInLocal(
                                commentData,
                                taskDao,
                                userId,
                                sessionManager
                            )
                        }
                        if (socketData.eventType == SocketHandler.TaskEvent.CANCELED_TASK.name) {
                            if (commentData != null) {
                                updateTaskCanceledInLocal(
                                    commentData,
                                    taskDao,
                                    userId,
                                    sessionManager
                                )
                            }
                        }
                        if (socketData.eventType == SocketHandler.TaskEvent.UN_CANCEL_TASK.name) {
                            if (commentData != null) {
                                updateTaskUnCanceledInLocal(
                                    commentData,
                                    taskDao,
                                    sessionManager
                                )
                            }
                        }
                        if (socketData.eventType == SocketHandler.TaskEvent.TASK_DONE.name) {
                            if (commentData != null) {
                                updateTaskDoneInLocal(commentData, taskDao, sessionManager)
                            }
                        }
                        if (socketData.eventType == SocketHandler.TaskEvent.JOINED_TASK.name) {
                            println("Socket: JOINED_TASK triggered ")
                            println("Socket: JOINED_TASK:  $commentData")
                            updateTaskJoinedInLocal(commentData, taskDao, sessionManager)
                            updateContactsInDB()
                        }
                    }

                    SocketHandler.TaskEvent.TASK_HIDDEN.name, SocketHandler.TaskEvent.TASK_SHOWN.name -> {
                        val hideData = gson.fromJson<SocketHideUnHideTaskResponse>(
                            arguments,
                            object : TypeToken<SocketHideUnHideTaskResponse>() {}.type
                        ).data
                        if (socketData.eventType == SocketHandler.TaskEvent.TASK_HIDDEN.name) {
                            if (hideData != null) {
                                updateTaskHideInLocal(hideData, taskDao, sessionManager)
                            }
                        }
                        if (socketData.eventType == SocketHandler.TaskEvent.TASK_SHOWN.name) {
                            if (hideData != null) {
                                updateTaskUnHideInLocal(hideData, taskDao, sessionManager)
                            }
                        }
                    }

                    /*SocketHandler.TaskEvent.TASK_UPDATE_PRIVATE.name -> {
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
                        EventBus.getDefault().post(LocalEvents.RefreshTasksEvent())
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
                        EventBus.getDefault().post(LocalEvents.RefreshTasksEvent())
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
                            EventBus.getDefault().post(LocalEvents.RefreshTasksEvent())
                        }
                    }*/
                }
            } else if (socketData.module == "SubTaskComments") {
                if (socketData.eventType == SocketHandler.TaskEvent.SUBTASK_NEW_COMMENT.name) {
                    val newComment =
                        gson.fromJson<CommentsFilesUploadedSocketEventResponse>(
                            arguments,
                            object :
                                TypeToken<CommentsFilesUploadedSocketEventResponse>() {}.type
                        ).data
                    launch {
                        localSubTask.addFilesUnderComment(
                            newComment.subTaskId,
                            newComment,
                            newComment.id
                        )
                    }
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
            when (val response = dashboardRepository.uploadFiles(uploadFilesToServer.request)) {
                is ApiResponse.Success -> {
                    saveFilesInDB(
                        uploadFilesToServer.request.moduleName,
                        uploadFilesToServer.request.moduleId,
                        response.data.uploadData,
                        taskDao
                    )
//                    notificationTitle =
//                        if (filesCount > 1) "$filesCount files has been uploaded" else "$filesCount file has been uploaded"
//                    createNotification(
//                        LocalEvents.CreateNotification(
//                            moduleName = uploadFilesToServer.request.moduleName,
//                            moduleId = uploadFilesToServer.request.moduleId,
//                            notificationTitle = notificationTitle,
//                            isOngoing = false,
//                            indeterminate = false,
//                            notificationIcon = R.drawable.icon_upload
//                        )
//                    )

                }

                is ApiResponse.Error -> {
                    alert(response.error.message)

//                    createNotification(
//                        LocalEvents.CreateNotification(
//                            moduleName = uploadFilesToServer.request.moduleName,
//                            moduleId = uploadFilesToServer.request.moduleId,
//                            notificationTitle = response.error.message,
//                            isOngoing = false,
//                            indeterminate = false,
//                            notificationIcon = R.drawable.icon_upload
//                        )
//                    )
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

    fun endUserSession(context: Context) {
        launch {
            taskRepository.eraseTaskTable()
            taskRepository.eraseSubTaskTable()
            taskDao.deleteAllData()
            topicsV2Dao.deleteAllData()
            projectsV2Dao.deleteAll()
            connectionsV2Dao.deleteAll()
        }
        SocketHandler.sendLogout()
        sessionManager.endUserSession()
        // Cancel all periodic work with the tag "contactSync"
        WorkManager.getInstance(context)
            .cancelAllWorkByTag(ContactSyncWorker.CONTACT_SYNC_WORKER_TAG)
    }

    fun updateRootUnread(
        requireActivity: FragmentActivity
    ) {
        val toMeUnread = sessionManager.isToMeUnread()
        val fromMeUnread = sessionManager.isFromMeUnread()
        val hiddenUnread = sessionManager.isHiddenUnread()

        val sharedViewModel = ViewModelProvider(requireActivity).get(SharedViewModel::class.java)
        sharedViewModel.isToMeUnread.value = toMeUnread
        sharedViewModel.isFromMeUnread.value = fromMeUnread
        sharedViewModel.isHiddenUnread.value = hiddenUnread
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onInitSocketEventCallBack(event: LocalEvents.InitSocketEventCallBack?) {
        handleSocketEvents()
    }

    private fun updateContactsInDB() {
        launch {
            when (val response = dashboardRepository.getAllConnectionsV2()) {
                is ApiResponse.Success -> {
                    println("Socket: JOINED_TASK: contacts  ${response.data.contacts.size}")
                    connectionsV2Dao.insertAll(response.data.contacts)
                    sessionManager.saveSyncedContacts(response.data.contacts.toLightContacts())
                }

                is ApiResponse.Error -> {
                }
            }
        }
    }
}