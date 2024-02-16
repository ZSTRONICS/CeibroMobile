package com.zstronics.ceibro.ui.tasks.v2.taskdetailv2.fragments.detailparent

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.CeibroApplication
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.database.dao.DownloadedDrawingV2Dao
import com.zstronics.ceibro.data.database.dao.DrawingPinsV2Dao
import com.zstronics.ceibro.data.database.dao.GroupsV2Dao
import com.zstronics.ceibro.data.database.dao.InboxV2Dao
import com.zstronics.ceibro.data.database.dao.TaskV2Dao
import com.zstronics.ceibro.data.database.models.tasks.CeibroTaskV2
import com.zstronics.ceibro.data.database.models.tasks.TaskFiles
import com.zstronics.ceibro.data.remote.TaskRemoteDataSource
import com.zstronics.ceibro.data.repos.dashboard.IDashboardRepository
import com.zstronics.ceibro.data.repos.task.ITaskRepository
import com.zstronics.ceibro.data.repos.task.TaskRootStateTags
import com.zstronics.ceibro.data.repos.task.models.v2.TaskSeenResponse
import com.zstronics.ceibro.data.sessions.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TaskDetailParentV2VM @Inject constructor(
    override val viewState: TaskDetailParentV2State,
    val sessionManager: SessionManager,
    private val taskRepository: ITaskRepository,
    private val remoteTask: TaskRemoteDataSource,
    val dashboardRepository: IDashboardRepository,
    val taskDao: TaskV2Dao,
    val groupsV2Dao: GroupsV2Dao,
    private val inboxV2Dao: InboxV2Dao,
    val drawingPinsDao: DrawingPinsV2Dao,
    val downloadedDrawingV2Dao: DownloadedDrawingV2Dao
) : HiltBaseViewModel<ITaskDetailParentV2.State>(), ITaskDetailParentV2.ViewModel {
    val user = sessionManager.getUser().value

    val _taskDetail: MutableLiveData<CeibroTaskV2> = MutableLiveData()
    val taskDetail: LiveData<CeibroTaskV2> = _taskDetail
    val originalTask: MutableLiveData<CeibroTaskV2> = MutableLiveData()

    val _drawingFile: MutableLiveData<ArrayList<TaskFiles>> = MutableLiveData(arrayListOf())
    val drawingFile: MutableLiveData<ArrayList<TaskFiles>> = _drawingFile

    val _onlyImages: MutableLiveData<ArrayList<TaskFiles>> = MutableLiveData(arrayListOf())
    val onlyImages: MutableLiveData<ArrayList<TaskFiles>> = _onlyImages

    val _imagesWithComments: MutableLiveData<ArrayList<TaskFiles>> =
        MutableLiveData(arrayListOf())
    val imagesWithComments: MutableLiveData<ArrayList<TaskFiles>> = _imagesWithComments

    val _documents: MutableLiveData<ArrayList<TaskFiles>> = MutableLiveData(arrayListOf())
    val documents: MutableLiveData<ArrayList<TaskFiles>> = _documents

    var isTaskBeingDone: MutableLiveData<Boolean> = MutableLiveData(false)

    var rootState = ""
    var selectedState = ""
    var taskId: String = ""
    var descriptionExpanded: MutableLiveData<Boolean> = MutableLiveData(false)


    override fun onFirsTimeUiCreate(bundle: Bundle?) {
        super.onFirsTimeUiCreate(bundle)

        launch {
            val taskData: CeibroTaskV2? = CeibroApplication.CookiesManager.taskDataForDetails
            val taskDataFromNotification: CeibroTaskV2? =
                CeibroApplication.CookiesManager.taskDataForDetailsFromNotification
            val parentRootState = CeibroApplication.CookiesManager.taskDetailRootState
            val parentSelectedState = CeibroApplication.CookiesManager.taskDetailSelectedSubState
            if (parentRootState != null) {
                rootState = parentRootState
            }
            if (parentSelectedState != null) {
                selectedState = parentSelectedState
            }


            if (taskDataFromNotification != null) {         //It means detail is opened via notification if not null

                if (CeibroApplication.CookiesManager.jwtToken.isNullOrEmpty()) {
                    sessionManager.setUser()
                    sessionManager.setToken()
                }

                taskId = taskDataFromNotification.id
                isTaskBeingDone.postValue(taskDataFromNotification.isBeingDoneByAPI)
                rootState = TaskRootStateTags.ToMe.tagValue
                _taskDetail.postValue(taskDataFromNotification!!)
                originalTask.postValue(taskDataFromNotification!!)

                val seenByMe = taskDataFromNotification.seenBy.find { it1 -> it1 == user?.id }
                taskSeen(taskDataFromNotification.id) { }
            } else {
                taskData?.let { task ->
                    taskId = task.id
                    val isTaskBeingDone1 = taskDao.getTaskIsBeingDoneByAPI(task.id)
                    isTaskBeingDone.postValue(isTaskBeingDone1)
                    _taskDetail.postValue(task)
                    originalTask.postValue(task)


                    val seenByMe = task.seenBy.find { it == user?.id }
                    taskSeen(task.id) { }
                } ?: run {
                    alert("No details to display")
                }
            }

        }
    }

    fun taskSeen(
        taskId: String,
        onBack: (taskSeenData: TaskSeenResponse.TaskSeen) -> Unit,
    ) {
        launch {
            //loading(true)
            taskRepository.taskSeen(taskId) { isSuccess, taskSeenData ->
                if (isSuccess) {
                    if (taskSeenData != null) {
                        launch {
                            updateGenericTaskSeenInLocal(
                                taskSeenData,
                                taskDao,
                                user?.id,
                                sessionManager,
                                drawingPinsDao,
                                inboxV2Dao
                            )
                        }
                        onBack(taskSeenData)
                    }
                }

            }
        }
    }
}