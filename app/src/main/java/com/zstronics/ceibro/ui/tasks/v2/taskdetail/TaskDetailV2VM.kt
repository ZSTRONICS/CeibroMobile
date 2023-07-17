package com.zstronics.ceibro.ui.tasks.v2.taskdetail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.github.tntkhang.fullscreenimageview.library.FullScreenImageViewActivity
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.database.dao.TaskV2Dao
import com.zstronics.ceibro.data.database.models.tasks.CeibroTaskV2
import com.zstronics.ceibro.data.database.models.tasks.Events
import com.zstronics.ceibro.data.database.models.tasks.TaskFiles
import com.zstronics.ceibro.data.repos.dashboard.attachment.AttachmentTags
import com.zstronics.ceibro.data.repos.task.ITaskRepository
import com.zstronics.ceibro.data.repos.task.models.v2.ForwardTaskV2Request
import com.zstronics.ceibro.data.repos.task.models.v2.TaskSeenResponse
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.ui.attachment.imageExtensions
import com.zstronics.ceibro.ui.socket.LocalEvents
import dagger.hilt.android.lifecycle.HiltViewModel
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject

@HiltViewModel
class TaskDetailV2VM @Inject constructor(
    override val viewState: TaskDetailV2State,
    private val sessionManager: SessionManager,
    private val taskRepository: ITaskRepository,
    private val taskDao: TaskV2Dao
) : HiltBaseViewModel<ITaskDetailV2.State>(), ITaskDetailV2.ViewModel {
    val user = sessionManager.getUser().value

    val _taskDetail: MutableLiveData<CeibroTaskV2> = MutableLiveData()
    val taskDetail: LiveData<CeibroTaskV2> = _taskDetail

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

    init {
        EventBus.getDefault().register(this)
    }

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


    private fun taskSeen(
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
//                        val task = taskDetail.value
//                        val userSeen = task?.assignedToState?.find { it.userId == taskSeenData.state.userId }
//                        if (userSeen != null) {
//                            val index = task.assignedToState.indexOf(userSeen)
//                            task.assignedToState[index] = taskSeenData.state
//                        }
//                        task?.creatorState = taskSeenData.creatorState
//                        task?.seenBy = taskSeenData.seenBy
//                        task.let { _taskDetail.postValue(it) }
                    }
                    //loading(false, "")

                } else {
                    //loading(false, "")
                }
            }
        }
    }


    fun forwardTask(
        taskId: String,
        forwardTaskV2Request: ForwardTaskV2Request,
        onBack: (task: CeibroTaskV2) -> Unit,
    ) {
        launch {
            loading(true)
            taskRepository.forwardTask(taskId, forwardTaskV2Request) { isSuccess, task, errorMsg ->
                if (isSuccess) {

                    if (task != null) {
                        _taskDetail.postValue(task)
                        onBack(task)
                    }
                    loading(false, "")
                    updateForwardTaskInLocal(task, taskDao, user?.id)
                } else {
                        loading(false, errorMsg)
                }
            }
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTaskForwardEvent(event: LocalEvents.TaskForwardEvent?) {
        val task = event?.task
        if (task != null) {
            taskDetail.value?.let { taskDetail ->
                if (task.id == taskDetail.id) {
                    task.let {
                        _taskDetail.postValue(it)
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        EventBus.getDefault().unregister(this)
    }


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