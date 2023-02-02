package com.zstronics.ceibro.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.zstronics.ceibro.data.database.models.subtask.AllSubtask

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
}
