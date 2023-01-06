package com.zstronics.ceibro.data.database.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zstronics.ceibro.data.database.models.tasks.ProjectSubTask

class ProjectSubTaskListTypeConverter {

    @TypeConverter
    fun fromString(value: String): List<ProjectSubTask> {
        val type = object : TypeToken<List<ProjectSubTask>>() {}.type
        return Gson().fromJson(value, type)
    }

    @TypeConverter
    fun fromList(list: List<ProjectSubTask>): String {
        return Gson().toJson(list)
    }
}
