package com.zstronics.ceibro.data.database.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zstronics.ceibro.data.database.models.subtask.SubTaskStateItem
import com.zstronics.ceibro.data.database.models.subtask.Viewer

class SubTaskStateListTypeConverter {

    @TypeConverter
    fun fromString(value: String): List<SubTaskStateItem>? {
        val type = object : TypeToken<List<SubTaskStateItem>>() {}.type
        return Gson().fromJson(value, type)
    }

    @TypeConverter
    fun fromList(list: List<SubTaskStateItem>?): String? {
        return Gson().toJson(list)
    }
}
