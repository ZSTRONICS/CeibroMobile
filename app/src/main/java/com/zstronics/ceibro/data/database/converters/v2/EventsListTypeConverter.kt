package com.zstronics.ceibro.data.database.converters.v2

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zstronics.ceibro.data.database.models.tasks.Events

class EventsListTypeConverter {

    @TypeConverter
    fun fromString(value: String): List<Events> {
        val type = object : TypeToken<List<Events>>() {}.type
        return Gson().fromJson(value, type)
    }

    @TypeConverter
    fun fromList(list: List<Events>): String {
        return Gson().toJson(list)
    }
}
