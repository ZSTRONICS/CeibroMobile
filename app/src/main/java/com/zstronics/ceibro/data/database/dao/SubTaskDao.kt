package com.zstronics.ceibro.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.zstronics.ceibro.data.database.models.subtask.AllSubtask

@Dao
interface SubTaskDao {
    @Insert
    suspend fun insertSubTask(subTask: AllSubtask)

    @Query("SELECT * FROM sub_tasks")
    suspend fun getAllSubTasks(): List<AllSubtask>

    @Insert
    suspend fun insertAllSubTasks(list: List<AllSubtask>)

    @Query("DELETE FROM sub_tasks")
    suspend fun deleteAllSubTasks()
}
