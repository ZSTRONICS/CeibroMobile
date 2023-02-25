package com.zstronics.ceibro.data.local

import com.zstronics.ceibro.data.database.dao.SubTaskDao
import com.zstronics.ceibro.data.database.models.subtask.AllSubtask
import com.zstronics.ceibro.data.database.models.subtask.SubTaskComments
import javax.inject.Inject

class SubTaskLocalDataSource @Inject constructor(private val subTaskDao: SubTaskDao) :
    ISubTaskLocalDataSource {
    override suspend fun getSubTasks(): List<AllSubtask> = subTaskDao.getAllSubTasks()

    override suspend fun insertAllSubTasks(list: List<AllSubtask>) {
        subTaskDao.insertAllSubTasks(list)
    }

    override suspend fun eraseSubTaskTable() {
        subTaskDao.deleteAllSubTasks()
    }

    override suspend fun insertSubTask(subTask: AllSubtask) {
        subTaskDao.insertSubTask(subTask)
    }

    override suspend fun getSubTaskByTaskId(taskId: String): List<AllSubtask> =
        subTaskDao.getSubTaskByTaskId(taskId)


    override suspend fun getSingleSubTaskCount(subtaskId: String) : Int {
        return subTaskDao.getSingleSubTask(subtaskId)
    }

    override suspend fun updateSubTask(subTask: AllSubtask) {
        subTaskDao.updateSubTask(subTask)
    }

    override suspend fun deleteSubtaskById(subTaskId: String) {
        subTaskDao.deleteSubtaskById(subTaskId)
    }

    override suspend fun deleteSubtaskByTaskId(taskId: String) {
        subTaskDao.deleteSubtaskByTaskId(taskId)
    }
    override suspend fun addComment(subTaskId: String?, comment: SubTaskComments){
        subTaskDao.addComment(subTaskId,comment)
    }
}