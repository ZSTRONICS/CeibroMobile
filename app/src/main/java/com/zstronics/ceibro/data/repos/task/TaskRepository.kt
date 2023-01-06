package com.zstronics.ceibro.data.repos.task

import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.database.models.tasks.CeibroTask
import com.zstronics.ceibro.data.local.TaskLocalDataSource
import com.zstronics.ceibro.data.remote.TaskRemoteDataSource
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

}