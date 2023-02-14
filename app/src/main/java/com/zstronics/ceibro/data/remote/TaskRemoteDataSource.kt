package com.zstronics.ceibro.data.remote

import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.base.BaseNetworkRepository
import com.zstronics.ceibro.data.repos.auth.signup.GenericResponse
import com.zstronics.ceibro.data.repos.task.models.*
import javax.inject.Inject

class TaskRemoteDataSource @Inject constructor(private val service: TaskRetroService) :
    ITaskRemoteDataSource, BaseNetworkRepository() {
    override suspend fun tasks(state: String, noPaginate: Boolean): ApiResponse<TasksResponse> =
        executeSafely(
            call =
            {
                service.tasks(state, noPaginate)
            }
        )

    override suspend fun newTask(newTask: NewTaskRequest): ApiResponse<NewTaskResponse> =
        executeSafely(
            call =
            {
                service.newTask(newTask)
            }
        )

    override suspend fun newTaskNoAdvanceOptions(newTask: NewTaskRequestNoAdvanceOptions): ApiResponse<NewTaskResponse> =
        executeSafely(
            call =
            {
                service.newTaskNoAdvanceOptions(newTask)
            }
        )

    override suspend fun updateTaskByIdNoAdvanceOptions(taskId:String, updateTask: UpdateDraftTaskRequestNoAdvanceOptions): ApiResponse<NewTaskResponse> =
        executeSafely(
            call =
            {
                service.updateTaskByIdNoAdvanceOptions(taskId, updateTask)
            }
        )

    override suspend fun updateTaskNoStateNoAdvanceOptions(taskId:String, updateTask: UpdateTaskRequestNoAdvanceOptions): ApiResponse<NewTaskResponse> =
        executeSafely(
            call =
            {
                service.updateTaskNoStateNoAdvanceOptions(taskId, updateTask)
            }
        )

    override suspend fun deleteTask(taskId:String): ApiResponse<GenericResponse> =
        executeSafely(
            call =
            {
                service.deleteTask(taskId)
            }
        )

}