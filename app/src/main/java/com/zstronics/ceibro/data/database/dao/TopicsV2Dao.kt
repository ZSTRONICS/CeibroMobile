package com.zstronics.ceibro.data.database.dao

import androidx.room.*
import com.zstronics.ceibro.data.repos.task.models.TopicsResponse
import com.zstronics.ceibro.data.repos.task.models.TopicsV2DatabaseEntity

@Dao
interface TopicsV2Dao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopicData(task: TopicsV2DatabaseEntity)

    @Query("SELECT * FROM topics_v2")
    suspend fun getTopicsData(): TopicsV2DatabaseEntity?

    @Query("SELECT topicsData FROM topics_v2 WHERE id = :topicId")
    fun getTopicNameById(topicId: String): String?

    @Query("DELETE FROM topics_v2")
    suspend fun deleteAllData()
}
