package com.zstronics.ceibro.data.database.converters.v2

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.zstronics.ceibro.data.repos.task.models.TaskV2Response

class CeibroTaskV2TypeConverter {

    @TypeConverter
    fun fromString(value: String): TaskV2Response.AllTasks {
        return Gson().fromJson(value, TaskV2Response.AllTasks::class.java)
    }

    @TypeConverter
    fun fromTaskMember(data: TaskV2Response.AllTasks): String {
        return Gson().toJson(data)
    }
}
