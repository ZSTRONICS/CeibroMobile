package com.zstronics.ceibro.data.remote

import com.zstronics.ceibro.data.repos.task.models.*
import retrofit2.Response
import retrofit2.http.*

interface TaskRetroService {
    @GET("task")
    suspend fun tasks(
        @Query("state") state: String,
        @Query("noPaginate") noPaginate: Boolean
    ): Response<TasksResponse>

    @GET("task")
    suspend fun subTaskById(
        @Path("taskId") taskId: String
    ): Response<TasksResponse>

    @POST("task")
    suspend fun newTaskNoAdvanceOptions(@Body requestBody: NewTaskRequestNoAdvanceOptions): Response<NewTaskResponse>

    @POST("task")
    suspend fun newTask(@Body requestBody: NewTaskRequest): Response<NewTaskResponse>

    @PATCH("task/{taskId}")
    suspend fun updateTaskByIdNoAdvanceOptions(
        @Path("taskId") taskId: String,
        @Body requestBody: UpdateDraftTaskRequestNoAdvanceOptions,
    ): Response<NewTaskResponse>

    @GET("task/subtask")
    suspend fun getAllSubTasksForUser(
        @Query("state") state: String,
        @Query("noPaginate") noPaginate: Boolean
    ): Response<AllSubtasksResponse>

    @POST("task/subtask")
    suspend fun newSubTask(@Body requestBody: NewSubtaskRequest): Response<NewSubTaskResponse>

    @GET("task/{taskId}")
    suspend fun getSubTaskByTaskId(
        @Path("taskId") taskId: String,
        @Query("noPaginate") noPaginate: Boolean
    ): Response<SubTaskByTaskResponse>

    @POST("task/st/statechange")
    suspend fun rejectSubtask(@Body requestBody: UpdateSubTaskStatusRequest): Response<SubTaskByTaskResponse>

    @POST("task/st/statechange")
    suspend fun updateSubtaskStatus(@Body requestBody: UpdateSubTaskStatusWithoutCommentRequest): Response<SubTaskByTaskResponse>

}