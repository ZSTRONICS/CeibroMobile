package com.zstronics.ceibro.data.remote

import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.base.BaseNetworkRepository
import com.zstronics.ceibro.data.repos.auth.signup.GenericResponse
import com.zstronics.ceibro.data.repos.task.models.*
import javax.inject.Inject

class SubTaskRemoteDataSource @Inject constructor(private val service: TaskRetroService) :
    ISubTaskRemoteDataSource, BaseNetworkRepository() {
    override suspend fun getAllSubTasksForUser(state: String, noPaginate: Boolean): ApiResponse<AllSubtasksResponse> =
        executeSafely(
            call =
            {
                service.getAllSubTasksForUser(state, noPaginate)
            }
        )

    override suspend fun getSubtaskStatuses(subTaskId: String): ApiResponse<SubtaskStatusResponse> =
        executeSafely(
            call =
            {
                service.getSubtaskStatuses(subTaskId)
            }
        )

    override suspend fun newSubTask(newTask: NewSubtaskRequest): ApiResponse<NewSubTaskResponse> =
        executeSafely(
            call =
            {
                service.newSubTask(newTask)
            }
        )

    override suspend fun updateSubTaskById(subtaskId: String, updateSubTask: UpdateDraftSubtaskRequest): ApiResponse<NewSubTaskResponse> =
        executeSafely(
            call =
            {
                service.updateSubTaskById(subtaskId, updateSubTask)
            }
        )

    override suspend fun updateSubTask(subtaskId: String, updateSubTask: UpdateSubtaskRequest): ApiResponse<NewSubTaskResponse> =
        executeSafely(
            call =
            {
                service.updateSubTask(subtaskId, updateSubTask)
            }
        )

    override suspend fun removeSubTaskMember(editDetailRequest: SubTaskEditDetailRequest): ApiResponse<SubTaskByTaskResponse> =
        executeSafely(
            call =
            {
                service.removeSubTaskMember(editDetailRequest)
            }
        )

    override suspend fun markAsDoneForSubtaskMember(editDetailRequest: SubTaskEditDetailRequest): ApiResponse<SubTaskByTaskResponse> =
        executeSafely(
            call =
            {
                service.markAsDoneForSubtaskMember(editDetailRequest)
            }
        )

    override suspend fun deleteSubTask(subtaskId: String): ApiResponse<GenericResponse> =
        executeSafely(
            call =
            {
                service.deleteSubTask(subtaskId)
            }
        )

    override suspend fun getSubTaskByTaskId(taskId: String, noPaginate: Boolean): ApiResponse<SubTaskByTaskResponse> =
        executeSafely(
            call =
            {
                service.getSubTaskByTaskId(taskId, noPaginate)
            }
        )

    override suspend fun rejectSubtask(updateSubTaskStatusRequest: UpdateSubTaskStatusRequest): ApiResponse<SubTaskByTaskResponse> =
        executeSafely(
            call =
            {
                service.rejectSubtask(updateSubTaskStatusRequest)
            }
        )

    override suspend fun updateSubtaskStatus(updateSubTaskStatusRequest: UpdateSubTaskStatusWithoutCommentRequest): ApiResponse<SubTaskByTaskResponse> =
        executeSafely(
            call =
            {
                service.updateSubtaskStatus(updateSubTaskStatusRequest)
            }
        )
}