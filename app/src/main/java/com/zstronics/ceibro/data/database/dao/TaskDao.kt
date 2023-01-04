package com.zstronics.ceibro.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.zstronics.ceibro.data.database.models.tasks.ProjectTask

@Dao
interface TaskDao {
    @Insert
    suspend fun insertTask(task: ProjectTask)

    @Query("SELECT * FROM tasks")
    suspend fun getTasks(): List<ProjectTask>
}
