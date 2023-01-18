package com.zstronics.ceibro.data.remote

import com.zstronics.ceibro.data.repos.task.models.*
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface TaskRetroService {
    @GET("task")
    suspend fun tasks(
        @Query("state") state: String,
        @Query("noPaginate") noPaginate: Boolean
    ): Response<TasksResponse>

    @POST("task")
    suspend fun newTaskNoAdvanceOptions(@Body requestBody: NewTaskRequestNoAdvanceOptions): Response<NewTaskResponse>

    @POST("task")
    suspend fun newTask(@Body requestBody: NewTaskRequest): Response<NewTaskResponse>



    @GET("task/subtask")
    suspend fun getAllSubTasksForUser(
        @Query("state") state: String
    ): Response<AllSubtasksResponse>
}