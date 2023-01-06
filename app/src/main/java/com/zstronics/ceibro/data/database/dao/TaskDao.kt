package com.zstronics.ceibro.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.zstronics.ceibro.data.database.models.tasks.CeibroTask

@Dao
interface TaskDao {
    @Insert
    suspend fun insertTask(task: CeibroTask)

    @Query("SELECT * FROM tasks")
    suspend fun getTasks(): List<CeibroTask>

    @Insert
    suspend fun insertAllTasks(list: List<CeibroTask>)

    @Query("DELETE FROM tasks")
    suspend fun deleteAllTasks()
}
