package com.zstronics.ceibro.data.remote

import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.base.BaseNetworkRepository
import com.zstronics.ceibro.data.repos.task.models.*
import javax.inject.Inject

class SubTaskRemoteDataSource @Inject constructor(private val service: TaskRetroService) :
    ISubTaskRemoteDataSource, BaseNetworkRepository() {
    override suspend fun getAllSubTasksForUser(state: String): ApiResponse<AllSubtasksResponse> =
        executeSafely(
            call =
            {
                service.getAllSubTasksForUser(state)
            }
        )

    override suspend fun newSubTask(newTask: NewSubtaskRequest): ApiResponse<NewSubTaskResponse> =
        executeSafely(
            call =
            {
                service.newSubTask(newTask)
            }
        )

    override suspend fun getSubTaskByTaskId(taskId: String): ApiResponse<SubTaskByTaskResponse> =
        executeSafely(
            call =
            {
                service.getSubTaskByTaskId(taskId)
            }
        )

    override suspend fun rejectSubtask(updateSubTaskStatusRequest: UpdateSubTaskStatusRequest): ApiResponse<SubTaskByTaskResponse> =
        executeSafely(
            call =
            {
                service.rejectSubtask(updateSubTaskStatusRequest)
            }
        )
}