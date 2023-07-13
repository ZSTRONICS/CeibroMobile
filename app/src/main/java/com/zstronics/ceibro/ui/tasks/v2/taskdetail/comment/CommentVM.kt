package com.zstronics.ceibro.ui.tasks.v2.taskdetail.comment

import android.content.Context
import android.os.Bundle
import android.os.Handler
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.database.dao.TaskV2Dao
import com.zstronics.ceibro.data.database.models.tasks.CeibroTaskV2
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
    var taskData: CeibroTaskV2? = null

    override fun onFirsTimeUiCreate(bundle: Bundle?) {
        super.onFirsTimeUiCreate(bundle)

        val bundleTaskData: CeibroTaskV2? = bundle?.getParcelable("taskData")
        if (bundleTaskData != null) {
            taskData = bundleTaskData
        }
    }

    fun uploadComment(
        context: Context,
        onBack: () -> Unit
    ) {
        val list = getCombinedList()
        if (viewState.comment.value.toString() == "" && list.isEmpty()) {
            alert("Please add comment or files")
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
                        event = TaskDetailEvents.Comment.eventValue,
                        taskId = taskData?.id ?: "",
                        hasFiles = true,
                        eventWithFileUploadV2Request = request
                    )) {
                        is ApiResponse.Success -> {
                            val commentData = response.data.data
                            eventData = commentData
                            isSuccess = true
                        }

                        is ApiResponse.Error -> {
                            loading(false, response.error.message)
                        }
                    }

                } else {        //if list is empty, moving to else part
                    val request = EventCommentOnlyUploadV2Request(
                        message = viewState.comment.value.toString()
                    )

                    loading(true)
                    when (val response = dashboardRepository.uploadEventWithoutFilesV2(
                        event = TaskDetailEvents.Comment.eventValue,
                        taskId = taskData?.id ?: "",
                        hasFiles = false,
                        eventCommentOnlyUploadV2Request = request
                    )) {
                        is ApiResponse.Success -> {
                            val commentData = response.data.data
                            eventData = commentData
                            isSuccess = true
                        }

                        is ApiResponse.Error -> {
                            loading(false, response.error.message)
                        }
                    }
                }

                taskData = updateTaskCommentInLocal(eventData, taskDao, user?.id)

                val handler = Handler()
                handler.postDelayed(Runnable {
                    loading(false, "")
                    if (isSuccess) {
                        onBack()
                    }
                }, 50)

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

}