package com.zstronics.ceibro.data.database.converters.v2

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.zstronics.ceibro.data.repos.task.models.v2.AllTasksV2NewResponse

class CeibroTaskAndEventsV2TypeConverter {

    @TypeConverter
    fun fromString(value: String): AllTasksV2NewResponse.NewData {
        return Gson().fromJson(value, AllTasksV2NewResponse.NewData::class.java)
    }

    @TypeConverter
    fun fromTaskMember(data: AllTasksV2NewResponse.NewData): String {
        return Gson().toJson(data)
    }
}
