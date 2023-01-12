package com.zstronics.ceibro.data.repos.task

import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.database.models.tasks.CeibroTask
import com.zstronics.ceibro.data.local.TaskLocalDataSource
import com.zstronics.ceibro.data.remote.TaskRemoteDataSource
import com.zstronics.ceibro.data.repos.task.models.NewTaskRequest
import com.zstronics.ceibro.data.repos.task.models.NewTaskRequestNoAdvanceOptions
import javax.inject.Inject

class TaskRepository @Inject constructor(
    private val local: TaskLocalDataSource,
    private val remote: TaskRemoteDataSource
) : ITaskRepository {
    override suspend fun tasks(): List<CeibroTask> {
        val list = local.tasks()
        return if (list.isEmpty()) {
            /// Fetch from remote and save into local
            when (val response = remote.tasks()) {
                is ApiResponse.Success -> {
                    val tasks = response.data.allTasks
                    local.insertAllTasks(tasks)
                    return tasks
                }
                else -> emptyList()
            }
        } else {
            list
        }
    }

    override suspend fun eraseTaskTable() = local.eraseTaskTable()

    override suspend fun newTask(
        newTask: NewTaskRequest,
        callBack: (isSuccess: Boolean, message: String) -> Unit
    ) {
        when (val response = remote.newTask(newTask)) {
            is ApiResponse.Success -> {
                local.insertTask(response.data.newTask)
                callBack(true, "")
            }
            is ApiResponse.Error -> {
                callBack(true, response.error.message)
            }
        }
    }

    override suspend fun newTask(
        newTask: NewTaskRequestNoAdvanceOptions,
        callBack: (isSuccess: Boolean, message: String) -> Unit
    ) {
        when (val response = remote.newTask(newTask)) {
            is ApiResponse.Success -> {
                local.insertTask(response.data.newTask)
                callBack(true, "")
            }
            is ApiResponse.Error -> {
                callBack(false, response.error.message)
            }
        }
    }
}