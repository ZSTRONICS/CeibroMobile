package com.zstronics.ceibro.data.database.converters.v2

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zstronics.ceibro.data.database.models.inbox.ActionFilesData
import com.zstronics.ceibro.data.repos.task.models.v2.LocalFilesToStore

class ActionFilesDataListTypeConverter {

    @TypeConverter
    fun fromString(value: String): List<ActionFilesData>? {
        val type = object : TypeToken<List<ActionFilesData>>() {}.type
        return Gson().fromJson(value, type)
    }

    @TypeConverter
    fun fromList(list: List<ActionFilesData>?): String? {
        return Gson().toJson(list)
    }
}
