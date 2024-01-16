package com.zstronics.ceibro.ui.splash

import android.content.Context
import androidx.work.WorkManager
import com.onesignal.OneSignal
import com.zstronics.ceibro.base.validator.IValidator
import com.zstronics.ceibro.base.validator.Validator
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.database.dao.ConnectionsV2Dao
import com.zstronics.ceibro.data.database.dao.DrawingPinsV2Dao
import com.zstronics.ceibro.data.database.dao.FloorsV2Dao
import com.zstronics.ceibro.data.database.dao.GroupsV2Dao
import com.zstronics.ceibro.data.database.dao.ProjectsV2Dao
import com.zstronics.ceibro.data.database.dao.TaskV2Dao
import com.zstronics.ceibro.data.database.dao.TopicsV2Dao
import com.zstronics.ceibro.data.repos.auth.IAuthRepository
import com.zstronics.ceibro.data.repos.task.TaskRepository
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.ui.contacts.ContactSyncWorker
import com.zstronics.ceibro.ui.socket.SocketHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    override val viewState: SplashState,
    override var validator: Validator?,
    private val repository: IAuthRepository,
    private val taskRepository: TaskRepository,
    private val taskDao: TaskV2Dao,
    private val topicsV2Dao: TopicsV2Dao,
    private val projectsV2Dao: ProjectsV2Dao,
    private val floorV2Dao: FloorsV2Dao,
    private val groupV2Dao: GroupsV2Dao,
    private val connectionsV2Dao: ConnectionsV2Dao,
    private val drawingPinsDao: DrawingPinsV2Dao,
    val sessionManager: SessionManager
) : HiltBaseViewModel<ISplash.State>(), ISplash.ViewModel, IValidator {

    init {
        if (sessionManager.isUserLoggedIn()) {
//            getProfile()
        }
    }

    override fun getProfile() {
        launch {
            when (val response = repository.getUserProfile()) {

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
            drawingPinsDao.deleteAll()
        }
        sessionManager.endUserSession()
        // Cancel all periodic work with the tag "contactSync"
        WorkManager.getInstance(context)
            .cancelAllWorkByTag(ContactSyncWorker.CONTACT_SYNC_WORKER_TAG)
    }

    fun deleteTaskAndEvents() {
        launch {
            taskDao.deleteAllEventsData()
            taskDao.deleteAllTasksData()
        }
    }

}