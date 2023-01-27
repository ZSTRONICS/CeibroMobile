package com.zstronics.ceibro.data.database.dao

import androidx.room.Dao
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

    @Query("SELECT * FROM sub_tasks where taskId = :taskId")
    suspend fun getSubTaskByTaskId(taskId: String): List<AllSubtask>

    @Update
    suspend fun updateSubTask(subTask: AllSubtask)
}
