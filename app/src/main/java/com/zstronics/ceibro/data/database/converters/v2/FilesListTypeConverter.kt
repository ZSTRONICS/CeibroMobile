package com.zstronics.ceibro.data.database.converters.v2

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zstronics.ceibro.data.database.models.tasks.Files

class FilesListTypeConverter {

    @TypeConverter
    fun fromString(value: String): List<Files> {
        val type = object : TypeToken<List<Files>>() {}.type
        return Gson().fromJson(value, type)
    }

    @TypeConverter
    fun fromList(list: List<Files>): String {
        return Gson().toJson(list)
    }
}
