package com.zstronics.ceibro.data.local

import com.zstronics.ceibro.data.database.models.tasks.ProjectTask

interface ITaskLocalDataSource {
    suspend fun tasks(): List<ProjectTask>
    suspend fun insertAllTasks(list: List<ProjectTask>)
}