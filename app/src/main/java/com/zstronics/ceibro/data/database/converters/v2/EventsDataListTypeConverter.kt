package com.zstronics.ceibro.data.database.converters.v2

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zstronics.ceibro.data.database.models.tasks.EventData
import com.zstronics.ceibro.data.database.models.tasks.Events
import com.zstronics.ceibro.data.database.models.tasks.Files

class EventsDataListTypeConverter {

    @TypeConverter
    fun fromString(value: String): List<EventData>? {
        val type = object : TypeToken<List<EventData>>() {}.type
        return Gson().fromJson(value, type)
    }

    @TypeConverter
    fun fromList(list: List<EventData>?): String {
        return Gson().toJson(list)
    }
}
