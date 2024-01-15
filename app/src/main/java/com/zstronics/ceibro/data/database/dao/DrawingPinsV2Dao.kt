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


    @Query("DELETE FROM drawing_pins_v2")
    suspend fun deleteAll()
}
