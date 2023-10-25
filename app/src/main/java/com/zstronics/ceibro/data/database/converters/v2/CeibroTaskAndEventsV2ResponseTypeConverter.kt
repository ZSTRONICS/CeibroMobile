package com.zstronics.ceibro.data.database.converters.v2

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.zstronics.ceibro.data.repos.task.models.TaskV2Response
import com.zstronics.ceibro.data.repos.task.models.v2.AllTasksV2NewResponse

class CeibroTaskAndEventsV2ResponseTypeConverter {

    @TypeConverter
    fun fromString(value: String): AllTasksV2NewResponse {
        return Gson().fromJson(value, AllTasksV2NewResponse::class.java)
    }

    @TypeConverter
    fun fromTaskMember(data: AllTasksV2NewResponse): String {
        return Gson().toJson(data)
    }
}
