package com.zstronics.ceibro.data.database.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zstronics.ceibro.data.database.models.subtask.AssignedTo

class AssignToListTypeConverter {

    @TypeConverter
    fun fromString(value: String): List<AssignedTo> {
        val type = object : TypeToken<List<AssignedTo>>() {}.type
        return Gson().fromJson(value, type)
    }

    @TypeConverter
    fun fromList(list: List<AssignedTo>): String {
        return Gson().toJson(list)
    }
}
