package com.zstronics.ceibro.data.database.converters.v2

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zstronics.ceibro.data.repos.task.models.TopicsResponse

class TopicDataListTypeConverter {

    @TypeConverter
    fun fromString(value: String): List<TopicsResponse.TopicData> {
        val type = object : TypeToken<List<TopicsResponse.TopicData>>() {}.type
        return Gson().fromJson(value, type)
    }

    @TypeConverter
    fun fromList(list: List<TopicsResponse.TopicData>): String {
        return Gson().toJson(list)
    }
}
