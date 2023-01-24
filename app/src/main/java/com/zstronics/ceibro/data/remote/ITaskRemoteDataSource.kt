package com.zstronics.ceibro.data.remote

import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.repos.task.models.*

interface ITaskRemoteDataSource {
    suspend fun tasks(state: String = "all", noPaginate: Boolean = true): ApiResponse<TasksResponse>
    suspend fun newTask(newTask: NewTaskRequest): ApiResponse<NewTaskResponse>
    suspend fun newTaskNoAdvanceOptions(newTask: NewTaskRequestNoAdvanceOptions): ApiResponse<NewTaskResponse>
}