package com.zstronics.ceibro.data.database.dao

import androidx.room.*
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.AllCeibroConnections
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.CeibroConnectionGroupV2

@Dao
interface ConnectionGroupV2Dao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConnectionGroup(connectionData: CeibroConnectionGroupV2)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMultipleConnectionGroup(connectionsList: List<CeibroConnectionGroupV2>)


    @Query("DELETE FROM connection_group_v2")
    suspend fun deleteAll()
}
