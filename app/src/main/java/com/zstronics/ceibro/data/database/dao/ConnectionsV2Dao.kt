package com.zstronics.ceibro.data.database.dao

import androidx.paging.PagingSource
import androidx.room.*
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.AllCeibroConnections
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.ConnectionsV2DatabaseEntity

@Dao
interface ConnectionsV2Dao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(connectionData: AllCeibroConnections.CeibroConnection)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(connectionsList: List<AllCeibroConnections.CeibroConnection>)

    @Query("SELECT * FROM connections_v2")
    suspend fun getAll(): List<AllCeibroConnections.CeibroConnection>

//    @Query("SELECT * FROM connections_v2")
//    fun getPagedConnections(): PagingSource<Int, AllCeibroConnections.CeibroConnection>

    @Query("DELETE FROM connections_v2")
    suspend fun deleteAll()
}
