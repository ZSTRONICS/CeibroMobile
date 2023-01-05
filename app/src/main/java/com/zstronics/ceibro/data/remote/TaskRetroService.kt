package com.zstronics.ceibro.data.remote

import com.zstronics.ceibro.data.repos.task.models.TasksResponse
import retrofit2.Response
import retrofit2.http.GET

interface TaskRetroService {
    @GET("task")
    suspend fun tasks(): Response<TasksResponse>
}