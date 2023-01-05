package com.zstronics.ceibro.data.repos.task

import com.zstronics.ceibro.data.database.models.tasks.ProjectTask

interface ITaskRepository {
    suspend fun tasks(): List<ProjectTask>
}