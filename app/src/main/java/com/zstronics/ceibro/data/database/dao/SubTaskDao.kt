package com.zstronics.ceibro.data.database.dao

import androidx.room.*
import com.zstronics.ceibro.data.database.TableNames
import com.zstronics.ceibro.data.database.models.subtask.AllSubtask
import com.zstronics.ceibro.data.database.models.subtask.SubTaskComments

@Dao
interface SubTaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubTask(subTask: AllSubtask)

    @Query("SELECT * FROM sub_tasks")
    suspend fun getAllSubTasks(): List<AllSubtask>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllSubTasks(list: List<AllSubtask>)

    @Query("DELETE FROM sub_tasks")
    suspend fun deleteAllSubTasks()

    @Query("SELECT * FROM sub_tasks WHERE taskId = :taskId")
    suspend fun getSubTaskByTaskId(taskId: String): List<AllSubtask>

    @Query("SELECT COUNT(*) FROM sub_tasks WHERE id = :subTaskId")
    suspend fun getSingleSubTask(subTaskId: String): Int

    @Update
    suspend fun updateSubTask(subTask: AllSubtask)

    @Query("DELETE FROM sub_tasks WHERE id = :subTaskId")
    suspend fun deleteSubtaskById(subTaskId: String)

    @Query("DELETE FROM sub_tasks WHERE taskId = :taskId")
    suspend fun deleteSubtaskByTaskId(taskId: String)

    @Query("SELECT * FROM ${TableNames.SubTasks} WHERE id = :subTaskId")
    suspend fun getSubTaskById(subTaskId: String): AllSubtask?

    // Add a new comment to the recentComments list
    suspend fun addComment(subTaskId: String?, comment: SubTaskComments) {
        val subtask = subTaskId?.let { getSubTaskById(it) }
        val comments = subtask?.recentComments
        comments?.add(comment)
        subtask?.recentComments = comments
        subtask?.let { updateSubTask(it) }
    }
}
