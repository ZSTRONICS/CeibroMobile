package com.zstronics.ceibro.data.database.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zstronics.ceibro.data.database.models.tasks.TaskMember

class TaskMemberListTypeConverter {

    @TypeConverter
    fun fromString(value: String): List<TaskMember>? {
        val type = object : TypeToken<List<TaskMember>>() {}.type
        return Gson().fromJson(value, type)
    }

    @TypeConverter
    fun fromList(list: List<TaskMember>?): String? {
        return Gson().toJson(list)
    }
}
