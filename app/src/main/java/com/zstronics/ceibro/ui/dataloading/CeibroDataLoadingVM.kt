package com.zstronics.ceibro.ui.dataloading

import android.content.Context
import android.util.Log
import androidx.work.WorkManager
import com.zstronics.ceibro.base.KEY_TOKEN_VALID
import com.zstronics.ceibro.base.KEY_updatedAndNewContacts
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.base.CookiesManager
import com.zstronics.ceibro.data.database.dao.ConnectionsV2Dao
import com.zstronics.ceibro.data.database.dao.ProjectsV2Dao
import com.zstronics.ceibro.data.database.dao.TaskV2DaoHelper
import com.zstronics.ceibro.data.database.dao.TaskV2Dao
import com.zstronics.ceibro.data.database.dao.TopicsV2Dao
import com.zstronics.ceibro.data.local.FileAttachmentsDataSource
import com.zstronics.ceibro.data.remote.TaskRemoteDataSource
import com.zstronics.ceibro.data.repos.dashboard.IDashboardRepository
import com.zstronics.ceibro.data.repos.dashboard.contacts.SyncContactsRequest
import com.zstronics.ceibro.data.repos.projects.IProjectRepository
import com.zstronics.ceibro.data.repos.projects.projectsmain.ProjectsV2DatabaseEntity
import com.zstronics.ceibro.data.repos.task.TaskRepository
import com.zstronics.ceibro.data.repos.task.TaskRootStateTags
import com.zstronics.ceibro.data.repos.task.models.TasksV2DatabaseEntity
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
    init {
        sessionManager.setUser()
    }

    var apiSucceedCount = 0f
    suspend fun loadAppData(context: Context, callBack: () -> Unit) {
        Log.d("Data loading stared at ", DateUtils.getCurrentTimeStamp())
        GlobalScope.launch {
            val lastUpdatedAt = sessionManager.getUpdatedAtTimeStamp()
            when (val response = remoteTask.getAllTaskWithEventsSeparately(lastUpdatedAt)) {
                is ApiResponse.Success -> {
                    sessionManager.saveUpdatedAtTimeStamp(response.data.newData.latestUpdatedAt)
                    /*// START => Update TO ME into database
                    val toMeLocal =
                        TaskV2DaoHelper(taskDao).getTasks(TaskRootStateTags.ToMe.tagValue)
//                    val toMeLocal = taskDao.getTasks(TaskRootStateTags.ToMe.tagValue)
                    val toMeRemote = response.data.allTasks.toMe
                    // insert data on first time
                    if (TaskV2DaoHelper(taskDao).isTaskListEmpty(
                            TaskRootStateTags.ToMe.tagValue,
                            toMeLocal
                        )
                    ) {
                        TaskV2DaoHelper(taskDao).insertTaskData(
                            TasksV2DatabaseEntity(
                                rootState = TaskRootStateTags.ToMe.tagValue,
                                allTasks = toMeRemote
                            )
                        )
                    } else {
//                        toMeLocal.allTasks.new.addAll(toMeRemote.new)
//                        toMeLocal.allTasks.unread.addAll(toMeRemote.unread)
//                        toMeLocal.allTasks.ongoing.addAll(toMeRemote.ongoing)
//                        toMeLocal.allTasks.done.addAll(toMeRemote.done)
                        toMeLocal.allTasks.new = toMeRemote.new
                        toMeLocal.allTasks.ongoing = toMeRemote.ongoing
                        toMeLocal.allTasks.done = toMeRemote.done

                        TaskV2DaoHelper(taskDao).insertTaskData(
                            TasksV2DatabaseEntity(
                                rootState = TaskRootStateTags.ToMe.tagValue,
                                allTasks = toMeRemote
                            )
                        )
                    }
                    // END => Update TO ME into database


                    // START => Update FROM ME into database
                    val fromMeLocal =
                        TaskV2DaoHelper(taskDao).getTasks(TaskRootStateTags.FromMe.tagValue)
                    val fromMeRemote = response.data.allTasks.fromMe
                    // insert data on first time
                    if (TaskV2DaoHelper(taskDao).isTaskListEmpty(
                            TaskRootStateTags.FromMe.tagValue,
                            fromMeLocal
                        )
                    ) {
                        TaskV2DaoHelper(taskDao).insertTaskData(
                            TasksV2DatabaseEntity(
                                rootState = TaskRootStateTags.FromMe.tagValue,
                                allTasks = fromMeRemote
                            )
                        )
                    } else {
//                        fromMeLocal.allTasks.new.addAll(fromMeRemote.new)
//                        fromMeLocal.allTasks.unread.addAll(fromMeRemote.unread)
//                        fromMeLocal.allTasks.ongoing.addAll(fromMeRemote.ongoing)
//                        fromMeLocal.allTasks.done.addAll(fromMeRemote.done)
                        fromMeLocal.allTasks.new = mutableListOf()
                        fromMeLocal.allTasks.ongoing = fromMeRemote.ongoing
                        fromMeLocal.allTasks.done = fromMeRemote.done

                        TaskV2DaoHelper(taskDao).insertTaskData(
                            TasksV2DatabaseEntity(
                                rootState = TaskRootStateTags.FromMe.tagValue,
                                allTasks = fromMeLocal.allTasks
                            )
                        )
                    }
                    // END => Update FROM ME into database

                    // START => Update HIDDEN into database
                    val hiddenLocal =
                        TaskV2DaoHelper(taskDao).getTasks(TaskRootStateTags.Hidden.tagValue)
                    val hiddenRemote = response.data.allTasks.hidden
                    // insert data on first time
                    if (TaskV2DaoHelper(taskDao).isTaskListEmpty(
                            TaskRootStateTags.Hidden.tagValue,
                            hiddenLocal
                        )
                    ) {
                        TaskV2DaoHelper(taskDao).insertTaskData(
                            TasksV2DatabaseEntity(
                                rootState = TaskRootStateTags.Hidden.tagValue,
                                allTasks = hiddenRemote
                            )
                        )
                    } else {
//                        hiddenLocal.allTasks.ongoing.addAll(hiddenRemote.ongoing)
//                        hiddenLocal.allTasks.done.addAll(hiddenRemote.done)
//                        hiddenLocal.allTasks.canceled.addAll(hiddenRemote.canceled)
                        hiddenLocal.allTasks.ongoing = hiddenRemote.ongoing
                        hiddenLocal.allTasks.done = hiddenRemote.done
                        hiddenLocal.allTasks.canceled = hiddenRemote.canceled

                        TaskV2DaoHelper(taskDao).insertTaskData(
                            TasksV2DatabaseEntity(
                                rootState = TaskRootStateTags.Hidden.tagValue,
                                allTasks = hiddenLocal.allTasks
                            )
                        )
                    }
                    // END => Update FROM ME into database*/

                    val allTasks = response.data.newData.allTasks
                    val allEvents = response.data.newData.allEvents

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
                    val data = response.data.projects
                    if (data != null) {
                        projectDao.insert(ProjectsV2DatabaseEntity(1, projects = data))
                    }
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
            taskDao.deleteAllTasksData()
            taskDao.deleteAllEventsData()
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
}