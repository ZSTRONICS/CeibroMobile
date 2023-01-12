package com.zstronics.ceibro.data.local

import com.zstronics.ceibro.data.database.models.tasks.CeibroTask

interface ITaskLocalDataSource {
    suspend fun tasks(): List<CeibroTask>
    suspend fun insertAllTasks(list: List<CeibroTask>)
    suspend fun eraseTaskTable()
    suspend fun insertTask(task: CeibroTask)
}