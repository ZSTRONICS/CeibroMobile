package com.zstronics.ceibro.ui.tasks.v2.newtask

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.database.dao.ConnectionsV2Dao
import com.zstronics.ceibro.data.database.dao.DraftNewTaskV2Dao
import com.zstronics.ceibro.data.database.dao.TaskV2Dao
import com.zstronics.ceibro.data.repos.dashboard.attachment.AttachmentModules
import com.zstronics.ceibro.data.repos.dashboard.attachment.AttachmentTags
import com.zstronics.ceibro.data.repos.dashboard.attachment.v2.AttachmentUploadV2Request
import com.zstronics.ceibro.data.repos.task.ITaskRepository
import com.zstronics.ceibro.data.repos.task.models.v2.LocalFilesToStore
import com.zstronics.ceibro.data.repos.task.models.v2.NewTaskToSave
import com.zstronics.ceibro.data.repos.task.models.v2.NewTaskV2Entity
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.ui.networkobserver.NetworkConnectivityObserver
import com.zstronics.ceibro.ui.socket.LocalEvents
import dagger.hilt.android.lifecycle.HiltViewModel
import ee.zstronics.ceibro.camera.AttachmentTypes
import ee.zstronics.ceibro.camera.PickedImages
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

@HiltViewModel
class NewTaskV2VM @Inject constructor(
    override val viewState: NewTaskV2State,
    private val sessionManager: SessionManager,
    private val taskRepository: ITaskRepository,
    private val taskDao: TaskV2Dao,
    private val connectionsV2Dao: ConnectionsV2Dao,
    private val draftNewTaskV2Dao: DraftNewTaskV2Dao,
    private var networkConnectivityObserver: NetworkConnectivityObserver
) : HiltBaseViewModel<INewTaskV2.State>(), INewTaskV2.ViewModel {
    val user = sessionManager.getUser().value
    val listOfImages: MutableLiveData<ArrayList<PickedImages>> = MutableLiveData(arrayListOf())
    val onlyImages: MutableLiveData<ArrayList<PickedImages>> = MutableLiveData(arrayListOf())
    val imagesWithComments: MutableLiveData<ArrayList<PickedImages>> =
        MutableLiveData(arrayListOf())
    val documents: MutableLiveData<ArrayList<PickedImages>> = MutableLiveData(arrayListOf())

    var taskId = ""

    override fun onFirsTimeUiCreate(bundle: Bundle?) {
        super.onFirsTimeUiCreate(bundle)
        val oldCreatedTask = sessionManager.getSavedNewTaskData()
        if (oldCreatedTask != null) {
            if (oldCreatedTask.topic != null) {
                viewState.selectedTopic.value = oldCreatedTask.topic
                viewState.taskTitle.value = oldCreatedTask.topic.topic
            }

            var assigneeMembers = ""
            if (oldCreatedTask.selfAssigned != null) {
                if (oldCreatedTask.selfAssigned) {
                    assigneeMembers += if (oldCreatedTask.selectedContacts.isNullOrEmpty()) {
                        "Me"
                    } else {
                        "Me; "
                    }
                }
                viewState.selfAssigned.value = oldCreatedTask.selfAssigned
            }
            if (!oldCreatedTask.selectedContacts.isNullOrEmpty()) {
                var index = 0
                val selectedContactList = oldCreatedTask.selectedContacts
                for (item in selectedContactList) {
                    assigneeMembers += if (index == selectedContactList.size - 1) {
                        "${item.contactFirstName} ${item.contactSurName}"
                    } else {
                        "${item.contactFirstName} ${item.contactSurName}; "
                    }
                    index++
                }
                viewState.selectedContacts.value = selectedContactList.toMutableList()
            }
            viewState.assignToText.value = assigneeMembers

            if (oldCreatedTask.project != null) {
                viewState.selectedProject.value = oldCreatedTask.project
                viewState.projectText.value = oldCreatedTask.project.title
            }

            if (!oldCreatedTask.dueDate.isNullOrEmpty()) {
                viewState.dueDate.value = oldCreatedTask.dueDate
            }
        }
    }


    fun createNewTask(
        doneImageRequired: Boolean,
        doneCommentsRequired: Boolean,
        activity: FragmentActivity,
        onBack: (String) -> Unit,
    ) {

        if (viewState.taskTitle.value.toString() == "") {
            alert("Topic is required")
        } else if (viewState.assignToText.value.toString() == "") {
            alert("Assignee is required")
        } else {
            launch {
                val selectedIds = viewState.selectedContacts.value?.map { it.id }
                val selectedContacts =
                    selectedIds?.let { connectionsV2Dao.getByIds(it) } ?: emptyList()

                val assignedToCeibroUsers =
                    selectedContacts.filter { it.isCeiborUser }
                        .map {
                            NewTaskV2Entity.AssignedToStateNewEntity(
                                it.phoneNumber, it.userCeibroData?.id.toString()
                            )
                        } as ArrayList<NewTaskV2Entity.AssignedToStateNewEntity>
                if (viewState.selfAssigned.value == true) {
                    if (user != null) {
                        assignedToCeibroUsers.add(
                            NewTaskV2Entity.AssignedToStateNewEntity(
                                user.phoneNumber, user.id
                            )
                        )
                    }
                }

                val invitedNumbers = selectedContacts.filter { !it.isCeiborUser }
                    .map { it.phoneNumber }
                val projectId = viewState.selectedProject.value?._id ?: ""
                val list = getCombinedList()

                val newTaskRequest = NewTaskV2Entity(
                    topic = viewState.selectedTopic.value?.id.toString(),
                    project = projectId,
                    assignedToState = assignedToCeibroUsers,
                    dueDate = viewState.dueDate.value.toString(),
                    creator = user?.id.toString(),
                    description = viewState.description.value.toString(),
                    doneImageRequired = doneImageRequired,
                    doneCommentsRequired = doneCommentsRequired,
                    invitedNumbers = invitedNumbers,
                    hasPendingFilesToUpload = list.isNotEmpty()
                )

                val newTaskToSave = NewTaskToSave(
                    topic = viewState.selectedTopic.value,
                    project = viewState.selectedProject.value,
                    selectedContacts = selectedContacts,
                    dueDate = viewState.dueDate.value,
                    selfAssigned = viewState.selfAssigned.value
                )
                sessionManager.saveNewTaskData(newTaskToSave)
                if (networkConnectivityObserver.isNetworkAvailable().not()) {
                    saveDataInLocal(newTaskRequest, list, onBack)
                    return@launch
                }

                if (list.isNotEmpty()) {
                    loading(true, "Creating task with files")
//                    createIndeterminateNotificationForFileUpload(
//                        activity = activity,
//                        channelId = "file_upload_channel",
//                        channelName = "File Upload Progress",
//                        notificationTitle = "Uploading Files"
//                    )


                    taskRequest = newTaskRequest
                    taskList = list
                    onBack.invoke("ServiceCall")
                    loading(false, "")

//                    taskRepository.newTaskV2WithFiles(
//                        newTaskRequest,
//                        list
//                    ) { isSuccess, task, errorMessage ->
//                        if (isSuccess) {
//                            updateCreatedTaskInLocal(task, taskDao, user?.id, sessionManager)
//                            val handler = Handler(Looper.getMainLooper())
//                            handler.postDelayed({
//                                onBack()
//                                loading(false, "")
//                                hideIndeterminateNotificationForFileUpload(activity)
//                            }, 50)
//                        } else {
//                            hideIndeterminateNotificationForFileUpload(activity)
//                            loading(false, errorMessage)
//                        }
//                    }
                } else {
                    loading(true)
                    taskRepository.newTaskV2WithoutFiles(newTaskRequest) { isSuccess, task, errorMessage ->
                        if (isSuccess) {
                            updateCreatedTaskInLocal(task, taskDao, sessionManager)
                            val handler = Handler(Looper.getMainLooper())
                            handler.postDelayed({
                                onBack.invoke("")
                                loading(false, "")
                            }, 50)
                        } else {
                            loading(false, errorMessage)
                        }
                    }
                }

            }
        }
    }

    private suspend fun saveDataInLocal(
        newTaskRequest: NewTaskV2Entity,
        list: java.util.ArrayList<PickedImages>,
        onBack: (String) -> Unit
    ) {
        val localFilesData = list.map {
            LocalFilesToStore(
                fileUri = it.fileUri.toString(),
                comment = it.comment,
                fileName = it.fileName,
                fileSizeReadAble = it.fileSizeReadAble,
                editingApplied = it.editingApplied,
                attachmentType = it.attachmentType
            )
        }
        newTaskRequest.apply {
            this.filesData = localFilesData
        }
        // Use the IO dispatcher for database operations
        withContext(Dispatchers.IO) {
            // Insert or replace the data into the database
            draftNewTaskV2Dao.upsert(newTaskRequest)
            // Invoke the callback on the appropriate thread (in this case, the main thread)
            withContext(Dispatchers.Main) {
                onBack.invoke("")
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
        val metadataString2 =
            Gson().toJson(metadataString)     //again passing to make the json to convert into json string with slashes

        val request = AttachmentUploadV2Request(
            moduleId = taskId,
            moduleName = AttachmentModules.Task.name,
            files = attachmentUriList,
            metadata = metadataString2
        )
        EventBus.getDefault()
            .post(LocalEvents.UploadFilesToV2Server(request))
    }

    companion object {
        var taskRequest: NewTaskV2Entity? = null
        var taskList: ArrayList<PickedImages>? = null
    }
}