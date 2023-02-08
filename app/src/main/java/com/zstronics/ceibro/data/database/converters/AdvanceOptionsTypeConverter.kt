package com.zstronics.ceibro.data.database.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.zstronics.ceibro.data.database.models.tasks.AdvanceOptions

class AdvanceOptionsTypeConverter {

    @TypeConverter
    fun fromString(value: String): AdvanceOptions? {
        return Gson().fromJson(value, AdvanceOptions::class.java)
    }

    @TypeConverter
    fun fromAdvanceOptions(advanceOptions: AdvanceOptions?): String {
        return Gson().toJson(advanceOptions)
    }
}
