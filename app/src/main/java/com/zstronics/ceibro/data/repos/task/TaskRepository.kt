package com.zstronics.ceibro.data.repos.task

import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.database.models.subtask.AllSubtask
import com.zstronics.ceibro.data.database.models.subtask.SubTaskComments
import com.zstronics.ceibro.data.database.models.tasks.CeibroTask
import com.zstronics.ceibro.data.database.models.tasks.CeibroTaskV2
import com.zstronics.ceibro.data.local.SubTaskLocalDataSource
import com.zstronics.ceibro.data.local.TaskLocalDataSource
import com.zstronics.ceibro.data.remote.SubTaskRemoteDataSource
import com.zstronics.ceibro.data.remote.TaskRemoteDataSource
import com.zstronics.ceibro.data.repos.task.models.*
import com.zstronics.ceibro.data.repos.task.models.v2.ForwardTaskV2Request
import com.zstronics.ceibro.data.repos.task.models.v2.NewTaskV2Request
import javax.inject.Inject

class TaskRepository @Inject constructor(
    private val localTask: TaskLocalDataSource,
    private val remoteTask: TaskRemoteDataSource,
    private val localSubTask: SubTaskLocalDataSource,
    private val remoteSubTask: SubTaskRemoteDataSource
) : ITaskRepository {

    /// Following calls are for TODO - Task

    override suspend fun tasks(): List<CeibroTask> {
        val list = localTask.tasks()
        return if (list.isEmpty()) {
            /// Fetch from remote and save into local
            when (val response = remoteTask.tasks()) {
                is ApiResponse.Success -> {
                    val tasks = response.data.allTasks
                    localTask.insertAllTasks(tasks)
                    return tasks
                }
                else -> emptyList()
            }
        } else {
            list
        }
    }

    override suspend fun getTaskById(taskId: String): CeibroTask {
        return localTask.getTaskById(taskId)
    }

    override suspend fun eraseTaskTable() = localTask.eraseTaskTable()

    override suspend fun newTask(
        newTask: NewTaskRequest,
        callBack: (isSuccess: Boolean, message: String) -> Unit
    ) {
        when (val response = remoteTask.newTask(newTask)) {
            is ApiResponse.Success -> {
                response.data.newTask?.let { localTask.insertTask(it) }
                callBack(true, "")
            }
            is ApiResponse.Error -> {
                callBack(false, response.error.message)
            }
        }
    }

    override suspend fun newTaskV2(
        newTask: NewTaskV2Request,
        callBack: (isSuccess: Boolean, taskId: String) -> Unit
    ) {
        when (val response = remoteTask.newTaskV2(newTask)) {
            is ApiResponse.Success -> {
                callBack(true, response.data.newTask.id)
            }
            is ApiResponse.Error -> {
                callBack(false, response.error.message)
            }
        }
    }

    override suspend fun forwardTask(
        taskId: String,
        forwardTaskV2Request: ForwardTaskV2Request,
        callBack: (isSuccess: Boolean, task: CeibroTaskV2?) -> Unit
    ) {
        when (val response = remoteTask.forwardTask(taskId, forwardTaskV2Request)) {
            is ApiResponse.Success -> {
                callBack(true, response.data.newTask)
            }
            is ApiResponse.Error -> {
                callBack(false, null)
            }
        }
    }

    override suspend fun newTaskNoAdvanceOptions(
        newTask: NewTaskRequestNoAdvanceOptions,
        callBack: (isSuccess: Boolean, message: String, data: CeibroTask?) -> Unit
    ) {
        when (val response = remoteTask.newTaskNoAdvanceOptions(newTask)) {
            is ApiResponse.Success -> {
                response.data.newTask?.let { localTask.insertTask(it) }
                callBack(true, "", response.data.newTask)
            }
            is ApiResponse.Error -> {
                callBack(false, response.error.message, null)
            }
        }
    }

    override suspend fun updateTaskByIdNoAdvanceOptions(
        taskId: String,
        updateTask: UpdateDraftTaskRequestNoAdvanceOptions,
        callBack: (isSuccess: Boolean, message: String) -> Unit
    ) {
        when (val response = remoteTask.updateTaskByIdNoAdvanceOptions(taskId, updateTask)) {
            is ApiResponse.Success -> {
                val responseObj = response.data.newTask
                if (responseObj?._id != null) {
                    localTask.updateTask(response.data.newTask)
                }
                callBack(true, "")
            }
            is ApiResponse.Error -> {
                callBack(false, response.error.message)
            }
        }
    }

    override suspend fun updateTaskNoStateNoAdvanceOptions(
        taskId: String,
        updateTask: UpdateTaskRequestNoAdvanceOptions,
        callBack: (isSuccess: Boolean, message: String) -> Unit
    ) {
        when (val response = remoteTask.updateTaskNoStateNoAdvanceOptions(taskId, updateTask)) {
            is ApiResponse.Success -> {
                val responseObj = response.data.newTask
                if (responseObj?._id != null) {
                    localTask.updateTask(response.data.newTask)
                }
                callBack(true, "")
            }
            is ApiResponse.Error -> {
                callBack(false, response.error.message)
            }
        }
    }

    override suspend fun deleteTask(
        taskId: String,
        callBack: (isSuccess: Boolean, message: String) -> Unit
    ) {
        when (val response = remoteTask.deleteTask(taskId)) {
            is ApiResponse.Success -> {
                val responseObj = response.data.message

                localTask.deleteTaskById(taskId)
                localSubTask.deleteSubtaskByTaskId(taskId)

                callBack(true, response.data.message)
            }
            is ApiResponse.Error -> {
                callBack(false, response.error.message)
            }
        }
    }


    /// Following calls are for Sub-Task
    override suspend fun getAllSubtasks(): List<AllSubtask> {
        val list = localSubTask.getSubTasks()
        return if (list.isEmpty()) {
            when (val response = remoteSubTask.getAllSubTasksForUser("all")) {
                is ApiResponse.Success -> {
                    val subTasks = response.data.allSubtasks
                    localSubTask.insertAllSubTasks(subTasks)
                    return subTasks
                }
                else -> emptyList()
            }
        } else {
            list
        }
    }

    override suspend fun getSubtaskStatuses(
        subTaskId: String,
        callBack: (isSuccess: Boolean, message: String, subtaskStatusData: List<SubtaskStatusData>?) -> Unit
    ) {
        when (val response = remoteSubTask.getSubtaskStatuses(subTaskId)) {
            is ApiResponse.Success -> {
                val subTaskStatuses = response.data.result
                callBack(true, "", subTaskStatuses)
            }
            is ApiResponse.Error -> {
                callBack(false, response.error.message, emptyList())
            }
        }
    }

    override suspend fun getSubTaskByTaskId(taskId: String): List<AllSubtask> {
        val list = localSubTask.getSubTaskByTaskId(taskId)
        return if (list.isEmpty()) {
            when (val response = remoteSubTask.getSubTaskByTaskId(taskId)) {
                is ApiResponse.Success -> {
                    val subTasks = response.data.results.subtasks
                    localSubTask.insertAllSubTasks(subTasks)
                    return subTasks
                }
                else -> emptyList()
            }
        } else {
            list
        }
    }

    override suspend fun newSubTask(
        newTask: NewSubtaskRequest,
        callBack: (isSuccess: Boolean, message: String, data: AllSubtask?) -> Unit
    ) {
        when (val response = remoteSubTask.newSubTask(newTask)) {
            is ApiResponse.Success -> {
                response.data.newSubtask?.let { localSubTask.insertSubTask(it) }
                callBack(true, "", response.data.newSubtask)
            }
            is ApiResponse.Error -> {
                callBack(false, response.error.message, null)
            }
        }
    }

    override suspend fun updateSubTaskById(
        subtaskId: String,
        updateSubTask: UpdateDraftSubtaskRequest,
        callBack: (isSuccess: Boolean, message: String) -> Unit
    ) {
        when (val response = remoteSubTask.updateSubTaskById(subtaskId, updateSubTask)) {
            is ApiResponse.Success -> {
                response.data.newSubtask?.let { localSubTask.updateSubTask(it) }
                callBack(true, "")
            }
            is ApiResponse.Error -> {
                callBack(false, response.error.message)
            }
        }
    }

    override suspend fun updateSubTask(
        subtaskId: String,
        updateSubTask: UpdateSubtaskRequest,
        callBack: (isSuccess: Boolean, message: String) -> Unit
    ) {
        when (val response = remoteSubTask.updateSubTask(subtaskId, updateSubTask)) {
            is ApiResponse.Success -> {
                response.data.newSubtask?.let { localSubTask.updateSubTask(it) }
                callBack(true, "")
            }
            is ApiResponse.Error -> {
                callBack(false, response.error.message)
            }
        }
    }

    override suspend fun updateMemberInSubTask(
        subtaskId: String,
        addMemberSubTask: AddMemberSubtaskRequest,
        callBack: (isSuccess: Boolean, message: String, subtask: AllSubtask?) -> Unit
    ) {
        var changedSubTask: AllSubtask? = null

        when (val response = remoteSubTask.updateMemberInSubTask(subtaskId, addMemberSubTask)) {
            is ApiResponse.Success -> {
                changedSubTask = response.data.newSubtask

                if (changedSubTask != null) {
                    localSubTask.updateSubTask(changedSubTask)
                }

                callBack(true, "", changedSubTask)
            }
            is ApiResponse.Error -> {
                callBack(false, response.error.message, changedSubTask)
            }
        }
    }

    override suspend fun removeSubTaskMember(
        taskId: String,
        subTaskId: String,
        memberId: String,
        callBack: (isSuccess: Boolean, message: String, subtask: AllSubtask?) -> Unit
    ) {
        val editDetailRequest = SubTaskEditDetailRequest(
            taskId = taskId,
            subTaskId = subTaskId,
            memberId = memberId
        )
        var changedSubTask: AllSubtask? = null

        when (val response = remoteSubTask.removeSubTaskMember(editDetailRequest)) {
            is ApiResponse.Success -> {
                val subTasks = response.data.results.subtasks
                val task = response.data.results.task

                if (subTasks.isNotEmpty()) {
                    val subTask = subTasks.find { it.id == subTaskId }
                    if (subTask != null) {
                        localSubTask.updateSubTask(subTask)
                        changedSubTask = subTask
                    }
                }
                if (task?._id != null) {
                    localTask.updateTask(task)
                }

                callBack(true, "", changedSubTask)
            }
            is ApiResponse.Error -> {
                callBack(false, response.error.message, changedSubTask)
            }
        }
    }

    override suspend fun markAsDoneForSubtaskMember(
        taskId: String,
        subTaskId: String,
        memberId: String,
        callBack: (isSuccess: Boolean, message: String, subtask: AllSubtask?) -> Unit
    ) {
        val editDetailRequest = SubTaskEditDetailRequest(
            taskId = taskId,
            subTaskId = subTaskId,
            memberId = memberId
        )
        var changedSubTask: AllSubtask? = null

        when (val response = remoteSubTask.markAsDoneForSubtaskMember(editDetailRequest)) {
            is ApiResponse.Success -> {
                val subTasks = response.data.results.subtasks
                val task = response.data.results.task

                if (subTasks.isNotEmpty()) {
                    val subTask = subTasks.find { it.id == subTaskId }
                    if (subTask != null) {
                        localSubTask.updateSubTask(subTask)
                        changedSubTask = subTask
                    }
                }
                if (task?._id != null) {
                    localTask.updateTask(task)
                }

                callBack(true, "", changedSubTask)
            }
            is ApiResponse.Error -> {
                callBack(false, response.error.message, changedSubTask)
            }
        }
    }

    override suspend fun deleteSubTask(
        subtaskId: String,
        callBack: (isSuccess: Boolean, message: String) -> Unit
    ) {
        when (val response = remoteSubTask.deleteSubTask(subtaskId)) {
            is ApiResponse.Success -> {
                val responseObj = response.data.message

                localSubTask.deleteSubtaskById(subtaskId)

                callBack(true, response.data.message)
            }
            is ApiResponse.Error -> {
                callBack(false, response.error.message)
            }
        }
    }

    override suspend fun eraseSubTaskTable() = localSubTask.eraseSubTaskTable()

    private suspend fun syncSubTask() {
        when (val response = remoteSubTask.getAllSubTasksForUser("all")) {
            is ApiResponse.Success -> {
                val subTasks = response.data.allSubtasks
                localSubTask.insertAllSubTasks(subTasks)
            }
            else -> {}
        }
    }

    private suspend fun syncTask() {
        when (val response = remoteTask.tasks()) {
            is ApiResponse.Success -> {
                val tasks = response.data.allTasks
                localTask.insertAllTasks(tasks)
            }
            else -> {}
        }
    }

    override suspend fun syncTasksAndSubTasks() {
        syncTask()
        syncSubTask()
    }

    override suspend fun rejectSubtask(updateSubTaskStatusRequest: UpdateSubTaskStatusRequest): Triple<Boolean, Boolean, Boolean> {
        val response = remoteSubTask.rejectSubtask(updateSubTaskStatusRequest)
        return processStatusResponse(
            updateSubTaskStatusRequest.subTaskId,
            updateSubTaskStatusRequest.taskId,
            response
        )
    }

    override suspend fun updateSubtaskStatus(updateSubTaskStatusRequest: UpdateSubTaskStatusWithoutCommentRequest): Triple<Boolean, Boolean, Boolean> {
        val response = remoteSubTask.updateSubtaskStatus(updateSubTaskStatusRequest)
        return processStatusResponse(
            updateSubTaskStatusRequest.subTaskId,
            updateSubTaskStatusRequest.taskId,
            response
        )
    }

    private suspend fun processStatusResponse(
        subTaskId: String,
        taskId: String,
        response: ApiResponse<SubTaskByTaskResponse>
    ): Triple<Boolean, Boolean, Boolean> {
        when (response) {
            is ApiResponse.Success -> {
                val subTasks = response.data.results.subtasks
                val task = response.data.results.task
                var taskDeleted = false
                var subTaskDeleted = false
                if (subTasks.isEmpty()) {
                    localSubTask.deleteSubtaskById(subTaskId)
                    subTaskDeleted = true
                } else {
                    val subTask = subTasks.find { it.id == subTaskId }
                    if (subTask != null) {
                        localSubTask.updateSubTask(subTask)
                        subTaskDeleted = false
                    }
                }

                if (task?._id == null) {
                    taskDeleted = true
                    localTask.deleteTaskById(taskId)
                } else {
                    taskDeleted = false
                    localTask.updateTask(task)
                }
                return Triple(true, taskDeleted, subTaskDeleted)
            }
            else -> return Triple(false, false, false)
        }
    }

    override suspend fun postCommentSubtask(
        request: SubtaskCommentRequest,
        callBack: (isSuccess: Boolean, message: String, data: SubTaskComments?) -> Unit
    ) {
        when (val response = remoteSubTask.postCommentSubtask(request)) {
            is ApiResponse.Success -> {
                localSubTask.addComment(request.subTaskId, response.data.result)
                callBack(true, "", response.data.result)
            }
            is ApiResponse.Error -> callBack(false, response.error.message, null)
        }
    }

    override suspend fun getSubtaskRejections(
        subTaskId: String,
        callBack: (isSuccess: Boolean, message: String, subTaskRejections: List<SubTaskRejections>) -> Unit
    ) {
        when (val response = remoteSubTask.getSubtaskRejections(subTaskId)) {
            is ApiResponse.Success -> {
                val rejections = response.data.result
                callBack(true, "", rejections)
            }
            is ApiResponse.Error -> callBack(false, response.error.message, emptyList())
        }
    }

    override suspend fun getAllCommentsOfSubtask(
        subTaskId: String,
        callBack: (isSuccess: Boolean, message: String, subTaskRejections: ArrayList<SubTaskComments>) -> Unit
    ) {
        when (val response = remoteSubTask.getAllCommentsOfSubtask(subTaskId)) {
            is ApiResponse.Success -> {
                callBack(true, "", response.data.result)
            }
            is ApiResponse.Error -> callBack(false, response.error.message, arrayListOf())
        }
    }


    //New APIs for Task

    override suspend fun getAllTopics(
        callBack: (isSuccess: Boolean, message: String, data: TopicsResponse?) -> Unit
    ) {
        when (val response = remoteTask.getAllTopics()) {
            is ApiResponse.Success -> {
                callBack(true, "", response.data)
            }
            is ApiResponse.Error -> {
                callBack(false, response.error.message, null)
            }
        }
    }

    override suspend fun saveTopic(
        requestBody: NewTopicCreateRequest,
        callBack: (isSuccess: Boolean, message: String, data: NewTopicResponse?) -> Unit
    ) {
        when (val response = remoteTask.saveTopic(requestBody)) {
            is ApiResponse.Success -> {
                val responseObj = response.data

                callBack(true, "", response.data)
            }
            is ApiResponse.Error -> {
                callBack(false, response.error.message, null)
            }
        }
    }

}