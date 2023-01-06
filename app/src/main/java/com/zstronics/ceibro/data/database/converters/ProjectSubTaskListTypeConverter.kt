package com.zstronics.ceibro.data.database.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zstronics.ceibro.data.database.models.tasks.ProjectSubTaskStatus

class ProjectSubTaskListTypeConverter {

    @TypeConverter
    fun fromString(value: String): List<ProjectSubTaskStatus> {
        val type = object : TypeToken<List<ProjectSubTaskStatus>>() {}.type
        return Gson().fromJson(value, type)
    }

    @TypeConverter
    fun fromList(list: List<ProjectSubTaskStatus>): String {
        return Gson().toJson(list)
    }
}
