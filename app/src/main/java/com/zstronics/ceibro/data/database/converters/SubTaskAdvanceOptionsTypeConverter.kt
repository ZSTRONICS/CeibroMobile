package com.zstronics.ceibro.data.database.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.zstronics.ceibro.data.database.models.subtask.SubTaskAdvanceOptions
import com.zstronics.ceibro.data.database.models.tasks.AdvanceOptions

class SubTaskAdvanceOptionsTypeConverter {

    @TypeConverter
    fun fromString(value: String): SubTaskAdvanceOptions {
        return Gson().fromJson(value, SubTaskAdvanceOptions::class.java)
    }

    @TypeConverter
    fun fromAdvanceOptions(subTaskAdvanceOptions: SubTaskAdvanceOptions): String {
        return Gson().toJson(subTaskAdvanceOptions)
    }
}
