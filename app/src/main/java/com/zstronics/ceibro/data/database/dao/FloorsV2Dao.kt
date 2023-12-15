package com.zstronics.ceibro.data.database.dao

import androidx.room.*
import com.zstronics.ceibro.data.database.models.projects.CeibroFloorV2

@Dao
interface FloorsV2Dao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFloor(floorData: CeibroFloorV2)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMultipleFloors(floorsData: List<CeibroFloorV2>)

    @Query("SELECT * FROM floors_v2 WHERE projectId = :projectId ORDER BY updatedAt DESC")
    suspend fun getAllProjectFloors(projectId: String): List<CeibroFloorV2>

//    @Query("SELECT * FROM projects_v2 WHERE isHiddenByMe = :isHiddenByMe ORDER BY updatedAt DESC")
//    suspend fun getAllHiddenProjects(isHiddenByMe: Boolean = true): List<CeibroProjectV2>
//
//    @Query("SELECT * FROM projects_v2 WHERE isHiddenByMe = :isHiddenByMe ORDER BY updatedAt DESC")
//    suspend fun getAllProjects(isHiddenByMe: Boolean = false): List<CeibroProjectV2>
//
//    @Query("SELECT * FROM projects_v2 WHERE isHiddenByMe = :isHiddenByMe AND isRecentlyUsedByMe = :isRecentlyUsedByMe ORDER BY updatedAt DESC")
//    suspend fun getAllRecentUsedProjects(isHiddenByMe: Boolean = false, isRecentlyUsedByMe: Boolean = true): List<CeibroProjectV2>
//
//    @Query("SELECT * FROM projects_v2 WHERE isHiddenByMe = :isHiddenByMe AND isFavoriteByMe = :isFavoriteByMe ORDER BY updatedAt DESC")
//    suspend fun getAllFavoriteProjects(isHiddenByMe: Boolean = false, isFavoriteByMe: Boolean = true): List<CeibroProjectV2>

    @Query("DELETE FROM floors_v2")
    suspend fun deleteAll()
}
