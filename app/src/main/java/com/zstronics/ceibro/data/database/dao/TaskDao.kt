package com.zstronics.ceibro.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import androidx.room.Update
import com.zstronics.ceibro.data.database.models.tasks.CeibroTask

@Dao
interface TaskDao {
    @Insert(onConflict = REPLACE)
    suspend fun insertTask(task: CeibroTask)

    @Query("SELECT * FROM tasks")
    suspend fun getTasks(): List<CeibroTask>

    @Insert
    suspend fun insertAllTasks(list: List<CeibroTask>)

    @Query("DELETE FROM tasks")
    suspend fun deleteAllTasks()

    @Update
    suspend fun updateTask(task: CeibroTask)
}
