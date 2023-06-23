package com.zstronics.ceibro.data.database.converters.v2

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.zstronics.ceibro.data.repos.task.models.TaskV2Response
import com.zstronics.ceibro.data.repos.task.models.TopicsResponse

class TopicsResponseTypeConverter {

    @TypeConverter
    fun fromString(value: String): TopicsResponse {
        return Gson().fromJson(value, TopicsResponse::class.java)
    }

    @TypeConverter
    fun fromTaskMember(data: TopicsResponse): String {
        return Gson().toJson(data)
    }
}
