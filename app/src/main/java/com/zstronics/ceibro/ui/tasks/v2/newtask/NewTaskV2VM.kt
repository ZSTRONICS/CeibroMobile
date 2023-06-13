package com.zstronics.ceibro.ui.tasks.v2.newtask

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.repos.dashboard.attachment.AttachmentModules
import com.zstronics.ceibro.data.repos.dashboard.attachment.AttachmentTags
import com.zstronics.ceibro.data.repos.dashboard.attachment.v2.AttachmentUploadV2Request
import com.zstronics.ceibro.data.repos.task.ITaskRepository
import com.zstronics.ceibro.data.repos.task.models.v2.NewTaskV2Request
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.ui.socket.LocalEvents
import dagger.hilt.android.lifecycle.HiltViewModel
import ee.zstronics.ceibro.camera.AttachmentTypes
import ee.zstronics.ceibro.camera.PickedImages
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

@HiltViewModel
class NewTaskV2VM @Inject constructor(
    override val viewState: NewTaskV2State,
    sessionManager: SessionManager,
    private val taskRepository: ITaskRepository,
) : HiltBaseViewModel<INewTaskV2.State>(), INewTaskV2.ViewModel {
    val user = sessionManager.getUser().value
    val listOfImages: MutableLiveData<ArrayList<PickedImages>> = MutableLiveData(arrayListOf())
    val onlyImages: MutableLiveData<ArrayList<PickedImages>> = MutableLiveData(arrayListOf())
    val imagesWithComments: MutableLiveData<ArrayList<PickedImages>> =
        MutableLiveData(arrayListOf())
    val documents: MutableLiveData<ArrayList<PickedImages>> = MutableLiveData(arrayListOf())

    var taskId = ""

    fun createNewTask(
        context: Context,
        doneImageRequired: Boolean,
        doneCommentsRequired: Boolean,
        onBack: () -> Unit,
    ) {

        if (viewState.taskTitle.value.toString() == "") {
            alert("Topic is required")
        } else if (viewState.assignToText.value.toString() == "") {
            alert("Assign to required")
        } else {
            val assignedToCeibroUsers =
                viewState.selectedContacts.value?.filter { it.isCeiborUser }
                    ?.map {
                        NewTaskV2Request.AssignedToState(
                            it.phoneNumber, it.userCeibroData?.id.toString()
                        )
                    } ?: listOf()
            val invitedNumbers = viewState.selectedContacts.value?.filter { !it.isCeiborUser }
                ?.map { it.phoneNumber } ?: listOf()
            val projectId = "648341057898edf39ae0b1e9"
//            val projectId = viewState.selectedProject.value?.id ?: ""
            val newTaskRequest = NewTaskV2Request(
                topic = viewState.selectedTopic.value?.id.toString(),
                project = projectId,
                assignedToState = assignedToCeibroUsers,
                dueDate = viewState.dueDate.value.toString(),
                creator = user?.id.toString(),
                description = viewState.description.value.toString(),
                invitedNumbers = invitedNumbers,
                doneImageRequired = doneImageRequired,
                doneCommentsRequired = doneCommentsRequired
            )

            launch {
                loading(true)
                taskRepository.newTaskV2(newTaskRequest) { isSuccess, taskId ->
                    if (isSuccess) {
                        val list = getCombinedList()
                        if (list.isNotEmpty()) {
                            uploadTaskFiles(context, list, taskId)
                        }
                        onBack()
                        loading(false, "")
                    } else {
                        loading(false, taskId)
                    }
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

    private fun uploadTaskFiles(
        context: Context,
        list: ArrayList<PickedImages>,
        taskId: String
    ) {

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

            AttachmentUploadV2Request.AttachmentMetaData(
                fileName = file.fileName,
                orignalFileName = file.fileName,
                tag = tag,
                comment = file.comment.trim()
            )
        }
        val metadataString = Gson().toJson(metaData)
        val metadataString2 = Gson().toJson(metadataString)     //again passing to make the json to convert into json string with slashes
        //"[{\"comment\":\"comments\",\"fileName\":\"IMG_20230614_114840.jpg\",\"orignalFileName\":\"IMG_20230614_114840.jpg\",\"tag\":\"image+comment\"}]"

        val request = AttachmentUploadV2Request(
            moduleId = taskId,
            moduleName = AttachmentModules.Task.name,
            files = attachmentUriList,
            metadata = metadataString2
        )
        println("RequestMetadataString22: $metadataString2")

        EventBus.getDefault()
            .post(LocalEvents.UploadFilesToV2Server(request))
    }
}