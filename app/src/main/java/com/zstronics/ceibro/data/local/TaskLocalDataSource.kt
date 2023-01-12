package com.zstronics.ceibro.data.local

import com.zstronics.ceibro.data.database.dao.TaskDao
import com.zstronics.ceibro.data.database.models.tasks.CeibroTask
import javax.inject.Inject

class TaskLocalDataSource @Inject constructor(private val taskDao: TaskDao) : ITaskLocalDataSource {
    override suspend fun tasks(): List<CeibroTask> = taskDao.getTasks()

    override suspend fun insertAllTasks(list: List<CeibroTask>) {
        taskDao.insertAllTasks(list)
    }

    override suspend fun eraseTaskTable() {
        taskDao.deleteAllTasks()
    }

    override suspend fun insertTask(task: CeibroTask) {
        taskDao.insertTask(task)
    }
}