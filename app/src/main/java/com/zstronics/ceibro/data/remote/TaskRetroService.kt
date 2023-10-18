package com.zstronics.ceibro.data.remote

import com.zstronics.ceibro.data.repos.auth.signup.GenericResponse
import com.zstronics.ceibro.data.repos.task.models.*
import com.zstronics.ceibro.data.repos.task.models.v2.AllTasksResponse
import com.zstronics.ceibro.data.repos.task.models.v2.AllTasksV2Response
import com.zstronics.ceibro.data.repos.task.models.v2.EventV2Response
import com.zstronics.ceibro.data.repos.task.models.v2.ForwardTaskV2Request
import com.zstronics.ceibro.data.repos.task.models.v2.HideTaskResponse
import com.zstronics.ceibro.data.repos.task.models.v2.NewTaskV2Entity
import com.zstronics.ceibro.data.repos.task.models.v2.NewTaskV2Response
import com.zstronics.ceibro.data.repos.task.models.v2.SyncTasksBody
import com.zstronics.ceibro.data.repos.task.models.v2.SyncTasksResponse
import com.zstronics.ceibro.data.repos.task.models.v2.TaskSeenResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface TaskRetroService {
    @GET("v1/task")
    suspend fun tasks(
        @Query("state") state: String,
        @Query("noPaginate") noPaginate: Boolean
    ): Response<TasksResponse>

    @GET("v1/task")
    suspend fun subTaskById(
        @Path("taskId") taskId: String
    ): Response<TasksResponse>

    @POST("v1/task")
    suspend fun newTaskNoAdvanceOptions(@Body requestBody: NewTaskRequestNoAdvanceOptions): Response<NewTaskResponse>

    @POST("v1/task")
    suspend fun newTask(@Body requestBody: NewTaskRequest): Response<NewTaskResponse>

    @PATCH("v1/task/{taskId}")
    suspend fun updateTaskByIdNoAdvanceOptions(
        @Path("taskId") taskId: String,
        @Body requestBody: UpdateDraftTaskRequestNoAdvanceOptions,
    ): Response<NewTaskResponse>

    @POST("/v2/task/syncEvents/{taskId}")
    suspend fun syncEvents(
        @Path("taskId") taskId: String,
        @Body requestBody: SyncTasksBody
    ): Response<SyncTasksResponse>

    @PATCH("v1/task/{taskId}")
    suspend fun updateTaskNoStateNoAdvanceOptions(
        @Path("taskId") taskId: String,
        @Body requestBody: UpdateTaskRequestNoAdvanceOptions,
    ): Response<NewTaskResponse>

    @DELETE("v1/task/{taskId}")
    suspend fun deleteTask(
        @Path("taskId") taskId: String
    ): Response<GenericResponse>


    @GET("v1/task/subtask")
    suspend fun getAllSubTasksForUser(
        @Query("state") state: String,
        @Query("noPaginate") noPaginate: Boolean
    ): Response<AllSubtasksResponse>

    @GET("v1/task/st/getStatus/{subTaskId}")
    suspend fun getSubtaskStatuses(
        @Path("subTaskId") subTaskId: String
    ): Response<SubtaskStatusResponse>

    @POST("v1/task/subtask")
    suspend fun newSubTask(@Body requestBody: NewSubtaskRequest): Response<NewSubTaskResponse>

    @PATCH("v1/task/subTask/{subTaskId}")
    suspend fun updateSubTaskById(
        @Path("subTaskId") subTaskId: String,
        @Body requestBody: UpdateDraftSubtaskRequest
    ): Response<NewSubTaskResponse>

    @PATCH("v1/task/subTask/{subTaskId}")
    suspend fun updateSubTask(
        @Path("subTaskId") subTaskId: String,
        @Body requestBody: UpdateSubtaskRequest
    ): Response<NewSubTaskResponse>

    @PATCH("v1/task/subTask/{subTaskId}")
    suspend fun updateMemberInSubTask(
        @Path("subTaskId") subTaskId: String,
        @Body requestBody: AddMemberSubtaskRequest
    ): Response<NewSubTaskResponse>

    @POST("v1/task/st/removeMember")
    suspend fun removeSubTaskMember(
        @Body requestBody: SubTaskEditDetailRequest
    ): Response<SubTaskByTaskResponse>

    @POST("v1/task/st/markAsDone")
    suspend fun markAsDoneForSubtaskMember(
        @Body requestBody: SubTaskEditDetailRequest
    ): Response<SubTaskByTaskResponse>

    @DELETE("v1/task/subtask/{subTaskId}")
    suspend fun deleteSubTask(
        @Path("subTaskId") subTaskId: String
    ): Response<GenericResponse>

    @GET("v1/task/{taskId}")
    suspend fun getSubTaskByTaskId(
        @Path("taskId") taskId: String,
        @Query("noPaginate") noPaginate: Boolean
    ): Response<SubTaskByTaskResponse>

    @POST("v1/task/st/statechange")
    suspend fun rejectSubtask(@Body requestBody: UpdateSubTaskStatusRequest): Response<SubTaskByTaskResponse>

    @POST("v1/task/st/statechange")
    suspend fun updateSubtaskStatus(@Body requestBody: UpdateSubTaskStatusWithoutCommentRequest): Response<SubTaskByTaskResponse>

    @POST("v1/task/st/comment")
    suspend fun postCommentSubtask(@Body request: SubtaskCommentRequest): Response<SubtaskCommentResponse>

    @GET("v1/task/st/getRejections/{subTaskId}")
    suspend fun getSubtaskRejections(
        @Path("subTaskId") subTaskId: String
    ): Response<SubTaskRejectionsResponse>

    @GET("v1/task/st/comment/{subTaskId}")
    suspend fun getAllCommentsOfSubtask(
        @Path("subTaskId") subTaskId: String
    ): Response<AllCommentsResponse>


    //New APIs for Task

    @GET("v2/task/topic")
    suspend fun getAllTopics(): Response<TopicsResponse>

    @POST("v2/task/topic")
    suspend fun saveTopic(@Body requestBody: NewTopicCreateRequest): Response<NewTopicResponse>

    @POST("v2/task")
    suspend fun newTaskV2(@Body requestBody: NewTaskV2Entity): Response<NewTaskV2Response>

    @Multipart
    @POST("v2/task/files")
    suspend fun newTaskV2WithFiles(
        @Query("hasFiles") hasFiles: Boolean,
        @Part("dueDate") dueDate: RequestBody,
        @Part("topic") topic: RequestBody,
        @Part("project") project: RequestBody,
        @Part("assignedToState") assignedToState: RequestBody,
        @Part("creator") creator: RequestBody,
        @Part("description") description: RequestBody,
        @Part("doneImageRequired") doneImageRequired: RequestBody,
        @Part("doneCommentsRequired") doneCommentsRequired: RequestBody,
        @Part("invitedNumbers") invitedNumbers: RequestBody,
        @Part files: List<MultipartBody.Part>?,
        @Part("metadata") metadata: RequestBody
    ): Response<NewTaskV2Response>

    @Multipart
    @POST("v2/task/files")
    suspend fun newTaskV2WithoutFiles(
        @Query("hasFiles") hasFiles: Boolean,
        @Part("dueDate") dueDate: RequestBody,
        @Part("topic") topic: RequestBody,
        @Part("project") project: RequestBody,
        @Part("assignedToState") assignedToState: RequestBody,
        @Part("creator") creator: RequestBody,
        @Part("description") description: RequestBody,
        @Part("doneImageRequired") doneImageRequired: RequestBody,
        @Part("doneCommentsRequired") doneCommentsRequired: RequestBody,
        @Part("invitedNumbers") invitedNumbers: RequestBody
    ): Response<NewTaskV2Response>

    @GET("v2/task/{rootState}")
    suspend fun getAllTasks(@Path("rootState") rootState: String): Response<TaskV2Response>

    @GET("v2/task/sync/{updatedAtTimeStamp}")
    suspend fun syncAllTask(@Path("updatedAtTimeStamp") updatedAtTimeStamp: String): Response<AllTasksV2Response>

    @GET("v2/task/syncTask/{updatedAtTimeStamp}")
    suspend fun getTaskWithUpdatedTimeStamp(@Path("updatedAtTimeStamp") updatedAtTimeStamp: String): Response<AllTasksResponse>

    @POST("v2/task/forward/{taskId}")
    suspend fun forwardTask(
        @Path("taskId") taskId: String,
        @Body request: ForwardTaskV2Request
    ): Response<NewTaskV2Response>

    @POST("v2/task/seen/{taskId}")
    suspend fun taskSeen(
        @Path("taskId") taskId: String
    ): Response<TaskSeenResponse>

    @POST("v2/task/cancel/{taskId}")
    suspend fun cancelTask(
        @Path("taskId") taskId: String
    ): Response<EventV2Response>

    @POST("v2/task/uncancel/{taskId}")
    suspend fun unCancelTask(
        @Path("taskId") taskId: String
    ): Response<EventV2Response>


    @POST("v2/task/hide/{taskId}")
    suspend fun hideTask(
        @Path("taskId") taskId: String
    ): Response<HideTaskResponse>

    @POST("v2/task/unhide/{taskId}")
    suspend fun unHideTask(
        @Path("taskId") taskId: String
    ): Response<HideTaskResponse>

}