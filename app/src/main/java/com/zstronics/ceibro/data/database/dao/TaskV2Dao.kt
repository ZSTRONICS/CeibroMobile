package com.zstronics.ceibro.data.database.dao

import androidx.room.*
import com.zstronics.ceibro.data.repos.task.models.TasksV2DatabaseEntity
import com.zstronics.ceibro.data.repos.task.models.TasksV2DatabaseEntitySingle

@Dao
interface TaskV2Dao {
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertTaskData(task: TasksV2DatabaseEntity)
//
//    @Query("SELECT * FROM tasks_v2_internal WHERE rootState = :rootState")
//    suspend fun getTasks(rootState: String): TasksV2DatabaseEntity?

    @Query("DELETE FROM tasks_v2")
    suspend fun deleteAllData()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTaskDataWithState(task: TasksV2DatabaseEntitySingle)

    @Query("SELECT * FROM tasks_v2 WHERE rootState = :rootState AND subState = :subState")
    suspend fun getTasksByState(rootState: String, subState: String): TasksV2DatabaseEntitySingle?
}
