package com.zstronics.ceibro.ui.tasks.v2.newtask

import android.content.Context
import android.os.Handler
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.database.dao.TaskV2Dao
import com.zstronics.ceibro.data.database.models.tasks.CeibroTaskV2
import com.zstronics.ceibro.data.repos.dashboard.attachment.AttachmentModules
import com.zstronics.ceibro.data.repos.dashboard.attachment.AttachmentTags
import com.zstronics.ceibro.data.repos.dashboard.attachment.v2.AttachmentUploadV2Request
import com.zstronics.ceibro.data.repos.task.ITaskRepository
import com.zstronics.ceibro.data.repos.task.TaskRootStateTags
import com.zstronics.ceibro.data.repos.task.models.TaskV2Response
import com.zstronics.ceibro.data.repos.task.models.TasksV2DatabaseEntity
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
    private val taskDao: TaskV2Dao
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
            alert("Assignee is required")
        } else {
            val assignedToCeibroUsers =
                (viewState.selectedContacts.value?.filter { it.isCeiborUser }
                    ?.map {
                        NewTaskV2Request.AssignedToStateNewRequest(
                            it.phoneNumber, it.userCeibroData?.id.toString()
                        )
                    } ?: listOf()) as ArrayList<NewTaskV2Request.AssignedToStateNewRequest>
            if (viewState.selfAssigned.value == true) {
                if (user != null) {
                    assignedToCeibroUsers.add(
                        NewTaskV2Request.AssignedToStateNewRequest(
                            user.phoneNumber, user.id
                        )
                    )
                }
            }
            
            val invitedNumbers = viewState.selectedContacts.value?.filter { !it.isCeiborUser }
                ?.map { it.phoneNumber } ?: listOf()
            val projectId = viewState.selectedProject.value?.id ?: ""
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
                taskRepository.newTaskV2(newTaskRequest) { isSuccess, task ->
                    if (isSuccess) {
                        if (task != null) {
                            var taskToMe = false
                            var taskFromMe = false
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
                                    val taskLocalData = taskDao.getTasks(TaskRootStateTags.FromMe.tagValue)

                                    if (taskLocalData != null) {
                                        val index = taskLocalData.allTasks.unread.indexOfFirst { it.id == task.id } ?: -1
                                        if (index >= 0) {       //it means this task ID already exists
                                            val updatedList = taskLocalData.allTasks.unread.toMutableList()
                                            updatedList[index] = task
                                            taskLocalData.allTasks.unread = updatedList.toList()
                                        } else {
                                            val newList: MutableList<CeibroTaskV2> = mutableListOf()
                                            val oldList = taskLocalData.allTasks.unread.toMutableList()
                                            newList.add(task)
                                            newList.addAll(oldList)
                                            taskLocalData.allTasks.unread = newList.toList()
                                        }
                                        taskDao.insertTaskData(
                                            taskLocalData
                                        )
                                    } else {
                                        val newList: MutableList<CeibroTaskV2> = mutableListOf()
                                        newList.add(task)

                                        taskDao.insertTaskData(
                                            TasksV2DatabaseEntity(
                                                rootState = TaskRootStateTags.FromMe.tagValue,
                                                allTasks = TaskV2Response.AllTasks(
                                                    new = listOf(),
                                                    unread = newList.toList(),
                                                    ongoing = listOf(),
                                                    done = listOf()
                                                )
                                            )
                                        )
                                    }
                                }
                                if (taskToMe) {
                                    val taskLocalData = taskDao.getTasks(TaskRootStateTags.ToMe.tagValue)

                                    if (taskLocalData != null) {
                                        val index = taskLocalData.allTasks.new.indexOfFirst { it.id == task.id } ?: -1
                                        if (index >= 0) {       //it means this task ID already exists
                                            val updatedList = taskLocalData.allTasks.new.toMutableList()
                                            updatedList[index] = task
                                            taskLocalData.allTasks.new = updatedList.toList()
                                        } else {
                                            val newList: MutableList<CeibroTaskV2> = mutableListOf()
                                            val oldList = taskLocalData.allTasks.new.toMutableList()
                                            newList.add(task)
                                            newList.addAll(oldList)
                                            taskLocalData.allTasks.new = newList.toList()
                                        }
                                        taskDao.insertTaskData(
                                            taskLocalData
                                        )
                                    } else {
                                        val newList: MutableList<CeibroTaskV2> = mutableListOf()
                                        newList.add(task)

                                        taskDao.insertTaskData(
                                            TasksV2DatabaseEntity(
                                                rootState = TaskRootStateTags.ToMe.tagValue,
                                                allTasks = TaskV2Response.AllTasks(
                                                    unread = listOf(),
                                                    new = newList.toList(),
                                                    ongoing = listOf(),
                                                    done = listOf()
                                                )
                                            )
                                        )
                                    }
                                }
                            }
                        }
                        val list = getCombinedList()
                        if (list.isNotEmpty()) {
                            task?.id?.let { uploadTaskFiles(context, list, it) }
                        }
                        val handler = Handler()
                        handler.postDelayed(Runnable {
                            onBack()
                            loading(false, "")
                        }, 50)
                    } else {
                        loading(false, "Unable to create task")
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
}