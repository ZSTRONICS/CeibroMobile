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
import com.zstronics.ceibro.data.repos.task.models.v2.AllTasksV2Response
import com.zstronics.ceibro.data.repos.task.models.v2.EventV2Response
import com.zstronics.ceibro.data.repos.task.models.v2.ForwardTaskV2Request
import com.zstronics.ceibro.data.repos.task.models.v2.HideTaskResponse
import com.zstronics.ceibro.data.repos.task.models.v2.NewTaskV2Entity
import com.zstronics.ceibro.data.repos.task.models.v2.NewTaskV2Response
import com.zstronics.ceibro.data.repos.task.models.v2.TaskSeenResponse
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

    override suspend fun forwardTask(taskId: String, forwardTaskV2Request: ForwardTaskV2Request): ApiResponse<NewTaskV2Response> =
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

    override suspend fun updateTaskNoStateNoAdvanceOptions(taskId:String, updateTask: UpdateTaskRequestNoAdvanceOptions): ApiResponse<NewTaskResponse> =
        executeSafely(
            call =
            {
                service.updateTaskNoStateNoAdvanceOptions(taskId, updateTask)
            }
        )

    override suspend fun deleteTask(taskId:String): ApiResponse<GenericResponse> =
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

    override suspend fun cancelTask(taskId: String): ApiResponse<EventV2Response> =
        executeSafely(
            call =
            {
                service.cancelTask(taskId)
            }
        )

    override  suspend fun unCancelTask(taskId: String): ApiResponse<EventV2Response> =
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

}