package com.zstronics.ceibro.data.repos.task

import com.zstronics.ceibro.data.database.models.tasks.CeibroTask

interface ITaskRepository {
    suspend fun tasks(): List<CeibroTask>
    suspend fun eraseTaskTable()
}