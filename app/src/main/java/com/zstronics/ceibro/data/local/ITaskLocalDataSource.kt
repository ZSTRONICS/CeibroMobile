package com.zstronics.ceibro.data.local

import com.zstronics.ceibro.data.database.models.tasks.CeibroTask
import com.zstronics.ceibro.data.database.models.tasks.TaskMember

interface ITaskLocalDataSource {
    suspend fun tasks(): List<CeibroTask>
    suspend fun insertAllTasks(list: List<CeibroTask>)
    suspend fun eraseTaskTable()
    suspend fun insertTask(task: CeibroTask)
    suspend fun updateTask(task: CeibroTask)
    suspend fun getSingleTaskCount(taskId: String): Int
    suspend fun getTaskById(taskId: String): CeibroTask
    suspend fun deleteTaskById(taskId: String)

    suspend fun getFilteredTasks(
        projectId: String = "",
        selectedStatus: String = "",
        selectedDueDate: String = "",
        assigneeToMembers: List<TaskMember>? = null
    ): List<CeibroTask>
}