package com.zstronics.ceibro.data.local

import com.zstronics.ceibro.data.database.dao.TaskDao
import com.zstronics.ceibro.data.database.models.tasks.CeibroTask
import com.zstronics.ceibro.data.database.models.tasks.TaskMember
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

    override suspend fun getSingleTaskCount(taskId: String): Int {
        return taskDao.getSingleTask(taskId)
    }

    override suspend fun getTaskById(taskId: String): CeibroTask {
        return taskDao.getTaskById(taskId)
    }

    override suspend fun updateTask(task: CeibroTask) {
        taskDao.updateTask(task)
    }

    override suspend fun deleteTaskById(taskId: String) {
        taskDao.deleteTaskById(taskId)
    }

    override suspend fun getFilteredTasks(
        projectId: String,
        selectedStatus: String,
        selectedDueDate: String,
        assigneeToMembers: List<TaskMember>?
    ): List<CeibroTask> {
        return taskDao.getFilteredTasks(
            selectedStatus = selectedStatus,
            selectedDueDate = selectedDueDate,
            assigneeToMembers = assigneeToMembers
        )
    }
}