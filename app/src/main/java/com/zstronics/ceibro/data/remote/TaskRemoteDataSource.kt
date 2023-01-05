package com.zstronics.ceibro.data.remote

import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.base.BaseNetworkRepository
import com.zstronics.ceibro.data.repos.task.models.TasksResponse
import javax.inject.Inject

class TaskRemoteDataSource @Inject constructor(private val service: TaskRetroService) :
    ITaskRemoteDataSource, BaseNetworkRepository() {
    override suspend fun tasks(): ApiResponse<TasksResponse> = executeSafely(
        call =
        {
            service.tasks()
        }
    )

}