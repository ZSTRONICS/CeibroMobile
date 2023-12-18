package com.zstronics.ceibro.data.database.converters.v2

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zstronics.ceibro.data.database.models.projects.DrawingV2

class DrawingsV2ListTypeConverter {

    @TypeConverter
    fun fromString(value: String): List<DrawingV2>? {
        val type = object : TypeToken<List<DrawingV2>?>() {}.type
        return Gson().fromJson(value, type)
    }

    @TypeConverter
    fun fromList(list: List<DrawingV2>?): String {
        return Gson().toJson(list)
    }
}
