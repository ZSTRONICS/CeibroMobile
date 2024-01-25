package com.zstronics.ceibro.data.database.dao

import androidx.room.*
import com.zstronics.ceibro.data.database.models.inbox.CeibroInboxV2
import com.zstronics.ceibro.data.database.models.projects.CeibroGroupsV2

@Dao
interface InboxV2Dao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInboxItem(inboxData: CeibroInboxV2)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMultipleInboxItem(inboxData: List<CeibroInboxV2>)

    @Query("SELECT * FROM inbox_v2 ORDER BY createdAt DESC")
    suspend fun getAllInboxItems(): List<CeibroInboxV2>

    @Query("SELECT * FROM inbox_v2 WHERE taskId = :taskId")
    suspend fun getInboxTaskData(taskId: String): CeibroInboxV2?


    @Query("DELETE FROM inbox_v2")
    suspend fun deleteAll()
}
