package com.zstronics.ceibro.data.remote

import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.base.BaseNetworkRepository
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
import com.zstronics.ceibro.data.repos.task.models.v2.PinnedCommentV2Response
import com.zstronics.ceibro.data.repos.task.models.v2.SocketReSyncUpdateV2Request
import com.zstronics.ceibro.data.repos.task.models.v2.SyncTaskEventsBody
import com.zstronics.ceibro.data.repos.task.models.v2.SyncTaskEventsResponse
import com.zstronics.ceibro.data.repos.task.models.v2.TaskSeenResponse
import okhttp3.MultipartBody
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
                service.newTask(newTask)
            }
        )

    override suspend fun newTaskV2(newTask: NewTaskV2Entity): ApiResponse<NewTaskV2Response> =
        executeSafely(
            call =
            {
                service.newTaskV2(newTask)
            }
        )

    override suspend fun newTaskV2WithFiles(
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
    ): ApiResponse<NewTaskV2Response> {
        return executeSafely(call = {
            service.newTaskV2WithFiles(
                tags = tags,
                confirmer = confirmer,
                viewer = viewer,
                hasFiles = hasFiles,
                dueDate = dueDate,
                title = title,
                project = project,
                assignedToState = assignedToState,
                creator = creator,
                description = description,
                doneImageRequired = doneImageRequired,
                doneCommentsRequired = doneCommentsRequired,
                invitedNumbers = invitedNumbers,
                files = files,
                metadata = metadata
            )
        })
    }

    override suspend fun newTaskV2WithFilesWithPinData(
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
    ): ApiResponse<NewTaskV2Response> {
        return executeSafely(call = {
            service.newTaskV2WithFilesWithPinData(
                tags = tags,
                confirmer = confirmer,
                viewer = viewer,
                hasFiles = hasFiles,
                dueDate = dueDate,
                title = title,
                project = project,
                assignedToState = assignedToState,
                creator = creator,
                description = description,
                doneImageRequired = doneImageRequired,
                doneCommentsRequired = doneCommentsRequired,
                pinData = pinData,
                invitedNumbers = invitedNumbers,
                files = files,
                metadata = metadata
            )
        })
    }

    override suspend fun newTaskV2WithoutFiles(
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
    ): ApiResponse<NewTaskV2Response> {
        return executeSafely(call = {
            service.newTaskV2WithoutFiles(
                tags = tags,
                confirmer = confirmer,
                viewer = viewer,
                hasFiles = hasFiles,
                dueDate = dueDate,
                title = title,
                project = project,
                assignedToState = assignedToState,
                creator = creator,
                description = description,
                doneImageRequired = doneImageRequired,
                doneCommentsRequired = doneCommentsRequired,
                invitedNumbers = invitedNumbers
            )
        })
    }

    override suspend fun forwardTask(
        taskId: String,
        forwardTaskV2Request: ForwardTaskV2Request
    ): ApiResponse<EventV2Response> =
        executeSafely(
            call =
            {
                service.forwardTask(taskId, forwardTaskV2Request)
            }
        )

    override suspend fun taskSeen(taskId: String): ApiResponse<TaskSeenResponse> =
        executeSafely(
            call =
            {
                service.taskSeen(taskId)
            }
        )

    override suspend fun newTaskNoAdvanceOptions(newTask: NewTaskRequestNoAdvanceOptions): ApiResponse<NewTaskResponse> =
        executeSafely(
            call =
            {
                service.newTaskNoAdvanceOptions(newTask)
            }
        )

    override suspend fun updateTaskByIdNoAdvanceOptions(
        taskId: String,
        updateTask: UpdateDraftTaskRequestNoAdvanceOptions
    ): ApiResponse<NewTaskResponse> =
        executeSafely(
            call =
            {
                service.updateTaskByIdNoAdvanceOptions(taskId, updateTask)
            }
        )

    override suspend fun syncEvents(
        taskId: String,
        request: SyncTaskEventsBody
    ): ApiResponse<SyncTaskEventsResponse> =
        executeSafely(
            call =
            {
                service.syncEvents(taskId, request)
            }
        )

    override suspend fun updateTaskNoStateNoAdvanceOptions(
        taskId: String,
        updateTask: UpdateTaskRequestNoAdvanceOptions
    ): ApiResponse<NewTaskResponse> =
        executeSafely(
            call =
            {
                service.updateTaskNoStateNoAdvanceOptions(taskId, updateTask)
            }
        )

    override suspend fun deleteTask(taskId: String): ApiResponse<GenericResponse> =
        executeSafely(
            call =
            {
                service.deleteTask(taskId)
            }
        )


    override suspend fun getAllTopics(): ApiResponse<TopicsResponse> =
        executeSafely(
            call =
            {
                service.getAllTopics()
            }
        )

    override suspend fun getAllInboxTasks(inboxTimeStamp: String): ApiResponse<InboxTaskResponse> =
        executeSafely(
            call =
            {
                service.getAllInboxTasks(inboxTimeStamp)
            }
        )

    override suspend fun saveTopic(requestBody: NewTopicCreateRequest): ApiResponse<NewTopicResponse> =
        executeSafely(
            call =
            {
                service.saveTopic(requestBody)
            }
        )

    override suspend fun getAllTasks(rootState: String): ApiResponse<TaskV2Response> =
        executeSafely(
            call =
            {
                service.getAllTasks(rootState)
            }
        )

    override suspend fun syncAllTask(updatedAtTimeStamp: String): ApiResponse<AllTasksV2Response> =
        executeSafely(
            call =
            {
                service.syncAllTask(updatedAtTimeStamp)
            }
        )

    override suspend fun getAllTaskWithEventsSeparately(updatedAtTimeStamp: String): ApiResponse<AllTasksV2NewResponse> =
        executeSafely(
            call =
            {
                service.getAllTaskWithEventsSeparately(updatedAtTimeStamp)
            }
        )


    override suspend fun cancelTask(taskId: String): ApiResponse<EventV2Response> =
        executeSafely(
            call =
            {
                service.cancelTask(taskId)
            }
        )

    override suspend fun unCancelTask(taskId: String): ApiResponse<EventV2Response> =
        executeSafely(
            call =
            {
                service.unCancelTask(taskId)
            }
        )


    override suspend fun hideTask(taskId: String): ApiResponse<HideTaskResponse> =
        executeSafely(
            call =
            {
                service.hideTask(taskId)
            }
        )

    override suspend fun unHideTask(taskId: String): ApiResponse<HideTaskResponse> =
        executeSafely(
            call =
            {
                service.unHideTask(taskId)
            }
        )

    override suspend fun getTaskById(taskId: String): ApiResponse<ForwardedToMeNewTaskV2Response> =
        executeSafely(
            call =
            {
                service.getTaskById(taskId)
            }
        )

    override suspend fun pinOrUnpinComment(
        taskId: String,
        eventId: String,
        isPinned: Boolean
    ): ApiResponse<PinnedCommentV2Response> =
        executeSafely(
            call =
            {
                service.pinOrUnpinComment(taskId, eventId, isPinned)
            }
        )


    override suspend fun syncTaskAndEvents(request: SocketReSyncUpdateV2Request): ApiResponse<AllTasksV2NewResponse.NewData> =
        executeSafely(
            call =
            {
                service.syncTaskAndEvents(request)
            }
        )


    override suspend fun getTaskFilesByTaskId(taskId: String): ApiResponse<AllTaskFilesV2Response> =
        executeSafely(
            call =
            {
                service.getTaskFilesByTaskId(taskId)
            }
        )


    override suspend fun approveOrRejectTask(
        approvalEvent: String,
        taskId: String,
        hasFiles: Boolean,
        comment: RequestBody,
        files: List<MultipartBody.Part>?,
        metadata: RequestBody
    ): ApiResponse<EventV2Response> {
        return executeSafely(call = {
            service.approveOrRejectTask(
                approvalEvent = approvalEvent,
                taskId = taskId,
                hasFiles = hasFiles,
                comment = comment,
                files = files,
                metadata = metadata
            )
        })
    }

    override suspend fun approveOrRejectTaskWithoutFiles(
        approvalEvent: String,
        taskId: String,
        hasFiles: Boolean,
        comment: RequestBody
    ): ApiResponse<EventV2Response> {

        return executeSafely(call = {
            service.approveOrRejectTaskWithoutFiles(
                approvalEvent = approvalEvent,
                taskId = taskId,
                hasFiles = hasFiles,
                comment = comment
            )
        })
    }

}