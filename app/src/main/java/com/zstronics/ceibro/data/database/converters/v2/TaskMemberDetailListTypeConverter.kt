package com.zstronics.ceibro.data.database.converters.v2

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zstronics.ceibro.data.database.models.projects.CeibroProjectV2
import com.zstronics.ceibro.data.database.models.tasks.TaskMemberDetail

class TaskMemberDetailListTypeConverter {

    @TypeConverter
    fun fromString(value: String): List<TaskMemberDetail>? {
        val type = object : TypeToken<List<TaskMemberDetail>?>() {}.type
        return Gson().fromJson(value, type)
    }

    @TypeConverter
    fun fromList(list: List<TaskMemberDetail>?): String {
        return Gson().toJson(list)
    }
}
