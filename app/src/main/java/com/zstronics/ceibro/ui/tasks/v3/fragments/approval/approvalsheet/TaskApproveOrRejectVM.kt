package com.zstronics.ceibro.ui.tasks.v3.fragments.approval.approvalsheet

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.cancelAndMakeToast
import com.zstronics.ceibro.base.viewmodel.Dispatcher
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
import com.zstronics.ceibro.ui.tasks.v2.newtask.CreateNewTaskService
import com.zstronics.ceibro.ui.tasks.v2.taskdetail.comment.CommentVM
import dagger.hilt.android.lifecycle.HiltViewModel
import ee.zstronics.ceibro.camera.AttachmentTypes
import ee.zstronics.ceibro.camera.PickedImages
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskApproveOrRejectVM @Inject constructor(
    override val viewState: TaskApproveOrRejectState,
    val sessionManager: SessionManager,
    val dashboardRepository: IDashboardRepository,
    private val taskDao: TaskV2Dao,
) : HiltBaseViewModel<ITaskApproveOrReject.State>(), ITaskApproveOrReject.ViewModel {
    val user = sessionManager.getUser().value

    val listOfImages: MutableLiveData<ArrayList<PickedImages>> = MutableLiveData(arrayListOf())
    val onlyImages: MutableLiveData<ArrayList<PickedImages>> = MutableLiveData(arrayListOf())
    val imagesWithComments: MutableLiveData<ArrayList<PickedImages>> =
        MutableLiveData(arrayListOf())
    val documents: MutableLiveData<ArrayList<PickedImages>> = MutableLiveData(arrayListOf())


    private val _taskType: MutableLiveData<String> = MutableLiveData()
    val taskType: LiveData<String> = _taskType


    private val _task: MutableLiveData<CeibroTaskV2> = MutableLiveData()
    val task: LiveData<CeibroTaskV2> = _task


    override fun onFirsTimeUiCreate(bundle: Bundle?) {
        super.onFirsTimeUiCreate(bundle)
        val taskType = bundle?.getString("updateTaskType")
        val task: CeibroTaskV2? = bundle?.getParcelable("CeibroTaskV2")
        task?.let {
            this._task.value = it
        }
        taskType?.let {
            this._taskType.value = it
        }
    }

    fun filesCounter(): Int {
        return ((listOfImages.value?.size ?: 0) + (documents.value?.size ?: 0))
    }

    fun approveOrRejectTask(
        context: Context,
        onBack: (eventData: EventV2Response.Data?) -> Unit
    ) {
        val list = getCombinedList()
        if (viewState.comment.value.toString().trim() == "" && list.isEmpty()) {
            alert(context.getString(R.string.please_add_comment_or_files))
        } else {
            GlobalScope.launch {
                var eventData: EventV2Response.Data? = null
                var isSuccess = false

                if (list.isNotEmpty()) {
                    loading(true)
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

                    eventData = null

//                    CommentVM.eventWithFileUploadV2RequestData = request
//                    val serviceIntent = Intent(context, CreateNewTaskService::class.java)
//                    serviceIntent.putExtra("ServiceRequest", "commentRequest")
//                    serviceIntent.putExtra("taskId", taskId)
//                    serviceIntent.putExtra("event", TaskDetailEvents.Comment.eventValue)
//                    context.startService(serviceIntent)
//
//                    loading(false, "")
//                    launch(Dispatcher.Main) {
//                        onBack.invoke(eventData)
//                    }


//                    when (val response = dashboardRepository.uploadEventWithFilesV2(
//                        event = TaskDetailEvents.Comment.eventValue,
//                        taskId = taskId ?: "",
//                        hasFiles = true,
//                        eventWithFileUploadV2Request = request
//                    )) {
//                        is ApiResponse.Success -> {
//                            val commentData = response.data.data
//                            isSuccess = true
//                            eventData = commentData
//                        }
//
//                        is ApiResponse.Error -> {
//                            cancelAndMakeToast(context, response.error.message, Toast.LENGTH_SHORT)
//                        }
//                    }

                } else {        //if list is empty, moving to else part
                    val request = EventCommentOnlyUploadV2Request(
                        message = viewState.comment.value.toString()
                    )

                    loading(true)
//                    when (val response = dashboardRepository.uploadEventWithoutFilesV2(
//                        event = TaskDetailEvents.Comment.eventValue,
//                        taskId = taskId ?: "",
//                        hasFiles = false,
//                        eventCommentOnlyUploadV2Request = request
//                    )) {
//                        is ApiResponse.Success -> {
//                            val commentData = response.data.data
//                            isSuccess = true
//                            eventData = commentData
//                        }
//
//                        is ApiResponse.Error -> {
//                            launch(Dispatcher.Main) {
//                                cancelAndMakeToast(
//                                    context,
//                                    response.error.message,
//                                    Toast.LENGTH_SHORT
//                                )
//                            }
//                        }
//                    }
                }
//                updateTaskCommentInLocal(
//                    eventData,
//                    taskDao,
//                    inboxV2Dao,
//                    user?.id,
//                    sessionManager,
//                    drawingPinsDao
//                )
//
//                Handler(Looper.getMainLooper()).postDelayed({
//                    loading(false, "")
//                    if (isSuccess) {
//                        onBack(eventData)
//                    }
//                }, 10)

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