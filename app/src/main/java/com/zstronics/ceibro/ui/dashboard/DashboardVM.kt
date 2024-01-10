package com.zstronics.ceibro.ui.dashboard

import android.content.Context
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.work.WorkManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.onesignal.OneSignal
import com.zstronics.ceibro.BuildConfig
import com.zstronics.ceibro.base.viewmodel.Dispatcher
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.database.dao.ConnectionsV2Dao
import com.zstronics.ceibro.data.database.dao.DrawingPinsV2Dao
import com.zstronics.ceibro.data.database.dao.FloorsV2Dao
import com.zstronics.ceibro.data.database.dao.GroupsV2Dao
import com.zstronics.ceibro.data.database.dao.ProjectsV2Dao
import com.zstronics.ceibro.data.database.dao.TaskV2Dao
import com.zstronics.ceibro.data.database.dao.TopicsV2Dao
import com.zstronics.ceibro.data.local.FileAttachmentsDataSource
import com.zstronics.ceibro.data.local.SubTaskLocalDataSource
import com.zstronics.ceibro.data.remote.TaskRemoteDataSource
import com.zstronics.ceibro.data.repos.auth.IAuthRepository
import com.zstronics.ceibro.data.repos.auth.login.UserUpdatedSocketResponse
import com.zstronics.ceibro.data.repos.chat.messages.socket.SocketEventTypeResponse
import com.zstronics.ceibro.data.repos.dashboard.IDashboardRepository
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.AllCeibroConnections
import com.zstronics.ceibro.data.repos.projects.IProjectRepository
import com.zstronics.ceibro.data.repos.projects.drawing.ProjectDrawingUploadedSocketResponse
import com.zstronics.ceibro.data.repos.projects.group.ProjectGroupV2CreatedSocketResponse
import com.zstronics.ceibro.data.repos.projects.floor.ProjectFloorV2CreatedSocketResponse
import com.zstronics.ceibro.data.repos.projects.group.ProjectGroupV2DeletedSocketResponse
import com.zstronics.ceibro.data.repos.projects.projectsmain.ProjectV2CreatedUpdatedSocketResponse
import com.zstronics.ceibro.data.repos.projects.projectsmain.ProjectsWithMembersResponse
import com.zstronics.ceibro.data.repos.task.TaskRepository
import com.zstronics.ceibro.data.repos.task.models.TopicsV2DatabaseEntity
import com.zstronics.ceibro.data.repos.task.models.v2.NewTaskV2Entity
import com.zstronics.ceibro.data.repos.task.models.v2.SocketForwardedToMeNewTaskEventV2Response
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
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class DashboardVM @Inject constructor(
    override val viewState: DashboardState,
    val sessionManager: SessionManager,
    val localSubTask: SubTaskLocalDataSource,
    private val taskRepository: TaskRepository,
    val remoteTask: TaskRemoteDataSource,
    private val projectRepository: IProjectRepository,
    val dashboardRepository: IDashboardRepository,
    private val authRepository: IAuthRepository,
    val fileAttachmentsDataSource: FileAttachmentsDataSource,
    private val taskDao: TaskV2Dao,
    private val drawingPinsDao: DrawingPinsV2Dao,
    private val topicsV2Dao: TopicsV2Dao,
    private val projectsV2Dao: ProjectsV2Dao,
    private val floorV2Dao: FloorsV2Dao,
    private val groupV2Dao: GroupsV2Dao,
    private val connectionsV2Dao: ConnectionsV2Dao,
) : HiltBaseViewModel<IDashboard.State>(), IDashboard.ViewModel {
    var user = sessionManager.getUser().value
    var userId: String? = ""

    companion object {
        val DataQueue: MutableList<Pair<SocketEventTypeResponse, String>> = mutableListOf();
        val executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        var isProcessing = false;
    }

    init {
        userId = sessionManager.getUserId()
        user = sessionManager.getUser().value
        EventBus.getDefault().register(this)

        // Schedule the processQueue method to run periodically
        val initialDelay = 100L // Initial delay in milliseconds
        val period = 100L // Period between executions in milliseconds
        executor.scheduleAtFixedRate(
            this::processQueue,
            initialDelay,
            period,
            TimeUnit.MILLISECONDS
        )

    }

    private fun processQueue() {
        if (DataQueue.isEmpty() || isProcessing) {
        } else {
//            println("Heartbeat SocketEvent Calling => EXECUTING FUNCTION ${DataQueue[0].first.eventType}")
            isProcessing = true
            val data = DataQueue[0]
            processSocketData(data.first, data.second)
        }
    }


    fun pushToQueue(socketData: SocketEventTypeResponse, arguments: String) {
        DataQueue.add(Pair(socketData, arguments));
    }


    private fun processSocketData(socketData: SocketEventTypeResponse, arguments: String) {
        val gson = Gson()
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
                        sessionManager,
                        drawingPinsDao
                    )

//                    val notificationTitle: String =
//                        if (taskCreatedData.data?.topic?.topic.isNullOrEmpty())
//                            "" else taskCreatedData.data?.topic?.topic.toString()


                    /*EventBus.getDefault().post(
                        LocalEvents.CreateSimpleNotification(
                            moduleId = taskCreatedData.data?.id ?: "",
                            moduleName = socketData.module,
                            notificationTitle =
                            if (notificationTitle.isNotEmpty()) {
                                "New task created as \"$notificationTitle\""
                            } else {
                                "New task created"
                            },
                            notificationDescription = taskCreatedData.data?.description ?: "",
                            isOngoing = false,
                            indeterminate = false,
                            notificationIcon = R.drawable.app_logo,
                            isTaskCreated = true
                        )
                    )*/

                }

                SocketHandler.TaskEvent.TASK_FORWARDED.name -> {
                    val eventData = gson.fromJson<SocketNewTaskEventV2Response>(
                        arguments,
                        object : TypeToken<SocketNewTaskEventV2Response>() {}.type
                    ).data
                    launch {
                        updateForwardTaskInLocal(eventData, taskDao, userId, sessionManager, drawingPinsDao)
                    }
                }

                SocketHandler.TaskEvent.TASK_FORWARDED_TO_ME.name -> {
                    val eventData = gson.fromJson<SocketForwardedToMeNewTaskEventV2Response>(
                        arguments,
                        object : TypeToken<SocketForwardedToMeNewTaskEventV2Response>() {}.type
                    ).data
                    launch {
                        updateForwardedToMeNewTaskInLocal(eventData, taskDao, userId, sessionManager, drawingPinsDao)
                    }
                }

                SocketHandler.TaskEvent.TASK_SEEN.name -> {
                    val taskSeen = gson.fromJson<SocketTaskSeenV2Response>(
                        arguments,
                        object : TypeToken<SocketTaskSeenV2Response>() {}.type
                    ).data
                    launch {
                        updateGenericTaskSeenInLocal(taskSeen, taskDao, userId, sessionManager, drawingPinsDao)
                    }
                }

                SocketHandler.TaskEvent.NEW_TASK_COMMENT.name, SocketHandler.TaskEvent.TASK_DONE.name, SocketHandler.TaskEvent.CANCELED_TASK.name,
                SocketHandler.TaskEvent.UN_CANCEL_TASK.name, SocketHandler.TaskEvent.JOINED_TASK.name -> {
                    val commentData = gson.fromJson<SocketNewTaskEventV2Response>(
                        arguments,
                        object : TypeToken<SocketNewTaskEventV2Response>() {}.type
                    ).data
                    Log.d("TASK_EVENT", commentData?.taskId.toString())

                    if (socketData.eventType == SocketHandler.TaskEvent.NEW_TASK_COMMENT.name) {
                        launch {
                            updateTaskCommentInLocal(
                                commentData,
                                taskDao,
                                userId,
                                sessionManager, drawingPinsDao
                            )
                        }
                    }
                    if (socketData.eventType == SocketHandler.TaskEvent.CANCELED_TASK.name) {
                        if (commentData != null) {
                            launch {
                                updateTaskCanceledInLocal(
                                    commentData,
                                    taskDao,
                                    userId,
                                    sessionManager, drawingPinsDao
                                )
                            }
                        }
                    }
                    if (socketData.eventType == SocketHandler.TaskEvent.UN_CANCEL_TASK.name) {
                        if (commentData != null) {
                            launch {
                                updateTaskUnCanceledInLocal(
                                    commentData,
                                    taskDao,
                                    sessionManager, drawingPinsDao
                                )
                            }
                        }
                    }
                    if (socketData.eventType == SocketHandler.TaskEvent.TASK_DONE.name) {
                        if (commentData != null) {
                            launch {
                                updateTaskDoneInLocal(commentData, taskDao, sessionManager, drawingPinsDao)
                            }
                        }
                    }
                    if (socketData.eventType == SocketHandler.TaskEvent.JOINED_TASK.name) {
                        launch {
                            updateTaskJoinedInLocal(commentData, taskDao, sessionManager, drawingPinsDao)
                        }
                        updateContactsInDB {

                        }
                    }
                }

                SocketHandler.TaskEvent.TASK_HIDDEN.name, SocketHandler.TaskEvent.TASK_SHOWN.name -> {
                    val hideData = gson.fromJson<SocketHideUnHideTaskResponse>(
                        arguments,
                        object : TypeToken<SocketHideUnHideTaskResponse>() {}.type
                    ).data
                    if (socketData.eventType == SocketHandler.TaskEvent.TASK_HIDDEN.name) {
                        if (hideData != null) {
                            launch {
                                updateTaskHideInLocal(hideData, taskDao, sessionManager, drawingPinsDao)
                            }
                        }
                    }
                    if (socketData.eventType == SocketHandler.TaskEvent.TASK_SHOWN.name) {
                        if (hideData != null) {
                            launch {
                                updateTaskUnHideInLocal(hideData, taskDao, sessionManager, drawingPinsDao)
                            }
                        }
                    }
                }
            }
        }
        else if (socketData.module == "project") {
            when (socketData.eventType) {
                SocketHandler.ProjectEvent.PROJECT_CREATED.name -> {
                    val newProject = gson.fromJson<ProjectV2CreatedUpdatedSocketResponse>(
                            arguments,
                            object : TypeToken<ProjectV2CreatedUpdatedSocketResponse>() {}.type
                        ).data

                    addCreatedProjectInLocal(newProject, projectsV2Dao)
                }

                SocketHandler.ProjectEvent.PROJECT_UPDATED.name -> {
                    val updatedProject = gson.fromJson<ProjectV2CreatedUpdatedSocketResponse>(
                            arguments,
                            object : TypeToken<ProjectV2CreatedUpdatedSocketResponse>() {}.type
                        ).data

                    updateProjectInLocal(updatedProject, projectsV2Dao)
                }

                SocketHandler.ProjectEvent.REFRESH_PROJECTS.name -> {

                    EventBus.getDefault().post(LocalEvents.ProjectRefreshEvent())

                }


                SocketHandler.ProjectEvent.PROJECT_FLOOR_CREATED.name -> {
                    val newFloor =
                        gson.fromJson<ProjectFloorV2CreatedSocketResponse>(
                            arguments,
                            object : TypeToken<ProjectFloorV2CreatedSocketResponse>() {}.type
                        ).data

                    addCreatedFloorInLocal(newFloor, floorV2Dao)
//                    EventBus.getDefault().post(LocalEvents.GroupCreatedEvent(newGroup))
                }

                SocketHandler.ProjectEvent.PROJECT_GROUP_CREATED.name -> {
                    val newGroup =
                        gson.fromJson<ProjectGroupV2CreatedSocketResponse>(
                            arguments,
                            object : TypeToken<ProjectGroupV2CreatedSocketResponse>() {}.type
                        ).data
                    addGroupCreatedInLocal(newGroup, groupV2Dao, projectsV2Dao)
//                    EventBus.getDefault().post(LocalEvents.GroupCreatedEvent(newGroup))
                }

                SocketHandler.ProjectEvent.PROJECT_GROUP_UPDATED.name -> {
                    try {
                        val updatedGroup =
                            gson.fromJson<ProjectGroupV2CreatedSocketResponse>(
                                arguments,
                                object : TypeToken<ProjectGroupV2CreatedSocketResponse>() {}.type
                            ).data

                        addGroupCreatedInLocal(updatedGroup, groupV2Dao, projectsV2Dao)
//                        EventBus.getDefault().post(LocalEvents.GroupCreatedEvent(updatedGroup))
                    } catch (e: Exception) {
                        println("Some data error")
                    }
                }

                SocketHandler.ProjectEvent.PROJECT_GROUP_DELETED.name -> {
                    try {
                        val groupIdToDelete =
                            gson.fromJson<ProjectGroupV2DeletedSocketResponse>(
                                arguments,
                                object : TypeToken<ProjectGroupV2DeletedSocketResponse>() {}.type
                            ).data
                        deleteGroupInLocal(groupIdToDelete, groupV2Dao)
//                        EventBus.getDefault().post(LocalEvents.GroupCreatedEvent(updatedGroup))
                    } catch (e: Exception) {
                        println("Some data error")
                    }
                }

                SocketHandler.ProjectEvent.DRAWING_FILE_UPLOADED.name -> {
                    try {
                        val uploadedFile =
                            gson.fromJson<ProjectDrawingUploadedSocketResponse>(
                                arguments,
                                object : TypeToken<ProjectDrawingUploadedSocketResponse>() {}.type
                            ).data

                        addUploadedDrawingInLocal(uploadedFile, groupV2Dao, floorV2Dao)
                    } catch (e: Exception) {
                        println("Some data error")
                    }
                }

            }
        } else if (socketData.module == "user") {
            when (socketData.eventType) {
                SocketHandler.UserEvent.USER_UPDATED.name -> {
                    val updatedUser =
                        gson.fromJson<UserUpdatedSocketResponse>(
                            arguments,
                            object : TypeToken<UserUpdatedSocketResponse>() {}.type
                        ).data


                    if (updatedUser.id == sessionManager.getUserId()) {
                        val oldUser = sessionManager.getUser().value

                        oldUser?.let {
                            it.firstName = updatedUser.firstName
                            it.surName = updatedUser.surName
                            it.email = updatedUser.email
                            it.phoneNumber = updatedUser.phoneNumber
                            it.profilePic = updatedUser.profilePic
                            it.jobTitle = updatedUser.jobTitle
                            it.companyName = updatedUser.companyName
                        }
                        println(" user id!!! ${updatedUser.id}==${sessionManager.getUserId()}")

                        sessionManager.updateUser(oldUser)
                        EventBus.getDefault().post(LocalEvents.UserDataUpdated())
                    } else {
                        println("Invalid user id!!! ${updatedUser.id}==${sessionManager.getUserId()}")
                        updateContactsInDB {
                            if (it) {
                                EventBus.getDefault().post(LocalEvents.UpdateConnections)
                            }
                        }

                    }


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

        DataQueue.removeAt(0);
        isProcessing = false;
    }

    suspend fun getDraftTasks(): List<NewTaskV2Entity> {
        return draftNewTaskV2Internal.getUnSyncedRecords() ?: emptyList()
    }

    suspend fun getTopicList(): TopicsV2DatabaseEntity? {

        val topic: TopicsV2DatabaseEntity? = topicsV2Dao.getTopicsData()

        return topic

    }


    suspend fun getContactsList(): List<AllCeibroConnections.CeibroConnection> {
        val contactsFromDatabase: List<AllCeibroConnections.CeibroConnection> =
            connectionsV2Dao.getAll()

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
            }
            println("Heartbeat SocketEvent: ${socketData.eventType}")
            if (socketData.eventType.equals(SocketHandler.TaskEvent.TASK_SEEN.name, true)) {
                println("Heartbeat SocketEvent TASK_SEEN DATA_RECEIVED: ${arguments}")
            }
            if (socketData.eventType.equals(SocketHandler.TaskEvent.TASK_CREATED.name, true)) {
                println("Heartbeat SocketEvent TASK_CREATED DATA: ${arguments}")
            }

            pushToQueue(socketData, arguments);
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
        //  executor.shutdown()
        //    executor.awaitTermination(1, TimeUnit.MINUTES)
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
        val oneSignalPlayerId = OneSignal.getDeviceState()?.userId
        SocketHandler.sendLogout(oneSignalPlayerId)
        launch {
            taskRepository.eraseTaskTable()
            taskRepository.eraseSubTaskTable()
            taskDao.deleteAllEventsData()
            taskDao.deleteAllTasksData()
            topicsV2Dao.deleteAllData()
            projectsV2Dao.deleteAll()
            groupV2Dao.deleteAll()
            floorV2Dao.deleteAll()
            connectionsV2Dao.deleteAll()
            draftNewTaskV2Internal.deleteAllData()
        }
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

    //Moved to fragment
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    fun onInitSocketEventCallBack(event: LocalEvents.InitSocketEventCallBack?) {
//        handleSocketEvents()
//    }

    private fun updateContactsInDB(callback: (result: Boolean) -> Unit) {
        launch {
            when (val response = dashboardRepository.getAllConnectionsV2()) {
                is ApiResponse.Success -> {
                    println("Socket: JOINED_TASK: contacts  ${response.data.contacts.size}")
                    connectionsV2Dao.insertAll(response.data.contacts)
                    sessionManager.saveSyncedContacts(response.data.contacts.toLightContacts())
                    callback.invoke(true)
                }

                is ApiResponse.Error -> {
                    callback.invoke(false)
                }
            }
        }
    }

}