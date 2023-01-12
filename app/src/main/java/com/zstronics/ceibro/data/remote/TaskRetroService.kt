package com.zstronics.ceibro.data.remote

import com.zstronics.ceibro.data.repos.task.models.TasksResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface TaskRetroService {
    @GET("task")
    suspend fun tasks(@Query("state") state: String, @Query("noPaginate") noPaginate: Boolean): Response<TasksResponse>
}