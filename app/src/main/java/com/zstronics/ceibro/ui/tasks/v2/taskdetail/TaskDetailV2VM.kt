package com.zstronics.ceibro.ui.tasks.v2.taskdetail

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.database.dao.TaskV2Dao
import com.zstronics.ceibro.data.database.models.tasks.CeibroTaskV2
import com.zstronics.ceibro.data.database.models.tasks.Events
import com.zstronics.ceibro.data.database.models.tasks.Files
import com.zstronics.ceibro.data.repos.dashboard.attachment.AttachmentTags
import com.zstronics.ceibro.data.repos.task.ITaskRepository
import com.zstronics.ceibro.data.repos.task.models.TaskV2Response
import com.zstronics.ceibro.data.repos.task.models.TasksV2DatabaseEntity
import com.zstronics.ceibro.data.repos.task.models.v2.ForwardTaskV2Request
import com.zstronics.ceibro.data.sessions.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TaskDetailV2VM @Inject constructor(
    override val viewState: TaskDetailV2State,
    private val sessionManager: SessionManager,
    private val taskRepository: ITaskRepository,
    private val taskDao: TaskV2Dao
) : HiltBaseViewModel<ITaskDetailV2.State>(), ITaskDetailV2.ViewModel {
    val user = sessionManager.getUser().value

    private val _taskDetail: MutableLiveData<CeibroTaskV2> = MutableLiveData()
    val taskDetail: LiveData<CeibroTaskV2> = _taskDetail

    private val _onlyImages: MutableLiveData<ArrayList<Files>> = MutableLiveData(arrayListOf())
    val onlyImages: MutableLiveData<ArrayList<Files>> = _onlyImages

    private val _imagesWithComments: MutableLiveData<ArrayList<Files>> = MutableLiveData(arrayListOf())
    val imagesWithComments: MutableLiveData<ArrayList<Files>> = _imagesWithComments

    private val _documents: MutableLiveData<ArrayList<Files>> = MutableLiveData(arrayListOf())
    val documents: MutableLiveData<ArrayList<Files>> = _documents

    private val _taskEvents: MutableLiveData<ArrayList<Events>> = MutableLiveData()
    val taskEvents: MutableLiveData<ArrayList<Events>> = _taskEvents

    var rootState = ""
    var selectedState = ""

    override fun onFirsTimeUiCreate(bundle: Bundle?) {
        super.onFirsTimeUiCreate(bundle)

        val taskData: CeibroTaskV2? = bundle?.getParcelable("taskDetail")
        val parentRootState = bundle?.getString("rootState")
        val parentSelectedState = bundle?.getString("selectedState")
        if (parentRootState != null) {
            rootState = parentRootState
        }
        if (parentSelectedState != null) {
            selectedState = parentSelectedState
        }
        taskData.let { _taskDetail.postValue(it) }

    }


    fun separateFiles(files: List<Files>) {
        val onlyImage: ArrayList<Files> = arrayListOf()
        val imagesWithComment: ArrayList<Files> = arrayListOf()
        val document: ArrayList<Files> = arrayListOf()

        for (item in files) {
            when (item.fileTag) {
                AttachmentTags.Image.tagValue -> {
                    onlyImage.add(item)
                }
                AttachmentTags.ImageWithComment.tagValue -> {
                    imagesWithComment.add(item)
                }
                AttachmentTags.File.tagValue -> {
                    document.add(item)
                }
            }
        }

        _onlyImages.postValue(onlyImage)
        _imagesWithComments.postValue(imagesWithComment)
        _documents.postValue(document)
    }

    fun handleEvents(events: List<Events>) {
        _taskEvents.postValue(events as ArrayList<Events>)
    }


    fun forwardTask(
        taskId: String,
        forwardTaskV2Request: ForwardTaskV2Request,
        onBack: (task: CeibroTaskV2) -> Unit,
    ) {
        launch {
            loading(true)
            taskRepository.forwardTask(taskId, forwardTaskV2Request) { isSuccess, task ->
                if (isSuccess) {
//                    val list = getCombinedList()
//                    if (list.isNotEmpty()) {
//                        uploadTaskFiles(context, list, taskId)
//                    }
                    if (task != null) {
                        _taskDetail.postValue(task)
                        onBack(task)
                    }
                    loading(false, "")
                    insertUpdatedTask(task)
                } else {
                    loading(false, "")
                }
            }
        }
    }

    private fun insertUpdatedTask(task: CeibroTaskV2?) {
        launch {
            val taskLocalData = taskDao.getTasks(rootState)

            if (taskLocalData != null) {
                if (selectedState.equals("new", true)) {

                    val index = taskLocalData.allTasks.new.indexOfFirst { it.id == task?.id } ?: -1
                    if (index >= 0) {
                        val updatedList = taskLocalData.allTasks.new.toMutableList()
                        if (task != null) {
                            updatedList[index] = task
                        }
                        taskLocalData.allTasks.new = updatedList.toList()
                    }

                } else if (selectedState.equals("unread", true)) {

                    val index = taskLocalData.allTasks.unread.indexOfFirst { it.id == task?.id } ?: -1
                    if (index >= 0) {
                        val updatedList = taskLocalData.allTasks.unread.toMutableList()
                        if (task != null) {
                            updatedList[index] = task
                        }
                        taskLocalData.allTasks.unread = updatedList.toList()
                    }

                } else if (selectedState.equals("ongoing", true)) {

                    val index = taskLocalData.allTasks.ongoing.indexOfFirst { it.id == task?.id } ?: -1
                    if (index >= 0) {
                        val updatedList = taskLocalData.allTasks.ongoing.toMutableList()
                        if (task != null) {
                            updatedList[index] = task
                        }
                        taskLocalData.allTasks.ongoing = updatedList.toList()
                    }

                } else if (selectedState.equals("done", true)) {

                    val index = taskLocalData.allTasks.done.indexOfFirst { it.id == task?.id } ?: -1
                    if (index >= 0) {
                        val updatedList = taskLocalData.allTasks.done.toMutableList()
                        if (task != null) {
                            updatedList[index] = task
                        }
                        taskLocalData.allTasks.done = updatedList.toList()
                    }

                } else {
                    if (rootState == "from-me") {
                        val index = taskLocalData.allTasks.unread.indexOfFirst { it.id == task?.id } ?: -1
                        if (index >= 0) {
                            val updatedList = taskLocalData.allTasks.unread.toMutableList()
                            if (task != null) {
                                updatedList[index] = task
                            }
                            taskLocalData.allTasks.unread = updatedList.toList()
                        }
                    } else {
                        //this will be for rootState "to-me" and for to-me we'll insert it in new
                        val index = taskLocalData.allTasks.new.indexOfFirst { it.id == task?.id } ?: -1
                        if (index >= 0) {
                            val updatedList = taskLocalData.allTasks.new.toMutableList()
                            if (task != null) {
                                updatedList[index] = task
                            }
                            taskLocalData.allTasks.new = updatedList.toList()
                        }
                    }
                }

                taskDao.insertTaskData(
                    taskLocalData
                )
            }

        }
    }

}