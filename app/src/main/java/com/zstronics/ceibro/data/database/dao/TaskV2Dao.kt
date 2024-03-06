package com.zstronics.ceibro.data.database.dao

import androidx.room.*
import com.zstronics.ceibro.data.database.models.tasks.CeibroDrawingPins
import com.zstronics.ceibro.data.database.models.tasks.CeibroTaskV2
import com.zstronics.ceibro.data.database.models.tasks.Events

@Dao
interface TaskV2Dao {

    @Query("DELETE FROM tasks_v2_basic")
    suspend fun deleteAllTasksData()

    @Query("DELETE FROM tasks_v2_events")
    suspend fun deleteAllEventsData()


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

    @Query("SELECT * FROM tasks_v2_basic WHERE taskRootState = :taskRootState ORDER BY updatedAt DESC")
    suspend fun getRootAllTasks(taskRootState: String): List<CeibroTaskV2>

    @Query("SELECT * FROM tasks_v2_basic WHERE id = :taskId")
    suspend fun getTaskByID(taskId: String): CeibroTaskV2?

    @Query("DELETE FROM tasks_v2_basic WHERE id = :taskId")
    suspend fun deleteTaskByID(taskId: String)

    @Query("UPDATE tasks_v2_basic SET seenBy = :seenBy, updatedAt = :updatedAt WHERE id = :taskId")
    suspend fun updateTaskSeen(taskId: String, seenBy: List<String>, updatedAt: String)

    @Query("UPDATE tasks_v2_basic SET seenBy = :seenBy, hiddenBy = :hiddenBy, updatedAt = :updatedAt, toMeState = :toMeState, fromMeState = :fromMeState, hiddenState = :hiddenState, creatorState = :creatorState WHERE id = :taskId")
    suspend fun updateTaskOnEvent(taskId: String, seenBy: List<String>, hiddenBy: List<String>, updatedAt: String, toMeState: String, fromMeState: String, hiddenState: String, creatorState: String)

    @Query("UPDATE tasks_v2_basic SET isHiddenByMe = :isHiddenByMe, hiddenBy = :hiddenBy, updatedAt = :updatedAt, toMeState = :toMeState, fromMeState = :fromMeState, hiddenState = :hiddenState, pinData = :pinData WHERE id = :taskId")
    suspend fun updateTaskHideUnHide(taskId: String, isHiddenByMe: Boolean, hiddenBy: List<String>, updatedAt: String, toMeState: String, fromMeState: String, hiddenState: String, pinData: CeibroDrawingPins?, taskRootState: String, isCanceled: Boolean, isTaskInApproval: Boolean, userSubState: String)

    @Update
    suspend fun updateTask(task: CeibroTaskV2)

    @Query("SELECT * FROM tasks_v2_basic")
    suspend fun getAllTasks(): List<CeibroTaskV2>?

    @Query("UPDATE tasks_v2_basic SET isBeingDoneByAPI = :isBeingDone WHERE id = :taskId")
    suspend fun updateTaskIsBeingDoneByAPI(taskId: String, isBeingDone: Boolean)

    @Query("SELECT isBeingDoneByAPI FROM tasks_v2_basic WHERE id = :taskId")
    suspend fun getTaskIsBeingDoneByAPI(taskId: String): Boolean




    //Following functions are for events of tasks
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEventData(task: Events)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMultipleEvents(tasks: List<Events>)

    @Query("SELECT * FROM tasks_v2_events WHERE taskId = :taskId ORDER BY createdAt ASC")
    suspend fun getEventsOfTask(taskId: String): List<Events>

    @Query("SELECT * FROM tasks_v2_events WHERE taskId = :taskId AND isPinned = :isPinned ORDER BY updatedAt ASC")
    suspend fun getPinnedEventsOfTask(taskId: String, isPinned: Boolean): List<Events>

    @Query("SELECT * FROM tasks_v2_events WHERE taskId = :taskId AND id = :eventId")
    suspend fun getSingleEvent(taskId: String, eventId: String): Events?

    @Query("SELECT * FROM tasks_v2_events")
    suspend fun getAllEvents(): List<Events>?
}
