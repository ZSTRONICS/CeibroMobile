package com.zstronics.ceibro.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.zstronics.ceibro.data.database.models.tasks.CeibroTask
import com.zstronics.ceibro.data.database.models.tasks.TaskMember
import com.zstronics.ceibro.data.repos.chat.room.Member

@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: CeibroTask)

    @Query("SELECT * FROM tasks")
    suspend fun getTasks(): List<CeibroTask>

    @Query("SELECT * FROM tasks WHERE _id = :taskId")
    suspend fun getTaskById(taskId: String): CeibroTask

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTasks(list: List<CeibroTask>)

    @Query("DELETE FROM tasks")
    suspend fun deleteAllTasks()

    @Query("SELECT COUNT(*) FROM tasks WHERE _id = :taskId")
    suspend fun getSingleTask(taskId: String): Int

    @Update
    suspend fun updateTask(task: CeibroTask)

    @Query("DELETE FROM tasks WHERE _id = :taskId")
    suspend fun deleteTaskById(taskId: String)

    @Query("SELECT * FROM tasks WHERE (:selectedStatus = '' OR subTaskStatusCount = :selectedStatus) AND (:selectedDueDate = '' OR dueDate = :selectedDueDate) AND (:assigneeToMembers IS NULL OR assignedTo IN (:assigneeToMembers))")
    suspend fun getFilteredTasks(
        selectedStatus: String = "",
        selectedDueDate: String = "",
        assigneeToMembers: List<TaskMember>? = null
    ): List<CeibroTask>
}
