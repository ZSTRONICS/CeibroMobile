package com.zstronics.ceibro.data.repos.tasks

import com.zstronics.ceibro.data.database.dao.TaskDao
import com.zstronics.ceibro.data.database.models.tasks.ProjectTask
import javax.inject.Inject

class TaskRepository @Inject constructor(private val taskDao: TaskDao) {
    suspend fun tasks(): List<ProjectTask> = taskDao.getTasks()
    suspend fun insertTask(task: ProjectTask) = taskDao.insertTask(task)
}
