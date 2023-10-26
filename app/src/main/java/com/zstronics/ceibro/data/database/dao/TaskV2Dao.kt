package com.zstronics.ceibro.data.database.dao

import androidx.room.*
import com.zstronics.ceibro.data.database.models.tasks.CeibroTaskV2
import com.zstronics.ceibro.data.database.models.tasks.Events
import com.zstronics.ceibro.data.repos.task.models.TasksV2DatabaseEntitySingle

@Dao
interface TaskV2Dao {
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertTaskData(task: TasksV2DatabaseEntity)
//
//    @Query("SELECT * FROM tasks_v2_internal WHERE rootState = :rootState")
//    suspend fun getTasks(rootState: String): TasksV2DatabaseEntity?

    @Query("DELETE FROM tasks_v2_basic")
    suspend fun deleteAllTasksData()

    @Query("DELETE FROM tasks_v2_events")
    suspend fun deleteAllEventsData()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTaskDataWithState(task: TasksV2DatabaseEntitySingle)

    @Query("SELECT * FROM tasks_v2 WHERE rootState = :rootState AND subState = :subState")
    suspend fun getTasksByState(rootState: String, subState: String): TasksV2DatabaseEntitySingle?



    //Following functions are for tasks
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTaskData(task: CeibroTaskV2)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMultipleTasks(tasks: List<CeibroTaskV2>)

    @Query("SELECT * FROM tasks_v2_basic WHERE toMeState = :toMeState ORDER BY updatedAt DESC")
    suspend fun getToMeTasks(toMeState: String): List<CeibroTaskV2>

    @Query("SELECT * FROM tasks_v2_basic WHERE fromMeState = :fromMeState ORDER BY updatedAt DESC")
    suspend fun getFromMeTasks(fromMeState: String): List<CeibroTaskV2>

    @Query("SELECT * FROM tasks_v2_basic WHERE hiddenState = :hiddenState ORDER BY updatedAt DESC")
    suspend fun getHiddenTasks(hiddenState: String): List<CeibroTaskV2>




    //Following functions are for events of tasks
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEventData(task: Events)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMultipleEvents(tasks: List<Events>)

    @Query("SELECT * FROM tasks_v2_events WHERE taskId = :taskId ORDER BY updatedAt DESC")
    suspend fun getEventsOfTask(taskId: String): List<Events>
}
