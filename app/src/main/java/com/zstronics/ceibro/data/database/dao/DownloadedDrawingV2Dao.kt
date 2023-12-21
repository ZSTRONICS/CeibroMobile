package com.zstronics.ceibro.data.database.dao

import androidx.room.*
import com.zstronics.ceibro.data.database.models.projects.CeibroDownloadDrawingV2
import com.zstronics.ceibro.data.database.models.projects.CeibroFloorV2

@Dao
interface DownloadedDrawingV2Dao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownloadDrawing(drawingData: CeibroDownloadDrawingV2)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMultipleDownloadDrawing(drawingData: List<CeibroDownloadDrawingV2>)

        @Query("SELECT * FROM downloaded_drawing_v2 WHERE downloadId = :downloadId")
    suspend fun getDownloadedDrawingByDownloadId(downloadId: Long): CeibroDownloadDrawingV2?

//    @Query("SELECT * FROM floors_v2 WHERE projectId = :projectId ORDER BY updatedAt DESC")
//    suspend fun getAllProjectFloors(projectId: String): List<CeibroFloorV2>
//
//    @Query("SELECT * FROM floors_v2 WHERE _id = :floorId")
//    suspend fun getFloorByFloorId(floorId: String): CeibroFloorV2

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

    @Query("DELETE FROM downloaded_drawing_v2")
    suspend fun deleteAll()
}
