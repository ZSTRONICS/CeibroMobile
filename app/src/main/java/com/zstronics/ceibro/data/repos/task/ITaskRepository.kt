package com.zstronics.ceibro.data.repos.task

import com.zstronics.ceibro.data.database.models.subtask.AllSubtask
import com.zstronics.ceibro.data.database.models.tasks.CeibroTask
import com.zstronics.ceibro.data.repos.task.models.*

interface ITaskRepository {
    suspend fun tasks(): List<CeibroTask>
    suspend fun getTaskById(taskId: String): CeibroTask
    suspend fun eraseTaskTable()
    suspend fun newTask(
        newTask: NewTaskRequest,
        callBack: (isSuccess: Boolean, message: String) -> Unit
    )

    suspend fun newTaskNoAdvanceOptions(
        newTask: NewTaskRequestNoAdvanceOptions,
        callBack: (isSuccess: Boolean, message: String) -> Unit
    )

    suspend fun updateTaskByIdNoAdvanceOptions(
        taskId: String,
        updateTask: UpdateDraftTaskRequestNoAdvanceOptions,
        callBack: (isSuccess: Boolean, message: String) -> Unit
    )

    suspend fun getAllSubtasks(): List<AllSubtask>
    suspend fun eraseSubTaskTable()

    suspend fun newSubTask(
        newTask: NewSubtaskRequest,
        callBack: (isSuccess: Boolean, message: String) -> Unit
    )

    suspend fun getSubTaskByTaskId(taskId: String): List<AllSubtask>

    suspend fun syncTasksAndSubTasks()

    suspend fun rejectSubtask(updateSubTaskStatusRequest: UpdateSubTaskStatusRequest): Triple<Boolean,Boolean, Boolean>
    suspend fun updateSubtaskStatus(updateSubTaskStatusRequest: UpdateSubTaskStatusWithoutCommentRequest): Triple<Boolean,Boolean, Boolean>
}
