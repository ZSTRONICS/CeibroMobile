package com.zstronics.ceibro.data.local

import com.zstronics.ceibro.data.database.models.subtask.AllSubtask

interface ISubTaskLocalDataSource {
    suspend fun getSubTasks(): List<AllSubtask>
    suspend fun insertAllSubTasks(list: List<AllSubtask>)
    suspend fun eraseSubTaskTable()
    suspend fun insertSubTask(subTask: AllSubtask)
    suspend fun getSubTaskByTaskId(taskId: String): List<AllSubtask>
}