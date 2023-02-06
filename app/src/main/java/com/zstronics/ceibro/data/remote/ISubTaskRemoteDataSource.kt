package com.zstronics.ceibro.data.remote

import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.repos.task.models.*

interface ISubTaskRemoteDataSource {
    suspend fun getAllSubTasksForUser(state: String = "all", noPaginate: Boolean = true): ApiResponse<AllSubtasksResponse>
    suspend fun newSubTask(newTask: NewSubtaskRequest): ApiResponse<NewSubTaskResponse>
    suspend fun getSubTaskByTaskId(taskId: String, noPaginate: Boolean = true): ApiResponse<SubTaskByTaskResponse>
    suspend fun rejectSubtask(updateSubTaskStatusRequest: UpdateSubTaskStatusRequest): ApiResponse<SubTaskByTaskResponse>
    suspend fun updateSubtaskStatus(updateSubTaskStatusRequest: UpdateSubTaskStatusWithoutCommentRequest): ApiResponse<SubTaskByTaskResponse>
}