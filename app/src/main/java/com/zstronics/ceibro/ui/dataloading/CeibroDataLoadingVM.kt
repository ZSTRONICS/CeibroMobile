package com.zstronics.ceibro.ui.dataloading

import android.content.Context
import android.util.Log
import androidx.work.WorkManager
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.database.dao.ConnectionsV2Dao
import com.zstronics.ceibro.data.database.dao.ProjectsV2Dao
import com.zstronics.ceibro.data.database.dao.TaskDaoHelper
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
import com.zstronics.ceibro.ui.contacts.findDeletedContacts
import com.zstronics.ceibro.ui.contacts.findNewContacts
import com.zstronics.ceibro.ui.contacts.toLightContacts
import com.zstronics.ceibro.ui.socket.SocketHandler
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
    fun loadAppData(context: Context, callBack: () -> Unit) {
        Log.d("Data loading stared at ", DateUtils.getCurrentTimeStamp())
        GlobalScope.launch {
            val lastUpdatedAt = sessionManager.getUpdatedAtTimeStamp()
            when (val response = remoteTask.syncAllTask(lastUpdatedAt)) {
                is ApiResponse.Success -> {
                    sessionManager.saveUpdatedAtTimeStamp(response.data.allTasks.latestUpdatedAt)

                    // START => Update TO ME into database
                    val toMeLocal = TaskDaoHelper(taskDao).getTasks(TaskRootStateTags.ToMe.tagValue)
//                    val toMeLocal = taskDao.getTasks(TaskRootStateTags.ToMe.tagValue)
                    val toMeRemote = response.data.allTasks.toMe
                    // insert data on first time
                    if (TaskDaoHelper(taskDao).isTaskListEmpty(TaskRootStateTags.ToMe.tagValue, toMeLocal)) {
                        TaskDaoHelper(taskDao).insertTaskData(
                            TasksV2DatabaseEntity(
                                rootState = TaskRootStateTags.ToMe.tagValue,
                                allTasks = toMeRemote
                            )
                        )
                    } else {
                        toMeLocal.allTasks.new.addAll(toMeRemote.new)
                        toMeLocal.allTasks.unread.addAll(toMeRemote.unread)
                        toMeLocal.allTasks.ongoing.addAll(toMeRemote.ongoing)
                        toMeLocal.allTasks.done.addAll(toMeRemote.done)

                        TaskDaoHelper(taskDao).insertTaskData(
                            TasksV2DatabaseEntity(
                                rootState = TaskRootStateTags.ToMe.tagValue,
                                allTasks = toMeRemote
                            )
                        )
                    }
                    // END => Update TO ME into database


                    // START => Update FROM ME into database
                    val fromMeLocal = TaskDaoHelper(taskDao).getTasks(TaskRootStateTags.FromMe.tagValue)
                    val fromMeRemote = response.data.allTasks.fromMe
                    // insert data on first time
                    if (TaskDaoHelper(taskDao).isTaskListEmpty(TaskRootStateTags.FromMe.tagValue, fromMeLocal)) {
                        TaskDaoHelper(taskDao).insertTaskData(
                            TasksV2DatabaseEntity(
                                rootState = TaskRootStateTags.FromMe.tagValue,
                                allTasks = fromMeRemote
                            )
                        )
                    } else {
                        fromMeLocal.allTasks.new.addAll(fromMeRemote.new)
                        fromMeLocal.allTasks.unread.addAll(fromMeRemote.unread)
                        fromMeLocal.allTasks.ongoing.addAll(fromMeRemote.ongoing)
                        fromMeLocal.allTasks.done.addAll(fromMeRemote.done)

                        TaskDaoHelper(taskDao).insertTaskData(
                            TasksV2DatabaseEntity(
                                rootState = TaskRootStateTags.FromMe.tagValue,
                                allTasks = fromMeLocal.allTasks
                            )
                        )
                    }
                    // END => Update FROM ME into database

                    // START => Update HIDDEN into database
                    val hiddenLocal = TaskDaoHelper(taskDao).getTasks(TaskRootStateTags.Hidden.tagValue)
                    val hiddenRemote = response.data.allTasks.hidden
                    // insert data on first time
                    if (TaskDaoHelper(taskDao).isTaskListEmpty(TaskRootStateTags.Hidden.tagValue, hiddenLocal)) {
                        TaskDaoHelper(taskDao).insertTaskData(
                            TasksV2DatabaseEntity(
                                rootState = TaskRootStateTags.Hidden.tagValue,
                                allTasks = hiddenRemote
                            )
                        )
                    } else {
                        hiddenLocal.allTasks.ongoing.addAll(hiddenRemote.ongoing)
                        hiddenLocal.allTasks.done.addAll(hiddenRemote.done)
                        hiddenLocal.allTasks.canceled.addAll(hiddenRemote.canceled)

                        TaskDaoHelper(taskDao).insertTaskData(
                            TasksV2DatabaseEntity(
                                rootState = TaskRootStateTags.Hidden.tagValue,
                                allTasks = hiddenLocal.allTasks
                            )
                        )
                    }
                    // END => Update FROM ME into database

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
        launch {
            /*Contacts sync */
            val user = sessionManager.getUser().value

            val roomContacts = connectionsV2Dao.getAll()

            val phoneContacts = getLocalContacts(context)

            val manualContacts = sessionManager.getSyncedContacts() ?: emptyList()

            val contacts = if (user?.autoContactSync == true) {
                phoneContacts
            } else {
                manualContacts
            }

            val deletedContacts = findDeletedContacts(roomContacts, contacts).toLightContacts()

            val updatedContacts =
                compareContactsAndUpdateList(roomContacts, contacts)

            val updatedAndNewContacts = mutableListOf<SyncContactsRequest.CeibroContactLight>()
            updatedAndNewContacts.addAll(updatedContacts)

            val newContacts = findNewContacts(roomContacts, contacts)
            updatedAndNewContacts.addAll(newContacts)

            // Delete contacts API call
            if (deletedContacts.isNotEmpty()) {

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

            if (sessionManager.isLoggedIn() && updatedAndNewContacts.isNotEmpty()) {
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
                updateLocalContacts(callBack)
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
}