package com.zstronics.ceibro.data.database.converters.v2

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zstronics.ceibro.data.database.models.tasks.TaskFiles

class TaskFilesListTypeConverter {

    @TypeConverter
    fun fromString(value: String): List<TaskFiles> {
        val type = object : TypeToken<List<TaskFiles>>() {}.type
        return Gson().fromJson(value, type)
    }

    @TypeConverter
    fun fromList(list: List<TaskFiles>): String {
        return Gson().toJson(list)
    }
}
