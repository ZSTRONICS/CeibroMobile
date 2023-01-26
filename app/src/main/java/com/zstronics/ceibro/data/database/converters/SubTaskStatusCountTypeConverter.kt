package com.zstronics.ceibro.data.database.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.zstronics.ceibro.data.database.models.tasks.SubTaskStatusCount

class SubTaskStatusCountTypeConverter {

    @TypeConverter
    fun fromString(value: String): SubTaskStatusCount? {
        return Gson().fromJson(value, SubTaskStatusCount::class.java)
    }

    @TypeConverter
    fun fromTaskMember(subTaskStatusCount: SubTaskStatusCount?): String {
        return Gson().toJson(subTaskStatusCount)
    }
}
