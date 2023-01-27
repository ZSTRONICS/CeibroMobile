package com.zstronics.ceibro.data.repos.task

import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.database.models.subtask.AllSubtask
import com.zstronics.ceibro.data.database.models.tasks.CeibroTask
import com.zstronics.ceibro.data.local.SubTaskLocalDataSource
import com.zstronics.ceibro.data.local.TaskLocalDataSource
import com.zstronics.ceibro.data.remote.SubTaskRemoteDataSource
import com.zstronics.ceibro.data.remote.TaskRemoteDataSource
import com.zstronics.ceibro.data.repos.task.models.NewSubtaskRequest
import com.zstronics.ceibro.data.repos.task.models.NewTaskRequest
import com.zstronics.ceibro.data.repos.task.models.NewTaskRequestNoAdvanceOptions
import javax.inject.Inject

class TaskRepository @Inject constructor(
    private val localTask: TaskLocalDataSource,
    private val remoteTask: TaskRemoteDataSource,
    private val localSubTask: SubTaskLocalDataSource,
    private val remoteSubTask: SubTaskRemoteDataSource
) : ITaskRepository {

    /// Following calls are for TODO - Task

    override suspend fun tasks(): List<CeibroTask> {
        val list = localTask.tasks()
        return if (list.isEmpty()) {
            /// Fetch from remote and save into local
            when (val response = remoteTask.tasks()) {
                is ApiResponse.Success -> {
                    val tasks = response.data.allTasks
                    localTask.insertAllTasks(tasks)
                    return tasks
                }
                else -> emptyList()
            }
        } else {
            list
        }
    }

    override suspend fun eraseTaskTable() = localTask.eraseTaskTable()

    override suspend fun newTask(
        newTask: NewTaskRequest,
        callBack: (isSuccess: Boolean, message: String) -> Unit
    ) {
        when (val response = remoteTask.newTask(newTask)) {
            is ApiResponse.Success -> {
                localTask.insertTask(response.data.newTask)
                callBack(true, "")
            }
            is ApiResponse.Error -> {
                callBack(false, response.error.message)
            }
        }
    }

    override suspend fun newTaskNoAdvanceOptions(
        newTask: NewTaskRequestNoAdvanceOptions,
        callBack: (isSuccess: Boolean, message: String) -> Unit
    ) {
        when (val response = remoteTask.newTaskNoAdvanceOptions(newTask)) {
            is ApiResponse.Success -> {
                localTask.insertTask(response.data.newTask)
                callBack(true, "")
            }
            is ApiResponse.Error -> {
                callBack(false, response.error.message)
            }
        }
    }

    /// Following calls are for Sub-Task
    override suspend fun getAllSubtasks(): List<AllSubtask> {
        val list = localSubTask.getSubTasks()
        return if (list.isEmpty()) {
            when (val response = remoteSubTask.getAllSubTasksForUser("all")) {
                is ApiResponse.Success -> {
                    val subTasks = response.data.allSubtasks
                    localSubTask.insertAllSubTasks(subTasks)
                    return subTasks
                }
                else -> emptyList()
            }
        } else {
            list
        }
    }

    override suspend fun getSubTaskByTaskId(taskId: String): List<AllSubtask> {
        val list = localSubTask.getSubTaskByTaskId(taskId)
        return if (list.isEmpty()) {
            when (val response = remoteSubTask.getSubTaskByTaskId(taskId)) {
                is ApiResponse.Success -> {
                    val subTasks = response.data.results.subtasks
                    localSubTask.insertAllSubTasks(subTasks)
                    return subTasks
                }
                else -> emptyList()
            }
        } else {
            list
        }
    }

    override suspend fun newSubTask(
        newTask: NewSubtaskRequest,
        callBack: (isSuccess: Boolean, message: String) -> Unit
    ) {
        when (val response = remoteSubTask.newSubTask(newTask)) {
            is ApiResponse.Success -> {
                localSubTask.insertSubTask(response.data.newSubtask)
                callBack(true, "")
            }
            is ApiResponse.Error -> {
                callBack(false, response.error.message)
            }
        }
    }

    override suspend fun eraseSubTaskTable() = localSubTask.eraseSubTaskTable()

    private suspend fun syncSubTask() {
        when (val response = remoteSubTask.getAllSubTasksForUser("all")) {
            is ApiResponse.Success -> {
                val subTasks = response.data.allSubtasks
                localSubTask.insertAllSubTasks(subTasks)
            }
        }
    }

    private suspend fun syncTask() {
        when (val response = remoteTask.tasks()) {
            is ApiResponse.Success -> {
                val tasks = response.data.allTasks
                localTask.insertAllTasks(tasks)
            }
        }
    }

    override suspend fun syncTasksAndSubTasks() {
        syncSubTask()
        syncTask()
    }
}