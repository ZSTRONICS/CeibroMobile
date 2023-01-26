package com.zstronics.ceibro.data.database.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zstronics.ceibro.data.database.models.subtask.Viewer

class ViewerListTypeConverter {

    @TypeConverter
    fun fromString(value: String): List<Viewer> {
        val type = object : TypeToken<List<Viewer>>() {}.type
        return Gson().fromJson(value, type)
    }

    @TypeConverter
    fun fromList(list: List<Viewer>): String {
        return Gson().toJson(list)
    }
}
