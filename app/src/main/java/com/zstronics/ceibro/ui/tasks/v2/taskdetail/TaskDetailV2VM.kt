package com.zstronics.ceibro.ui.tasks.v2.taskdetail

import android.os.Bundle
import android.os.Handler
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.database.dao.TaskV2Dao
import com.zstronics.ceibro.data.database.models.tasks.CeibroTaskV2
import com.zstronics.ceibro.data.database.models.tasks.Events
import com.zstronics.ceibro.data.database.models.tasks.Files
import com.zstronics.ceibro.data.repos.dashboard.attachment.AttachmentTags
import com.zstronics.ceibro.data.repos.task.ITaskRepository
import com.zstronics.ceibro.data.repos.task.TaskRootStateTags
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

    private val _imagesWithComments: MutableLiveData<ArrayList<Files>> =
        MutableLiveData(arrayListOf())
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
        var taskToMe = false
        var taskFromMe = false
        if (task != null) {
            if (task.creator.id == user?.id) {
                taskFromMe = true
            }
            for (item in task.assignedToState) {
                if (item.userId == user?.id) {
                    taskToMe = true
                }
            }

            launch {
                if (taskFromMe) {
                    val taskFromMeLocalData = taskDao.getTasks(TaskRootStateTags.FromMe.tagValue)

                    if (taskFromMeLocalData != null) {
                        val newTask = taskFromMeLocalData.allTasks.new.find { it.id == task.id }
                        val unreadTask = taskFromMeLocalData.allTasks.unread.find { it.id == task.id }
                        val ongoingTask = taskFromMeLocalData.allTasks.ongoing.find { it.id == task.id }
                        val doneTask = taskFromMeLocalData.allTasks.done.find { it.id == task.id }
                        if (newTask != null) {
                            val allTaskList = taskFromMeLocalData.allTasks.new.toMutableList()
                            val taskIndex = allTaskList.indexOf(newTask)

                            allTaskList[taskIndex] = task
                            taskFromMeLocalData.allTasks.new = allTaskList.toList()

                        } else if (unreadTask != null) {
                            val allTaskList = taskFromMeLocalData.allTasks.unread.toMutableList()
                            val taskIndex = allTaskList.indexOf(unreadTask)

                            allTaskList[taskIndex] = task
                            taskFromMeLocalData.allTasks.unread = allTaskList.toList()

                        } else if (ongoingTask != null) {
                            val allTaskList = taskFromMeLocalData.allTasks.ongoing.toMutableList()
                            val taskIndex = allTaskList.indexOf(ongoingTask)

                            allTaskList[taskIndex] = task
                            taskFromMeLocalData.allTasks.ongoing = allTaskList.toList()

                        } else if (doneTask != null) {
                            val allTaskList = taskFromMeLocalData.allTasks.done.toMutableList()
                            val taskIndex = allTaskList.indexOf(doneTask)

                            allTaskList[taskIndex] = task
                            taskFromMeLocalData.allTasks.done = allTaskList.toList()
                        }

                        taskDao.insertTaskData(
                            taskFromMeLocalData
                        )
                    }
                }

                if (taskToMe) {
                    val taskToMeLocalData = taskDao.getTasks(TaskRootStateTags.ToMe.tagValue)

                    if (taskToMeLocalData != null) {
                        val newTask = taskToMeLocalData.allTasks.new.find { it.id == task.id }
                        val unreadTask = taskToMeLocalData.allTasks.unread.find { it.id == task.id }
                        val ongoingTask = taskToMeLocalData.allTasks.ongoing.find { it.id == task.id }
                        val doneTask = taskToMeLocalData.allTasks.done.find { it.id == task.id }
                        if (newTask != null) {
                            val allTaskList = taskToMeLocalData.allTasks.new.toMutableList()
                            val taskIndex = allTaskList.indexOf(newTask)

                            allTaskList[taskIndex] = task
                            taskToMeLocalData.allTasks.new = allTaskList.toList()

                        } else if (unreadTask != null) {
                            val allTaskList = taskToMeLocalData.allTasks.unread.toMutableList()
                            val taskIndex = allTaskList.indexOf(unreadTask)

                            allTaskList[taskIndex] = task
                            taskToMeLocalData.allTasks.unread = allTaskList.toList()

                        } else if (ongoingTask != null) {
                            val allTaskList = taskToMeLocalData.allTasks.ongoing.toMutableList()
                            val taskIndex = allTaskList.indexOf(ongoingTask)

                            allTaskList[taskIndex] = task
                            taskToMeLocalData.allTasks.ongoing = allTaskList.toList()

                        } else if (doneTask != null) {
                            val allTaskList = taskToMeLocalData.allTasks.done.toMutableList()
                            val taskIndex = allTaskList.indexOf(doneTask)

                            allTaskList[taskIndex] = task
                            taskToMeLocalData.allTasks.done = allTaskList.toList()
                        }

                        taskDao.insertTaskData(
                            taskToMeLocalData
                        )
                    }
                }
            }

        }
    }

}