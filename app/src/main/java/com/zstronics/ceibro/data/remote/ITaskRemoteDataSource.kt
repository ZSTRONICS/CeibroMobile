package com.zstronics.ceibro.data.remote

import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.repos.auth.signup.GenericResponse
import com.zstronics.ceibro.data.repos.task.models.NewTaskRequest
import com.zstronics.ceibro.data.repos.task.models.NewTaskRequestNoAdvanceOptions
import com.zstronics.ceibro.data.repos.task.models.NewTaskResponse
import com.zstronics.ceibro.data.repos.task.models.NewTopicCreateRequest
import com.zstronics.ceibro.data.repos.task.models.NewTopicResponse
import com.zstronics.ceibro.data.repos.task.models.TaskV2Response
import com.zstronics.ceibro.data.repos.task.models.TasksResponse
import com.zstronics.ceibro.data.repos.task.models.TopicsResponse
import com.zstronics.ceibro.data.repos.task.models.UpdateDraftTaskRequestNoAdvanceOptions
import com.zstronics.ceibro.data.repos.task.models.UpdateTaskRequestNoAdvanceOptions
import com.zstronics.ceibro.data.repos.task.models.v2.AllTaskFilesV2Response
import com.zstronics.ceibro.data.repos.task.models.v2.AllTasksV2NewResponse
import com.zstronics.ceibro.data.repos.task.models.v2.AllTasksV2Response
import com.zstronics.ceibro.data.repos.task.models.v2.EventV2Response
import com.zstronics.ceibro.data.repos.task.models.v2.ForwardTaskV2Request
import com.zstronics.ceibro.data.repos.task.models.v2.ForwardedToMeNewTaskV2Response
import com.zstronics.ceibro.data.repos.task.models.v2.HideTaskResponse
import com.zstronics.ceibro.data.repos.task.models.v2.InboxTaskResponse
import com.zstronics.ceibro.data.repos.task.models.v2.NewTaskV2Entity
import com.zstronics.ceibro.data.repos.task.models.v2.NewTaskV2Response
import com.zstronics.ceibro.data.repos.task.models.v2.SocketReSyncUpdateV2Request
import com.zstronics.ceibro.data.repos.task.models.v2.SyncTaskEventsBody
import com.zstronics.ceibro.data.repos.task.models.v2.SyncTaskEventsResponse
import com.zstronics.ceibro.data.repos.task.models.v2.TaskSeenResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody

interface ITaskRemoteDataSource {
    suspend fun tasks(state: String = "all", noPaginate: Boolean = true): ApiResponse<TasksResponse>
    suspend fun newTask(newTask: NewTaskRequest): ApiResponse<NewTaskResponse>
    suspend fun newTaskNoAdvanceOptions(newTask: NewTaskRequestNoAdvanceOptions): ApiResponse<NewTaskResponse>
    suspend fun updateTaskByIdNoAdvanceOptions(
        taskId: String,
        updateTask: UpdateDraftTaskRequestNoAdvanceOptions
    ): ApiResponse<NewTaskResponse>

    suspend fun updateTaskNoStateNoAdvanceOptions(
        taskId: String,
        updateTask: UpdateTaskRequestNoAdvanceOptions
    ): ApiResponse<NewTaskResponse>

    suspend fun syncEvents(
        taskId: String,
        request: SyncTaskEventsBody
    ): ApiResponse<SyncTaskEventsResponse>

    suspend fun deleteTask(taskId: String): ApiResponse<GenericResponse>

    suspend fun getAllTopics(): ApiResponse<TopicsResponse>
    suspend fun getAllInboxTasks(inboxTimeStamp: String): ApiResponse<InboxTaskResponse>
    suspend fun saveTopic(requestBody: NewTopicCreateRequest): ApiResponse<NewTopicResponse>
    suspend fun getAllTasks(rootState: String): ApiResponse<TaskV2Response>
    suspend fun syncAllTask(updatedAtTimeStamp: String): ApiResponse<AllTasksV2Response>
    suspend fun getAllTaskWithEventsSeparately(updatedAtTimeStamp: String): ApiResponse<AllTasksV2NewResponse>

    suspend fun newTaskV2(newTask: NewTaskV2Entity): ApiResponse<NewTaskV2Response>
    suspend fun newTaskV2WithFiles(
        tags: RequestBody,
        confirmer: RequestBody,
        viewer: RequestBody,
        title: RequestBody,
        hasFiles: Boolean,
        dueDate: RequestBody,
        project: RequestBody,
        assignedToState: RequestBody,
        creator: RequestBody,
        description: RequestBody,
        doneImageRequired: RequestBody,
        doneCommentsRequired: RequestBody,
        invitedNumbers: RequestBody,
        files: List<MultipartBody.Part>?,
        metadata: RequestBody
    ): ApiResponse<NewTaskV2Response>

    suspend fun newTaskV2WithFilesWithPinData(
        tags: RequestBody,
        confirmer: RequestBody,
        viewer: RequestBody,
        title: RequestBody,
        hasFiles: Boolean,
        dueDate: RequestBody,
        project: RequestBody,
        assignedToState: RequestBody,
        creator: RequestBody,
        description: RequestBody,
        doneImageRequired: RequestBody,
        doneCommentsRequired: RequestBody,
        pinData: RequestBody,
        invitedNumbers: RequestBody,
        files: List<MultipartBody.Part>?,
        metadata: RequestBody
    ): ApiResponse<NewTaskV2Response>

    suspend fun newTaskV2WithoutFiles(
        tags: RequestBody,
        confirmer: RequestBody,
        viewer: RequestBody,
        title: RequestBody,
        hasFiles: Boolean,
        dueDate: RequestBody,
        project: RequestBody,
        assignedToState: RequestBody,
        creator: RequestBody,
        description: RequestBody,
        doneImageRequired: RequestBody,
        doneCommentsRequired: RequestBody,
        invitedNumbers: RequestBody
    ): ApiResponse<NewTaskV2Response>

    suspend fun forwardTask(
        taskId: String,
        forwardTaskV2Request: ForwardTaskV2Request
    ): ApiResponse<EventV2Response>

    suspend fun taskSeen(taskId: String): ApiResponse<TaskSeenResponse>
    suspend fun cancelTask(taskId: String): ApiResponse<EventV2Response>
    suspend fun unCancelTask(taskId: String): ApiResponse<EventV2Response>
    suspend fun hideTask(taskId: String): ApiResponse<HideTaskResponse>
    suspend fun unHideTask(taskId: String): ApiResponse<HideTaskResponse>
    suspend fun getTaskById(taskId: String): ApiResponse<ForwardedToMeNewTaskV2Response>
    suspend fun syncTaskAndEvents(request: SocketReSyncUpdateV2Request): ApiResponse<AllTasksV2NewResponse.NewData>
    suspend fun getTaskFilesByTaskId(taskId: String): ApiResponse<AllTaskFilesV2Response>
}