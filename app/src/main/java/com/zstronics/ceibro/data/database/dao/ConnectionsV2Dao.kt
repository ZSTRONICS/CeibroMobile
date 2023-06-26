package com.zstronics.ceibro.data.database.dao

import androidx.room.*
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.ConnectionsV2DatabaseEntity

@Dao
interface ConnectionsV2Dao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(connectionsData: ConnectionsV2DatabaseEntity)

    @Query("SELECT * FROM connections_v2")
    suspend fun getAll(): ConnectionsV2DatabaseEntity?

    @Query("DELETE FROM connections_v2")
    suspend fun deleteAll()
}
