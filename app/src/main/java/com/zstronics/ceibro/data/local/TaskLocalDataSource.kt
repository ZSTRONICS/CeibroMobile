package com.zstronics.ceibro.data.local

import com.zstronics.ceibro.data.database.dao.TaskDao
import com.zstronics.ceibro.data.database.models.tasks.ProjectTask
import javax.inject.Inject

class TaskLocalDataSource @Inject constructor(private val taskDao: TaskDao) : ITaskLocalDataSource {
    override suspend fun tasks(): List<ProjectTask> = taskDao.getTasks()

    override suspend fun insertAllTasks(list: List<ProjectTask>) {
        taskDao.insertAllTasks(list)
    }
}