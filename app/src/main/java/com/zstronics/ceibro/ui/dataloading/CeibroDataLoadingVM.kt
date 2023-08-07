package com.zstronics.ceibro.ui.dataloading

import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.database.dao.ConnectionsV2Dao
import com.zstronics.ceibro.data.database.dao.ProjectsV2Dao
import com.zstronics.ceibro.data.database.dao.TaskV2Dao
import com.zstronics.ceibro.data.local.FileAttachmentsDataSource
import com.zstronics.ceibro.data.remote.TaskRemoteDataSource
import com.zstronics.ceibro.data.repos.dashboard.IDashboardRepository
import com.zstronics.ceibro.data.repos.projects.IProjectRepository
import com.zstronics.ceibro.data.repos.projects.projectsmain.ProjectsV2DatabaseEntity
import com.zstronics.ceibro.data.repos.task.TaskRootStateTags
import com.zstronics.ceibro.data.repos.task.models.TasksV2DatabaseEntity
import com.zstronics.ceibro.data.sessions.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CeibroDataLoadingVM @Inject constructor(
    override val viewState: CeibroDataLoadingState,
    val sessionManager: SessionManager,
    private val projectRepository: IProjectRepository,
    val dashboardRepository: IDashboardRepository,
    val fileAttachmentsDataSource: FileAttachmentsDataSource,
    private val remoteTask: TaskRemoteDataSource,
    private val taskDao: TaskV2Dao,
    private val projectDao: ProjectsV2Dao,
    private val connectionsV2Dao: ConnectionsV2Dao,
) : HiltBaseViewModel<ICeibroDataLoading.State>(), ICeibroDataLoading.ViewModel {
    init {
        sessionManager.setUser()
    }

    var apiSucceedCount = 0
    fun loadAppData(callBack: () -> Unit) {
        launch {
            when (val response = remoteTask.getAllTasks(TaskRootStateTags.ToMe.tagValue)) {
                is ApiResponse.Success -> {
                    taskDao.insertTaskData(
                        TasksV2DatabaseEntity(
                            rootState = TaskRootStateTags.ToMe.tagValue,
                            allTasks = response.data.allTasks
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

            when (val response = remoteTask.getAllTasks(TaskRootStateTags.FromMe.tagValue)) {
                is ApiResponse.Success -> {
                    taskDao.insertTaskData(
                        TasksV2DatabaseEntity(
                            rootState = TaskRootStateTags.FromMe.tagValue,
                            allTasks = response.data.allTasks
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

            when (val response = remoteTask.getAllTasks(TaskRootStateTags.Hidden.tagValue)) {
                is ApiResponse.Success -> {
                    taskDao.insertTaskData(
                        TasksV2DatabaseEntity(
                            rootState = TaskRootStateTags.Hidden.tagValue,
                            allTasks = response.data.allTasks
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

//            when (val response = remoteTask.getAllTopics()) {
//                is ApiResponse.Success -> {
//                    topicsV2Dao.insertTopicData(
//                        TopicsV2DatabaseEntity(
//                            0,
//                            topicsData = response.data
//                        )
//                    )
//                }
//                is ApiResponse.Error -> {
//                }
//            }

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
            when (val response = dashboardRepository.getAllConnectionsV2(
                sessionManager.getUserId()
            )) {
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
    }
}