package com.zstronics.ceibro.data.local

import androidx.lifecycle.LiveData
import com.zstronics.ceibro.data.database.models.tasks.CeibroTask

interface ITaskLocalDataSource {
    suspend fun tasks(): List<CeibroTask>
    suspend fun insertAllTasks(list: List<CeibroTask>)
    suspend fun eraseTaskTable()
    suspend fun insertTask(task: CeibroTask)
    suspend fun updateTask(task: CeibroTask)
    suspend fun getSingleTaskCount(taskId: String): Int
    suspend fun getTaskById(taskId: String): CeibroTask
    suspend fun deleteTaskById(taskId: String)
}