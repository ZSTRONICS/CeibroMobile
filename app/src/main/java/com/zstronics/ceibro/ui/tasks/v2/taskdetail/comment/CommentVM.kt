package com.zstronics.ceibro.ui.tasks.v2.taskdetail.comment

import android.content.Context
import android.os.Bundle
import android.os.Handler
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.viewmodel.Dispatcher
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.database.dao.TaskV2Dao
import com.zstronics.ceibro.data.database.models.tasks.CeibroTaskV2
import com.zstronics.ceibro.data.database.models.tasks.Events
import com.zstronics.ceibro.data.repos.dashboard.IDashboardRepository
import com.zstronics.ceibro.data.repos.dashboard.attachment.AttachmentTags
import com.zstronics.ceibro.data.repos.task.TaskRootStateTags
import com.zstronics.ceibro.data.repos.task.models.v2.EventCommentOnlyUploadV2Request
import com.zstronics.ceibro.data.repos.task.models.v2.EventV2Response
import com.zstronics.ceibro.data.repos.task.models.v2.EventWithFileUploadV2Request
import com.zstronics.ceibro.data.repos.task.models.v2.TaskDetailEvents
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.ui.tasks.task.TaskStatus
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

                if (eventData != null) {
                    val taskID = eventData.taskId

                    val hiddenByCurrentUser = eventData.taskData.hiddenBy.find { it == user?.id }
                    if (hiddenByCurrentUser != null) {
                    //it means task must be searched from hidden rootState and child states[ongoing and done] and then move the task to another root state from hidden
                    }
                    else if (eventData.taskData.creatorState.equals(TaskStatus.CANCELED.name, true)) {
                        // it means task must be in hidden rootState and child state will be canceled. search an update the task only
                    }
                    else {
                        val taskEvent = Events(
                            id = eventData.id,
                            taskId = eventData.taskId,
                            eventType = eventData.eventType,
                            initiator = eventData.initiator,
                            eventData = eventData.eventData,
                            commentData = eventData.commentData,
                            createdAt = eventData.createdAt,
                            updatedAt = eventData.updatedAt,
                            v = null
                        )
                        val taskEventList: MutableList<Events> = mutableListOf()
                        taskEventList.add(taskEvent)

                        launch {
                            val taskToMeLocalData = taskDao.getTasks(TaskRootStateTags.ToMe.tagValue)
                            val taskFromMeLocalData = taskDao.getTasks(TaskRootStateTags.FromMe.tagValue)

                            if (taskToMeLocalData != null) {
                                val newTask = taskToMeLocalData.allTasks.new.find { it.id == taskID }
                                val unreadTask = taskToMeLocalData.allTasks.unread.find { it.id == taskID }
                                val ongoingTask = taskToMeLocalData.allTasks.ongoing.find { it.id == taskID }
                                val doneTask = taskToMeLocalData.allTasks.done.find { it.id == taskID }

                                if (newTask != null) {
                                    val allTaskList = taskToMeLocalData.allTasks.new.toMutableList()
                                    val taskIndex = allTaskList.indexOf(newTask)

                                    var oldEvents = newTask.events.toMutableList()
                                    if (oldEvents.isNotEmpty()) {
                                        val oldOnlyEvent = oldEvents.find { it.id == eventData.id }
                                        if (oldOnlyEvent != null) {     //means event already exist, so replace it
                                            val oldEventIndex = oldEvents.indexOf(oldOnlyEvent)
                                            oldEvents[oldEventIndex] = taskEvent
                                            newTask.events = oldEvents.toList()
                                        } else {
                                            oldEvents.add(taskEvent)
                                            newTask.events = oldEvents.toList()
                                        }
                                    } else {
                                        oldEvents = taskEventList
                                        newTask.events = oldEvents.toList()
                                    }
                                    newTask.hiddenBy = eventData.taskData.hiddenBy
                                    newTask.seenBy = eventData.taskData.seenBy

                                    allTaskList[taskIndex] = newTask
                                    taskToMeLocalData.allTasks.new = allTaskList.toList()
                                    taskData = newTask
                                } else if (unreadTask != null) {
                                    val allTaskList = taskToMeLocalData.allTasks.unread.toMutableList()
                                    val taskIndex = allTaskList.indexOf(unreadTask)

                                    var oldEvents = unreadTask.events.toMutableList()
                                    if (oldEvents.isNotEmpty()) {
                                        val oldOnlyEvent = oldEvents.find { it.id == eventData.id }
                                        if (oldOnlyEvent != null) {     //means event already exist, so replace it
                                            val oldEventIndex = oldEvents.indexOf(oldOnlyEvent)
                                            oldEvents[oldEventIndex] = taskEvent
                                            unreadTask.events = oldEvents.toList()
                                        } else {
                                            oldEvents.add(taskEvent)
                                            unreadTask.events = oldEvents.toList()
                                        }
                                    } else {
                                        oldEvents = taskEventList
                                        unreadTask.events = oldEvents.toList()
                                    }
                                    unreadTask.hiddenBy = eventData.taskData.hiddenBy
                                    unreadTask.seenBy = eventData.taskData.seenBy

                                    allTaskList[taskIndex] = unreadTask
                                    taskToMeLocalData.allTasks.unread = allTaskList.toList()
                                    taskData = unreadTask
                                } else if (ongoingTask != null) {
                                    val allTaskList = taskToMeLocalData.allTasks.ongoing.toMutableList()
                                    val taskIndex = allTaskList.indexOf(ongoingTask)

                                    var oldEvents = ongoingTask.events.toMutableList()
                                    if (oldEvents.isNotEmpty()) {
                                        val oldOnlyEvent = oldEvents.find { it.id == eventData.id }
                                        if (oldOnlyEvent != null) {     //means event already exist, so replace it
                                            val oldEventIndex = oldEvents.indexOf(oldOnlyEvent)
                                            oldEvents[oldEventIndex] = taskEvent
                                            ongoingTask.events = oldEvents.toList()
                                        } else {
                                            oldEvents.add(taskEvent)
                                            ongoingTask.events = oldEvents.toList()
                                        }
                                    } else {
                                        oldEvents = taskEventList
                                        ongoingTask.events = oldEvents.toList()
                                    }
                                    ongoingTask.hiddenBy = eventData.taskData.hiddenBy
                                    ongoingTask.seenBy = eventData.taskData.seenBy

                                    allTaskList[taskIndex] = ongoingTask
                                    taskToMeLocalData.allTasks.ongoing = allTaskList.toList()
                                    taskData = ongoingTask
                                } else if (doneTask != null) {
                                    val allTaskList = taskToMeLocalData.allTasks.done.toMutableList()
                                    val taskIndex = allTaskList.indexOf(doneTask)

                                    var oldEvents = doneTask.events.toMutableList()
                                    if (oldEvents.isNotEmpty()) {
                                        val oldOnlyEvent = oldEvents.find { it.id == eventData.id }
                                        if (oldOnlyEvent != null) {     //means event already exist, so replace it
                                            val oldEventIndex = oldEvents.indexOf(oldOnlyEvent)
                                            oldEvents[oldEventIndex] = taskEvent
                                            doneTask.events = oldEvents.toList()
                                        } else {
                                            oldEvents.add(taskEvent)
                                            doneTask.events = oldEvents.toList()
                                        }
                                    } else {
                                        oldEvents = taskEventList
                                        doneTask.events = oldEvents.toList()
                                    }
                                    doneTask.hiddenBy = eventData.taskData.hiddenBy
                                    doneTask.seenBy = eventData.taskData.seenBy

                                    allTaskList[taskIndex] = doneTask
                                    taskToMeLocalData.allTasks.done = allTaskList.toList()
                                    taskData = doneTask
                                }

                                taskDao.insertTaskData(
                                    taskToMeLocalData
                                )
                            }

                            if (taskFromMeLocalData != null) {
                                val newTask = taskFromMeLocalData.allTasks.new.find { it.id == taskID }
                                val unreadTask = taskFromMeLocalData.allTasks.unread.find { it.id == taskID }
                                val ongoingTask = taskFromMeLocalData.allTasks.ongoing.find { it.id == taskID }
                                val doneTask = taskFromMeLocalData.allTasks.done.find { it.id == taskID }

                                if (newTask != null) {
                                    val allTaskList = taskFromMeLocalData.allTasks.new.toMutableList()
                                    val taskIndex = allTaskList.indexOf(newTask)

                                    var oldEvents = newTask.events.toMutableList()
                                    if (oldEvents.isNotEmpty()) {
                                        val oldOnlyEvent = oldEvents.find { it.id == eventData.id }
                                        if (oldOnlyEvent != null) {     //means event already exist, so replace it
                                            val oldEventIndex = oldEvents.indexOf(oldOnlyEvent)
                                            oldEvents[oldEventIndex] = taskEvent
                                            newTask.events = oldEvents.toList()
                                        } else {
                                            oldEvents.add(taskEvent)
                                            newTask.events = oldEvents.toList()
                                        }
                                    } else {
                                        oldEvents = taskEventList
                                        newTask.events = oldEvents.toList()
                                    }
                                    newTask.hiddenBy = eventData.taskData.hiddenBy
                                    newTask.seenBy = eventData.taskData.seenBy

                                    allTaskList[taskIndex] = newTask
                                    taskFromMeLocalData.allTasks.new = allTaskList.toList()
                                    taskData = newTask
                                } else if (unreadTask != null) {
                                    val allTaskList = taskFromMeLocalData.allTasks.unread.toMutableList()
                                    val taskIndex = allTaskList.indexOf(unreadTask)

                                    var oldEvents = unreadTask.events.toMutableList()
                                    if (oldEvents.isNotEmpty()) {
                                        val oldOnlyEvent = oldEvents.find { it.id == eventData.id }
                                        if (oldOnlyEvent != null) {     //means event already exist, so replace it
                                            val oldEventIndex = oldEvents.indexOf(oldOnlyEvent)
                                            oldEvents[oldEventIndex] = taskEvent
                                            unreadTask.events = oldEvents.toList()
                                        } else {
                                            oldEvents.add(taskEvent)
                                            unreadTask.events = oldEvents.toList()
                                        }
                                    } else {
                                        oldEvents = taskEventList
                                        unreadTask.events = oldEvents.toList()
                                    }
                                    unreadTask.hiddenBy = eventData.taskData.hiddenBy
                                    unreadTask.seenBy = eventData.taskData.seenBy

                                    allTaskList[taskIndex] = unreadTask
                                    taskFromMeLocalData.allTasks.unread = allTaskList.toList()
                                    taskData = unreadTask
                                } else if (ongoingTask != null) {
                                    val allTaskList = taskFromMeLocalData.allTasks.ongoing.toMutableList()
                                    val taskIndex = allTaskList.indexOf(ongoingTask)

                                    var oldEvents = ongoingTask.events.toMutableList()
                                    if (oldEvents.isNotEmpty()) {
                                        val oldOnlyEvent = oldEvents.find { it.id == eventData.id }
                                        if (oldOnlyEvent != null) {     //means event already exist, so replace it
                                            val oldEventIndex = oldEvents.indexOf(oldOnlyEvent)
                                            oldEvents[oldEventIndex] = taskEvent
                                            ongoingTask.events = oldEvents.toList()
                                        } else {
                                            oldEvents.add(taskEvent)
                                            ongoingTask.events = oldEvents.toList()
                                        }
                                    } else {
                                        oldEvents = taskEventList
                                        ongoingTask.events = oldEvents.toList()
                                    }
                                    ongoingTask.hiddenBy = eventData.taskData.hiddenBy
                                    ongoingTask.seenBy = eventData.taskData.seenBy

                                    allTaskList[taskIndex] = ongoingTask
                                    taskFromMeLocalData.allTasks.ongoing = allTaskList.toList()
                                    taskData = ongoingTask
                                } else if (doneTask != null) {
                                    val allTaskList = taskFromMeLocalData.allTasks.done.toMutableList()
                                    val taskIndex = allTaskList.indexOf(doneTask)

                                    var oldEvents = doneTask.events.toMutableList()
                                    if (oldEvents.isNotEmpty()) {
                                        val oldOnlyEvent = oldEvents.find { it.id == eventData.id }
                                        if (oldOnlyEvent != null) {     //means event already exist, so replace it
                                            val oldEventIndex = oldEvents.indexOf(oldOnlyEvent)
                                            oldEvents[oldEventIndex] = taskEvent
                                            doneTask.events = oldEvents.toList()
                                        } else {
                                            oldEvents.add(taskEvent)
                                            doneTask.events = oldEvents.toList()
                                        }
                                    } else {
                                        oldEvents = taskEventList
                                        doneTask.events = oldEvents.toList()
                                    }
                                    doneTask.hiddenBy = eventData.taskData.hiddenBy
                                    doneTask.seenBy = eventData.taskData.seenBy

                                    allTaskList[taskIndex] = doneTask
                                    taskFromMeLocalData.allTasks.done = allTaskList.toList()
                                    taskData = doneTask
                                }

                                taskDao.insertTaskData(
                                    taskFromMeLocalData
                                )
                            }
                        }

                    }
                }
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