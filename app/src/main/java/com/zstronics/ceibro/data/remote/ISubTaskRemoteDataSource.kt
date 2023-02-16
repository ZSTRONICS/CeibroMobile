package com.zstronics.ceibro.data.remote

import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.repos.auth.signup.GenericResponse
import com.zstronics.ceibro.data.repos.task.models.*

interface ISubTaskRemoteDataSource {
    suspend fun getAllSubTasksForUser(state: String = "all", noPaginate: Boolean = true): ApiResponse<AllSubtasksResponse>
    suspend fun getSubtaskStatuses(subTaskId: String): ApiResponse<SubtaskStatusResponse>
    suspend fun newSubTask(newTask: NewSubtaskRequest): ApiResponse<NewSubTaskResponse>
    suspend fun updateSubTaskById(subtaskId: String, updateSubTask: UpdateDraftSubtaskRequest): ApiResponse<NewSubTaskResponse>
    suspend fun updateSubTask(subtaskId: String, updateSubTask: UpdateSubtaskRequest): ApiResponse<NewSubTaskResponse>
    suspend fun removeSubTaskMember(editDetailRequest: SubTaskEditDetailRequest): ApiResponse<SubTaskByTaskResponse>
    suspend fun markAsDoneForSubtaskMember(editDetailRequest: SubTaskEditDetailRequest): ApiResponse<SubTaskByTaskResponse>
    suspend fun deleteSubTask(subtaskId: String): ApiResponse<GenericResponse>
    suspend fun getSubTaskByTaskId(taskId: String, noPaginate: Boolean = true): ApiResponse<SubTaskByTaskResponse>
    suspend fun rejectSubtask(updateSubTaskStatusRequest: UpdateSubTaskStatusRequest): ApiResponse<SubTaskByTaskResponse>
    suspend fun updateSubtaskStatus(updateSubTaskStatusRequest: UpdateSubTaskStatusWithoutCommentRequest): ApiResponse<SubTaskByTaskResponse>
}