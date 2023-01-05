package com.zstronics.ceibro.data.remote

import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.repos.task.models.TasksResponse

interface ITaskRemoteDataSource {
    suspend fun tasks(): ApiResponse<TasksResponse>
}