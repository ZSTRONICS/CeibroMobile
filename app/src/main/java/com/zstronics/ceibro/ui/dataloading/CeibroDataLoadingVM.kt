package com.zstronics.ceibro.ui.dataloading

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.work.WorkManager
import com.zstronics.ceibro.base.KEY_TOKEN_VALID
import com.zstronics.ceibro.base.KEY_updatedAndNewContacts
import com.zstronics.ceibro.base.navgraph.host.NAVIGATION_Graph_ID
import com.zstronics.ceibro.base.navgraph.host.NAVIGATION_Graph_START_DESTINATION_ID
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.base.CookiesManager
import com.zstronics.ceibro.data.database.dao.ConnectionsV2Dao
import com.zstronics.ceibro.data.database.dao.ProjectsV2Dao
import com.zstronics.ceibro.data.database.dao.TaskV2Dao
import com.zstronics.ceibro.data.database.dao.TopicsV2Dao
import com.zstronics.ceibro.data.database.models.tasks.CeibroTaskV2
import com.zstronics.ceibro.data.database.models.tasks.Events
import com.zstronics.ceibro.data.local.FileAttachmentsDataSource
import com.zstronics.ceibro.data.remote.TaskRemoteDataSource
import com.zstronics.ceibro.data.repos.NotificationTaskData
import com.zstronics.ceibro.data.repos.dashboard.IDashboardRepository
import com.zstronics.ceibro.data.repos.dashboard.contacts.SyncContactsRequest
import com.zstronics.ceibro.data.repos.projects.IProjectRepository
import com.zstronics.ceibro.data.repos.task.TaskRepository
import com.zstronics.ceibro.data.repos.task.models.TopicsV2DatabaseEntity
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.extensions.getLocalContacts
import com.zstronics.ceibro.ui.contacts.ContactSyncWorker
import com.zstronics.ceibro.ui.contacts.compareContactsAndUpdateList
import com.zstronics.ceibro.ui.contacts.compareExistingAndNewContacts
import com.zstronics.ceibro.ui.contacts.findDeletedContacts
import com.zstronics.ceibro.ui.contacts.toLightContacts
import com.zstronics.ceibro.ui.socket.SocketHandler
import com.zstronics.ceibro.ui.tasks.task.TaskStatus
import com.zstronics.ceibro.utils.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CeibroDataLoadingVM @Inject constructor(
    override val viewState: CeibroDataLoadingState,
    val sessionManager: SessionManager,
    private val taskRepository: TaskRepository,
    private val projectRepository: IProjectRepository,
    val dashboardRepository: IDashboardRepository,
    val fileAttachmentsDataSource: FileAttachmentsDataSource,
    private val remoteTask: TaskRemoteDataSource,
    private val taskDao: TaskV2Dao,
    private val topicsV2Dao: TopicsV2Dao,
    private val projectsV2Dao: ProjectsV2Dao,
    private val projectDao: ProjectsV2Dao,
    private val connectionsV2Dao: ConnectionsV2Dao,
) : HiltBaseViewModel<ICeibroDataLoading.State>(), ICeibroDataLoading.ViewModel {
    var taskData: NotificationTaskData? = null
    var taskId: String = ""
    var navigationGraphId: Int = 0
    var startDestinationId: Int = 0

    init {
        sessionManager.setUser()
    }

    override fun onFirsTimeUiCreate(bundle: Bundle?) {
        super.onFirsTimeUiCreate(bundle)

        taskData = bundle?.getParcelable("notificationTaskData")
        val navigationGraphId1 = bundle?.getInt(NAVIGATION_Graph_ID, 0)
        val startDestinationId1 = bundle?.getInt(NAVIGATION_Graph_START_DESTINATION_ID, 0)
        taskData?.let {
            if (CookiesManager.jwtToken.isNullOrEmpty()) {
                sessionManager.setUser()
                sessionManager.setToken()
                sessionManager.isUserLoggedIn()
            }
            taskId = it.taskId
            if (navigationGraphId1 != null) {
                navigationGraphId = navigationGraphId1
            }
            if (startDestinationId1 != null) {
                startDestinationId = startDestinationId1
            }
        }

    }

    var apiSucceedCount = 0f
    suspend fun loadAppData(context: Context, callBack: () -> Unit) {
        Log.d("Data loading stared at ", DateUtils.getCurrentTimeStamp())
        GlobalScope.launch {
            val lastUpdatedAt = sessionManager.getUpdatedAtTimeStamp()
            when (val response = remoteTask.getAllTaskWithEventsSeparately(lastUpdatedAt)) {
                is ApiResponse.Success -> {
//                    taskDao.deleteAllEventsData()
//                    taskDao.deleteAllTasksData()
                    sessionManager.saveUpdatedAtTimeStamp(response.data.newData.latestUpdatedAt)

                    val allTasks = response.data.newData.allTasks ?: taskDao.getAllTasks()?.toMutableList() ?: mutableListOf<CeibroTaskV2>()
                    val allEvents = response.data.newData.allEvents ?: taskDao.getAllEvents()?.toMutableList() ?: mutableListOf<Events>()

                    taskDao.insertMultipleTasks(allTasks)
                    taskDao.insertMultipleEvents(allEvents)

                    val toMeNewTask = allTasks.filter { it.toMeState == TaskStatus.NEW.name.lowercase() }.sortedByDescending { it.updatedAt }.toMutableList()
                    val toMeOngoingTask = allTasks.filter { it.toMeState == TaskStatus.ONGOING.name.lowercase() }.sortedByDescending { it.updatedAt }.toMutableList()
                    val toMeDoneTask = allTasks.filter { it.toMeState == TaskStatus.DONE.name.lowercase() }.sortedByDescending { it.updatedAt }.toMutableList()

                    val fromMeUnreadTask = allTasks.filter { it.fromMeState == TaskStatus.UNREAD.name.lowercase() }.sortedByDescending { it.updatedAt }.toMutableList()
                    val fromMeOngoingTask = allTasks.filter { it.fromMeState == TaskStatus.ONGOING.name.lowercase() }.sortedByDescending { it.updatedAt }.toMutableList()
                    val fromMeDoneTask = allTasks.filter { it.fromMeState == TaskStatus.DONE.name.lowercase() }.sortedByDescending { it.updatedAt }.toMutableList()

                    val hiddenCanceledTask = allTasks.filter { it.hiddenState == TaskStatus.CANCELED.name.lowercase() }.sortedByDescending { it.updatedAt }.toMutableList()
                    val hiddenOngoingTask = allTasks.filter { it.hiddenState == TaskStatus.ONGOING.name.lowercase() }.sortedByDescending { it.updatedAt }.toMutableList()
                    val hiddenDoneTask = allTasks.filter { it.hiddenState == TaskStatus.DONE.name.lowercase() }.sortedByDescending { it.updatedAt }.toMutableList()

                    CookiesManager.toMeNewTasks.postValue(toMeNewTask)
                    CookiesManager.toMeOngoingTasks.postValue(toMeOngoingTask)
                    CookiesManager.toMeDoneTasks.postValue(toMeDoneTask)

                    CookiesManager.fromMeUnreadTasks.postValue(fromMeUnreadTask)
                    CookiesManager.fromMeOngoingTasks.postValue(fromMeOngoingTask)
                    CookiesManager.fromMeDoneTasks.postValue(fromMeDoneTask)

                    CookiesManager.hiddenCanceledTasks.postValue(hiddenCanceledTask)
                    CookiesManager.hiddenOngoingTasks.postValue(hiddenOngoingTask)
                    CookiesManager.hiddenDoneTasks.postValue(hiddenDoneTask)

                    apiSucceedCount++
                    callBack.invoke()
                }

                is ApiResponse.Error -> {
                    val newTasks = taskDao.getToMeTasks(TaskStatus.NEW.name.lowercase()).toMutableList()
                    val ongoingTasks = taskDao.getToMeTasks(TaskStatus.ONGOING.name.lowercase()).toMutableList()
                    val doneTasks = taskDao.getToMeTasks(TaskStatus.DONE.name.lowercase()).toMutableList()
                    val fromMeUnreadTasks = taskDao.getFromMeTasks(TaskStatus.UNREAD.name.lowercase()).toMutableList()
                    val fromMeOngoingTasks = taskDao.getFromMeTasks(TaskStatus.ONGOING.name.lowercase()).toMutableList()
                    val fromMeDoneTasks = taskDao.getFromMeTasks(TaskStatus.DONE.name.lowercase()).toMutableList()
                    val hiddenCanceledTasks = taskDao.getHiddenTasks(TaskStatus.CANCELED.name.lowercase()).toMutableList()
                    val hiddenOngoingTasks = taskDao.getHiddenTasks(TaskStatus.ONGOING.name.lowercase()).toMutableList()
                    val hiddenDoneTasks = taskDao.getHiddenTasks(TaskStatus.DONE.name.lowercase()).toMutableList()

                    CookiesManager.toMeNewTasks.postValue(newTasks)
                    CookiesManager.toMeOngoingTasks.postValue(ongoingTasks)
                    CookiesManager.toMeDoneTasks.postValue(doneTasks)

                    CookiesManager.fromMeUnreadTasks.postValue(fromMeUnreadTasks)
                    CookiesManager.fromMeOngoingTasks.postValue(fromMeOngoingTasks)
                    CookiesManager.fromMeDoneTasks.postValue(fromMeDoneTasks)

                    CookiesManager.hiddenCanceledTasks.postValue(hiddenCanceledTasks)
                    CookiesManager.hiddenOngoingTasks.postValue(hiddenOngoingTasks)
                    CookiesManager.hiddenDoneTasks.postValue(hiddenDoneTasks)

                    apiSucceedCount++
                    callBack.invoke()
                }
            }
        }.join()

        launch {
            when (val response = remoteTask.getAllTopics()) {
                is ApiResponse.Success -> {
                    topicsV2Dao.insertTopicData(
                        TopicsV2DatabaseEntity(
                            0,
                            topicsData = response.data
                        )
                    )
                    apiSucceedCount++
                    callBack.invoke()
                }

                is ApiResponse.Error -> {
                    apiSucceedCount++
                    callBack.invoke()
                }
            }
        }
        launch {
            when (val response = projectRepository.getProjectsV2()) {
                is ApiResponse.Success -> {
                    val data = response.data.allProjects
                    projectDao.insertMultipleProject(data)

                    apiSucceedCount++
                    callBack.invoke()
                }

                is ApiResponse.Error -> {
                    apiSucceedCount++
                    callBack.invoke()
                }
            }
        }
        launch {
            when (val response = dashboardRepository.getAllConnectionsV2()) {
                is ApiResponse.Success -> {
                    connectionsV2Dao.insertAll(response.data.contacts)
                    apiSucceedCount++
                    callBack.invoke()
                }

                is ApiResponse.Error -> {
                    apiSucceedCount++
                    callBack.invoke()
                }
            }
        }
        GlobalScope.launch {
            /*Contacts sync */
            val user = sessionManager.getUser().value
            sessionManager.saveBooleanValue(
                KEY_TOKEN_VALID,
                !CookiesManager.jwtToken.isNullOrEmpty()
            )

            val roomContacts = connectionsV2Dao.getAll()

            val phoneContacts = getLocalContacts(context, sessionManager)

            val manualContacts = sessionManager.getSyncedContacts() ?: emptyList()

            val contacts = if (user?.autoContactSync == true) {
                phoneContacts
            } else {
                manualContacts
            }

            val deletedContacts =
                findDeletedContacts(roomContacts, contacts).toLightContacts()     //ok

            var updatedAndNewContacts = mutableListOf<SyncContactsRequest.CeibroContactLight>()

            val updatedContacts =
                compareExistingAndNewContacts(roomContacts, contacts)
            updatedAndNewContacts.addAll(updatedContacts)

            if (user?.autoContactSync == false && phoneContacts.size > 0) {
                val updatedContacts2 =
                    compareContactsAndUpdateList(roomContacts, phoneContacts)
                updatedAndNewContacts.addAll(updatedContacts2)
            }

//            val newContacts = findNewContacts(roomContacts, contacts)
//            updatedAndNewContacts.addAll(newContacts)

            // Delete contacts API call
            if (sessionManager.isLoggedIn() && deletedContacts.isNotEmpty()) {

                val isDeleteAll = deletedContacts.size == roomContacts.size

                val contactsToDelete: List<SyncContactsRequest.CeibroContactLight> =
                    if (isDeleteAll) emptyList()
                    else deletedContacts

                val request = SyncContactsRequest(contacts = contactsToDelete)
                when (val response =
                    dashboardRepository.syncDeletedContacts(isDeleteAll, request)) {
                    is ApiResponse.Success -> {
                        updateLocalContacts(callBack)
                    }

                    is ApiResponse.Error -> {
                        apiSucceedCount++
                        callBack.invoke()
                    }
                }
            } else {
                apiSucceedCount++
                callBack.invoke()
            }

            updatedAndNewContacts =
                updatedAndNewContacts.filter { it.phoneNumber != user?.phoneNumber }.toMutableList()

            launch {
                if (sessionManager.isLoggedIn() && updatedAndNewContacts.isNotEmpty()) {
                    sessionManager.saveIntegerValue(
                        KEY_updatedAndNewContacts,
                        updatedAndNewContacts.size
                    )
                    val request = SyncContactsRequest(contacts = updatedAndNewContacts)
                    when (val response =
                        dashboardRepository.syncContacts(request)) {
                        is ApiResponse.Success -> {
                            updateLocalContacts(callBack)
                        }

                        is ApiResponse.Error -> {
                            apiSucceedCount++
                            callBack.invoke()
                        }
                    }
                } else {
                    sessionManager.saveIntegerValue(KEY_updatedAndNewContacts, -1)
                    updateLocalContacts(callBack)
                }
            }
        }
    }

    private suspend fun updateLocalContacts(callBack: () -> Unit) {
        when (val response = dashboardRepository.getAllConnectionsV2()) {
            is ApiResponse.Success -> {
                connectionsV2Dao.insertAll(response.data.contacts)
                sessionManager.saveSyncedContacts(response.data.contacts.toLightContacts())
                apiSucceedCount++
                callBack.invoke()
            }

            is ApiResponse.Error -> {
                apiSucceedCount++
                callBack.invoke()
            }
        }
    }


    fun endUserSession(context: Context) {
        launch {
            taskRepository.eraseTaskTable()
            taskRepository.eraseSubTaskTable()
            taskDao.deleteAllEventsData()
            taskDao.deleteAllTasksData()
            topicsV2Dao.deleteAllData()
            projectsV2Dao.deleteAll()
            connectionsV2Dao.deleteAll()
            draftNewTaskV2Internal.deleteAllData()
        }
        SocketHandler.sendLogout()
        sessionManager.endUserSession()
        // Cancel all periodic work with the tag "contactSync"
        WorkManager.getInstance(context)
            .cancelAllWorkByTag(ContactSyncWorker.CONTACT_SYNC_WORKER_TAG)
    }
}