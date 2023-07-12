package com.zstronics.ceibro.data.database.converters.v2

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zstronics.ceibro.data.database.models.tasks.EventFiles

class EventFilesListTypeConverter {

    @TypeConverter
    fun fromString(value: String): List<EventFiles> {
        val type = object : TypeToken<List<EventFiles>>() {}.type
        return Gson().fromJson(value, type)
    }

    @TypeConverter
    fun fromList(list: List<EventFiles>): String {
        return Gson().toJson(list)
    }
}
