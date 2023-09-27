package com.zstronics.ceibro.data.database.dao

import androidx.room.*
import com.zstronics.ceibro.data.repos.task.models.v2.NewTaskV2Entity

@Dao
interface DraftNewTaskV2Dao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(task: NewTaskV2Entity)

    @Query("SELECT * FROM draft_new_task_v2")
    suspend fun getUnSyncedRecords(): List<NewTaskV2Entity>?

    @Query("DELETE FROM draft_new_task_v2 WHERE task_id = :taskId")
    suspend fun deleteTaskById(taskId: Int)

    @Query("DELETE FROM draft_new_task_v2")
    suspend fun deleteAllData()
}
