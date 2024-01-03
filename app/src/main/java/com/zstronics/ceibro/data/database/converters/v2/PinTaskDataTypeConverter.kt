package com.zstronics.ceibro.data.database.converters.v2

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.zstronics.ceibro.data.database.models.tasks.AdvanceOptions
import com.zstronics.ceibro.data.database.models.tasks.CeibroDrawingPins

class PinTaskDataTypeConverter {

    @TypeConverter
    fun fromString(value: String): CeibroDrawingPins.PinTaskData? {
        return Gson().fromJson(value, CeibroDrawingPins.PinTaskData::class.java)
    }

    @TypeConverter
    fun fromAdvanceOptions(drawingPins: CeibroDrawingPins.PinTaskData?): String {
        return Gson().toJson(drawingPins)
    }
}
