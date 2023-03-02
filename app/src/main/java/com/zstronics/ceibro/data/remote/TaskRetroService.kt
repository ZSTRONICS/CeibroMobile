package com.zstronics.ceibro.data.remote

import com.zstronics.ceibro.data.repos.auth.signup.GenericResponse
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

    @PATCH("task/{taskId}")
    suspend fun updateTaskNoStateNoAdvanceOptions(
        @Path("taskId") taskId: String,
        @Body requestBody: UpdateTaskRequestNoAdvanceOptions,
    ): Response<NewTaskResponse>

    @DELETE("task/{taskId}")
    suspend fun deleteTask(
        @Path("taskId") taskId: String
    ): Response<GenericResponse>



    @GET("task/subtask")
    suspend fun getAllSubTasksForUser(
        @Query("state") state: String,
        @Query("noPaginate") noPaginate: Boolean
    ): Response<AllSubtasksResponse>

    @GET("task/st/getStatus/{subTaskId}")
    suspend fun getSubtaskStatuses(
        @Path("subTaskId") subTaskId: String
    ): Response<SubtaskStatusResponse>

    @POST("task/subtask")
    suspend fun newSubTask(@Body requestBody: NewSubtaskRequest): Response<NewSubTaskResponse>

    @PATCH("task/subTask/{subTaskId}")
    suspend fun updateSubTaskById(
        @Path("subTaskId") subTaskId: String,
        @Body requestBody: UpdateDraftSubtaskRequest
    ): Response<NewSubTaskResponse>

    @PATCH("task/subTask/{subTaskId}")
    suspend fun updateSubTask(
        @Path("subTaskId") subTaskId: String,
        @Body requestBody: UpdateSubtaskRequest
    ): Response<NewSubTaskResponse>

    @PATCH("task/subTask/{subTaskId}")
    suspend fun updateMemberInSubTask(
        @Path("subTaskId") subTaskId: String,
        @Body requestBody: AddMemberSubtaskRequest
    ): Response<NewSubTaskResponse>

    @POST("task/st/removeMember")
    suspend fun removeSubTaskMember(
        @Body requestBody: SubTaskEditDetailRequest
    ): Response<SubTaskByTaskResponse>

    @POST("task/st/markAsDone")
    suspend fun markAsDoneForSubtaskMember(
        @Body requestBody: SubTaskEditDetailRequest
    ): Response<SubTaskByTaskResponse>

    @DELETE("task/subtask/{subTaskId}")
    suspend fun deleteSubTask(
        @Path("subTaskId") subTaskId: String
    ): Response<GenericResponse>

    @GET("task/{taskId}")
    suspend fun getSubTaskByTaskId(
        @Path("taskId") taskId: String,
        @Query("noPaginate") noPaginate: Boolean
    ): Response<SubTaskByTaskResponse>

    @POST("task/st/statechange")
    suspend fun rejectSubtask(@Body requestBody: UpdateSubTaskStatusRequest): Response<SubTaskByTaskResponse>

    @POST("task/st/statechange")
    suspend fun updateSubtaskStatus(@Body requestBody: UpdateSubTaskStatusWithoutCommentRequest): Response<SubTaskByTaskResponse>

    @POST("task/st/comment")
    suspend fun postCommentSubtask(@Body request: SubtaskCommentRequest): Response<SubtaskCommentResponse>

    @GET("task/st/getRejections/{subTaskId}")
    suspend fun getSubtaskRejections(
        @Path("subTaskId") subTaskId: String
    ): Response<SubTaskRejectionsResponse>

    @GET("task/st/comment/{subTaskId}")
    suspend fun getAllCommentsOfSubtask(
        @Path("subTaskId") subTaskId: String
    ): Response<AllCommentsResponse>

}