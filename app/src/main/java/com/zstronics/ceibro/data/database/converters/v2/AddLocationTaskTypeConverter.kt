package com.zstronics.ceibro.data.database.converters.v2

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.zstronics.ceibro.data.database.models.tasks.CeibroDrawingPins
import com.zstronics.ceibro.ui.locationv2.usage.AddLocationTask

class AddLocationTaskTypeConverter {

    @TypeConverter
    fun fromString(value: String): AddLocationTask? {
        return Gson().fromJson(value, AddLocationTask::class.java)
    }

    @TypeConverter
    fun fromAdvanceOptions(locationTask: AddLocationTask?): String {
        return Gson().toJson(locationTask)
    }
}
