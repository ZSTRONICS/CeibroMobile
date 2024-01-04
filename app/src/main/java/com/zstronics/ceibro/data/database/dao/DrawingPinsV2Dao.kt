package com.zstronics.ceibro.data.database.dao

import androidx.room.*
import com.zstronics.ceibro.data.database.models.projects.CeibroGroupsV2
import com.zstronics.ceibro.data.database.models.tasks.CeibroDrawingPins

@Dao
interface DrawingPinsV2Dao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSinglePinData(drawingPins: CeibroDrawingPins)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMultiplePins(drawingPins: List<CeibroDrawingPins>)

    @Query("SELECT * FROM drawing_pins_v2 WHERE drawingId = :drawingId")
    suspend fun getAllDrawingPins(drawingId: String): List<CeibroDrawingPins>

//
//    @Query("SELECT * FROM groups_v2 WHERE _id = :groupId")
//    suspend fun getGroupByGroupId(groupId: String): CeibroGroupsV2?

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

//    @Query("DELETE FROM groups_v2 WHERE _id = :groupId")
//    suspend fun deleteGroupById(groupId: String)

    @Query("DELETE FROM drawing_pins_v2")
    suspend fun deleteAll()
}
