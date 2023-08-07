package com.zstronics.ceibro.data.repos.task

import com.zstronics.ceibro.data.database.models.subtask.AllSubtask
import com.zstronics.ceibro.data.database.models.subtask.SubTaskComments
import com.zstronics.ceibro.data.database.models.tasks.CeibroTask
import com.zstronics.ceibro.data.database.models.tasks.CeibroTaskV2
import com.zstronics.ceibro.data.repos.task.models.*
import com.zstronics.ceibro.data.repos.task.models.v2.ForwardTaskV2Request
import com.zstronics.ceibro.data.repos.task.models.v2.NewTaskV2Request
import com.zstronics.ceibro.data.repos.task.models.v2.TaskSeenResponse

interface ITaskRepository {
    suspend fun tasks(): List<CeibroTask>
    suspend fun getTaskById(taskId: String): CeibroTask
    suspend fun eraseTaskTable()
    suspend fun newTask(
        newTask: NewTaskRequest,
        callBack: (isSuccess: Boolean, message: String) -> Unit
    )

    suspend fun newTaskV2(
        newTask: NewTaskV2Request,
        callBack: (isSuccess: Boolean, task: CeibroTaskV2?, errorMessage: String) -> Unit
    )

    suspend fun forwardTask(
        taskId: String,
        forwardTaskV2Request: ForwardTaskV2Request,
        callBack: (isSuccess: Boolean, task: CeibroTaskV2?, errorMsg: String) -> Unit
    )

    suspend fun taskSeen(
        taskId: String,
        callBack: (isSuccess: Boolean, taskSeenData: TaskSeenResponse.TaskSeen?) -> Unit
    )

    suspend fun newTaskNoAdvanceOptions(
        newTask: NewTaskRequestNoAdvanceOptions,
        callBack: (isSuccess: Boolean, message: String, data: CeibroTask?) -> Unit
    )

    suspend fun updateTaskByIdNoAdvanceOptions(
        taskId: String,
        updateTask: UpdateDraftTaskRequestNoAdvanceOptions,
        callBack: (isSuccess: Boolean, message: String) -> Unit
    )

    suspend fun updateTaskNoStateNoAdvanceOptions(
        taskId: String,
        updateTask: UpdateTaskRequestNoAdvanceOptions,
        callBack: (isSuccess: Boolean, message: String) -> Unit
    )

    suspend fun deleteTask(
        taskId: String,
        callBack: (isSuccess: Boolean, message: String) -> Unit
    )


    suspend fun getAllSubtasks(): List<AllSubtask>
    suspend fun getSubtaskStatuses(
        subTaskId: String,
        callBack: (isSuccess: Boolean, message: String, subtaskStatusData: List<SubtaskStatusData>?) -> Unit
    )

    suspend fun eraseSubTaskTable()

    suspend fun newSubTask(
        newTask: NewSubtaskRequest,
        callBack: (isSuccess: Boolean, message: String, data: AllSubtask?) -> Unit
    )

    suspend fun updateSubTaskById(
        subtaskId: String,
        updateSubTask: UpdateDraftSubtaskRequest,
        callBack: (isSuccess: Boolean, message: String) -> Unit
    )

    suspend fun updateSubTask(
        subtaskId: String,
        updateSubTask: UpdateSubtaskRequest,
        callBack: (isSuccess: Boolean, message: String) -> Unit
    )

    suspend fun updateMemberInSubTask(
        subtaskId: String,
        addMemberSubTask: AddMemberSubtaskRequest,
        callBack: (isSuccess: Boolean, message: String, subtask: AllSubtask?) -> Unit
    )

    suspend fun removeSubTaskMember(
        taskId: String,
        subTaskId: String,
        memberId: String,
        callBack: (isSuccess: Boolean, message: String, subtask: AllSubtask?) -> Unit
    )

    suspend fun markAsDoneForSubtaskMember(
        taskId: String,
        subTaskId: String,
        memberId: String,
        callBack: (isSuccess: Boolean, message: String, subtask: AllSubtask?) -> Unit
    )

    suspend fun getSubTaskByTaskId(taskId: String): List<AllSubtask>

    suspend fun syncTasksAndSubTasks()

    suspend fun rejectSubtask(updateSubTaskStatusRequest: UpdateSubTaskStatusRequest): Triple<Boolean, Boolean, Boolean>
    suspend fun updateSubtaskStatus(updateSubTaskStatusRequest: UpdateSubTaskStatusWithoutCommentRequest): Triple<Boolean, Boolean, Boolean>

    suspend fun deleteSubTask(
        subtaskId: String,
        callBack: (isSuccess: Boolean, message: String) -> Unit
    )

    suspend fun postCommentSubtask(
        request: SubtaskCommentRequest,
        callBack: (isSuccess: Boolean, message: String, data: SubTaskComments?) -> Unit
    )

    suspend fun getSubtaskRejections(
        subTaskId: String,
        callBack: (isSuccess: Boolean, message: String, subTaskRejections: List<SubTaskRejections>) -> Unit
    )

    suspend fun getAllCommentsOfSubtask(
        subTaskId: String,
        callBack: (isSuccess: Boolean, message: String, allComments: ArrayList<SubTaskComments>) -> Unit
    )



    //New APIs for Task

    suspend fun getAllTopics(
        callBack: (isSuccess: Boolean, message: String, data: TopicsResponse?) -> Unit
    )
    suspend fun saveTopic(
        requestBody: NewTopicCreateRequest,
        callBack: (isSuccess: Boolean, message: String, data: NewTopicResponse?) -> Unit
    )

}
