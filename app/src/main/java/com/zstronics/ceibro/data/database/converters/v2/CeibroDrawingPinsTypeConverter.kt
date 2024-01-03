package com.zstronics.ceibro.data.database.converters.v2

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.zstronics.ceibro.data.database.models.tasks.CeibroDrawingPins

class CeibroDrawingPinsTypeConverter {

    @TypeConverter
    fun fromString(value: String): CeibroDrawingPins? {
        return Gson().fromJson(value, CeibroDrawingPins::class.java)
    }

    @TypeConverter
    fun fromAdvanceOptions(drawingPins: CeibroDrawingPins?): String {
        return Gson().toJson(drawingPins)
    }
}
