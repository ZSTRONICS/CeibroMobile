package com.zstronics.ceibro.data.remote

import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.repos.auth.signup.GenericResponse
import com.zstronics.ceibro.data.repos.task.models.*

interface ITaskRemoteDataSource {
    suspend fun tasks(state: String = "all", noPaginate: Boolean = true): ApiResponse<TasksResponse>
    suspend fun newTask(newTask: NewTaskRequest): ApiResponse<NewTaskResponse>
    suspend fun newTaskNoAdvanceOptions(newTask: NewTaskRequestNoAdvanceOptions): ApiResponse<NewTaskResponse>
    suspend fun updateTaskByIdNoAdvanceOptions(taskId: String, updateTask: UpdateDraftTaskRequestNoAdvanceOptions): ApiResponse<NewTaskResponse>
    suspend fun updateTaskNoStateNoAdvanceOptions(taskId: String, updateTask: UpdateTaskRequestNoAdvanceOptions): ApiResponse<NewTaskResponse>
    suspend fun deleteTask(taskId: String): ApiResponse<GenericResponse>

    suspend fun getAllTopics(): ApiResponse<TopicsResponse>
    suspend fun saveTopic(requestBody: NewTopicCreateRequest): ApiResponse<NewTopicResponse>
}