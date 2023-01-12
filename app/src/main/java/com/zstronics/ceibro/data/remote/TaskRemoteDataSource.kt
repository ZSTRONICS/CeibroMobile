package com.zstronics.ceibro.data.remote

import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.base.BaseNetworkRepository
import com.zstronics.ceibro.data.repos.task.models.NewTaskRequest
import com.zstronics.ceibro.data.repos.task.models.NewTaskRequestNoAdvanceOptions
import com.zstronics.ceibro.data.repos.task.models.NewTaskResponse
import com.zstronics.ceibro.data.repos.task.models.TasksResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
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
                val body: RequestBody = RequestBody.create(
                    "application/json".toMediaTypeOrNull(),
                    newTask.toString()
                )
                service.newTask(body)
            }
        )

    override suspend fun newTask(newTask: NewTaskRequestNoAdvanceOptions): ApiResponse<NewTaskResponse> =
        executeSafely(
            call =
            {
                val body: RequestBody = RequestBody.create(
                    "application/json".toMediaTypeOrNull(),
                    newTask.toString()
                )
                service.newTask(body)
            }
        )
}