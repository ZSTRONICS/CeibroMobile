package com.zstronics.ceibro.ui.tasks.v2.taskdetail.comment

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.cancelAndMakeToast
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.base.CookiesManager
import com.zstronics.ceibro.data.database.dao.TaskV2Dao
import com.zstronics.ceibro.data.database.models.tasks.CeibroTaskV2
import com.zstronics.ceibro.data.repos.NotificationTaskData
import com.zstronics.ceibro.data.repos.dashboard.IDashboardRepository
import com.zstronics.ceibro.data.repos.dashboard.attachment.AttachmentTags
import com.zstronics.ceibro.data.repos.task.models.v2.EventCommentOnlyUploadV2Request
import com.zstronics.ceibro.data.repos.task.models.v2.EventV2Response
import com.zstronics.ceibro.data.repos.task.models.v2.EventWithFileUploadV2Request
import com.zstronics.ceibro.data.repos.task.models.v2.TaskDetailEvents
import com.zstronics.ceibro.data.sessions.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import ee.zstronics.ceibro.camera.AttachmentTypes
import ee.zstronics.ceibro.camera.PickedImages
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommentVM @Inject constructor(
    override val viewState: CommentState,
    val sessionManager: SessionManager,
    val dashboardRepository: IDashboardRepository,
    private val taskDao: TaskV2Dao,
) : HiltBaseViewModel<IComment.State>(), IComment.ViewModel {
    val user = sessionManager.getUser().value
    val listOfImages: MutableLiveData<ArrayList<PickedImages>> = MutableLiveData(arrayListOf())
    val onlyImages: MutableLiveData<ArrayList<PickedImages>> = MutableLiveData(arrayListOf())
    val imagesWithComments: MutableLiveData<ArrayList<PickedImages>> =
        MutableLiveData(arrayListOf())
    val documents: MutableLiveData<ArrayList<PickedImages>> = MutableLiveData(arrayListOf())
    var notificationTaskData: MutableLiveData<NotificationTaskData?> = MutableLiveData()
    var taskId: String = ""
    var doneCommentsRequired: Boolean = false
    var doneImageRequired: Boolean = false
    var actionToPerform: MutableLiveData<String> = MutableLiveData("")

    override fun onFirsTimeUiCreate(bundle: Bundle?) {
        super.onFirsTimeUiCreate(bundle)

        val doneComment = bundle?.getBoolean("doneCommentsRequired")
        val doneImage = bundle?.getBoolean("doneImageRequired")
        val tasksId = bundle?.getString("taskId")
        val action = bundle?.getString("action")
        if (tasksId != null) {
            taskId = tasksId
        }
        doneComment?.let { doneCommentsRequired = it }
        doneImage?.let { doneImageRequired = it }
        action?.let { actionToPerform.value = it }


        val taskData2: NotificationTaskData? = bundle?.getParcelable("notificationTaskData")
        if (taskData2 != null) {
            if (CookiesManager.jwtToken.isNullOrEmpty()) {
                sessionManager.setUser()
                sessionManager.isUserLoggedIn()
            }
            println("TaskData:comment $taskData2")
            actionToPerform.value = TaskDetailEvents.Comment.eventValue
            notificationTaskData.postValue(taskData2)
            taskId = taskData2.taskId
        }
    }

    fun uploadComment(
        context: Context,
        onBack: (eventData: EventV2Response.Data?) -> Unit
    ) {
        val list = getCombinedList()
        if (viewState.comment.value.toString() == "" && list.isEmpty()) {
            alert("Please add comment or files")
        } else {
            GlobalScope.launch {
                var eventData: EventV2Response.Data? = null
                var isSuccess = false

                if (list.isNotEmpty()) {
                    val attachmentUriList = list.map {
                        it.file
                    }
                    val metaData = list.map { file ->
                        var tag = ""
                        if (file.attachmentType == AttachmentTypes.Image) {
                            tag = if (file.comment.isNotEmpty()) {
                                AttachmentTags.ImageWithComment.tagValue
                            } else {
                                AttachmentTags.Image.tagValue
                            }
                        } else if (file.attachmentType == AttachmentTypes.Pdf || file.attachmentType == AttachmentTypes.Doc) {
                            tag = AttachmentTags.File.tagValue
                        }

                        EventWithFileUploadV2Request.AttachmentMetaData(
                            fileName = file.fileName,
                            orignalFileName = file.fileName,
                            tag = tag,
                            comment = file.comment.trim()
                        )
                    }
                    val metadataString = Gson().toJson(metaData)
                    val metadataString2 =
                        Gson().toJson(metadataString)     //again passing to make the json to convert into json string with slashes

                    val request = EventWithFileUploadV2Request(
                        files = attachmentUriList,
                        message = viewState.comment.value.toString(),
                        metadata = metadataString2
                    )

                    loading(true)
                    when (val response = dashboardRepository.uploadEventWithFilesV2(
                        event = TaskDetailEvents.Comment.eventValue,
                        taskId = taskId ?: "",
                        hasFiles = true,
                        eventWithFileUploadV2Request = request
                    )) {
                        is ApiResponse.Success -> {
                            val commentData = response.data.data
                            isSuccess = true
                            eventData = commentData
                        }

                        is ApiResponse.Error -> {
                            cancelAndMakeToast(context, response.error.message, Toast.LENGTH_SHORT)
                        }
                    }

                } else {        //if list is empty, moving to else part
                    val request = EventCommentOnlyUploadV2Request(
                        message = viewState.comment.value.toString()
                    )

                    loading(true)
                    when (val response = dashboardRepository.uploadEventWithoutFilesV2(
                        event = TaskDetailEvents.Comment.eventValue,
                        taskId = taskId ?: "",
                        hasFiles = false,
                        eventCommentOnlyUploadV2Request = request
                    )) {
                        is ApiResponse.Success -> {
                            val commentData = response.data.data
                            isSuccess = true
                            eventData = commentData
                        }

                        is ApiResponse.Error -> {
                            cancelAndMakeToast(context, response.error.message, Toast.LENGTH_SHORT)
                        }
                    }
                }
                updateTaskCommentInLocal(eventData, taskDao, user?.id, sessionManager)

                Handler(Looper.getMainLooper()).postDelayed({
                    loading(false, "")
                    if (isSuccess) {
                        onBack(eventData)
                    }
                },10)

            }
        }
    }

    fun doneTask(
        context: Context,
        onBack: (eventData: EventV2Response.Data?) -> Unit
    ) {
        val list = getCombinedList()
        val imageList = getCombinedImagesList()
        if (doneCommentsRequired && viewState.comment.value.toString() == "") {
            alert(context.resources.getString(R.string.comment_is_required_to_mark_task_as_done))
        } else if (doneImageRequired && imageList.isEmpty()) {
            alert(context.resources.getString(R.string.image_is_required_to_mark_task_as_done))
        } else {
            launch {
                var eventData: EventV2Response.Data? = null
                var isSuccess = false

                if (list.isNotEmpty()) {
                    val attachmentUriList = list.map {
                        it.file
                    }
                    val metaData = list.map { file ->
                        var tag = ""
                        if (file.attachmentType == AttachmentTypes.Image) {
                            tag = if (file.comment.isNotEmpty()) {
                                AttachmentTags.ImageWithComment.tagValue
                            } else {
                                AttachmentTags.Image.tagValue
                            }
                        } else if (file.attachmentType == AttachmentTypes.Pdf || file.attachmentType == AttachmentTypes.Doc) {
                            tag = AttachmentTags.File.tagValue
                        }

                        EventWithFileUploadV2Request.AttachmentMetaData(
                            fileName = file.fileName,
                            orignalFileName = file.fileName,
                            tag = tag,
                            comment = file.comment.trim()
                        )
                    }
                    val metadataString = Gson().toJson(metaData)
                    val metadataString2 =
                        Gson().toJson(metadataString)     //again passing to make the json to convert into json string with slashes

                    val request = EventWithFileUploadV2Request(
                        files = attachmentUriList,
                        message = viewState.comment.value.toString(),
                        metadata = metadataString2
                    )

                    loading(true)
                    when (val response = dashboardRepository.uploadEventWithFilesV2(
                        event = TaskDetailEvents.DoneTask.eventValue,
                        taskId = taskId ?: "",
                        hasFiles = true,
                        eventWithFileUploadV2Request = request
                    )) {
                        is ApiResponse.Success -> {
                            val commentData = response.data.data
                            isSuccess = true
                            eventData = commentData
                        }

                        is ApiResponse.Error -> {
                            cancelAndMakeToast(context, response.error.message, Toast.LENGTH_SHORT)
                        }
                    }

                } else {        //if list is empty, moving to else part
                    val request = EventCommentOnlyUploadV2Request(
                        message = viewState.comment.value.toString()
                    )

                    loading(true)
                    when (val response = dashboardRepository.uploadEventWithoutFilesV2(
                        event = TaskDetailEvents.DoneTask.eventValue,
                        taskId = taskId ?: "",
                        hasFiles = false,
                        eventCommentOnlyUploadV2Request = request
                    )) {
                        is ApiResponse.Success -> {
                            val commentData = response.data.data
                            isSuccess = true
                            eventData = commentData
                        }

                        is ApiResponse.Error -> {
                            cancelAndMakeToast(context, response.error.message, Toast.LENGTH_SHORT)
                        }
                    }
                }

                updateTaskDoneInLocal(eventData, taskDao, sessionManager)

                loading(false, "")
                if (isSuccess) {
                    onBack(eventData)
                }
            }
        }
    }


    private fun getCombinedList(): ArrayList<PickedImages> {
        val listOfImages = listOfImages.value
        val documents = documents.value
        val combinedList = arrayListOf<PickedImages>()
        if (listOfImages != null) {
            combinedList.addAll(listOfImages)
        }
        if (documents != null) {
            combinedList.addAll(documents)
        }
        return combinedList
    }

    private fun getCombinedImagesList(): ArrayList<PickedImages> {
        val listOfImages = listOfImages.value
//        val documents = documents.value
        val combinedList = arrayListOf<PickedImages>()
        if (listOfImages != null) {
            combinedList.addAll(listOfImages)
        }
//        if (documents != null) {
//            combinedList.addAll(documents)
//        }
        return combinedList
    }

}