package com.zstronics.ceibro.data.database.dao

import androidx.room.*
import com.zstronics.ceibro.data.database.models.tasks.LocalTaskDetailFiles
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.AllCeibroConnections

@Dao
interface TaskDetailFilesV2Dao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(connectionData: LocalTaskDetailFiles)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllFiles(connectionsList: List<LocalTaskDetailFiles>)

    @Query("SELECT * FROM task_v2_files WHERE taskId = :taskId")
    suspend fun getAllFilesOfTask(taskId: String): List<LocalTaskDetailFiles>


    @Query("DELETE FROM task_v2_files")
    suspend fun deleteAll()
}
