package com.zstronics.ceibro.ui.tasks.v2.taskdetail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.github.tntkhang.fullscreenimageview.library.FullScreenImageViewActivity
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.database.dao.TaskV2Dao
import com.zstronics.ceibro.data.database.models.tasks.CeibroTaskV2
import com.zstronics.ceibro.data.database.models.tasks.Events
import com.zstronics.ceibro.data.database.models.tasks.TaskFiles
import com.zstronics.ceibro.data.repos.dashboard.IDashboardRepository
import com.zstronics.ceibro.data.repos.dashboard.attachment.AttachmentTags
import com.zstronics.ceibro.data.repos.task.ITaskRepository
import com.zstronics.ceibro.data.repos.task.models.v2.EventCommentOnlyUploadV2Request
import com.zstronics.ceibro.data.repos.task.models.v2.TaskDetailEvents
import com.zstronics.ceibro.data.repos.task.models.v2.TaskSeenResponse
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.ui.attachment.imageExtensions
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TaskDetailV2VM @Inject constructor(
    override val viewState: TaskDetailV2State,
    val sessionManager: SessionManager,
    private val taskRepository: ITaskRepository,
    val dashboardRepository: IDashboardRepository,
    val taskDao: TaskV2Dao
) : HiltBaseViewModel<ITaskDetailV2.State>(), ITaskDetailV2.ViewModel {
    val user = sessionManager.getUser().value

    val _taskDetail: MutableLiveData<CeibroTaskV2> = MutableLiveData()
    val taskDetail: LiveData<CeibroTaskV2> = _taskDetail
    val originalTask: MutableLiveData<CeibroTaskV2> = MutableLiveData()

    private val _onlyImages: MutableLiveData<ArrayList<TaskFiles>> = MutableLiveData(arrayListOf())
    val onlyImages: MutableLiveData<ArrayList<TaskFiles>> = _onlyImages

    private val _imagesWithComments: MutableLiveData<ArrayList<TaskFiles>> =
        MutableLiveData(arrayListOf())
    val imagesWithComments: MutableLiveData<ArrayList<TaskFiles>> = _imagesWithComments

    private val _documents: MutableLiveData<ArrayList<TaskFiles>> = MutableLiveData(arrayListOf())
    val documents: MutableLiveData<ArrayList<TaskFiles>> = _documents

    private val _taskEvents: MutableLiveData<ArrayList<Events>> = MutableLiveData()
    val taskEvents: MutableLiveData<ArrayList<Events>> = _taskEvents

    var rootState = ""
    var selectedState = ""

//    init {
//        EventBus.getDefault().register(this)
//    }

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
        taskData.let {
            _taskDetail.postValue(it)
            originalTask.postValue(it)
        }
        taskData?.id?.let { it1 ->
            val seenByMe = taskData.seenBy.find { it == user?.id }
            if (seenByMe == null) {
                taskSeen(it1) { }
            }
        }
    }


    fun separateFiles(files: List<TaskFiles>) {
        val onlyImage: ArrayList<TaskFiles> = arrayListOf()
        val imagesWithComment: ArrayList<TaskFiles> = arrayListOf()
        val document: ArrayList<TaskFiles> = arrayListOf()

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
        _taskEvents.postValue(ArrayList(events))
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
                        updateGenericTaskSeenInLocal(taskSeenData, taskDao, user?.id)
                        onBack(taskSeenData)
                    }

                } else {
                    //loading(false, "")
                }
            }
        }
    }


    fun doneTask(
        taskId: String,
        onBack: (task: CeibroTaskV2?) -> Unit
    ) {
        launch {
            var isSuccess = false
            var taskData: CeibroTaskV2? = null

            val request = EventCommentOnlyUploadV2Request(
                message = ""
            )

            loading(true)
            when (val response = dashboardRepository.uploadEventWithoutFilesV2(
                event = TaskDetailEvents.DoneTask.eventValue,
                taskId = taskId,
                hasFiles = false,
                eventCommentOnlyUploadV2Request = request
            )) {
                is ApiResponse.Success -> {
                    val commentData = response.data.data
                    taskData = updateTaskDoneInLocal(commentData, taskDao, sessionManager)
                    isSuccess = true
                }

                is ApiResponse.Error -> {
                    loading(false, response.error.message)
                }
            }

            val handler = Handler()
            handler.postDelayed(Runnable {
                loading(false, "")
                if (isSuccess) {
                    onBack(taskData)
                }
            }, 50)
        }
    }





    fun isSameTask(newEvent: Events, taskId: String) = newEvent.taskId == taskId

//    override fun onCleared() {
//        super.onCleared()
//        EventBus.getDefault().unregister(this)
//    }


    fun openImageViewer(context: Context, fileUrl: ArrayList<String>?, position: Int) {
        if (!fileUrl.isNullOrEmpty()) {
            val fileExtension = fileUrl[0].substringAfterLast(".")
            if (imageExtensions.contains(".$fileExtension")) {
                val fullImageIntent = Intent(
                    context,
                    FullScreenImageViewActivity::class.java
                )

                val uriString: ArrayList<String> = arrayListOf()
                uriString.addAll(fileUrl)
                fullImageIntent.putExtra(FullScreenImageViewActivity.URI_LIST_DATA, uriString)

                fullImageIntent.putExtra(
                    FullScreenImageViewActivity.IMAGE_FULL_SCREEN_CURRENT_POS, position
                )
                context.startActivity(fullImageIntent)
            } else {
                // Handle other file types
            }
        }
    }
}