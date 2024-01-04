package com.zstronics.ceibro.data.database.dao

import androidx.room.*
import com.zstronics.ceibro.data.database.models.projects.CeibroDownloadDrawingV2

@Dao
interface DownloadedDrawingV2Dao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownloadDrawing(drawingData: CeibroDownloadDrawingV2)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMultipleDownloadDrawing(drawingData: List<CeibroDownloadDrawingV2>)

    @Query("SELECT * FROM downloaded_drawing_v2 WHERE downloadId = :downloadId")
    suspend fun getDownloadedDrawingByDownloadId(downloadId: Long): CeibroDownloadDrawingV2?

    @Query("SELECT * FROM downloaded_drawing_v2 WHERE groupId = :groupId")
    suspend fun getDownloadedDrawingByGroupId(groupId: String): CeibroDownloadDrawingV2?

    @Query("SELECT * FROM downloaded_drawing_v2 WHERE drawingId = :drawingId")
    suspend fun getDownloadedDrawingByDrawingId(drawingId: String): CeibroDownloadDrawingV2?

    @Query("DELETE FROM downloaded_drawing_v2 WHERE drawingId = :drawingId")
    suspend fun deleteByDrawingID(drawingId: String)

    @Query("DELETE FROM downloaded_drawing_v2")
    suspend fun deleteAll()
}
